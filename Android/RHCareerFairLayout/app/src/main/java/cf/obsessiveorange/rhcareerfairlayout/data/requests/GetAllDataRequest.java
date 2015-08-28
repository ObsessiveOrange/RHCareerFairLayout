package cf.obsessiveorange.rhcareerfairlayout.data.requests;

import android.util.Log;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import cf.obsessiveorange.rhcareerfairlayout.RHCareerFairLayout;
import cf.obsessiveorange.rhcareerfairlayout.data.managers.ConnectionManager;
import cf.obsessiveorange.rhcareerfairlayout.data.managers.DBManager;
import cf.obsessiveorange.rhcareerfairlayout.data.models.wrappers.DataWrapper;

/**
 * Created by Benedict on 8/12/2015.
 */
public class GetAllDataRequest extends ConnectionManager.Request {

    Runnable mSuccessHandler;
    Runnable mExceptionHandler;
    Runnable mFailHandler;

    public GetAllDataRequest(Runnable successHandler, Runnable exceptionHandler, Runnable failHandler){
        super();

        mSuccessHandler = successHandler;
        mExceptionHandler = exceptionHandler;
        mFailHandler = failHandler;

//             For eventual support of historical data.
//            req.setUrl(RHCareerFairLayout.URL_BASE + "/data/all");
        setUrl(RHCareerFairLayout.URL_BASE + "/data/latest/all");
        setMethod(ConnectionManager.Request.HTTPMethod.GET);
//             For eventual support of historical data.
//            req.setQueryParams(new HashMap<String, String>() {{
//                put("year", requestYear);
//                put("quarter", requestQuarter);
//            }});
        setHeaderParams(null);
        setBodyParams(null);
        setResponseHandler(new ConnectionManager.ResponseHandler() {
            @Override
            public void handleSuccess(String response) {
                ObjectMapper mapper = new ObjectMapper();
                // NOTE:
                // This will allow for backward-compatibility, should any new fields be added.
                // However, it will also not warn if new fields are left unused.
                mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                DataWrapper dataWrapper = null;
                try {
                    dataWrapper = mapper.readValue(response, DataWrapper.class);
                    Log.d(RHCareerFairLayout.RH_CFL, "Object deserialized successfully");

                    DBManager.loadNewData(dataWrapper);
                    Log.d(RHCareerFairLayout.RH_CFL, "Object input into DB successfully");
                } catch (Exception e) {
                    handleException(e);
                    return;
                }

                mSuccessHandler.run();
            }

            @Override
            public void handleException(Exception e) {
                Log.e(RHCareerFairLayout.RH_CFL, "Exception thrown while downloading data.", e);

                mExceptionHandler.run();
            }

            @Override
            public void handleFailure(String response) {
                Log.e(RHCareerFairLayout.RH_CFL, "Failed to download data. Response: " + response);

                mFailHandler.run();
            }
        });
    }


}
