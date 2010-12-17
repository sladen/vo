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

package cds.aladin;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import cds.allsky.AllskyPanel;
import cds.allsky.AllskyTask;
import cds.tools.Util;

public class FrameAllskyTool extends JFrame implements ActionListener{


	public Aladin aladin;
    public AllskyPanel allskyPanel;
    protected JButton b_cancel;
    protected JButton b_close;
    protected JButton b_pause;
    protected JButton b_ok;
    protected JButton b_help;
	public AllskyTask task;
    
    private String OK,PAUSE,RESTART,RESUME,CLOSE,ABORT,DONE;
    private String title, titlehelp, canceltip, hidetip;
    
    String help;
    private int bitpix;
    
    
	private FrameAllskyTool(Aladin aladin) {
		super();
        Aladin.setIcon(this);
        this.aladin = aladin;
        createChaine(aladin.getChaine());
	    setTitle(title);

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);
        Util.setCloseShortcut(this, false, aladin);

        addWindowListener(new WindowAdapter() {
           public void windowClosing(WindowEvent e) { close(); }
        });

	    getContentPane().setLayout(new BorderLayout(1,1));
        getContentPane().add(createPanel(), "Center");

        setLocation(500,100);
        pack();
	}

	private void createChaine(Chaine chaine) {
		ABORT = chaine.getString("ABORT");
		PAUSE = chaine.getString("PAUSE");
		OK = chaine.getString("START");
		RESUME = chaine.getString("RESUME");
		DONE = chaine.getString("DONE");
		CLOSE = chaine.getString("HIDE1");
		RESTART = chaine.getString("RE_START");
		title = chaine.getString("TITLEALLSKY");
		help = chaine.getString("HELPALLSKY");
		titlehelp = chaine.getString("HHELP");
		canceltip = chaine.getString("TIPCANCALLSKY");
		hidetip = chaine.getString("TIPHIDEALLSKY");
	}

	private JPanel createPanel() {
		JPanel p = new JPanel(new BorderLayout(1,1));
		allskyPanel=new AllskyPanel(aladin);
		p.add(allskyPanel, BorderLayout.CENTER);
		b_ok = new JButton(OK);
		b_ok.addActionListener(this);
		b_ok.setEnabled(false);
		b_cancel = new JButton(ABORT);
		b_cancel.addActionListener(this);
		b_cancel.setToolTipText(canceltip);
		b_cancel.setEnabled(false);
		b_close = new JButton(CLOSE);
		b_close.setToolTipText(hidetip);
		b_close.addActionListener(this);
		b_help = Util.getHelpButton(this,help);
		JPanel fin = new JPanel();
		fin.add(b_ok);
        fin.add(b_cancel);
        fin.add(b_close);
        fin.add(b_help);
		p.add(fin, BorderLayout.SOUTH);
		return p;
	}

	public static void display(Aladin aladin) {
		if( aladin.frameAllsky==null ) aladin.frameAllsky = new FrameAllskyTool(aladin);
	       aladin.frameAllsky.setVisible(true);
	}
	
	/** retourne la tache courante si elle existe, sinon null (PF - juillet 2010) */
	public AllskyTask getTask() { return task; }

    /** Fermeture de la fenêtre */
    private void close() {
       setVisible(false);
    }
    
    public void export() {
    	String path = allskyPanel.getOutputPath();
    	Plan plan;
    	if (task != null)
    		plan = task.getPlanPreview();
    	else {
    		int n = aladin.calque.newPlanBG(path, allskyPanel.getLabel(), null, null );
            plan = aladin.calque.getPlan(n);
            while (!plan.isSync()) {
            	try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
            }
            try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
            
            	
    	}
    	aladin.save.saveImage(path+"Allsky.hpx",plan,1);
    	
    }

	public void actionPerformed(ActionEvent e) {

		// START / RESTART / RESUME / PAUSE 
		if (e.getSource() == b_ok) {
			// PAUSE
			if (e.getActionCommand() == PAUSE) {
				if (task != null)
					task.stopThread();
				setResume();
				stop();
				return;
			}

			// initialisation correcte des barres de progression et boutons
	        allskyPanel.resetProgress();
//	        allskyPanel.enable(true,AllskyPanel.JPG);

//			b_ok.setEnabled(false);
//			b_pause.setEnabled(true);
	        b_ok.setText(PAUSE);
			
			b_cancel.setEnabled(true);
		    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

		    if (bitpix == -1) {
		    	allskyPanel.init();
		    }
		    
		    // effectue le nettoyage selon les "reset" cochés
		    allskyPanel.toReset();
		    
		    //lance les taches en arrière plan
//		    if (task == null || e.getActionCommand() == RESTART || e.getActionCommand() == RESUME)
		    	task = new AllskyTask(this);
		    if (task.isDone()) {
		    	try {
		    		task.doInBackground();
		    	} catch (Exception e1) {
		    		// TODO Auto-generated catch block
		    		e1.printStackTrace();
		    		return;
		    	}

				// Thread qui attend de savoir que les calculs sont terminés
				new Thread(new Runnable() {
					public void run() {
						boolean done = false;
						while(!done) {
							try {
								Thread.sleep(1000);
							} catch (InterruptedException e) {
							}
							if (task == null || task.isDone()) {
								Aladin.trace(3,"Task END");
								done=true;
								if (task == null) stop();
							}
//							else System.out.println("Task continue");
						}
					}
				}).start();
		    }
		    // else rien, il faut attendre que la tache soit terminee ou interrompue
		    showBuild();
		} else if (e.getSource() == b_cancel) {			
			if (task != null)
				task.stopThread();
			stop();
			allskyPanel.resetHpx();
			allskyPanel.resetIndex();
		} else if (e.getSource() == b_close) {			
			close();
		} else if (e.getSource() == b_help) {
			help();
		}

	}
	

	public void stop() {
//        b_ok.setEnabled(true);
//        b_pause.setEnabled(false);
		setResume();
        b_cancel.setEnabled(false);
        setCursor(null);
//        allskyPanel.enable(true,allskyPanel.INDEX);
//        allskyPanel.enable(true,allskyPanel.TESS);
//        allskyPanel.enable(true,allskyPanel.JPG);
	}


	private void showPublish() {
		allskyPanel.showPublish();
	}
	public void showDisplay() {
		allskyPanel.showDisplay();
	}
	private void showBuild() {
		allskyPanel.showBuild();
	}

	public void done() {
		showPublish();
		setDone();
		setCursor(null);
	}

	public void setRestart() {
		displayReStart();
		allskyPanel.setRestart();
	}

	public void setResume() {
		displayResume();
		allskyPanel.setResume();
	}

	public void setDone() {
		displayDone();
		allskyPanel.setDone();
	}
	
	public void initStart() {
		displayStart();
		allskyPanel.setStart();
	}


	public void displayStart() {
		b_ok.setText(OK);
		b_ok.setEnabled(true);
		b_cancel.setEnabled(true);
	}
	public void displayReStart() {
		b_ok.setText(RESTART);
		b_ok.setEnabled(true);
		b_cancel.setEnabled(true);
	}
	public void displayResume() {
		b_ok.setText(RESUME);
		b_ok.setEnabled(true);
		b_cancel.setEnabled(true);
	}
	public void displayDone() {
		b_ok.setText(DONE);
		b_ok.setEnabled(false);
		b_cancel.setEnabled(false);
	}
	public void help() {
		JOptionPane.showMessageDialog(this, help, titlehelp, JOptionPane.INFORMATION_MESSAGE);
	}
}
