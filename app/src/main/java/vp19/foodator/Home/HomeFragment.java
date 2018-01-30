/**
 *  Name : HomeFragment
 *  Type : Fragment
 *  ContentView : fragment_home
 *  Authentication : Signed In users
 *  Purpose : To display the posts of the users
 */
package vp19.foodator.Home;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.zip.Inflater;

import vp19.foodator.R;
public class HomeFragment extends Fragment {
    private static final String TAG = "HomeFragment";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home,container,false);
        return view;
    }
}
