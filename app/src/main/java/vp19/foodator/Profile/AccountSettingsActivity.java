/**
 *  Name : AccountSettingsActivity
 *  Type : Activity
 *  ContentView : activity_accountsettings
 *  Authentication : Signed In users
 *  Purpose : To Control various fragments : Edit Profile , Sign Out
 */
package vp19.foodator.Profile;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import java.util.ArrayList;

import vp19.foodator.R;
import vp19.foodator.utils.BottomNavigationViewHelper;
import vp19.foodator.utils.FirebaseMethods;
import vp19.foodator.utils.SectionsStatePagerAdapter;
public class AccountSettingsActivity extends AppCompatActivity {
    private static final String TAG = "AccountSettingsActivity";
    Context mContext=AccountSettingsActivity.this;
    public SectionsStatePagerAdapter pagerAdapter;
    public ViewPager mViewPager;
    public RelativeLayout mRelativeLayout;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accountsettings);
        mViewPager=findViewById(R.id.container);
        mRelativeLayout=findViewById(R.id.relLayout1);
        setupFragments();
        setupBottomNavigationView();
        transferControl();
        setupSettingsList();
        //Setup Backarrow
        ImageView backarrow=findViewById(R.id.backArrow);
        backarrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    /**
     *  Checks if the intent has arrived from Edit Profile button or through the settings
     */
    private void transferControl(){
        Intent intent=getIntent();
        //If the call is from ProfileActivity
        if(intent.hasExtra(getString(R.string.calling_activity))){
            Log.d(TAG, "transferControl: ");
            try {
                setViewPager(0);
            }
            catch (NullPointerException e)
            {
                Log.d(TAG, "transferControl:Error Setting Viewpager "+e.getMessage());
            }
            catch (IllegalStateException e){
                Log.d(TAG, "transferControl: Illegal State Exception");
            }
        }
        //If the call is from Account Settings
       else if(intent.hasExtra(getString(R.string.return_to_fragment))){
            FirebaseMethods firebaseMethods=new FirebaseMethods(mContext);
            String imgUrl=intent.getStringExtra(getString(R.string.selected_image));
            firebaseMethods.uploadImage("Profile Pic","",0,imgUrl,null,false);
            finish();
        }
    }

    /**
     * Setup fragments for Section State Pager Adapter
     */
    public void setupFragments(){
        pagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        pagerAdapter.addFragment(new EditProfileFragment(),getString(R.string.edit_profile)); //fragment 0
        pagerAdapter.addFragment(new SignOutFragment(),getString(R.string.sign_out)); //fragment 1
    }

    /**
     * Sets the view pager to specified fragment
     * @param fragmentNumber : The fragment number of the page to be set for viewpager :
     *                       0 : Edit Profile
     *                       1 : Sign Out
     */
    public void setViewPager(int fragmentNumber){
        mRelativeLayout.setVisibility(View.GONE);
        mViewPager.setAdapter(pagerAdapter);
        mViewPager.setCurrentItem(fragmentNumber);
    }
    /**
     *  Setup the List view to be displayed on the relative layout and to sense the item that is clicked
     */
    private void setupSettingsList(){
        ListView listView=(ListView)findViewById(R.id.lvAccountSettings);
        ArrayList<String> list=new ArrayList<>();
        list.add(getString(R.string.edit_profile));
        list.add(getString(R.string.sign_out));
        ArrayAdapter arrayAdapter=new ArrayAdapter(mContext,android.R.layout.simple_list_item_1,list);
        listView.setAdapter(arrayAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                setViewPager(position);
            }
        });
    }
    private void setupBottomNavigationView(){
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx)findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx,this.getWindow());
        BottomNavigationViewHelper.enableNavgation(mContext,bottomNavigationViewEx);
        Menu menu=bottomNavigationViewEx.getMenu();
        MenuItem menuItem=menu.getItem(2);
        menuItem.setChecked(true);
        BottomNavigationViewHelper.setIcon(menuItem,2);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        setupBottomNavigationView();
    }
}
