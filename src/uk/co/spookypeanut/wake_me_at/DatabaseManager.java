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

// REF#0004

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

/**
 * Handle the database used in WakeMe@
 * @author spookypeanut
 */
public class DatabaseManager
{
    private SQLiteDatabase db;
    private String LOG_NAME;

    private final String DB_NAME = "WakeMeAtDB";
    private final int DB_VERSION = 2;

    private final String TABLE_NAME = "Locations";
    private final String TABLE_ROW_ID = "id";
    private final String TABLE_ROW_NICK = "table_row_nick";
    private final String TABLE_ROW_LAT = "table_row_lat";
    private final String TABLE_ROW_LONG = "table_row_long";
    private final String TABLE_ROW_PRESET = "table_row_preset";

    //NB: These columns are only used when the preset is set to "custom"
    private final String TABLE_ROW_PROV = "table_row_prov";
    private final String TABLE_ROW_RADIUS = "table_row_radius";
    private final String TABLE_ROW_UNIT = "table_row_unit";

    private final String TABLE_ROW_SOUND = "table_row_sound";
    private final String TABLE_ROW_RINGTONE = "table_row_ringtone";
    private final String TABLE_ROW_CRESC = "table_row_cresc";
    private final String TABLE_ROW_VIBRATE = "table_row_vibrate";
    private final String TABLE_ROW_SPEECH = "table_row_speech";
    private final String TABLE_ROW_TOAST = "table_row_toast";

    private final String TABLE_ROW_WARNING = "table_row_warning";
    private final String TABLE_ROW_WARN_SOUND = "table_row_warn_sound";
    private final String TABLE_ROW_WARN_VIBRATE = "table_row_warn_vibrate";
    private final String TABLE_ROW_WARN_TOAST = "table_row_warn_toast";
    Context mContext;


    /** Constructor
     * @param context The context that the db is being handled in
     */
    public DatabaseManager(Context context) {
        LOG_NAME = (String) context.getText(R.string.app_name_nospaces);
        this.mContext = context;
        openDatabase();
    }
    
    public void openDatabase() {
        WakeMeAtDbHelper helper = new WakeMeAtDbHelper(mContext);
        this.db = helper.getWritableDatabase();
    }

    
    public String getLiveDBPath() {
        String packName = (String) mContext.getText(R.string.package_name);
        return ("//data//"+ packName + "//databases//" + DB_NAME);
    }
    
    public String getBackupDir() {
        String packName = (String) mContext.getText(R.string.package_name);
        return ("data/" + packName);
    }
    
    public String getBackupDBName() {
        return DB_NAME;
    }
    
    public File getBackupDBFile() {
        File sd = Environment.getExternalStorageDirectory();
        return new File(sd, getBackupDBPath());
    }
    
    public String getBackupDBPath() {
        return getBackupDir() + "/" + getBackupDBName();
    }
    
    public File getLiveDBFile() {
        File data = Environment.getDataDirectory();
        return new File(data, getLiveDBPath());
    }
    /**
     * Export the entire database to external storage
     */
    public void exportDatabaseToSD() {
        try {
            File sd = Environment.getExternalStorageDirectory();
            Log.d(LOG_NAME, "Trying to export data to SD card");

            if (sd.canWrite()) {
                File currentDB = getLiveDBFile();
                File backupDir = new File(sd, getBackupDir());
                // First we have to make the directory, if it doesn't exist
                backupDir.mkdirs();
                File backupDB = getBackupDBFile();
                FileChannel src = new FileInputStream(currentDB).getChannel();
                FileChannel dst = new FileOutputStream(backupDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                src.close();
                dst.close();
                String toastMessage = String.format(mContext.getString(R.string.export_db_success,
                        backupDB.toString()));
                Toast.makeText(mContext, toastMessage, Toast.LENGTH_LONG).show();
                Log.d(LOG_NAME, "Exported to SD card");
            } else {
                Log.e(LOG_NAME, "Can't write to SD card");
            }
        } catch (Exception e) {
            Log.d(LOG_NAME, "Failed to export to SD card");
            Log.e(LOG_NAME, e.toString());
            String toastMessage = String.format(mContext.getString(R.string.export_db_failure,
                    e.toString()));
            Toast.makeText(mContext, toastMessage, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Import the entire database from external storage
     */
    public void importDatabaseFromSD() {
        // Close the SQLiteOpenHelper so it will commit the created empty
        // database to internal storage.
        try {
            Log.d(LOG_NAME, "Importing database");
            File backupDB = getBackupDBFile();
            File liveDB = getLiveDBFile();
            if (backupDB.exists()) {
                boolean wasOpen = false;
                if (isOpen()) {
                    wasOpen = true;
                    close();
                }
                FileChannel src = new FileInputStream(backupDB).getChannel();
                FileChannel dst = new FileOutputStream(liveDB).getChannel();
                dst.transferFrom(src, 0, src.size());
                Log.d(LOG_NAME, "Database imported");
                if (wasOpen) {
                    Log.d(LOG_NAME, "Database being re-opened");
                    openDatabase();
                }
            } else {
                Log.e(LOG_NAME, "Backup database doesn't exist");
                Log.d(LOG_NAME, getBackupDBPath());
            }
        } catch (Exception e) {
            Log.d(LOG_NAME, "Failed to import from SD card");
            Log.e(LOG_NAME, e.toString());
            Toast.makeText(mContext, e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Get the number of rows in the db
     * @return Rows in db
     */
    public int getRowCount() {
        ArrayList <ArrayList <Object>> allData = getAllRowsAsArrays();
        return allData.size();
    }

    /**
     * Add a new row to the db
     * @param rowNick The nickname of the location
     * @param rowLat The latitude of the location
     * @param rowLong The longitude of the location
     * @param rowProv The location provider (gps, network, etc) of the location
     * @param rowRadius The distance away from the location to alert the user
     * @param rowUnit The units of rowRadius
     * @return The rowId of the newly created row
     */
    public long addRow(String rowNick, double rowLat, double rowLong, int preset,
                       int rowProv, float rowRadius, String rowUnit,
                       boolean rowSound, String rowRingtone, boolean rowCresc,
                       boolean rowVibrate, boolean rowSpeech, boolean rowToast,
                       boolean rowWarning, boolean rowWSound, boolean rowWVibrate,
                       boolean rowWToast) {
        // this is a key value pair holder used by android's SQLite functions
        ContentValues values = new ContentValues();
        values.put(TABLE_ROW_NICK, rowNick);
        values.put(TABLE_ROW_LAT, rowLat);
        values.put(TABLE_ROW_LONG, rowLong);
        values.put(TABLE_ROW_PRESET, preset);
        values.put(TABLE_ROW_PROV, rowProv);
        values.put(TABLE_ROW_RADIUS, rowRadius);
        values.put(TABLE_ROW_UNIT, rowUnit);
        values.put(TABLE_ROW_SOUND, rowSound);
        values.put(TABLE_ROW_RINGTONE, rowRingtone);
        values.put(TABLE_ROW_CRESC, rowCresc);
        values.put(TABLE_ROW_VIBRATE, rowVibrate);
        values.put(TABLE_ROW_SPEECH, rowSpeech);
        values.put(TABLE_ROW_TOAST, rowToast);

        values.put(TABLE_ROW_WARNING, rowWarning);
        values.put(TABLE_ROW_WARN_SOUND, rowWSound);
        values.put(TABLE_ROW_WARN_VIBRATE, rowWVibrate);
        values.put(TABLE_ROW_WARN_TOAST, rowWToast);
        long rowId;
        try {
            rowId = db.insert(TABLE_NAME, null, values);
            return rowId;
        }
        catch(Exception e) {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * Delete a row from the db
     * @param rowID The row to delete
     */
    public void deleteRow(long rowID) {
        try {
            db.delete(TABLE_NAME, TABLE_ROW_ID + "=" + rowID, null);
        }
        catch (Exception e) {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }
    }


    /**
     * Change the entry for a given row in the database
     * @param rowId The id in the database of the row to change
     * @param rowNick The new nickname for the location
     * @param rowLat The new latitude for the location
     * @param rowLong The new longitude for the location
     * @param rowProv The new location provider for the location
     * @param rowRadius The new distance away to alert the user
     * @param rowUnit The units that rowRadius is in
     */
    public void updateRow(long rowId, String rowNick, double rowLat, double rowLong, int preset,
                          int rowProv, float rowRadius, String rowUnit,
                          boolean rowSound, String rowRingtone, boolean rowCresc,
                          boolean rowVibrate, boolean rowSpeech, boolean rowToast,
                          boolean rowWarning, boolean rowWSound, boolean rowWVibrate,
                          boolean rowWToast) {
        // this is a key value pair holder used by android's SQLite functions
        ContentValues values = new ContentValues();
        values.put(TABLE_ROW_NICK, rowNick);
        values.put(TABLE_ROW_LAT, rowLat);
        values.put(TABLE_ROW_LONG, rowLong);
        values.put(TABLE_ROW_PRESET, preset);
        values.put(TABLE_ROW_PROV, rowProv);
        values.put(TABLE_ROW_RADIUS, rowRadius);
        values.put(TABLE_ROW_UNIT, rowUnit);
        values.put(TABLE_ROW_SOUND, rowSound);
        values.put(TABLE_ROW_RINGTONE, rowRingtone);
        values.put(TABLE_ROW_CRESC, rowCresc);
        values.put(TABLE_ROW_VIBRATE, rowVibrate);
        values.put(TABLE_ROW_SPEECH, rowSpeech);
        values.put(TABLE_ROW_TOAST, rowToast);

        values.put(TABLE_ROW_WARNING, rowWarning);
        values.put(TABLE_ROW_WARN_SOUND, rowWSound);
        values.put(TABLE_ROW_WARN_VIBRATE, rowWVibrate);
        values.put(TABLE_ROW_WARN_TOAST, rowWToast);
        try {
            db.update(TABLE_NAME, values, TABLE_ROW_ID + "=" + rowId, null);
        }
        catch (Exception e) {
            Log.e("DB Error", e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Get a string item from the database
     * @param rowId The Id of the row to use
     * @param column The column to get the string from
     * @return The string item
     */
    public String getDatumS(long rowId, String column) {
        Cursor cursor;
        String returnValue = "";

        try {
            cursor = db.query (
                    TABLE_NAME,
                    new String[] {column},
                            TABLE_ROW_ID + "=" + rowId,
                            null, null, null, null, null
            );
            cursor.moveToFirst();
            returnValue = cursor.getString(0);
            cursor.close();
        }
        catch (SQLException e)
        {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }
        return returnValue;
    }

    public int getDatumI(long rowId, String column) {
        Cursor cursor;
        int returnValue = 0;

        try {
            cursor = db.query (
                    TABLE_NAME,
                    new String[] {column},
                            TABLE_ROW_ID + "=" + rowId,
                            null, null, null, null, null
            );
            cursor.moveToFirst();
            returnValue = cursor.getInt(0);
            cursor.close();
        }
        catch (SQLException e)
        {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }
        return returnValue;
    }

    /**
     * Set an entry in the database
     * @param rowId The id of the row to set
     * @param column The column to set
     * @param value The value to set the entry to
     */
    public void setDatumS(long rowId, String column, String value) {
        ContentValues values = new ContentValues();
        values.put(column, value);
        try {
            db.update(TABLE_NAME, values, TABLE_ROW_ID + "=" + rowId, null);
        }
        catch (Exception e) {
            Log.e("DB Error", e.toString());
            e.printStackTrace();
        }
    }

    /**
     * Set an entry to a double
     * @param rowId The id of the row to use
     * @param column The column to set
     * @param value The value to set to
     */
    public void setDatumD(long rowId, String column, double value) {
        setDatumS(rowId, column, Double.toString(value));
    }

    /**
     * Set an entry to a float
     * @param rowId The id of the row to use
     * @param column The column to set
     * @param value The value to set to
     */
    public void setDatumF(long rowId, String column, float value) {
        setDatumS(rowId, column, Float.toString(value));
    }

    /**
     * Set an entry to an integer
     * @param rowId The id of the row to use
     * @param column The column to set
     * @param value The value to set to
     */
    public void setDatumI(long rowId, String column, int value) {
        setDatumS(rowId, column, Integer.toString(value));
    }

    public void setDatumB(long rowId, String column, boolean value) {
        if (value) {
            setDatumS(rowId, column, "1");
        } else {
            setDatumS(rowId, column, "0");
        }
    }

    public String getNick(long rowId) {
        return getDatumS(rowId, TABLE_ROW_NICK);
    }

    public void setNick(long rowId, String nick) {
        setDatumS(rowId, TABLE_ROW_NICK, nick);
    }

    public double getLatitude(long rowId) {
        return Double.valueOf(getDatumS(rowId, TABLE_ROW_LAT));
    }

    public void setLatitude(long rowId, double latitude) {
        setDatumD(rowId, TABLE_ROW_LAT, latitude);
    }

    public double getLongitude(long rowId) {
        return Double.valueOf(getDatumS(rowId, TABLE_ROW_LONG));
    }

    public void setLongitude(long rowId, double longitude) {
        setDatumD(rowId, TABLE_ROW_LONG, longitude);
    }

    public int getPreset(long rowId) {
        return getDatumI(rowId, TABLE_ROW_PRESET);
    }

    public void setPreset(long rowId, int preset) {
        setDatumI(rowId, TABLE_ROW_PRESET, preset);
    }

    public int getProvider(long rowId) {
        return getDatumI(rowId, TABLE_ROW_PROV);
    }

    public void setProvider(long rowId, int provider) {
        setDatumI(rowId, TABLE_ROW_PROV, provider);
    }

    public float getRadius(long rowId) {
        return Float.valueOf(getDatumS(rowId, TABLE_ROW_RADIUS));
    }

    public void setRadius(long rowId, float radius) {
        setDatumF(rowId, TABLE_ROW_RADIUS, radius);
    }

    public String getUnit(long rowId) {
        return getDatumS(rowId, TABLE_ROW_UNIT);
    }

    public void setUnit(long rowId, String unit) {
        setDatumS(rowId, TABLE_ROW_UNIT, unit);
    }

    public boolean getSound(long rowId) {
        return (0 != getDatumI(rowId, TABLE_ROW_SOUND));
    }

    public void setSound(long rowId, boolean sound) {
        setDatumB(rowId, TABLE_ROW_SOUND, sound);
    }

    public String getRingtone(long rowId) {
        return getDatumS(rowId, TABLE_ROW_RINGTONE);
    }

    public void setRingtone(long rowId, String ringtone) {
        setDatumS(rowId, TABLE_ROW_RINGTONE, ringtone);
    }

    public boolean getCresc(long rowId) {
        return (0 != getDatumI(rowId, TABLE_ROW_CRESC));
    }

    public void setCresc(long rowId, boolean cresc) {
        setDatumB(rowId, TABLE_ROW_CRESC, cresc);
    }

    public boolean getVibrate(long rowId) {
        return (0 != getDatumI(rowId, TABLE_ROW_VIBRATE));
    }

    public void setVibrate(long rowId, boolean vibrate) {
        setDatumB(rowId, TABLE_ROW_VIBRATE, vibrate);
    }

    public boolean getSpeech(long rowId) {
        return (0 != getDatumI(rowId, TABLE_ROW_SPEECH));
    }

    public void setSpeech(long rowId, boolean speech) {
        setDatumB(rowId, TABLE_ROW_SPEECH, speech);
    }

    public boolean getToast(long rowId) {
        return (0 != getDatumI(rowId, TABLE_ROW_TOAST));
    }

    public void setToast(long rowId, boolean toast) {
        setDatumB(rowId, TABLE_ROW_TOAST, toast);
    }

    public boolean getWarning(long rowId) {
        return (0 != getDatumI(rowId, TABLE_ROW_WARNING));
    }

    public void setWarning(long rowId, boolean warning) {
        setDatumB(rowId, TABLE_ROW_WARNING, warning);
    }

    public boolean getWarnSound(long rowId) {
        return (0 != getDatumI(rowId, TABLE_ROW_WARN_SOUND));
    }

    public void setWarnSound(long rowId, boolean warnSound) {
        setDatumB(rowId, TABLE_ROW_WARN_SOUND, warnSound);
    }

    public boolean getWarnVibrate(long rowId) {
        return (0 != getDatumI(rowId, TABLE_ROW_WARN_VIBRATE));
    }

    public void setWarnVibrate(long rowId, boolean warnVibrate) {
        setDatumB(rowId, TABLE_ROW_WARN_VIBRATE, warnVibrate);
    }

    public boolean getWarnToast(long rowId) {
        return (0 != getDatumI(rowId, TABLE_ROW_WARN_TOAST));
    }

    public void setWarnToast(long rowId, boolean warnToast) {
        setDatumB(rowId, TABLE_ROW_WARN_TOAST, warnToast);
    }

    /**
     * Get all the row ids from the database as a list
     * @return List of row ids
     */
    public ArrayList<Long> getIdsAsList() {
        ArrayList<Long> returnList = new ArrayList<Long>();
        Cursor cursor;

        try {
            cursor = db.query (
                    TABLE_NAME,
                    new String[] {TABLE_ROW_ID},
                    null, null, null,
                    null, null, null
            );
            cursor.moveToFirst();

            // if there is data available after the cursor's pointer, add
            // it to the ArrayList that will be returned by the method.
            if (!cursor.isAfterLast()) {
                do {
                    returnList.add(cursor.getLong(0));
                }
                while (cursor.moveToNext());
            }
            cursor.close();
        }
        catch (SQLException e) {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }
        return returnList;
    }

    /**
     * Get a database row as an array
     * @param rowId The id of the row to get
     * @return The row as an array
     */
    public ArrayList<Object> getRowAsArray(long rowId) {
        // create an array list to store data from the database row.
        // I would recommend creating a JavaBean compliant object
        // to store this data instead.  That way you can ensure
        // data types are correct.
        ArrayList<Object> rowArray = new ArrayList<Object>();
        Cursor cursor;

        try {
            cursor = db.query (
                    TABLE_NAME,
                    new String[] { TABLE_ROW_ID, TABLE_ROW_NICK,
                            TABLE_ROW_LAT, TABLE_ROW_LONG,
                            TABLE_ROW_PRESET,
                            TABLE_ROW_PROV, TABLE_ROW_RADIUS,
                            TABLE_ROW_UNIT,
                            TABLE_ROW_SOUND, TABLE_ROW_RINGTONE,
                            TABLE_ROW_CRESC, TABLE_ROW_VIBRATE,
                            TABLE_ROW_SPEECH, TABLE_ROW_TOAST,
                            TABLE_ROW_WARNING, TABLE_ROW_WARN_SOUND,
                            TABLE_ROW_WARN_VIBRATE, TABLE_ROW_WARN_TOAST
                    },
                    TABLE_ROW_ID + "=" + rowId,
                    null, null, null, null, null
            );
            cursor.moveToFirst();

            // if there is data available after the cursor's pointer, add
            // it to the ArrayList that will be returned by the method.
            if (!cursor.isAfterLast()) {
                do {
                    rowArray.add(cursor.getLong(0));      //TABLE_ROW_ID
                    rowArray.add(cursor.getString(1));    //TABLE_ROW_NICK
                    rowArray.add(cursor.getDouble(2));    //TABLE_ROW_LAT
                    rowArray.add(cursor.getDouble(3));    //TABLE_ROW_LONG
                    rowArray.add(cursor.getInt(4));       //TABLE_ROW_PRESET
                    rowArray.add(cursor.getInt(5));       //TABLE_ROW_PROV
                    rowArray.add(cursor.getFloat(6));     //TABLE_ROW_RADIUS
                    rowArray.add(cursor.getString(7));    //TABLE_ROW_UNIT
                    rowArray.add(cursor.getInt(8) != 0);  //TABLE_ROW_SOUND
                    rowArray.add(cursor.getString(9));    //TABLE_ROW_RINGTONE
                    rowArray.add(cursor.getInt(10) != 0); //TABLE_ROW_CRESC
                    rowArray.add(cursor.getInt(11) != 0); //TABLE_ROW_VIBRATE
                    rowArray.add(cursor.getInt(12) != 0); //TABLE_ROW_SPEECH
                    rowArray.add(cursor.getInt(13) != 0); //TABLE_ROW_TOAST
                    rowArray.add(cursor.getInt(14) != 0); //TABLE_ROW_WARNING
                    rowArray.add(cursor.getInt(15) != 0); //TABLE_ROW_WARN_SOUND
                    rowArray.add(cursor.getInt(16) != 0); //TABLE_ROW_WARN_VIBRATE
                    rowArray.add(cursor.getInt(17) != 0); //TABLE_ROW_WARN_TOAST
                }
                while (cursor.moveToNext());
            }
            cursor.close();
        }
        catch (SQLException e) {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }
        return rowArray;
    }

    /**
     * Close the database
     */
    public void close() {
        db.close();
    }

    public boolean isOpen() {
        return db.isOpen();
    }
    
    /**
     * Print the entire database to the debug log
     */
    public void logOutArray() {
        Log.d(LOG_NAME, "Start of array log");
        ArrayList<ArrayList<Object>> data = getAllRowsAsArrays();
        for (int position=0; position < data.size(); position++)
        {
            ArrayList<Object> row = data.get(position);
            Log.d(LOG_NAME, row.get(0).toString() + ", " +  //TABLE_ROW_ID
                            row.get(1).toString() + ", " +  //TABLE_ROW_NICK
                            row.get(2).toString() + ", " +  //TABLE_ROW_LAT
                            row.get(3).toString() + ", " +  //TABLE_ROW_LONG
                            row.get(4).toString() + ", " +  //TABLE_ROW_PRESET
                            row.get(5).toString() + ", " +  //TABLE_ROW_PROV
                            row.get(6).toString() + ", " +  //TABLE_ROW_RADIUS
                            row.get(7).toString() + ", " +  //TABLE_ROW_UNIT
                            row.get(8).toString() + ", " +  //TABLE_ROW_SOUND
                            row.get(9).toString() + ", " +  //TABLE_ROW_RINGTONE
                            row.get(10).toString() + ", " + //TABLE_ROW_CRESC
                            row.get(11).toString() + ", " + //TABLE_ROW_VIBRATE
                            row.get(12).toString() + ", " + //TABLE_ROW_SPEECH
                            row.get(13).toString() + ", " + //TABLE_ROW_TOAST
                            row.get(14).toString() + ", " + //TABLE_ROW_WARNING
                            row.get(15).toString() + ", " + //TABLE_ROW_WARN_SOUND
                            row.get(16).toString() + ", " + //TABLE_ROW_WARN_VIBRATE
                            row.get(17).toString()          //TABLE_ROW_WARN_TOAST
                );
        }
        Log.d(LOG_NAME, "End of array log");
    }

    /**
     * Get all of the database rows, as a list of lists
     * @return List of rows, as lists
     */
    public ArrayList<ArrayList<Object>> getAllRowsAsArrays() {
        ArrayList<ArrayList<Object>> dataArrays = new ArrayList<ArrayList<Object>>();
        Cursor cursor;

        try {
            cursor = db.query(
                    TABLE_NAME,
                    new String[] { TABLE_ROW_ID, TABLE_ROW_NICK,
                            TABLE_ROW_LAT, TABLE_ROW_LONG, TABLE_ROW_PRESET,
                            TABLE_ROW_PROV, TABLE_ROW_RADIUS,
                            TABLE_ROW_UNIT,
                            TABLE_ROW_SOUND, TABLE_ROW_RINGTONE,
                            TABLE_ROW_CRESC, TABLE_ROW_VIBRATE,
                            TABLE_ROW_SPEECH, TABLE_ROW_TOAST,
                            TABLE_ROW_WARNING, TABLE_ROW_WARN_SOUND,
                            TABLE_ROW_WARN_VIBRATE, TABLE_ROW_WARN_TOAST
                            },
                            null, null, null, null, null
            );
            cursor.moveToFirst();

            if (!cursor.isAfterLast()) {
                do {
                    ArrayList<Object> dataList = new ArrayList<Object>();

                    dataList.add(cursor.getLong(0));      //TABLE_ROW_ID
                    dataList.add(cursor.getString(1));    //TABLE_ROW_NICK
                    dataList.add(cursor.getDouble(2));    //TABLE_ROW_LAT
                    dataList.add(cursor.getDouble(3));    //TABLE_ROW_LONG
                    dataList.add(cursor.getInt(4));       //TABLE_ROW_PRESET
                    dataList.add(cursor.getInt(5));       //TABLE_ROW_PROV
                    dataList.add(cursor.getFloat(6));     //TABLE_ROW_RADIUS
                    dataList.add(cursor.getString(7));    //TABLE_ROW_UNIT
                    dataList.add(cursor.getInt(8) != 0);  //TABLE_ROW_SOUND
                    dataList.add(cursor.getString(9));    //TABLE_ROW_RINGTONE
                    dataList.add(cursor.getInt(10) != 0); //TABLE_ROW_CRESC
                    dataList.add(cursor.getInt(11) != 0); //TABLE_ROW_VIBRATE
                    dataList.add(cursor.getInt(12) != 0); //TABLE_ROW_SPEECH
                    dataList.add(cursor.getInt(13) != 0); //TABLE_ROW_TOAST
                    dataList.add(cursor.getInt(14) != 0); //TABLE_ROW_WARNING
                    dataList.add(cursor.getInt(15) != 0); //TABLE_ROW_WARN_SOUND
                    dataList.add(cursor.getInt(16) != 0); //TABLE_ROW_WARN_VIBRATE
                    dataList.add(cursor.getInt(17) != 0); //TABLE_ROW_WARN_TOAST

                    dataArrays.add(dataList);
                }
                while (cursor.moveToNext());
            }
            cursor.close();
        }
        catch (SQLException e) {
            Log.e("DB Error", e.toString());
            e.printStackTrace();
        }
        return dataArrays;
    }


    /**
     * Helper class for the database
     * Inherits from SQLiteOpenHelper
     * @author spookypeanut
     *
     */
    private class WakeMeAtDbHelper extends SQLiteOpenHelper {
        public WakeMeAtDbHelper(Context context) {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String newTableQueryString = "create table " +
            TABLE_NAME +
            " (" +
            TABLE_ROW_ID + " integer primary key autoincrement not null," +
            TABLE_ROW_NICK + " TEXT," +
            TABLE_ROW_LAT + " DOUBLE," +
            TABLE_ROW_LONG + " DOUBLE," +

            TABLE_ROW_PRESET + " INT," +
            TABLE_ROW_PROV + " INT," +
            TABLE_ROW_RADIUS + " FLOAT," +
            TABLE_ROW_UNIT + " TEXT," +

            TABLE_ROW_SOUND + " INT," +
            TABLE_ROW_RINGTONE + " TEXT," +
            TABLE_ROW_CRESC + " INT," +
            TABLE_ROW_VIBRATE + " INT," +
            TABLE_ROW_SPEECH + " INT," +
            TABLE_ROW_TOAST + " INT," +

            TABLE_ROW_WARNING + " INT," +
            TABLE_ROW_WARN_SOUND + " INT," +
            TABLE_ROW_WARN_VIBRATE + " INT," +
            TABLE_ROW_WARN_TOAST + " INT" +
            ");";
            db.execSQL(newTableQueryString);
        }

        @Override
        public synchronized void close() {
            db.close();
        }

        //REF#0015
        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String upgradeMessage = "Upgrading db v" + oldVersion + " to v" + newVersion;
            Log.d(LOG_NAME, upgradeMessage);
            /*switch (oldVersion) {
            case 2:
                // Add the column for the location type preset
                if (newVersion <= 2){
                    return;
                }
                db.beginTransaction();
                try {
                    // add category column to the providers table
                    db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + TABLE_ROW_PRESET + " INT;");
                    db.setTransactionSuccessful();
                } catch (Throwable ex) {
                    Log.e(LOG_NAME, ex.getMessage(), ex);
                    break; // force to destroy all old data;
                } finally {
                    db.endTransaction();
                }
            case 3:
                // Add the location provider int column, transfer location provider to it
                if (newVersion <= 3) {
                    return;
                }
                db.beginTransaction();
                try {
                    // add category column to the providers table
                    db.execSQL("ALTER TABLE " + TABLE_NAME + " ADD COLUMN " + TABLE_ROW_PRESET + " INT;");
                    db.setTransactionSuccessful();
                } catch (Throwable ex) {
                    Log.e(LOG_NAME, ex.getMessage(), ex);
                    break; // force to destroy all old data;
                } finally {
                    db.endTransaction();
                }
            case 9999999:
                if (newVersion <= 9999999) {
                    return;
                }

                return;
            }*/
            Log.e(LOG_NAME, "Couldn't upgrade db to " + newVersion + ". Existing data must be destroyed.");
            destroyOldTables(db);
            onCreate(db);
        }

        /**
         * Destroy the old tables, if they can't be upgraded
         * @param db The database to drop the tables of
         */
        private void destroyOldTables(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        }
    }
}
