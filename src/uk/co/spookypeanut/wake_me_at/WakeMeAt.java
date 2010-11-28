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
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

public class WakeMeAt extends Activity {
    public static final int GETLOCMAP = 1;
    public static final String PREFS_NAME = "WakeMeAtPrefs";
    public static final String LOG_NAME = "WakeMeAt";
    
    private String mNick = "New Location";
    private double mLatitude = 0.0;
    private double mLongitude = 0.0;
    private float mRadius = 0;
    private String mLocProv = "";
    private DatabaseManager db;
    private long mRowId;
    
    private OnItemSelectedListener locProvListener =  new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent,
                View view, int pos, long id) {
            Log.d(LOG_NAME, "Selected loc prov");
            changedLocProv(parent.getSelectedItem().toString());
        }

        public void onNothingSelected(AdapterView<?> parent) {}
    };
        

    
    private TextWatcher mRadiusWatcher = new TextWatcher() {
        @Override
        public void afterTextChanged(Editable s) {
            changedRadius(Float.valueOf(s.toString()));
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count,
                int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before,
                int count) {}
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
            changedRadius(radius);
            Spinner locProvSpin = (Spinner)findViewById(R.id.loc_provider);
            mLocProv = locProvSpin.getSelectedItem().toString();
            changedLocProv(mLocProv);
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
    private OnClickListener mChangeNickListener = new OnClickListener() {
        public void onClick(View v) {
            Log.d(LOG_NAME, "mChangeNickListener.onClick()");
            Dialog monkey = onCreateDialog(0);
            monkey.show();
        }
    };
    protected void changedLatLong(double latitude, double longitude) {
        changedLatLong(latitude, longitude, false);
    }
    protected void changedLatLong(double latitude, double longitude, boolean load) {
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

    protected void changedLocProv(String locProv) {
        db.setProvider(mRowId, locProv);
    }

    protected void changedNick(String nick) {
        mNick = nick;
        db.setNick(mRowId, nick);
    }

    protected void changedRadius(float radius) {
        mRadius = radius;
        db.setRadius(mRowId, radius);
    }
    
    private long createDefaultRow() {
        return db.addRow (
            "zero", 10.0, 20.0,
            "network", (float) 1800.0
        );
    }
    
    protected void loadLatLong() {
        changedLatLong(0, 0, true);
    }
    
    protected void loadLocProv() {
        mLocProv = db.getProvider(mRowId);
        Spinner locProvSpin = (Spinner)findViewById(R.id.loc_provider);
        SpinnerAdapter adapter = locProvSpin.getAdapter();
        for(int i = 0; i < adapter.getCount(); i++) {
            if(adapter.getItem(i).equals(mLocProv)) {
                locProvSpin.setSelection(i);
            }
        }
    }
    
    protected void loadNick() {
        mNick = db.getNick(mRowId);
        Button nickButton = (Button)findViewById(R.id.nickButton);
        nickButton.setText(mNick);
    }
    
    protected void loadRadius() {
        mRadius = db.getRadius(mRowId);
        TextView radText = (TextView)findViewById(R.id.radius);
        radText.setText(String.valueOf(mRadius));
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
    
    protected void onActivityResult (int requestCode,
            int resultCode, Intent data) {
        if (requestCode == GETLOCMAP) {
            String latLongString = data.getAction();

            String tempStrings[] = latLongString.split(",");
            String latString = tempStrings[0];
            String longString = tempStrings[1];
            double latDbl = Double.valueOf(latString.trim()).doubleValue();
            double longDbl = Double.valueOf(longString.trim()).doubleValue();
            changedLatLong(latDbl, longDbl);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        db = new DatabaseManager(this);
        Log.d(LOG_NAME, "Created db");
        Button button;
        
        mRowId = settings.getLong("currRowId", (int) -1);
        Log.d(LOG_NAME, "Row detected: " + mRowId);
        if (mRowId == -1) {
            mRowId = createDefaultRow();
            Log.d(LOG_NAME, "Row created");
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("currRowId", mRowId);
            editor.commit();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        button = (Button)findViewById(R.id.nickButton);
        button.setOnClickListener(mChangeNickListener);
        
        button = (Button)findViewById(R.id.getLocationMapButton);
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
        s.setOnItemSelectedListener(locProvListener);
        
        loadNick();
        // TODO: add popup for entering nick
        loadLatLong();
        loadRadius();
        loadLocProv();
    }

    @Override
    protected Dialog onCreateDialog(int type) {
            LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.text_input, null);
            final EditText nickBox = (EditText)textEntryView.findViewById(R.id.input_edit);
            return new AlertDialog.Builder(WakeMeAt.this)
                .setIcon(R.drawable.x)
                .setTitle("blah")
                .setView(textEntryView) 
                .setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        changedNick(nickBox.getText().toString());
                        logOutArray();
                    }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d(LOG_NAME, "clicked negative");
                    }
                })
                .create();
    }
}
