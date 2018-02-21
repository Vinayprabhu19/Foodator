package vp19.foodator.Food;

import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import vp19.foodator.Models.Restaurant;
import vp19.foodator.R;

import static java.security.AccessController.getContext;

public class ViewRestaurantActivity extends AppCompatActivity {
    private Restaurant restaurant;
    private Context mContext=ViewRestaurantActivity.this;
    //fonts
    private Typeface fUbuntuBold;
    private Typeface fUbuntuLight;
    private Typeface fUbuntuMedium;
    private Typeface fUbuntuRegular;
    private Typeface fUbuntuMono;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_restaurant);
        initWidgets();
    }
    private void initWidgets(){
        fUbuntuBold = Typeface.createFromAsset(mContext.getAssets(), "fonts/Ubuntu-B.ttf");
        fUbuntuLight = Typeface.createFromAsset(mContext.getAssets(), "fonts/Ubuntu-L.ttf");
        fUbuntuMedium = Typeface.createFromAsset(mContext.getAssets(), "fonts/Ubuntu-M.ttf");
        fUbuntuRegular = Typeface.createFromAsset(mContext.getAssets(), "fonts/Ubuntu-R.ttf");
        fUbuntuMono = Typeface.createFromAsset(mContext.getAssets(), "fonts/UbuntuMono-B.ttf");
        Intent intent=getIntent();
        restaurant=(Restaurant)intent.getSerializableExtra(getString(R.string.calling_activity)); 
        ImageView backArrow=findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        TextView pageTitle=findViewById(R.id.pageTitle);
        TextView restaurantName=findViewById(R.id.restaurantName);
        TextView rating=findViewById(R.id.rating);
        TextView ratingText=findViewById(R.id.ratingText);
        restaurantName.setText(restaurant.getName());
        rating.setText(Float.toString(restaurant.getRating()));
        ratingText.setText(restaurant.getRating_text());
        restaurantName.setTypeface(fUbuntuBold);
        rating.setTypeface(fUbuntuBold);
        ratingText.setTypeface(fUbuntuRegular);
        pageTitle.setText("Restaurant");
        pageTitle.setTypeface(fUbuntuMedium);
    }
}
