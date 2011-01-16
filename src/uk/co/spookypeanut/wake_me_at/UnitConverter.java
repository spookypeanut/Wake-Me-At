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

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

public class UnitConverter
{
    public final String LOG_NAME = WakeMeAt.LOG_NAME;

    Context mContext;
    Resources mRes;

    
    private final List <Unit> mUnitList =
            Arrays.asList(
                new Unit("metre", "m", 1, "km", null),
                new Unit("foot", "ft", 0.3048, "mi", null),
                new Unit("kilometre", "km", 1000, null, "m"),
                new Unit("mile", "mi", 1609.344, null, "ft"));

    Unit mUnit;
    
    // Number of decimal places to round to
    static final int DP = 2;
    
    private double[] stringArrayToDoubleArray(String[] inputArray) {
        double[] returnArray = new double[inputArray.length];
        int i;
        for(i=0; i < inputArray.length; i++) {
            returnArray[i] = Double.valueOf(inputArray[i]);
        }
        return returnArray;
    }
    
    public UnitConverter(Context context, String unitAbbrev) {
        Unit unit = getFromAbbrev(unitAbbrev);
        this.mContext = context;
        mRes = mContext.getResources();

        switchUnit(unit);
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
        double rawValue = roundToDecimals(toUnit(value), DP);
        return "" + roundToDecimals(toUnit(value), DP) + mUnit.getAbbrev();
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
    
    public class Unit {
        String mName;
        String mAbbrev;
        double mValue;
        String mBigger;
        String mSmaller;
        
        public Unit(String name, String abbrev, double value, String bigger, String smaller) {
            mName = name;
            mAbbrev = abbrev;
            mValue = value;
            mBigger = bigger;
            mSmaller = smaller;
        }
        
        public String getName() {
            return mName;
        }
        
        public String getAbbrev() {
            return mAbbrev;
        }
        
        public double getValue() {
            return mValue;
        }
    }
}