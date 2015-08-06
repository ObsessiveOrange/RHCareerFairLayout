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
import android.database.Cursor;
import android.graphics.Color;
import android.support.v4.widget.CursorAdapter;
import android.support.v4.widget.ResourceCursorAdapter;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import cf.obsessiveorange.rhcareerfairlayout.R;
import cf.obsessiveorange.rhcareerfairlayout.RHCareerFairLayout;
import cf.obsessiveorange.rhcareerfairlayout.data.DBAdapter;
import cf.obsessiveorange.rhcareerfairlayout.data.models.Company;

public class CompaniesCellAdapter extends RecyclerView.Adapter<CompaniesCellAdapter.ViewHolder> {

    // Hold on to a CursorAdapter for handling of cursor reference - will automatically
    // clear cursor when done.
    CursorAdapter mCursorAdapter;

    Context mContext;

    LayoutInflater mInflater;

    public CompaniesCellAdapter(Context context, Cursor c) {

        mContext = context;
        mInflater = LayoutInflater.from(mContext);

        mCursorAdapter = new ResourceCursorAdapter(mContext, R.layout.cell_company, c, 0) {

            @Override
            public void bindView(View view, Context context, Cursor cursor) {
                // Binding operations

                final CheckBox showOnMapCheckBox = (CheckBox) view.findViewById(R.id.show_on_map);
                final TextView companyNameTextView = (TextView) view.findViewById(R.id.company_name);
                final TextView tableNumberTextView = (TextView) view.findViewById(R.id.table_number);

                final Company company = new Company(cursor);
                final Long table = cursor.getLong(cursor.getColumnIndexOrThrow(DBAdapter.KEY_TABLE));
                final Boolean selected = cursor.getInt(cursor.getColumnIndexOrThrow(DBAdapter.KEY_SELECTED)) > 0;

                view.setBackgroundColor(Color.WHITE);
                showOnMapCheckBox.setChecked(selected);
                companyNameTextView.setText(company.getName());
                tableNumberTextView.setText(table.toString());
//                v.setImageURI(Uri.parse(value));

                view.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        boolean current = showOnMapCheckBox.isChecked();
                        Log.d(RHCareerFairLayout.RH_CFL, "Updating DB: Setting company " + company.getId() + " to " + !current);
                        DBAdapter.setCompanySelected(company.getId(), !current);
                        synchronized (RHCareerFairLayout.companySelectionChanged){
                            RHCareerFairLayout.companySelectionChanged.notifyChanged();
                        }
                        showOnMapCheckBox.setChecked(!current);
                        changeCursor(DBAdapter.getFilteredCompaniesCursor());
                    }
                });

                view.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        v.setBackgroundColor(Color.GREEN);
                        return true;
                    }
                });
            }
        };
    }

    @Override
    public int getItemCount() {
        return mCursorAdapter.getCount();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Passing the binding operation to cursor loader
        mCursorAdapter.getCursor().moveToPosition(position);
        mCursorAdapter.bindView(holder.itemView, mContext, mCursorAdapter.getCursor());
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Passing the inflater job to the cursor-adapter
        View v = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
        return new ViewHolder(v);
    }

    public void changeCursor(Cursor cursor){
        mCursorAdapter.changeCursor(cursor);
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