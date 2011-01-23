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

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Tracking extends Activity {
    public static final String PREFS_NAME = "WakeMeAtPrefs";
    public final String LOG_NAME = WakeMeAt.LOG_NAME;
    
    private DatabaseManager db;
    private UnitConverter uc;
    private long mRowId;
    
    private Location mFinalDestination = new Location("");
    
    private String mNick;
    private String mUnit;
    
    private double mMetresAway;
    
    @Override
    protected void onCreate(Bundle icicle) {
        Log.d(LOG_NAME, "Tracking.onCreate");
        super.onCreate(icicle);
        
        db = new DatabaseManager(this);

        setContentView(R.layout.tracking);
        
        Button button = (Button)findViewById(R.id.alarmButtonStop);
        button.setOnClickListener(mStopService);
        
        button = (Button)findViewById(R.id.alarmButtonEdit);
        button.setOnClickListener(mEditLocation);
        
        button = (Button)findViewById(R.id.alarmButtonMain);
        button.setOnClickListener(mMainWindow);

        onNewIntent(this.getIntent());
    }
    
    private void rowChanged(long rowId) {
        Log.v(LOG_NAME, "Tracking.rowChanged(" + rowId + ")");
        mRowId = rowId;
        mNick = db.getNick(mRowId);
        mFinalDestination.setLatitude(db.getLatitude(mRowId));
        mFinalDestination.setLongitude(db.getLongitude(mRowId));

        mUnit = db.getUnit(mRowId);
        
        uc = new UnitConverter(this, mUnit);
    }
    
    private void distanceChanged(double distance) {
        Log.v(LOG_NAME, "Tracking.distanceChanged(" + distance + ")");
        mMetresAway = distance;
        Log.v(LOG_NAME, "" + R.id.alarmMessageTextView);
        TextView tv = (TextView)findViewById(R.id.alarmMessageTextView);
        Log.v(LOG_NAME, tv.toString());
        Log.v(LOG_NAME, uc.out(mMetresAway));
        Log.v(LOG_NAME, mNick);
        String message = String.format(getString(R.string.alarmMessage),
                uc.out(mMetresAway), mNick);
        Log.v(LOG_NAME, message);
        tv.setText(message);
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        Log.v(LOG_NAME, "onNewIntent(" + intent.toString());
        Bundle extras = intent.getExtras();
        rowChanged(extras.getLong("rowId"));
        distanceChanged(extras.getDouble("metresAway"));
    }
    
    private void stopService() {
        stopService(new Intent(Tracking.this, WakeMeAtService.class));
    }
    
    private OnClickListener mEditLocation = new Button.OnClickListener() {
        public void onClick(View v) {
            stopService();
            Intent i = new Intent(Tracking.this.getApplication(), EditLocation.class);
            i.putExtra("rowId", mRowId);
            startActivity(i);
            finish();
        }
    };
    
    private OnClickListener mStopService = new Button.OnClickListener() {
        public void onClick(View v) {
            stopService();
            finish();
        }
    };
    
    private OnClickListener mMainWindow = new Button.OnClickListener() {
        public void onClick(View v) {
            stopService();
            Intent i = new Intent(Tracking.this.getApplication(), WakeMeAt.class);
            startActivity(i);
            finish();
        }
    };
}
