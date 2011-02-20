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
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

// REF#0014

public class Shortcuts extends ListActivity {
    private static String LOG_NAME = WakeMeAt.LOG_NAME;
    public static String BROADCAST_UPDATE;

    private static final String ROWID_KEY = "uk.co.spookypeanut.wake_me_at.Shortcuts";
    private LayoutInflater mInflater;
    private LocListAdapter mLocListAdapter;
    private DatabaseManager db;

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        Log.d(LOG_NAME, "onListItemClick(" + l + ", " + v + ", " + position
                + ", " + id + ")");
        createShortcut(mLocListAdapter.getItemId(position));
    }

    public void shortcutList() {
        db = new DatabaseManager(this);
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);


        Log.d(LOG_NAME, "setListAdaptor");
        setListAdapter(new LocListAdapter(this));

        Log.d(LOG_NAME, "setContentView");
        setContentView(R.layout.shortcuts);

        ListView lv = getListView(); 
        registerForContextMenu(lv);
        
        mLocListAdapter = (LocListAdapter) getListAdapter();
        Log.d(LOG_NAME, "End shortcutList()");
    }
    
    
    public void createShortcut(long rowId) {
        db.logOutArray();
        Log.d(LOG_NAME, "Shortcuts.createShortcut");
        
        Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
        shortcutIntent.setClassName(this, this.getClass().getName());
        shortcutIntent.putExtra(ROWID_KEY, rowId);
        
        Intent intent = new Intent();
        intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
        intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.shortcut_name));
        Parcelable iconResource = Intent.ShortcutIconResource.fromContext(
                this,  R.drawable.icon);
        intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, iconResource);

        setResult(RESULT_OK, intent);
        finish();
    }

    public void startService(long rowId) {
        Intent intent = new Intent(WakeMeAtService.ACTION_FOREGROUND);
        intent.setClass(Shortcuts.this, WakeMeAtService.class);
        intent.putExtra("rowId", rowId);
        this.startService(intent);
    }
    
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // Resolve the intent

        final Intent intent = getIntent();
        final String action = intent.getAction();

        // If the intent is a request to create a shortcut, we'll do that and
        // exit

        if (Intent.ACTION_CREATE_SHORTCUT.equals(action)) {
            shortcutList();
            return;
        }

        // If we weren't launched with a CREATE_SHORTCUT intent, simply put up
        // an informative
        // display.
        Log.d(LOG_NAME, "Shortcuts.onCreate");
        Toast myToast = Toast.makeText(this, "Not creating", Toast.LENGTH_SHORT);
        myToast.show();
        Bundle extras = intent.getExtras();
        long rowId = extras.getLong(ROWID_KEY);
        
        startService(rowId);
        finish();
    }

    private class LocListAdapter extends BaseAdapter {
        public LocListAdapter(Context context) {
            Log.d(LOG_NAME, "LocListAdapter constructor");
        }


        public int getCount() {
            return db.getRowCount();

        }

        public Object getItem(int position) {
            return getItemId(position);
        }

        public long getItemId(int position) {
            return db.getIdsAsList().get(position);
        }

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
