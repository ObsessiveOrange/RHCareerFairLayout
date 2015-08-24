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
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;
import com.quinny898.library.persistentsearch.SearchBox;
import com.quinny898.library.persistentsearch.SearchBox.MenuListener;
import com.quinny898.library.persistentsearch.SearchBox.SearchListener;

import java.util.Stack;

import cf.obsessiveorange.rhcareerfairlayout.R;
import cf.obsessiveorange.rhcareerfairlayout.RHCareerFairLayout;
import cf.obsessiveorange.rhcareerfairlayout.data.managers.ConnectionManager;
import cf.obsessiveorange.rhcareerfairlayout.data.managers.DBManager;
import cf.obsessiveorange.rhcareerfairlayout.data.models.Term;
import cf.obsessiveorange.rhcareerfairlayout.data.requests.GetAllDataRequest;
import cf.obsessiveorange.rhcareerfairlayout.ui.fragments.VPLayoutContainerFragment;
import cf.obsessiveorange.rhcareerfairlayout.ui.fragments.VPParentFragment;

/**
 * This activity just provides a toolbar.
 * Toolbar is manipulated by VPParentFragment.
 */
public class MainActivity extends AppCompatActivity {

    public static MainActivity instance;

    private SearchBox search;
    private Toolbar toolbar;

    private Stack<Integer> backStack = new Stack<Integer>();
    private boolean pressedBack = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        instance = this;
//        Pull using: adb pull /sdcard/RHCareerFairLayoutTrace.trace "D:\1. Work\Workspaces\Java Workspace\RHCareerFairLayout\android\RHCareerFairLayout"
//        Debug.startMethodTracing("RHCareerFairLayoutTrace");

        setContentView(R.layout.activity_main);

        DBManager.setupDBAdapterIfNeeded(this);

        // Setup search, without suggestions, and defaulting to last searched string.
        search = (SearchBox) findViewById(R.id.searchbox);
        search.enableVoiceRecognition(this);
        search.setSearchWithoutSuggestions(true);
        search.setSearchString(getSearchString());

        // Setup toolbar
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Setup title
        Term term = DBManager.getTerm();
        if (term == null) {
            reloadData();
            return;
        } else {
            ((TextView) findViewById(R.id.main_txt_title)).setText(getString(R.string.career_fair_format, term.getQuarter(), term.getYear()));
        }

        // Setup parent fragment with tabPager
        FragmentManager fm = getSupportFragmentManager();
        if (fm.findFragmentByTag(RHCareerFairLayout.PARENT_FRAGMENT_TAG) == null) {
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.main_frg_body, new VPParentFragment(),
                    RHCareerFairLayout.PARENT_FRAGMENT_TAG);
            ft.commit();
            fm.executePendingTransactions();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflates default menu, adds search icon (no drawable found, using IconDrawable library.)
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
        // Handle option item selections.
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

                                reloadData();

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
        // Setup DBAdapter - may have been closed previously.
        DBManager.setupDBAdapterIfNeeded(this);
        super.onResume();
    }

    @Override
    protected void onPause() {
        // Release resources & close adapter.
        DBManager.close();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
//        Debug.stopMethodTracing()

        super.onDestroy();
    }

    /**
     * ActivityResults will usually be coming from either the detail activity's find on map button.
     * the
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {


        VPParentFragment fragmentParent = (VPParentFragment) getSupportFragmentManager().findFragmentById(R.id.main_frg_body);

        switch (requestCode) {
            case RHCareerFairLayout.REQUEST_CODE_FIND_ON_MAP:
                if (resultCode == RESULT_OK) {
                    // Go to Layout fragment.
                    fragmentParent.getPager().setCurrentItem(0);

                    // Check if current fragment is Layout fragment
                    Fragment fragment = fragmentParent.getCurrentFragment();
                    if (fragment instanceof VPLayoutContainerFragment) {
                        VPLayoutContainerFragment mapFragment = (VPLayoutContainerFragment) fragment;
                        long companyId = data.getLongExtra(RHCareerFairLayout.INTENT_KEY_SELECTED_COMPANY, -1);
                        if (companyId == -1) {
                            throw new IllegalStateException("Invalid companyId provided back to map.");
                        }
                        mapFragment.flashCompany(companyId);
                    }
                }
                break;
//            case 1234:
//                if (resultCode == RESULT_OK) {
//                    ArrayList<String> matches = data
//                            .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//                    search.populateEditText(matches);
//                }
//                super.onActivityResult(requestCode, resultCode, data);
            default:
                break;
        }
    }

    public void openSearch() {
        // Negate toolbar title, reveal search and add listeners.
        toolbar.setTitle("");
        search.revealFromMenuItem(R.id.action_search, this);
        search.setMenuListener(new MenuListener() {

            @Override
            public void onMenuClick() {
                closeSearch();
            }

        });
        search.setSearchListener(new SearchListener() {

            @Override
            public void onSearchOpened() {
                // No specific setup needed
            }

            @Override
            public void onSearchClosed() {
                // Only close if search is already open - prevents bug where opening search immediately closes it.
                if (search.getSearchOpen()) {
                    closeSearch();
                }
            }

            @Override
            public void onSearchTermChanged(String term) {

                // Immediately save the search string
                saveSearchString();

                // Refresh the companies list view.
                synchronized (RHCareerFairLayout.refreshCompaniesNotifier) {
                    RHCareerFairLayout.refreshCompaniesNotifier.notifyChanged();
                }
            }

            @Override
            public void onSearch(String searchTerm) {

                // Immediately save the search string
                saveSearchString();

                // Refresh the companies list view.
                synchronized (RHCareerFairLayout.refreshCompaniesNotifier) {
                    RHCareerFairLayout.refreshCompaniesNotifier.notifyChanged();
                }
            }

            @Override
            public void onSearchCleared() {

                // Immediately clear the search string
                saveSearchString();

                // Refresh the companies list view.
                synchronized (RHCareerFairLayout.refreshCompaniesNotifier) {
                    RHCareerFairLayout.refreshCompaniesNotifier.notifyChanged();
                }
            }

        });

    }

    public String getSearchString() {
        SharedPreferences prefs = MainActivity.this.getSharedPreferences(
                RHCareerFairLayout.RH_CFL,
                Context.MODE_PRIVATE);

        return prefs.getString(RHCareerFairLayout.PREF_KEY_SEARCH_STRING, "");
    }

    public SearchBox getSearch() {
        return search;
    }

    /**
     * Close the search bar, which hides keyboard as well.
     */
    public void closeSearch() {

        // Make sure search string is saved
        saveSearchString();

        // Hide search bar.
        search.hideCircularlyToMenuItem(R.id.action_search, this);
    }

    /**
     * Clear search focus, then hide keyboard.
     */
    public void clearSearchFocus() {
        if (search.hasFocus()) {
            search.clearFocus();
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(search.getWindowToken(), 0);
    }

    /**
     * Handles back presses - changes action based on whta is visible
     */
    @Override
    public void onBackPressed() {

        // Preferentially close search
        if (search.isShown()) {
            closeSearch();
            return;
        }

        // Then navigate down the backStack.
        if (!backStack.isEmpty()) {

            // Ignore next push to backStack - we caused it here.
            pressedBack = true;

            // Get last position, and send viewPager to that.
            int newPosition = backStack.pop();
            VPParentFragment fragmentParent = (VPParentFragment) getSupportFragmentManager().findFragmentById(R.id.main_frg_body);
            fragmentParent.getPager().setCurrentItem(newPosition);

        }
    }

    /**
     * Add current view to backStack, for later navigation back down it.
     * @param position The integer position of the current item.
     */
    public void pushToBackStack(int position) {
        while (backStack.size() > 10) {
            backStack.remove(0);
        }

        if (!pressedBack) {
            backStack.push(position);
        }
        pressedBack = false;
    }

    /**
     * Save search string to SharedPreferences.
     */
    private void saveSearchString() {
        SharedPreferences prefs = MainActivity.this.getSharedPreferences(
                RHCareerFairLayout.RH_CFL,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(RHCareerFairLayout.PREF_KEY_SEARCH_STRING, search.getSearchText());
        editor.apply();
    }


    /**
     * Private helper method to reload data, presenting dialog on failure.
     */
    private void reloadData() {

        final ProgressDialog progressDialog = ProgressDialog.show(MainActivity.this, "", "Loading...");

        // If successful, notify all that that refreshed.
        Runnable successHandler = new Runnable() {
            @Override
            public void run() {
                synchronized (RHCareerFairLayout.refreshCompaniesNotifier) {
                    RHCareerFairLayout.refreshCompaniesNotifier.notifyChanged();
                }
                synchronized (RHCareerFairLayout.refreshMapNotifier) {
                    RHCareerFairLayout.refreshMapNotifier.notifyChanged();
                }

                progressDialog.dismiss();
            }
        };

        // If exception or failure, show error message, and ask if they want to retry.
        Runnable exceptionHandler = new Runnable() {
            @Override
            public void run() {

                progressDialog.dismiss();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        DialogFragment df = new DialogFragment() {
                            @Override
                            public Dialog onCreateDialog(Bundle savedInstanceState) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Error");
                                builder.setMessage(getResources().getString(R.string.loadingStatus_errorDownloadingData));
                                builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        dismiss();

                                        reloadData();

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
                    }
                });
            }
        };

        Runnable failHandler = new Runnable() {
            @Override
            public void run() {

                progressDialog.dismiss();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        DialogFragment df = new DialogFragment() {
                            @Override
                            public Dialog onCreateDialog(Bundle savedInstanceState) {
                                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Error");
                                builder.setMessage(getResources().getString(R.string.loadingStatus_errorParsingData));
                                builder.setPositiveButton("Retry", new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                        dismiss();

                                        reloadData();

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
                    }
                });
            }
        };

        GetAllDataRequest req = new GetAllDataRequest(successHandler, exceptionHandler, failHandler);

        Log.d(RHCareerFairLayout.RH_CFL, "Data not saved or outdated. Downloading.");
        ConnectionManager.enqueueRequest(req);
    }
}
