package vp19.foodator.Models;

public class Restaurant {
    private int res_id;
    private String name;
    private double lat;
    private double lon;
    private float rating;
    private String rating_text;
    private String featured_image;
    private String address;
    private double distance;
    public Restaurant(int res_id, String name, double lat, double lon, float rating, String rating_text, String featured_image, String address) {
        this.res_id = res_id;
        this.name = name;
        this.lat = lat;
        this.lon = lon;
        this.rating = rating;
        this.rating_text = rating_text;
        this.featured_image = featured_image;
        this.address = address;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Restaurant(){

    }
    @Override
    public String toString() {
        return "Restaurant{" +
                "res_id=" + res_id +
                ", name='" + name + '\'' +
                ", lat=" + lat +
                ", lon=" + lon +
                ", rating=" + rating +
                ", rating_text='" + rating_text + '\'' +
                ", featured_image='" + featured_image + '\'' +
                '}';
    }

    public int getRes_id() {
        return res_id;
    }

    public void setRes_id(int res_id) {
        this.res_id = res_id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getRating_text() {
        return rating_text;
    }

    public void setRating_text(String rating_text) {
        this.rating_text = rating_text;
    }

    public String getFeatured_image() {
        return featured_image;
    }

    public void setFeatured_image(String featured_image) {
        this.featured_image = featured_image;
    }
}
