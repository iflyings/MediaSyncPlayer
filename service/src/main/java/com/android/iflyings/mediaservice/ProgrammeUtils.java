package com.android.iflyings.mediaservice;

import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;


public class ProgrammeUtils {

    public static JSONObject createProgrammeObject() throws JSONException {
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        JSONObject po = new JSONObject();
        po.put("name", "proj1");
        JSONArray wa = new JSONArray();
        wa.put(createBlockObject(file));
        po.put("list", wa);
        return po;
    }

    private static JSONObject createBlockObject(File file) throws JSONException {
        JSONObject wo = new JSONObject();
        wo.put("type", "multi");
        wo.put("name", file.getName());
        wo.put("left", 0);
        wo.put("top", 0);
        wo.put("width", 1920);
        wo.put("height", 1080);
        JSONArray ba = new JSONArray();

        ba.put(createBlockObject(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), 0, 0, 960, 540, true));
        //ba.put(createBlockObject(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), 960, 0, 960, 540, true));
        //ba.put(createBlockObject(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), 0, 540, 960, 540, false));
        //ba.put(createBlockObject(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), 960, 540, 960, 540, false));
        //ba.put(createBlockObject(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), 480, 270, 960, 540));
        //ba.put(createBlockObject(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), 0, 0, 320, 240));
        //ba.put(createBlockObject(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), 0, 1080 - 240, 320, 240));

        wo.put("list", ba);
        return wo;
    }

    private static JSONObject createBlockObject(File file, int left, int top, int width, int height,
                                                boolean hardCodec) throws JSONException {
        JSONObject bo = new JSONObject();
        bo.put("name", file.getName());
        bo.put("type", "block");
        bo.put("left", left);
        bo.put("top", top);
        bo.put("width", width);
        bo.put("height", height);
        JSONArray va = new JSONArray();
        if (file.isDirectory()) {
            File[] array = file.listFiles((dir, name) -> name.endsWith("mp4") || name.endsWith("jpg"));
            if (array != null) {
                Arrays.sort(array, (f1, f2) -> f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase()));
                for (int i = 0;i < array.length;i ++) {
                    File f = array[i];
                    JSONObject fo = new JSONObject();
                    if (f.getName().endsWith("mp4")) {
                        fo.put("type", "video");
                        fo.put("codec", hardCodec);
                    } else if (f.getName().endsWith("jpg")) {
                        fo.put("type", "image");
                    }
                    fo.put("token", String.valueOf(i));
                    fo.put("path", f.getAbsolutePath());
                    va.put(fo);
                }
            }
        }
        bo.put("list", va);
        return bo;
    }

    private static JSONObject createSyncObject(File file) throws JSONException {
        JSONObject wo = new JSONObject();
        wo.put("type", "sync");
        wo.put("name", file.getName());
        wo.put("left", 0);
        wo.put("top", 0);
        wo.put("width", 1920);
        wo.put("height", 1080);
        JSONArray va = new JSONArray();
        if (file.isDirectory()) {
            File[] array = file.listFiles((dir, name) -> name.endsWith("mp4") || name.endsWith("jpg"));
            if (array != null) {
                Arrays.sort(array, (f1, f2) -> f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase()));
                for (int i = 0;i < array.length;i ++) {
                    File f = array[i];
                    JSONObject fo = new JSONObject();
                    if (f.getName().endsWith("mp4")) {
                        fo.put("type", "video");
                    } else if (f.getName().endsWith("jpg")) {
                        fo.put("type", "image");
                    }
                    fo.put("token", String.valueOf(i));
                    fo.put("path", f.getAbsolutePath());
                    va.put(fo);
                }
            }
        }
        wo.put("list", va);
        return wo;
    }
    private static JSONObject createVideoObject(File file) throws JSONException {
        JSONObject wo = new JSONObject();
        wo.put("type", "sync");
        wo.put("name", file.getName());
        wo.put("left", 0);
        wo.put("top", 0);
        wo.put("width", 640);
        wo.put("height", 480);
        JSONArray va = new JSONArray();
        if (file.isDirectory()) {
            File[] array = file.listFiles((dir, name) -> name.endsWith("mp4"));
            if (array != null) {
                Arrays.sort(array, (f1, f2) -> f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase()));
                for (File f : array) {
                    JSONObject fo = new JSONObject();
                    fo.put("type", "video");
                    fo.put("path", f.getAbsolutePath());
                    va.put(fo);
                }
            }
        }
        wo.put("list", va);
        return wo;
    }
    private static JSONObject createImageObject(File file) throws JSONException {
        JSONObject wo = new JSONObject();
        wo.put("type", "sync");
        wo.put("name", file.getName());
        wo.put("left", 0);
        wo.put("top", 0);
        wo.put("width", 640);
        wo.put("height", 480);
        JSONArray va = new JSONArray();
        if (file.isDirectory()) {
            File[] array = file.listFiles((dir, name) -> name.endsWith("jpg"));
            if (array != null) {
                Arrays.sort(array, (f1, f2) -> f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase()));
                for (File f : array) {
                    JSONObject fo = new JSONObject();
                    fo.put("token", "123");
                    fo.put("type", "image");
                    fo.put("path", f.getAbsolutePath());
                    va.put(fo);
                }
            }
        }
        wo.put("list", va);
        return wo;
    }
    private static JSONObject createSourceObject() throws JSONException {
        JSONObject wo = new JSONObject();
        wo.put("type", "source");
        wo.put("name", "source1");
        wo.put("left", 0);
        wo.put("top", 0);
        wo.put("width", 1920);
        wo.put("height", 1080);
        JSONArray sa = new JSONArray();
        JSONObject si = new JSONObject();
        si.put("type", "source");
        si.put("src", "HDMI1");
        sa.put(si);
        wo.put("list", sa);
        return wo;
    }


}
