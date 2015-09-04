package cf.obsessiveorange.rhcareerfairlayout.ui.application;

import android.app.Application;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;

import cf.obsessiveorange.rhcareerfairlayout.R;

/**
 * Created by Benedict on 8/15/2015.
 */
public class RHCareerFairLayoutApplication extends Application {


    /**
     * This is a subclass of {@link Application} used to provide shared objects for this app, such as
     * the {@link Tracker}.
     */
    private Tracker mTracker;

    /**
     * Gets the default {@link Tracker} for this {@link Application}.
     *
     * @return tracker
     */
    synchronized public Tracker getDefaultTracker() {
        if (mTracker == null) {
            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.setLocalDispatchPeriod(5);
            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }
        return mTracker;
    }
}
