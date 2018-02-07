package jack.village;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

/**
 * Created by jack on 30/01/2018.
 */

public class PagerAdapter extends FragmentStatePagerAdapter {
    int numberOfTabs;


    //Populate the contents of the screen
    public PagerAdapter(FragmentManager fragmentManager, int numberOfTabs) {
        super(fragmentManager);
        this.numberOfTabs = numberOfTabs;
    }

    @Override
    public Fragment getItem(int position) {

        switch (position) {
            case 0:
                return new TabHome();
            case 1:
                return new TabContact();
            case 2:
                return new TabNotes();
            case 3:
                return new TabDonate();
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return numberOfTabs;
    }
}