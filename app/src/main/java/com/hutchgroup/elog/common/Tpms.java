package com.hutchgroup.elog.common;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.nfc.Tag;
import android.util.Log;

import com.hutchgroup.elog.MainActivity;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


/**
 * Created by Deepak on 12/20/2016.
 */

public class Tpms {

    public static final String TPMS_NAME = "iTPMS";
    String TAG = "Tpms";
    // Member fields
    private final BluetoothAdapter mAdapter;
    public static int mState;

    private static final UUID sppUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device


    private static int[] TpmsData = new int[4];
    private static boolean newreceived;
    private static ConnectThread mConnectThread;
    private static ConnectedThread mConnectedThread;

    public static boolean HeartBeat = false;

    public Tpms() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
    }

    private synchronized void setState(int state) {
        mState = state;
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }


    /**
     * Start the chat service. Specifically start AcceptThread to begin a
     * session in listening (server) mode. Called by the Activity onResume()
     */
    public synchronized void start() {
        Log.d(TAG, "start");

        //121 LogFile.write(CanMessages.class.getName() + "::start", LogFile.CAN_BUS_READ, LogFile.CANBUS_LOG);
        // Cancel any thread attempting to make a connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        setState(STATE_LISTEN);

    }


    /**
     * Deepak Sharma
     * 21 March 2016
     * Connect bluetooth
     * Start the ConnectThread to initiate a connection to a remote device.
     *
     * @param device The BluetoothDevice to connect
     * @param secure Socket Security type - Secure (true) , Insecure (false)
     */
    public synchronized void connect(BluetoothDevice device, boolean secure) {
        Log.d(TAG, "connect to: " + device);
        //121 LogFile.write(CanMessages.class.getName() + "::connect to " + device, LogFile.CAN_BUS_READ, LogFile.CANBUS_LOG);
        // Cancel any thread attempting to make a connection
        if (mState == STATE_CONNECTING) {
            if (mConnectThread != null) {
                mConnectThread.cancel();
                mConnectThread = null;
            }
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        HeartBeat = false;

        // Start the thread to connect with the given device
        mConnectThread = new ConnectThread(device, secure);
        mConnectThread.start();
        //onUpdateCanbusIcon(STATE_CONNECTING);
        setState(STATE_CONNECTING);
    }

    /**
     * Start the ConnectedThread to begin managing a Bluetooth connection
     *
     * @param socket The BluetoothSocket on which the connection was made
     * @param device The BluetoothDevice that has been connected
     */
    public synchronized void connected(BluetoothSocket socket, BluetoothDevice
            device, final String socketType) {
        Log.d(TAG, "connected, Socket Type:" + socketType);

        //121 LogFile.write(CanMessages.class.getName() + "::connected, socket type: " + socketType, LogFile.CAN_BUS_READ, LogFile.CANBUS_LOG);
        // Cancel the thread that completed the connection
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        // Cancel any thread currently running a connection
        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }

        // Start the thread to manage the connection and perform transmissions
        mConnectedThread = new ConnectedThread(socket, socketType);
        mConnectedThread.start();

        setState(STATE_CONNECTED);
    }


    /**
     * Stop all threads
     */
    public synchronized void stop() {
        Log.d(TAG, "stop");
        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }


        if (mConnectedThread != null) {
            mConnectedThread.cancel();
            mConnectedThread = null;
        }


        setState(STATE_NONE);
    }

    /**
     * This thread runs while attempting to make an outgoing connection
     * with a device. It runs straight through; the connection either
     * succeeds or fails.
     */
    private class ConnectThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;
        private String mSocketType;

        public ConnectThread(BluetoothDevice device, boolean secure) {
            //121 LogFile.write("Connecting to the device " + device.getName(), LogFile.BLUETOOTH_CONNECTIVITY, LogFile.CANBUS_LOG);
            mmDevice = device;
            BluetoothSocket tmp = null;
            mSocketType = secure ? "Secure" : "Insecure";

            // Get a BluetoothSocket for a connection with the
            // given BluetoothDevice
            try {
                if (secure) {
                    tmp = device.createRfcommSocketToServiceRecord(
                            sppUUID);
                } else {
                    tmp = device.createInsecureRfcommSocketToServiceRecord(
                            sppUUID);
                }
            } catch (IOException e) {
                Log.e(TAG, "Socket Type: " + mSocketType + "create() failed", e);
                LogFile.write("Socket Type: " + mSocketType + "create() failed:" + e.getMessage(), LogFile.BLUETOOTH_CONNECTIVITY, LogFile.ERROR_LOG);
            }
            mmSocket = tmp;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectThread SocketType:" + mSocketType);
            setName("ConnectThread" + mSocketType);
            //121 LogFile.write("Begin connect to socket " + mSocketType, LogFile.BLUETOOTH_CONNECTIVITY, LogFile.CANBUS_LOG);
            // Always cancel discovery because it will slow down a connection
            mAdapter.cancelDiscovery();

            // Make a connection to the BluetoothSocket
            try {
                // This is a blocking call and will only return on a
                // successful connection or an exception
                mmSocket.connect();

            } catch (IOException e) {
                // Close the socket
                try {

                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                    LogFile.write("unable to close() " + mSocketType +
                            " socket during connection failure: " + e2.getMessage(), LogFile.BLUETOOTH_CONNECTIVITY, LogFile.ERROR_LOG);
                }
                setState(STATE_LISTEN);
                LogFile.write("Failed to connect:" + e.getMessage(), LogFile.BLUETOOTH_CONNECTIVITY, LogFile.ERROR_LOG);
                //121 LogFile.write("Failed to connect:" + e.getMessage(), LogFile.BLUETOOTH_CONNECTIVITY, LogFile.CANBUS_LOG);
                //connectionFailed();
                return;
            } catch (Exception e) {
                // Close the socket
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure", e2);
                    LogFile.write("unable to close() " + mSocketType +
                            " socket during connection failure: " + e2.getMessage(), LogFile.BLUETOOTH_CONNECTIVITY, LogFile.ERROR_LOG);
                } catch (Exception e1) {
                    Log.e(TAG, "unable to close() " + mSocketType +
                            " socket during connection failure" + e1.getMessage());
                    LogFile.write("unable to close() " + mSocketType +
                            " socket during connection failure: " + e1.getMessage(), LogFile.BLUETOOTH_CONNECTIVITY, LogFile.ERROR_LOG);
                }
                setState(STATE_LISTEN);
                LogFile.write("Failed to connect:" + e.getMessage(), LogFile.BLUETOOTH_CONNECTIVITY, LogFile.ERROR_LOG);
                //121 LogFile.write("Failed to connect:" + e.getMessage(), LogFile.BLUETOOTH_CONNECTIVITY, LogFile.CANBUS_LOG);
                //connectionFailed();
                return;
            }

            // Reset the ConnectThread because we're done
            synchronized (Tpms.this) {
                //    mConnectThread.cancel();
                mConnectThread = null;
            }

            // Start the connected thread
            connected(mmSocket, mmDevice, mSocketType);
        }

        public void cancel() {
            try {
                mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect " + mSocketType + " socket failed", e);
                LogFile.write("close() of connect " + mSocketType + " socket failed:" + e.getMessage(), LogFile.BLUETOOTH_CONNECTIVITY, LogFile.ERROR_LOG);
            }
        }
    }

    /**
     * This thread runs during a connection with a remote device.
     * It handles all incoming and outgoing transmissions.
     */
    private class ConnectedThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket, String socketType) {
            Log.d(TAG, "create ConnectedThread: " + socketType);
            //121 LogFile.write("create ConnectedThread: " + socketType, LogFile.BLUETOOTH_CONNECTIVITY, LogFile.CANBUS_LOG);
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            // Get the BluetoothSocket input and output streams
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();

            } catch (IOException e) {
                Log.e(TAG, "temp sockets not created", e);
                LogFile.write("temp sockets are not created: " + e.getMessage(), LogFile.BLUETOOTH_CONNECTIVITY, LogFile.ERROR_LOG);
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            Log.i(TAG, "BEGIN mConnectedThread");
            //121 LogFile.write("BEGIN get data", LogFile.CAN_BUS_READ, LogFile.CANBUS_LOG);


            byte[] buffer = new byte[1024];
            int buf_len;
            String errorMessage;

            // Keep listening to the InputStream while connected
            while (true) {
                try {
                    //Thread.sleep(1);
                    buf_len = mmInStream.read(buffer);

                    if (buf_len == -1) {
                        errorMessage = "Tpms: disconnected: Read -1 input stream";
                        mmInStream.close();
                        break;
                    }
                    parse(buffer);

                } catch (IOException e) {
                    errorMessage = "Tpms: IOException: " + e.getMessage();
                    break;
                } catch (Exception e) {
                    errorMessage = "Tpms: Exception: " + e.getMessage();
                    break;
                }
            }
            error(errorMessage);

        }

        private void error(String message) {
            setState(STATE_LISTEN);
            LogFile.write(message, LogFile.BLUETOOTH_CONNECTIVITY, LogFile.ERROR_LOG);
            Log.i(TAG, message);
        }

        public void cancel() {
            try {

                if (mmSocket != null)
                    mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
                LogFile.write("close() of connect socket failed: " + e.getMessage(), LogFile.BLUETOOTH_CONNECTIVITY, LogFile.ERROR_LOG);
            }
        }

        public void write(byte[] command) {
            if (mState != STATE_CONNECTED)
                return;
            if (mmOutStream != null) {
                try {
                    mmOutStream.write(command);
                    mmOutStream.flush();
                } catch (Exception e) {
                    Log.e(TAG, "Output stream write exception");
                }
            }
        }
    }

    public void parseMessage(byte[] buf, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int v = buf[i] & 0xFF;
            String hex = Integer.toString(v, 16);
            sb.append(hex);
            sb.append(" ");

        }
        String data = sb.toString();
        Log.i(TAG, data);
    }

    void parse(byte[] readBuf) {
        // only taken first sentence to parse data
        if (readBuf[0] == 84 && readBuf[1] == 80 && readBuf[2] == 86  && readBuf.length >= 12) {//&& readBuf[3] == 44
            int ibuf = 0;
            for (int ii = 4; ii < 11; ii++) {
                ibuf += Tpms.this.B2I(readBuf[ii]);
            }
            if (ibuf % 256 == Tpms.this.B2I(readBuf[11]) % 256) {
                /*int si;
                long li = ((((0 | ((long) (readBuf[4] < 0 ? readBuf[4] + 256 : readBuf[4]))) << 8) | ((long) (readBuf[5] < 0 ? readBuf[5] + 256 : readBuf[5]))) << 8) | ((long) (readBuf[6] < 0 ? readBuf[6] + 256 : readBuf[6]));
                if (readBuf[7] < 0) {
                    si = readBuf[7] + 256;
                } else {
                    si = readBuf[7];
                }*/

                StringBuilder sb = new StringBuilder();
                for (int i = 3; i <= 7; i++) {
                    int v = readBuf[i] & 0xFF;
                    String hex = Integer.toString(v, 16);
                    if (hex.length() == 1) hex = "0" + hex;
                    sb.append(hex);
                    sb.append(" ");

                }
                String id = sb.toString();
                int temperature = Tpms.this.B2I(readBuf[8]) - 50;
                int pressure = Tpms.this.B2I(readBuf[9]);
                int voltage = Tpms.this.B2I(readBuf[10]);
                // putTpmsData(0, id);
                putTpmsData(1, temperature);
                putTpmsData(2, pressure);
                putTpmsData(3, voltage);
                setnewreceived();
                Log.i(TAG, "SensorId: " + id + ", Temperature: " + temperature + ", pressure: " + pressure + ", voltage: " + (voltage * 1.0f / 50.0f));
            }
        }
    }

    // convert byte to int
    private int B2I(byte Bd) {
        byte ii = Bd;
        return (Bd & 128) == 0 ? ii : ii + 256;
    }

    public static void putTpmsData(int index, int data) {
        if (index < 4 && index >= 0) {
            TpmsData[index] = data;
        }
    }

    public static int getTpmsData(int index) {
        if (index >= 4 || index < 0) {
            return 0;
        }
        return TpmsData[index];
    }

    public static boolean isnewreceived() {
        return newreceived;
    }

    public static void clearnewreceived() {
        newreceived = false;
    }

    public static void setnewreceived() {
        newreceived = true;
    }

    Thread thCanHB = null;

    public void StartTpmsHB() {
        if (thCanHB != null) {
            thCanHB.interrupt();
            thCanHB = null;
        }

        thCanHB = new Thread(runnableHB);
        thCanHB.setName("TpmsHB");
        thCanHB.start();
    }

    public void StopTpmsHB() {
        if (thCanHB != null) {
            thCanHB.interrupt();
            thCanHB = null;
        }
    }


    public static String deviceAddress;
    int connectRequest = 1; // connect to bluetooth if disconnected
    private Runnable runnableHB = new Runnable() {
        @Override
        public void run() {
            try {
                while (mState != STATE_CONNECTED && connectRequest < 60) {
                    Thread.sleep(1000);
                    connectRequest++;
                }
                connectRequest = 1;
            } catch (InterruptedException e) {

            }

            while (true) {
                try {
                    Thread.sleep(1000);
                    if (MainActivity.undockingMode) {
                        continue;
                    }
                    // reconnect logic
                    else if (mState == STATE_LISTEN) {
                        if (connectRequest == 60) {
                            connectRequest = 1;
                            // Get the BluetoothDevice object
                            BluetoothDevice device = mAdapter.getRemoteDevice(deviceAddress);
                            Log.i("TPMS", "Connect...");
                            // Attempt to connect to the device
                            Tpms.this.connect(device, true);
                        } else {
                            connectRequest++;
                        }
                    }

                } catch (InterruptedException e) {
                    break;
                }

            }
        }
    };

}