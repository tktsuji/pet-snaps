package blackbox.petsnaps;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import blackbox.petsnaps.FilterFragments.AllPostsFragment;
import blackbox.petsnaps.FilterFragments.BirdFragment;
import blackbox.petsnaps.FilterFragments.CatFragment;
import blackbox.petsnaps.FilterFragments.DogFragment;
import blackbox.petsnaps.FilterFragments.RabbitFragment;
import blackbox.petsnaps.FilterFragments.ReptileFragment;
import blackbox.petsnaps.FilterFragments.RodentFragment;

public class PagerAdapter extends FragmentStatePagerAdapter{
    int mNumTabs;

    public PagerAdapter(FragmentManager fm, int numTabs) {
        super(fm);
        this.mNumTabs = numTabs;
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
            default:
                return null;

        }
    }

    @Override
    public int getCount() {
        return mNumTabs;
    }
}
