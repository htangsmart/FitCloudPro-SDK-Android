package com.github.kilnn.wristband2.sample.dial.custom.util;

import android.content.Context;
import android.text.TextUtils;

import java.io.File;

import androidx.annotation.Nullable;

/**
 * 其实和SimpleFileDownloader实现一模一样，只是为了逻辑更清楚，专门建一个类用于表单下载
 */
public class DialDownloader extends SimpleFileDownloader {

    private static DefaultFilePathGenerator PATH_GENERATOR = new DefaultFilePathGenerator();

    public DialDownloader(Context context) {
        super(context);
        setFilePathGenerator(PATH_GENERATOR);
        setNoGzip();
    }

    public DialDownloader(Context context, boolean downloadPost) {
        super(context, downloadPost);
        setFilePathGenerator(PATH_GENERATOR);
        setNoGzip();
    }

    private static File sCacheFileDir;

    @Nullable
    public static File getFileDir(Context context) {
        if (sCacheFileDir != null) return sCacheFileDir;
        return sCacheFileDir = PATH_GENERATOR.getFileDir(context);
    }

    @Nullable
    public static String getFileName(Context context, String url) {
        if (TextUtils.isEmpty(url)) return null;
        return PATH_GENERATOR.getFileName(context, url);
    }

    @Nullable
    public static File getFileByUrl(Context context, String url) {
        File dir = DialDownloader.getFileDir(context);
        if (dir != null) {
            String fileName = DialDownloader.getFileName(context, url);
            if (!TextUtils.isEmpty(fileName)) {
                return new File(dir, fileName);
            }
        }
        return null;
    }

    public static class DialDownloadException extends Exception {
        private int errorCode;

        public DialDownloadException(int errorCode) {
            this.errorCode = errorCode;
        }

        public int getErrorCode() {
            return errorCode;
        }
    }
}
