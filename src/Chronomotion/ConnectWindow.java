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

public class ConnectWindow {

	private Chronomotion Parent;
	private JDialog frame;
	private JList ComPortList;
	private DefaultListModel ComPortListModel;

	public ConnectWindow(Chronomotion parent) {

		Parent = parent;
		frame = new JDialog();
		frame.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		frame.setBounds(100, 100, 450, 300);

		JButton btnNewButton = new JButton("Connect");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Connect();
			}
		});
		frame.getContentPane().add(btnNewButton, BorderLayout.SOUTH);

		JLabel lblConnect = new JLabel("Connect: ");
		frame.getContentPane().add(lblConnect, BorderLayout.WEST);

		ComPortList = new JList();
		frame.getContentPane().add(ComPortList, BorderLayout.CENTER);
	}

	public void Connect() {
		Parent.GetMerlinController().Init(ComPortList.getSelectedValue().toString());
	}

	public void Load() {
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
	}

	public void Show(boolean show) {
		Load();
		frame.setVisible(show);
	}

}
