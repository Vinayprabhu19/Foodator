/**
 *  Name : EditProfileFragment
 *  Type : Fragment
 *  ContentView : fragment_editprofile
 *  Authentication : Signed In users
 *  Purpose : To provide interface for users to change credentials and profile photo
 */
package vp19.foodator.Profile;
import android.app.Activity;
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
import android.widget.Toast;

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
        Activity activity=getActivity();
        initWidgets(view);
        setupFirebaseAuth();
        initImageLoader();
        return view;
    }

    /**
     * Initialise the widgets
     * @param view
     */
    private void initWidgets(View view){
        mProfilePhoto = (ImageView) view.findViewById(R.id.profile_photo);
        changeProfilePic=view.findViewById(R.id.changeProfilePhoto);
        mUsername = (EditText) view.findViewById(R.id.username);
        mDisplayName = (EditText) view.findViewById(R.id.display_name);
        mFirebaseMethods = new FirebaseMethods(getActivity());
        saveChanges=view.findViewById(R.id.saveChanges);
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
    }
    private void initImageLoader(){
        UniversalImageLoader imageLoader=new UniversalImageLoader(getActivity());
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }

    /**
     * Setup profile picture url to the database user_account_settings
     * @param dataSnapshot : Current snapshot of the database
     */
    private void setProfileImage(DataSnapshot dataSnapshot) throws IllegalStateException {
        UserAccountSettings settings;
        settings=dataSnapshot.child(getString(R.string.dbname_user_account_settings))
                .child(user.getUid())
                .getValue(UserAccountSettings.class);
        String imgURL=settings.getProfile_photo();
        UniversalImageLoader.setImage(imgURL, mProfilePhoto, null, "");
    }

    /**
     * Fetch username and display name from database and display them.
     * @param snapshot
     */
    private void setupLayoutWidgets(final DataSnapshot snapshot) throws IllegalStateException{
        UserAccountSettings settings=mFirebaseMethods.getUserAccountSettings(snapshot);
        mUsername.setText(settings.getUsername());
        mDisplayName.setText(settings.getDisplay_name());
        //Save Changes
        saveChanges.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeChangestoDatabase(snapshot);
                Intent intent=new Intent(getActivity(),ProfileActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                getActivity().finish();
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
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
            }
        };
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                try {
                    user=mAuth.getCurrentUser();
                    setProfileImage(dataSnapshot);
                    setupLayoutWidgets(dataSnapshot);
                }
                catch (NullPointerException e){
                    Log.d(TAG, "onDataChange: Null pointer Exception "+e.getMessage());
                }
                catch (IllegalStateException e){
                    Log.d(TAG, "onDataChange: Caught Illegal State Exception" + e.getMessage());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Write changed data to database
     * @param snapshot
     */
    public void writeChangestoDatabase(DataSnapshot snapshot){
        UserAccountSettings oldSettings=snapshot.child(getString(R.string.dbname_user_account_settings))
                .child(user.getUid())
                .getValue(UserAccountSettings.class);
        UserAccountSettings settings=new UserAccountSettings();
        String newUsername=mUsername.getText().toString();
        String newDisplayName=mDisplayName.getText().toString();
        Log.d(TAG, "writeChangestoDatabase: " + newUsername + isStringNull(newUsername));
        if(isStringNull(newUsername) | isStringNull(newDisplayName)){
            Log.d(TAG, "writeChangestoDatabase: Null Entry");
            Toast.makeText(getContext(),"Blank Entry",Toast.LENGTH_SHORT).show();
            return;
        }
        //Create the new model
        settings.setUsername(newUsername);
        settings.setDisplay_name(newDisplayName);
        settings.setProfile_photo(oldSettings.getProfile_photo());
        settings.setPosts(oldSettings.getPosts());
        settings.setFollowers(oldSettings.getFollowers());
        settings.setFollowing(oldSettings.getFollowing());

        //update database
        myRef.child(getString(R.string.dbname_user_account_settings))
                .child(user.getUid())
                .setValue(settings);
        Toast.makeText(getContext(),"Changes Saved Successfully",Toast.LENGTH_SHORT).show();
    }

    /**
     * Check is string is null
     * @param s
     * @return
     */
    public boolean isStringNull(String s){
        if((s.replace(" ","")).equals("")){
            return true;
        }
        return false;
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