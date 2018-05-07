package vp19.foodator.Home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import org.w3c.dom.Comment;

import java.util.ArrayList;
import java.util.Collections;

import vp19.foodator.Models.Photo;
import vp19.foodator.Models.User;
import vp19.foodator.Models.UserAccountSettings;
import vp19.foodator.R;
import vp19.foodator.utils.StringManipulation;
import vp19.foodator.utils.UniversalImageLoader;
import vp19.foodator.utils.sortPhotoByDate;

public class CommentActivity extends AppCompatActivity {
    private static final String TAG = "CommentActivity";
    private Context mContext=CommentActivity.this;
    private ScrollView sv;
    //firebase authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseUser user;
    private UserAccountSettings userDetails;
    private ArrayList<vp19.foodator.Models.Comment> comments;
    //Widgets
    private EditText comment;
    private Button postButton;
    private String photoId;
    private long commentCount;
    private LinearLayout rootLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);
        init();
        setupComments();
    }
    private void setupComments(){
        final LayoutInflater vi= (LayoutInflater) mContext.getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Query query=myRef.child(getString(R.string.dbname_comments)).child(photoId);
        query.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                try{
                    vp19.foodator.Models.Comment comment=dataSnapshot.getValue(vp19.foodator.Models.Comment.class);
                    View view=vi.inflate(R.layout.view_comment,null);
                    ImageView profileImage=view.findViewById(R.id.profileImage);
                    TextView commentText=view.findViewById(R.id.comment);
                    UniversalImageLoader.setImage(comment.getProfile_pic(),profileImage,null,"");
                    String combinedComment=comment.getUsername()+" ";
                    String username=comment.getUsername();
                    combinedComment+=comment.getComment();
                    Spannable wordtoSpan = new SpannableString(combinedComment);
                    wordtoSpan.setSpan(new ForegroundColorSpan(Color.BLACK), 0,username.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    commentText.setText(wordtoSpan);
                    rootLayout.addView(view);
                    sv.scrollTo(0,sv.getBottom());
                }
                catch (NullPointerException e){
                    Log.d(TAG, "onChildAdded: "+e.getMessage());
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void initImageLoader(){
        UniversalImageLoader imageLoader=new UniversalImageLoader(mContext);
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }
    public void init(){
        setupFirebaseAuth();
        initImageLoader();
        Intent intent=getIntent();
        comments=new ArrayList<>();
        rootLayout=findViewById(R.id.rootLayout);
        photoId=intent.getStringExtra(getString(R.string.photo_id));
        getCommentCount();
        comment=findViewById(R.id.comment);
        postButton=findViewById(R.id.postButton);
        ImageView backArrow=findViewById(R.id.backArrow);
        sv=findViewById(R.id.scrollView);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Query query1=myRef.child(getString(R.string.dbname_user_account_settings)).child(user.getUid());
        query1.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                userDetails=dataSnapshot.getValue(UserAccountSettings.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Query query2 = myRef.child(getString(R.string.dbname_comments)).child(photoId);
                    query2.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            if (StringManipulation.isStringNull(comment.getText().toString()))
                                return;
                            vp19.foodator.Models.Comment userComment = new vp19.foodator.Models.Comment();
                            userComment.setPhoto_id(photoId);
                            userComment.setComment(comment.getText().toString());
                            userComment.setProfile_pic(userDetails.getProfile_photo());
                            userComment.setUsername(userDetails.getDisplay_name());
                            myRef.child(getString(R.string.dbname_comments)).child(photoId).child(Long.toString(commentCount + 1)).setValue(userComment);
                            commentCount++;
                            comment.setText("");
                            Query query=myRef.child(getString(R.string.dbname_photos)).child(photoId);
                            query.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    Photo photo=dataSnapshot.getValue(Photo.class);
                                    photo.setComments((int)commentCount);
                                    myRef.child(getString(R.string.dbname_photos)).child(photoId).setValue(photo);
                                    myRef.child(getString(R.string.dbname_user_photos)).child(photo.getUser_id()).child(photoId).setValue(photo);
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {

                                }
                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }
                catch (NullPointerException e){
                    Log.d(TAG, "onClick: "+e.getMessage());
                }
            }

        });
    }
    private void getCommentCount(){
        Query query=myRef.child(getString(R.string.dbname_comments)).child(photoId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                commentCount=dataSnapshot.getChildrenCount();
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
