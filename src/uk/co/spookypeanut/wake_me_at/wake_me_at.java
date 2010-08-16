package uk.co.spookypeanut.wake_me_at;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

//Create an anonymous implementation of OnClickListener


public class wake_me_at extends Activity {
	private OnClickListener mCorkyListener = new OnClickListener() {
	    public void onClick(View v) {
	    	Toast toast = Toast.makeText(getApplicationContext(), "Clicked", Toast.LENGTH_SHORT);
	    	toast.show();
	    	get_location getlocation = new get_location();
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
