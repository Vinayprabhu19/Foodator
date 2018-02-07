/**
 *  Name : HomeFragment
 *  Type : Fragment
 *  ContentView : fragment_home
 *  Authentication : Signed In users
 *  Purpose : To display the posts of the users
 */
package vp19.foodator.Home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.Inflater;

import vp19.foodator.Models.Photo;
import vp19.foodator.Models.UserAccountSettings;
import vp19.foodator.Profile.ProfileActivity;
import vp19.foodator.Profile.UserProfileActivity;
import vp19.foodator.R;
import vp19.foodator.utils.FirebaseMethods;
import vp19.foodator.utils.SquareImageView;
import vp19.foodator.utils.StringManipulation;
import vp19.foodator.utils.UniversalImageLoader;

import static vp19.foodator.R.string.photo;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private Typeface font;
    //firebase authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseUser user;
    private FirebaseMethods firebaseMethods;
    //Widgets
    ViewGroup rootLayout;
    ArrayList<Photo> photoList;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        initImageLoader();
        setupFirebaseAuth();
        try{
           queryPhotos();
        }
        catch (NullPointerException e){
            Log.d(TAG, "onCreateView: Exception"+e.getMessage());
        }
        return view;
    }
    private void initImageLoader(){
        font = Typeface.createFromAsset(getContext().getAssets(), "fonts/straight.ttf");
        UniversalImageLoader imageLoader=new UniversalImageLoader(getContext());
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }
    private void queryPhotos() throws NullPointerException{
        photoList=new ArrayList<>();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        Query query=reference.child(getString(R.string.dbname_photos))
                .orderByChild(getString(R.string.attr_date));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    Photo photo=ds.getValue(Photo.class);
                    Log.d(TAG, "Photo list" + photo.getImage_path());
                    photoList.add(photo);
                }
                setViews();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void setViews(){
        Collections.reverse(photoList);
        rootLayout=getView().findViewById(R.id.root);
        final LayoutInflater vi = (LayoutInflater) getContext().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(int i=0;i<photoList.size();i++){
            final Photo photo=photoList.get(i);
            String userId=photo.getUser_id();
            View view = vi.inflate(R.layout.layout_post,null);
            view.setTag(photo.getPhoto_id());

            //Get widgets in a view
            final ImageView profileImage=view.findViewById(R.id.profileImage);
            final TextView displayName=view.findViewById(R.id.display_name);
            SquareImageView image=view.findViewById(R.id.imagePost);
            final ProgressBar progressBar=view.findViewById(R.id.progressBar);
            final TextView description=view.findViewById(R.id.description);
            final ToggleButton btn_like=view.findViewById(R.id.ivLike);
            final ToggleButton btn_dislike=view.findViewById(R.id.ivDislike);
            final TextView likes=view.findViewById(R.id.tvLike);
            final TextView dislikes=view.findViewById(R.id.tvDislike);

            //Get the user details
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
            Query query=reference.child(getString(R.string.dbname_user_account_settings))
                    .child(userId);
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    UserAccountSettings settings=dataSnapshot.getValue(UserAccountSettings.class);
                    setImage(profileImage,settings.getProfile_photo(),progressBar);
                    displayName.setText(settings.getDisplay_name());
                    String caption=photo.getCaption();
                    if(!StringManipulation.isStringNull(caption))
                        description.setText(caption);
                    displayName.setTypeface(font);
                    displayName.setTextSize(16);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
            displayName.setTag(photo.getUser_id());
            likes.setText(Integer.toString(photo.getLikes()));
            dislikes.setText(Integer.toString(photo.getDislikes()));
            setImage(image,photo.getImage_path(),progressBar);
            rootLayout.addView(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "Clicked View " + v.getTag());
                }
            });
            displayName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String userID=displayName.getTag().toString();
                    String myUserID=user.getUid();
                    if(userID.equals(myUserID)){
                        Intent intent=new Intent(getActivity(), ProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NO_ANIMATION);
                        getActivity().overridePendingTransition(0, 0);
                        startActivity(intent);
                    }
                    else{
                        Intent intent=new Intent(getActivity(), UserProfileActivity.class);
                        intent.putExtra(getString(R.string.calling_activity),userID);
                        startActivity(intent);
                    }
                }
            });

            //Handle Likes and Dislikes
        }
    }
    /**
     * Setting up Firebase Authentication
     */
    private void setupFirebaseAuth(){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase= FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();
        firebaseMethods=new FirebaseMethods(getContext());
        user=mAuth.getCurrentUser();
    }
    @Override
    public void onStart() {
        super.onStart();
    }
    @Override
    public void onStop() {
        super.onStop();
    }
    private void setImage(ImageView gridImage, String URL, final ProgressBar progressBar){
        ImageLoader imageLoader=ImageLoader.getInstance();
        imageLoader.displayImage(URL, gridImage, new ImageLoadingListener() {
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
}
