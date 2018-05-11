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

import vp19.foodator.R;

import static com.nostra13.universalimageloader.core.ImageLoader.TAG;

/**
 * Created by Vinay Prabhu on 18-Feb-18.
 */

public class RecipeFragment extends Fragment {
    private String searchText;
    //fonts
    private Typeface font;
    private Typeface fUbuntuBold;
    private Typeface fUbuntuLight;
    private Typeface fUbuntuMedium;
    private Typeface fUbuntuRegular;
    private Typeface fUbuntuMono;
    //firebase authentication
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference myRef;
    private FirebaseUser user;
    //widgets
    private LayoutInflater vi;
    private LinearLayout rootLayout;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe,container,false);
        try {
            setupFirebaseAuth();
            init(view);
        }
        catch (NullPointerException e){
            Log.d(TAG, "onCreateView: "+e.getMessage());
        }
        return view;
    }
    private void IngredientsView(){
        rootLayout.removeAllViews();
        final TextView ingredients=(TextView)vi.inflate(R.layout.view_recipe,null);
        ingredients.setText(R.string.string_ingredients);
        ingredients.setTypeface(fUbuntuBold);
        ingredients.setTextSize(20);
        ingredients.setBackgroundColor(getContext().getColor(R.color.dark_orange));
        rootLayout.addView(ingredients);
        Query query=myRef.child(getString(R.string.dbname_recipe)).child(searchText).child(getString(R.string.attr_ingredients));
        try {
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds:dataSnapshot.getChildren()){
                        TextView tv=(TextView)vi.inflate(R.layout.view_recipe,null);
                        tv.setText(ds.getValue().toString());
                        rootLayout.addView(tv);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        catch (Exception e){
            Log.d(TAG, "IngredientsView: "+e.getMessage());
        }
        final TextView steps=(TextView)vi.inflate(R.layout.view_recipe,null);
        steps.setText(R.string.string_steps);
        steps.setTypeface(fUbuntuBold);
        steps.setTextSize(20);
        steps.setBackgroundColor(getContext().getColor(R.color.green));
        rootLayout.addView(steps);
        ingredients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IngredientsView();
            }
        });
        steps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StepsView();
            }
        });
    }
    private void StepsView()throws NullPointerException{
        rootLayout.removeAllViews();
        final TextView ingredients=(TextView)vi.inflate(R.layout.view_recipe,null);
        ingredients.setText(R.string.string_ingredients);
        ingredients.setTypeface(fUbuntuBold);
        ingredients.setTextSize(20);
        ingredients.setBackgroundColor(getContext().getColor(R.color.green));
        rootLayout.addView(ingredients);
        Query query=myRef.child(getString(R.string.dbname_recipe)).child(searchText).child(getString(R.string.attr_steps));
        try {
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    for (DataSnapshot ds:dataSnapshot.getChildren()){
                        TextView tv=(TextView)vi.inflate(R.layout.view_recipe,null);
                        tv.setText(ds.getValue().toString());
                        rootLayout.addView(tv);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
        catch (Exception e){
            Log.d(TAG, "IngredientsView: "+e.getMessage());
        }
        final TextView steps=(TextView)vi.inflate(R.layout.view_recipe,null);
        steps.setText(R.string.string_steps);
        steps.setTypeface(fUbuntuBold);
        steps.setTextSize(20);
        steps.setBackgroundColor(getContext().getColor(R.color.dark_orange));
        rootLayout.addView(steps);
        ingredients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IngredientsView();
            }
        });
        steps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StepsView();
            }
        });
    }
    private void init(View view) throws NullPointerException{
        searchText=((FoodActivity)getActivity()).searchText;
        searchText.replaceAll(" ","_");
        vi = (LayoutInflater) getContext().getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        rootLayout=view.findViewById(R.id.rootLayout);
        fUbuntuBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Ubuntu-B.ttf");
        fUbuntuLight = Typeface.createFromAsset(getContext().getAssets(), "fonts/Ubuntu-L.ttf");
        fUbuntuMedium = Typeface.createFromAsset(getContext().getAssets(), "fonts/Ubuntu-M.ttf");
        fUbuntuRegular = Typeface.createFromAsset(getContext().getAssets(), "fonts/Ubuntu-R.ttf");
        fUbuntuMono = Typeface.createFromAsset(getContext().getAssets(), "fonts/UbuntuMono-B.ttf");
        TextView ingredients=(TextView)vi.inflate(R.layout.view_recipe,null);
        ingredients.setTypeface(fUbuntuBold);
        ingredients.setTextSize(20);
        TextView steps=(TextView)vi.inflate(R.layout.view_recipe,null);
        steps.setTypeface(fUbuntuBold);
        steps.setTextSize(20);
        ingredients.setText(R.string.string_ingredients);
        steps.setText(R.string.string_steps);
        ingredients.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IngredientsView();
            }
        });
        steps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StepsView();
            }
        });
        rootLayout.addView(ingredients);
        rootLayout.addView(steps);
    }
    private void setupFirebaseAuth() throws  NullPointerException{
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase= FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();
        user=mAuth.getCurrentUser();
    }
}
