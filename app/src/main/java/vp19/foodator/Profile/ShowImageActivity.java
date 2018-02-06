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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

import vp19.foodator.Models.Photo;
import vp19.foodator.Models.UserAccountSettings;
import vp19.foodator.R;
import vp19.foodator.utils.FirebaseMethods;
import vp19.foodator.utils.StringManipulation;
import vp19.foodator.utils.UniversalImageLoader;

public class ShowImageActivity extends AppCompatActivity {
    private static final String TAG = "ShowImageActivity";
    private Context mContext=ShowImageActivity.this;
    private String imageURL;
    private String PhotoId;
    private String userId;
    //Widgets
    private ImageView btn_back;
    private ImageView imagePost;
    private ImageView profileImage;
    private TextView displayName;
    private ProgressBar progressBar;
    private TextView description;
    //firebase authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods firebaseMethods;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0,0);
        setContentView(R.layout.activity_show_image);
        init();
        setupFirebaseAuth();
    }
    private void init(){
        //Get Extras from intent
        Intent intent=getIntent();
        imageURL=intent.getStringExtra(getString(R.string.selected_image));
        PhotoId=intent.getStringExtra(getString(R.string.photo_id));
        userId=intent.getStringExtra(getString(R.string.attr_user_id));
        //Get the widgets
        imagePost=findViewById(R.id.imagePost);
        displayName=findViewById(R.id.display_name);
        profileImage=findViewById(R.id.profileImage);
        progressBar=findViewById(R.id.progressBar);
        progressBar.setVisibility(View.GONE);
        description=findViewById(R.id.description);
        //Set image for the post
        UniversalImageLoader.setImage(imageURL, imagePost, null, "");
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
    private void setParams(UserAccountSettings settings){
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
        Query query1=myRef.child(getString(R.string.dbname_user_account_settings))
                .child(userId);
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserAccountSettings settings=dataSnapshot.getValue(UserAccountSettings.class);
                setParams(settings);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        Query query2=myRef.child(getString(R.string.dbname_photos))
                .child(PhotoId);
        query2.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Photo photo=dataSnapshot.getValue(Photo.class);
                String caption=photo.getCaption();
                if(!StringManipulation.isStringNull(caption))
                    description.setText(caption);
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
}
