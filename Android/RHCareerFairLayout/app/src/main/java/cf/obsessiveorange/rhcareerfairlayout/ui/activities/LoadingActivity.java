package cf.obsessiveorange.rhcareerfairlayout.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import cf.obsessiveorange.rhcareerfairlayout.R;
import cf.obsessiveorange.rhcareerfairlayout.RHCareerFairLayout;
import cf.obsessiveorange.rhcareerfairlayout.data.managers.ConnectionManager;
import cf.obsessiveorange.rhcareerfairlayout.data.managers.DBManager;
import cf.obsessiveorange.rhcareerfairlayout.data.requests.GetAllDataRequest;


public class LoadingActivity extends Activity {

    public static final String KEY_FORCE_REFRESH = "forceRefresh";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        final Button retryButton = (Button) findViewById(R.id.loading_btn_retry);
        retryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reloadData();
            }
        });

        reloadData();

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void reloadData() {

        final TextView statusTextView = (TextView) findViewById(R.id.loading_txt_status);
        final Button retryButton = (Button) findViewById(R.id.loading_btn_retry);

        DBManager.setupDBAdapterIfNeeded(this);

        // Set default loading view
        statusTextView.setText(getString(R.string.loadingStatus_checkingForNewData));
        retryButton.setVisibility(View.INVISIBLE);

        // TODO: Get last update time from server
        Long localUpdateTime = DBManager.getLastUpdateTime();

        // TODO: Change this logic to factor in server update times.
        // if no data, or data is within cache time, skip.
        if (localUpdateTime != null && (System.currentTimeMillis() - localUpdateTime) < TimeUnit.DAYS.toMillis(RHCareerFairLayout.DATA_CACHE_TIME_IN_DAYS)) {

            statusTextView.setText(getString(R.string.loadingStatus_dataUpToDate));
            Log.d(RHCareerFairLayout.RH_CFL, getString(R.string.loadingStatus_dataUpToDate));

            doneLoadingData();

        }
        // Else, retrieve new data.
        else {

            // Set status as retrieving new data
            statusTextView.setText(getString(R.string.loadingStatus_downloadingData));
            Log.d(RHCareerFairLayout.RH_CFL, getString(R.string.loadingStatus_downloadingData));

            // Set result handlers for success, fail and exceptions.
            Runnable successHandler = new Runnable() {
                @Override
                public void run() {
                    doneLoadingData();
                }
            };

            Runnable exceptionHandler = new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            statusTextView.setText(getString(R.string.loadingStatus_errorDownloadingData));
                            retryButton.setVisibility(View.VISIBLE);
                        }
                    });
                }
            };

            Runnable failHandler = new Runnable() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            statusTextView.setText(getString(R.string.loadingStatus_errorParsingData));
                            retryButton.setVisibility(View.VISIBLE);
                        }
                    });
                }
            };

            // Create new request with the result handlers above, and add to request queue.
            GetAllDataRequest req = new GetAllDataRequest(successHandler, exceptionHandler, failHandler);
            ConnectionManager.enqueueRequest(req);
        }
    }

    private void doneLoadingData() {
        // Go to next activity; do not cache this one.
        Intent launchNextActivity;
        launchNextActivity = new Intent(LoadingActivity.this, MainActivity.class);
        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(launchNextActivity);
    }
}
