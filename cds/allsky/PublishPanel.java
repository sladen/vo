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

import java.awt.FileDialog;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import cds.aladin.Aladin;
import cds.aladin.Chaine;
import cds.aladin.TreeNodeAllsky;
import cds.tools.Util;

public class PublishPanel extends JPanel implements ActionListener {

	private static final String PUBLISH = "PUBLISH";
//    private static final String CREATE_MENU = "CREATE MENU";
    private String OPEN; 
    private final static String TEST_FULL = "Try your survey";
    private final static String LOCAL = "Just for you"; 
    private final static String LOCAL_FULL = "Use the menu \"File -> Open\" on the newly built directory";
    private final static String HPX = "Restricted access"; 
    private final static String HPX_FULL = 
    	"<html>Export your data in a healpix map and send it to your collaborators.<br>" +
    	"This file is following the healpix format NESTED, and will use pixels values <br>" +
    	"for the angular resolution of 51\" (nside=4096).</html>";
//    private final static String LOCAL_FULL = "Create an item in the menu \"File -> Allsky\" to go " +
//            "right to this allsky survey";
//	private final static String LOCAL_END = "The following entry has been added to your AlaGlu.dic\n"+
//		"Please keep available the path to your directory";
	
	private final static String PUBLIC = "Public distribution";
	private final static String PUBLIC_FULL = "If you want other people to access your data, " +
			"you need to allow an HTTP access to your newly built directory (jpg files, see Tab 3)"; 
	private final static String PUBLIC_HTTP = "URL of your server : ";
	private JTextField url = new JTextField(30);
    private JButton bLocal = new JButton(); 
	private JButton bPublic = new JButton(PUBLISH);
	private JButton bExport = new JButton("Export to HPX Map");
	
	private Aladin aladin;
	AllskyPanel allsky;
	
	public PublishPanel(Aladin a,AllskyPanel allskyPanel) {
		super(new GridBagLayout());
		aladin = a;
		allsky = allskyPanel;
		createChaine(aladin.getChaine());
		
		Border emptyBorder = BorderFactory.createEmptyBorder(30, 0, 0, 0);
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(1, 3, 1, 3);
		c.anchor = GridBagConstraints.NORTHWEST;
	    c.gridy = 0;
	    
	    // local
        c.gridy++;
	    c.gridx = 0;
	    JLabel titleLocal = new JLabel(LOCAL);
	    titleLocal.setFont(titleLocal.getFont().deriveFont(Font.BOLD));
	    add(titleLocal,c);
	    c.gridy++;
	    c.gridwidth =GridBagConstraints.REMAINDER;// remplit toute la ligne
	    add(new JLabel(LOCAL_FULL),c);
	    c.gridwidth=1;
	    c.gridx++;c.gridy++;
	    bLocal.setText(OPEN);
	    bLocal.addActionListener(this);
	    add(bLocal,c);
	    c.gridy++;
	    c.gridx=0;
	    
	    // Export HPX
	    JLabel titleHPX = new JLabel(HPX);
	    titleHPX.setFont(titleHPX.getFont().deriveFont(Font.BOLD));
	    titleHPX.setBorder(emptyBorder);
	    add(titleHPX,c);
	    c.gridy++;
	    c.gridwidth =GridBagConstraints.REMAINDER;// remplit toute la ligne
	    add(new JLabel(HPX_FULL),c);
	    c.gridwidth=1;
	    c.gridx++;c.gridy++;
	    bExport.addActionListener(this);
	    add(bExport,c);
	    
	    // public
	    c.gridy++;
	    c.gridx = 0;
	    JLabel titlePublic = new JLabel(PUBLIC);
	    titlePublic.setFont(titleLocal.getFont());
	    titlePublic.setBorder(emptyBorder);
	    add(titlePublic,c);
	    c.gridx++;
	    add(new JLabel(""));
	    c.gridx = 0;
	    c.gridy++;
	    c.gridwidth =GridBagConstraints.REMAINDER;// remplit toute la ligne
	    add(new JLabel(PUBLIC_FULL),c);
	    c.gridwidth=1;
	    c.gridy++;
	    c.fill = GridBagConstraints.NONE;
	    c.gridx++;
	    add(url,c);
	    c.gridy++;
	    add(bPublic,c);
	    bPublic.addActionListener(this);
	    newAllskyDir(AllskyConst.SURVEY);
	}
	
	public void clearForms() {
		url.setText("");
	}
	
	private void createChaine(Chaine chaine) {
		OPEN = chaine.getString("MOPENLOAD");
	}
	private TreeNodeAllsky gluSky = null;

	/**
	 * génération de l'entrée GLU dans le dico interne
	 */
	public void createGluSky(boolean flagLocal) {
        String name       = allsky.getLabel();
        String actionName = name+".local.hpx";
		String shortDescr = "My short description of "+name;
		String fullDescr  = "My long description of "+name;
		String hpxParam   = allsky.getOrder()+ (flagLocal ? " fits":" jpeg fits");
		String profile    = "localdef";
		String copyright  = allsky.getCopyright();
		String path       = "Local/"+name;
		String u          = flagLocal ? Util.concatDir(allsky.getOutputPath(),null) : url.getText();
		
		gluSky = new TreeNodeAllsky(aladin, actionName, "zzz", u, name, shortDescr, fullDescr, 
		      profile,copyright, null, path, hpxParam);
		
		aladin.glu.addGluSky(gluSky);
		aladin.glu.writeGluAppDic();
	}

	public void newAllskyDir(String dir) {
	    url.setText("http://servername.org/"+dir);
	    url.repaint();
	}
	
	public void actionPerformed(ActionEvent ae) {
		if (ae.getActionCommand() == OPEN) {
			openLocal();
		}
		else
		if (ae.getActionCommand() == PUBLISH) {
			createGluSky(false);
			String glurec = gluSky.getGluDic();
			System.out.println(glurec);
//			JOptionPane.showMessageDialog(null,LOCAL_END+"\n\n"+glurec);
		}
		else if (ae.getSource() == bExport) {
			allsky.export();
		}
	}

	/**
	 * 
	 */
	private void openLocal() {
		String f = dirBrowser();
		f=aladin.getFullFileName(f);
		aladin.creatLocalPlane(f,f.substring(f.lastIndexOf(Util.FS)-1));
	}
	

	/** Ouverture de la fenêtre de sélection d'un fichier */
	private String dirBrowser() {
		FileDialog fd = new FileDialog(aladin.frameAllsky,"Running directory selection");
		fd.setDirectory(allsky.getOutputPath());

		// (thomas) astuce pour permettre la selection d'un repertoire
		// (c'est pas l'ideal, mais je n'ai pas trouve de moyen plus propre en AWT)
		fd.setFile("-");
		fd.setVisible(true);
		String selectdir = fd.getDirectory();
		String name =  fd.getFile();
		// si on n'a pas changé le nom, on a selectionne un repertoire
		if( name!=null && name.equals("-") ) name = "";
		String t = (selectdir==null?"":selectdir)+(name==null?"":name);
		return t;
	}

	
	
}
