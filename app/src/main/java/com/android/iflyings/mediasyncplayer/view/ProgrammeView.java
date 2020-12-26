package com.android.iflyings.mediasyncplayer.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.iflyings.mediasyncplayer.impl.DebugPlayerImpl;
import com.android.iflyings.mediasyncplayer.info.ProgrammeInfo;
import com.android.iflyings.mediasyncplayer.info.WindowInfo;
import com.android.iflyings.mediasyncplayer.impl.SyncPlayerImpl;
import com.android.iflyings.mediasyncplayer.multi.MultiPlayerView;
import com.android.iflyings.mediasyncplayer.sync.SyncPlayerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


@SuppressLint("ViewConstructor")
public class ProgrammeView extends FrameLayout implements SyncPlayerImpl, DebugPlayerImpl {
    private ProgrammeInfo mProgrammeInfo;

    public ProgrammeView(@NonNull Context context) {
        this(context, null, 0, 0);
    }

    public ProgrammeView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ProgrammeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public ProgrammeView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    public void startPlayer(ProgrammeInfo programmeInfo) {
        mProgrammeInfo = programmeInfo;
        post(() -> {
            if (getChildCount() > 0) {
                removeAllViewsInLayout();
            }
            int index = 0;
            List<WindowInfo> wlist = programmeInfo.getWindowList();
            for (WindowInfo wi : wlist) {
                LayoutParams lp = new LayoutParams(wi.getWindowWidth(), wi.getWindowHeight());
                lp.gravity = Gravity.TOP | Gravity.START;
                lp.leftMargin = wi.getWindowLeft();
                lp.topMargin = wi.getWindowTop();
                switch (wi.getWindowType()) {
                    case WindowInfo.WINDOW_TYPE_SYNC: {
                        addViewInLayout(new SyncPlayerView(getContext(), wi), index++, lp);
                        break;
                    }
                    case WindowInfo.WINDOW_TYPE_SOURCE: {
                        addViewInLayout(new SourcePlayerView(getContext(), wi), index++, lp);
                        break;
                    }
                    case WindowInfo.WINDOW_TYPE_MULTI: {
                        addViewInLayout(new MultiPlayerView(getContext(), wi), index++, lp);
                        break;
                    }
                }

            }
            requestLayout();
        });
    }

    public void stopPlayer() {
        if (getChildCount() > 0) {
            removeAllViews();
        }
    }

    @Override
    public JSONObject getStartInfo() throws JSONException {
        if (mProgrammeInfo == null) {
            return null;
        }
        JSONObject po = new JSONObject();
        po.put("name", mProgrammeInfo.getProgrammeName());
        JSONArray wa = new JSONArray();
        for (int i = 0; i < getChildCount(); i ++) {
            if (getChildAt(i) instanceof SyncPlayerImpl) {
                JSONObject mo = ((SyncPlayerImpl) getChildAt(i)).getStartInfo();
                if (mo != null) {
                    wa.put(mo);
                }
            }
        }
        po.put("list",wa);
        return po;
    }

    @Override
    public int setStartInfo(JSONObject jsonObject) throws JSONException {
        if (mProgrammeInfo == null)
            return 0;
        String name = jsonObject.getString("name");
        if (name.equals(mProgrammeInfo.getProgrammeName())) {
            JSONArray wa = jsonObject.getJSONArray("list");
            for (int i = 0;i < wa.length();i ++) {
                JSONObject wo = wa.getJSONObject(i);
                for (int j = 0; j < getChildCount(); j ++) {
                    if (getChildAt(i) instanceof SyncPlayerImpl) {
                        if (0 != ((SyncPlayerImpl) getChildAt(i)).setStartInfo(wo)) {
                            return -1;
                        }
                    }
                }
            }
        }
        return 0;
    }

    @Override
    public JSONObject getUpdateInfo() throws JSONException {
        return null;
    }

    @Override
    public int setUpdateInfo(JSONObject jsonObject) throws JSONException {
        return 0;
    }

    @Override
    public String dumpPlayer() {
        StringBuilder sb = new StringBuilder();
        if (mProgrammeInfo == null) {
            sb.append("no programme info");
            sb.append("\n");
        } else {
            sb.append("ProgrammeName = ").append(mProgrammeInfo.getProgrammeName()).append("\n");
            for (int i = 0; i < getChildCount(); i++) {
                if (getChildAt(i) instanceof DebugPlayerImpl) {
                    sb.append("  Window ").append(i).append(" ");
                    sb.append(((DebugPlayerImpl) getChildAt(i)).dumpPlayer());
                    sb.append("\n");
                }
            }
        }
        return sb.toString();
    }
}
