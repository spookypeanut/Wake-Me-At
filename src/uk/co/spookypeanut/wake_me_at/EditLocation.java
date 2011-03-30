package uk.co.spookypeanut.wake_me_at;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ExpandableListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;

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


/**
 * Activity for editing a location saved in the database
 * @author spookypeanut
 *
 */
public class EditLocation extends ExpandableListActivity {
    public static final int GETLOCMAP = 1;
    public static final String PREFS_NAME = "WakeMeAtPrefs";

    private String LOG_NAME;
    private String BROADCAST_UPDATE;
    
    public static final int NICKDIALOG = 0;
    public static final int RADIUSDIALOG = 1;
    private boolean mDialogOpen = false;
    
    private DatabaseManager db;
    private UnitConverter uc;
    private LayoutInflater mInflater;
    private Context mContext;
    private LocSettingsAdapter mLocSettingsAdapter;

    private long mRowId;
    private long mRunningRowId = -1;
    private String mNick = "New Location";
    private double mLatitude = 0.0;
    private double mLongitude = 0.0;
    private float mRadius = 0;
    private int mLocProv = -1;
    private int mPreset = -1;
    private Presets mPresetObj;
    private String mUnit = "";

    private static final int INDEX_ACTIV = 0;
    private static final int INDEX_LOC = 1;
    private static final int INDEX_PRESET = 2;
    private static final int INDEX_RADIUS = 3;
    private static final int INDEX_UNITS = 4;
    private static final int INDEX_LOCPROV = 5;
    private static final int NUM_SETTINGS = 6;

    private String[] mTitles = {
        "Activate alarm",
        "Location",
        "Preset",
        "Radius",
        "Units",
        "Location provider",
        "Sound",
        "Ringtone",
        "Crescendo",
        "Vibration",
        "Speech"
    };
    private String[] mDescription = {
        "Tap here to activate the alarm",
        "Tap to view / edit location",
        "Type of transport used",
        "Distance away to trigger alarm",
        "The units to measure distance in",
        "The method to use to determine location",
        "Toggle the audible alarm",
        "Set the ringtone for the alarm",
        "Start the alarm quiet, and get louder",
        "Toggle the vibration alarm",
        "Toggle synthesized speech"
    };

    @Override
    public boolean onChildClick(ExpandableListView l, View v, int groupPosition, int childPosition, long id) {
        Log.d(LOG_NAME, "onChildClick(" + l + ", " + v + ", " + groupPosition + ", " + childPosition + ", " + id + ")");
        int position = mLocSettingsAdapter.getGlobalPosition(groupPosition, childPosition);
        Log.d(LOG_NAME, "onListItemClick(" + l + ", " + v + ", " + position + ", " + id + ")");
        switch (position) {
            case INDEX_ACTIV:
                if (isActive()) {
                    stopService();
                } else {
                    startService();
                }
                break;
            case INDEX_PRESET:
                Log.d(LOG_NAME, "Preset pressed");
                Presets forList = new Presets(mContext, 0);
                final String[] presetList = forList.getAllNames();

                AlertDialog.Builder presetBuilder = new AlertDialog.Builder(this);
                presetBuilder.setItems(presetList, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        changedPreset(item);
                    }
                });
                AlertDialog presetAlert = presetBuilder.create();
                presetAlert.show();
                break;
            case INDEX_LOC:
                getLoc();
                break;
            case INDEX_RADIUS:
                if (mPresetObj.isCustom()) {
                    Dialog monkey = onCreateDialog(RADIUSDIALOG);
                    monkey.show();
                }
                break;
            case INDEX_UNITS:
                if (mPresetObj.isCustom()) {
                    ArrayList<String> unitList = uc.getNameList();
                    final String[] items = unitList.toArray(new String[unitList.size()]);

                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setItems(items, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            changedUnit(items[item]);
                        }
                    });
                    AlertDialog alert = builder.create();
                    alert.show();
                }
                break;
            case INDEX_LOCPROV:
                if (mPresetObj.isCustom()) {
                    List<String> allProviders = Arrays.asList(this.getResources().getStringArray(R.array.locProvHuman));
                    final String[] locProvs = allProviders.toArray(new String[allProviders.size()]);

                    AlertDialog.Builder lpbuilder = new AlertDialog.Builder(this);
                    lpbuilder.setItems(locProvs, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int item) {
                            changedLocProv(item);
                        }
                    });
                    AlertDialog lpalert = lpbuilder.create();
                    lpalert.show();
                }
                break;
        }
        return true;
    }

    public BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(LOG_NAME, "Received broadcast");
            Bundle extras = intent.getExtras();
            mRunningRowId = extras.getLong("rowId");
            updateForm();
        }
   };
   
    private boolean isActive() {
        return (mRunningRowId == mRowId);
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
    
    private OnClickListener mChangeNickListener = new OnClickListener() {
        public void onClick(View v) {
            Dialog monkey = onCreateDialog(NICKDIALOG);
            monkey.show();
        }
    };

    /**
     * Starts the alarm service for the current activity's database entry
     */
    private void startService () {
        Log.d(LOG_NAME, "EditLocation.startService");
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
     * Method called to change the preset in the database
     * @param preset
     */
    protected void changedPreset(int preset) {
        Log.d(LOG_NAME, "changedPreset");
        mPreset = preset;
        newPresetGui();
        db.setPreset(mRowId, preset);
        updateForm();
    }

    /**
     * Method called to change the location provider in the database
     * @param locProv
     */
    protected void changedLocProv(int locProv) {
        Log.d(LOG_NAME, "changedLocProv");
        mLocProv = locProv;
        db.setProvider(mRowId, locProv);
        updateForm();
    }

    /**
     * Method called to change the unit of distance used in the database
     * @param unit as a name
     */
    protected void changedUnit(String unitName) {
        Log.d(LOG_NAME, "changedUnit");
        String unit = uc.getAbbrevFromName(unitName);

        mUnit = unit;
        db.setUnit(mRowId, unit);
        updateForm();
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
            "",              // Nickname
            1000.0, 1000.0,  // Lat long
            0,               // Preset
            1,               // Location provider
            (float) 1.80,    // Radius
            "km",            // Unit
            true,            // Sound
            "none",          // Ringtone
            false,           // Crescendo 
            true,            // Vibration
            true             // Speech
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
    }
    
    /**
     * Load the preset from the database
     */
    protected void loadPreset() {
        mPreset = db.getPreset(mRowId);
        newPresetGui();
    }
    
    private void newPresetGui() {
        ExpandableListView v = getExpandableListView();
        mPresetObj.switchPreset(mPreset);
        if (mPresetObj.isCustom()) {
            v.expandGroup(1);
        } else {
            v.collapseGroup(1);
        }
    }
        
    /**
     * Load the location provider from the database
     */
    protected void loadLocProv() {
        mLocProv = db.getProvider(mRowId);
    }
    
    /**
     * Update the gui to the latest values
     */
    protected void updateForm() {
        TextView nickTextView = (TextView) findViewById(R.id.nick);
        nickTextView.setText(mNick);

        Log.d(LOG_NAME, "Data set changed!");
        mLocSettingsAdapter.notifyDataSetChanged();
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
    }
    
    /**
     * Load the radius from the database
     */
    protected void loadRadius() {
        Log.d(LOG_NAME, "loadRadius()");
        mRadius = db.getRadius(mRowId);
    }   
    
    /**
     * Load the distance unit to use from the database
     */
    protected void loadUnit() {
        Log.d(LOG_NAME, "loadUnit()");
        mUnit = db.getUnit(mRowId);
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
        mContext = this;
        mInflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        mPresetObj = new Presets(mContext, 0);

        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        db = new DatabaseManager(this);
        
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

        setListAdapter(new LocSettingsAdapter(this));
        mLocSettingsAdapter = (LocSettingsAdapter) getExpandableListAdapter();

        TextView tv = (TextView)findViewById(R.id.nick);
        tv.setOnClickListener(mChangeNickListener);

        uc = new UnitConverter(this, "m");
        ArrayList<String> units = uc.getAbbrevList();
        if (units.isEmpty()) {
            Log.wtf(LOG_NAME, "How can there be no units!?");
        }

        loadNick();
        loadLatLong();
        loadPreset();
        loadRadius();
        loadLocProv();
        loadUnit();
        updateForm();

        resetGroups();

        Log.d(LOG_NAME, "End of EditLocation.onCreate");
    }

    private void resetGroups() {
        ExpandableListView v = getExpandableListView();
        v.expandGroup(0);
        for (int i = 1; i < mLocSettingsAdapter.groupName.length; i++) {
            v.collapseGroup(i);
        }
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

    /**
     * Class for the location settings list in the edit location activity
     * @author spookypeanut
     */
    private class LocSettingsAdapter extends BaseExpandableListAdapter {
        // The number of items in each section of the list
        int groupSize[] = {3, 3, 5};
        // The names of the sections of the list
        String groupName[] = {"Basic", "Advanced", "Alarm"};
        
        public LocSettingsAdapter(Context context) {
            Log.d(LOG_NAME, "LocSettingsAdapter constructor");
        }
        

        public int getGlobalPosition(int groupPosition, int childPosition) {
            int countGroup;
            int total = 0;
            for (countGroup = groupPosition; countGroup > 0; countGroup--) {
                total += groupSize[countGroup - 1];
            }
            total += childPosition;
            return total;
        }
        
        public String getSubtitle(int position) {
            switch (position) {
                case INDEX_ACTIV:
                    if (isActive()) {
                        return "Running";
                    }
                    return "Not running";
                case INDEX_LOC:
                    return mDescription[position];
                case INDEX_PRESET:
                    return mPresetObj.getName();
                case INDEX_RADIUS:
                    float rad;
                    String radUnit;
                    if (mPresetObj.isCustom()) {
                        rad = mRadius;
                        radUnit = mUnit;
                    } else {
                        rad = mPresetObj.getRadius();
                        radUnit = mPresetObj.getUnit();
                    }
                    return String.valueOf(rad) + radUnit;
                case INDEX_UNITS:
                    String unit;
                    if (mPresetObj.isCustom()) {
                        unit = mUnit;
                    } else {
                        unit = mPresetObj.getUnit();
                    }
                    UnitConverter temp = new UnitConverter(mContext, unit);
                    return temp.getName();
                case INDEX_LOCPROV:
                    int locProv;
                    if (mPresetObj.isCustom()) {
                        locProv = mLocProv;
                    } else {
                        locProv = mPresetObj.getLocProv();
                    }
                    return mContext.getResources().getStringArray(R.array.locProvHuman)[locProv];
            }
            Log.wtf(LOG_NAME, "EditLocation.getSubtitle: Invalid position passed");
            return "crap";
        }
        

        /* (non-Javadoc)
         * @see android.widget.ExpandableListAdapter#getGroupCount()
         */
        @Override
        public int getGroupCount() {
            return groupSize.length;
        }


        /* (non-Javadoc)
         * @see android.widget.ExpandableListAdapter#getChildrenCount(int)
         */
        @Override
        public int getChildrenCount(int groupPosition) {
            return groupSize[groupPosition];
        }


        /* (non-Javadoc)
         * @see android.widget.ExpandableListAdapter#getGroup(int)
         */
        @Override
        public Object getGroup(int groupPosition) {
            // TODO Auto-generated method stub
            return null;
        }


        /* (non-Javadoc)
         * @see android.widget.ExpandableListAdapter#getChild(int, int)
         */
        @Override
        public Object getChild(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return null;
        }


        /* (non-Javadoc)
         * @see android.widget.ExpandableListAdapter#getGroupId(int)
         */
        @Override
        public long getGroupId(int groupPosition) {
            // TODO Auto-generated method stub
            return 0;
        }


        /* (non-Javadoc)
         * @see android.widget.ExpandableListAdapter#getChildId(int, int)
         */
        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return getGlobalPosition(groupPosition, childPosition);
        }


        /* (non-Javadoc)
         * @see android.widget.ExpandableListAdapter#hasStableIds()
         */
        @Override
        public boolean hasStableIds() {
            // TODO Auto-generated method stub
            return false;
        }


        /* (non-Javadoc)
         * @see android.widget.ExpandableListAdapter#getGroupView(int, boolean, android.view.View, android.view.ViewGroup)
         */
        @Override
        public View getGroupView(int groupPosition, boolean isExpanded,
                View convertView, ViewGroup parent) {
            Log.d(LOG_NAME, "getGroupView(" + groupPosition + ", " + isExpanded + ", " + convertView + ", " + parent + ")");
            View row;
            
            if (null == convertView) {
                row = mInflater.inflate(R.layout.edit_loc_list_group, null);
            } else {
                row = convertView;
            }

            boolean enabled = true;

            TextView tv = (TextView) row.findViewById(R.id.locSettingName);
            tv.setText(groupName[groupPosition]);
            tv.setEnabled(enabled);
            return row;
        }


        /* (non-Javadoc)
         * @see android.widget.ExpandableListAdapter#getChildView(int, int, boolean, android.view.View, android.view.ViewGroup)
         */
        @Override
        public View getChildView(int groupPosition, int childPosition,
                boolean isLastChild, View convertView, ViewGroup parent) {
            Log.d(LOG_NAME, "getChildView(" + groupPosition + ", " + childPosition + "," + isLastChild + ", " + convertView + ", " + parent + ")");
            // TODO Auto-generated method stub
            int position = getGlobalPosition(groupPosition, childPosition);
            Log.d(LOG_NAME, "getView(" + position + "), mRowId: " + mRowId);
            View row;
            
            if (null == convertView) {
                row = mInflater.inflate(R.layout.edit_loc_list_entry, null);
            } else {
                row = convertView;
            }

            boolean enabled = true;

            if (mPresetObj.isCustom() == false) {
                // If we're set to anything other than custom, disable the
                // relevant lines
                Log.d(LOG_NAME, "Preset is not custom");
                switch (position) {
                    case INDEX_LOCPROV:
                    case INDEX_UNITS:
                    case INDEX_RADIUS:
                        Log.d(LOG_NAME, "Disable this one (" + position + ")");
                        enabled = false;
                        break;
                }
            }
            TextView tv = (TextView) row.findViewById(R.id.locSettingName);
            tv.setText(mTitles[position]);
            tv.setEnabled(enabled);
            
            tv = (TextView) row.findViewById(R.id.locSettingDesc);
            tv.setText(getSubtitle(position));
            tv.setEnabled(enabled);
            return row;
        }


        /* (non-Javadoc)
         * @see android.widget.ExpandableListAdapter#isChildSelectable(int, int)
         */
        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            // TODO Auto-generated method stub
            return true;
        }
    }
}
