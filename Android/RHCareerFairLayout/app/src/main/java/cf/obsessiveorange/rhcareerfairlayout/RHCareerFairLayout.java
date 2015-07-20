package cf.obsessiveorange.rhcareerfairlayout;

import java.util.ArrayList;

import cf.obsessiveorange.rhcareerfairlayout.ui.fragments.VPCompaniesFragment;
import cf.obsessiveorange.rhcareerfairlayout.ui.fragments.VPFiltersFragment;
import cf.obsessiveorange.rhcareerfairlayout.ui.fragments.ViewPagerTabFragmentScrollViewFragment;
import cf.obsessiveorange.rhcareerfairlayout.ui.models.NavigationItem;

/**
 * Singleton class for global variables.
 *
 * Created by Benedict on 7/14/2015.
 */
public class RHCareerFairLayout {
    public static final String RH_CFL = "RH-CFL";
    public static final int APP_VERSION = 1;

    public static final ArrayList<NavigationItem> tabs = new ArrayList<NavigationItem>();
    static{
        tabs.add(new NavigationItem("Map", new ViewPagerTabFragmentScrollViewFragment()));
        tabs.add(new NavigationItem("Companies", new VPCompaniesFragment()));
        tabs.add(new NavigationItem("Filters", new VPFiltersFragment()));
    }
}
