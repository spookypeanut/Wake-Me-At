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
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class Alarm extends Activity implements TextToSpeech.OnInitListener, OnUtteranceCompletedListener {
    public static final String PREFS_NAME = "WakeMeAtPrefs";
    public final String LOG_NAME = WakeMeAt.LOG_NAME;
    private final int TTS_REQUEST_CODE = 27;
    private final String POST_UTTERANCE = "WMAPostUtterance";
    // TODO: Set this globally
    private final String BROADCAST_UPDATE = "uk.co.spookypeanut.wake_me_at.alarmupdate";
    
    private DatabaseManager db;
    private UnitConverter uc;
    private MediaPlayer mMediaPlayer;
    private Vibrator mVibrator;
    
    private boolean mVibrateOn = false;
    private boolean mNoiseOn = false;
    //TODO: Should I ignore all tts stuff if this is off? Probably
    private boolean mSpeechOn = false;
    
    private long mRowId;
    
    private Location mFinalDestination = new Location("");
    
    private String mNick;
    private String mUnit;
    private boolean mAlarm;
    
    private double mMetresAway;

    private TextToSpeech mTts;
    
    @Override
    protected void onCreate(Bundle icicle) {
        Log.d(LOG_NAME, "Alarm.onCreate");
        super.onCreate(icicle);
        
        db = new DatabaseManager(this);
        mMediaPlayer = new MediaPlayer();
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
        Log.v(LOG_NAME, "" + R.id.alarmMessageTextView);
        TextView tv = (TextView)findViewById(R.id.alarmMessageTextView);
        Log.v(LOG_NAME, tv.toString());
        Log.v(LOG_NAME, "" + mMetresAway);
        Log.v(LOG_NAME, uc.out(mMetresAway));
        Log.v(LOG_NAME, mNick);
        String message;
        if (mMetresAway < 0) {
            message = "Awaiting first location fix";
        } else {
            message = String.format(getString(R.string.alarmMessage),
                    uc.out(mMetresAway), mNick);
        }
        Log.v(LOG_NAME, message);
        tv.setText(message);
        if (mAlarm && mSpeechOn) speak();

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
        mAlarm = true;
        if (mSpeechOn) speak();
        if (mVibrateOn) mVibrator.vibrate(pattern, 0);
        if (mNoiseOn && (mMediaPlayer.isPlaying() == false)) {
            startAlarmtone();
        }
    }
    
    private void stopAlarm() {
        if (mMediaPlayer != null) {
            try {
                mMediaPlayer.stop();
            } catch (IllegalStateException e) {
                e.printStackTrace();
            }
            mMediaPlayer.release();
        }
        if (mVibrator != null) {
            mVibrator.cancel();
        }
        if (mTts != null) {
            mTts.stop();
        }
    }
    
    private boolean startAlarmtone() {
        mMediaPlayer.reset();
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
        final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        if (audioManager.getStreamVolume(AudioManager.STREAM_ALARM) != 0) {
                   mMediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
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
                    Log.d(LOG_NAME, "Headset: " + audioManager.isWiredHeadsetOn());
                    Log.d(LOG_NAME, "Speakerphone: " + audioManager.isSpeakerphoneOn());
                   mMediaPlayer.start();
        }
        return true;
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        Log.v(LOG_NAME, "onNewIntent(" + intent.toString());
        Bundle extras = intent.getExtras();
        rowChanged(extras.getLong("rowId"));
        distanceChanged(extras.getDouble("metresAway"));
        if (extras.getBoolean("alarm") == true) {
            startAlarm();
        } else if (extras.getBoolean("alarm") == false) {
            stopAlarm();
        }
    }
    @Override
    protected void onResume() { 
        super.onResume();
        IntentFilter filter = new IntentFilter(BROADCAST_UPDATE);
        this.registerReceiver(this.mReceiver, filter);
        Log.d(LOG_NAME, "Registered receiver");
    }
    
    @Override
    protected void onPause() {
        this.unregisterReceiver(this.mReceiver);
        Log.d(LOG_NAME, "Unregistered receiver");
        super.onPause();
    }
    
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        Log.v(LOG_NAME, "onActivityResult(" +
                requestCode + ", " +
                resultCode + ", ");
        if (requestCode == TTS_REQUEST_CODE) {
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
        if (mAlarm) startAlarm();
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
        stopAlarm();
        stopService(new Intent(Alarm.this, WakeMeAtService.class));
    }
    
    private OnClickListener mEditLocation = new Button.OnClickListener() {
        public void onClick(View v) {
            stopService();
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
            stopService();
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
        }
   };
}
