package com.loy.kit.log.core;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.loy.kit.utils.ConvertUtil;

/**
 * @author Loy
 * @time 2022/8/26 16:54
 * @des
 */
public class ViewPrinterProvider {
    public static final String TAG_FLOATING_VIEW = "TAG_FLOATING_VIEW";
    public static final String TAG_LOG_VIEW = "TAG_LOG_VIEW";

    private final FrameLayout contentView;
    private final RecyclerView recyclerView;
    private FrameLayout backgroundView;
    private boolean isOpen;
    private View floatView;

    public ViewPrinterProvider(FrameLayout contentView, RecyclerView recyclerView) {
        this.contentView = contentView;
        this.recyclerView = recyclerView;
    }

    // 显示悬浮按钮
    public void showFloatView() {
        if (contentView.findViewWithTag(TAG_FLOATING_VIEW) != null) {
            return;
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ConvertUtil.dp2px(40),
                                                                       ConvertUtil.dp2px(30));
        params.gravity = Gravity.BOTTOM | Gravity.END;
        params.bottomMargin = ConvertUtil.dp2px(100);

        floatView = genFloatView();
        floatView.setTag(TAG_FLOATING_VIEW);
        floatView.setAlpha(0.8f);
        floatView.setBackgroundColor(Color.BLACK);
        contentView.addView(floatView, params);
    }

    private View genFloatView() {
        if (floatView != null) {
            return floatView;
        }
        TextView textView = new TextView(contentView.getContext());
        textView.setGravity(Gravity.CENTER);
        textView.setText("Log");
        textView.setTextColor(Color.MAGENTA);
        textView.setOnClickListener((v)->{
            if (!isOpen) {
                showLogView();
            }
        });
        return floatView = textView;
    }

    private void showLogView() {
        if (contentView.findViewWithTag(TAG_LOG_VIEW) != null) {
            return;
        }
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                                                       ConvertUtil.dp2px(200));

        params.gravity = Gravity.BOTTOM;
        View logView = genLogView();
        logView.setTag(TAG_LOG_VIEW);
        contentView.addView(logView, params);
        isOpen = true;
    }

    private View genLogView() {
        if (backgroundView != null) {
            return backgroundView;
        }
        FrameLayout bgView = new FrameLayout(contentView.getContext());
        bgView.setBackgroundColor(Color.BLACK);
        bgView.addView(recyclerView);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.END;
        TextView textView = new TextView(contentView.getContext());
        textView.setText("Close");
        textView.setTextColor(Color.MAGENTA);
        textView.setOnClickListener((v)->{
            closeLogView();
        });
        bgView.addView(textView, params);
        return backgroundView = bgView;
    }

    private void closeLogView() {
        isOpen = false;
        contentView.removeView(genLogView());
    }

}
