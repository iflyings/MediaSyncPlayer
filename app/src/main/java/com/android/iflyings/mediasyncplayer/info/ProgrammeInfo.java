package com.android.iflyings.mediasyncplayer.info;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;


public class ProgrammeInfo {

    private ArrayList<WindowInfo> mWindowList;
    private String mProgrammeName;

    private ProgrammeInfo() {}

    public String getProgrammeName() {
        return mProgrammeName;
    }
    public List<WindowInfo> getWindowList() {
        return mWindowList;
    }

    public JSONObject toJSONObject() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", mProgrammeName);
        JSONArray list = new JSONArray();
        for (int i = 0;i < mWindowList.size();i ++) {
            list.put(mWindowList.get(i).toJSONObject());
        }
        jsonObject.put("list", list);
        return jsonObject;
    }

    public static ProgrammeInfo from(JSONObject jsonObject) throws JSONException {
        ProgrammeInfo pi = new ProgrammeInfo();
        pi.mProgrammeName = jsonObject.getString("name");
        JSONArray list = jsonObject.getJSONArray("list");
        pi.mWindowList = new ArrayList<>(list.length());
        for (int i = 0;i < list.length();i ++) {
            pi.mWindowList.add(WindowInfo.from(list.getJSONObject(i)));
        }
        return pi;
    }
}
