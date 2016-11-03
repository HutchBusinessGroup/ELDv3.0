package com.hutchgroup.elog.tasks;

import android.os.AsyncTask;
import android.util.Log;

import com.hutchgroup.elog.beans.VersionInformationBean;
import com.hutchgroup.elog.common.GetCall;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.db.VersionInformationDB;

public class AppUpdateData extends AsyncTask<String, Void, Boolean> {
    String TAG = AppUpdateData.class.getName();

    private PostTaskListener<Boolean> postTaskListener;

    public AppUpdateData(PostTaskListener<Boolean> postTaskListener){
        this.postTaskListener = postTaskListener;
    }

    @Override
    protected Boolean doInBackground(String... param) {
        Log.i(TAG, "AppUpdateData running");
        boolean status;
        try {
            String version = param[0];//pInfo.versionName;
            Log.i(TAG, "current version=" + version);

            boolean checkUpdate = GetCall.checkUpdate(version);
            if (checkUpdate) {
                VersionInformationBean bean = VersionInformationDB.getVersionInformation();
                int newVersion = Integer.valueOf(bean.getCurrentVersion().replace(".", ""));

                Log.i(TAG, "new version=" + newVersion);

                int oldVersion = Integer.valueOf(version.replace(".", ""));
                if (newVersion > oldVersion) {
                    status = true;
                } else {
                    status = false;
                }
            } else {
                status = false;
            }
        } catch (Exception e) {
            LogFile.write(AppUpdateData.class.getName() + "::Version Update Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            status = false;
        }

        return status;
    }

    @Override
    protected void onPreExecute() {
        // showMainView(false);
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



