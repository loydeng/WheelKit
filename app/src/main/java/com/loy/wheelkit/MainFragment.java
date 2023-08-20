package com.loy.wheelkit;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.loy.kit.widgets.recyclerview.BaseAdapter;
import com.loy.kit.widgets.recyclerview.BaseItem;
import com.loy.kit.widgets.recyclerview.BaseViewHolder;
import com.loy.wheelkit.databinding.FragmentMainBinding;

import java.util.ArrayList;
import java.util.List;

public class MainFragment extends Fragment {

    private FragmentMainBinding binding;
    private List<MyItem> items;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);

        items = new ArrayList<>();

        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        items.add(new MyItem(getTextView(), R.string.log, R.id.action_MainFragment_to_LogFragment));
        items.add(new MyItem(getTextView(), R.string.camera, R.id.action_MainFragment_to_CameraFragment));
        binding.rv.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rv.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
        binding.rv.setAdapter(new MyAdapter(items));
    }

    private TextView getTextView() {
        TextView textView = new TextView(getContext());
        textView.setTextSize(24);
        textView.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        layoutParams.gravity = Gravity.CENTER_HORIZONTAL;
        layoutParams.topMargin = 10;
        layoutParams.bottomMargin = 10;
        textView.setLayoutParams(layoutParams);
        return textView;
    }

    private class MyItem extends BaseItem {
        private final int titleId;
        private final int fragmentId;

        public MyItem(@NonNull View view, int titleId, int fragmentId) {
            super(view);
            this.titleId = titleId;
            this.fragmentId = fragmentId;
            mOnItemClickListener = (holder, item, position) -> {
                NavHostFragment.findNavController(MainFragment.this).navigate(MyItem.this.fragmentId);
            };
        }

        @Override
        public void bind(@NonNull BaseViewHolder holder, int position) {
            ((TextView) holder.itemView).setText(titleId);
        }

    }

    private class MyAdapter extends BaseAdapter<MyItem> {
        public MyAdapter(List<MyItem> data) {
            super(data);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}