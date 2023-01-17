package com.loy.kit.widgets.recyclerview;

import android.util.SparseArray;
import android.util.SparseIntArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import java.util.List;

/**
 * @author Loy
 * @time 2022/9/5 16:32
 * @des
 */
public abstract class BaseItem<T extends BaseItem> {
    private static final SparseIntArray LAYOUT_SPARSE_ARRAY = new SparseIntArray();
    private static final SparseArray<View> VIEW_SPARSE_ARRAY   = new SparseArray<>();

    static BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutByType = LAYOUT_SPARSE_ARRAY.get(viewType, -1);
        if (layoutByType != -1) {
            return new BaseViewHolder(LayoutInflater.from(parent.getContext()).inflate(layoutByType, parent, false));
        }
        View viewByType = VIEW_SPARSE_ARRAY.get(viewType);
        if (viewByType != null) {
            return new BaseViewHolder(viewByType);
        }
        throw new RuntimeException("onCreateViewHolder: get holder from view type failed.");
    }

    public abstract void bind(@NonNull final BaseViewHolder holder, final int position);

    public void partialUpdate(List<Object> payloads) {
    }

    public void onViewRecycled(@NonNull final BaseViewHolder holder, final int position) {}

    void bindViewHolder(@NonNull final BaseViewHolder holder, final int position) {
        if (mOnItemClickListener != null) {
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mOnItemClickListener != null) {
                        //noinspection unchecked
                        mOnItemClickListener.onItemClick(holder, (T) BaseItem.this, getIndex());
                    }
                }
            });
        } else {
            holder.itemView.setOnClickListener(null);
        }
        if (mOnItemLongClickListener != null) {
            holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    if (mOnItemLongClickListener != null) {
                        return mOnItemLongClickListener.onItemLongClick(holder, (T) BaseItem.this, getIndex());
                    }
                    return false;
                }
            });
        } else {
            holder.itemView.setOnLongClickListener(null);
        }
        bind(holder, position);
    }

    public int getIndex() {
        return getAdapter().getItems().indexOf(this);
    }

    private final int viewType;
    BaseAdapter<T> mAdapter;

    private OnItemClickListener<T>     mOnItemClickListener;
    private OnItemLongClickListener<T> mOnItemLongClickListener;

    public BaseItem(@LayoutRes int layoutId) {
        viewType = getViewTypeByLayoutId(layoutId);
        LAYOUT_SPARSE_ARRAY.put(viewType, layoutId);
    }

    public BaseItem(@NonNull View view) {
        viewType = getViewTypeByView(view);
        VIEW_SPARSE_ARRAY.put(viewType, view);
    }

    private int getViewTypeByLayoutId(@LayoutRes int layoutId) {
        return layoutId + getClass().hashCode();
    }

    private int getViewTypeByView(@NonNull View view) {
        return view.hashCode() + getClass().hashCode();
    }
    public int getViewType() {
        return viewType;
    }

    public BaseAdapter<T> getAdapter() {
        return mAdapter;
    }

    public boolean isViewType(@LayoutRes int layoutId) {
        return viewType == getViewTypeByLayoutId(layoutId);
    }

    public boolean isViewType(@NonNull View view) {
        return viewType == getViewTypeByView(view);
    }

    public interface OnItemClickListener<T> {
        void onItemClick(BaseViewHolder holder, T item, int position);
    }

    public interface OnItemLongClickListener<T> {
        boolean onItemLongClick(BaseViewHolder holder, T item, int position);
    }
}
