/**
 *  Name : SearchFragment
 *  Type : Fragment
 *  ContentView : fragment_search
 *  Authentication : Signed In users
 *  Purpose : To search for users,tags,posts
 */
package vp19.foodator.Home;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import vp19.foodator.Food.FoodActivity;
import vp19.foodator.Models.Photo;
import vp19.foodator.Models.UserAccountSettings;
import vp19.foodator.Profile.ProfileActivity;
import vp19.foodator.Profile.ShowImageActivity;
import vp19.foodator.Profile.UserProfileActivity;
import vp19.foodator.R;
import vp19.foodator.utils.GridImageAdapter;
import vp19.foodator.utils.StringManipulation;
import vp19.foodator.utils.UniversalImageLoader;

public class SearchFragment extends Fragment {
    private static final String TAG = "SearchFragment";
    //fonts
    private Typeface dudu;
    private Typeface straight;
    private Typeface fUbuntuBold;
    private Typeface fUbuntuLight;
    private Typeface fUbuntuMedium;
    private Typeface fUbuntuRegular;
    private Typeface fUbuntuMono;
    private String myUserID;
    //Widgets
    private EditText Search;
    private ImageView btn_search;
    private String textSearch;
    private LinearLayout rootLayout;
    private TextView errorText;
    private LayoutInflater vi;
    //firebase authentication
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseUser user;
    //maps
    MapView mapView;
    GoogleMap map;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search,container,false);
        try{
            init(view);
        }
        catch (NullPointerException e){
            Log.d(TAG, "NullPointerException "+e.getMessage());
        }
        return view;
    }

    /**
     * Init various widgets and instnace variables
     * @param view
     * @throws NullPointerException
     */
    private void init(View view) throws  NullPointerException{
        initImageLoader();
        dudu= Typeface.createFromAsset(getContext().getAssets(), "fonts/dudu.ttf");
        straight= Typeface.createFromAsset(getContext().getAssets(), "fonts/straight.ttf");
        fUbuntuBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Ubuntu-B.ttf");
        fUbuntuLight = Typeface.createFromAsset(getContext().getAssets(), "fonts/Ubuntu-L.ttf");
        fUbuntuMedium = Typeface.createFromAsset(getContext().getAssets(), "fonts/Ubuntu-M.ttf");
        fUbuntuRegular = Typeface.createFromAsset(getContext().getAssets(), "fonts/Ubuntu-R.ttf");
        fUbuntuMono = Typeface.createFromAsset(getContext().getAssets(), "fonts/UbuntuMono-B.ttf");
        vi = (LayoutInflater) getContext().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Search=view.findViewById(R.id.textSearch);
        btn_search=view.findViewById(R.id.btn_search);
        rootLayout=view.findViewById(R.id.rootLayout);
        errorText=view.findViewById(R.id.textError);
        errorText.setVisibility(View.GONE);
        errorText.setTypeface(dudu);
        Search.setTypeface(fUbuntuRegular);
        setupFirebaseAuth();
        Search.setImeOptions(EditorInfo.IME_ACTION_SEARCH);
        Search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    textSearch=Search.getText().toString();
                    processSearchText();
                    return true;
                }
                return false;
            }
        });
        //Handle search button
        btn_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textSearch=Search.getText().toString();
                processSearchText();
            }
        });
    }
    private void initImageLoader(){
        UniversalImageLoader imageLoader=new UniversalImageLoader(getContext());
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }

    /**
     * Process the text entered in the search bar
     */
    private void processSearchText(){
        rootLayout.removeAllViews();
        errorText.setVisibility(View.GONE);
        //Hotspot
        if(StringManipulation.isStringNull(textSearch)){
            Fragment childFragment = new MapFragment();
            FragmentTransaction transaction = getChildFragmentManager().beginTransaction();
            transaction.replace(R.id.rootLayout, childFragment).commit();
        }
        //If the search is a hash tag
        else if(textSearch.startsWith("#")){
            handleHashTagQuery();
        }
        //Recipe
        else if(textSearch.startsWith("$")){

        }
        //Profile Query
        else{
            handleProfileQuery();
        }
    }

    /**
     * Handle hash tag query
     * @throws NullPointerException
     */
    private void handleHashTagQuery() throws NullPointerException{
        final ArrayList<Photo> photoList=new ArrayList<>();
        Query query=myRef.child(getString(R.string.dbname_photos));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    Photo photo=ds.getValue(Photo.class);
                    String tag=photo.getTags();
                    textSearch=textSearch.toLowerCase();
                    tag=tag.toLowerCase();
                    if(tag.contains(textSearch)){
                        photoList.add(photo);
                    }
                }
                setTagViews(photoList);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Set the gridview with the images
     * @param photoList : List of the photos to be set
     */
    private void setTagViews(final ArrayList<Photo> photoList){
        View tagView=vi.inflate(R.layout.view_tag_link,null,true);
        Button button=tagView.findViewById(R.id.searchRestaurant);
        button.setTypeface(fUbuntuBold);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent=new Intent(getActivity(),FoodActivity.class);
                intent.putExtra(getString(R.string.calling_activity),textSearch);
                startActivity(intent);
            }
        });
        rootLayout.addView(tagView);
        if(photoList.size() == 0)
            return;
        //Get widgets
        View view = vi.inflate(R.layout.view_gridview,null,true);
        ImageView image=view.findViewById(R.id.image1);
        GridView gridView = view.findViewById(R.id.gridView);
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth = gridWidth/3;
        final ArrayList<String> imgURLs=new ArrayList<>();
        if(photoList.size()>0) {
            final Photo photo=photoList.get(0);
            UniversalImageLoader.setImage(photo.getImage_path(), image, null, "");
            image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(getContext(),ShowImageActivity.class);
                    intent.putExtra(getString(R.string.selected_image),photo.getImage_path());
                    intent.putExtra(getString(R.string.photo_id),photo.getPhoto_id());
                    intent.putExtra(getString(R.string.attr_user_id),photo.getUser_id());
                    startActivity(intent);
                }
            });
        }
        photoList.add(photoList.get(0));
        photoList.remove(0);
        for(int i=0;i<photoList.size();i++)
            imgURLs.add(photoList.get(i).getImage_path());
        gridView.setColumnWidth(imageWidth);
        GridImageAdapter adapter= new GridImageAdapter(getContext(), R.layout.layout_grid_imageview, "", imgURLs);
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(getContext(),ShowImageActivity.class);
                intent.putExtra(getString(R.string.selected_image),imgURLs.get(position));
                intent.putExtra(getString(R.string.photo_id),photoList.get(position).getPhoto_id());
                intent.putExtra(getString(R.string.attr_user_id),photoList.get(position).getUser_id());
                startActivity(intent);
            }
        });
        rootLayout.addView(view);
    }

    /**
     * Handle profile query
     * @throws NullPointerException
     */
    private void handleProfileQuery() throws  NullPointerException{
        final ArrayList<UserAccountSettings> users=new ArrayList<>();
        final ArrayList<String> userIDs=new ArrayList<>();
        Query query=myRef.child(getString(R.string.dbname_user_account_settings));
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot ds:dataSnapshot.getChildren()){
                    UserAccountSettings user=ds.getValue(UserAccountSettings.class);
                    String username=user.getUsername();
                    username=username.toLowerCase();
                    Log.d(TAG, "handleProfileQuery "+username);
                    if(username.contains(textSearch)){
                        Log.d(TAG, "handleProfileQuery : has username ");
                        users.add(user);
                        userIDs.add(ds.getKey());
                    }
                }
                setProfileViews(users,userIDs);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Set the views for the profile
     * @param users : result of the users from the query
     * @param userIDs : userIDs of the users
     * @throws NullPointerException
     */
    private void setProfileViews(ArrayList<UserAccountSettings> users , final ArrayList<String> userIDs) throws  NullPointerException{
        //Set the layout
        if(users.size() == 0){
            errorText.setVisibility(View.VISIBLE);
        }
        else{
            for(int i=0;i<users.size();i++){
                final UserAccountSettings user=users.get(i);
                final String userID=userIDs.get(i);
                View view = vi.inflate(R.layout.view_search_result,null);
                ImageView profileImage=view.findViewById(R.id.profileImage);
                TextView userName=view.findViewById(R.id.username);
                UniversalImageLoader.setImage(user.getProfile_photo(),profileImage,null,"");
                userName.setText(user.getUsername());
                userName.setTypeface(fUbuntuRegular);
                rootLayout.addView(view);
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
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
            }
        }
    }
    /**
     * Setting up Firebase Authentication
     */
    private void setupFirebaseAuth() throws  NullPointerException{
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

    /**
     * See if the fragment is set in the viewpager
     * @param isVisibleToUser : true if set ,else false
     */
    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint: "+isVisibleToUser);
        if(isVisibleToUser){
            HomeActivity activity=((HomeActivity)getActivity());
            if(!StringManipulation.isStringNull(activity.searchString)){
                textSearch=activity.searchString;
                activity.searchString="";
                Search.setText(textSearch);
                Log.d(TAG, "Search "+textSearch);
                processSearchText();
            }
        }
    }
}
