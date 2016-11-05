/*
 * This source is part of the
 *      _____  ___   ____
 *  __ / / _ \/ _ | / __/___  _______ _
 * / // / , _/ __ |/ _/_/ _ \/ __/ _ `/
 * \___/_/|_/_/ |_/_/ (_)___/_/  \_, /
 *                              /___/
 * repository.
 *
 * Copyright (C) 2016 Carmen Alvarez (c@rmen.ca)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package ca.rmen.android.networkmonitor.app.prefs;

import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ca.rmen.android.networkmonitor.R;
import ca.rmen.android.networkmonitor.app.dialog.DialogFragmentFactory;
import ca.rmen.android.networkmonitor.provider.NetMonColumns;

class SelectedFieldsAdapter extends RecyclerView.Adapter<SelectedFieldsAdapter.SelectedFieldHolder> {

    private static class SelectedField {
        final String dbName;
        final String label;
        final String tip;

        // Build the list of choices for the user.  Look up the friendly label of each column name, and pre-select the one the user chose last time.
        SelectedField(String dbName, String label, String tip) {
            this.dbName = dbName;
            this.label = label;
            this.tip = tip;
        }

        @Override
        public String toString() {
            return label;
        }
    }


    private final FragmentActivity mActivity;
    private final SelectedField[] mSelectedFields;
    private final Set<String> mCheckedItems = new HashSet<>();

    SelectedFieldsAdapter(FragmentActivity activity) {
        mActivity = activity;
        String[] dbColumns = NetMonColumns.getColumnNames(activity);
        String[] columnLabels = NetMonColumns.getColumnLabels(activity);
        mSelectedFields = new SelectedField[dbColumns.length];
        for (int i = 0; i < dbColumns.length; i++) {
            int tipId = activity.getResources().getIdentifier(dbColumns[i] + "_help", "string", activity.getPackageName());
            String tip = tipId > 0 ? activity.getString(tipId) : null;
            mSelectedFields[i] = new SelectedField(dbColumns[i], columnLabels[i], tip);
        }
        // Preselect the columns from the preferences
        List<String> selectedColumns = NetMonPreferences.getInstance(activity).getSelectedColumns();
        mCheckedItems.addAll(selectedColumns);
    }

    void selectAll() {
        for (SelectedField field : mSelectedFields) mCheckedItems.add(field.dbName);
        notifyDataSetChanged();
    }

    void selectNone() {
        mCheckedItems.clear();
        notifyDataSetChanged();
    }

    void selectColumns(String[] dbColumns) {
        mCheckedItems.clear();
        Collections.addAll(mCheckedItems, dbColumns);
        notifyDataSetChanged();
    }

    List<String> getSelectedColumns() {
        List<String> result = new ArrayList<>();
        for (SelectedField field : mSelectedFields) {
            if (mCheckedItems.contains(field.dbName)) {
                result.add(field.dbName);
            }
        }
        return result;
    }

    @Override
    public void onBindViewHolder(SelectedFieldHolder holder, int position) {
        final SelectedField selectedField = mSelectedFields[position];
        holder.textView.setText(selectedField.label);
        holder.imageView.setVisibility(TextUtils.isEmpty(selectedField.tip) ? View.GONE : View.VISIBLE);
        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(mCheckedItems.contains(selectedField.dbName));
        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean checked) {
                if (checked) mCheckedItems.add(selectedField.dbName);
                else mCheckedItems.remove(selectedField.dbName);
                notifyDataSetChanged();
            }
        });
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DialogFragmentFactory.showInfoDialog(mActivity, selectedField.label, selectedField.tip);
            }
        });
    }

    @Override
    public SelectedFieldHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new SelectedFieldHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.field_item, parent, false));
    }

    @Override
    public int getItemCount() {
        return mSelectedFields.length;
    }

    static class SelectedFieldHolder extends RecyclerView.ViewHolder {
        final TextView textView;
        final CheckBox checkBox;
        final ImageView imageView;

        SelectedFieldHolder(View itemView) {
            super(itemView);
            textView = (TextView) itemView.findViewById(android.R.id.text1);
            checkBox = (CheckBox) itemView.findViewById(android.R.id.checkbox);
            imageView = (ImageView) itemView.findViewById(R.id.field_help);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    checkBox.performClick();
                }
            });
        }
    }
}
