package com.android.iflyings.mediasyncplayer.util;

import java.io.FileInputStream;
import java.io.IOException;

import kotlin.text.Charsets;

public class FileUtils {

    public static String readStrFromFile(String filePath) throws IOException {
        FileInputStream fis = new FileInputStream(filePath);
        byte[] buffer = new byte[fis.available()];
        String text = null;
        if (0 < fis.read(buffer)) {
            text = new String(buffer, Charsets.UTF_8);
        }
        fis.close();
        return text;
    }


}
