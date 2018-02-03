/**
 *  Name : PhotoFragment
 *  Type : Fragment
 *  ContentView : fragment_photo
 *  Authentication : Signed In users
 *  Purpose : To capture photo on camera
 */
package vp19.foodator.Share;


import android.Manifest;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import vp19.foodator.Profile.AccountSettingsActivity;
import vp19.foodator.R;

public class PhotoFragment extends Fragment {
    private static final String TAG = "PhotoFragment";
    private static final int PHOTO_FRAGMENT=1;
    private static final int CAMERA_REQUEST_CODE=5;
    private Bitmap bitmap;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_photo,container,false);
        Button launch_camera=view.findViewById(R.id.btn_launch_camera);
        launch_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Check if the camera permission are available
                if(((ShareActvity)getActivity()).checkPermission(Manifest.permission.CAMERA)){
                    Log.d(TAG, "onClick: Camera available");
                    Intent cameraIntent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(cameraIntent,CAMERA_REQUEST_CODE);
                }
                else{
                    Toast.makeText(getContext(),"Can't Start Camera. No permissions given",Toast.LENGTH_LONG).show();
                    Log.d(TAG, "onClick: Not available");
                    Intent intent=new Intent(getActivity(),ShareActvity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
            }
        });
        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CAMERA_REQUEST_CODE){
            Log.d(TAG, "onActivityResult: Done Taking the Photo");
            //Navigating to final screen to publish the photo
            try {
                bitmap=(Bitmap)data.getExtras().get("data");
                Intent intent=new Intent(getActivity(),NextActivity.class);
                intent.putExtra(getString(R.string.selected_bitmap),bitmap);
                startActivity(intent);
            }
            catch(NullPointerException e){
                Log.d(TAG, "onActivityResult: Camera Exception");
            }
        }
    }
}
