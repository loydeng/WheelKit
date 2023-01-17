package com.loy.kit.utils;

import android.media.AudioManager;
import android.os.Build;

import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Loy
 * @time 2022/8/30 15:49
 * @des
 */
public class VolumeUtil {

    public static boolean isSpeakerOn() {
        AudioManager am = ServiceManagerUtil.getAudioManager();
        return am.isSpeakerphoneOn();
    }

    public static void setSpeakerOn(boolean isOn) {
        AudioManager am = ServiceManagerUtil.getAudioManager();
        am.setMode(AudioManager.MODE_IN_COMMUNICATION);
        am.setSpeakerphoneOn(isOn);
    }

    @IntDef({AudioManager.MODE_CURRENT,
            AudioManager.MODE_IN_CALL,
            AudioManager.MODE_NORMAL,
            AudioManager.MODE_RINGTONE,
            AudioManager.MODE_IN_COMMUNICATION,
            AudioManager.MODE_INVALID})
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.SOURCE)
    @interface Mode{}

    public static void setMode(@Mode int mode) {
        AudioManager am = ServiceManagerUtil.getAudioManager();
        am.setMode(mode);
    }

    @IntDef({AudioManager.STREAM_VOICE_CALL,
            AudioManager.STREAM_SYSTEM,
            AudioManager.STREAM_RING,
            AudioManager.STREAM_MUSIC,
            AudioManager.STREAM_ALARM,
            AudioManager.STREAM_NOTIFICATION,
            AudioManager.STREAM_DTMF,
            AudioManager.STREAM_ACCESSIBILITY}
        )
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.SOURCE)
    @interface Type{}

    public static int getVolume(@Type int streamType) {
        AudioManager am = ServiceManagerUtil.getAudioManager();
        //noinspection ConstantConditions
        return am.getStreamVolume(streamType);
    }

    @IntDef({
            AudioManager.FLAG_SHOW_UI,
            AudioManager.FLAG_ALLOW_RINGER_MODES,
            AudioManager.FLAG_PLAY_SOUND,
            AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE,
            AudioManager.FLAG_VIBRATE,
    })
    @Target(ElementType.PARAMETER)
    @Retention(RetentionPolicy.SOURCE)
    @interface Flag{}

    public static void setVolume(@Type int streamType, int volume, @Flag int flags) {
        AudioManager am = ServiceManagerUtil.getAudioManager();
        try {
            am.setStreamVolume(streamType, volume, flags);
        } catch (SecurityException ignore) {
        }
    }

    public static int getMaxVolume(@Type int streamType) {
        AudioManager am = ServiceManagerUtil.getAudioManager();
        //noinspection ConstantConditions
        return am.getStreamMaxVolume(streamType);
    }

    public static int getMinVolume(@Type int streamType) {
        AudioManager am = ServiceManagerUtil.getAudioManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            //noinspection ConstantConditions
            return am.getStreamMinVolume(streamType);
        }
        return 0;
    }
}
