package vp19.foodator.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Raghuveer on 13-02-2018.
 */

public class LocationServices extends Service {
    private static final String TAG = "LocationServices";

    private LocationListener listener;
    private LocationManager locationManager;

    //global variables
    private double mLongitude,mLatitude;
    private static final long TIME_TO_UPDATE = 3000;//in millisecond
    private static final float MIN_DIST = 0;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: Location Service started");

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
//                Intent i = new Intent("location_update");
//                i.putExtra("coordinates",location.getLongitude()+" "+location.getLatitude());
//                sendBroadcast(i);
                mLatitude = location.getLatitude();
                mLongitude = location.getLongitude();
                Log.d(TAG, "onLocationChanged: Longitude:"+mLatitude+"  Latitude:"+mLongitude);
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {
                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
            }
        };

        locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

        //noinspection MissingPermission
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,TIME_TO_UPDATE,MIN_DIST,listener);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            //noinspection MissingPermission
            locationManager.removeUpdates(listener);
        }
    }
}
