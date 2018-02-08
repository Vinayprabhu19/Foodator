/**
 *  Name : LikesFragment
 *  Type : Fragment
 *  ContentView : fragment_likes
 *  Authentication : Signed In users
 *  Purpose : To notify users when their followers like their pic
 */
package vp19.foodator.Home;
import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import vp19.foodator.Models.Notification;
import vp19.foodator.R;

public class LikesFragment extends Fragment {
    private static final String TAG = "LikesFragment";
    private Typeface font1;
    private Typeface font2;
    private String myUserID;
    private LayoutInflater vi;
    //Widgets
    private TextView notificationText;
    private LinearLayout rootLayout;
    //firebase authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseUser user;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_likes,container,false);
        try{
            setupwidgets(view);
            setupFirebaseAuth();
            setNotifications();
        }
        catch (NullPointerException e){

        }
        return view;
    }
    private void setupwidgets(View view) throws NullPointerException{
        vi = (LayoutInflater) getContext().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        notificationText=view.findViewById(R.id.tvNotification);
        font1 = Typeface.createFromAsset(getContext().getAssets(), "fonts/dudu.ttf");
        font2 = Typeface.createFromAsset(getContext().getAssets(), "fonts/straight.ttf");
        notificationText.setTypeface(font1);
        rootLayout=view.findViewById(R.id.rootLayout);
    }
    private void setNotifications()throws NullPointerException{
        rootLayout.removeAllViews();
        Query query=myRef.child(getString(R.string.dbname_notifications)).child(myUserID);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
             for(DataSnapshot ds : dataSnapshot.getChildren()){
                 Notification notification=ds.getValue(Notification.class);
                 View view=vi.inflate(R.layout.view_notification_message,null);
                 TextView message=view.findViewById(R.id.notification);
                 message.setText(notification.getMessage());
                 message.setTypeface(font2);
                 rootLayout.addView(view);
             }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    /**
     * Setting up Firebase Authentication
     */
    private void setupFirebaseAuth() throws NullPointerException{
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase= FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();
        user=mAuth.getCurrentUser();
        myUserID=user.getUid();
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
