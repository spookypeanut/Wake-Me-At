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

import java.util.HashMap;
import java.util.Map;

import android.content.Context;

public class UnitConverter
{
    Map <String, Double> mValues = new HashMap<String, Double>();
    Map <String, String> mAbbrev = new HashMap<String, String>();
    
    public UnitConverter() {

        
        String unit = "Metres";
        mValues.put(unit, 1.0);
        mAbbrev.put(unit, "m");
        
        unit = "Feet";
        mValues.put(unit, 0.3048);
        mAbbrev.put(unit, "ft");
    }
    
    public double convert(double value, String sourceUnit, String destUnit) {
        return value * mValues.get(sourceUnit) / mValues.get(destUnit);
    }
}