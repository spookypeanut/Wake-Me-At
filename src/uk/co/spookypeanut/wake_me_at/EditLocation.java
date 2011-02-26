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
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.LocationManager;
import android.media.AudioManager;
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
import android.widget.ToggleButton;
import android.widget.AdapterView.OnItemSelectedListener;

/**
 * Activity for editing a location saved in the database
 * @author spookypeanut
 *
 */
public class EditLocation extends Activity {
    public static final int GETLOCMAP = 1;
    public static final String PREFS_NAME = "WakeMeAtPrefs";

    private String LOG_NAME;
    private String BROADCAST_UPDATE;
    
    public static final int NICKDIALOG = 0;
    public static final int RADIUSDIALOG = 1;
    private boolean mDialogOpen = false;
    
    private DatabaseManager db;
    private UnitConverter uc;

    private long mRowId;
    private String mNick = "New Location";
    private double mLatitude = 0.0;
    private double mLongitude = 0.0;
    private float mRadius = 0;
    private String mLocProv = "";
    private String mUnit = "";

    private OnItemSelectedListener locProvListener =  new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent,
                View view, int pos, long id) {
            Log.d(LOG_NAME, "Selected loc prov: " + parent.getSelectedItem().toString());
            changedLocProv(parent.getSelectedItem().toString());
        }
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    private OnItemSelectedListener unitListener =  new OnItemSelectedListener() {
        public void onItemSelected(AdapterView<?> parent,
                View view, int pos, long id) {
            Log.d(LOG_NAME, "Selected unit: " + parent.getSelectedItem().toString());
            changedUnit(parent.getSelectedItem().toString());
        }
        public void onNothingSelected(AdapterView<?> parent) {}
    };

    private OnClickListener mGetLocMapListener = new Button.OnClickListener() {
        public void onClick(View v) {
            getLoc();
        }
    };
    
    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_NAME, "Received broadcast");
            Bundle extras = intent.getExtras();
            long broadcastRowId = extras.getLong("rowId");
            serviceRunning(broadcastRowId == mRowId);
        }
   };
   
   /**
    * Method that is called when it's determined that an alarm service is running
    * @param isThisRow True if the service that's running is for the
    *                  same location as this activity
    */
    private void serviceRunning (boolean isThisRow) {
       ToggleButton tg = (ToggleButton)findViewById(R.id.editLocationRunningToggle);
       tg.setChecked(isThisRow);
       
       Button button = (Button)findViewById(R.id.startService);
       button.setEnabled(!isThisRow);

       button = (Button)findViewById(R.id.stopService);
       button.setEnabled(isThisRow);
   }
   
   @Override
   protected void onPause() {
       this.unregisterReceiver(this.mReceiver);
       Log.d(LOG_NAME, "Unregistered receiver");
       super.onPause();
   }
   
   @Override
   protected void onResume() { 
       super.onResume();
       IntentFilter filter = new IntentFilter(BROADCAST_UPDATE);
       this.registerReceiver(this.mReceiver, filter);
       Log.d(LOG_NAME, "Registered receiver");
   }

    /**
     * Get a new user-specified location, via
     * a GetLocationMap activity
     */
    private void getLoc() {
        if (mDialogOpen == false) {
            Intent i = new Intent(EditLocation.this.getApplication(), GetLocationMap.class);
            i.putExtra("latitude", mLatitude);
            i.putExtra("longitude", mLongitude);
            i.putExtra("nick", mNick);
            Log.d(LOG_NAME, i.toString());
            startActivityForResult(i, GETLOCMAP);
        } else {
            Log.w(LOG_NAME, "Dialog open, skipping location map");
        }
    }
    
    private OnClickListener mStartListener = new OnClickListener() {
        public void onClick(View v) {
            startService();
        }
    };
    
    private OnClickListener mStopListener = new OnClickListener() {
        public void onClick(View v) {
            stopService();
        }
    };
    
    private OnClickListener mRunningListener = new OnClickListener() {
        public void onClick(View v) {
            Log.d(LOG_NAME, "mRunningListener, v = " + v);
            ToggleButton tg = (ToggleButton) v;
            if (tg.isChecked()) {
                startService();
            } else {
                stopService();
            }
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
    
    /**
     * Starts the alarm service for the current activity's database entry
     */
    private void startService () {
        Button radiusButton = (Button)findViewById(R.id.radiusButton);
        Float radius = Float.valueOf(radiusButton.getText().toString());
        changedRadius(radius);
        Spinner unitSpin = (Spinner)findViewById(R.id.unitList);
        mUnit = unitSpin.getSelectedItem().toString();
        changedUnit(mUnit);
        Spinner locProvSpin = (Spinner)findViewById(R.id.loc_provider);
        mLocProv = locProvSpin.getSelectedItem().toString();
        changedLocProv(mLocProv);
        Intent intent = new Intent(WakeMeAtService.ACTION_FOREGROUND);
        intent.setClass(EditLocation.this, WakeMeAtService.class);
        intent.putExtra("rowId", mRowId);
        startService(intent);
    }
    
    /**
     * Stops the alarm service
     */
    private void stopService() {
        stopService(new Intent(EditLocation.this, WakeMeAtService.class));
    }
    
    /**
     * Method called to install a new latitude and longitude in that database
     * @param latitude
     * @param longitude
     */
    protected void changedLatLong(double latitude, double longitude) {
        db.setLatitude(mRowId, latitude);
        db.setLongitude(mRowId, longitude);
        mLatitude = latitude;
        mLongitude = longitude;
        updateForm();
    }

    /**
     * Method called to change the location provider in the database
     * @param locProv
     */
    protected void changedLocProv(String locProv) {
        Log.d(LOG_NAME, "changedLocProv");
        mLocProv = locProv;
        db.setProvider(mRowId, locProv);
    }

    /**
     * Method called to change the unit of distance used in the database
     * @param unit
     */
    protected void changedUnit(String unit) {
        Log.d(LOG_NAME, "changedUnit");
        mUnit = unit;
        db.setUnit(mRowId, unit);
        Log.d(LOG_NAME, "end changedUnit");
    }
    
    /**
     * Method called to change the location's nickname in the database
     * @param nick
     */
    protected void changedNick(String nick) {
        mNick = nick;
        db.setNick(mRowId, nick);
        updateForm();
    }

    /**
     * Location called to change the radius in the database
     * @param radius
     */
    protected void changedRadius(float radius) {
        mRadius = radius;
        db.setRadius(mRowId, radius);
        updateForm();
    }
    
    /**
     * Create a new row in the database with default values
     * @return The id of the new row
     */
    private long createDefaultRow() {
        // TODO: move all strings / constants out to R
        return db.addRow (
            "", 1000.0, 1000.0,
            0,
            "network", (float) 1.80, "km"
        );
    }
    
    /**
     * Load the latitude and longitude from the database
     */
    protected void loadLatLong() {
        mLatitude = db.getLatitude(mRowId);
        mLongitude = db.getLongitude(mRowId);
        // If the lat / long are default (invalid) values, prompt
        // the user to select a location
        if (mLatitude == 1000 && mLongitude == 1000) {
            getLoc();
        }
        updateForm();
    }
    
    /**
     * Load the location provider from the database
     */
    protected void loadLocProv() {
        mLocProv = db.getProvider(mRowId);
        updateForm();
    }
    
    /**
     * Update the gui to the latest values
     */
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
        Spinner unitSpin = (Spinner)findViewById(R.id.unitList);
        adapter = unitSpin.getAdapter();
        for(int i = 0; i < adapter.getCount(); i++) {
            if(adapter.getItem(i).equals(mUnit)) {
                unitSpin.setSelection(i);
            }
        }
        TextView latText = (TextView)findViewById(R.id.latitude);
        TextView longText = (TextView)findViewById(R.id.longitude);
        latText.setText(String.valueOf(mLatitude));
        longText.setText(String.valueOf(mLongitude));
    }
    
    /**
     * Load the nickname of the location from the database
     */
    protected void loadNick() {
        Log.v(LOG_NAME, "loadNick()");
        mNick = db.getNick(mRowId);
        // If the nickname is blank (the default value),
        // prompt the user for one
        if (mNick.equals("")) {
            Log.v(LOG_NAME, "Nick is empty");
            Dialog monkey = onCreateDialog(NICKDIALOG);
            monkey.show();
        }
        updateForm();
    }
    
    /**
     * Load the radius from the database
     */
    protected void loadRadius() {
        Log.d(LOG_NAME, "loadRadius()");
        mRadius = db.getRadius(mRowId);
        updateForm();
    }   
    
    /**
     * Load the distance unit to use from the database
     */
    protected void loadUnit() {
        Log.d(LOG_NAME, "loadUnit()");
        mUnit = db.getUnit(mRowId);
        updateForm();
    }   
    
    @Override
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

    @Override
    protected void onCreate(Bundle icicle) {
        LOG_NAME = (String) getText(R.string.app_name_nospaces);
        BROADCAST_UPDATE = (String) getText(R.string.serviceBroadcastName);
        
        Log.d(LOG_NAME, "EditLocation.onCreate");
        super.onCreate(icicle);

        setVolumeControlStream(AudioManager.STREAM_ALARM);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        db = new DatabaseManager(this);
        Button button;
        
        Bundle extras = this.getIntent().getExtras();
        mRowId = extras.getLong("rowId");
        
        Log.d(LOG_NAME, "Row detected: " + mRowId);
        if (mRowId == -1) {
            mRowId = createDefaultRow();
            Log.d(LOG_NAME, "Row created");
            SharedPreferences.Editor editor = settings.edit();
            editor.putLong("currRowId", mRowId);
            editor.commit();
        }

        setContentView(R.layout.edit_location);

        button = (Button)findViewById(R.id.nickButton);
        button.setOnClickListener(mChangeNickListener);
        
        button = (Button)findViewById(R.id.getLocationMapButton);
        button.setOnClickListener(mGetLocMapListener);
        
        button = (Button)findViewById(R.id.startService);
        button.setOnClickListener(mStartListener);

        button = (Button)findViewById(R.id.stopService);
        button.setOnClickListener(mStopListener);
        button.setEnabled(false);
        
        ToggleButton tb = (ToggleButton)findViewById(R.id.editLocationRunningToggle);
        tb.setOnClickListener(mRunningListener);
        
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
       
        uc = new UnitConverter(this, "m");
        ArrayList<String> units = uc.getAbbrevList();
        if (units.isEmpty()) {
            Log.wtf(LOG_NAME, "How can there be no units!?");
        }
        Log.d(LOG_NAME, units.toString());

        spinnerArrayAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, units);

        spinnerArrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        s = (Spinner) findViewById(R.id.unitList);
        s.setAdapter(spinnerArrayAdapter);
        s.setOnItemSelectedListener(unitListener);

        loadNick();
        loadLatLong();
        loadRadius();
        loadLocProv();
        loadUnit();
    }

    @Override
    protected Dialog onCreateDialog(int type) {
        if (mDialogOpen == false) {
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
                            mDialogOpen = false;
                            loadLatLong();
                        }
                    };
                break;
                case RADIUSDIALOG:
                    title = "Radius";
                    // REF#0006
                    inputBox.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
                    positiveListener = new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            changedRadius(Float.valueOf(inputBox.getText().toString()));
                            db.logOutArray();
                            mDialogOpen = false;
                        }
                    };
                break;
                default:
                    Log.wtf(LOG_NAME, "Invalid dialog type " + type);
            }
            mDialogOpen = true;
            return new AlertDialog.Builder(EditLocation.this)
                .setTitle(title)
                .setView(textEntryView) 
                .setPositiveButton(R.string.alert_dialog_ok, positiveListener)
                .setNegativeButton(R.string.alert_dialog_cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        Log.d(LOG_NAME, "clicked negative");
                        mDialogOpen = false;
                    }
                })
                .create();
        } else {
            Log.w(LOG_NAME, "Dialog already open, skipping");
            return null;
        }
    }
    

    
    @Override
    protected void onDestroy() {
        db.close();
        super.onDestroy();
    }
}
