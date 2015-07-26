package cf.obsessiveorange.rhcareerfairlayout;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import cf.obsessiveorange.rhcareerfairlayout.data.DBAdapter;
import cf.obsessiveorange.rhcareerfairlayout.data.models.managers.ConnectionManager;
import cf.obsessiveorange.rhcareerfairlayout.data.models.wrappers.DataWrapper;


public class Loading extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading);

        final TextView statusTextView = (TextView)findViewById(R.id.loading_status_textview);
        final Timer timer = new Timer();

        final String requestYear = "2015";
        final String requestQuarter = "Fall";

        DBAdapter.setupDBAdapter(this);

        statusTextView.setText(getString(R.string.loadingStatus_checkingForNewData));

        // TODO: Get last update time from server
        Long lastUpdateTime = DBAdapter.getLastUpdateTime(requestYear, requestQuarter);

        // TODO: Change this logic to factor in last update times.
        if(lastUpdateTime != null){
            statusTextView.setText(getString(R.string.loadingStatus_dataUpToDate));
            Log.d(RHCareerFairLayout.RH_CFL, "Data already downloaded, skipping retrieval.");

            TimerTask doneLoadingData = new TimerTask() {
                @Override
                public void run() {
                    doneLoadingData();
                }
            };

            timer.schedule(doneLoadingData, 1000);

        }
        else {
            statusTextView.setText(getString(R.string.loadingStatus_downloadingData));
            Log.d(RHCareerFairLayout.RH_CFL, "Data not saved or outdated. Downloading.");

            ConnectionManager.Request req = new ConnectionManager.Request();
            req.setUrl("http://192.168.2.30:8080/api/data/all");
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
                        Log.e(RHCareerFairLayout.RH_CFL, "Error occurred during deserialization", e);
                        e.printStackTrace();
                        return;
                    }
                    Log.d(RHCareerFairLayout.RH_CFL, "Object deserialized successfully");

                    DBAdapter.loadNewData(dataWrapper);

                    Log.d(RHCareerFairLayout.RH_CFL, "Object input into DB successfully");

                    doneLoadingData();
                }

                @Override
                public void handleException(Exception e) {
                    statusTextView.setText(getString(R.string.loadingStatus_errorDownloadingData));
                    // TODO: Retry button
                }

                @Override
                public void handleFailure(String response) {
                    statusTextView.setText(getString(R.string.loadingStatus_errorDownloadingData));
                    // TODO: Retry button
                }
            });

            ConnectionManager.enqueueRequest(req);
        }
    }

    public void doneLoadingData(){
        Intent launchNextActivity;
        launchNextActivity = new Intent(this, MainActivity.class);
        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        launchNextActivity.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        startActivity(launchNextActivity);
    }
}
