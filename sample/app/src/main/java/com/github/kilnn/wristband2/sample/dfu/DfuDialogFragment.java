package com.github.kilnn.wristband2.sample.dfu;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.kilnn.wristband2.sample.R;
import com.htsmart.wristband2.dfu.DfuCallback;
import com.htsmart.wristband2.dfu.DfuManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;


public class DfuDialogFragment extends AppCompatDialogFragment {

    public interface DfuSuccessListener {
        void onDfuSuccess();
    }

    private static final String EXTRA_URI = "uri";

    private DfuManager mDfuManager;

    private ProgressBar mProgressBar;
    private TextView mPercentageTv;
    private TextView mStateTv;

    private String mUri;

    /**
     * @param uri dfu file uri
     * @return DfuDialogFragment instance
     */
    public static DfuDialogFragment newInstance(String uri) {
        DfuDialogFragment dialog = new DfuDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_URI, uri);
        dialog.setArguments(bundle);
        return dialog;
    }

    public DfuDialogFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mDfuManager = new DfuManager(getActivity());
        mDfuManager.setDfuCallback(mDfuCallback);
        mDfuManager.init();

        mUri = getArguments().getString(EXTRA_URI);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Context context = getContext();
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        View view = LayoutInflater.from(context).inflate(R.layout.dialog_dfu, null);
        mProgressBar = view.findViewById(R.id.progress);
        mProgressBar.setIndeterminate(true);
        mPercentageTv = view.findViewById(R.id.percentage_tv);
        mStateTv = view.findViewById(R.id.state_tv);
        builder.setView(view);
        setCancelable(false);
        builder.setCancelable(false);
        return builder.create();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //TODO The example here is used to upgrade the firmware.
        // If you need to upgrade the dial, the parameter is passed false
        mDfuManager.start(mUri, true);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        mDfuManager.cancel();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mDfuManager.release();
    }

    private DfuCallback mDfuCallback = new DfuCallback() {

        @Override
        public void onError(int errorType, int errorCode) {
            toastError(getContext(), errorType, errorCode);
            dismissAllowingStateLoss();
        }

        @Override
        public void onStateChanged(int state, boolean cancelable) {
            switch (state) {
                case DfuManager.STATE_CHECK_DFU_FILE:
                    mStateTv.setText(R.string.dfu_state_dfu_file);
                    break;
                case DfuManager.STATE_CHECK_DFU_MODE:
                    mStateTv.setText(R.string.dfu_state_dfu_mode);
                    break;
                case DfuManager.STATE_FIND_DFU_DEVICE:
                    mStateTv.setText(R.string.dfu_state_dfu_device);
                    break;
                case DfuManager.STATE_DFU_ING:
                    mStateTv.setText(R.string.dfu_state_dfu_process);
                    break;
            }
            setCancelable(cancelable);
        }

        @SuppressLint("StringFormatMatches")
        @Override
        public void onProgressChanged(int progress) {
            if (progress == 0) {
                mStateTv.setText(R.string.dfu_progress_start);
            } else if (progress == 100) {
                mStateTv.setText(R.string.dfu_progress_completed);
            }
            mProgressBar.setIndeterminate(false);
            mProgressBar.setProgress(progress);
            mPercentageTv.setText(getString(R.string.percentage, String.valueOf(progress)));
        }

        @Override
        public void onSuccess() {
            Toast.makeText(getContext(), R.string.dfu_success, Toast.LENGTH_SHORT).show();

            if (getActivity() != null && (getActivity() instanceof DfuSuccessListener)) {
                ((DfuSuccessListener) getActivity()).onDfuSuccess();
            }

            dismissAllowingStateLoss();
        }
    };

    public static void toastError(Context context, int errorType, int errorCode) {
        int toastId = 0;
        if (errorType == DfuManager.ERROR_TYPE_BT) {
            if (errorCode == DfuManager.ERROR_CODE_BT_UNSUPPORTED) {
                toastId = R.string.ble_not_support;
            } else if (errorCode == DfuManager.ERROR_CODE_BT_DISABLE) {
                toastId = R.string.bt_not_open;
            }
        } else if (errorType == DfuManager.ERROR_TYPE_DFU_FILE) {
            if (errorCode == DfuManager.ERROR_CODE_DFU_FILE_URI
                    || errorCode == DfuManager.ERROR_CODE_DFU_FILE_FORMAT) {
                toastId = R.string.dfu_error_file_format;
            } else if (errorCode == DfuManager.ERROR_CODE_DFU_FILE_NOT_EXIST) {
                toastId = R.string.dfu_error_file_not_exist;
            } else if (errorCode == DfuManager.ERROR_CODE_DFU_FILE_DOWNLOAD) {
                toastId = R.string.dfu_error_file_download;
            }
        } else if (errorType == DfuManager.ERROR_TYPE_DFU_MODE) {
            if (errorCode == DfuManager.ERROR_CODE_DFU_MODE_HARDWARE_INFO) {
                //do nothing
            } else if (errorCode == DfuManager.ERROR_CODE_DFU_MODE_LOW_BATTERY) {
                toastId = R.string.dfu_error_low_battery;
            } else if (errorCode == DfuManager.ERROR_CODE_DFU_MODE_ABORT) {
                toastId = R.string.device_disconnected;
            } else {
                toastId = R.string.dfu_error_enter_dfu_failed;
            }
        } else if (errorType == DfuManager.ERROR_TYPE_DFU_DEVICE) {
            if (errorCode == DfuManager.ERROR_CODE_DFU_DEVICE_SCAN_FAILED) {
                toastId = R.string.dfu_error_scan_failed;
            } else if (errorCode == DfuManager.ERROR_CODE_DFU_DEVICE_NOT_FOUND) {
                toastId = R.string.dfu_error_device_not_found;
            }
        } else if (errorType == DfuManager.ERROR_TYPE_DFU_PROCESS) {
            if (errorCode == DfuManager.ERROR_CODE_DFU_PROCESS_SERVICE_NOT_READY
                    || errorCode == DfuManager.ERROR_CODE_DFU_PROCESS_STARTUP_FAILED) {
                toastId = R.string.dfu_error_service_not_ready;
            }
        }

        if (toastId != 0) {
            Toast.makeText(context, toastId, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, context.getString(R.string.dfu_failed) + "  errorCode:" + errorCode, Toast.LENGTH_SHORT).show();
        }
    }

}
