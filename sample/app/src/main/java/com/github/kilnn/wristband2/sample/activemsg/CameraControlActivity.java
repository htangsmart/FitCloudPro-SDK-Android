package com.github.kilnn.wristband2.sample.activemsg;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.OrientationEventListener;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.github.kilnn.wristband2.sample.BaseActivity;
import com.github.kilnn.wristband2.sample.R;
import com.htsmart.wristband2.WristbandApplication;
import com.htsmart.wristband2.WristbandManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;
import java.util.List;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.Consumer;

@SuppressWarnings("ResultOfMethodCallIgnored")
@SuppressLint("CheckResult")
public class CameraControlActivity extends BaseActivity {

    Camera camera;
    Button snap;
    Button switchCamera;

    SurfaceView surfaceView;
    int camera_id = 0;
    IOrientationEventListener iOriListener;

    final int SUCCESS = 233;
    SnapHandler handler = new SnapHandler();

    int camera_direction = CameraInfo.CAMERA_FACING_BACK;

    private WristbandManager mWristbandManager = WristbandApplication.getWristbandManager();
    private Disposable mDisposable;

    public void switchCamera() {
        if (camera_direction == CameraInfo.CAMERA_FACING_BACK) {
            camera_direction = CameraInfo.CAMERA_FACING_FRONT;
        } else {
            camera_direction = CameraInfo.CAMERA_FACING_BACK;
        }
        int mNumberOfCameras = Camera.getNumberOfCameras();
        CameraInfo cameraInfo = new CameraInfo();
        for (int i = 0; i < mNumberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == camera_direction) {
                camera_id = i;
            }
        }
        if (null != camera) {
            camera.stopPreview();
            camera.release();
        }
        camera = Camera.open(camera_id);
        try {
            camera.setPreviewDisplay(surfaceView.getHolder());
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

        setCameraAndDisplay(surfaceView.getWidth(), surfaceView.getHeight());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_camera_control);

        surfaceView = this.findViewById(R.id.surfaceView);
        switchCamera = this.findViewById(R.id.switch_btn);
        switchCamera.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                switchCamera();
            }

        });
        snap = this.findViewById(R.id.snap);
        snap.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                takePhoto();
            }

        });


        surfaceView.getHolder().setKeepScreenOn(true);
        surfaceView.getHolder().addCallback(new Callback() {

            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                int mNumberOfCameras = Camera.getNumberOfCameras();
                // Find the ID of the default camera
                CameraInfo cameraInfo = new CameraInfo();
                for (int i = 0; i < mNumberOfCameras; i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == CameraInfo.CAMERA_FACING_BACK) {
                        camera_id = i;
                    }
                }
                camera = Camera.open(camera_id);
                try {
                    camera.setPreviewDisplay(holder);
                    camera.startPreview();

                    iOriListener.enable();

                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                setCameraAndDisplay(width, height);

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                if (null != camera) {
                    camera.release();
                    camera = null;
                }

            }

        });
        iOriListener = new IOrientationEventListener(this);

        mDisposable = mWristbandManager.observerWristbandMessage()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Integer>() {
                    @Override
                    public void accept(Integer integer) throws Exception {
                        if (integer == WristbandManager.MSG_CAMERA_TAKE_PHOTO) {
                            takePhoto();
                        }
                    }
                });
    }


    @Override
    protected void onStart() {
        super.onStart();
        mWristbandManager.setCameraStatus(true)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        toast(R.string.operation_success);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e("sample", "", throwable);
                        toast(R.string.operation_failed);
                    }
                });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mWristbandManager.setCameraStatus(false)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action() {
                    @Override
                    public void run() throws Exception {
                        toast(R.string.operation_success);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.e("sample", "", throwable);
                        toast(R.string.operation_failed);
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        this.iOriListener.disable();
        mDisposable.dispose();
    }

    private void takePhoto() {
        camera.takePicture(null, null, new PictureCallback() {
            @Override
            public void onPictureTaken(byte[] data, Camera camera) {
                final byte[] tempdata = data;
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        File dir = new File(Environment.getExternalStorageDirectory(), "cameraTest");
                        if (!dir.exists()) {
                            dir.mkdirs();
                        }
                        String name = DateFormat.format("yyyyMMdd_hhmmss", Calendar.getInstance()) + ".jpg";
                        File f = new File(dir, name);
                        if (!f.exists()) {
                            try {
                                f.createNewFile();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        FileOutputStream outputStream;
                        try {
                            outputStream = new FileOutputStream(f);
                            outputStream.write(tempdata);
                            outputStream.close();
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        handler.sendEmptyMessage(SUCCESS);
                    }

                });
                thread.start();
            }

        });
    }

    public class IOrientationEventListener extends OrientationEventListener {

        public IOrientationEventListener(Context context) {
            super(context);
        }


        @Override
        public void onOrientationChanged(int orientation) {
            if (ORIENTATION_UNKNOWN == orientation) {
                return;
            }
            CameraInfo info = new CameraInfo();
            Camera.getCameraInfo(camera_id, info);
            orientation = (orientation + 45) / 90 * 90;
            int rotation = 0;
            if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
                rotation = (info.orientation - orientation + 360) % 360;
            } else {
                rotation = (info.orientation + orientation) % 360;
            }
            if (null != camera) {
                Camera.Parameters parameters = camera.getParameters();
                parameters.setRotation(rotation);
                camera.setParameters(parameters);
            }
        }

    }

    public void setCameraAndDisplay(int width, int height) {
        Camera.Parameters parameters = camera.getParameters();
        List<Size> pictureSizeList = parameters.getSupportedPictureSizes();
        Size picSize = CameraUtils.getProperSize(pictureSizeList, ((float) width) / height);
        if (null != picSize) {
            parameters.setPictureSize(picSize.width, picSize.height);
        } else {
            picSize = parameters.getPictureSize();
        }
        List<Size> previewSizeList = parameters.getSupportedPreviewSizes();
        Size preSize = CameraUtils.getProperSize(previewSizeList, ((float) width) / height);
        if (null != preSize) {
            Log.v("TestCameraActivityTag", preSize.width + "," + preSize.height);
            parameters.setPreviewSize(preSize.width, preSize.height);
        }

        float w = picSize.width;
        float h = picSize.height;
        surfaceView.setLayoutParams(new RelativeLayout.LayoutParams((int) (height * (w / h)), height));

        parameters.setJpegQuality(100);

        if (parameters.getSupportedFocusModes().contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE)) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }
        camera.cancelAutoFocus();
        camera.setDisplayOrientation(0);
        camera.setParameters(parameters);
    }

    class SnapHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == SUCCESS) {
                Toast.makeText(CameraControlActivity.this, "Picture save to cameraTest dir!", Toast.LENGTH_SHORT).show();
            }
            try {
                camera.setPreviewDisplay(surfaceView.getHolder());
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
