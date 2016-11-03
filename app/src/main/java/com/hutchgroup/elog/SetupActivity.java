package com.hutchgroup.elog;

/*
 * SetupActivity
 * Purpose: To check and make sure ELD application's requirement is match before running the ELD
 */

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.WindowManager;

import com.hutchgroup.elog.common.ConstantFlag;
import com.hutchgroup.elog.common.Utility;
import com.hutchgroup.elog.fragments.BTBConnectionFragment;
import com.hutchgroup.elog.fragments.CanBusDataFragment;
import com.hutchgroup.elog.fragments.GpsSignalFragment;
import com.hutchgroup.elog.fragments.SummaryFragment;
import com.hutchgroup.elog.fragments.WelcomeSetupFragment;
import com.hutchgroup.elog.fragments.WirelessConnectivityFragment;


public class SetupActivity extends AppCompatActivity implements WelcomeSetupFragment.OnFragmentInteractionListener,
        CanBusDataFragment.OnFragmentInteractionListener,
        WirelessConnectivityFragment.OnFragmentInteractionListener,
        BTBConnectionFragment.OnFragmentInteractionListener,
        GpsSignalFragment.OnFragmentInteractionListener,
        SummaryFragment.OnFragmentInteractionListener {

    String TAG = SetupActivity.class.getName();
    SharedPreferences prefs = null;

    SummaryFragment summaryFragment;

    /*
     * Review date: Jun 29 2016
     * Reviewed by: Minh Tran
     * Purpose: create activity for setup
     * Input: the Bundle to get data transfer from the other Activity (it's required by Android)
     * Output: no output
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //saving the context to check and grant permissions
        Utility.context = this;
        //check and grant permisstions
        Utility.checkAndGrantPermissions();
        //get IMEI of the device
        Utility.IMEIGet(SetupActivity.this);

        //check if device is phone, set the application support only Portrait mode
        if (!Utility.isLargeScreen(getApplicationContext())) {
            //set the orientation of application to Portrait mode
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        //setup layout for the Activity
        setContentView(R.layout.setup_activity);

        //get fragment manager to add Fragment into Activity
        FragmentManager manager = getSupportFragmentManager();
        //create Welcome fragment to add it in
        WelcomeSetupFragment fragment = new WelcomeSetupFragment();

        //get the transaction to add fragment
        FragmentTransaction ft = manager.beginTransaction();
        //call replace to add fragment into the layout
        ft.replace(R.id.container, fragment);
        //set the transition if open for fragment
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        //commit the transaction to show fragment
        ft.commit();

        //create summary fragment to save status of setup progress
        summaryFragment = new SummaryFragment();
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    /*
     * Review date: Jun 29 2016
     * Reviewed by: Minh Tran
     * Purpose: catch the event when user change orientation of device
     * Input: the new configuration (it is from Android)
     * Output: no output
     */
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        //check if it is phone, call return for no handle configuration in fragment
        if (!Utility.isLargeScreen(getApplicationContext())) {
            return;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
     * Review date: Jun 29 2016
     * Reviewed by: Minh Tran
     * Purpose: call from interface to go to WirelessConnectivity screen
     * Input: no input
     * Output: no output
     */
    @Override
    public void onNextToWirelessConnectivity() {
        //create Wireless fragment
        WirelessConnectivityFragment fragment = new WirelessConnectivityFragment();

        //call replace to show the fragment
        replaceFragment(fragment);
    }

    /*
     * Review date: Jun 29 2016
     * Reviewed by: Minh Tran
     * Purpose: call from interface to go to Bluetooth Connectivity screen or GPS checking
     * Input: no input
     * Output: no output
     */
    @Override
    public void onNextFromWirelessConnectivity() {
        //check if it is production build, show Bluetooth Connectivity
        if (ConstantFlag.PRODUCTION_BUILD) {
            //create Bluetooth Connectivity fragment
            BTBConnectionFragment fragment = new BTBConnectionFragment();
            //call replace to show the fragment
            replaceFragment(fragment);
        } else {
            //if not a production build, call to show GPS checking
            onNextToGPSCheck();
        }
    }

    /*
     * Review date: Jun 29 2016
     * Reviewed by: Minh Tran
     * Purpose: call from interface to show data from CanBus
     * Input: no input
     * Output: no output
     */
    @Override
    public void onNextToCanbusRead() {
        //create canbus fragment to show data
        CanBusDataFragment fragment = new CanBusDataFragment();

        //call replace to show the fragment
        replaceFragment(fragment);
    }

    /*
     * Review date: Jun 29 2016
     * Reviewed by: Minh Tran
     * Purpose: call from interface to show GPS checking screen
     * Input: no input
     * Output: no output
     */
    @Override
    public void onNextToGPSCheck() {
        //create GPS checking fragment
        GpsSignalFragment fragment = new GpsSignalFragment();

        //call replace to show the fragment
        replaceFragment(fragment);
    }

    /*
     * Review date: Jun 29 2016
     * Reviewed by: Minh Tran
     * Purpose: call from interface to go to summary screen
     * Input: no input
     * Output: no output
     */
    @Override
    public void onNextToSummary() {
        //call replace to show Summary
        replaceFragment(summaryFragment);
    }

    /*
     * Review date: Jun 29 2016
     * Reviewed by: Minh Tran
     * Purpose: replace function to show the fragment
     * Input: fragment to display
     * Output: no output
     */
    private void replaceFragment(Fragment fragment) {
        //get fragment manager to add Fragment into Activity
        FragmentManager manager = getSupportFragmentManager();

        //get the transaction to add fragment
        FragmentTransaction ft = manager.beginTransaction();
        //call replace to add fragment into the layout
        ft.replace(R.id.container, fragment);
        //set the transition if open for fragment
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        //commit the transaction to show fragment
        ft.commitAllowingStateLoss();
    }

    /*
     * Review date: Jun 29 2016
     * Reviewed by: Minh Tran
     * Purpose: call from interface to go to ELD
     * Input: no input
     * Output: no output
     */
    @Override
    public void proceedToELD() {
        //get the preference of the application
        prefs = this.getSharedPreferences("HutchGroup", getBaseContext().MODE_PRIVATE);
        //save the value false for firstrun
        prefs.edit().putBoolean("firstrun", false).commit();

        //create intent to launch ELD
        Intent intent = new Intent(SetupActivity.this, SplashActivity.class);
        //create bundle to pass flag firstrun to ELD
        Bundle bundle = new Bundle();
        //put value for flag firstrun
        bundle.putBoolean("firstrun", true);
        //put bundle into intent to pass to ELD activity
        intent.putExtras(bundle);
        //start the SplashActivity
        startActivity(intent);
        //call finish to close SetupActivity
        finish();
    }

    /*
     * Review date:
     * Reviewed by:
     * Purpose:
     * Input:
     * Output:
     */
    @Override
    public void onUpdateCellularStatus(boolean isPassed) {
        //summaryLayout.setVisibility(View.VISIBLE);

        if (summaryFragment != null) {
            summaryFragment.updateCellularConnection(isPassed);
        }
    }

    /*
     * Review date:
     * Reviewed by:
     * Purpose:
     * Input:
     * Output:
     */
    @Override
    public void onUpdateWifiStatus(boolean isPassed) {
        if (summaryFragment != null) {
            summaryFragment.updateWifiConnection(isPassed);
        }
    }

    /*
     * Review date:
     * Reviewed by:
     * Purpose:
     * Input:
     * Output:
     */
    @Override
    public void onUpdateHutchConnectionStatus(boolean isPassed) {
        if (summaryFragment != null) {
            summaryFragment.updateHutchConnection(isPassed);
        }
    }

    /*
     * Review date:
     * Reviewed by:
     * Purpose:
     * Input:
     * Output:
     */
    @Override
    public void onUpdateDownloadDataStatus(boolean isPassed) {
        if (summaryFragment != null) {
            summaryFragment.updateDownloadConfiguration(isPassed);
        }
    }

    /*
     * Review date:
     * Reviewed by:
     * Purpose:
     * Input:
     * Output:
     */
    @Override
    public void onUpdateBluetoothOnOff(boolean isPassed) {
        if (summaryFragment != null) {
            summaryFragment.updateBluetoothOnOff(isPassed);
        }
    }

    /*
     * Review date:
     * Reviewed by:
     * Purpose:
     * Input:
     * Output:
     */
    @Override
    public void onUpdateBluetoothConnection(boolean isPassed) {
        if (summaryFragment != null) {
            summaryFragment.updateBluetoothConnection(isPassed);
        }
    }

    /*
     * Review date:
     * Reviewed by:
     * Purpose:
     * Input:
     * Output:
     */
    @Override
    public void onUpdateBluetoothHeartBeat(boolean isPassed) {
        if (summaryFragment != null) {
            summaryFragment.updateBluetoothHeartBeat(isPassed);
        }
    }

    /*
     * Review date:
     * Reviewed by:
     * Purpose:
     * Input:
     * Output:
     */
    @Override
    public void onUpdateFindingStatelliteStatus(boolean isPassed) {
        if (summaryFragment != null) {
            summaryFragment.updateFindingStatelliteStatus(isPassed);
        }
    }

    /*
     * Review date:
     * Reviewed by:
     * Purpose:
     * Input:
     * Output:
     */
    @Override
    public void onUpdateUTCTimeStatus(boolean isPassed) {
        if (summaryFragment != null) {
            summaryFragment.updateUTCTimeStatus(isPassed);
        }
    }

    /*
     * Review date:
     * Reviewed by:
     * Purpose:
     * Input:
     * Output:
     */
    @Override
    public void onUpdateLatitudeLongitudeStatus(boolean isPassed) {
        if (summaryFragment != null) {
            summaryFragment.updateLatitudeLongitudeStatus(isPassed);
        }
    }

    /*
     * Review date:
     * Reviewed by:
     * Purpose:
     * Input:
     * Output:
     */
    @Override
    public void onUpdateCurrentLocationStatus(boolean isPassed, int numberFailed) {
        if (summaryFragment != null) {
            summaryFragment.updateCurrentLocationStatus(isPassed, numberFailed);
        }
    }

}
