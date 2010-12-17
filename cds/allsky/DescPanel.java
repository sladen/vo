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
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.Border;

import cds.aladin.Chaine;
import cds.tools.Util;

public class DescPanel extends JPanel implements ActionListener {

	private String REP_SOURCE;
	private String REP_DEST;
    private String REP_DEST_RESET;
	private static final String s_index = "Indexation";// ("+AllskyConst.HPX_FINDER+" subdir)";
	private static final String s_hpxfiles = "Healpix diamonds";
//	private static final String s_jpg = "JPG diamonds";

	private JLabel destLabel;
	private JLabel sourceLabel;
	private String LABEL_ALLSKY;
	private String DESC_ALLSKY;
	private String DESCFULL_ALLSKY;
	private String COPYR_ALLSKY;
	private String ORIGIN_ALLSKY;
	
	private String descfullSuff = " allsky survey";
	
//	private JCheckBox 	reset = new JCheckBox();
//	private JButton 	reset = new JButton();
	private JLabel		resetLabel = new JLabel();
	private JCheckBox	resetIndex = new JCheckBox();
	private JCheckBox	resetHpx = new JCheckBox();
//	private JCheckBox	resetJpg = new JCheckBox();
	private JButton 	browse_S = new JButton();
	private JButton 	browse_D = new JButton();
	private JTextField 	dir_S = new JTextField(30);
	private JTextField 	dir_D = new JTextField(30);
    //	private JTextField dir_A = new JTextField(AllskyConst.SURVEY,10);
	private JTextField 	label = new JTextField(30);
	private JTextField 	desc = new JTextField(30);
	private JTextField 	descfull = new JTextField(30);
	private JTextField 	copyright = new JTextField(30);
	private JTextField 	origin = new JTextField(30);
	private String defaultDirectory;
	private AllskyPanel parentPanel;
	private String BROWSE;
	
	public DescPanel(String defaultDir, AllskyPanel parent) {
		super();
		createChaine(parent.aladin.getChaine());
		init();
		
		setLayout(new GridBagLayout());
		this.defaultDirectory = defaultDir;
		this.parentPanel = parent;
		
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(1, 3, 1, 3);
		c.anchor = GridBagConstraints.NORTHWEST;
	    c.gridy = 0;
	    
		// Zone de sélection des répertoires
	    c.gridx = 0;
	    add(sourceLabel,c);
	    c.gridx++;
	    
	    add(dir_S,c);
	    c.gridx++;
	    add(browse_S,c);
	    
	    c.gridy++;
	    c.gridx = 0;
		add(destLabel,c);
	    c.gridx++;
	    add(dir_D,c);
	    c.gridx++;
	    add(browse_D,c);

	    
	    c.gridy++;
	    c.gridx = 0;
	    c.gridwidth = 2;
	    c.anchor = GridBagConstraints.CENTER;
	    c.insets.bottom=30;
	    add(resetLabel,c);
	    c.gridx++;
	    add(resetIndex,c);
	    c.gridx++;
	    add(resetHpx,c);
//	    c.gridx++;
//	    add(resetJpg,c);
	    c.insets.bottom=1;
	    
	    c.gridwidth =GridBagConstraints.REMAINDER;
	    
	    c.anchor = GridBagConstraints.NORTHWEST;
	    c.gridwidth=1;
	    
	    // Zone de description pour la sortie GLU
	    c.gridy++;
	    c.gridx = 0;
	    add(new JLabel(LABEL_ALLSKY),c);
	    c.gridx++;
	    add(label,c);

	    c.gridy++;
	    c.gridx = 0;
	    add(new JLabel(DESC_ALLSKY),c);
	    c.gridx++;
	    add(desc,c);

	    c.gridy++;
	    c.gridx = 0;
	    add(new JLabel(DESCFULL_ALLSKY),c);
	    c.gridx++;
	    add(descfull,c);

	    c.gridy++;
	    c.gridx = 0;
	    add(new JLabel(COPYR_ALLSKY),c);
	    c.gridx++;
	    add(copyright,c);

	    c.gridy++;
	    c.gridx = 0;
	    add(new JLabel(ORIGIN_ALLSKY),c);
	    c.gridx++;
	    add(origin,c);
	    
        // Juste pour avoir un seul code - on l'enlèvera plus tard
        if( parentPanel.aladin.isProto() ) {
           final JCheckBox cb = new JCheckBox("DSS Schmidt plates",false);
           cb.addActionListener(new ActionListener() {
              public void actionPerformed(ActionEvent e) { DBBuilder.DSS = cb.isSelected(); }
           });
           c.gridx=0;
           c.gridy++;
           add(new JLabel("Prototype flags:"),c);
           c.gridx++;
           add(cb,c);
        }
	}

	private void createChaine(Chaine chaine) {
		REP_SOURCE = chaine.getString("REPSALLSKY");
		BROWSE = chaine.getString("FILEBROWSE");
		REP_DEST = chaine.getString("REPDALLSKY");
		REP_DEST_RESET = chaine.getString("REPRESALLSKY");
		LABEL_ALLSKY = chaine.getString("LABELALLSKY");
		DESC_ALLSKY = chaine.getString("DESCALLSKY");
		DESCFULL_ALLSKY = chaine.getString("DESCFALLSKY");
		COPYR_ALLSKY = chaine.getString("COPYRIGHT");
		ORIGIN_ALLSKY = chaine.getString("ORIGIN");
	}
	
	public void init() {
		Border emptyBorder = BorderFactory.createEmptyBorder(0, 0, 30, 0);
		destLabel = new JLabel(REP_DEST);
		sourceLabel = new JLabel(REP_SOURCE);
		sourceLabel.setFont(sourceLabel.getFont().deriveFont(Font.BOLD));
		sourceLabel.setFont(sourceLabel.getFont().deriveFont(sourceLabel.getFont().getSize2D()+2));
		sourceLabel.setBorder(emptyBorder);
		dir_S.addActionListener(this);
		dir_S.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				super.focusLost(e);
				if (!dir_S.getText().equals(""))
					actionPerformed(new ActionEvent(dir_S,-1, "dirBrowser Action"));
			}
		});
		browse_S.setText(BROWSE);
		browse_S.addActionListener(this);
		dir_D.addActionListener(this);
		dir_D.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				super.focusLost(e);
				if (!dir_D.getText().equals(""))
				actionPerformed(new ActionEvent(dir_D,-1, "dirBrowser Action"));
			}
		});
		browse_D.setText(BROWSE);
		browse_D.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { dirBrowser(dir_D); }
		});
		resetLabel.setText(REP_DEST_RESET);
		resetIndex.setText(s_index);
		resetIndex.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (resetIndex.isSelected())
					parentPanel.aladin.frameAllsky.setRestart(); 
				else if (!resetHpx.isSelected())
					parentPanel.aladin.frameAllsky.setResume();
			}
		});
		resetHpx.setText(s_hpxfiles);
		resetHpx.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (resetHpx.isSelected())
					parentPanel.aladin.frameAllsky.setRestart(); 
				else if (!resetIndex.isSelected())
					parentPanel.aladin.frameAllsky.setResume();
				}
		});
		setResetSelected(false);
		setResetEnable(false);
	}
	
	public void clearForms() {
		dir_S.setText("");
		dir_D.setText("");
		parentPanel.actionPerformed(new ActionEvent("",-1, "dirBrowser Action"));
		label.setText("");
		desc.setText("");
		descfull.setText("");
		copyright.setText("");
		origin.setText("");
		setResetSelected(false);
		setResetEnable(false);
	}


	/** Ouverture de la fenêtre de sélection d'un fichier */
	private void dirBrowser(JTextField dir) {
		FileDialog fd = new FileDialog(parentPanel.aladin.frameAllsky,"Running directory selection");
		if( dir!=null && !dir.getText().equals("")) fd.setDirectory(dir.getText());
		else fd.setDirectory(defaultDirectory);

		// (thomas) astuce pour permettre la selection d'un repertoire
		// (c'est pas l'ideal, mais je n'ai pas trouve de moyen plus propre en AWT)
		fd.setFile("-");
		fd.setVisible(true);
		String selectdir = fd.getDirectory();
		String name =  fd.getFile();
		// Si annulé
		if (name == null)
			return;
		// si on n'a pas changé le nom, on a selectionne un repertoire
		if( name!=null && name.equals("-") ) name = "";
		String t = (selectdir==null?"":selectdir)+name;
		dir.setText(t);
		parentPanel.actionPerformed(new ActionEvent(dir,-1, "dirBrowser Action"));
	}


	public String getInputPath() {
		return dir_S.getText();
	}
	public String getOutputPath() {
		return dir_D.getText();
	}


	public JTextField getSourceDirField() {
		return dir_S;
	}


	public void actionPerformed(ActionEvent e) {

		// change le nom du bouton dans la frame si RESET est utilisé
		/*
		if (e.getSource() == reset) {
//			if (reset.isSelected())
//				parentPanel.displayStart();
//			else
//				parentPanel.displayReStart();
//			return;
			parentPanel.showClean(true);
			return;
		}
		*/
		// ajoute un FS à la fin s'il n'existe pas
		if (e.getSource() == dir_S || e.getSource() == dir_D) {
			JTextField field = ((JTextField)e.getSource());
			String txt = field.getText();
			if (!txt.endsWith(Util.FS)) {
				txt = txt+Util.FS;
				field.setText(txt);
				field.repaint();
			}
			
		}

		
		if (e.getSource() == dir_S) {
			initTxt();
		}
		else if (e.getSource() == browse_S) {
			dirBrowser(dir_S); 
    		initTxt();
    	}
		
		if (e.getSource() == dir_D) {
			newAllskyDir();
		}
		else if (e.getSource() == browse_D) {
			newAllskyDir();
    	}
		
	}


	/**
	 * Itialisation des variables textuelles en fonction du nouveau répertoire
	 * SOURCE
	 */
	private void initTxt() {
		String txt = dir_S.getText();
		int i = txt.lastIndexOf(Util.FS);
		if (i==-1)
			return;
		
		// ne traite pas le dernier séparateur
		while (i+1==txt.length()) {
			txt = txt.substring(0, i);
		}
		// cherche le dernier mot et le met dans le label
		String str = txt.substring(txt.lastIndexOf(Util.FS)+1);
		label.setText(str);
		desc.setText(str);
		descfull.setText(str+descfullSuff );
//		dir_A.setText(str+AllskyConst.SURVEY);

		// rééinitialise le répertoire de destination avec le chemin des données d'entrée
		dir_D.setText("");
		newAllskyDir();
	}


	public String getLabel() {
		return label.getText();
	}

	public String getAuthor() {
		return origin.getText();
	}

	public String getCopyright() {
		return copyright.getText();
	}


	/**
	 * Applique les modifications si le nom du répertoire DESTINATION change
	 */
	private void newAllskyDir() {
		String str = dir_D.getText();
		// enlève les multiples FS à la fin
		while (str.endsWith(Util.FS))
			str = str.substring(0, str.lastIndexOf(Util.FS));
				
		// si l'entrée est vide, on remet le défaut
		if (str.equals("")) {
			// réinitalise le répertoire SURVEY et l'utilise
			initDirD();
			parentPanel.newAllskyDir();
			return;
		}
		// cherche le dernier mot
		AllskyConst.SURVEY = str.substring(str.lastIndexOf(Util.FS)+1);
		
		// ajoute un FS à la fin
		str = str+Util.FS;
		dir_D.setText(str);
		dir_D.repaint();
		parentPanel.newAllskyDir();
	}


	private void initDirD() {
		AllskyConst.SURVEY = getLabel()+AllskyConst.ALLSKY;
		String path = dir_S.getText();
		// enlève les multiples FS à la fin
		while (path.endsWith(Util.FS))
			path = path.substring(0, path.lastIndexOf(Util.FS));
		
		dir_D.setText(path+AllskyConst.ALLSKY+Util.FS);
		dir_D.repaint();
	}


	public void setResetSelected(boolean b) {
		resetIndex.setSelected(b);
		resetHpx.setSelected(b);
	}

	public boolean toResetIndex() {
		return resetIndex.isSelected();
	}
	public boolean toResetHpx() {
		return resetHpx.isSelected();
	}

	public void setResetEnable(boolean enable) {
		if (enable)
			resetLabel.setFont(resetLabel.getFont().deriveFont(Font.BOLD));
		else
			resetLabel.setFont(resetLabel.getFont().deriveFont(Font.PLAIN));

		resetIndex.setEnabled(enable);
		resetHpx.setEnabled(enable);
	}

}
