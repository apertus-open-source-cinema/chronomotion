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

import gnu.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;

public class CommunicationManager implements Runnable {

	private ArrayList<String> _CommandList;
	private InputStream in;
	private OutputStream out;
	private boolean _readytosend;
	private SerialPort serialPort;
	private Thread Watcher;
	private int[] _TotalSteps;
	private int[] _CurrentSteps;
	private int[] _SiderealRate;
	private boolean Connected = false;
	static boolean debug_serial_commands = false;

	public CommunicationManager() {
		_CommandList = new ArrayList<String>();
		_readytosend = true;
		_TotalSteps = new int[2];
		_TotalSteps[0] = 1; // to prevent division by zero
		_TotalSteps[1] = 1; // to prevent division by zero
		_CurrentSteps = new int[2];
		_SiderealRate = new int[2];
	}

	void SetCurrentSteps(int steps, AXIS axis) {
		this._CurrentSteps[TranslateAxis(axis) - 1] = steps;
	}

	void SetTotalSteps(int steps, AXIS axis) {
		this._TotalSteps[TranslateAxis(axis) - 1] = steps;
	}

	int GetTotalSteps(AXIS axis) {
		return this._TotalSteps[TranslateAxis(axis) - 1];
	}

	void SetSiderealRate(int rate, AXIS axis) {
		this._SiderealRate[TranslateAxis(axis) - 1] = rate;
	}

	int GetSiderealRate(AXIS axis) {
		return this._SiderealRate[TranslateAxis(axis) - 1];
	}

	int GetCurrentSteps(AXIS axis) {
		return this._CurrentSteps[TranslateAxis(axis) - 1];
	}

	public void ExecuteCommand(String command) {
		this._CommandList.add(command);
	}

	public void RotateAxis(AXIS Axis, int Speed) {
		StopMotor(Axis);
		SetSpeed(Axis, Speed);
		StartMotor(Axis);
	}

	public void RotateAxis(AXIS Axis, int Speed, int Ratio, DIRECTION dir) {
		StopMotor(Axis);
		SetSpeed(Axis, TranslateSpeed(Speed), Ratio, dir);
		StartMotor(Axis);
	}

	public void RotateAxis(AXIS Axis, String Speed, int Ratio, DIRECTION dir) {
		StopMotor(Axis);
		SetSpeed(Axis, Speed, Ratio, dir);
		StartMotor(Axis);
	}

	public void GotoPosition(AXIS Axis, int position) {
		StopMotor(Axis);

		ExecuteCommand(":G" + TranslateAxis(Axis) + "40\r");
		ExecuteCommand(":S" + TranslateAxis(Axis) + TranslatePosition(position) + "\r");

		StartMotor(Axis);
	}

	void GotoPosition(AXIS Axis, float degrees) {
		StopMotor(Axis);

		ExecuteCommand(":G" + TranslateAxis(Axis) + "40\r");

		int pos = 0;
		pos = (int) (degrees * GetTotalSteps(Axis) / 360.0f) + 8388608;
		ExecuteCommand(":S" + TranslateAxis(Axis) + TranslatePosition(pos) + "\r");

		StartMotor(Axis);
	}

	// Convert from ENUM to int
	private int TranslateAxis(AXIS Axis) {
		int axis = 0;
		if (Axis == AXIS.PAN) {
			axis = 1;
		}
		if (Axis == AXIS.TILT) {
			axis = 2;
		}
		return axis;
	}

	private String TranslateSpeed(int speed) {
		String temp;
		temp = Integer.toHexString(speed);
		StringBuilder dest = new StringBuilder(temp);
		for (int i = temp.length(); i < 6; i++) {
			dest.append("0");
		}
		return dest.toString();
	}

	private String TranslatePosition(int pos) {
		String temp;
		temp = Integer.toHexString(pos);
		StringBuilder dest = new StringBuilder(temp);
		for (int i = temp.length(); i < 6; i++) {
			dest.append("0");
		}

		StringBuilder dest2 = new StringBuilder("");
		dest2.append(dest.charAt(4));
		dest2.append(dest.charAt(5));

		dest2.append(dest.charAt(2));
		dest2.append(dest.charAt(3));

		dest2.append(dest.charAt(0));
		dest2.append(dest.charAt(1));

		return dest2.toString().toUpperCase();
	}

	// Start the Motor on a specific axis
	public void StartMotor(AXIS Axis) {
		ExecuteCommand(":J" + TranslateAxis(Axis) + "\r");
	}

	// Stop the Motor on a specific axis
	public void StopMotor(AXIS Axis) {
		ExecuteCommand(":L" + TranslateAxis(Axis) + "\r");
	}

	// Read the current position of one axis
	public void ReadAxisPosition(AXIS Axis) {
		ExecuteCommand(":j" + TranslateAxis(Axis) + "\r");
	}

	// Trigger Shutter
	public void TriggerShutter() {
		// TODO allow HDR bracketing by triggering multiple times
		ExecuteCommand(":O11\r");
		ExecuteCommand(":O10\r");
	}

	public void StartShutter() {
	}

	public void StopShutter() {
	}

	// Init both axis and read the the number of encoder steps per turn
	public void InitAxisPositions() {
		ExecuteCommand(":F1\r");
		ExecuteCommand(":F2\r");
		ExecuteCommand(":a1\r");
		ExecuteCommand(":D1\r");
		ExecuteCommand(":a2\r");
		ExecuteCommand(":D2\r");
	}

	// TODO
	public float GetMeassuredSpeed(AXIS Axis) {
		if (Axis == AXIS.TILT) { /*
								 * int delta_tilt_steps = (int)
								 * ((GetCurrentSteps(AXIS.TILT) -
								 * last_tilt_steps_per_second) / (float)
								 * delta_time * 1000.0f);
								 * tilt_steps_per_second.setText
								 * (delta_tilt_steps + "");
								 * last_tilt_steps_per_second =
								 * MerlinController.GetCurrentSteps(AXIS.TILT);
								 * tilt_degrees_per_second
								 * .setText(Math.round((float) delta_tilt_steps
								 * / (float)
								 * MerlinController.GetTotalSteps(AXIS.TILT) *
								 * 1000.0f) / 1000.0f * 360.0f * 60 + "");
								 */
		}
		return 0.0f;
	}

	// Set speed value based on degrees per min (rather than integer #)
	public void SetSpeed(AXIS Axis, float degrees) {
		double temp = this._TotalSteps[TranslateAxis(Axis) - 1] / 360.0; // get
																			// steps
																			// per
																			// degree
		temp *= degrees; // get number of steps for our particular speed
		temp *= 60.0; // per minute

		// double speed = 0;
		/*
		 * if (_ratio[axis - 1] == 1) { speed = 19531.25 * temp; } else { speed
		 * = 666666.0 * temp; }
		 */

		// speed can be between 1 .. 3x256=768 ?

		int ratio;
		int Speed = (int) degrees; // temp solution TODO
		String speed;
		int direction = 0;
		if (Speed > 0) {
			direction = 1;
		} else {
			direction = 2;
		}

		// TODO wrong
		Speed = Math.abs(Speed);
		if (Speed > 512) {
			ratio = 3;
		} else if ((Speed > 256) && (Speed <= 512)) {
			ratio = 1;
		} else {
			ratio = 4; // trobuel with slew mode
		}
		String speed1 = (Integer.toHexString(256 - (Speed % 256))).charAt(1) + "";
		String speed2 = (Integer.toHexString(256 - (Speed % 256))).charAt(0) + "";
		speed = speed1 + speed2 + "0000";
		// Speed "00" doesnt work

		// ratio: a single character specifying a speed ration – 1 = normal, 3 =
		// fast, 4 = slew(?)
		ExecuteCommand(":G" + TranslateAxis(Axis) + "" + ratio + "" + direction + "\r"); // ExecuteCommand(":G"
																							// +
																							// axis
																							// +
																							// "3"
																							// +
																							// direction
																							// +
																							// "\r");
		ExecuteCommand(":I" + TranslateAxis(Axis) + "" + speed.toUpperCase() + "\r");
	}

	public void SetSpeed(AXIS Axis, String speed, int ratio, DIRECTION dir) {
		int direction;
		if (dir == DIRECTION.CCW) {
			direction = 1;
		} else {
			direction = 2;
		}

		ExecuteCommand(":G" + TranslateAxis(Axis) + "" + ratio + "" + direction + "\r"); // ExecuteCommand(":G"
																							// +
																							// axis
																							// +
																							// "3"
																							// +
																							// direction
																							// +
																							// "\r");
		ExecuteCommand(":I" + TranslateAxis(Axis) + "" + speed.toUpperCase() + "\r");
	}

	public void ReadSiderealRate(AXIS axis) {
		ExecuteCommand(":D" + TranslateAxis(axis) + "\r");
	}

	public void Init(String Port) {

		try {
			connect(Port);
		} catch (Exception e) {
			e.printStackTrace();
		}
		Watcher = new Thread(this);
		Watcher.start();

		InitAxisPositions();
	}

	public boolean IsConnected() {
		return Connected;
	}

	void SendString(String message, String response) {
		// debuging
		if (debug_serial_commands) {
			System.out.println("Writing \"" + message + "\" to " + serialPort.getName());
		}
		try {
			out.write(message.getBytes());
		} catch (IOException e) {
		}
	}

	// serial commands to send per seconds
	// 100 seems to work fine, it seems to work with values up to 250 for most
	// commands
	// higher values might be possible but can lead to loss of data so to stay
	// on the safe side
	// this also depends on the kind of device, USB to serial adapter require
	// slower values than onboard serial ports
	float commands_per_second = 40;

	public void run() {
		while (Thread.currentThread() == Watcher) {
			if (!this._CommandList.isEmpty()) {
				if (this._readytosend) {
					try {
						int c = 0;
						for (int index = 0; index < this._CommandList.get(0).length(); index++) {
							c = this._CommandList.get(0).charAt(index);
							this.out.write(c);
						}
						this._CommandList.remove(0);
						/*
						 * while ((c = System.in.read()) > -1) {
						 * this.out.write(c); }
						 */
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			try {
				Thread.sleep((int) (1.0f / commands_per_second * 1000));
			} catch (InterruptedException e) {
				break;
			}
		}
	}

	/**
	 * @return A HashSet containing the CommPortIdentifier for all serial ports
	 *         that are not currently being used.
	 */
	public static ArrayList<CommPortIdentifier> getAvailableSerialPorts() {
		ArrayList<CommPortIdentifier> h = new ArrayList();
		Enumeration thePorts = CommPortIdentifier.getPortIdentifiers();
		while (thePorts.hasMoreElements()) {
			CommPortIdentifier com = (CommPortIdentifier) thePorts.nextElement();
			switch (com.getPortType()) {
			case CommPortIdentifier.PORT_SERIAL:
				try {
					CommPort thePort = com.open("CommUtil", 50);
					thePort.close();
					h.add(com);
				} catch (PortInUseException e) {
					System.out.println("Port, " + com.getName() + ", is in use.");
				} catch (Exception e) {
					System.err.println("Failed to open port " + com.getName());
					e.printStackTrace();
				}
			}
		}
		return h;
	}

	public void connect(String portName) throws Exception {
		CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);

		if (portIdentifier.isCurrentlyOwned()) {
			System.out.println("Error: Port is currently in use");
		} else {
			CommPort commPort = portIdentifier.open(this.getClass().getName(), 2000);
			if (commPort instanceof SerialPort) {
				serialPort = (SerialPort) commPort;
				serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);

				in = serialPort.getInputStream();
				out = serialPort.getOutputStream();

				serialPort.addEventListener(new SerialReader(in, this));
				serialPort.notifyOnDataAvailable(true);
				Connected = true;
			} else {
				System.out.println("Error: Only serial ports are handled by this example.");
			}
		}
	}

	public static class SerialReader implements SerialPortEventListener {

		private InputStream _in;
		private byte[] _buffer = new byte[1024];
		private CommunicationManager _manager;
		private ArrayList<String> _CommandsList;

		public SerialReader(InputStream in, CommunicationManager Manager) {
			this._in = in;
			this._manager = Manager;
			_CommandsList = new ArrayList<String>();
		}

		public void serialEvent(SerialPortEvent arg0) {
			int data;

			try {
				int len = 0;
				while ((data = _in.read()) > -1) {
					if (data == '\r') {
						data = 64;
						// break;
					}
					_buffer[len++] = (byte) data;
				}
				String line = new String(_buffer, 0, len);
				// debuging
				if (debug_serial_commands) {
					System.out.println(line);
				}
				String[] commands;
				commands = line.split("@");
				_CommandsList.addAll(Arrays.asList(commands));
				AnalyzeResponses();

			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}

		private void AnalyzeResponses() {
			while (_CommandsList.size() >= 2) {
				String line1 = _CommandsList.get(0);
				String line2 = _CommandsList.get(1);
				if (line1.contains(":a")) { // ":a" request the total number of
											// encoder counts per turn (in HEX)
					if (line2.charAt(0) == '=') { // this is the response
													// starting with a "="

						// remove the first character which is "="
						String CutString = line2.substring(1, line2.length());

						// Low byte is sent first (A35483 is read 8354A3)
						// so we need to switch position of each character pair
						int len = CutString.length();
						StringBuilder dest = new StringBuilder(len);

						dest.append(CutString.charAt(4));
						dest.append(CutString.charAt(5));

						dest.append(CutString.charAt(2));
						dest.append(CutString.charAt(3));

						dest.append(CutString.charAt(0));
						dest.append(CutString.charAt(1));

						String ReverseCutString = dest.toString();

						// and then convert from a HEX String to integer
						int intValue = Integer.parseInt(ReverseCutString, 16);

						// determine which axis and save the value accordingly
						if (line1.substring(2, 3).equals("1")) { // ":a1"
							_manager.SetTotalSteps(intValue, AXIS.PAN);
						} else if (line1.substring(2, 3).equals("2")) { // ":a2"
							_manager.SetTotalSteps(intValue, AXIS.TILT);
						}

						// System.out.println("a1: " + intValue); //debug

						// remove both request and response from our list of
						// saved commands
						_CommandsList.remove(0);
						_CommandsList.remove(0);
					}
				} else if (line1.contains(":j")) { // request current position
													// from encoder
					if (line2.charAt(0) == '=') { // this is the response
													// starting with a "="

						// remove the first character which is "="
						String CutString = line2.substring(1, line2.length());

						// Low byte is sent first (A35483 is read 8354A3)
						// so we need to switch position of each character pair
						int len = CutString.length();
						StringBuilder dest = new StringBuilder(len);

						dest.append(CutString.charAt(4));
						dest.append(CutString.charAt(5));

						dest.append(CutString.charAt(2));
						dest.append(CutString.charAt(3));

						dest.append(CutString.charAt(0));
						dest.append(CutString.charAt(1));

						String ReverseCutString = dest.toString();

						// and then convert from a HEX String to integer
						int intValue = Integer.parseInt(ReverseCutString, 16);

						// determine which axis and save the value accordingly
						if (line1.substring(2, 3).equals("1")) { // ":j1"
							_manager.SetCurrentSteps(intValue, AXIS.PAN);
						} else if (line1.substring(2, 3).equals("2")) { // ":j2"
							_manager.SetCurrentSteps(intValue, AXIS.TILT);
						}

						// System.out.println("a1: " + intValue); //debug

						// remove both request and response from our list of
						// saved commands
						_CommandsList.remove(0);
						_CommandsList.remove(0);
					}
				} else if (line1.contains(":D")) { // Get Sidreal Rate
					if (line2.charAt(0) == '=') { // this is the response
													// starting with a "="

						// remove the first character which is "="
						String CutString = line2.substring(1, line2.length());

						// Low byte is sent first (A35483 is read 8354A3)
						// so we need to switch position of each character pair
						int len = CutString.length();
						StringBuilder dest = new StringBuilder(len);

						dest.append(CutString.charAt(4));
						dest.append(CutString.charAt(5));

						dest.append(CutString.charAt(2));
						dest.append(CutString.charAt(3));

						dest.append(CutString.charAt(0));
						dest.append(CutString.charAt(1));

						String ReverseCutString = dest.toString();

						// and then convert from a HEX String to integer
						int intValue = Integer.parseInt(ReverseCutString, 16);

						// determine which axis and save the value accordingly
						if (line1.substring(2, 3).equals("1")) { // ":j1"
							_manager.SetSiderealRate(intValue, AXIS.PAN);
						} else if (line1.substring(2, 3).equals("2")) { // ":j2"
							_manager.SetSiderealRate(intValue, AXIS.TILT);
						}

						// System.out.println("a1: " + intValue); //debug

						// remove both request and response from our list of
						// saved commands
						_CommandsList.remove(0);
						_CommandsList.remove(0);
					}
				} else {
					_CommandsList.remove(0);
				}
			}
		}

		/*
		 * public static class SerialReader implements Runnable {
		 * 
		 * InputStream input;
		 * 
		 * public SerialReader(InputStream in) { this.input = in; }
		 * 
		 * public void run() { byte[] buffer = new byte[1024]; int len = -1; try
		 * { while ((len = this.input.read(buffer)) > -1) {
		 * //System.out.print(new String(buffer, 0, len));
		 * 
		 * if (len > 0) { String message = new String(buffer, 0, len);
		 * message.replaceAll("\r", "\\r"); System.out.print("Response: " +
		 * message + "\n"); } } } catch (IOException e) { e.printStackTrace(); }
		 * } }
		 */
		public static class SerialWriter implements Runnable {

			OutputStream output;

			public SerialWriter(OutputStream out) {
				this.output = out;
			}

			public void run() {
				try {
					int c = 0;
					while ((c = System.in.read()) > -1) {
						this.output.write(c);
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}
