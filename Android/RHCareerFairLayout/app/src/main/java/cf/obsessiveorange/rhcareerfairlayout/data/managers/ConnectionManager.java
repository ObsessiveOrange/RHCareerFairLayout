package cf.obsessiveorange.rhcareerfairlayout.data.managers;

import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import cf.obsessiveorange.rhcareerfairlayout.RHCareerFairLayout;

/**
 * Helper class to manage HTTP connections. Allows for multiple concurrent connections from thread pool.
 *
 * Created by Benedict on 7/14/2015.
 */
public class ConnectionManager {
    private static final int MAX_CONN_THREADS = 5;
    private static final long THREAD_IDLE_TIME = 10L;

    private static ExecutorService executorService = new ThreadPoolExecutor(1, MAX_CONN_THREADS, THREAD_IDLE_TIME, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());

    static {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
    }

    public static void enqueueRequest(Request r) {
        executorService.submit(r);
    }

    public static class ResponseHandler

    {
        public void handleSuccess(String response){

        }

        public void handleFailure(String response){

        }

        public void handleException(Exception e){

        }
    }

    public static class Request implements Runnable {

        private String url;
        private HTTPMethod method = HTTPMethod.GET;
        private HashMap<String, String> queryParams, headerParams, bodyParams;
        private ResponseHandler responseHandler = new ResponseHandler();

        @Override
        public void run() {
            OutputStream out = null;
            InputStream in = null;

            try {
                StringBuilder urlBuilder = new StringBuilder();
                urlBuilder.append(url);

                // Set query parameters
                if(queryParams != null) {
                    urlBuilder.append("?");
                    for (Map.Entry<String, String> queryParam : queryParams.entrySet()) {
                        if(urlBuilder.length() != 1){
                            urlBuilder.append("&");
                        }
                        urlBuilder.append(queryParam.getKey());
                        urlBuilder.append("=");
                        urlBuilder.append(queryParam.getValue());
                    }
                }

                //Build URL and open connection
                HttpURLConnection urlConnection = (HttpURLConnection) new URL(urlBuilder.toString()).openConnection();
                urlConnection.setRequestMethod(method.getValue());

                // Set header parameters
                if(headerParams != null) {
                    for (Map.Entry<String, String> headerParam : headerParams.entrySet()) {
                        urlConnection.setRequestProperty(headerParam.getKey(), headerParam.getValue());
                    }
                }

                //OutputStream out = new BufferedOutputStream(urlConnection.getOutputStream());
                in = new BufferedInputStream(urlConnection.getInputStream());

                BufferedReader r = new BufferedReader(new InputStreamReader(in));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = r.readLine()) != null) {
                    response.append(line);
                }

                if (urlConnection.getResponseCode() < 400 && responseHandler != null) {
                    Log.d(RHCareerFairLayout.RH_CFL, "Successfully retrieved data");
                    responseHandler.handleSuccess(response.toString());
                } else if(urlConnection.getResponseCode() >= 400 && responseHandler != null) {
                    Log.d(RHCareerFairLayout.RH_CFL, "Failed to retrieve data");
                    responseHandler.handleFailure(response.toString());
                }
            } catch (Exception e) {
                Log.d(RHCareerFairLayout.RH_CFL, "Exception thrown during retrieval or processing of data", e);
                responseHandler.handleException(e);
            } finally {
                if (out != null) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        Log.d(RHCareerFairLayout.RH_CFL, "Error closing output stream");
                        e.printStackTrace();
                    }
                }
                if (in != null) {
                    try {
                        in.close();
                    } catch (IOException e) {
                        Log.d(RHCareerFairLayout.RH_CFL, "Error closing input stream");
                        e.printStackTrace();
                    }
                }
            }
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public HTTPMethod getMethod() {
            return method;
        }

        public void setMethod(HTTPMethod method) {
            this.method = method;
        }

        public HashMap<String, String> getQueryParams() {
            return queryParams;
        }

        public void setQueryParams(HashMap<String, String> queryParams) {
            this.queryParams = queryParams;
        }

        public HashMap<String, String> getHeaderParams() {
            return headerParams;
        }

        public void setHeaderParams(HashMap<String, String> headerParams) {
            this.headerParams = headerParams;
        }

        public HashMap<String, String> getBodyParams() {
            return bodyParams;
        }

        public void setBodyParams(HashMap<String, String> bodyParams) {
            this.bodyParams = bodyParams;
        }

        public ResponseHandler getResponseHandler() {
            return responseHandler;
        }

        public void setResponseHandler(ResponseHandler responseHandler) {
            this.responseHandler = responseHandler;
        }

        public enum HTTPMethod {
            POST("POST"),
            GET("GET");
            private String value;

            private HTTPMethod(String HTTPMethod) {
                this.value = HTTPMethod;
            }

            public String getValue() {
                return value;
            }
        }
    }
}
