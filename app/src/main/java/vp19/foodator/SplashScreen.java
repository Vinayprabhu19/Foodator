package vp19.foodator;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import vp19.foodator.Home.HomeActivity;
import vp19.foodator.Login.LoginActivity;

import static java.security.AccessController.getContext;

public class SplashScreen extends AppCompatActivity {
    private Context mContext=SplashScreen.this;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        hideBottomNavigation(getWindow());
        TextView tv=findViewById(R.id.title);
        Typeface font = Typeface.createFromAsset(mContext.getAssets(), "fonts/dudu.ttf");
        tv.setTypeface(font);
        setupFirebaseAuth();
    }
    public static void hideBottomNavigation(Window window){
        View decorView = window.getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
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
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                checkCurrentUser(user);
            }
        };
    }
    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
}
