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
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import javax.swing.JButton;


enum STATE {

	RUNNING, PAUSED, STOPPED
}

enum HEADPHASE {

	POSTSHOOTDELAY, MOVING, WAITING, STOPPED, RELEASINGSHUTTER
}

public class Timeline extends JPanel implements Runnable, java.io.Serializable, MouseListener, MouseMotionListener,
		KeyListener {

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
	private MainWindow Mainwindow;
	private HEADPHASE CurrentPhase = HEADPHASE.STOPPED;
	private JLabel PhaseStateLabel;
	private String ActiveChannel;
	private int dragIndex = NOT_DRAGGING;
	private final static int DRAG_THRESHHOLD = 5;
	private final static int NOT_DRAGGING = -1;

	public Timeline() {
		me = this;
		CurrentState = STATE.STOPPED;
		Updater = new Thread(this);
		Worker = new Thread(this);
		addMouseListener(this);
		addMouseMotionListener(this);

		ActiveChannel = "Tilt";

		// For Testing
		Keyframe K1 = new Keyframe(0);
		K1.SetParameter("Tilt", 0.0f);
		K1.SetParameter("Bezier-X", 500.0f);
		K1.SetParameter("Bezier-Y", 0.0f);
		Keyframes.add(K1);

		Keyframe K2 = new Keyframe(1500);
		K2.SetParameter("Tilt", -30.0f);
		K2.SetParameter("Bezier-X", 500.0f);
		K2.SetParameter("Bezier-Y", 0.0f);
		Keyframes.add(K2);

		Keyframe K3 = new Keyframe(3000);
		K3.SetParameter("Tilt", 0.0f);
		K3.SetParameter("Bezier-X", 500.0f);
		K3.SetParameter("Bezier-Y", 0.0f);
		Keyframes.add(K3);

		Keyframe K4 = new Keyframe(0);
		K4.SetParameter("Pan", 0.0f);
		K4.SetParameter("Bezier-X", 500.0f);
		K4.SetParameter("Bezier-Y", 0.0f);
		Keyframes.add(K4);

		Keyframe K6 = new Keyframe(3000);
		K6.SetParameter("Pan", 60.0f);
		K6.SetParameter("Bezier-X", 500.0f);
		K6.SetParameter("Bezier-Y", 0.0f);
		Keyframes.add(K6);

		this.OrderKeyframes();

		// To allow keyboard interaction
		setFocusable(true);
		addKeyListener(this);

		// Show evaluation values when clicking into the timeline
		addMouseListener(new java.awt.event.MouseAdapter() {

			@Override
			public void mousePressed(java.awt.event.MouseEvent evt) {

				// Get coordinates
				int x = evt.getX();
				int y = evt.getY();

				float value = ((float) (x - margin) / me.getScaleX());

				// Snapping
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

	/*
	 * Set Chronomotion Parent Class to allow setting parameters or using
	 * functions from external classes
	 */
	public void SetParent(Chronomotion parent) {
		this.Parent = parent;
	}

	/*
	 * Set Chronomotion Mainwindow Class to allow altering the GUI in which the
	 * timeline is embedded in
	 */
	public void SetMainWindow(MainWindow MainWindow) {
		this.Mainwindow = MainWindow;
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
					float PreviousKeyframeTime = GetTime(this.ActiveChannel, GetPreviousKeyframeIndex(this.ActiveChannel, GetCurrentTime()));
					float NextKeyframeTime = GetTime(this.ActiveChannel, GetNextKeyframeIndex(this.ActiveChannel, GetCurrentTime()));
					float delta_time = NextKeyframeTime - PreviousKeyframeTime;
					float time_factor_current_segment = (GetCurrentTime() - GetTime(this.ActiveChannel, GetPreviousKeyframeIndex(this.ActiveChannel, GetCurrentTime()))) / delta_time;
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

		float PreviousKeyframeTime = GetTime(channel, GetPreviousKeyframeIndex(channel, time));
		float NextKeyframeTime = GetTime(channel, GetNextKeyframeIndex(channel, time));
		float delta_time = NextKeyframeTime - PreviousKeyframeTime;

		// float time_factor_current_segment = (time - GetTime(channel,
		// GetPreviousKeyframeIndex(channel, time))) / delta_time;

		// float Y1 = GetKeyframe(channel, GetPreviousKeyframeIndex(channel,
		// time)).GetParameter(channel);

		Point2D.Double P1 = new Point2D.Double(this.GetKeyframe(channel, GetPreviousKeyframeIndex(channel, time)).GetTime(), this.GetKeyframe(channel, GetPreviousKeyframeIndex(channel, time)).GetParameter(channel));
		Point2D.Double P2 = new Point2D.Double(this.GetKeyframe(channel, GetNextKeyframeIndex(channel, time)).GetTime(), this.GetKeyframe(channel, GetNextKeyframeIndex(channel, time)).GetParameter(channel));

		Point2D.Double ctrl1 = new Point2D.Double(P1.x + this.GetKeyframe(channel, GetPreviousKeyframeIndex(channel, time)).GetParameter("Bezier-X"), P1.y + (this.GetKeyframe(channel, GetPreviousKeyframeIndex(channel, time)).GetParameter("Bezier-Y")));
		Point2D.Double ctrl2 = new Point2D.Double(P2.x - this.GetKeyframe(channel, GetNextKeyframeIndex(channel, time)).GetParameter("Bezier-X"), P2.y - (this.GetKeyframe(channel, GetNextKeyframeIndex(channel, time)).GetParameter("Bezier-Y")));

		/*
		 * float k = (GetKeyframe(channel, GetNextKeyframeIndex(channel,
		 * time)).GetParameter(channel) - GetKeyframe(channel,
		 * GetPreviousKeyframeIndex(channel, time)).GetParameter(channel)) /
		 * delta_time; float ret = (k * time_factor_current_segment * delta_time
		 * + d); return ret;
		 */

		// Bezier
		CubicCurve2D.Double cubicCurve = new CubicCurve2D.Double(P1.x, P1.y, ctrl1.x, ctrl1.y, ctrl2.x, ctrl2.y, P2.x, P2.y);
		// cubicCurve.g
		// cubicCurve.
		Point2D point;

		/*
		 * double target_precision = 3; double current_precision = 0; double
		 * threshold = 0; int iterator = 0; while (current_precision <
		 * target_precision) { while (iterator * Math.pow(10,
		 * -current_precision) <= 1) { point = Bezierutils.pointOnCurve(iterator
		 * * Math.pow(10, -current_precision), cubicCurve, null); } iterator++;
		 * if (threshold < Math.abs(point.getX() - time)){ current_precision } }
		 */
		double temp = 10;
		double accuracy = 999999;
		for (int i = 0; i < (100 * delta_time); i++) {
			point = pointOnCurve((i / (100 * delta_time)), cubicCurve);
			if (Math.abs(accuracy) > Math.abs(time - point.getX())) {
				temp = point.getY();
				accuracy = Math.abs(time - point.getX());
			}
		}

		return (float) temp;
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

					/*
					 * The following block tries to implement semiautomatic
					 * target tracking where instead of a GOTO command to the
					 * head the position is controlled and changed by the
					 * Chronomotion software. This will result in more accurate
					 * position finding but a longer movement phase
					 * 
					 * // Check if target position has been reached // we can't
					 * reach the target 100% so this is the threshold // which
					 * is considered "good enough" int error_threshold = 150;
					 * int targetsteps = (int)
					 * ((this.GetParameter(GetNextKeyframeIndex
					 * (GetCurrentTime()), "Tilt") * (float)
					 * Parent.MerlinController.GetTotalSteps(AXIS.PAN) /
					 * 360.0f)); int delta_tilt_steps =
					 * Parent.MerlinController.GetCurrentSteps(AXIS.TILT) -
					 * 8388608 - targetsteps;
					 * 
					 * if ((delta_tilt_steps < error_threshold)) { // target
					 * reached this.ChangeHeadState(HEADPHASE.WAITING); }
					 */

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
			Parent.WriteLogtoConsole("Next Frame Pan: " + this.GetTargetValue(NextTargetShutterReleaseTime, "Pan"));
			Parent.WriteLogtoConsole("Next Frame Tilt: " + this.GetTargetValue(NextTargetShutterReleaseTime, "Tilt"));

			float NextShootParameter_Pan = this.GetTargetValue(NextTargetShutterReleaseTime, "Pan");
			float NextShootParameter_Tilt = this.GetTargetValue(NextTargetShutterReleaseTime, "Tilt");
			Parent.GetMerlinController().GotoPosition(AXIS.PAN, NextShootParameter_Pan);
			Parent.GetMerlinController().GotoPosition(AXIS.TILT, NextShootParameter_Tilt);

			Parent.WriteLogtoConsole("Changing State to: " + newstate);
			PhaseStateLabel.setText("Moving");
			CurrentPhase = HEADPHASE.MOVING;

		} else if ((this.GetCurrentHeadPhase().equals(HEADPHASE.POSTSHOOTDELAY)) && (newstate == HEADPHASE.MOVING)) {
			// loop continues

			NextTargetShutterReleaseTime = GetNextShutterReleaseTime(GetCurrentTime());

			Parent.WriteLogtoConsole("Next Frame at " + NextTargetShutterReleaseTime + " seconds");
			Parent.WriteLogtoConsole("Next Frame Pan: " + this.GetTargetValue(NextTargetShutterReleaseTime, "Pan"));
			Parent.WriteLogtoConsole("Next Frame Tilt: " + this.GetTargetValue(NextTargetShutterReleaseTime, "Tilt"));

			float NextShootParameter_Pan = this.GetTargetValue(NextTargetShutterReleaseTime, "Pan");
			float NextShootParameter_Tilt = this.GetTargetValue(NextTargetShutterReleaseTime, "Tilt");
			Parent.GetMerlinController().GotoPosition(AXIS.PAN, NextShootParameter_Pan);
			Parent.GetMerlinController().GotoPosition(AXIS.TILT, NextShootParameter_Tilt);

			Parent.WriteLogtoConsole("Changing State to: " + newstate);
			PhaseStateLabel.setText("Moving");
			CurrentPhase = HEADPHASE.MOVING;

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
	public float GetTime(String channel, int index) {
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
		return GetTime(this.ActiveChannel, index);
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

	public Point2D pointOnCurve(double t, CubicCurve2D curve) {
		Point2D.Double resultHere;
		if (null != curve) {
			double x1 = curve.getX1(), y1 = curve.getY1();
			double cx1 = curve.getCtrlX1(), cy1 = curve.getCtrlY1();
			double cx2 = curve.getCtrlX2(), cy2 = curve.getCtrlY2();
			double x2 = curve.getX2(), y2 = curve.getY2();
			// Coefficients of the parametric representation of the cubic
			double ax = cx1 - x1, ay = cy1 - y1;
			double bx = cx2 - cx1 - ax, by = cy2 - cy1 - ay;
			double cx = x2 - cx2 - ax - bx - bx; // instead of ...-ax-2*bx. Does
													// it worth?
			double cy = y2 - cy2 - ay - by - by;

			double x = x1 + (t * ((3 * ax) + (t * ((3 * bx) + (t * cx)))));
			double y = y1 + (t * ((3 * ay) + (t * ((3 * by) + (t * cy)))));

			resultHere = new Point2D.Double(x, y);
		} else {
			resultHere = null;
		}
		return resultHere;
	}

	Point2D.Double Transform2Screen(Point2D Input) {
		double x = -this.getOffsetX() + (margin + Input.getX() * getScaleX());
		double y = this.getOffsetY() + (this.getHeight() - margin - (Input.getY() * getScaleY()));
		Point2D.Double temp = new Point2D.Double(x, y);
		return temp;
	}

	Point2D.Float Transform2Screen(float X, float Y) {
		float x = -this.getOffsetX() + (margin + X * getScaleX());
		float y = this.getOffsetY() + (this.getHeight() - margin - (Y * getScaleY()));
		Point2D.Float temp = new Point2D.Float(x, y);
		return temp;
	}

	private int margin = 5;
	private int MarginTop = 25;
	private Color KeyframeRectangleColor = new Color(90, 90, 90);
	private Color GOTOIndicatorColor = new Color(100, 255, 120);
	private Color TimeBarColor = new Color(190, 190, 190);
	private Color BackgroundGradientStartColor = new Color(190, 190, 190);
	private Color BackgroundGradientEndColor = new Color(240, 240, 240);
	private Color ShutterReleaseCircleColor = new Color(158, 193, 216);
	private Color HightlightedKeyframeRectangleColor = new Color(47, 61, 129);
	private Color AxisColor = new Color(110, 110, 110);
	private Color LineColor = new Color(40, 40, 40);
	private Color BezierTangentColor = new Color(80, 80, 80);
	private Color CurrentTimeIndicatorColor = new Color(200, 40, 40);
	private Color EvaluationTimeIndicatorColor = new Color(58, 68, 118);
	private int KeyframeRectangleDimension = 4;
	private int ShutterCircleDimension = 8;
	private int GOTOIndicatorDimension = 4;
	private float ScaleX = 5;
	private float ScaleY = 5;
	private float OffsetX = 0;
	private float OffsetY = 0;

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		Graphics2D g2 = (Graphics2D) g;
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		// 180 to -180° area Background Gradient
		// Positive Area
		GradientPaint gradient = new GradientPaint(Transform2Screen(0, 0).x, Transform2Screen(0, 0).y, BackgroundGradientEndColor, Transform2Screen(0, 0).x, Transform2Screen(0, 180).y, BackgroundGradientStartColor, true);
		g2.setPaint(gradient);
		g2.fillRect((int) Transform2Screen(0, 0).x, (int) Transform2Screen(0, 180).y, this.getWidth(), (int) (180 * getScaleY()));
		g2.setPaint(BackgroundGradientStartColor);
		g2.draw(new Line2D.Double(Transform2Screen(0, 180).x, Transform2Screen(0, 180).y, Transform2Screen(0, 180).x + this.getWidth(), Transform2Screen(0, 180).y));
		// Negative Area
		GradientPaint gradient2 = new GradientPaint(Transform2Screen(0, 0).x, Transform2Screen(0, 0).y, BackgroundGradientEndColor, Transform2Screen(0, 0).x, Transform2Screen(0, -180).y, BackgroundGradientStartColor, true);
		g2.setPaint(gradient2);
		g2.fillRect((int) Transform2Screen(0, 0).x, (int) Transform2Screen(0, 0).y, this.getWidth(), (int) (180 * getScaleY()));
		g2.setPaint(BackgroundGradientStartColor);
		g2.draw(new Line2D.Double(Transform2Screen(0, -180).x, Transform2Screen(0, -180).y, Transform2Screen(0, -180).x + this.getWidth(), Transform2Screen(0, -180).y));

		// "180°" Labels
		g2.drawString("180°", (int) Transform2Screen(0, 180).x + 2, (int) Transform2Screen(0, 180).y - 2);
		g2.setPaint(BackgroundGradientEndColor);
		g2.drawString("-180°", (int) Transform2Screen(0, -180).x + 2, (int) Transform2Screen(0, -180).y - 2);

		// Time Bar
		g2.setColor(TimeBarColor);
		g2.fillRect(0, 0, this.getWidth(), 30);
		g2.setColor(AxisColor);
		g2.draw(new Line2D.Double(margin, MarginTop, this.getWidth() - margin, MarginTop));
		g2.drawString("Time", margin + 2, 12);

		// X Axis Indicators
		// Lets define the standard scale 1:1 with 10 seconds = 50 pixels width
		// 1 second = 5 pixels - TODO: verify

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
			g2.draw(new Line2D.Double(-(int) this.getOffsetX() + indicator_x * getScaleX() + margin, MarginTop, -(int) this.getOffsetX() + indicator_x * getScaleX() + margin, MarginTop - 3));
			// Indicator Label
			g2.drawString(indicator + "s", -(int) this.getOffsetX() + margin - 5 + (indicator_x * getScaleX()), MarginTop - 5);
			indicator_x += 10 * scale_decade;
			indicator += 10 * scale_decade;
		}

		// Axis
		g2.setColor(AxisColor);
		g2.draw(new Line2D.Double(-(int) this.getOffsetX() + margin, (int) this.getOffsetY() + this.getHeight() - margin, this.getWidth(), (int) this.getOffsetY() + this.getHeight() - margin));

		// "0" Label
		g2.drawString("0", -(int) this.getOffsetX() + margin, (int) this.getOffsetY() + this.getHeight() - margin - 2);

		// Small indicator for "0"
		g2.draw(new Line2D.Double(-(int) this.getOffsetX() + margin, (int) this.getOffsetY() + this.getHeight() - margin - (getScaleY() * 1000), -(int) this.getOffsetX() + this.getWidth(), (int) this.getOffsetY() + this.getHeight() - margin - (getScaleY() * 1000)));

		// Animation Lines
		for (int i = 0; i < this.GetNumberOfKeyframes(this.ActiveChannel) - 1; i++) {
			g2.setColor(LineColor);
			g2.setStroke(new BasicStroke(2.0f));

			// Bezier
			Point2D.Double P1 = new Point2D.Double(-(int) this.getOffsetX() + (int) (margin + this.GetKeyframe(this.ActiveChannel, i).GetTime() * getScaleX()), (int) this.getOffsetY() + (int) (this.getHeight() - margin - (this.GetKeyframe(this.ActiveChannel, i).GetParameter(this.ActiveChannel) * getScaleY())));
			Point2D.Double P2 = new Point2D.Double(-(int) this.getOffsetX() + (int) (margin + this.GetKeyframe(this.ActiveChannel, i + 1).GetTime() * getScaleX()), (int) this.getOffsetY() + (int) (this.getHeight() - margin - (this.GetKeyframe(this.ActiveChannel, i + 1).GetParameter(this.ActiveChannel) * getScaleY())));

			Point2D.Double ctrl1 = new Point2D.Double(P1.x + this.GetKeyframe(this.ActiveChannel, i).GetParameter("Bezier-X") * getScaleX(), P1.y - (this.GetKeyframe(this.ActiveChannel, i).GetParameter("Bezier-Y") * getScaleY()));
			Point2D.Double ctrl2 = new Point2D.Double(P2.x - this.GetKeyframe(this.ActiveChannel, i + 1).GetParameter("Bezier-X") * getScaleX(), P2.y + (this.GetKeyframe(this.ActiveChannel, i + 1).GetParameter("Bezier-Y") * getScaleY()));

			CubicCurve2D.Double cubicCurve = new CubicCurve2D.Double(P1.x, P1.y, ctrl1.x, ctrl1.y, ctrl2.x, ctrl2.y, P2.x, P2.y);

			g2.draw(cubicCurve);
		}

		// Keyframe Dots
		for (int i = 0; i < this.GetNumberOfKeyframes(this.ActiveChannel); i++) {
			int X = -(int) this.getOffsetX() + (int) (margin + (this.GetKeyframe(this.ActiveChannel, i).GetTime() * getScaleX()) - KeyframeRectangleDimension / 2);
			int Y = (int) this.getOffsetY() + (int) (this.getHeight() - margin - (this.GetKeyframe(this.ActiveChannel, i).GetParameter(this.ActiveChannel) * getScaleY())) - KeyframeRectangleDimension / 2;
			if (this.GetKeyframe(this.ActiveChannel, i).isHightlighted()) {
				g2.setColor(HightlightedKeyframeRectangleColor);
				g2.drawString(this.GetKeyframe(this.ActiveChannel, i).GetParameter(this.ActiveChannel) + "", X + 3, Y - 4);
			} else {
				g2.setColor(KeyframeRectangleColor);
			}
			g2.fillRect(X, Y, KeyframeRectangleDimension, KeyframeRectangleDimension);
		}

		// Keyframe Bezier Dots
		for (int i = 0; i < this.GetNumberOfKeyframes(this.ActiveChannel); i++) {
			// First and last keyframe only has one bezier point, all others
			// have two
			if ((i == 0) || (i == this.GetNumberOfKeyframes(this.ActiveChannel) - 1)) {
				Point2D.Double P1 = new Point2D.Double(-(int) this.getOffsetX() + (int) (margin + this.GetKeyframe(this.ActiveChannel, i).GetTime() * getScaleX()), (int) this.getOffsetY() + (int) (this.getHeight() - margin - (this.GetKeyframe(this.ActiveChannel, i).GetParameter(this.ActiveChannel) * getScaleY())));
				Point2D.Double ctrl1 = new Point2D.Double(P1.x + this.GetKeyframe(this.ActiveChannel, i).GetParameter("Bezier-X") * getScaleX(), P1.y + (this.GetKeyframe(this.ActiveChannel, i).GetParameter("Bezier-Y") * getScaleY()));
				if (this.GetKeyframe(this.ActiveChannel, i).isHightlighted()) {
					g2.setColor(HightlightedKeyframeRectangleColor);
					g2.fillOval((int) ctrl1.x, (int) ctrl1.y, KeyframeRectangleDimension, KeyframeRectangleDimension);
				}
			} else {
				// these keyframes have two bezier control points
				Point2D.Double P1 = new Point2D.Double(-(int) this.getOffsetX() + (int) (margin + this.GetKeyframe(this.ActiveChannel, i).GetTime() * getScaleX()), (int) this.getOffsetY() + (int) (this.getHeight() - margin - (this.GetKeyframe(this.ActiveChannel, i).GetParameter(this.ActiveChannel) * getScaleY())));
				Point2D.Double ctrl1 = new Point2D.Double(P1.x + this.GetKeyframe(this.ActiveChannel, i).GetParameter("Bezier-X") * getScaleX(), P1.y - (this.GetKeyframe(this.ActiveChannel, i).GetParameter("Bezier-Y") * getScaleY()));
				Point2D.Double ctrl2 = new Point2D.Double(P1.x - this.GetKeyframe(this.ActiveChannel, i).GetParameter("Bezier-X") * getScaleX(), P1.y + (this.GetKeyframe(this.ActiveChannel, i).GetParameter("Bezier-Y") * getScaleY()));
				// Point2D.Double ctrl1 = new Point2D.Double(-(int)
				// this.getOffsetX() + (int) (margin +
				// this.GetKeyframe(this.ActiveChannel,
				// i).GetParameter("Bezier-X"))
				// * getScaleX(), (int) this.getOffsetY() + (int)
				// (this.getHeight()
				// - margin - (this.GetKeyframe(this.ActiveChannel,
				// i).GetParameter("Bezier-Y") * getScaleY()))); // Control
				if (this.GetKeyframe(this.ActiveChannel, i).isHightlighted()) {
					g2.setColor(HightlightedKeyframeRectangleColor);
					g2.fillOval((int) ctrl1.x - KeyframeRectangleDimension / 2, (int) ctrl1.y - KeyframeRectangleDimension / 2, KeyframeRectangleDimension, KeyframeRectangleDimension);
					g2.fillOval((int) ctrl2.x - KeyframeRectangleDimension / 2, (int) ctrl2.y - KeyframeRectangleDimension / 2, KeyframeRectangleDimension, KeyframeRectangleDimension);
				}
			}
		}

		// Keyframe Bezier Lines
		g2.setColor(BezierTangentColor);
		g2.setStroke(new BasicStroke(1.0f));
		for (int i = 0; i < this.GetNumberOfKeyframes(this.ActiveChannel) - 1; i++) {
			if (this.GetKeyframe(this.ActiveChannel, i).isHightlighted()) {
				Point2D.Double P1 = new Point2D.Double(-(int) this.getOffsetX() + (int) (margin + this.GetKeyframe(this.ActiveChannel, i).GetTime() * getScaleX()), (int) this.getOffsetY() + (int) (this.getHeight() - margin - (this.GetKeyframe(this.ActiveChannel, i).GetParameter(this.ActiveChannel) * getScaleY())));

				Point2D.Double ctrl1 = new Point2D.Double(P1.x + this.GetKeyframe(this.ActiveChannel, i).GetParameter("Bezier-X") * getScaleX(), P1.y - (this.GetKeyframe(this.ActiveChannel, i).GetParameter("Bezier-Y") * getScaleY()));
				Point2D.Double ctrl2 = new Point2D.Double(P1.x - this.GetKeyframe(this.ActiveChannel, i).GetParameter("Bezier-X") * getScaleX(), P1.y + (this.GetKeyframe(this.ActiveChannel, i).GetParameter("Bezier-Y") * getScaleY()));

				// Bezier Tangent for Point 1
				g2.draw(new Line2D.Double(P1.x, P1.y, ctrl1.x, ctrl1.y));

				// Bezier Tangent for Point 2
				g2.draw(new Line2D.Double(P1.x, P1.y, ctrl2.x, ctrl2.y));
			}
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
		for (int i = 1; i < 50; i++) { // TODO 50 is just a placeholder for now
			int X = -(int) this.getOffsetX() + (int) (margin + (i * this.getTimelapseShutterPeriod() * getScaleX()) - ShutterCircleDimension / 2);
			int Y = (int) (MarginTop + 5);
			g2.fillOval(X, Y, ShutterCircleDimension, ShutterCircleDimension);
		}

		// Current Time Indicator
		g2.setColor(CurrentTimeIndicatorColor);
		g2.setStroke(new BasicStroke(1.0f));
		int X = -(int) this.getOffsetX() + (int) (margin + getScaleX() * this.GetCurrentTime());
		g2.draw(new Line2D.Double(X, margin, X, this.getHeight() - margin));

		// Evaluation Time Indicator
		g2.setColor(EvaluationTimeIndicatorColor);
		g2.setStroke(new BasicStroke(1.0f));
		X = -(int) this.getOffsetX() + (int) (margin + getScaleX() * this.getEvaluateTime());
		g2.draw(new Line2D.Double(X, margin, X, this.getHeight() - margin));

		// Evaluation Time Value
		X = -(int) this.getOffsetX() + (int) (margin + getScaleX() * this.getEvaluateTime());
		int Y = (int) (this.getOffsetY()) + (int) (this.getHeight() - margin - this.GetTargetValue(this.getEvaluateTime(), this.GetActiveChannel()) * getScaleY());
		g2.setColor(EvaluationTimeIndicatorColor);
		g2.drawString(this.GetTargetValue(this.getEvaluateTime(), this.GetActiveChannel()) + "", X + 3, Y - 4);
		g2.fillRect(X - 1, Y, KeyframeRectangleDimension - 1, KeyframeRectangleDimension - 1);
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

	public float getOffsetX() {
		return OffsetX;
	}

	public void setOffsetX(float OffsetX) {
		this.OffsetX = OffsetX;
	}

	public float getOffsetY() {
		return OffsetY;
	}

	public void setOffsetY(float OffsetY) {
		this.OffsetY = OffsetY;
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

	@Override
	public void mouseDragged(MouseEvent e) {
		if (dragIndex == NOT_DRAGGING)
			return;

		// Calculate values from on-screen positions
		float X = (e.getX() + this.getOffsetX() - margin) / getScaleX();
		float Y = (this.getOffsetY() + this.getHeight() - margin - e.getY()) / getScaleY();

		this.GetKeyframe(this.ActiveChannel, dragIndex).SetTime(X);
		this.GetKeyframe(this.ActiveChannel, dragIndex).SetParameter(this.ActiveChannel, Y);

		Mainwindow.TimelineHighlightKeyframeChange(dragIndex);

		repaint();
	}

	@Override
	public void mouseMoved(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	/*
	 * double click to add new keyframes to the timeline
	 */
	@Override
	public void mouseClicked(MouseEvent e) {
		if (e.getClickCount() == 2 && !e.isConsumed()) {

			e.consume();

			// Calculate values from on-screen positions
			float X = (e.getX() + this.getOffsetX() - margin) / getScaleX();
			float Y = (this.getOffsetY() + this.getHeight() - margin - e.getY()) / getScaleY();

			// this.GetKeyframe(this.ActiveChannel, dragIndex).SetTime(X);
			// this.GetKeyframe(this.ActiveChannel,
			// dragIndex).SetParameter(this.ActiveChannel, Y);

			Keyframe K1 = new Keyframe(X);
			K1.SetParameter(this.ActiveChannel, Y);
			K1.SetParameter("Bezier-X", 10.0f);
			K1.SetParameter("Bezier-Y", 0.0f);
			Keyframes.add(K1);

			this.OrderKeyframes();
		}
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {
		dragIndex = NOT_DRAGGING;
		int minDistance = Integer.MAX_VALUE;
		int indexOfClosestPoint = -1;
		for (int i = 0; i < this.GetNumberOfKeyframes(this.GetActiveChannel()); i++) {
			int X = -(int) this.getOffsetX() + (int) (margin + (this.GetKeyframe(this.ActiveChannel, i).GetTime() * getScaleX()) - KeyframeRectangleDimension / 2);
			int Y = (int) this.getOffsetY() + (int) (this.getHeight() - margin - (this.GetKeyframe(this.ActiveChannel, i).GetParameter(this.ActiveChannel) * getScaleY())) - KeyframeRectangleDimension / 2;

			int deltaX = X - e.getX();
			int deltaY = Y - e.getY();
			int distance = (int) (Math.sqrt(deltaX * deltaX + deltaY * deltaY));
			if (distance < minDistance) {
				minDistance = distance;
				indexOfClosestPoint = i;
			}
		}

		// Do nothing if the closest keyframe is further away than our
		// DRAG_THRESHHOLD
		if (minDistance > DRAG_THRESHHOLD)
			return;
		else {
			dragIndex = indexOfClosestPoint;
			this.GetKeyframe(this.ActiveChannel, dragIndex).setHightlighted(true);
		}
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// Do nothing if no keyframe is being dragged
		if (dragIndex == NOT_DRAGGING)
			return;

		// Calculate values from on-screen positions
		float X = (e.getX() + this.getOffsetX() - margin) / getScaleX();
		// float Y = (e.getY() - this.getOffsetY() - this.getHeight() + margin)
		// / getScaleY();
		float Y = (this.getOffsetY() + this.getHeight() - margin - e.getY()) / getScaleY();

		this.GetKeyframe(this.ActiveChannel, dragIndex).SetTime(X);
		this.GetKeyframe(this.ActiveChannel, dragIndex).SetParameter(this.ActiveChannel, Y);

		Mainwindow.TimelineHighlightKeyframeChange(dragIndex);

		dragIndex = NOT_DRAGGING;
		this.OrderKeyframes();
		repaint();
	}

	@Override
	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		int a = 1;
	}

	@Override
	public void keyReleased(KeyEvent e) {
		// TODO Auto-generated method stub
		int a = 1;
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		int a = 1;
	}
}
