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

package cf.obsessiveorange.rhcareerfairlayout;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.Log;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.HashMap;

import cf.obsessiveorange.rhcareerfairlayout.data.DBAdapter;
import cf.obsessiveorange.rhcareerfairlayout.data.models.managers.ConnectionManager;
import cf.obsessiveorange.rhcareerfairlayout.data.models.wrappers.DataWrapper;
import cf.obsessiveorange.rhcareerfairlayout.ui.BaseActivity;
import cf.obsessiveorange.rhcareerfairlayout.ui.fragments.ViewPagerTabFragmentParentFragment;

/**
 * This activity just provides a toolbar.
 * Toolbar is manipulated by ViewPagerTabFragmentParentFragment.
 */
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(ViewPagerTabFragmentParentFragment.FRAGMENT_TAG) == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.fragment, new ViewPagerTabFragmentParentFragment(),
                    ViewPagerTabFragmentParentFragment.FRAGMENT_TAG);
            ft.commit();
            fm.executePendingTransactions();
        }
        //new RetrieveFeedTask().execute();

        DBAdapter.setupDBAdapter(this);
        DBAdapter.open();

        ConnectionManager.Request req = new ConnectionManager.Request();
        req.setUrl("http://192.168.2.30:8080/api/data/all");
        req.setMethod(ConnectionManager.Request.HTTPMethod.GET);
        req.setQueryParams(new HashMap<String, String>() {{
            put("year", "2015");
            put("quarter", "Fall");
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
            }
        });

        ConnectionManager.enqueueRequest(req);

    }
}
