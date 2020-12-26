package com.android.iflyings.mediasyncplayer;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Calendar;


public class CrashHandler implements Thread.UncaughtExceptionHandler {
    public static final String TAG = "CrashHandler";

    // 系统默认的UncaughtException处理类
    private Thread.UncaughtExceptionHandler mDefaultHandler;
    // CrashHandler实例
    @SuppressLint("StaticFieldLeak")
    private final static CrashHandler INSTANCE = new CrashHandler();
    // 程序的Context对象
    private Context mContext;

    /**
     * 保证只有一个CrashHandler实例
     */
    private CrashHandler() {
    }

    /**
     * 获取CrashHandler实例 ,单例模式
     */
    public static CrashHandler getInstance() {
        return INSTANCE;
    }

    /**
     * 初始化
     *
     * @param context 上下文
     */
    public void init(Context context) {
        mContext = context;
        // 获取系统默认的UncaughtException处理器
        mDefaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        // 设置该CrashHandler为程序的默认处理器
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    /**
     * 当 UncaughtException 发生时会转入该函数来处理
     */
    @Override
    public void uncaughtException(@NonNull Thread thread, @NonNull Throwable e) {
        if (!handleException(e) && mDefaultHandler != null) {
            // 如果用户没有处理则让系统默认的异常处理器来处理
            mDefaultHandler.uncaughtException(thread, e);
        } else {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Log.e(TAG, "error : ", ex);
            }
            // 退出程序
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(1);
        }
    }

    public static void writeErrorInfoToFile(String text) {
        String sb = "\n\n" + collectTimeInfo() + text + "\n\n";
        saveErrorInfoToFile(sb);
    }

    private boolean handleException(Throwable e) {
        if (e == null) {
            return false;
        }

        String sb = collectTimeInfo() +
                collectDeviceInfo(mContext) +
                collectThrowableInfo(e);
        saveErrorInfoToFile(sb);

        return false;
    }

    private static String collectTimeInfo() {
        StringBuilder sb = new StringBuilder();
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH) + 1;
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int second = calendar.get(Calendar.SECOND);
        String timeInfo = year + "-" + month + "-" + day + " " + hour + ":" + minute + ":" + second;

        sb.append("\n");
        sb.append("****************************************");
        sb.append(timeInfo);
        sb.append("****************************************");
        sb.append("\n");

        return sb.toString();
    }

    private String collectDeviceInfo(Context context) {
        StringBuilder sb = new StringBuilder();
        try {
            PackageInfo pi = context.getPackageManager().getPackageInfo(context.getPackageName(),
                    PackageManager.GET_ACTIVITIES);
            if (pi != null) {
                sb.append("versionName:").append(pi.versionName).append("\n");
                sb.append("versionCode:").append(pi.versionCode).append("\n");
            }
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "an error occur when collect package info", e);
        }
        /*
        Field[] fields = Build.class.getDeclaredFields();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                sb.append(field.getName()).append(":").append(Objects.requireNonNull(field.get(null)).toString()).append("\n");
                //Log.d(TAG, field.getName() + " : " + field.get(null));
            } catch (Exception e) {
                Log.e(TAG, "an error occur when collect crash info", e);
            }
        }*/
        return sb.toString();
    }

    /**
     *
     * @param throwable 异常
     * @return 错误信息
     */

    private String collectThrowableInfo(Throwable throwable) {
        Writer writer = new StringWriter();
        PrintWriter printWriter = new PrintWriter(writer);
        throwable.printStackTrace(printWriter);
        Throwable cause = throwable.getCause();
        while (cause != null) {
            cause.printStackTrace(printWriter);
            cause = cause.getCause();
        }
        printWriter.close();
        return writer.toString() + "\n";
    }

    /**
     * 保存错误信息到文件
     */
    private static void saveErrorInfoToFile(String errorInfo) {
        try {
            String fileName = "errorLog.txt";
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                String path = Environment.getExternalStorageDirectory() + "/errorLog";
                Log.e(TAG, path);
                File dir = new File(path);
                if (!dir.exists() && !dir.mkdir()) {
                    Log.e(TAG, "create errorLog folder error!!!");
                    return;
                }
                File file = new File(path + File.separator + fileName);
                if (!file.exists() && !file.createNewFile()) {
                    Log.e(TAG, "create errorLog.txt file error!!!");
                    return;
                } else {
                    if (file.length() > 64 * 1024) {
                        if (!file.delete() || !file.createNewFile()) {
                            Log.e(TAG, "recreate errorLog.txt file error!!!");
                            return;
                        }
                    }
                }
                FileWriter fileWriter = new FileWriter(file, true);
                fileWriter.write(errorInfo);
                fileWriter.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
