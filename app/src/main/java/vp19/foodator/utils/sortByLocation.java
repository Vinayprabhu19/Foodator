package vp19.foodator.utils;

import android.util.Log;

import java.util.Comparator;

import vp19.foodator.Models.Restaurant;

/**
 * Created by Vinay Prabhu on 20-Feb-18.
 */

public class sortByLocation implements Comparator<Restaurant> {
    private static final String TAG = "sortByLocation";

    @Override
    public int compare(final Restaurant place1, final Restaurant place2) {
        if(place1.getDistance() > place2.getDistance())
            return 1;
        return -1;
    }

}
