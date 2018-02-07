package vp19.foodator.Models;

/**
 * Created by Vinay Prabhu on 07-Feb-18.
 */

public class Notification {
    private String message;

    public Notification(String message) {
        this.message = message;
    }

    public Notification() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "Notification{" +
                "message='" + message + '\'' +
                '}';
    }
}
