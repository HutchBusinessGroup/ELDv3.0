package com.hutchgroup.elog.tasks;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.Mail;
import com.hutchgroup.elog.common.Utility;

import java.io.File;
import java.util.Calendar;

/**
 * Created by Deepak Sharma on 3/9/2017.
 */

public class ReportIssue extends AsyncTask<String, Void, Boolean> {
    String TAG = "ReportIssue";

    public ReportIssue() {

    }

    @Override
    protected Boolean doInBackground(String... param) {
        boolean status;
        String fileName = LogFile.LOG_CANBUS_NAME + "-" + Utility.getCurrentDate() + "-" + Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        fileName += LogFile.LOG_EXTENSION;
        String filePath = Environment.getExternalStorageDirectory() + "/" + fileName;
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
        String subject = param[0] + " - " + Utility.IMEI;
        m.setSubject(subject);

        String content = param[1];
        m.setBody("From IMEI:" + Utility.IMEI + "\n" + content);

        try {
            m.addAttachment(filePath);
        } catch (Exception e) {

        }


        try {
            if (m.send()) {
                Log.i(TAG, "Email was sent successfully");
                status = true;

                /*File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                }*/

            } else {
                status = false;
            }
        } catch (Exception e) {
            //Toast.makeText(MailApp.this, "There was a problem sending the email.", Toast.LENGTH_LONG).show();
            Log.i(TAG, "Could not send mail" + e.getMessage());
            status = false;
        }
        return status;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
    }

    public interface IMailProgress {

        void onMailSent(boolean status);

    }
}
