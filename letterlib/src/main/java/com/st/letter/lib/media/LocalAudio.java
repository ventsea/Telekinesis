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

public class LocalAudio {

    private static final String TAG = "LocalAudio";
    //表示操作的表  Uri.parse("content://media/external/audio/media");
    private static final Uri URI = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    private static final String[] INFO = new String[]{
            String.valueOf(MediaStore.Audio.Media._ID),
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.DATA
    };

//    private static final String selection = "mime_type in ('audio/mpeg','audio/x-ms-wma') and is_music>0 ";

    private static final String sortOrder = MediaStore.Audio.Media.DEFAULT_SORT_ORDER;

    public static List<Audio> scanAllAudio(Context context) {
        List<Audio> audios = new ArrayList<>();
        ContentResolver resolver = context.getApplicationContext().getContentResolver();
        Cursor cursor = resolver.query(URI, INFO, null, null, sortOrder);
        Log.d(TAG, "resolver query");
        if (cursor != null) {
            Log.d(TAG, "cursor != null");
            if (cursor.moveToFirst()) {
                Log.d(TAG, "cursor can moveToFirst");
                int idIdx = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                int titleIdx = cursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
                int artistIdx = cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
                int durationIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DURATION);
                int sizeIdx = cursor.getColumnIndex(MediaStore.Audio.Media.SIZE);
                int dataIdx = cursor.getColumnIndex(MediaStore.Audio.Media.DATA);
                do {
                    Audio audio = new Audio();
                    audio.id = cursor.getInt(idIdx);
                    audio.title = cursor.getString(titleIdx);
                    audio.artist = cursor.getString(artistIdx);
                    audio.duration = cursor.getInt(durationIdx);
                    audio.size = cursor.getLong(sizeIdx);
                    audio.data = cursor.getString(dataIdx);
                    audio.type = audio.getMimeType(audio.data);
                    audios.add(audio);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return audios;
    }

    public static class Audio extends LocalDB {
        public int id;
        public String title;
        public String artist;
        public int duration;
        public long size;
        public String type;

        public Audio() {

        }

        @Override
        public String toString() {
            return "Audio{" +
                    "id=" + id +
                    ", title='" + title + '\'' +
                    ", artist='" + artist + '\'' +
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
                data = sb.append(HTTP).append(ip).append(COLON).append(port).append(FILE).append(URLEncoder.encode(data, "UTF-8")).toString();
            } catch (Exception e) {
                Log.e("TransFile", "buildCorrectFileBean error", e);
            }
        }
    }
}
