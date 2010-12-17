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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JTable;

import cds.fits.Fits;

public class BuildPanel extends JPanel implements ActionListener {
	protected static final String BEST = "best";
	protected static final String FIRST = "first";
	final private String RESO = "Which pixel resolution do you want for your ALLSKY ?";
	final private String SAMERESO = "About the same as original images";	

	private static JTable tab = null;

	final private String METHOD_LABEL = "Algorithms to manage pixels";
	final private String FAST_METHOD = "First (fast)";
	final private String BILI_METHOD = "Bilinear";
	final private String AVERAGE_METHOD = "Averages with fading";
	
	private JLabel 	method_label = new JLabel(METHOD_LABEL);
	private ButtonGroup 	groupSampl = new ButtonGroup();
	private ButtonGroup 	groupOverlay = new ButtonGroup();
	private JRadioButton 	samplFast = new JRadioButton(FAST_METHOD);
	private JRadioButton 	overlayFast = new JRadioButton(FAST_METHOD);
	private JRadioButton 	samplBest = new JRadioButton(BILI_METHOD);
	private JRadioButton 	overlayBest = new JRadioButton(AVERAGE_METHOD);
	private ActionListener 	methodsListener = new MethodSelectListener(
			new JRadioButton[] {samplFast, overlayFast, samplBest, overlayBest});

	final private JLabel labelSampl = new JLabel("Sampling :");
	final private JLabel labelOverlay = new JLabel("Overlay :");
	
	final private String BITPIX = "Bitpix size for your output ALLSKY images";
	final private String CHECKKEEP = "Build only missing (don't overwrite)";
	final private String BIT8 = "short (8bits)";
	final private String BIT16 = "int (16bits)";
	final private String BIT32 = "long int (32bits)";
	final private String BIT_32 = "float real (32bits)";
	final private String BIT64 = "double real (64bits)";

	private ButtonGroup 	groupBitpix = new ButtonGroup();
	private JCheckBox 	keepBB = new JCheckBox("Keep original coding (only for homogenous images)");

	private ActionListener 	bitpixListener = new BitpixListener(keepBB);
	private JRadioButton 	bit8 = new JRadioButton(BIT8, false);
	private JRadioButton 	bit16 = new JRadioButton(BIT16, false);
	private JRadioButton 	bit32 = new JRadioButton(BIT32, true);
	private JRadioButton 	bit_32 = new JRadioButton(BIT_32, false);
	private JRadioButton 	bit_64 = new JRadioButton(BIT64, false);

	private double bscale;
	private double bzero;
	private double blank;
	private int bitpixO = -1;
	private JLabel bitpixLabel = new JLabel(BITPIX);
	private JLabel resoLabel;

	public BuildPanel() {
		super(new GridBagLayout());

		init();
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;

		// Zone de sélection du bitpix
		c.gridy++;
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(bitpixLabel, c);
		c.gridwidth = 1;
		c.gridy++;
		add(bit8, c);
		c.gridx++;
		add(bit16, c);
		c.gridx++;
		add(bit32, c);
		c.gridy++;
		c.gridx = 0;
		add(bit_32, c);
		c.gridx++;
		add(bit_64, c);
		c.gridx++;
		add(keepBB, c);

		// Zone de sélection de la résolution
		c.gridx = 0;
		c.gridy++;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(new JSeparator(JSeparator.HORIZONTAL), c);
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;

		c.gridy++;
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(resoLabel, c);
		c.gridwidth = 1;

		// Tableau des résolutions
		c.gridx = 0;
		c.gridy++;
		c.anchor = GridBagConstraints.CENTER;
		c.gridwidth = GridBagConstraints.REMAINDER;

		tab.setRowSelectionAllowed(false);

		add(tab.getTableHeader(), c);
		c.gridy++;
		add(tab, c);
		c.gridwidth = 1;

		// méthode fast=plus proche / best=bilinéaire
		c.gridx = 0;
		c.gridy++;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(new JSeparator(JSeparator.HORIZONTAL), c);
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = 1;
		c.gridy++;
		add(method_label, c);

		c.gridx = 0;
		c.gridy++;
		add(labelSampl,c);
		c.gridx++;
		add(samplFast, c);
		c.gridx++;
		add(samplBest, c);

		c.gridx = 0;
		c.gridy++;
		add(labelOverlay,c);
		c.gridx++;
		add(overlayFast, c);
		c.gridx++;
		add(overlayBest, c);

	}
	
	public void init() {
		tab = new TableNside();
		resoLabel = new JLabel(RESO);
		resoLabel.setFont(resoLabel.getFont().deriveFont(Font.BOLD));
		
		// bitpix
		bitpixLabel.setFont(bitpixLabel.getFont().deriveFont(Font.BOLD));
		keepBB.setEnabled(false);
		initBitpix();
		
		// methodes
		method_label.setFont(resoLabel.getFont().deriveFont(Font.BOLD));
		samplBest.setSelected(true);
		overlayBest.setSelected(true);
		initMethods();
		initListenerBitpix();
	}

	/**
	 * 
	 */
	private void initMethods() {
		groupSampl.add(samplBest);
		groupSampl.add(samplFast);
		groupOverlay.add(overlayBest);
		groupOverlay.add(overlayFast);
		samplBest.addActionListener(methodsListener);
		samplFast.addActionListener(methodsListener);
		overlayBest.addActionListener(methodsListener);
		overlayFast.addActionListener(methodsListener);
		
		samplBest.setActionCommand(BEST);
		samplFast.setActionCommand(FIRST);
		overlayBest.setActionCommand(BEST);
		overlayFast.setActionCommand(FIRST);
	}

	private void initListenerBitpix() {
		bit8.addActionListener(bitpixListener);
		bit16.addActionListener(bitpixListener);
		bit32.addActionListener(bitpixListener);
		bit_32.addActionListener(bitpixListener);
		bit_64.addActionListener(bitpixListener);
	}

	private void initBitpix() {
		bit8.setActionCommand("8");
		bit16.setActionCommand("16");
		bit32.setActionCommand("32");
		bit_32.setActionCommand("-32");
		bit_64.setActionCommand("-64");

		bit8.addActionListener(this);
		bit16.addActionListener(this);
		bit32.addActionListener(this);
		bit_32.addActionListener(this);
		bit_64.addActionListener(this);

		groupBitpix.add(bit8);
		groupBitpix.add(bit16);
		groupBitpix.add(bit32);
		groupBitpix.add(bit_32);
		groupBitpix.add(bit_64);

	}
	
	public void clearForms() {
		bitpixO = -1;
		bit8.setSelected(false);
		bit16.setSelected(false);
		bit32.setSelected(true);
		bit_32.setSelected(false);
		bit_64.setSelected(false);
		blank = Fits.DEFAULT_BLANK;
		bscale = Fits.DEFAULT_BSCALE;
		bzero = Fits.DEFAULT_BZERO;
		samplBest.setSelected(true);
		overlayBest.setSelected(true);
		((TableNside) tab).reset();
	}

	public int setSelectedOrder(int val) {
		int i = ((TableNside) tab).setSelectedOrder(val);
		((TableNside) tab).setDefaultRow(i);
		tab.repaint();
		return i;
	}

	/**
	 * 
	 * @return order choisi ou -1 s'il doit etre calculé
	 */
	public int getOrder() {
		return ((TableNside) tab).getOrder();
	}

	public void setBScaleBZero(double bscale, double bzero) {
		this.bscale = bscale;
		this.bzero = bzero;
	}

	public void setBlank(double blank) {
		this.blank = blank;
	}

	public double getBscale() {
		// si ce n'est pas le bitpix original
		// on renvoie une valeur par défaut
		if (this.bitpixO != getBitpix())
			return Fits.DEFAULT_BSCALE;
		return bscale;
	}

	public double getBzero() {
		// si ce n'est pas le bitpix original
		// on renvoie une valeur par défaut
		if (this.bitpixO != getBitpix())
			return Fits.DEFAULT_BZERO;
		return bzero;
	}

	public double getBlank() {
		// si ce n'est pas le bitpix original
		// on renvoie une valeur par défaut
		if (this.bitpixO != getBitpix())
			return Fits.DEFAULT_BLANK;
		return blank;
	}
	
	public boolean isKeepBB() {
		return keepBB.isSelected();
	}

	public boolean toFast() {
		return samplFast.isSelected();
	}

	public void setOriginalBitpix(int bitpix) {
		this.bitpixO = bitpix;
		((BitpixListener) bitpixListener).setDefault(bitpix);
		switch (bitpix) {
		case 8:
			bit8.doClick();
			break;
		case 16:
			bit16.doClick();
			break;
		case 32:
			bit32.doClick();
			break;
		case -32:
			bit_32.doClick();
			break;
		case 64:
			bit_64.doClick();
			break;

		}
	}

	public int getOriginalBitpix() {
		return bitpixO;
	}


	/**
	 * Renvoie le bitpix sélectionné dans le formulaire
	 * 
	 * @return
	 */
	public int getBitpix() {
		ButtonModel b = groupBitpix.getSelection();
		int i = TableNside.DEFAULT_BITPIX;
		try {
			i = Integer.parseInt(b.getActionCommand());
		} catch (NumberFormatException e) {
			e.printStackTrace();
		}
		return i;
	}

	public void actionPerformed(ActionEvent arg0) {
		// on applique aussi la modification dans le tableau (calcul des volumes
		// disques)
		((TableNside) tab).setBitpix(getBitpix());

	}

}

