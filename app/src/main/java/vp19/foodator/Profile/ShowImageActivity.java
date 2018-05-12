package vp19.foodator.Profile;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
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

import vp19.foodator.Food.ImageIdentifyActivity;
import vp19.foodator.Home.CommentActivity;
import vp19.foodator.Models.Photo;
import vp19.foodator.Models.UserAccountSettings;
import vp19.foodator.Models.liked_photo;
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
    private String currentUserID;
    private Typeface font;
    //Widgets
    private ImageView btn_back;
    private ImageView imagePost;
    private ImageView profileImage;
    private TextView displayName;
    private ProgressBar progressBar;
    private TextView description;
    private ImageView postOptions;
    private ToggleButton btn_like;
    private ToggleButton btn_comment;
    private TextView likes;
    private TextView comments;
    //firebase authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseMethods firebaseMethods;
    private FirebaseUser user;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        overridePendingTransition(0,0);
        setContentView(R.layout.activity_show_image);
        init();
        setupFirebaseAuth();
    }
    private void init(){
        font = Typeface.createFromAsset(mContext.getAssets(), "fonts/Ubuntu-R.ttf");
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
        postOptions=findViewById(R.id.postOptions);
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
        postOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(userId.equals(currentUserID))
                    createPopupMenu(R.menu.post_user_menu);
                else
                    createPopupMenu(R.menu.post_menu);
            }
        });
    }
    private void createPopupMenu(int menu){
        PopupMenu popupMenu=new PopupMenu(mContext,postOptions);
        popupMenu.inflate(menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.identify :
                        Log.d(TAG, "onMenuItemClick: identify ");
                        Intent intent=new Intent(mContext, ImageIdentifyActivity.class);
                        intent.putExtra(getString(R.string.selected_bitmap),imageURL);
                        startActivity(intent);
                    break;
                    case R.id.report:
                        Log.d(TAG, "onMenuItemClick: Report");
                        break;
                    case R.id.delete:
                        deletePhoto();
                        Log.d(TAG, "onMenuItemClick: Delete");
                }
                return false;
            }
        });
        popupMenu.show();
    }
    private void deletePhoto(){
        myRef.child(getString(R.string.dbname_photos)).child(PhotoId).removeValue();
        myRef.child(getString(R.string.dbname_user_photos)).child(currentUserID).child(PhotoId).removeValue();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserAccountSettings settings=dataSnapshot.child(getString(R.string.dbname_user_account_settings)).child(currentUserID).getValue(UserAccountSettings.class);
                settings.setPosts(settings.getPosts()-1);
                myRef.child(getString(R.string.dbname_user_account_settings)).child(currentUserID).setValue(settings);
                Intent intent=new Intent(mContext,ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
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
    private void setParams(UserAccountSettings settings){
        UniversalImageLoader.setImage(settings.getProfile_photo(), profileImage, null, "");
        displayName.setText(settings.getDisplay_name());
        displayName.setTypeface(font);
    }
    /**
     * Setting up Firebase Authentication
     */
    private void setupFirebaseAuth(){
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase= FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();
        firebaseMethods=new FirebaseMethods(mContext);
        user=mAuth.getCurrentUser();
        currentUserID=mAuth.getCurrentUser().getUid();
        Query query1=myRef.child(getString(R.string.dbname_user_account_settings))
                .child(userId);
        try {
            query1.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    UserAccountSettings settings = dataSnapshot.getValue(UserAccountSettings.class);
                    setParams(settings);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
            Query query2 = myRef.child(getString(R.string.dbname_photos))
                    .child(PhotoId);
            query2.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    final Photo photo = dataSnapshot.getValue(Photo.class);
                    String caption = photo.getCaption();
                    if (!StringManipulation.isStringNull(caption))
                        description.setText(caption);
                    btn_like=findViewById(R.id.ivLike);
                    btn_comment=findViewById(R.id.ivComment);
                    checkForPhotoLike(btn_like,photo.getPhoto_id());
                    likes=findViewById(R.id.tvLike);
                    comments=findViewById(R.id.tvComment);
                    likes.setText(Integer.toString(photo.getLikes()));
                    comments.setText(Integer.toString(photo.getComments()));
                    btn_comment.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent=new Intent(ShowImageActivity.this, CommentActivity.class);
                            intent.putExtra(getString(R.string.photo_id),photo.getPhoto_id());
                            startActivity(intent);
                        }
                    });
                    btn_like.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(btn_like.isChecked()) {
                                btn_like.setBackgroundDrawable(mContext.getDrawable(R.drawable.ic_star_color));
                                increaseLikes(photo.getPhoto_id(),photo.getUser_id(),likes);
                                Log.d(TAG, "onClick: checked");
                            }
                            else{
                                btn_like.setBackgroundDrawable(mContext.getDrawable(R.drawable.ic_star_nocolor));
                                decreaseLikes(photo.getPhoto_id(),photo.getUser_id(),likes);
                                Log.d(TAG, "onClick: nochecked");
                            }
                        }
                    });
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        catch (NullPointerException e){
            Log.d(TAG, "setupFirebaseAuth: "+e.getMessage());
        }
    }
    @Override
    public void onStart() {
        super.onStart();
    }
    @Override
    public void onStop() {
        super.onStop();
    }
    private void checkForPhotoLike(final ToggleButton btn_like, final String photo_id){
        Query query=myRef.child(getString(R.string.dbname_liked_photos)).child(user.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(photo_id)){
                    btn_like.setBackgroundDrawable(mContext.getDrawable(R.drawable.ic_star_color));
                    btn_like.setChecked(true);
                }
                else {
                    btn_like.setBackgroundDrawable(mContext.getDrawable(R.drawable.ic_star_nocolor));
                    btn_like.setChecked(false);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void decreaseLikes(final String photo_id,final String user_id,final TextView likes){
        Query query;
        query=myRef.child(getString(R.string.dbname_photos));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Photo photo=dataSnapshot.child(photo_id).getValue(Photo.class);
                photo.setLikes(photo.getLikes()-1);
                likes.setText(Integer.toString(photo.getLikes()));
                myRef.child(getString(R.string.dbname_photos)).child(photo_id).setValue(photo);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        query=myRef.child(getString(R.string.dbname_user_photos)).child(user_id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Photo photo=dataSnapshot.child(photo_id).getValue(Photo.class);
                photo.setLikes(photo.getLikes()-1);
                myRef.child(getString(R.string.dbname_user_photos)).child(user_id).child(photo_id).setValue(photo);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        myRef.child(getString(R.string.dbname_liked_photos)).child(user.getUid()).child(photo_id).removeValue();
    }
    private void increaseLikes(final String photo_id,final String user_id,final TextView likes){
        Query query;
        query=myRef.child(getString(R.string.dbname_photos));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Photo photo=dataSnapshot.child(photo_id).getValue(Photo.class);
                photo.setLikes(photo.getLikes()+1);
                likes.setText(Integer.toString(photo.getLikes()));
                myRef.child(getString(R.string.dbname_photos)).child(photo_id).setValue(photo);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        query=myRef.child(getString(R.string.dbname_user_photos)).child(user_id);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Photo photo=dataSnapshot.child(photo_id).getValue(Photo.class);
                photo.setLikes(photo.getLikes()+1);
                myRef.child(getString(R.string.dbname_user_photos)).child(user_id).child(photo_id).setValue(photo);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
        myRef.child(getString(R.string.dbname_liked_photos)).child(user.getUid()).child(photo_id).setValue(new liked_photo(photo_id));
    }
}
