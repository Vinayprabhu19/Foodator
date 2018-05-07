package vp19.foodator.Models;

public class Comment {
    String photo_id;
    String profile_pic;
    String comment;
    String username;
    public Comment(){

    }

    public Comment(String photo_id, String profile_pic, String comment, String username) {
        this.photo_id = photo_id;
        this.profile_pic = profile_pic;
        this.comment = comment;
        this.username = username;
    }

    public String getPhoto_id() {
        return photo_id;
    }

    public void setPhoto_id(String photo_id) {
        this.photo_id = photo_id;
    }

    public String getProfile_pic() {
        return profile_pic;
    }

    public void setProfile_pic(String profile_pic) {
        this.profile_pic = profile_pic;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
