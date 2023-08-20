package com.loy.kit.media.render;

import android.graphics.Bitmap;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.loy.kit.Utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * @author Loy
 * @time 2021/12/27 17:48
 * @des
 */
public class ShaderHelper {

    // short 数组转buffer
    public static ShortBuffer getCoordinateShortBuffer(short[] coordinate) {
        ShortBuffer shortBuffer = ByteBuffer.allocateDirect(coordinate.length * 2)
                .order(ByteOrder.nativeOrder())
                .asShortBuffer()
                .put(coordinate);
        shortBuffer.position(0);
        return shortBuffer;
    }

    // float 数组转buffer, 常用于顶点坐标
    public static FloatBuffer getCoordinateFloatBuffer(float[] coordinate) {
        FloatBuffer floatBuffer = ByteBuffer.allocateDirect(coordinate.length * 4)
                .order(ByteOrder.nativeOrder())
                .asFloatBuffer()
                .put(coordinate);
        floatBuffer.position(0);
        return floatBuffer;
    }

    // 着色器类型, 就顶点着色器和纹理着色器
    public enum ShaderType {
        VERTEX(GLES20.GL_VERTEX_SHADER), FRAGMENT(GLES20.GL_FRAGMENT_SHADER);

        private int type;

        private ShaderType(int type) {
            this.type = type;
        }

        public int getType() {
            return type;
        }
    }

    //纹理类型, 2d , oes
    public enum TextureType {
        Sample2D(GLES20.GL_TEXTURE_2D), OES(GLES11Ext.GL_TEXTURE_EXTERNAL_OES),
        ;
        private int value;

        private TextureType(int v) {
            this.value = v;
        }

        public int getValue() {
            return value;
        }
    }

    // 纹理单元, 一共32个, 第0个默认激活, 其他需要显性指定.
    // 纹理对象的纹理数据只能通过纹理单元才能传递给着色器中的采样器对象,可以这样理解,纹理对象存储了纹理数据,纹理单元则是纹理数据的访问接口.
    public enum TextureUnit {
        TEXTURE0(GLES20.GL_TEXTURE0),
        TEXTURE1(GLES20.GL_TEXTURE1),
        TEXTURE2(GLES20.GL_TEXTURE2),
        TEXTURE3(GLES20.GL_TEXTURE3),
        TEXTURE4(GLES20.GL_TEXTURE4),
        TEXTURE5(GLES20.GL_TEXTURE5),
        TEXTURE6(GLES20.GL_TEXTURE6),
        TEXTURE7(GLES20.GL_TEXTURE7),
        TEXTURE8(GLES20.GL_TEXTURE8),
        TEXTURE9(GLES20.GL_TEXTURE9),
        TEXTURE10(GLES20.GL_TEXTURE10),
        TEXTURE11(GLES20.GL_TEXTURE11),
        TEXTURE12(GLES20.GL_TEXTURE12),
        TEXTURE13(GLES20.GL_TEXTURE13),
        TEXTURE14(GLES20.GL_TEXTURE14),
        TEXTURE15(GLES20.GL_TEXTURE15),
        TEXTURE16(GLES20.GL_TEXTURE16),
        TEXTURE17(GLES20.GL_TEXTURE17),
        TEXTURE18(GLES20.GL_TEXTURE18),
        TEXTURE19(GLES20.GL_TEXTURE19),
        TEXTURE20(GLES20.GL_TEXTURE20),
        TEXTURE21(GLES20.GL_TEXTURE21),
        TEXTURE22(GLES20.GL_TEXTURE22),
        TEXTURE23(GLES20.GL_TEXTURE23),
        TEXTURE24(GLES20.GL_TEXTURE24),
        TEXTURE25(GLES20.GL_TEXTURE25),
        TEXTURE26(GLES20.GL_TEXTURE26),
        TEXTURE27(GLES20.GL_TEXTURE27),
        TEXTURE28(GLES20.GL_TEXTURE28),
        TEXTURE29(GLES20.GL_TEXTURE29),
        TEXTURE30(GLES20.GL_TEXTURE30),
        TEXTURE31(GLES20.GL_TEXTURE31),
        ;
        private int value;

        TextureUnit(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }

        public int getUnitIndex() {
            return value - GLES20.GL_TEXTURE0;
        }

        public static TextureUnit getUnitByIndex(int index) {
            return TextureUnit.values()[index];
        }
    }

    // 独立封装内存溢出异常
    public static class GlOutOfMemoryException extends RuntimeException {
        public GlOutOfMemoryException(String msg) {
            super(msg);
        }
    }

    // 创建纹理, 需指定纹理类型和纹理层, 返回纹理句柄
    public static int createTexture(TextureType type) {
        int texId = genTexture();

        setTextureParam(type.getValue(), texId);

        return texId;
    }

    private static int genTexture() {
        final int[] textureArray = new int[1];
        GLES20.glGenTextures(1, textureArray, 0);
        checkNoGLES2Error("GLES20.glGenTextures");
        return textureArray[0];
    }

    private static void setTextureParam(int texture, int texId) {
        GLES20.glBindTexture(texture, texId);
        GLES20.glTexParameteri(texture, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);     // 放大过滤方式
        GLES20.glTexParameteri(texture, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);    // 缩小过滤方式
        GLES20.glTexParameteri(texture, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glTexParameteri(texture, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
        GLES20.glBindTexture(texture, 0);
    }

    // 检测着色器程序的编译错误, fail-fast
    public static void checkCompileShaderError(int shader) {
        int[] compileStatus = new int[]{GLES20.GL_FALSE};
        GLES20.glGetShaderiv(shader, GLES20.GL_COMPILE_STATUS, compileStatus, 0);//校验编译
        if (compileStatus[0] != GLES20.GL_TRUE) {
            throw new RuntimeException(GLES20.glGetShaderInfoLog(shader));
        }
    }

    // 加载编译shader, 返回着色器句柄
    public static int loadShader(ShaderType type, String shaderStr) {
        int shader = GLES20.glCreateShader(type.getType());
        GLES20.glShaderSource(shader, shaderStr);
        GLES20.glCompileShader(shader);
        checkCompileShaderError(shader);
        return shader;
    }

    // 检查显卡程序创建错误
    public static void checkCreateProgramError(int program) {
        if (program == 0) {
            throw new RuntimeException("GLES20.glCreateProgram() error: " + GLES20.glGetError());
        }
    }

    // 检测显卡程序链接错误
    public static void checkLinkProgramError(int program) {
        int[] linkStatus = new int[]{GLES20.GL_FALSE};
        GLES20.glGetProgramiv(program, GLES20.GL_LINK_STATUS, linkStatus, 0);
        if (linkStatus[0] != GLES20.GL_TRUE) {
            throw new RuntimeException(GLES20.glGetProgramInfoLog(program));
        }
    }

    // 创建显卡程序, 需要顶点和片元着色器句柄, 返回程序句柄
    public static int createProgram(int vertexShader, int fragmentShader) {
        int program = GLES20.glCreateProgram();
        checkCreateProgramError(program);
        GLES20.glAttachShader(program, vertexShader);
        GLES20.glAttachShader(program, fragmentShader);
        GLES20.glLinkProgram(program);
        checkLinkProgramError(program);
        return program;
    }

    // 获取程序中的 attribute 属性, 需要程序句柄和变量属性名, 返回属性句柄
    public static int getAttribLocation(int program, String attrName) {
        if (program == -1) {
            throw new RuntimeException("The program has been released");
        }
        int location = GLES20.glGetAttribLocation(program, attrName);
        if (location < 0) {
            throw new RuntimeException("Could not locate '" + attrName + "' in program");
        }
        return location;
    }

    // 获取程序中的 uniform 属性, 需要程序句柄和变量属性名, 返回属性句柄
    public static int getUniformLocation(int program, String uniformName) {
        if (program == -1) {
            throw new RuntimeException("The program has been released");
        }
        int location = GLES20.glGetUniformLocation(program, uniformName);
        if (location < 0) {
            throw new RuntimeException("Could not locate uniform '" + uniformName + "' in program");
        }
        return location;
    }

    // 检查 GLES 错误
    public static void checkNoGLES2Error(String msg) {
        int error = GLES20.glGetError();
        if (error != GLES20.GL_NO_ERROR) {
            throw error == GLES20.GL_OUT_OF_MEMORY
                    ? new GlOutOfMemoryException(msg)
                    : new RuntimeException(msg + ", error: " + GLES20.glGetString(error));
        }
    }

    // 使用程序
    public static void useProgram(int program) {
        if (program == -1) {
            throw new RuntimeException("The program has been released");
        }
        synchronized (EGLHelper.lock) {
            GLES20.glUseProgram(program);
        }
        checkNoGLES2Error("glUseProgram");
    }

    // 释放程序
    public static void releaseProgram(int program) {
        if (program == -1) {
            throw new RuntimeException("The program has been released");
        }
        synchronized (EGLHelper.lock) {
            GLES20.glDeleteProgram(program);
        }
        checkNoGLES2Error("glDeleteProgram");
    }

    // 释放着色器, 需要着色器句柄
    public static void releaseShader(int shader) {
        GLES20.glDeleteShader(shader);
    }

    // 释放纹理
    public static void destroyTextures(int num, int[] texIds) {
        GLES20.glDeleteTextures(num, texIds, 0);
    }

    // 释放单个纹理
    public static void destroyTexture(int texIds) {
        GLES20.glDeleteTextures(1, new int[]{texIds}, 0);
    }

    public static int decodeBitmapTo2DTexture(Bitmap bitmap) {
        int texId = genTexture();

        loadBitmapToTexture(texId, bitmap);

        return texId;
    }

    public static void loadBitmapToTexture(int textureId, Bitmap bitmap) {
        //告诉OpenGL后面纹理调用应该是应用于哪个纹理对象
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId);

        //设置缩小的时候（GL_TEXTURE_MIN_FILTER）使用mipmap三线程过滤
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR_MIPMAP_LINEAR);
        //设置放大的时候（GL_TEXTURE_MAG_FILTER）使用双线程过滤
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        //Android设备y坐标是反向的，正常图显示到设备上是水平颠倒的，解决方案就是设置纹理包装，纹理T坐标（y）设置镜面重复
        //ball读取纹理的时候  t范围坐标取正常值+1
        //GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_MIRRORED_REPEAT);

        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0);

        bitmap.recycle();

        //快速生成mipmap贴图
        GLES20.glGenerateMipmap(GLES20.GL_TEXTURE_2D);

        //解除纹理操作的绑定
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
    }

    public static class FrameBufferHandle {
        private final int[] frameBufferIds;
        private final int[] frameBufferTextureIds;
        private final int pixelFormat;

        public FrameBufferHandle() {
            this(GLES20.GL_RGBA);
        }

        public FrameBufferHandle(int pixelFormat) {
            this.frameBufferIds = new int[]{GLES20.GL_NONE};
            this.frameBufferTextureIds = new int[]{GLES20.GL_NONE};
            switch (pixelFormat) {
                case GLES20.GL_LUMINANCE:
                case GLES20.GL_RGB:
                case GLES20.GL_RGBA:
                    this.pixelFormat = pixelFormat;
                    break;
                default:
                    throw new IllegalArgumentException("Invalid pixel format: " + pixelFormat);
            }
        }

        public int getBufferId() {
            return frameBufferIds[0];
        }

        public int getTextureId() {
            return frameBufferTextureIds[0];
        }

        public void create(int width, int height) {
            GLES20.glGenFramebuffers(1, frameBufferIds, 0);

            GLES20.glGenTextures(1, frameBufferTextureIds, 0);

            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, getTextureId());

            GLES20.glTexImage2D(GLES20.GL_TEXTURE_2D, 0, pixelFormat, width, height, 0, pixelFormat, GLES20.GL_UNSIGNED_BYTE, null);

            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, 0);
            checkNoGLES2Error("FrameBufferHandle create");

            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, getBufferId());

            GLES20.glFramebufferTexture2D(GLES20.GL_FRAMEBUFFER, GLES20.GL_COLOR_ATTACHMENT0, GLES20.GL_TEXTURE_2D, getTextureId(), 0);
            // Check that the framebuffer is in a good state.
            final int status = GLES20.glCheckFramebufferStatus(GLES20.GL_FRAMEBUFFER);
            if (status != GLES20.GL_FRAMEBUFFER_COMPLETE) {
                throw new IllegalStateException("Framebuffer not complete, status: " + status);
            }
            GLES20.glBindFramebuffer(GLES20.GL_FRAMEBUFFER, 0);
        }

        public void destroy() {
            if (getTextureId() != GLES20.GL_NONE) {
                GLES20.glDeleteTextures(1, frameBufferTextureIds, 0);
                frameBufferTextureIds[0] = GLES20.GL_NONE;
            }
            if (getBufferId() != GLES20.GL_NONE) {
                GLES20.glDeleteFramebuffers(1, frameBufferIds, 0);
                frameBufferIds[0] = GLES20.GL_NONE;
            }
        }
    }

    public static String readShaderFromRawResource(final int resourceId) {
        final InputStream inputStream = Utils.getAppContext().getResources().openRawResource(resourceId);
        final InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
        final BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

        String nextLine;
        final StringBuilder body = new StringBuilder();

        try {
            while ((nextLine = bufferedReader.readLine()) != null) {
                body.append(nextLine);
                body.append('\n');
            }
        } catch (IOException e) {
            return null;
        }
        return body.toString();
    }
}
