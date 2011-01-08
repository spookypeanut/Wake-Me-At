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
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class WakeMeAt extends ListActivity {
    public static final String PREFS_NAME = "WakeMeAtPrefs";
    public final String LOG_NAME = "WakeMe@";
    private DatabaseManager db;
    private LayoutInflater mInflater;

    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mn_wake_me_at, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
        case R.id.mn_new_loc:
            newLocation();
            return true;
        case R.id.mn_quit:
            Log.wtf(LOG_NAME, "Unimplemented");
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    protected void newLocation () {
        Intent i = new Intent(WakeMeAt.this.getApplication(), EditLocation.class);
        i.putExtra("rowId", -1);
        //Log.d(LOG_NAME, "About to start activity");
        startActivity(i);
    }
    
    protected void onListItemClick (ListView l, View v, int position, long id) {
        Log.d(LOG_NAME, "onListItemClick(" + l + ", " + v + ", " + position + ", " + id + ")");
        Intent i = new Intent(WakeMeAt.this.getApplication(), EditLocation.class);
        i.putExtra("rowId", (long) position + 1);
        Log.d(LOG_NAME, "About to start activity");
        startActivity(i);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);
      Log.d(LOG_NAME, "onCreateContextMenu");
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.mn_context_wake_me_at, menu);
    }
    
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_NAME, "Start onCreate()");
        super.onCreate(savedInstanceState);
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Log.d(LOG_NAME, "DatabaseManager");
        db = new DatabaseManager(this);
        
        Log.d(LOG_NAME, "setListAdaptor");
        setListAdapter(new LocListAdapter(this));

        Log.d(LOG_NAME, "setContentView");
        setContentView(R.layout.wake_me_at);

        ListView lv = getListView(); 
        registerForContextMenu(lv);

        Log.d(LOG_NAME, "End onCreate()");
    }
    private class LocListAdapter extends BaseAdapter {
        public LocListAdapter(Context context) {
            Log.d(LOG_NAME, "LocListAdapter constructor");
        }

        public int getCount() {
            Log.d(LOG_NAME, "getCount()");
            return db.getRowCount();
            
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(LOG_NAME, "getView(" + position + ")");
            View row;
            
            if (null == convertView) {
                Log.d(LOG_NAME, "is null");
                row = mInflater.inflate(R.layout.wma_list_entry, null);
            } else {
                Log.d(LOG_NAME, "not null");
                row = convertView;
            }
            Log.d(LOG_NAME, "row = " + row.toString());
            
            TextView tv = (TextView) row.findViewById(R.id.locListName);
            tv.setText(db.getNick(position + 1));
            
            tv = (TextView) row.findViewById(R.id.locListDesc);
            tv.setText(db.getProvider(position + 1));
            Log.d(LOG_NAME, "end getView(" + position + ")");
            return row;
        }
    }
}