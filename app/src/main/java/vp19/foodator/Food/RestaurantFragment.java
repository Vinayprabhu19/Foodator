package vp19.foodator.Food;

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
import android.view.Window;
import android.widget.ImageView;
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
import com.nostra13.universalimageloader.core.ImageLoader;

import java.util.ArrayList;

import vp19.foodator.Models.Restaurant;
import vp19.foodator.Models.UserLocation;
import vp19.foodator.R;
import vp19.foodator.utils.UniversalImageLoader;
import vp19.foodator.utils.ZomatoAPI;

/**
 * Created by Vinay Prabhu on 18-Feb-18.
 */

public class RestaurantFragment extends Fragment {
    private static final String TAG = "RestaurantSearchActivit";
    private double mLatitude,mLongitude;
    Typeface font;
    LayoutInflater vi;
    LinearLayout rootLayout;
    //firebase authentication
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseUser user;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_restaurant,container,false);
        initWidgets(view);
        queryViews();
        return view;
    }
    private void setViews(){
        ArrayList<Restaurant> restaurants=new ArrayList<>();
        try {
            restaurants = new ZomatoAPI("burger", mLatitude, mLongitude, 100, 100, "", "").execute(getString(R.string.zomato_search)).get();
            Log.d(TAG, "restSize "+restaurants.size());
        }
        catch (Exception e){
            Log.d(TAG, "setViews: "+e.getMessage());
        }
        Log.d(TAG, "setViews: "+restaurants.size());
        for(int i=0;i<restaurants.size();i++){
            View view=vi.inflate(R.layout.view_restaurant,null);
            ImageView image=view.findViewById(R.id.image);
            TextView name=view.findViewById(R.id.name);
            TextView rating=view.findViewById(R.id.rating);
            TextView address=view.findViewById(R.id.address);
            UniversalImageLoader.setImage(restaurants.get(i).getFeatured_image(),image,null,"");
            name.setTypeface(font);
            name.setText(restaurants.get(i).getName());
            rating.setText(Float.toString(restaurants.get(i).getRating()));
            address.setText(restaurants.get(i).getAddress());
            rootLayout.addView(view);
        }

    }
    private void queryViews(){
        Query query=myRef.child(getString(R.string.dbname_user_locations)).child(user.getUid());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                UserLocation mLocation=dataSnapshot.getValue(UserLocation.class);
                mLatitude=mLocation.getLat();
                mLongitude=mLocation.getLon();
                setViews();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void initWidgets(View view){
        initImageLoader();
        vi = (LayoutInflater) getContext().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootLayout=view.findViewById(R.id.rootLayout);
        font = Typeface.createFromAsset(getContext().getAssets(), "fonts/straight.ttf");
        setupFirebaseAuth();

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
