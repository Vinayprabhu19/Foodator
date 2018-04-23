package vp19.foodator.Models;

public class liked_photo {
    String photo_id;

    public liked_photo(String photo_id) {
        this.photo_id = photo_id;
    }
    public liked_photo(){

    }

    public String getPhoto_id() {
        return photo_id;
    }

    public void setPhoto_id(String photo_id) {
        this.photo_id = photo_id;
    }
}
