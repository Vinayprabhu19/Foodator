package vp19.foodator.Share;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import vp19.foodator.R;
import vp19.foodator.utils.BottomNavigationViewHelper;
import vp19.foodator.utils.Permissions;
import vp19.foodator.utils.SectionPagerAdapter;

public class ShareActvity extends AppCompatActivity {
    private static final String TAG = "ShareActvity";
    private int ACTIVITY_NUM=1;
    private Context mContext=ShareActvity.this;
    private final int VERIFY_PERMISSION_REQUEST=1;
    private ViewPager mViewPager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);
        setupBottomNavigationView();
        //The code
        if(checkPermissionsArray(Permissions.PERMISSIONS)){
            setupViewPager();
        }
        else{
            verifyPermissions(Permissions.PERMISSIONS);
        }
    }
    public int getTask(){
        return getIntent().getFlags();
    }
    public int getCurrentItemNumber(){
        return mViewPager.getCurrentItem();
    }
    private void setupViewPager(){
        SectionPagerAdapter adapter=new SectionPagerAdapter(getSupportFragmentManager());
        adapter.addFragmet(new GalleryFragment());
        adapter.addFragmet(new PhotoFragment());
        mViewPager=findViewById(R.id.container);
        mViewPager.setAdapter(adapter);
        TabLayout tabLayout=findViewById(R.id.tabsBottom);
        tabLayout.setupWithViewPager(mViewPager);
        tabLayout.getTabAt(0).setText((getString(R.string.galley)));
        tabLayout.getTabAt(1).setText((getString(R.string.photo)));
    }
    /**
     * Verifiy all the permissions
     * @param Permissions
     */
    public void verifyPermissions(String[] Permissions){
        ActivityCompat.requestPermissions(
                ShareActvity.this,
                Permissions,
                VERIFY_PERMISSION_REQUEST

        );
    }
    public boolean checkPermissionsArray(String[] Permissions){
        for(int i=0;i<Permissions.length;i++){
            String check=Permissions[i];
            if(!checkPermission(check)){
                return false;
            }
        }
        return true;
    }
    public boolean checkPermission(String permission){
        int permissionRequest= ActivityCompat.checkSelfPermission(mContext,permission);
        if(permissionRequest != PackageManager.PERMISSION_GRANTED){
            Log.d(TAG, "checkPermission: Permission Not Granted for "+permission);
            return false;
        }
        else {
            Log.d(TAG, "checkPermission: Permission Granted for "+permission);
            return true;
        }
    }
    public void setupBottomNavigationView(){
        BottomNavigationViewEx bottomNavigationViewEx = (BottomNavigationViewEx)findViewById(R.id.bottomNavViewBar);
        BottomNavigationViewHelper.setupBottomNavigationView(bottomNavigationViewEx,this.getWindow());
        BottomNavigationViewHelper.enableNavgation(mContext,bottomNavigationViewEx);
        Menu menu=bottomNavigationViewEx.getMenu();
        MenuItem menuItem=menu.getItem(ACTIVITY_NUM);
        menuItem.setChecked(true);
        BottomNavigationViewHelper.setIcon(menuItem,ACTIVITY_NUM);
    }
    @Override
    protected void onPostResume() {
        super.onPostResume();
        setupBottomNavigationView();
    }
}
