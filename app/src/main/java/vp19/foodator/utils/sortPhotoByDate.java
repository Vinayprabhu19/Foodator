package vp19.foodator.utils;

import java.util.Comparator;

import vp19.foodator.Models.Photo;

/**
 * Created by Vinay Prabhu on 09-Feb-18.
 */

public class sortPhotoByDate implements Comparator<Photo> {
    @Override
    public int compare(Photo o1, Photo o2) {
        return o2.getDate_created().compareTo(o1.getDate_created());
    }
}
