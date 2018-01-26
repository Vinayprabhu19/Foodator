/**
 *  Model for User table
 */
package vp19.foodator.Models;
public class User {
    private String user_id;
    private String user_name;
    private String email;
    private long phone_num;

    public User(String user_id, String user_name, String email, long phone_num) {
        this.user_id = user_id;
        this.user_name = user_name;
        this.email = email;
        this.phone_num = phone_num;
    }

    public User(){

    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getUser_name() {
        return user_name;
    }

    public void setUser_name(String user_name) {
        this.user_name = user_name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public long getPhone_num() {
        return phone_num;
    }

    public void setPhone_num(long phone_num) {
        this.phone_num = phone_num;
    }

    @Override
    public String toString() {
        return "User{" +
                "user_id='" + user_id + '\'' +
                ", user_name='" + user_name + '\'' +
                ", email='" + email + '\'' +
                ", phone_num=" + phone_num +
                '}';
    }
}
