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
import android.view.View;
import android.view.ViewGroup;

import com.github.ksoichiro.android.observablescrollview.ObservableRecyclerView;
import com.github.ksoichiro.android.observablescrollview.ObservableScrollViewCallbacks;

import cf.obsessiveorange.rhcareerfairlayout.R;
import cf.obsessiveorange.rhcareerfairlayout.RHCareerFairLayout;
import cf.obsessiveorange.rhcareerfairlayout.data.DBAdapter;
import cf.obsessiveorange.rhcareerfairlayout.ui.BaseFragment;
import cf.obsessiveorange.rhcareerfairlayout.ui.adapters.CompaniesCellAdapter;

/**
 * Fragment for ViewPagerTabFragmentActivity.
 * ScrollView callbacks are handled by its parent fragment, not its parent activity.
 */
public class VPCompaniesFragment extends BaseFragment {

    Thread companySelectionChangedWatcher;
    ObservableRecyclerView recyclerView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recyclerview, container, false);

        recyclerView = (ObservableRecyclerView) view.findViewById(R.id.scroll);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setHasFixedSize(false);

        recyclerView.setAdapter(new CompaniesCellAdapter(getActivity(), DBAdapter.getFilteredCompaniesCursor()));

        Fragment parentFragment = getParentFragment();
        ViewGroup viewGroup = (ViewGroup) parentFragment.getView();
        if (viewGroup != null) {
            recyclerView.setTouchInterceptionViewGroup((ViewGroup) viewGroup.findViewById(R.id.container));
            if (parentFragment instanceof ObservableScrollViewCallbacks) {
                recyclerView.setScrollViewCallbacks((ObservableScrollViewCallbacks) parentFragment);
            }
        }

        return view;
    }

    @Override
    public void onResume() {


        companySelectionChangedWatcher = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        synchronized (RHCareerFairLayout.categorySelectionChanged) {
                            RHCareerFairLayout.categorySelectionChanged.wait();
                            if (!RHCareerFairLayout.categorySelectionChanged.hasChanged()) {
                                continue;
                            }
                        }
                        ((CompaniesCellAdapter)recyclerView.getAdapter()).changeCursor(DBAdapter.getFilteredCompaniesCursor());
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                recyclerView.getAdapter().notifyDataSetChanged();
                            }
                        });
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
}
