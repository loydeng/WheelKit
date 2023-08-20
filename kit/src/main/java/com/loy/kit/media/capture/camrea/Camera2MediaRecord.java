package com.loy.kit.media.capture.camrea;

import android.media.MediaRecorder;
import android.view.Surface;

import com.loy.kit.log.SdkLog;
import com.loy.kit.media.capture.camrea.bean.Profile;

import java.io.IOException;

/**
 * @author loy
 * @tiem 2023/8/17 17:13
 * @des
 */
public class Camera2MediaRecord {
    private MediaRecorder mMediaRecorder;

    public Camera2MediaRecord() {
        mMediaRecorder = new MediaRecorder();
    }

    public boolean prepare(String path, int rotation, Profile profile) {
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);


        mMediaRecorder.setOrientationHint(rotation);

        // 与预览视图大小保持一致, 预览视图根据横竖屏情况, 可能对换了宽高

        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

        mMediaRecorder.setVideoSize(profile.getWidth(), profile.getHeight());
        mMediaRecorder.setVideoFrameRate(Camera1MediaRecord.FRAME_RATE);
        mMediaRecorder.setVideoEncodingBitRate(profile.getBitrate());

        mMediaRecorder.setAudioChannels(Camera1MediaRecord.STEREO_CHANNEL);
        mMediaRecorder.setAudioSamplingRate(Camera1MediaRecord.AUDIO_SAMPLE_RATE);
        mMediaRecorder.setAudioEncodingBitRate(Camera1MediaRecord.AUDIO_BIT_RATE);

        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        mMediaRecorder.setOutputFile(path);
        try {
            mMediaRecorder.prepare();
        } catch (IOException e) {
            SdkLog.e("MediaRecorder prepare failed:" + e.getMessage());
            return false;
        }
        return true;
    }

    public Surface getSurface() {
        return mMediaRecorder.getSurface();
    }

    public void startRecord() {
        mMediaRecorder.start();
    }

    public void stopRecord() {
        mMediaRecorder.stop();
        mMediaRecorder.reset();
    }

    public void release() {
        if (mMediaRecorder != null) {
            mMediaRecorder.release();
            mMediaRecorder = null;
        }
    }
}
