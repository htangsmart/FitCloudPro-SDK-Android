package com.github.kilnn.wristband2.sample.dial.custom;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.htsmart.wristband2.dial.DialDrawer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class DialGridData {

    private Uri backgroundUri;
    private Uri styleUri;
    private DialDrawer.Position position;

    public DialGridData(Uri backgroundUri, Uri styleUri, DialDrawer.Position position) {
        this.backgroundUri = backgroundUri;
        this.styleUri = styleUri;
        this.position = position;
    }

    public Uri getBackgroundUri() {
        return backgroundUri;
    }

    public void setBackgroundUri(Uri backgroundUri) {
        this.backgroundUri = backgroundUri;
    }

    public Uri getStyleUri() {
        return styleUri;
    }

    public void setStyleUri(Uri styleUri) {
        this.styleUri = styleUri;
    }

    public DialDrawer.Position getPosition() {
        return position;
    }

    public void setPosition(DialDrawer.Position position) {
        this.position = position;
    }

    @Nullable
    public static List<Uri> loadBackgrounds(Context context, DialDrawer.Shape shape) {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (dir == null) return null;
        dir = new File(dir, getDirNameFromShape(shape));
        if (!dir.exists()) return null;
        File[] allFiles = dir.listFiles();
        if (allFiles == null) return null;
        List<Uri> uriList = new ArrayList<>(allFiles.length);
        for (File file : allFiles) {
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".jpg")
                    || fileName.endsWith(".jpeg")) {
                uriList.add(Uri.fromFile(file));
            }
        }
        return uriList;
    }

    @NonNull
    public static List<DialDrawer.Position> loadPositions() {
        List<DialDrawer.Position> list = new ArrayList<>();
        list.add(DialDrawer.Position.BOTTOM);
        list.add(DialDrawer.Position.TOP);
        list.add(DialDrawer.Position.LEFT);
        list.add(DialDrawer.Position.RIGHT);
        return list;
    }

    private static String getDirNameFromShape(@NonNull DialDrawer.Shape shape) {
        if (shape.isShapeCircle()) {
            return "circle_" + shape.width() + "_" + shape.height();
        } else {
            return "rect_" + shape.width() + "_" + shape.height();
        }
    }

    public static Uri getOutputBackgroundUri(Context context, DialDrawer.Shape shape) {
        File dir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        if (dir == null) return null;
        dir = new File(dir, getDirNameFromShape(shape));
        String fileName = UUID.randomUUID().toString().replace("-", "") + ".jpg";
        File file = new File(dir, fileName);
        if (!file.getParentFile().exists()) {//父目录不存在
            if (!file.getParentFile().mkdirs()) {
                return null;
            }
        }
        return Uri.fromFile(file);
    }

    public static Intent getCropIntent(Uri originalFileUri, Uri outputFileUri, DialDrawer.Shape shape) {
        Intent intent = new Intent("com.android.camera.action.CROP");
        intent.setDataAndType(originalFileUri, "image/*");
        intent.putExtra("crop", "true");
        intent.putExtra("aspectX", shape.width());
        intent.putExtra("aspectY", shape.height());
        intent.putExtra("outputX", shape.width());
        intent.putExtra("outputY", shape.height());

        intent.putExtra("scale", true);
        intent.putExtra("scaleUpIfNeeded", true); // 部分机型没有设置该参数截图会有黑边
        intent.putExtra("return-data", false);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
        intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());
        // 不启用人脸识别
        intent.putExtra("noFaceDetection", false);
        return intent;
    }

}
