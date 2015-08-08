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
import android.graphics.Typeface;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.sql.SQLException;
import java.util.Map;
import java.util.TreeMap;

import cf.obsessiveorange.rhcareerfairlayout.R;
import cf.obsessiveorange.rhcareerfairlayout.RHCareerFairLayout;
import cf.obsessiveorange.rhcareerfairlayout.data.managers.DBManager;
import cf.obsessiveorange.rhcareerfairlayout.data.models.Category;

public class FiltersCellAdapter extends RecyclerView.Adapter<FiltersCellAdapter.ViewHolder> {

    // Hold on to a CursorAdapter for handling of cursor reference - will automatically
    // clear cursor when done.
    Cursor mCursor;
    LayoutInflater mInflater;
    TreeMap<Integer, Integer> mOffsets = new TreeMap<Integer, Integer>();
    boolean headerLocationsGenerated = false;

    public FiltersCellAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        refreshData();
    }

    public void refreshData(){
        changeCursor(DBManager.getCategoriesCursor());
    }

    public void changeCursor(Cursor cursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;
        generateHeaderLocations();
    }

    private void generateHeaderLocations() {
        int headers = 0;
        for (int position = 0; position < mCursor.getCount() + headers; position++) {
            if (position == 0) {
                // New header
                Map.Entry<Integer, Integer> prevNumHeaders = mOffsets.lowerEntry(position);
                mOffsets.put(position, (prevNumHeaders == null ? 0 : prevNumHeaders.getValue()) + 1);
            }
            // If is not first item in section, check for headers.
            else if (mOffsets.get(position - 1) == null) {
                // Get offset based on how many section headers are above.
                int offsetPosition = position - mOffsets.lowerEntry(position).getValue();

                String prevType = mCursor.moveToPosition(offsetPosition - 1) ?
                        mCursor.getString(mCursor.getColumnIndexOrThrow(DBManager.KEY_TYPE)) :
                        null;
                String currType = mCursor.moveToPosition(offsetPosition) ?
                        mCursor.getString(mCursor.getColumnIndexOrThrow(DBManager.KEY_TYPE)) :
                        null;

                // If both types do not match, then it is an header.
                if (prevType == null || currType == null || !prevType.equalsIgnoreCase(currType)) {
                    // New header
                    Map.Entry<Integer, Integer> prevNumHeaders = mOffsets.lowerEntry(position);
                    mOffsets.put(position, (prevNumHeaders == null ? 0 : prevNumHeaders.getValue()) + 1);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount() + mOffsets.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // Get offset based on how many section headers are above.
        Map.Entry<Integer, Integer> entry = mOffsets.lowerEntry(position);
        int offset = entry == null ? 0 : entry.getValue();
        int offsetPosition = position - offset;

        // If it is the offsets map, it's a header
        if (mOffsets.get(position) != null) {
            buildHeader(holder, offsetPosition);
        }
        // Otherwise, it's an item.
        else {
            buildItem(holder, offsetPosition);
        }
    }

    private void buildHeader(final ViewHolder holder, int offsetPosition) {
        if (mCursor.moveToPosition(offsetPosition)) {
            final Category category = new Category(mCursor);

            holder.filterActiveCheckbox.setVisibility(View.GONE);

            holder.filterNameTextView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            holder.filterNameTextView.setTypeface(null, Typeface.BOLD);
            holder.filterNameTextView.setText(category.getType() + "s");

            holder.cellRoot.setBackgroundColor(Color.LTGRAY);
        } else {
            Log.d(RHCareerFairLayout.RH_CFL, "Invalid cursor position detected while creating header cell: " + offsetPosition);
        }
    }

    private void buildItem(final ViewHolder holder, int offsetPosition) {
        if (mCursor.moveToPosition(offsetPosition)) {
            final Category category = new Category(mCursor);
            final Boolean selected = mCursor.getInt(mCursor.getColumnIndexOrThrow(DBManager.KEY_SELECTED)) > 0;

            holder.filterActiveCheckbox.setVisibility(View.VISIBLE);
            holder.filterActiveCheckbox.setChecked(selected);

//                v.setImageURI(Uri.parse(value));

            holder.filterNameTextView.setGravity(Gravity.CENTER);
            holder.filterNameTextView.setTypeface(null, Typeface.NORMAL);
            holder.filterNameTextView.setText(category.getName());

            holder.cellRoot.setBackgroundColor(Color.WHITE);

            holder.cellRoot.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    boolean current = holder.filterActiveCheckbox.isChecked();
                    Log.d(RHCareerFairLayout.RH_CFL, "Updating DB: Setting category " + category.getId() + " to " + !current);
                    try {
                        DBManager.setCategorySelected(category.getId(), !current);
                    } catch (SQLException e) {
                        Log.d(RHCareerFairLayout.RH_CFL, "Error updating selected categories", e);
                    }
                    synchronized (RHCareerFairLayout.categorySelectionChanged) {
                        RHCareerFairLayout.categorySelectionChanged.notifyChanged();
                    }
                    holder.filterActiveCheckbox.setChecked(!current);
                    refreshData();
                }
            });

            holder.cellRoot.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    v.setBackgroundColor(Color.GREEN);
                    return true;
                }
            });
        } else {
            Log.d(RHCareerFairLayout.RH_CFL, "Invalid cursor position detected while creating item cell: " + offsetPosition);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = mInflater.inflate(R.layout.cell_filter, parent, false);
        return new ViewHolder(v);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        LinearLayout cellRoot;
        CheckBox filterActiveCheckbox;
        TextView filterNameTextView;

        public ViewHolder(View view) {
            super(view);
            cellRoot = (LinearLayout) view.findViewById(R.id.cell_root);
            filterActiveCheckbox = (CheckBox) view.findViewById(R.id.filter_active);
            filterNameTextView = (TextView) view.findViewById(R.id.filter_name);
        }
    }

}