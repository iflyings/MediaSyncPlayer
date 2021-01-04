package com.android.iflyings.mediaservice;

import android.os.Environment;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.Arrays;


public class ProgrammeUtils {

    public static JSONObject createProgrammeObject() throws JSONException {
        File file = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
        JSONObject po = new JSONObject();
        po.put("name", "proj1");
        JSONArray wa = new JSONArray();
        wa.put(createWindowObject(file));
        po.put("list", wa);
        return po;
    }

    private static JSONObject createWindowObject(File file) throws JSONException {
        JSONObject wo = new JSONObject();
        wo.put("type", "multi");
        wo.put("name", file.getName());
        wo.put("left", 0);
        wo.put("top", 0);
        wo.put("width", 1920);
        wo.put("height", 1080);
        JSONArray ba = new JSONArray();

        ba.put(createVideoBlockObject(file, 0, 0, 960, 540));
        ba.put(createVideoBlockObject(file, 960, 0, 960, 540));
        ba.put(createVideoBlockObject(file, 0, 540, 960, 540));
        ba.put(createImageBlockObject(file, 960, 540, 960, 540));
        wo.put("list", ba);
        return wo;
    }

    private static JSONObject createScrollTextObject(String text) throws JSONException {
        JSONObject so = new JSONObject();
        so.put("content", text);
        so.put("location", "bottom");
        so.put("fontColor", "#FFFF0000");
        so.put("fontSize", 30);
        so.put("typeface", "msyh");
        return so;
    }

    private static JSONObject createTextBlockObject(File file, int l, int t, int r, int b) throws JSONException {
        JSONObject wo = new JSONObject();
        wo.put("name", file.getName());
        wo.put("left", l);
        wo.put("top", t);
        wo.put("width", r);
        wo.put("height", b);
        JSONArray va = new JSONArray();
        if (file.isDirectory()) {
            File[] array = file.listFiles((dir, name) -> name.endsWith("txt"));
            if (array != null) {
                Arrays.sort(array, (f1, f2) -> f1.getName().toLowerCase().compareTo(f2.getName().toLowerCase()));
                for (File f : array) {
                    JSONObject fo = new JSONObject();
                    fo.put("type", "text");
                    fo.put("delay", 500);
                    fo.put("step", 10);
                    fo.put("fontSize", 30);
                    fo.put("path", f.getAbsolutePath());
                    va.put(fo);
                }
            }
        }
        wo.put("list", va);
        return wo;
    }
    private static JSONObject createVideoBlockObject(File file, int l, int t, int r, int b) throws JSONException {
        JSONObject wo = new JSONObject();
        wo.put("name", file.getName());
        wo.put("left", l);
        wo.put("top", t);
        wo.put("width", r);
        wo.put("height", b);
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
    private static JSONObject createImageBlockObject(File file, int l, int t, int r, int b) throws JSONException {
        JSONObject wo = new JSONObject();
        wo.put("name", file.getName());
        wo.put("left", l);
        wo.put("top", t);
        wo.put("width", r);
        wo.put("height", b);
        wo.put("text", createScrollTextObject("除去泰戈尔清新自然的文笔，在《飞鸟集》中，我更多感受到的是一种对生活的热爱以及对爱的思索。毫无疑问，泰戈尔的灵感来源于生活，但同时更高于生活；他用自己对生活的热爱，巧妙地隐去了一些苦难与黑暗，而将所剩的光明与微笑毫无保留地献给了读者。他对爱的思索，更是涵盖了多个方面，包括青年男女间纯真的爱情、母亲对孩子永存的母爱、人与自然间难以言喻的爱……尤其是对于爱情，泰戈尔毫不吝啬地运用了大量的比喻修辞来赞美爱情的美好与伟大。在泰戈尔眼中，世界需要爱，人生更需要爱，正如他在《飞鸟集》中所写的一样：“我相信你的爱，就让这作为我最后的话吧。在另一方面，泰戈尔捕捉了大量关于自然界的灵感。他说天空的黄昏像一盏灯，说微风中的树叶像思绪的断片，说鸟儿的鸣唱是晨曦来自大地的回音；他将自然界的一切拟人化。他让天空和大海对话，让鸟儿和云对话，让花儿和太阳对话……总之，在泰戈尔的诗里，世界是人性化的，自然也是人性化的，万物都有它们自己的生长与思考；而他只是为它们的人性化整理思想碎片而已。而这，便也是《飞鸟集》名字的由来：“思想掠过我的心头，仿佛群群野鸭飞过天空，我听到了它们振翅高飞的声音。”这就是泰戈尔，这就是《飞鸟集》。或许，对于人类的文明史来讲，《飞鸟集》不过是沧海一粟而已；然而，我却只想说，它是一种别具一格的清新，在如今繁忙拥挤的都市里，用它蕴涵的广阔无边的自然荒野，为我们开创另一个天堂。1910年发表的哲理诗集《吉檀迦利》最早显示了泰戈尔的独特风格。从形式上看，这是一部献给神的颂歌，“吉檀迦利”就是“献诗”的意思。但泰戈尔歌颂的并不是“一神教”拥有绝对权威、巍然凌驾于万物之上的神，而是万物化成一体的泛神，是人人可以亲近、具有浓厚平民色彩的存在。诗人劝告那些盲目的顶礼膜拜者们：“把礼赞和数珠撇在一边罢！”因神并不在那幽暗的神殿里，“他是在锄着枯地的农夫那里/在敲石的造路工人那里/太阳下，阴雨里/他和他们同在/衣袍上蒙着尘土。”人们应该脱下圣袍，到泥土里去迎接神，“在劳动里，流汗里/和他站在一起罢。《吉檀迦利》所表现出的泛神论思想，无疑与印度古代典籍如《奥义书》等息息相通。但泰戈尔在发扬本民族传统的时候，并无意营造一个封闭的世界，他渴望长期隔绝的东西方能够不断接近、沟通。"));
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
