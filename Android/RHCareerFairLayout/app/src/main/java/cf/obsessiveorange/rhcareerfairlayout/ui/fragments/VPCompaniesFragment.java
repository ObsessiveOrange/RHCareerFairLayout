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

package cf.obsessiveorange.rhcareerfairlayout.ui.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;
import com.joanzapata.android.iconify.IconDrawable;
import com.joanzapata.android.iconify.Iconify;

import java.sql.SQLException;

import cf.obsessiveorange.rhcareerfairlayout.R;
import cf.obsessiveorange.rhcareerfairlayout.RHCareerFairLayout;
import cf.obsessiveorange.rhcareerfairlayout.data.managers.DBManager;
import cf.obsessiveorange.rhcareerfairlayout.ui.activities.MainActivity;
import cf.obsessiveorange.rhcareerfairlayout.ui.adapters.CompaniesCellAdapter;

/**
 * Fragment for ViewPagerTabFragmentActivity.
 * ScrollView callbacks are handled by its parent fragment, not its parent activity.
 */
public class VPCompaniesFragment extends BaseFragment {

    private View mView;
    private Thread companySelectionChangedWatcher;
    private ObservableRecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_companies, container, false);

        setHasOptionsMenu(true);

        recyclerView = (ObservableRecyclerView) mView.findViewById(R.id.scroll);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(false);

        recyclerView.setAdapter(new CompaniesCellAdapter(getActivity()));

        Fragment parentFragment = getParentFragment();
        ViewGroup viewGroup = (ViewGroup) parentFragment.getView();
        if (viewGroup != null) {
            recyclerView.setTouchInterceptionViewGroup((ViewGroup) viewGroup.findViewById(R.id.container));
            if (parentFragment instanceof ObservableScrollViewCallbacks) {
                recyclerView.setScrollViewCallbacks((ObservableScrollViewCallbacks) parentFragment);
            }
        }

        final String searchText = ((MainActivity) getActivity()).getSearchString();
        final TextView notificationTextView = (TextView) mView.findViewById(R.id.companies_txt_notificationBox);

        if (searchText != null && !searchText.isEmpty()) {
            notificationTextView.setText(getResources().getString(R.string.notification_Search, searchText));
            notificationTextView.setVisibility(View.VISIBLE);
        } else {
            notificationTextView.setVisibility(View.GONE);
        }

        return mView;
    }

    @Override
    public void onResume() {


        companySelectionChangedWatcher = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        synchronized (RHCareerFairLayout.refreshCompaniesNotifier) {
                            if (!RHCareerFairLayout.refreshCompaniesNotifier.hasChanged()) {
                                RHCareerFairLayout.refreshCompaniesNotifier.wait();
                                if (!RHCareerFairLayout.refreshCompaniesNotifier.hasChanged()) {
                                    continue;
                                }
                            }
                        }
                        ((CompaniesCellAdapter) recyclerView.getAdapter()).refreshData();

                        final String searchText = ((MainActivity) getActivity()).getSearchString();
                        final TextView notificationTextView = (TextView) mView.findViewById(R.id.companies_txt_notificationBox);

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (searchText != null && !searchText.isEmpty()) {
                                    notificationTextView.setText(getResources().getString(R.string.notification_Search, searchText));
                                    notificationTextView.setVisibility(View.VISIBLE);
                                } else {
                                    notificationTextView.setVisibility(View.GONE);
                                }
                            }
                        });

                        synchronized (RHCareerFairLayout.refreshMapNotifier) {
                            RHCareerFairLayout.refreshMapNotifier.notifyChanged();
                        }
                    }
                    Log.d(RHCareerFairLayout.RH_CFL, "companySelectionChangedWatcher thread stopped.");
                    companySelectionChangedWatcher = null;
                } catch (InterruptedException e) {
                    Log.d(RHCareerFairLayout.RH_CFL, "companySelectionChangedWatcher thread stopped.");
                    companySelectionChangedWatcher = null;
                }
            }
        });
        companySelectionChangedWatcher.start();

        super.onResume();
    }

    @Override
    public void onPause() {
        if (companySelectionChangedWatcher != null) {
            companySelectionChangedWatcher.interrupt();
        }
        super.onPause();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        MenuItem searchBtn = menu.findItem(R.id.action_search);
        searchBtn.setVisible(true);

        SubMenu selectionMenu = menu.addSubMenu(getResources().getString(R.string.selection_options_btn));
        selectionMenu.getItem().setIcon(
                new IconDrawable(
                        this.getActivity(),
                        Iconify.IconValue.fa_edit)
                        .colorRes(R.color.accentNoTransparency)
                        .actionBarSize());
        selectionMenu.getItem().setTitle(getResources().getString(R.string.selection_options_btn));
        selectionMenu.getItem().setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);


        MenuItem selectAllItem = selectionMenu.add(getResources().getString(R.string.btn_select_all));
        selectAllItem.setIcon(
                new IconDrawable(
                        this.getActivity(),
                        Iconify.IconValue.fa_check_square_o)
                        .colorRes(R.color.accentNoTransparency)
                        .actionBarSize());
        selectAllItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        selectAllItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(getActivity(), "Selected all items", Toast.LENGTH_SHORT).show();

                try {
                    DBManager.setFilteredCompaniesSelected(true);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                ((CompaniesCellAdapter) recyclerView.getAdapter()).refreshData();

                synchronized (RHCareerFairLayout.refreshMapNotifier) {
                    RHCareerFairLayout.refreshMapNotifier.notifyChanged();
                }

                return true;
            }
        });


        MenuItem deselectAllItem = selectionMenu.add(getResources().getString(R.string.btn_deselect_all));
        deselectAllItem.setIcon(
                new IconDrawable(
                        this.getActivity(),
                        Iconify.IconValue.fa_square_o)
                        .colorRes(R.color.accentNoTransparency)
                        .actionBarSize());
        deselectAllItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_IF_ROOM);
        deselectAllItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Toast.makeText(getActivity(), "Deselected all items", Toast.LENGTH_SHORT).show();

                try {
                    DBManager.setFilteredCompaniesSelected(false);
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                ((CompaniesCellAdapter) recyclerView.getAdapter()).refreshData();

                synchronized (RHCareerFairLayout.refreshMapNotifier) {
                    RHCareerFairLayout.refreshMapNotifier.notifyChanged();
                }

                return true;
            }
        });
    }
}
