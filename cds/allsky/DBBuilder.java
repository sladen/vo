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

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Iterator;

import cds.aladin.Aladin;
import cds.aladin.MyInputStream;
import cds.fits.Fits;
import cds.tools.pixtools.Util;


public class DBBuilder  {
   
    static public boolean DSS = false;

	protected int ordermin = 3;
	protected long nummin = 0;
	protected long nummax = 0;
	ArrayList<Long> npix_list;
	final static int ORDER = 9; // 2^9 = 512 = SIDE
	private final int SIDE = 512;
	protected double automin = 0;
	protected double automax = 0;
	private boolean cutlog = false;
	protected int bitpix = 16;
	protected String localServer = null;
	private int progress = 0;
	
	private int lastN3 = -1;
	public static boolean DEBUG = true;

	public static String FS;
	private long NMAX = 0;
	private int NCURRENT = 0;
	private int ordermax;
	private boolean stopped = false;

	static { FS = System.getProperty("file.separator"); }

	
	public void build(int ordermax, String outpath, int mybitpix, boolean overwrite, boolean fast, boolean keepBB) throws Exception {
		localServer = outpath + AllskyConst.HPX_FINDER;
		build(ordermax, outpath, mybitpix, overwrite, fast, localServer, keepBB);
	}
//	public void build(int ordermax, String outpath, int mybitpix, boolean overwrite, boolean fast,String hpxfinder) throws Exception {
//		progress = 0;
//		this.ordermax = ordermax;
//		long t = System.currentTimeMillis();
//		localServer = hpxfinder;
//		bitpix = mybitpix; 
////		hpx = new HpxBuilder(mybitpix, localServer);
//		hpx.setBitpix(mybitpix);
//		hpx.setLocalServer(localServer);
//
//		outpath += FS; 
//		hpx.createHealpixOrder(ORDER);
////		boolean rebuild = false;
//		
//		
//		//rebuild=Boolean.parseBoolean(args[9]);
//		
//		
//		// récupère les numéros des losanges du niveau haut (ordermin)
//		searchMinMax();
////		npix_list = new ArrayList<Long>((int) (nummax-nummin));
////		for (int i = 0; i < nummax-nummin ; i++) {
////			npix_list.add(i+nummin);
////		}
//
//		// TODO cherche à définir l'ordre si ce n'est pas déjà imposé
//		if (ordermax==-1) {
//			ordermax=4;
//		}
//
//		// pour chaque losange sélectionné
//		NMAX = npix_list.size();
//
//		Thread thisThread = Thread.currentThread();
//		for (int n = 0 ; thread == thisThread && n < npix_list.size() ; n++) {
//			progress++;
//			createHpx(outpath, ordermin, ordermax, npix_list.get(n), overwrite,fast);
//		}
//		System.out.println(">> Build in "+(System.currentTimeMillis()-t)/1000+"s");
//	}
	
//	public void build(int ordermax, String outpath, int mybitpix, boolean overwrite, boolean fast,String hpxfinder) throws Exception {
//		progress = 0;
//		this.ordermax = ordermax;
//		long t = System.currentTimeMillis();
//		localServer = hpxfinder;
//		bitpix = mybitpix; 
////		hpx = new HpxBuilder(mybitpix, localServer);
//		hpx.setBitpix(mybitpix);
//		hpx.setLocalServer(localServer);
//
//		outpath += FS; 
//		hpx.createHealpixOrder(ORDER);
////		boolean rebuild = false;
//		
//		
//		//rebuild=Boolean.parseBoolean(args[9]);
//		
//		
//		// récupère les numéros des losanges du niveau haut (ordermin)
//		searchMinMax();
////		npix_list = new ArrayList<Long>((int) (nummax-nummin));
////		for (int i = 0; i < nummax-nummin ; i++) {
////			npix_list.add(i+nummin);
////		}
//
//		// TODO cherche à définir l'ordre si ce n'est pas déjà imposé
//		if (ordermax==-1) {
//			ordermax=4;
//		}
//
//		// pour chaque losange sélectionné
//		NMAX = npix_list.size();
//
//		Thread thisThread = Thread.currentThread();
//		for (int n = 0 ; thread == thisThread && n < npix_list.size() ; n++) {
//			progress++;
//			createHpx(outpath, ordermin, ordermax, npix_list.get(n), overwrite,fast);
//		}
//		System.out.println(">> Build in "+(System.currentTimeMillis()-t)/1000+"s");
//	}
	
	public void build(int ordermax, String outpath, int mybitpix, boolean overwrite, boolean fast, 
			String hpxfinder, boolean keepBB) throws Exception {
		progress = 0;
		this.ordermax = ordermax;
		long t = System.currentTimeMillis();
		localServer = hpxfinder;
		bitpix = mybitpix;
		outpath += FS; 
		
		// récupère les numéros des losanges du niveau haut (ordermin)
		if (nummin == nummax && nummin == 0)
			searchMinMax();
		else {
			npix_list = new ArrayList<Long>((int) (nummax-nummin));
			for (int i = 0; i < nummax-nummin ; i++) {
				npix_list.add(i+nummin);
			}
		}
		
		// TODO cherche à définir l'ordre si ce n'est pas déjà imposé
		if (ordermax==-1) ordermax=4;
		
		// pour chaque losange sélectionné
		NMAX = npix_list.size();

	   // Numéro courant dans la liste npix_list
	   NCURRENT = 0;

	   int nbProc = Runtime.getRuntime().availableProcessors();
	   Aladin.trace(3,"Available processors => "+nbProc);
	   long heapsize = Runtime.getRuntime().maxMemory();
	   nbProc = (int) ( (heapsize / 512000 < nbProc)?heapsize / 512000:nbProc);
	   if (nbProc==0) nbProc=1;
	   Aladin.trace(3,"Used processors => "+nbProc);

	   // Lancement des threads de calcul
	   createThread(nbProc,outpath,ordermin,ordermax,overwrite,fast, keepBB);

	   // Attente de la fin du travail
	   while( stillAlive() ) {
	      if( stopped ) { destroyThread(); return; }
	      cds.tools.Util.pause(1000);
	   }
	   //	   createPropertiesFile(outpath);
	   Aladin.trace(3,">> Build in "+cds.tools.Util.getTemps(System.currentTimeMillis()-t));
	}
/*
	private void createPropertiesFile(String dir) {
		 // the properties file will be used to check the file modification date
	    // and to store other parameters
	        Properties prop = new Properties();
	        prop.setProperty(KEY_ORIGINAL_PATH, originalPath);
	        if (isLocal) {
	            try {
	                String modifDate = new File(originalPath).lastModified() + "";
	                prop.setProperty(KEY_LAST_MODIFICATON_DATE, modifDate);
	            } catch (Exception e) {}
	        }
	        prop.setProperty(KEY_PROCESSING_DATE, DateFormat.getDateTimeInstance(DateFormat.LONG, DateFormat.LONG).format(new Date()));
	        prop.setProperty(KEY_NSIDE_FILE, newNSideImage+""); // TODO : oui, je sais, j'ai merdé sur les noms !! newNSideImage devrait s'appeler newNSideFile
	        prop.setProperty(KEY_NSIDE_PIXEL, newNSideFile+""); // et newNSideFile devrait s'appeler newNSidePixel

	        prop.setProperty(KEY_ORDER_GENERATED_IMGS, hpxOrderGeneratedImgs+"");

	        prop.setProperty(KEY_ORDERING, ordering);

	        prop.setProperty(KEY_TFIELDS, nField+"");

	        prop.setProperty(KEY_TTYPES, Util.join(tfieldNames, ','));

	        prop.setProperty(KEY_LOCAL_DATA, isLocal+"");

	        prop.setProperty(KEY_GZ, isGZ+"");

	        prop.setProperty(KEY_OFFSET, initialOffsetHpx+"");

	        prop.setProperty(KEY_SIZERECORD, sizeRecord+"");

	        prop.setProperty(KEY_ISPARTIAL, isPartial+"");

	        prop.setProperty(KEY_ARGB, isARGB+"");

	        prop.setProperty(KEY_NBPIXGENERATEDIMAGE, nbPixGeneratedImage+"");

	        prop.setProperty(KEY_CURTFORMBITPIX, curTFormBitpix+"");

	        prop.setProperty(KEY_COORDSYS, coordsys+"");

	        prop.setProperty(KEY_LENHPX, Util.join(lenHpx, ','));

	        prop.setProperty(KEY_TYPEHPX, Util.join(typeHpx, ','));

	        prop.setProperty(KEY_ALADINVERSION, aladin.VERSION);


	        try {
	            prop.store(new FileOutputStream(propertiesFile(dir)), null);
	        } catch (FileNotFoundException e) {
	            return false;
	        } catch (IOException e) {
	            return false;
	        }

	        return true;
	    }
	}
	*/
	private void searchMinMax() {
	   
	   long max = Util.nbrPix(Util.nside(ordermin));
	   
		// nummin nummax
		String path = Util.getFilePath(localServer, ordermax, 0);
		path = path.substring(0, path.lastIndexOf(Util.FS)); // enleve le nom de fichier
		path = path.substring(0, path.lastIndexOf(Util.FS)); // enleve le nom de repertoire
		
		File f = new File(path);
		String[] dirs = f.list();
		npix_list = new ArrayList<Long>();
		for (String dir : dirs) {
			int i = Util.getNDirFromPath(dir+Util.FS);
			// ajoute tous les losanges touchés par ce répertoire
			for (long n = Util.idx(i, ordermax, ordermin) ; n <= Util.idx(i+10000-1, ordermax, ordermin) ; n++) {
			   if( n>max ) break;
				if (!npix_list.contains(n)) npix_list.add(n);
			}
		}
	}



	/** Création d'un losange et de toute sa descendance si nécessaire.
	 * Méthode récursive qui 
	 * 1) Vérifie si le travail n'a pas déjà été fait en se basant sur
	 *    l'existance d'un fichier fits (si overwrite à false)
	 * 2) Si order==maxOrder, calcul le losange terminal => createLeaveHpx(...)
	 * 3) sinon concatène les 4 fils (appel récursif) en 1 losange => createNodeHpx(...)
	 * 
	 * @param path  répertoire où stocker la base
	 * @param sky   Nom de la base (premier niveau de répertoire dans path)
	 * @param order Ordre healpix du losange de départ
	 * @param maxOrder Ordre healpix max de la descendance
	 * @param npix Numéro healpix du losange
	 * @param overwrite true s'il doit y avoir écrasement d'une base antérieure,
	 *                  false si on reprend le calcul sur ce qui a déjà été fait
	 * @param fast true si on utilise la méthode la plus rapide (non bilinéaire, non moyennée)
	 * @return Le losange
	 */
	Fits createHpx(HpxBuilder hpx, String path,
			int order, int maxOrder, long npix,boolean fast) throws Exception {
		String file = Util.getFilePath(path,order,npix);

// Plus possible dans le cas de travail en multi-thread, mais le pere tueras les fils (PF - juillet 2010)
//		if (thread != Thread.currentThread()) return null;
		
		// si le process a été arrêté on essaie de ressortir au plus vite
		if (stopped) {
			return null;
		}
		
		if( order==maxOrder ) return createLeaveHpx(hpx,file,order,npix, fast);      
		Fits fils[] = new Fits[4];
		boolean found = false;
		for( int i =0; !stopped && i<4; i++ ) {
			fils[i] = createHpx(hpx, path,order+1,maxOrder,npix*4+i,fast);
			if (fils[i] != null && !found)
				found = true;
		}
		if (!found) return null;
		for( int i =0; i<4; i++ ) {
			// s'il y a au moins un fils, on met les autres à vides et pas null
			// pour pouvoir quand meme construire le parent (tronqué)
			if (fils[i] == null) {
				fils[i] = new Fits(SIDE,SIDE,bitpix);
			}		
		}
		return createNodeHpx(file,path,order,npix,fils,fast);
	}
	
	
	// Classe des threads de calcul
    class ThreadBuilder extends Thread {
       String outpath;
       int ordermin;
       int ordermax;
       boolean overwrite;
       boolean fast;
       HpxBuilder hpx;
       static final int WAIT=0;
       static final int EXEC=1;
       static final int DIED=2;
       
       private int mode=WAIT;
       private boolean encore=true;
       
       public ThreadBuilder(String name,String outpath, HpxBuilder hpx, int ordermin,int ordermax,boolean overwrite,boolean fast) {
          super(name);
          this.outpath = outpath;
          this.ordermin = ordermin;
          this.ordermax = ordermax;
          this.overwrite = overwrite;
          this.fast = fast;
          this.hpx = hpx;
          Aladin.trace(3,"Creating "+getName());
       }
       
       /** Le thread est mort */
       public boolean isDied() { return mode==DIED; }
       
       /** Retourne le mode courant du thread (WAIT,EXEC ou DIED) */
       public int getMode() { return mode; }
       
       /** Demande la mort du thread */
       public void tue() { encore=false; }
       
       public void run() {
          mode=EXEC;
          while( encore ) {
             long npix = getNextNpix();
             if( npix==-1 ) break;
             try { 
//                System.out.println(Thread.currentThread().getName()+" process tree "+npix+"/"+NMAX+"...");
                Fits f = createHpx(hpx, outpath, ordermin, ordermax, npix, fast);
                if (f!=null) {
                	lastN3 = (int)npix;
                }
             } catch( Throwable e ) { e.printStackTrace(); }
          }
          mode=DIED;
          Aladin.trace(3,Thread.currentThread().getName()+" died !");
       }
    }
	
    // Retourne le prochain numéro de pixel à traiter par les threads de calcul, -1 si terminer
	private long getNextNpix() {
	   getlock();
	   long pix = NCURRENT==npix_list.size()  ? -1 : npix_list.get(NCURRENT++);
	   progress++;
	   unlock();
	   return pix;
	}
	
	// Gère l'accès exclusif par les threads de calcul à la liste des losanges à traiter (npix_list) 
	private void getlock() {
	   while( true ) {
	      synchronized(lockObj) { if( !lock ) { lock=true; return; } }
	      cds.tools.Util.pause(10);
	   }
	}
	private void unlock() { synchronized(lockObj) { lock=false; } }
	
    private Object lockObj = new Object();
    private boolean lock=false;
	
    // Liste des Threads de calcul
    private ArrayList<ThreadBuilder> threadList = new ArrayList<ThreadBuilder>();
	private int fct;
	private double bscale;
	private double bzero;
	private double blank;
	private double[] datacut;
    
	// Crée une série de threads de calcul
	private void createThread(int n,String outpath,int ordermin,int ordermax,boolean overwrite,boolean fast, boolean keepBB) {
		for( int i=0; i<n; i++ ) {
			HpxBuilder hpx = new HpxBuilder();
			hpx.setBitpix(bitpix, keepBB);
			hpx.setLocalServer(localServer);
			hpx.createHealpixOrder(ORDER);
			hpx.setBscale(bscale);
			hpx.setBzero(bzero);
			hpx.setBlank(blank);
			hpx.setDataCut(datacut);
			ThreadBuilder t = new ThreadBuilder("Builder"+i,outpath, hpx,ordermin,ordermax,overwrite,fast);
			threadList.add( t );
			t.start();
		}
	}

	// Demande l'arrêt de tous les threads de calcul
	void destroyThread() {
	   Iterator<ThreadBuilder> it = threadList.iterator();
	   while( it.hasNext() ) it.next().tue();
	}
	
	// Retourne true s'il reste encore au-moins un thread de calcul vivant
	boolean stillAlive() {
	   Iterator<ThreadBuilder> it = threadList.iterator();
	   while( it.hasNext() ) if( !it.next().isDied() ) return true;
	   return false;
	}


	/** Création d'un losange par concaténation de ses 4 fils
	 * et suppression des fichiers 8bits FITS des fils en question
	 * puisque désormais inutiles.
	 * @param file Nom du fichier complet, mais sans l'extension
	 * @param path Path de la base
	 * @param sky  Nom de la base
	 * @param order Ordre Healpix du losange
	 * @param npix Numéro Healpix du losange
	 * @param fils les 4 fils du losange
	 */
	Fits createNodeHpx(String file,String path,int order,long npix,Fits fils[], boolean fast) throws Exception {
		if (npix%1000 == 0 || DEBUG)
			System.out.print("Cumul de "+file+"... ");
		int w=SIDE;
		// essaye de récupérer l'ancien fichier s'il existe
		Fits out;
		out = findFits(file+".fits");
		out = new Fits(w,w,bitpix);
		out.setBlank(getBlank());
		if (getBscale() != Double.NaN && getBzero() != Double.NaN) { 
			out.setBscale(getBscale());
			out.setBzero(getBzero());
		}
		Fits in;
		for( int dg=0; dg<2; dg++ ) {
			for( int hb=0; hb<2; hb++ ) {
				int quad = dg<<1 | hb;
				in = fils[quad];
				int offX = (dg*w)>>>1;
				int offY = ((1-hb)*w)>>>1;
				double pix,p1,p2=0,p3=0,p4=0;
				for( int y=0; y<w; y+=2 ) {
					for( int x=0; x<w; x+=2 ) {
						p1=in.getPixelDouble(x,y);
//						if (fast) {
//							pix  = p1;
//						}
//						else {
							p2=in.getPixelDouble(x+1,y);
							p3=in.getPixelDouble(x,y+1);
							p4=in.getPixelDouble(x+1,y+1);
							if( p1>p2 && (p1<p3 || p1<p4) || p1<p2 && (p1>p3 || p1>p4) ) pix=p1;
							else if( p2>p1 && (p2<p3 || p2<p4) || p2<p1 && (p2>p3 || p2>p4) ) pix=p2;
							else if( p3>p1 && (p3<p2 || p3<p4) || p3<p1 && (p3>p2 || p3>p4) ) pix=p3;
							else pix=p4;
//						}
						out.setPixelDouble(offX+(x>>>1), offY+(y>>>1), pix);
					}
				}
			}
		}
		// écrit les vrais pixels en FITS
		out.writeFITS(file+".fits");
		
		for( int i=0; i<4; i++ ) {
			fils[i].free();
		}

		return out;
	}

	/** Création d'un losange par concaténation de ses 4 fils
	 * et suppression des fichiers 8bits FITS des fils en question
	 * puisque désormais inutiles.
	 * @param file Nom du fichier complet, mais sans l'extension
	 * @param path Path de la base
	 * @param sky  Nom de la base
	 * @param order Ordre Healpix du losange
	 * @param npix Numéro Healpix du losange
	 * @param fils les 4 fils du losange
	 * @deprecated
	 */
	/*
	Fits createNodeHpx(String file,String path,int order,long npix,Fits fils[], boolean overwrite) throws Exception {
		switch (bitpix) {
			case 8:
			case 16:
			case 32:
				return createNodeHpxInt(file, order, npix, fils,overwrite);
			case -32:
			case -64:
				return createNodeHpxDouble(file, order, npix, fils,overwrite);
			case 0:
				return createNodeHpxColor(file, order, npix, fils,overwrite);
		}
		return null;
	}*/
	
	/**
	 * 
	 * @param file
	 * @param order
	 * @param npix
	 * @param fils
	 * @param overwrite
	 * @return
	 * @deprecated
	 * @throws Exception
	 */
	Fits createNodeHpxInt(String file,int order,long npix,Fits fils[], boolean overwrite) throws Exception {
//		long t= System.currentTimeMillis();
		if (npix%1000 == 0 || DEBUG) System.out.println("Cumul de "+file+"... ");
		int w=SIDE;
		// essaye de récupérer l'ancien fichier s'il existe
		Fits out;
		out = findFits(file+".fits");
		// si le fichier fits existe déjà et qu'on ne doit pas l'écraser
		if (out != null && !overwrite) {
			// on re-créé juste le jpg
			toJPG(out,file+".jpg");
			return out;
		}
		out = new Fits(w,w,bitpix);
		out.setBlank(getBlank());
		if (getBscale() != Double.NaN && getBzero() != Double.NaN) { 
			out.setBscale(getBscale());
			out.setBzero(getBzero());
		}
		Fits in;
		int pix,p1,p2=0,p3=0,p4=0;
		for( int dg=0; dg<2; dg++ ) {
			for( int hb=0; hb<2; hb++ ) {
				int quad = dg<<1 | hb;
				in = fils[quad];
				int offX = (dg*w)>>>1;
				int offY = ((1-hb)*w)>>>1;
				for( int y=0; y<w; y+=2 ) {
					for( int x=0; x<w; x+=2 ) {
						p1=in.getPixelInt(x,y);
						p2=in.getPixelInt(x+1,y);
						p3=in.getPixelInt(x,y+1);
						p4=in.getPixelInt(x+1,y+1);
					
                        if( p1>p2 && (p1<p3 || p1<p4) || p1<p2 && (p1>p3 || p1>p4) ) pix=p1;
                        else if( p2>p1 && (p2<p3 || p2<p4) || p2<p1 && (p2>p3 || p2>p4) ) pix=p2;
                        else if( p3>p1 && (p3<p2 || p3<p4) || p3<p1 && (p3>p2 || p3>p4) ) pix=p3;
                        else pix=p4;
		                  
						out.setPixelInt(offX+(x>>>1), offY+(y>>>1), pix);
					}
				}
			}
		}
		// écrit les vrais pixels en FITS
		out.writeFITS(file+".fits");
		// l'écrit en JPG
//		toJPG(out,file+".jpg");
		
		for( int i=0; i<4; i++ ) {
			fils[i].free();
		}
//		System.out.println("=> "+(System.currentTimeMillis()-t)+"ms");
		return out;
	}
	
	
	/**
	 * 
	 * @param file
	 * @param order
	 * @param npix
	 * @param fils
	 * @param overwrite
	 * @return
	 * @deprecated
	 * @throws Exception
	 */
	Fits createNodeHpxDouble(String file,int order,long npix,Fits fils[], boolean overwrite) throws Exception {
//		long t= System.currentTimeMillis();
		if (npix%1000 == 0 || DEBUG)
			System.out.print("Cumul de "+file+"... ");
		int w=SIDE;
		// essaye de récupérer l'ancien fichier s'il existe
		Fits out;
		out = findFits(file+".fits");
		// si le fichier fits existe déjà et qu'on ne doit pas l'écraser
		if (out != null && !overwrite) {
			// on re-créé juste le jpg
			toJPG(out,file+".jpg");
			return out;
		}
		out = new Fits(w,w,bitpix);
		out.setBlank(getBlank());
		if (getBscale() != Double.NaN && getBzero() != Double.NaN) { 
			out.setBscale(getBscale());
			out.setBzero(getBzero());
		}
		Fits in;
		for( int dg=0; dg<2; dg++ ) {
			for( int hb=0; hb<2; hb++ ) {
				int quad = dg<<1 | hb;
				in = fils[quad];
				int offX = (dg*w)>>>1;
				int offY = ((1-hb)*w)>>>1;
				double pix,p1,p2=0,p3=0,p4=0;
				for( int y=0; y<w; y+=2 ) {
					for( int x=0; x<w; x+=2 ) {
						p1=in.getPixelDouble(x,y);
						p2=in.getPixelDouble(x+1,y);
						p3=in.getPixelDouble(x,y+1);
						p4=in.getPixelDouble(x+1,y+1);
					
                        if( p1>p2 && (p1<p3 || p1<p4) || p1<p2 && (p1>p3 || p1>p4) ) pix=p1;
                        else if( p2>p1 && (p2<p3 || p2<p4) || p2<p1 && (p2>p3 || p2>p4) ) pix=p2;
                        else if( p3>p1 && (p3<p2 || p3<p4) || p3<p1 && (p3>p2 || p3>p4) ) pix=p3;
                        else pix=p4;
		                  
                        out.setPixelDouble(offX+(x>>>1), offY+(y>>>1), pix);
					}
				}
			}
		}
		// écrit les vrais pixels en FITS
		out.writeFITS(file+".fits");
		// l'écrit en JPG
//		toJPG(out,file+".jpg");
		
		for( int i=0; i<4; i++ ) {
			fils[i].free();
		}

		return out;
	}
	

	/**
	 * 
	 * @param file
	 * @param order
	 * @param npix
	 * @param fils
	 * @param overwrite
	 * @return
	 * @throws Exception
	 * @deprecated
	 */
	Fits createNodeHpxColor(String file,int order,long npix,Fits fils[], boolean overwrite) throws Exception {
		if (DEBUG)
			System.out.print("Cumul de "+file+"... ");
		int w=SIDE;
		// essaye de récupérer l'ancien fichier s'il existe
		Fits out;
		out = findFits(file+".fits");
		// si le fichier fits existe déjà et qu'on ne doit pas l'écraser
		if (out != null && !overwrite) {
			// on re-créé juste le jpg
			toJPG(out,file+".jpg");
			return out;
		}
		out = new Fits(w,w,bitpix);
		Fits in;
		for( int dg=0; dg<2; dg++ ) {
			for( int hb=0; hb<2; hb++ ) {
				int quad = dg<<1 | hb; // multiplie par 2
				in = fils[quad];
				in.inverseYColor();
				int offX = (dg*w)>>>1; // divise par 2 
				int offY = ((1-hb)*w)>>>1;// divise par 2
				int pix=0;//,p1,p2=0,p3=0,p4=0;
				for( int y=0; y<w; y+=2 ) {
					for( int x=0; x<w; x+=2 ) {
						pix=in.getPixelRGB(x,y);
		                  
                        out.setPixelRGB(offX+(x>>>1), offY+(y>>>1), pix);
					}
				}
			}
		}
		out.inverseYColor();
		out.writeJPEG(file+".jpg");
		
		for( int i=0; i<4; i++ ) {
			fils[i].free();
		}
//		System.out.println("=> "+(System.currentTimeMillis()-t)+"ms");
		return out;
	}
	
	/** Création d'un losange terminal par algorithme du plus proche pixel
	 * @param file Nom du fichier de destination (complet mais sans l'extension)
	 * @param order Ordre healpix du losange
	 * @param npix Numéro Healpix du losange
	 * @param overwrite écrase le précédent losange
	 * @param fast méthode rapide (non biblinéaire, non moyennée)
	 * @return null si rien trouvé pour construire ce fichier
	 */
	Fits createLeaveHpx(HpxBuilder hpx, String file,int order,long npix,boolean fast) throws Exception {
		long t = System.currentTimeMillis();
		
		// essaye de récupérer l'ancien fichier s'il existe
		Fits out = findFits(file+".fits");
		if (out != null) {
			return out;
		}
		
		// il n'existe pas donc, on le créé 
		int nside_file = Util.nside(order);
		int nside = Util.nside(order+ORDER);
		
//		if( fast || bitpix==0 ) out = hpx.buildHealpix(nside_file, npix, nside);
//      else out = hpx.buildBestHealpix(nside_file, npix, nside);
		
        if( DSS ) out = hpx.buildDSSHealpix(nside_file, npix, nside);
        else out = hpx.buildHealpix(nside_file, npix, nside, fast);
		if( out !=null ) {
			createPath(file);
			// convertit l'ordre 
			if (bitpix!=0) {
				// écrit les vrais pixels
				out.writeFITS(file+".fits");
				// puis en JPG
//				toJPG(out,file+".jpg");
			}
			else {
				out.inverseYColor();
				out.writeJPEG(file+".jpg");
			}
		}

		if( out!=null && (npix%10 == 0 || DEBUG) ) Aladin.trace(3,"Génération de "+file+" ("+(System.currentTimeMillis()-t)+"ms)");
		return out;
	}
	
	/** Création si nécessaire des répertoires et sous-répertoire du fichier 
	 * passé en paramètre */
	public void createPath(String filename) {
	   File f;
	   // Pour accélerer, on teste d'abord l'existence éventuelle du dernier répertoire
	   int i = filename.lastIndexOf('/');
	   if( i<0 ) return;
	   f = new File( filename.substring(0,i) ) ;
	   if( f.exists() ) return;

	   // Test et création si nécecessaire des répertoires
	   for( int pos=filename.indexOf('/',3); pos>=0; pos=filename.indexOf('/',pos+1)) {
	      f = new File( filename.substring(0,pos) );
	      if( !f.exists() ) f.mkdir();
	   }
	}

	/** Recherche et chargement d'un losange déjà calculé (présence du fichier Fits 8 bits).
	 *  Retourne null si non trouvé
	 * @param filefits Nom du fichier fits (complet avec extension)
	 */
	Fits findJpg(String file) throws Exception {
//		long t=System.currentTimeMillis();
		File f = new File(file+".jpg");
		if( !f.exists() ) return null;
		Fits out = new Fits();
		out.loadJpeg(file+".jpg", true);
//		System.out.println("Relecture de "+file+".jpg ... => "+(System.currentTimeMillis()-t)+"ms");
		return out;
	}

	/** Recherche et chargement d'un losange déjà calculé (présence du fichier Fits 8 bits).
	 *  Retourne null si non trouvé
	 * @param filefits Nom du fichier fits (complet avec extension)
	 */
	Fits findFits(String filefits) throws Exception {
		File f = new File(filefits);
		if( !f.exists() ) return null;
		Fits out = new Fits();
		MyInputStream is = new MyInputStream( new FileInputStream(f)); 
		out.loadFITS(is);
		out.setFilename(filefits);
		is.close();
		return out;
	}
	
	protected void toJPG(Fits out, String filename) throws Exception {
		double [] range;
		if (automin == automax && automin == 0) {
			range = out.findAutocutRange();
			automin = range[0];
			automax = range[1];
		}
		
		// l'écrit en JPG
		switch (fct) {
		case JPGPanel.ASINH: out.toPix8ASinH(automin, automax);
		case JPGPanel.LOG:out.toPix8Log(automin, automax);
		case JPGPanel.SQRT:out.toPix8Sqrt(automin, automax);
		case JPGPanel.SQR:out.toPix8Pow(automin, automax);
		case JPGPanel.LINEAR:out.toPix8(automin, automax);
		}
		out.writeJPEG(filename);
	}


	public int getProgress() {
		if (NMAX == 0)
			return 0;
		return (int) (progress*100./NMAX);
	}

	public void setBScaleBZero(double bscale, double bzero) {
		this.bscale = bscale;
		this.bzero = bzero;
	}
	public void setBlank(double blank) {
		this.blank = blank;
	}

	/**
	 * @return the bscale
	 */
	public double getBscale() {
		return bscale;
	}
	
	/**
	 * @return the bzero
	 */
	public double getBzero() {
		return bzero;
	}

	/**
	 * @param automin the automin to set
	 */
	public void setAutoCut(double[] autocut, int fct) {
		automin = autocut[0];
		automin = automin/getBscale() + getBzero();
		automin = Fits.toBitpixRange(automin, bitpix, new double[] {autocut[2], autocut[3]});
		automax = autocut[1];
		automax = automax/getBscale() + getBzero();
		automax = Fits.toBitpixRange(automax, bitpix, new double[] {autocut[2], autocut[3]});
		
		this.fct = fct; 
		datacut = new double[] {autocut[2], autocut[3]}; 
		
	}


	/**
	 * @return the blank
	 */
	public double getBlank() {
		return blank;
	}

	
	public void stop() {
		stopped = true;
	}

	public void reset(String path) {
		cds.tools.Util.deleteDir(new File(path));
	}

	public int getLastN3() {
		return lastN3;
	}
	
	/*
	public void testVitesse() throws Exception {
		String outpath = "/tmp/";
		localServer = outpath + AllskyConst.HPX_FINDER;
		hpx = new HpxBuilder(16, localServer); 

		outpath += FS; 
		hpx.createHealpixOrder(ORDER);
		
		long t= System.currentTimeMillis();
		Thread thisThread = Thread.currentThread();
		thread = thisThread;
		long max = Util.getMax(11);
		for (int n = 0 ; thread == thisThread && n < 1 ; n++) {
			hpx.downFiles = new ArrayList<DownFile>();
			createHpx(outpath, AllskyConst.SURVEY, 3, 11, 1, false);
		}
		System.out.println("=> "+(System.currentTimeMillis()-t)+"ms");
	}*/
}
