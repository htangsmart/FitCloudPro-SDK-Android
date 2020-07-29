package com.github.kilnn.wristband2.sample.dial.custom;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.github.kilnn.wristband2.sample.R;
import com.htsmart.wristband2.dial.DialDrawer;
import com.htsmart.wristband2.dial.DialView;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class DialGridView extends RecyclerView {

    public static abstract class Listener {
        void onSelect(DialGridData data, int position) {

        }

        void onDelete(DialGridData data, int position) {

        }

        void onAddClick() {

        }
    }

    public static int dip2px(Context context, float dpValue) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    private DialGridAdapter mAdapter;

    public DialGridView(@NonNull Context context, boolean editEnabled) {
        super(context);
        //这个Padding是根据 R.layout.item_dial_custom 计算的固定值
        int paddingLeft = dip2px(context, 16);
        setPadding(paddingLeft, 0, 0, 0);
        GridLayoutManager manager = new GridLayoutManager(getContext(), 3);
        setLayoutManager(manager);
        mAdapter = new DialGridAdapter(editEnabled);
        setAdapter(mAdapter);
    }

    @NonNull
    public DialGridAdapter getAdapter() {
        return mAdapter;
    }

    private static class AddViewHolder extends ViewHolder {

        private DialView dialView;

        private AddViewHolder(@NonNull View itemView) {
            super(itemView);
            dialView = itemView.findViewById(R.id.dial_view);
        }
    }

    private static class PreviewViewHolder extends ViewHolder {

        private DialView dialView;
        private ImageView deleteView;

        private PreviewViewHolder(@NonNull View itemView) {
            super(itemView);
            dialView = itemView.findViewById(R.id.dial_view);
            deleteView = itemView.findViewById(R.id.img_delete);
        }
    }

    public static class DialGridAdapter extends Adapter<ViewHolder> {

        private boolean editEnabled;
        private DialDrawer.Shape shape = DialDrawer.Shape.createFromLcd(0);
        private List<DialGridData> datas;
        private int selectPosition = 0;//默认选择第一个数据
        private boolean deleteMode;
        private Listener listener;

        private DialGridAdapter(boolean editEnabled) {
            this.editEnabled = editEnabled;
        }

        public void setShape(@NonNull DialDrawer.Shape shape) {
            this.shape = shape;
        }

        public void setDatas(List<DialGridData> datas) {
            this.datas = datas;
            //纠正因数据改变，可能出错的选中项
            if (this.datas == null || selectPosition >= this.datas.size()) {
                selectPosition = 0;
            }
        }

        public int getSelectPosition() {
            return selectPosition;
        }

        public void setListener(Listener listener) {
            this.listener = listener;
        }

        public boolean isDeleteMode() {
            return deleteMode;
        }

        public void exitDeleteMode() {
            this.deleteMode = false;
            notifyDataSetChanged();
        }

        @Override
        public int getItemCount() {
            int count = datas == null ? 0 : datas.size();
            return editEnabled ? count + 1 : count;
        }

        private static final int TYPE_ADD = 0;
        private static final int TYPE_PREVIEW = 1;

        @Override
        public int getItemViewType(int position) {
            if (editEnabled && position == 0) {
                return TYPE_ADD;
            } else {
                return TYPE_PREVIEW;
            }
        }

        @NonNull
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            if (viewType == TYPE_ADD) {
                return new AddViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_dial_add, parent, false));
            } else {
                return new PreviewViewHolder(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_dial_preview, parent, false));
            }
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            if (getItemViewType(position) == TYPE_ADD) {
                AddViewHolder viewHolder = (AddViewHolder) holder;
                viewHolder.dialView.setShape(shape);
                viewHolder.dialView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (isDeleteMode()) {
                            exitDeleteMode();
                            return;
                        }
                        if (listener != null) {
                            listener.onAddClick();
                        }
                    }
                });
            } else {
                final PreviewViewHolder viewHolder = (PreviewViewHolder) holder;
                final int dataPosition = editEnabled ? position - 1 : position;
                final DialGridData data = datas.get(dataPosition);

                viewHolder.dialView.setShape(shape);
                viewHolder.dialView.setBackgroundSource(data.getBackgroundUri());
                viewHolder.dialView.setStyleSource(data.getStyleUri());
                viewHolder.dialView.setStylePosition(data.getPosition());
                viewHolder.dialView.setChecked(dataPosition == selectPosition);
                viewHolder.dialView.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (selectPosition != dataPosition) {
                            selectPosition = dataPosition;
                            if (listener != null) {
                                listener.onSelect(data, selectPosition);
                            }
                            notifyDataSetChanged();
                        }
                    }
                });

                if (editEnabled) {
                    viewHolder.dialView.setLongClickable(true);
                    viewHolder.dialView.setOnLongClickListener(new OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (deleteMode) return false;
                            deleteMode = true;
                            notifyDataSetChanged();
                            return true;
                        }
                    });
                } else {
                    viewHolder.dialView.setLongClickable(false);
                }

                if (deleteMode) {
                    Animation animation = AnimationUtils.loadAnimation(viewHolder.deleteView.getContext(), R.anim.scale_in);
                    animation.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) {
                        }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            viewHolder.deleteView.setVisibility(VISIBLE);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) {
                        }
                    });
                    viewHolder.deleteView.startAnimation(animation);
                    viewHolder.deleteView.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            DialGridData removeData = datas.remove(dataPosition);
                            if (selectPosition >= datas.size()) {
                                selectPosition = 0;
                            }
                            if (datas.size() <= 0) {
                                deleteMode = false;
                            }
                            notifyDataSetChanged();
                            if (listener != null) {
                                listener.onDelete(removeData, dataPosition);
                            }
                        }
                    });
                } else {
                    viewHolder.deleteView.clearAnimation();
                    viewHolder.deleteView.setVisibility(INVISIBLE);
                }
            }
        }
    }
}


