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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.GestureDetector.OnGestureListener;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class GetLocationMap extends MapActivity
implements LocationListener {
    public static final String PREFS_NAME = "WakeMeAtPrefs";
    private String LOG_NAME;
//    private String BROADCAST_UPDATE;
    MapView mapView;
    Context mContext;
    MapOverlay mItemizedOverlay;
    GeoPoint mDest;
    String mNick;
    double mOrigLat, mOrigLong;
    LayoutInflater mInflater;
    private List<Address> mResults;
    Dialog mResultsDialog;
    boolean mSatellite = false;
    Location mCurrLoc;
    UnitConverter uc;
    String mSearchTerm;
    
    @Override
    public void onCreate(Bundle icicle) {
        LOG_NAME = (String) getText(R.string.app_name_nospaces);
//        BROADCAST_UPDATE = (String) getText(R.string.serviceBroadcastName);
        
        Log.d(LOG_NAME, "GetLocationMap.onCreate()");
        super.onCreate(icicle);

        setVolumeControlStream(AudioManager.STREAM_ALARM);

        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mContext = this;
        uc = new UnitConverter(this, "m");
        setContentView(R.layout.get_location_map);
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);
        mapView.setSatellite(mSatellite);
        Bundle extras = this.getIntent().getExtras();
        mOrigLat = extras.getDouble("latitude");
        mOrigLong = extras.getDouble("longitude");
        if (mOrigLat == 1000 && mOrigLong == 1000) {
            Location currLoc = getCurrentLocation();
            mOrigLat = currLoc.getLatitude();
            mOrigLong = currLoc.getLongitude();
        }
        mNick = extras.getString("nick");

        Drawable drawable = this.getResources().getDrawable(R.drawable.x);
        mItemizedOverlay = new MapOverlay(drawable, this);

        Toast.makeText(getApplicationContext(), R.string.open_map_toast,
                Toast.LENGTH_SHORT).show();
        
        moveDestinationTo(mOrigLat, mOrigLong);
        onSearchRequested();
    }

    @Override
    public void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(intent);
    }

    
    /**
     * When passed a new intent, run this
     * Can be either via onNewIntent or onCreate
     * @param intent
     */
    private void handleIntent(Intent intent) {
        setIntent(intent);
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
          String query = intent.getStringExtra(SearchManager.QUERY);
          resultsDialog(query);
        }
    }
    
    /**
     * Toggle the map mode between map and satellite
     */
    private void toggleMapMode() {
        // TODO: Switch the name on the menu too
        mSatellite = !mSatellite;
        mapView.setSatellite(mSatellite);
    }
    
    /**
     * Move the destination marker on the map, and zoom to it
     * @param latitude The new latitude of the marker
     * @param longitude The new longitude of the marker
     */
    private void moveDestinationTo(double latitude, double longitude) {
        List<Overlay> mapOverlays = mapView.getOverlays();
        GeoPoint returnValue = new GeoPoint((int) (latitude * 1E6), 
                                            (int) (longitude * 1E6));
        OverlayItem destinationOverlay = new OverlayItem(returnValue,
                "Wake Me Here",
                "Location To Set Off Alarm");
        mItemizedOverlay.addOverlay(destinationOverlay);
        mapOverlays.add(mItemizedOverlay);
        moveMapTo(returnValue);
    }

    /**
     * Move the map to a given point
     * @param latitude The new latitude
     * @param longitude The new longitude
     */
    private void moveMapTo(double latitude, double longitude) {
        GeoPoint location = new GeoPoint((int) (latitude * 1E6), 
                                            (int) (longitude * 1E6));
        moveMapTo(location);
    }

    /**
     * Move the map to a given point
     * @param location The new location
     */
    private void moveMapTo(GeoPoint location) {
        if (location != null) {
            Log.d(LOG_NAME, "moving to " + location.getLatitudeE6() + ", " + location.getLongitudeE6());
            MapController mc = mapView.getController();
            mc.setZoom(15);
            mc.animateTo(location);
        } else {
            Log.e(LOG_NAME, "Location to move to was null");
        }
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.mn_get_location_map, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.mn_orig_loc:
            moveMapTo(mOrigLat, mOrigLong);
            return true;
        case R.id.mn_search:
            onSearchRequested();
            return true;
        case R.id.mn_curr_loc:
            Location here = getCurrentLocation();
            if (here != null) {
                moveMapTo(here.getLatitude(), here.getLongitude());
            } else {
                Log.e(LOG_NAME, "Location inaccessible");
            }
            
            return true;
        case R.id.mn_satellite:
            toggleMapMode();
            return true;
        default:
            return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     * Get the current location via GPS
     * @return Current location
     */
    private Location getCurrentLocation() {
        Location currentLocation = new Location("");
        LocationManager locMan;
        locMan = (LocationManager) getSystemService(LOCATION_SERVICE);
        locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000, 2, this);
        String provider = locMan.getBestProvider(new Criteria(), true);
        if (provider == null) {
            Log.wtf(LOG_NAME, "Provider is null");
            // TODO: do this properly 
            return currentLocation;
        }
        if(!locMan.isProviderEnabled(provider)){
            Log.wtf(LOG_NAME, "Provider is disabled");
            // TODO: do this properly 
            return currentLocation;
        }
        currentLocation = locMan.getLastKnownLocation(provider);
        locMan.removeUpdates(this);

        if(currentLocation == null){
            Log.wtf(LOG_NAME, "Return value from getLastKnownLocation is null");
            // TODO: do this properly 
            return currentLocation;
        }
        return currentLocation;
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    @Override
    public void onLocationChanged(Location location) {
        // TODO Auto-generated method stu
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }
    
    @Override
    public boolean onSearchRequested() {
        Log.d(LOG_NAME, "Searching");
        startSearch(mNick, true, null, false);
        return true;
    }
    
    /**
     * Display a dialog listing the search results
     * @param searchTerm The text entered in the search box
     */
    private void resultsDialog(String searchTerm) {
        mSearchTerm = searchTerm;
        mResults = getSearchLocations(searchTerm);
        
        if (mResults == null) {
            Dialog badConnectionDlg = new AlertDialog.Builder(mContext)
                .setTitle("No data connection")
                .setPositiveButton(R.string.alert_dialog_retry, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        // TODO: How can I retry?
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d(LOG_NAME, "clicked negative");
                    }
                })
                .create();
          
            badConnectionDlg.show();
            return;
        }
        mResultsDialog = new Dialog(mContext);
        mResultsDialog.setContentView(R.layout.search_list);
        Log.d(LOG_NAME, "content view is set");

        ListView list = (ListView) mResultsDialog.findViewById(R.id.result_list);
        Log.d(LOG_NAME, "using list " + list.toString());
        list.setAdapter(new SearchListAdapter(this));
        list.setOnItemClickListener(mResultClickListener);
        Log.d(LOG_NAME, "adapter is set");

        mResultsDialog.setTitle("Location");
        
        Log.d(LOG_NAME, "About to show dialog");
        mResultsDialog.show();
        Log.d(LOG_NAME, "Dialog shown");
    }
    
    /**
     * Search the map for the given text
     * @param address The text to search for
     * @return A list of addresses that match the text
     */
    private List<Address> getSearchLocations(String address) {
        Log.d(LOG_NAME, "getSearchLocations");
        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        List<Address> locations = null;
        try {
            locations = geoCoder.getFromLocationName(address, 5);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (locations == null) {
            Log.wtf(LOG_NAME, "Couldn't retrieve locations: no data connection?");
            return null;
        }
        for (int i = 0; i < locations.size(); i++) {
            Log.d(LOG_NAME, "Location " + i + ": " + locations.get(i).toString());
        }
        return locations;

    }
    
    private OnItemClickListener mResultClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position,
                long id) {
            Log.d(LOG_NAME, "onItemClick(" + parent + ", " + view + ", " + position + ", " + id + ")");
            double latitude = mResults.get(position).getLatitude();
            double longitude = mResults.get(position).getLongitude();
            moveMapTo(latitude, longitude);
            Log.d(LOG_NAME, "dialog is " + view.getContext().toString() + "");
            mResultsDialog.dismiss();
        }
    };
    
    /**
     * The list in the search results dialog
     * @author spookypeanut
     *
     */
    private class SearchListAdapter extends BaseAdapter {
        public SearchListAdapter(Context context) {
            Log.d(LOG_NAME, "LocListAdapter constructor");
            mCurrLoc = getCurrentLocation();
        }

        @Override
        public int getCount() {
            return mResults.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //Log.d(LOG_NAME, "getView(" + position + ")");
            View row;
            
            if (null == convertView) {
                row = mInflater.inflate(R.layout.search_list_entry, null);
            } else {
                row = convertView;
            }
            Address result = mResults.get(position);
            TextView tv = (TextView) row.findViewById(R.id.searchListLine0);
            tv.setText(result.getAddressLine(0));
            tv = (TextView) row.findViewById(R.id.searchListLine1);
            tv.setText(result.getAddressLine(1));
            tv = (TextView) row.findViewById(R.id.searchListLine2);
            tv.setText(result.getAddressLine(2));
            tv = (TextView) row.findViewById(R.id.searchListDist);
            Location resultAsLoc = new Location("");
            resultAsLoc.setLatitude(result.getLatitude());
            resultAsLoc.setLongitude(result.getLongitude());
            tv.setText(uc.out(mCurrLoc.distanceTo(resultAsLoc)) + " away");
           
            
            return row;
        }
    }

    /**
     * Overlay for the destination marker
     * @author spookypeanut
     *
     */
    public class MapOverlay extends ItemizedOverlay<OverlayItem> implements OnGestureListener {
        private GestureDetector gestureDetector;
        private MapView mapView;

        private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
        private Context mContext;

        public MapOverlay(Drawable defaultMarker, Context context) {
            super(boundCenter(defaultMarker));
            mContext = context;
            gestureDetector = new GestureDetector(this);
        }

        public MapOverlay(Drawable defaultMarker) {
            super(boundCenter(defaultMarker));
        }

        public void addOverlay(OverlayItem overlay) {
            mOverlays.add(overlay);
            populate();
        }

        @Override
        protected OverlayItem createItem(int i) {
            return mOverlays.get(i);
        }

        @Override
        public int size() {
            return mOverlays.size();
        }

        @Override
        public boolean onTouchEvent(MotionEvent event, MapView mv) {
            mapView = mv;
            if (gestureDetector.onTouchEvent(event)) {
                return true;
            }
            return false;
        }

        @Override
        public void onLongPress(MotionEvent event) {
            // TODO: this doesn't show, need to thread it http://developer.android.com/guide/appendix/faq/commontasks.html#threading
            Toast.makeText(getApplicationContext(), R.string.long_press_toast,
                    Toast.LENGTH_SHORT).show();

            Geocoder geoCoder = new Geocoder(mContext, Locale.getDefault());
            mDest = mapView.getProjection().fromPixels(
                    (int) event.getX(),
                    (int) event.getY());

            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            dialog.setTitle("Location");
            List<Address> addresses = null;
            Log.d(LOG_NAME, "Attempting geocoder lookup from " + mDest.getLatitudeE6() + ", " + mDest.getLongitudeE6());
            try {
                addresses = geoCoder.getFromLocation(
                        mDest.getLatitudeE6()  / 1E6, 
                        mDest.getLongitudeE6() / 1E6, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String dialogMsg = "Latitude / Longitude:\n";
            dialogMsg += mDest.getLatitudeE6() / 1E6 + ", " + mDest.getLongitudeE6() / 1E6 + "\n";
            if (addresses != null && addresses.size() > 0) 
            {
                for (int i=0; i<addresses.get(0).getMaxAddressLineIndex(); i++)
                    dialogMsg += addresses.get(0).getAddressLine(i) + "\n";
            } else {
                Log.wtf(LOG_NAME, "GeoCoder returned null");
                dialogMsg += "(Address retrieval failed: no data connection?)";
            }
            dialog.setMessage(dialogMsg);
            dialog.setCancelable(true);
            dialog.setPositiveButton(R.string.uselocationbutton,
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,
                        int which) {
                    Intent i = new Intent();
                    i.putExtra("searchTerm", mSearchTerm);
                    setResult(RESULT_OK, i.setAction(
                            mDest.getLatitudeE6() / 1E6 + "," +
                            mDest.getLongitudeE6() / 1E6));
                    finish();
                }
            });
            dialog.setNegativeButton(R.string.dontuselocationbutton, null);
            dialog.show();
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                float velocityY) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
                float distanceY) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

    }
}
