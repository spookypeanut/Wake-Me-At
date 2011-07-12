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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

//REF#0010
/**
 * Class for the compass which appears on the alarm / tracking screen
 * @author spookypeanut
 */
class Compass extends SurfaceView implements SurfaceHolder.Callback {
    private final String LOG_NAME;
    private final String BROADCAST_UPDATE;

    private final float RADTODEGREES = (float) (180.0 / 3.14159265);

    private Paint mPaint = new Paint();
    private Path mNeedlePath = new Path();

    private CompassThread mThread = null;
    private Context mContext;

    private final int mBkgColor = getResources().getColor(R.color.mainbg);
    private final int mDialColor = getResources().getColor(R.color.compassdials);
    private final int mNeedleColor = getResources().getColor(R.color.compassneedle);
    
    private SensorManager mSensorManager;
    private int mLayoutWidth, mLayoutHeight;
    private SurfaceView mSurfaceView;

    private float[] mOriValues = {(float)10.0, (float)10.0, (float)10.0};
    
    private float[] mGravity = new float[3];
    private float[] mGeoMag = new float[3];
    private float[] mRotMatrix = new float[16];
    private float[] mInclMatrix = new float[16];
    
    private double mCurrLat, mCurrLong;
    private Location mCurrLoc;
    private Location mDestLoc;
    private long mRowId;

    public Compass(Context context) {
        super(context);
        LOG_NAME = (String) context.getText(R.string.app_name_nospaces);
        BROADCAST_UPDATE = (String) context.getText(R.string.serviceBroadcastName);
    }

    public Compass(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        LOG_NAME = (String) context.getText(R.string.app_name_nospaces);
        BROADCAST_UPDATE = (String) context.getText(R.string.serviceBroadcastName);
        
        Log.d(LOG_NAME, "Compass constructor");
        mContext = context;
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        preparePaths();

        mCurrLoc = new Location("");
        mDestLoc = new Location("");
    }

    
    private void drawDials(Canvas canvas) {
        Paint paint = mPaint;
        Path outer = new Path();
        Path inner = new Path();
        Path northMark = new Path();
        Path eastMark = new Path();
        final int drawColor = mDialColor;
        final int bkgColor = mBkgColor;
        final int radius = 80;
        final int lineWidth = 3;
        final int fontSize = 35;

        paint.setColor(drawColor);
        outer.addCircle(0, 0, radius + lineWidth, Path.Direction.CW);
        canvas.drawPath(outer, paint);

        paint.setColor(bkgColor);
        inner.addCircle(0, 0, radius - lineWidth, Path.Direction.CW);
        canvas.drawPath(inner, paint);

        paint.setColor(drawColor);
        paint.setTextSize(fontSize);
        paint.setTextAlign(Paint.Align.CENTER);
        // Up is negative. Which seems strange.
        canvas.drawText ("N", (float) 0.0, (float) -1.4 * radius, paint);

        northMark.moveTo((float) lineWidth, (float) (radius));
        northMark.lineTo((float) -lineWidth, (float) (radius));
        northMark.lineTo((float) -lineWidth, (float) (-radius * 1.2));
        northMark.lineTo((float) lineWidth, (float) (-radius * 1.2));
        northMark.close();
        canvas.drawPath(northMark, paint);

        eastMark.moveTo((float) (-radius), (float) lineWidth);
        eastMark.lineTo((float) (-radius), (float) -lineWidth);
        eastMark.lineTo((float) (radius), (float) -lineWidth);
        eastMark.lineTo((float) (radius), (float) lineWidth);
        eastMark.close();
        canvas.drawPath(eastMark, paint);
    }

    private void preparePaths() {
        // Construct a wedge-shaped path
        int needleWidth = 15;
        int needleLength = 60;
        
        mNeedlePath.moveTo(0, -needleLength);
        mNeedlePath.lineTo(-needleWidth, needleLength);
        mNeedlePath.lineTo(0, (float) (needleLength * 0.75));
        mNeedlePath.lineTo(needleWidth, needleLength);
        mNeedlePath.close();
    }

    private final SensorEventListener mListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
            boolean sensorReady = false;
            //Log.d(LOG_NAME, "Compass.onSensorChanged, " + event.sensor.getType());

            switch (event.sensor.getType()) {
            case Sensor.TYPE_MAGNETIC_FIELD:
                mGeoMag = event.values.clone();
                sensorReady = true;
                break;
            case Sensor.TYPE_ACCELEROMETER:
                mGravity = event.values.clone();
                sensorReady = true;
                break;
            }   

            if (sensorReady && mGeoMag != null && mGravity != null) {
                //Log.d(LOG_NAME, "All values present");
                sensorReady = false;

                SensorManager.getRotationMatrix(mRotMatrix, mInclMatrix, mGravity, mGeoMag);
                SensorManager.getOrientation(mRotMatrix, mOriValues);
            }

        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub
        }
    };
    
    //REF#0011, REF#0012
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (canvas == null) {
            return;
        }
        Paint paint = mPaint;

        canvas.drawColor(mBkgColor);

        int cx = mLayoutWidth / 2;
        int cy = mLayoutHeight / 2;
        
        canvas.setMatrix(null);
        canvas.translate(cx, cy);
        
        float northRotation = 0;
        if (mOriValues != null) {
            northRotation = -mOriValues[0] * RADTODEGREES;
            canvas.rotate(northRotation);
        }

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        drawDials(canvas);

        float bearing = mCurrLoc.bearingTo(mDestLoc);
        canvas.rotate(bearing);
        paint.setColor(mNeedleColor);
        canvas.drawPath(mNeedlePath, mPaint);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        // TODO Auto-generated method stub
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        if (mThread == null) {
            mThread = new CompassThread(holder, mContext, new Handler() {
                @Override
                public void handleMessage(Message m) {
                    Log.d(LOG_NAME, "CompassThread.handleMessage");
                }
            });
        }
        
        mThread.setRunning(true);
        Log.d(LOG_NAME, "Before start, mThread.getState(): " + mThread.getState());
        mThread.start();
        Log.d(LOG_NAME, "After start, mThread.getState(): " + mThread.getState());

        mSurfaceView = (SurfaceView) findViewById(R.id.compassSurface);
        mLayoutWidth = mSurfaceView.getWidth();
        mLayoutHeight = mSurfaceView.getHeight();
        Log.d(LOG_NAME, "SurfaceView is " + mLayoutWidth + "x" + mLayoutHeight);
        
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(mListener, sensor, SensorManager.SENSOR_DELAY_UI);
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(mListener, sensor, SensorManager.SENSOR_DELAY_UI);
        
        IntentFilter filter = new IntentFilter(BROADCAST_UPDATE);
        mContext.registerReceiver(this.mReceiver, filter);
        Log.d(LOG_NAME, "Registered receiver");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mContext.unregisterReceiver(this.mReceiver);
        Log.d(LOG_NAME, "Unregistered receiver");
        mSensorManager.unregisterListener(mListener);
        boolean retry = true;
        Log.d(LOG_NAME, "Before setRunning(false), mThread.getState(): " + mThread.getState());

        mThread.setRunning(false);
        Log.d(LOG_NAME, "After setRunning(false), mThread.getState(): " + mThread.getState());

        while (retry) {
            try {
                mThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
        Log.d(LOG_NAME, "After mThread.join(), mThread.getState(): " + mThread.getState());
        CompassThread limbo = mThread;
        mThread = null;
        limbo.interrupt();
    }

    /**
     * Thread class for the compass surface
     * @author spookypeanut
     */
    class CompassThread extends Thread {
        private SurfaceHolder mSurfaceHolder;
        private boolean mRun = false;

        public CompassThread(SurfaceHolder surfaceHolder, Context context,
                Handler handler) {
            mSurfaceHolder = surfaceHolder;
        }
        
        /**
         * Set the running flag of the thread
         * @param run True if thread should be set to running
         */
        public void setRunning(boolean run) {
            mRun = run;
        }

        @Override
        public void run() {
            Canvas c;
            while (mRun) {
                c = null;
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {
                        onDraw(c);
                    }
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
    }
    
    /**
     * Method called when the location on the service has changed
     * @param rowId
     */
    private void locationChanged(long rowId) {
        if (rowId >= 0) {
            DatabaseManager db = new DatabaseManager(mContext);
            mDestLoc.setLatitude(db.getLatitude(rowId));
            mDestLoc.setLongitude(db.getLongitude(rowId));
            db.close();
        }
    }
    
    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_NAME, "Received broadcast");
            Bundle extras = intent.getExtras();
            mCurrLat = extras.getDouble("currLat");
            mCurrLong = extras.getDouble("currLong");
            Log.d(LOG_NAME, "currently " + mCurrLat + ", " + mCurrLong);
            mCurrLoc.setLatitude(mCurrLat);
            mCurrLoc.setLongitude(mCurrLong);
            if (mRowId != extras.getLong("rowId")) {
                mRowId = extras.getLong("rowId");
                locationChanged(mRowId);
            }
            
        }
   };
}
