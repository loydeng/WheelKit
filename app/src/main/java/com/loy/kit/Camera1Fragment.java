package com.loy.kit;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.loy.kit.log.SdkLog;
import com.loy.kit.media.capture.camrea.CameraClient;
import com.loy.kit.media.capture.camrea.CameraHelper;
import com.loy.kit.media.capture.camrea.bean.Error;
import com.loy.kit.media.capture.camrea.bean.Profile;
import com.loy.kit.media.capture.camrea.bean.SensorInfo;

import com.loy.app.databinding.FragmentCamera1Binding;

import java.util.List;

/**
 * @author loy
 * @tiem 2023/2/26 12:01
 * @des
 */
public class Camera1Fragment extends Fragment implements View.OnClickListener {
    private FragmentCamera1Binding mBinding;
    private CameraClient cameraClient;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = FragmentCamera1Binding.inflate(inflater, container, false);
        mBinding.capture.setOnClickListener(this);
        mBinding.record.setOnClickListener(this);
        mBinding.switcher.setOnClickListener(this);

        cameraClient = new CameraClient(this.getContext(), CameraClient.API.Camera);
        cameraClient.querySensorInfo(new CameraClient.Callback<List<SensorInfo>>() {
            @Override
            public void onSuccess(List<SensorInfo> result) {
                for (SensorInfo sensorInfo : result) {
                    SdkLog.e("" + sensorInfo.getProfiles());
                    if (sensorInfo.isBack() && sensorInfo.getProfiles().contains(Profile.PROFILE_720P)) {
                        sensorInfo.setExpectProfile(Profile.PROFILE_720P);
                        cameraClient.open(sensorInfo, mBinding.surface);
                        return;
                    }
                }
            }

            @Override
            public void onFailure(Error error) {

            }
        });
        return mBinding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        cameraClient.close();
        mBinding = null;

    }

    @Override
    public void onClick(View v) {
        if (v.getId() == mBinding.capture.getId()) {
            cameraClient.takePicture(CameraHelper.getOutputMediaFileUri(".jpeg").getPath());
        } else if (v.getId() == mBinding.record.getId()) {
            if (cameraClient.isRecording()) {
                cameraClient.stopRecord();
            } else {
                cameraClient.startRecord(CameraHelper.getOutputMediaFileUri(".mp4").getPath());
            }
        } else if (v.getId() == mBinding.switcher.getId()) {
            cameraClient.switcher();
        }
    }
}
