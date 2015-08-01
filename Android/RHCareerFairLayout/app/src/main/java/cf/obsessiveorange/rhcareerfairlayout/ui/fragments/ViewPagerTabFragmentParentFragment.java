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

import android.animation.ValueAnimator;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import cf.obsessiveorange.rhcareerfairlayout.R;
import cf.obsessiveorange.rhcareerfairlayout.RHCareerFairLayout;
import cf.obsessiveorange.rhcareerfairlayout.ui.BaseFragment;
import cf.obsessiveorange.rhcareerfairlayout.ui.SlidingTabLayout;

/**
 * This fragment manages ViewPager and its child Fragments.
 * Scrolling techniques are basically the same as ViewPagerTab2Activity.
 */
public class ViewPagerTabFragmentParentFragment extends BaseFragment implements ObservableScrollViewCallbacks {
    public static final String FRAGMENT_TAG = "fragment";

    private TouchInterceptionFrameLayout mInterceptionLayout;
    private ViewPager mPager;
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

            // If interceptionLayout can move, it should intercept.
            // And once it begins to move, horizontal scroll shouldn't work any longer.

//            View toolbarView = getActivity().findViewById(R.id.toolbar);

//            int toolbarHeight = toolbarView.getHeight();
//            int translationY = (int) mInterceptionLayout.getTranslationY();
            boolean scrollingUp = 0 < diffY;
            boolean scrollingDown = diffY < 0;
            if (scrollingDown) {
                if (toolbarIsHidden()) {
                    return false;
                }
                showToolbar();
            } else if (scrollingUp) {

                if (toolbarIsShown()) {
                    return false;
                }
                hideToolbar();
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
        View view = inflater.inflate(R.layout.fragment_viewpagertabfragment_parent, container, false);

        AppCompatActivity parentActivity = (AppCompatActivity) getActivity();
        mPagerAdapter = new NavigationAdapter(getChildFragmentManager());
        mPager = (ViewPager) view.findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);

        SlidingTabLayout slidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);
        slidingTabLayout.setCustomTabView(R.layout.tab_indicator, android.R.id.text1);
        slidingTabLayout.setSelectedIndicatorColors(getResources().getColor(R.color.accent));
        slidingTabLayout.setDistributeEvenly(true);
        slidingTabLayout.setViewPager(mPager);

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
        // This event can be used only when TouchInterceptionFrameLayout
        // doesn't handle the consecutive events.
        adjustToolbar(scrollState);
//        }
    }

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

    private void adjustToolbar(ScrollState scrollState) {
        final Scrollable scrollable = getCurrentScrollable();
        if (scrollable == null) {
            return;
        }
        int scrollY = scrollable.getCurrentScrollY();
        if (scrollState == ScrollState.DOWN) {
            showToolbar();
        } else if (scrollState == ScrollState.UP) {
            hideToolbar();
        }
//        } else if (scrollState == ScrollState.UP) {
//            if (toolbarHeight <= scrollY) {
//                hideToolbar();
//            } else {
//                showToolbar();
//            }
//        } else if (!toolbarIsShown() && !toolbarIsHidden()) {
//            // Toolbar is moving but doesn't know which to move:
//            // you can change this to hideToolbar()
//            showToolbar();
//        }
    }

    private Fragment getCurrentFragment() {
        return mPagerAdapter.getItemAt(mPager.getCurrentItem());
    }

    public boolean toolbarIsShown() {
        return mInterceptionLayout.getTranslationY() == 0;
    }

    public boolean toolbarIsHidden() {
        View view = getView();
        if (view == null) {
            return false;
        }
        View toolbarView = getActivity().findViewById(R.id.toolbar);
        return mInterceptionLayout.getTranslationY() == -toolbarView.getHeight();
    }

    public void showToolbar(Runnable... completionHandlers) {
        animateToolbar(0, completionHandlers);
    }

    public void hideToolbar(Runnable... completionHandlers) {
        View toolbarView = getActivity().findViewById(R.id.toolbar);
        animateToolbar(-toolbarView.getHeight(), completionHandlers);
    }

    private void animateToolbar(final float toY, final Runnable... completionHandlers) {
        float layoutTranslationY = mInterceptionLayout.getTranslationY();
        if (layoutTranslationY != toY) {
            final ValueAnimator animator = ValueAnimator.ofFloat(mInterceptionLayout.getTranslationY(), toY).setDuration(200);
            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator animation) {
                    float translationY = (float) animation.getAnimatedValue();
                    View toolbarView = getActivity().findViewById(R.id.toolbar);
                    mInterceptionLayout.setTranslationY(translationY);
                    toolbarView.setTranslationY(translationY);
                    if (translationY < 0) {
                        FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mInterceptionLayout.getLayoutParams();
                        lp.height = (int) (-translationY + getScreenHeight());
                        mInterceptionLayout.requestLayout();
                    }
                }
            });
            animator.start();
            new Handler().postDelayed(new Runnable() {
                public void run() {
                    animator.end();

                    for(Runnable completionHandler : completionHandlers){
                        completionHandler.run();
                    }
                }
            }, animator.getDuration());

        }
    }

    /**
     * This adapter provides two types of fragments as an example.
     * {@linkplain #createItem(int)} should be modified if you use this example for your app.
     */
    private static class NavigationAdapter extends CacheFragmentStatePagerAdapter {

        public NavigationAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        protected Fragment createItem(int position) {
            Fragment f;
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
}
