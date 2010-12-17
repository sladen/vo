// Copyright 2010 - UDS/CNRS
// The Aladin program is distributed under the terms
// of the GNU General protected License version 3.
//
//This file is part of Aladin.
//
//    Aladin is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General protected License as published by
//    the Free Software Foundation, version 3 of the License.
//
//    Aladin is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
//    GNU General protected License for more details.
//
//    The GNU General protected License is available in COPYING file
//    along with Aladin.
//

package cds.allsky;

import static cds.allsky.AllskyConst.INDEX;
import static cds.allsky.AllskyConst.TESS;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import cds.aladin.Aladin;
import cds.aladin.Chaine;
import cds.fits.Fits;
import cds.tools.pixtools.Util;

public class AllskyPanel extends JPanel implements ActionListener {
	private AllskyStepsPanel pSteps = null;
	
	private String s_DESC, s_BUILD, s_DISP, s_PUBLISH, s_RGB, s_CLEAN, s_ERR, s_ERRFITS;
	private String tipDesc, tipBuild, tipDisplay, tipPublish, tipClean, tipRGB;
	BuildPanel pBuild = null;
	private PublishPanel pPublish;
	private JPGPanel pDisplay;
	private RGBPanel pRGB;
//	private CleanPanel pClean;
	
	protected Aladin aladin;

	BorderLayout bLay = new BorderLayout(20, 10);
	DescPanel pDesc;
	JPanel pBuildAll;

	// Onglet Open
	JTextField field;
	JTextArea glu;
	JPanel pView;
	private int bitpix = -1;
	private int order;
	private JTabbedPane pTab;
	
	private int lastN3 = 0;

	protected int getLastN3() {
		return lastN3;
	}

	protected void setLastN3(int lastN3) {
		this.lastN3 = lastN3;
	}

	public AllskyPanel(Aladin a) {
		super();
		aladin = a;
		createChaine(aladin.getChaine());
		createPanel();
		DBBuilder.DEBUG = (aladin.levelTrace>0)?true:false;

	}

	private void createPanel() {
		pTab = new JTabbedPane();
		pBuildAll = new JPanel(bLay);
		pSteps = new AllskyStepsPanel();
		pBuild = new BuildPanel();
		pDisplay = new JPGPanel(this);
		pPublish = new PublishPanel(aladin,this);
		pBuildAll.add(pSteps, BorderLayout.SOUTH);
		pBuildAll.add(pBuild, BorderLayout.CENTER);

		pDesc = new DescPanel(aladin.getDefaultDirectory(), this);
		pDesc.getSourceDirField().addActionListener(this);
		
		if (aladin.isProto())
			pRGB = new RGBPanel(aladin);
//		pClean = new CleanPanel(this);

		// c.anchor = GridBagConstraints.WEST;
		// c.gridwidth=1;
		// c.gridx = 2; c.gridy++;
		// pBuild.add(check_cut_only,c);

		// ----
		// ajoute l'onglet dans le panel
		pTab.addTab(s_DESC, null, pDesc, tipDesc);
		pTab.addTab(s_BUILD, null, pBuildAll, tipBuild);
		pTab.addTab(s_DISP, null, pDisplay, tipDisplay);
		pTab.addTab(s_PUBLISH, null, pPublish, tipPublish);
		// ajoute le 2e onglet dans le panel
		// JPanel pRGB = new JPanel(new GridBagLayout());
		// p.addTab("Color Composition", pRGB);
		// ajoute le 3e onglet dans le panel
		// pView = new AllskyGluPanel(new GridBagLayout(), dir_A.getText());
		// p.addTab("Open", pView);
		
		if (pRGB!=null) {
			pTab.addTab(s_RGB, null, pRGB, tipRGB);
			pTab.addChangeListener(new ChangeListener() {
				
				public void stateChanged(ChangeEvent e) {
					if (pTab.getSelectedComponent() == pRGB)
						pRGB.init();
				}
			});		
		}
//		pTab.addTab(s_CLEAN, null, pClean, tipClean);

		add(pTab, BorderLayout.CENTER);
		
	}
	
	private void createChaine(Chaine chaine) {
		s_RGB = chaine.getString("MRGB");
		s_CLEAN = chaine.getString("MCLEAN");
		s_DESC = chaine.getString("MDESC");
		s_BUILD = chaine.getString("MBUILD");
		s_DISP = chaine.getString("MDISPLAY");
		s_PUBLISH = chaine.getString("MPUBLISH");
		s_ERRFITS = chaine.getString("ERRFITS");
		s_ERR = chaine.getString("ERROR");
		tipBuild = chaine.getString("MTIPBUILD");
		tipDisplay = chaine.getString("MTIPDISPLAY");
		tipPublish = chaine.getString("MTIPPUBLISH");
		tipRGB = chaine.getString("MTIPRGB");
		tipClean = chaine.getString("MTIPCLEAN");
		
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == pDesc.getSourceDirField()) {
			init();
		}
	}

	/**
	 * Cherche un fichier fits dans l'arborescence et itialise les variables
	 * bitpix et le cut avec Cherche aussi le meilleur nside pour la résolution
	 * du fichier trouvé
	 * 
	 * @param text
	 */
	public void init() {
		String text = pDesc.getInputPath().trim();
		if (text != null && !text.equals("")) {
			try {
				// lit un fichier FITS dans le réperoire sélectionné
				Fits file = Fits.getFits(text);
				if (file == null || file.getCalib() == null) {
					JOptionPane.showMessageDialog(this, s_ERRFITS
						+ text, s_ERR, JOptionPane.ERROR_MESSAGE);
					return;
				}
				// récupère le bitpix
				bitpix = file.bitpix;
				pBuild.setOriginalBitpix(bitpix);
				// récupère le bscale/bzero
				pBuild.setBScaleBZero(file.bscale, file.bzero);
				// récupère le blank
				pBuild.setBlank(file.blank);
				// récupère le min max pour le cut
				initCut();
				// calcule le meilleur nside
				long nside = healpix.core.HealpixIndex.calculateNSide(
						file.getCalib().GetResol()[0] * 3600.);
				order = (int) Util.order((int)nside);
				setSelectedOrder(order - HpxBuilder.ORDER);
				
			} catch (Exception e1) {
//				e1.printStackTrace();
			}
		}
//		pPublish.init();   PF juin 2010
	}

	protected void initCut() {
		Fits file = Fits.getFits(getInputPath());
		double[] cut = ThreadAutoCut.run(file);
		pDisplay.setCut(cut);
		if (pBuild.getBitpix() != getOriginalBitpix())
			convertCut(pBuild.getBitpix());
	}
	private int setSelectedOrder(int val) {
		return pBuild.setSelectedOrder(val);
	}

	protected double[] getCut() {
		return pDisplay.getCut();
	}
	
	protected void convertCut(int bitpix) {
		double[] cut = pDisplay.getCut();
		double [] oldminmax = new double[] {cut[2],cut[3]};
		cut[0] = Fits.toBitpixRange(cut[0], bitpix, oldminmax);
		cut[1] = Fits.toBitpixRange(cut[1], bitpix, oldminmax);
		pDisplay.setCut(cut);
	}
	
	protected int getMethod() {
		return pDisplay.getFct();
	}

	protected double[] getBScaleBZero() {
		return new double[]{pBuild.getBscale(), pBuild.getBzero()};
	}
	/**
	 * 
	 * @return order choisi ou -1 s'il doit etre calculé
	 */
	protected int getOrder() {
		return pBuild.getOrder();
	}

	protected int getBitpix() {
		return pBuild.getBitpix();
	}
	
	protected double getBlank() {
		return pBuild.getBlank();
	}

	protected String getInputPath() {
		return pDesc.getInputPath();
	}

	public String getOutputPath() {
		return pDesc.getOutputPath();
	}


	protected void setProgress(int mode, int value) {
		switch (mode) {
		case INDEX:
			pSteps.setProgressIndex(value);
			break;
		case TESS:
			pSteps.setProgressTess(value);
			break;
//		case JPG:
//			pSteps.setProgressJpg(value);
//			break;
		}
	}

	protected void enable(boolean selected, int mode) {
		pSteps.select(selected, mode);
	}

/*
	public void showClean() {
		pClean.selectAll();
		pTab.setSelectedComponent(pClean);
	}
	public void showClean(boolean main) {
		if (main)
			pClean.selectMain();
		pTab.setSelectedComponent(pClean);
	}
	*/
	public void showPublish() {
		pTab.setSelectedComponent(pPublish);
	}

	public void showDisplay() {
		pTab.setSelectedComponent(pDisplay);
	}
	public void showBuild() {
		pTab.setSelectedComponent(pBuildAll);
	}

	public void resetProgress() {
		enable(true, INDEX);
		enable(true, TESS);
		setProgress(INDEX, 0);
		setProgress(TESS, 0);
	}

	public String getLabel() {
		return pDesc.getLabel();
	}

	protected String getAuthor() {
		return pDesc.getAuthor();
	}

	protected String getCopyright() {
		return pDesc.getCopyright();
	}

	protected void newAllskyDir() {
		pPublish.newAllskyDir(AllskyConst.SURVEY);
//		pClean.newAllskyDir(pDesc.getOutputPath());
		// si un repertoire de sortie ALLSKY existe déjà, on change le nom du bouton START
		aladin.frameAllsky.initStart();
		pDesc.setResetEnable(false);
		if ((new File(pDesc.getOutputPath())).exists()) {
			 // met le bouton Reset utilisable, mais pas selectionné
			aladin.frameAllsky.setResume();
			pDesc.setResetEnable(true);
		}
	}

	public void setRestart() {
		pDesc.setResetEnable(true);
		pDesc.setResetSelected(true);
	}
	public void setResume() {
		pDesc.setResetEnable(true);
		pDesc.setResetSelected(false);
	}
	public void setDone() {
		pDesc.setResetEnable(false);
		pDesc.setResetSelected(false);
	}
	public void setStart() {
		pDesc.setResetEnable(false);
		pDesc.setResetSelected(false);
	}

	public void toReset() {
		 if (pDesc.toResetIndex())
			 resetIndex();
		 if (pDesc.toResetHpx())
			 resetHpx();
//		 if (pDesc.toResetJpg())
//			 resetJpg();
	}
	public void resetIndex() {
		cds.tools.Util.deleteDir(new File(getOutputPath()+AllskyConst.HPX_FINDER));
	}
	public void resetHpx() {
		File dir = new File(getOutputPath());
		File[] children = dir.listFiles();
		// pour tous répertoires Norder du répertoire principal
		for (int i=0; i<children.length; i++) {
			if( children[i].getName().startsWith("Norder") && children[i].isDirectory()) {
				cds.tools.Util.deleteDir(children[i]);
			}
		}
	}
//	public void resetJpg() {
//		cds.tools.Util.deleteDir(new File(getOutputPath()),".*\\.jpg$");
//	}
    protected boolean toFast() {
       return pBuild.toFast();
   }

	protected void setInitDir(String txt) {
		pSteps.setProgressIndexTxt(txt);
	}

	/**
	 * @return the keepBB
	 */
	protected boolean isKeepBB() {
		return pBuild.isKeepBB();
	}

	protected int getOriginalBitpix() {
		return pBuild.getOriginalBitpix();
	}

	protected void displayStart() {
		aladin.frameAllsky.displayStart();
		pDesc.setResetEnable(false);
		pDesc.setResetSelected(false);
	}
	protected void displayReStart() {
		aladin.frameAllsky.displayReStart();
	}

	protected void clearForms() {
		AllskyConst.SURVEY = AllskyConst.ALLSKY;
		pDesc.clearForms();
		pBuild.clearForms();
		pSteps.clearForms();
		pDisplay.clearForms();
		pPublish.clearForms();
//		pClean.clearForms();
		aladin.frameAllsky.initStart();
//		pRGB.clearForms();
	}

	protected void export() {
		aladin.frameAllsky.export();
	}
}


class ThreadAutoCut extends Thread {
	static Fits file = null;
	static double[] cut = null;

	protected static double[] run(Fits file) {
		ThreadAutoCut.file = file;
		(new ThreadAutoCut()).run();
		return cut;
	}


	public void run() {
		try {
			cut = file.findAutocutRange();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}