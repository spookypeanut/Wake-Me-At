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
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.OnGestureListener;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

//@SuppressWarnings("unused")
public class GetLocationMap extends MapActivity
implements LocationListener {
    public static final String PREFS_NAME = "WakeMeAtPrefs";
    public final String LOG_NAME = "WakeMe@";
    MapView mapView;
    MapOverlay itemizedOverlay;
    GeoPoint destination;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.get_location_map);
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        setDefaultKeyMode(DEFAULT_KEYS_SEARCH_LOCAL);

        Bundle extras = this.getIntent().getExtras();
        String searchAddr = extras.getString("searchAddr").trim();

        List<Overlay> mapOverlays = mapView.getOverlays();
        Drawable drawable = this.getResources().getDrawable(R.drawable.x);
        itemizedOverlay = new MapOverlay(drawable, this);

        if (searchAddr.length() == 0) {
            Toast.makeText(getApplicationContext(),
                    "No search terms, using current location",
                    Toast.LENGTH_SHORT).show();
            destination = getCurrentLocation(true);
        } else {
            Toast.makeText(getApplicationContext(),
                    "Searching for \"" + searchAddr + "\"",
                    Toast.LENGTH_SHORT).show();
            destination = getSearchLocation(searchAddr, true);
        }
        OverlayItem destinationOverlay = new OverlayItem(destination,
                                            "Wake Me Here",
                                            "Location To Set Off Alarm");
        itemizedOverlay.addOverlay(destinationOverlay);
        mapOverlays.add(itemizedOverlay);
    }

    private GeoPoint getSearchLocation(String address, boolean moveMap) {
        Geocoder geoCoder = new Geocoder(this, Locale.getDefault());
        GeoPoint returnValue = new GeoPoint(0,0);
        try {
            List<Address> locations = geoCoder.getFromLocationName(
                    address, 5);
            if (locations.size() > 0) {
                returnValue = new GeoPoint(
                        (int) (locations.get(0).getLatitude() * 1E6), 
                        (int) (locations.get(0).getLongitude() * 1E6));
                //         mapView.invalidate();
            }    
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (moveMap) {
            MapController mc = mapView.getController(); 
            mc.animateTo(returnValue);
        }
        return returnValue;

    }    
    private GeoPoint getCurrentLocation(boolean moveMap) {
        GeoPoint returnValue = new GeoPoint(0,0);
        LocationManager locMan;
        locMan = (LocationManager) getSystemService(LOCATION_SERVICE);
        locMan.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                1000, 2, this);
        String provider = locMan.getBestProvider(new Criteria(), true);
        if (provider == null) {
            // TODO: do this properly 
            return returnValue;
        }
        if(!locMan.isProviderEnabled(provider)){
            // TODO: do this properly 
            return returnValue;
        }
        Location currentLocation = locMan.getLastKnownLocation(provider);
        if(currentLocation == null){
            // TODO: do this properly 
            return returnValue;
        }
        returnValue = new GeoPoint((int) (currentLocation.getLatitude() * 1E6), 
                (int) (currentLocation.getLongitude() * 1E6));
        if (moveMap) {
            MapController mc = mapView.getController(); 
            mc.animateTo(returnValue);
        }
        locMan.removeUpdates(this);
        return returnValue;
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }
    /**
     * @see android.app.Activity#onResume()
     */
    @Override
    public void onResume() {
        super.onResume();
    }

    /**
     * @param icicle
     */
    public void onPause(Bundle icicle) {
        super.onPause();
    }

    @Override
    public void onSaveInstanceState(Bundle icicle) {
        super.onSaveInstanceState(icicle);
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
        Bundle appDataBundle = null;
        appDataBundle = new Bundle();
        appDataBundle.putString("demo_key", "nothing");

        // Now call the Activity member function that invokes the Search Manager UI.
        startSearch(null, false, appDataBundle, false);
        
        // Returning true indicates that we did launch the search, instead of blocking it.
        return true;
    }
    
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

        public boolean onTouchEvent(MotionEvent event, MapView mv) {
            mapView = mv;
            if (gestureDetector.onTouchEvent(event)) {
                return true;
            }
            return false;
        }
        

        @Override
        public void onLongPress(MotionEvent event) {
            Geocoder geoCoder = new Geocoder(mContext, Locale.getDefault());
            destination = mapView.getProjection().fromPixels(
                    (int) event.getX(),
                    (int) event.getY());

            AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
            dialog.setTitle("Location");
            List<Address> addresses = null;
            try {
                addresses = geoCoder.getFromLocation(
                        destination.getLatitudeE6()  / 1E6, 
                        destination.getLongitudeE6() / 1E6, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }
            String address = "";
            if (addresses.size() > 0) 
            {
                for (int i=0; i<addresses.get(0).getMaxAddressLineIndex(); 
                        i++)
                    address += addresses.get(0).getAddressLine(i) + "\n";
            }

            dialog.setMessage(address);
            dialog.setCancelable(true);
            dialog.setPositiveButton(R.string.uselocationbutton,
                    new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,
                        int which) {
                    Intent i = new Intent();
                    setResult(RESULT_OK, i.setAction(
                                             destination.getLatitudeE6() / 1E6 + "," +
                                             destination.getLongitudeE6() / 1E6));
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
