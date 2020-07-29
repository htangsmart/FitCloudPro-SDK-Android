package com.github.kilnn.wristband2.sample.dial.custom.util;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.github.kilnn.wristband2.sample.util.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;

import androidx.annotation.NonNull;

/**
 * Created by Kilnn on 2017/6/5.
 * 一个简单的文件下载类,下载http或者https链接的文件。
 */

public class SimpleFileDownloader {

    private FilePathGenerator mFilePathGenerator;
    private Listener mListener;
    private DownloadHandler mDownloadHandler;
    private DownloadThread mDownloadThread;
    private Context mContext;

    private int mRequestStorageSpace = 20;//MB
    private boolean mDownloadPost;
    private boolean mNoGzip;

    public SimpleFileDownloader(Context context) {
        mContext = context.getApplicationContext();
        mDownloadHandler = new DownloadHandler(Looper.getMainLooper(), this);
    }

    public SimpleFileDownloader(Context context, boolean downloadPost) {
        mContext = context.getApplicationContext();
        mDownloadPost = downloadPost;
        mDownloadHandler = new DownloadHandler(Looper.getMainLooper(), this);
    }

    public void setFilePathGenerator(FilePathGenerator generator) {
        mFilePathGenerator = generator;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void setRequestStorageSpace(int mb) {
        mRequestStorageSpace = mb;
    }

    public void setNoGzip() {
        mNoGzip = true;
    }

    //文件存储在application-specific目录，不需要额外的访问权限
    public void start(String url) {
        if (TextUtils.isEmpty(url)
                || (!url.startsWith("http") && !url.startsWith("https"))) {
            mDownloadHandler.sendError(ERROR_URI);
            return;
        }
        if (mFilePathGenerator == null) {
            mFilePathGenerator = new DefaultFilePathGenerator();
        }
        File parentFile = mFilePathGenerator.getFileDir(mContext);
        if (parentFile == null) {
            mDownloadHandler.sendError(ERROR_SD_CARD);
            return;
        }
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            mDownloadHandler.sendError(ERROR_FILE);
            return;
        }
        double availableSize = Utils.getAvailableSpace(parentFile);
        if (availableSize < mRequestStorageSpace) {
            mDownloadHandler.sendError(ERROR_INSUFFICIENT_SPACE);
            return;
        }
        File file = new File(parentFile, mFilePathGenerator.getFileName(mContext, url));
        if (file.exists()) {
            mDownloadHandler.sendCompleted(file.getAbsolutePath());
            return;
        }

        cancel();
        mDownloadThread = new DownloadThread(mDownloadHandler, url, file.getAbsolutePath(), mDownloadPost, mNoGzip);
        mDownloadThread.start();
    }

    public void cancel() {
        if (mDownloadThread != null && mDownloadThread.isAlive()) {
            mDownloadThread.close();
        }
    }

    public void release() {
        mListener = null;
        cancel();
    }

    private static class DownloadThread extends Thread {
        private DownloadHandler handler;
        private String downloadUrl;
        private String filePath;
        private int progress;
        private volatile boolean interceptFlag;
        private boolean downloadPost;
        private boolean noGzip;

        private DownloadThread(DownloadHandler handler, String downloadUrl, String filePath
                , boolean downloadPost, boolean noGzip) {
            this.handler = handler;
            this.downloadUrl = downloadUrl;
            this.filePath = filePath;
            this.downloadPost = downloadPost;
            this.noGzip = noGzip;
        }

        @Override
        public void run() {
            HttpURLConnection conn = null;
            try {
                URL url = new URL(downloadUrl);
                conn = (HttpURLConnection) url.openConnection();
                if (downloadPost) {
                    conn.setRequestMethod("POST");
                }
                if (noGzip) {
                    conn.setRequestProperty("Accept-Encoding", "identity");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (conn == null) {
                handler.sendError(ERROR_NET_WORK);
                return;
            }

            InputStream is = null;
            FileOutputStream fos = null;

            try {
                is = conn.getInputStream();

                File tempFile = new File(filePath + ".tmp");
                int length = conn.getContentLength();
                fos = new FileOutputStream(tempFile);

                boolean completed = false;
                int count = 0;
                byte buffer[] = new byte[1024];
                do {
                    int numRead = is.read(buffer);
                    count += numRead;
                    progress = (int) (((float) count / length) * 100);

                    //更新进度
                    if (progress < 0) {
                        handler.sendProgress(-1);
                    } else {
                        handler.sendProgress(progress);
                    }

                    if (numRead <= 0) {
                        completed = true;
                        break;
                    }
                    fos.write(buffer, 0, numRead);
                } while (!interceptFlag);//点击取消就停止下载.

                if (completed) {
                    completed = tempFile.renameTo(new File(filePath));
                }
                if (completed) {
                    handler.sendCompleted(filePath);
                } else {
                    if (interceptFlag) {
                        handler.sendCanceled();
                    } else {
                        handler.sendError(ERROR_UNKNOWN);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
                handler.sendError(ERROR_UNKNOWN);
            } finally {
                conn.disconnect();
                try {
                    if (is != null) is.close();
                    if (fos != null) fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        private void close() {
            interceptFlag = true;
            interrupt();
        }

    }

    private static class DownloadHandler extends Handler {
        static final int MSG_PROGRESS = 1;//进度变化
        static final int MSG_COMPLETED = 2;//完成
        static final int MSG_ERROR = 3;//错误
        static final int MSG_CANCELED = 4;//取消

        private WeakReference<SimpleFileDownloader> reference;

        private DownloadHandler(Looper looper, SimpleFileDownloader downloader) {
            super(looper);
            reference = new WeakReference<>(downloader);
        }

        private void sendProgress(int progress) {
            Message message = obtainMessage(MSG_PROGRESS);
            message.arg1 = progress;
            message.sendToTarget();
        }

        private void sendCompleted(String filePath) {
            Message message = obtainMessage(MSG_COMPLETED);
            message.obj = filePath;
            message.sendToTarget();
        }

        private void sendError(int errorCode) {
            Message message = obtainMessage(MSG_ERROR);
            message.arg1 = errorCode;
            message.sendToTarget();
        }

        private void sendCanceled() {
            sendEmptyMessage(MSG_CANCELED);
        }

        @Override
        public void handleMessage(Message msg) {
            SimpleFileDownloader manager = reference.get();
            if (manager == null || manager.mListener == null) return;
            switch (msg.what) {
                case MSG_PROGRESS:
                    manager.mListener.onProgress(msg.arg1);
                    break;
                case MSG_COMPLETED:
                    manager.mListener.onCompleted((String) msg.obj);
                    break;
                case MSG_ERROR:
                    manager.mListener.onError(msg.arg1);
                    break;
                case MSG_CANCELED:
                    manager.mListener.onCanceled();
                    break;
            }
        }
    }

    public static final int ERROR_URI = 1001;//URL错误
    public static final int ERROR_SD_CARD = 1002;//SD卡不存在
    public static final int ERROR_FILE = 1003;//文件错误
    public static final int ERROR_NET_WORK = 1004;//网络错误
    public static final int ERROR_INSUFFICIENT_SPACE = 1005;//存储空间不足
    public static final int ERROR_UNKNOWN = 1006;//未知错误

    public interface Listener {

        /**
         * 正常情况下是从0到100.如果为-1，表示未知的文件大小，不能计算进度，但是文件可以正常下载
         *
         * @param progress
         */
        void onProgress(int progress);//下载进度改变

        void onCompleted(@NonNull String filePath);//完成

        void onCanceled();//取消

        void onError(int errorCode);//发生错误
    }

    public static class SimpleListener implements Listener {

        @Override
        public void onProgress(int progress) {

        }

        @Override
        public void onCompleted(@NonNull String filePath) {

        }

        @Override
        public void onCanceled() {

        }

        @Override
        public void onError(int errorCode) {

        }
    }

}
