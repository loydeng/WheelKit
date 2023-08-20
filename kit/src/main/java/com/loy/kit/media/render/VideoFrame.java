package com.loy.kit.media.render;

import android.graphics.Matrix;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author loy
 * @tiem 2023/7/30 11:10
 * @des
 */
public class VideoFrame {

    public static class RefCountMonitor {
        private final AtomicInteger refCount;
        private final Runnable releaseCallback;

        public RefCountMonitor(Runnable callback) {
            this.refCount = new AtomicInteger(1);
            this.releaseCallback = callback;
        }

        public void retain() {
            if (refCount.incrementAndGet() < 2) {
                throw new IllegalStateException("Reference is already release(refCount < 1), can not retain !");
            }
        }

        boolean safeRetain() {
            int currentRefCount = refCount.get();
            while (currentRefCount > 0) {
                if (refCount.weakCompareAndSet(currentRefCount, currentRefCount + 1)) {
                    return true;
                }
                currentRefCount = refCount.get();
            }
            return false;
        }

        public void release() {
            int newCount = refCount.decrementAndGet();
            if (newCount < 0) {
                throw new IllegalStateException("Reference is already release(refCount < 1), can not release again!");
            }
            if (newCount == 0 && releaseCallback != null) {
                releaseCallback.run();
            }
        }
    }

    private final List<TextureObject> mTextureObjects;

    private final int mWidth;
    private final int mHeight;
    private final int mRotation;
    private final Matrix mTransformMatrix;
    private final long mTimestampNs;
    private final RefCountMonitor mRefCountMonitor;

    public VideoFrame(List<TextureObject> textureObjectList,
                      int width, int height, int rotation, Matrix matrix, long timestampNs, RefCountMonitor refCountMonitor) {
        this.mTextureObjects = textureObjectList;
        this.mWidth = width;
        this.mHeight = height;
        this.mRotation = rotation;

        this.mTransformMatrix = matrix;
        this.mTimestampNs = timestampNs;
        this.mRefCountMonitor = refCountMonitor;
    }

    public void retain() {
        mRefCountMonitor.retain();
    }

    public void release() {
        mRefCountMonitor.release();
    }

    public List<TextureObject> getTextureObjects() {
        return mTextureObjects;
    }

    public int getWidth() {
        return mWidth;
    }

    public int getHeight() {
        return mHeight;
    }

    public int getRotation() {
        return mRotation;
    }

    public Matrix getTransformMatrix() {
        return mTransformMatrix;
    }

    public long getTimestampNs() {
        return mTimestampNs;
    }

    public int getRotatedWidth() {
        if (mRotation % 180 == 0) {
            return mWidth;
        }
        return mHeight;
    }

    public int getRotatedHeight() {
        if (mRotation % 180 == 0) {
            return mHeight;
        }
        return mWidth;
    }
}
