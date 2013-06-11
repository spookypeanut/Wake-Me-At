package uk.co.spookypeanut.wake_me_at;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;

import android.app.ActionBar;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.text.format.Time;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
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


public class Alarm extends Activity implements TextToSpeech.OnInitListener, OnUtteranceCompletedListener {
    public static final String PREFS_NAME = "WakeMeAtPrefs";
    private String LOG_NAME;
    private String BROADCAST_UPDATE;
    private final int TTS_REQUEST_CODE = 27;
    private final String POST_UTTERANCE = "WMAPostUtterance";

    private DatabaseManager db;
    private UnitConverter uc;
    private MediaPlayer mMediaPlayer;
    private Vibrator mVibrator;
    private TextToSpeech mTts;
    private Handler mHandler = new Handler();
    // In milliseconds
    private long mLastLocTime;

    private boolean mVibrateOn = true;
    private boolean mNoiseOn = true;
    private boolean mSpeechOn = false;
    private boolean mToastOn = false;

    private float mCrescVolume = (float) 0.1;
    private long mRowId;
    private Location mFinalDestination = new Location("");
    private String mNick;
    private String mUnit;
    private boolean mAlarmSounding;

    private double mMetresAway;
    
    protected void alarmWindowFlags() {
        //REF#0026
        final Window win = getWindow();
        // This method of waking up the device seems to be required on > 4.0
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                   | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                   | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                   | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                   | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
    }
    
    @Override
    protected void onCreate(Bundle icicle) {
        LOG_NAME = (String) getText(R.string.app_name_nospaces);
        BROADCAST_UPDATE = (String) getText(R.string.serviceBroadcastName);
        super.onCreate(icicle);

        setVolumeControlStream(AudioManager.STREAM_ALARM);

        db = new DatabaseManager(this);
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, TTS_REQUEST_CODE);

        Intent thisIntent = this.getIntent();
        Bundle extras = thisIntent.getExtras();
        if (extras.getBoolean("alarm") == true) {
            alarmWindowFlags();
        }
        setContentView(R.layout.alarm);
        if (android.os.Build.VERSION.SDK_INT >= 11) {
            ActionBar actionBar = getActionBar();
            actionBar.setTitle(R.string.alarmTitle);
            actionBar.setSubtitle(R.string.app_name);
        }
        Button button = (Button)findViewById(R.id.alarmButtonStop);
        button.setOnClickListener(mStopService);

        button = (Button)findViewById(R.id.alarmButtonEdit);
        button.setOnClickListener(mEditLocation);

        button = (Button)findViewById(R.id.alarmButtonMain);
        button.setOnClickListener(mMainWindow);

        onNewIntent(thisIntent);
    }

    private void rowChanged(long rowId) {
        db.logOutArray();
        mRowId = rowId;
        mNick = db.getNick(mRowId);
        mFinalDestination.setLatitude(db.getLatitude(mRowId));
        mFinalDestination.setLongitude(db.getLongitude(mRowId));

        mUnit = db.getUnit(mRowId);
        mNoiseOn = db.getSound(mRowId);
        mVibrateOn = db.getVibrate(mRowId);
        mSpeechOn = db.getSpeech(mRowId);
        mToastOn = db.getToast(mRowId);

        uc = new UnitConverter(this, mUnit);
    }

    private void distanceChanged(double distance, long locTime) {
        mMetresAway = distance;
        mLastLocTime = locTime;
        updateText();
        if (mAlarmSounding && mSpeechOn) speak();
    }

    private void updateText() {
        Time currTime = new Time();
        currTime.setToNow();
        long locAge = (currTime.toMillis(true) - mLastLocTime) / 1000;

        String messageOne;
        String messageTwo;
        if (mMetresAway < 0) {
            messageOne = (String) getText(R.string.alarmAwaitingFix);
            messageTwo = "";
        } else {
            messageOne = String.format(getString(R.string.alarmMessage),
                    uc.out(mMetresAway), mNick);
            messageTwo = String.format(getString(R.string.locAgeMessage),
                    locAge);
        }
        TextView tv = (TextView)findViewById(R.id.alarmMessageOneTextView);
        tv.setText(messageOne);
        tv = (TextView)findViewById(R.id.alarmMessageTwoTextView);
        tv.setText(messageTwo);
        mHandler.removeCallbacks(mRunEverySecond);
        mHandler.postDelayed(mRunEverySecond, 1000);
    }

    private Runnable mRunEverySecond = new Runnable() {
        public void run() {
            updateText();
            if (mMediaPlayer != null && db.getCresc(mRowId) && mCrescVolume < 1) {
                mCrescVolume += .1;
                mMediaPlayer.setVolume(mCrescVolume, mCrescVolume);
            }
            mHandler.removeCallbacks(mRunEverySecond);
            if (WakeMeAtService.serviceRunning) {
                mHandler.postDelayed(mRunEverySecond, 1000);
            }
        }
    };

    private void speak() {
        if (mTts != null && false == mTts.isSpeaking()) {
        //if (mTts != null) {
            HashMap<String, String> myHashAlarm = new HashMap<String, String>();
            myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
                            String.valueOf(AudioManager.STREAM_ALARM));
            myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
                            POST_UTTERANCE);
            String speech = String.format(getString(R.string.alarmSpeech),
                                          uc.outSpeech(mMetresAway), mNick);
            mTts.speak(speech, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
        }
    }

    private void startAlarm() {
        long pattern[] = {100, 300, 100, 100, 100, 100, 100, 200, 100, 400};

        if (mSpeechOn) speak();
        if (mVibrateOn) mVibrator.vibrate(pattern, 0);
        if (mNoiseOn) {
            startAlarmtone();
        }
        if (false == mAlarmSounding) {
            Log.d(LOG_NAME, "Sound alarm!");
            mAlarmSounding = true;

            if (mToastOn) {
                String message = String.format(getString(R.string.alarmMessage),
                    uc.out(mMetresAway), mNick);
                Toast.makeText(getApplicationContext(), message,
                    Toast.LENGTH_LONG).show();
            }
        }
    }

    private void stopAlarm() {
        if (mMediaPlayer != null) {
            try {
                final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
                //REF#0009
                audioManager.abandonAudioFocus(null);
                mMediaPlayer.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if (mVibrator != null) {
            mVibrator.cancel();
        }
        if (mTts != null) {
            mTts.stop();
        }
    }

    private boolean startAlarmtone() {
        float alarmVolume = (float) 1.0;
        if (null == mMediaPlayer) {
            mMediaPlayer = new MediaPlayer();
            Uri alert = Uri.parse(db.getRingtone(mRowId));
            try {
                mMediaPlayer.setDataSource(this, alert);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
            mMediaPlayer.setVolume(alarmVolume, alarmVolume);
            mMediaPlayer.setLooping(true);
            try {
                 mMediaPlayer.prepare();
            }
            catch (IllegalStateException e) {
                e.printStackTrace();
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            // REF#0008
            final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
            audioManager.requestAudioFocus(null, AudioManager.STREAM_ALARM,
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
        }
        // TODO Something tells me there's a simpler way to set off alarms...
        if (db.getCresc(mRowId)) {
            alarmVolume = mCrescVolume;
        } else {
            alarmVolume = (float) 1.0;
        }
        if (true == mMediaPlayer.isPlaying()) {
            return true;
        }
        if (alarmVolume != 0) {
            mMediaPlayer.start();
        }
        return true;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        rowChanged(extras.getLong("rowId"));
        distanceChanged(extras.getDouble("metresAway"),
                        extras.getLong("locTime"));
        if (extras.getBoolean("alarm") == true) {
            startAlarm();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(BROADCAST_UPDATE);
        this.registerReceiver(this.mReceiver, filter);
        if (true == mAlarmSounding) {
            startAlarm();
        }
    }

    @Override
    protected void onPause() {
        this.unregisterReceiver(this.mReceiver);
        stopAlarm();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (!mAlarmSounding) {
            super.onBackPressed();
        } else {
            Log.w(LOG_NAME, "Pressing back is not allowed while alarm is sounding");
        }
    }

    private void initializeTts() {
        Log.d(LOG_NAME, "Initialize tts");
            mTts = new TextToSpeech(this, this);
            Locale loc = new Locale("en", "","");
            if(mTts.isLanguageAvailable(loc) >= TextToSpeech.LANG_AVAILABLE){
                mTts.setLanguage(loc);
            }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        if (requestCode == TTS_REQUEST_CODE && mSpeechOn == true) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                initializeTts();
            } else {
                Log.e(LOG_NAME, "No TTS data");
                Toast.makeText(getApplicationContext(), R.string.noTtsError,
                        Toast.LENGTH_LONG).show();
                // TODO Move this to EditLocation, when turning speech on
                // missing data, install it
                //Intent installIntent = new Intent();
                //installIntent.setAction(
                //    TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                //startActivity(installIntent);
            }
        }
    }

    @Override
    public void onUtteranceCompleted(String uttId) {
        if (uttId == POST_UTTERANCE) {
            Log.d(LOG_NAME, "finished speaking");
        }
    }

    @Override
    public void onInit(int status) {
        // REF#0003
        if (mTts.setOnUtteranceCompletedListener(this) == TextToSpeech.ERROR) {
            Log.wtf(LOG_NAME, "setOnUtteranceCompletedListener failed");
        }
        if (mAlarmSounding) startAlarm();
    }

    @Override
    protected void onDestroy() {
        db.close();
        if (mTts != null) {
            mTts.shutdown();
        }
        mHandler.removeCallbacks(mRunEverySecond);
        super.onDestroy();
    }

    private void stopService() {
        mAlarmSounding = false;
        stopAlarm();
        stopService(new Intent(Alarm.this, WakeMeAtService.class));
    }

    private OnClickListener mEditLocation = new Button.OnClickListener() {
        public void onClick(View v) {
            if (mAlarmSounding) {
                stopService();
            }
            Intent i = new Intent(Alarm.this.getApplication(), EditLocation.class);
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
            if (mAlarmSounding) {
                stopService();
            }
            Intent i = new Intent(Alarm.this.getApplication(), WakeMeAt.class);
            startActivity(i);
            finish();
        }
    };

    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Bundle extras = intent.getExtras();
            long broadcastId = extras.getLong("rowId");
            if (broadcastId != mRowId && broadcastId > 0) {
                rowChanged(broadcastId);
            }
            distanceChanged(extras.getDouble("metresAway"),
                            extras.getLong("locTime"));
            if (extras.getBoolean("alarm") == true) {
                startAlarm();
            }
        }
   };
}
