package com.hutchgroup.elog.fragments;


import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.usb.UsbConstants;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.DialogFragment;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioButton;

import com.hutchgroup.elog.R;
import com.hutchgroup.elog.common.LogFile;
import com.hutchgroup.elog.common.OutputFile;
import com.hutchgroup.elog.common.StorageDirectory;
import com.hutchgroup.elog.common.Utility;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;
import java.util.StringTokenizer;

public class OutputFileSendDialog extends DialogFragment {

    public OutputFileDialogInterface mListener;

    EditText etComment, etPassword;
    RadioButton rdUsbDrive, rdBluetooth, rdEmail, rdWebService;
    Button btnSend;
    String TAG = OutputFileSendDialog.class.getName();
    //UsbManager mUsbManager;
    private static final String ACTION_USB_PERMISSION = "com.hutchgroup.elog.USB_PERMISSION";

    private static final int SAVE_REQUEST_CODE = 42;
    PendingIntent mPermissionIntent;

    private static int TIMEOUT = 0;
    private boolean forceClaim = true;

    UsbManager mUsbManager = null;
    IntentFilter filterAttached_and_Detached = null;
    ImageButton imgCancel;

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    final UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
//Body of your click handler
                            Thread thread = new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    //TransferToUSB(device);
                                    Log.i("UsbFile", "SaveTestFile UsbReceiver");
                                    SaveReportToUSB();
                                }
                            });
                            thread.start();
                        }
                    } else {
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };

    public OutputFileSendDialog() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_output_file_send, container);
        try {
            initialize(view);
            // getDialog().setTitle("Send ELD Data");
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
            this.setCancelable(true);


        } catch (Exception e) {
            LogFile.write(OutputFileSendDialog.class.getName() + "::onCreateView Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        try {

        } catch (Exception e) {
            LogFile.write(OutputFileSendDialog.class.getName() + "::onActivityCreated Error: " + e.getMessage(), LogFile.USER_INTERACTION, LogFile.ERROR_LOG);
        }
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume");
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        super.onResume();
    }

    private void initialize(View view) {
        // register broad cast reciever to check permission to communicate with usb
        mUsbManager = (UsbManager) getActivity().getSystemService(Context.USB_SERVICE);
        //mPermissionIntent = PendingIntent.getBroadcast(getActivity(), 0, new Intent(ACTION_USB_PERMISSION), 0);
        //IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        //getActivity().registerReceiver(mUsbReceiver, filter);

        //
        filterAttached_and_Detached = new IntentFilter(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        filterAttached_and_Detached.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filterAttached_and_Detached.addAction(ACTION_USB_PERMISSION);
        //
        getActivity().registerReceiver(mUsbReceiver, filterAttached_and_Detached);

        etComment = (EditText) view.findViewById(R.id.etComment);
        etPassword = (EditText) view.findViewById(R.id.etPassword);
        etPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getActionMasked();

                switch (action) {

                    case MotionEvent.ACTION_DOWN:
                        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                        break;

                    case MotionEvent.ACTION_UP:
                        etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        break;
                }

                return v.onTouchEvent(event);
            }
        });
        rdUsbDrive = (RadioButton) view.findViewById(R.id.rdUsbDrive);
        rdUsbDrive.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    etPassword.setVisibility(View.VISIBLE);
                } else {
                    etPassword.setVisibility(View.GONE);
                }
            }
        });
        rdBluetooth = (RadioButton) view.findViewById(R.id.rdBluetooth);
        rdEmail = (RadioButton) view.findViewById(R.id.rdEmail);
        rdWebService = (RadioButton) view.findViewById(R.id.rdWebService);
        imgCancel = (ImageButton) view.findViewById(R.id.imgCancel);
        imgCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.hideKeyboard(getActivity(), v);
                dismiss();
            }
        });

        btnSend = (Button) view.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utility.hideKeyboard(getActivity(), v);
                if (rdUsbDrive.isChecked()) {
                    String password = etPassword.getText().toString();
                    if (password.isEmpty()) {
                        Utility.showMsg("Please enter Password!");
                    } else {
                        String encryptedPassword, salt;
                        if (Utility.user1.isOnScreenFg()) {
                            encryptedPassword = Utility.user1.getPassword();
                            salt = Utility.user1.getSalt();
                        } else {
                            encryptedPassword = Utility.user2.getPassword();
                            salt = Utility.user2.getSalt();

                        }

                        if (!Utility.computeSHAHash(password, salt).equals(encryptedPassword)) {
                            Utility.showMsg("Please entered valid Password!");
                        } else {
                            /*HashMap<String, UsbDevice> deviceList = mUsbManager.getDeviceList();
                            if (deviceList.isEmpty()) {

                                Utility.showMsg("Please attach USB Drive first");
                            } else {
                                UsbDevice device = deviceList.get(0);
                                try {
                                    mUsbManager.requestPermission(device, mPermissionIntent);
                                } catch (Exception exe) {
                                    Utility.showMsg("Error: " + exe.getMessage());
                                }
                                // export();
                                Log.i("UsbFile", "SaveTestFile onSend");
                                SaveTestFile();
                            }*/

                            if (StorageDirectory.getUSBDirectory() == "") {
                                Utility.showMsg("Please attach USB Drive first");
                            } else {
                                //this cannot work from API 19+, because we don't have permission to write on USB, cannot use UsbManager to grant permission
                                SaveReportToUSB();

                                //this is used from API 19, it uses Storage Access Framework
                                //saveReportWithSAF();
                            }
                        }
                    }

                } else if (rdBluetooth.isChecked()) {

                    SendFile(1);
                } else if (rdEmail.isChecked()) {
                    SendFile(0);
                } else if (rdWebService.isChecked()) {
                } else {
                    Utility.showMsg("Please select send method!");
                }
            }
        });

    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();
        getActivity().unregisterReceiver(mUsbReceiver);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode,
                                 Intent resultData) {

        Uri currentUri = null;

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == SAVE_REQUEST_CODE) {

                if (resultData != null) {
                    currentUri = resultData.getData();
                    if (currentUri.getHost().equals("com.android.externalstorage.documents")) {
                        final String encodedPath = currentUri.getEncodedPath();
                        final String path = encodedPath.substring(encodedPath.indexOf("%3A") + 3);
                        final File[] storagePoints = new File("/storage").listFiles();

                        // document/primary is in /storage/emulated/legacy and thus will fail the exists check in the else handling loop check
                        if (encodedPath.startsWith("/document/primary")) {
                            // External file stored in Environment path
                            final File externalFile = new File(Environment.getExternalStorageDirectory(), path);
                            MediaScannerConnection.scanFile(getActivity(),
                                    new String[]{externalFile.getAbsolutePath()}, null, null);
                        } else {
                            // External file stored in one of the mount points, check each mount point for the file
                            for (int i = 0, j = storagePoints.length; i < j; ++i) {
                                final File externalFile = new File(storagePoints[i], path);
                                if (externalFile.exists()) {
                                    MediaScannerConnection.scanFile(getActivity(),
                                            new String[]{externalFile.getAbsolutePath()}, null, null);
                                    break;
                                }
                            }
                        }
                    }

                    writeFileContent(currentUri);
                }
            }
        }
    }

    public void saveReportWithSAF() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("text/*");
        intent.putExtra(Intent.EXTRA_TITLE, OutputFile.fileName);

        startActivityForResult(intent, SAVE_REQUEST_CODE);
    }

    private void writeFileContent(Uri uri) {
        try {
            Utility.showMsg("WriteFile: " + uri);
            ParcelFileDescriptor pfd = getActivity().getContentResolver().openFileDescriptor(uri, "w");

            FileOutputStream out = new FileOutputStream(pfd.getFileDescriptor());

            String textContent = OutputFile.getOutputFile(etComment.getText() + "");
            Utility.showMsg("textContent: " + textContent);

            PrintWriter pw = new PrintWriter(out);
            pw.println(textContent);
            pw.flush();
            pw.close();
            out.flush();
            out.close();

            pfd.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Utility.showMsg("FileNotFoundException: " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Utility.showMsg("IOException: " + e.getMessage());
        }
    }

    private void TransferToUSB(UsbDevice device) {
        try {
            String data = OutputFile.getOutputFile(etComment.getText() + "");

            File file = new File(Utility.context.getFilesDir(), OutputFile.fileName);

            try {
                Thread.sleep(3000);
                FileOutputStream f = new FileOutputStream(file);
                PrintWriter pw = new PrintWriter(f);
                pw.write(data);
                pw.flush();
                pw.close();
                f.close();

                UsbInterface intf = device.getInterface(0);
                UsbEndpoint endpoint = intf.getEndpoint(0);
                for (int i = 0; i < intf.getEndpointCount(); i++) {

                    int type = intf.getEndpoint(i).getType();
                    int direction = intf.getEndpoint(i).getDirection();
                    if (direction == UsbConstants.USB_DIR_OUT) {
                        endpoint = intf.getEndpoint(i);
                    }
                }

                UsbDeviceConnection connection = mUsbManager.openDevice(device);
                connection.claimInterface(intf, forceClaim);

                byte[] bytes = new byte[(int) file.length()];
                FileInputStream fileInputStream = new FileInputStream(file);
                fileInputStream.read(bytes);

                int count = connection.bulkTransfer(endpoint, bytes, bytes.length, TIMEOUT);
                //  connection.close();
                outputFileSuccess();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Utility.showMsg("Error: File is not found");
                outputFileFailed();
            } catch (IOException e) {
                e.printStackTrace();
                outputFileFailed();
            } catch (InterruptedException e) {

            }

            //call method to set up device communication
//            UsbInterface intf = device.getInterface(0);
//            UsbEndpoint endpoint = intf.getEndpoint(0);
//            UsbDeviceConnection connection = mUsbManager.openDevice(device);
//            connection.claimInterface(intf, forceClaim);
//            connection.bulkTransfer(endpoint, bytes, bytes.length, TIMEOUT);
//            connection.close();
        } catch (Exception e) {
            Utility.showMsg("Error: " + e.getMessage());
        }
    }

    private void SendFile(int sendOption) {
        try {

            String data = OutputFile.getOutputFile(etComment.getText() + "");

            FileOutputStream outputStream;

            outputStream = getActivity().openFileOutput(OutputFile.fileName, Context.MODE_WORLD_READABLE);
            outputStream.write(data.getBytes());


            Intent emailIntent = new Intent(Intent.ACTION_SEND);
            emailIntent.setType("text/plain");

            if (sendOption == 1)
                emailIntent.setPackage("com.android.bluetooth");
            else if (sendOption == 2) {
                emailIntent.setPackage("com.android.bluetooth");
                emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"bdeepak.jsharma@gmail.com"});
                emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Output File");
                emailIntent.putExtra(Intent.EXTRA_TEXT, "Testing output file");
                emailIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            }
            File file = new File(Utility.context.getFilesDir(), OutputFile.fileName);
            if (!file.exists() || !file.canRead()) {
                return;
            }
            Uri uri = Uri.fromFile(file);
            //Uri uri = Uri.parse("content://" + CachedFileProvider.AUTHORITY + "/"                + OutputFile.fileName);
            emailIntent.putExtra(Intent.EXTRA_STREAM, uri);

            startActivity(Intent.createChooser(emailIntent, "Pick an Email provider"));

            outputStream.close();
            outputFileSuccess();
        } catch (Exception ex) {
            //data transfer failed
            outputFileFailed();
        }
    }

    public void export() {

        String data = OutputFile.getOutputFile(etComment.getText() + "");
        //save file through usb
        String dir = StorageDirectory.getUSBDirectory();
        Utility.showMsg("Directory: " + dir);
        //dir += "/download";
        File downloadDir = new File(dir + "/download");
        downloadDir.mkdirs();
        File file = new File(downloadDir, OutputFile.fileName);

        try {
            Thread.sleep(3000);
            FileOutputStream f = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(f);
            pw.write(data);
            pw.flush();
            pw.close();
            f.close();

            outputFileSuccess();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Utility.showMsg("Error: File is not found");
            outputFileFailed();
        } catch (IOException e) {
            e.printStackTrace();
            outputFileFailed();
        } catch (InterruptedException e) {
            outputFileFailed();
        }
    }

    private void outputFileSuccess() {
        SharedPreferences sp = getActivity().getSharedPreferences("HutchGroup", getContext().MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        e.putString("data_transfer", Utility.getCurrentDate());
        e.commit();
        if (mListener != null) {
            mListener.outputFileSuccess();
        }
    }

    private void outputFileFailed() {
        SharedPreferences sp = getActivity().getSharedPreferences("HutchGroup", getContext().MODE_PRIVATE);
        SharedPreferences.Editor e = sp.edit();
        e.putString("data_transfer_failed", Utility.getCurrentDate());
        e.commit();
        if (mListener != null) {
            mListener.outputFileFailed();
        }
    }

    private void SaveReportToUSB() {
        try {
            String root = StorageDirectory.getUSBDirectory();
            //Utility.showMsg("Root folder=" + root);
            File rootDir = new File(root);
            rootDir.mkdirs();

            String data = OutputFile.getOutputFile(etComment.getText() + "");

            File file = new File(rootDir, OutputFile.fileName);
            if (file.exists()) {
                file.delete();
            }

            FileOutputStream out = new FileOutputStream(file);
            PrintWriter pw = new PrintWriter(out);
            pw.println(data);
            pw.flush();
            pw.close();
            out.flush();
            out.close();

            outputFileSuccess();
            //Utility.showMsg("Send report successfully!");
        } catch (Exception e) {
            e.printStackTrace();
            //Utility.showMsg("Error in Send report: " + e.getMessage());
            outputFileFailed();
        }
    }


    public interface OutputFileDialogInterface {
        void outputFileSuccess();

        void outputFileFailed();
    }
}
