package com.github.kilnn.wristband2.sample.dial.custom;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.htsmart.wristband2.dial.DialView;
import com.htsmart.wristband2.dial.DialViewEngine;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class MyDialViewEngine implements DialViewEngine {
    @Override
    public void loadDialBackground(Context context, final DialView view, Uri uri) {
        GlideApp.with(context)
                .asBitmap()
                .load(uri)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        view.setBackgroundBitmap(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        view.clearBackgroundBitmap();
                    }
                });
    }

    @Override
    public void loadDialStyle(Context context, final DialView view, Uri uri) {
        GlideApp.with(context)
                .asBitmap()
                .load(uri)
                .into(new CustomTarget<Bitmap>() {
                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        view.setStyleBitmap(resource);
                    }

                    @Override
                    public void onLoadCleared(@Nullable Drawable placeholder) {
                        view.clearStyleBitmap();
                    }
                });
    }
}
