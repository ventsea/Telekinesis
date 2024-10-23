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
import static com.st.letter.lib.media.URLConstant.THUMB_IMG;

public class LocalImages {

    private static final String TAG = "LocalImages";
    //表示操作的表  Uri.parse("content://media/external/images/media");
    private static final Uri URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    private static final String[] INFO = new String[]{
            String.valueOf(MediaStore.Images.Media._ID),
            MediaStore.Images.Media.TITLE,
            MediaStore.Images.Media.SIZE,
            MediaStore.Images.Media.DATA
    };

    private static final String sortOrder = MediaStore.Images.Media.DEFAULT_SORT_ORDER;

    public static List<Image> scanAllImages(Context context) {
        List<Image> images = new ArrayList<>();
        ContentResolver resolver = context.getApplicationContext().getContentResolver();
        Cursor cursor = resolver.query(URI, INFO, null, null, sortOrder);
        Log.d(TAG, "resolver query");
        if (cursor != null) {
            Log.d(TAG, "cursor != null");
            if (cursor.moveToFirst()) {
                Log.d(TAG, "cursor can moveToFirst");
                int idIdx = cursor.getColumnIndex(MediaStore.Images.Media._ID);
                int titleIdx = cursor.getColumnIndex(MediaStore.Images.Media.TITLE);
                int sizeIdx = cursor.getColumnIndex(MediaStore.Images.Media.SIZE);
                int dataIdx = cursor.getColumnIndex(MediaStore.Images.Media.DATA);
                do {
                    Image image = new Image();
                    image.id = cursor.getInt(idIdx);
                    image.title = cursor.getString(titleIdx);
                    image.size = cursor.getLong(sizeIdx);
                    image.data = cursor.getString(dataIdx);
                    image.type = image.getMimeType(image.data);
                    images.add(image);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return images;
    }


    public static class Image extends LocalDB {
        public int id;
        public String title;
        public String iconUrl;
        public long size;
        public String type;

        public Image() {

        }

        @Override
        public String toString() {
            return "Image{" +
                    "id=" + id +
                    ", title='" + title + '\'' +
                    ", iconUrl='" + iconUrl + '\'' +
                    ", size='" + size + '\'' +
                    ", type='" + type + '\'' +
                    ", data='" + data + '\'' +
                    '}';
        }

        @Override
        public void buildCorrectFileBean(String ip, int port) {
            try {
                StringBuilder sb = new StringBuilder();
                if (data != null) {
                    iconUrl = sb.append(HTTP).append(ip).append(COLON).append(port).append(THUMB_IMG).append(URLEncoder.encode(data, "UTF-8")).toString();
                    sb.delete(0, sb.length()); //build dir 完成， 清除，重新build data
                    data = sb.append(HTTP).append(ip).append(COLON).append(port).append(FILE).append(URLEncoder.encode(data, "UTF-8")).toString();
                }
            } catch (Exception e) {
                Log.e("TransFile", "buildCorrectFileBean error", e);
            }
        }
    }
}
