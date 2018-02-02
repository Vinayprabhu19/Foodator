/**
 *  Name : EditProfileFragment
 *  Type : Fragment
 *  ContentView : fragment_editprofile
 *  Authentication : Signed In users
 *  Purpose : To provide interface for users to change credentials and profile photo
 */
package vp19.foodator.Profile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
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

import vp19.foodator.Models.UserAccountSettings;
import vp19.foodator.R;
import vp19.foodator.Share.ShareActvity;
import vp19.foodator.utils.FirebaseMethods;
import vp19.foodator.utils.UniversalImageLoader;


public class EditProfileFragment extends Fragment {
    private static final String TAG = "EditProfileFragment";
    //Widgets
    private TextView changeProfilePic;
    private ImageView mProfilePhoto;
    private ImageView saveChanges;
    //firebase authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private FirebaseMethods mFirebaseMethods;
    private DatabaseReference myRef;
    private FirebaseUser user;

    private EditText mDisplayName, mUsername;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editprofile, container, false);
        mProfilePhoto = (ImageView) view.findViewById(R.id.profile_photo);
        changeProfilePic=view.findViewById(R.id.changeProfilePhoto);
        mUsername = (EditText) view.findViewById(R.id.username);
        mDisplayName = (EditText) view.findViewById(R.id.display_name);
//        mDescription = (EditText) view.findViewById(R.id.description);
//        mEmail = (EditText) view.findViewById(R.id.email);
//        mPhoneNumber = (EditText) view.findViewById(R.id.phoneNumber);
        mFirebaseMethods = new FirebaseMethods(getActivity());
        saveChanges=view.findViewById(R.id.saveChanges);
        Log.d(TAG, "onCreateView: Edit Profile");
        //backarrow to go back to "ProfileActivity"
        ImageView backArrow = (ImageView) view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Navigating back to profile activity");
                getActivity().finish();
            }
        });
        //Change profile picture
        changeProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(), ShareActvity.class);
                intent.putExtra(getString(R.string.edit_profile),"Edit profile");
                getActivity().startActivity(intent);
            }
        });
        //Save Changes
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(),ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
        setupFirebaseAuth();
        initImageLoader();
        return view;
    }
    private void initImageLoader(){
        UniversalImageLoader imageLoader=new UniversalImageLoader(getActivity());
        ImageLoader.getInstance().init(imageLoader.getConfig());
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
     * Fetch username and display name from database and display them.
     * @param settings
     */
    private void setupLayoutWidgets(UserAccountSettings settings){
        mUsername.setText(settings.getUsername());
        mDisplayName.setText(settings.getDisplay_name());
    }

    /**
     * Setting up Firebase Authentication
     */
    private void setupFirebaseAuth(){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();
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
                    setProfileImage(dataSnapshot);
                    setupLayoutWidgets(mFirebaseMethods.getUserAccountSettings(dataSnapshot));
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
}