/*! Copyright (C) 2011-2012 All Rights Reserved
 *! Author : Sebastian Pichelhofer
 *! Description: Class for a single keyframe as used in timeline to create motion sequences 
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

	/*
	 * To create a new keyframe you need to specify the time it should have upon
	 * creation, all other parameters can be added later. A keyframe can have
	 * any number of key/value pairs as String/Float values.
	 */
	Keyframe(float time) {
		Time = time;
	}

	/*
	 * Change the time of a keyframe
	 */
	public void SetTime(float time) {
		Time = time;
	}

	/*
	 * Return whether the current keyframe has already been applied in the
	 * timeline. Traditionally when a timeline is executed the keyframes are
	 * applied at their desired time one after the other as the current time
	 * passes the keyframe time.
	 */
	public boolean IsApplied() {
		return Applied;
	}

	/*
	 * Return Time of the Keyframe
	 */
	public float GetTime() {
		return Time;
	}

	/*
	 * Return one keyframe parameter value by specifying the key as String
	 */
	public float GetParameter(String key) {
		return Parameters.get(key);
	}

	/*
	 * Return all keyframe parameters as key/value pairs in a hashmap
	 */
	public Map<String, Float> GetAllParameters() {
		return Parameters;
	}

	/*
	 * Does a particular key exist in the list of parameters of this keyframe
	 */
	public boolean HasKey(String key) {
		return Parameters.containsKey(key);
	}

	/*
	 * Set one key/value pair
	 */
	public void SetParameter(String key, float val) {
		Parameters.put(key, val);
	}

	/*
	 * In the GUI the user can select any keyframe for editing This function
	 * returns whether the current keyframe is currently selected for editing
	 */
	public boolean isHightlighted() {
		return Hightlighted;
	}

	/*
	 * Set the current keyframe to be highlighted in the GUI so it can be edited
	 */
	public void setHightlighted(boolean Hightlighted) {
		this.Hightlighted = Hightlighted;
	}
}
