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
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import cf.obsessiveorange.rhcareerfairlayout.R;

public class CompaniesCellAdapter extends RecyclerView.Adapter<CompaniesCellAdapter.ViewHolder> {
    private LayoutInflater mInflater;
    private ArrayList<String> mItems;

    public CompaniesCellAdapter(Context context, ArrayList<String> items) {
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
        return new ViewHolder(mInflater.inflate(R.layout.cell_company, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        viewHolder.showOnMapCheckBox.setChecked(false);
        viewHolder.companyNameTextView.setText(mItems.get(position));
        viewHolder.tableNumberTextView.setText("#");
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        CheckBox showOnMapCheckBox;
        TextView companyNameTextView;
        TextView tableNumberTextView;

        public ViewHolder(View view) {
            super(view);
            showOnMapCheckBox = (CheckBox) view.findViewById(R.id.show_on_map);
            companyNameTextView = (TextView) view.findViewById(R.id.company_name);
            tableNumberTextView = (TextView) view.findViewById(R.id.table_number);
        }
    }
}
//public class MyRecyclerAdapter extends Adapter<MyRecyclerAdapter.ViewHolder {
//
//    // PATCH: Because RecyclerView.Adapter in its current form doesn't natively support
//    // cursors, we "wrap" a CursorAdapter that will do all teh job
//    // for us
//    CursorAdapter mCursorAdapter;
//
//    Context mContext;
//
//    public MyRecyclerAdapter(Context context, Cursor c) {
//
//        mContext = context;
//
//        mCursorAdapter = new CursorAdapter(mContext, c, 0) {
//
//            @Override
//            public View newView(Context context, Cursor cursor, ViewGroup parent) {
//                // Inflate the view here
//            }
//
//            @Override
//            public void bindView(View view, Context context, Cursor cursor) {
//                // Binding operations
//
//            }
//        };
//    }
//
//    public static class ViewHolder extends RecyclerView.ViewHolder{
//
//        View v1;
//
//        public ViewHolder(View itemView) {
//            super(itemView);
//            v1 = itemView.findViewById(R.id.v1);
//        }
//    }
//
//    @Override
//    public int getItemCount() {
//        return mCursorAdapter.getCount();
//    }
//
//    @Override
//    public void onBindViewHolder(ViewHolder holder, int position) {
//        // Passing the binding operation to cursor loader
//        mCursorAdapter.getCursor().moveToPosition(position);
//        mCursorAdapter.bindView(holder.itemView, mContext, mCursorAdapter.getCursor());
//
//    }
//
//    @Override
//    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//        // Passing the inflater job to the cursor-adapter
//        View v = mCursorAdapter.newView(mContext, mCursorAdapter.getCursor(), parent);
//        return new ViewHolder(v);
//    }
//
//}
