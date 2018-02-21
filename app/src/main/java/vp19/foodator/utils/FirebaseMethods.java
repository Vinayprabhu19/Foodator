/**
 *  Name : FirebaseMethods
 *  Type : Utility java class
 *  ContentView : None
 *  Authentication : None
 *  Purpose : To handle firebase related methods
 */
package vp19.foodator.utils;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ThreadLocalRandom;

import vp19.foodator.Home.HomeActivity;
import vp19.foodator.Models.Photo;
import vp19.foodator.Models.User;
import vp19.foodator.Models.UserAccountSettings;
import vp19.foodator.Profile.AccountSettingsActivity;
import vp19.foodator.Profile.ProfileActivity;
import vp19.foodator.R;

public class FirebaseMethods {
    private static final String TAG = "FirebaseMethods";

    //firebase authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private StorageReference mStorageRef;
    private String FIREBASE_IMG_STORAGE="photos/users";
    private Context mContext;
    private String userID;
    private double mPhotoUploadProgress=0;

    public FirebaseMethods(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mContext = context;
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        myRef=mFirebaseDatabase.getReference();
        if(mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
        }
    }

    /**
     *  Check if the username already exists while registering new user
     * @param username : Username entered in registration
     * @param dataSnapshot : Current snapshot of the database
     * @return true if it exists else false
     */
    public boolean checkIfUsernameExists(String username, DataSnapshot dataSnapshot){
        User user=new User();
        for(DataSnapshot ds:dataSnapshot.child(userID).getChildren())
        {
            user.setUser_name(ds.getValue(User.class).getUser_name());
            String oUsername=StringManipulation.expandUserName(user.getUser_name());
            if(oUsername.equals(username)){
                Log.d(TAG, "checkIfUsernameExists: Found A Match");
                return true;
            }
        }
        return false;
    }

    /**
     * Send email verification for the registered user
     */
    public void sendVerification(){
        FirebaseUser user=FirebaseAuth.getInstance().getCurrentUser();
        if(user != null){
            user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                    }
                    else
                        Toast.makeText(mContext,"Couldn't send mail verification",Toast.LENGTH_SHORT);
                }
            });
        }
    }
    /**
     * Register new email and password to Firebase Authentication
     * @param email : Email
     * @param password : Password
     * @param username : username
     */
    public void registerNewEmail(final String email,String password, final String username) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(TAG, "createUserWithEmail:onComplete:" + task.isSuccessful());

                        // If sign in fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            Toast.makeText(mContext, R.string.auth_failed,
                                    Toast.LENGTH_SHORT).show();
                        }else if(task.isSuccessful()) {
                            sendVerification();
                            userID = mAuth.getCurrentUser().getUid();
                            Log.d(TAG, "onComplete: Authstate changed" + userID);
                        }
                    }
                });
    }

    /**
     * Adds new user to the database after the user successfully registers
     * @param email : Email
     * @param username : username
     * @param profile_photo : Url for the Profile Photo
     * @param displayName : Name to displayed for the user
     */
    public void addNewUser(String email,String username,String profile_photo,String displayName)
    {
        User user=new User(userID,StringManipulation.condenseUsername(username),email,1);
        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .setValue(user);
        UserAccountSettings settings=new UserAccountSettings(
                displayName,0,0,0,profile_photo,StringManipulation.condenseUsername(username));
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .setValue(settings);
    }

    /**
     * Retrieves the User account settings to be displayed in the profile
     * @param dataSnapshot : current snapshot of the database
     * @return USerAccountSettings for the logged in user
     */
    public UserAccountSettings getUserAccountSettings(DataSnapshot dataSnapshot){
        UserAccountSettings settings=new UserAccountSettings();
        DataSnapshot ds= dataSnapshot.child(mContext.getString(R.string.dbname_user_account_settings)).child(userID);
        try{
        settings.setDisplay_name(
                ds.getValue(UserAccountSettings.class)
                .getDisplay_name()
            );
        settings.setFollowers(
                ds.getValue(UserAccountSettings.class)
                        .getFollowers()
        );
        settings.setFollowing(
                ds.getValue(UserAccountSettings.class)
                        .getFollowing()
        );
        settings.setPosts(
                ds.getValue(UserAccountSettings.class)
                        .getPosts()
        );
        settings.setProfile_photo(
                ds.getValue(UserAccountSettings.class)
                        .getProfile_photo()
        );
        settings.setUsername(
                ds.getValue(UserAccountSettings.class)
                        .getUsername()
        );}
        catch (NullPointerException e){
            Log.d(TAG, "getUserAccountSettings: Exception"+e.getMessage());
        }
        return settings;
    }

    /**
     *  Get the count of the images uploaded by the user
     * @param dataSnapshot : Current snapshots of the user
     * @return :  Count of the images uploaded by the user
     */
    public int getImageCount(DataSnapshot dataSnapshot){
        int count=0;
        DataSnapshot ds=dataSnapshot.child(mContext.getString(R.string.dbname_user_photos)).child(userID);
        count=(int)ds.getChildrenCount();
        return count;
    }

    /**
     * Set the posts counts for the user
     * @param dataSnapshot : Current snapshots of the user
     */
    public void setPostsCount(DataSnapshot dataSnapshot){
        UserAccountSettings settings=new UserAccountSettings();
        DataSnapshot ds= dataSnapshot.child(mContext.getString(R.string.dbname_user_account_settings)).child(userID);
        int image_count=getImageCount(dataSnapshot);
        Log.d(TAG, "setPostsCount: Image count is "+image_count);
        try{
            settings.setDisplay_name(
                    ds.getValue(UserAccountSettings.class)
                            .getDisplay_name()
            );
            settings.setFollowers(
                    ds.getValue(UserAccountSettings.class)
                            .getFollowers()
            );
            settings.setFollowing(
                    ds.getValue(UserAccountSettings.class)
                            .getFollowing()
            );
            settings.setPosts(
                    image_count
            );
            settings.setProfile_photo(
                    ds.getValue(UserAccountSettings.class)
                            .getProfile_photo()
            );
            settings.setUsername(
                    ds.getValue(UserAccountSettings.class)
                            .getUsername()
            );}
        catch (NullPointerException e){
            Log.d(TAG, "getUserAccountSettings: Exception"+e.getMessage());
        }
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .setValue(settings);
    }

    /**
     * Upload image to firebase storage
     * @param photoType : Type of the photo : New Photo or Profile Photo
     * @param description : Description provided for new photo
     * @param image_count : Image count of the user
     * @param imgURL : URL of the image to be uploaded
     * @param mFitStatus : (Scale Type) true -> Center Inside , False -> Center Crop
     * @throws NullPointerException
     */
    public void uploadImage(String photoType, final String description, final int image_count, final String imgURL,Bitmap bm, final boolean mFitStatus) throws NullPointerException{
        //From share Activity
        if(photoType.equals(mContext.getString(R.string.new_photo))){
            RandomString gen = new RandomString(8, ThreadLocalRandom.current());
            StorageReference storageReference=mStorageRef
                    .child(FIREBASE_IMG_STORAGE +"/"+userID+"/photo"+gen.nextString()+(image_count+1));
            //convert image to bitmap
            if(bm == null){
                bm=ImageManager.getBitmap(imgURL);
            }
            UploadTask uploadTask=null;
            byte[] bytes=ImageManager.getBytesFromBitmap(bm,100);
            uploadTask=storageReference.putBytes(bytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri ImageUri=taskSnapshot.getDownloadUrl();
                    Toast.makeText(mContext,"Photo upload Success !",Toast.LENGTH_SHORT).show();
                    //Add photo to photos and user_photos
                    addPhotoToDatabase(description,ImageUri.toString(),mFitStatus);
                    myRef.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Log.d(TAG, "onDataChange: Called Value listener");
                            setPostsCount(dataSnapshot);
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(mContext,"Photo upload failed !",Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress=(100*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                    Toast.makeText(mContext,"Photo upload progress "+String.format("%.0f",progress),Toast.LENGTH_SHORT).show();
                }
            });
        }
        else{
            ((AccountSettingsActivity)mContext).setViewPager(0);
            StorageReference storageReference=mStorageRef
                    .child(FIREBASE_IMG_STORAGE +"/"+userID+"/profile_photo");
            //convert image to bitmap
            if(bm == null){
                bm=ImageManager.getBitmap(imgURL);
            }
            UploadTask uploadTask=null;
            byte[] bytes=ImageManager.getBytesFromBitmap(bm,100);
            uploadTask=storageReference.putBytes(bytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    Uri ImageUri=taskSnapshot.getDownloadUrl();
                    Toast.makeText(mContext,"Photo upload Success !",Toast.LENGTH_SHORT).show();
                    //Insert into user account settings
                    addProfilePic(ImageUri.toString());
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(mContext,"Photo upload failed !",Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress=(100*taskSnapshot.getBytesTransferred())/taskSnapshot.getTotalByteCount();
                    Toast.makeText(mContext,"Photo upload progress "+String.format("%.0f",progress),Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    /**
     * Add profile Picture to the user database
     * @param imageUrl : URL of the image uploaded to the storage
     */
    private void addProfilePic(String imageUrl){
        UserAccountSettings settings=new UserAccountSettings();
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .child(mContext.getString(R.string.attr_profile_pic))
                .setValue(imageUrl);
    }

    /**
     * Utility function to get the current time
     * @return Simple Date Format time
     */
    private String getTime(){
        SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-DD'T'HH-mm-ss'Z'", Locale.UK);
        sdf.setTimeZone(TimeZone.getTimeZone("Asia/Kolkata"));
        return sdf.format(new Date());
    }

    /**
     * Add the photo to the database
     * @param description : Description of the photo
     * @param ImaUri : Image Uri from the storage
     * @param mFitStatus : (Scale Type) true -> Center Inside , False -> Center Crop
     */
    private void addPhotoToDatabase(String description,String ImaUri,boolean mFitStatus){
        String newPhotoKey=myRef.child(mContext.getString(R.string.dbname_photos)).push().getKey();
        String tags=StringManipulation.getTags(description);
        Photo photo=new Photo(description,getTime(),ImaUri,newPhotoKey,userID,tags,0,0,mFitStatus);

        //insert into database
        myRef.child(mContext.getString(R.string.dbname_photos)).child(newPhotoKey).setValue(photo);
        myRef.child(mContext.getString(R.string.dbname_user_photos)).child(userID).child(newPhotoKey).setValue(photo);
    }
}