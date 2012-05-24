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
	private float StartTime = 0;
	private float PauseTime = 0;
	private float EvaluateTime = 0;
	private STATE CurrentState;
	private ArrayList<Keyframe> Keyframes = new ArrayList<Keyframe>();
	private float TargetTilt = 0.0f; // TODO
	private Thread Updater;
	private Thread Worker;
	private int UpdatesPerSecond = 20;
	private boolean UpdaterRunning = false;
	private float TimelapseShutterPeriod = 5;
	private float PostShootDelay = 1;
	private Chronomotion Parent;
	private HEADPHASE CurrentPhase = HEADPHASE.STOPPED;

	public Timeline() {
		me = this;
		CurrentState = STATE.STOPPED;
		Updater = new Thread(this);
		Worker = new Thread(this);
		// For Testing
		Keyframe Frame1 = new Keyframe(0);
		Frame1.SetParameter("tilt", 0.0f);
		Keyframes.add(Frame1);
		Keyframe Frame2 = new Keyframe(30);
		Frame2.SetParameter("tilt", 50.0f);
		Keyframes.add(Frame2);
		Keyframe Frame3 = new Keyframe(50);
		Frame3.SetParameter("tilt", 30.0f);
		Keyframes.add(Frame3);

		addMouseListener(new java.awt.event.MouseAdapter() {

			@Override
			public void mousePressed(java.awt.event.MouseEvent evt) {
				int x = evt.getX();
				int y = evt.getY();
				float value = ((float) (x - margin) / me.getScaleX());
				me.setEvaluateTime(value);
				// Parent.GetParent().WriteLogtoConsole("mouse: " + value);
				me.Redraw();
			}
		});
	}

	/*
	 * public void SetParent(MerlinControlTest01View parent) { this.Parent =
	 * parent; }
	 */
	public void SetEvaluateTime(long EvalTime) {
		this.setEvaluateTime(EvalTime);
	}

	public void AddKeyFrame(String Channel, float Time, float value) {
		Keyframe Frame = new Keyframe(Time);
		Frame.SetParameter(Channel, value);
		Keyframes.add(Frame);
		OrderKeyframes();
	}

	/*
	 * Get Keyframes in the correct order again, sort by time This should be
	 * called every time a new keyframe is added or an existing keyframes is
	 * moved in the timeaxis
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
				float PreviousKeyframeTime = GetTime(GetPreviousKeyframeIndex(GetCurrentTime()));
				float NextKeyframeTime = GetTime(GetNextKeyframeIndex(GetCurrentTime()));
				float delta_time = NextKeyframeTime - PreviousKeyframeTime;
				float time_factor_current_segment = (GetCurrentTime() - GetTime(GetPreviousKeyframeIndex(GetCurrentTime()))) / delta_time;
				float d = GetParameter(GetPreviousKeyframeIndex(GetCurrentTime()), "tilt");
				float k = (GetParameter(GetNextKeyframeIndex(GetCurrentTime()), "tilt") - GetParameter(GetPreviousKeyframeIndex(GetCurrentTime()), "tilt")) / delta_time;
				TargetTilt = k * time_factor_current_segment * delta_time + d;
				// System.out.println("PreviousKeyframeTime = " +
				// PreviousKeyframeTime);
				// System.out.println("NextKeyframeTime = " + NextKeyframeTime);
				// System.out.println("time_factor_current_segment = " +
				// time_factor_current_segment);
				// System.out.println("k = " + k);
				// System.out.println("TargetPitch = " + TargetPitch);
				// System.out.println("time_factor_current_segment = " +
				// time_factor_current_segment);
			}
		}
	}

	public float GetTargetValue(float time) {
		float PreviousKeyframeTime = GetTime(GetPreviousKeyframeIndex(time));
		float NextKeyframeTime = GetTime(GetNextKeyframeIndex(time));
		float delta_time = NextKeyframeTime - PreviousKeyframeTime;
		float time_factor_current_segment = (time - GetTime(GetPreviousKeyframeIndex(time))) / delta_time;
		float d = GetParameter(GetPreviousKeyframeIndex(time), "tilt");
		float k = (GetParameter(GetNextKeyframeIndex(time), "tilt") - GetParameter(GetPreviousKeyframeIndex(time), "tilt")) / delta_time;
		float ret = (k * time_factor_current_segment * delta_time + d);
		return ret;
	}

	public float GetTargetTilt() {
		return TargetTilt;
	}

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

	// Returns the next scheduled shutter release AFTER the currenttime
	// parameter

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

	public int GetNextKeyframeIndex(float currenttime) {
		int tempindex = -1;
		float tempvalue = 0;
		// Find all occurances that are after the current time
		for (int i = 0; i < Keyframes.size(); i++) {
			if (currenttime < Keyframes.get(i).GetTime()) {
				// store the lowest one
				if ((tempvalue > Keyframes.get(i).GetTime()) || (tempvalue == 0)) {
					tempindex = i;
					tempvalue = Keyframes.get(i).GetTime();
				}
			}
		}
		return tempindex;
	}

	public int GetPreviousKeyframeIndex(float currenttime) {
		int tempindex = -1;
		float tempvalue = 0;
		// Find all occurances that are before the current time
		for (int i = 0; i < Keyframes.size(); i++) {
			if (currenttime >= Keyframes.get(i).GetTime()) {
				// store the highest one
				if ((tempvalue < Keyframes.get(i).GetTime()) || (tempvalue == 0)) {
					tempindex = i;
					tempvalue = Keyframes.get(i).GetTime();
				}
			}
		}
		return tempindex;
	}

	public void RemoveKeyframe(int index) {
		Keyframes.remove(index);
		Redraw();
	}

	public void UpdaterStart() {
		if (!Updater.isAlive()) {
			Updater.start();
		}
		if (!Worker.isAlive()) {
			Worker.start();
		}
		UpdaterRunning = true;
	}

	public void UpdaterStop() {
		UpdaterRunning = false;
	}

	public HEADPHASE GetCurrentHeadPhase() {
		return this.CurrentPhase;
	}

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
		} else {
			CurrentState = newstate;
		}
	}

	private int StopUpdateFrequency = 5;
	private int MovingUpdateFrequency = 50;
	private int ShutterReleaseUpdateFrequency = 100;
	private float NextTargetShutterReleaseTime = 0.0f;

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

	public void ChangeHeadState(HEADPHASE newstate) {
		if ((this.GetCurrentHeadPhase().equals(HEADPHASE.POSTSHOOTDELAY)) && (newstate == HEADPHASE.MOVING)) {
			// Looped start
			// float NextKeyframeParameter =
			// GetParameter(GetNextKeyframeIndex(GetCurrentTime()), "tilt");
			float NextShootParameter = this.GetTargetValue(this.GetNextShutterReleaseTime(GetCurrentTime()));
			Parent.GetMerlinController().GotoPosition(AXIS.TILT, NextShootParameter);
			CurrentPhase = HEADPHASE.MOVING;
			//Parent.SetPhaseState("Moving");
			System.out.println("NextShootParameter = " + NextShootParameter);
		} else if (newstate == HEADPHASE.POSTSHOOTDELAY) {
			// Calculate when to trigger the shutter release next
			NextTargetShutterReleaseTime = GetNextShutterReleaseTime(this.GetCurrentTime());
			//Parent.SetPhaseState("Post Shoot Delay");
			CurrentPhase = HEADPHASE.POSTSHOOTDELAY;
			System.out.println("NextTargetShutterReleaseTime = " + NextTargetShutterReleaseTime);
		} else if (newstate == HEADPHASE.RELEASINGSHUTTER) {
			// twice --- why odes it only work this way?
			Parent.GetMerlinController().TriggerShutter();
			Parent.GetMerlinController().TriggerShutter();
			//Parent.SetPhaseState("Triggering Shutter");
			System.out.println("snap!");
			CurrentPhase = HEADPHASE.RELEASINGSHUTTER;
		} else if (newstate == HEADPHASE.WAITING) {
			//Parent.SetPhaseState("Waiting");
			CurrentPhase = HEADPHASE.WAITING;
		} else if ((this.GetCurrentHeadPhase().equals(HEADPHASE.STOPPED)) && (newstate == HEADPHASE.MOVING)) {
			// Initial start
			float NextShootParameter = this.GetTargetValue(this.GetNextShutterReleaseTime(GetCurrentTime()));
			Parent.GetMerlinController().GotoPosition(AXIS.TILT, NextShootParameter);
			CurrentPhase = HEADPHASE.MOVING;
			//Parent.SetPhaseState("Moving");
			NextTargetShutterReleaseTime = GetNextShutterReleaseTime(this.GetCurrentTime());
			System.out.println("NextTargetShutterReleaseTime = " + NextTargetShutterReleaseTime);
			System.out.println("NextShootParameter = " + NextShootParameter);
		}
	}

	public void Redraw() {
		Update();
		repaint();
	}

	public void SetKeyframeHighlight(int selectedindex) {
		for (int i = 0; i < Keyframes.size(); i++) {
			Keyframes.get(i).setHightlighted(false);
		}
		Keyframes.get(selectedindex).setHightlighted(true);
		Redraw();
	}

	public int GetNumberOfKeyframes() {
		return Keyframes.size();
	}

	public float GetParameter(int index, String key) {
		if (index > Keyframes.size() - 1) {
			return -1;
		} else if (index < 0) {
			return -1;
		} else {
			return Keyframes.get(index).GetParameter(key);
		}
	}

	public float GetTime(int index) {
		if (index > Keyframes.size() - 1) {
			return -1.0f;
		} else if (index < 0) {
			return -1.0f;
		} else {
			return Keyframes.get(index).GetTime();
		}
	}

	public void SetParameter(int index, String key, float value) {
		Keyframes.get(index).SetParameter(key, value);
	}

	public void SetParameter(int index, String key, int value) {
		Keyframes.get(index).SetParameter(key, value);
	}

	public void SetTime(int index, float newtime) {
		Keyframes.get(index).SetTime(newtime);
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
		g2.drawString("Time [seconds]", margin + 2, 12);
		// 0 Indicator
		g2.draw(new Line2D.Double(0 * getScaleX() + margin, MarginTop, 0 * getScaleX() + margin, MarginTop - 3));
		g2.drawString("0", margin + 2, MarginTop - 1);
		// 10 Indicator
		g2.draw(new Line2D.Double(10 * getScaleX() + margin, MarginTop, 10 * getScaleX() + margin, MarginTop - 3));
		g2.drawString("10", 10 * getScaleX() + margin + 2, MarginTop - 1);
		// 20 Indicator
		g2.draw(new Line2D.Double(20 * getScaleX() + margin, MarginTop, 20 * getScaleX() + margin, MarginTop - 3));
		g2.drawString("20", 20 * getScaleX() + margin + 2, MarginTop - 1);
		// 30 Indicator
		g2.draw(new Line2D.Double(30 * getScaleX() + margin, MarginTop, 30 * getScaleX() + margin, MarginTop - 3));
		g2.drawString("30", 30 * getScaleX() + margin + 2, MarginTop - 1);
		// Axis
		g2.setColor(AxisColor);
		g2.draw(new Line2D.Double(margin, this.getHeight() - margin, this.getWidth(), this.getHeight() - margin));
		g2.drawString("0", margin, this.getHeight() - margin - 2);
		g2.draw(new Line2D.Double(margin, this.getHeight() - margin - (getScaleY() * 1000), this.getWidth(), this.getHeight() - margin - (getScaleY() * 1000)));
		g2.drawString("1000", margin, this.getHeight() - margin - (getScaleY() * 1000) - 2);
		// KeyFrame Lines
		g2.setColor(LineColor);
		g2.setStroke(new BasicStroke(2.0f));
		for (int i = 0; i < Keyframes.size() - 1; i++) {
			int X1 = (int) (margin + Keyframes.get(i).GetTime() * getScaleX());
			int Y1 = (int) (this.getHeight() - margin - (Keyframes.get(i).GetParameter("tilt") * getScaleY()));
			int X2 = (int) (margin + Keyframes.get(i + 1).GetTime() * getScaleX());
			int Y2 = (int) (this.getHeight() - margin - (Keyframes.get(i + 1).GetParameter("tilt") * getScaleY()));
			g2.draw(new Line2D.Double(X1, Y1, X2, Y2));
		}
		// Keyframe Rectangles
		for (int i = 0; i < Keyframes.size(); i++) {
			int X = (int) (margin + (Keyframes.get(i).GetTime() * getScaleX()) - KeyframeRectangleDimension / 2);
			int Y = (int) (this.getHeight() - margin - (Keyframes.get(i).GetParameter("tilt") * getScaleY())) - KeyframeRectangleDimension / 2;
			if (Keyframes.get(i).isHightlighted()) {
				g2.setColor(HightlightedKeyframeRectangleColor);
				g2.drawString(Keyframes.get(i).GetParameter("tilt") + "", X + 3, Y - 4);
			} else {
				g2.setColor(KeyframeRectangleColor);
			}
			g2.fillRect(X, Y, KeyframeRectangleDimension, KeyframeRectangleDimension);
		} // GOTO Indicators
		g2.setColor(GOTOIndicatorColor);
		for (int i = 0; i < 20; i++) {
			int X = (int) (margin + (i * this.getTimelapseShutterPeriod() * ScaleX) - GOTOIndicatorDimension / 2);
			int Y = (int) ((this.getHeight() - margin - (this.GetTargetValue(i * this.getTimelapseShutterPeriod()) * getScaleY())) - GOTOIndicatorDimension / 2);
			g2.fillOval(X, Y, GOTOIndicatorDimension, GOTOIndicatorDimension);
		} // Shutter Release Circles
		g2.setColor(ShutterReleaseCircleColor);
		for (int i = 0; i < 20; i++) {
			int X = (int) (margin + (i * this.getTimelapseShutterPeriod() * ScaleX) - ShutterCircleDimension / 2);
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
	}

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

	public float getEvaluateTime() {
		return EvaluateTime;
	}

	public void setEvaluateTime(float EvaluateTime) {
		this.EvaluateTime = EvaluateTime;
	}
}
