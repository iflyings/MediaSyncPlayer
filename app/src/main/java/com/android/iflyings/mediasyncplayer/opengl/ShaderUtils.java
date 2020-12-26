package com.android.iflyings.mediasyncplayer.opengl;

import android.opengl.GLES30;
import android.util.Log;

import com.android.iflyings.mediasyncplayer.MyApplication;

import java.io.IOException;
import java.io.InputStream;
import java.nio.Buffer;


public class ShaderUtils {

    private final static String TAG = "ShaderUtils";

    private ShaderUtils() {}

    public static int compileVertexShader(String shaderCode) {
        return compileShader(GLES30.GL_VERTEX_SHADER, shaderCode);
    }

    public static int compileFragmentShader(String shaderCode) {
        return compileShader(GLES30.GL_FRAGMENT_SHADER, shaderCode);
    }

    private static int compileShader(int type, String shaderCode) {
        // 1.创建一个新的着色器对象
        int shaderObjectId = GLES30.glCreateShader(type);
        // 2.获取创建状态
        if (shaderObjectId == 0) {
            // 在OpenGL中，都是通过整型值去作为OpenGL对象的引用。之后进行操作的时候都是将这个整型值传回给OpenGL进行操作。
            // 返回值0代表着创建对象失败。
            return 0;
        }
        // 3.将着色器代码上传到着色器对象中
        GLES30.glShaderSource(shaderObjectId, shaderCode);
        // 4.编译着色器对象
        GLES30.glCompileShader(shaderObjectId);
        // 5.获取编译状态：OpenGL将想要获取的值放入长度为1的数组的首位
        int [] compileStatus = new int[1];
        GLES30.glGetShaderiv(shaderObjectId, GLES30.GL_COMPILE_STATUS, compileStatus, 0);
        // 6.验证编译状态
        if (compileStatus[0] == 0) {
            // 编译失败获取出错信息
            Log.e(TAG, "compileShader: " + GLES30.glGetShaderInfoLog(shaderObjectId));
            // 删除创建的着色器对象
            GLES30.glDeleteShader(shaderObjectId);
            // 7.返回着色器对象：失败，为0
            return 0;
        }
        // 7.返回着色器对象：成功，非0
        return shaderObjectId;
    }

    public static int linkProgram(int vertexShaderId, int fragmentShaderId) {
        if (vertexShaderId == 0 || fragmentShaderId == 0) {
            return 0;
        }
        // 1.创建一个OpenGL程序对象
        int programObjectId = GLES30.glCreateProgram();
        // 2.获取创建状态
        if (programObjectId == 0) {
            return 0;
        }
        // 3.将顶点着色器依附到OpenGL程序对象
        GLES30.glAttachShader(programObjectId, vertexShaderId);
        checkError();
        // 3.将片段着色器依附到OpenGL程序对象
        GLES30.glAttachShader(programObjectId, fragmentShaderId);
        checkError();
        // 4.将两个着色器链接到OpenGL程序对象
        GLES30.glLinkProgram(programObjectId);
        checkError();
        // 5.获取链接状态：OpenGL将想要获取的值放入长度为1的数组的首位
        int[] linkStatus = new int[1];
        GLES30.glGetProgramiv(programObjectId, GLES30.GL_LINK_STATUS, linkStatus, 0);
        // 6.验证链接状态
        if (linkStatus[0] == 0) {
            // 链接失败获取出错信息
            Log.e(TAG, "linkProgram: " + GLES30.glGetProgramInfoLog(programObjectId));
            // 删除程序对象
            GLES30.glDeleteProgram(programObjectId);
            // 7.返回程序对象：失败，为0
            return 0;
        }
        // 7.返回程序对象：成功，非0
        return programObjectId;
    }

    public static int assembleProgram(String shaderCode, String fragmentShader) {
        int vertexShaderId = compileVertexShader(shaderCode);
        if (vertexShaderId == 0) {
            throw new IllegalStateException("vertexShader:\n" + shaderCode);
        }
        int fragmentShaderId = compileFragmentShader(fragmentShader);
        if (fragmentShaderId == 0) {
            throw new IllegalStateException("fragmentShader:\n" + fragmentShader);
        }
        checkError();

        return linkProgram(vertexShaderId, fragmentShaderId);
    }

    public static void checkError() {
        int error;
        while((error = GLES30.glGetError()) != GLES30.GL_NO_ERROR) {
            Throwable t = new Throwable();
            Log.e("zw", "GL error: " + error, t);
            throw new IllegalStateException("GL error: " + error);
        }
    }

    public static String readRawTextFile(int resId) {
        InputStream inputStream = MyApplication.getApplicationResources().openRawResource(resId);
        try {
            byte[] byteArray = new byte[inputStream.available()];
            int byteLen = inputStream.read(byteArray);
            return new String(byteArray, 0, byteLen);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static int glGenBuffers(Buffer vertexBuffer, int size) {
        int [] bufferIds = new int[1];
        GLES30.glGenBuffers(1, bufferIds, 0);//申请一个缓冲区
        if (bufferIds[0] != 0) {
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, bufferIds[0]);//绑定缓冲区
            GLES30.glBufferData(GLES30.GL_ARRAY_BUFFER, vertexBuffer.capacity() * size, vertexBuffer, GLES30.GL_STATIC_DRAW);//把数据存储到GPU中
            GLES30.glBindBuffer(GLES30.GL_ARRAY_BUFFER, 0);
        }
        return bufferIds[0];
    }

    public static void glDeleteBuffers(int bufferId) {
        int [] bufferIds = new int[1];
        bufferIds[0] = bufferId;
        GLES30.glDeleteBuffers(1, bufferIds, 0);//申请一个缓冲区

    }
}
