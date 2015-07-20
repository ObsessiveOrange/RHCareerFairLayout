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

package cf.obsessiveorange.rhcareerfairlayout.ui.activity;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cf.obsessiveorange.rhcareerfairlayout.R;

public class FiltersCellAdapter extends RecyclerView.Adapter<FiltersCellAdapter.ViewHolder> {
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<String> mItems;

    public FiltersCellAdapter(Context context, ArrayList<String> items) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        mItems = items;
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    @Override
    public int getItemViewType(int position) {
        return super.getItemViewType(position);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                break;
            case 1:
                break;
            default:
                break;
        }
        return new ViewHolder(mInflater.inflate(R.layout.cell_filter, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder viewHolder, int position) {
        viewHolder.cellRoot.setBackgroundColor(Color.WHITE);
        viewHolder.filterActiveCheckbox.setChecked(false);
        viewHolder.filterNameTextView.setText(mItems.get(position));
        viewHolder.cellRoot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                viewHolder.filterActiveCheckbox.setChecked(!viewHolder.filterActiveCheckbox.isChecked());
                v.setBackgroundColor(Color.GREEN);

            }
        });
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
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
