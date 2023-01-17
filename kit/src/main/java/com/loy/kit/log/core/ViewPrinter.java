package com.loy.kit.log.core;

import android.app.Activity;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.loy.kit.R;
import com.loy.kit.Utils;
import com.loy.kit.widgets.recyclerview.BaseAdapter;
import com.loy.kit.widgets.recyclerview.BaseItem;
import com.loy.kit.widgets.recyclerview.BaseViewHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * 将日志打印到视图中, 然后可以直接查看
 * @author Loy
 * @time 2022/8/26 15:52
 * @des
 */
public class ViewPrinter implements Printer {
    private final RecyclerView mRecyclerView;
    private final LogViewAdapter mLogViewAdapter;
    private final ViewPrinterProvider mViewPrinterProvider;

    public ViewPrinter() {
        Activity activity = Utils.currentActivity();
        FrameLayout contentView = activity.findViewById(android.R.id.content);
        mRecyclerView = new RecyclerView(activity);
        mLogViewAdapter = new LogViewAdapter(new ArrayList<>());
        mRecyclerView.setLayoutManager(new LinearLayoutManager(mRecyclerView.getContext()));
        //mRecyclerView.addItemDecoration(new DividerItemDecoration(mRecyclerView.getContext(),DividerItemDecoration.VERTICAL));
        mRecyclerView.setAdapter(mLogViewAdapter);
        mViewPrinterProvider = new ViewPrinterProvider(contentView, mRecyclerView);
    }

    public ViewPrinterProvider getViewPrinterProvider() {
        return mViewPrinterProvider;
    }

    @Override
    public void log(Config config, int level, String tag, String message) {
        mLogViewAdapter.addItem(new LogPojo(level, message));
        mRecyclerView.smoothScrollToPosition(mLogViewAdapter.getItemCount() - 1);
    }

    private static class LogViewAdapter extends BaseAdapter<LogPojo>{
        public LogViewAdapter(List<LogPojo> data) {
            super(data);
        }
    }

    private static class LogPojo extends BaseItem<LogPojo> {
        @Level
        private final int level;
        private final String message;

        public LogPojo(int level, String message) {
            super(R.layout.view_printer);
            this.level = level;
            this.message = message;
        }

        @Override
        public void bind(@NonNull BaseViewHolder holder, int position) {
            TextView textView = holder.findViewById(R.id.msg);
            textView.setText(message);
            textView.setTextColor(Logger.Color(level));
        }
    }
}
