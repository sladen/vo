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
import java.util.Arrays;
import java.util.Random;

import cds.aladin.Aladin;
import cds.fits.Fits;
import cds.tools.pixtools.Util;


/**
 * Classe de traitement Fits pour Anaïs
 */
final public class SkyGenerator {
   
   private static final String FS = System.getProperty("file.separator");
   
   double progress = 0;
   
   // Fichier source de test
   Fits in;
   int leave=0; // Nombre de feuilles traitées (losanges terminaux)
   int node=0;  // Nombre de noeuds traités (losanges non-terminaux)
      
   public SkyGenerator() {}
   
   
//   /** Création des fichiers Allsky FITS 8 bits et JPEG pour tout un niveau Healpix
//    * @param path Emplacement de la base
//    * @param survey nom du survey
//    * @param order order Healpix
//    * @param outLosangeWidth largeur des losanges pour le Allsky (typiquement 64 ou 128 pixels)
//    * @param mode 0-first, 1-moyenne, 2-mediane, 13-sigma
//    * @param flagColor true si on travaille en RGB
//    */
//   public void createAllSky(String path,String survey,int order,int outLosangeWidth,int mode,boolean flagColor) throws Exception {
//      long t=System.currentTimeMillis();
//      int nside = (int)Math.pow(2,order);
//      int n = 12*nside*nside;
//      int nbOutLosangeWidth = (int)Math.sqrt(n);
//      int nbOutLosangeHeight = (int)((double)n/nbOutLosangeWidth);
//      if( (double)n/nbOutLosangeWidth!=nbOutLosangeHeight ) nbOutLosangeHeight++;
//      int outFileWidth = outLosangeWidth * nbOutLosangeWidth;
//System.out.println("Création Allsky order="+order+" mode="+MODE[mode]+(flagColor ? " color":"")
//      +": "+n+" losanges ("+nbOutLosangeWidth+"x"+nbOutLosangeHeight
//      +" de "+outLosangeWidth+"x"+outLosangeWidth+" soit "+outFileWidth+"x"+nbOutLosangeHeight*outLosangeWidth+" pixels)...");
//      Fits out = new Fits(outFileWidth,nbOutLosangeHeight*outLosangeWidth, flagColor ? 0 : 8);
//      
//      for( int npix=0; npix<n; npix++ ) {
//    	  progress = npix*100./n;
//         String name = Util.getFilePath(survey, order, npix);
//         Fits in = new Fits();
//         String filename = path+FS+name;
//if( npix%100==0 ) System.out.print(npix+"...");
//         try {
//            if (flagColor) {
//            	in.loadJpeg(filename+".jpg",flagColor);
//            }
//            else
//            	in.loadFITS(filename+".fits");
//            if( !flagColor && in.bitpix!=8 ) {
//            	if (survey.equalsIgnoreCase("WENSS"))
//            		in.autocutWENSS();
//            	else if (survey.startsWith("IRAS")) {
//        			if (survey.endsWith("12MU"))
//        				in.autocutIRIS12();
//        			else if (survey.endsWith("100MU"))
//        				in.autocutIRIS60();
//        			else if (survey.endsWith("60MU"))
//        				in.autocutIRIS25();
//        			else if (survey.endsWith("25MU"))
//        				in.autocutIRIS100();
////        				in.toPix8IRAS();
//            	}
//            	else if (survey.startsWith("2MASS"))
//            		in.autocut2MASS();
//            	else if (survey.startsWith("GLIMPSEALL1"))
//            		in.autocutGLIMPSE1();
//            	else if (survey.startsWith("GLIMPSEALL2"))
//            		in.autocutGLIMPSE2();
//            	else if (survey.startsWith("GLIMPSEALL3"))
//            		in.autocutGLIMPSE3();
//            	else if (survey.startsWith("GLIMPSEALL4"))
//            		in.autocutGLIMPSE4();
//
//            	
//            	else {
//            		if (min ==0 && max == 0)
//            			in.toPix8(); // --> conséquence un autocut pour chaque losange, sympa, 
//            		// mais peut avoir des effets visuels bizarres
//            		else
//            			in.toPix8(min,max);
//            	}
//            }
//            int yLosange=npix/nbOutLosangeWidth;
//            int xLosange=npix%nbOutLosangeWidth;
//            int gap = in.width/outLosangeWidth;
//            int nombre=gap*gap;
//            int liste [] = new int[nombre];
//            for( int y=0; y<in.width/gap; y++ ) {
//               for( int x=0; x<in.width/gap; x++ ) {
//                  
//                  double total=0;
//                  double carre=0;
//                  
//                  int i=0;
//                  int p=0;
//                  int max=Integer.MIN_VALUE;
//                  for( int y1=0; y1<gap; y1++ ) {
//                     for( int x1=0; x1<gap; x1++) {
//                        p = flagColor ? in.getPixelRGB(x*gap+x1,y*gap+y1) : in.getPix8(x*gap+x1,in.height-1-(y*gap+y1)) ;
//                        if( mode==FIRST ) break;
//                        if( mode==MEDIANE ) { liste[i++]=p; continue; }
//                        if( mode==MAX ) { if( p>max ) max=p; continue; }
//                        total+=p;
//                        carre+=p*p;
//                     }
//                  }
//                  int pix8=0;
//                  switch(mode) {
//                     case FIRST:   pix8=p; break;
//                     case MOYENNE: pix8 = (int)(total/nombre); break;
//                     case MEDIANE: Arrays.sort(liste); pix8 = liste[(int)nombre/2]; break;
//                     case SIGMA:   pix8=(int)( Math.sqrt(carre/nombre - (total/nombre)*(total/nombre))); break;
//                     case MAX:     pix8=max; break;
//                     default: throw new Exception("mode "+MODE[mode]+" non supporté !");
//                  }
//                  
//                  int xOut= xLosange*outLosangeWidth + x;
//                  int yOut = yLosange*outLosangeWidth +y;
//                  
//                  if( flagColor ) out.setPixelRGB(xOut, yOut, pix8);
//                  else {
//                     out.setPixelInt(xOut, out.height-1-yOut, pix8);
//                     out.setPix8(xOut, out.height-1-yOut, pix8);
//                  }
//               }
//            }
//         } catch( Exception e ) {
////            System.err.println("Erreur sur "+name +" ("+e.getMessage()+")");
//         } finally {
//        	 progress=100;
//         }
//      }
//      String filename = path+FS+survey+FS+"Norder"+order+FS+"Allsky";
//      cds.tools.Util.createPath(filename);
//      out.writeJPEG(filename+".jpg");
//      if( !flagColor ) out.writeFITS(filename+".fits");
//      
//      System.out.println("\nConstruction "+survey+FS+"Norder"+order+FS+"Allsky en "+
//            (int)((System.currentTimeMillis()-t)/1000)+"s");
//      progress=100;
//   }
//   

   /** Création des fichiers Allsky.fits (true bitpix) et Allsky.jpg (8 bits) pour tout un niveau Healpix
    * Rq : seule la méthode FIRST est supportée
    * @param path Emplacement du survey
    * @param order order Healpix
    * @param outLosangeWidth largeur des losanges pour le Allsky (typiquement 64 ou 128 pixels)
    */
   public void createAllSky(String path,int order,int outLosangeWidth,double pixelMin,double pixelMax, boolean keepBB) throws Exception {
      long t=System.currentTimeMillis();
      int nside = (int)Math.pow(2,order);
      int n = 12*nside*nside;
      int nbOutLosangeWidth = (int)Math.sqrt(n);
      int nbOutLosangeHeight = (int)((double)n/nbOutLosangeWidth);
      if( (double)n/nbOutLosangeWidth!=nbOutLosangeHeight ) nbOutLosangeHeight++;
      int outFileWidth = outLosangeWidth * nbOutLosangeWidth;
      
      Aladin.trace(3,"Création Allsky order="+order+" mode=FIRST "
      +": "+n+" losanges ("+nbOutLosangeWidth+"x"+nbOutLosangeHeight
      +" de "+outLosangeWidth+"x"+outLosangeWidth+" soit "+outFileWidth+"x"+nbOutLosangeHeight*outLosangeWidth+" pixels)...");
      Fits out = null;
      
      for( int npix=0; npix<n; npix++ ) {
         progress = npix*100./n;
         String name = Util.getFilePath("", order, npix);
         Fits in = new Fits();
         String filename = path+FS+name;
if( npix%100==0 ) Aladin.trace(3,npix+"...");
         try {
            in.loadFITS(filename+".fits");
            if( out==null ) {
               out = new Fits(outFileWidth,nbOutLosangeHeight*outLosangeWidth,in.bitpix);
               if( in.hasBlank() ) out.setBlank( in.getBlank() );
               out.setBscale( in.getBscale() );
               out.setBzero( in.getBzero() );
               // initilialise toutes les valeurs à Blank
               for( int y=0; y<out.height; y++ ) {
            	   for( int x=0; x<out.width; x++ ) {
            		   out.setPixelDouble(x, out.height-1-y, out.getBlank());
            	   }
               }
            }
                    
            int yLosange=npix/nbOutLosangeWidth;
            int xLosange=npix%nbOutLosangeWidth;
            int gap = in.width/outLosangeWidth;
            for( int y=0; y<in.width/gap; y++ ) {
               for( int x=0; x<in.width/gap; x++ ) {
                  double p=in.getPixelDouble(x*gap,in.height-1-y*gap);
                  if (keepBB)
                	  p = in.getPixelFull(x*gap,in.height-1-y*gap);
                  int xOut= xLosange*outLosangeWidth + x;
                  int yOut = yLosange*outLosangeWidth +y;
//                  double pixelFinal = p/in.getBscale()-in.getBzero();
//                  if (in.bitpix > 0)
//                	  out.setPixelInt(x, y, (int)pixelFinal);
//                  else if (in.bitpix !=0 )
                	  out.setPixelDouble(xOut, out.height-1-yOut, p);
               }
            }
         } catch( Exception e ) {
//            System.err.println("createAllSky error: "+e.getMessage());
         } //finally { progress=100; }
      }
      
      // Détermination des pixCutmin..pixCutmax et min..max directement dans le fichier AllSky
      if( out==null ) throw new Exception("createAllSky error: null output file !");
      double range [] = out.findAutocutRange();
      
      // Indication du pixelmin et pixelmax par l'utilisateur ?
      if( pixelMin!=0 || pixelMax!=0 ) { range[0]=pixelMin; range[1]=pixelMax; }
      
      out.headerFits.setKeyValue("PIXELMIN", range[0]+"");
      out.headerFits.setKeyValue("PIXELMAX", range[1]+"");
      out.headerFits.setKeyValue("DATAMIN",  range[2]+"");
      out.headerFits.setKeyValue("DATAMAX",  range[3]+"");
      Aladin.trace(3,"pixelMinMax = ["+range[0]+" "+range[1]+"] dataMinMax=["+range[2]+" "+range[3]+"]");
      
      // Ecriture du FITS (true bits)
      String filename = path+FS+"Norder"+order+FS+"Allsky";
      cds.tools.Util.createPath(filename);
      out.writeFITS(filename+".fits");
      
      // Cut et Ecriture du JPEG 8 bits
//      out.toPix8(range[0],range[1]);
//      filename = path+FS+"Norder"+order+FS+"Allsky";
//      cds.tools.Util.createPath(filename);
//      out.writeJPEG(filename+".jpg");
      
      Aladin.trace(3,"\nConstruction Allsky en "+ (int)((System.currentTimeMillis()-t)/1000)+"s");
      
      progress=100;
   }
   
   /** Création d'un AllSky JPEG couleur à partir des images JPEG à l'ordre indiqué
    * Rq : seule la méthode FIRST est supportée
    * @param path Emplacement du survey
    * @param order order Healpix
    * @param outLosangeWidth largeur des losanges pour le Allsky (typiquement 64 ou 128 pixels)
    */
   public void createAllSkyJpgColor(String path,int order,int outLosangeWidth) throws Exception {
      long t=System.currentTimeMillis();
      int nside = (int)Math.pow(2,order);
      int n = 12*nside*nside;
      int nbOutLosangeWidth = (int)Math.sqrt(n);
      int nbOutLosangeHeight = (int)((double)n/nbOutLosangeWidth);
      if( (double)n/nbOutLosangeWidth!=nbOutLosangeHeight ) nbOutLosangeHeight++;
      int outFileWidth = outLosangeWidth * nbOutLosangeWidth;
      
      Aladin.trace(3,"Création Allsky order="+order+" mode=FIRST color"
      +": "+n+" losanges ("+nbOutLosangeWidth+"x"+nbOutLosangeHeight
      +" de "+outLosangeWidth+"x"+outLosangeWidth+" soit "+outFileWidth+"x"+nbOutLosangeHeight*outLosangeWidth+" pixels)...");

      Fits out = new Fits(outFileWidth,nbOutLosangeHeight*outLosangeWidth, 0);
      
      for( int npix=0; npix<n; npix++ ) {
         progress = npix*100./n;
         String name = Util.getFilePath(order, npix);
         Fits in = new Fits();
         String filename = path+FS+name;
if( npix%100==0 ) Aladin.trace(3,npix+"...");
         try {
            in.loadJpeg(filename+".jpg",true);
            int yLosange=npix/nbOutLosangeWidth;
            int xLosange=npix%nbOutLosangeWidth;
            int gap = in.width/outLosangeWidth;
            for( int y=0; y<in.width/gap; y++ ) {
               for( int x=0; x<in.width/gap; x++ ) {
                  int p=in.getPixelRGB(x*gap,y*gap);
                  int xOut = xLosange*outLosangeWidth + x;
                  int yOut = yLosange*outLosangeWidth + y;
                  out.setPixelRGB(xOut, yOut, p);
               }
            }
         }
         catch( Exception e ) { 
//          System.err.println("createAllSkyJpgColor error: "+e.getMessage());
         } finally { progress=100; }
      }
      
      if( out==null ) throw new Exception("createAllSkyJpgColor error: null output file !");

      String filename = path+FS+"Norder"+order+FS+"Allsky";
      cds.tools.Util.createPath(filename);
      out.writeJPEG(filename+".jpg");
      
      Aladin.trace(3,"\nConstruction "+filename+" en "+
            (int)((System.currentTimeMillis()-t)/1000)+"s");
      progress=100;
   }

   
   /** Simulation d'un traitement de génération d'une image à partir d'une autre
    * comme le fera SkyBrowser pour la génération de la base Healpix
    */
   public static void main(String[] args) {
      
      try {
         SkyGenerator sg = new SkyGenerator();
         boolean color = false;
         String path="";
         int order=3;
         int size=64;
         double pixelMin=0,pixelMax=0;

         for( int i=0; i<args.length; i++ ) {
            if( args[i].equals("-color") ) color=true;
            else if( args[i].startsWith("-order=") ) order = Integer.parseInt(args[i].substring(7));
            else if( args[i].startsWith("-size=") )  size = Integer.parseInt(args[i].substring(6));
            else if( args[i].startsWith("-cut=") )   {
               String s = args[i].substring(5);
               int j = s.indexOf(',');
               pixelMin = Double.parseDouble(s.substring(0,j));
               pixelMax = Double.parseDouble(s.substring(j+1));
            }
            else path = args[i];
         }
         
         if( color ) sg.createAllSkyJpgColor(path, order, size);
         else sg.createAllSky(path,order,size,pixelMin,pixelMax,true);


//         if (args.length>1) {
//        	 color = Boolean.parseBoolean(args[1]);
////        	 System.out.println("color="+color);
//         }
//         int order = 3;
//
//         if (args.length >= ((color)?3:2) ) {
//        	 order = Integer.parseInt(args[(color)?2:1]);
//            int size = 64;
//            if (args.length==((color)?4:3) )
//               size = Integer.parseInt(args[(color)?3:2]);
////          sg.createAllSky(args[0], args[1],order,size, FIRST,color);
//            sg.createAllSkyJpgColor(args[0],order,size);
//         }
//         else {
////            sg.createAllSky(args[0],args[1],3,64,FIRST,true);
//            sg.createAllSky(args[0],3,64);
//         }

      } catch( Exception e) {
         e.printStackTrace();
         System.out.println("Usage: [-color] [-order=nn] [-size=xx] [-cut=pixelMin,pixelMax] /Path/Survey");
      }
   }


   public double getProgress() {
	   return progress;
   }
}
