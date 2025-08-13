package com.loy.kit;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.loy.kit.utils.PermissionUtil;
import com.loy.kit.utils.ThreadUtil;
import com.loy.kit.utils.ToastUtil;
import com.loy.app.R;

import java.util.List;

public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        PermissionUtil.request(this,
                new PermissionUtil.PermissionResult() {
                    @Override
                    public void onGranted() {
                        ThreadUtil.postDelayOnUI(() -> {
                            startActivity(new Intent(SplashActivity.this, MainActivity.class));
                            finish();
                        }, 1000);
                    }

                    @Override
                    public void onDenied(List<String> deniedPermissions, List<String> justBlockedPermissions, List<String> blockedPermissions) {
                        ToastUtil.show("权限申请未通过, 即将退出应用");
                        ThreadUtil.postDelayOnUI(() -> {
                            System.exit(1);
                        }, 2000);
                    }
                }, Manifest.permission.WRITE_EXTERNAL_STORAGE,  Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA);
    }
}
