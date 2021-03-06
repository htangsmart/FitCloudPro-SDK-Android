package com.github.kilnn.wristband2.sample.dial.custom;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.kilnn.wristband2.sample.R;
import com.github.kilnn.wristband2.sample.dial.custom.bean.DialRequestParam;
import com.github.kilnn.wristband2.sample.dial.custom.bean.DialSubBinParam;
import com.github.kilnn.wristband2.sample.dial.custom.util.DialUtils;
import com.htsmart.wristband2.bean.DialSubBinInfo;
import com.htsmart.wristband2.dial.DialDrawer;
import com.htsmart.wristband2.dial.DialView;

import java.util.List;

public class DialSubSelectFragment extends AppCompatDialogFragment {

    private static final String EXTRA_DIAL_PARAM = "dial_param";

    public static DialSubSelectFragment newInstance(DialRequestParam param) {
        DialSubSelectFragment fragment = new DialSubSelectFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(EXTRA_DIAL_PARAM, param);
        fragment.setArguments(bundle);
        return fragment;
    }

    public interface Listener {
        void onBinFlagSelected(byte binFlag);
    }

    private Listener mListener;

    private DialRequestParam mParam;
    private RecyclerView mRecyclerView;
    private InnerAdapter mAdapter;
    private byte mSelectBinFlag;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (getParentFragment() != null && getParentFragment() instanceof Listener) {
            mListener = (Listener) getParentFragment();
        } else if (context instanceof Listener) {
            mListener = (Listener) context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam = getArguments().getParcelable(EXTRA_DIAL_PARAM);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        if (mParam == null || mParam.getSubBinParams() == null || mParam.getSubBinParams().size() <= 0) {
            return super.onCreateDialog(savedInstanceState);
        }
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_sub_select, null);
        mRecyclerView = view.findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        mAdapter = new InnerAdapter();
        mRecyclerView.setAdapter(mAdapter);

        view.findViewById(R.id.btn_sure).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    mListener.onBinFlagSelected(mSelectBinFlag);
                }
                dismissAllowingStateLoss();
            }
        });

        return new AlertDialog.Builder(getContext())
                .setView(view)
                .setCancelable(true)
                .create();
    }

    private static class InnerViewHolder extends RecyclerView.ViewHolder {
        private DialView dialView;

        private InnerViewHolder(@NonNull View itemView) {
            super(itemView);
            dialView = itemView.findViewById(R.id.dial_view);
        }
    }

    private class InnerAdapter extends RecyclerView.Adapter<InnerViewHolder> {

        private final List<DialSubBinParam> subBinParams;
        private final DialDrawer.Shape shape;
        private int selectPosition = 0;

        private InnerAdapter() {
            subBinParams = mParam.getSubBinParams();
            shape = DialDrawer.Shape.createFromLcd(mParam.getLcd());
            //默认选择第一个
            mSelectBinFlag = mParam.getSubBinParams().get(0).getBinFlag();
        }

        @NonNull
        @Override
        public InnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new InnerViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_dial_sub_select, parent, false));
        }

        @Override
        public void onBindViewHolder(@NonNull InnerViewHolder holder, int position) {
            final DialSubBinParam subBinParam = subBinParams.get(position);
            final DialView dialView = holder.dialView;
            final Context context = dialView.getContext();

            dialView.setShape(shape);

            if (subBinParam.getDialType() == DialSubBinInfo.TYPE_NORMAL) {
                if (!TextUtils.isEmpty(subBinParam.getImgUrl())) {
                    //成功获取到缩略图，那么使用缩略图
                    dialView.setBackgroundSource(Uri.parse(subBinParam.getImgUrl()));
                } else {
                    dialView.clearBackgroundBitmap();
                }
                dialView.clearStyleBitmap();
            } else {
                dialView.setBackgroundSource(DialUtils.getUriFromDrawableResId(context, R.drawable.dial_default_bg));
                //自定义的表盘
                switch (subBinParam.getDialType()) {
                    case DialSubBinInfo.TYPE_CUSTOM_STYLE_WHITE:
                        dialView.setStyleSource(DialUtils.getUriFromDrawableResId(context, R.drawable.dial_style1));
                        break;
                    case DialSubBinInfo.TYPE_CUSTOM_STYLE_BLACK:
                        dialView.setStyleSource(DialUtils.getUriFromDrawableResId(context, R.drawable.dial_style2));
                        break;
                    case DialSubBinInfo.TYPE_CUSTOM_STYLE_YELLOW:
                        dialView.setStyleSource(DialUtils.getUriFromDrawableResId(context, R.drawable.dial_style3));
                        break;
                    case DialSubBinInfo.TYPE_CUSTOM_STYLE_GREEN:
                        dialView.setStyleSource(DialUtils.getUriFromDrawableResId(context, R.drawable.dial_style4));
                        break;
                    case DialSubBinInfo.TYPE_CUSTOM_STYLE_GRAY:
                        dialView.setStyleSource(DialUtils.getUriFromDrawableResId(context, R.drawable.dial_style5));
                        break;
                    default:
                        dialView.clearStyleBitmap();
                        break;
                }
                dialView.setStylePosition(DialDrawer.Position.TOP);
            }
            dialView.setChecked(position == selectPosition);
            dialView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //默认选择第一个
                    mSelectBinFlag = mParam.getSubBinParams().get(position).getBinFlag();
                    selectPosition = position;
                    notifyDataSetChanged();
                }
            });
        }

        @Override
        public int getItemCount() {
            return subBinParams.size();
        }
    }

}
