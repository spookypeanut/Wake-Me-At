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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class WakeMeAtService extends Service implements LocationListener {
    static final String ACTION_FOREGROUND = "uk.co.spookypeanut.wake_me_at.service";
    public final String LOG_NAME = WakeMeAt.LOG_NAME;
    // TODO: Set this globally
    private final String BROADCAST_UPDATE = WakeMeAt.BROADCAST_UPDATE;


    private static final int ALARMNOTIFY_ID = 1;
    
    private static final Class<?>[] mStartForegroundSignature = new Class[] {
        int.class, Notification.class};
    private static final Class<?>[] mStopForegroundSignature = new Class[] {
        boolean.class};
    
    private DatabaseManager db;
    private UnitConverter uc;
    
    private long mRowId;
    private String mNick;
    private double mRadius;
    private double mMetresAway = -1.0;
    private Location mCurrLocation = new Location("");
    private Location mFinalDestination = new Location("");
    private String mProvider;
    private String mUnit;
    
    private boolean mAlarm = false;

    private Intent mAlarmIntent;
    
    private LocationManager locationManager;
    private NotificationManager mNM;
    private Notification mNotification;
    private PendingIntent mIntentOnSelect;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];
    
    void startForegroundCompat(int id, Notification notification) {
        // If we have the new startForeground API, then use it.
        if (mStartForeground != null) {
            mStartForegroundArgs[0] = Integer.valueOf(id);
            mStartForegroundArgs[1] = notification;
            try {
                mStartForeground.invoke(this, mStartForegroundArgs);
            } catch (InvocationTargetException e) {
                // Should not happen.
                Log.w(LOG_NAME, "Unable to invoke startForeground", e);
            } catch (IllegalAccessException e) {
                // Should not happen.
                Log.w(LOG_NAME, "Unable to invoke startForeground", e);
            }
            return;
        }
        
        // Fall back on the old API.
        setForeground(true);
        mNM.notify(id, notification);
    }
    
    void stopForegroundCompat(int id) {
        // If we have the new stopForeground API, then use it.
        if (mStopForeground != null) {
            mStopForegroundArgs[0] = Boolean.TRUE;
            try {
                mStopForeground.invoke(this, mStopForegroundArgs);
            } catch (InvocationTargetException e) {
                // Should not happen.
                Log.w(LOG_NAME, "Unable to invoke stopForeground", e);
            } catch (IllegalAccessException e) {
                // Should not happen.
                Log.w(LOG_NAME, "Unable to invoke stopForeground", e);
            }
            return;
        }
        
        // Fall back on the old API.  Note to cancel BEFORE changing the
        // foreground state, since we could be killed at that point.
        mNM.cancel(id);
        setForeground(false);
    }
    
    @Override
    public void onCreate() {
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        db = new DatabaseManager(this);

        try {
            mStartForeground = getClass().getMethod("startForeground",
                    mStartForegroundSignature);
            mStopForeground = getClass().getMethod("stopForeground",
                    mStopForegroundSignature);
        } catch (NoSuchMethodException e) {
            // Running on an older platform.
            mStartForeground = mStopForeground = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(LOG_NAME, "onStartCommand()");
        Bundle extras = intent.getExtras();

        mRowId = extras.getLong("rowId");
        mNick = db.getNick(mRowId);
        mFinalDestination.setLatitude(db.getLatitude(mRowId));
        mFinalDestination.setLongitude(db.getLongitude(mRowId));
        mRadius = db.getRadius(mRowId);
        mProvider = db.getProvider(mRowId);
        mUnit = db.getUnit(mRowId);
        
        uc = new UnitConverter(this, mUnit);
        Log.d(LOG_NAME, "Provider: \"" + mProvider + "\"");
        Log.d(LOG_NAME,
            "Passed latlong: " + mFinalDestination.getLatitude() +
            ", " + mFinalDestination.getLongitude());
        
        locationManager =
            (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        registerLocationListener();
        
        handleCommand(intent);
        updateAlarm();

        Toast.makeText(getApplicationContext(), R.string.foreground_service_started,
                Toast.LENGTH_SHORT).show();
        
        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_NAME, "WakeMeAtService.onDestroy()");
        // Make sure our notification is gone.
        stopForegroundCompat(ALARMNOTIFY_ID);
        unregisterLocationListener();
        Toast.makeText(getApplicationContext(), R.string.foreground_service_stopped,
                Toast.LENGTH_SHORT).show();
        mRowId = -1;
        mAlarm = false;
        mMetresAway = -1;
        updateAlarm();
        db.close();
        super.onDestroy();
    }

    public void registerLocationListener() {
        Log.d(LOG_NAME, "registerLocationListener()");
        if (locationManager == null) {
          Log.e(LOG_NAME,
              "TrackRecordingService: Do not have any location manager.");
          return;
        }
        Log.d(LOG_NAME,
            "Preparing to register location listener w/ TrackRecordingService...");
        try {
          long desiredInterval = 10;
          locationManager.requestLocationUpdates(
              mProvider, desiredInterval,
              10, WakeMeAtService.this);
        } catch (RuntimeException e) {
          Log.e(LOG_NAME,
              "Could not register location listener: " + e.getMessage(), e);
        }
      }

    public void unregisterLocationListener() {
        if (locationManager == null) {
          Log.e(LOG_NAME,
              "locationManager is null");
          return;
        }
        locationManager.removeUpdates(this);
        Log.d(LOG_NAME,
            "Location listener is unregistered");
    }
 
    void handleCommand(Intent intent) {
        if (ACTION_FOREGROUND.equals(intent.getAction())) {
            // In this sample, we'll use the same text for the ticker and the expanded notification
            CharSequence text = getText(R.string.foreground_service_started);

            // Set the icon, scrolling text and timestamp
            mNotification = new Notification(R.drawable.icon, text,
                    System.currentTimeMillis());

            // The PendingIntent to launch our activity if the user selects this notification
            Intent i = new Intent(this, Alarm.class);
            i.putExtra("rowId", mRowId);
            i.putExtra("metresAway", mMetresAway);
            i.putExtra("alarm", mAlarm);
            
            mIntentOnSelect = PendingIntent.getActivity(this, 0, i, 0);

            // Set the info for the views that show in the notification panel.
            mNotification.setLatestEventInfo(this, getText(R.string.foreground_service_started),
                           text, mIntentOnSelect);
            
            startForegroundCompat(ALARMNOTIFY_ID, mNotification);
        }
    }
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
    
    @Override
    public void onLocationChanged(Location location) {
        Log.v(LOG_NAME, "onLocationChanged()");
        mCurrLocation = location;
        mMetresAway = location.distanceTo(mFinalDestination);
        // message is, e.g. You are 200m from Welwyn North
        String message = String.format(getString(R.string.notif_full),
                            uc.out(mMetresAway),
                            mNick);
        mNotification.setLatestEventInfo(this, getText(R.string.app_name), message, mIntentOnSelect);
        mNM.notify(ALARMNOTIFY_ID, mNotification);
        if (mMetresAway < uc.toMetres(mRadius)) {
            soundAlarm();
        } 
        updateAlarm();
    }

    public void updateAlarm () {
        Log.d(LOG_NAME, "Changing the distance away");
        if (mAlarmIntent == null || mAlarmIntent.getClass() != null) {
            mAlarmIntent = new Intent(BROADCAST_UPDATE);
        }
        mAlarmIntent.putExtra("rowId", mRowId);
        mAlarmIntent.putExtra("metresAway", mMetresAway);
        mAlarmIntent.putExtra("alarm", mAlarm);
        mAlarmIntent.putExtra("currLat", mCurrLocation.getLatitude());
        mAlarmIntent.putExtra("currLong", mCurrLocation.getLongitude());
        Log.d(LOG_NAME, "Sending broadcast");
        sendStickyBroadcast(mAlarmIntent);
    }
    
    public void cancelAlarm() {
        Log.d(LOG_NAME, "WakeMeAtService.cancelAlarm");
        mAlarm = false;
        Intent alarmIntent = new Intent(WakeMeAtService.this.getApplication(), Alarm.class);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtra("rowId", mRowId);
        alarmIntent.putExtra("metresAway", mMetresAway);
        
        startActivity(alarmIntent);
    }
    
    public void soundAlarm() {
        mAlarm = true;
        Intent alarmIntent = new Intent(WakeMeAtService.this.getApplication(), Alarm.class);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtra("rowId", mRowId);
        alarmIntent.putExtra("metresAway", mMetresAway);
        startActivity(alarmIntent);
        updateAlarm();
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
