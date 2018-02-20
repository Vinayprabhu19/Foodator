package vp19.foodator.utils;

import android.os.AsyncTask;
import android.util.Log;

import java.net.*;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
//JSON
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONString;

import vp19.foodator.Models.Restaurant;

public class ZomatoAPI extends AsyncTask<String,Void,ArrayList<Restaurant>>{
    private static final String TAG = "ZomatoAPI";
    private StringBuilder response = new StringBuilder();
    private String parameter;
    private ArrayList<Restaurant> restaurants;
    public ZomatoAPI(String query, double latitude, double longitude, double radius, int count, String sort, String order) {
        this.restaurants=new ArrayList<>();
        parameter="?q="+query+"&lat="+latitude+"&lon="+longitude+"&radius="+radius+"&count="+count+"&sort="+sort+"&order="+order;
    }
    @Override
    protected ArrayList<Restaurant> doInBackground(String... theURL) {
        try {
            String API_KEY = "a6fe33601f7b6c1fc4ac6c8a8ec90412";
            theURL[0]+=parameter;
            //Set the URL
            URL url = new URL(theURL[0]);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setDoInput(true);
            connection.setRequestProperty("user-key", API_KEY);
            connection.connect();
            int responseCode = connection.getResponseCode();
            Log.d(TAG, "doInBackground: code"+responseCode);
            //Get the response
            BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
            in.close();
            connection.disconnect();
            JSONObject jsonObj = new JSONObject(response.toString());
            //Get the json object in array form
            JSONObject object;
            JSONObject location;
            JSONObject rating;
            JSONArray array=jsonObj.getJSONArray("restaurants");
            for(int i=0;i<array.length();i++){
                Restaurant restaurant=new Restaurant();
                object=array.getJSONObject(i).getJSONObject("restaurant");
                location=object.getJSONObject("location");
                rating= object.getJSONObject("user_rating");
                restaurant.setName(object.getString("name"));
                restaurant.setRes_id(Integer.parseInt(object.getString("id")));
                restaurant.setFeatured_image(object.getString("featured_image"));
                restaurant.setLat(location.getDouble("latitude"));
                restaurant.setLon(location.getDouble("longitude"));
                restaurant.setRating((float)rating.getDouble("aggregate_rating"));
                restaurant.setRating_text(rating.getString("rating_text"));
                restaurant.setAddress(location.getString("address"));
                restaurants.add(restaurant);
            }
        }
        catch (Exception e){
            Log.d(TAG, "doInBackground: "+e.getMessage());
        }
        return restaurants;
    }

    @Override
    protected void onPostExecute(ArrayList<Restaurant> restaurants) {
        super.onPostExecute(restaurants);
        Log.d(TAG, "onPostExecute: "+restaurants.size());
    }

    public ArrayList<Restaurant> getRestaurants() {
        return restaurants;
    }
}






