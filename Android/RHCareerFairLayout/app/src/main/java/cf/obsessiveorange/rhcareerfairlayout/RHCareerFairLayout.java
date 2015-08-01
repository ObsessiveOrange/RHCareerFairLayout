package cf.obsessiveorange.rhcareerfairlayout;

import java.util.ArrayList;

import cf.obsessiveorange.rhcareerfairlayout.ui.fragments.VPCompaniesFragment;
import cf.obsessiveorange.rhcareerfairlayout.ui.fragments.VPFiltersFragment;
import cf.obsessiveorange.rhcareerfairlayout.ui.fragments.VPMapContainerFragment;
import cf.obsessiveorange.rhcareerfairlayout.ui.models.NavigationItem;

/**
 * Singleton class for global variables.
 * <p>
 * Created by Benedict on 7/14/2015.
 */
public class RHCareerFairLayout {
    public static final String RH_CFL = "RH-CFL";
    public static final int APP_VERSION = 1;

//    public static final String URL_BASE = "http://192.168.2.30:8080/api";
    public static final String URL_BASE = "http://rhcareerfair.cf/api";

    public static final ArrayList<NavigationItem> tabs = new ArrayList<NavigationItem>();

    static {
        tabs.add(new NavigationItem("Map", new VPMapContainerFragment()));
        tabs.add(new NavigationItem("Companies", new VPCompaniesFragment()));
        tabs.add(new NavigationItem("Filters", new VPFiltersFragment()));
    }

    public static final ChangeNotifier companySelectionChanged = new ChangeNotifier();
    public static final ChangeNotifier categorySelectionChanged = new ChangeNotifier();

    public static class ChangeNotifier {
        private boolean changed = false;

        public boolean hasChanged() {
            return changed;
        }

        public void notifyChanged() {
            this.changed = true;
            notifyAll();
        }


    }
}
