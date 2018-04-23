package vp19.foodator.Food;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

import vp19.foodator.R;
import vp19.foodator.utils.Threads.NetworkThread;
import vp19.foodator.camera_eye.Classifier;
import vp19.foodator.camera_eye.TensorFlowImageClassifier;
import vp19.foodator.utils.SquareImageView;

public class ImageIdentifyActivity extends AppCompatActivity {
    private static final String TAG = "ImageIdentifyActivity";
    //Widgets
    private SquareImageView image;
    private Context mContext=ImageIdentifyActivity.this;
    //Tensorflow
    //private TensorFlowImageClassifier c;
    private static final int INPUT_SIZE = 299;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;
    private static final String INPUT_NAME = "Mul";
    private static final String OUTPUT_NAME = "final_result";
    private static final String MODEL_FILE = "file:///android_asset/graph.pb";
    private static final String LABEL_FILE = "file:///android_asset/labels.txt";
    private Classifier classifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_identify);
        init();
    }
    private void init(){
        Intent intent=getIntent();
        final String imageURL=intent.getStringExtra(getString(R.string.selected_bitmap));
        //Download the image into bitmap
        NetworkThread netThread=new NetworkThread(imageURL);
        netThread.start();
        try {
            netThread.join();
        }
        catch (InterruptedException e){
            Log.d(TAG, "init: "+e.getMessage());
        }
        Bitmap bitmap=netThread.getBitmap();
        bitmap=getResizedBitmap(bitmap,INPUT_SIZE,INPUT_SIZE);
        classifier = TensorFlowImageClassifier.create(
                            getAssets(),
                            MODEL_FILE,
                            LABEL_FILE,
                            INPUT_SIZE,
                            IMAGE_MEAN,
                            IMAGE_STD,
                            INPUT_NAME,
                            OUTPUT_NAME);
        final List<Classifier.Recognition> results = classifier.recognizeImage(bitmap);
        ListView listView=findViewById(R.id.list);
        ArrayList<String> list=new ArrayList<>();
        final ArrayList<String> names=new ArrayList<>();
        for(Classifier.Recognition i : results){
            list.add(i.getTitle()+" "+String.format("%.2f",i.getConfidence()*100)+"%");
            names.add(i.getTitle());
            Log.d(TAG, i.getTitle()+" "+String.format("%.2f",i.getConfidence()*100)+"%");
        }
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1,list);
        listView.setAdapter(adapter);
        image=findViewById(R.id.image);
        image.setImageBitmap(bitmap);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent=new Intent(mContext,FoodActivity.class);
                intent.putExtra(getString(R.string.calling_activity),names.get(position));
                startActivity(intent);
            }
        });
    }
    public Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(
                bm, 0, 0, width, height, matrix, false);
        bm.recycle();
        return resizedBitmap;
    }
}
