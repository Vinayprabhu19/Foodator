package vp19.foodator.Food;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
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

import vp19.foodator.Home.HomeFragment;
import vp19.foodator.Home.LikesFragment;
import vp19.foodator.Home.SearchFragment;
import vp19.foodator.Models.Restaurant;
import vp19.foodator.Models.UserLocation;
import vp19.foodator.R;
import vp19.foodator.utils.SectionPagerAdapter;
import vp19.foodator.utils.UniversalImageLoader;
import vp19.foodator.utils.ZomatoAPI;

public class FoodActivity extends AppCompatActivity {
    private Context mContext=FoodActivity.this;
    private ViewPager viewPager;
    public String searchText;
    //fonts
    private Typeface font;
    private Typeface fUbuntuBold;
    private Typeface fUbuntuLight;
    private Typeface fUbuntuMedium;
    private Typeface fUbuntuRegular;
    private Typeface fUbuntuMono;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);
        hide(getWindow());
        init();
        setupViewPager();
    }

    /**
     *  Setup the viewpager for the fragments
     */
    private void setupViewPager(){
        SectionPagerAdapter adapter=new SectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragmet(new RestaurantFragment());
        adapter.addFragmet(new RecipeFragment());

        viewPager=findViewById(R.id.container);
        viewPager.setAdapter(adapter);
        viewPager.setCurrentItem(0);
        //Modify the tab layout
        TabLayout tabLayout=findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);

        //Get the individual tabs
        TabLayout.Tab tab1=tabLayout.getTabAt(0); // Restaurant
        TabLayout.Tab tab2=tabLayout.getTabAt(1); // Recipe

        //Set the icons
        tab1.setText("Restaurants");
        tab2.setText("Recipe");
    }
    private void init(){
        Intent intent=getIntent();
        searchText=intent.getStringExtra(getString(R.string.calling_activity));
        searchText=searchText.replace("#","");
        ImageView backArrow=findViewById(R.id.backArrow);
        TextView title=findViewById(R.id.pageTitle);
        TextView searchTextView=findViewById(R.id.searchText);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        font = Typeface.createFromAsset(mContext.getAssets(), "fonts/straight.ttf");
        fUbuntuBold = Typeface.createFromAsset(mContext.getAssets(), "fonts/Ubuntu-B.ttf");
        fUbuntuLight = Typeface.createFromAsset(mContext.getAssets(), "fonts/Ubuntu-L.ttf");
        fUbuntuMedium = Typeface.createFromAsset(mContext.getAssets(), "fonts/Ubuntu-M.ttf");
        fUbuntuRegular = Typeface.createFromAsset(mContext.getAssets(), "fonts/Ubuntu-R.ttf");
        fUbuntuMono = Typeface.createFromAsset(mContext.getAssets(), "fonts/UbuntuMono-B.ttf");
        title.setTypeface(fUbuntuRegular);
        searchTextView.setText(searchText);
        searchTextView.setTypeface(fUbuntuBold);
    }
    public static void hide(Window window){
        final View decorView = window.getDecorView();
        final int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);
        decorView.setOnSystemUiVisibilityChangeListener
                (new View.OnSystemUiVisibilityChangeListener() {
                    @Override
                    public void onSystemUiVisibilityChange(int visibility) {
                        // Note that system bars will only be "visible" if none of the
                        // LOW_PROFILE, HIDE_NAVIGATION, or FULLSCREEN flags are set.
                        if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                            decorView.setSystemUiVisibility(uiOptions);
                            // adjustments to your UI, such as showing the action bar or
                            // other navigational controls.
                        } else {
                            // adjustments to your UI, such as hiding the action bar or
                            // other navigational controls.
                        }
                    }
                });
    }
}
