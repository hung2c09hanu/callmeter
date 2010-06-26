/*
 * Copyright (C) 2009-2010 Felix Bechstein, The Android Open Source Project
 * 
 * This file is part of Call Meter 3G.
 * 
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program; If not, see <http://www.gnu.org/licenses/>.
 */
package de.ub0r.android.callmeter.ui.prefs;

import android.app.ListActivity;
import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import de.ub0r.android.callmeter.R;
import de.ub0r.android.callmeter.data.DataProvider;

/**
 * {@link ListActivity} for setting rules.
 * 
 * @author flx
 */
public class Rules extends ListActivity implements OnClickListener,
		OnItemClickListener {
	/** Tag for output. */
	public static final String TAG = "prefs.rules";

	/** Plans. */
	private RuleAdapter adapter = null;

	/** Item menu: edit. */
	private static final int WHICH_EDIT = 0;
	/** Item menu: up. */
	private static final int WHICH_UP = 1;
	/** Item menu: down. */
	private static final int WHICH_DOWN = 2;
	/** Item menu: delete. */
	private static final int WHICH_DELETE = 3;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setTitle(this.getString(R.string.settings) + " > "
				+ this.getString(R.string.rules));
		this.setContentView(R.layout.prefs_rules);
		this.adapter = new RuleAdapter(this);
		this.setListAdapter(this.adapter);
		this.getListView().setOnItemClickListener(this);
		this.findViewById(R.id.ok).setOnClickListener(this);
		this.findViewById(R.id.add).setOnClickListener(this);
	}

	/**
	 * Swap two rules.
	 * 
	 * @param position
	 *            Position of selected plan
	 * @param direction
	 *            Direction to swap the rules. up: -1, down: +1
	 */
	private void swap(final int position, final int direction) {
		final ContentResolver cr = this.getContentResolver();
		Cursor cursor = null;
		// get rules
		final String idCurrent = String.valueOf(this.adapter
				.getItemId(position));
		final String idOther = String.valueOf(this.adapter.getItemId(position
				+ direction));
		cursor = cr.query(Uri.withAppendedPath(DataProvider.Rules.CONTENT_URI,
				idCurrent), DataProvider.Rules.PROJECTION, null, null, null);
		if (cursor == null || !cursor.moveToFirst()) {
			return;
		}
		final int orderCurrent = cursor.getInt(DataProvider.Rules.INDEX_ORDER);
		cursor.close();
		cursor = cr.query(Uri.withAppendedPath(DataProvider.Rules.CONTENT_URI,
				idOther), DataProvider.Rules.PROJECTION, null, null, null);
		if (cursor == null || !cursor.moveToFirst()) {
			return;
		}
		final int orderOther = cursor.getInt(DataProvider.Rules.INDEX_ORDER);
		cursor.close();

		// set new order
		final ContentValues cvCurrent = new ContentValues();
		ContentValues cvOther = null;
		if (orderCurrent == orderOther) {
			cvCurrent.put(DataProvider.Rules.ORDER, orderCurrent + direction);
		} else {
			cvOther = new ContentValues();
			cvCurrent.put(DataProvider.Rules.ORDER, orderOther);
			cvOther.put(DataProvider.Rules.ORDER, orderCurrent);
		}

		// push changes
		cr.update(Uri.withAppendedPath(DataProvider.Rules.CONTENT_URI,
				idCurrent), cvCurrent, null, null);
		if (cvOther != null) {
			cr.update(Uri.withAppendedPath(DataProvider.Rules.CONTENT_URI,
					idOther), cvOther, null, null);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onItemClick(final AdapterView<?> parent, final View view,
			final int position, final long id) {
		final Builder builder = new Builder(this);
		builder.setItems(R.array.prefs_rules_dialog,
				new android.content.DialogInterface.OnClickListener() {
					@Override
					public void onClick(final DialogInterface dialog,
							final int which) {
						switch (which) {
						case WHICH_EDIT:
							final Intent intent = new Intent(// .
									Rules.this, RuleEdit.class);
							intent.setData(Uri.withAppendedPath(
									DataProvider.Rules.CONTENT_URI, String
											.valueOf(id)));
							Rules.this.startActivity(intent);
							break;
						case WHICH_UP:
							Rules.this.swap(position, -1);
							break;
						case WHICH_DOWN:
							Rules.this.swap(position, 1);
							break;
						case WHICH_DELETE:
							Rules.this.getContentResolver().delete(
									Uri.withAppendedPath(
											DataProvider.Rules.CONTENT_URI,
											String.valueOf(id)), null, null);
							break;
						default:
							break;
						}
					}
				});
		builder.show();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public final void onClick(final View v) {
		switch (v.getId()) {
		case R.id.add:
			final Intent intent = new Intent(this, RuleEdit.class);
			this.startActivity(intent);
			break;
		case R.id.ok:
			this.finish();
			break;
		default:
			break;
		}
	}
}
