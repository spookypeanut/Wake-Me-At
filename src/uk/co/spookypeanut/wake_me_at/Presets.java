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

import android.content.Context;

/**
 * A class to dispense preset information
 * @author spookypeanut
 */

public class Presets
{
    //private String LOG_NAME;

    Context mContext;
    Preset mPreset;
    int mIndex;

    private final Preset mPresetArray[] = {
                new Preset("Custom", (float)1803.0, 1, "m"),
                new Preset("Train", (float)1.8, 1, "km"),
                new Preset("Bus (local)", (float)200.0, 0, "m"),
                new Preset("Bus (inter-city)", (float)2.0, 1, "km")
    };
    
    /**
     * Unit converter constructor
     * @param unitAbbrev The abbreviation of the unit to initialize to
     */
    public Presets(Context context, int preset) {
        switchPreset(preset);
    }

    public void switchPreset(int preset) {
        mPreset = mPresetArray[preset];
        mIndex = preset;
    }

    public boolean isCustom() {
        return (getName() == "Custom");
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

    public int getIndex() {
        return mIndex;
    }

    public String getName() {
        return mPreset.getName();
    }

    public float getRadius() {
        return mPreset.getRadius();
    }

    public int getLocProv() {
        return mPreset.getLocProv();
    }

    public String getUnit() {
        return mPreset.getUnit();
    }

    /**
     * An object to represent a mode-of-transport preset
     * @author spookypeanut
     *
     */
    public class Preset {
        String mName;
        float mRadius;
        int mLocProv;
        String mUnit;
        
        /**
         * Constructor
         * @param name The name of the preset (eg Train)
         * @param radius The distance away to alert the user (in *unit*s)
         * @param locProv The location provider to use
         * @param unit The name of the unit that *radius* refers to
         */
        public Preset(String name, float radius, int locProv, String unit) {
            mName = name;
            mRadius = radius;
            mLocProv = locProv;
            mUnit = unit;
        }
        
        /**
         * Get the name of the preset
         * @return The name of the preset
         */
        public String getName() {
            return mName;
        }
        
        /**
         * Get the radius of the preset
         * @return The radius of the preset
         */
        public float getRadius() {
            return mRadius;
        }
        
        /**
         * Get the location provider used in the preset
         * @return The number of the location provider
         */
        public int getLocProv() {
            return mLocProv;
        }
        
        /**
         * Get the name of the unit used in the preset
         * @return The name of the unit
         */
        public String getUnit() {
            return mUnit;
        }
    }
}
