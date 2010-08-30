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

import java.util.List;

import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class GetLocation extends MapActivity
		implements LocationListener {
    MapView mapView;
    MapOverlay itemizedOverlay;
    GeoPoint destination;

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.get_location);
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        
        List<Overlay> mapOverlays = mapView.getOverlays();
        Drawable drawable = this.getResources().getDrawable(R.drawable.x);
        itemizedOverlay = new MapOverlay(drawable, this);
        destination = getCurrentLocation(true);
        OverlayItem destinationOverlay = new OverlayItem(destination,
        		                             "Wake Me Here",
        		                             "Location To Set Off Alarm");
        itemizedOverlay.addOverlay(destinationOverlay);
        mapOverlays.add(itemizedOverlay);
        
        Bundle extras = this.getIntent().getExtras();
        String message = extras.getString("message");
    	Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
    	toast.show();
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
            GeoPoint point = new GeoPoint((int) (currentLocation.getLatitude() * 1E6), 
                                          (int) (currentLocation.getLongitude() * 1E6));
            if (moveMap) {
                MapController mc = mapView.getController(); 
            	mc.animateTo(point);
            }
            locMan.removeUpdates(this);
            return point;

    	
    }
    
    @Override
    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		
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

}
