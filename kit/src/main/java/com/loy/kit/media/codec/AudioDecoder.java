package com.loy.kit.media.codec;

import android.media.AudioManager;

import com.loy.kit.utils.ServiceManagerUtil;

/**
 * @author loyde
 * @tiem 2023/2/21 19:53
 * @des
 */ // 音频解码器
public class AudioDecoder extends Codec {

    private void getFrameInfo() {
        AudioManager am = ServiceManagerUtil.getAudioManager();
        String sampleRate = am.getProperty(AudioManager.PROPERTY_OUTPUT_SAMPLE_RATE);
        String framesPerBuffer = am.getProperty(AudioManager.PROPERTY_OUTPUT_FRAMES_PER_BUFFER);
    }

    @Override
    public void configure() {

    }
}
