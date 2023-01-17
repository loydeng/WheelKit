package com.loy.kit.utils;

import android.content.Context;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IntDef;

import com.loy.kit.Utils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.ref.WeakReference;

/**
 * @author Loy
 * @time 2021/1/28 19:48
 * @des
 */
public class ToastUtil {
    public static WeakReference<Toast> sWeakReference;

    private static void cancelPre() {
        if (sWeakReference != null) {
            Toast toast = sWeakReference.get();
            if (toast != null) {
                toast.cancel();
            }
            sWeakReference = null;
        }
    }

    private static void show(Toast toast) {
        if (toast != null) {
            cancelPre();
            sWeakReference = new WeakReference<>(toast);
            toast.show();
        }
    }

    public static void show(String content) {
        ThreadUtil.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                Toast toast = Toast.makeText(Utils.getAppContext(), content, Toast.LENGTH_SHORT);
                show(toast);
            }
        });
    }

    public static void show(int stringResId) {
        ThreadUtil.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                Context context = Utils.getAppContext();
                Toast toast = Toast.makeText(context, context.getText(stringResId), Toast.LENGTH_SHORT);
                show(toast);
            }
        });
    }

    /**
     * 限定入参值, 可以是 int , long 或 string, 对应于 IntDef, LongDef, StringDef
     * 不必定义复杂的枚举类型
     */
    @IntDef({Toast.LENGTH_SHORT,Toast.LENGTH_LONG})
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.RUNTIME)
    @interface Duration{}

    public static class Config{
        private View contentView;
        private String message;
        private int bgColor;
        private float textSize;
        private int textColor;
        private int duration;
        private int gravity;
        private int xOffset;
        private int yOffset;
        private float hMargin;
        private float vMargin;

        public Config setContentView(View contentView) {
            this.contentView = contentView;
            return this;
        }

        public Config setMessage(String message) {
            this.message = message;
            return this;
        }

        public Config setBgColor(int bgColor) {
            this.bgColor = bgColor;
            return this;
        }

        public Config setTextSize(float textSize) {
            this.textSize = textSize;
            return this;
        }

        public Config setTextColor(int textColor) {
            this.textColor = textColor;
            return this;
        }

        public Config setDuration(int duration) {
            this.duration = duration;
            return this;
        }

        public Config setGravity(int gravity) {
            this.gravity = gravity;
            return this;
        }

        public Config setXOffset(int xOffset) {
            this.xOffset = xOffset;
            return this;
        }

        public Config setYOffset(int yOffset) {
            this.yOffset = yOffset;
            return this;
        }

        public Config setHorizontalMargin(float hMargin) {
            this.hMargin = hMargin;
            return this;
        }

        public Config setVerticalMargin(float vMargin) {
            this.vMargin = vMargin;
            return this;
        }
    }

    public static void show(Config config) {
        ThreadUtil.runOnUIThread(() -> {
            Toast toast = Toast.makeText(Utils.getAppContext(), "", Toast.LENGTH_SHORT);
            if (config.contentView != null) {
                toast.setView(config.contentView);
            }else {
                View layoutView = toast.getView();
                TextView textView = layoutView.findViewById(android.R.id.message);
                if (config.bgColor != 0) {
                    layoutView.setBackgroundColor(config.bgColor);
                }
                if (config.textColor != 0) {
                    textView.setTextColor(config.textColor);
                }
                if (config.textSize > 0) {
                    textView.setTextSize(config.textSize);
                }
                textView.setText(config.message);
            }
            if (config.gravity != 0) {
                toast.setGravity(config.gravity, config.xOffset, config.yOffset);
            }
            toast.setMargin(config.hMargin, config.vMargin);
            if (config.duration == Toast.LENGTH_LONG) {
                toast.setDuration(Toast.LENGTH_LONG);
            }

            show(toast);
        });
    }
}
