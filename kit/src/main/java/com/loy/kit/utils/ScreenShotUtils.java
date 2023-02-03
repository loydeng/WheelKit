/*
package com.loy.kit.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

*/
/**
 * @author Loy
 * @time 2021/9/29 14:29
 * @des
 *//*

public class ScreenShotUtils {

    private static Bitmap.CompressFormat sImageFormat = Bitmap.CompressFormat.JPEG;

    public static void screenShot(AppCompatActivity activity) {
        Bitmap bitmap = getBitmapFromActivity(activity);
        boolean b = save(bitmap, sImageFormat);
        if (b) {
            ToastUtils.show("截图成功");
        }
    }

    private static String getScreenShotSavePath() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String time = dateFormat.format(new Date());
        String filePath = AppConstants.LOCAL_RECORD + File.separator + "screenshot-" + time + "." + sImageFormat.name().toLowerCase();
        File file = new File(filePath);
        File parentFile = file.getParentFile();
        if (!parentFile.exists()) {
            parentFile.mkdirs();
        }
        return filePath;
    }

    private static Bitmap getBitmapFromActivity(AppCompatActivity activity) {

        View rootView = activity.getWindow().getDecorView();

        Rect rect = new Rect();
        rootView.getWindowVisibleDisplayFrame(rect);
        int left = rect.left;
        int right = rect.right;
        int width = right - left;
        DisplayMetrics screenMetrics = ScreenUtils.getScreenMetrics();
        Bitmap pb = Bitmap.createBitmap(width, screenMetrics.heightPixels, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(pb);

        List<View> allView = getAllVisibleView(rootView);

        Map<SurfaceViewRenderer, Bitmap> renderers = new HashMap<>();
        Iterator<View> iterator = allView.iterator();
        while (iterator.hasNext()) {
            View next = iterator.next();
            if (next instanceof SurfaceViewRenderer) {
                renderers.put((SurfaceViewRenderer) next, null);
            }
        }

        CountDownLatch latch = new CountDownLatch(renderers.size());

        Iterator<Map.Entry<SurfaceViewRenderer, Bitmap>> entryIterator = renderers.entrySet().iterator();
        while (entryIterator.hasNext()) {
            Map.Entry<SurfaceViewRenderer, Bitmap> next = entryIterator.next();
            SurfaceViewRenderer renderer = next.getKey();
            renderer.addFrameListener(new EglRenderer.FrameListener() {
                @Override
                public void onFrame(Bitmap bitmap) {
                    if (bitmap != null) {
                        Next.setValue(bitmap);
                        //renderer.removeFrameListener(this);
                        latch.countDown();
                    }
                }
            }, 1);
        }

        try {
            latch.await();
        } catch (InterruptedException e) {
            SdkLog.e("screenshot err:" + e.getMessage());
        }

        int[] location = new int[2];
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        for (int i = 0; i < allView.size(); i++) {
            View v = allView.get(i);
            if (v instanceof SurfaceViewRenderer) {
                SurfaceViewRenderer surfaceView = (SurfaceViewRenderer) v;
                surfaceView.getLocationOnScreen(location);
                canvas.drawBitmap(renderers.get(surfaceView), location[0] - left, location[1], paint);
            } else {
                v.setDrawingCacheEnabled(true);
                Bitmap bitmap = v.getDrawingCache();
                v.getLocationOnScreen(location);
                canvas.drawBitmap(bitmap, location[0] - left, location[1], paint);
                v.setDrawingCacheEnabled(false);
                v.destroyDrawingCache();
            }
        }
        return pb;
    }

    private static float getScale(View showView, int frameWidth, int frameHeight) {
        float scale = 0;
        int viewWidth = showView.getWidth();
        int viewHeight = showView.getHeight();
        float viewAspectRatio = (float) viewWidth / viewHeight;
        float frameAspectRatio = (float) frameWidth / frameHeight;
        float smallScale = 0; // 保持原图比例, 居于内部, 有黑边
        float bigScale = 0; // // 保持原图比例, 填充容器, 原图被截断
        if (viewAspectRatio > frameAspectRatio) {
            smallScale = (float) viewHeight / frameHeight;
            bigScale = (float) viewWidth / frameWidth;
        }else {
            smallScale = (float) viewWidth / frameWidth;
            bigScale = (float) viewHeight / frameHeight;
        }
        scale = bigScale; // 填充
        return scale;
    }

    private static List<View> getAllVisibleView(View parentView) {
        ArrayList<View> views = new ArrayList<>();
        View pv = parentView;
        if (pv.getVisibility() == View.VISIBLE) {
            views.add(pv);
            if (pv instanceof ViewGroup) {
                ViewGroup vg = (ViewGroup) pv;
                for (int i = 0; i < vg.getChildCount(); i++) {
                    View cv = vg.getChildAt(i);
                    views.addAll(getAllVisibleView(cv));
                }
            }
        }
        return views;
    }

    // 无法正常显示 surfaceView, 视图黑屏
    private static Bitmap getBitmapFromView(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        Bitmap bmp = view.getDrawingCache();

        Bitmap ret = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight());

        view.setDrawingCacheEnabled(false);
        view.destroyDrawingCache();

        return ret;
    }

    private static boolean save(Bitmap src, Bitmap.CompressFormat format) {
        if (isEmptyBitmap(src))
            return false;

        OutputStream os = null;
        boolean ret = false;
        try {
            os = new BufferedOutputStream(new FileOutputStream(getScreenShotSavePath()));
            ret = src.compress(format, 100, os);
            src.recycle();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            CloseUtils.closeIO(os);
        }

        return ret;
    }

    private static boolean isEmptyBitmap(Bitmap src) {
        return src == null || src.getWidth() == 0 || src.getHeight() == 0;
    }
}
*/
