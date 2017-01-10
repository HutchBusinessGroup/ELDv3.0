package com.hutchgroup.elog.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.hutchgroup.elog.common.GetCall;
import com.hutchgroup.elog.common.Utility;

public class SyncData extends AsyncTask<String, Void, Boolean> {
    String TAG = SyncData.class.getName();

    private PostTaskListener<Boolean> postTaskListener;

    public SyncData(PostTaskListener<Boolean> postTaskListener) {
        this.postTaskListener = postTaskListener;
    }

    @Override
    protected Boolean doInBackground(String... param) {
        Log.i(TAG, "Sync data");
        boolean status = GetCall.CarrierInfoSync();
        if (status) {
            status = GetCall.AccountSync(0);
            if (status) {
                status = GetCall.TrailerInfoGetSync();
                if (status) {
                    status = GetCall.AxleInfoGetSync();
                    if (status) {
                        String date = Utility.getCurrentDate() + " 00:00:00";
                        GetCall.LogEventSync(date);
                    }
                }
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



