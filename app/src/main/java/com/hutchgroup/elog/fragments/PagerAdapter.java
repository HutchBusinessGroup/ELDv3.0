package com.hutchgroup.elog.fragments;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

//adapter to display 3 tab in Settings
public class PagerAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> fragments;
    private int mCurrentPosition = -1;

    private SettingsFragment settingsFragment;

    public PagerAdapter(FragmentManager fm, SettingsFragment frag) {
        super(fm);

        settingsFragment = frag;
        this.fragments = new ArrayList<Fragment>();
        fragments.add(new TabSystemFragment());
        fragments.add(new TabDisplayFragment());
        fragments.add(new TabSoundFragment());

        ((TabSystemFragment)fragments.get(0)).setSettingsFragment(settingsFragment);
    }

    @Override
    public void setPrimaryItem(ViewGroup container, int position, Object object) {
        super.setPrimaryItem(container, position, object);
        if (position != mCurrentPosition) {
            Fragment fragment = (Fragment) object;
            //using custom ScrollableViewpager to let user scroll down for long information tab such as TabSystem
            // and to change height of the displayed tab
            ScrollableViewpager pager = (ScrollableViewpager) container;
            if (fragment != null && fragment.getView() != null) {
                mCurrentPosition = position;
                //need to call this function to update height of the current tab
                pager.measureCurrentView(fragment.getView());
            }
        }
    }

    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return fragments.size();
    }
}