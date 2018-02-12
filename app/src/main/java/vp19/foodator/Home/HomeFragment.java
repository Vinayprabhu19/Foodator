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
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
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
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.volokh.danylo.hashtaghelper.HashTagHelper;

import java.util.ArrayList;
import java.util.Collections;

import vp19.foodator.Models.Photo;
import vp19.foodator.Models.UserAccountSettings;
import vp19.foodator.Profile.ProfileActivity;
import vp19.foodator.Profile.UserProfileActivity;
import vp19.foodator.R;
import vp19.foodator.utils.FirebaseMethods;
import vp19.foodator.utils.SquareImageView;
import vp19.foodator.utils.StringManipulation;
import vp19.foodator.utils.UniversalImageLoader;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    private Typeface font;
    private View fragmentView;
    private static int no_posts=10;
    //firebase authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseUser user;

    //Widgets
    ViewGroup rootLayout;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        fragmentView=view;
        try{
            initImageLoader();
            setupFirebaseAuth();
            queryPhotos();
            refresh(view);
        }
        catch (NullPointerException e){
            Log.d(TAG, "onCreateView: Exception"+e.getMessage());
        }
        return view;
    }

    /**
     * Refresh the layout
     * @param view fragment view
     */
    private void refresh(View view) throws NullPointerException{
        final SwipeRefreshLayout refresh=view.findViewById(R.id.refresh);
        refresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                queryPhotos();
                refresh.setRefreshing(false);
            }
        });
    }
    private void initImageLoader(){
        font = Typeface.createFromAsset(getContext().getAssets(), "fonts/straight.ttf");
        UniversalImageLoader imageLoader=new UniversalImageLoader(getContext());
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }

    /**
     * Query Photos for the users followed by the user
     * @throws NullPointerException
     */
    private void queryPhotos() throws NullPointerException{
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference();
        final ArrayList<String> users=new ArrayList<>();
        Query query=reference.child(getString(R.string.dbname_following)).child(user.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot ds : dataSnapshot.getChildren()){
                    users.add(ds.getKey());
                }
                users.add(user.getUid());
                getPhotos(users);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    /**
     * Get the photos for the users
     * @param users : users followed by current user
     * @throws NullPointerException
     */
    private void getPhotos(final ArrayList<String> users)throws NullPointerException{
        final ArrayList<Photo> photoList = new ArrayList<>();
        Query query=myRef.child(getString(R.string.dbname_user_photos));
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(int i=0;i<users.size();i++) {
                    for (DataSnapshot ds : dataSnapshot.child(users.get(i)).getChildren()) {
                        Photo photo = ds.getValue(Photo.class);
                        photoList.add(photo);
                    }
                }
                Log.d(TAG, "setViews " + photoList.size());
                Collections.sort(photoList,new sortPhotoByDate());
                setViews(photoList);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Dynamically set the views in the rootLayout
     * @param photoList
     * @throws NullPointerException
     */
    private void setViews(final ArrayList<Photo> photoList) throws NullPointerException{
        rootLayout=fragmentView.findViewById(R.id.root);
        rootLayout.removeAllViews();
        final LayoutInflater vi;
        try{
            vi = (LayoutInflater) getContext().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        catch (NullPointerException e){
            Log.d(TAG, "setViews: NullPointerException");
            return;
        }
        int len=(no_posts<photoList.size())?no_posts:photoList.size();
        for(int i=0;i<len;i++){
            View v=vi.inflate(R.layout.layout_post,null);
            rootLayout.addView(v);
        }
        TextView loadMore=(TextView)vi.inflate(R.layout.view_loadmore,null);
        if(len==no_posts)
            rootLayout.addView(loadMore);
        for(int i=0;i<len;i++){
            final Photo photo=photoList.get(i);
            final String userId=photo.getUser_id();
            View view = rootLayout.getChildAt(i);
            view.setTag(photo.getPhoto_id());

            //Get widgets in a view
            HashTagHelper mTextHashTagHelper;
            final ImageView profileImage=view.findViewById(R.id.profileImage);
            final TextView displayName=view.findViewById(R.id.display_name);
            SquareImageView image=view.findViewById(R.id.imagePost);
            final ProgressBar progressBar=view.findViewById(R.id.progressBar);
            final TextView description=view.findViewById(R.id.description);
            final ToggleButton btn_like=view.findViewById(R.id.ivLike);
            final ToggleButton btn_dislike=view.findViewById(R.id.ivDislike);
            final TextView likes=view.findViewById(R.id.tvLike);
            final TextView dislikes=view.findViewById(R.id.tvDislike);
            final ImageView postOptions=view.findViewById(R.id.postOptions);
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
            mTextHashTagHelper = HashTagHelper.Creator.create(getResources().getColor(R.color.light_blue), new HashTagHelper.OnHashTagClickListener() {
                @Override
                public void onHashTagClicked(String hashTag) {
                    hashTag="#"+hashTag;
                    Log.d(TAG, "onHashTagClicked: "+hashTag);
                    ((HomeActivity)getActivity()).searchString=hashTag;
                    ((HomeActivity)getActivity()).viewPager.setCurrentItem(0);
                }
            });
            mTextHashTagHelper.handle(description);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "View clicked " + v.getTag());
                }
            });
            postOptions.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(photo.getUser_id().equals(user.getUid()))
                        createPopupMenu(photo,postOptions,R.menu.post_user_menu);
                    else
                        createPopupMenu(photo,postOptions,R.menu.post_menu);
                }
            });
            displayName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String userID=displayName.getTag().toString();
                    String myUserID=user.getUid();
                    if(userID.equals(myUserID)){
                        Intent intent=new Intent(getActivity(), ProfileActivity.class);
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
            loadMore.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    no_posts+=5;
                    setViews(photoList);
                }
            });
            //Handle Likes and Dislikes
        }
    }
    private void createPopupMenu(final Photo photo, ImageView postOptions,int menu)throws NullPointerException{
        PopupMenu popupMenu=new PopupMenu(getContext(),postOptions);
        popupMenu.inflate(menu);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()){
                    case R.id.identify :
                        Log.d(TAG, "onMenuItemClick: identify ");
                        break;
                    case R.id.recipe:
                        Log.d(TAG, "onMenuItemClick: Recipe");
                        break;
                    case R.id.report:
                        Log.d(TAG, "onMenuItemClick: Report");
                        break;
                    case R.id.delete:
                        deletePhoto(photo);
                        Log.d(TAG, "onMenuItemClick: Delete");
                }
                return false;
            }
        });
        popupMenu.show();
    }
    private void deletePhoto(Photo photo) throws NullPointerException{
        myRef.child(getString(R.string.dbname_photos)).child(photo.getPhoto_id()).removeValue();
        myRef.child(getString(R.string.dbname_user_photos)).child(user.getUid()).child(photo.getPhoto_id()).removeValue();
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserAccountSettings settings=dataSnapshot.child(getString(R.string.dbname_user_account_settings)).child(user.getUid()).getValue(UserAccountSettings.class);
                settings.setPosts(settings.getPosts()-1);
                myRef.child(getString(R.string.dbname_user_account_settings)).child(user.getUid()).setValue(settings);
                Intent intent=new Intent(getContext(),ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
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
    private void setImage(ImageView gridImage, String URL, final ProgressBar progressBar)throws NullPointerException{
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
