package com.github.kilnn.wristband2.sample.file;

import android.content.Context;

import androidx.annotation.NonNull;

import java.io.File;

/**
 * Created by Kilnn on 2017/6/5.
 */

public interface FilePathGenerator {
    File getFileDir(Context context);

    @NonNull
    String getFileName(Context context, @NonNull String url);
}
