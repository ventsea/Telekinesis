package com.st.letter.lib.media;

import android.util.Log;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import static com.st.letter.lib.media.URLConstant.COLON;
import static com.st.letter.lib.media.URLConstant.FILE;
import static com.st.letter.lib.media.URLConstant.HTTP;

public class LocalDocs {

    private static final String TAG = "LocalDocs";

    private static final List<String> DOC_TYPE = new ArrayList<>();

    static {
        DOC_TYPE.add(".doc");
        DOC_TYPE.add(".docx");
        DOC_TYPE.add(".xls");
        DOC_TYPE.add(".xlsx");
        DOC_TYPE.add(".ppt");
        DOC_TYPE.add(".wps");
        DOC_TYPE.add(".pdf");
        DOC_TYPE.add(".txt");
        DOC_TYPE.add(".ebk");
        DOC_TYPE.add(".umd");
        DOC_TYPE.add(".chm");
    }

    private ScanDocsResultListener mListener;

    private boolean mScanHidden;
    private int mCoreSize;

    private ThreadPoolExecutor mScanExecutor;
    private ConcurrentLinkedQueue<File> mScanDirectorQueue;
    private ConcurrentLinkedQueue<Doc> mScannedFiles;
    private List<Doc> mResult;
    private List<DocScan> mRunnableList;

    public LocalDocs(boolean scanHidden) {
        mScanHidden = scanHidden;
        mCoreSize = Runtime.getRuntime().availableProcessors();
        mScanExecutor = new ThreadPoolExecutor(mCoreSize, mCoreSize, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
        mScanDirectorQueue = new ConcurrentLinkedQueue<>();
        mScannedFiles = new ConcurrentLinkedQueue<>();
        mResult = new ArrayList<>();
    }

    public void scanAllDocs(File scanFile, ScanDocsResultListener resultListener) {
        if (!scanFile.exists() || !scanFile.isDirectory()) {
            resultListener.onFinish(mResult);
            return;
        }
        if (mListener == null) mListener = resultListener;

        File[] firstScanFiles = scanFile.listFiles(new DocFilter(mScanHidden));

        if (firstScanFiles == null || firstScanFiles.length <= 0) {
            resultListener.onFinish(mResult);
            return;
        }

        mRunnableList = new ArrayList<>();
        for (File file : firstScanFiles) {
            if (file.isDirectory()) {
                mScanDirectorQueue.offer(file);
                mRunnableList.add(new DocScan(mScanHidden, mScanDirectorQueue, mScannedFiles));
            } else {
                if (checkContainsDoc(file.getName())) {
                    Doc doc = new Doc();
                    doc.title = file.getName();
                    doc.size = file.length();
                    doc.data = file.getAbsolutePath();
                    mScannedFiles.offer(doc);
                }
            }
        }

        if (mRunnableList.isEmpty()) {
            resultListener.onFinish(mResult);
            return;
        }

        while (mRunnableList.size() < mCoreSize) {
            mRunnableList.add(new DocScan(mScanHidden, mScanDirectorQueue, mScannedFiles));
        }

        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                for (Runnable runnable : mRunnableList) {
                    mScanExecutor.execute(runnable);
                }

                mScanExecutor.shutdown();

                try {
                    mScanExecutor.awaitTermination(Long.MAX_VALUE, TimeUnit.SECONDS);
                } catch (InterruptedException e) {
                    Log.e(TAG, "awaitTermination error " + e.getMessage());
                }

                if (mListener != null)
                    mListener.onFinish(new ArrayList<>(mScannedFiles));
            }
        });
    }

    public void stopScan() {
        if (mRunnableList != null && !mRunnableList.isEmpty()) {
            for (DocScan runnable : mRunnableList) {
                runnable.stop();
            }
        }
    }

    private static class DocScan implements Runnable {

        private boolean scanHidden;
        private ConcurrentLinkedQueue<File> scanDirectorQueue;
        private ConcurrentLinkedQueue<Doc> scannedFiles;
        private boolean stop;

        private DocScan(boolean scanHidden, ConcurrentLinkedQueue<File> scanDirector, ConcurrentLinkedQueue<Doc> scanFile) {
            this.scanHidden = scanHidden;
            scanDirectorQueue = scanDirector;
            scannedFiles = scanFile;
        }

        private void stop() {
            stop = true;
        }

        @Override
        public void run() {
            while (!scanDirectorQueue.isEmpty()) {
                File scanDir = scanDirectorQueue.poll();
                if (stop) break;
                if (scanDir == null) continue;
                File[] files = scanDir.listFiles(new DocFilter(scanHidden));
                if (files == null) continue;
                for (File file : files) {
                    if (file.isDirectory()) {
                        scanDirectorQueue.offer(file);
                    } else {
                        if (checkContainsDoc(file.getName())) {
                            Doc doc = new Doc();
                            doc.title = file.getName();
                            doc.size = file.length();
                            doc.data = file.getAbsolutePath();
                            scannedFiles.offer(doc);
                        }
                    }
                }
            }
        }
    }

    private static class DocFilter implements FilenameFilter {

        private boolean scanHidden;

        private DocFilter(boolean scanHidden) {
            this.scanHidden = scanHidden;
        }

        @Override
        public boolean accept(File dir, String name) {
            return scanHidden || !name.startsWith(".");
        }
    }

    public static boolean checkContainsDoc(String filePath) {
        filePath = filePath.toLowerCase();
        if (filePath.contains(".")) {
            String type = filePath.substring(filePath.lastIndexOf("."));
            return DOC_TYPE.contains(type);
        }
        return false;
    }

    public interface ScanDocsResultListener {
        void onFinish(List<Doc> files);
    }

    public static class Doc extends LocalDB {
        public String title;
        public long size;

        public Doc() {

        }

        @Override
        public String toString() {
            return "Doc{" +
                    "title='" + title + '\'' +
                    ", size=" + size +
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
