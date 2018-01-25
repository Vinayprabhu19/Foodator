package vp19.foodator.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by Vinay Prabhu on 24-Jan-18.
 */

public class ImageManager {
    private static final String TAG = "ImageManager";
    public static Bitmap getBitmap(String url){
        File file=new File(url);
        FileInputStream fis=null;
        Bitmap bitmap=null;
        try {
            fis=new FileInputStream(url);
            bitmap= BitmapFactory.decodeStream(fis);
        }
        catch (FileNotFoundException e){
            Log.d(TAG, "getBitmap: "+e.getMessage());
        }
        finally {
            try {
                fis.close();
            }
            catch (IOException e){
                Log.d(TAG, "getBitmap: "+e.getMessage());
            }
        }
        return bitmap;
    }

    public static byte[] getBytesFromBitmap(Bitmap bm,int quality){
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,quality,stream);
        return stream.toByteArray();
    }
}
