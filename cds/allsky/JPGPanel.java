package cds.allsky;

import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import cds.tools.Util;

public class JPGPanel extends JPanel implements ActionListener {

	private static final String OK = "Build JPGs";
	static final protected int ASINH  = 0;
	static final protected int LOG    = 1;
	static final protected int SQRT   = 2;
	static final protected int LINEAR = 3;
	static final protected int SQR    = 4;
	static final protected int MULTFCT= 5;
	static final protected String TRANSFERTFCT[] = { "Asinh", "Log","Sqrt","Linear","Pow2"," -- " };

	private String CUT = "Display pixel contrast";
	private String CUT_MAX = "Max";
	private String CUT_ALADIN = "Reset cut";
	private String CUT_MIN = "Min";
	private JTextField tCutMin = new JTextField(10);
	private JTextField tCutMax = new JTextField(10);
	ButtonGroup transfertCBG;    // Pour indiquer la fonction de transfert
	JRadioButton[] transfertCB;  // Pour indiquer la fonction de transfert
	
	JButton ok = new JButton(OK);
	JProgressBar progressJpg = new JProgressBar(0,100);
	
	final private String txt = "<html>Generated Allsky <b>Fits</b> files can be displayed by Aladin, <br>" +
			"only throught local acces.<br>" +
			"Thanks to this panel, you could choose the way your Fits files will be written in <b>JPG</b> <br>" +
			"in order to allow a distant acces for you allsky map.<br></html>";
	// private JCheckBox check_keep = new JCheckBox(CHECKKEEP, true);
		// private JCheckBox check_reso = new JCheckBox(SAMERESO, true);
	//	private JCheckBox check_cut_auto = new JCheckBox(CUT_ALADIN, true);
		// private JCheckBox check_cut_only = new JCheckBox(CUT_ONLY, false);
	
		private JButton bCutAuto = new JButton(CUT_ALADIN);
		double[] cut = new double[4];
		private final AllskyPanel allsky;

	public JPGPanel(final AllskyPanel parent) {
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
//		c.fill = GridBagConstraints.HORIZONTAL;
		label = new JLabel(txt);
		c.gridheight = 5;
		c.insets.bottom=30;
		add(label,c);
		c.insets.bottom=0;
		c.gridy++;c.gridy++;c.gridy++;c.gridy++;c.gridy++;
		c.gridheight = 1;
		// Zone pour le cut de l'histogramme
		c.gridx = 0;
		c.gridy++;
//		c.fill = GridBagConstraints.HORIZONTAL;
//		add(new JSeparator(JSeparator.HORIZONTAL), c);
//		c.fill = GridBagConstraints.NONE;
//		c.gridy++;
		label = new JLabel(CUT);
		label.setFont(label.getFont().deriveFont(Font.BOLD));
		add(label, c);

		c.gridx = 0;
		c.gridy++;
		JPanel minmax = new JPanel(new FlowLayout());
		label = new JLabel(CUT_MIN);
		minmax.add(label);
		minmax.add(tCutMin);
		label = new JLabel(CUT_MAX);
		minmax.add(label);
		minmax.add(tCutMax);
		bCutAuto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				parent.initCut();
			}
		});
		minmax.add(bCutAuto);
		
		add(minmax, c);
		
		// Les fonctions de transfert
		c.gridwidth = 1;

		c.gridx=1;
		c.gridy++;
		transfertCBG = new ButtonGroup();
		transfertCB = new JRadioButton[TRANSFERTFCT.length];
		for( int i=0; i<TRANSFERTFCT.length; i++ ) {
			transfertCB[i] = new JRadioButton(TRANSFERTFCT[i],true);
			noBold(transfertCB[i]);
//			transfertCB[i].addActionListener(this);
			transfertCBG.add(transfertCB[i]);
			if( i!=TRANSFERTFCT.length-1 ) add(transfertCB[i],c);
			c.gridx++;
		}
		transfertCBG.setSelected(transfertCB[LINEAR].getModel(), true);

		c.gridx=0;
		c.gridy++;
		c.insets.top=30;
		ok.addActionListener(this);
		c.insets.top=1;
		add(ok, c);
		
		// barre de progression
		c.gridx=0;
		c.gridy++;
		progressJpg.setStringPainted(true);
		add(progressJpg,c);
	}
	
	public void clearForms() {
		tCutMin.setText("");
		tCutMax.setText("");
		cut = new double[4];
		progressJpg.setValue(0);
	}

	public void setCut(double[] cut) {
		// affiche les valeurs réelles avec bscale et bzero
		double[] bb = allsky.getBScaleBZero();
		tCutMin.setText(Util.myRound(cut[0]*bb[0]+bb[1]));
		tCutMax.setText(Util.myRound(cut[1]*bb[0]+bb[1]));
		this.cut = cut;
	}
	
	public double[] getCut() {
		String s = tCutMin.getText();
		// convertit pour garder les valeurs codées sans bscale et bzero
		double[] bb = allsky.getBScaleBZero();
		try {
			cut[0] = (Double.parseDouble(s)-bb[1])/bb[0];
			s = tCutMax.getText();
			cut[1] = (Double.parseDouble(s)-bb[1])/bb[0];
		} catch (NumberFormatException e) {
			cut[0] = 0; cut[1] = 0;
		}
		return cut;
	}
	
	public boolean isLog() {
		return transfertCB[LOG].isSelected();
	}
	public boolean isASinH() {
		return transfertCB[ASINH].isSelected();
	}
	public boolean isPow() {
		return transfertCB[SQR].isSelected();
	}
	public boolean isSqrt() {
		return transfertCB[SQRT].isSelected();
	}
	
	public int getFct() {
		int n;
		for( n=0; n<transfertCB.length && !transfertCB[n].isSelected(); n++ ) ;
		return n;
	}

	protected JComponent noBold(JComponent c) {
		c.setFont(c.getFont().deriveFont(Font.PLAIN));
		return c;
	}

	public void setProgress(int value) {
		progressJpg.setValue(value);
	}
		
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == OK) {
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			JPGBuild jpgThread = new JPGBuild(getCut(), allsky.getOutputPath());
			jpgThread.start();
			(new ThreadProgressBar(jpgThread)).start();
		}
		setCursor(null);
	}
	
	class ThreadProgressBar implements Runnable {
		Object thread;
		public ThreadProgressBar(Object source) {
			thread = source;
		}
				
		public synchronized void start(){
			// lance en arrière plan le travail
			(new Thread(this)).start();
		}
		public void run() {
			int value = 0;
			while(thread != null && value < 99) {
				value = (int)((JPGBuild)thread).getProgress();
				setProgress(value);
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
				}
			}
			setProgress(100);
		}
	}
}
