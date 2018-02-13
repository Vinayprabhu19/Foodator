package vp19.foodator;

import android.*;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import vp19.foodator.Home.HomeActivity;
import vp19.foodator.Login.LoginActivity;
import vp19.foodator.utils.LocationServices;

import static java.security.AccessController.getContext;

public class SplashScreen extends AppCompatActivity {
    private static final String TAG = "SplashScreen";
    private Context mContext=SplashScreen.this;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        if(!runtime_permissions()) {
            enableStuffs();
        }
    }
    public static void hideBottomNavigation(Window window){
        View decorView = window.getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }

    private void enableStuffs() {
        hideBottomNavigation(getWindow());
        TextView tv=findViewById(R.id.title);
        Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/dudu.ttf");
        tv.setTypeface(font);
        setupFirebaseAuth();
        //starting location service
        Log.d(TAG, "onCreate: Starting location services");
        Intent intent=new Intent(getApplicationContext(), LocationServices.class);
        startService(intent);
    }

    private boolean runtime_permissions() {
        if(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){

            requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION},100);

            return true;
        }
        return false;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == 100){
            if( grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                enableStuffs();
            }else {
                runtime_permissions();
            }
        }
    }

    /**
     * Setting up Firebase Authentication
     */
    private void checkCurrentUser(final FirebaseUser user){
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent;
                if(user == null){
                    intent = new Intent(mContext, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                }
                else {
                    intent= new Intent(SplashScreen.this, HomeActivity.class);
                }
                startActivity(intent);
                finish();
            }
        }, 0);
    }
    private void setupFirebaseAuth(){
        mAuth = FirebaseAuth.getInstance();
        if(mAuth != null){
            FirebaseUser user=mAuth.getCurrentUser();
            checkCurrentUser(user);
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