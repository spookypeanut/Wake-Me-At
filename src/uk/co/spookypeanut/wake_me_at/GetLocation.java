package uk.co.spookypeanut.wake_me_at;

import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.OverlayItem;

public class GetLocation extends MapActivity implements LocationListener {
    MapView mapView;
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        setContentView(R.layout.get_location);
        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        
        List<Overlay> mapOverlays = mapView.getOverlays();
        Drawable drawable = this.getResources().getDrawable(R.drawable.x);
        MapOverlay itemizedoverlay = new MapOverlay(drawable);
        GeoPoint point = getCurrentLocation(true);
        OverlayItem overlayitem = new OverlayItem(point, "Hola, Mundo!", "I'm in Mexico City!");
        itemizedoverlay.addOverlay(overlayitem);
        mapOverlays.add(itemizedoverlay);
    }
    
    private GeoPoint getCurrentLocation(boolean moveMap) {
    		GeoPoint returnValue = new GeoPoint(0,0);
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 2, this);
            String provider = locationManager.getBestProvider(new Criteria(), true);
            if (provider == null) {
            	// TODO: do this properly 
                    return returnValue;
            }
            if(!locationManager.isProviderEnabled(provider)){
            	// TODO: do this properly 
            		return returnValue;
            }
            Location currentLocation = locationManager.getLastKnownLocation(provider);
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
            locationManager.removeUpdates(this);
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