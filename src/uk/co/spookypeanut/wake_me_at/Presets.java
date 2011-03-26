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

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.util.Log;

/**
 * A class to dispense preset information
 * @author spookypeanut
 */

public class Presets
{
    private String LOG_NAME;

    Context mContext;
    Preset mPreset;

    private final Preset mPresetArray[] = {
                new Preset("Custom", 1803.0, 1, "m"),
                new Preset("Train", 1.8, 1, "km"),
                new Preset("Bus (local)", 200.0, 0, "m"),
                new Preset("Bus (inter-city)", 2.0, 1, "km")
    };
    
    /**
     * Unit converter constructor
     * @param unitAbbrev The abbreviation of the unit to initialize to
     */
    public Presets(Context context, int preset) {
        LOG_NAME = (String) context.getText(R.string.app_name_nospaces);
        mPreset = mPresetArray[preset];
    }

    /**
     * Get the array of preset names
     * @return The array
     */
    public String[] getAllNames() {
        String[] returnArray = new String[mPresetArray.length];
        for (int i = 0; i < mPresetArray.length; i++) {
            returnArray[i] = mPresetArray[i].getName();
        }
        return returnArray;
    }

    public double getRadius() {
        return mPreset.getRadius();
    }

    /**
     * An TODO object to represent a unit
     * @author spookypeanut
     *
     */
    public class Preset {
        String mName;
        double mRadius;
        int mLocProv;
        String mUnit;
        
        /**
         * Constructor
         * @param name The name of the unit for use in selecting it (eg foot)
         * TODO @param abbrev The abbreviation of the unit
         * @param value The number of metres in this unit
         * @param system The unit system that this unit is in (imperial, metric, etc)
         * @param plural The plural of the unit (eg feet)
         */
        public Preset(String name, double radius, int locProv, String unit) {
            mName = name;
            mRadius = radius;
            mLocProv = locProv;
            mUnit = unit;
        }
        
        /**
         * Get the name of the unit
         * @return The name of the unit
         */
        public String getName() {
            return mName;
        }
        
        /**
         * Get the radius of the preset
         * @return The radius of the preset
         */
        public double getRadius() {
            return mRadius;
        }
        
        /**
         * Get the abbreviation of the unit
         * @return The abbreviation of the unit
         */
        public int getLocProv() {
            return mLocProv;
        }
        
        /**
         * Get the TODO number of metres in the unit
         * @return The number of metres in the unit
         */
        public String getUnit() {
            return mUnit;
        }
    }
}
