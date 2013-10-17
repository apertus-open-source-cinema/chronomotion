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

import javax.bluetooth.RemoteDevice;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JList;
import java.awt.BorderLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.Vector;

import net.miginfocom.swing.MigLayout;
import java.awt.Font;
import java.io.IOException;

import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.JRadioButton;
import javax.swing.ListSelectionModel;
import javax.swing.AbstractListModel;
import javax.swing.JComboBox;
import javax.swing.DefaultComboBoxModel;

public class ConnectWindow {

	private Chronomotion Parent;
	private JDialog frame;
	private JList ComPortList;
	private JComboBox BTDevicesBox;
	private DefaultComboBoxModel BTPortListModel;
	private DefaultListModel ComPortListModel;
	private JRadioButton rdbtnCableConnection;
	private JRadioButton rdbtnBluetoothConnection;

	public ConnectWindow(Chronomotion parent) {

		Parent = parent;
		frame = new JDialog();
		frame.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		frame.setBounds(100, 100, 450, 300);

		JButton btnNewButton = new JButton("Connect");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Connect();
				frame.dispose(); // close the JDialog
			}
		});
		frame.getContentPane().setLayout(new MigLayout("", "[27px][407px,grow]", "[][239px][][][grow][23px]"));

		rdbtnCableConnection = new JRadioButton("Serial Cable Connection");
		rdbtnCableConnection.setSelected(true);
		frame.getContentPane().add(rdbtnCableConnection, "flowx,cell 0 0 2 1");

		rdbtnBluetoothConnection = new JRadioButton("Bluetooth Connection");
		frame.getContentPane().add(rdbtnBluetoothConnection, "cell 0 2 2 1");

		ButtonGroup ConnectionSelection = new ButtonGroup();
		ConnectionSelection.add(rdbtnCableConnection);
		ConnectionSelection.add(rdbtnBluetoothConnection);

		BTDevicesBox = new JComboBox();

		frame.getContentPane().add(BTDevicesBox, "cell 1 3,growx");
		frame.getContentPane().add(btnNewButton, "cell 0 5 2 1,growx,aligny top");

		JLabel lblConnect = new JLabel("Port: ");
		frame.getContentPane().add(lblConnect, "cell 0 1,alignx left,growy");

		ComPortList = new JList();
		frame.getContentPane().add(ComPortList, "cell 1 1,grow");
	}

	public void Connect() {
		
		if (rdbtnCableConnection.isSelected()) { // Is the Radiobutton to use Serial connection checked?
			if (!ComPortList.isSelectionEmpty()) { // Is a COM port selected?
				Parent.GetMerlinController().InitSerial(ComPortList.getSelectedValue().toString());
			}
		}
		
		if (rdbtnBluetoothConnection.isSelected()) { // Is the Radiobutton to use Bluetooth connection checked?
			if (true) { // TODO: Is a device detected?
				Parent.GetMerlinController().InitBT(BTDevicesBox.getSelectedItem().toString());
			}
		}
		this.Show(false);
	}

	public void Load() {
		// Find all Serial COM Ports
		ArrayList<CommPortIdentifier> h = Parent.GetMerlinController().getAvailableSerialPorts();
		ComPortListModel = new DefaultListModel();
		if (h.size() == 0) {
			ComPortListModel.addElement("No COM Port detected");
		} else {
			for (int i = 0; i < h.size(); i++) {
				ComPortListModel.addElement(h.get(i).getName());
			}
		}
		ComPortList.setModel(ComPortListModel);

		// Find all Bluetooth devices
		Parent.GetMerlinController().InquireAvailableBluetoothPorts();
		Vector BTdevicelist = Parent.GetMerlinController().GetAvailableBluetoothPorts();
		BTPortListModel = new DefaultComboBoxModel();
		if (BTdevicelist.size() == 0) {
			BTPortListModel.addElement("No Bluetooth device detected");
		} else {
			for (int i = 0; i < BTdevicelist.size(); i++) {
				try {
					BTPortListModel.addElement(BTdevicelist.get(i).toString() + " - " + (((RemoteDevice) BTdevicelist.get(i)).getFriendlyName(false)));
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		BTDevicesBox.setModel(BTPortListModel);
	}

	public void Show(boolean show) {
		Load();
		frame.setVisible(show);
	}

}
