package com.github.kilnn.wristband2.sample.dfu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import com.github.kilnn.wristband2.sample.BaseActivity;
import com.github.kilnn.wristband2.sample.R;
import com.github.kilnn.wristband2.sample.utils.AndPermissionHelper;
import com.htsmart.wristband2.DfuUpdateHelper;
import com.htsmart.wristband2.bean.WristbandUpgradeInfo;

/**
 * Dfu.
 */
public class DfuActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dfu);
    }

    public void check_remote(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                final WristbandUpgradeInfo info = DfuUpdateHelper.checkUpgrade();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (info == null) {
                            toast("No Updated");
                        } else {
                            startDfu(info.hardwareUrl);
                        }
                    }
                });
            }
        }).start();
    }

    public void select_local(View view) {
        AndPermissionHelper.fileRequest(DfuActivity.this, new AndPermissionHelper.AndPermissionHelperListener1() {
            @Override
            public void onSuccess() {
                //去选择文件
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                try {
                    startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), SELECT_FILE_REQ);
                } catch (android.content.ActivityNotFoundException ex) {
                    Toast.makeText(DfuActivity.this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void startDfu(String uri) {
        DfuDialogFragment.newInstance(uri).show(getSupportFragmentManager(), null);
    }

    private static final int SELECT_FILE_REQ = 1;
    private String mSelectFile;

    @Override
    protected void onResume() {
        super.onResume();
        if (mSelectFile != null) {
            startDfu(mSelectFile);
            mSelectFile = null;
        }
    }

    @Override
    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        switch (requestCode) {
            case SELECT_FILE_REQ: {
                if (resultCode == RESULT_OK) {
                    // Get the Uri of the selected file
                    Uri uri = data.getData();
                    mSelectFile = FileUtils.getPath(this, uri);
                }
            }
            break;
        }
    }

}
