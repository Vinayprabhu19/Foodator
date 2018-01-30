/**
 *  Name : GalleryFragment
 *  Type : Fragment
 *  ContentView : fragment_gallery
 *  Authentication : Signed In users
 *  Purpose : To share image in the storage
 */
package vp19.foodator.Share;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.util.ArrayList;

import vp19.foodator.Profile.AccountSettingsActivity;
import vp19.foodator.R;
import vp19.foodator.utils.FileSearch;
import vp19.foodator.utils.GridImageAdapter;
public class GalleryFragment extends Fragment {
    private static final String TAG = "GalleryFragment";
    private static final int NUM_GRID_COLS=3;
    private static final String append="file:/";
    //Widgets
    private GridView gridView;
    private ImageView galleryImage;
    private ProgressBar progressBar;
    private Spinner directorySpinner;
    private Switch mFit;
    //vars
    private ArrayList<String> directories;
    private ArrayList<String> sDirectoryList;
    //File Paths
    public static String ROOT_DIR;
    public static String CAMERA_DIR;
    private String mSelectedImage;
    private boolean mFitStatus;
    FileSearch searchHelper;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gallery,container,false);
        galleryImage=view.findViewById(R.id.galleryImage);
        gridView=view.findViewById(R.id.gridView);
        progressBar=view.findViewById(R.id.progressBar);
        mFit=view.findViewById(R.id.swtich_fit);
        progressBar.setVisibility(View.GONE);
        directorySpinner=view.findViewById(R.id.spinnerDirectory);
        directories=new ArrayList<>();
        ImageView close=view.findViewById(R.id.btn_close);
        init();
        initMfit();
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().finish();
            }
        });
        TextView nextScreen=view.findViewById(R.id.tvNext);
        nextScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean isShareActivity=((ShareActvity)getActivity()).getTask();
                if(isShareActivity){
                    Intent intent=new Intent(getActivity(),NextActivity.class);
                    Bundle extras=new Bundle();
                    extras.putString(getString(R.string.selected_image),mSelectedImage);
                    extras.putBoolean(getString(R.string.fit_status),mFitStatus);
                    intent.putExtras(extras);
                    startActivity(intent);
                }
                else{
                    Intent intent=new Intent(getActivity(),AccountSettingsActivity.class);
                    intent.putExtra(getString(R.string.selected_image),mSelectedImage);
                    intent.putExtra(getString(R.string.return_to_fragment),getString(R.string.edit_profile));
                    getActivity().startActivity(intent);
                    getActivity().finish();
                }
            }
        });
        return view;
    }
    private void initMfit(){
        mFit.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                    galleryImage.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                else
                    galleryImage.setScaleType(ImageView.ScaleType.CENTER_CROP);
                mFitStatus=isChecked;
            }
        });
    }
    private void init(){
        ROOT_DIR= Environment.getExternalStorageDirectory().getPath();
        CAMERA_DIR="DCIM";
        searchHelper=new FileSearch(ROOT_DIR);
        try {
            searchHelper.join();
        }
        catch (InterruptedException e){
            Log.d(TAG, "init: "+e.getMessage());
        }
        directories=searchHelper.pathArray;
        //directories.add(0,CAMERA_DIR);
        ArrayAdapter<String> adapter=new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_dropdown_item,directories);
        directorySpinner.setAdapter(adapter);
        directorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String absolutePath=ROOT_DIR+"/"+directories.get(position);
                Log.d(TAG, "onItemSelected: Item selected is  "+ absolutePath);
                setupGridView(absolutePath);
                ((ShareActvity)getActivity()).setupBottomNavigationView();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void setupGridView(String absolutePath){
        final ArrayList<String>  imgURLS=searchHelper.getFilePaths(absolutePath);
        int gridWidth = getResources().getDisplayMetrics().widthPixels;
        int imageWidth= gridWidth / NUM_GRID_COLS;
        gridView.setColumnWidth(imageWidth);
        try{
            GridImageAdapter adapter=new GridImageAdapter(getContext(),R.layout.layout_grid_imageview,append,imgURLS);
            gridView.setAdapter(adapter);
        }
        catch (Exception e){
            Log.d(TAG, "setupGridView: Caught Exception"+e.getMessage());
        }
        final ImageView gridImage=getView().findViewById(R.id.galleryImage);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemClick: Image clicked is "+imgURLS.get(position));
                ((ShareActvity)getActivity()).setupBottomNavigationView();
                setImage(gridImage,imgURLS.get(position));
                mSelectedImage=imgURLS.get(position);
            }
        });
    }

    private void setImage(ImageView gridImage,String URL){
        ImageLoader imageLoader=ImageLoader.getInstance();
        imageLoader.displayImage(append + URL, gridImage, new ImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                progressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                progressBar.setVisibility(View.GONE);
            }

            @Override
            public void onLoadingCancelled(String imageUri, View view) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }
}
