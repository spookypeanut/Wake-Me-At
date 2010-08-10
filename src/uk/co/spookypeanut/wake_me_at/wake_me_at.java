package uk.co.spookypeanut.wake_me_at;

import android.app.Activity;
import android.os.Bundle;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;


public class wake_me_at extends MapActivity{
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        MapView mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
    }
    @Override
    protected boolean isRouteDisplayed() {
        // TODO Auto-generated method stub
        return false;
    }

}