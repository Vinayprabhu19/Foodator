package vp19.foodator;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.ittianyu.bottomnavigationviewex.BottomNavigationViewEx;

import vp19.foodator.utils.BottomNavigationViewHelper;

public class LikesActivity extends AppCompatActivity {
    private Context mContext=LikesActivity.this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_likes);
    }
}
