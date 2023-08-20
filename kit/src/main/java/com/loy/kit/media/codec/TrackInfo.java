package com.loy.kit.media.codec;

import android.media.MediaFormat;

import com.loy.kit.media.codec.CodecFormat.FormatKey;
import static com.loy.kit.media.codec.CodecFormat.*;
import androidx.annotation.NonNull;

/**
 * @author loyde
 * @tiem 2023/2/21 19:41
 * @des 文件中流轨信息类
 */
public class TrackInfo {
    private int index;
    private MediaFormat format;

    public TrackInfo(int index, MediaFormat format) {
        this.index = index;
        this.format = format;
    }

    public int getIndex() {
        return index;
    }

    public TrackInfo setIndex(int index) {
        this.index = index;
        return this;
    }

    public MediaFormat getFormat() {
        return format;
    }

    public TrackInfo setFormat(MediaFormat format) {
        this.format = format;
        return this;
    }

    public String getMime() {
        return format.getString(MediaFormat.KEY_MIME);
    }

    public boolean isVideo() {
        return getMime().startsWith(VIDEO_FORMAT_PREFIX);
    }

    public boolean isAudio() {
        return getMime().startsWith(AUDIO_FORMAT_PREFIX);
    }

    public int getWidth() {
        return isVideo() ? format.getInteger(MediaFormat.KEY_WIDTH) : 0;
    }

    public int getHeight() {
        return isVideo() ? format.getInteger(MediaFormat.KEY_HEIGHT) : 0;
    }

    public int getFrameRate() {
        return isVideo() ? format.getInteger(FormatKey.Video.FRAME_RATE) : 0;
    }

    public long getDuration() {
        return isVideo() ? format.getLong(MediaFormat.KEY_DURATION) : 0;
    }

    @NonNull
    @Override
    public String toString() {
        return "[" + "index:" + index + ", format:" + format.toString() + "]";
    }
}
