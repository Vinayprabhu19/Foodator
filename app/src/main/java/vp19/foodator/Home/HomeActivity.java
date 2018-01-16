package vp19.foodator.Home;

import android.content.Context;
import android.graphics.Typeface;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import vp19.foodator.R;
import vp19.foodator.utils.BottomNavigationViewHelper;

public class HomeActivity extends AppCompatActivity {
    private static final String TAG = "HomeActivity";
    private Context mContext=HomeActivity.this;
    private int ACTIVITY_NUM=0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        setupBottomNavigationView();
        setupViewPager();
    }
    /**
     * Responsible for adding 3 tabs : Logo_text , Likes , Search
     */
    private void setupViewPager(){
        SectionPagerAdapter adapter=new SectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragmet(new HomeFragment());
        adapter.addFragmet(new SearchFragment());
        adapter.addFragmet(new LikesFragment());
        ViewPager viewPager=(ViewPager) findViewById(R.id.container);
        viewPager.setAdapter(adapter);

        //Modify the tab layout
        TabLayout tabLayout=(TabLayout)findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        tabLayout.setSelectedTabIndicatorHeight(0);

        //Get the individual tabs
        TabLayout.Tab tab1=tabLayout.getTabAt(0); // Logo
        TabLayout.Tab tab2=tabLayout.getTabAt(1); // Search
        TabLayout.Tab tab3=tabLayout.getTabAt(2); // Likes

        //Get custom view for first tab
        LinearLayout tabOne = (LinearLayout) LayoutInflater.from(this).inflate(R.layout.logo_text_tab, null);
        TextView textView=(TextView)tabOne.getChildAt(1);
        Typeface typeface = Typeface.createFromAsset(getAssets(), "fonts/berlin_sans.ttf");
        textView.setTypeface(typeface);
        tab1.setCustomView(tabOne);
        tab2.setIcon(R.drawable.ic_search);
        tab3.setIcon(R.drawable.ic_likes);


    }
    private void setupBottomNavigationView(){
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx)findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx,this.getWindow());
        BottomNavigationViewHelper.enableNavgation(mContext,bottomNavigationViewEx);
        Menu menu=bottomNavigationViewEx.getMenu();
        MenuItem menuItem=menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
    }
}
