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

import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import vp19.foodator.Models.UserLocation;
import vp19.foodator.R;

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

    //firebase authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseUser user;
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(TAG, "onCreate: Location Service started");
        setupFirebaseAuth();
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                mLatitude = location.getLatitude();
                mLongitude = location.getLongitude();
                if(user != null){
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            UserLocation location=new UserLocation();
                            location.setLat(mLatitude);
                            location.setLon(mLongitude);
                            location.setTimeStamp(getTime());
                            myRef.child(getString(R.string.dbname_user_locations))
                                    .child(user.getUid())
                                    .setValue(location);
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
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
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,TIME_TO_UPDATE,0,listener);

    }
    /**
     * Utility function to get the current time
     * @return Simple Date Format time
     */
    private String getTime(){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-DD'T'HH-mm-ss'Z'", Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        return sdf.format(new Date());
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        if(locationManager != null){
            //noinspection MissingPermission
            locationManager.removeUpdates(listener);
        }
    }
    /**
     * Setting up Firebase Authentication
     */
    private void setupFirebaseAuth() throws  NullPointerException{
        FirebaseApp.initializeApp(getApplicationContext());
        mFirebaseDatabase= FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        myRef=mFirebaseDatabase.getReference();
        if(mAuth !=null)
            user=mAuth.getCurrentUser();
    }
}
