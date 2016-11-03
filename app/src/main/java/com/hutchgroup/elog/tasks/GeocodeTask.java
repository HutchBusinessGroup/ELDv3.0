package com.hutchgroup.elog.tasks;

import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.util.Log;

import com.hutchgroup.elog.common.GetCall;
import com.hutchgroup.elog.common.Utility;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeocodeTask extends AsyncTask<Void, Void, Address> {

    String errorMessage = "";

    String TAG = SyncData.class.getName();

    private PostTaskListener<Address> postTaskListener;

    public GeocodeTask(PostTaskListener<Address> postTaskListener){
        this.postTaskListener = postTaskListener;
    }

    @Override
    protected void onPreExecute() {

    }

    @Override
    protected Address doInBackground(Void ... none) {
        Log.e(TAG, "Get current location");
        try {
            Geocoder geocoder = new Geocoder(Utility.context, Locale.getDefault());
            List<Address> addresses = null;

            try {
                addresses = geocoder.getFromLocation(Utility.currentLocation.getLatitude(), Utility.currentLocation.getLongitude(), 1);
            } catch (IOException ioException) {
                errorMessage = "Service Not Available";
                Log.e(TAG, errorMessage + " " + ioException.getMessage());
            } catch (IllegalArgumentException illegalArgumentException) {
                errorMessage = "Invalid Latitude or Longitude Used";
                Log.e(TAG, errorMessage + ". " +
                        "Latitude = " + Utility.currentLocation.getLatitude() + ", Longitude = " +
                        Utility.currentLocation.getLongitude() + " " + illegalArgumentException.getMessage());
            }

            if (addresses != null && addresses.size() > 0) {
                return addresses.get(0);
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    protected void onPostExecute(Address address) {
        if (postTaskListener != null)
            postTaskListener.onPostTask(address);
    }

    public interface PostTaskListener<K> {
        void onPostTask(K result);
    }
}


