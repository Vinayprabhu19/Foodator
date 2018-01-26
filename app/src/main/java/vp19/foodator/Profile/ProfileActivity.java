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
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupActivityWidgets();
        initImageLoader();
        setupFirebaseAuth();
        setupBottomNavigationView();
        setupToolbar();
        tempGridSetup();
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
    private void tempGridSetup() {
        ArrayList<String> imgURLs = new ArrayList<>();
        imgURLs.add("https://www.ndtv.com/cooks/images/kuttu.dosa.jpg");
        imgURLs.add("https://expressgiftservices.files.wordpress.com/2015/01/35_jalebi.png");
        imgURLs.add("https://thefoodfairy.files.wordpress.com/2011/08/dsc_8376.jpg");
        imgURLs.add("https://i1.wp.com/files.hungryforever.com/wp-content/uploads/2017/01/02171720/paper-dosa-recipe.jpg?w=1269&strip=all&quality=80");
        //imgURLs.add("http://foodofinterest.com/wp-content/uploads/2017/01/DSC_0037_00037-1.jpg");
        //imgURLs.add("https://i.ytimg.com/vi/-1sT6hy_wg4/maxresdefault.jpg");
        imgURLs.add("http://www.nithyas-kitchen.com/wp-content/uploads/2016/07/idli-dosa-batter-using-mixie.1024x1024.jpg");
        imgURLs.add("https://i2.wp.com/files.hungryforever.com/wp-content/uploads/2015/04/Featured-image-masala-dosa-recipe-720x378.jpg?resize=720%2C378");
        imgURLs.add("http://cdn1.foodviva.com/static-content/food-images/desserts-sweets-recipes/rasgulla/rasgulla.jpg");
        imgURLs.add("http://files.hungryforever.com/wp-content/uploads/2016/01/11225703/Featured-image-pongal-delhi.jpg");
        imgURLs.add("https://i.ytimg.com/vi/SZ-uZt4zjs8/maxresdefault.jpg");
        imgURLs.add("http://images.media-allrecipes.com/userphotos/250x250/708879.jpg");
        imgURLs.add("http://www.bikanervala.ae/images/south-indian/header1.jpg");
        imgURLs.add("https://www.ndtv.com/cooks/images/mysore.masala.dosa.1%20%281%29.jpg");
        imgURLs.add("http://maayeka.com/wp-content/uploads/2012/03/Gulab-jamunrasgulla-maayeka.jpg");
        imgURLs.add("http://www.vegrecipesofindia.com/wp-content/uploads/2016/05/dosa-recipe-5.jpg");
        imgURLs.add("http://www.diettaste.com/images/side-dishes/sweet-potato-flatbread-roti3-w.jpg");
        imgURLs.add("http://www.bollywoodsweetbazaar.com.au/wp-content/uploads/2017/06/indian-snacks.jpg");
        imgURLs.add("http://www.manjulaskitchen.com/blog/wp-content/uploads/aloo_puri.jpg");
        imgURLs.add("https://inhabitat.com/wp-content/blogs.dir/1/files/2016/03/Leftovers-for-Hungry-Indian-Food.jpg");
        imgURLs.add("http://www.vegrecipesofindia.com/wp-content/uploads/2012/09/dry-aloo-matar-recipe-04.jpg");
        imgURLs.add("http://onedaycart.com/odcb/wp-content/uploads/2015/01/Rava-Idli.jpg");
        imgURLs.add("https://i0.wp.com/files.hungryforever.com/wp-content/uploads/2017/06/20131010/easy-rasgulla-recipes-600x451.jpg?resize=600%2C451");
        imgURLs.add("https://www.ndtv.com/cooks/images/idli.2.jpg");
        imgURLs.add("https://www.ndtv.com/cooks/images/Radha%20Ballavi%20%28Stuffed%20Puri%29.jpg");

        setupImageGrid(imgURLs);
    }

    /**
     * Setup the image grid with Grid image adapter
     * @param imgURLs : List of image urls
     */
    private void setupImageGrid(ArrayList<String> imgURLs) {
        gridView = (GridView) findViewById(R.id.gridView);

        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth/NUM_GRID_COLUMNS;
        gridView.setColumnWidth(imageWidth);

        GridImageAdapter adpater = new GridImageAdapter(mContext, R.layout.layout_grid_imageview, "", imgURLs);
        gridView.setAdapter(adpater);

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
