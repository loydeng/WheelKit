package com.loy.kit.widgets.recyclerview;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Collections;
import java.util.List;

/**
 * @author Loy
 * @time 2022/9/5 16:23
 * @des
 */
public class BaseAdapter<Item extends BaseItem> extends RecyclerView.Adapter<BaseViewHolder> {

    private final List<Item> mData;
    private RecyclerView mRecyclerView;

    public BaseAdapter(List<Item> data) {
        mData = data;
    }

    @Override
    public int getItemViewType(int position) {
        Item item = mData.get(position);
        item.mAdapter = this;
        return item.getViewType();
    }

    @NonNull
    @Override
    public BaseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return BaseItem.onCreateViewHolder(parent, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position) {
        mData.get(position).bindViewHolder(holder, position);
    }

    @Override
    public void onBindViewHolder(@NonNull BaseViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (payloads.isEmpty()) {
            super.onBindViewHolder(holder, position, payloads);
            return;
        }
        mData.get(position).partialUpdate(payloads);
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public void onDetachedFromRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onDetachedFromRecyclerView(recyclerView);
        if (mRecyclerView == recyclerView) {
            mRecyclerView = null;
        }
    }

    @Override
    public void onViewAttachedToWindow(@NonNull BaseViewHolder holder) {
        super.onViewAttachedToWindow(holder);
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull BaseViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }

    @Override
    public void onViewRecycled(@NonNull BaseViewHolder holder) {
        super.onViewRecycled(holder);
        int pos = holder.getAdapterPosition();
        if (pos < 0 || pos >= mData.size()) {
            return;
        }
        mData.get(pos).onViewRecycled(holder, pos);
    }

    public List<Item> getItems() {
        return Collections.unmodifiableList(mData);
    }

    public void addItem(Item item) {
        mData.add(item);
        notifyItemInserted(mData.size() - 1);
    }

    public void remove(int index) {
        if (isRangeValid(index)) {
            mData.remove(index);
            notifyItemRemoved(index);
        }
    }

    public void remove(Item item) {
        int index = mData.indexOf(item);
        if (index != -1) {
            mData.remove(index);
            notifyItemRemoved(index);
        }
    }

    public void update(Item item) {
        int index = mData.indexOf(item);
        if (index != -1) {
            notifyItemChanged(index);
        }
    }

    public void update(Item item, List<Object> payloads) {
        int index = mData.indexOf(item);
        if (index != -1) {
            notifyItemChanged(index, payloads);
        }
    }

    public void replace(int index, Item item) {
        if (isRangeValid(index)) {
            mData.set(index, item);
            notifyItemChanged(index);
        }
    }

    public void swapItem(int firstIndex, int secondIndex) {
        if (firstIndex != secondIndex
                && isRangeValid(firstIndex)
                && isRangeValid(secondIndex)) {
            Collections.swap(mData, firstIndex,secondIndex);
            notifyItemMoved(firstIndex, secondIndex);
        }
    }

    private boolean isRangeValid(int index) {
        return index >= 0 && index < mData.size();
    }
}
