package com.loy.kit.utils;

import android.content.res.AssetFileDescriptor;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.Build;
import android.view.Surface;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * @author loyde
 * @des
 * @time 2022/4/14 21:45
 */
public class CodecUtil {
    public static final String VIDEO_FORMAT_PREFIX = "video/";
    public static final String AUDIO_FORMAT_PREFIX = "audio/";

    public interface Mime {
        interface Video {
            String H264 = MediaFormat.MIMETYPE_VIDEO_AVC;
            String H265 = MediaFormat.MIMETYPE_VIDEO_HEVC;
            String VP8 = MediaFormat.MIMETYPE_VIDEO_VP8;
            String VP9 = MediaFormat.MIMETYPE_VIDEO_VP9;
            String RAW = MediaFormat.MIMETYPE_VIDEO_RAW;
        }

        interface Audio {
            String AAC = MediaFormat.MIMETYPE_AUDIO_AAC;
            String OPUS = MediaFormat.MIMETYPE_AUDIO_OPUS;
            String RAW = MediaFormat.MIMETYPE_AUDIO_RAW;
        }

        interface Text {

        }
    }

    public interface FormatKey {
        String MIME = MediaFormat.KEY_MIME;
        String DURATION = MediaFormat.KEY_DURATION;
        String BITRATE = MediaFormat.KEY_BIT_RATE;
        String BITRATE_MODE = MediaFormat.KEY_BITRATE_MODE;
        String MAX_INPUT_SIZE = MediaFormat.KEY_MAX_INPUT_SIZE;

        interface Video {
            String WIDTH = MediaFormat.KEY_WIDTH;
            String HEIGHT = MediaFormat.KEY_HEIGHT;
            String FRAME_RATE = MediaFormat.KEY_FRAME_RATE;
            String I_FRAME_INTERVAL = MediaFormat.KEY_I_FRAME_INTERVAL;
            String COLOR_FORMAT = MediaFormat.KEY_COLOR_FORMAT;
        }

        interface Audio {
            String SAMPLE_RATE = MediaFormat.KEY_SAMPLE_RATE;
            String CHANNELS = MediaFormat.KEY_CHANNEL_COUNT;
            //String DEPTH = MediaFormat.KEY_PCM_ENCODING;
        }
    }

    public interface FormatValue {
        interface Video {
            int All = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible;  //api 21  compat format
            // ----------------------------------------------------------------
            // |   use showSupportColorFormat see hardware support
            // |
            //\|/
            int I420 = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Planar;
            int NV12 = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar;
            int NV21 = MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420PackedSemiPlanar;


            int RGB = MediaCodecInfo.CodecCapabilities.COLOR_Format24bitRGB888;
            //int RGBA = MediaCodecInfo.CodecCapabilities.COLOR_FormatRGBAFlexible;
            int ARGB = MediaCodecInfo.CodecCapabilities.COLOR_Format32bitARGB8888;
            int BGR = MediaCodecInfo.CodecCapabilities.COLOR_Format24bitBGR888;
            int BGRA = MediaCodecInfo.CodecCapabilities.COLOR_Format32bitBGRA8888;
            //int ABGR = MediaCodecInfo.CodecCapabilities.COLOR_Format32bitABGR8888;

        }
    }

    public static MediaCodecInfo findFromCodecList(String mime, boolean isEncoder) {
        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        MediaCodecInfo[] codecInfos = mediaCodecList.getCodecInfos();
        for (MediaCodecInfo codecInfo : codecInfos) {
            boolean filter = codecInfo.isEncoder();
            if (isEncoder == filter) {
                //MediaCodecInfo.CodecCapabilities capabilitiesForType = codecInfo.getCapabilitiesForType(mime);
                //capabilitiesForType.isFormatSupported()
                //codecInfo.isHardwareAccelerated();
                //codecInfo.isSoftwareOnly();
                String[] supportedTypes = codecInfo.getSupportedTypes();
                for (String supportedType : supportedTypes) {
                    if (supportedType.equals(mime)) {
                        return codecInfo;
                    }
                }
            }
        }
        return null;
    }

    public static String getCodecName(MediaFormat format, boolean isEncoder) {
        MediaCodecList mediaCodecList = new MediaCodecList(MediaCodecList.REGULAR_CODECS);
        return isEncoder ? mediaCodecList.findEncoderForFormat(format) : mediaCodecList.findDecoderForFormat(format);
    }

    public static int[] showSupportColorFormat(String mime, boolean isEncoder) {
        MediaCodecInfo info = findFromCodecList(mime, isEncoder);
        if (info != null) {
            MediaCodecInfo.CodecCapabilities cap = info.getCapabilitiesForType(mime);
            int[] colorFormats = new int[cap.colorFormats.length];
            System.arraycopy(cap.colorFormats, 0, colorFormats, 0, colorFormats.length);
            return colorFormats;
        }else {
            return new int[0];
        }
    }

    // 文件中流轨信息类
    public static class TrackInfo {
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

    // 二次封装 MediaExtractor 解复用器, 增强易用性, 可复用性.
    public static class Demuxer {

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
                if (mime.startsWith(VIDEO_FORMAT_PREFIX)) {
                    mVideoTrackInfo.add(new TrackInfo(i, mediaFormat));
                } else if (mime.startsWith(AUDIO_FORMAT_PREFIX)) {
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
        public boolean readData(Buffer buffer) {
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

    public static class Buffer {
        private final int index;
        private final ByteBuffer byteBuffer;
        private int offset;
        private int size;
        private int flags;
        private long presentationTimeUs ;

        public Buffer(int index, ByteBuffer byteBuffer) {
            this.index = index;
            this.byteBuffer = byteBuffer;
        }

        public void clear() {
            byteBuffer.clear();
        }

        public void endFlag() {
            setOffset(0);
            setSize(0);
            setPresentationTimeUs(0);
            setFlags(MediaCodec.BUFFER_FLAG_END_OF_STREAM);
        }

        public int getIndex() {
            return index;
        }

        public ByteBuffer getByteBuffer() {
            return byteBuffer;
        }

        public int getOffset() {
            return offset;
        }

        public Buffer setOffset(int offset) {
            this.offset = offset;
            return this;
        }

        public int getSize() {
            return size;
        }

        public Buffer setSize(int size) {
            this.size = size;
            return this;
        }

        public int getFlags() {
            return flags;
        }

        public Buffer setFlags(int flags) {
            this.flags = flags;
            return this;
        }

        public long getPresentationTimeUs() {
            return presentationTimeUs;
        }

        public Buffer setPresentationTimeUs(long presentationTimeUs) {
            this.presentationTimeUs = presentationTimeUs;
            return this;
        }
    }

    // 编解码器
    public static abstract class Codec {
        public enum Type {
            H264(Mime.Video.H264),
            H265(Mime.Video.H265),
            VP8(Mime.Video.VP8),
            VP9(Mime.Video.VP9);

            private final String mime;

            Type(String mime) {
                this.mime = mime;
            }

            public String getMime() {
                return mime;
            }
        }

        protected Type type;
        protected MediaCodec mCodec;
        protected MediaFormat mFormat;
        protected Surface mOutputSurface;

        public abstract void configure();

        public void start() {
            mCodec.start();
        }

        public void stop() {
            mCodec.stop();
        }

        public void reset() {
            mCodec.reset();
        }

        public void release() {
            mCodec.release();
        }


        // 获取有效输入缓冲区
        public Buffer getInputBuffer() {
            // 获取有效输入缓冲区的索引
            int index = mCodec.dequeueInputBuffer(0);
            if (index >= 0) {
                ByteBuffer inputBuffer = mCodec.getInputBuffer(index);
                inputBuffer.clear();
                return new Buffer(index, inputBuffer);
            }
            return null;
        }

        // 同步方式向编码器发数据
        public void fillData(Buffer buffer) {
            // pts
            //long microSecond = System.nanoTime() / 1000;
            // 将数据发回给编码器
            mCodec.queueInputBuffer(buffer.getIndex(), buffer.getOffset(), buffer.getSize(), buffer.getPresentationTimeUs(), buffer.getFlags());
        }

        // 同步方式从编码器取数据
        public byte[] readData() {
            byte[] bytes = new byte[0];
            MediaCodec.BufferInfo info = new MediaCodec.BufferInfo();
            // 获取输出缓冲区状态
            int index = mCodec.dequeueOutputBuffer(info, 10_000);
            if (index == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) { // 编码器输出格式改变, 首次设置就会回调一次, 中间不会再修改.
                // 这里可以获取编码器格式
                // MediaFormat outputFormat = mCodec.getOutputFormat();
                // 然后可以开启复用器, 将数据写入文件.
                // int trackIndex = mediaMuxer.addTrack(outputFormat);
                // mediaMuxer.start();
            } else if (index == MediaCodec.INFO_TRY_AGAIN_LATER) { // 暂无数据, 一会再读
            } else if (index == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) { // 表示输出缓冲已变化, 有新数据可取.
                // ignore
            } else {// 拿到输出队列有效索引
                ByteBuffer outputBuffer = mCodec.getOutputBuffer(index);
                if ((info.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0) {// buffer 配置信息
                    //info.size = 0; //可以忽略
                    //也可以写入关键帧前面, 即 sps pps
                }
                if (info.size != 0) {
                    outputBuffer.position(info.offset);
                    outputBuffer.limit(info.offset + info.size);
                    bytes = new byte[outputBuffer.remaining()];
                    outputBuffer.get(bytes);// can use outputBuffer
                }
                // 释放缓冲, 提供后续复用
                mCodec.releaseOutputBuffer(index, false);

                if ((info.flags & MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) { // 数据结束
                    return null;
                }
                return bytes;
            }

            return new byte[0];
        }

        // 异步方式,  onInputBufferAvailable 回调时可喂数据,
        // onOutputBufferAvailable 回调时可拿数据
        public void onBufferAvailable(MediaCodec.Callback callback) {
            mCodec.setCallback(callback);
        }
    }

    // 编码器
    public static abstract class Encoder extends Codec {
    }

    // 视频编码器
    public static class VideoEncoder extends Encoder {
        private static final int flags = MediaCodec.CONFIGURE_FLAG_ENCODE;

        public static class Builder {
            private Type type;
            private int width;
            private int height;
            private int bitrate;
            private int frameRate;
            private int keyFrameInternal;
            private Surface outputSurface;

            public Builder setType(Type type) {
                this.type = type;
                return this;
            }

            public Builder setWidth(int width) {
                this.width = width;
                return this;
            }

            public Builder setHeight(int height) {
                this.height = height;
                return this;
            }

            public Builder setBitrate(int bitrate) {
                this.bitrate = bitrate;
                return this;
            }

            public Builder setFrameRate(int frameRate) {
                this.frameRate = frameRate;
                return this;
            }

            public Builder setKeyFrameInternal(int keyFrameInternal) {
                this.keyFrameInternal = keyFrameInternal;
                return this;
            }

            public Builder setOutputSurface(Surface outputSurface) {
                this.outputSurface = outputSurface;
                return this;
            }

            public VideoEncoder build() throws IOException {
                if (type == null || width <= 0 || height <= 0 || bitrate <= 0 || frameRate <= 0 || keyFrameInternal <= 0) {
                    throw new IllegalArgumentException("value must nonnull or > 0 exclude input surface");
                }
                VideoEncoder videoEncoder = new VideoEncoder(this);
                return videoEncoder;
            }
        }

        private VideoEncoder(Builder builder) throws IOException {
            mFormat = MediaFormat.createVideoFormat(builder.type.getMime(), builder.width, builder.height);
            mFormat.setInteger(FormatKey.BITRATE, builder.bitrate);
            mFormat.setInteger(FormatKey.Video.FRAME_RATE, builder.frameRate);
            mFormat.setInteger(FormatKey.Video.I_FRAME_INTERVAL, builder.keyFrameInternal);
            mCodec = MediaCodec.createEncoderByType(builder.type.getMime());
            mOutputSurface = builder.outputSurface;
        }

        @Override
        public void configure() {
            mCodec.configure(mFormat, mOutputSurface, null, flags);
        }

        protected Surface getInputSurface() {
            return mCodec.createInputSurface();
        }


    }

    // 解码器
    public static abstract class Decoder extends Codec {
    }


    // 视频解码器
    public static class VideoDecoder extends Decoder {
        public static class Builder {
            private Type type;
            private int width;
            private int height;
            private int frameRate;
            private int colorFormat;
            private Surface outputSurface;

            public Builder setType(Type type) {
                this.type = type;
                return this;
            }

            public Builder setWidth(int width) {
                this.width = width;
                return this;
            }

            public Builder setHeight(int height) {
                this.height = height;
                return this;
            }

            public Builder setFrameRate(int frameRate) {
                this.frameRate = frameRate;
                return this;
            }

            public Builder setColorFormat(int colorFormat) {
                this.colorFormat = colorFormat;
                return this;
            }

            public Builder setOutputSurface(Surface outputSurface) {
                this.outputSurface = outputSurface;
                return this;
            }

            public VideoDecoder build() throws IOException {
                if (type == null || width <= 0 || height <= 0) {
                    throw new IllegalArgumentException("value must nonnull or > 0 exclude input surface and colorFormat default");
                }
                return new VideoDecoder(this);
            }
        }

        private VideoDecoder(Builder builder) throws IOException {
            mFormat = MediaFormat.createVideoFormat(builder.type.mime, builder.width, builder.height);
            mFormat.setInteger(FormatKey.Video.FRAME_RATE, builder.frameRate);
            mFormat.setInteger(FormatKey.Video.COLOR_FORMAT, (builder.colorFormat == 0 ? FormatValue.Video.All : builder.colorFormat));
            //String name = findCodec(mFormat, false);
            //mCodec = MediaCodec.createByCodecName(name);
            mCodec = MediaCodec.createDecoderByType(builder.type.mime);
            mOutputSurface = builder.outputSurface;
        }

        @Override
        public void configure() {
            mCodec.configure(mFormat, mOutputSurface, null, 0);
        }

    }

    // 音频解码器
    public static class AudioDecoder extends Decoder {

        @Override
        public void configure() {

        }
    }

    // 封装
    public static class Muxer {
        enum FileFormat {// 容器内部支持的编码视频格式有限制,
            MP4(MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4), // 音频 AAC LC/HE V1/HE V2/ELD; 视频 h263/ h264 avc BP
            GPP_3(MediaMuxer.OutputFormat.MUXER_OUTPUT_3GPP),
            OGG(MediaMuxer.OutputFormat.MUXER_OUTPUT_OGG),// 音频
            ;
            private final int format;

            FileFormat(int format) {
                this.format = format;
            }

            public int getFormat() {
                return format;
            }
        }

        private final MediaMuxer mMuxer;

        public Muxer(String path, FileFormat fileFormat) throws IOException {
            mMuxer = new MediaMuxer(path, fileFormat.format);
            //mMuxer.setLocation(1,1);
            //mMuxer.setOrientationHint(0);
        }

        public int addTrack(MediaFormat format) {
            return mMuxer.addTrack(format);
        }

        public void start() {
            mMuxer.start();
        }

        //trackIndex is from addTrack, buffer and info is from either MediaCodec or MediaExtractor
        public void write(int trackIndex, ByteBuffer buffer, MediaCodec.BufferInfo info) {
            mMuxer.writeSampleData(trackIndex, buffer, info);
        }

        public void release() {
            mMuxer.stop();
            mMuxer.release();
        }
    }


    public void main() {

    }



/*

    //val outFile = File(AppConstants.APP_MEDIA_SOURCE + File.separator + fileName)
    if (!outFile.exists()) {
        FileIOUtils.checkFileOrCreate(outFile)
        decodeToFile(outFile, colorFormat)
    } else {
        ToastUtils.show("$fileName 文件已生成")
    }

    fun decodeToFile(file:File, colorFormat:Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val thread = HandlerThread("decoder")
            thread.start()
            val handler = Handler(thread.looper)
            val afd = resources.openRawResourceFd(R.raw.out)
            handler.post {
                val demuxer = CodecUtils.Demuxer(afd)
                if (demuxer.selectDefaultVideoTrack()) {
                    val trackInfo = demuxer.currentTrackInfo
                    val decoder = CodecUtils.VideoDecoder.Builder()
                            .setType(CodecUtils.Codec.Type.H264)
                            .setWidth(trackInfo.width)
                            .setHeight(trackInfo.height)
                            .setFrameRate(trackInfo.frameRate)
                            .setColorFormat(colorFormat).build()
                    val fos = FileOutputStream(file, true)
                    decoder.configure()
                    decoder.start()
                    var hasData = true;
                    while (true) {
                        if (hasData) {
                            val inputBuffer = decoder.inputBuffer
                            if (inputBuffer != null) {
                                hasData = demuxer.readData(inputBuffer)
                                if (!hasData) {
                                    inputBuffer.endFlag()
                                }
                                decoder.fillData(inputBuffer)
                            }
                        }

                        val data = decoder.readData()
                        if (data == null) {
                            break
                        } else {
                            if (data.isNotEmpty()) {
                                fos.write(data, 0, data.size)
                            }
                        }
                    }
                    fos.close()
                    decoder.stop()
                    decoder.release()
                }

                demuxer.release()

                ToastUtils.show("解码完毕,原始数据为".plus(FileIOUtils.getFileName(file)))

                thread.quitSafely()
            }
        }
    }
*/

}
