package com.ventsea.communication;

import android.content.Context;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.ventsea.communication.bean.FileType;
import com.ventsea.communication.bean.TransFileList;
import com.ventsea.communication.utils.Utils;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import io.netty.util.internal.SystemPropertyUtil;

import static org.junit.Assert.*;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {

    private final Pattern INSECURE_URI = Pattern.compile(".*[<>&\"].*");

    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getTargetContext();

        assertEquals("com.ventsea.communication.test", appContext.getPackageName());
    }

    @Test
    public void testUrl() {
//        Uri uri = Uri.parse("http://192.168.49.1:8080/file?dir=root&name=test");
        Uri uri = Uri.parse("/file?dir=root&name=test");
//        Uri uri = Uri.parse("?dir=root&name=test");
        Set<String> names = uri.getQueryParameterNames();
        assertEquals(2, names.size());
        for (String name : names) {
            String parameter = uri.getQueryParameter(name);
            if ("root".equals(name)) {
                assertEquals("root", parameter);
            }
            if ("name".equals(name)) {
                assertEquals("test", parameter);
            }
        }
        String hello = uri.getQueryParameter("hello");
        assertEquals(null, hello);
        List<String> list = uri.getPathSegments();
        Log.e("DB_HAI", list.toString());
        for (String pathSegment : list) {
            Log.e("DB_HAI", pathSegment);
        }
    }

    @Test
    public void testSanitize() {
        // Decode the path.
        String uri = "/.filedir=root&name<=test.";
        try {
            uri = URLDecoder.decode(uri, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new Error(e);
        }

        if (uri.isEmpty() || uri.charAt(0) != '/') {
            Log.d("DB_HAI", "NULL");
        }

        // Convert file separators.
        uri = uri.replace('/', File.separatorChar);

        // Simplistic dumb security check.
        // You will have to do something serious in the production environment.
        if (uri.contains(File.separator + '.') ||
                uri.contains('.' + File.separator) ||
                uri.charAt(0) == '.' || uri.charAt(uri.length() - 1) == '.' || INSECURE_URI.matcher(uri).matches()) {
            Log.d("DE_HAI", "contains NULL");
        }

        Log.d("DB_HAI", uri);
    }

    @Test
    public void testBuildFileList() {
        String test = "12345";
        subTest(test);
        assertEquals("6", test);
    }

    private void subTest(String s) {
        s = "6";
    }

    @Test
    public void testFileBean() {
        TransFileList.TransFile file = new TransFileList.TransFile.Build(FileType.APK, new File("/sdcard/OneKey_V1.0.0_CPFA000_1206_14-53.Debug.apk")).build();
    }

    @Test
    public void testUri() {
        String url = "1234.com/sss/ssss.apk";
        url = url.substring(url.lastIndexOf("/") + 1);
        assertEquals("ssss.apk", url);
    }

    @Test
    public void testGetQueryParameter() throws UnsupportedEncodingException {
        String test = "/file?dir=%2Fstorage%2Femulated%2F0%2FDownload%2F%E5%BF%AB%E7%89%99.apk%26name%3D%E5%BF%AB%E7%89%99.apk%26ft%3Dapplication%2Fvnd.android.package-archive";
        String t1 = URLDecoder.decode(test, "UTF-8");
        String t2 = URLDecoder.decode(t1, "UTF-8");
        Uri parse1 = Uri.parse(t1);
        Uri parse2 = Uri.parse(t2);
        String name1 = parse1.getQueryParameter("name");
        String name2 = parse2.getQueryParameter("ft");
        assertEquals("快牙.apk", name1);
        assertEquals(name2, name1);
    }

    @Test
    public void testDecodeTwo() {
        String test = "/file?dir=%2Fstorage%2Femulated%2F0%2FDownload%2F%E5%BF%AB%E7%89%99.apk%26name%3D%E5%BF%AB%E7%89%99.apk%26ft%3Dapplication%2Fvnd.android.package-archive";
        try {
            String t1 = URLDecoder.decode(test, "UTF-8");
            String t2 = URLDecoder.decode(t1, "UTF-8");
            assertEquals(t1, t2);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testDocMimeType() {
        String mimeType = getMimeType("what the fuck.ppt");
        String type = Utils.getMimeType("/storage/emulated/0/PictureSelector/CameraImage/PictureSelector_20181127_161623.JPEG");
        Log.d("DB_TEST", type + "");
    }

    private String getMimeType(String filePath) {
        try {
            filePath = URLEncoder.encode(filePath.trim().replaceAll(" ", ""), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        String ext = MimeTypeMap.getFileExtensionFromUrl(filePath);
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(ext);
    }

    @Test
    public void testAverageAssign()  {
        List<String> test = new ArrayList<>();
        List<List<String>> lists = Utils.averageAssign(test, 5);
        for (int i = 0; i< lists.size(); i++) {
            Log.d("DB_TEST", "" + lists.get(i).size());
            for (String s : lists.get(i)) {
                Log.d("DB_TEST", s);
            }
        }
        Log.d("DB_TEST", lists.size() + "");
    }
}
