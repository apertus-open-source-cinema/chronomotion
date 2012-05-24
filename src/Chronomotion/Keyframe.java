/*! Copyright (C) 2011 All Rights Reserved
 *! Author : Sebastian Pichelhofer
 *! Description:
-----------------------------------------------------------------------------**
 *!
 *!  This program is free software: you can redistribute it and/or modify
 *!  it under the terms of the GNU General Public License as published by
 *!  the Free Software Foundation, either version 3 of the License, or
 *!  (at your option) any later version.
 *!
 *!  This program is distributed in the hope that it will be useful,
 *!  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *!  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *!  GNU General Public License for more details.
 *!
 *!  You should have received a copy of the GNU General Public License
 *!  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *!
 *!  It means that the program's users have the four essential freedoms:
 *!
 *!   * The freedom to run the program, for any purpose (freedom 0).
 *!   * The freedom to study how the program works, and change it to make it do what you wish (freedom 1). Access to the source code is a precondition for this.
 *!   * The freedom to redistribute copies so you can help your neighbor (freedom 2).
 *!   * The freedom to distribute copies of your modified versions to others (freedom 3). By doing this you can give the whole community a chance to benefit from your changes. Access to the source code is a precondition for this.

-----------------------------------------------------------------------------**/
package Chronomotion;

import java.util.HashMap;
import java.util.Map;

public class Keyframe {

    private float Time;
    private Map<String, Float> Parameters = new HashMap<String, Float>();
    private boolean Applied = false;
    private boolean Hightlighted = false;

    Keyframe() {
    }

    Keyframe(float time) {
        Time = time;
    }

    public void SetTime(float time) {
        Time = time;
    }

    public boolean IsApplied() {
        return Applied;
    }

    public float GetTime() {
        return Time;
    }

    public float GetParameter(String key) {
        return Parameters.get(key);
    }

    public void SetParameter(String key, float val) {
        Parameters.put(key, val);
    }

    public boolean isHightlighted() {
        return Hightlighted;
    }

    public void setHightlighted(boolean Hightlighted) {
        this.Hightlighted = Hightlighted;
    }  
}
