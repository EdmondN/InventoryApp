/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ItemContract.ItemEntry;

/**
 * {@link ItemCursorAdapter} is an adapter for a list or grid view
 * that uses a {@link Cursor} of item data as its data source. This adapter knows
 * how to create list items for each row of item data in the {@link Cursor}.
 */
class ItemCursorAdapter extends CursorAdapter {

    /**
     * Constructs a new {@link ItemCursorAdapter}.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     */
    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    /**
     * Makes a new blank list item view. No data is set (or bound) to the views yet.
     *
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // Inflate a list item view using the layout specified in list_item.xml
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    /**
     * This method binds the item data (in the current row pointed to by cursor) to the given
     * list item layout. For example, the name for the current item can be set on the name TextView
     * in the list item layout.
     *
     * @param view    Existing view, returned earlier by newView() method
     * @param context app context
     * @param cursor  The cursor from which to get the data. The cursor is already moved to the
     *                correct row.
     */
    @Override
    public void bindView(View view, final Context context, final Cursor cursor) {
        // Find individual views that we want to modify in the list item layout
        TextView nameTextView = view.findViewById(R.id.name);
        TextView summaryTextView = view.findViewById(R.id.summary);
        TextView priceTextView = view.findViewById(R.id.product_price);
        TextView quantityTextView = view.findViewById(R.id.quantity);

        // Find the columns of ITEM attributes that we're interested in
        int nameColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_NAME);
        int descriptionColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_DESCRIPTION);
        int priceColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);

        // Read the ITEM attributes from the Cursor for the current item
        String itemName = cursor.getString(nameColumnIndex);
        String itemDescription = cursor.getString(descriptionColumnIndex);
        int itemPrice = cursor.getInt(priceColumnIndex);
        int itemQuantity = cursor.getInt(quantityColumnIndex);

        // If the item description is empty string or null, then use some default text
        // that says "Unknown description", so the TextView isn't blank.
        if (TextUtils.isEmpty(itemDescription)) {
            itemDescription = context.getString(R.string.unknown_description);
        }

        // Update the TextViews with the attributes for the current item
        nameTextView.setText(itemName);
        summaryTextView.setText(itemDescription);
        String euro = context.getString(R.string.unit_item_price);
        priceTextView.setText(Integer.toString(itemPrice)+euro);
        quantityTextView.setText(Integer.toString(itemQuantity));

        // Find the sale button
        Button saleButton = view.findViewById(R.id.list_item_sale);

        // Get the position before the button is clicked
        final int position = cursor.getPosition();

        saleButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                // Move the cursor to the correct position
                cursor.moveToPosition(position);

                // Get the Uri for the current drug
                int IdColumnIndex = cursor.getColumnIndex(ItemEntry._ID);
                final long itemId = cursor.getLong(IdColumnIndex);
                Uri mCurrentItemUri = ContentUris.withAppendedId(ItemEntry.CONTENT_URI, itemId);

                // Find the columns of drug attributes that we're interested in
                int quantityColumnIndex = cursor.getColumnIndex(ItemEntry.COLUMN_ITEM_QUANTITY);

                // Read the drug attributes from the Cursor for the current drug
                String itemQuantity = cursor.getString(quantityColumnIndex);

                // Convert the string to an integer
                int updateQuantity = Integer.parseInt(itemQuantity);

                if (updateQuantity > 0) {
                    // Decrease the quantity by 1
                    updateQuantity--;

                    // Defines an object to contain the updated values
                    ContentValues updateValues = new ContentValues();
                    updateValues.put(ItemEntry.COLUMN_ITEM_QUANTITY, updateQuantity);

                    //update the phone with the content URI mCurrentPhoneUri and pass in the new
                    //content values. Pass in null for the selection and selection args
                    //because mCurrentPhoneUri will already identify the correct row in the database that
                    // we want to modify.
                    context.getContentResolver().update(mCurrentItemUri, updateValues, null, null);
                } else {
                    Toast.makeText(context, "Item out of stock", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}