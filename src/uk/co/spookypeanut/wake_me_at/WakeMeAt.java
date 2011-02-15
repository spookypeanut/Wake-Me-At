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
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioManager;
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
    public static String LOG_NAME;
    public static String BROADCAST_UPDATE;
    
    private DatabaseManager db;
    private LayoutInflater mInflater;
    private LocListAdapter mLocListAdapter;
    private Context mContext;
    
    private long mRowId;
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mn_wake_me_at, menu);
        return true;
    }
    
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
        Log.d(LOG_NAME, "onListItemClick(" + l + ", " + v + ", " + position + ", " + id + ")");
        mLocListAdapter.editLocation(position);
    }
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenuInfo menuInfo) {
      super.onCreateContextMenu(menu, v, menuInfo);
      Log.d(LOG_NAME, "onCreateContextMenu");
      AdapterContextMenuInfo myInfo = (AdapterContextMenuInfo) menuInfo;

      long id = myInfo.id;
      menu.setHeaderTitle(db.getNick(id));
      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.mn_context_wake_me_at, menu);
    }
    
    @Override
    public boolean onContextItemSelected(MenuItem item) {
      AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
      switch (item.getItemId()) {
      case R.id.mn_delete_loc:
          Log.d(LOG_NAME, "Delete item selected: " + info.position);
          mLocListAdapter.deleteItem(info.position);
        return true;
      case R.id.mn_start:
          mLocListAdapter.startService(info.position);
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
    protected void onCreate(Bundle savedInstanceState) {
        LOG_NAME = (String) getText(R.string.app_name_nospaces);
        BROADCAST_UPDATE = (String) getText(R.string.serviceBroadcastName);
        Log.d(LOG_NAME, "Start onCreate()");

        super.onCreate(savedInstanceState);

        setVolumeControlStream(AudioManager.STREAM_ALARM);

        mContext = this;
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        Log.d(LOG_NAME, "DatabaseManager");
        db = new DatabaseManager(this);
        
        Log.d(LOG_NAME, "setListAdaptor");
        setListAdapter(new LocListAdapter(this));

        Log.d(LOG_NAME, "setContentView");
        setContentView(R.layout.wake_me_at);

        ListView lv = getListView(); 
        registerForContextMenu(lv);
        
        mLocListAdapter = (LocListAdapter) getListAdapter();
        Log.d(LOG_NAME, "End onCreate()");
    }
    
    private void rowChanged(long newRowId) {
        Log.d(LOG_NAME, "rowChanged(" + newRowId + ")");
        mRowId = newRowId;
        mLocListAdapter.notifyDataSetChanged();
    }
    
    private class LocListAdapter extends BaseAdapter {
        // REF#0007
        public LocListAdapter(Context context) {
            Log.d(LOG_NAME, "LocListAdapter constructor");
        }
        
        public void startService(int position) {
            Intent intent = new Intent(WakeMeAtService.ACTION_FOREGROUND);
            intent.setClass(WakeMeAt.this, WakeMeAtService.class);
            intent.putExtra("rowId", getListAdapter().getItemId(position));
            mContext.startService(intent);
        }
        
        public void deleteItem(int position) {
            db.deleteRow(getListAdapter().getItemId(position));
            this.notifyDataSetChanged();
        }
        
        public void addItem() {
            Intent i = new Intent(WakeMeAt.this.getApplication(), EditLocation.class);
            i.putExtra("rowId", (long) -1);
            startActivity(i);
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

        public void editLocation(int position) {
            Intent i = new Intent(WakeMeAt.this.getApplication(), EditLocation.class);
            i.putExtra("rowId", getItemId(position));
            Log.d(LOG_NAME, "About to start activity");
            startActivity(i);
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
            long id = db.getIdsAsList().get(position);
            //Log.d(LOG_NAME, "getView(" + id + "), mRowId: " + mRowId);
            View row;
            
            if (null == convertView) {
                row = mInflater.inflate(R.layout.wma_list_entry, null);
            } else {
                row = convertView;
            }
            if (id == mRowId) {
                row.setBackgroundColor(Color.RED);
            } else {
                row.setBackgroundColor(Color.TRANSPARENT);
            }
            //Log.d(LOG_NAME, "row = " + row.toString());
            
            TextView tv = (TextView) row.findViewById(R.id.locListName);
            //Log.d(LOG_NAME, "Nick: " + db.getNick(id));
            tv.setText(db.getNick(id));
            
            tv = (TextView) row.findViewById(R.id.locListDesc);
            tv.setText(db.getProvider(id));
            return row;
        }
    }
    
    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_NAME, "Received broadcast");
            Bundle extras = intent.getExtras();
            if (mRowId != extras.getLong("rowId")) {
                rowChanged(extras.getLong("rowId"));
            }
        }
   };
}