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
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import vp19.foodator.Login.LoginActivity;
import vp19.foodator.Models.Photo;
import vp19.foodator.Models.UserAccountSettings;
import vp19.foodator.R;
import vp19.foodator.utils.BottomNavigationViewHelper;
import vp19.foodator.utils.FirebaseMethods;
import vp19.foodator.utils.GridImageAdapter;
import vp19.foodator.utils.UniversalImageLoader;

public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";
    //constants
    private Context mContext=ProfileActivity.this;
    private int ACTIVITY_NUM=2;
    private int NUM_GRID_COLUMNS=3;
    //Widgets
    private ProgressBar mProgressbar;
    private ImageView mProfilePhoto;
    private TextView mPosts,mFollowers,mFollowing,mDisplayName,mProfileName;
    private GridView gridView;
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
        setContentView(R.layout.activity_profile);
        setupBottomNavigationView();
        setupActivityWidgets();
        initImageLoader();
        setupFirebaseAuth();
        setupToolbar();
        TextView editProfile=findViewById(R.id.textEditProfile);
        editProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext,AccountSettingsActivity.class);
                intent.putExtra(getString(R.string.calling_activity),getString(R.string.profile_activity));
                startActivity(intent);
            }
        });
    }
    private void UrlGridSetup(DataSnapshot snapshot) {
        ArrayList<String> imgList=new ArrayList<>();
        photo_id=new ArrayList<>();
        snapshot=snapshot.child(getString(R.string.dbname_user_photos)).child(user.getUid());
        for(DataSnapshot ds:snapshot.getChildren()){
            photo=ds.getValue(Photo.class);
            String url=photo.getImage_path();
            String id=photo.getPhoto_id();
            imgList.add(url);
            photo_id.add(id);
        }
        Log.d(TAG, "UrlGridSetup: Finisehd loop");
        setupImageGrid(imgList);
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
    }

    /**
     * Setup top profile toolbar and the widgets in it
     */
    private void setupToolbar(){
        Toolbar toolbar=findViewById(R.id.profileToolBar);
        setSupportActionBar(toolbar);
        ImageView profileMenu=findViewById(R.id.profileMenu);
        profileMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(mContext,AccountSettingsActivity.class);
                startActivity(intent);
            }
        });
    }
    private void setupBottomNavigationView(){
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx)findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx,this.getWindow());
        BottomNavigationViewHelper.enableNavgation(mContext,bottomNavigationViewEx);
        Menu menu=bottomNavigationViewEx.getMenu();
        MenuItem menuItem=menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
        BottomNavigationViewHelper.setIcon(menuItem,ACTIVITY_NUM);
    }
    /**
     * Setup profile picture url to the database user_account_settings
     * @param dataSnapshot : Current snapshot of the database
     */
    private void setProfileImage(DataSnapshot dataSnapshot) {
        UserAccountSettings settings = new UserAccountSettings();
        settings=dataSnapshot.child(getString(R.string.dbname_user_account_settings))
                .child(user.getUid())
                .getValue(UserAccountSettings.class);
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
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
            }
        };
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    user=mAuth.getCurrentUser();
                    //Retrieve User Information
                    UserAccountSettings settings = firebaseMethods.getUserAccountSettings(dataSnapshot);
                    setupLayoutWidgets(settings);
                    setProfileImage(dataSnapshot);
                    UrlGridSetup(dataSnapshot);
                    //Retrieve images for the grid
                }
                catch (NullPointerException e){
                    Log.d(TAG, "onDataChange: Null pointer Exception "+e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
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
        setupBottomNavigationView();
    }
}
