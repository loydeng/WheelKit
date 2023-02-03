package com.loy.kit;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Stack;

/**
 * @author Loy
 * @time 2022/8/18 17:07
 * @des
 */
class ActivityObserver implements Application.ActivityLifecycleCallbacks {
    private final Stack<Activity> mActivityStack;
    private final TopActivityState mState;

    private static class TopActivityState{
        private boolean isBackground; // Stop
        private boolean isCover;      // Pause
        private boolean isActive;     // resume

        private void active() {
            isActive = true;
            isCover = false;
            isBackground = false;
        }

        private void covered() {
            isCover = true;
            isActive = false;
            isBackground = false;
        }

        private void goBack() {
            isBackground = true;
            isCover = false;
            isActive = false;
        }
    }

    public ActivityObserver() {
        mActivityStack = new Stack<>();
        mState = new TopActivityState();
    }

    public void pushActivity(Activity activity) {
        mActivityStack.push(activity);
    }

    public void popActivity(Activity activity) {
        activity.finish();
        mActivityStack.remove(activity);
    }

    public Activity currentActivity() {
        return mActivityStack.peek();
    }

    public Activity findByClassName(String className) {
        for (Activity activity : mActivityStack) {
            if (activity.getClass().getSimpleName().equals(className)) {
                return activity;
            }
        }
        return null;
    }

    public void finishAll() {
        while (!mActivityStack.isEmpty()) {
            mActivityStack.pop().finish();
        }
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity, @Nullable Bundle savedInstanceState) {
        pushActivity(activity);
    }

    @Override
    public void onActivityStarted(@NonNull Activity activity) {
        if (activity == currentActivity()) {
            mState.covered();
        }
    }

    @Override
    public void onActivityResumed(@NonNull Activity activity) {
        mState.active();
    }

    @Override
    public void onActivityPaused(@NonNull Activity activity) {
        if (activity == currentActivity()) {
            mState.covered();
        }
    }

    @Override
    public void onActivityStopped(@NonNull Activity activity) {
        if (activity == currentActivity()) {
            mState.goBack();
        }
    }

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
        popActivity(activity);
    }
}
