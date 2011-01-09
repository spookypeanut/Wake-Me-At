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

public class Alarm extends Activity {
    public static final String PREFS_NAME = "WakeMeAtPrefs";
    public final String LOG_NAME = WakeMeAt.LOG_NAME;
    
    private DatabaseManager db;
    private long mRowId;
    
    private String mNick;
    private double mLatitude;
    private double mLongitude;
    private float mRadius;
    private String mLocProv;
    private int mUnit;

    @Override
    protected void onCreate(Bundle icicle) {
        Log.d(LOG_NAME, "Alarm.onCreate");
        super.onCreate(icicle);
        
        setContentView(R.layout.alarm);
        
        db = new DatabaseManager(this);
        
    }
}
