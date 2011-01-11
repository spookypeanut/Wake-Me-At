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
import android.content.res.Resources;

public class UnitConverter
{
    Context mContext;
    Resources mRes;
    
    int mUnit;

    String[] mNames;
    double[] mValues;
    String[] mAbbrevs;
    
    String mName;
    double mValue;
    String mAbbrev;
    
    static final int DP = 2;
    
    private double[] stringArrayToDoubleArray(String[] inputArray) {
        double[] returnArray = new double[inputArray.length];
        int i;
        for(i=0; i < inputArray.length; i++) {
            returnArray[i] = Double.valueOf(inputArray[i]);
        }
        return returnArray;
    }
    
    public UnitConverter(Context context, int unit) {
        this.mContext = context;
        mRes = mContext.getResources();

        mNames = mRes.getStringArray(R.array.unit_names);
        mValues = stringArrayToDoubleArray(mRes.getStringArray(R.array.unit_values));
        mAbbrevs = mRes.getStringArray(R.array.unit_abbrevs);

        switchUnit(unit);
    }
    
    
    public String getName() {
        return mName;
    }
    
    public String getAbbrev() {
        return mAbbrev;
    }
    
    public double toMetres(double value) {
        return convert(value, mUnit, 0);
    }
    
    public double toUnit(double value) {
        return convert(value, 0, mUnit);
    }
    
    public String out(double value) {
        // 0 is always metres (used internally)
        return "" + roundToDecimals(toUnit(value), DP) + mAbbrev;
    }
    
    public void switchUnit(int unit) {
        mUnit = unit;

        mName = mNames[unit];
        mValue = mValues[unit];
        mAbbrev = mAbbrevs[unit];
    }
    
    public static double roundToDecimals(double d, int c) {
        int temp = (int) (d * Math.pow(10, c));
        return (double) temp / Math.pow(10, c);
    }
    
    public double convert(double value, int sourceUnit, int destUnit) {
        return value * mValues[sourceUnit] / mValues[destUnit];
    }
}