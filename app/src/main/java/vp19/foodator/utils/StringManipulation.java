package vp19.foodator.utils;

/**
 * Created by Vinay Prabhu on 19-Jan-18.
 */

public class StringManipulation {
    public static String expandUserName(String username){
        return username.replace("."," ");
    }
    public static String condenseUsername(String username){
        return username.replace(" ",".");
    }
}
