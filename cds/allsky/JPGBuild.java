package cds.allsky;

import java.io.File;

import cds.aladin.Aladin;
import cds.fits.Fits;

public class JPGBuild implements Runnable {

	double[] cutminmax;
	String dirpath;
	int progress;
	
	public JPGBuild(double[] cut, String path) {
		dirpath=path;
		cutminmax=cut;
	}
	
	/**
	 * Lance l'écriture JPG de tous les fichiers FITS trouvés dans la hiérarchie
	 * @param cut
	 * @param dir
	 */
	private void toJPG(double[] cut, File dir) {
		File[] children = dir.listFiles();
		for (int i=0; i<children.length; i++) {
			if (children[i].isDirectory())
				toJPG(cut,children[i]);
			else {
				String filename = children[i].getPath();
				filename = filename.substring(0, filename.lastIndexOf("."));
				toJPG(cut, filename);
			}
		}
	}

	/**
	 * Ouvre une image fits et la convertit en JPG au meme endroit
	 * @param cut
	 * @param filename
	 */
	private void toJPG(final double[] cut, final String filename) {
		Fits file = new Fits();
		try {
			file.loadFITS(filename+".fits");
			// Cut et Ecriture du JPEG 8 bits
			file.toPix8(cut[0],cut[1]);
			file.writeJPEG(filename+".jpg");
			
		} catch (Exception e) {
			Aladin.trace(3,e.getMessage());
		}
	}
	
	public int getProgress() {
		return progress;
	}

	public synchronized void start(){
		(new Thread(this)).start();
	}
	
	public void run() {
		File dir = new File(dirpath);
		if (dir.isDirectory()) {
			File[] children = dir.listFiles();
			// pour tous répertoires Norder du répertoire principal
			for (int i=0; i<children.length; i++) {
				if( children[i].getName().startsWith("Norder") && children[i].isDirectory()) {
					//int n = Integer.parseInt(name.substring(6));
					toJPG(cutminmax,children[i]);
				}
				progress = (int) (i*100./(children.length-1));
			}
		}
		
	}
}