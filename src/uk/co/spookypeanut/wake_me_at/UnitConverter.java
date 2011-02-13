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
 * A class that is set to a particular unit type, that facilitates the conversion between units
 * @author spookypeanut
 */

public class UnitConverter
{
    public final String LOG_NAME = WakeMeAt.LOG_NAME;
    private final int SYSTEM_METRIC = 1;
    private final int SYSTEM_IMPERIAL = 2;
    
    // Number of decimal places to round to
    static final int DP = 1;
    static final int SF = 2;
    
    // The value that determines which unit in a unit system will be used
    private final double LARGESTNUM = 500;

    Context mContext;
    
    // Metre is a special unit, as it's the one that's used internally
    private final Unit mMetreUnit = new Unit("metre", "m", 1, SYSTEM_METRIC, "metres");
    
    private final List <Unit> mUnitList = 
            Arrays.asList(
                mMetreUnit,
                new Unit("kilometre", "km", 1000, SYSTEM_METRIC, "kilometres"),
                
                new Unit("foot", "ft", 0.3048, SYSTEM_IMPERIAL, "feet"),
                new Unit("yard", "yd", 0.9144, SYSTEM_IMPERIAL, "yards"),
                new Unit("mile", "mi", 1609.344, SYSTEM_IMPERIAL, "miles"));

    Unit mUnit;
    
    /**
     * Unit converter constructor
     * @param unitAbbrev The abbreviation of the unit to initialize to
     */
    public UnitConverter(Context context, String unitAbbrev) {
        Unit unit = getFromAbbrev(unitAbbrev);
        switchUnit(unit);
    }
    
    /**
     * Get the list of unit abbreviations
     * @return The list
     */
    public ArrayList<String> getAbbrevList() {
        ArrayList<String> returnList = new ArrayList<String>();
        for (Iterator<Unit> i = mUnitList.iterator(); i.hasNext();) {
            Unit currUnit = i.next();
            returnList.add(currUnit.getAbbrev());
        }
        return returnList;
    }
    
    /**
     * Get a unit object from its abbreviation
     * @param abbrev The abbreviation of the required unit
     * @return The unit object
     */
    private Unit getFromAbbrev(String abbrev) {
        for (Iterator<Unit> i = mUnitList.iterator(); i.hasNext();) {
            Unit currUnit = i.next();
            String currAbbrev = currUnit.getAbbrev();
            if (currAbbrev.equals(abbrev)) {
                return currUnit;
            }
        }
        Log.wtf(LOG_NAME, "The unit \"" + abbrev + "\" was not found");
        return null;
    }
    
    /**
     * Get the name of the current unit
     * @return The name of the unit
     */
    public String getName() {
        return mUnit.getName();
    }
    
    /**
     * Get the abbreviation of the current unit
     * @return The abbreviation of the unit
     */
    public String getAbbrev() {
        return mUnit.getAbbrev();
    }
    
    /**
     * Convert a value in the current unit to metres
     * @param value The value to convert to metres
     * @return The value in metres
     */
    public double toMetres(double value) {
        return convert(value, mUnit, getFromAbbrev("m"));
    }
    
    /**
     * Convert a value in metres to the current unit
     * @param value The value to convert to the current unit
     * @return The value in the current unit
     */
    public double toUnit(double value) {
        return convert(value, getFromAbbrev("m"), mUnit);
    }
    
    /**
     * Convert a double to a certain number of significant figures
     * REF#0013
     * @param value The value to convert
     * @param significant The number of significant figures to use
     * @return The string with the desired formatting
     */
    public static String formatToSignificant(double value,
            int significant)
         {
            MathContext mathContext = new MathContext(significant,
               RoundingMode.DOWN);
            BigDecimal bigDecimal = new BigDecimal(value,
               mathContext);
            return bigDecimal.toPlainString();
         } 
    
    /**
     * The "standard" method for rounding
     * @param value The value to round
     * @return String containing the rounded value
     */
    private static String round(double value) {
        return formatToSignificant(value, SF);
    }
    
    /**
     * A string representation of a value, including the unit abbreviation
     * @param value The value to convert to a string
     * @return A string in the form "100.2yd"
     */
    public String out(double value) {
        // 0 is always metres (used internally)
        Unit bestUnit = getBestUnit(value);
        double outValue = convert(value, mMetreUnit, bestUnit);
        String outAbbrev = bestUnit.getAbbrev();
        
        return String.format("%s%s", round(outValue), outAbbrev);
    }
    
    /**
     * A string representation of a value, ready to be spoken
     * Similar to out(), but designed to be sent to text-to-speech
     * @param value The value to convert to a string
     * @return A string in the form "100.2 yards"
     */
    public String outSpeech(double value) {
        // 0 is always metres (used internally)
        Unit bestUnit = getBestUnit(value);
        double outValue = convert(value, mMetreUnit, bestUnit);
        String outName = bestUnit.getPlural();
        
        return String.format("%s%s", round(outValue), outName);
    }
    
    /**
     * Switch the objects current unit
     * @param unit The unit to switch to
     */
    public void switchUnit(Unit unit) {
        mUnit = unit;
    }
    
    /**
     * A generic unit converter
     * @param value The value to convert to another unit
     * @param sourceUnit The source unit for the conversion
     * @param destUnit The destination unit for the conversion
     * @return The value in the desired unit
     */
    public double convert(double value, Unit sourceUnit, Unit destUnit) {
        return value * sourceUnit.getValue() / destUnit.getValue();
    }
    
    /**
     * Return a list of unit objects that are in a given unit system
     * @param system An int value of the desired unit system
     * @return A list of unit objects in the given system
     */
    private ArrayList<Unit> unitsFromSystem(int system) {
        ArrayList<Unit> returnList = new ArrayList<Unit>();
        for (Iterator<Unit> i = mUnitList.iterator(); i.hasNext();) {
            Unit currUnit = i.next();
            int currSystem = currUnit.getSystem();
            if (currSystem == system) {
                returnList.add(currUnit);
            }
        }
        return returnList;
    }
    
    /**
     * Get the best unit for a human-readable description of a given value 
     * @param srcValue Value to fine unit for, in metres
     * @return A unit object of the best unit to use
     */
    private Unit getBestUnit(double srcValue) {
//        Log.d(LOG_NAME, "getBestUnit(" + srcValue + ")");
        Unit currBestUnit = null;
        double currBestValue = 0;
        int system = mUnit.getSystem();
        ArrayList<Unit> unitList = unitsFromSystem(system);
        double destValue;
        for (Iterator<Unit> i = unitList.iterator(); i.hasNext();) {
            Unit currUnit = i.next();
            if (currBestUnit == null) {
                currBestUnit = currUnit;
                currBestValue = convert(srcValue, mMetreUnit, currUnit);
                continue;
            }
            //Log.d(LOG_NAME, "Best unit so far: " + currBestUnit.getAbbrev());
            destValue = convert(srcValue, mMetreUnit, currUnit);
            if ((destValue < LARGESTNUM) && (destValue > currBestValue || currBestValue > LARGESTNUM)) {
                    currBestUnit = currUnit;
                    currBestValue = destValue;
            }
        }
        if (currBestUnit == null) {
            Log.wtf(LOG_NAME, "No best unit found");
            Log.d(LOG_NAME, "unit = " + mUnit.getAbbrev() + ", value = " + srcValue);
        }
//        Log.d(LOG_NAME, "Best unit for " + srcValue + ": " + currBestUnit.getAbbrev());
        return currBestUnit;
    }
    
    /**
     * An object to represent a unit
     * @author spookypeanut
     *
     */
    public class Unit {
        String mName;
        String mAbbrev;
        double mValue;
        int mSystem;
        String mPlural;
        
        /**
         * Constructor
         * @param name The name of the unit for use in selecting it (eg foot)
         * @param abbrev The abbreviation of the unit
         * @param value The number of metres in this unit
         * @param system The unit system that this unit is in (imperial, metric, etc)
         * @param plural The plural of the unit (eg feet)
         */
        public Unit(String name, String abbrev, double value, int system, String plural) {
            mName = name;
            mAbbrev = abbrev;
            mValue = value;
            mSystem = system;
            mPlural = plural;
        }
        
        /**
         * Get the name of the unit
         * @return The name of the unit
         */
        public String getName() {
            return mName;
        }
        
        /**
         * Get the plural of the unit
         * @return The plural of the unit
         */
        public String getPlural() {
            return mPlural;
        }
        
        /**
         * Get the abbreviation of the unit
         * @return The abbreviation of the unit
         */
        public String getAbbrev() {
            return mAbbrev;
        }
        
        /**
         * Get the number of metres in the unit
         * @return The number of metres in the unit
         */
        public double getValue() {
            return mValue;
        }
        
        /**
         * Get the system that the unit is in
         * @return The system that the unit is in
         */
        public int getSystem() {
            return mSystem;
        }
    }
}