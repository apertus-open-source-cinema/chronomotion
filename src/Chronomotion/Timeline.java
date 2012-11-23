/*! Copyright (C) 2012 All Rights Reserved
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

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Line2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;
import javax.swing.JPanel;

enum STATE {

	RUNNING, PAUSED, STOPPED
}

enum HEADPHASE {

	POSTSHOOTDELAY, MOVING, WAITING, STOPPED, RELEASINGSHUTTER
}

public class Timeline extends JPanel implements Runnable, java.io.Serializable {

	private Timeline me;
	private float CurrentTime = 0;
	private long StartTime = 0;
	private float PauseTime = 0;
	private float EvaluateTime = 0;
	private STATE CurrentState;
	private ArrayList<Keyframe> Keyframes = new ArrayList<Keyframe>();
	private float TargetValue = 0.0f; // TODO
	private Thread Updater;
	private Thread Worker;
	private int UpdatesPerSecond = 20;
	private boolean UpdaterRunning = false;
	private float TimelapseShutterPeriod = 5;
	private float PostShootDelay = 1;
	private Chronomotion Parent;
	private HEADPHASE CurrentPhase = HEADPHASE.STOPPED;
	private JLabel PhaseStateLabel;
	private String ActiveChannel;

	public Timeline() {
		me = this;
		CurrentState = STATE.STOPPED;
		Updater = new Thread(this);
		Worker = new Thread(this);

		ActiveChannel = "Tilt";

		// For Testing
		AddKeyFrame(0, "Tilt", 0.0f);
		AddKeyFrame(30, "Tilt", 50.0f);
		AddKeyFrame(50, "Tilt", 30.0f);

		AddKeyFrame(0, "Pan", 0.0f);
		AddKeyFrame(10, "Pan", 30.0f);
		AddKeyFrame(50, "Pan", 50.0f);

		this.OrderKeyframes();

		// Show evaluation values when clicking into the timeline
		addMouseListener(new java.awt.event.MouseAdapter() {

			@Override
			public void mouseClicked(java.awt.event.MouseEvent evt) {

				// Get coordinates
				int x = evt.getX();
				int y = evt.getY();

				float value = ((float) (x - margin) / me.getScaleX());

				// snapping
				value = Math.round(value / me.getTimelapseShutterPeriod()) * me.getTimelapseShutterPeriod();

				// Set the Evaluate Time
				me.setEvaluateTime(value);

				// Debug
				// Parent.GetParent().WriteLogtoConsole("mouse: " + value);

				// Update the GUI
				me.Redraw();
			}
		});
	}

	public void SetPhaseStateLabel(JLabel newPhaseStateLabel) {
		PhaseStateLabel = newPhaseStateLabel;
	}

	public void SetParent(Chronomotion parent) {
		this.Parent = parent;
	}

	/*
	 * Add one keyframe with one key(string)/value(float) pair
	 */
	public void AddKeyFrame(float Time, String key, float value) {
		Keyframe Frame = new Keyframe(Time);
		Frame.SetParameter(key, value);
		Keyframes.add(Frame);

		OrderKeyframes();
	}

	/*
	 * The timeline displays only keyframes of one channel at the same time
	 * Switching channels is traditionally done with a GUI element listing all
	 * available channels to choose from
	 */

	public void SetActiveChannel(String newchannel) {
		this.ActiveChannel = newchannel;
		Redraw();
	}

	public String GetActiveChannel() {
		return this.ActiveChannel;
	}

	/*
	 * Get Keyframes in the correct order again, sort by time. This should be
	 * called every time a new keyframe is added or an existing keyframe is
	 * moved on the time axis
	 */
	public void OrderKeyframes() {
		Comparator<Keyframe> comparator = new KeyFrameTimeComparator();
		java.util.Collections.sort(Keyframes, comparator);
		Redraw();
	}

	public void Update() {
		UpdateTargetValue();
	}

	public void UpdateTargetValue() {
		if (CurrentState == STATE.RUNNING) {
			if (StartTime != 0) {
				if (GetNextKeyframeIndex(this.ActiveChannel, GetCurrentTime()) > 0) {
					float PreviousKeyframeTime = GetTime(GetPreviousKeyframeIndex(this.ActiveChannel, GetCurrentTime()), this.ActiveChannel);
					float NextKeyframeTime = GetTime(GetNextKeyframeIndex(this.ActiveChannel, GetCurrentTime()), this.ActiveChannel);
					float delta_time = NextKeyframeTime - PreviousKeyframeTime;
					float time_factor_current_segment = (GetCurrentTime() - GetTime(GetPreviousKeyframeIndex(this.ActiveChannel, GetCurrentTime()), this.ActiveChannel)) / delta_time;
					float d = GetKeyframe(this.ActiveChannel, GetPreviousKeyframeIndex(this.ActiveChannel, GetCurrentTime())).GetParameter(this.ActiveChannel);

					float k = (GetKeyframe(this.ActiveChannel, GetNextKeyframeIndex(this.ActiveChannel, GetCurrentTime())).GetParameter(this.ActiveChannel) - GetKeyframe(this.ActiveChannel, GetPreviousKeyframeIndex(this.ActiveChannel, GetCurrentTime())).GetParameter(this.ActiveChannel)) / delta_time;
					TargetValue = k * time_factor_current_segment * delta_time + d;

					// Parent.WriteLogtoConsole("PreviousKeyframeTime = " +
					// PreviousKeyframeTime);
					// Parent.WriteLogtoConsole("NextKeyframeTime = " +
					// NextKeyframeTime);
					// Parent.WriteLogtoConsole("time_factor_current_segment = "
					// +
					// time_factor_current_segment);
					// Parent.WriteLogtoConsole("k = " + k);
					// Parent.WriteLogtoConsole("TargetPitch = " + TargetPitch);
					// Parent.WriteLogtoConsole("time_factor_current_segment = "
					// +
					// time_factor_current_segment);
				}
			}
		}
	}

	/*
	 * Return the value of the channel at the current time
	 */
	public float GetCurrentTargetValue(String channel) {
		// TODO
		return 0.0f;
	}

	/*
	 * Calculate the value of the graph of one channel at any given time. These
	 * are the values used to send to the connected pan/tilt head. Only linear
	 * interpolation is supported at this time. Bezier interpolation will follow
	 * in the future.
	 */
	public float GetTargetValue(float time, String channel) {
		float PreviousKeyframeTime = GetTime(GetPreviousKeyframeIndex(this.ActiveChannel, time), channel);
		float NextKeyframeTime = GetTime(GetNextKeyframeIndex(this.ActiveChannel, time), channel);
		float delta_time = NextKeyframeTime - PreviousKeyframeTime;

		float time_factor_current_segment = (time - GetTime(GetPreviousKeyframeIndex(this.ActiveChannel, time), channel)) / delta_time;
		float d = GetKeyframe(channel, GetPreviousKeyframeIndex(this.ActiveChannel, time)).GetParameter(channel);
		float k = (GetKeyframe(channel, GetNextKeyframeIndex(this.ActiveChannel, time)).GetParameter(channel) - GetKeyframe(channel, GetPreviousKeyframeIndex(this.ActiveChannel, time)).GetParameter(channel)) / delta_time;
		float ret = (k * time_factor_current_segment * delta_time + d);
		return ret;
	}

	/*
	 * Returns the current position in the timeline when it is being played,
	 * will return 0 if the timeline is not running
	 */
	public float GetCurrentTime() {
		if (StartTime == 0) {
			CurrentTime = 0;
		} else {
			CurrentTime = System.currentTimeMillis() - StartTime;
		}
		if (CurrentState == STATE.RUNNING) {
			// Update();
			return ((float) CurrentTime / 1000.0f);
		} else if (CurrentState == STATE.STOPPED) {
			return 0.0f;
		} else if (CurrentState == STATE.PAUSED) {
			return (float) (PauseTime - StartTime) / 1000.0f;
		} else {
			return 0.0f;
		}
	}

	/*
	 * Returns the next scheduled shutter release after the current time
	 * parameter
	 */
	public float GetNextShutterReleaseTime(float currenttime) {
		float temp = 0.0f;
		int limit = 10000;
		for (int i = 0; i < limit; i++) {
			if (i * this.TimelapseShutterPeriod > currenttime) {
				temp = i * this.TimelapseShutterPeriod;
				break;
			}
		}
		return temp;
	}

	/*
	 * Find the index of the next keyframe in the current active channel after
	 * the supplied time
	 */
	public int GetNextKeyframeIndex(String channel, float currenttime) {
		int tempindex = -1;
		float tempvalue = 0;
		// Find all occurances that are after the current time in the selected
		// channel
		for (int i = 0; i < this.GetNumberOfKeyframes(channel); i++) {
			if (currenttime < this.GetKeyframe(channel, i).GetTime()) {
				// store the lowest one
				if ((tempvalue > this.GetKeyframe(channel, i).GetTime()) || (tempvalue == 0)) {
					tempindex = i;
					tempvalue = this.GetKeyframe(channel, i).GetTime();
				}
			}
		}
		return tempindex;
	}

	/*
	 * Find the index of the previous keyframe in the selected Channel before
	 * the supplied time
	 */
	public int GetPreviousKeyframeIndex(String channel, float currenttime) {
		int tempindex = -1;
		float tempvalue = 0;
		// Find all occurrences that are before the current time
		for (int i = 0; i < this.GetNumberOfKeyframes(channel); i++) {
			if (currenttime >= this.GetKeyframe(channel, i).GetTime()) {
				// store the highest one
				if ((tempvalue < this.GetKeyframe(channel, i).GetTime()) || (tempvalue == 0)) {
					tempindex = i;
					tempvalue = this.GetKeyframe(channel, i).GetTime();
				}
			}
		}
		return tempindex;
	}

	/*
	 * Delete one keyframe providing its index TODO: select channel
	 */
	public void RemoveKeyframe(int index) {
		Keyframes.remove(index);
		Redraw();
	}

	/*
	 * When starting to run an animation the threads need to be started
	 */
	public void UpdaterStart() {
		if (!Updater.isAlive()) {
			Updater.start();
		}
		if (!Worker.isAlive()) {
			Worker.start();
		}
		UpdaterRunning = true;
	}

	/*
	 * Stop the Thread
	 */
	public void UpdaterStop() {
		UpdaterRunning = false;
	}

	/*
	 * Return state the remote head is currently in
	 */
	public HEADPHASE GetCurrentHeadPhase() {
		return this.CurrentPhase;
	}

	/*
	 * Return state the System is currently in.
	 */
	public STATE GetCurrentPhase() {
		return this.CurrentState;
	}

	/*
	 * Change the state of the Animation - this will trigger a whole range of
	 * things to happen.
	 */
	public void ChangeState(STATE newstate) {
		if ((CurrentState == STATE.STOPPED) && (newstate == STATE.RUNNING)) {
			// START FROM BEGINNING
			CurrentState = newstate;
			StartTime = System.currentTimeMillis();
			PauseTime = 0;
			this.ChangeHeadState(HEADPHASE.MOVING);
			this.UpdaterStart();
		} else if (newstate == STATE.STOPPED) {
			// STOP - RESET
			CurrentState = newstate;
			StartTime = 0;
			PauseTime = 0;
			this.UpdaterStop();
			this.Update();
			this.repaint();
		} else if ((CurrentState == STATE.RUNNING) && (newstate == STATE.RUNNING)) {
			// We missed a target - moving on
			CurrentState = newstate;
			StartTime = 0;
			PauseTime = 0;
			this.UpdaterStop();
			this.Update();
			this.repaint();
		} else {
			CurrentState = newstate;
		}
	}

	private int StopUpdateFrequency = 5;
	private int MovingUpdateFrequency = 50;
	private int ShutterReleaseUpdateFrequency = 100;
	private float NextTargetShutterReleaseTime = 0.0f;

	/*
	 * While animation is running update the state constantly
	 */
	public void run() {
		while (Thread.currentThread() == Updater) {
			if (UpdaterRunning) {
				Update();
				repaint();
				try {
					Thread.sleep((int) (1.0f / UpdatesPerSecond * 1000));
				} catch (InterruptedException ex) {
					Logger.getLogger(Timeline.class.getName()).log(Level.SEVERE, null, ex);
				}
			}
		}
		while (Thread.currentThread() == Worker) {
			if (UpdaterRunning) {
				if (CurrentPhase == HEADPHASE.STOPPED) {
					// Just wait - nothing to do
					try {
						Thread.sleep((int) (1.0f / StopUpdateFrequency * 1000));
					} catch (InterruptedException ex) {
						Logger.getLogger(Timeline.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
				if (CurrentPhase == HEADPHASE.MOVING) {
					// Check if target position has been reached
					int error_treshhold = 150;
					// int targetsteps = (int) ((this.GetParameter(
					// GetNextKeyframeIndex(GetCurrentTime()), "tilt")
					// * (float) Parent.MerlinController
					// .GetTotalSteps(AXIS.PAN) / 360.0f));
					// int delta_tilt_steps = Parent.MerlinController
					// .GetCurrentSteps(AXIS.TILT) - 8388608 - targetsteps;
					// if ((delta_tilt_steps < error_treshhold)) {
					// target reached
					// this.ChangeHeadState(HEADPHASE.WAITING);
					// }

					// we reached the keyframe time, likely the head was not
					// able to reach the position in time -> bad

					if (this.GetCurrentTime() > this.NextTargetShutterReleaseTime)
						this.ChangeHeadState(HEADPHASE.WAITING);

					try {
						Thread.sleep((int) (1.0f / MovingUpdateFrequency * 1000));
					} catch (InterruptedException ex) {
						Logger.getLogger(Timeline.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
				if (CurrentPhase == HEADPHASE.WAITING) {
					// Wait until the time is right for a shutter trigger
					if (NextTargetShutterReleaseTime <= this.GetCurrentTime()) {
						this.ChangeHeadState(HEADPHASE.RELEASINGSHUTTER);
					}
					try {
						Thread.sleep((int) (1.0f / ShutterReleaseUpdateFrequency * 1000));
					} catch (InterruptedException ex) {
						Logger.getLogger(Timeline.class.getName()).log(Level.SEVERE, null, ex);
					}
				}
				if (CurrentPhase == HEADPHASE.RELEASINGSHUTTER) {
					// after triggering shutter
					this.ChangeHeadState(HEADPHASE.POSTSHOOTDELAY);
				}
				if (CurrentPhase == HEADPHASE.POSTSHOOTDELAY) {
					// Just sleep until the PostShootDelay is over
					try {
						Thread.sleep((int) (this.getPostShootDelay() * 1000));
					} catch (InterruptedException ex) {
						Logger.getLogger(Timeline.class.getName()).log(Level.SEVERE, null, ex);
					}
					// Wait until the time is right for a shutter release
					this.ChangeHeadState(HEADPHASE.MOVING);
				}
			}
		}
	}

	/*
	 * Change the state of the remote head. This is the proper way to stop the
	 * head operation by setting it to HEADPHASE.STOPPED or to start an
	 * animation by setting it to HEADPHASE.MOVING
	 */
	public void ChangeHeadState(HEADPHASE newstate) {
		if ((this.GetCurrentHeadPhase().equals(HEADPHASE.STOPPED)) && (newstate == HEADPHASE.MOVING)) {
			// Animation Initial Start

			NextTargetShutterReleaseTime = GetNextShutterReleaseTime(GetCurrentTime());

			Parent.WriteLogtoConsole("Next Frame at " + NextTargetShutterReleaseTime + " seconds");
			Parent.WriteLogtoConsole("Next Frame Tilt: " + this.GetTargetValue(NextTargetShutterReleaseTime, "tilt"));
			Parent.WriteLogtoConsole("Next Frame Pan: " + this.GetTargetValue(NextTargetShutterReleaseTime, "pan"));

			// float NextShootParameter =
			// this.GetTargetValue(this.GetNextShutterReleaseTime(GetCurrentTime()),
			// this.ActiveChannel);
			// Parent.GetMerlinController().GotoPosition(AXIS.TILT,
			// NextShootParameter);

			Parent.WriteLogtoConsole("Changing State to: " + newstate);
			PhaseStateLabel.setText("Moving");
			CurrentPhase = HEADPHASE.MOVING;

		} else if ((this.GetCurrentHeadPhase().equals(HEADPHASE.POSTSHOOTDELAY)) && (newstate == HEADPHASE.MOVING)) {
			// loop continues

			NextTargetShutterReleaseTime = GetNextShutterReleaseTime(GetCurrentTime());

			Parent.WriteLogtoConsole("Next Frame at " + NextTargetShutterReleaseTime + " seconds");
			Parent.WriteLogtoConsole("Next Frame Tilt: " + this.GetTargetValue(NextTargetShutterReleaseTime, "tilt"));
			Parent.WriteLogtoConsole("Next Frame Pan: " + this.GetTargetValue(NextTargetShutterReleaseTime, "pan"));

			// float NextShootParameter =
			// this.GetTargetValue(this.GetNextShutterReleaseTime(GetCurrentTime()),
			// this.ActiveChannel);
			// Parent.GetMerlinController().GotoPosition(AXIS.TILT,
			// NextShootParameter);

		} else if (newstate == HEADPHASE.POSTSHOOTDELAY) {
			PhaseStateLabel.setText("Post Shoot Delay");
			CurrentPhase = HEADPHASE.POSTSHOOTDELAY;
			Parent.WriteLogtoConsole("Changing State to: " + newstate);

		} else if (newstate == HEADPHASE.RELEASINGSHUTTER) {
			CurrentPhase = HEADPHASE.RELEASINGSHUTTER;
			Parent.WriteLogtoConsole("Changing State to: " + newstate);
			PhaseStateLabel.setText("Triggering Shutter");
			Parent.WriteLogtoConsole("snap!");

			// TODO: twice --- why does it only work this way?
			Parent.GetMerlinController().TriggerShutter();
			Parent.GetMerlinController().TriggerShutter();

		} else if (newstate == HEADPHASE.WAITING) {
			PhaseStateLabel.setText("Waiting");
			CurrentPhase = HEADPHASE.WAITING;
			Parent.WriteLogtoConsole("Changing State to: " + newstate);

		} else if ((this.GetCurrentHeadPhase().equals(HEADPHASE.MOVING)) && (newstate == HEADPHASE.STOPPED)) {
			PhaseStateLabel.setText("Idle");
			Parent.WriteLogtoConsole("Changing State to: " + newstate);
			// TODO
		}
	}

	/*
	 * Redraw the GUI to display updated values
	 */
	public void Redraw() {
		Update();
		repaint();
	}

	/*
	 * In the GUI we can highlight a particular frame and display its
	 * coordinates. Also when editing a keyframe you will always want to edit
	 * the one you are currently editing thus the highlighted keyframe
	 */
	public void SetKeyframeHighlight(String channel, int selectedindex) {
		// Clear highlighted state for all keyframes to guarantee only a single
		// keyframe will be highlighted at a time.
		for (int i = 0; i < Keyframes.size(); i++) {
			Keyframes.get(i).setHightlighted(false);
		}
		// Set Highlighted
		GetKeyframe(channel, selectedindex).setHightlighted(true);

		// Make sure we actually see the newly highlighted keyframe
		Redraw();
	}

	/*
	 * Returns the total number of keyframes in this timeline
	 */
	public int GetNumberOfKeyframes() {
		return Keyframes.size();
	}

	/*
	 * Returns the number of keyframes in a specific channel in this timeline
	 */
	public int GetNumberOfKeyframes(String channel) {
		int count = 0;
		for (int i = 0; i < Keyframes.size(); i++) {
			if (Keyframes.get(i).HasKey(channel)) {
				count++;
			}
		}
		return count;
	}

	/*
	 * Return a specific keyframe from a particular channel with index
	 */
	public Keyframe GetKeyframe(String channel, int index) {
		int count = 0;

		// iterate through all keyframes that have a value in the specified
		// channel (key) return the element at index
		for (int i = 0; i < Keyframes.size(); i++) {
			if (Keyframes.get(i).HasKey(channel)) {
				if (count == index) {
					return Keyframes.get(i);
				}
				count++;
			}
		}

		// return null if the element does not exist
		return null;
	}

	/*
	 * Set time of a keyframe
	 */
	public void SetTime(String channel, int index, float newtime) {
		GetKeyframe(channel, index).SetTime(newtime);

		// Since the order can change due to the changed time reorder all
		// keyframes
		this.OrderKeyframes();
	}

	/*
	 * Get the time of a keyframe with index from the selected channel
	 */
	public float GetTime(int index, String channel) {
		if (index > this.GetNumberOfKeyframes(channel) - 1) {
			return -1.0f;
		} else if (index < 0) {
			return -1.0f;
		} else {
			return this.GetKeyframe(channel, index).GetTime();
		}
	}

	/*
	 * Get the time of a keyframe with index from the current active channel
	 */
	public float GetTime(int index) {
		return GetTime(index, this.ActiveChannel);
	}

	/*
	 * Set Parameter (key/value pair) of a keyframe
	 */
	public void SetParameter(String channel, int index, String key, float value) {
		GetKeyframe(channel, index).SetParameter(key, value);
	}

	/*
	 * Set Parameter (key/value pair) of a keyframe
	 */
	public void SetParameter(String channel, int index, String key, int value) {
		GetKeyframe(channel, index).SetParameter(key, value);
	}

	private int margin = 5;
	private int MarginTop = 25;
	private Color KeyframeRectangleColor = new Color(90, 90, 90);
	private Color GOTOIndicatorColor = new Color(100, 255, 120);
	private Color TimeBarColor = new Color(190, 190, 190);
	private Color ShutterReleaseCircleColor = new Color(90, 220, 90);
	private Color HightlightedKeyframeRectangleColor = new Color(255, 0, 0);
	private Color AxisColor = new Color(110, 110, 110);
	private Color LineColor = new Color(40, 40, 40);
	private Color CurrentTimeIndicatorColor = new Color(200, 40, 40);
	private Color EvaluationTimeIndicatorColor = new Color(40, 200, 40);
	private int KeyframeRectangleDimension = 4;
	private int ShutterCircleDimension = 8;
	private int GOTOIndicatorDimension = 4;
	private float ScaleX = 10;
	private float ScaleY = 10;

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		// Time Area
		g2.setColor(TimeBarColor);
		g2.fillRect(0, 0, this.getWidth(), 30);
		g2.setColor(AxisColor);
		// Horizontal Line
		g2.draw(new Line2D.Double(margin, MarginTop, this.getWidth() - margin, MarginTop));
		g2.drawString("Time", margin + 2, 12);

		// X Axis Indicators
		// Lets define the standard scale 1:1 with 10 seconds = 50 pixels width
		// 1 second = 5 pixels

		// There should not be more than one indicator each 50 pixels to prevent
		// making them so dense that you can't read them
		int number_of_indicators = (int) (this.getWidth() / 10 * getScaleX());

		int indicator = 0;
		int indicator_x = 0;
		int scale_decade = 1;
		if (10 / getScaleX() > this.getWidth() / 50) {
			scale_decade = 10;
		}
		for (int i = 0; i < number_of_indicators; i++) {
			// Indicator Line
			g2.draw(new Line2D.Double(indicator_x * getScaleX() + margin, MarginTop, indicator_x * getScaleX() + margin, MarginTop - 3));
			// Indicator Label
			g2.drawString(indicator + "s", margin - 5 + (indicator_x * getScaleX()), MarginTop - 5);
			indicator_x += 10 * scale_decade;
			indicator += 10 * scale_decade;
		}

		// Axis
		g2.setColor(AxisColor);
		g2.draw(new Line2D.Double(margin, this.getHeight() - margin, this.getWidth(), this.getHeight() - margin));
		g2.drawString("0", margin, this.getHeight() - margin - 2);
		g2.draw(new Line2D.Double(margin, this.getHeight() - margin - (getScaleY() * 1000), this.getWidth(), this.getHeight() - margin - (getScaleY() * 1000)));
		g2.drawString("1000", margin, this.getHeight() - margin - (getScaleY() * 1000) - 2);

		// KeyFrame Lines
		g2.setColor(LineColor);
		g2.setStroke(new BasicStroke(2.0f));
		for (int i = 0; i < this.GetNumberOfKeyframes(this.ActiveChannel) - 1; i++) {
			int X1 = (int) (margin + this.GetKeyframe(this.ActiveChannel, i).GetTime() * getScaleX());
			int Y1 = (int) (this.getHeight() - margin - (this.GetKeyframe(this.ActiveChannel, i).GetParameter(this.ActiveChannel) * getScaleY()));
			int X2 = (int) (margin + this.GetKeyframe(this.ActiveChannel, i + 1).GetTime() * getScaleX());
			int Y2 = (int) (this.getHeight() - margin - (this.GetKeyframe(this.ActiveChannel, i + 1).GetParameter(this.ActiveChannel) * getScaleY()));
			g2.draw(new Line2D.Double(X1, Y1, X2, Y2));
		}

		// Keyframe dots
		for (int i = 0; i < this.GetNumberOfKeyframes(this.ActiveChannel); i++) {
			int X = (int) (margin + (this.GetKeyframe(this.ActiveChannel, i).GetTime() * getScaleX()) - KeyframeRectangleDimension / 2);
			int Y = (int) (this.getHeight() - margin - (this.GetKeyframe(this.ActiveChannel, i).GetParameter(this.ActiveChannel) * getScaleY())) - KeyframeRectangleDimension / 2;
			if (this.GetKeyframe(this.ActiveChannel, i).isHightlighted()) {
				g2.setColor(HightlightedKeyframeRectangleColor);
				g2.drawString(this.GetKeyframe(this.ActiveChannel, i).GetParameter(this.ActiveChannel) + "", X + 3, Y - 4);
			} else {
				g2.setColor(KeyframeRectangleColor);
			}
			g2.fillRect(X, Y, KeyframeRectangleDimension, KeyframeRectangleDimension);
		}

		// Frame GOTO Indicators
		g2.setColor(GOTOIndicatorColor);
		for (int i = 0; i < 20; i++) {
			int X = (int) (margin + (i * this.getTimelapseShutterPeriod() * getScaleX()) - GOTOIndicatorDimension / 2);
			// int Y = (int) ((this.getHeight() - margin -
			// (this.GetTargetValue(i * this.getTimelapseShutterPeriod(),
			// this.ActiveChannel) * getScaleY())) - GOTOIndicatorDimension /
			// 2);
			// g2.fillOval(X, Y, GOTOIndicatorDimension,
			// GOTOIndicatorDimension);
		}

		// Shutter Release Circles
		g2.setColor(ShutterReleaseCircleColor);
		for (int i = 1; i < 20; i++) { // TODO 20 is just a placeholder for now
			int X = (int) (margin + (i * this.getTimelapseShutterPeriod() * getScaleX()) - ShutterCircleDimension / 2);
			int Y = (int) (MarginTop + 5);
			g2.fillOval(X, Y, ShutterCircleDimension, ShutterCircleDimension);
		}

		// Current Time Indicator
		g2.setColor(CurrentTimeIndicatorColor);
		g2.setStroke(new BasicStroke(1.0f));
		int X = (int) (margin + getScaleX() * this.GetCurrentTime());
		g2.draw(new Line2D.Double(X, margin, X, this.getHeight() - margin));

		// Evaluation Time Indicator
		g2.setColor(EvaluationTimeIndicatorColor);
		g2.setStroke(new BasicStroke(1.0f));
		X = (int) (margin + getScaleX() * this.getEvaluateTime());
		g2.draw(new Line2D.Double(X, margin, X, this.getHeight() - margin));

		// Evaluation Time Value
		X = (int) (margin + getScaleX() * this.getEvaluateTime());
		int Y = (int) (this.getHeight() - margin - this.GetTargetValue(this.getEvaluateTime(), this.GetActiveChannel()) * getScaleY());
		g2.setColor(EvaluationTimeIndicatorColor);
		g2.drawString(this.GetTargetValue(this.getEvaluateTime(), this.GetActiveChannel()) + "", X + 3, Y - 4);
		g2.fillRect(X-1, Y, KeyframeRectangleDimension-1, KeyframeRectangleDimension-1);
	}

	/*
	 * Scales in both X and Y direction allow the timeline to be viewed at any
	 * zoom level independently for both axis
	 */
	public float getScaleX() {
		return ScaleX;
	}

	public void setScaleX(float ScaleX) {
		this.ScaleX = ScaleX;
	}

	public float getScaleY() {
		return ScaleY;
	}

	public void setScaleY(float ScaleY) {
		this.ScaleY = ScaleY;
	}

	public float getTimelapseShutterPeriod() {
		return TimelapseShutterPeriod;
	}

	public void setTimelapseShutterPeriod(float TimelapseShutterPeriod) {
		this.TimelapseShutterPeriod = TimelapseShutterPeriod;
	}

	public float getPostShootDelay() {
		return PostShootDelay;
	}

	public void setPostShootDelay(float PostShootDelay) {
		this.PostShootDelay = PostShootDelay;
	}

	/*
	 * The evaluate time is when you click into the timeline to move the current
	 * time slider to a custom position When the timeline is executed the time
	 * starts running from zero though
	 */
	public float getEvaluateTime() {
		return EvaluateTime;
	}

	public void setEvaluateTime(float EvaluateTime) {
		this.EvaluateTime = EvaluateTime;
	}

	public void SetEvaluateTime(long EvalTime) {
		this.setEvaluateTime(EvalTime);
	}
}
