package cds.allsky;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import cds.tools.Util;

public class CleanPanel extends JPanel implements ActionListener {
	private static final String s_OK = "Clean now";
//	private static final String s_all = "Select all";
	private static final String s_main = "Dir ";
	private static final String s_index = "Indexation ("+AllskyConst.HPX_FINDER+" subdir)";
	private static final String s_fits = "Generated FITS files";
	private static final String s_jpg = "Generated JPG files";
	private static final String s_forms = "Forms fields";

	JButton ok = new JButton(s_OK);
//	JButton all = new JButton(s_all);
	JCheckBox main = new JCheckBox(s_main);
	JCheckBox index = new JCheckBox(s_index);
	JCheckBox fits = new JCheckBox(s_fits);
	JCheckBox jpg = new JCheckBox(s_jpg);
	JCheckBox forms = new JCheckBox(s_forms);

	private AllskyPanel allsky;
	String txt = "<html>Generated Allsky files consume heavy disk space. <br>" +
			"If the result for these parameters is ok for you, you can now remove <br>" +
			"temporary/unused files (used for Aladin to run quicker during the (re)building).<br>" +
			"Please choose FITS/JPG files to delete depending on the way you want to publish it." +
			"</html>";
	private String allskyDir;

	public CleanPanel(final AllskyPanel parent) {
		super(new GridBagLayout());
		allsky = parent;
		JLabel label;
		GridBagConstraints c = new GridBagConstraints();
		c.gridx = 0;
		c.gridy = 0;
		c.anchor = GridBagConstraints.WEST;
		c.fill = GridBagConstraints.NONE;
		c.gridwidth = GridBagConstraints.REMAINDER;
		// Texte d'intro
		label = new JLabel(txt);
		c.gridheight = 5;
		c.insets.bottom=30;
		add(label,c);
		c.insets.bottom=0;
		c.gridy++;c.gridy++;c.gridy++;c.gridy++;c.gridy++;
		c.gridheight = 1;
		c.gridwidth = GridBagConstraints.RELATIVE;
		
		// définit la largeur de la 1ere col avec des espaces
		c.gridx = 0;
		c.gridy++;
		c.gridwidth = 1;
		add(new JLabel("       "),c);
		
		// Cases à cocher		
//		all.addActionListener(this);
		main.addActionListener(this);
		index.addActionListener(this);
		fits.addActionListener(this);
		jpg.addActionListener(this);
		forms.addActionListener(this);
		ok.addActionListener(this);
		
//		c.gridy++;
//		c.gridwidth=4;
//		add(all,c);
		c.gridwidth = 2;
		c.gridy++;
		add(main,c);
		c.gridx++;
		c.gridy++;
		add(index,c);
		c.gridy++;
		add(fits,c);
		c.gridy++;
		add(jpg,c);
		c.gridx=0;
		c.gridy++;
		add(forms,c);
		
		// bouton ok
		c.gridx=0;
		c.gridy++;
		add(ok,c);
		
	}
	
	public void clearForms() {
		unselectMain();
		forms.setSelected(false);
	}
	
	public void actionPerformed(ActionEvent e) {
		String actionCommand = e.getActionCommand();
		if (actionCommand == s_OK) {
			clean();
		}
//		else if (e.getActionCommand() == s_all) {
//			selectAll();
//		}
		else if (actionCommand == s_index) {
			if (!index.isSelected())
				main.setSelected(false);
		}
		else if (actionCommand == s_fits) {
			if (!fits.isSelected())
				main.setSelected(false);
		}
		else if (actionCommand == s_jpg) {
			if (!jpg.isSelected())
				main.setSelected(false);
		}
		else if (e.getSource() == main) {
			if (main.isSelected())
				selectMain();
			else
				unselectMain();
		}
	}
	private void clean() {
		boolean dir = jpg.isSelected() && fits.isSelected() && index.isSelected();
		if (allskyDir == null)
			return;
		if (dir)
			Util.deleteDir(new File(allskyDir));
		else {
			if (jpg.isSelected())
				Util.deleteDir(new File(allskyDir), ".*\\.jpg$");
			if (fits.isSelected())
				Util.deleteDir(new File(allskyDir), ".*\\.fits$");
			if (index.isSelected())
				Util.deleteDir(new File(allskyDir+AllskyConst.HPX_FINDER));
		}
		if (forms.isSelected()) {
			allsky.clearForms();
		}
	}
	public void selectMain() {
		index.setSelected(true);
		fits.setSelected(true);
		jpg.setSelected(true);
	}
	private void unselectMain() {
		main.setSelected(false);
		index.setSelected(false);
		fits.setSelected(false);
		jpg.setSelected(false);
	}
	public void selectAll() {
		main.doClick();
		forms.setSelected(true);
	}

	public void newAllskyDir(String dir) {
		allskyDir = dir;
	    main.setText(s_main+dir);
	    repaint();
	}
}
