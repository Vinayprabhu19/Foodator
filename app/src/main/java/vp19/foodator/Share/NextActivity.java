/**
 *  Name : NextActivity
 *  Type : Activity
 *  ContentView : activity_next
 *  Authentication : Signed In users
 *  Purpose : Final step to share the image with description
 */
package vp19.foodator.Share;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import vp19.foodator.Home.HomeActivity;
import vp19.foodator.Models.UserAccountSettings;
import vp19.foodator.R;
import vp19.foodator.utils.FirebaseMethods;
public class NextActivity extends AppCompatActivity{
    private static final String TAG = "NextActivity";
    private String SelectedImage;
    private boolean mFitStatus;
    private Bitmap bitmap;
    //Comstants
    private static String append="file:/";
    private final Context mContext=NextActivity.this;
    //Widgets
    private ImageView image;
    private EditText description;
    private ProgressBar progressBar;
    private ImageView btn_close;
    private TextView btn_post;
    Intent intent;
    //Firebase
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference myRef;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseMethods mFirebaseMethods;
    private FirebaseMethods firebaseMethods;
    private StorageReference mStorageRef;
    private int image_count=0;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_next);
        setupWidgets();
        setupFirebaseAuth();
        btn_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btn_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Upload Image
                if(intent.hasExtra(getString(R.string.selected_image))){
                    mFirebaseMethods.uploadImage(getString(R.string.new_photo),description.getText().toString(),image_count,SelectedImage,null,mFitStatus);
                }
                else if(intent.hasExtra(getString(R.string.selected_bitmap))){
                    mFirebaseMethods.uploadImage(getString(R.string.new_photo),description.getText().toString(),image_count,null,bitmap,mFitStatus);
                }

                Intent intent=new Intent(mContext, HomeActivity.class);
                mContext.startActivity(intent);
            }
        });
    }
    private void setupWidgets(){
        image=findViewById(R.id.image);
        btn_close=findViewById(R.id.btn_close);
        btn_post=findViewById(R.id.tvPost);
        description=findViewById(R.id.description);
        progressBar=findViewById(R.id.progressBar);
        mFirebaseMethods=new FirebaseMethods(mContext);
        progressBar.setVisibility(View.GONE);

        intent=getIntent();
        if(intent.hasExtra(getString(R.string.selected_image))){
            SelectedImage=intent.getStringExtra(getString(R.string.selected_image));
            mFitStatus=intent.getBooleanExtra(getString(R.string.fit_status),false);
            setImage();
        }
        else if(intent.hasExtra(getString(R.string.selected_bitmap))){
            bitmap=(Bitmap)intent.getParcelableExtra(getString(R.string.selected_bitmap));
            mFitStatus=true;
            image.setImageBitmap(bitmap);
        }
    }
    private void setImage(){
        if(mFitStatus)
            image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        else
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
        ImageLoader imageLoader=ImageLoader.getInstance();
        imageLoader.displayImage(append + SelectedImage, image, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
    /**
     * Setting up Firebase Authentication
     */

    private void setupFirebaseAuth(){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        firebaseMethods=new FirebaseMethods(mContext);
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
            }
        };
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                image_count=mFirebaseMethods.getImageCount(dataSnapshot);
                Log.d(TAG, "onDataChange: Image Count "+image_count);
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

