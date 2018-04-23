package vp19.foodator.utils.Threads;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.IOException;
import java.net.URL;

public class NetworkThread extends Thread {
    private Bitmap bitmap;
    private String imageURL;
    public  NetworkThread(String imgURL){
        super("Network Thread");
        this.imageURL=imgURL;
    }

    public Bitmap getBitmap() {
        return bitmap;
    }

    @Override
    public void run() {
        try {
            URL url = new URL(imageURL);
            bitmap = BitmapFactory.decodeStream(url.openConnection().getInputStream());
        }
        catch (IOException e){

        }
    }
}
