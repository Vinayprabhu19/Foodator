/**
 *  Name : HomeFragment
 *  Type : Fragment
 *  ContentView : fragment_home
 *  Authentication : Signed In users
 *  Purpose : To display the posts of the users
 */
package vp19.foodator.Home;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;

import vp19.foodator.Models.Photo;
import vp19.foodator.Models.UserAccountSettings;
import vp19.foodator.Models.liked_photo;
import vp19.foodator.Profile.ProfileActivity;
import vp19.foodator.Profile.UserProfileActivity;
import vp19.foodator.R;
import vp19.foodator.utils.SquareImageView;
import vp19.foodator.utils.StringManipulation;
import vp19.foodator.utils.UniversalImageLoader;
import vp19.foodator.utils.sortPhotoByDate;

public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    //fonts
    private Typeface font;
    private Typeface fUbuntuBold;
    private Typeface fUbuntuLight;
    private Typeface fUbuntuMedium;
    private Typeface fUbuntuRegular;
    private Typeface fUbuntuMono;
    private View fragmentView;
    private int sPosts=0,ePosts=10;
    private LinearLayout progressLayout;
    //firebase authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseUser user;
    private ArrayList<Photo> photoList;
    //Widgets
    ViewGroup rootLayout;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        fragmentView=view;
        try{
            init();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    socketConnect();
                }
            }).start();
            setupFirebaseAuth();
            queryPhotos();
            refresh(view);
        }
        catch (NullPointerException e){
            Log.d(TAG, "onCreateView: Exception"+e.getMessage());
        }
        return view;
    }
    private void socketConnect(){
        try{
            Socket soc=new Socket("192.168.43.17",2004);
            DataOutputStream dout=new DataOutputStream(soc.getOutputStream());
            BufferedReader  din  = new BufferedReader(new InputStreamReader(soc.getInputStream()));
            String url="https://i.ndtvimg.com/i/2017-11/oats-idli_620x330_71510224674.jpg";
            dout.writeUTF(url);
            String s=din.readLine();
            Log.d(TAG, "socketConnect: Item :" + s);
            dout.write(20);
            dout.flush();
            dout.close();
            soc.close();
        }
        catch (IOException e){
            Log.d(TAG, "socketConnect: "+e.getMessage());
        }
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
                sPosts=0;
                ePosts=10;
                queryPhotos();
                refresh.setRefreshing(false);
            }
        });
    }
    private void init(){
        font = Typeface.createFromAsset(getContext().getAssets(), "fonts/Ubuntu-R.ttf");
        fUbuntuBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Ubuntu-B.ttf");
        fUbuntuLight = Typeface.createFromAsset(getContext().getAssets(), "fonts/Ubuntu-L.ttf");
        fUbuntuMedium = Typeface.createFromAsset(getContext().getAssets(), "fonts/Ubuntu-M.ttf");
        fUbuntuRegular = Typeface.createFromAsset(getContext().getAssets(), "fonts/Ubuntu-R.ttf");
        fUbuntuMono = Typeface.createFromAsset(getContext().getAssets(), "fonts/UbuntuMono-B.ttf");
        photoList=new ArrayList<>();
        rootLayout=fragmentView.findViewById(R.id.root);
        LayoutInflater vi= (LayoutInflater) getContext().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        progressLayout=(LinearLayout)vi.inflate(R.layout.layout_progress,null);
        ImageView progressImage=progressLayout.findViewById(R.id.progressImage);
        TextView progressText=progressLayout.findViewById(R.id.progressText);
        progressImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.fork,null));
        progressText.setText(getString(R.string.string_adding_spices));
        progressText.setTypeface(fUbuntuBold);
        UniversalImageLoader imageLoader=new UniversalImageLoader(getContext());
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }

    /**
     * Query Photos for the users followed by the user
     * @throws NullPointerException
     */
    private void queryPhotos() throws NullPointerException{
        rootLayout.removeAllViews();
        rootLayout.addView(progressLayout);
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
        photoList.clear();
        Query query=myRef.child(getString(R.string.dbname_user_photos));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
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
                rootLayout.removeViewAt(0);
                setViews();
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Dynamically set the views in the rootLayout
     * @throws NullPointerException
     */
    private void setViews() throws NullPointerException{
        final LayoutInflater vi;
        try{
            vi = (LayoutInflater) getContext().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        catch (NullPointerException e){
            Log.d(TAG, "setViews: NullPointerException");
            return;
        }
        int len=(ePosts<photoList.size())?ePosts:photoList.size();
        for(int i=sPosts;i<len;i++){
            View v=vi.inflate(R.layout.layout_post,null);
            rootLayout.addView(v);
        }
        RelativeLayout relativeLayout=(RelativeLayout)vi.inflate(R.layout.view_loadmore,null);
        TextView loadMore=relativeLayout.findViewById(R.id.loadmore);
        loadMore.setTypeface(font);
        if(len==ePosts)
            rootLayout.addView(relativeLayout);
        for(int i=sPosts;i<len;i++){
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
            final ToggleButton btn_comment=view.findViewById(R.id.ivComment);
            final TextView likes=view.findViewById(R.id.tvLike);
            final TextView comments=view.findViewById(R.id.tvComment);
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
                    description.setTypeface(font);
                    displayName.setTypeface(font);
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {
                }
            });
            checkForPhotoLike(btn_like,photo.getPhoto_id());
            displayName.setTag(photo.getUser_id());
            likes.setText(Integer.toString(photo.getLikes()));
            comments.setText(Integer.toString(photo.getComments()));
            setImage(image,photo.getImage_path(),progressBar);
            mTextHashTagHelper = HashTagHelper.Creator.create(getResources().getColor(R.color.dark_violet), new HashTagHelper.OnHashTagClickListener() {
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
            btn_like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if(btn_like.isChecked()) {
                        btn_like.setBackgroundDrawable(getContext().getDrawable(R.drawable.ic_star_color));
                        increaseLikes(photo.getPhoto_id(),photo.getUser_id(),likes);
                        Log.d(TAG, "onClick: checked");
                    }
                    else{
                        btn_like.setBackgroundDrawable(getContext().getDrawable(R.drawable.ic_star_nocolor));
                        decreaseLikes(photo.getPhoto_id(),photo.getUser_id(),likes);
                        Log.d(TAG, "onClick: nochecked");
                    }
                }
            });
            relativeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rootLayout.removeViewAt(rootLayout.getChildCount()-1);
                    sPosts=ePosts;
                    ePosts+=5;
                    setViews();
                }
            });
            //Handle Likes and Dislikes
        }
    }
    // checkForPhotoLike(btn_like,photo.getPhoto_id());
    private void checkForPhotoLike(final ToggleButton btn_like, final String photo_id){
        Query query=myRef.child(getString(R.string.dbname_liked_photos)).child(user.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if(dataSnapshot.hasChild(photo_id)){
                    btn_like.setBackgroundDrawable(getContext().getDrawable(R.drawable.ic_star_color));
                    btn_like.setChecked(true);
                }
                else {
                    btn_like.setBackgroundDrawable(getContext().getDrawable(R.drawable.ic_star_nocolor));
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

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if(isVisibleToUser){
            try {
                getActivity().getWindow().setSoftInputMode(
                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
                ((HomeActivity)getActivity()).setupBottomNavigationView();
            }
            catch (NullPointerException e){
                Log.d(TAG, "setUserVisibleHint: "+e.getMessage());
            }
        }
    }
}
