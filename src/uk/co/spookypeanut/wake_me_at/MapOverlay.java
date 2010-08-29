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
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.GestureDetector.OnGestureListener;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class MapOverlay extends ItemizedOverlay implements OnGestureListener {
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
		GeoPoint p = mapView.getProjection().fromPixels(
				(int) event.getX(),
				(int) event.getY());

		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle("Location");
		List<Address> addresses = null;
		try {
			addresses = geoCoder.getFromLocation(
					p.getLatitudeE6()  / 1E6, 
					p.getLongitudeE6() / 1E6, 1);
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
		dialog.show();
	}

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	} 
}
