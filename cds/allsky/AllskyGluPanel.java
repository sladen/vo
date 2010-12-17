// Copyright 2010 - UDS/CNRS
// The Aladin program is distributed under the terms
// of the GNU General Public License version 3.
//
//This file is part of Aladin.
//
//    Aladin is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, version 3 of the License.
//
//    Aladin is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General Public License for more details.
//
//    The GNU General Public License is available in COPYING file
//    along with Aladin.
//

package cds.allsky;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;

public class AllskyGluPanel extends JPanel {
	
	JTextField field = new JTextField(40);
	private JTextArea glu;

	public AllskyGluPanel(GridBagLayout layout, String path) {
		super(layout);
		init(path);
	}

	public void init(String path) {
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(1, 3, 1, 3);
		c.anchor = GridBagConstraints.WEST;
		c.gridx = GridBagConstraints.RELATIVE;
		c.gridy=0;
		JLabel titre = new JLabel("Open your ALLSKY");
		titre.setFont(titre.getFont().deriveFont(Font.BOLD));
		add(titre,c);
		field.setText(path);
		JButton b = new JButton("Browse...");
		field.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {
				String txt = glu.getText();
				String path = field.getText();
				txt = txt.replaceAll("ALLSKY FROM.*", "ALLSKY FROM " + path);
				String FS = System.getProperty("file.separator");
				int index = path.lastIndexOf(FS);
				String path_1, last;
				if (index==-1) {
					path_1 = path;
					last = "";
				}
				else {
					path_1 = path.substring(0, index);
					last = path.substring(index+1);
				}
				txt = txt.replaceAll("%U.*", "%U " + path_1);
				txt = txt.replaceAll("Aladin.Label.*", "Aladin.Label " + last);
				txt = txt.replaceAll("Aladin.Survey.*", "Aladin.Survey " + last);
				glu.setText(txt);
				glu.updateUI();
			}

		});
		c.gridy++;
		add(field,c);
		add(b,c);

		c.gridy++;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(new JSeparator(JSeparator.HORIZONTAL),c);
		c.fill = GridBagConstraints.NONE;
		c.gridy++;

		titre = new JLabel("Share thanks to this Glu entry");
		titre.setFont(titre.getFont().deriveFont(Font.BOLD));
		add(titre,c);

		c.gridy++;
		glu = new JTextArea(10,40);
		glu.setText(
				"%A MYALLSKY\n" +
				"%D MY OWN ALLSKY FROM\n" +
				"%O CDS'aladin\n" +
				"%Z ALADIN\n" +
				"%U \n" +
				"Aladin.Label \n" +
				"Aladin.HpxParam 3 \n" +
				"Aladin.Survey \n"

		);

		c.gridy++;
		b = new JButton("Open");
		b.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// TODO ???
//				aladin.calque.newPlanBG(f,in,label,PlanBG.DRAWPIXEL,0);
			}

	});
    
}
}
