/*
 * Copyright 2014 Soichiro Kashima
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cf.obsessiveorange.rhcareerfairlayout.ui.fragments;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.github.ksoichiro.android.observablescrollview.CacheFragmentStatePagerAdapter;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.github.ksoichiro.android.observablescrollview.ScrollState;
import com.github.ksoichiro.android.observablescrollview.ScrollUtils;
import com.github.ksoichiro.android.observablescrollview.Scrollable;
import com.github.ksoichiro.android.observablescrollview.TouchInterceptionFrameLayout;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.quinny898.library.persistentsearch.SearchBox;

import cf.obsessiveorange.rhcareerfairlayout.R;
import cf.obsessiveorange.rhcareerfairlayout.RHCareerFairLayout;
import cf.obsessiveorange.rhcareerfairlayout.ui.activities.MainActivity;
import cf.obsessiveorange.rhcareerfairlayout.ui.application.RHCareerFairLayoutApplication;
import cf.obsessiveorange.rhcareerfairlayout.ui.views.SlidingTabLayout;

/**
 * This fragment manages ViewPager and its child Fragments.
 * Scrolling techniques are basically the same as ViewPagerTab2Activity.
 */
public class VPParentFragment extends BaseFragment implements ObservableScrollViewCallbacks {

    private TouchInterceptionFrameLayout mInterceptionLayout;
    private ViewPager mPager;
    private int mLastPage = 0;
    private NavigationAdapter mPagerAdapter;
    private int mSlop;
    private ScrollState mLastScrollState;
    private TouchInterceptionFrameLayout.TouchInterceptionListener mInterceptionListener = new TouchInterceptionFrameLayout.TouchInterceptionListener() {
        @Override
        public boolean shouldInterceptTouchEvent(MotionEvent ev, boolean moving, float diffX, float diffY) {
            if (mSlop < Math.abs(diffX) && Math.abs(diffY) < Math.abs(diffX)) {
                Log.d(RHCareerFairLayout.RH_CFL, "Scrolled Horizontally");
                // Horizontal scroll is maybe handled by ViewPager
                return false;
            }
            return false;
        }

        @Override
        public void onDownMotionEvent(MotionEvent ev) {
        }

        @Override
        public void onMoveMotionEvent(MotionEvent ev, float diffX, float diffY) {
            View toolbarView = getActivity().findViewById(R.id.toolbar);
            float translationY = ScrollUtils.getFloat(mInterceptionLayout.getTranslationY() + diffY, -toolbarView.getHeight(), 0);
            mInterceptionLayout.setTranslationY(translationY);
            toolbarView.setTranslationY(translationY);
            if (translationY < 0) {
                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mInterceptionLayout.getLayoutParams();
                lp.height = (int) (-translationY + getScreenHeight());
                mInterceptionLayout.requestLayout();
            }
        }

        @Override
        public void onUpOrCancelMotionEvent(MotionEvent ev) {
            adjustToolbar(mLastScrollState);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_parent, container, false);

        final MainActivity parentActivity = MainActivity.instance;
        mPagerAdapter = new NavigationAdapter(getChildFragmentManager());
        mPager = (ViewPager) view.findViewById(R.id.pager);
        ViewPager.SimpleOnPageChangeListener pageChangeListener = new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                Log.d(RHCareerFairLayout.RH_CFL, "Selected page:" + position);

                if (getCurrentFragment() != null) {
                    // If position is layout fragment, and scale is at base, show the toolbar. Otherwise, hide it.
                    if (position == 0) {
                        if (((VPLayoutContainerFragment) getCurrentFragment()).getLayoutView().getScaleFactor() == 1.0) {
                            showToolbar();
                        } else {
                            hideToolbar();
                        }
                    }

                    // Push to the backStack.
                    parentActivity.pushToBackStack(mLastPage);
                }

                // Send tracking info
                RHCareerFairLayoutApplication application = (RHCareerFairLayoutApplication) parentActivity.getApplication();
                Tracker tracker = application.getDefaultTracker();
                Log.i(RHCareerFairLayout.RH_CFL, "Setting screen name: " + RHCareerFairLayout.tabs.get(position).getTitle());
                tracker.setScreenName(RHCareerFairLayout.tabs.get(position).getTitle());
                tracker.send(new HitBuilders.ScreenViewBuilder().build());

                // Set trailing marker.
                mLastPage = position;
            }
        };

        // Configure adapter.
        mPager.addOnPageChangeListener(pageChangeListener);
        mPager.setOffscreenPageLimit(2);
        mPager.setAdapter(mPagerAdapter);

        // Trigger first change, so that backStack and tracker code fires the first time.
        pageChangeListener.onPageSelected(0);

        // Create sliding tab layout for viewpager
        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        slidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        slidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.primaryLight));
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(mPager);

        // Set touch boundary slopes to differentiate between vertical and horizontal swipes.
        // Set scrolling listeners.
        ViewConfiguration vc = ViewConfiguration.get(parentActivity);
        mSlop = vc.getScaledTouchSlop();
        mInterceptionLayout = (TouchInterceptionFrameLayout) view.findViewById(R.id.container);
        mInterceptionLayout.setScrollInterceptionListener(mInterceptionListener);

        return view;
    }

    @Override
    public void onScrollChanged(int scrollY, boolean firstScroll, boolean dragging) {
    }

    @Override
    public void onDownMotionEvent() {
    }

    @Override
    public void onUpOrCancelMotionEvent(ScrollState scrollState) {
        // Check if toolbar state needs to be changed.
        adjustToolbar(scrollState);
    }

    /**
     * Return scrollable at current position
     * @return scrollable at current position, Null if fragment has no scrollable element.
     */
    private Scrollable getCurrentScrollable() {
        Fragment fragment = getCurrentFragment();
        if (fragment == null) {
            return null;
        }
        View view = fragment.getView();
        if (view == null) {
            return null;
        }
        return (Scrollable) view.findViewById(R.id.scroll);
    }

    /**
     * Show/Hide toolbar if needed.
     * @param scrollState scrollState event from calling method. Used to determine whether to hide/show.
     */
    private void adjustToolbar(ScrollState scrollState) {
        final Scrollable scrollable = getCurrentScrollable();

        // If not a scrollable (eg, layout), early-out.
        if (scrollable == null) {
            return;
        }
        // If finger movement down (Scrolled Up), or is already at top of the list, show toolbar
        if (scrollState == ScrollState.DOWN || (scrollable instanceof RecyclerView
                && ((RecyclerView) scrollable).computeVerticalScrollOffset() == 0)) {
            showToolbar();

            // Also clear search focus.
            MainActivity.instance.clearSearchFocus();
        }
        // Else, hide toolbar.
        else if (scrollState == ScrollState.UP) {
            hideToolbar();

            // Also clear search focus.
            MainActivity.instance.clearSearchFocus();
        }
    }

    /**
     * Gets currently selected fragment
     * @return Fragment instance that is currently selected.
     */
    public Fragment getCurrentFragment() {
        return mPagerAdapter.getItemAt(mPager.getCurrentItem());
    }

    /**
     * Show the toolbar
     * @param completionHandlers any handlers to be fired after completion of the animation
     */
    public void showToolbar(Runnable... completionHandlers) {
        animateToolbar(0, completionHandlers);
    }

    /**
     * Hide the toolbar
     * @param completionHandlers any handlers to be fired after completion of the animation
     */
    public void hideToolbar(Runnable... completionHandlers) {
        View toolbarView = getActivity().findViewById(R.id.toolbar);
        animateToolbar(-toolbarView.getHeight(), completionHandlers);
    }

    /**
     * Private helper method to animate toolbar and related views to a position.
     * @param toY New Y position.
     * @param completionHandlers any handlers to be fired after completion of the animation
     */
    private void animateToolbar(final float toY, final Runnable... completionHandlers) {
        float layoutTranslationY = mInterceptionLayout.getTranslationY();
        if (layoutTranslationY != toY) {
            // get translation
            final float translationY = -layoutTranslationY + toY;

            final View toolbarView = getActivity().findViewById(R.id.toolbar);
            final SearchBox searchView = ((MainActivity) getActivity()).getSearch();

            // Need to offset for the -6dp margin on search bar
            Resources r = getResources();
            float offset = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6, r.getDisplayMetrics());

            // Start animating all the items
            mInterceptionLayout.animate().y(toY).setDuration(200);
            toolbarView.animate().y(toY).setDuration(200);
            searchView.animate().y(toY - offset).setDuration(200);

            // Relayout child view at animation completion
            final FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mInterceptionLayout.getLayoutParams();
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    lp.height = (int) (Math.max(0, -translationY) + getScreenHeight());
                    mInterceptionLayout.requestLayout();
                }
            }, translationY < 0 ? 0 : 200);

            // Start calling all handlers, giving time for re-layout to occur.
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    for (Runnable completionHandler : completionHandlers) {
                        completionHandler.run();
                    }
                }
            }, 250);

        }
    }

    /**
     * Implementation of CacheFragmentStatePagerAdapter. Gets fragments from RHCareerFairLayout singleton.
     */
    private static class NavigationAdapter extends CacheFragmentStatePagerAdapter {

        public NavigationAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        protected Fragment createItem(int position) {

            return RHCareerFairLayout.tabs.get(position).getFragment();
        }

        @Override
        public int getCount() {
            return RHCareerFairLayout.tabs.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return RHCareerFairLayout.tabs.get(position).getTitle();
        }
    }

    public ViewPager getPager() {
        return mPager;
    }

}
