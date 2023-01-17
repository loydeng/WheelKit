package com.loy.kit.widgets.recyclerview;

import android.util.SparseArray;
import android.view.View;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

/**
 * @author Loy
 * @time 2022/9/5 15:58
 * @des
 */
public class BaseViewHolder extends RecyclerView.ViewHolder {
    private final SparseArray<View> views = new SparseArray<>();

    public BaseViewHolder(@NonNull View itemView) {
        super(itemView);
    }

    public <V extends View> V findViewById(@IdRes int id) {
        View view = views.get(id);
        if (view == null) {
            view = itemView.findViewById(id);
            views.put(id, view);
        }
        return (V) view;
    }

    public void setOnClickListener(@IdRes int id, View.OnClickListener listener) {
        findViewById(id).setOnClickListener(listener);
    }

    public void setOnLongClickListener(@IdRes int id, View.OnLongClickListener listener) {
        findViewById(id).setOnLongClickListener(listener);
    }
}
