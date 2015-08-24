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

package cf.obsessiveorange.rhcareerfairlayout.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import cf.obsessiveorange.rhcareerfairlayout.R;
import cf.obsessiveorange.rhcareerfairlayout.RHCareerFairLayout;
import cf.obsessiveorange.rhcareerfairlayout.data.managers.DBManager;
import cf.obsessiveorange.rhcareerfairlayout.data.models.Company;
import cf.obsessiveorange.rhcareerfairlayout.ui.activities.DetailActivity;
import cf.obsessiveorange.rhcareerfairlayout.ui.activities.MainActivity;
import cf.obsessiveorange.rhcareerfairlayout.ui.fragments.VPLayoutContainerFragment;
import cf.obsessiveorange.rhcareerfairlayout.ui.fragments.VPParentFragment;

public class CompaniesCellAdapter extends RecyclerView.Adapter<CompaniesCellAdapter.ViewHolder> {

    // Hold on to a CursorAdapter for handling of cursor reference - will automatically
    // clear cursor when done.
    Cursor mCursor;
    LayoutInflater mInflater;

    public CompaniesCellAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        refreshData();
    }

    /**
     * Get a new cursor, and force a refresh of the RecyclerView
     */
    public void refreshData() {
        changeCursor(DBManager.getFilteredCompaniesCursor());

        MainActivity.instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

    /**
     * Get a new cursor, and force a refresh of the RecyclerView, refreshing only the single item that changed.
     */
    public void refreshData(final int position) {
        changeCursor(DBManager.getFilteredCompaniesCursor());

        MainActivity.instance.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyItemChanged(position);
            }
        });
    }

    /**
     * Change cursor, and make sure previous one is closed properly.
     * @param cursor New cursor
     */
    public void changeCursor(Cursor cursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount();
    }

    /**
     * Bind new values to cell
     * @param holder ViewHolder for cell
     * @param position position of item.
     */
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // Call helper method.
        buildItem(holder, position);
    }

    /**
     * Builds an item cell
     * @param holder ViewHolder for cell
     * @param position position of item
     */
    private void buildItem(final ViewHolder holder, final int position) {
        if (mCursor.moveToPosition(position)) {
            // Get company object for cursor at position.
            final Company company = new Company(mCursor);

            // Get selected flag, and table number
            final Boolean selected = mCursor.getInt(mCursor.getColumnIndexOrThrow(DBManager.KEY_SELECTED)) > 0;
            final Long table = mCursor.getLong(mCursor.getColumnIndexOrThrow(DBManager.KEY_TABLE));

            // Update checkbox (remove listener first to prevent infinite loop).
            holder.showOnMapCheckBox.setOnCheckedChangeListener(null);
            holder.showOnMapCheckBox.setChecked(selected);

            // Set company name
            holder.companyNameTextView.setText(company.getName());

            // Set table number
            holder.tableNumberTextView.setText(table.toString());

            // Add onCheck/onClick listeners for when user taps on them.
            holder.showOnMapCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    Log.d(RHCareerFairLayout.RH_CFL, "Updating DB: Setting company " + company.getId() + " to " + isChecked);
                    MainActivity.instance.clearSearchFocus();
                    DBManager.setCompanySelected(company.getId(), isChecked);
                    synchronized (RHCareerFairLayout.refreshMapNotifier) {
                        RHCareerFairLayout.refreshMapNotifier.notifyChanged();
                    }
                    refreshData(position);
                }
            });

            holder.companyNameTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    MainActivity.instance.clearSearchFocus();
                    Intent detailIntent = new Intent(MainActivity.instance, DetailActivity.class);
                    detailIntent.putExtra(RHCareerFairLayout.INTENT_KEY_SELECTED_COMPANY, company.getId());
                    MainActivity.instance.startActivityForResult(detailIntent, RHCareerFairLayout.REQUEST_CODE_FIND_ON_MAP);

                }
            });

            holder.tableNumberTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    MainActivity.instance.clearSearchFocus();

                    VPParentFragment fragmentParent = (VPParentFragment) MainActivity.instance.getSupportFragmentManager().findFragmentById(R.id.main_frg_body);

                    fragmentParent.getPager().setCurrentItem(0);
                    Fragment fragment = fragmentParent.getCurrentFragment();
                    if (fragment instanceof VPLayoutContainerFragment) {
                        VPLayoutContainerFragment mapFragment = (VPLayoutContainerFragment) fragment;
                        long tableId = Long.valueOf(holder.tableNumberTextView.getText().toString());
                        if (tableId == -1) {
                            throw new IllegalStateException("Invalid tableId provided back to map.");
                        }
                        mapFragment.flashTable(tableId);
                    }
                    else{
                        Log.e(RHCareerFairLayout.RH_CFL, "ERROR: First fragment not layout fragment.");
                    }

                }
            });
        } else {
            Log.d(RHCareerFairLayout.RH_CFL, "Invalid cursor position detected while creating item cell: " + position);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        // Create a new ViewHolder for the cell.
        View v = mInflater.inflate(R.layout.cell_company, parent, false);
        return new ViewHolder(v);
    }

    /**
     * Use viewholder pattern for efficiency, reducing calls to findViewById
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout cellRoot;
        CheckBox showOnMapCheckBox;
        TextView companyNameTextView;
        TextView tableNumberTextView;

        public ViewHolder(View view) {
            super(view);
            cellRoot = (LinearLayout) view.findViewById(R.id.cell_root);
            showOnMapCheckBox = (CheckBox) view.findViewById(R.id.company_chk_showOnMap);
            companyNameTextView = (TextView) view.findViewById(R.id.company_txt_name);
            tableNumberTextView = (TextView) view.findViewById(R.id.company_txt_tableNumber);
        }
    }
}