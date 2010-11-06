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

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class GetLocation extends Activity {
    public static final int GETLOCMAP = 1;
    public static final String PREFS_NAME = "WakeMeAtPrefs";
    public static final String LOG_NAME = "WakeMeAt";
    
    private double mLatitude = 0.0;
    private double mLongitude = 0.0;
    private float mRadius = 0;
    private String mLocProv = "";


    private OnClickListener mGetLocMapListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Intent i = new Intent(GetLocation.this.getApplication(), GetLocationMap.class);
            EditText searchAddrBox = (EditText)findViewById(R.id.searchAddrBox);
            String searchAddr = searchAddrBox.getText().toString();
            
            i.putExtra("searchAddr", searchAddr);
            Log.d(LOG_NAME, i.toString());
            Log.d(LOG_NAME, searchAddr);
            startActivityForResult(i, GETLOCMAP);
        }
    };
 
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.get_location);

        // Capture our button from layout
        Button button = (Button)findViewById(R.id.getLocationMapButton);
        // Register the onClick listener with the implementation above
        button.setOnClickListener(mGetLocMapListener);
        
        loadLatLong();
    }

    protected void loadLatLong() {
        latLongChanged(0, 0, true);
    }
    
    protected void latLongChanged(float latitude, float longitude) {
        latLongChanged(latitude, longitude, false);
    }
    
    protected void latLongChanged(float latitude, float longitude, boolean load) {
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        if (load) {
            latitude = settings.getFloat("latitude", (float) 0.0);
            longitude = settings.getFloat("longitude", (float) 0.0);
        } else {
            SharedPreferences.Editor editor = settings.edit();
            editor.putFloat("latitude", latitude);
            editor.putFloat("longitude", longitude);
            editor.commit();
        }
        mLatitude = latitude;
        mLongitude = longitude;
//        String latLongString = latitude + "," + longitude;
//        Toast.makeText(getApplicationContext(), latLongString,
//                Toast.LENGTH_SHORT).show();
        TextView latText = (TextView)findViewById(R.id.latitude);
        TextView longText = (TextView)findViewById(R.id.longitude);
        latText.setText(String.valueOf(latitude));
        longText.setText(String.valueOf(longitude));
    }
    

    protected void onActivityResult (int requestCode,
            int resultCode, Intent data) {
        if (requestCode == GETLOCMAP) {
            String latLongString = data.getAction();

            String tempStrings[] = latLongString.split(",");
            String latString = tempStrings[0];
            String longString = tempStrings[1];
            float latFloat = Float.valueOf(latString.trim()).floatValue();
            float longFloat = Float.valueOf(longString.trim()).floatValue();
            latLongChanged(latFloat, longFloat);
        }
    }
}
