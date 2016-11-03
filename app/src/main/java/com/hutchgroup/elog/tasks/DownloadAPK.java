package com.hutchgroup.elog.tasks;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.hutchgroup.elog.beans.VersionInformationBean;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.db.VersionInformationDB;
import com.hutchgroup.elog.services.AutoStartService;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class DownloadAPK extends AsyncTask<String, Integer, Boolean> {
    String TAG = DownloadAPK.class.getName();

    private DownloadAPKListener<Boolean> downloadListener;
    private boolean autoDownload;

    public DownloadAPK(DownloadAPKListener<Boolean> downloadListener, boolean auto) {
        this.downloadListener = downloadListener;
        this.autoDownload = auto;
    }

    @Override
    protected Boolean doInBackground(String... param) {
        try {
            Log.i(TAG, "downaload apk");
            VersionInformationBean bean = VersionInformationDB.getVersionInformation();

            String path = Environment.getExternalStorageDirectory() + "/hutch/";
            File file = new File(path);
            file.mkdirs();

            String fileName = path + bean.getUpdateArchiveName();

            InputStream input = null;
            OutputStream output = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(bean.getUpdateUrl());
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                File outputFile = new File(file, bean.getUpdateArchiveName());
                if (outputFile.exists()) {
                    outputFile.delete();
                }

                // expect HTTP 200 OK, so we don't mistakenly save error report
                // instead of the file
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
//                    return "Server returned HTTP " + connection.getResponseCode()
//                            + " " + connection.getResponseMessage();
                    if (downloadListener != null) {
                        downloadListener.closeProgress(fileName);
                    }
                    return false;
                }


                // this will be useful to display download percentage
                // might be -1: server did not report the length
                int fileLength = connection.getContentLength();

                // download the file
                input = connection.getInputStream();
                output = new FileOutputStream(path + bean.getUpdateArchiveName());

                byte data[] = new byte[10240];
                long total = 0;
                int count;
                Log.i(TAG, "save apk into disk");
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        if (downloadListener != null) {
                            downloadListener.closeProgress(fileName);
                        }
                        return false;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                }
                Log.i(TAG, "complete saving apk into disk");
            } catch (IOException e1) {
                Log.i(TAG, "downaload error:" + e1.getMessage());
                if (downloadListener != null) {
                    downloadListener.closeProgress(fileName);
                }
                return false;
            } catch (Exception e) {
                Log.i(TAG, "downaload error:" + e.getMessage());
                if (downloadListener != null) {
                    downloadListener.closeProgress(fileName);
                }
                return false;
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                    if (downloadListener != null) {
                        downloadListener.closeProgress(fileName);
                    }
                }

                if (connection != null)
                    connection.disconnect();
            }

            Log.i(TAG, "call to install");
            if (bean.getAutoUpdateFg()) {
                AutoStartService.stopTask = true;
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(
                        Uri.fromFile(new File(Environment.getExternalStorageDirectory() + "/hutch/" + bean.getUpdateArchiveName())),
                        "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                Utility.context.startActivity(intent);
            } else {
                Utility.showAlertMsg("Update file " + bean.getUpdateArchiveName() + " is already downloaded in folder " + Environment.getExternalStorageDirectory() + "/download");
            }

            if (downloadListener != null) {
                downloadListener.closeProgress(fileName);
                downloadListener.autoLogout();
            }
        } catch (Exception e1) {
            Log.e(TAG, "Failed to update new apk " + e1.getMessage());
            if (autoDownload) {
                LogFile.write("DownloadAPK error: " + e1.getMessage(), LogFile.AUTOMATICALLY_TASK, LogFile.ERROR_LOG);
            } else {
                LogFile.write("DownloadAPK error: " + e1.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
            }
            return false;
        }


        return true;
    }


    protected void onProgressUpdate(Integer... progress) {
        Log.i(TAG, "progress " + progress[0]);
        if (downloadListener != null) {
            downloadListener.updateProgress(progress[0]);
        }
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            //call download and install the new apk
            Log.i(TAG, "download successfully");
        } else {
            Log.i(TAG, "download failed");
        }
    }

    public interface DownloadAPKListener<K> {
        //void onPostTask(K result);
        void updateProgress(int value);

        void closeProgress(String fileName);

        void progressError();

        void autoLogout();
    }
}



