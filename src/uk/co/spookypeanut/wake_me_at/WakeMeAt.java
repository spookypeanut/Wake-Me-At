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
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class WakeMeAt extends Activity {
    public static final int GETLOCMAP = 1;
    public static final String PREFS_NAME = "WakeMeAtPrefs";
    public static final String LOG_NAME = "WakeMeAt";
    
    private double mLatitude = 0.0;
    private double mLongitude = 0.0;
    private float mRadius = 0;
    private String mLocProv = "";
    private DatabaseManager db;
    private long mRowId;
    
    private TextWatcher mRadiusWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            Log.d(LOG_NAME, "afterTextChanged");
            radiusChanged(Float.valueOf(s.toString()));
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {
            // TODO Auto-generated method stub
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {
            Log.d(LOG_NAME, "onTextChanged");
        }
    };
    
    private OnClickListener mGetLocMapListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Intent i = new Intent(WakeMeAt.this.getApplication(), GetLocationMap.class);
            EditText searchAddrBox = (EditText)findViewById(R.id.searchAddrBox);
            String searchAddr = searchAddrBox.getText().toString();
            
            i.putExtra("searchAddr", searchAddr);
            Log.d(LOG_NAME, i.toString());
            Log.d(LOG_NAME, searchAddr);
            startActivityForResult(i, GETLOCMAP);
        }
    };
    
    private OnClickListener mStartListener = new OnClickListener() {
        public void onClick(View v) {
            EditText radiusBox = (EditText)findViewById(R.id.radius);
            Float radius = Float.valueOf(radiusBox.getText().toString());
            radiusChanged(radius);
            Spinner locProvSpin = (Spinner)findViewById(R.id.loc_provider);
            mLocProv = locProvSpin.getSelectedItem().toString();
            locProvChanged(mLocProv);
            Intent intent = new Intent(WakeMeAtService.ACTION_FOREGROUND);
            intent.setClass(WakeMeAt.this, WakeMeAtService.class);
            intent.putExtra("latitude", mLatitude);
            intent.putExtra("longitude", mLongitude);
            intent.putExtra("radius", mRadius);
            intent.putExtra("provider", mLocProv);
            startService(intent);
        }
    };
    private OnClickListener mStopListener = new OnClickListener() {
        public void onClick(View v) {
            stopService(new Intent(WakeMeAt.this, WakeMeAtService.class));
        }
    };
    protected void onCreate(Bundle savedInstanceState) {
        
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        db = new DatabaseManager(this);
        Log.d(LOG_NAME, "Created db");

        mRowId = settings.getLong("currRowId", (int) -1);
        Log.d(LOG_NAME, "Row detected: " + mRowId);
        if (mRowId == -1) {
            mRowId = createDefaultRow();
            Log.d(LOG_NAME, "Row created");
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("currRowId", mRowId);
            editor.commit();
        }
        Log.d(LOG_NAME, "Nick: " + db.getNick(mRowId));

        
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Button button = (Button)findViewById(R.id.getLocationMapButton);
        button.setOnClickListener(mGetLocMapListener);
        
        button = (Button)findViewById(R.id.startService);
        button.setOnClickListener(mStartListener);

        button = (Button)findViewById(R.id.stopService);
        button.setOnClickListener(mStopListener);
        
        EditText radiusBox = (EditText)findViewById(R.id.radius);
        radiusBox.addTextChangedListener(mRadiusWatcher);

        LocationManager tmpLM = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Spinner s = (Spinner) findViewById(R.id.loc_provider);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, tmpLM.getProviders(false));
        s.setAdapter(spinnerArrayAdapter);
        
        loadLatLong();
        loadRadius();
    }

    private void logOutArray() {
        Log.d(LOG_NAME, "Start of array log");
        ArrayList<ArrayList<Object>> data = db.getAllRowsAsArrays();
        for (int position=0; position < data.size(); position++)
        { 
            ArrayList<Object> row = data.get(position);
            Log.d(LOG_NAME, row.get(0).toString() + ", " +
                            row.get(1).toString() + ", " +
                            row.get(2).toString() + ", " +
                            row.get(3).toString() + ", " +
                            row.get(4).toString() + ", " +
                            row.get(5).toString());
        }
        Log.d(LOG_NAME, "End of array log");
    }

    private long createDefaultRow() {
        return db.addRow (
            "zero", 10.0, 20.0,
            "network", (float) 1800.0
        );
    }

    protected void loadLatLong() {
        latLongChanged(0, 0, true);
    }
    
    protected void latLongChanged(double latitude, double longitude) {
        latLongChanged(latitude, longitude, false);
    }
    
    protected void latLongChanged(double latitude, double longitude, boolean load) {
        logOutArray();
        if (load) {
            latitude = db.getLatitude(mRowId);
            longitude = db.getLongitude(mRowId);
        } else {
            db.setLatitude(mRowId, latitude);
            db.setLongitude(mRowId, longitude);
        }
        logOutArray();
        mLatitude = latitude;
        mLongitude = longitude;
        TextView latText = (TextView)findViewById(R.id.latitude);
        TextView longText = (TextView)findViewById(R.id.longitude);
        latText.setText(String.valueOf(latitude));
        longText.setText(String.valueOf(longitude));
    }
    
    protected void loadRadius() {
        mRadius = db.getRadius(mRowId);
        TextView radText = (TextView)findViewById(R.id.radius);
        radText.setText(String.valueOf(mRadius));
    }
    
    protected void radiusChanged(float radius) {
        mRadius = radius;
        db.setRadius(mRowId, radius);
    }
    
    protected void loadLocProv() {
        locProvChanged("", true);
    }
    
    protected void locProvChanged(String locProv) {
        locProvChanged(locProv, false);
    }
    
    protected void locProvChanged(String locProv, boolean load) {
        if (load) {
            db.getProvider(mRowId);
        } else {
            db.setProvider(mRowId, locProv);
        }
        mLocProv = locProv;
        Spinner locProvSpin = (Spinner)findViewById(R.id.loc_provider);
        SpinnerAdapter adapter = locProvSpin.getAdapter();
        for(int i = 0; i < adapter.getCount(); i++) {
                        if(adapter.getItem(i).equals(locProv)) {
                            locProvSpin.setSelection(i);
                        }
                  }
    }
    protected void onActivityResult (int requestCode,
            int resultCode, Intent data) {
        if (requestCode == GETLOCMAP) {
            String latLongString = data.getAction();

            String tempStrings[] = latLongString.split(",");
            String latString = tempStrings[0];
            String longString = tempStrings[1];
            double latDbl = Double.valueOf(latString.trim()).doubleValue();
            double longDbl = Double.valueOf(longString.trim()).doubleValue();
            latLongChanged(latDbl, longDbl);
        }
    }
}
