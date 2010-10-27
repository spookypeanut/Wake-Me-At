package uk.co.spookypeanut.wake_me_at;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DatabaseManager
{
    // the Activity or Application that is creating an object from this class.
    Context context;

    // a reference to the database used by this application/object
    private SQLiteDatabase db;

    private final String DB_NAME = "WakeMeAtDB";
    private final int DB_VERSION = 1;

    // These constants are specific to the database table.  They should be
    // changed to suit your needs.
    private final String TABLE_NAME = "Locations";
    private final String TABLE_ROW_ID = "id";
    private final String TABLE_ROW_NICK = "table_row_nick";
    private final String TABLE_ROW_LAT = "table_row_lat";
    private final String TABLE_ROW_LONG = "table_row_long";

    public DatabaseManager(Context context)
    {
        this.context = context;

        // create or open the database
        CustomSQLiteOpenHelper helper = new CustomSQLiteOpenHelper(context);
        this.db = helper.getWritableDatabase();
    }

    public void addRow(String rowNick, String rowLat, String rowLong)
    {
        // this is a key value pair holder used by android's SQLite functions
        ContentValues values = new ContentValues();
        values.put(TABLE_ROW_NICK, rowNick);
        values.put(TABLE_ROW_LAT, rowLat);
        values.put(TABLE_ROW_LONG, rowLong);

        // ask the database object to insert the new data 
        try{db.insert(TABLE_NAME, null, values);}
        catch(Exception e)
        {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }
    }

    public void deleteRow(long rowID)
    {
        // ask the database manager to delete the row of given id
        try {db.delete(TABLE_NAME, TABLE_ROW_ID + "=" + rowID, null);}
        catch (Exception e)
        {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }
    }

    public void updateRow(long rowID, String rowNick, String rowLat, String rowLong)
    {
        // this is a key value pair holder used by android's SQLite functions
        ContentValues values = new ContentValues();
        values.put(TABLE_ROW_NICK, rowNick);
        values.put(TABLE_ROW_LAT, rowLat);
        values.put(TABLE_ROW_LONG, rowLong);

        // ask the database object to update the database row of given rowID
        try {db.update(TABLE_NAME, values, TABLE_ROW_ID + "=" + rowID, null);}
        catch (Exception e)
        {
            Log.e("DB Error", e.toString());
            e.printStackTrace();
        }
    }

    public ArrayList<Object> getRowAsArray(long rowID)
    {
        // create an array list to store data from the database row.
        // I would recommend creating a JavaBean compliant object 
        // to store this data instead.  That way you can ensure
        // data types are correct.
        ArrayList<Object> rowArray = new ArrayList<Object>();
        Cursor cursor;

        try
        {
            // this is a database call that creates a "cursor" object.
            // the cursor object store the information collected from the
            // database and is used to iterate through the data.
            cursor = db.query
            (
                    TABLE_NAME,
                    new String[] { TABLE_ROW_ID, TABLE_ROW_NICK,
                            TABLE_ROW_LAT, TABLE_ROW_LONG },
                            TABLE_ROW_ID + "=" + rowID,
                            null, null, null, null, null
            );

            // move the pointer to position zero in the cursor.
            cursor.moveToFirst();

            // if there is data available after the cursor's pointer, add
            // it to the ArrayList that will be returned by the method.
            if (!cursor.isAfterLast())
            {
                do
                {
                    rowArray.add(cursor.getLong(0));
                    rowArray.add(cursor.getString(1));
                    rowArray.add(cursor.getString(2));
                }
                while (cursor.moveToNext());
            }

            // let java know that you are through with the cursor.
            cursor.close();
        }
        catch (SQLException e) 
        {
            Log.e("DB ERROR", e.toString());
            e.printStackTrace();
        }

        // return the ArrayList containing the given row from the database.
        return rowArray;
    }

    public ArrayList<ArrayList<Object>> getAllRowsAsArrays()
    {
        // create an ArrayList that will hold all of the data collected from
        // the database.
        ArrayList<ArrayList<Object>> dataArrays = new ArrayList<ArrayList<Object>>();

        // this is a database call that creates a "cursor" object.
        // the cursor object store the information collected from the
        // database and is used to iterate through the data.
        Cursor cursor;

        try
        {
            // ask the database object to create the cursor.
            cursor = db.query(
                    TABLE_NAME,
                    new String[] { TABLE_ROW_ID, TABLE_ROW_NICK,
                            TABLE_ROW_LAT, TABLE_ROW_LONG },
                            null, null, null, null, null
            );

            // move the cursor's pointer to position zero.
            cursor.moveToFirst();

            // if there is data after the current cursor position, add it
            // to the ArrayList.
            if (!cursor.isAfterLast())
            {
                do
                {
                    ArrayList<Object> dataList = new ArrayList<Object>();

                    dataList.add(cursor.getLong(0));
                    dataList.add(cursor.getString(1));
                    dataList.add(cursor.getString(2));

                    dataArrays.add(dataList);
                }
                // move the cursor's pointer up one position.
                while (cursor.moveToNext());
            }
        }
        catch (SQLException e)
        {
            Log.e("DB Error", e.toString());
            e.printStackTrace();
        }

        // return the ArrayList that holds the data collected from
        // the database.
        return dataArrays;
    }




    /**********************************************************************
     * THIS IS THE BEGINNING OF THE INTERNAL SQLiteOpenHelper SUBCLASS.
     * 
     * I MADE THIS CLASS INTERNAL SO I CAN COPY A SINGLE FILE TO NEW APPS 
     * AND MODIFYING IT - ACHIEVING DATABASE FUNCTIONALITY.  ALSO, THIS WAY 
     * I DO NOT HAVE TO SHARE CONSTANTS BETWEEN TWO FILES AND CAN
     * INSTEAD MAKE THEM PRIVATE AND/OR NON-STATIC.  HOWEVER, I THINK THE
     * INDUSTRY STANDARD IS TO KEEP THIS CLASS IN A SEPARATE FILE.
     *********************************************************************/

    /**
     * This class is designed to check if there is a database that currently
     * exists for the given program.  If the database does not exist, it creates
     * one.  After the class ensures that the database exists, this class
     * will open the database for use.  Most of this functionality will be
     * handled by the SQLiteOpenHelper parent class.  The purpose of extending
     * this class is to tell the class how to create (or update) the database.
     * 
     * @author Randall Mitchell
     *
     */
    private class CustomSQLiteOpenHelper extends SQLiteOpenHelper
    {
        public CustomSQLiteOpenHelper(Context context)
        {
            super(context, DB_NAME, null, DB_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            // This string is used to create the database.  It should
            // be changed to suit your needs.
            String newTableQueryString = "create table " +
            TABLE_NAME +
            " (" +
            TABLE_ROW_ID + " integer primary key autoincrement not null," +
            TABLE_ROW_NICK + " text," +
            TABLE_ROW_LAT + " text" +
            TABLE_ROW_LONG + " text" +
            ");";
            // execute the query string to the database.
            db.execSQL(newTableQueryString);
        }


        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            // NOTHING TO DO HERE. THIS IS THE ORIGINAL DATABASE VERSION.
            // OTHERWISE, YOU WOULD SPECIFIY HOW TO UPGRADE THE DATABASE.
        }
    }
}