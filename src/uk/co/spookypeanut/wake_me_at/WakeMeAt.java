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

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class WakeMeAt extends Activity {
    public static final int GETLOCATION = 1;
    public static final String PREFS_NAME = "WakeMeAtPrefs";


    private OnClickListener mCorkyListener = new Button.OnClickListener() {
        public void onClick(View v) {
            Intent i = new Intent(WakeMeAt.this.getApplication(), GetLocation.class);
            EditText searchAddrBox = (EditText)findViewById(R.id.searchAddrBox);
            String searchAddr = searchAddrBox.getText().toString();
            
            i.putExtra("searchAddr", searchAddr); 
            startActivityForResult(i, GETLOCATION);
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        // Capture our button from layout
        Button button = (Button)findViewById(R.id.getLocationButton);
        // Register the onClick listener with the implementation above
        button.setOnClickListener(mCorkyListener);
        
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
        float latitude = settings.getFloat("latitude", (float) 0.0);
        float longitude = settings.getFloat("longitude", (float) 0.0);

        String latLongString = longitude + "," + latitude;
        Toast.makeText(getApplicationContext(), latLongString,
                Toast.LENGTH_SHORT).show();
    }

    protected void onActivityResult (int requestCode,
            int resultCode, Intent data) {
        if (requestCode == GETLOCATION) {
            String latLongString = data.getAction();

            String tempStrings[] = latLongString.split(",");
            String latString = tempStrings[0];
            String longString = tempStrings[1];
            float latFloat = Float.valueOf(latString.trim()).floatValue();
            float longFloat = Float.valueOf(longString.trim()).floatValue();
            SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
            SharedPreferences.Editor editor = settings.edit();
            editor.putFloat("latitude", latFloat);
            editor.putFloat("longitude", longFloat);
            editor.commit();
        }
    }
}
