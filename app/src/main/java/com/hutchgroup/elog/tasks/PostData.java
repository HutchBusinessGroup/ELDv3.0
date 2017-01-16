package com.hutchgroup.elog.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.hutchgroup.elog.common.GetCall;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.PostCall;
import com.hutchgroup.elog.common.Utility;

public class PostData extends AsyncTask<String, Void, Boolean> {
    String TAG = PostData.class.getName();

    private PostTaskListener<Boolean> postTaskListener;

    public PostData(PostTaskListener<Boolean> postTaskListener) {
        this.postTaskListener = postTaskListener;
    }

    @Override
    protected Boolean doInBackground(String... param) {
        //LogFile.write(TAG + "::PostData: called by user", LogFile.AUTOMATICALLY_TASK, LogFile.AUTOSYNC_LOG);
        boolean status = PostCall.PostAll();
        if (status) {
            status = PostCall.PostAccount();
            if (status) {
                PostCall.PostDVIR();
                PostCall.PostDTC();
                PostCall.PostAlert();
                PostCall.PostTPMS();
                PostCall.PostTrailerStatus();
                PostCall.PostVehicleInfo();
            }
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


