/**
 *  Name : SearchFragment
 *  Type : Fragment
 *  ContentView : fragment_search
 *  Authentication : Signed In users
 *  Purpose : To search for users,tags,posts
 */
package vp19.foodator.Home;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import vp19.foodator.R;
public class SearchFragment extends Fragment {
    private static final String TAG = "SearchFragment";
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search,container,false);

        return view;
    }
}
