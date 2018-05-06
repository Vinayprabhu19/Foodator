package vp19.foodator.Food;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import vp19.foodator.Models.Restaurant;
import vp19.foodator.Models.UserLocation;
import vp19.foodator.R;
import vp19.foodator.utils.UniversalImageLoader;
import vp19.foodator.utils.ZomatoAPI;
import vp19.foodator.utils.sortByLocation;

/**
 * Created by Vinay Prabhu on 18-Feb-18.
 */

public class RestaurantFragment extends Fragment {
    private static final String TAG = "RestaurantSearchActivit";
    private double mLatitude,mLongitude;
    private int l=0,h=10;
    //fonts
    private Typeface font;
    private Typeface fUbuntuBold;
    private Typeface fUbuntuLight;
    private Typeface fUbuntuMedium;
    private Typeface fUbuntuRegular;
    private Typeface fUbuntuMono;
    private LayoutInflater vi;
    private LinearLayout rootLayout;
    private String searchText;
    private LinearLayout progressLayout;
    //firebase authentication
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseUser user;
    private ArrayList<Restaurant> restaurants;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant,container,false);
        Log.d(TAG, "onCreateView: Restaurant Search activity initiated");
        initWidgets(view);
        queryViews();
        return view;
    }
    private void setViews(){
        int n=restaurants.size();
        int len;
        len=(n<h)?n:h;
        Log.d(TAG, "setViews: "+l + " " + h);
        for(int i=l;i<len;i++){
            View view=vi.inflate(R.layout.view_restaurant,null);
            ImageView image=view.findViewById(R.id.image);
            TextView name=view.findViewById(R.id.name);
            TextView rating=view.findViewById(R.id.rating);
            TextView address=view.findViewById(R.id.address);
            UniversalImageLoader.setImage(restaurants.get(i).getFeatured_image(),image,null,"");
            name.setTypeface(fUbuntuRegular);
            name.setText(restaurants.get(i).getName());
            rating.setText(Float.toString(restaurants.get(i).getRating()));
            address.setText(restaurants.get(i).getAddress());
            address.setTypeface(fUbuntuRegular);
            rootLayout.addView(view);
            final int r_no=i;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent=new Intent(getContext(),ViewRestaurantActivity.class);
                    intent.putExtra(getString(R.string.calling_activity),restaurants.get(r_no));
                    startActivity(intent);
                }
            });
        }
        if(len==h){
            RelativeLayout layout=(RelativeLayout)vi.inflate(R.layout.view_loadmore,null);
            rootLayout.addView(layout);
            rootLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    rootLayout.removeViewAt(rootLayout.getChildCount()-1);
                    l=h;
                    h+=10;
                    setViews();
                }
            });
        }
    }
    private void queryViews(){
        rootLayout.removeAllViews();
        rootLayout.addView(progressLayout);
        Query query=myRef.child(getString(R.string.dbname_user_locations)).child(user.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserLocation mLocation=dataSnapshot.getValue(UserLocation.class);
                restaurants.clear();
                mLatitude=mLocation.getLat();
                mLongitude=mLocation.getLon();
                try {
                    restaurants = new ZomatoAPI(searchText, mLatitude, mLongitude, 100, 100, "", "").execute(getString(R.string.zomato_search)).get();
                }
                catch (Exception e){
                    Log.d(TAG, "setViews: "+e.getMessage());
                }
               /* for(int i=0;i<restaurants.size();i++){
                    Restaurant r=restaurants.get(i);
                    restaurants.get(i).setDistance(getDistance(mLatitude,mLongitude,r.getLat(),r.getLon()));
                }*/
                //Collections.sort(restaurants,new sortByLocation());
                rootLayout.removeViewAt(0);
                setViews();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private double getDistance(final double lat1,final double lon1,final double lat2,final double lon2){
        Log.d(TAG, "getDistance: "+Math.sqrt(Math.pow((lat1-lat2),2)+Math.pow((lon1-lon2),2)));
        return Math.sqrt(Math.pow((lat1-lat2),2)+Math.pow((lon1-lon2),2));
    }
    private void initWidgets(View view){
        searchText=((FoodActivity)getActivity()).searchText;
        initImageLoader();
        vi = (LayoutInflater) getContext().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootLayout=view.findViewById(R.id.rootLayout);
        progressLayout=(LinearLayout)vi.inflate(R.layout.layout_progress,null);
        font = Typeface.createFromAsset(getContext().getAssets(), "fonts/straight.ttf");
        fUbuntuBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Ubuntu-B.ttf");
        fUbuntuLight = Typeface.createFromAsset(getContext().getAssets(), "fonts/Ubuntu-L.ttf");
        fUbuntuMedium = Typeface.createFromAsset(getContext().getAssets(), "fonts/Ubuntu-M.ttf");
        fUbuntuRegular = Typeface.createFromAsset(getContext().getAssets(), "fonts/Ubuntu-R.ttf");
        fUbuntuMono = Typeface.createFromAsset(getContext().getAssets(), "fonts/UbuntuMono-B.ttf");
        ImageView progressImage=progressLayout.findViewById(R.id.progressImage);
        TextView progressText=progressLayout.findViewById(R.id.progressText);
        setupFirebaseAuth();
        restaurants=new ArrayList<>();
        progressImage.setImageDrawable(ResourcesCompat.getDrawable(getResources(),R.drawable.hotel,null));
        progressText.setText(getString(R.string.string_searching_nearby));
        progressText.setTypeface(fUbuntuBold);
        final SwipeRefreshLayout refreshLayout=view.findViewById(R.id.refresh);
        refreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                l=0;
                h=10;
                queryViews();
                refreshLayout.setRefreshing(false);
            }
        });

    }
    private void initImageLoader(){
        UniversalImageLoader imageLoader=new UniversalImageLoader(getContext());
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }

    private void setupFirebaseAuth() throws  NullPointerException{
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase= FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();
        user=mAuth.getCurrentUser();
    }

}
