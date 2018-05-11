package vp19.foodator.Home;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import vp19.foodator.Food.ViewRestaurantActivity;
import vp19.foodator.Models.Photo;
import vp19.foodator.Models.Restaurant;
import vp19.foodator.Models.User;
import vp19.foodator.Models.UserLocation;
import vp19.foodator.R;

/**
 * Created by Raghuveer on 10-02-2018.
 */

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "MapFragment";
    private static final float DEFAULT_ZOOM = 15;
    public static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    public static final String COARSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1234;

    private boolean mLocationPermissionGranted = false;
    private GoogleMap mMap;
    private FusedLocationProviderClient mFusedLocationProviderClient;

    //firebase authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseUser user;

    private double curLat,curLon;
    private Bitmap hotelMarker;
    private Bitmap currentMarker;
    private Bitmap otherMarker;

    private ArrayList<UserLocation> locations;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_search_map, container, false);
        getLocationPermission();
        return v;
    }

    private void init() {
        int height = 100;
        int width = 100;
        BitmapDrawable hotelDrawable=(BitmapDrawable)getResources().getDrawable(R.drawable.hotel);
        BitmapDrawable currentDrawable=(BitmapDrawable)getResources().getDrawable(R.drawable.current_marker);
        BitmapDrawable otherDrawable=(BitmapDrawable)getResources().getDrawable(R.drawable.other_marker);
        Bitmap h=hotelDrawable.getBitmap();
        Bitmap c=currentDrawable.getBitmap();
        Bitmap o=otherDrawable.getBitmap();
        hotelMarker = Bitmap.createScaledBitmap(h, width, height, false);
        currentMarker=Bitmap.createScaledBitmap(c,width,height,false);
        otherMarker=Bitmap.createScaledBitmap(o,width,height,false);
        Log.d(TAG, "init: initializing map fragment");
        locations=new ArrayList<>();
        try {
            SupportMapFragment supportMapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            supportMapFragment.getMapAsync(this);
            setupFirebaseAuth();
            queryLocations();
        }
        catch (NullPointerException e){
            Log.d(TAG, "init: "+e.getMessage());
        }
    }
    private void queryLocations() throws NullPointerException{
        Query query=myRef.child(getString(R.string.dbname_user_locations));
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                locations.clear();
                Log.d(TAG, "plotMap  Next Iteration\n");
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    UserLocation location=ds.getValue(UserLocation.class);
                    locations.add(location);
                }
                plotMap();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Query query2=myRef.child(getString(R.string.dbname_user_locations)).child(user.getUid());
        query2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserLocation location=dataSnapshot.getValue(UserLocation.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void plotMap() throws NullPointerException{
        Log.d(TAG, "plotMap: plotting locations");
        String activityName;
        try {
            activityName=getActivity().getClass().getSimpleName();
        }
        catch (NullPointerException e){
            Log.d(TAG, "plotMap: "+e.getMessage());
            return;
        }
        Log.d(TAG, "plotMap: "+activityName);
        activityName=activityName.replaceAll(" ","");
        if(activityName.equals(getString(R.string.viewRestaurant_activity))){
            Log.d(TAG, "plotMap: true");
            ViewRestaurantActivity viewRestaurantActivity = (ViewRestaurantActivity) getActivity();
            viewRestaurantActivity.initWidgets();
            Restaurant restaurant = viewRestaurantActivity.getRestaurant();
            //  .icon function
            mMap.addMarker(new MarkerOptions().position(new LatLng(restaurant.getLat(),restaurant.getLon()))
                    .title(restaurant.getName()).icon(BitmapDescriptorFactory.fromBitmap(hotelMarker))).showInfoWindow();

            mMap.addMarker(new MarkerOptions().position(new LatLng(curLat,curLon)).title("User").icon(BitmapDescriptorFactory.fromBitmap(currentMarker)));
        }
        else if(activityName.equals(getString(R.string.home_activity))) {
            Log.d(TAG, "plotMap: homeactivity");
            for(int i=0;i<locations.size();i++) {
                if(locations.get(i).getLat()==curLat && locations.get(i).getLon()==curLon)
                    continue;
                double latitude=locations.get(i).getLat(),longitude=locations.get(i).getLon();
                mMap.addMarker(new MarkerOptions().position(new LatLng(latitude,longitude)).title("User").icon(BitmapDescriptorFactory.fromBitmap(otherMarker)));
            }
            mMap.addMarker(new MarkerOptions().position(new LatLng(curLat,curLon)).title("User").icon(BitmapDescriptorFactory.fromBitmap(currentMarker)));
        }
    }
    @Override
    public void onMapReady(GoogleMap googleMap) throws NullPointerException {
        Log.d(TAG, "onMapReady: Map is ready");
        mMap = googleMap;
        Log.d(TAG, "onMapReady: getting device location");
        if(mLocationPermissionGranted) {
            getDeviceLocation();
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mMap.setMyLocationEnabled(true);
        }
    }

    private void getLocationPermission() throws NullPointerException {
        Log.d(TAG, "getLocationPermission: getting location permission");
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};

        if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                FINE_LOCATION)  == PackageManager.PERMISSION_GRANTED) {
            if(ContextCompat.checkSelfPermission(getActivity().getApplicationContext(),
                    COARSE_LOCATION)  == PackageManager.PERMISSION_GRANTED){
                mLocationPermissionGranted = true;
                init();
            }else {
                ActivityCompat.requestPermissions(getActivity(),
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE);
            }
        }else {
            ActivityCompat.requestPermissions(getActivity(),
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private void getDeviceLocation() throws NullPointerException{
        Log.d(TAG, "getDeviceLocation: getting current device location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getActivity());

        try {
            if(mLocationPermissionGranted){
                Task location = mFusedLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if(task.isSuccessful()){
                            Log.d(TAG, "onComplete: found location");
                            Location currentLocation = (Location) task.getResult();
                            curLat=currentLocation.getLatitude();
                            curLon=currentLocation.getLongitude();
                            moveCamera(new LatLng(currentLocation.getLatitude(),currentLocation.getLongitude()), DEFAULT_ZOOM);
                        }else {
                            Log.d(TAG, "onComplete: current location is null");
                            Toast.makeText(getActivity(), "unable to get current location",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }catch (SecurityException e){
            Log.d(TAG, "getDeviceLocation: Security Exception"+e.getMessage());
        }
    }

    private void moveCamera(LatLng latLng, float zoom){
        Log.d(TAG, "moveCamera: moving Camera to:-  lat: "+latLng.latitude+", lng: "+latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng,zoom));
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mLocationPermissionGranted = false;

        switch (requestCode){
            case LOCATION_PERMISSION_REQUEST_CODE:{
                if(grantResults.length>0){
                    for(int i=0;i<grantResults.length;i++){
                        if(grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            mLocationPermissionGranted = false;
                            return;
                        }
                    }
                    mLocationPermissionGranted =true;
                    //initialize our map
                    init();
                }
            }
        }
    }
    private void setupFirebaseAuth(){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase= FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();
        if(mAuth != null){
            FirebaseUser user=mAuth.getCurrentUser();
        }
    }
    @Override
    public void onStart() {
        super.onStart();
    }
    @Override
    public void onStop() {
        super.onStop();
    }
}
