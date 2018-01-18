package vp19.foodator.Login;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import vp19.foodator.R;

/**
 * Created by Vinay Prabhu on 18-Jan-18.
 */

public class RegisterActivity extends AppCompatActivity{
    private static final String TAG = "RegisterActvity";
    private Context mContext=RegisterActivity.this;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

    }
}
