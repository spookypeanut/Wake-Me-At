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

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * @author spookypeanut
 * Handle the database used in WakeMe@
 */
/**
 * @author spookypeanut
 *
 */
public class DatabaseManager
{
    private SQLiteDatabase db;
    public final String LOG_NAME = WakeMeAt.LOG_NAME;

    private final String DB_NAME = "WakeMeAtDB";
    private final int DB_VERSION = 2;

    private final String TABLE_NAME = "Locations";
    private final String TABLE_ROW_ID = "id";
    private final String TABLE_ROW_NICK = "table_row_nick";
    private final String TABLE_ROW_LAT = "table_row_lat";
    private final String TABLE_ROW_LONG = "table_row_long";
    private final String TABLE_ROW_PROV = "table_row_prov";
    private final String TABLE_ROW_RADIUS = "table_row_radius";
    private final String TABLE_ROW_UNIT = "table_row_unit";
    
    Context context;


    /** Constructor
     * @param context The context that the db is being handled in
     */
    public DatabaseManager(Context context) {
        this.context = context;
        CustomSQLiteOpenHelper helper = new CustomSQLiteOpenHelper(context);
        this.db = helper.getWritableDatabase();
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
    public long addRow(String rowNick, double rowLat, double rowLong,
                       String rowProv, float rowRadius, String rowUnit) {
        // this is a key value pair holder used by android's SQLite functions
        ContentValues values = new ContentValues();
        values.put(TABLE_ROW_NICK, rowNick);
        values.put(TABLE_ROW_LAT, rowLat);
        values.put(TABLE_ROW_LONG, rowLong);
        values.put(TABLE_ROW_PROV, rowProv);
        values.put(TABLE_ROW_RADIUS, rowRadius);
        values.put(TABLE_ROW_UNIT, rowUnit);
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
    public void updateRow(long rowId, String rowNick, double rowLat, double rowLong,
                          String rowProv, float rowRadius, String rowUnit) {
        // this is a key value pair holder used by android's SQLite functions
        ContentValues values = new ContentValues();
        values.put(TABLE_ROW_NICK, rowNick);
        values.put(TABLE_ROW_LAT, rowLat);
        values.put(TABLE_ROW_LONG, rowLong);
        values.put(TABLE_ROW_PROV, rowProv);
        values.put(TABLE_ROW_RADIUS, rowRadius);
        values.put(TABLE_ROW_UNIT, rowUnit);
        try {
            db.update(TABLE_NAME, values, TABLE_ROW_ID + "=" + rowId, null);
        }
        catch (Exception e) {
            Log.e("DB Error", e.toString());
            e.printStackTrace();
        }
    }

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

    public void setDatumD(long rowId, String column, double value) {
        setDatumS(rowId, column, Double.toString(value));
    }

    public void setDatumF(long rowId, String column, float value) {
        setDatumS(rowId, column, Float.toString(value));
    }

    public void setDatumI(long rowId, String column, int value) {
        setDatumS(rowId, column, Integer.toString(value));
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

    public String getProvider(long rowId) {
        return getDatumS(rowId, TABLE_ROW_PROV);
    }

    public void setProvider(long rowId, String provider) {
        setDatumS(rowId, TABLE_ROW_PROV, provider);
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
                            TABLE_ROW_PROV, TABLE_ROW_RADIUS,
                            TABLE_ROW_UNIT},
                            TABLE_ROW_ID + "=" + rowId,
                            null, null, null, null, null
            );
            cursor.moveToFirst();

            // if there is data available after the cursor's pointer, add
            // it to the ArrayList that will be returned by the method.
            if (!cursor.isAfterLast()) {
                do {
                    rowArray.add(cursor.getLong(0));
                    rowArray.add(cursor.getString(1));
                    rowArray.add(cursor.getDouble(2));
                    rowArray.add(cursor.getDouble(3));
                    rowArray.add(cursor.getString(4));
                    rowArray.add(cursor.getFloat(5));
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

    public void close() {
        db.close();
    }
    
    public void logOutArray() {
        Log.d(LOG_NAME, "Start of array log");
        ArrayList<ArrayList<Object>> data = getAllRowsAsArrays();
        for (int position=0; position < data.size(); position++)
        { 
            ArrayList<Object> row = data.get(position);
            Log.d(LOG_NAME, row.get(0).toString() + ", " +
                            row.get(1).toString() + ", " +
                            row.get(2).toString() + ", " +
                            row.get(3).toString() + ", " +
                            row.get(4).toString() + ", " +
                            row.get(5).toString() + ", " +
                            row.get(6).toString());
        }
        Log.d(LOG_NAME, "End of array log");
    }

    public ArrayList<ArrayList<Object>> getAllRowsAsArrays() {
        ArrayList<ArrayList<Object>> dataArrays = new ArrayList<ArrayList<Object>>();
        Cursor cursor;

        try {
            cursor = db.query(
                    TABLE_NAME,
                    new String[] { TABLE_ROW_ID, TABLE_ROW_NICK,
                            TABLE_ROW_LAT, TABLE_ROW_LONG,
                            TABLE_ROW_PROV, TABLE_ROW_RADIUS,
                            TABLE_ROW_UNIT},
                            null, null, null, null, null
            );
            cursor.moveToFirst();

            if (!cursor.isAfterLast()) {
                do {
                    ArrayList<Object> dataList = new ArrayList<Object>();

                    dataList.add(cursor.getLong(0));
                    dataList.add(cursor.getString(1));
                    dataList.add(cursor.getDouble(2));
                    dataList.add(cursor.getDouble(3));
                    dataList.add(cursor.getString(4));
                    dataList.add(cursor.getFloat(5));
                    dataList.add(cursor.getString(6));

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

    
    private class CustomSQLiteOpenHelper extends SQLiteOpenHelper {
        public CustomSQLiteOpenHelper(Context context) {
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
            TABLE_ROW_PROV + " TEXT," +
            TABLE_ROW_RADIUS + " FLOAT," +
            TABLE_ROW_UNIT + " TEXT" +
            ");";
            db.execSQL(newTableQueryString);
        }

        @Override
        public synchronized void close() {
            db.close();
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            String upgradeMessage = "Upgrading db v" + oldVersion + " to v" + newVersion;
            Log.d(LOG_NAME, upgradeMessage);
        }
        
    }
}