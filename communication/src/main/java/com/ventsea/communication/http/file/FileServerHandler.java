package com.ventsea.communication.http.file;

import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.ventsea.communication.bean.FileType;
import com.ventsea.communication.utils.Utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URLDecoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.DefaultHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.handler.stream.ChunkedFile;
import io.netty.util.CharsetUtil;

import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.METHOD_NOT_ALLOWED;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpResponseStatus.PARTIAL_CONTENT;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

class FileServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static final String TAG = FileServer.TAG;
    private static final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");
    private static final String HTTP_DATE_FORMAT = "EEE, dd MMM yyyy HH:mm:ss zzz";
    private static final String HTTP_DATE_GMT_TIMEZONE = "GMT";
    private static final int HTTP_CACHE_SECONDS = 60;

    private static final String THUMB = "thumb";
    private static final String FILE = "file";
    private static final String DIR = "dir";
    private static final String APP_ICON = "app_icon";

    private IFileServer.FileServerListener sendFileListener;

    FileServerHandler(IFileServer.FileServerListener listener) {
        sendFileListener = listener;
    }

    void setSendFileListener(IFileServer.FileServerListener listener) {
        sendFileListener = listener;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        //  'http://xxx.xxx.xx.xx:????/file?dir=xxx&name=xxxx'
        if (!request.decoderResult().isSuccess()) {
            sendError(ctx, BAD_REQUEST);
            return;
        }
        if (request.method() != GET) {
            sendError(ctx, METHOD_NOT_ALLOWED);
            return;
        }

        // Decode the path.
        String url = URLDecoder.decode(request.uri(), "UTF-8");
        Log.d(TAG, "request url : " + url); // '/file?dir=xxx&name=xxx'

        Uri uri = Uri.parse(request.uri());
        List<String> segments = uri.getPathSegments();
        if (segments.size() == 1) {
            String s = segments.get(0);
            if (FILE.equals(s)) {
                responseFile(ctx, request, uri, false);
                return;
            }
            if (THUMB.equals(s)) {
                responseFile(ctx, request, uri, true);
                return;
            }
        }

        sendError(ctx, NOT_FOUND);
    }

    private void responseFile(ChannelHandlerContext ctx, FullHttpRequest request, Uri uri, boolean thumb) throws Exception {
        String dir = uri.getQueryParameter(DIR);
        if (dir == null) {
            sendError(ctx, NOT_FOUND);
            return;
        }

        if (sanitizeUri(dir) == null) {//校验有效地址
            sendError(ctx, FORBIDDEN);
            return;
        }

        File file = new File(dir);

        String address = getRemoteAddress(ctx);

        if (file.isHidden() || !file.exists()) {
            sendError(ctx, NOT_FOUND);
            if (sendFileListener != null && !thumb)
                sendFileListener.onSendError(address, uri);
            return;
        }

        if (!file.isFile()) {
            sendError(ctx, FORBIDDEN);
            return;
        }

        if (thumb) {
            String icon = uri.getQueryParameter(APP_ICON);
            if (icon != null) { //APK icon
                File imgThumb = Utils.convertIconThumb(file.getAbsolutePath());
                if (imgThumb != null && imgThumb.exists()) {
                    if (imgThumb.exists()) {
                        file = imgThumb;
                    } else {
                        sendError(ctx, FORBIDDEN);
                        return;
                    }
                }
            } else if (file.length() > 500 * 1024) {
                //图片缩略图
                int type = FileType.getInstance().getFileType(Utils.getMimeType(file.getAbsolutePath()));
                switch (type) {
                    case FileType.IMG:
                        file = Utils.convertImgThumb(file.getAbsolutePath());
                        break;
                    case FileType.VIDEO:
                        file = Utils.convertVideoThumb(file.getAbsolutePath());
                        break;
                    default:
                        sendError(ctx, FORBIDDEN);
                        return;
                }
            }
        }

        RandomAccessFile raf;
        try {
            raf = new RandomAccessFile(file, "r");
        } catch (FileNotFoundException ignore) {
            Log.d(TAG, "RandomAccessFile FileNotFoundException");
            sendError(ctx, NOT_FOUND);
            if (sendFileListener != null && !thumb)
                sendFileListener.onSendError(address, uri);
            return;
        }
        long fileLength = raf.length();
        long offset = 0;

        String range = request.headers().get(HttpHeaderNames.RANGE);
        Log.d(TAG, "Range : " + range + "， fileLength : " + fileLength);
        if (range != null) {
            range = range.replaceAll("bytes=", "");
            if (range.contains(",")) {
                //not support
                sendError(ctx, BAD_REQUEST);
                return;
            } else {
                offset = getOffset(range, fileLength);
                fileLength = getLength(range, fileLength);
                if (fileLength == -1 || offset == -1) {
                    sendError(ctx, BAD_REQUEST);
                    return;
                }
            }
            sendResponse(ctx, request, file, fileLength, PARTIAL_CONTENT); //response 206
            Log.d(TAG, "response 206");
        } else {
            sendResponse(ctx, request, file, fileLength, OK); //response 200
            Log.d(TAG, "response 200");
        }

        if (sendFileListener != null && !thumb)
            sendFileListener.onSendStarted(address, uri);

        // Write the content.
        ChannelFuture sendFileFuture;
        ChannelFuture lastContentFuture;
        try {
            sendFileFuture = ctx.writeAndFlush(new HttpChunkedInput(new ChunkedFile(raf, offset, fileLength, 8192)), ctx.newProgressivePromise()); //ChunkedFile 处理了多1的异常？
            // HttpChunkedInput will write the end marker (LastHttpContent) for us.
            lastContentFuture = sendFileFuture;

            if (sendFileListener != null && !thumb) {
                MyChannelProgressiveFutureListener futureListener = new MyChannelProgressiveFutureListener();
                futureListener.setParameter(address, uri);
                futureListener.setProgressListener(sendFileListener);
                sendFileFuture.addListener(futureListener);
            }

            // Decide whether to close the connection or not.
            if (!HttpUtil.isKeepAlive(request)) {
                // Close the connection when the whole content is written out.
                lastContentFuture.addListener(ChannelFutureListener.CLOSE);
            }
        } catch (IOException e) {
            if (sendFileListener != null)
                sendFileListener.onSendError(address, uri);
        }
    }

    private static String sanitizeUri(String uri) {

        if (uri.isEmpty() || uri.charAt(0) != '/') {
            return null;
        }

        // Convert file separators.
        uri = uri.replace('/', File.separatorChar);

        // Simplistic dumb security check.
        // You will have to do something serious in the production environment.
        if (uri.contains(File.separator + '.') ||
                uri.contains('.' + File.separator) ||
                uri.charAt(0) == '.' || uri.charAt(uri.length() - 1) == '.' || INSECURE_URI.matcher(uri).matches()) {
            return null;
        }

        return uri;
    }

    private String getRemoteAddress(ChannelHandlerContext ctx) {
        String address = ctx.channel().remoteAddress().toString();
        return address.substring(1, address.indexOf(":"));
    }

    private static long getOffset(String range, long fileLength) {
        if (range == null) return 0;
        String[] split = range.split("-");
        if (split.length == 2) {
            return Long.valueOf(split[0]);
        } else if (split.length == 1) {
            if (range.indexOf("-") == 0) {
                return fileLength - Long.valueOf(split[0]);
            } else {
                return Long.valueOf(split[0]);
            }
        } else {
            if (range.equals("0--1")) {
                return 0;
            }
            Log.e(TAG, "getOffset 非法请求");
            return -1;
        }
    }

    private static long getLength(String range, long fileLength) {
        if (range == null) return fileLength;
        String[] split = range.split("-");
        if (split.length == 2) {                                                                // xxx-yyy (包括xxx，和yyy之间的字节)
            fileLength = Long.valueOf(split[1]) - Long.valueOf(split[0]) + 1;
        } else if (split.length == 1) {
            if (range.indexOf("-") == 0) {                                                      // -xxx (最后xxx个字节)
                fileLength = Long.valueOf(split[0]);
            } else {                                                                            // xxx- (包括xxx以后的字节)
                fileLength = fileLength - Long.valueOf(split[0]);
            }
        } else {
            if (range.equals("0--1")) {
                return 0;
            }
            Log.e(TAG, "getLength 非法请求");
            return -1;
        }
        return fileLength;
    }

    private static void sendResponse(ChannelHandlerContext ctx, FullHttpRequest request, File file, long fileLength, HttpResponseStatus status) {
        HttpResponse response = new DefaultHttpResponse(HTTP_1_1, status);
        setContentLength(response, fileLength);
        setAcceptRanges(response);
        setDisposition(response, file);
        setContentTypeHeader(response, file);
        setDateAndCacheHeaders(response, file);
        if (HttpUtil.isKeepAlive(request)) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
        }
        ctx.write(response);
    }

    private static void setContentLength(HttpResponse response, long length) {
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH, length);
    }

    private static void setAcceptRanges(HttpResponse response) {
        response.headers().set("Accept-Ranges", "bytes");
    }

    private static void sendError(ChannelHandlerContext ctx, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HTTP_1_1, status, Unpooled.copiedBuffer("Failure: " + status + "\r\n", CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/plain; charset=UTF-8");

        // Close the connection as soon as the error message is sent.
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    /**
     * Sets the Date and Cache headers for the HTTP Response
     *
     * @param response    HTTP response
     * @param fileToCache file to extract content type
     */
    private static void setDateAndCacheHeaders(HttpResponse response, File fileToCache) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat(HTTP_DATE_FORMAT, Locale.US);
        dateFormatter.setTimeZone(TimeZone.getTimeZone(HTTP_DATE_GMT_TIMEZONE));

        // Date header
        Calendar time = new GregorianCalendar();
        response.headers().set(HttpHeaderNames.DATE, dateFormatter.format(time.getTime()));

        // Add cache headers
        time.add(Calendar.SECOND, HTTP_CACHE_SECONDS);
        response.headers().set(HttpHeaderNames.EXPIRES, dateFormatter.format(time.getTime()));
        response.headers().set(HttpHeaderNames.CACHE_CONTROL, "private, max-age=" + HTTP_CACHE_SECONDS);
        response.headers().set(HttpHeaderNames.LAST_MODIFIED, dateFormatter.format(new Date(fileToCache.lastModified())));
    }

    private static void setDisposition(HttpResponse response, File file) {
        response.headers().set(HttpHeaderNames.CONTENT_DISPOSITION, "attachment; filename=" + file.getName());
    }

    /**
     * Sets the content type header for the HTTP Response
     *
     * @param response HTTP response
     * @param file     file to extract content type
     */
    private static void setContentTypeHeader(HttpResponse response, File file) {
        String name = file.getName();
        String substring = file.getName().substring(name.lastIndexOf(".") + 1, name.length()).toLowerCase();
        String extension;
        if (TextUtils.isEmpty(substring)) {
            extension = "application/octet-stream";
        } else {
            extension = MimeTypeMap.getSingleton().getMimeTypeFromExtension(substring);
            if (extension == null) extension = "application/octet-stream";
        }
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, extension);
    }
}
