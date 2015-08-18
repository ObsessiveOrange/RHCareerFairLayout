package cf.obsessiveorange.rhcareerfairlayout.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;
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

        final Timer timer = new Timer();

        DBManager.setupDBAdapterIfNeeded(this);

        statusTextView.setText(getString(R.string.loadingStatus_checkingForNewData));
        retryButton.setVisibility(View.INVISIBLE);

        // TODO: Get last update time from server
        Long localUpdateTime = DBManager.getLastUpdateTime();

        // TODO: Change this logic to factor in server update times.
        if (localUpdateTime != null && (System.currentTimeMillis() - localUpdateTime) < TimeUnit.DAYS.toMillis(RHCareerFairLayout.DATA_CACHE_TIME_IN_DAYS)) {
            statusTextView.setText(getString(R.string.loadingStatus_dataUpToDate));
            Log.d(RHCareerFairLayout.RH_CFL, "Data already downloaded, skipping retrieval.");

            TimerTask doneLoadingData = new TimerTask() {
                @Override
                public void run() {
                    doneLoadingData();
                }
            };

            timer.schedule(doneLoadingData, 500);

        } else {
            statusTextView.setText(getString(R.string.loadingStatus_downloadingData));
            Log.d(RHCareerFairLayout.RH_CFL, "Data not saved or outdated. Downloading.");

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

            GetAllDataRequest req = new GetAllDataRequest(successHandler, exceptionHandler, failHandler);

            Log.d(RHCareerFairLayout.RH_CFL, "Data not saved or outdated. Downloading.");
            ConnectionManager.enqueueRequest(req);
        }
    }

    private void doneLoadingData(){
        Intent launchNextActivity;
        launchNextActivity = new Intent(LoadingActivity.this, MainActivity.class);
        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(launchNextActivity);
    }
}
