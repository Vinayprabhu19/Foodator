package vp19.foodator.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import vp19.foodator.Models.UserAccountSettings;
import vp19.foodator.R;
import vp19.foodator.utils.FirebaseMethods;
import vp19.foodator.utils.UniversalImageLoader;

public class ShowImageActivity extends AppCompatActivity {
    private static final String TAG = "ShowImageActivity";
    private Context mContext=ShowImageActivity.this;
    private String imageURL;
    private String PhotoId;
    //Widgets
    private ImageView btn_back;
    private ImageView imagePost;
    private ImageView profileImage;
    private TextView displayName;
    //firebase authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseUser user;
    private FirebaseMethods firebaseMethods;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_image);
        init();
        setupFirebaseAuth();
    }
    private void init(){
        Intent intent=getIntent();
        imageURL=intent.getStringExtra(getString(R.string.selected_image));
        PhotoId=intent.getStringExtra(getString(R.string.photo_id));
        imagePost=findViewById(R.id.imagePost);
        displayName=findViewById(R.id.display_name);
        profileImage=findViewById(R.id.profileImage);
        UniversalImageLoader.setImage(imageURL, imagePost, null, "");
        int width=getResources().getDisplayMetrics().widthPixels;
        int ivwidth = imagePost.getDrawable().getIntrinsicWidth();
        int ivheight = imagePost.getDrawable().getIntrinsicHeight();
        if(ivheight  > ivwidth){
            imagePost.getLayoutParams().width=width;
            imagePost.getLayoutParams().height=width;
            imagePost.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        }
        Log.d(TAG, "init: ImageDim" + ivwidth);
        Log.d(TAG, "init: ImageDim"+ivheight);
        initImageLoader();
        btn_back=findViewById(R.id.backArrow);
        btn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void initImageLoader(){
        UniversalImageLoader imageLoader=new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }
    private void setParams(DataSnapshot dataSnapshot){
        UserAccountSettings settings;
        settings=dataSnapshot.child(getString(R.string.dbname_user_account_settings))
                .child(user.getUid())
                .getValue(UserAccountSettings.class);
        UniversalImageLoader.setImage(settings.getProfile_photo(), profileImage, null, "");
        displayName.setText(settings.getDisplay_name());
    }
    /**
     * Setting up Firebase Authentication
     */
    private void setupFirebaseAuth(){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase= FirebaseDatabase.getInstance();
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
                    setParams(dataSnapshot);
                }
                catch (NullPointerException e){

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

}
