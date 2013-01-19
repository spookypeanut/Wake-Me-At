package uk.co.spookypeanut.wake_me_at;

import java.io.IOException;
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
import android.os.Handler;
import android.os.IBinder;

import android.text.format.Time;
import android.util.Log;
import android.widget.Toast;

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


/**
 * The service that watches the current location, and triggers the alarm if
 * required
 * @author spookypeanut
 */
public class WakeMeAtService extends Service implements LocationListener {
    static final String ACTION_FOREGROUND = "uk.co.spookypeanut.wake_me_at.service";
    private String LOG_NAME;
    private String BROADCAST_UPDATE;
    public static boolean serviceRunning = false;

    // The minimum time (in milliseconds) before reporting the location again
    static final long SECONDS = 1000;
    private long mMinTime;
    private long mNoLocationWarningTime;
    private long mWarningRepeat;

    // The minimum distance (in metres) before reporting the location again
    static final float minDistance = 0;

    private Handler mHandler = new Handler();
    Time lastLocation = new Time();

    private static final int ALARMNOTIFY_ID = 1;

    private static final Class<?>[] mStartForegroundSignature = new Class[] {
        int.class, Notification.class};
    private static final Class<?>[] mStopForegroundSignature = new Class[] {
        boolean.class};

    private DatabaseManager db;
    private UnitConverter uc;

    private long mRowId;
    private String mNick;
    private Location mCurrLocation = new Location("");
    private Location mFinalDestination = new Location("");
    private double mMetresAway = -1.0;
    private int mPreset;
    private float mRadius;
    private int mProvider;
    private String mUnit;
    private boolean mWarnSound;
    private boolean mWarnVibrate;
    private boolean mWarnToast;
    private boolean mWarningOn;

    private boolean mAlarm = false;

    private Intent mAlarmIntent;

    private LocationManager mLocationManager;
    private NotificationManager mNM;
    private Notification mNotification;
    private PendingIntent mIntentOnSelect;
    private Method mStartForeground;
    private Method mStopForeground;
    private Object[] mStartForegroundArgs = new Object[2];
    private Object[] mStopForegroundArgs = new Object[1];

    /**
     * Copied from one of the API example tools. One day I'll have to
     * actually go through and figure out what this does (or rather,
     * why it does it)
     * @param id
     * @param notification
     */
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
        LOG_NAME = (String) getText(R.string.app_name_nospaces);
        BROADCAST_UPDATE = (String) getText(R.string.serviceBroadcastName);
        mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        db = new DatabaseManager(this);

        if(MockLocations.isMockLocationSet(this) == true) {
            try {
                mockLocations = new MockLocations(this.getApplicationContext());
                Toast.makeText(getApplicationContext(), R.string.mock_locations_allowed, Toast.LENGTH_LONG).show();
            } catch (IOException e) {
                Log.e(LOG_NAME, "unable to create MockLocations instance", e);
            }
        } else {
            Toast.makeText(getApplicationContext(), R.string.mock_locations_not_allowed, Toast.LENGTH_LONG).show();
            Log.e(LOG_NAME, "'Allow Mock Locations' setting is not set");
        }

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

        // Get everything we need from the database
        mRowId = extras.getLong("rowId");
        Log.d(LOG_NAME, "row Id for alarm is " + mRowId);
        mNick = db.getNick(mRowId);
        mFinalDestination.setLatitude(db.getLatitude(mRowId));
        mFinalDestination.setLongitude(db.getLongitude(mRowId));
        Log.d(LOG_NAME,
                "Passed latlong: " + mFinalDestination.getLatitude() +
                ", " + mFinalDestination.getLongitude());
        mPreset = db.getPreset(mRowId);
        Presets presetObj = new Presets(this, mPreset);
        // If this is set to custom, use the values from the database
        // If not, use the values from the preset
        if (presetObj.isCustom()) {
            mRadius = db.getRadius(mRowId);
            mProvider = db.getProvider(mRowId);
            mUnit = db.getUnit(mRowId);
        } else {
            mRadius = presetObj.getRadius();
            mProvider = presetObj.getLocProv();
            mUnit = presetObj.getUnit();
        }
        mWarnSound = db.getWarnSound(mRowId);
        mWarnVibrate = db.getWarnVibrate(mRowId);
        mWarnToast = db.getWarnToast(mRowId);
        // We turn the warning on if the global warning flag is true, and if
        // at least one of the warning types is true
        mWarningOn = db.getWarning(mRowId) && (mWarnSound ||
                                               mWarnVibrate ||
                                               mWarnToast);
        Log.d(LOG_NAME, "Provider: \"" + mProvider + "\"");

        String lp = this.getResources()
                        .getStringArray(R.array.locProvAndroid)[mProvider];
        if ("gps".equals(lp)) {
            mMinTime = 10 * SECONDS;
            mNoLocationWarningTime = 30 * SECONDS;
        } else {
            mMinTime = 45 * SECONDS;
            mNoLocationWarningTime = 90 * SECONDS;
        }
        mWarningRepeat = mMinTime;

        uc = new UnitConverter(this, mUnit);

        mLocationManager =
            (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        registerLocationListener();

        if (!mLocationManager.isProviderEnabled(lp)) {
            stopService();
            return START_NOT_STICKY;

        }
        createNotification(intent);

        serviceRunning = true;
        updateAlarm();

        Toast.makeText(getApplicationContext(),
                       R.string.foreground_service_started,
                       Toast.LENGTH_SHORT).show();

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }


    private void stopService() {
        // Make sure our notification is gone.
        stopForegroundCompat(ALARMNOTIFY_ID);
        unregisterLocationListener();
        mHandler.removeCallbacks(mCheckLocationAge);

        // Set everything back to default values, and tell the alarm activity
        mRowId = -1;
        mAlarm = false;
        mMetresAway = -1;
        updateAlarm();
    }

    @Override
    public void onDestroy() {
        Log.d(LOG_NAME, "WakeMeAtService.onDestroy()");
        Toast.makeText(getApplicationContext(),
                       R.string.foreground_service_stopped,
                       Toast.LENGTH_SHORT).show();
        stopService();

        serviceRunning = false;
        db.close();
        if (mAlarmIntent != null && mAlarmIntent.getClass() != null) {
            removeStickyBroadcast(mAlarmIntent);
        }
        super.onDestroy();
    }

    /**
     * Method that registers the service as a location listener
     */
    public void registerLocationListener() {
        Log.d(LOG_NAME, "registerLocationListener()");
        if (mLocationManager == null) {
            Log.e(LOG_NAME,
                  "TrackRecordingService: Do not have any location manager.");
            return;
        }
        Log.d(LOG_NAME,
              "Preparing to register loc listener w/TrackRecordingService...");
        try {
            String lp = this.getResources()
                            .getStringArray(R.array.locProvAndroid)[mProvider];
            mLocationManager.requestLocationUpdates(lp,
                                                    mMinTime,
                                                    minDistance,
                                                    WakeMeAtService.this);
        } catch (RuntimeException e) {
            Log.e(LOG_NAME,
                  "Couldn't register location listener: " + e.getMessage(), e);
        }
    }

    /**
     * Unregister the location listener. Called in onDestroy.
     */
    public void unregisterLocationListener() {
        if (mLocationManager == null) {
          Log.e(LOG_NAME,
              "locationManager is null");
          return;
        }
        mLocationManager.removeUpdates(this);
        Log.d(LOG_NAME,
              "Location listener is unregistered");
    }

    void createNotification(Intent intent) {
        if (ACTION_FOREGROUND.equals(intent.getAction())) {
            // The text to use as the title of our notification
            CharSequence text = getText(R.string.foreground_service_started);

            // Set the icon, scrolling text and timestamp
            mNotification = new Notification(R.drawable.icontaskbar, text,
                    System.currentTimeMillis());

            // The PendingIntent to launch our activity if the user
            // selects this notification
            Intent i = new Intent(this, Alarm.class);
            i.putExtra("rowId", mRowId);
            i.putExtra("metresAway", mMetresAway);
            i.putExtra("alarm", mAlarm);

            // It appears that the extras aren't updated, so we use
            // FLAG_CANCEL_CURRENT to completely start from scratch
            mIntentOnSelect = PendingIntent.getActivity(this, 0, i,
                                         PendingIntent.FLAG_CANCEL_CURRENT);

            // Set the info for the views that show in the notification panel.
            CharSequence msg = getText(R.string.foreground_service_started);
            mNotification.setLatestEventInfo(this, msg, text,
                                             mIntentOnSelect);

            startForegroundCompat(ALARMNOTIFY_ID, mNotification);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLocationChanged(Location location) {
        lastLocation.setToNow();
        Log.v(LOG_NAME, "onLocationChanged(" +
                        lastLocation.toMillis(false) + ")");
        mHandler.removeCallbacks(mCheckLocationAge);
        if (mWarningOn) {
            mHandler.postDelayed(mCheckLocationAge, mMinTime);
        }

        mCurrLocation = location;
        mMetresAway = location.distanceTo(mFinalDestination);
        // message is, e.g. You are 200m from Welwyn North
        String message = String.format(getString(R.string.notif_full),
                            uc.out(mMetresAway),
                            mNick);
        mNotification.setLatestEventInfo(this, getText(R.string.app_name),
                                         message, mIntentOnSelect);
        // Turn off the sound and vibrate, in case they were turned
        // on by the old location warning
        mNotification.defaults &= ~Notification.DEFAULT_SOUND;
        mNotification.defaults &= ~Notification.DEFAULT_VIBRATE;
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
        mAlarmIntent.putExtra("locTime", lastLocation.toMillis(true));
        Log.d(LOG_NAME, "Sending broadcast");
        Log.d(LOG_NAME, "mRowId: " + mRowId);
        sendStickyBroadcast(mAlarmIntent);
    }

    public void cancelAlarm() {
        Log.d(LOG_NAME, "WakeMeAtService.cancelAlarm");
        mAlarm = false;
        Intent alarmIntent = new Intent(WakeMeAtService.this.getApplication(),
                                        Alarm.class);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtra("rowId", mRowId);
        alarmIntent.putExtra("metresAway", mMetresAway);

        startActivity(alarmIntent);
    }

    public void soundAlarm() {
        mAlarm = true;
        Intent alarmIntent = new Intent(WakeMeAtService.this.getApplication(),
                                        Alarm.class);
        alarmIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        alarmIntent.putExtra("rowId", mRowId);
        alarmIntent.putExtra("metresAway", mMetresAway);
        startActivity(alarmIntent);
        updateAlarm();
    }

    @Override
    public void onProviderDisabled(String provider) {
        String lp = this.getResources()
                        .getStringArray(R.array.locProvAndroid)[mProvider];
        if (provider != lp) {
            Log.wtf(LOG_NAME, "Current provider (" + lp +
                  ") doesn't match the listener provider (" + provider + ")");

        }
        String message = String.format(getString(R.string.providerDisabledMessage),
                this.getResources().getStringArray(R.array.locProvHuman)[mProvider]);
        Toast.makeText(getApplicationContext(), message,
                Toast.LENGTH_LONG).show();
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(LOG_NAME, "onProviderEnabled(" + provider + ")");
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(LOG_NAME, "onStatusChanged(" + provider + ", " + status + ")");
    }

    private Runnable mCheckLocationAge = new Runnable() {
        public void run() {
            Time currTime = new Time();
            currTime.setToNow();
            long millis = currTime.toMillis(false) -
                          lastLocation.toMillis(false);

            Log.d(LOG_NAME, "Curr: " + currTime.toMillis(false) +
                            ", last: " + lastLocation.toMillis(false));
            Log.d(LOG_NAME, "Diff: " + millis +
                            " vs limit: " + mNoLocationWarningTime);
            if (millis >= mNoLocationWarningTime) {
                oldLocationWarning(millis / 1000);
                mHandler.removeCallbacks(mCheckLocationAge);
                mHandler.postDelayed(mCheckLocationAge, mWarningRepeat);
                return;
            }
            mHandler.removeCallbacks(mCheckLocationAge);
            mHandler.postDelayed(mCheckLocationAge, mMinTime);
        }

    };

    private void oldLocationWarning(long age) {
        String msg = String.format(getString(R.string.oldLocationWarning), age);
        if (mWarnToast) {
            Toast.makeText(getApplicationContext(), msg,
                    Toast.LENGTH_LONG).show();
        }

        // We should still add a notification that the location is old, even
        // if none of the warning settings are turned on
        Context context = getApplicationContext();
        CharSequence contentTitle = "Old location";
        CharSequence contentText = msg;

        Intent notificationIntent = new Intent(this, Alarm.class);
        PendingIntent contentIntent;
        contentIntent = PendingIntent.getActivity(this, 0,
                                                  notificationIntent, 0);
        if (mWarnSound) {
            mNotification.defaults |= Notification.DEFAULT_SOUND;
        }
        if (mWarnVibrate) {
            mNotification.defaults |= Notification.DEFAULT_VIBRATE;
        }

        mNotification.setLatestEventInfo(context, contentTitle,
                                         contentText, contentIntent);
        mNM.notify(ALARMNOTIFY_ID, mNotification);
    }
}
