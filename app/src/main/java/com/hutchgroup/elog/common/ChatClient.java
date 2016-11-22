package com.hutchgroup.elog.common;


import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.hutchgroup.elog.MainActivity;
import com.hutchgroup.elog.R;
import com.hutchgroup.elog.beans.MessageBean;
import com.hutchgroup.elog.db.MessageDB;
import com.hutchgroup.elog.db.UserDB;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Date;

/**
 * Created by Dev-1 on 4/14/2016.
 */
public class ChatClient {

    public static ChatMessageInterface mListener;
    public static ChatMessageReceiveIndication icListner;
    // private ChatThread mConnectedThread;
    private static Socket socket = null;
    public static final int PORT_NUMBER = 32101;//85; //
    public static final String HOST = "207.194.137.58";//"192.168.0.7";//"207.194.137.58";//"10.0.2.2";//
    private static Thread chatThread = null, hbTread;
    public static PrintWriter out = null;
    public static BufferedReader in = null;
    private static Date lastHeartBeatTime = Utility.newDate();


    // Created By: Deepak Sharma
    // Created Date: 14 April 2016
    // Purpose: connect to server
    public static void connect() {

        if (!Utility.isInternetOn())
            return;
        if (out != null && !out.checkError())
            return;
        if (chatThread != null) {
            chatThread.interrupt();
            chatThread = null;
        }

        chatThread = new Thread(new Runnable() {

            @Override
            public void run() {
                chatThread.setName("ChatClient-Connect");

                try {
                    connecting = true;
                    socket = new Socket(HOST, PORT_NUMBER);
                    lastHeartBeatTime =Utility.newDate();
                    //  socket.setKeepAlive(true);
                    //socket.setSoTimeout(0);
                    out = new PrintWriter(socket.getOutputStream());
                    in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                    MessageBean bean = MessageDB.CreateMessage(Utility.IMEI, Utility.user1.getAccountId(), Utility.user1.getAccountId(), "Connect");
                    MessageDB.Send(bean);

                    if (Utility.user2.getAccountId() > 0) {
                        bean = MessageDB.CreateMessage(Utility.IMEI, Utility.user2.getAccountId(), Utility.user2.getAccountId(), "Connect");
                        MessageDB.Send(bean);
                    }
                    if (icListner != null) {
                        icListner.onServerStatusChanged(true);
                    }
                    Log.i("ChatInfo:", "Connected");
                    connecting = false;
                    while (true) {
                        String msg = null;
                        try {
                            msg = in.readLine();
                        } catch (Exception e) {
                            out = null;
                        }

                        if (msg == null) {
                            Log.i("ChatInfo:", "Disconnected-Conn");
                            break;
                        } else {
                            receive(msg);
                        }
                    }

                } catch (Exception e) {
                    Utility.printError(e.getMessage());
                    // LogFile.write(ChatClient.class.getName() + "::checkConnection Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                }
                connecting = false;

            }
        });
        chatThread.start();

    }

    private static boolean connecting = false;

    public static void checkConnection() {
        if (hbTread != null) {
            hbTread.interrupt();
            hbTread = null;
        }

        hbTread = new Thread(new Runnable() {
            @Override
            public void run() {
                hbTread.setName("ChatClient-HB");

                while (true) {
                    try {
                        Thread.sleep(5000);

                        if (connecting || !Utility.isInternetOn()) continue;

                        connecting = true;

                        if (Utility.user1.getAccountId() > 0) {
                            long differ = (Utility.newDate().getTime() - lastHeartBeatTime.getTime()) / 1000;
                            if (out == null || out.checkError() || differ > 10) {
                                if (icListner != null) {
                                    icListner.onServerStatusChanged(false);
                                }
                                reconnect();
                            }
                            // send heart beat
                            SendHB();
                        }


                    } catch (InterruptedException e) {
                        Utility.printError(e.getMessage());
                        break;
                        //LogFile.write(ChatClient.class.getName() + "::checkConnection Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                    }
                    connecting = false;
                }
            }
        });
        hbTread.start();
    }

    // Created By: Deepak Sharma
    // Created Date: 27 June 2016
    // Purpose: send Heart Beat
    public static void SendHB() {
        try {
            String message = "HB:" + Utility.vehicleId;
            if (out != null && !out.checkError()) {

                out.println(message);
                out.flush();
            } else {
                Log.i("ChatClient", "no send");

            }

        } catch (Exception e) {
            //  LogFile.write(ChatClient.class.getName() + "::send Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
            Utility.printError(e.getMessage());
        }

    }

    private synchronized static void reconnect() {

        try {

            socket.close();

            Thread.sleep(1000);
        } catch (Exception e) {

            Utility.printError(e.getMessage());
            //  LogFile.write(ChatClient.class.getName() + "::reconnect Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
        }
        connect();

    }

    // Created By: Deepak Sharma
    // Created Date: 14 April 2016
    // Purpose: disconnect connection from server
    public static void disconnect() {
        Thread thDisconnect = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    if (hbTread != null) {
                        hbTread.interrupt();
                        hbTread = null;
                    }

                    socket.close();
                    in = null;
                    out = null;
                    Log.i("ChatInfo:", "Disconnected");
                } catch (Exception e) {

                    Utility.printError(e.getMessage());
                    // LogFile.write(ChatClient.class.getName() + "::disconnect Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                }
            }
        });
        thDisconnect.setName("ChatClient-Disconnect");
        thDisconnect.start();
    }

    // Created By: Deepak Sharma
    // Created Date: 14 April 2016
    // Purpose: received message to server
    public static void send(final String message) {

        Thread thSend = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Log.i("ChatClient", "send");
                    if (out != null && !out.checkError()) {
                        Log.i("ChatClient", "sending " + message);
                        out.println(message);
                        out.flush();
                    } else {
                        Log.i("ChatClient", "no send");

                    }

                } catch (Exception e) {
                    Log.i("ChatClient", "sending error: " + e.getMessage());
                    Utility.printError(e.getMessage());
                    LogFile.write(ChatClient.class.getName() + "::send Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);
                }
            }
        });
        thSend.setName("ChatClient-Send");
        thSend.start();
    }

    // Created By: Deepak Sharma
    // Created Date: 14 April 2016
    // Purpose: received message from server
    private static void receive(String message) {

        try {
            if (message.equals("HB")) {
                lastHeartBeatTime = Utility.newDate();
                System.out.println("HeartBeat: " + lastHeartBeatTime);
                return;
            }

            Log.i("ChatClient", "receive");
            Log.i("ChatClient", "RecievedMessage:" + message);
            // Looper.prepare();
            System.out.println("RecievedMessage:" + message);
          /*  if (message.equals("HB")) {
                lastHeartBeatTime = Utility.newDate();
                System.out.println("HeartBeat: " + lastHeartBeatTime);
                return;
            }*/
            JSONObject json = new JSONObject(message);
            MessageBean bean = new MessageBean();
            bean.setMessage(json.getString("Message"));
            bean.setCreatedById(json.getInt("CreatedById"));
            bean.setMessageToId(json.getInt("MessageToId"));
            bean.setMessageDate(json.getString("MessageDate"));
            bean.setDeliveredFg(0);
            bean.setReadFg(0);
            bean.setSendFg(0);
            bean.setDeviceId(json.getString("DeviceId"));
            bean.setSyncFg(1);
            bean.setFlag(json.getString("Flag"));
            if (bean.getFlag().equals("Message")) {
                //if (MessageActivity.mHandler != null) {
                //MessageActivity.mHandler.sendMessage(obj);
                MessageDB.Save(bean);

                if (mListener != null) {
                    mListener.onMessageUpdated(bean, MESSAGE);
                } else {
                    if (icListner != null) {
                        icListner.onMessageReceived();
                    }
                    getNotification(bean);
                }

                //}
                // MessageActivity.received(bean);
            } else if (bean.getFlag().equals("Read")) {

                MessageDB.MessageStatusUpdate(Utility.onScreenUserId, bean.getMessageToId());
                if (mListener != null) {
                    mListener.onMessageUpdated(bean, READFG);
                }

            } else if (bean.getFlag().equals("Online")) {
                Log.i("ChatClient", "online");
                if (!Utility.onlineUserList.contains(bean.getCreatedById() + "")) {
                    Utility.onlineUserList.add(bean.getCreatedById() + "");
                }

                if (mListener != null) {
                    mListener.onMessageUpdated(bean, ONLINE);
                }
            } else if (bean.getFlag().equals("OnlineList")) {
                String[] arrUser = bean.getMessage().split(",");
                for (String user : arrUser) {
                    if (!Utility.onlineUserList.contains(user)) {
                        Utility.onlineUserList.add(user);
                    }
                }
            } else if (bean.getFlag().equals("Offline")) {
                Log.i("ChatClient", "offline");
                Utility.onlineUserList.remove(bean.getCreatedById() + "");

                if (mListener != null) {
                    mListener.onMessageUpdated(bean, OFFLINE);
                }
            } else if (bean.getFlag().equals("Offline2")) {
                String arr[] = bean.getMessage().split(",");

                for (String u : arr) {
                    Utility.onlineUserList.remove(u);

                }

                if (mListener != null) {
                    mListener.onMessageUpdated(bean, OFFLINE2);
                }
            } else if (bean.getFlag().equals("HB")) {
                lastHeartBeatTime =Utility.newDate();
                System.out.println("HeartBeat: " + lastHeartBeatTime);
            }
            //Looper.loop();

        } catch (Exception e) {
            //   Log.i("ChatClient", "receive error: " + e.getMessage());
            Utility.printError(e.getMessage());
            //LogFile.write(ChatClient.class.getName() + "::receive Error:" + e.getMessage(), LogFile.DATABASE, LogFile.ERROR_LOG);

        }
    }

    private static void getNotification(MessageBean bean) {
        int activeUserId = Utility.user1.isOnScreenFg() ? Utility.user1.getAccountId() : Utility.user2.getAccountId();
        if (activeUserId != bean.getMessageToId()) {
            return;
        }

        String userName = UserDB.getUserName(bean.getCreatedById());

        Intent resultIntent = new Intent(Utility.context, MainActivity.class);
        Bundle b = new Bundle();
        b.putInt("UserId", bean.getCreatedById());
        b.putString("UserName", userName);
        resultIntent.putExtras(b);

        PendingIntent pendingIntent = PendingIntent.getActivity(Utility.context, 0,   resultIntent, Intent.FILL_IN_ACTION);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(Utility.context)
                        .setSmallIcon(R.drawable.ic_lanucher)
                        .setContentTitle(userName)
                        .setContentText(bean.getMessage());

        mBuilder.setContentIntent(pendingIntent);
        /*Intent resultIntent = new Intent(Utility.context, MessageActivity.class);
        Bundle b = new Bundle();
        b.putInt("UserId", bean.getCreatedById());
        b.putString("UserName", userName);
        resultIntent.putExtras(b);

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        Utility.context,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);*/

        NotificationManager mNotifyMgr =
                (NotificationManager) (Utility.context).getSystemService(Utility.context.NOTIFICATION_SERVICE);
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setAutoCancel(true);
        mNotifyMgr.notify(1001, mBuilder.build());

        if (Utility._appSetting.getMessageReading() == 1 && MainActivity.textToSpeech != null) {

            //   String toUserName = Utility.user1.getAccountId() == bean.getMessageToId() ? Utility.user1.getFirstName() : Utility.user2.getFirstName();
            String textToSpeech = bean.getMessage();
            MainActivity.textToSpeech.playSilence(2000, TextToSpeech.QUEUE_ADD, null);
            MainActivity.textToSpeech.speak(textToSpeech, TextToSpeech.QUEUE_ADD, null);

        }

    }

    public static final int MESSAGE = 1, ONLINE = 2, OFFLINE = 3, OFFLINE2 = 4, ONLINELIST = 5, READFG = 6;

    public interface ChatMessageInterface {
        void onMessageUpdated(MessageBean obj, int flag);
    }

    public interface ChatMessageReceiveIndication {
        void onMessageReceived();

        void onServerStatusChanged(boolean status);
    }
}
