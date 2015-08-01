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

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import cf.obsessiveorange.rhcareerfairlayout.data.DBAdapter;
import cf.obsessiveorange.rhcareerfairlayout.data.models.Term;
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

        DBAdapter.setupDBAdapterIfNeeded(this);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        Term term = DBAdapter.getTerm();
        ((TextView) findViewById(R.id.career_fair_title)).setText(getString(R.string.career_fair_format, term.getQuarter(), term.getYear()));

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(ViewPagerTabFragmentParentFragment.FRAGMENT_TAG) == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.fragment, new ViewPagerTabFragmentParentFragment(),
                    ViewPagerTabFragmentParentFragment.FRAGMENT_TAG);
            ft.commit();
            fm.executePendingTransactions();
        }
        //new RetrieveFeedTask().execute();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id){
            case R.id.refresh_data:
                DialogFragment df = new DialogFragment(){
                    @Override
                    public Dialog onCreateDialog(Bundle savedInstanceState) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Reload Data?");
                        builder.setMessage("This will clear all your current results. Continue?");
                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dismiss();

                                Intent reloadDataIntent = new Intent(getActivity(), Loading.class);
                                reloadDataIntent.putExtra(Loading.KEY_FORCE_REFRESH, true);
                                reloadDataIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                reloadDataIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                reloadDataIntent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                                startActivity(reloadDataIntent);
                            }
                        });
                        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dismiss();
                            }
                        });
                        return builder.create();
                    }
                };
                df.show(getFragmentManager(), null);
                break;
            default:
                break;
        }
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

        DBAdapter.setupDBAdapterIfNeeded(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        DBAdapter.setupDBAdapterIfNeeded(this);
    }

    @Override
    protected void onStop() {

        super.onStop();
    }
}
