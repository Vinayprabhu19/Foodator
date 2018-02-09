package vp19.foodator.Models;

/**
 * Model for the notification
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
