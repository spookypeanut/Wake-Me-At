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

import java.util.HashMap;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnUtteranceCompletedListener;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class Alarm extends Activity implements TextToSpeech.OnInitListener, OnUtteranceCompletedListener {
    public static final String PREFS_NAME = "WakeMeAtPrefs";
    public final String LOG_NAME = WakeMeAt.LOG_NAME;
    private final int TTS_REQUEST_CODE = 1;
    private final String POST_UTTERANCE = "WMAPostUtterance";
    
    private DatabaseManager db;
    private UnitConverter uc;
    private long mRowId;
    
    private String mNick;
    private double mLatitude;
    private double mLongitude;
    private float mRadius;
    private String mLocProv;
    private int mUnit;

    private TextToSpeech mTts;
    
    @Override
    protected void onCreate(Bundle icicle) {
        Log.d(LOG_NAME, "Alarm.onCreate");
        super.onCreate(icicle);
        
        Bundle extras = this.getIntent().getExtras();
        mRowId = extras.getLong("rowId");

        db = new DatabaseManager(this);
        mNick = db.getNick(mRowId);
        mLatitude = db.getLatitude(mRowId);
        mLongitude = db.getLongitude(mRowId);
        mRadius = db.getRadius(mRowId);
        mLocProv = db.getProvider(mRowId);
        mUnit = db.getUnit(mRowId);
        
        uc = new UnitConverter(this, mUnit);

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
        
    }
    
    @Override
    protected void onActivityResult(
            int requestCode, int resultCode, Intent data) {
        if (requestCode == TTS_REQUEST_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                // success, create the TTS instance
                Log.d(LOG_NAME, "text to speech present");
                mTts = new TextToSpeech(this, this);
                Locale loc = new Locale("en", "","");
                if(mTts.isLanguageAvailable(loc) >= TextToSpeech.LANG_AVAILABLE){
                    mTts.setLanguage(loc);
                }
                mTts.setOnUtteranceCompletedListener(this);
            } else {
                // missing data, install it
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
        Log.d(LOG_NAME, "Alarm.onInit(" + status + ")");
        HashMap<String, String> myHashAlarm = new HashMap<String, String>();
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM,
                String.valueOf(AudioManager.STREAM_ALARM));
        myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
        POST_UTTERANCE);
        String speech = String.format(getString(R.string.alarmSpeech),
                                      1234, uc.getName(), mNick);
        mTts.speak(speech, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
    }
    

    @Override
    protected void onDestroy() {
      super.onDestroy();
      mTts.shutdown();
    }
    
    private void stopService() {
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
}
