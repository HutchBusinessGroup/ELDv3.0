package com.hutchgroup.elog.tasks;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.hutchgroup.elog.common.ConstantFlag;
import com.hutchgroup.elog.common.GetCall;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Mail;
import com.hutchgroup.elog.common.PostCall;
import com.hutchgroup.elog.common.Utility;

import java.io.File;
import java.util.Calendar;

public class MailSync extends AsyncTask<String, Void, Boolean> {
    String TAG = MessageSyncData.class.getName();

    private PostTaskListener<Boolean> postTaskListener;
    private boolean attachmentUsed;

    public MailSync(final PostTaskListener<Boolean> postTaskListener, boolean attachment) {
        this.postTaskListener = postTaskListener;
        attachmentUsed = attachment;
    }

    @Override
    protected Boolean doInBackground(String... param) {
        boolean status;

        String filePath = getLogFilePath(LogFile.ERROR_LOG);
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                return true;
            }
        } catch (Exception exe) {
        }

        Mail m = new Mail("support@hutchsystems.com", "#72Such6");

        String[] toArr = {"support@hutchsystems.com"};
        m.setTo(toArr);
        m.setFrom("support@hutchsystems.com");
        String subject = param[0];
        m.setSubject(subject);

        String content = param[1];
        m.setBody("From IMEI:" + Utility.IMEI + "\n" + content);

        if (attachmentUsed) {
            try {
                m.addAttachment(filePath);
                //121 if (ConstantFlag.Flag_Log_CanBus)
                //121     m.addAttachment(getLogFilePath(LogFile.CANBUS_LOG));
                //122 if (ConstantFlag.Flag_Log_NoLogin)
                //122    m.addAttachment(getLogFilePath(LogFile.NOLOGIN_LOG));
                //123 if (ConstantFlag.Flag_Log_DriverEvent)
                //123     m.addAttachment(getLogFilePath(LogFile.DRIVEREVENT_LOG));
                //124 if (ConstantFlag.Flag_Log_AutoSyncTask)
                //124     m.addAttachment(getLogFilePath(LogFile.AUTOSYNC_LOG));
                //125 if (ConstantFlag.Flag_Log_TCPClient)
                //125     m.addAttachment(getLogFilePath(LogFile.TCPCLIENT_LOG));
                //126 if (ConstantFlag.Flag_Log_GPS)
                //126     m.addAttachment(getLogFilePath(LogFile.GPS_LOG));
            } catch (Exception e) {
                //Toast.makeText(MailApp.this, "There was a problem sending the email.", Toast.LENGTH_LONG).show();
                Log.i(TAG, "Could not attach file" + e.getMessage());
            }
        }

        try {
            if (m.send()) {
                //Toast.makeText(MailApp.this, "Email was sent successfully.", Toast.LENGTH_LONG).show();
                Log.i(TAG, "Email was sent successfully");
                status = true;

                File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }

            } else {
                //Toast.makeText(MailApp.this, "Email was not sent.", Toast.LENGTH_LONG).show();
                Log.i(TAG, "Email was not sent");
                status = false;
            }
        } catch (Exception e) {
            //Toast.makeText(MailApp.this, "There was a problem sending the email.", Toast.LENGTH_LONG).show();
            Log.i(TAG, "Could not send mail" + e.getMessage());
            status = false;
        }
        return status;

    }

    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
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

    public String getLogFilePath(int logType) {
        String path = "";
        String logName = "";
        switch (logType) {
            case LogFile.ERROR_LOG:
                logName = LogFile.LOG_ERROR_NAME;
                break;
            case LogFile.CANBUS_LOG:
                logName = LogFile.LOG_CANBUS_NAME + "-" + Utility.getCurrentDate() + "-" + Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
                break;
            case LogFile.NOLOGIN_LOG:
                logName = LogFile.LOG_NOLOGIN_NAME;
                break;
            case LogFile.DRIVEREVENT_LOG:
                logName = LogFile.LOG_DRIVEREVENT_NAME;
                break;
            case LogFile.AUTOSYNC_LOG:
                logName = LogFile.LOG_AUTOSYNC_NAME;
                break;
            case LogFile.TCPCLIENT_LOG:
                logName = LogFile.LOG_TCPCLIENT_NAME;
                break;
            case LogFile.GPS_LOG:
                logName = LogFile.LOG_GPS_NAME;
                break;
        }
        logName += LogFile.LOG_EXTENSION;
        if (isExternalStorageWritable()) {
            path = Environment.getExternalStorageDirectory() + "/" + logName;
        } else {
            path = logName;
        }
        return path;
    }

    public interface PostTaskListener<K> {
        void onPostTask(K result);
    }
}



