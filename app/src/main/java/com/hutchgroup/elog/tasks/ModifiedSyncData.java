package com.hutchgroup.elog.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.hutchgroup.elog.common.GetCall;
import com.hutchgroup.elog.common.PostCall;
import com.hutchgroup.elog.common.Utility;

public class ModifiedSyncData extends AsyncTask<String, Void, Boolean> {
    String TAG = ModifiedSyncData.class.getName();

    private PostTaskListener<Boolean> postTaskListener;

    public ModifiedSyncData(PostTaskListener<Boolean> postTaskListener){
        this.postTaskListener = postTaskListener;
    }
    @Override
    protected Boolean doInBackground(String... param) {
        boolean status = GetCall.EditRequestSync();
        if (status) {
            PostCall.POSTEVENTSYNC(3, 3);
        }
        return status;

    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean result) {

//
        if (result != null && postTaskListener != null) {
            postTaskListener.onPostTask(result);
        }
    }

    public interface PostTaskListener<K> {
        void onPostTask(K result);
    }
}


