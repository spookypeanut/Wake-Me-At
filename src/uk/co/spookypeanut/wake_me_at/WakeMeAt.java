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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class WakeMeAt extends ListActivity {
    public static final String PREFS_NAME = "WakeMeAtPrefs";
    public static final String LOG_NAME = "WakeMeAt";
    private DatabaseManager db;
    
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
        i.putExtra("rowid", -1);
        //Log.d(LOG_NAME, "About to start activity");
        startActivity(i);
    }
    
    protected void onListItemClick (ListView l, View v, int position, long id) {
        //Log.d(LOG_NAME, "onListItemClick(" + l + ", " + v + ", " + position + ", " + id + ")");
        Intent i = new Intent(WakeMeAt.this.getApplication(), EditLocation.class);
        i.putExtra("rowid", position + 1);
        //Log.d(LOG_NAME, "About to start activity");
        startActivity(i);
    }
    
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_NAME, "Start onCreate()");
        super.onCreate(savedInstanceState);

        //Log.d(LOG_NAME, "DatabaseManager");
        db = new DatabaseManager(this);
        
        //Log.d(LOG_NAME, "setListAdaptor");
        setListAdapter(new LocListAdapter(this));

        //Log.d(LOG_NAME, "setContentView");
        setContentView(R.layout.wake_me_at);
    }
    private class LocListAdapter extends BaseAdapter {
        public LocListAdapter(Context context) {
            mContext = context;
        }

        public int getCount() {
            return db.getRowCount();
            
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            LocEntry sv;
            if (convertView == null) {
                sv = new LocEntry(mContext, db.getNick(position + 1),
                        db.getProvider(position + 1));
            } else {
                sv = (LocEntry) convertView;
                sv.setTitle(db.getNick(position + 1));
                sv.setDialogue(db.getProvider(position + 1));
            }

            return sv;
        }

        private Context mContext;

    }
    private class LocEntry extends LinearLayout {
        public LocEntry(Context context, String title, String words) {
            super(context);

            this.setOrientation(VERTICAL);

            // Here we build the child views in code. They could also have
            // been specified in an XML file.

            mTitle = new TextView(context);
            mTitle.setText(title);
            addView(mTitle, new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            mDialogue = new TextView(context);
            mDialogue.setText(words);
            addView(mDialogue, new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }

        public void setTitle(String title) {
            mTitle.setText(title);
        }

        public void setDialogue(String words) {
            mDialogue.setText(words);
        }

        private TextView mTitle;
        private TextView mDialogue;
    }
}