package com.loy.kit.utils;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.loy.kit.R;
import com.loy.kit.Utils;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Loy
 * @time 2021/4/1 20:33
 * @des
 */
public class PermissionUtil {
    public static final int SETTINGS_CODE = 0x666;

    public static void request(FragmentActivity activity, PermissionResult callback, String ...permissions) {
        if (activity == null) {
            return;
        }
        Config config = new Config(callback, permissions);
        if (config.permissions == null || config.permissions.length == 0 ||
                Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            config.mResult.onGranted();
        } else {
            ArrayList<String> requestPermissions = new ArrayList<>();
            for (String p : config.permissions) {
                if (PackageManager.PERMISSION_DENIED == ContextCompat.checkSelfPermission(activity, p)) {
                    requestPermissions.add(p);
                }
            }

            if (requestPermissions.isEmpty()) {
                config.mResult.onGranted();
            } else {
                config.needRequestPermissions = requestPermissions;
                FragmentManager fragmentManager = activity.getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                PermissionFragment fragment = new PermissionFragment(config);
                fragmentTransaction.add(fragment, PermissionFragment.class.getSimpleName());
                fragmentTransaction.commitAllowingStateLoss();
            }
        }
    }

    public static void gotoSettings(FragmentActivity activity){
        DialogUtil.showConfirmDialog(activity, "权限设置", "跳至系统应用设置", ()->{
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                       Uri.fromParts("package", AppUtil.getPackageName(), null));
            activity.startActivityForResult(intent, SETTINGS_CODE);
        }, null);
    }

    public interface PermissionResult {
        void onGranted();

        /**
         * 是否显示需要解释说明对话框, 选择确定时才继续申请权限, 否则会取消权限申请.
         * 针对于已经遭拒的权限, 再次申请时的说明.
         *
         * @return
         */
        default boolean onRationale() {
            return false;
        }

        /**
         * 申请的权限中有被拒绝项时, 会回调此方法, 返回所有遭拒权限
         * @param deniedPermissions 所有被拒权限集合
         * @param justBlockedPermissions 当次被设置不再询问的权限集合
         * @param blockedPermissions 所有被设置不再询问的权限集合
         */
        void onDenied(List<String> deniedPermissions, List<String> justBlockedPermissions, List<String> blockedPermissions);
    }

    private static class Config {
        private final String[] permissions;
        private final PermissionResult mResult;
        private List<String> needRequestPermissions;
        private List<String> preDenyRequestPermissions;
        private String explanation;

        public Config(PermissionResult result, String ...permissions) {
            this.permissions = permissions;
            mResult = (result == null ? EmptyUtil.getEmptyImpl(PermissionResult.class) : result);
        }

        public Config setExplanation(String explanation) {
            this.explanation = explanation;
            return this;
        }
    }

    public static class PermissionFragment extends Fragment {
        public static final int REQUEST_CODE = 0x223;

        private final Config config;
        // "androidx.appcompat:appcompat:1.3.0-alpha02"  需要此包 版本 1.3.0-alpha02 以上
        // private ActivityResultCallback<Map<String, Boolean>> mCallback;

        public PermissionFragment(Config config) {
            this.config = config;
        }

        private void requestPermissions() {
            requestPermissions(ConvertUtil.list2Array(config.needRequestPermissions), REQUEST_CODE);
        }

        @Override
        public void onAttach(@NonNull Context context) {
            super.onAttach(context);
            //registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), mCallback);

            ArrayList<String> rationalPermissions = new ArrayList<>();
            for (String p : config.needRequestPermissions) {
                if (shouldShowRequestPermissionRationale(p)) { // 上次被拒绝
                    rationalPermissions.add(p);
                }
            }
            config.preDenyRequestPermissions = rationalPermissions;

            if (config.mResult.onRationale() && !rationalPermissions.isEmpty()) {
                DialogUtil.showConfirmDialog(Utils.currentActivity(),
                                             ResourceUtil.getString(R.string.permission_title),
                                             EmptyUtil.isStringNotEmpty(config.explanation) ? config.explanation : "需要权限",
                                             () -> {
                                                  requestPermissions(ConvertUtil.list2Array(config.needRequestPermissions), REQUEST_CODE);
                                              },
                                             () -> {
                                                  config.mResult.onDenied(config.needRequestPermissions, EmptyUtil.getEmptyList(), EmptyUtil.getEmptyList());
                                              });
            } else {
                requestPermissions();
            }
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            if (requestCode == REQUEST_CODE) {
                ArrayList<String> denyList = new ArrayList<>(); // 拒绝
                ArrayList<String> blockList = new ArrayList<>(); // 勾选不再询问, 可能包含以前的
                ArrayList<String> justBlockList = new ArrayList<>(); // 勾选不再询问, 仅本次申请时勾选
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) { // 拒绝授权
                        String permission = permissions[i];
                        if (shouldShowRequestPermissionRationale(permission)) { // 未勾选不再询问
                            denyList.add(permission);
                        }else { // 勾选不再询问
                            blockList.add(permission);
                            if (config.preDenyRequestPermissions.contains(permission)) { // 本次勾选
                                justBlockList.add(permission);
                            }
                        }
                    }
                }
                if (denyList.isEmpty()) {
                    config.mResult.onGranted();
                }else {
                    config.mResult.onDenied(denyList, justBlockList, blockList);
                }
            }
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            Fragment fragment = fragmentManager.findFragmentByTag(PermissionFragment.class.getSimpleName());
            if (fragment != null) {
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.remove(fragment);
                fragmentTransaction.commitAllowingStateLoss();
            }
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
        }

        @Override
        public void onDetach() {
            super.onDetach();
        }

        @Override
        public void onDestroy() {
            super.onDestroy();
        }
    }
}
