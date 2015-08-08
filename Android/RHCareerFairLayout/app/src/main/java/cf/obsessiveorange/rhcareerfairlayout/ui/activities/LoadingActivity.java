package cf.obsessiveorange.rhcareerfairlayout.ui.activities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import cf.obsessiveorange.rhcareerfairlayout.R;
import cf.obsessiveorange.rhcareerfairlayout.RHCareerFairLayout;
import cf.obsessiveorange.rhcareerfairlayout.data.managers.DBManager;
import cf.obsessiveorange.rhcareerfairlayout.data.managers.ConnectionManager;
import cf.obsessiveorange.rhcareerfairlayout.data.models.wrappers.DataWrapper;


public class LoadingActivity extends Activity {

    public static final String KEY_FORCE_REFRESH = "forceRefresh";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        boolean forceRefresh = getIntent().getBooleanExtra(KEY_FORCE_REFRESH, false);
        reloadData(forceRefresh);

    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void reloadData(boolean forceRefresh) {

        final TextView statusTextView = (TextView) findViewById(R.id.loading_status_textview);
        final Timer timer = new Timer();

        final String requestYear = "2015";
        final String requestQuarter = "Fall";

        DBManager.setupDBAdapterIfNeeded(this);

        statusTextView.setText(getString(R.string.loadingStatus_checkingForNewData));

        // TODO: Get last update time from server
        Long lastUpdateTime = DBManager.getLastUpdateTime(requestYear, requestQuarter);

        // TODO: Change this logic to factor in last update times.
        if (!forceRefresh && lastUpdateTime != null) {
            statusTextView.setText(getString(R.string.loadingStatus_dataUpToDate));
            Log.d(RHCareerFairLayout.RH_CFL, "Data already downloaded, skipping retrieval.");

            TimerTask doneLoadingData = new TimerTask() {
                @Override
                public void run() {
                    doneLoadingData();
                }
            };

            timer.schedule(doneLoadingData, 1000);

        } else {
            statusTextView.setText(getString(R.string.loadingStatus_downloadingData));
            Log.d(RHCareerFairLayout.RH_CFL, "Data not saved or outdated. Downloading.");

            ConnectionManager.Request req = new ConnectionManager.Request();
            req.setUrl(RHCareerFairLayout.URL_BASE + "/data/all");
            req.setMethod(ConnectionManager.Request.HTTPMethod.GET);
            req.setQueryParams(new HashMap<String, String>() {{
                put("year", requestYear);
                put("quarter", requestQuarter);
            }});
            req.setHeaderParams(null);
            req.setBodyParams(null);
            req.setResponseHandler(new ConnectionManager.ResponseHandler() {
                @Override
                public void handleSuccess(String response) {
                    ObjectMapper mapper = new ObjectMapper();
                    DataWrapper dataWrapper = null;
                    try {
                        dataWrapper = mapper.readValue(response, DataWrapper.class);
                    } catch (IOException e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                statusTextView.setText("Error occurred during deserialization");
                            }
                        });
                        Log.e(RHCareerFairLayout.RH_CFL, "Error occurred during deserialization", e);
                        e.printStackTrace();
                        return;
                    }
                    Log.d(RHCareerFairLayout.RH_CFL, "Object deserialized successfully");

                    try {
                        DBManager.loadNewData(dataWrapper);
                    } catch (SQLException e) {
                        Log.d(RHCareerFairLayout.RH_CFL, "SQL exception while loading data into DB", e);
                        finish();
                    }

                    Log.d(RHCareerFairLayout.RH_CFL, "Object input into DB successfully");

                    doneLoadingData();
                }

                @Override
                public void handleException(Exception e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            statusTextView.setText(getString(R.string.loadingStatus_errorDownloadingData));
                            // TODO: Retry button
                        }
                    });
                    Log.d(RHCareerFairLayout.RH_CFL, "Exception thrown while downloading data.", e);
                }

                @Override
                public void handleFailure(String response) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            statusTextView.setText(getString(R.string.loadingStatus_errorDownloadingData));
                            // TODO: Retry button
                        }
                    });
                    Log.d(RHCareerFairLayout.RH_CFL, "Failed to download data. Response: " + response);
                }
            });

            Log.d(RHCareerFairLayout.RH_CFL, "Data not saved or outdated. Downloading.");
            ConnectionManager.enqueueRequest(req);
        }
    }

    public void doneLoadingData() {
        Intent launchNextActivity;
        launchNextActivity = new Intent(this, MainActivity.class);
        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(launchNextActivity);
    }
}
