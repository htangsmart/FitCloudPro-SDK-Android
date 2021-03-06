package com.github.kilnn.wristband2.sample.dial.custom;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;

import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.github.kilnn.wristband2.sample.R;
import com.github.kilnn.wristband2.sample.dfu.DfuDialogFragment;
import com.github.kilnn.wristband2.sample.dial.custom.util.DialDownloader;
import com.github.kilnn.wristband2.sample.dial.custom.util.SimpleFileDownloader;
import com.htsmart.wristband2.dfu.DfuCallback;
import com.htsmart.wristband2.dfu.DfuManager;
import com.htsmart.wristband2.dial.DialDrawer;
import com.htsmart.wristband2.dial.DialWriter;

import java.io.File;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Cancellable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * 1.下载表盘
 * 2.copy临时文件
 * 3.生成图片
 * 4.修改临时文件
 * 5.升级到手环
 */
public class DialCustomDialogFragment extends AppCompatDialogFragment {

    public static class DialCustomParam implements Parcelable {
        public String binUrl;
        public Uri backgroundUri;
        public Uri styleUri;
        public DialDrawer.Shape shape;
        public DialDrawer.ScaleType scaleType;
        public DialDrawer.Position position;
        public byte binFlag;

        public DialCustomParam() {
        }

        protected DialCustomParam(Parcel in) {
            binUrl = in.readString();
            backgroundUri = in.readParcelable(Uri.class.getClassLoader());
            styleUri = in.readParcelable(Uri.class.getClassLoader());
            int shapeInt = in.readInt();
            int shapeWidth = in.readInt();
            int shapeHeight = in.readInt();
            if (shapeInt == DialDrawer.Shape.SHAPE_CIRCLE) {
                shape = DialDrawer.Shape.createCircle(shapeWidth);
            } else {
                shape = DialDrawer.Shape.createRectangle(shapeWidth, shapeHeight);
            }
            scaleType = DialDrawer.ScaleType.fromId(in.readInt());
            position = DialDrawer.Position.fromId(in.readInt());
            binFlag = in.readByte();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(binUrl);
            dest.writeParcelable(backgroundUri, flags);
            dest.writeParcelable(styleUri, flags);
            dest.writeInt(shape.shape());
            dest.writeInt(shape.width());
            dest.writeInt(shape.height());
            dest.writeInt(scaleType.getId());
            dest.writeInt(position.getId());
            dest.writeByte(binFlag);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<DialCustomParam> CREATOR = new Creator<DialCustomParam>() {
            @Override
            public DialCustomParam createFromParcel(Parcel in) {
                return new DialCustomParam(in);
            }

            @Override
            public DialCustomParam[] newArray(int size) {
                return new DialCustomParam[size];
            }
        };
    }

    private static final String EXTRA_PARAM = "param";

    public static DialCustomDialogFragment newInstance(DialCustomParam param) {
        DialCustomDialogFragment fragment = new DialCustomDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_PARAM, param);
        fragment.setArguments(bundle);
        return fragment;
    }

    private DialCustomParam mParam;
    private TextView mTvTitle;
    private TextView mTvPercent;
    private ProgressBar mProgressBar;
    private DfuManager mDfuManager;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam = getArguments().getParcelable(EXTRA_PARAM);
        }
        mDfuManager = new DfuManager(getContext());
        mDfuManager.setDfuCallback(mDfuCallback);
        mDfuManager.init();
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        if (mParam == null) {
            return super.onCreateDialog(savedInstanceState);
        }
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_dial_custom, null);
        mTvTitle = view.findViewById(R.id.tv_title);
        mTvPercent = view.findViewById(R.id.tv_percent);
        mProgressBar = view.findViewById(R.id.progress_bar);

        startCreateDial();

        return new AlertDialog.Builder(getContext())
                .setView(view)
                .setCancelable(false)
                .create();
    }

    private Disposable mCreateDialDisposable;

    /**
     * 生成表盘
     */
    @SuppressLint("CheckResult")
    private void startCreateDial() {
        //1.检查是否需要下载
        File binFile = DialDownloader.getFileByUrl(getContext(), mParam.binUrl);
        String binPath = binFile != null && binFile.exists() ? binFile.getAbsolutePath() : null;

        Single<String> binSingle;
        if (TextUtils.isEmpty(binPath)) {
            binSingle = Single.create(new SingleOnSubscribe<String>() {
                @Override
                public void subscribe(final SingleEmitter<String> emitter) throws Exception {
                    final DialDownloader dialDownloader = new DialDownloader(getContext());
                    dialDownloader.setListener(new SimpleFileDownloader.SimpleListener() {
                        @Override
                        public void onCompleted(@NonNull String filePath) {
                            emitter.onSuccess(filePath);
                        }

                        @Override
                        public void onError(int errorCode) {
                            emitter.tryOnError(new DialDownloader.DialDownloadException(errorCode));
                        }
                    });
                    emitter.setCancellable(new Cancellable() {
                        @Override
                        public void cancel() throws Exception {
                            dialDownloader.cancel();
                        }
                    });
                    dialDownloader.start(mParam.binUrl);
                }
            });
        } else {
            binSingle = Single.just(binPath);
        }
        mCreateDialDisposable = Single
                .zip(binSingle, prepareBitmaps(), new BiFunction<String, Bitmap[], DialWriter>() {
                    @Override
                    public DialWriter apply(String s, Bitmap[] bitmaps) throws Exception {
                        File binFile = new File(s);
                        DialWriter writer = new DialWriter(binFile, bitmaps[0], bitmaps[1], mParam.position);

                        File temp = new File(binFile.getParent(), "temp_" + binFile.getName());
                        writer.setCopyFile(temp);

                        writer.setAutoScalePreview(true);
                        return writer;
                    }
                })
                .map(new Function<DialWriter, File>() {
                    @Override
                    public File apply(DialWriter dialWriter) throws Exception {
                        return dialWriter.execute(mCreateDialDisposable);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe(new Consumer<Disposable>() {
                    @Override
                    public void accept(Disposable disposable) throws Exception {
                        mTvTitle.setText(R.string.ds_dial_create);
                        mTvPercent.setText(null);
                        mProgressBar.setIndeterminate(true);
                    }
                })
                .subscribe(new Consumer<File>() {
                    @Override
                    public void accept(File file) throws Exception {
                        mTvTitle.setText(R.string.ds_dial_syncing);
                        mProgressBar.setProgress(0);
                        mProgressBar.setIndeterminate(false);
                        mDfuManager.upgradeDial(file.getAbsolutePath(), mParam.binFlag);
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.w("CreateDial Failed", throwable);
                        toast(R.string.ds_dial_create_failed);
                        dismissAllowingStateLoss();
                    }
                });
    }

    private Single<Bitmap[]> prepareBitmaps() {
        return Single.zip(glideLoadBitmap(mParam.backgroundUri), glideLoadBitmap(mParam.styleUri), new BiFunction<Bitmap, Bitmap, Bitmap[]>() {
            @Override
            public Bitmap[] apply(Bitmap background, Bitmap style) throws Exception {
                Bitmap[] results = new Bitmap[2];
                results[0] = DialDrawer.createDialBackground(background, mParam.shape, mParam.scaleType);
                results[1] = DialDrawer.createDialPreview(background, style, mParam.shape, mParam.scaleType, mParam.position, mParam.shape.width(), mParam.shape.height());
                return results;
            }
        }).subscribeOn(Schedulers.computation());
    }

    private Single<Bitmap> glideLoadBitmap(final Uri uri) {
        return Single.create(new SingleOnSubscribe<Bitmap>() {
            @Override
            public void subscribe(final SingleEmitter<Bitmap> emitter) throws Exception {
                GlideApp.with(getContext())
                        .asBitmap()
                        .load(uri)
                        .into(new CustomTarget<Bitmap>() {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                emitter.onSuccess(resource);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                emitter.tryOnError(new BitmapCreateException());
                            }
                        });
            }
        });
    }

    private static class BitmapCreateException extends Exception {

    }

    private DfuCallback mDfuCallback = new DfuCallback() {
        @Override
        public void onError(int errorType, int errorCode) {
            DfuDialogFragment.toastError(getContext(), errorType, errorCode);
            dismissAllowingStateLoss();
        }

        @Override
        public void onStateChanged(int state, boolean cancelable) {

        }

        @Override
        public void onProgressChanged(int progress) {
            mTvPercent.setText(progress + "%");
            mProgressBar.setProgress(progress);
        }

        @Override
        public void onSuccess() {
            mTvTitle.setText(R.string.ds_dial_sync_success);
            mTvPercent.setText(100 + "%");
            mProgressBar.setProgress(100);
            setCancelable(true);
            mDismissDisposable = Completable.timer(3, TimeUnit.SECONDS)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action() {
                        @Override
                        public void run() throws Exception {
                            try {
                                dismissAllowingStateLoss();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }, new Consumer<Throwable>() {
                        @Override
                        public void accept(Throwable throwable) throws Exception {
                            throwable.printStackTrace();
                        }
                    });
        }
    };

    private Disposable mDismissDisposable;

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (mDismissDisposable != null && !mDismissDisposable.isDisposed()) {
            mDismissDisposable.dispose();
        }
    }

    public void toast(@NonNull String msg) {
        Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
    }

    public void toast(@StringRes int resId) {
        Toast.makeText(getContext(), resId, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mCreateDialDisposable != null && !mCreateDialDisposable.isDisposed()) {
            mCreateDialDisposable.dispose();
        }
        mDfuManager.release();
    }

}
