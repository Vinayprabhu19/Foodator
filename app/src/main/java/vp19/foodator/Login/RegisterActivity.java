/**
 *  Name : RegisterActivity
 *  Type : Activity
 *  ContentView : activity_register
 *  Authentication : Signed Out users
 *  Purpose : To help users register new account
 */
package vp19.foodator.Login;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import vp19.foodator.R;
import vp19.foodator.utils.FirebaseMethods;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    //firebase authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseMethods firebaseMethods;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    //Widgets
    private EditText mEmail, mPassword, mUsername;
    private Button btnRegister;
    private ProgressBar mProgressBar;
    //Other variables
    private Context mContext;
    private String email, username, password;
    private String append;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        hide(getWindow());
        mContext=RegisterActivity.this;
        firebaseMethods = new FirebaseMethods(mContext);
        Log.d(TAG, "onCreate: Started...");
        initWidgets();
        setupFirebaseAuth();
        init();
    }

    /**
     * To perform various checks on the fields entered during the registration.
     * If the fields are vald then register the user on Firebase authentication
     */
    private void init() {
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = mEmail.getText().toString();
                username = mUsername.getText().toString();
                password = mPassword.getText().toString();
                if(checkInputsForNull(email,username,password)){
                    mProgressBar.setVisibility(View.VISIBLE);
                    firebaseMethods.registerNewEmail(email,password,username);
                }
            }
        });
        TextView link=findViewById(R.id.link_sigin);
        link.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext,LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK |Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    /**
     * Checks the fields if any of them is null
     * @param email
     * @param username
     * @param pwd
     * @return
     */
    private boolean checkInputsForNull(String email,String username, String pwd) {
        if(email.equals("") || username.equals("") || pwd.equals("")) {
            Toast.makeText(mContext, "All fields are mandatory!", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
    /**
     * Initialise all activity widgets
     */
    private void initWidgets() {
        mProgressBar =  findViewById(R.id.progressBar);
        mEmail =  findViewById(R.id.input_email);
        btnRegister =  findViewById(R.id.btn_register);
        mUsername =  findViewById(R.id.input_username);
        mPassword =  findViewById(R.id.input_password);
        mContext = RegisterActivity.this;
        mProgressBar.setVisibility(View.GONE);
    }
    /**
     * ---------------------------------------firebase stuff------------------------------------------
     */
    /**
     * setup firebase auth object
     */
    private void setupFirebaseAuth() {
        Log.d(TAG, "setupFirebaseAuth: setting firebase auth");
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    myRef.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            //Check if username is not already in use
                            String displayName=username;
                            if(firebaseMethods.checkIfUsernameExists(username,dataSnapshot)){
                                append=myRef.push().getKey().substring(3,10);
                                Log.d(TAG, "onDataChange: User Name already exists");
                                username = username + append;
                            }
                            //Add user to database
                            //Add user settings to database
                            firebaseMethods.addNewUser(email,username,"",displayName);
                            firebaseMethods.sendVerification();
                            Toast.makeText(mContext,"Signup Successfull. Verify Email",Toast.LENGTH_SHORT).show();
                            mAuth.signOut();
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {
                        }
                    });
                    finish();
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
}