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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


//REF#0010
class Compass extends SurfaceView implements SurfaceHolder.Callback {
    public final String LOG_NAME = WakeMeAt.LOG_NAME;

    private Paint mPaint = new Paint();
    private Path mPath = new Path();

    private CompassThread mThread;

    private float[] mValues = {(float)10.0, (float)10.0, (float)10.0};

    public Compass(Context context) {
        super(context);
        prepareArrow();
    }

    public Compass(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        prepareArrow();
    }

    private void prepareArrow() {
        // Construct a wedge-shaped path
        mPath.moveTo(0, -50);
        mPath.lineTo(-20, 60);
        mPath.lineTo(0, 50);
        mPath.lineTo(20, 60);
        mPath.close();
    }

    @Override
    protected void onFinishInflate() {
        getHolder().addCallback(this);
    }

    //REF#0011
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(LOG_NAME, "Compass.onDraw");
        //Paint paint = mPaint;

        //paint.setColor(Color.RED);
        canvas.drawColor(Color.BLUE);

        /*paint.setAntiAlias(true);
       paint.setStyle(Paint.Style.FILL);

       int w = canvas.getWidth();
       int h = canvas.getHeight();
       int cx = w / 2;
       int cy = h / 2;

       canvas.translate(cx, cy);
       if (mValues != null) {            
           canvas.rotate(-mValues[0]);
       }
       canvas.drawPath(mPath, mPaint);*/
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
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see android.view.SurfaceHolder.Callback#surfaceDestroyed(android.view.SurfaceHolder)
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub

    }

    class CompassThread extends Thread {
        private SurfaceHolder _surfaceHolder;
        private Compass _panel;
        private boolean _run = false;

        public CompassThread(SurfaceHolder surfaceHolder, Compass panel) {
            _surfaceHolder = surfaceHolder;
            _panel = panel;
        }

        public void setRunning(boolean run) {
            _run = run;
        }

        @Override
        public void run() {
            Canvas c;
            while (_run) {
                c = null;
                try {
                    c = _surfaceHolder.lockCanvas(null);
                    synchronized (_surfaceHolder) {
                        _panel.onDraw(c);
                    }
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        _surfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }
    }
}
