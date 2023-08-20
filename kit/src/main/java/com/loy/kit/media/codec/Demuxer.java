package com.loy.kit.media.codec;

import android.content.res.AssetFileDescriptor;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author loyde
 * @tiem 2023/2/21 19:42
 * @des 二次封装 MediaExtractor 解复用器, 增强易用性, 可复用性.
 */
public class Demuxer {
    // 解封装
    private MediaExtractor mExtractor;
    private TrackInfo currentTrackInfo;
    private final ArrayList<TrackInfo> mVideoTrackInfo = new ArrayList<>();
    private final ArrayList<TrackInfo> mAudioTrackInfo = new ArrayList<>();

    public Demuxer(String path) throws IOException {
        mExtractor = new MediaExtractor();
        mExtractor.setDataSource(path);
        findIndex();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public Demuxer(AssetFileDescriptor afd) throws IOException {
        mExtractor = new MediaExtractor();
        mExtractor.setDataSource(afd);
        findIndex();
    }

    private void findIndex() {
        int trackCount = mExtractor.getTrackCount();
        for (int i = 0; i < trackCount; i++) {  // index <--> MediaFormat
            MediaFormat mediaFormat = mExtractor.getTrackFormat(i);
            String mime = mediaFormat.getString(MediaFormat.KEY_MIME);
            if (mime.startsWith(CodecFormat.VIDEO_FORMAT_PREFIX)) {
                mVideoTrackInfo.add(new TrackInfo(i, mediaFormat));
            } else if (mime.startsWith(CodecFormat.AUDIO_FORMAT_PREFIX)) {
                mAudioTrackInfo.add(new TrackInfo(i, mediaFormat));
            }
        }
    }

    public ArrayList<TrackInfo> getVideoTrackInfo() {
        return mVideoTrackInfo;
    }

    public ArrayList<TrackInfo> getAudioTrackInfo() {
        return mAudioTrackInfo;
    }

    public TrackInfo getDefaultVideoTrackInfo() {
        return mVideoTrackInfo.isEmpty() ? null : mVideoTrackInfo.get(0);
    }

    public TrackInfo getDefaultAudioTrackInfo() {
        return mAudioTrackInfo.isEmpty() ? null : mAudioTrackInfo.get(0);
    }

    public TrackInfo getCurrentTrackInfo() {
        return currentTrackInfo;
    }

    public void selectTrack(TrackInfo info) {
        mExtractor.selectTrack(info.getIndex());
        currentTrackInfo = info;
    }

    public void unSelectCurrent() {
        if (currentTrackInfo != null) {
            mExtractor.unselectTrack(currentTrackInfo.getIndex());
            currentTrackInfo = null;
        }
    }

    public boolean selectDefaultVideoTrack() {
        TrackInfo trackInfo = getDefaultVideoTrackInfo();
        boolean hasTrack = trackInfo != null;
        if (hasTrack) {
            selectTrack(trackInfo);
        }
        return hasTrack;
    }

    public boolean selectDefaultAudioTrack() {
        TrackInfo trackInfo = getDefaultAudioTrackInfo();
        boolean hasTrack = trackInfo != null;
        if (hasTrack) {
            selectTrack(trackInfo);
        }
        return hasTrack;
    }

    public void seekTo(long time) {
        if (currentTrackInfo != null) {
            mExtractor.seekTo(time, MediaExtractor.SEEK_TO_CLOSEST_SYNC); // 无法找到刚好的那一时间帧, 就最接的帧, 另外可选偏前或后
        }
    }

    // info, buffer 都是传出数据, 返回值表示是否结束, true为有数据返回. 最后一次返回数据一般不是满buffer.
    // 结束时, 可以往编码其中加入标志位EOS,即 codec.queueInputBuffer(inputIndex, 0, 0, 0L, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
    public boolean readData(CodecBuffer buffer) {
        buffer.setPresentationTimeUs(0);
        buffer.clear();
        int len = mExtractor.readSampleData(buffer.getByteBuffer(), 0);
        if (len < 0) {
            return false;
        } else {
            buffer.setOffset(0);
            buffer.setSize(len);
            buffer.setFlags(0);
            buffer.setPresentationTimeUs(mExtractor.getSampleTime());
        }
        mExtractor.advance();
        return true;
    }

    public void release() {
        mExtractor.release();
        mExtractor = null;
    }
}
