package com.hutchgroup.elog.tracklocations;

import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import com.hutchgroup.elog.FirstActivity;
import com.hutchgroup.elog.common.CanMessages;
import com.hutchgroup.elog.common.GPSTracker;
import com.hutchgroup.elog.common.Utility;

public class GpsBroadCastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action)) {
            Log.i("ELog", "Boot completed");
            Intent i = new Intent(context, FirstActivity.class);
            //Intent i = new Intent(context, SetupActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

        } else if (BluetoothDevice.ACTION_ACL_CONNECTED.equals(action)) {

            BluetoothDevice device = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            showMessage(context, device.getName() + " connected");
        } else if (BluetoothDevice.ACTION_ACL_DISCONNECTED.equals(action)) {
            BluetoothDevice device = intent
                    .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

            showMessage(context, "Device disconnected: " + device.getName());
        } else if (Intent.ACTION_SCREEN_ON.equals(action)) {
            showMessage(context, "Screen On ");

        } else if (Intent.ACTION_SHUTDOWN.equals(action)) {
            showMessage(context, "Shutdown starts ");

            try {
                ClientSocket obj = new ClientSocket(
                        Utility.ServerIp, Utility.Port, context);
                String signal = GPSTracker.getShutDownEvent();
                obj.execute(signal, "-1");
                Thread.sleep(5000);
            } catch (InterruptedException exe) {

            }
        }
    }

    private void showMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
