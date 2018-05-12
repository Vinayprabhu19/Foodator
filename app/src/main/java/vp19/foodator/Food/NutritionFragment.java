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

import vp19.foodator.Models.Nutrition;
import vp19.foodator.R;
public class NutritionFragment extends Fragment {
    private String searchText;
    private static final String TAG = "NutritionFragment";
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
    private TextView calorieText;
    private TextView calorieValue;
    private TextView carbsText;
    private TextView carbsValue;
    private TextView fatText;
    private TextView fatValue;
    private TextView proteinText;
    private TextView proteinValue;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_nutrition,container,false);
        try {
            setupFirebaseAuth();
            init(view);
            setValues();
        }
        catch (NullPointerException e){
            Log.d(TAG, "onCreateView: "+e.getMessage());
        }
        return view;
    }
    private void setValues()throws NullPointerException{
        Query query=myRef.child(getString(R.string.dbname_nutrition)).child(searchText);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                try {
                    Nutrition nutrition = dataSnapshot.getValue(Nutrition.class);
                    calorieValue.setText(nutrition.getCalories());
                    carbsValue.setText(nutrition.getCarbs());
                    fatValue.setText(nutrition.getFat());
                    proteinValue.setText(nutrition.getProtein());
                }
                catch (NullPointerException e){
                    Log.d(TAG, "onDataChange: "+e.getMessage());
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
    private void init(View view){
        searchText=((FoodActivity)getActivity()).searchText;
        searchText.replaceAll(" ","_");
        fUbuntuBold = Typeface.createFromAsset(getContext().getAssets(), "fonts/Ubuntu-B.ttf");
        fUbuntuLight = Typeface.createFromAsset(getContext().getAssets(), "fonts/Ubuntu-L.ttf");
        fUbuntuMedium = Typeface.createFromAsset(getContext().getAssets(), "fonts/Ubuntu-M.ttf");
        fUbuntuRegular = Typeface.createFromAsset(getContext().getAssets(), "fonts/Ubuntu-R.ttf");
        fUbuntuMono = Typeface.createFromAsset(getContext().getAssets(), "fonts/UbuntuMono-B.ttf");
        //Widgets
        calorieText=view.findViewById(R.id.caloriesTitle);
        calorieValue=view.findViewById(R.id.calorieTv);
        carbsText=view.findViewById(R.id.carbsTitle);
        carbsValue=view.findViewById(R.id.carbsTv);
        fatText=view.findViewById(R.id.fatTitle);
        fatValue=view.findViewById(R.id.fatTv);
        proteinText=view.findViewById(R.id.proteinTitle);
        proteinValue=view.findViewById(R.id.protienTv);
        //set typeface
        calorieText.setTypeface(fUbuntuBold);
        carbsText.setTypeface(fUbuntuBold);
        fatText.setTypeface(fUbuntuBold);
        proteinText.setTypeface(fUbuntuBold);
        calorieValue.setTypeface(fUbuntuRegular);
        carbsValue.setTypeface(fUbuntuRegular);
        fatValue.setTypeface(fUbuntuRegular);
        proteinValue.setTypeface(fUbuntuRegular);
    }
    private void setupFirebaseAuth() throws  NullPointerException{
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase= FirebaseDatabase.getInstance();
        myRef=mFirebaseDatabase.getReference();
        user=mAuth.getCurrentUser();
    }
}
