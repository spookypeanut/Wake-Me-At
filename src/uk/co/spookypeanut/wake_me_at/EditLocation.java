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

import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.InputType;
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

public class EditLocation extends Activity {
    public static final int GETLOCMAP = 1;
    public static final String PREFS_NAME = "WakeMeAtPrefs";
    public static final String LOG_NAME = "WakeMeAt";
    
    public static final int NICKDIALOG = 0;
    public static final int RADIUSDIALOG = 1;
    
    private DatabaseManager db;
    private long mRowId;
    
    private String mNick = "New Location";
    private double mLatitude = 0.0;
    private double mLongitude = 0.0;
    private float mRadius = 0;
    private String mLocProv = "";

    private OnItemSelectedListener locProvListener =  new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent,
                View view, int pos, long id) {
            Log.d(LOG_NAME, "Selected loc prov");
            changedLocProv(parent.getSelectedItem().toString());
        }

        public void onNothingSelected(AdapterView<?> parent) {}
    };

    private OnClickListener mGetLocMapListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Intent i = new Intent(EditLocation.this.getApplication(), GetLocationMap.class);
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
            Button radiusButton = (Button)findViewById(R.id.radiusButton);
            Float radius = Float.valueOf(radiusButton.getText().toString());
            changedRadius(radius);
            Spinner locProvSpin = (Spinner)findViewById(R.id.loc_provider);
            mLocProv = locProvSpin.getSelectedItem().toString();
            changedLocProv(mLocProv);
            Intent intent = new Intent(WakeMeAtService.ACTION_FOREGROUND);
            intent.setClass(EditLocation.this, WakeMeAtService.class);
            intent.putExtra("latitude", mLatitude);
            intent.putExtra("longitude", mLongitude);
            intent.putExtra("radius", mRadius);
            intent.putExtra("provider", mLocProv);
            startService(intent);
        }
    };
    
    private OnClickListener mStopListener = new OnClickListener() {
        public void onClick(View v) {
            stopService(new Intent(EditLocation.this, WakeMeAtService.class));
        }
    };
    private OnClickListener mChangeNickListener = new OnClickListener() {
        public void onClick(View v) {
            Dialog monkey = onCreateDialog(NICKDIALOG);
            monkey.show();
        }
    };
    private OnClickListener mChangeRadiusListener = new OnClickListener() {
        public void onClick(View v) {
            Dialog monkey = onCreateDialog(RADIUSDIALOG);
            monkey.show();
        }
    };
    protected void changedLatLong(double latitude, double longitude) {
        db.setLatitude(mRowId, latitude);
        db.setLongitude(mRowId, longitude);
        mLatitude = latitude;
        mLongitude = longitude;
        updateForm();
    }

    protected void changedLocProv(String locProv) {
        db.setProvider(mRowId, locProv);
    }

    protected void changedNick(String nick) {
        mNick = nick;
        db.setNick(mRowId, nick);
        updateForm();
    }

    protected void changedRadius(float radius) {
        mRadius = radius;
        db.setRadius(mRowId, radius);
        updateForm();
    }
    
    private long createDefaultRow() {
        return db.addRow (
            "zero", 10.0, 20.0,
            "network", (float) 1800.0
        );
    }
    
    protected void loadLatLong() {
        mLatitude = db.getLatitude(mRowId);
        mLongitude = db.getLongitude(mRowId);
        updateForm();
    }
    
    protected void loadLocProv() {
        mLocProv = db.getProvider(mRowId);
        updateForm();
    }
    
    protected void updateForm() {
        Button nickButton = (Button)findViewById(R.id.nickButton);
        nickButton.setText(mNick);
        Button radText = (Button)findViewById(R.id.radiusButton);
        radText.setText(String.valueOf(mRadius));
        Spinner locProvSpin = (Spinner)findViewById(R.id.loc_provider);
        SpinnerAdapter adapter = locProvSpin.getAdapter();
        for(int i = 0; i < adapter.getCount(); i++) {
            if(adapter.getItem(i).equals(mLocProv)) {
                locProvSpin.setSelection(i);
            }
        }        
        TextView latText = (TextView)findViewById(R.id.latitude);
        TextView longText = (TextView)findViewById(R.id.longitude);
        latText.setText(String.valueOf(mLatitude));
        longText.setText(String.valueOf(mLongitude));
    }
    
    protected void loadNick() {
        mNick = db.getNick(mRowId);
        updateForm();
    }
    
    protected void loadRadius() {
        Log.d(LOG_NAME, "loadRadius()");
        mRadius = db.getRadius(mRowId);
        updateForm();
    }   

    
    protected void onActivityResult (int requestCode,
            int resultCode, Intent data) {
        if (requestCode == GETLOCMAP && data != null) {
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
        Button button;
        
        Bundle extras = this.getIntent().getExtras();
        int passedRowId = extras.getInt("rowid");
        
        mRowId = passedRowId;
        Log.d(LOG_NAME, "Row detected: " + mRowId);
        if (mRowId == -1) {
            mRowId = createDefaultRow();
            Log.d(LOG_NAME, "Row created");
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("currRowId", mRowId);
            editor.commit();
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.edit_location);

        button = (Button)findViewById(R.id.nickButton);
        button.setOnClickListener(mChangeNickListener);
        
        button = (Button)findViewById(R.id.getLocationMapButton);
        button.setOnClickListener(mGetLocMapListener);
        
        button = (Button)findViewById(R.id.startService);
        button.setOnClickListener(mStartListener);

        button = (Button)findViewById(R.id.stopService);
        button.setOnClickListener(mStopListener);
        
        Button radiusBox = (Button)findViewById(R.id.radiusButton);
        radiusBox.setOnClickListener(mChangeRadiusListener);

        LocationManager tmpLM = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        List<String> providers = tmpLM.getProviders(true);
        if (providers.isEmpty()) {
            Log.wtf(LOG_NAME, "How can there be no location providers!?");
        }
        Log.d(LOG_NAME, providers.toString());

        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, providers);

        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        Spinner s = (Spinner) findViewById(R.id.loc_provider);
        s.setAdapter(spinnerArrayAdapter);
        
        s.setOnItemSelectedListener(locProvListener);
        
        loadNick();
        loadLatLong();
        loadRadius();
        loadLocProv();
    }

    @Override
    protected Dialog onCreateDialog(int type) {
            LayoutInflater factory = LayoutInflater.from(this);
            final View textEntryView = factory.inflate(R.layout.text_input, null);
            final EditText inputBox = (EditText)textEntryView.findViewById(R.id.input_edit);
            String title = "";
            DialogInterface.OnClickListener positiveListener = null;
            switch (type) {
                case NICKDIALOG:
                    title = "Location name";
                    positiveListener = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            changedNick(inputBox.getText().toString());
                            db.logOutArray();
                        }
                    };
                break;
                case RADIUSDIALOG:
                    title = "Radius";
                    inputBox.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    positiveListener = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            changedRadius(Float.valueOf(inputBox.getText().toString()));
                            db.logOutArray();
                        }
                    };
                break;
                default:
                    Log.wtf(LOG_NAME, "Invalid dialog type " + type);
            }
            return new AlertDialog.Builder(EditLocation.this)
                .setTitle(title)
                .setView(textEntryView) 
                .setPositiveButton(R.string.alert_dialog_ok, positiveListener)
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d(LOG_NAME, "clicked negative");
                    }
                })
                .create();
    }
}
