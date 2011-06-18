package uk.co.spookypeanut.wake_me_at;

/*
 This file is part of Wake Me At. Wake Me At is the legal property
 of its developer, Henry Bush (spookypeanut).

 Wake Me At is free software: you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation, either version 3 of the License, or
 (at your option) any later version.

 Wake Me At is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with Wake Me At, in the file "COPYING".  If not, see 
 <http://www.gnu.org/licenses/>.
 */

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

// REF#0014

/**
 * Activity for creating home screen shortcuts (and running them)
 * @author spookypeanut
 */
public class Shortcuts extends ListActivity {
    private static String LOG_NAME;
    public static String BROADCAST_UPDATE;

    private static final String ROWID_KEY = "uk.co.spookypeanut.wake_me_at.Shortcuts";
    private LayoutInflater mInflater;
    private LocListAdapter mLocListAdapter;
    private DatabaseManager db = null;
    
    public WakeMeAt mBlah = new WakeMeAt();

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(LOG_NAME, "onListItemClick(" + l + ", " + v + ", " + position
                + ", " + id + ")");
        createShortcut(mLocListAdapter.getItemId(position));
    }

    /**
     * Inflate the list of possible shortcuts
     */
    public void shortcutList() {
        db = new DatabaseManager(this);
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        setListAdapter(new LocListAdapter(this));
        setContentView(R.layout.shortcuts);

        ListView lv = getListView(); 
        registerForContextMenu(lv);
        
        mLocListAdapter = (LocListAdapter) getListAdapter();
    }
    
    /**
     * Create a home screen shortcut of the given database row
     * @param rowId
     */
    public void createShortcut(long rowId) {
        db.logOutArray();
        Log.d(LOG_NAME, "Shortcuts.createShortcut");
        
        Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
        shortcutIntent.setClassName(this, this.getClass().getName());
        shortcutIntent.putExtra(ROWID_KEY, rowId);
        
        String nick = db.getNick(rowId);
        
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME,
                        String.format(getString(R.string.shortcut_name), nick));
        Parcelable iconResource = Intent.ShortcutIconResource.fromContext(
                this, R.drawable.iconstar);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);

        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        if (db != null) {
            db.close();
        }
        super.onDestroy();
    }
    
    @Override
    public void onCreate(Bundle icicle) {
        LOG_NAME = (String) getText(R.string.app_name_nospaces);
        BROADCAST_UPDATE = (String) getText(R.string.serviceBroadcastName);
        Log.d(LOG_NAME, "Shortcuts.onCreate");
        super.onCreate(icicle);

        final Intent intent = getIntent();
        final String action = intent.getAction();

        // If the intent is a request to create a shortcut
        // we do that and exit
        if (Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
            shortcutList();
            return;
        }

        // If not, we're running from a shortcut. Get the rowId
        // from the intent, and start the service
        Bundle extras = intent.getExtras();
        long rowId = extras.getLong(ROWID_KEY);
        
        Intent serviceIntent = new Intent(WakeMeAtService.ACTION_FOREGROUND);
        serviceIntent.setClass(Shortcuts.this, WakeMeAtService.class);
        serviceIntent.putExtra("rowId", rowId);
        startService(serviceIntent);
        finish();
    }

    /**
     * The list adaptor for the possible shortcut locations
     * @author spookypeanut
     */
    private class LocListAdapter extends BaseAdapter {
        public LocListAdapter(Context context) {
            Log.d(LOG_NAME, "LocListAdapter constructor");
        }

        @Override
        public int getCount() {
            return db.getRowCount();
        }

        @Override
        public Object getItem(int position) {
            return getItemId(position);
        }

        @Override
        public long getItemId(int position) {
            return db.getIdsAsList().get(position);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            long id = db.getIdsAsList().get(position);
            // Log.d(LOG_NAME, "getView(" + id + "), mRowId: " + mRowId);
            View row;

            if (null == convertView) {
                row = mInflater.inflate(R.layout.shortcuts_list_entry, null);
            } else {
                row = convertView;
            }

            TextView tv = (TextView) row.findViewById(R.id.locListName);
            tv.setText(db.getNick(id));

            return row;
        }
    }
}
