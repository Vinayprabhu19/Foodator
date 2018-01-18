package vp19.foodator.utils;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Raghuveer on 12-11-2017.
 */

public class SectionsStatePagerAdapter extends FragmentStatePagerAdapter{

    private final List<Fragment> mFragmentlist = new ArrayList<>();
    private final HashMap<Fragment, Integer> mFragments = new HashMap<>();
    private final HashMap<String, Integer> mFragmentNumbers = new HashMap<>();
    private final HashMap<Integer, String> mFragmentNames = new HashMap<>();

    public SectionsStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentlist.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentlist.size();
    }

    public void addFragment(Fragment fragment, String fragmentName) {
        mFragmentlist.add(fragment);
        mFragments.put(fragment, mFragmentlist.size()-1);
        mFragmentNumbers.put(fragmentName, mFragmentlist.size()-1);
        mFragmentNames.put(mFragmentlist.size()-1, fragmentName);
    }

    /**
     * Returns fragment with name @param
     * @param fragmentName
     * @return
     */
    public Integer getFragmentNumber(String fragmentName) {
        if(mFragmentNumbers.containsKey(fragmentName)) {
            return mFragmentNumbers.get(fragmentName);
        }else {
            return null;
        }
    }

    /**
     * Returns fragment with name @param
     * @param fragment
     * @return
     */
    public Integer getFragmentNumber(Fragment fragment) {
        if(mFragmentNumbers.containsKey(fragment)) {
            return mFragmentNumbers.get(fragment);
        }else {
            return null;
        }
    }

    /**
     * Returns fragment with name @param
     * @param fragmentNumber
     * @return
     */
    public String getFragmentName(Integer fragmentNumber) {
        if(mFragmentNames.containsKey(fragmentNumber)) {
            return mFragmentNames.get(fragmentNumber);
        }else {
            return null;
        }
    }
}