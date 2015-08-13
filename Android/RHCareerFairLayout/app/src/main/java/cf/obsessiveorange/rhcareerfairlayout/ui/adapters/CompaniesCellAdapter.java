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

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import cf.obsessiveorange.rhcareerfairlayout.R;
import cf.obsessiveorange.rhcareerfairlayout.RHCareerFairLayout;
import cf.obsessiveorange.rhcareerfairlayout.data.managers.DBManager;
import cf.obsessiveorange.rhcareerfairlayout.data.models.Company;
import cf.obsessiveorange.rhcareerfairlayout.ui.activities.DetailActivity;

public class CompaniesCellAdapter extends RecyclerView.Adapter<CompaniesCellAdapter.ViewHolder> {

    // Hold on to a CursorAdapter for handling of cursor reference - will automatically
    // clear cursor when done.
    Cursor mCursor;
    Context mContext;
    LayoutInflater mInflater;

    public CompaniesCellAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        refreshData();
    }

    public void refreshData() {
        Cursor c = DBManager.getFilteredCompaniesCursor();

        changeCursor(DBManager.getFilteredCompaniesCursor());

        ((Activity)mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                notifyDataSetChanged();
            }
        });
    }

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

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        buildItem(holder, position);
    }

    private void buildItem(final ViewHolder holder, int position) {
        if (mCursor.moveToPosition(position)) {
            final Company company = new Company(mCursor);
            final Boolean selected = mCursor.getInt(mCursor.getColumnIndexOrThrow(DBManager.KEY_SELECTED)) > 0;
            final Long table = mCursor.getLong(mCursor.getColumnIndexOrThrow(DBManager.KEY_TABLE));

            holder.showOnMapCheckBox.setChecked(selected);

            holder.companyNameTextView.setText(company.getName());

            holder.tableNumberTextView.setText(table.toString());

//                v.setImageURI(Uri.parse(value));

            holder.cellRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean current = holder.showOnMapCheckBox.isChecked();
                    Log.d(RHCareerFairLayout.RH_CFL, "Updating DB: Setting company " + company.getId() + " to " + !current);
                    DBManager.setCompanySelected(company.getId(), !current);
                    synchronized (RHCareerFairLayout.refreshMapNotifier) {
                        RHCareerFairLayout.refreshMapNotifier.notifyChanged();
                    }
                    holder.showOnMapCheckBox.setChecked(!current);
                    refreshData();
                }
            });

            holder.cellRoot.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    Intent detailIntent = new Intent(mContext, DetailActivity.class);
                    detailIntent.putExtra(RHCareerFairLayout.INTENT_KEY_SELECTED_COMPANY, company.getId());
                    ((Activity)mContext).startActivityForResult(detailIntent, RHCareerFairLayout.REQUEST_CODE_FIND_ON_MAP);

                    return true;
                }
            });
        } else {
            Log.d(RHCareerFairLayout.RH_CFL, "Invalid cursor position detected while creating item cell: " + position);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = mInflater.inflate(R.layout.cell_company, parent, false);
        return new ViewHolder(v);
    }


    public static class ViewHolder extends RecyclerView.ViewHolder {
        LinearLayout cellRoot;
        CheckBox showOnMapCheckBox;
        TextView companyNameTextView;
        TextView tableNumberTextView;

        public ViewHolder(View view) {
            super(view);
            cellRoot = (LinearLayout) view.findViewById(R.id.cell_root);
            showOnMapCheckBox = (CheckBox) view.findViewById(R.id.show_on_map);
            companyNameTextView = (TextView) view.findViewById(R.id.company_name);
            tableNumberTextView = (TextView) view.findViewById(R.id.table_number);
        }
    }
}