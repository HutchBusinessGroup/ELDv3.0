package com.hutchgroup.elog.tracklocations;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import android.content.Context;
import android.os.AsyncTask;

import com.hutchgroup.elog.db.TrackingDB;

public class ClientSocket extends AsyncTask<String, Void, String> {

    String host;
    int port;
    boolean status;
    Context context;
    String response;
    boolean isConnected;
    Socket socket = null;
    DataOutputStream dataOutputStream = null;

    public ClientSocket(String host, int port, Context context) {
        this.host = host;
        this.port = port;
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {

        try {
            // System.out.println(host + ":" + port);
            // Create a new Socket instance and connect to host
            // socket = new Socket(host, port);
            if (!isConnected) {
                socket = new Socket();
                socket.connect(new InetSocketAddress(host, port), 30000);
                dataOutputStream = new DataOutputStream(
                        socket.getOutputStream());

            }
            isConnected = true;
            String response = params[0] + "\n";
            dataOutputStream.writeBytes(response);
            status = true;

        } catch (IOException e) {
            response = e.toString();
            e.printStackTrace();
            isConnected = false;
        } finally {
            if (!isConnected) {
                // close socket
                if (socket != null) {
                    try {

                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                // close output stream
                if (dataOutputStream != null) {
                    try {
                        dataOutputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return params[1];
    }

    @Override
    protected void onPostExecute(String id) {
        if (status) {

            TrackingDB.removeGpsSignal(id);
            status = false;
        }
    }
}
