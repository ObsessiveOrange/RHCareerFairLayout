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

package cf.obsessiveorange.rhcareerfairlayout.ui.activities;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchBox.MenuListener;
import com.quinny898.library.persistentsearch.SearchBox.SearchListener;

import java.util.ArrayList;

import cf.obsessiveorange.rhcareerfairlayout.R;
import cf.obsessiveorange.rhcareerfairlayout.RHCareerFairLayout;
import cf.obsessiveorange.rhcareerfairlayout.data.managers.DBManager;
import cf.obsessiveorange.rhcareerfairlayout.data.models.Term;
import cf.obsessiveorange.rhcareerfairlayout.ui.fragments.VPMapContainerFragment;
import cf.obsessiveorange.rhcareerfairlayout.ui.fragments.VPParentFragment;

/**
 * This activity just provides a toolbar.
 * Toolbar is manipulated by VPParentFragment.
 */
public class MainActivity extends BaseActivity {

    private SearchBox search;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

//        Pull using: adb pull /sdcard/RHCareerFairLayoutTrace.trace "D:\1. Work\Workspaces\Java Workspace\RHCareerFairLayout\android\RHCareerFairLayout"
//        Debug.startMethodTracing("RHCareerFairLayoutTrace");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DBManager.setupDBAdapterIfNeeded(this);


        search = (SearchBox) findViewById(R.id.searchbox);
        search.enableVoiceRecognition(this);
        search.setSearchWithoutSuggestions(true);
        search.setSearchString(getSearchString());

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        Term term = DBManager.getTerm();
        ((TextView) findViewById(R.id.career_fair_title)).setText(getString(R.string.career_fair_format, term.getQuarter(), term.getYear()));

        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(VPParentFragment.FRAGMENT_TAG) == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.fragment, new VPParentFragment(),
                    VPParentFragment.FRAGMENT_TAG);
            ft.commit();
            fm.executePendingTransactions();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        menu.findItem(R.id.action_search).setIcon(
                new IconDrawable(
                        this,
                        Iconify.IconValue.fa_search)
                        .colorRes(R.color.accentNoTransparency)
                        .actionBarSize());

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        DialogFragment df;

        switch (id) {
            case R.id.refresh_data:
                df = new DialogFragment() {
                    @Override
                    public Dialog onCreateDialog(Bundle savedInstanceState) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                        builder.setTitle("Reload Data?");
                        builder.setMessage("This will clear all your current results. Continue?");
                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                dismiss();

                                Intent reloadDataIntent = new Intent(getActivity(), LoadingActivity.class);
                                reloadDataIntent.putExtra(LoadingActivity.KEY_FORCE_REFRESH, true);
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
            case R.id.about:
                df = new DialogFragment() {
                    @Override
                    public Dialog onCreateDialog(Bundle savedInstanceState) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

                        builder.setTitle(getString(R.string.about));
                        builder.setMessage(getString(R.string.about_description));
                        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {

                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dismiss();
                            }
                        });
                        return builder.create();
                    }
                };
                df.show(getFragmentManager(), "");
                break;
            case R.id.action_search:
                openSearch();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        DBManager.setupDBAdapterIfNeeded(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        DBManager.setupDBAdapterIfNeeded(this);
    }

    @Override
    protected void onDestroy() {
//        Debug.stopMethodTracing();

        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        VPParentFragment fragmentParent = (VPParentFragment) getSupportFragmentManager().findFragmentById(R.id.fragment);

        switch (requestCode) {
            case RHCareerFairLayout.REQUEST_CODE_FIND_ON_MAP:
                if (resultCode == RESULT_OK) {
                    fragmentParent.getPager().setCurrentItem(0);
                    Fragment fragment = fragmentParent.getCurrentFragment();
                    if (fragment instanceof VPMapContainerFragment) {
                        VPMapContainerFragment mapFragment = (VPMapContainerFragment) fragment;
                        long companyId = data.getLongExtra(RHCareerFairLayout.INTENT_KEY_SELECTED_COMPANY, -1);
                        if (companyId == -1) {
                            throw new IllegalStateException("Invalid companyId provided back to map.");
                        }
                        mapFragment.flashCompany(companyId);
                    }
                }
                break;
            case 1234:
                if (resultCode == RESULT_OK) {
                    ArrayList<String> matches = data
                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    search.populateEditText(matches);
                }
                super.onActivityResult(requestCode, resultCode, data);
            default:
                break;
        }
    }

    public void openSearch() {
        toolbar.setTitle("");
        search.revealFromMenuItem(R.id.action_search, this);
//		for (int x = 0; x < 10; x++) {
//			SearchResult option = new SearchResult("Result "
//					+ Integer.toString(x), getResources().getDrawable(
//					R.drawable.ic_history));
//			search.addSearchable(option);
//		}
        search.setMenuListener(new MenuListener() {

            @Override
            public void onMenuClick() {
                closeSearch();
            }

        });
        search.setSearchListener(new SearchListener() {

            @Override
            public void onSearchOpened() {

            }

            @Override
            public void onSearchClosed() {
                if (search.getSearchOpen()) {
                    closeSearch();
                }
            }

            @Override
            public void onSearchTermChanged(String term) {
                synchronized (RHCareerFairLayout.refreshCompaniesNotifier) {
                    RHCareerFairLayout.refreshCompaniesNotifier.notifyChanged();
                }

                saveSearchString();
            }

            @Override
            public void onSearch(String searchTerm) {

                synchronized (RHCareerFairLayout.refreshCompaniesNotifier) {
                    RHCareerFairLayout.refreshCompaniesNotifier.notifyChanged();
                }

                saveSearchString();
            }

            @Override
            public void onSearchCleared() {

                synchronized (RHCareerFairLayout.refreshCompaniesNotifier) {
                    RHCareerFairLayout.refreshCompaniesNotifier.notifyChanged();
                }

                saveSearchString();
            }

        });

    }

    public void closeSearch() {
        search.hideCircularlyToMenuItem(R.id.action_search, this);
    }

    @Override
    public void onBackPressed() {
        if (search.isShown()) {
            closeSearch();
            return;
        }

        super.onBackPressed();
    }

    public String getSearchText() {
        return search.getSearchText();
    }

    public SearchBox getSearch() {
        return search;
    }

    public void setSearch(SearchBox search) {
        this.search = search;
    }

    public void enableSearch() {
        MenuItem searchBtn = toolbar.getMenu().getItem(R.id.action_search);
        searchBtn.setVisible(true);
    }

    public void disableSearch() {
        MenuItem searchBtn = toolbar.getMenu().getItem(R.id.action_search);
        searchBtn.setVisible(false);
    }

    private void saveSearchString() {
        SharedPreferences prefs = ((Context) MainActivity.this).getSharedPreferences(
                RHCareerFairLayout.RH_CFL,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        if (getSearchText() != null) {
            editor.putString(RHCareerFairLayout.PREF_KEY_SEARCH_STRING, getSearchText());
        }
        editor.apply();
    }

    private String getSearchString() {
        SharedPreferences prefs = ((Context) MainActivity.this).getSharedPreferences(
                RHCareerFairLayout.RH_CFL,
                Context.MODE_PRIVATE);

        return prefs.getString(RHCareerFairLayout.PREF_KEY_SEARCH_STRING, "");
    }
}
