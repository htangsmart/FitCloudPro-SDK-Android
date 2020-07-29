package com.github.kilnn.wristband2.sample.dial.custom.util;

import android.content.Context;

import java.io.File;

import androidx.annotation.NonNull;

/**
 * Created by Kilnn on 2017/6/5.
 */

public interface FilePathGenerator {
    File getFileDir(Context context);

    @NonNull
    String getFileName(Context context, @NonNull String url);
}
