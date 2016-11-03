package com.hutchgroup.elog.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.hutchgroup.elog.common.GetCall;
import com.hutchgroup.elog.common.PostCall;
import com.hutchgroup.elog.common.Utility;

public class MessageSyncData extends AsyncTask<String, Void, Boolean> {
    String TAG = MessageSyncData.class.getName();

    private PostTaskListener<Boolean> postTaskListener;

    public MessageSyncData(PostTaskListener<Boolean> postTaskListener){
        this.postTaskListener = postTaskListener;
    }

    @Override
    protected Boolean doInBackground(String... param) {
        boolean status = GetCall.MessageSync();
        if (status) {
            PostCall.POSTMessageSYNC();
        }
        return status;
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



