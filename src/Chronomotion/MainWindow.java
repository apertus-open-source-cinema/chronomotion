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
import java.awt.GridLayout;
import javax.swing.JTextField;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

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
		frame.setBounds(10, 106, 686, 603);
		frame.getContentPane().setLayout(new MigLayout("", "[205px][grow]", "[176.00px][][]"));

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

		ManuelMotionPanel = new JPanel();
		ManuelMotionPanel.setBorder(new TitledBorder(null, "Manual Motion", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		frame.getContentPane().add(ManuelMotionPanel, "cell 0 1,alignx left,aligny top");
		ManuelMotionPanel.setLayout(new GridLayout(0, 1, 0, 0));

		Tilt = new JPanel();
		Tilt.setBorder(new TitledBorder(null, "Tilt", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		ManuelMotionPanel.add(Tilt);
		Tilt.setLayout(new GridLayout(0, 2, 0, 0));

		label_2 = new JLabel("Speed");
		Tilt.add(label_2);

		label_3 = new JLabel("Ratio");
		Tilt.add(label_3);

		ManualMotionTiltSpeed = new JTextField();
		ManualMotionTiltSpeed.setText("0");
		ManualMotionTiltSpeed.setColumns(10);
		Tilt.add(ManualMotionTiltSpeed);

		ManualMotionTiltRatio = new JTextField();
		ManualMotionTiltRatio.setText("1");
		ManualMotionTiltRatio.setColumns(10);
		Tilt.add(ManualMotionTiltRatio);

		Pan = new JPanel();
		Pan.setBorder(new TitledBorder(null, "Pan", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		ManuelMotionPanel.add(Pan);
		Pan.setLayout(new GridLayout(0, 2, 0, 0));

		lblNewLabel = new JLabel("Speed");
		Pan.add(lblNewLabel);

		lblNewLabel_1 = new JLabel("Ratio");
		Pan.add(lblNewLabel_1);

		ManualMotionPanSpeed = new JTextField();
		ManualMotionPanSpeed.setText("0");
		Pan.add(ManualMotionPanSpeed);
		ManualMotionPanSpeed.setColumns(10);

		ManualMotionPanRatio = new JTextField();
		ManualMotionPanRatio.setText("1");
		Pan.add(ManualMotionPanRatio);
		ManualMotionPanRatio.setColumns(10);

		panel_1 = new JPanel();
		panel_1.setBorder(new TitledBorder(null, "Control", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		ManuelMotionPanel.add(panel_1);
		panel_1.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));

		ManualMotionStart = new JButton("Start");
		ManualMotionStart.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mousePressed(java.awt.event.MouseEvent evt) {
				ManualMotionStartPressed(evt);
			}
		});
		panel_1.add(ManualMotionStart);

		ManualMotionStop = new JButton("Stop");
		ManualMotionStop.addMouseListener(new java.awt.event.MouseAdapter() {
			public void mousePressed(java.awt.event.MouseEvent evt) {
				ManualMotionStopPressed(evt);
			}
		});
		ManualMotionStop.setEnabled(false);
		panel_1.add(ManualMotionStop);

		GOTOPanel = new JPanel();
		GOTOPanel.setBorder(new TitledBorder(null, "Goto", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		frame.getContentPane().add(GOTOPanel, "flowx,cell 1 1,alignx left,aligny top");
		GOTOPanel.setLayout(new MigLayout("", "[18px][86px][57px][55px]", "[23px][23px]"));

		lblPan_1 = new JLabel("Pan");
		GOTOPanel.add(lblPan_1, "cell 0 0,alignx center,aligny center");

		GOTOPanPos = new JTextField();
		GOTOPanel.add(GOTOPanPos, "cell 1 0,alignx center,aligny center");
		GOTOPanPos.setText("0");
		GOTOPanPos.setColumns(10);

		GotoPanStart = new JButton("Start");
		GotoPanStart.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				GotoPanStartMousePressed(e);
			}
		});
		GOTOPanel.add(GotoPanStart, "cell 2 0,alignx center,aligny center");

		GotoPanStop = new JButton("Stop");
		GotoPanStop.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				GotoPanStopMousePressed(e);
			}
		});
		GOTOPanel.add(GotoPanStop, "cell 3 0,alignx center,aligny center");
		GotoPanStop.setEnabled(false);

		lblTilt_1 = new JLabel("Tilt");
		GOTOPanel.add(lblTilt_1, "cell 0 1,alignx center,aligny center");

		GOTOTiltPos = new JTextField();
		GOTOPanel.add(GOTOTiltPos, "cell 1 1,alignx center,aligny center");
		GOTOTiltPos.setText("0");
		GOTOTiltPos.setColumns(10);

		GotoTiltStart = new JButton("Start");
		GotoTiltStart.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				GotoTiltStartMousePressed(e);
			}
		});
		GOTOPanel.add(GotoTiltStart, "cell 2 1,alignx center,aligny center");

		GotoTiltStop = new JButton("Stop");
		GotoTiltStop.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				GotoTiltStopMousePressed(e);
			}
		});
		GotoTiltStop.setEnabled(false);
		GOTOPanel.add(GotoTiltStop, "cell 3 1,alignx center,aligny center");
		
		TimelapseParameterPanel = new JPanel();
		TimelapseParameterPanel.setBorder(new TitledBorder(null, "Time lapse parameters", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		frame.getContentPane().add(TimelapseParameterPanel, "cell 1 2,alignx left,aligny top");
		TimelapseParameterPanel.setLayout(new MigLayout("", "[140px][140px]", "[20px][20px]"));
		
		lblNewLabel_2 = new JLabel("Shutter Period [seconds]");
		TimelapseParameterPanel.add(lblNewLabel_2, "cell 0 0,grow");
		
		textField = new JTextField();
		TimelapseParameterPanel.add(textField, "cell 1 0,grow");
		textField.setColumns(10);
		
		lblNewLabel_3 = new JLabel("Post Shutter Delay [seconds]");
		TimelapseParameterPanel.add(lblNewLabel_3, "cell 0 1,grow");
		
		textField_1 = new JTextField();
		TimelapseParameterPanel.add(textField_1, "cell 1 1,grow");
		textField_1.setColumns(10);

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
	private JPanel ManuelMotionPanel;
	private JPanel Pan;
	private JLabel lblNewLabel;
	private JLabel lblNewLabel_1;
	private JTextField ManualMotionPanSpeed;
	private JTextField ManualMotionPanRatio;
	private JPanel Tilt;
	private JLabel label_2;
	private JLabel label_3;
	private JTextField ManualMotionTiltSpeed;
	private JTextField ManualMotionTiltRatio;
	private JButton ManualMotionStart;
	private JButton ManualMotionStop;
	private JPanel panel_1;
	private JPanel GOTOPanel;
	private JLabel lblPan_1;
	private JLabel lblTilt_1;
	private JTextField GOTOPanPos;
	private JTextField GOTOTiltPos;
	private JButton GotoPanStart;
	private JButton GotoPanStop;
	private JButton GotoTiltStart;
	private JButton GotoTiltStop;
	private JPanel TimelapseParameterPanel;
	private JLabel lblNewLabel_2;
	private JTextField textField;
	private JLabel lblNewLabel_3;
	private JTextField textField_1;

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

	private void ManualMotionStartPressed(java.awt.event.MouseEvent evt) {
		Parent.GetMerlinController().RotateAxis(AXIS.PAN, ManualMotionPanSpeed.getText(), Integer.parseInt(ManualMotionPanRatio.getText()), DIRECTION.CCW);
		Parent.GetMerlinController().RotateAxis(AXIS.TILT, ManualMotionTiltSpeed.getText(), Integer.parseInt(ManualMotionTiltRatio.getText()), DIRECTION.CCW);

		ManualMotionStop.setEnabled(true);
		ManualMotionStart.setEnabled(false);
		Parent.WriteLogtoConsole("Started Manual Motion with Pan(S: " + ManualMotionPanSpeed.getText() + "|R: " + Integer.parseInt(ManualMotionPanRatio.getText()) + ") Tilt(S: " + ManualMotionTiltSpeed.getText() + "|R: " + Integer.parseInt(ManualMotionTiltRatio.getText()) + ")");
	}

	private void ManualMotionStopPressed(java.awt.event.MouseEvent evt) {
		Parent.GetMerlinController().StopMotor(AXIS.PAN);
		Parent.GetMerlinController().StopMotor(AXIS.TILT);

		ManualMotionStop.setEnabled(false);
		ManualMotionStart.setEnabled(true);
		Parent.WriteLogtoConsole("Stopped Manual Motion");
	}

	private void GotoPanStartMousePressed(java.awt.event.MouseEvent evt) {
		Parent.GetMerlinController().GotoPosition(AXIS.PAN, Float.parseFloat(GOTOPanPos.getText()));
		GotoPanStop.setEnabled(true);
		GotoPanStart.setEnabled(false);
		Parent.WriteLogtoConsole("Started Pan GOTO Position: " + Float.parseFloat(GOTOPanPos.getText()));
	}

	private void GotoTiltStartMousePressed(java.awt.event.MouseEvent evt) {
		Parent.GetMerlinController().GotoPosition(AXIS.TILT, Float.parseFloat(GOTOTiltPos.getText()));
		GotoTiltStop.setEnabled(true);
		GotoTiltStart.setEnabled(false);
		Parent.WriteLogtoConsole("Started Tilt GOTO Position: " + Float.parseFloat(GOTOPanPos.getText()));
	}

	private void GotoPanStopMousePressed(java.awt.event.MouseEvent evt) {
		Parent.GetMerlinController().GotoPosition(AXIS.PAN, Float.parseFloat(GOTOPanPos.getText()));
		GotoPanStart.setEnabled(true);
		GotoPanStop.setEnabled(false);
		Parent.WriteLogtoConsole("Stopped Pan GOTO");
	}

	private void GotoTiltStopMousePressed(java.awt.event.MouseEvent evt) {
		Parent.GetMerlinController().GotoPosition(AXIS.TILT, Float.parseFloat(GOTOTiltPos.getText()));
		GotoTiltStop.setEnabled(false);
		GotoTiltStart.setEnabled(true);
		Parent.WriteLogtoConsole("Stopped Tilt GOTO");
	}

}
