package com.hutchgroup.elog.tasks;

import android.os.AsyncTask;
import com.hutchgroup.elog.common.GetCall;
import com.hutchgroup.elog.common.Utility;

public class LogInfoSyncData extends AsyncTask<String, Void, Boolean> {
    String TAG = SyncData.class.getName();

    private PostTaskListener<Boolean> postTaskListener;

    public LogInfoSyncData(PostTaskListener<Boolean> postTaskListener){
        this.postTaskListener = postTaskListener;
    }

    @Override
    protected Boolean doInBackground(String... param) {
        String loginDate = param[0];
        boolean result = GetCall.LogInfoSync(loginDate, Utility.getCurrentDate());
        return result;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean result) {

        if (result != null && postTaskListener != null)
            postTaskListener.onPostTask(result);
    }

    public interface PostTaskListener<K> {
        void onPostTask(K result);
    }
}

