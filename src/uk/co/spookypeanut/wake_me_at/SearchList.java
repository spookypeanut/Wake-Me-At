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

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.ListActivity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;

public class SearchList extends ListActivity {
    public static final String PREFS_NAME = "WakeMeAtPrefs";
    public static final String LOG_NAME = "WakeMeAt";
    private LayoutInflater mInflater;
    private List<Address> mResults;
    
    @Override
    public void onNewIntent(final Intent newIntent) {
        Log.d(LOG_NAME, "onNewIntent");
        super.onNewIntent(newIntent);
        
        // get and process search query here
        final Intent queryIntent = getIntent();
        final String queryAction = queryIntent.getAction();
        if (Intent.ACTION_SEARCH.equals(queryAction)) {
            Log.d(LOG_NAME, "query");
        }
        else {
            Log.d(LOG_NAME, "no query");
        }
    }
    protected void onListItemClick (ListView l, View v, int position, long id) {
        Log.d(LOG_NAME, "onListItemClick(" + l + ", " + v + ", " + position + ", " + id + ")");
        Intent i = new Intent(SearchList.this.getApplication(), EditLocation.class);
        i.putExtra("rowid", position + 1);
        Log.d(LOG_NAME, "About to start activity");
        //startActivity(i);
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_NAME, "Start onCreate()");
        super.onCreate(savedInstanceState);
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Log.d(LOG_NAME, "inflater done");

        // get and process search query here
        final Intent queryIntent = getIntent();
        final String queryAction = queryIntent.getAction();
        Log.d(LOG_NAME, "queryIntent: " + queryIntent.toString() + ", queryAction: " + queryAction);
        mResults = getSearchLocations(queryIntent.getStringExtra(SearchManager.QUERY));

        Log.d(LOG_NAME, "setListAdaptor");
        setListAdapter(new SearchListAdapter(this));
        
        Log.d(LOG_NAME, "setContentView");
        setContentView(R.layout.search_list);

        Log.d(LOG_NAME, "End onCreate()");
    }
    
    private List<Address> getSearchLocations(String address) {
        Log.d(LOG_NAME, "getSearchLocations");
        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        List<Address> locations = null;
        try {
            locations = geoCoder.getFromLocationName(address, 5);
        } catch (IOException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < locations.size(); i++) {
            Log.d(LOG_NAME, "Location " + i + ": " + locations.get(i).toString());
        }
        return locations;

    }
    private class SearchListAdapter extends BaseAdapter {
        public SearchListAdapter(Context context) {
            Log.d(LOG_NAME, "LocListAdapter constructor");
        }

        public int getCount() {
            Log.d(LOG_NAME, "getCount()");
            return mResults.size();
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
                row = mInflater.inflate(R.layout.search_list_entry, null);
            } else {
                Log.d(LOG_NAME, "not null");
                row = convertView;
            }
            Log.d(LOG_NAME, "row = " + row.toString());
            
            Log.d(LOG_NAME, "end getView(" + position + ")");
            return row;
        }
    }
}