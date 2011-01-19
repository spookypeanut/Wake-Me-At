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
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

public class UnitConverter
{
    public final String LOG_NAME = WakeMeAt.LOG_NAME;
    private final int SYSTEM_METRIC = 1;
    private final int SYSTEM_IMPERIAL = 2;
    
    // Number of decimal places to round to
    static final int DP = 1;
    
    // The value that determines which unit in a unit system will be used
    private final double LARGESTNUM = 500;

    Context mContext;
    Resources mRes;
    
    // Metre is a special unit, as it's the one that's used internally
    private final Unit mMetreUnit = new Unit("metre", "m", 1, SYSTEM_METRIC, "metres");
    
    private final List <Unit> mUnitList = 
            Arrays.asList(
                mMetreUnit,
                new Unit("foot", "ft", 0.3048, SYSTEM_IMPERIAL, "feet"),
                new Unit("kilometre", "km", 1000, SYSTEM_METRIC, "kilometres"),
                new Unit("mile", "mi", 1609.344, SYSTEM_IMPERIAL, "miles"));

    Unit mUnit;
    
    
    public UnitConverter(Context context, String unitAbbrev) {
        Unit unit = getFromAbbrev(unitAbbrev);
        this.mContext = context;
        mRes = mContext.getResources();

        switchUnit(unit);
    }
    
    public ArrayList<String> getAbbrevList() {
        ArrayList<String> returnList = new ArrayList<String>();
        for (Iterator<Unit> i = mUnitList.iterator(); i.hasNext();) {
            Unit currUnit = i.next();
            returnList.add(currUnit.getAbbrev());
        }
        return returnList;
    }
    
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
    
    public String getName() {
        return mUnit.getName();
    }
    
    public String getAbbrev() {
        return mUnit.getAbbrev();
    }
    
    public double toMetres(double value) {
        return convert(value, mUnit, getFromAbbrev("m"));
    }
    
    public double toUnit(double value) {
        return convert(value, getFromAbbrev("m"), mUnit);
    }
    
    public String out(double value) {
        // 0 is always metres (used internally)
        Unit bestUnit = getBestUnit(value);
        double outValue = convert(value, mMetreUnit, bestUnit);
        String outAbbrev = bestUnit.getAbbrev();
        
        return String.format("%." + DP + "f%s", outValue, outAbbrev);
    }
    
    public String outSpeech(double value) {
        // 0 is always metres (used internally)
        Unit bestUnit = getBestUnit(value);
        double outValue = convert(value, mMetreUnit, bestUnit);
        String outName = bestUnit.getPlural();
        
        return String.format("%." + DP + "f %s", outValue, outName);
    }
    
    public void switchUnit(Unit unit) {
        mUnit = unit;
    }
    
    public static double roundToDecimals(double d, int c) {
        int temp = (int) (d * Math.pow(10, c));
        return (double) temp / Math.pow(10, c);
    }
    
    public double convert(double value, Unit sourceUnit, Unit destUnit) {
        return value * sourceUnit.getValue() / destUnit.getValue();
    }
    
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
    
    private Unit getBestUnit(double srcValue) {
        Unit currBestUnit = null;
        double currBestValue = 0;
        int system = mUnit.getSystem();
        ArrayList<Unit> unitList = unitsFromSystem(system);
        double destValue;
        for (Iterator<Unit> i = unitList.iterator(); i.hasNext();) {
            Unit currUnit = i.next();
            Log.d(LOG_NAME, "Testing unit " + currUnit.getAbbrev());
            if (currBestUnit == null) {
                currBestUnit = currUnit;
                currBestValue = convert(srcValue, mMetreUnit, currUnit);
                continue;
            }
            Log.d(LOG_NAME, "Best unit so far: " + currBestUnit.getAbbrev());
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
        Log.d(LOG_NAME, "Best unit: " + currBestUnit.getAbbrev());
        return currBestUnit;
    }
    
    public class Unit {
        String mName;
        String mAbbrev;
        double mValue;
        int mSystem;
        String mPlural;
        
        public Unit(String name, String abbrev, double value, int system, String plural) {
            mName = name;
            mAbbrev = abbrev;
            mValue = value;
            mSystem = system;
            mPlural = plural;
        }
        
        public String getName() {
            return mName;
        }
        
        public String getPlural() {
            return mPlural;
        }
        
        public String getAbbrev() {
            return mAbbrev;
        }
        
        public double getValue() {
            return mValue;
        }
        
        public int getSystem() {
            return mSystem;
        }
    }
}