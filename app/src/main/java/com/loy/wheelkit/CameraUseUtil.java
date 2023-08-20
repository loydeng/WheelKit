package com.loy.wheelkit;

import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.loy.kit.media.capture.camrea.CameraHelper;

/**
 * @author Loy
 * @time 2022/1/10 17:23
 * @des 仅用于获取拍照和录像的文件, 指定路径即可. 不直接用Camera, 而是Intent委托
 */
public class CameraUseUtil {

    public enum Error{
        NoCamera,
        NoTargetApp,
        PathErrorOrNoPermission,
        Cancel
    }

    public enum Type{
        Image,
        Video
    }


    public interface Callback {
        void onSuccess(Uri uri);

        void onError(Error error);
    }

    public static void recordVideo(AppCompatActivity activity, String path, Callback callback) {
        action(activity, Type.Video, path, callback);
    }

    public static void tackPicture(AppCompatActivity activity, String path, Callback callback) {
        action(activity, Type.Image, path, callback);
    }

    private static void action(AppCompatActivity activity, Type type, String path, Callback callback) {
        boolean cameraWork = CameraHelper.hasCamera(activity);
        if (!cameraWork) {
            // 相机不可用
            if (callback != null) {
                callback.onError(Error.NoCamera);
            }
        } else {
            FragmentManager manager = activity.getSupportFragmentManager();
            String name = CameraUseFragment.class.getName();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.add(new CameraUseFragment(path, type, new Callback() {
                @Override
                public void onSuccess(Uri uri) {
                    if (callback != null) {
                        callback.onSuccess(uri);
                    }
                    Fragment fragment = manager.findFragmentByTag(name);
                    if (fragment != null) {
                        FragmentTransaction fragmentTransaction = manager.beginTransaction();
                        fragmentTransaction.remove(fragment);
                        fragmentTransaction.commitAllowingStateLoss();
                    }
                }

                @Override
                public void onError(Error error) {
                    if (callback != null) {
                        callback.onError(error);
                    }
                    Fragment fragment = manager.findFragmentByTag(name);
                    if (fragment != null) {
                        FragmentTransaction fragmentTransaction = manager.beginTransaction();
                        fragmentTransaction.remove(fragment);
                        fragmentTransaction.commitAllowingStateLoss();
                    }
                }
            }), name);
            transaction.commitAllowingStateLoss();
        }
    }

}
