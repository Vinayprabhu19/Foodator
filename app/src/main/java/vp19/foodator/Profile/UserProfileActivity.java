/**
 *  Name : ProfileActivity
 *  Type : Activity
 *  ContentView : activity_profile
 *  Authentication : Signed In users
 *  Purpose : To display the profile of the user
 */
package vp19.foodator.Profile;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;
import java.util.Collections;

import vp19.foodator.Login.LoginActivity;
import vp19.foodator.Models.Notification;
import vp19.foodator.Models.Photo;
import vp19.foodator.Models.User;
import vp19.foodator.Models.UserAccountSettings;
import vp19.foodator.R;
import vp19.foodator.utils.BottomNavigationViewHelper;
import vp19.foodator.utils.FirebaseMethods;
import vp19.foodator.utils.GridImageAdapter;
import vp19.foodator.utils.UniversalImageLoader;

public class UserProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    //constants
    private Context mContext=UserProfileActivity.this;
    private int ACTIVITY_NUM=0;
    private int NUM_GRID_COLUMNS=3;
    private String userId;
    private String currentUserID;
    //Widgets
    private ProgressBar mProgressbar;
    private ImageView mProfilePhoto;
    private TextView mPosts,mFollowers,mFollowing,mDisplayName,mProfileName;
    private GridView gridView;
    private ImageView backArrow;
    ToggleButton followStatus;
    //firebase authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseUser user;
    private FirebaseMethods firebaseMethods;
    ArrayList<String> photo_id;
    //Model
    Photo photo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        hide(getWindow());
        setupFirebaseAuth();
        setFollowStatus();
        setupActivityWidgets();
        initImageLoader();
        UrlGridSetup();
    }
    private void setFollowStatus(){
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                DataSnapshot ds=dataSnapshot.child(getString(R.string.dbname_following)).child(currentUserID);
                if(ds.hasChild(userId)){
                    followStatus.setChecked(true);
                    followStatus.setBackgroundDrawable((getDrawable(R.drawable.round_background)));
                    followStatus.setTextColor(getColor(R.color.dark_violet));
                }
                else {
                    followStatus.setChecked(false);
                    followStatus.setBackgroundDrawable((getDrawable(R.drawable.round_background_color)));
                    followStatus.setTextColor(getColor(R.color.white));
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void UrlGridSetup() {
        photo_id=new ArrayList<>();
        Query query=myRef.child(getString(R.string.dbname_user_photos))
                .child(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<String> imgList=new ArrayList<>();
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    photo=ds.getValue(Photo.class);
                    String url=photo.getImage_path();
                    String id=photo.getPhoto_id();
                    imgList.add(url);
                    photo_id.add(id);
                }
                Collections.reverse(imgList);
                setupImageGrid(imgList);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * Setup the image grid with Grid image adapter
     * @param imgURLs : List of image urls
     */
    private void setupImageGrid(final ArrayList<String> imgURLs) {
        Log.d(TAG, "setupImageGrid: Setting up image grid");
        gridView = findViewById(R.id.gridView);

        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth/NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);
        GridImageAdapter adapter = new GridImageAdapter(mContext, R.layout.layout_grid_imageview, "", imgURLs);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(mContext,ShowImageActivity.class);
                intent.putExtra(getString(R.string.selected_image),imgURLs.get(position));
                intent.putExtra(getString(R.string.photo_id),photo_id.get(position));
                intent.putExtra(getString(R.string.attr_user_id),photo.getUser_id());
                startActivity(intent);
            }
        });
    }
    private void initImageLoader(){
        UniversalImageLoader imageLoader=new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }
    private void setupActivityWidgets() {
        mProgressbar =  findViewById(R.id.profileProgressBar);
        mProgressbar.setVisibility(View.GONE);
        mProfilePhoto = findViewById(R.id.profileImage);
        backArrow=findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        followStatus=findViewById(R.id.textFollowStatus);
        followStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "follow click");
                boolean isChecked=followStatus.isChecked();
                if(isChecked){
                    Log.d(TAG, "followed");
                    followStatus.setBackgroundDrawable((getDrawable(R.drawable.round_background)));
                    followStatus.setTextColor(getColor(R.color.dark_violet));
                    followUser();
                }
                else{
                    Log.d(TAG, "unfollow ");
                    followStatus.setBackgroundDrawable((getDrawable(R.drawable.round_background_color)));
                    followStatus.setTextColor(getColor(R.color.white));
                    unfollowUser();
                }
            }
        });
    }
    private void followUser(){
        Log.d(TAG, "followUser: ");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user= dataSnapshot.child(getString(R.string.dbname_users)).child(userId).getValue(User.class); // User to be followed
                User c_user=dataSnapshot.child(getString(R.string.dbname_users)).child(currentUserID).getValue(User.class); //Current User
                myRef.child(getString(R.string.dbname_following))
                        .child(currentUserID)
                        .child(userId)
                        .setValue(user);
                myRef.child(getString(R.string.dbname_followers))
                        .child(userId)
                        .child(currentUserID)
                        .setValue(c_user);
                UserAccountSettings userSettings=dataSnapshot.child(getString(R.string.dbname_user_account_settings)).child(userId).getValue(UserAccountSettings.class);
                UserAccountSettings currentUserSettings=dataSnapshot.child(getString(R.string.dbname_user_account_settings)).child(currentUserID).getValue(UserAccountSettings.class);
                userSettings.setFollowers(userSettings.getFollowers() + 1);
                currentUserSettings.setFollowing(currentUserSettings.getFollowing()+1);
                myRef.child(getString(R.string.dbname_user_account_settings)).child(userId).setValue(userSettings);
                myRef.child(getString(R.string.dbname_user_account_settings)).child(currentUserID).setValue(currentUserSettings);
                setupLayoutWidgets(userSettings);
                addNotification(currentUserSettings.getUsername());
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void unfollowUser(){
        Log.d(TAG, "unfollowUser: ");
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myRef.child(getString(R.string.dbname_following))
                        .child(currentUserID)
                        .child(userId)
                        .removeValue();
                myRef.child(getString(R.string.dbname_followers))
                        .child(userId)
                        .child(currentUserID)
                        .removeValue();
                UserAccountSettings userSettings=dataSnapshot.child(getString(R.string.dbname_user_account_settings)).child(userId).getValue(UserAccountSettings.class);
                UserAccountSettings currentUserSettings=dataSnapshot.child(getString(R.string.dbname_user_account_settings)).child(currentUserID).getValue(UserAccountSettings.class);
                userSettings.setFollowers(userSettings.getFollowers() - 1);
                currentUserSettings.setFollowing(currentUserSettings.getFollowing() - 1);
                myRef.child(getString(R.string.dbname_user_account_settings)).child(userId).setValue(userSettings);
                myRef.child(getString(R.string.dbname_user_account_settings)).child(currentUserID).setValue(currentUserSettings);
                setupLayoutWidgets(userSettings);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void addNotification(String username){
        Notification notification=new Notification();
        String message=username + " has followed you. ";
        notification.setMessage(message);
        myRef.child(getString(R.string.dbname_notifications)).child(userId).child(currentUserID).setValue(notification);
    }
    /**
     * Setup profile picture url to the database user_account_settings
     * @param settings : User account settings of the queried user
     */
    private void setProfileImage(UserAccountSettings settings) {
        String imgURL=settings.getProfile_photo();
        UniversalImageLoader.setImage(imgURL, mProfilePhoto, null, "");
    }
    /**
     * Setting up Firebase Authentication
     */
    private void setupFirebaseAuth(){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();
        firebaseMethods=new FirebaseMethods(mContext);
        Intent intent=getIntent();
        userId=intent.getStringExtra(getString(R.string.calling_activity));
        currentUserID=mAuth.getCurrentUser().getUid();
        Query query=myRef.child(getString(R.string.dbname_user_account_settings))
                .child(userId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserAccountSettings settings=dataSnapshot.getValue(UserAccountSettings.class);
                setupLayoutWidgets(settings);
                setProfileImage(settings);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
    }
    @Override
    public void onStop() {
        super.onStop();
    }

    /**
     *  Setup the layout widgets for the profile from the database
     * @param settings : The settings model containing details of the current user
     */
    void setupLayoutWidgets(UserAccountSettings settings){
        mDisplayName=findViewById(R.id.display_name);
        mProfileName=findViewById(R.id.profileName);
        mPosts=findViewById(R.id.tvPosts);
        mFollowers=findViewById(R.id.tvFollowers);
        mFollowing=findViewById(R.id.tvFollowing);
        mDisplayName.setText(settings.getDisplay_name());
        mProfileName.setText(settings.getUsername());
        mPosts.setText(Long.toString(settings.getPosts()));
        mFollowers.setText(Long.toString(settings.getFollowers()));
        mFollowing.setText(Long.toString(settings.getFollowing()));
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
