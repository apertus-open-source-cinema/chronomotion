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

import java.util.ArrayList;

import gnu.io.CommPortIdentifier;

import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.border.TitledBorder;
import javax.swing.JButton;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import java.awt.FlowLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JRadioButton;
import net.miginfocom.swing.MigLayout;
import javax.swing.UIManager;
import java.awt.Color;

public class MainWindow implements Runnable {

	private Chronomotion Parent;
	private final ButtonGroup buttonGroup = new ButtonGroup();
	private Thread Animator;
	private JButton btnUP;
	private JButton btnLeft;
	private JButton btnDown;
	private JButton btnRight;
	private JRadioButton rdbtnSpeedFast;
	private JRadioButton rdbtnSpeedSlow;
	private JPanel InfoPanel;
	private JLabel lblPan;
	private JLabel lblTilt;
	private JLabel lblPosPanDegrees;
	private JLabel lblPosTiltDegrees;
	private JLabel lblSpeed;
	private JLabel lblPosition;
	private JLabel label;
	private JLabel label_1;
	private JLabel lblSpeedPanDegrees;
	private JLabel lblSpeedTiltDegrees;

	public MainWindow(Chronomotion parent) {

		Parent = parent;
		// setLayout(null);

		JFrame frame = new JFrame();
		frame.setBounds(10, 106, 591, 464);
		frame.getContentPane().setLayout(new MigLayout("", "[205px][grow]", "[134px,grow]"));

		JPanel FastControlPanel = new JPanel();
		FastControlPanel.setBorder(new TitledBorder(UIManager.getBorder("TitledBorder.border"), "Quick Control", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(0, 0, 0)));
		frame.getContentPane().add(FastControlPanel, "cell 0 0,alignx left,aligny top");
		GridBagLayout gbl_FastControlPanel = new GridBagLayout();
		gbl_FastControlPanel.columnWidths = new int[] { 55, 0, 55, 0 };
		gbl_FastControlPanel.rowHeights = new int[] { 45, 45, 45, 0, 0 };
		gbl_FastControlPanel.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_FastControlPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		FastControlPanel.setLayout(gbl_FastControlPanel);

		btnUP = new JButton("UP");
		btnUP.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mousePressed(java.awt.event.MouseEvent evt) {
				UPMousePressed(evt);
			}

			public void mouseReleased(java.awt.event.MouseEvent evt) {
				UPMouseReleased(evt);
			}
		});
		GridBagConstraints gbc_btnUP = new GridBagConstraints();
		gbc_btnUP.fill = GridBagConstraints.BOTH;
		gbc_btnUP.insets = new Insets(0, 0, 5, 5);
		gbc_btnUP.gridx = 1;
		gbc_btnUP.gridy = 0;
		FastControlPanel.add(btnUP, gbc_btnUP);

		btnLeft = new JButton("LEFT");
		btnLeft.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mousePressed(java.awt.event.MouseEvent evt) {
				LEFTMousePressed(evt);
			}

			public void mouseReleased(java.awt.event.MouseEvent evt) {
				LEFTMouseReleased(evt);
			}
		});
		GridBagConstraints gbc_btnLeft = new GridBagConstraints();
		gbc_btnLeft.insets = new Insets(0, 0, 5, 5);
		gbc_btnLeft.fill = GridBagConstraints.BOTH;
		gbc_btnLeft.gridx = 0;
		gbc_btnLeft.gridy = 1;
		FastControlPanel.add(btnLeft, gbc_btnLeft);

		btnRight = new JButton("RIGHT");
		btnRight.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mousePressed(java.awt.event.MouseEvent evt) {
				RIGHTMousePressed(evt);
			}

			public void mouseReleased(java.awt.event.MouseEvent evt) {
				RIGHTMouseReleased(evt);
			}
		});
		GridBagConstraints gbc_btnRight = new GridBagConstraints();
		gbc_btnRight.fill = GridBagConstraints.BOTH;
		gbc_btnRight.insets = new Insets(0, 0, 5, 0);
		gbc_btnRight.gridx = 2;
		gbc_btnRight.gridy = 1;
		FastControlPanel.add(btnRight, gbc_btnRight);

		btnDown = new JButton("DOWN");
		btnDown.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mousePressed(java.awt.event.MouseEvent evt) {
				DOWNMousePressed(evt);
			}

			public void mouseReleased(java.awt.event.MouseEvent evt) {
				DOWNMouseReleased(evt);
			}
		});
		GridBagConstraints gbc_btnDown = new GridBagConstraints();
		gbc_btnDown.fill = GridBagConstraints.BOTH;
		gbc_btnDown.insets = new Insets(0, 0, 5, 5);
		gbc_btnDown.gridx = 1;
		gbc_btnDown.gridy = 2;
		FastControlPanel.add(btnDown, gbc_btnDown);

		rdbtnSpeedSlow = new JRadioButton("slow");
		buttonGroup.add(rdbtnSpeedSlow);
		rdbtnSpeedSlow.setSelected(true);
		GridBagConstraints gbc_rdbtnSpeedSlow = new GridBagConstraints();
		gbc_rdbtnSpeedSlow.insets = new Insets(0, 0, 0, 5);
		gbc_rdbtnSpeedSlow.gridx = 0;
		gbc_rdbtnSpeedSlow.gridy = 3;
		FastControlPanel.add(rdbtnSpeedSlow, gbc_rdbtnSpeedSlow);

		rdbtnSpeedFast = new JRadioButton("fast");
		buttonGroup.add(rdbtnSpeedFast);
		GridBagConstraints gbc_rdbtnSpeedFast = new GridBagConstraints();
		gbc_rdbtnSpeedFast.insets = new Insets(0, 0, 0, 5);
		gbc_rdbtnSpeedFast.gridx = 1;
		gbc_rdbtnSpeedFast.gridy = 3;

		FastControlPanel.add(rdbtnSpeedFast, gbc_rdbtnSpeedFast);

		InfoPanel = new JPanel();
		InfoPanel.setBorder(new TitledBorder(null, "Information", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		frame.getContentPane().add(InfoPanel, "cell 1 0,alignx left,aligny top");
		InfoPanel.setLayout(new MigLayout("", "[60.00][][][][][]", "[][]"));

		lblPosition = new JLabel("Position");
		InfoPanel.add(lblPosition, "cell 0 0");

		lblPan = new JLabel("Pan");
		InfoPanel.add(lblPan, "cell 1 0");

		lblPosPanDegrees = new JLabel("...");
		InfoPanel.add(lblPosPanDegrees, "cell 2 0");

		lblTilt = new JLabel("Tilt");
		InfoPanel.add(lblTilt, "cell 4 0");

		lblPosTiltDegrees = new JLabel("...");
		InfoPanel.add(lblPosTiltDegrees, "cell 5 0");

		lblSpeed = new JLabel("Speed");
		InfoPanel.add(lblSpeed, "cell 0 1");

		label = new JLabel("Pan");
		InfoPanel.add(label, "cell 1 1");

		lblSpeedPanDegrees = new JLabel("...");
		InfoPanel.add(lblSpeedPanDegrees, "cell 2 1");

		label_1 = new JLabel("Tilt");
		InfoPanel.add(label_1, "cell 4 1");

		lblSpeedTiltDegrees = new JLabel("...");
		InfoPanel.add(lblSpeedTiltDegrees, "cell 5 1");

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnNewMenu = new JMenu("File");
		menuBar.add(mnNewMenu);

		JMenuItem mntmNewMenuItem_1 = new JMenuItem("Connect");
		mntmNewMenuItem_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Parent.GetConnectWindow().Show(true);
			}
		});
		mnNewMenu.add(mntmNewMenuItem_1);

		JMenuItem mntmNewMenuItem_2 = new JMenuItem("Exit");
		mntmNewMenuItem_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		mnNewMenu.add(mntmNewMenuItem_2);

		frame.setVisible(true);
		Animator = new Thread(this);
		Animator.start();

	}

	float _animator_fps = 1;

	public void run() {
		while (Thread.currentThread() == Animator) {
			UpdateInforomationPanel();

			try {
				Thread.sleep((int) (1.0f / _animator_fps * 1000));
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	int last_pan_steps_per_second = 0;
	int last_tilt_steps_per_second = 0;
	long _last_time_stamp;

	private void UpdateInforomationPanel() {
		long now = System.currentTimeMillis();
		long delta_time = now - _last_time_stamp;
		_last_time_stamp = now;
		if (Parent.GetMerlinController() != null) {
			if (Parent.GetMerlinController().IsConnected()) {
				Parent.GetMerlinController().ReadAxisPosition(AXIS.PAN);
				Parent.GetMerlinController().ReadAxisPosition(AXIS.TILT);

				// pan_pos.setText(MerlinController.GetCurrentSteps(AXIS.PAN) -
				// 8388608
				// + "");
				// tilt_pos.setText(MerlinController.GetCurrentSteps(AXIS.TILT)
				// -
				// 8388608 + "");
				// pan_totalsteps.setText(MerlinController.GetTotalSteps(AXIS.PAN)
				// +
				// "");
				// tilt_totalsteps.setText(MerlinController.GetTotalSteps(AXIS.TILT)
				// +
				// "");
				lblPosPanDegrees.setText(((float) Math.round(((float) Parent.GetMerlinController().GetCurrentSteps(AXIS.PAN) - 8388608) / (float) Parent.GetMerlinController().GetTotalSteps(AXIS.PAN) * 360.0f * 10000.0f) / 10000.0f) + "");
				lblPosTiltDegrees.setText(((float) Math.round(((float) Parent.GetMerlinController().GetCurrentSteps(AXIS.TILT) - 8388608) / Parent.GetMerlinController().GetTotalSteps(AXIS.TILT) * 360.0f * 10000.0f) / 10000.0f) + "");

				int delta_pan_steps = (int) ((Parent.GetMerlinController().GetCurrentSteps(AXIS.PAN) - last_pan_steps_per_second) / (float) delta_time * 1000.0f);
				// pan_steps_per_second.setText(delta_pan_steps + "");
				last_pan_steps_per_second = Parent.GetMerlinController().GetCurrentSteps(AXIS.PAN);
				lblSpeedPanDegrees.setText(Math.round((float) delta_pan_steps / (float) Parent.GetMerlinController().GetTotalSteps(AXIS.PAN) * 1000.0f) / 1000.0f * 360.0f * 60 + "");

				int delta_tilt_steps = (int) ((Parent.GetMerlinController().GetCurrentSteps(AXIS.TILT) - last_tilt_steps_per_second) / (float) delta_time * 1000.0f);
				// tilt_steps_per_second.setText(delta_tilt_steps + "");
				last_tilt_steps_per_second = Parent.GetMerlinController().GetCurrentSteps(AXIS.TILT);
				lblSpeedPanDegrees.setText(Math.round((float) delta_tilt_steps / (float) Parent.GetMerlinController().GetTotalSteps(AXIS.TILT) * 1000.0f) / 1000.0f * 360.0f * 60 + "");

				// pan_sidereal.setText(MerlinController.GetSiderealRate(AXIS.PAN)
				// +
				// "");
				// tilt_sidereal.setText(MerlinController.GetSiderealRate(AXIS.TILT)
				// +
				// "");
				/*
				 * if (timeline1 != null) {
				 * Timeline_Time.setText(timeline1.GetCurrentTime() + "");
				 * Timeline_Target.setText(timeline1.GetTargetTilt() + ""); }
				 */
			}
		}
	}

	private int GetQuickControlSpeed() {
		int ratio = 0;
		if (rdbtnSpeedSlow.isSelected()) {
			ratio = 1;
		}
		if (rdbtnSpeedFast.isSelected()) {
			ratio = 3;
		}
		return ratio;
	}

	private void RIGHTMousePressed(java.awt.event.MouseEvent evt) {
		Parent.GetMerlinController().RotateAxis(AXIS.PAN, 128, GetQuickControlSpeed(), DIRECTION.CW);
		if (GetQuickControlSpeed() == 1)
			Parent.WriteLogtoConsole("Starting Quick Control: SLOW RIGHT");
		else
			Parent.WriteLogtoConsole("Starting Quick Control: FAST RIGHT");
	}

	private void RIGHTMouseReleased(java.awt.event.MouseEvent evt) {
		Parent.GetMerlinController().StopMotor(AXIS.PAN);
		Parent.WriteLogtoConsole("Stopped Quick Control: RIGHT");
	}

	private void UPMousePressed(java.awt.event.MouseEvent evt) {
		Parent.GetMerlinController().RotateAxis(AXIS.TILT, 128, GetQuickControlSpeed(), DIRECTION.CW);
		if (GetQuickControlSpeed() == 1)
			Parent.WriteLogtoConsole("Starting Quick Control: SLOW UP");
		else
			Parent.WriteLogtoConsole("Starting Quick Control: FAST UP");
	}

	private void UPMouseReleased(java.awt.event.MouseEvent evt) {
		Parent.GetMerlinController().StopMotor(AXIS.TILT);
		Parent.WriteLogtoConsole("Stopped Quick Control: UP");
	}

	private void DOWNMousePressed(java.awt.event.MouseEvent evt) {
		Parent.GetMerlinController().RotateAxis(AXIS.TILT, 128, GetQuickControlSpeed(), DIRECTION.CCW);
		if (GetQuickControlSpeed() == 1)
			Parent.WriteLogtoConsole("Starting Quick Control: SLOW DOWN");
		else
			Parent.WriteLogtoConsole("Starting Quick Control: FAST DOWN");
	}

	private void DOWNMouseReleased(java.awt.event.MouseEvent evt) {
		Parent.GetMerlinController().StopMotor(AXIS.TILT);
		Parent.WriteLogtoConsole("Stopped Quick Control: DOWN");
	}

	private void LEFTMousePressed(java.awt.event.MouseEvent evt) {
		Parent.GetMerlinController().RotateAxis(AXIS.PAN, 128, GetQuickControlSpeed(), DIRECTION.CCW);
		if (GetQuickControlSpeed() == 1)
			Parent.WriteLogtoConsole("Starting Quick Control: SLOW LEFT");
		else
			Parent.WriteLogtoConsole("Starting Quick Control: FAST LEFT");
	}

	private void LEFTMouseReleased(java.awt.event.MouseEvent evt) {
		Parent.GetMerlinController().StopMotor(AXIS.PAN);
		Parent.WriteLogtoConsole("Stopped Quick Control: LEFT");
	}
}
