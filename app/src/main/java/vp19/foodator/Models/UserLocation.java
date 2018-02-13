package vp19.foodator.Models;

/**
 * Created by Vinay Prabhu on 13-Feb-18.
 */

public class UserLocation {
    float X,Y;
    String timeStamp;

    public UserLocation(float x, float y, String timeStamp) {
        X = x;
        Y = y;
        this.timeStamp = timeStamp;
    }
    public UserLocation(){

    }

    public float getX() {
        return X;
    }

    public void setX(float x) {
        X = x;
    }

    public float getY() {
        return Y;
    }

    public void setY(float y) {
        Y = y;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }
}
