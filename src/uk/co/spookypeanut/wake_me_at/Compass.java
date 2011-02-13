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

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;



//REF#0010
class Compass extends SurfaceView implements SurfaceHolder.Callback {
    public final String LOG_NAME = WakeMeAt.LOG_NAME;

    private final float RADTODEGREES = (float) (180.0 / 3.14159265);

    
    private Paint mPaint = new Paint();
    private Path mPath = new Path();

    private CompassThread mThread;
    
    private SensorManager mSensorManager;
    private int mLayoutWidth, mLayoutHeight;
    private SurfaceView mSurfaceView;

    private float[] mOriValues = {(float)10.0, (float)10.0, (float)10.0};
    
    private float[] mGravity = new float[3];
    private float[] mGeoMag = new float[3];
    private float[] mRotMatrix = new float[16];
    private float[] mInclMatrix = new float[16];


    public Compass(Context context) {
        super(context);
        prepareArrow();
    }

    public Compass(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        Log.d(LOG_NAME, "Compass constructor");
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);
        mSensorManager = (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
        prepareArrow();

        // create thread only; it's started in surfaceCreated()
        mThread = new CompassThread(holder, context, new Handler() {
            @Override
            public void handleMessage(Message m) {
                Log.d(LOG_NAME, "CompassThread.handleMessage");
            }
        });
    }

    
    private void prepareArrow() {
        // Construct a wedge-shaped path
        mPath.moveTo(0, -50);
        mPath.lineTo(-20, 60);
        mPath.lineTo(0, 50);
        mPath.lineTo(20, 60);
        mPath.close();
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
        Paint paint = mPaint;

        canvas.drawColor(Color.WHITE);
        paint.setColor(Color.BLACK);

        paint.setAntiAlias(true);
        paint.setStyle(Paint.Style.FILL);

        int cx = mLayoutWidth / 2;
        int cy = mLayoutHeight / 2;
        
        canvas.translate(cx, cy);
        if (mOriValues != null) {
            canvas.rotate(-mOriValues[0] * RADTODEGREES);
        }
        
        Bitmap _scratch = BitmapFactory.decodeResource(getResources(), R.drawable.compass);
        Bitmap scaled = Bitmap.createScaledBitmap(_scratch, 200, 200, true);
        canvas.drawBitmap(scaled, -cx, -cy, null);
        
        //canvas.drawPath(mPath, mPaint);
    }

    /* (non-Javadoc)
     * @see android.view.SurfaceHolder.Callback#surfaceChanged(android.view.SurfaceHolder, int, int, int)
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        // TODO Auto-generated method stub
    }

    /* (non-Javadoc)
     * @see android.view.SurfaceHolder.Callback#surfaceCreated(android.view.SurfaceHolder)
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mThread.setRunning(true);
        mThread.start();
        
        mSurfaceView = (SurfaceView) findViewById(R.id.compassSurface);
        mLayoutWidth = mSurfaceView.getWidth();
        mLayoutHeight = mSurfaceView.getHeight();
        Log.d(LOG_NAME, "SurfaceView is " + mLayoutWidth + "x" + mLayoutHeight);
        
        Sensor sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mSensorManager.registerListener(mListener, sensor, SensorManager.SENSOR_DELAY_GAME);
        sensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(mListener, sensor, SensorManager.SENSOR_DELAY_GAME);
    }

    /* (non-Javadoc)
     * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.SurfaceHolder)
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mSensorManager.unregisterListener(mListener);
        boolean retry = true;
        mThread.setRunning(false);
        while (retry) {
            try {
                mThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }

    class CompassThread extends Thread {
        private SurfaceHolder mSurfaceHolder;
        private boolean mRun = false;

        public CompassThread(SurfaceHolder surfaceHolder, Context context,
                Handler handler) {
            mSurfaceHolder = surfaceHolder;
        }
        
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
}
