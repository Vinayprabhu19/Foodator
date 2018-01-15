package vp19.foodator.utils;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import vp19.foodator.CameraActvity;
import vp19.foodator.MainActivity;
import vp19.foodator.ProfileActivity;
import vp19.foodator.R;

/**
 * Created by Vinay Prabhu on 15-Jan-18.
 */

public class BottomNavigationViewHelper {
    private static final String TAG = "BottomNavigationViewHel";
    public static void setupBottomNavigationView(BottomNavigationViewEx bottomNavigationViewEx, Window window){
        bottomNavigationViewEx.enableAnimation(false);
        bottomNavigationViewEx.enableItemShiftingMode(false);
        bottomNavigationViewEx.enableShiftingMode(false);
        bottomNavigationViewEx.setTextVisibility(false);
        bottomNavigationViewEx.setIconVisibility(true);
        bottomNavigationViewEx.setIconSize(30,30);
        hide(window);
    }
    public static void enableNavgation(final Context context, BottomNavigationViewEx view)
    {
        view.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId())
                {
                    case R.id.ic_home:
                        Log.d(TAG, "onNavigationItemSelected: Home");
                        Intent intent1=new Intent(context, MainActivity.class); // NUM = 0
                        intent1.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        context.startActivity(intent1);
                        break;
                    case R.id.ic_camera:
                        Log.d(TAG, "onNavigationItemSelected: Camera");
                        Intent intent2=new Intent(context, CameraActvity.class); // NUM = 1
                        intent2.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        context.startActivity(intent2);
                        break;
                    case R.id.ic_profile:
                        Log.d(TAG, "onNavigationItemSelected: Profile");
                        Intent intent3=new Intent(context, ProfileActivity.class); // NUM = 2
                        intent3.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        context.startActivity(intent3);
                }
                return false;
            }
        });
    }

    public static void hide(Window window){
        View decorView = window.getDecorView();
// Hide both the navigation bar and the status bar.
// SYSTEM_UI_FLAG_FULLSCREEN is only available on Android 4.1 and higher, but as
// a general rule, you should design your app to hide the status bar whenever you
// hide the navigation bar.
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
    }
}
