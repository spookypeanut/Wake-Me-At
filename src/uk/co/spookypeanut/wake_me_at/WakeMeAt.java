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

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class WakeMeAt extends ListActivity {
    public static final String PREFS_NAME = "WakeMeAtPrefs";
    public static final String LOG_NAME = "WakeMeAt";
    private DatabaseManager db;

    private OnClickListener mEditLocButton = new Button.OnClickListener() {
        public void onClick(View v) {
            Intent i = new Intent(WakeMeAt.this.getApplication(), EditLocation.class);
            Log.d(LOG_NAME, "About to start activity");
            startActivity(i);
        }
    };
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_NAME, "Start onCreate()");
        super.onCreate(savedInstanceState);

        Log.d(LOG_NAME, "DatabaseManager");
        db = new DatabaseManager(this);
        
        Log.d(LOG_NAME, "setListAdaptor");
        setListAdapter(new LocListAdapter(this));

        Log.d(LOG_NAME, "setContentView");
        setContentView(R.layout.main);

        ArrayList<ArrayList<Object>> allData = db.getAllRowsAsArrays();
        for (int position=0; position < allData.size(); position++)
        { 
            ArrayList<Object> row = allData.get(position);
            
            Log.d(LOG_NAME, row.get(0).toString() + ", " +
                            row.get(1).toString() + ", " +
                            row.get(2).toString() + ", " +
                            row.get(3).toString() + ", " +
                            row.get(4).toString() + ", " +
                            row.get(5).toString());
        }

//        Button button;
    //    button = (Button)findViewById(R.id.edit_loc_button);
   //     button.setOnClickListener(mEditLocButton);
    }
    private class LocListAdapter extends BaseAdapter {
        public LocListAdapter(Context context) {
            mContext = context;
        }

        /**
         * The number of items in the list is determined by the number of speeches
         * in our array.
         * 
         * @see android.widget.ListAdapter#getCount()
         */
        public int getCount() {
            return db.getRowCount();
            
        }

        /**
         * Since the data comes from an array, just returning the index is
         * sufficient to get at the data. If we were using a more complex data
         * structure, we would return whatever object represents one row in the
         * list.
         * 
         * @see android.widget.ListAdapter#getItem(int)
         */
        public Object getItem(int position) {
            return position;
        }

        /**
         * Use the array index as a unique id.
         * 
         * @see android.widget.ListAdapter#getItemId(int)
         */
        public long getItemId(int position) {
            return position;
        }

        /**
         * Make a SpeechView to hold each row.
         * 
         * @see android.widget.ListAdapter#getView(int, android.view.View,
         *      android.view.ViewGroup)
         */
        public View getView(int position, View convertView, ViewGroup parent) {
            LocEntry sv;
            if (convertView == null) {
                sv = new LocEntry(mContext, db.getNick(position + 1),
                        db.getProvider(position + 1));
            } else {
                sv = (LocEntry) convertView;
                sv.setTitle(db.getNick(position + 1));
                sv.setDialogue(db.getProvider(position + 1));
            }

            return sv;
        }

        private Context mContext;

    }
    private class LocEntry extends LinearLayout {
        public LocEntry(Context context, String title, String words) {
            super(context);

            this.setOrientation(VERTICAL);

            // Here we build the child views in code. They could also have
            // been specified in an XML file.

            mTitle = new TextView(context);
            mTitle.setText(title);
            addView(mTitle, new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

            mDialogue = new TextView(context);
            mDialogue.setText(words);
            addView(mDialogue, new LinearLayout.LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        }

        /**
         * Convenience method to set the title of a SpeechView
         */
        public void setTitle(String title) {
            mTitle.setText(title);
        }

        /**
         * Convenience method to set the dialogue of a SpeechView
         */
        public void setDialogue(String words) {
            mDialogue.setText(words);
        }

        private TextView mTitle;
        private TextView mDialogue;
    }
    

}