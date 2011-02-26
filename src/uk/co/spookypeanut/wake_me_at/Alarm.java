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
import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.os.PowerManager.WakeLock;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

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

    private boolean mVibrateOn = true;
    private boolean mNoiseOn = true;
    private boolean mSpeechOn = false;
    
    private long mRowId;
    private Location mFinalDestination = new Location("");
    private String mNick;
    private String mUnit;
    private boolean mAlarmSounding;
    
    private double mMetresAway;
    
    @Override
    protected void onCreate(Bundle icicle) {
        LOG_NAME = (String) getText(R.string.app_name_nospaces);
        BROADCAST_UPDATE = (String) getText(R.string.serviceBroadcastName);
        Log.d(LOG_NAME, "Alarm.onCreate");
        super.onCreate(icicle);

        setVolumeControlStream(AudioManager.STREAM_ALARM);

        db = new DatabaseManager(this);
        mVibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        Intent checkIntent = new Intent();
        checkIntent.setAction(TextToSpeech.Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(checkIntent, TTS_REQUEST_CODE);
        
        setContentView(R.layout.alarm);
        
        Button button = (Button)findViewById(R.id.alarmButtonStop);
        button.setOnClickListener(mStopService);
        
        button = (Button)findViewById(R.id.alarmButtonEdit);
        button.setOnClickListener(mEditLocation);
        
        button = (Button)findViewById(R.id.alarmButtonMain);
        button.setOnClickListener(mMainWindow);

        onNewIntent(this.getIntent());
    }
    
    private void rowChanged(long rowId) {
        Log.v(LOG_NAME, "Alarm.rowChanged(" + rowId + ")");
        db.logOutArray();
        // TODO: Some issue here: it seems to not be changing the rowid when activating a second alarm  
        mRowId = rowId;
        mNick = db.getNick(mRowId);
        mFinalDestination.setLatitude(db.getLatitude(mRowId));
        mFinalDestination.setLongitude(db.getLongitude(mRowId));

        mUnit = db.getUnit(mRowId);
        
        uc = new UnitConverter(this, mUnit);
    }
    
    private void distanceChanged(double distance) {
        Log.v(LOG_NAME, "Alarm.distanceChanged(" + distance + ")");
        mMetresAway = distance;
        TextView tv = (TextView)findViewById(R.id.alarmMessageTextView);
        String message;
        if (mMetresAway < 0) {
            message = (String) getText(R.string.alarmAwaitingFix);
        } else {
            message = String.format(getString(R.string.alarmMessage),
                    uc.out(mMetresAway), mNick);
        }
        Log.v(LOG_NAME, message);
        tv.setText(message);
        if (mAlarmSounding && mSpeechOn) speak();

    }
    
    private void speak() {
        if (mTts != null) {
            HashMap<String, String> myHashAlarm = new HashMap<String, String>();
            myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
                            String.valueOf(AudioManager.STREAM_ALARM));
            myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
                            POST_UTTERANCE);
            Log.d(LOG_NAME, "Format is " + getString(R.string.alarmSpeech));
            String speech = String.format(getString(R.string.alarmSpeech),
                                          uc.outSpeech(mMetresAway), mNick);
            mTts.speak(speech, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
        }
    }
    
    private void startAlarm() {
        long pattern[] = {100, 300, 100, 100, 100, 100, 100, 200, 100, 400};

        //REF#0016
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_BRIGHT_WAKE_LOCK |
                        PowerManager.ACQUIRE_CAUSES_WAKEUP, LOG_NAME);
        wl.acquire();
        wl.release();

        mAlarmSounding = true;
        if (mSpeechOn) speak();
        if (mVibrateOn) mVibrator.vibrate(pattern, 0);
        if (mNoiseOn) {
            if (mMediaPlayer == null) {
                startAlarmtone();
            }
        }
    }
    
    private void stopAlarm() {
        if (mMediaPlayer != null) {
            try {
                Log.d(LOG_NAME, "Stopping media player");
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
        Log.d(LOG_NAME, "Alarm.startAlarmtone()");
        mMediaPlayer = new MediaPlayer();
        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM); 
        try {
            mMediaPlayer.setDataSource(this, alert);
        } catch (IllegalArgumentException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SecurityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IllegalStateException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // TODO Something tells me there's a simpler way to set off alarms...
        final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        int alarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);
        if (alarmVolume != 0) {
                    mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
                    mMediaPlayer.setVolume(alarmVolume, alarmVolume);
                    mMediaPlayer.setLooping(true);
                    try {
                     mMediaPlayer.prepare();
                } catch (IllegalStateException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
                    // REF#0008
                    audioManager.requestAudioFocus(null, AudioManager.STREAM_ALARM,
                            AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
                   mMediaPlayer.start();
        }
        return true;
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        Log.v(LOG_NAME, "Alarm.onNewIntent(" + intent.toString());
        Bundle extras = intent.getExtras();
        
        rowChanged(extras.getLong("rowId"));
        distanceChanged(extras.getDouble("metresAway"));
        if (extras.getBoolean("alarm") == true) {
            startAlarm();
        }
    }
    @Override
    protected void onResume() { 
        super.onResume();
        IntentFilter filter = new IntentFilter(BROADCAST_UPDATE);
        this.registerReceiver(this.mReceiver, filter);
        if (mAlarmSounding == true) {
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
        if (! mAlarmSounding) {
            super.onBackPressed();
        } else {
            Log.d(LOG_NAME, "Pressing back is not allowed while alarm is sounding");
        }
    }

    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        Log.v(LOG_NAME, "onActivityResult(" +
                requestCode + ", " +
                resultCode + ", ");
        if (requestCode == TTS_REQUEST_CODE && mSpeechOn == true) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                Log.d(LOG_NAME, "text to speech present");
                mTts = new TextToSpeech(this, this);
                Locale loc = new Locale("en", "","");
                if(mTts.isLanguageAvailable(loc) >= TextToSpeech.LANG_AVAILABLE){
                    mTts.setLanguage(loc);
                }
            } else {
                // missing data, install it
                Log.wtf(LOG_NAME, "No TTS data");
                Intent installIntent = new Intent();
                installIntent.setAction(
                    TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installIntent);
            }

            Log.d(LOG_NAME, "end of onActivityResult");
        }
    }
    
    @Override
    public void onUtteranceCompleted(String uttId) {
        Log.d(LOG_NAME, "finished speaking");
        if (uttId == POST_UTTERANCE) {
            Log.d(LOG_NAME, "finished speaking");
        } 
    }
    
    @Override
    public void onInit(int status) {
        // REF#0003
        Log.d(LOG_NAME, "Alarm.onInit(" + status + ")");
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
      super.onDestroy();
    }
    
    private void stopService() {
        Log.d(LOG_NAME, "Alarm.stopService()");
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
            Log.d(LOG_NAME, "Received broadcast");
            Bundle extras = intent.getExtras();
            distanceChanged(extras.getDouble("metresAway"));
            if (extras.getBoolean("alarm") == true) {
                startAlarm();
            }
        }
   };
}
