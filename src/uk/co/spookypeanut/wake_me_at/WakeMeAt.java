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

import android.app.Activity;
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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

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

        db = new DatabaseManager(this);
        Log.d(LOG_NAME, "setContentView");
        setContentView(R.layout.main);

        Log.d(LOG_NAME, "button");
        Button button;

        Log.d(LOG_NAME, "findViewById");
//        ListView locationList = new ListView(this); //(ListView)findViewById(R.id.location_list);
        
        ArrayList<String> names;
        ArrayList<Long> indices;
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

        setListAdapter(new SpeechListAdapter(this));

        button = (Button)findViewById(R.id.edit_loc_button);
        button.setOnClickListener(mEditLocButton);
    }
    private class SpeechListAdapter extends BaseAdapter {
        public SpeechListAdapter(Context context) {
            mContext = context;
        }

        /**
         * The number of items in the list is determined by the number of speeches
         * in our array.
         * 
         * @see android.widget.ListAdapter#getCount()
         */
        public int getCount() {
            return mTitles.length;
        }

        /**
         * Since the data comes from an array, just returning the index is
         * sufficent to get at the data. If we were using a more complex data
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
            SpeechView sv;
            if (convertView == null) {
                sv = new SpeechView(mContext, mTitles[position],
                        mDialogue[position]);
            } else {
                sv = (SpeechView) convertView;
                sv.setTitle(mTitles[position]);
                sv.setDialogue(mDialogue[position]);
            }

            return sv;
        }

        private Context mContext;
        private String[] mTitles = 
        {
                "Henry IV (1)",   
                "Henry V",
                "Henry VIII",       
                "Richard II",
                "Richard III",
                "Merchant of Venice",  
                "Othello",
                "King Lear"
        };
        

        private String[] mDialogue = 
        {
                "So", "Hear", "I", "First",
                "Now", "To", "Virtue", "Blow"
        };
    }
    private class SpeechView extends LinearLayout {
        public SpeechView(Context context, String title, String words) {
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