package com.st.letter.lib.media;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import static com.st.letter.lib.media.URLConstant.COLON;
import static com.st.letter.lib.media.URLConstant.FILE;
import static com.st.letter.lib.media.URLConstant.HTTP;
import static com.st.letter.lib.media.URLConstant.THUMB_VIDEO;

public class LocalVideo {

    private static final String TAG = "LocalVideo";
    //表示操作的表  Uri.parse("content://media/external/video/media");
    private static final Uri URI = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

    private static final String[] INFO = new String[]{
            String.valueOf(MediaStore.Video.Media._ID),
            MediaStore.Video.Media.TITLE,
            MediaStore.Video.Media.ALBUM,
            MediaStore.Video.Media.ARTIST,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DATA
    };

    private static final String sortOrder = MediaStore.Video.Media.DEFAULT_SORT_ORDER;

    public static List<Video> scanAllVideo(Context context) {
        List<Video> videos = new ArrayList<>();
        ContentResolver resolver = context.getApplicationContext().getContentResolver();
        Cursor cursor = resolver.query(URI, INFO, null, null, sortOrder);
        Log.d(TAG, "resolver query");
        if (cursor != null) {
            Log.d(TAG, "cursor != null");
            if (cursor.moveToFirst()) {
                Log.d(TAG, "cursor can moveToFirst");
                int idIdx = cursor.getColumnIndex(MediaStore.Video.Media._ID);
                int titleIdx = cursor.getColumnIndex(MediaStore.Video.Media.TITLE);
                int durationIdx = cursor.getColumnIndex(MediaStore.Video.Media.DURATION);
                int sizeIdx = cursor.getColumnIndex(MediaStore.Video.Media.SIZE);
                int dataIdx = cursor.getColumnIndex(MediaStore.Video.Media.DATA);
                do {
                    Video video = new Video();
                    video.id = cursor.getInt(idIdx);
                    video.title = cursor.getString(titleIdx);
                    video.duration = cursor.getInt(durationIdx);
                    video.size = cursor.getLong(sizeIdx);
                    video.data = cursor.getString(dataIdx);
                    video.type = video.getMimeType(video.data);
                    videos.add(video);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return videos;
    }

    public static class Video extends LocalDB {
        public int id;
        public String title;
        public String iconUrl;
        public int duration;
        public long size;
        public String type;

        public Video() {

        }

        @Override
        public String toString() {
            return "Video{" +
                    "id=" + id +
                    ", title='" + title + '\'' +
                    ", iconUrl='" + iconUrl + '\'' +
                    ", duration=" + duration +
                    ", size=" + size +
                    ", type=" + type +
                    ", data='" + data + '\'' +
                    '}';
        }

        @Override
        public void buildCorrectFileBean(String ip, int port) {
            try {
                StringBuilder sb = new StringBuilder();
                if (data != null) {
                    iconUrl = sb.append(HTTP).append(ip).append(COLON).append(port).append(THUMB_VIDEO).append(URLEncoder.encode(data, "UTF-8")).toString();
                    sb.delete(0, sb.length()); //build dir 完成， 清除，重新build data
                    data = sb.append(HTTP).append(ip).append(COLON).append(port).append(FILE).append(URLEncoder.encode(data, "UTF-8")).toString();
                }
            } catch (Exception e) {
                Log.e("TransFile", "buildCorrectFileBean error", e);
            }
        }
    }
}
