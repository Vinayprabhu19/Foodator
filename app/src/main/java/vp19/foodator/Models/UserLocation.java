package vp19.foodator.Models;

/**
 * Created by Vinay Prabhu on 13-Feb-18.
 */

public class UserLocation {
    double lat,lon;
    String timeStamp;


    public UserLocation(){

    }

    @Override
    public String toString() {
        return "UserLocation{" +
                "lat=" + lat +
                ", lon=" + lon +
                ", timeStamp='" + timeStamp + '\'' +
                '}';
    }

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLon() {
        return lon;
    }

    public void setLon(double lon) {
        this.lon = lon;
    }

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public UserLocation(double lat, double lon, String timeStamp) {

        this.lat = lat;
        this.lon = lon;
        this.timeStamp = timeStamp;
    }
}
