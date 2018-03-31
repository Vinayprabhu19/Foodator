/**
 *  Name : LoginActivity
 *  Type : Activity
 *  ContentView : activity_login
 *  Authentication : Signed Out users
 *  Purpose : To help users login to their account
 */
package vp19.foodator.Login;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import de.hdodenhof.circleimageview.CircleImageView;
import vp19.foodator.Home.HomeActivity;
import vp19.foodator.R;
import vp19.foodator.utils.LocationServices;
import vp19.foodator.utils.StringManipulation;

public class LoginActivity extends AppCompatActivity{
    //Constants
    private static final String TAG = "LoginActvity";
    private Context mContext=LoginActivity.this;
    private Typeface font;
    //Widgets
    private ProgressBar mProgressBar;
    private EditText mPassword,mEmail;
    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        hide(getWindow());
        mProgressBar =findViewById(R.id.progressBar);
        mProgressBar.setVisibility(View.GONE);
        mEmail=findViewById(R.id.input_email);
        mPassword=findViewById(R.id.input_password);
        setupFirebaseAuth();
        init();
    }
    /**
     * ---------------------------------------firebase stuff------------------------------------------
     */

    private void init() {
        handleImages();
        TextView title=findViewById(R.id.title);
        font = Typeface.createFromAsset(mContext.getAssets(), "fonts/neuro.ttf");
        title.setTypeface(font);
        Button btnLogin = (Button) findViewById(R.id.btn_login);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Attempting to login");
                String email = mEmail.getText().toString();
                String password = mPassword.getText().toString();
                if(StringManipulation.isStringNull(email) && StringManipulation.isStringNull(password)) {
                    Toast.makeText(mContext, "Please type your email and password", Toast.LENGTH_SHORT).show();
                }else {
                    mProgressBar.setVisibility(View.VISIBLE);
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this,new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    Log.d(TAG, "signInWithEmail:onComplete:" + task.isSuccessful());
                                    FirebaseUser user=mAuth.getCurrentUser();
                                    Boolean check=false;
                                    try {
                                        if(user.isEmailVerified()){
                                            Log.d(TAG, "onComplete: Email Verified");
                                            check=true;
                                        }
                                    }
                                    catch (NullPointerException e){
                                        Log.d(TAG, "onComplete: Null pointer exception"+e.getMessage());
                                    }
                                    // If sign in fails, display a message to the user. If sign in succeeds
                                    // the auth state listener will be notified and logic to handle the
                                    // signed in user can be handled in the listener.
                                    if (!task.isSuccessful()) {
                                        Log.w(TAG, "signInWithEmail:failed", task.getException());
                                        Toast.makeText(mContext, R.string.auth_failed,
                                                Toast.LENGTH_SHORT).show();
                                        mProgressBar.setVisibility(View.GONE);
                                        CircleImageView logo=findViewById(R.id.logo);
                                        logo.setImageDrawable(getDrawable(R.drawable.teddy_spill));
                                    }
                                    /*else if(!check){
                                        Toast.makeText(mContext, "Email Not Verfied",
                                                Toast.LENGTH_SHORT).show();
                                    }*/
                                    else {
                                        Log.d(TAG, "onComplete: successful login...");
                                        Toast.makeText(mContext, R.string.auth_success,
                                                Toast.LENGTH_SHORT).show();
                                        mProgressBar.setVisibility(View.GONE);
                                        startService(new Intent(getApplicationContext(), LocationServices.class));
                                        Intent intent=new Intent(mContext,HomeActivity.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
                                        startActivity(intent);
                                        finish();
                                    }
                                }
                            });
                }

            }
        });

        TextView linkSignUp = findViewById(R.id.link_signup);
        linkSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext,RegisterActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }
    /**
     * setup firebase auth object
     */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                } else {
                    // User is signed out
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
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
    @Override
    protected void onPostResume() {
        super.onPostResume();
    }
    public static void hide(Window window){
        final View decorView = window.getDecorView();
        final int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        // Note that system bars will only be "visible" if none of the
                        // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            decorView.setSystemUiVisibility(uiOptions);
                            // adjustments to your UI, such as showing the action bar or
                            // other navigational controls.
                        } else {
                            // adjustments to your UI, such as hiding the action bar or
                            // other navigational controls.
                        }
                    }
                });
    }
    private void handleImages(){
        Log.d(TAG, "handleImages: ");
        final CircleImageView logo=findViewById(R.id.logo);
        EditText email=findViewById(R.id.input_email);
        EditText password=findViewById(R.id.input_password);
        email.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                logo.setImageDrawable(getDrawable(R.drawable.teddy_looking_down));
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                logo.setImageDrawable(getDrawable(R.drawable.teddy_looking_straight));
                Log.d(TAG, "handleImages: focused");
            }

            @Override
            public void afterTextChanged(Editable s) {
                logo.setImageDrawable(getDrawable(R.drawable.teddy_looking_straight));
            }
        });
        password.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                logo.setImageDrawable(getDrawable(R.drawable.teddy_looking_down));
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                logo.setImageDrawable(getDrawable(R.drawable.teddy_hide_face));
                Log.d(TAG, "handleImages: focused");
            }

            @Override
            public void afterTextChanged(Editable s) {
                logo.setImageDrawable(getDrawable(R.drawable.teddy_hide_face));
            }
        });
        password.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(!hasFocus)
                    logo.setImageDrawable(getDrawable(R.drawable.teddy_looking_down));
            }
        });
    }
}
