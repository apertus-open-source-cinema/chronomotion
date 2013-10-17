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

import gnu.io.CommPortIdentifier;

import java.awt.Color;
import java.awt.EventQueue;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.UIManager;

public class Chronomotion {

	private MainWindow MWindow;
	private ConnectWindow ConWindow;

	private static CommunicationManager MerlinController;
	private int Debuglevel = 3;
	private org.fusesource.jansi.AnsiConsole AnsiConsole;

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		// Use System Look & Feel
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
		}

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Chronomotion window = new Chronomotion();
					window.ProcessArgs(args);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});

		// When exiting Chronomotion
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				System.out.println("Disconnecting COM Ports");
				try {
					GetMerlinController().disconnect();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Chronomotion() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		/*
		 * frame = new JFrame(); frame.setBounds(100, 100, 450, 300);
		 * frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		 */
		MWindow = new MainWindow(this);
		ConWindow = new ConnectWindow(this);
		// frame.add(MWindow);

		MerlinController = new CommunicationManager(this);

		AnsiConsole = new org.fusesource.jansi.AnsiConsole();
		AnsiConsole.systemInstall();
	}

	public ConnectWindow GetConnectWindow() {
		return ConWindow;
	}

	private void ProcessArgs(String[] args) {
		// TODO
	}

	// Write help message to console
	static void PrintHelp() {
		System.out.println("Chronomotion Help: ");
		System.out.println("Arguments: ");
		System.out.println("\t-h, --help\tshow this help message.");
		System.out.println("\t--debuglevel N\t0 - none, 1 - only errors, 2 - errors and warnings (default), 3 - everything");
	}

	// Change the text color of console messages
	public void SetConsoleColor(Color newcolor) {
		if (newcolor == Color.BLACK) {
			System.out.print("\033[30m");
		} else if (newcolor == Color.WHITE) {
			System.out.print("\033[39m");
		} else if (newcolor == Color.RED) {
			System.out.print("\033[31m");
		} else if (newcolor == Color.GREEN) {
			System.out.print("\033[32m");
		} else if (newcolor == Color.YELLOW) {
			System.out.print("\033[33m");
		} else if (newcolor == Color.BLUE) {
			System.out.print("\033[34m");
		} else if (newcolor == Color.MAGENTA) {
			System.out.print("\033[35m");
		} else if (newcolor == Color.CYAN) {
			System.out.print("\033[36m");
		}
		/*
		 * ANSI CODES: Black: \033[30m Red: \033[31m Green: \033[32m Yellow:
		 * \033[33m Blue: \033[34m Magenta: \033[35m Cyan: \033[36m White:
		 * \033[37m
		 */
	}

	// Write Log Message with correct color coding to console
	public void WriteLogtoConsole(String log) {
		if (Debuglevel > 2) {
			SetConsoleColor(Color.WHITE);
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.S");
			System.out.println("[" + sdf.format(cal.getTime()) + "] LOG:\033[1m " + log + "\033[22m\033[0m");
		}
	}

	// Write Warning Message with correct color coding to console
	public void WriteWarningtoConsole(String log) {
		if (Debuglevel > 1) {
			SetConsoleColor(Color.YELLOW);
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.S");
			System.out.println("[" + sdf.format(cal.getTime()) + "] WARNING: \033[1m" + log + "\033[22m\033[0m");
		}
	}

	// Write Error Message with correct color coding to console
	public void WriteErrortoConsole(String log) {
		if (Debuglevel > 0) {
			SetConsoleColor(Color.RED);
			Calendar cal = Calendar.getInstance();
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.S");
			System.out.println("[" + sdf.format(cal.getTime()) + "] ERROR: \033[1m" + log + "\033[22m\033[0m");
			SetConsoleColor(Color.WHITE);
		}
	}

	public static CommunicationManager GetMerlinController() {
		return MerlinController;
	}
}
