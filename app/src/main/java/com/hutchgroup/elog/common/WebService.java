package com.hutchgroup.elog.common;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;

public class WebService {
    public String doGet(String url) throws Exception {
        HttpGet req = new HttpGet(url);

        req.setHeader("Accept", "application/json");
        req.setHeader("Content-type", "application/json");
        DefaultHttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(req);
        StatusLine status = response.getStatusLine();
        if (status.getStatusCode() == 200) {
            return getContent(response.getEntity().getContent());
        }
       // LogFile.write("URL: " + url + "\n" + WebService.class.getName() + "::doGet Error:" + getContent(response.getEntity().getContent()) + ", ||" + status.toString(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
       // System.out.println("ErrorLog: " + status.toString());
        return null;
    }

    public String doPost(String url, String data) throws Exception {

        HttpPost req = new HttpPost(url);
        StringEntity e = new StringEntity(data.toString());
        req.setEntity(e);
        req.setHeader("Accept", "application/json");
        req.setHeader("Content-type", "application/json");
        DefaultHttpClient client = new DefaultHttpClient();
        HttpResponse response = client.execute(req);

        StatusLine status = response.getStatusLine();
        if (status.getStatusCode() == 200) {
            return getContent(response.getEntity().getContent());
        }
       // LogFile.write("URL: " + url + "\n" + WebService.class.getName() + "::doPost Error:" + getContent(response.getEntity().getContent()) + ", ||" + status.toString(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
       // System.out.println("ErrorLog: " + status.toString());
        return null;
    }

    private String getContent(InputStream in) throws Exception {
        StringBuilder sb = new StringBuilder();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(in));
            String temp;
            while ((temp = br.readLine()) != null) {
                sb.append(temp);
            }
            br.close();
            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
           // LogFile.write(WebService.class.getName() + "::doPost Error:" + e.getMessage(), LogFile.WEB_SERVICE, LogFile.ERROR_LOG);
        }
        return null;

    }
}
