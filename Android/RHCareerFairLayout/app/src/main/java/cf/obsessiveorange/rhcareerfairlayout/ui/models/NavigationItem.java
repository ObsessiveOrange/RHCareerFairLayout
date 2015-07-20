package cf.obsessiveorange.rhcareerfairlayout.ui.models;

import cf.obsessiveorange.rhcareerfairlayout.ui.BaseFragment;

/**
 * Created by Benedict on 7/16/2015.
 */
public class NavigationItem {

    private String title;
    private BaseFragment fragment;

    public NavigationItem(String title, BaseFragment fragment) {
        this.title = title;
        this.fragment = fragment;

    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public BaseFragment getFragment() {
        return fragment;
    }

    public void setFragment(BaseFragment fragment) {
        this.fragment = fragment;
    }
}
