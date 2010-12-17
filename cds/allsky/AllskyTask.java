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

import static cds.allsky.AllskyConst.INDEX;
import static cds.allsky.AllskyConst.JPG;
import static cds.allsky.AllskyConst.TESS;
import cds.aladin.Aladin;
import cds.aladin.Calib;
import cds.aladin.Coord;
import cds.aladin.FrameAllskyTool;
import cds.aladin.Plan;
import cds.aladin.PlanBG;
import cds.tools.pixtools.CDSHealpix;

public class AllskyTask implements Runnable{

	
	private static int NMAX = 0;
	
	FrameAllskyTool frame;
	AllskyPanel allsky;

	InitLocalAccess initializer = new InitLocalAccess();
	SkyGenerator sg = new SkyGenerator();
	DBBuilder builder = new DBBuilder();
    int mode = -1;
    boolean allskyOk;  // true si le allsky généré l'a bien été à la fin du processus de construction et pas en cours de ce processus
	
	public AllskyTask(FrameAllskyTool frame) {
		this.frame = frame;
		allsky = frame.allskyPanel;
		allskyOk=false;
	}
	
	private volatile Thread runner = null;

	private int order;

	private String input;

	private String output;

	private int bitpix;
	private boolean keepBB = false;
	private double[] cut;
	private int fct; // fonction de transfert
	private ThreadProgressBar progressBar;

	private double blank;
	private double bzero;
	private double bscale;

//	private Plan planPreview;
	
    public Plan getPlanPreview() {
        return allsky.aladin.calque.getPlan("MySky");
//		return planPreview;
	}

	public synchronized void startThread(){
		if(runner == null){
			runner = new Thread(this);
			try {
				runner.start(); // --> appelle run()
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public synchronized void stopThread(){
		
		if(runner != null){
			runner = null;
			Aladin.trace(2,"STOP ON "+mode);
			switch (mode) {
			case INDEX : initializer.stop();
			case TESS : builder.stop();
			}
			progressBar.stop();
		}
	}
	
	public void run() {
		try {
			mode = INDEX;
//			if (allsky.toReset()) builder.reset(output);
			boolean fast = allsky.toFast();
				
			// Créée un répertoire HpxFinder avec l'indexation des fichiers source pour l'ordre demandé
			// (garde l'ancien s'il existe déjà)
			if (mode<=INDEX) {
				Aladin.trace(2,"Launch Index");
				followProgress(mode,initializer);
//				initializer.setThread(runner);
				boolean init = initializer.build(input,output,order);
				// si le thread a été interrompu, on sort direct
				if (runner != Thread.currentThread()) {
					return;
				}
				if (init) {
					Aladin.trace(2,"Allsky... => Index built");
				}
				else 
					Aladin.trace(2,"Allsky... => Use previous Index");
				setProgress(mode,100);
				allsky.enable(false,INDEX);
			}
			// Création des fichiers healpix fits et jpg
			if (mode <= TESS) {
				mode = TESS;
				Aladin.trace(2,"Launch Tess ("+(fast?"fast":"best")+" method)");
//				builder.setThread(runner);
				followProgress(mode, builder);
				try {
					if (cut == null)
						cut = new double[] {0,0,0,0};
					builder.setAutoCut(cut, fct);
					builder.setBlank(blank);
					builder.setBScaleBZero(bscale,bzero);
					builder.build(order, output, bitpix,false, fast, keepBB);
					// si le thread a été interrompu, on sort direct
					if (runner != Thread.currentThread()) {
						return;
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Aladin.trace(2,"Allsky... => Hpx files built");
				setProgress(mode,100);
				allsky.enable(false,TESS);
			}
			// création du fichier allsky
			if (mode <= JPG) {
				mode = JPG;
				createAllSky();
			}
			allskyOk=true;

			frame.done();
			runner = null;
			mode = -1;
			Aladin.trace(2,"DONE");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// création des fichiers allsky
	public void createAllSky() {
	   if( allskyOk ) return;      // déjà fait

	   //          followProgress(mode,sg);
	   //          String path = Util.concatDir(output,AllskyConst.SURVEY);
	   try {
		   if( bitpix==0 ) sg.createAllSkyJpgColor(output,3,64);
		   else {
			   if( cut==null ) sg.createAllSky(output,3,64,0,0, keepBB);
			   else sg.createAllSky(output,3,64,cut[0],cut[1],keepBB);
		   }

	   } catch (Exception e) {
		   // TODO Auto-generated catch block
		   e.printStackTrace();
	   }
	   Aladin.trace(2,"... => Allsky done");
	   //          setProgress(mode,100);
	   //          allsky.enableMode(false,JPG);
	}

	void setInitDir(String txt) {
		allsky.setInitDir(txt);		
	}

	void setProgress(int stepMode, int i) {
		allsky.setProgress(stepMode, i);
	}

	private void followProgress(int stepMode, Object o) {
		progressBar = new ThreadProgressBar(stepMode,o, this);
		progressBar.start();
	}

	public void doInBackground() throws Exception {
		order = allsky.getOrder();
		if (order==-1)
			order = 3;
		output = allsky.getOutputPath();
		input = allsky.getInputPath();

		if (input == null || input.equals("")) {
			System.err.println("No input directory given");
			frame.stop();
			return ;
		}
		// récupère le bitpix dans le formulaire
		bitpix = allsky.getBitpix();
		keepBB = allsky.isKeepBB();
		double bb[] = allsky.getBScaleBZero();
		bscale = bb[0];
		bzero = bb[1];
		blank = allsky.getBlank();
		// si le bitpix change
		if (allsky.getOriginalBitpix() != bitpix)
			// on change aussi les bornes
			allsky.convertCut(bitpix);
		cut = allsky.getCut();
		fct = allsky.getMethod();
		
		if (mode == -1)
			mode = INDEX;
		
		startThread();
	}


    /**
     * Invoked when task's progress property changes.
     */
//    public void propertyChange(PropertyChangeEvent evt) {
//        if ("progress" == evt.getPropertyName()) {
//            int progress = (Integer) evt.getNewValue();
//            //allskyPanel.setProgress(mode,progress);
//        } 
//    }

	/**
	 * Teste s'il n'y a plus de tache en cours (arret normal ou interrompu)
	 */
	public boolean isDone() {
		return runner == null;
//		return runner != Thread.currentThread();
	}

	public void setLastN3(int lastN3) {
		allsky.setLastN3(lastN3);
	}

	public int getLastN3() {
		return allsky.getLastN3();
	}

	/** Création/rafraichissemnt d'un allsky (en l'état) et affichage */
	void preview(int last) {
	   try {
          createAllSky();
          
          Plan planPreview = allsky.aladin.calque.getPlan("MySky");
          if( planPreview==null || planPreview.isFree() ) {
             double[] res = CDSHealpix.pix2ang_nest(cds.tools.pixtools.Util.nside(3), last);
             double[] radec = CDSHealpix.polarToRadec(new double[] {res[0],res[1]});
             radec = Calib.GalacticToRaDec(radec[0],radec[1]);
             int n = allsky.aladin.calque.newPlanBG(allsky.getOutputPath(), "=MySky",Coord.getSexa(radec[0],radec[1]), "30" );
             allsky.aladin.trace(4,"AllskyTask.preview: Create MySky");
             planPreview = allsky.aladin.calque.getPlan(n);
          } else {
             ((PlanBG)planPreview).forceReload();
             allsky.aladin.calque.repaintAll();
             allsky.aladin.trace(4,"AllskyTask.preview: Create MySky");
             
          }
      } catch( Exception e ) {e.printStackTrace(); }
	}
	
	
}

class ThreadProgressBar implements Runnable {

	public static final int INDEX = AllskyConst.INDEX;
	public static final int TESS = AllskyConst.TESS;
	//	public static final int JPG = AllskyConst.JPG;

	//	private volatile Thread th_progress = null;
	private boolean stopped = false;
	int last = -1;
	Object thread;
	AllskyTask tasks;

	int mode ;
	public ThreadProgressBar(int stepMode, Object source, AllskyTask allskyTask) {
		mode=stepMode;
		thread = source;
		tasks = allskyTask;
	}
	public synchronized void start(){
		stopped=false;
		// lance en arrière plan le travail
		(new Thread(this)).start();
	}
	public synchronized void stop() {
		stopped=true;
		//		th_progress = null;
	}

	/**
	 * Va chercher la dernière valeur de progression
	 */
	public void run() {
		int value = 0;
		String txt = "";
		while(thread != null && !stopped && value < 99) {
			switch (mode) {
			case INDEX :
				value = (int)((InitLocalAccess)thread).getProgress();
				txt = ((InitLocalAccess)thread).getCurrentpath();
				tasks.setInitDir(txt);
				break;
			case TESS : 
				value = (int)((DBBuilder)thread).getProgress();
				int n3 = ((DBBuilder)thread).getLastN3();
				if (n3!=-1 && last!=n3) {
					tasks.preview(n3);
					tasks.setLastN3(n3);
					last = n3;
				}
				break;
				//			case JPG : 
				//				value = (int)((SkyGenerator)thread).getProgress();
				//				break;
			}
			tasks.setProgress(mode,value);
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
			}
		}
		stopped = true;
		tasks.setProgress(mode,value);
	}

}
