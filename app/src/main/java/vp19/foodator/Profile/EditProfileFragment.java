package vp19.foodator.Profile;

/**
 * Created by Vinay Prabhu on 17-Jan-18.
 */
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.nostra13.universalimageloader.core.ImageLoader;

import vp19.foodator.R;
import vp19.foodator.utils.UniversalImageLoader;


public class EditProfileFragment extends Fragment {

    private static final String TAG = "EditProfileFragment";

    private ImageView mProfilePhoto;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_editprofile, container, false);
        mProfilePhoto = (ImageView) view.findViewById(R.id.profile_photo);
        initImageLoader();
        setProfileImage();
        Log.d(TAG, "onCreateView: Edit Profile");
        //backarrow to go back to "ProfileActivity"
        ImageView backArrow = (ImageView) view.findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: Navigating back to profile activity");
                getActivity().finish();
            }
        });

        return view;
    }

    private void initImageLoader(){
        UniversalImageLoader imageLoader=new UniversalImageLoader(getActivity());
        ImageLoader.getInstance().init(imageLoader.getConfig());
    }
    private void setProfileImage() {
        Log.d(TAG, "setProfileImage: setting profile image");

        String imgURL = "avatarfiles.alphacoders.com/838/83876.jpg";
        UniversalImageLoader.setImage(imgURL, mProfilePhoto, null, "https://");
    }

}