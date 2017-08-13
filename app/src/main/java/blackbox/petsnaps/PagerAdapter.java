package blackbox.petsnaps;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import blackbox.petsnaps.FilterFragments.AllPostsFragment;
import blackbox.petsnaps.FilterFragments.BirdFragment;
import blackbox.petsnaps.FilterFragments.CatFragment;
import blackbox.petsnaps.FilterFragments.DogFragment;
import blackbox.petsnaps.FilterFragments.MyFavoritesFragment;
import blackbox.petsnaps.FilterFragments.MyPostsFragment;
import blackbox.petsnaps.FilterFragments.RabbitFragment;
import blackbox.petsnaps.FilterFragments.ReptileFragment;
import blackbox.petsnaps.FilterFragments.RodentFragment;
import blackbox.petsnaps.FilterFragments.TopPostsFragment;

public class PagerAdapter extends FragmentStatePagerAdapter{
    int mNumTabs;
    Bundle fragmentBundle;

    public PagerAdapter(FragmentManager fm, int numTabs, Bundle bundle) {
        super(fm);
        this.mNumTabs = numTabs;
        fragmentBundle = bundle;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new AllPostsFragment();
            case 1:
                return new DogFragment();
            case 2:
                return new CatFragment();
            case 3:
                return new BirdFragment();
            case 4:
                return new RabbitFragment();
            case 5:
                return new ReptileFragment();
            case 6:
                return new RodentFragment();
            case 7:
                return new MyPostsFragment();
            case 8:
                MyFavoritesFragment frag = new MyFavoritesFragment();
                frag.setArguments(fragmentBundle);
                return frag;
            case 9:
                return new TopPostsFragment();
            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return mNumTabs;
    }

}
