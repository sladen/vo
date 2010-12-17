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

import java.awt.Color;
import java.awt.Component;
import java.awt.event.MouseEvent;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import cds.aladin.Coord;
import cds.tools.Util;

public class TableNside extends JTable {

	static int  DEFAULT_BITPIX = 32;
	private int 	bitpix = DEFAULT_BITPIX; // bitpix par défaut pour les calculs d'espace disque
	static String 	TAB_RESO = "Angular resolution";
	static String 	TAB_RESO_NSIDE = "Healpix nside";
	static String 	TAB_RESO_ORDER = "File (512²) order";
	static String 	TAB_RESO_DISK = "Disk space";
	static String[] columnNames = { "", TableNside.TAB_RESO,
			TableNside.TAB_RESO_NSIDE, TableNside.TAB_RESO_DISK,"" };
	static String[] columnToolTips = {"","Angular best resolution of a pixel in the built allsky image",
		"Number of subdivision of one healpix face", TableNside.getDiskHeader(DEFAULT_BITPIX),""};
	
	static final int NSIDE_IDX = 2;
	private static final int ORDER_IDX = 4;

	TableNside() {
		super(createData(DEFAULT_BITPIX),columnNames);
		setAutoscrolls(true);
		// cache la colonne des niveaux
		this.getColumnModel().removeColumn(getColumnModel().getColumn(ORDER_IDX));
		int vColIndex = 1; 
		TableColumn col = getColumnModel().getColumn(vColIndex);
		col.setPreferredWidth(100); 
		vColIndex = 2; 
		col = getColumnModel().getColumn(vColIndex);
		col.setPreferredWidth(100); 
		vColIndex = 3; 
		col = getColumnModel().getColumn(vColIndex);
		col.setPreferredWidth(100); 
	}
	
	/**
	 * Crée un header avec un tooltip
	 */
	protected JTableHeader createDefaultTableHeader() {
        return new JTableHeader(columnModel) {
            public String getToolTipText(MouseEvent e) {
                java.awt.Point p = e.getPoint();
                int index = columnModel.getColumnIndexAtX(p.x);
                int realIndex = 
                        columnModel.getColumn(index).getModelIndex();
                return columnToolTips[realIndex];
            }
        };
    }


	public void setBitpix(int newbitpix) {
		bitpix = newbitpix;
		setDiskHeader(newbitpix);
		this.updateData();
	}
	private static String getDiskHeader(int bitpix) {
		return  "Max disk space (bitpix="+bitpix+")";
	}
	public static void setDiskHeader(int bitpix) {
		columnToolTips[3] = TableNside.getDiskHeader(bitpix);
	}
	private Color defaultColor = null;
	int defaultRow = -1;
	private int defaultOrder = -1;

	public void setDefaultRow(int row) {
		defaultRow = row;
	}

	public int getDefaultRow() {
		return defaultRow;
	}

	public int setSelectedOrder(int order) {
		defaultOrder = order;
		for (int i = 0; i < getRowCount(); i++) {
			if ((Float)getValueAt(i,ORDER_IDX) == order) {
				setValueAt(Boolean.TRUE,i,0);
				return i;
			}
		}
		return -1;
	}

	public void reset() {
		defaultOrder = -1;
		for (int i = 0; i < this.getColumnCount(); i++) {
			setValueAt(new Boolean(false),i,0);
		}
	}
	
	/**
	 * Fait un rendu différent (coloré bleu) pour la ligne conseillée par défaut
	 * + ajoute un tooltip sur la colonne des nsides
	 */
	public Component prepareRenderer(TableCellRenderer renderer,
			int rowIndex, int colIndex) {
		Component c = super.prepareRenderer(renderer, rowIndex, colIndex);
		Color color = getBackground();
		if (rowIndex==defaultRow && !isRowSelected(rowIndex))
			color = new Color(204, 234, 234); // bleuté
		c.setBackground(color);

		// ajoute un tooltip sur la colonne des nsides
		if (colIndex==NSIDE_IDX && c instanceof JComponent)
			((JComponent)c).setToolTipText(getOrder(rowIndex));
		return c;
	}


	/**
	 * @param bitpix
	 */
	private static Object[][] createData(int bitpix) {
		Object[][] data;
		// Tableau des résolutions
		data = new Object[9][5];
		
		// colonne des checkbox
		for (int i = 0; i < data.length; i++) {
			data[i][0] = new Boolean(false);
		}
		// colonne des ordres
		for (int i = 0; i < data.length; i++) {
			data[i][ORDER_IDX] = (float) i + 3;
		}
		// colonne des nsides
		for (int i = 0; i < data.length; i++) {
			data[i][NSIDE_IDX] = (int) Math.pow(2., (Float) data[i][ORDER_IDX] + DBBuilder.ORDER);
		}
		// colonne des resolutions
		for (int i = 0; i < data.length; i++) {
			// float val = (float) Math.sqrt
			// (4.*Math.PI*(180./Math.PI)*(180./Math.PI)*3600.*3600.
			// /(12.*(Float)data[i][2]*(Float)data[i][2]));
			float val = (float) Math.sqrt(4. * Math.PI * (180. / Math.PI)
					* (180. / Math.PI)
					/ (12. * (Integer) data[i][NSIDE_IDX] * (Integer) data[i][NSIDE_IDX]));
			data[i][1] = Coord.getUnit(val);
		}
		// colonne des volumes
		for (int i = 0; i < data.length; i++) {
			// fichiers de 1M (512x512 pixels en 32 bits)
			// nombre de fichiers 12 * 4^order
			float fact = Math.abs(bitpix)/32;
			double val = 0;
			for (int o = i; o >= 0; o--) {
				val += fact * (Math.pow(4., (Float) data[o][ORDER_IDX]) * 12.);
			}
			data[i][3] = Util.getUnitDisk(val * 1024. * 1024.);
		}
		return data;
	}
	

	/**
	 * Récupère le chiffre dans la colonne "order" de la ligne cochée
	 * 
	 * @return order choisi ou celui par défaut si rien n'est coché ou -1
	 */
	public int getOrder() {
		for (int i = 0; i < getRowCount(); i++) {
			if (getValueAt(i, 0) == Boolean.TRUE) {
				return ((Float) getValueAt(i, ORDER_IDX)).intValue();
			}
		}
		return defaultOrder;
	}

	/**
	 * Prépare un texte pour afficher le niveau dans un tooltip
	 * @param row
	 * @return
	 */
	public String getOrder(int row) {
		return "<html>512 x 2<sup>"+((Float) getValueAt(row, ORDER_IDX)).intValue()+"</sup></html>";
//		return String.format("2$1",((Float) getValueAt(row, ORDER_IDX)).intValue());
	}
	
	public void updateData() {
		// colonne des volumes
		for (int i = 0; i < this.getRowCount(); i++) {
			// fichiers de 1M (512x512 pixels en 32 bits)
			// nombre de fichiers 12 * 4^order
			float fact = Math.abs(bitpix)/32.f;
			double val = 0;
			for (int o = i; o >= 0; o--) {
				val += fact * (Math.pow(4., (Float) getValueAt(o,ORDER_IDX)) * 12.);
			}
			this.setValueAt(Util.getUnitDisk(val * 1024. * 1024.),i,3);
		}
		repaint();
	}
	
	@Override
	public Object getValueAt(int row, int column) {
		return super.getModel().getValueAt(row, column);
	}

	public Class<? extends Object> getColumnClass(int c) {
        return getValueAt(0, c).getClass();
    }

	public void setValueAt(Object value, int row, int col) {
		if (col != 0) {
			super.setValueAt(value, row, col);
			return;
		}
		// interdit de décocher une ligne déjà cochée
		if (value == Boolean.TRUE && getValueAt(row, 0) == Boolean.TRUE) {
			return;
		}
		// vérifie qu'une autre ligne n'est pas déjà cochée, si oui, on la
		// décoche
		for (int i = 0; i < getRowCount(); i++) {
				super.setValueAt(new Boolean(false), i, 0);
		}
		super.setValueAt(value, row, col);
	}
	

	public boolean isCellEditable(int row, int col) {
		if (col == 0)
			return true;
		return false;
	}


}
