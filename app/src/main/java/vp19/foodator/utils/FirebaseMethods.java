package vp19.foodator.utils;
import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import vp19.foodator.Models.User;
import vp19.foodator.Models.UserAccountSettings;
import vp19.foodator.R;

public class FirebaseMethods {
    private static final String TAG = "FirebaseMethods";

    //firebase authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;

    private Context mContext;
    private String userID;

    public FirebaseMethods(Context context) {
        mAuth = FirebaseAuth.getInstance();
        mContext = context;
        mFirebaseDatabase=FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();
        if(mAuth.getCurrentUser() != null) {
            userID = mAuth.getCurrentUser().getUid();
        }
    }
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
     * @param email
     * @param password
     * @param username
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

    public void addNewUser(String email,String username,String Description,String Website,String profile_photo)
    {
        User user=new User(userID,StringManipulation.condenseUsername(username),email,1);
        myRef.child(mContext.getString(R.string.dbname_users))
                .child(userID)
                .setValue(user);
        UserAccountSettings settings=new UserAccountSettings(
                Description,username,0,0,0,profile_photo,username);
        myRef.child(mContext.getString(R.string.dbname_user_account_settings))
                .child(userID)
                .setValue(settings);
    }
}