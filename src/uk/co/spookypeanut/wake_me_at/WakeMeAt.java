package uk.co.spookypeanut.wake_me_at;

import java.util.ArrayList;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

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


/**
 * The main activity, which has a list of all the locations
 * @author spookypeanut
 */
public class WakeMeAt extends ListActivity {
    public static final String PREFS_NAME = "WakeMeAtPrefs";
    public static String LOG_NAME;
    public static String BROADCAST_UPDATE;
    
    public static final double INVALIDLATLONG = 1000.0;
    
    private DatabaseManager db;
    private LayoutInflater mInflater;
    private LocListAdapter mLocListAdapter;
    private Context mContext;
    
    private long mRowId;
    
    /* (non-Javadoc)
     * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mn_wake_me_at, menu);
        return true;
    }
    
    /**
     * The items on the menu 
     * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.mn_new_loc:
            mLocListAdapter.addItem();
            return true;
        case R.id.mn_stop_all:
            stopService(new Intent(WakeMeAt.this, WakeMeAtService.class));
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    @Override
    protected void onListItemClick (ListView l, View v, int position, long id) {
        mLocListAdapter.editLocation(position);
    }
    
    /**
     * Create the context menu for the list items
     * @see android.app.Activity#onCreateContextMenu(android.view.ContextMenu, android.view.View, android.view.ContextMenu.ContextMenuInfo)
     */
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);
      Log.d(LOG_NAME, "onCreateContextMenu");
      AdapterContextMenuInfo myInfo = (AdapterContextMenuInfo) menuInfo;
      if (mLocListAdapter.isNewLocItem(myInfo.position)) {
          Log.d(LOG_NAME, "This is the new location item");
          return;
      }
      long id = myInfo.id;
      menu.setHeaderTitle(db.getNick(id));
      menu.setHeaderIcon(R.drawable.icon);

      MenuInflater inflater = getMenuInflater();
      if (WakeMeAtService.serviceRunning && (id == mRowId)) {
          inflater.inflate(R.menu.mn_context_running_wake_me_at, menu);
      } else {
          inflater.inflate(R.menu.mn_context_wake_me_at, menu);
      }
    }
    
    /**
     * Code for the items on the list item context menu
     * @see android.app.Activity#onContextItemSelected(android.view.MenuItem)
     */
    @Override
    public boolean onContextItemSelected(MenuItem item) {
      AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
      switch (item.getItemId()) {
      case R.id.mn_delete_loc:
          mLocListAdapter.deleteItem(info.position);
        return true;
      case R.id.mn_start:
          mLocListAdapter.startService(info.position);
        return true;
      case R.id.mn_stop:
          stopService(new Intent(WakeMeAt.this, WakeMeAtService.class));
        return true;
      case R.id.mn_edit_loc:
          mLocListAdapter.editLocation(info.position);
        return true;
      default:
        return super.onContextItemSelected(item);
      }
    }
    
    @Override
    protected void onPause() {
        this.unregisterReceiver(this.mReceiver);
        super.onPause();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        deleteEmpty();
        mLocListAdapter.notifyDataSetChanged();
        IntentFilter filter = new IntentFilter(BROADCAST_UPDATE);
        this.registerReceiver(this.mReceiver, filter);
    }
    
    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
    
    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        
        LOG_NAME = (String) getText(R.string.app_name_nospaces);
        BROADCAST_UPDATE = (String) getText(R.string.serviceBroadcastName);

        setVolumeControlStream(AudioManager.STREAM_ALARM);

        mContext = this;
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        db = new DatabaseManager(this);
        
        setContentView(R.layout.wake_me_at);
        setListAdapter(new LocListAdapter(this));

        ListView lv = getListView();
        registerForContextMenu(lv);
        
        mLocListAdapter = (LocListAdapter) getListAdapter();
    }
    
    public void deleteEmpty() {
        int i;
        ArrayList <Long> idList = db.getIdsAsList();
        for (i = 0; i < idList.size(); i++) {
            long id = idList.get(i);
            if ((db.getLatitude(id) == INVALIDLATLONG) &&
                (db.getLongitude(id) == INVALIDLATLONG)) {
                db.deleteRow(id);
            }
        }
    }
    
    /**
     * Method called when the rowId of the service has changed
     * @param newRowId
     */
    private void rowChanged(long newRowId) {
        mRowId = newRowId;
        mLocListAdapter.notifyDataSetChanged();
    }
    
    /**
     * Create a new row in the database with default values
     * @return The id of the new row
     */
    private long createDefaultRow() {
        // TODO: move all strings / constants out to R
        Uri temp = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM); 
        return db.addRow (
            "",              // Nickname
            INVALIDLATLONG,  // Lat
            INVALIDLATLONG,  // Long
            1,               // Preset
            1,               // Location provider
            (float) 1.80,    // Radius
            "km",            // Unit
            true,            // Sound
            temp.toString(), // Ringtone
            false,           // Crescendo 
            true,            // Vibration
            true,            // Speech
            false,           // Toast
            true,            // Warning
            true,            // Warn sound
            true,            // Warn vibrate
            true             // Warn toast
        );
    }
    
    /**
     * Class for the location list on the main activity
     * @author spookypeanut
     */
    private class LocListAdapter extends BaseAdapter {
        // REF#0007
        public LocListAdapter(Context context) {
            Log.d(LOG_NAME, "LocListAdapter constructor");
        }
        
        /**
         * Start the service for this list entry
         * @param position
         */
        public void startService(int position) {
            Intent intent = new Intent(WakeMeAtService.ACTION_FOREGROUND);
            intent.setClass(WakeMeAt.this, WakeMeAtService.class);
            intent.putExtra("rowId", getListAdapter().getItemId(position));
            mContext.startService(intent);
        }
        
        /**
         * Delete this list entry
         * @param position
         */
        public void deleteItem(int position) {
            db.deleteRow(getListAdapter().getItemId(position));
            this.notifyDataSetChanged();
        }
        
        /**
         * Create a new location in the database, and edit it
         */
        public void addItem() {
            long rowId = createDefaultRow();
            Intent i = new Intent(WakeMeAt.this.getApplication(), EditLocation.class);
            i.putExtra("rowId", rowId);
            startActivity(i);
        }
        
        @Override
        public int getCount() {
            // Add one for the "Add new location..." row
            return db.getRowCount() + 1;
            
        }

        @Override
        public Object getItem(int position) {
            return getItemId(position);
        }

        private boolean isNewLocItem(int position) {
            return (position == (getCount() - 1 ));
        }
        
        @Override
        public long getItemId(int position) {
            Log.d(LOG_NAME, "getItemId(" + position + ")");
            if (isNewLocItem(position)) {
                Log.d(LOG_NAME, "Returning 0");
                return 0;
            } else {
                return db.getIdsAsList().get(position);
            }
        }

        /**
         * Open the edit location activity for this list entry
         * @param position
         */
        public void editLocation(int position) {
            if (isNewLocItem(position)) {
                addItem();
                return;
            }
            Intent i = new Intent(WakeMeAt.this.getApplication(), EditLocation.class);
            i.putExtra("rowId", getItemId(position));
            Log.d(LOG_NAME, "About to start activity");
            startActivity(i);
        }
        
        /**
         * Create the view for a single line of the listview
         * @see android.widget.Adapter#getView(int, android.view.View, android.view.ViewGroup)
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Log.d(LOG_NAME, "getView(" + position + ")");
            boolean newLocItem;
            long id;
            int layout;
            if (isNewLocItem(position)) {
                layout = R.layout.wma_newloc_list_entry;
                newLocItem = true;
                id = -1;
            } else {
                layout = R.layout.wma_list_entry;
                newLocItem = false;
                id = db.getIdsAsList().get(position);
            }
            
            
            // We used to check if we already had a row, and neatly re-use it if we did.
            // Unfortunately, the "add new location" row uses a different layout, so we can't
            View row = mInflater.inflate(layout, null);

            // If this is the currently running location, highlight it
            if (WakeMeAtService.serviceRunning && (id == mRowId)) {
                row.setBackgroundDrawable(getResources().getDrawable(R.drawable.listitembg_hl));
            } else {
                row.setBackgroundDrawable(getResources().getDrawable(R.drawable.listitembg));
            }
            
            // Set the name of the location from the database
            TextView tv = (TextView) row.findViewById(R.id.locListName);
            String nick;
            if (newLocItem) {
                nick = getResources().getString(R.string.new_location_item);
            } else {
                nick = db.getNick(id);
            }
            if ("".equals(nick)) {
                tv.setText(R.string.mn_unnamed);
            } else {
                tv.setText(nick);
            }
            
            if (newLocItem) {
                return row;
            }
            
            // Set the description, which is the preset in the database
            String subtitle = new Presets(mContext, db.getPreset(id)).getName();
            tv = (TextView) row.findViewById(R.id.locListDesc);
            tv.setText(subtitle);
            return row;
        }
    }
    
    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_NAME, "WakeMeAt.onReceive");
            Bundle extras = intent.getExtras();
            long rowId = extras.getLong("rowId");
            // If the service is running, or the destination is removed
            if (WakeMeAtService.serviceRunning || rowId == -1) {
                if (mRowId != rowId) {
                    rowChanged(rowId);
                }
            }
        }
   };
}
