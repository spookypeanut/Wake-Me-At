package uk.co.spookypeanut.wake_me_at;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.provider.Settings;
import android.util.Log;

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

This particular file was originally taken from The Serval Project,
with much gratitude. REF#0025
*/

/**
 * a runnable class that sends mock locations for testing purposes
 */
public class MockLocations implements Runnable {
	
	/*
	 * private class level constants
	 */
    private String LOG_NAME;
	private final boolean V_LOG = false;
	
	private final String LOCATION_FILE = "mock-locations.txt";
	private final String LOCATION_ZIP_FILE = "mock-locations.zip";
	
	/*
	 * private class level variables
	 */
	private String locations = null;
	
	private LocationManager locationManager;
	
	private volatile boolean keepGoing = true;
	
	/**
	 * create the MockLocations class and open the zip file for the required 
	 * location data
	 * 
	 * @param context the application context
	 * 
	 * @throws IllegalArgumentException if the context field is null
	 * @throws IOException  if opening the zip file fails
	 */
	public MockLocations(Context context) throws IOException {
//        LOG_NAME = (String) getText(R.string.app_name_nospaces);
        LOG_NAME = "WakeMe@";

		Log.v(LOG_NAME, "opening the zip file");

		// open the zip file and get the required file inside
		ZipInputStream mZipInput = new ZipInputStream(context.getAssets().open(LOCATION_ZIP_FILE));
		ZipEntry mZipEntry;
		
		// look for the required file
		while((mZipEntry = mZipInput.getNextEntry())!= null) {
			
			Log.v(LOG_NAME, "ZipEntry: " + mZipEntry.getName());
			
			// read the bytes from the file and convert them to a string
			if(mZipEntry.getName().equals(LOCATION_FILE)) {
				
				Log.v(LOG_NAME, "required file found inside zip file");
				
				ByteArrayOutputStream mByteStream = new ByteArrayOutputStream();
				byte[] mBuffer = new byte[1024];
				int mCount;
				
				while((mCount = mZipInput.read(mBuffer)) != -1) {
					mByteStream.write(mBuffer, 0, mCount);
				}
				
				locations = new String(mByteStream.toByteArray(), "UTF-8");
			}
			
			Log.v(LOG_NAME, "location file successfully read");
			
			mZipInput.closeEntry();
		}
		
		mZipInput.close();
		
		// check to make sure everything was read successfully
		if(locations == null) {
			throw new IOException("unable to read the required file from the zip file");
		}
		
		// get an instance of the LocationManager class
		locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		
		// add a reference to our test provider
		// use the standard provider name so the rest of the code still works	
		locationManager.addTestProvider(LocationManager.GPS_PROVIDER, false, false, false, false, false, true, true, 0, 5);
	}
	
	/**
	 * check to confirm that the "Allow mock locations setting is set"
	 * @param context a context object used to gain access to application resources
	 * @return true if "Allow mock locations" is set, otherwise false
	 */
	public static boolean isMockLocationSet(Context context) { 
		if (Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ALLOW_MOCK_LOCATION).contentEquals("1")) { 
			return true;  
		} 
		else { 
			return false;
		} 
	} 
	
	/**
	 * request that this thread stops
	 */
	public void requestStop() {
		
		Log.v(LOG_NAME, "Mock loctions thread requested to stop");
		
		keepGoing = false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		Log.v(LOG_NAME, "Mock locations thread started");
		
		String[] mParts;
		
		int mLineCount = -1; 
		
		int mSleepTime;
		double mLatitude;
		double mLongitude;
		
		Location mLocation;
		
		// loop through each of the locations
		for(String mToken : locations.split("\\n")) {
			
			if(keepGoing == false)  {
				
				Log.v(LOG_NAME, "Mock locations thread stopped");
				
				return;
			}
			
			mLineCount++;
			
			// only process lines that aren't comments
			if(mToken.startsWith("#") == true) {
				continue;
			}
			
			mParts = mToken.split("\\|");
			
			/*
			 *  validate the line
			 */
			if(mParts.length != 3) {
				Log.e(LOG_NAME, "expected 3 data elements found '" + mParts.length + "' on line: " + mLineCount);
			}
			
			try {
				mSleepTime = Integer.parseInt(mParts[0]);
			} catch (NumberFormatException e) {
				Log.e(LOG_NAME, "unable to parse the sleep time element on line: " + mLineCount);
				continue;
			}
			
			try {
				mLatitude = Double.parseDouble(mParts[1]);
			} catch (NumberFormatException e) {
				Log.e(LOG_NAME, "unable to parse the latitude element on line: " + mLineCount);
				continue;
			}

			try {
				mLongitude = Double.parseDouble(mParts[2]);
			} catch (NumberFormatException e) {
				Log.e(LOG_NAME, "unable to parse the longitude element on line: " + mLineCount);
				continue;
			}
			
			// create the new location
			mLocation = new Location(LocationManager.GPS_PROVIDER);
			mLocation.setLatitude(mLatitude);
			mLocation.setLongitude(mLongitude);
			mLocation.setTime(System.currentTimeMillis());

			// send the new location
			locationManager.setTestProviderEnabled(LocationManager.GPS_PROVIDER, true);
			locationManager.setTestProviderLocation(LocationManager.GPS_PROVIDER, mLocation);
			
			Log.v(LOG_NAME, "new location sent");

			// sleep the thread
			try {
				Thread.sleep(mSleepTime * 1000);
			} catch (InterruptedException e) {
				if(keepGoing == false) {
					Log.v(LOG_NAME, "thread was interrupted and is stopping");
					return;
				} else {
					Log.w(LOG_NAME, "thread was interrupted without being requested to stop", e);
				}
			}
		}
	}
}
