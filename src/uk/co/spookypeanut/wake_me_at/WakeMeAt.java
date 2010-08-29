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
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;


//Create an anonymous implementation of OnClickListener


public class WakeMeAt extends Activity {
	private OnClickListener mCorkyListener = new Button.OnClickListener() {
	    public void onClick(View v) {
	    	Toast toast = Toast.makeText(getApplicationContext(), "Clicked", Toast.LENGTH_SHORT);
	    	toast.show();
	    	Intent i = new Intent(WakeMeAt.this.getApplication(), GetLocation.class);
            startActivity(i);
            
	    }
	};
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
	    // Capture our button from layout
	    Button button = (Button)findViewById(R.id.getLocationButton);
	    // Register the onClick listener with the implementation above
	    button.setOnClickListener(mCorkyListener);

	}
}
