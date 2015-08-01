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

import java.util.Map;
import java.util.TreeMap;

import cf.obsessiveorange.rhcareerfairlayout.R;
import cf.obsessiveorange.rhcareerfairlayout.RHCareerFairLayout;
import cf.obsessiveorange.rhcareerfairlayout.data.DBAdapter;
import cf.obsessiveorange.rhcareerfairlayout.data.models.Category;

public class SectionRecyclerAdapter extends RecyclerView.Adapter<SectionRecyclerAdapter.ViewHolder> {

    // Hold on to a CursorAdapter for handling of cursor reference - will automatically
    // clear cursor when done.
    Cursor mCursor;
    LayoutInflater mInflater;
    TreeMap<Integer, Integer> mOffsets;

    public SectionRecyclerAdapter(Context context) {

        mCursor = DBAdapter.getCategoriesCursor();
        mInflater = LayoutInflater.from(context);
        mOffsets = new TreeMap<Integer, Integer>();
    }

    public void changeCursor(Cursor cursor) {
        if (mCursor != null) {
            mCursor.close();
        }
        mCursor = cursor;
    }

    @Override
    public int getItemCount() {
        return mCursor.getCount() + mOffsets.size();
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        if (position == 0) {
            // Build header
            buildHeader(holder, position, 0);
        } else {
            // Get offset based on how many section headers are above.
            int offsetPosition = position - mOffsets.lowerEntry(position).getValue();

            // If first item in section, do not check, automatically an item
            if (mOffsets.get(position - 1) != null) {
                // Build item.
                buildItem(holder, offsetPosition);
            }
            // Can be header or item
            else {
                String prevType = mCursor.moveToPosition(offsetPosition - 1) ?
                        mCursor.getString(mCursor.getColumnIndexOrThrow(DBAdapter.KEY_TYPE)) :
                        null;
                String currType = mCursor.moveToPosition(offsetPosition) ?
                        mCursor.getString(mCursor.getColumnIndexOrThrow(DBAdapter.KEY_TYPE)) :
                        null;

                // If both types match, then it is an item.
                if (prevType != null && currType != null && prevType.equalsIgnoreCase(currType)) {
                    // Build item.
                    buildItem(holder, offsetPosition);
                }
                // Otherwise, it's a header.
                else {
                    // Build header.
                    buildHeader(holder, position, offsetPosition);
                }
            }
        }

        // Passing the binding operation to cursor loader
//        mCursorAdapter.getCursor().moveToPosition(position);
//        mCursorAdapter.bindView(holder.itemView, mContext, mCursorAdapter.getCursor());
    }

    private void buildHeader(final ViewHolder holder, int position, int offsetPosition) {
        if (mCursor.moveToPosition(offsetPosition)) {
            final Category category = new Category(mCursor);

            holder.filterActiveCheckbox.setVisibility(View.GONE);

            holder.filterNameTextView.setGravity(Gravity.START | Gravity.CENTER_VERTICAL);
            holder.filterNameTextView.setTypeface(null, Typeface.BOLD);
            holder.filterNameTextView.setText(category.getType());

            holder.cellRoot.setBackgroundColor(Color.LTGRAY);

            Map.Entry<Integer, Integer> prevNumHeaders = mOffsets.lowerEntry(position);
            mOffsets.put(position, (prevNumHeaders == null ? 0 : prevNumHeaders.getValue()) + 1);
        } else {
            Log.d(RHCareerFairLayout.RH_CFL, "Invalid cursor position detected while creating header cell: " + offsetPosition);
            return;
        }
    }

    private void buildItem(final ViewHolder holder, int offsetPosition) {
        if (mCursor.moveToPosition(offsetPosition)) {
            final Category category = new Category(mCursor);
            final Boolean selected = mCursor.getInt(mCursor.getColumnIndexOrThrow(DBAdapter.KEY_SELECTED)) > 0;

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
                    DBAdapter.setCategorySelected(category.getId(), !current);
                    synchronized (RHCareerFairLayout.categorySelectionChanged) {
                        RHCareerFairLayout.categorySelectionChanged.notifyChanged();
                    }
                    holder.filterActiveCheckbox.setChecked(!current);
                    changeCursor(DBAdapter.getCategoriesCursor());
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
            return;
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // Passing the inflater job to the cursor-adapter

        View v = mInflater.inflate(R.layout.cell_filter, parent, false);
//        View v = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
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