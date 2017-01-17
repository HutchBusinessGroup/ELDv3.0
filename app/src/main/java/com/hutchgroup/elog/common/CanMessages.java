package com.hutchgroup.elog.common;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.hutchgroup.elog.MainActivity;
import com.hutchgroup.elog.beans.DTCBean;
import com.hutchgroup.elog.beans.DiagnosticIndicatorBean;
import com.hutchgroup.elog.beans.GPSData;
import com.hutchgroup.elog.beans.VehicleInfoBean;
import com.hutchgroup.elog.db.DTCDB;
import com.hutchgroup.elog.db.VehicleInfoDB;

public class CanMessages {
    String TAG = "CanMessages";
    public static VehicleInfoBean _vehicleInfo = new VehicleInfoBean();
    public static long currentTime = System.currentTimeMillis();
    private static int DIAGNOSTIC_ENGINE_SYNCHRONIZTION = 3;
    private static int MALFUNCTION_ENGINE_SYNCHRONIZTION = 30 * 60;

    public static final String BT_NAME = "RNBT";
    public static final String BT_NAME_1 = "HUTCH";

    public static String deviceAddress;
    public static String deviceName;
    private static final UUID sppUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Member fields
    private final BluetoothAdapter mAdapter;

    private static ConnectThread mConnectThread;
    private static ConnectedThread mConnectedThread;
    public static int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0;       // we're doing nothing
    public static final int STATE_LISTEN = 1;     // now listening for incoming connections
    public static final int STATE_CONNECTING = 2; // now initiating an outgoing connection
    public static final int STATE_CONNECTED = 3;  // now connected to a remote device

    private static final byte RS232_FLAG = (byte) 0xC0;
    private static final byte RS232_ESCAPE = (byte) 0xDB;
    private static final byte RS232_ESCAPE_FLAG = (byte) 0xDC;
    private static final byte RS232_ESCAPE_ESCAPE = (byte) 0xDD;
    private static final String DEGREE = " \u00b0F";
    private static final int ACK = 0;
    private static final int FA_J1939 = 1;
    private static final int FD_J1939 = 2;
    private static final int FA_J1708 = 3;
    private static final int TX_J1939 = 5;
    public static final int RX_J1939 = 6;
    private static final int TX_J1708 = 8;
    public static final int RX_J1708 = 9;
    private static final int CPU_RESET = 17;
    private static final int STATS = 23;
    private static final double KM_TO_MI = 0.621371;
    private static final double L_TO_GAL = 0.264172;
    private static final double KPA_TO_PSI = 0.145037738;
    private static final double KW_TO_HP = 1.34102209;
    private static final Integer MAX_16 = 0xffff;
    private static final Integer MAX_32 = 0xffffffff;
    private static final Integer MAX_8 = 0xff;

    public static final int BOTH = 10;

    public static boolean HeartBeat = false;
    private byte[] m_buffer;
    private int m_count;
    private boolean isInvalid;
    private boolean isStuffed;
    private int m_size;

    public static String Speed = "-1";

    public static String OdometerReading = "0", EngineHours = "0", RPM = "-1", VIN = "", CoolantTemperature = "-99", Voltage = "0", Boost = "0", TotalFuelConsumed = "0",
            TotalIdleFuelConsumed = "0", TotalIdleHours = "0", TotalAverage = "0", WasherFluidLevel = "-99", FuelLevel1 = "0", EngineCoolantLevel = "-99", EngineOilLevel = "-99", BrakeApplicationPressure = "0",
            BrakePrimaryPressure = "0", BrakeSecondaryPressure = "0";
    public static boolean CriticalWarningFg;

    private static int supportedProtocol;

    public static long diagnosticEngineSynchronizationTime = System.currentTimeMillis();
    public static long CanDataTime = System.currentTimeMillis();

    public CanMessages() {
        mAdapter = BluetoothAdapter.getDefaultAdapter();
        mState = STATE_NONE;
        supportedProtocol = -1;
    }

    /**
     * Set the current state of the chat connection
     *
     * @param state An integer defining the current connection state
     */
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

        //121 LogFile.write(CanMessages.class.getName() + "::stop ", LogFile.CAN_BUS_READ, LogFile.CANBUS_LOG);
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

                btbError = false;
                if (mCanListner != null) {
                    mCanListner.onAlertClear();
                }
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
            synchronized (CanMessages.this) {
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

                byte[] msg = cpu_reset_message();
                try {
                    Log.i(TAG, "cpu reset");
                    tmpOut.write(msg);
                } catch (Exception e) {
                    Log.i(TAG, "cpu reset error: " + e.getMessage());
                }
                initializeFilters(tmpOut);
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
                    Thread.sleep(1);
                    // Read from the InputStream
                    currentTime = System.currentTimeMillis();
                    buf_len = mmInStream.read(buffer);

                    if (buf_len == -1) {
                        errorMessage = "disconnected: Read -1 input stream";
                        mmInStream.close();
                        break;
                    }

                    parseMessage(buffer, buf_len);
                    diagnosticEngineSynchronizationTime = System.currentTimeMillis();


                } catch (IOException e) {
                    errorMessage = "BTB: IOException: " + e.getMessage();
                    Log.i(TAG, errorMessage);
                    break;
                } catch (InterruptedException e) {
                    errorMessage = "BTB: Interputted: " + e.getMessage();
                    Log.i(TAG, errorMessage);
                    break;
                } catch (Exception e) {
                    errorMessage = "BTB: Exception: " + e.getMessage();
                    Log.i(TAG, errorMessage);
                    break;
                }
            }
            error(errorMessage);

        }

        private void error(String message) {
            setState(STATE_LISTEN);
            LogFile.write(message, LogFile.BLUETOOTH_CONNECTIVITY, LogFile.ERROR_LOG);

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
        try {

            if (len > buf.length) {
                len = buf.length;
            }

            for (int i = 0; i < len; i++) {
                processCharFromBus(buf[i]);
            }

        } catch (Exception e) {
            Log.i(TAG, "BTB: parseMessage error:" + e.getMessage());
            LogFile.write(CanMessages.class.getName() + "::parseMessage error: " + e.getMessage(), LogFile.CAN_BUS_READ, LogFile.ERROR_LOG);
            throw e;
        }

    }

    private void processCharFromBus(byte val) {
        try {
            // Is it the start of the message?
            if (val == RS232_FLAG) {
                isInvalid = false;
                isStuffed = false;
                m_size = -1;
                m_count = 0;
            } else if (!isInvalid) {
                if (val == RS232_ESCAPE) {
                    isStuffed = true;
                } else {
                    // If previous byte was an escape, then decode current byte
                    if (isStuffed) {
                        isStuffed = false;
                        if (val == RS232_ESCAPE_FLAG) {
                            val = RS232_FLAG;
                        } else if (val == RS232_ESCAPE_ESCAPE) {
                            val = RS232_ESCAPE;
                        } else {
                            isInvalid = true;
                            // Invalid byte after escape, must abort
                            return;
                        }
                    }
                    // At this point data is always unstuffed
                    if (m_count < m_buffer.length) {
                        m_buffer[m_count] = val;
                        m_count++;
                    } else {
                        // Full buffer
                    }

                    // At 2 bytes, we have enough info to calculate a real
                    // message length
                    if (m_count == 2) {
                        m_size = ((m_buffer[0] << 8) | m_buffer[1]) + 2;
                    }

                    // Have we received the entire message? If so, is it valid?
                    if (m_count == m_size
                            && val == cksum(m_buffer, m_count - 1)) {
                        m_count--; // Ignore the checksum at the end of the
                        // message
                        processPacket(m_buffer);
                    }
                }
            }
        } catch (Exception e) {
            Log.i(TAG, "BTB: processCharFromBus error: " + e.getMessage());
            System.out.println(e.getStackTrace()[0]);
            LogFile.write(CanMessages.class.getName() + "::processCharFromBus error: " + e.getMessage(), LogFile.CAN_BUS_READ, LogFile.ERROR_LOG);
            //121 LogFile.write("Error when parsing char from bus: " + e.getMessage(), LogFile.CAN_BUS_READ, LogFile.CANBUS_LOG);
            throw e;
        }
    }

    private void processPacket(byte[] packet) {
        int msgID = packet[2];
        if (msgID == RX_J1939) {
            CanDataTime = System.currentTimeMillis();
            if (supportedProtocol == -1) {
                supportedProtocol = RX_J1939;
            } else {
                if (supportedProtocol == RX_J1708) {
                    supportedProtocol = BOTH;
                }
            }

            final int pgn = ((packet[4] & 0xFF) << 16)
                    | ((packet[5] & 0xFF) << 8) | (packet[6] & 0xFF);
            int astxCnt;
            Double d;
            Integer i;
            StringBuilder sb;
            String[] fields;
            String out;
            String data;
            switch (pgn) {
                case 65214:
                    i = ((packet[11] & 0xFF) << 8) | ((packet[10]) & 0xFF);
                    if (i.equals(MAX_16)) break;
                    d = i * 0.5;
                    _vehicleInfo.setEngineRatePower(String.format("%.0f", d));
                    break;
                case 65203:
                    d = (((packet[13] & 0xFF) << 24) | ((packet[12] & 0xFF) << 16)
                            | ((packet[11] & 0xFF) << 8) | ((packet[10] & 0xFF))) * .5;
                    _vehicleInfo.setPTOFuelUsed(String.format("%.2f", d));
                    break;
                case 65255:
                    i = (((packet[17] & 0xFF) << 24) | ((packet[16] & 0xFF) << 16)
                            | ((packet[15] & 0xFF) << 8) | ((packet[14] & 0xFF))) * 5 / 100;
                    _vehicleInfo.setPTOHours(i);
                    break;
                case 65110:
                    d = (packet[10] & 0xFF) * .4;
                    _vehicleInfo.setDEFTankLevel(String.format("%.0f", d));

                    i = (packet[14] & 0xFF);
                    data = String.format("%8s", Integer.toBinaryString(i)).replace(' ', '0');
                    _vehicleInfo.setDEFTankLevelLow(data.substring(5, 7) == "001" ? "1" : "0");

                    break;
                case 61445:
                    i = (packet[13] & 0xFF);
                    _vehicleInfo.setTransmissionGear(i);
                    break;
                case 65272:
                    d = (packet[11] & 0xFF) * .4;
                    _vehicleInfo.setTransmissionOilLevel(String.format("%.0f", d));
                    break;
                case 65198:
                    i = (packet[15] & 0xFF) * 10;
                    if (i.equals(MAX_16))
                        break;
                    _vehicleInfo.setAirSuspension(i + "");
                    break;
                case 57344:
                    i = (packet[13] & 0xFF);
                    if (i.equals(MAX_16))
                        break;
                    data = String.format("%8s", Integer.toBinaryString(i)).replace(' ', '0');
                    _vehicleInfo.setSeatBeltFg(data.substring(6, 7) == "01" ? 1 : 0);
                    break;
                case 65207:
                    i = (((packet[19] & 0xFF) << 24) | ((packet[18] & 0xFF) << 16)
                            | ((packet[17] & 0xFF) << 8) | ((packet[16] & 0xFF))) * 5 / 100;
                    if (i.equals(MAX_16))
                        break;
                    _vehicleInfo.setCuriseTime(i);
                    break;
                case 65264:
                    i = (packet[15] & 0xFF);
                    if (i.equals(MAX_16))
                        break;
                    data = String.format("%8s", Integer.toBinaryString(i)).replace(' ', '0');
                    _vehicleInfo.setPTOEngagementFg(data.substring(0, 1) == "01" ? 1 : 0);

                    break;
                case 65279:
                    i = (packet[10] & 0xFF);
                    if (i.equals(MAX_16))
                        break;
                    data = String.format("%8s", Integer.toBinaryString(i)).replace(' ', '0');
                    _vehicleInfo.setWaterInFuelFg(data.substring(0, 1) == "01" ? 1 : 0);

                    break;
                case 64892:
                    i = (packet[11] & 0xFF);
                    if (i.equals(MAX_16))
                        break;
                    data = String.format("%8s", Integer.toBinaryString(i)).replace(' ', '0');
                    data = data.substring(4, 6);
                    _vehicleInfo.setRegenerationRequiredFg(data == "000" ? 0 : 1);
                    break;
                case 65212:
                    i = (((packet[25] & 0xFF) << 24) | ((packet[24] & 0xFF) << 16)
                            | ((packet[23] & 0xFF) << 8) | ((packet[22] & 0xFF)));
                    if (i.equals(MAX_16))
                        break;

                    _vehicleInfo.setBrakeApplication(i);
                    break;
                case 64914:
                    i = (packet[17] & 0xFF) * 4 / 10;
                    if (i.equals(MAX_16))
                        break;
                    _vehicleInfo.setDerateFg(i);
                    break;
                case 61441:
                    i = (packet[15] & 0xFF);
                    if (i.equals(MAX_16))
                        break;
                    data = String.format("%8s", Integer.toBinaryString(i)).replace(' ', '0');

                    CriticalWarningFg = (data.substring(2, 3) == "01");
                    _vehicleInfo.setPowerUnitABSFg(data.substring(0, 1) == "01" ? 1 : 0);
                    // EBS Red Warning Signla
                    break;
                case 61443:
                    i = (packet[12] & 0xFF); // third byte of byte length equal to 1
                    if (i.equals(MAX_16))
                        break;
                    String engineLoad = String.format("%.0f", (i * 1));
                    _vehicleInfo.setEngineLoad(engineLoad);
                    Log.i(TAG, "EngineLoad = " + engineLoad);
                    //odometerChanged();
                    //LogFile.write(CanMessages.class.getName() + "::read RPM from J1939: " + RPM, LogFile.CAN_BUS_READ, LogFile.CANBUS_LOG);
                    break;
                case 61444:
                    i = ((packet[14] & 0xFF) << 8) | (packet[13] & 0xFF);
                    if (i.equals(MAX_16))
                        break;
                    //  newData.put("RPM", (i * 0.125 + "")); /* SPN 190 */
                    CanMessages.RPM = String.format("%.0f", (i * 0.125));
                    _vehicleInfo.setRPM(RPM);
                    Log.i(TAG, "RPM = " + CanMessages.RPM);
                    //odometerChanged();
                    //LogFile.write(CanMessages.class.getName() + "::read RPM from J1939: " + RPM, LogFile.CAN_BUS_READ, LogFile.CANBUS_LOG);
                    break;

                case 64965:
                    sb = new StringBuilder();
                    astxCnt = 0;
                    for (int loop = 10; loop < packet.length; loop++) {
                        sb.append((char) packet[loop]);
                        if (packet[loop] == '*') {
                            astxCnt++;
                            if (astxCnt == 2)
                                break;
                        }
                    }
                    fields = sb.toString().split("\\*");
                    ////  newData.put("ECM", fields[1]); /* SPN 2902 */
                    Log.i(TAG, "ECM = " + fields[1]);
                    break;

                case 64997:
                    i = packet[17] & 0xff;
                    if (i.equals(MAX_16))
                        break;
                    d = i * KM_TO_MI;
                    out = String.format("%.2f mph", d);
                    // newData.put("RoadSpeed", out);
                    Log.i(TAG, "RoadSpeed = " + out);
                    break;

                case 65168:
                    i = ((packet[12] & 0xFF) << 8) | ((packet[11]) & 0xFF);
                    if (i.equals(MAX_16))
                        break;
                    d = i * 0.5 * KW_TO_HP;
                    out = String.format("%.0f HP", d);
                    // newData.put("HP", out); /* SPN 1247 */
                    Log.i(TAG, "HP = " + out);
                    break;

                case 65200:
                    i = (((packet[25] & 0xFF) << 24) | ((packet[24] & 0xFF) << 16)
                            | ((packet[23] & 0xFF) << 8) | ((packet[22] & 0xFF)));
                    if (i.equals(MAX_32))
                        break;
                    d = i * 0.05;
                    // newData.put("IdleHours", d + " hrs"); /* SPN 1037 */
                    break;

                case 65244:
                    i = (((packet[17] & 0xFF) << 24) | ((packet[16] & 0xFF) << 16)
                            | ((packet[15] & 0xFF) << 8) | ((packet[14] & 0xFF)));
                    if (i.equals(MAX_32))
                        break;
                    d = i * 0.05;
                    if (d > 0)
                        TotalIdleHours = String.format("%.2f", d);
                    _vehicleInfo.setIdleHours(TotalIdleHours);
                    //newData.put("IdleHours", d + " hrs"); /* SPN 235 */
                    Log.i(TAG, "Idle Hours = " + d);
                    i = (((packet[13] & 0xFF) << 24) | ((packet[12] & 0xFF) << 16)
                            | ((packet[11] & 0xFF) << 8) | ((packet[10] & 0xFF)));
                    if (i.equals(MAX_32))
                        break;
                    d = i * 0.5;
                    if (d > 0)
                        TotalIdleFuelConsumed = String.format("%.2f", d);
                    _vehicleInfo.setIdleFuelUsed(TotalIdleFuelConsumed);
                    break;

                case 65206:
                /* Max Acomplished Speed since last trip reset */
                    break;

                case 65209:
                    i = (((packet[13] & 0xFF) << 24) | ((packet[12] & 0xFF) << 16)
                            | ((packet[11] & 0xFF) << 8) | ((packet[10] & 0xFF)));
                    if (i.equals(MAX_32))
                        break;
                    d = i * 0.5 * L_TO_GAL;
                    out = String.format("%.2f gal", d);
                    // newData.put("TotalFuel", out); /* SPN 1001 */
                    i = (((packet[25] & 0xFF) << 24) | ((packet[24] & 0xFF) << 16)
                            | ((packet[23] & 0xFF) << 8) | ((packet[22] & 0xFF)));
                    if (i.equals(MAX_32))
                        break;
                    d = i * 0.5 * L_TO_GAL;
                    out = String.format("%.2f gal", d);
                    // newData.put("IdleFuel", out); /* SPN 1004 */
                    Log.i(TAG, "Idle Fuel = " + out);
                    i = (((packet[31] & 0xFF) << 8) | ((packet[30]) & 0xFF));
                    if (i.equals(MAX_16))
                        break;
                    d = i / 512 * KM_TO_MI * (1 / L_TO_GAL);
                    out = String.format("%.2f mi/gal", d);
                    //newData.put("MPG", out); /* SPN 1006 */
                    Log.i(TAG, "MPG = " + out);
                    break;

                case 65248:
                    if (Utility.pgn65217Fg)
                        break;

                    i = (((packet[13] & 0xFF) << 24) | ((packet[12] & 0xFF) << 16)
                            | ((packet[11] & 0xFF) << 8) | ((packet[10] & 0xFF)));
                    if (i.equals(MAX_32))
                        break;
                    d = i * 0.005;// * KM_TO_MI;
                    out = String.format("%.2f mi", d);
                    if (d > Double.parseDouble(CanMessages.OdometerReading))
                        OdometerReading = String.format("%.2f", d);

                    _vehicleInfo.setOdometerReading(OdometerReading);

                    Log.i(TAG, "Odo = " + out);
                    i = (((packet[17] & 0xFF) << 24) | ((packet[16] & 0xFF) << 16)
                            | ((packet[15] & 0xFF) << 8) | ((packet[14] & 0xFF)));
                    if (i.equals(MAX_32))
                        break;
                    d = i * 0.005 * KM_TO_MI;
                    out = String.format("%.2f mi", d);

                    Log.i(TAG, "Trip Odo = " + out);
                    break;
                case 65217:
                    Utility.pgn65217Fg = true;
                    i = (((packet[13] & 0xFF) << 24) | ((packet[12] & 0xFF) << 16)
                            | ((packet[11] & 0xFF) << 8) | ((packet[10] & 0xFF)));
                    if (i.equals(MAX_32))
                        break;
                    d = i * 0.005;// * KM_TO_MI;
                    out = String.format("%.2f mi", d);
                    if (d > Double.parseDouble(CanMessages.OdometerReading))
                        OdometerReading = String.format("%.2f", d);
                    _vehicleInfo.setOdometerReading(OdometerReading);
                    //notify to update layout
                    //odometerChanged();
                    // LogFile.write(CanMessages.class.getName() + "::read Odometer from J1939: " + OdometerReading, LogFile.CAN_BUS_READ, LogFile.CANBUS_LOG);
                    // newData.put("Odo", out); /* SPN 917 */
                    Log.i(TAG, "Odo = " + out);
                    i = (((packet[17] & 0xFF) << 24) | ((packet[16] & 0xFF) << 16)
                            | ((packet[15] & 0xFF) << 8) | ((packet[14] & 0xFF)));
                    if (i.equals(MAX_32))
                        break;
                    d = i * 0.005 * KM_TO_MI;
                    out = String.format("%.2f mi", d);
                    //newData.put("TripOdo", out); /* SPN 918 */
                    Log.i(TAG, "Trip Odo = " + out);
                    break;
                case 65226:
                    //packet[10] bits 1-2: protect lamp, bits 3-4: amber lamp, bits 5-6: red lamp, bits 7-8: MIL
                    //packet[11] reserved lamp
                    int length = (packet[1] - 11) / 4;
                    String dtcDateTime = Utility.getCurrentDateTime();

                    ArrayList<DTCBean> newDtcCode = new ArrayList<>();
                    for (i = 0; i < length; i++) {
                        Integer weird = (packet[14 + i * 4] & 0xff);
                        Integer spn = (packet[12 + i * 4] & 0xff);
                        spn |= ((packet[13 + i * 4] & 0xff) << 8);
                        spn |= ((weird & 0b1110_0000) << 11);
                        Integer fmi = weird & 0b00011111;
                        Integer oc = (packet[15 + i * 4] & 0xff) & 0x7f;
                        if (spn == 0) break;

                        String spnDescription = null;
                        String fmiDescription = null;


                        boolean dExists = false;

                        for (DTCBean dtc : Utility.dtcList) {
                            if (spn == dtc.getSpn() && fmi == dtc.getFmi() && oc == dtc.getOccurence() && dtc.getStatus() == 1) {
                                dExists = true;
                                break;
                            }
                        }

                        if (!dExists) {
                            if (0 < spn && spn < 7576) {
                                spnDescription = SPNMap.map[spn];
                            } else
                                spnDescription = "Contact manufacture";

                            if (0 < fmi && fmi < 32) {
                                fmiDescription = SPNMap.fmi[fmi];
                            } else {
                                fmiDescription = "Contact manufacture";
                            }

                            DTCBean dtcBean = new DTCBean();
                            dtcBean.setSpn(spn);
                            dtcBean.setSpnDescription(spnDescription);
                            dtcBean.setFmi(fmi);
                            dtcBean.setFmiDescription(fmiDescription);
                            dtcBean.setDateTime(dtcDateTime);
                            dtcBean.setProtocol("J1939");
                            dtcBean.setOccurence(oc);
                            dtcBean.setStatus(1);
                            Utility.dtcList.add(dtcBean);
                            newDtcCode.add(dtcBean);
                        }


                        /*if (spnDescription != null && fmiDescription != null)
                            out = String.format("%s, %s, OC %d", spnDescription, fmiDescription, oc);
                        else {
                            out = String.format("SPN %d, FMI %d, OC %d", spn, fmi, oc);
                        }*/
                        // add to database here

                    }

                    if (newDtcCode.size() > 0) {
                        DTCDB.Save(newDtcCode);
                        newDtcCode.clear();
                    }
                    // notifyt to ui here
                    break;
                case 65227:
                    length = (packet[1] - 11) / 4;
                    dtcDateTime = Utility.getCurrentDateTime();

                    newDtcCode = new ArrayList<>();
                    for (i = 0; i < length; i++) {
                        Integer weird = (packet[14 + i * 4] & 0xff);
                        Integer spn = (packet[12 + i * 4] & 0xff);
                        spn |= ((packet[13 + i * 4] & 0xff) << 8);
                        spn |= ((weird & 0b1110_0000) << 11);
                        Integer fmi = weird & 0b00011111;
                        Integer oc = (packet[15 + i * 4] & 0xff) & 0x7f;
                        if (spn == 0) break;
                        String spnDescription = null;
                        String fmiDescription = null;

                        boolean dExists = false;

                        for (DTCBean dtc : Utility.dtcList) {
                            if (spn == dtc.getSpn() && fmi == dtc.getFmi() && oc == dtc.getOccurence() && dtc.getStatus() == 0) {
                                dExists = true;
                                break;
                            }
                        }

                        if (!dExists) {
                            if (0 < spn && spn < 7576) {
                                spnDescription = SPNMap.map[spn];
                            } else
                                spnDescription = "Contact manufacture";

                            if (0 < fmi && fmi < 32) {
                                fmiDescription = SPNMap.fmi[fmi];
                            } else {
                                fmiDescription = "Contact manufacture";
                            }

                            DTCBean dtcBean = new DTCBean();
                            dtcBean.setSpn(spn);
                            dtcBean.setSpnDescription(spnDescription);
                            dtcBean.setFmi(fmi);
                            dtcBean.setFmiDescription(fmiDescription);
                            dtcBean.setDateTime(dtcDateTime);
                            dtcBean.setProtocol("J1939");
                            dtcBean.setOccurence(oc);
                            dtcBean.setStatus(0);
                            Utility.dtcList.add(dtcBean);
                            newDtcCode.add(dtcBean);
                        }


                        /*if (spnDescription != null && fmiDescription != null)
                            out = String.format("%s, %s, OC %d", spnDescription, fmiDescription, oc);
                        else {
                            out = String.format("SPN %d, FMI %d, OC %d", spn, fmi, oc);
                        }*/
                        // add to database here
                    }

                    if (newDtcCode.size() > 0) {
                        DTCDB.Save(newDtcCode);
                        newDtcCode.clear();
                    }
                    // notifyt to ui here
                    break;
                case 65253:
                    i = (((packet[13] & 0xFF) << 24) | ((packet[12] & 0xFF) << 16)
                            | ((packet[11] & 0xFF) << 8) | ((packet[10] & 0xFF)));
                    if (i.equals(MAX_32))
                        break;
                    d = i * 0.05;
                    out = String.format("%.2f", d);
                    //newData.put("Hours", out); /* SPN 247 */
                    if (d > Double.parseDouble(CanMessages.EngineHours))
                        CanMessages.EngineHours = out;
                    _vehicleInfo.setEngineHour(EngineHours);
                    //LogFile.write(CanMessages.class.getName() + "::read EngineHours from J1939: " + EngineHours, LogFile.CAN_BUS_READ, LogFile.CANBUS_LOG);
                    Log.i(TAG, "Engine Hours = " + out);
                    break;

                case 65257:
                    i = (((packet[17] & 0xFF) << 24) | ((packet[16] & 0xFF) << 16)
                            | ((packet[15] & 0xFF) << 8) | ((packet[14] & 0xFF)));
                    if (i.equals(MAX_32))
                        break;
                    d = i * 0.5;
                    if (d > 0)
                        TotalFuelConsumed = String.format("%.2f", d);
                    _vehicleInfo.setFuelUsed(TotalFuelConsumed);
                    break;

                case 65259:
                    sb = new StringBuilder();
                    astxCnt = 0;
                    for (int loop = 10; loop < packet.length; loop++) {
                        sb.append((char) packet[loop]);
                        if (packet[loop] == '*') {
                            astxCnt++;
                            if (astxCnt == 4)
                                break;
                        }
                    }
                    fields = sb.toString().split("\\*");

                    _vehicleInfo.setEngineSerialNo(fields[2]);
                   /* newData.put("Make", fields[0]); *//* SPN 586 *//*
                    newData.put("Model", fields[1]); *//* SPN 587 *//*
                    newData.put("Serial", fields[2]); *//* SPN 588 *//*
                    Log.i(TAG, "Make = " + fields[0]);
                    Log.i(TAG, "Model = " + fields[1]);
                    Log.i(TAG, "Serial = " + fields[2]);*/
                    break;

                case 65260:
                    sb = new StringBuilder();
                    for (int loop = 10; loop < packet.length; loop++) {
                        sb.append((char) packet[loop]);
                        if (packet[loop] == '*') {
                            break;
                        }
                    }
                    fields = sb.toString().split("\\*");
                    // newData.put("VIN", fields[0]);
                    VIN = fields[0];
                    // LogFile.write(CanMessages.class.getName() + "::read VIN from J1939: " + VIN, LogFile.CAN_BUS_READ, LogFile.CANBUS_LOG);
                    Log.i(TAG, "VIN = " + fields[0]);
                    break;

                case 65261:
                    i = packet[10] & 0xff;
                    if (i.equals(MAX_8))
                        break;
                    _vehicleInfo.setMaxRoadSpeed(i + "");
                    out = String.format("%d mph", i);
                    // newData.put("RoadSpeed", out);
                    Log.i(TAG, "Road Speed = " + out);
                    break;

                case 65262:
                    i = (packet[10] & 0xFF);
                    if (i.equals(MAX_8))
                        break;
                    d = (i - 40) * 9 / 5.0 + 32;
                    out = String.format("%.1f%s", d, DEGREE);
                    CoolantTemperature = String.format("%.2f", d);
                    _vehicleInfo.setCoolantTemperature(CoolantTemperature);
                    i = (packet[11] & 0xFF);
                    if (i.equals(MAX_8))
                        break;
                    d = (i - 40) * 9 / 5.0 + 32;
                    out = String.format("%.1f%s", d, DEGREE);

                    // newData.put("FuelTemp", out); /* SPN 174 */
                    // Log.i(TAG, "Fuel = " + out);
                    break;

                case 65263:
                    i = (packet[12] & 0xFF); //engine oil level
                    if (i.equals(MAX_8))
                        break;
                    d = i * .4;
                    if (d > 0) {
                        out = String.format("%.2f", d);
                        EngineOilLevel = out;
                        _vehicleInfo.setEngineOilLevel(EngineOilLevel);
                    }
                    i = (packet[17] & 0xFF); //engine coolant level
                    if (i.equals(MAX_8))
                        break;
                    d = i * .4;
                    if (d > 0) {
                        out = String.format("%.2f", d);
                        EngineCoolantLevel = out;
                        _vehicleInfo.setCoolantLevel(EngineCoolantLevel);
                    }
                    break;

                case 65265:
                    Integer num = ((packet[12] & 0xFF) << 8) | (packet[11] & 0xFF);
                    if (num.equals(MAX_16))
                        break;
                    int sp = num / 256;
                    if (sp != 255) {
                        Speed = String.format("%.0f", sp);
                        _vehicleInfo.setSpeed(Speed);
                        Log.i(TAG, "speed = " + Speed);
                        //  LogFile.write(CanMessages.class.getName() + "::read Speed from J1939: " + Speed, LogFile.CAN_BUS_READ, LogFile.CANBUS_LOG);
                    }

                    try {
                        i = (packet[14] & 0xFF);
                        data = String.format("%8s", Integer.toBinaryString(i)).replace(' ', '0');
                        data = data.substring(0, 1);
                        _vehicleInfo.setCruiseSetFg(data == "01" ? 1 : 0);
                    } catch (Exception e) {
                    }

                    i = (packet[15] & 0xFF);
                    if (i.equals(MAX_8))
                        break;

                    String cruiseSpeed = String.format("%.0f", i);
                    _vehicleInfo.setCruiseSpeed(cruiseSpeed);
                    break;

                case 65266:
                    i = (((packet[15] & 0xFF) << 8) | ((packet[14]) & 0xFF));
                    if (i.equals(MAX_16))
                        break;
                    d = i / 512d;
                    if (d < 10 && d > 0)
                        TotalAverage = String.format("%.2f", d);
                    _vehicleInfo.setAverage(TotalAverage);
                    //  newData.put("FuelRate", out); /* SPN 183 */
                    //  Log.i(TAG, "Fuel Rate = " + out);
                    break;

                case 65270:
                    i = (packet[11] & 0xFF);
                    if (i.equals(MAX_8))
                        break;
                    d = i * 2 * KPA_TO_PSI;
                    out = String.format("%.2f psi", d);
                    // newData.put("Boost", out);
                    //  Log.i(TAG, "Boost = " + out);
                    if (d > 0)
                        Boost = d + "";
                    _vehicleInfo.setBoost(Boost);
                    i = (packet[12] & 0xFF); /* SPN 102 */
                    if (i.equals(MAX_8))
                        break;
                    d = (i - 40) * 9 / 5.0 + 32;
                    out = String.format("%.1f%s", d, DEGREE);
                    // newData.put("Intake", out); /* SPN 105 */
                    // Log.i(TAG, "Intake = " + out);
                    break;

                case 65271:
                    Integer number = ((packet[15] & 0xFF) << 8) | (packet[14] & 0xFF);
                    float vol = number * 0.05f;
                    if (vol > 0)
                        Voltage = String.format("%.1f", vol);
                    _vehicleInfo.setBatteryVoltage(Voltage);
                    break;
                case 65274:
                    i = (packet[10] & 0xFF); // brake application pressure
                    if (i.equals(MAX_8))
                        break;
                    d = i * 4 * KPA_TO_PSI;
                    out = String.format("%.2f", d);
                    if (d > 0)
                        BrakeApplicationPressure = out;

                    i = (packet[11] & 0xFF); // brake primary pressure
                    if (i.equals(MAX_8))
                        break;
                    d = i * 4 * KPA_TO_PSI;
                    out = String.format("%.2f", d);
                    if (d > 0)
                        BrakePrimaryPressure = out;

                    i = (packet[12] & 0xFF); // brake secondary pressure
                    if (i.equals(MAX_8))
                        break;
                    d = i * 4 * KPA_TO_PSI;
                    out = String.format("%.2f", d);
                    if (d > 0)
                        BrakeSecondaryPressure = out;
                    break;
                case 65276:
                    i = (packet[10] & 0xFF);
                    if (i.equals(MAX_16))
                        break;
                    d = i * 0.4;
                    out = String.format("%.0f ", d);
                    if (d > 0)
                        WasherFluidLevel = out;
                    _vehicleInfo.setWasherFluidLevel(WasherFluidLevel);

                    i = (packet[11] & 0xFF);
                    if (i.equals(MAX_16))
                        break;
                    d = i * 0.4;
                    out = String.format("%.0f ", d);
                    if (d > 0)
                        FuelLevel1 = out;
                    break;
            }
        } else if (msgID == RX_J1708) {
            CanDataTime = System.currentTimeMillis();
            if (supportedProtocol == -1) {
                supportedProtocol = RX_J1708;
            } else {
                if (supportedProtocol == RX_J1939) {
                    supportedProtocol = BOTH;
                }
            }
            final int pid = ((packet[4] & 0xFF) << 8) | (packet[5] & 0xFF);
            Double d;
            Integer i;
            StringBuilder sb;
            String data;
            switch (pid) {
                case 150:
                    i = (packet[6] & 0xFF);
                    data = String.format("%8s", Integer.toBinaryString(i)).replace(' ', '0');
                    data = data.substring(1, 2);
                    _vehicleInfo.setPTOEngagementFg(data == "01" ? 1 : 0);
                    break;
                case 49:
                    i = (packet[6] & 0xFF);
                    data = String.format("%8s", Integer.toBinaryString(i)).replace(' ', '0');
                    data = data.substring(2, 3);
                    _vehicleInfo.setPowerUnitABSFg(data == "01" ? 1 : 0);
                    break;
                case 80:
                    d = (packet[6] & 0xFF) * 0.5;
                    if (d > 0)
                        WasherFluidLevel = String.format("%.2f", d);
                    _vehicleInfo.setWasherFluidLevel(WasherFluidLevel);
                    break;
                case 84:
                    d = (packet[6] & 0xFF) * 0.805;
                    Speed = Math.round(d) + "";
                    _vehicleInfo.setSpeed(Speed);
                    break;
                case 85:
                    i = (packet[6] & 0xFF);

                    data = String.format("%8s", Integer.toBinaryString(i)).replace(' ', '0');
                    data = data.substring(0, 0);
                    _vehicleInfo.setCruiseSetFg(data == "1" ? 1 : 0);
                    break;
                case 86:
                    d = (packet[6] & 0xFF) * 0.805;
                    String cruiseSpeed = String.format("%.0f", d);
                    _vehicleInfo.setCruiseSpeed(cruiseSpeed);
                    break;
                case 92:
                    d = (packet[6] & 0xFF) * 0.5;

                    String engineLoad = String.format("%.0f", (d * .5));
                    _vehicleInfo.setEngineLoad(engineLoad);
                    break;
                case 96:
                    d = (packet[6] & 0xFF) * 0.5;
                    if (d > 0)
                        FuelLevel1 = String.format("%.2f", d);
//                    newData.put("Cruise", d + " mi");
                    Log.i(TAG, "Speed = " + d + " mi");
                    break;
                case 97:
                    i = (packet[6] & 0xFF);

                    data = String.format("%8s", Integer.toBinaryString(i)).replace(' ', '0');
                    data = data.substring(7, 7); // 8th position 1 or 0 value
                    _vehicleInfo.setWaterInFuelFg(data == "1" ? 1 : 0);
                    break;
                case 98:
                    d = (packet[6] & 0xFF) * 0.5;
                    if (d > 0)
                        EngineOilLevel = String.format("%.2f", d);
                    _vehicleInfo.setEngineOilLevel(EngineOilLevel);
                    break;
                case 100:
                    d = (packet[6] & 0xFF) * 0.5;
//                    newData.put("Oil Pressure", d + " psi");
//                    Log.i(TAG, "Oil Pressure = " + d + " psi");
                    break;
                case 102:
                    d = (packet[6] & 0xFF) * 0.125;
                    // newData.put("Boost", d + " psi");
                    if (d > 0)
                        Boost = Math.round(d) + "";
                    _vehicleInfo.setBoost(Boost);
                    // Log.i(TAG, "Boost = " + d + " psi");
                    break;
                case 105:
                    i = (packet[6] & 0xFF);
                    // newData.put("Intake", i + DEGREE);
                    // Log.i(TAG, "Intake = " + i + DEGREE);
                    break;
                case 110:
                    d = (packet[6] & 0xFF) * 1.0;
                    CoolantTemperature = String.format("%.2f", d);
                    _vehicleInfo.setCoolantTemperature(CoolantTemperature);
                    //  newData.put("Coolant", i + DEGREE);
                    Log.i(TAG, "Coolant = " + d + DEGREE);
                    break;
                case 111:
                    d = (packet[6] & 0xFF) * 0.5;
                    if (d > 0)
                        EngineCoolantLevel = String.format("%.2f", d);
                    _vehicleInfo.setCoolantLevel(EngineCoolantLevel);
                    break;
                case 116:
                    d = (packet[6] & 0xFF) * 4.14 * KPA_TO_PSI;
                    if (d > 0)
                        BrakeApplicationPressure = String.format("%.2f", d);

                    break;
                case 117:
                    d = (packet[6] & 0xFF) * 4.14 * KPA_TO_PSI;
                    if (d > 0)
                        BrakePrimaryPressure = String.format("%.2f", d);
                    break;
                case 118:
                    d = (packet[6] & 0xFF) * 4.14 * KPA_TO_PSI;
                    if (d > 0)
                        BrakeSecondaryPressure = String.format("%.2f", d);
                    break;
                case 166:
                    i = ((packet[7] & 0xFF) << 8) | (packet[6] & 0xFF);
                    d = i * 0.745;
                    _vehicleInfo.setEngineRatePower(String.format("%.0f", d));
                    break;
                case 174:
                    d = ((packet[6] & 0xFF) | ((packet[7] & 0xFF) << 8)) * 0.25;
                    // newData.put("FuelTemp", d + DEGREE);
                    //  Log.i(TAG, "FuelTemp = " + d + DEGREE);
                    break;
                case 182:
                    d = ((packet[6] & 0xFF) | ((packet[7] & 0xFF) << 8)) * 0.125;
                    // newData.put("TotalFuel", d + "");
                    // Log.i(TAG, "TotalFuel = " + d);
                    break;
                case 183:
                    d = ((packet[6] & 0xFF) | ((packet[7] & 0xFF) << 8)) * 0.015625;
                    //  newData.put("FuelRate", d + " gal");
                    Log.i(TAG, "FuelRate = " + d + " gal");
                    break;
                case 185:
                    d = ((packet[6] & 0xFF) | ((packet[7] & 0xFF) << 8)) * 1.66072 / 1000;
                    if (d > 0 && d < 10)
                        TotalAverage = String.format("%.2f", d);
                    _vehicleInfo.setAverage(TotalAverage);
                    Log.i(TAG, "FuelRate = " + d + " gal");
                    break;
                case 190:
                    d = ((packet[6] & 0xFF) | ((packet[7] & 0xFF) << 8)) * 0.25;
                    CanMessages.RPM = Math.round(d) + "";
                    _vehicleInfo.setRPM(RPM);
                    break;
                case 168:
                    d = ((packet[6] & 0xFF) | ((packet[7] & 0xFF) << 8)) * 0.05;
                    //newData.put("RPM", d + "");
                    if (d > 0)
                        Voltage = String.format("%.1f", d);
                    _vehicleInfo.setBatteryVoltage(Voltage);
                    //  Log.i(TAG, "Voltage = " + d);
                    //  LogFile.write(CanMessages.class.getName() + "::read RPM from J1708: " + RPM, LogFile.CAN_BUS_READ, LogFile.CANBUS_LOG);
                    break;
                case 235:
                    d = ((packet[6] & 0xFF) | (packet[7] & 0xFF) << 8
                            | (packet[8] & 0xFF) << 16 | (packet[9] & 0xFF) << 24) * .05;
                    if (d > 0)
                        TotalIdleHours = String.format("%.2f", d);
                    _vehicleInfo.setIdleHours(TotalIdleHours);
                    break;
                case 236:
                    d = ((packet[6] & 0xFF) | (packet[7] & 0xFF) << 8
                            | (packet[8] & 0xFF) << 16 | (packet[9] & 0xFF) << 24) * .473;
                    if (d > 0)
                        TotalIdleFuelConsumed = String.format("%.2f", d);
                    _vehicleInfo.setIdleFuelUsed(TotalIdleFuelConsumed);
                    break;
                case 237:
                    i = packet[6] & 0xFF;
                    sb = new StringBuilder(i);
                    for (int loop = i - 1; loop >= 0; loop--) // try loop=i if i-1 doesn't work
                        sb.append((char) packet[loop + 7]);
                    VIN = sb.toString();
                    // LogFile.write(CanMessages.class.getName() + "::read VIN from J1708: " + VIN, LogFile.CAN_BUS_READ, LogFile.CANBUS_LOG);
                    // Log.i(TAG, "VIN = " + sb.toString());
                    break;
                case 243:
                    i = packet[6] * 0xFF;
                    sb = new StringBuilder(i);
                    for (int loop = 1; loop < i; loop++) {
                        sb.append((char) (packet[loop + 7] & 0xff));
                    }
                    String id = sb.toString();
                    String[] ids = id.split("\\*");
                    _vehicleInfo.setEngineSerialNo(ids[2]);
//                    newData.put("Make", ids[0]);
//                    newData.put("Model", ids[1]);
//                    newData.put("Serial", ids[2]);
                    break;
                case 244:
                    d = ((packet[7] & 0xFF) | (packet[8] & 0xFF) << 8
                            | (packet[9] & 0xFF) << 16 | (packet[10] & 0xFF) << 24) * .1;
                    // newData.put("TripDistance", d + " mi");
                    //Log.i(TAG, "TripDistance = " + d + " mi");
                    break;
                case 245:
                    d = ((packet[6] & 0xFF) | (packet[7] & 0xFF) << 8
                            | (packet[8] & 0xFF) << 16 | (packet[9] & 0xFF) << 24) * .1;
                    // newData.put("Odo", d + " mi");

                    Log.i(TAG, "Odo = " + d + " mi");
                    if (d > Double.parseDouble(CanMessages.OdometerReading))
                        CanMessages.OdometerReading = String.format("%.2f", d);

                    _vehicleInfo.setOdometerReading(OdometerReading);

                    //  LogFile.write(CanMessages.class.getName() + "::read Odometer from J1708: " + OdometerReading, LogFile.CAN_BUS_READ, LogFile.CANBUS_LOG);
                    //odometerChanged();
                    break;
                case 247:
                    d = ((packet[6] & 0xFF) | (packet[7] & 0xFF) << 8
                            | (packet[8] & 0xFF) << 16 | (packet[9] & 0xFF) << 24) * .05;
                    //  newData.put("Hours", d + " hrs");
                    if (d > Double.parseDouble(CanMessages.EngineHours))
                        CanMessages.EngineHours = String.format("%.2f", d);
                    _vehicleInfo.setEngineHour(EngineHours);
                    // LogFile.write(CanMessages.class.getName() + "::read EngineHours from J1708: " + EngineHours, LogFile.CAN_BUS_READ, LogFile.CANBUS_LOG);
                    Log.i(TAG, "EngineHours = " + d + " hrs");
                    break;
                case 250:
                    d = ((packet[6] & 0xFF) | (packet[7] & 0xFF) << 8
                            | (packet[8] & 0xFF) << 16 | (packet[9] & 0xFF) << 24) * .473;
                    if (d > 0)
                        TotalFuelConsumed = String.format("%.2f", d);
                    _vehicleInfo.setFuelUsed(TotalFuelConsumed);
                    break;

            }
        } else if (msgID == STATS) {
            diagnosticEngineSynchronizationTime = System.currentTimeMillis();
            byte a = packet[11];
            byte b = packet[12];
            byte c = packet[13];
            byte d = packet[14];
            Long canFramesCount = (long) (((a & 0xFF) << 24)
                    | ((b & 0xFF) << 16) | ((c & 0xFF) << 8) | (d & 0xFF));
            HeartBeat = true;
            // newData.put("Frames", canFramesCount + "");
            // Log.i(TAG, "Frames = " + canFramesCount);
            //LogFile.write(CanMessages.class.getName() + "::read STATS Frames: " + canFramesCount, LogFile.CAN_BUS_READ, LogFile.CANBUS_LOG);
            if (canFramesCount == 0) {
                use1708();
            }

        } else if (msgID == ACK) {
            /* Bark */
        }
    }

    private void sendCommand(byte[] command, OutputStream outputStream) {
        if (outputStream != null) {
            try {
                outputStream.write(command);
                outputStream.flush();
            } catch (IOException e) {
                Log.e("J1939", "Send Command Socket Closed", e);
                LogFile.write(CanMessages.class.getName() + "::sendCommand Error:" + e.getMessage(), LogFile.BLUETOOTH_CONNECTIVITY, LogFile.ERROR_LOG);
            }
        }

    }

    private void use1708() {
        int[] initPID_AddFilter = {84, 237, 243, 245, 190, 247, 110, 102, 168};
        int[] initPID_TxFilter = {237, 247};
        ConnectedThread r;
        synchronized (this) {
            // connection check logic pending
            r = mConnectedThread;
        }
        if (r == null)
            return;


        for (int pid : initPID_AddFilter) {
            byte[] message = filterAddJ1708(pid);
            r.write(message);
        }

        for (int pid : initPID_TxFilter) {
            byte[] message = filterTXJ1708(pid);
            r.write(message);
        }
    }

    private int cksum(byte[] commandBytes) {
        int count = 0;

        for (int i = 1; i < commandBytes.length; i++) {
            count += uByte(commandBytes[i]);
        }

        int retVal = (byte) (~(count & 0xFF) + (byte) 1);

        return retVal;
    }

    private int cksum(byte[] data, int numbytes) {
        int count = 0;

        for (int i = 0; i < numbytes; i++) {
            count += uByte(data[i]);
        }
        return (byte) (~(count & 0xFF) + (byte) 1);
    }

    private int uByte(byte b) {
        return (int) b & 0xFF;
    }

    public byte[] filterAddDelJ1939(byte port, long pgnLong, boolean add) {
        byte[] pgn = new byte[3];

        pgn[0] = (byte) ((pgnLong >> 16) & 0xFF);
        pgn[1] = (byte) ((pgnLong >> 8) & 0xFF);
        pgn[2] = (byte) ((pgnLong) & 0xFF);

        byte[] message = new byte[8];
        byte[] stuffed = new byte[17];
        int cnt;

        message[0] = 0;
        message[1] = 6;
        message[2] = (byte) (add ? FA_J1939 : FD_J1939);
        message[3] = port;
        System.arraycopy(pgn, 0, message, 4, 3);

        message[7] = (byte) cksum(message);

        // Tack on beginning of string marker
        ArrayList<Byte> stuffedByteArrayList = new ArrayList<Byte>();

        stuffed[0] = RS232_FLAG;
        stuffedByteArrayList.add(RS232_FLAG);

        int esc_cnt = 1;

        // Bytestuff
        for (cnt = 0; cnt < 8; cnt++) {
            if (message[cnt] == RS232_FLAG) {
                stuffed[cnt + esc_cnt] = RS232_ESCAPE;
                esc_cnt++;
                stuffed[cnt + esc_cnt] = RS232_ESCAPE_FLAG;
            } else if (message[cnt] == RS232_ESCAPE) {
                stuffed[cnt + esc_cnt] = RS232_ESCAPE;
                esc_cnt++;
                stuffed[cnt + esc_cnt] = RS232_ESCAPE_ESCAPE;
            } else {
                stuffed[cnt + esc_cnt] = message[cnt];
            }
        }
        return (stuffed);
    }

    private byte[] cpu_reset_message() {
        Log.i(TAG, "cpu reset message");
        byte[] message = new byte[7];

        message[0] = 0;
        message[1] = 0;
        message[2] = CPU_RESET;
        message[3] = (byte) 0x5A;
        message[4] = (byte) 0x69;
        message[5] = (byte) 0xA5;
        message[6] = (byte) cksum(message, 6);

        int size = message.length * 2 + 1;
        byte[] stuffed = new byte[size];
        int cnt;// Tack on beginning of string marker
        stuffed[0] = RS232_FLAG;
        int esc_cnt = 1;
        // bytestuff
        for (cnt = 0; cnt < message.length; cnt++) {
            if (message[cnt] == RS232_FLAG) {
                stuffed[cnt + esc_cnt] = RS232_ESCAPE;
                esc_cnt++;
                stuffed[cnt + esc_cnt] = RS232_ESCAPE_FLAG;
            } else if (message[cnt] == RS232_ESCAPE) {
                stuffed[cnt + esc_cnt] = RS232_ESCAPE;
                esc_cnt++;
                stuffed[cnt + esc_cnt] = RS232_ESCAPE_ESCAPE;
            } else {
                stuffed[cnt + esc_cnt] = message[cnt];
            }
        }

        return stuffed;
    }

    private byte[] filterAddJ1708(int pid) {
        byte[] message = new byte[6];

        message[0] = 0;
        message[1] = 4;
        message[2] = FA_J1708;
        message[3] = (byte) (pid >> 8);
        message[4] = (byte) pid;
        message[5] = (byte) cksum(message, 5);

        int size = message.length * 2 + 1;
        byte[] stuffed = new byte[size];
        int cnt;// Tack on beginning of string marker
        stuffed[0] = RS232_FLAG;
        int esc_cnt = 1;
        // bytestuff
        for (cnt = 0; cnt < message.length; cnt++) {
            if (message[cnt] == RS232_FLAG) {
                stuffed[cnt + esc_cnt] = RS232_ESCAPE;
                esc_cnt++;
                stuffed[cnt + esc_cnt] = RS232_ESCAPE_FLAG;
            } else if (message[cnt] == RS232_ESCAPE) {
                stuffed[cnt + esc_cnt] = RS232_ESCAPE;
                esc_cnt++;
                stuffed[cnt + esc_cnt] = RS232_ESCAPE_ESCAPE;
            } else {
                stuffed[cnt + esc_cnt] = message[cnt];
            }
        }

        return stuffed;
    }

    public byte[] filterTXJ1939(byte port, long pgnLong) {
        // c0 00 0a 05 00 pp gg nn 00 00 00 ff xx
        // PGN
        byte[] pgn = new byte[3];
        byte[] stuffed = new byte[30];

        pgn[0] = (byte) ((pgnLong) & 0xFF);
        pgn[1] = (byte) ((pgnLong >> 8) & 0xFF);
        pgn[2] = (byte) ((pgnLong >> 16) & 0xFF);

        byte[] message = new byte[14];
        int cnt;

        message[0] = 0;
        message[1] = (byte) (message.length - 2);
        message[2] = TX_J1939;
        message[3] = port;
        System.arraycopy(new byte[]{(byte) 0x00, (byte) 0xEA, (byte) 0x00},
                0, message, 4, 3);

        message[7] = (byte) 255; // destination addr
        message[8] = (byte) 252; // source addr
        message[9] = 6; // priority

        System.arraycopy(pgn, 0, message, 10, 3);

        message[13] = (byte) cksum(message);

        // Tack on beginning of string marker
        stuffed[0] = RS232_FLAG;
        int esc_cnt = 1;
        // bytestuff
        for (cnt = 0; cnt < message.length; cnt++) {
            if (message[cnt] == RS232_FLAG) {
                stuffed[cnt + esc_cnt] = RS232_ESCAPE;
                esc_cnt++;
                stuffed[cnt + esc_cnt] = RS232_ESCAPE_FLAG;
            } else if (message[cnt] == RS232_ESCAPE) {
                stuffed[cnt + esc_cnt] = RS232_ESCAPE;
                esc_cnt++;
                stuffed[cnt + esc_cnt] = RS232_ESCAPE_ESCAPE;
            } else {
                stuffed[cnt + esc_cnt] = message[cnt];
            }
        }

        return stuffed;
    }

    private byte[] filterTXJ1708(int pid) {
        byte[] message = new byte[9];
        message[0] = 0;
        message[1] = 7;
        message[2] = TX_J1708;
        message[3] = (byte) 128;
        message[4] = 0x00;
        message[5] = 0x00;
        message[6] = 3;
        message[7] = (byte) pid;
        message[8] = (byte) cksum(message, 8);

        int size = message.length * 2 + 1;
        byte[] stuffed = new byte[size];
        int cnt;// Tack on beginning of string marker
        stuffed[0] = RS232_FLAG;
        int esc_cnt = 1;
        // bytestuff
        for (cnt = 0; cnt < message.length; cnt++) {
            if (message[cnt] == RS232_FLAG) {
                stuffed[cnt + esc_cnt] = RS232_ESCAPE;
                esc_cnt++;
                stuffed[cnt + esc_cnt] = RS232_ESCAPE_FLAG;
            } else if (message[cnt] == RS232_ESCAPE) {
                stuffed[cnt + esc_cnt] = RS232_ESCAPE;
                esc_cnt++;
                stuffed[cnt + esc_cnt] = RS232_ESCAPE_ESCAPE;
            } else {
                stuffed[cnt + esc_cnt] = message[cnt];
            }
        }

        return stuffed;
    }

    private void initializeFilters(OutputStream outputStream) {
        m_buffer = new byte[4096];
        m_count = 0;

        long[] initPGN_AddFilter = {65265, 65214, 65217, 65261, 65262, 61441, 61443, 61444, 65248, 65253, 65260, 65270, 65271, 65257, 65266, 65209, 65209, 65244, 65226, 65227, 65263, 65276, 64914, 65212,
                64892, 65279, 65264, 65207, 57344, 65198, 65272, 61445, 65110, 65255, 65203};

        long[] initPGN_TxFilter = {65261, 65253, 65260, 65257, 65266, 65209, 65244, 65227};

        for (long pgn : initPGN_AddFilter) {
            byte[] message = filterAddDelJ1939((byte) 0, pgn, true);
            sendCommand(message, outputStream);
        }

        for (long pgn : initPGN_TxFilter) {
            byte[] message = filterTXJ1939((byte) 0, pgn);
            sendCommand(message, outputStream);
        }

        int[] initPID_AddFilter = {49, 80, 84, 85, 86, 92, 96, 97, 98, 102, 110, 111, 150, 168, 185, 190, 235, 236, 237, 245, 247, 250};
        int[] initPID_TxFilter = {235, 236, 237, 247, 250, 150, 166};

        for (int pid : initPID_AddFilter) {
            byte[] message = filterAddJ1708(pid);
            sendCommand(message, outputStream);
        }

        for (int pid : initPID_TxFilter) {
            byte[] message = filterTXJ1708(pid);
            sendCommand(message, outputStream);
        }
    }

    private void request1708() {
        ConnectedThread r;
        synchronized (this) {
            r = mConnectedThread;
        }
        if (r == null)
            return;
        int[] initPID_TxFilter = {235, 236, 237, 247, 250};

        for (int pid : initPID_TxFilter) {
            byte[] message = filterTXJ1708(pid);
            r.write(message);
        }

    }

    private void request1939() {
        // long[] initPGN_TxFilter = {65253, 65260};
        long[] initPGN_TxFilter = {65261, 65266, 65257, 65209, 65244, 65253, 65260, 65227};
        ConnectedThread r;
        synchronized (this) {
            r = mConnectedThread;
        }
        if (r == null)
            return;

        for (long pgn : initPGN_TxFilter) {
            byte[] message = filterTXJ1939((byte) 0, pgn);
            r.write(message);
        }

    }

    Thread thCanHB = null;
    Thread thDiagnostic = null;

    public void StartCanHB() {
        if (thCanHB != null) {
            thCanHB.interrupt();
            thCanHB = null;
        }

        thCanHB = new Thread(runnableHB);
        thCanHB.setName("CanHB");
        thCanHB.start();

        if (thDiagnostic != null) {
            thDiagnostic.interrupt();
            thDiagnostic = null;
        }

        thDiagnostic = new Thread(runnableDiagnostic);
        thDiagnostic.setName("thDiagnostic");
        thDiagnostic.start();
    }

    public void StopCanHB() {
        if (thCanHB != null) {
            thCanHB.interrupt();
            thCanHB = null;
        }

        if (thDiagnostic != null) {
            thDiagnostic.interrupt();
            thDiagnostic = null;
        }
    }


    Thread thTransmitTxHB = null;

    // Deepak Sharma
    // 3 Aug 2016
    // send request to bluetooth device every 5 seconds
    public void startTransmitRequestHB() {

        if (thTransmitTxHB != null) {
            thTransmitTxHB.interrupt();
            thTransmitTxHB = null;
        }
        thTransmitTxHB = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException exe) {
                    }

                    if (mState == CanMessages.STATE_CONNECTED) {
                        Log.i("CanMessageBTBFragment", "Request...");
                        CanMessages.this.request1939();
                    }
                }
            }
        });
        thTransmitTxHB.setName("TransmitRequest");
        thTransmitTxHB.start();
    }


    // Deepak Sharma
    // 4 October 2016
    // Reconnect Device after unpair and repair
    private void reconnectDevice() {

        try {
            BluetoothDevice device = mAdapter.getRemoteDevice(deviceAddress);

            Class btClass = Class.forName("android.bluetooth.BluetoothDevice");
            Method removeBondMethod = btClass.getMethod("removeBond");
            removeBondMethod.invoke(device);

            Method createBondMethod = btClass.getMethod("createBond");
            Boolean result = (Boolean) createBondMethod.invoke(device);
            if (result) {
                unpairFg = true;
                //device.setPairingConfirmation(true);
                CanMessages.this.connect(device, true);
            } else {
                unpairFg = false;
            }


        } catch (Exception exe) {
            unpairFg = false;
        }
    }

    private static int REPAIR_INTERVAL = 5 * 60;
    private static boolean unpairFg = false;
    private static boolean btbError = false;
    private static int BTB_ERROR_INTERVAL = 2 * 60;
    private Runnable runnableDiagnostic = new Runnable() {
        @Override
        public void run() {
            try {
                Thread.sleep(2 * 60 * 1000);
            } catch (InterruptedException exe) {
            }

            while (true) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException exe) {
                }

                if (MainActivity.undockingMode) {
                    continue;
                }
                int timeDifference = (int) ((System.currentTimeMillis() - diagnosticEngineSynchronizationTime) / 1000);
                // Log.i(TAG, "BTBSince: " + timeDifference + "");
               /* if (timeDifference > REPAIR_INTERVAL) {
                    if (!unpairFg) {
                        reconnectDevice();
                    }
                } else {
                    unpairFg = false;
                }*/


                if (timeDifference > MALFUNCTION_ENGINE_SYNCHRONIZTION) {
                    if (!DiagnosticIndicatorBean.EngineSynchronizationMalfunctionFg) {
                        DiagnosticIndicatorBean.EngineSynchronizationMalfunctionFg = true;
                        // save malfunction for engine synchronization compliance
                        DiagnosticMalfunction.saveDiagnosticIndicatorByCode("E", 1, "EngineSynchronizationMalfunctionFg");
                    }

                } else if (timeDifference > BTB_ERROR_INTERVAL) {
                    if (!btbError) {
                        btbError = true;
                        if (mCanListner != null && !ConstantFlag.Flag_Development) {
                            mCanListner.onAlertError();
                        }
                    }
                } else if (timeDifference > DIAGNOSTIC_ENGINE_SYNCHRONIZTION) {
                    if (!DiagnosticIndicatorBean.EngineSynchronizationDiagnosticFg) {

                        DiagnosticIndicatorBean.EngineSynchronizationDiagnosticFg = true;
                        // save data diagnostic event for engine synchronization
                        DiagnosticMalfunction.saveDiagnosticIndicatorByCode("2", 3, "EngineSynchronizationDiagnosticFg");
                        // startDiagnosticTimer();
                        Utility.saveDiagnosticTime(true);

                        if (mCanListner != null && !ConstantFlag.Flag_Development) {
                            mCanListner.onAlertWarning();
                        }
                    }
                } else {
                    // clear malfunction event
                    if (DiagnosticIndicatorBean.EngineSynchronizationMalfunctionFg) {
                        DiagnosticIndicatorBean.EngineSynchronizationMalfunctionFg = false;
                        // clear malfunction for engine synchronization compliance
                        DiagnosticMalfunction.saveDiagnosticIndicatorByCode("E", 2, "EngineSynchronizationMalfunctionFg");
                    }

                    // clear diagnostic event
                    if (DiagnosticIndicatorBean.EngineSynchronizationDiagnosticFg) {
                        btbError = false;
                        if (mCanListner != null) {
                            mCanListner.onAlertClear();
                        }
                        DiagnosticIndicatorBean.EngineSynchronizationDiagnosticFg = false;
                        // save data diagnostic event for engine synchronization
                        DiagnosticMalfunction.saveDiagnosticIndicatorByCode("2", 4, "EngineSynchronizationDiagnosticFg");

                    }
                }
            }
        }
    };

    int transmitRequest = 1; // request to can bus every 5 second
    int connectRequest = 1; // connect to bluetooth if disconnected
    int vehicleInfoRequest = 1;
    private Runnable runnableHB = new Runnable() {
        @Override
        public void run() {
            try {
                while (mState != CanMessages.STATE_CONNECTED && connectRequest < 60) {
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

                    if (mState == CanMessages.STATE_CONNECTED) {
                        if (GPSData.CurrentStatus < 3) { // if engine is on then record vehicle info
                            if (vehicleInfoRequest >= 60 * 5) {
                                vehicleInfoRequest = 1;
                                _vehicleInfo.setCreatedDate(Utility.getCurrentDateTime());
                                VehicleInfoDB.Save(_vehicleInfo);
                            } else
                                vehicleInfoRequest++;
                        }

                        if (transmitRequest == 5) {
                            transmitRequest = 1;
                            Log.i("CanMessage", "BTB Request...");
                            CanMessages.this.request1708();
                            CanMessages.this.request1939();
                        } else
                            transmitRequest++;
                    }
                    // reconnect logic
                    else if (CanMessages.mState == CanMessages.STATE_LISTEN) {
                        if (connectRequest == 60) {
                            connectRequest = 1;
                            // Get the BluetoothDevice object
                            BluetoothDevice device = mAdapter.getRemoteDevice(deviceAddress);
                            Log.i("CanMessage", "Connect...");
                            // Attempt to connect to the device
                            CanMessages.this.connect(device, true);
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

    public static ICanMessage mCanListner;

    public interface ICanMessage {
        void onAlertClear();

        void onAlertWarning();

        void onAlertError();
    }
}
