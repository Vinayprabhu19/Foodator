/**
 *  Name : Image Manager
 *  Type : Utility java class
 *  ContentView : None
 *  Authentication : None
 *  Purpose : To handle Image related methods
 */
package vp19.foodator.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class ImageManager {
    private static final String TAG = "ImageManager";

    /**
     * Conver image to bitmap format
     * @param url : URL of the image
     * @return : Bitmap
     */
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

    /**
     *  Obtain bytes array from the bitmap
     * @param bm : Bitmap
     * @param quality : The quality to be preserved 0->100
     * @return byte array
     */
    public static byte[] getBytesFromBitmap(Bitmap bm,int quality){
        ByteArrayOutputStream stream=new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.JPEG,quality,stream);
        return stream.toByteArray();
    }
}
