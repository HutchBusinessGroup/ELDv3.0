package com.hutchgroup.elog.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.hutchgroup.elog.common.GetCall;
import com.hutchgroup.elog.common.Utility;

public class DownloadConfiguration extends AsyncTask<String, Void, Boolean> {
    String TAG = DownloadConfiguration.class.getName();

    private PostTaskListener<Boolean> postTaskListener;

    public DownloadConfiguration(PostTaskListener<Boolean> postTaskListener){
        this.postTaskListener = postTaskListener;
    }

    @Override
    protected Boolean doInBackground(String... param) {
        Log.i(TAG, "Sync data");
        boolean status = GetCall.CarrierInfoSync();
        if (status) {
            status = GetCall.AccountSync(0);
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



