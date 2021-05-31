package com.github.kilnn.wristband2.sample.file;

import android.content.Context;
import android.os.Environment;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;


import com.github.kilnn.wristband2.sample.utils.Utils;

import java.io.File;


/**
 * Created by Kilnn on 2017/6/5.
 */

public class DefaultFilePathGenerator implements FilePathGenerator {

    @Override
    public File getFileDir(Context context) {
        //TODO 注意文件目录是否对外公开
        File[] files = ContextCompat.getExternalFilesDirs(context, Environment.DIRECTORY_DOWNLOADS);
        if (files == null || files.length <= 0 || files[0] == null) {
            return null;
        }
        return files[0];
    }

    @NonNull
    @Override
    public String getFileName(Context context, @NonNull String url) {
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("url can't be empty");
        }
        int last_divider = url.lastIndexOf("/");
        String fileOriginalName = url.substring(last_divider + 1, url.length());
        int last_suffix_divider = fileOriginalName.lastIndexOf(".");
        String fileSuffix = "";
        if (last_suffix_divider != -1) {
            fileSuffix = fileOriginalName.substring(last_suffix_divider, fileOriginalName.length());
        }
        return Utils.toMD5(url) + fileSuffix;
    }
}
