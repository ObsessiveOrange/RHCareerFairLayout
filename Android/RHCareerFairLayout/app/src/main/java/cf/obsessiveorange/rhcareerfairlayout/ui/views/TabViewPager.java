package cf.obsessiveorange.rhcareerfairlayout.ui.views;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import cf.obsessiveorange.rhcareerfairlayout.RHCareerFairLayout;

/**
 * Created by Benedict on 7/22/2015.
 */
public class TabViewPager extends ViewPager {
    public TabViewPager(Context context) {
        super(context);
    }

    public TabViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        try {
            return super.onInterceptTouchEvent(event);
        } catch (final IllegalArgumentException e) {
            // Ignore IllegalArgumentExceptions - thrown by ScaleGestureDetector; no fixes found.
            // See http://goo.gl/S826rH
            Log.d(RHCareerFairLayout.RH_CFL, "Ignoring IllegalArgumentException thrown by bug in ScaleGestureDetector");
            return false;
        }
    }
}
