package uk.co.spookypeanut.wake_me_at;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.Toast;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

/**
 * @author hbush
 *
 */
public class MapOverlay extends ItemizedOverlay {
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;

	public MapOverlay(Drawable defaultMarker, Context context) {
	  super(boundCenter(defaultMarker));
	  mContext = context;
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
	/* (non-Javadoc)
	 * @see com.google.android.maps.ItemizedOverlay#createItem(int)
	 */


	/* (non-Javadoc)
	 * @see com.google.android.maps.ItemizedOverlay#size()
	 */
	@Override
	public int size() {
	  return mOverlays.size();
	}
	
	
	@Override
	protected boolean onTap(int index) {
	  OverlayItem item = mOverlays.get(index);
	  AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
	  dialog.setTitle(item.getTitle());
	  dialog.setMessage(item.getSnippet());
	  dialog.show();
	  return true;
	}
}
