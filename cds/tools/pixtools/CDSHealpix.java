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

package cds.tools.pixtools;

import healpix.core.HealpixIndex;
import healpix.core.base.set.LongRangeSet;
import healpix.tools.SpatialVector;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Vector;

import org.asterope.healpix.PixToolsVector3d;

/** Wrapper Healpix CDS pour pouvoir passer facilement d'une librairie Healpix à une autre */
public final class CDSHealpix {
   
   static final String [] MODE = {
      "Panachage CDS",
      "Kuropatkin (JPL) + Oberto",
      "Will'O Mulan (GAIA)",
      "Kotek (based on Kuropatkin)"
   
   };
   
   static public final int PANACHAGE  = 0;
   static public final int KUROPATKIN = 1;
   static public final int WILL       = 2;
   static public final int KOTEK      = 3;
   
   static public final int MAXMODE=3;
   
   static final String MODEEXCEPTION = "This method is not supported in the current CDSHealpix mode";

   static private int mode = PANACHAGE;
   
   /** Positionne la librairie CDSHealpix à utiliser */
   static public void setMode(int mode) {
      if( mode==CDSHealpix.mode ) return;
      CDSHealpix.mode=mode;
      System.out.println("CDSHealpix current mode: "+MODE[mode]+" library");
   }
   
   /** Retourne le mode CDSHealpix courant */
   static public int getMode() { return mode; }
   
   /** Pour du debug - changement cyclique de mode */
   static public String switchMode() {
      mode++;
      if( mode>MAXMODE ) mode=0;
      return MODE[mode];
   }

   /** Voir Healpix documentation */
   static public double[] pix2ang_nest(long nside,long ipix) throws Exception {
      switch(mode) {
         case PANACHAGE:
         case WILL: synchronized(lockWill) { initWillMode(nside); return hWill.pix2ang_nest(ipix); }
         case KUROPATKIN: return PixTools.pix2ang_nest(nside, ipix);
         case KOTEK: return pixtoolsNestedKotek.pix2ang_nest(nside, ipix);
         default: throw new Exception(MODEEXCEPTION);
      }
   }
   
   /** Voir Healpix documentation */
   static public double[] pix2ang_ring(long nside,long ipix) throws Exception {
      switch(mode) {
         case PANACHAGE:
         case WILL: synchronized( lockWill ) { initWillMode(nside); return hWill.pix2ang_ring(ipix); }
         case KUROPATKIN: return PixTools.pix2ang_ring(nside, ipix);
         case KOTEK: return pixtoolsKotek.pix2ang_ring(nside, ipix);
         default: throw new Exception(MODEEXCEPTION);
      }
   }
   
   /** Voir Healpix documentation */
   static public long ang2pix_nest(long nside,double theta, double phi) throws Exception {
      switch(mode) {
         case PANACHAGE:
         case WILL: synchronized( lockWill ) { initWillMode(nside); return hWill.ang2pix_nest(theta, phi); }
         case KUROPATKIN: return PixTools.ang2pix_nest(nside, theta, phi);
         case KOTEK: return pixtoolsNestedKotek.ang2pix_nest(nside, theta, phi);
         default: throw new Exception(MODEEXCEPTION);
      }
   }
   
   /** Voir Healpix documentation */
   static public long ang2pix_ring(long nside,double theta, double phi) throws Exception {
      switch(mode) {
         case PANACHAGE:
         case WILL: synchronized( lockWill ) { initWillMode(nside); return hWill.ang2pix_ring(theta, phi); }
         case KUROPATKIN: return PixTools.ang2pix_ring(nside, theta, phi);
         case KOTEK: return pixtoolsKotek.ang2pix_ring(nside, theta, phi);
         default: throw new Exception(MODEEXCEPTION);
      }
   }
   
   /** Voir Healpix documentation */
   static public long[] query_disc(long nside,double ra, double dec, double radius) throws Exception {
	   return query_disc(nside, ra, dec, radius, true);
   }
   static public long[] query_disc(long nside,double ra, double dec, double radius, boolean inclusive) throws Exception {
	      switch(mode) {
	      case PANACHAGE:
	         case WILL: return query_discWill(nside,ra,dec,radius, inclusive);
	         case KUROPATKIN: return query_discOberto(nside,ra,dec,radius);
	         case KOTEK : return query_discKotek(nside,ra,dec,radius, inclusive);
	         default: throw new Exception(MODEEXCEPTION);
	      }	   
   }
   /** Voir Healpix documentation */
   static public long[] query_polygon(long nside,ArrayList<double[]>list) throws Exception {
      switch(mode) {
         case PANACHAGE:
         case WILL: return query_polygonWill(nside,list);
         case KOTEK: return query_polygonKotek(nside,list);
         default: throw new Exception(MODEEXCEPTION);
      }
   }
   
   /** Voir Healpix documentation */
   static public long nest2ring(long nside, long npix) throws Exception  {
      switch(mode) {
         case PANACHAGE:
         case WILL: synchronized( lockWill ) { initWillMode(nside); return hWill.nest2ring(npix); }
         case KUROPATKIN: return PixTools.nest2ring(nside, npix);
         case KOTEK: return pixtoolsNestedKotek.nest2ring(nside, npix);
         default: throw new Exception(MODEEXCEPTION);
      }
   }
   
   /** Voir Healpix documentation */
   static public double pixRes(long nside) {
      double res = 0.;
      double degrad = Math.toDegrees(1.0);
      double skyArea = 4.*Math.PI*degrad*degrad;
      double arcSecArea = skyArea*3600.*3600.;
      long npixels = 12*nside*nside;
      res = arcSecArea/npixels;
      res = Math.sqrt(res);
      return res;
   }
   
   /** Voir Healpix documentation */
   static public double[] radecToPolar(double[] radec) { return radecToPolar(radec,new double[2]); }
   static public double[] radecToPolar(double[] radec,double polar[]) {
      polar[0] = Math.PI/2. - radec[1]/180.*Math.PI;
      polar[1] = radec[0]/180.*Math.PI;
      return polar;
   }
   
   /** Voir Healpix documentation */
   static public double[] polarToRadec(double[] polar) { return polarToRadec(polar,new double[2]); }
   static public double[] polarToRadec(double[] polar,double radec[]) {
      radec[1] = (Math.PI/2. - polar[0])*180./Math.PI;
      radec[0] = polar[1]*180./Math.PI;
      return radec;
   }
   
   static final private double LOG2 = Math.log(2);
   static public long log2(long x) { return (long)(Math.log(x)/LOG2); }
   
   
   
   // ------------------------------- Particularités Kotek
   
   static private org.asterope.healpix.PixTools pixtoolsKotek = new org.asterope.healpix.PixTools();  // Objet Healpix mode Kotek
   static private org.asterope.healpix.PixToolsNested pixtoolsNestedKotek = new org.asterope.healpix.PixToolsNested();  // Objet Healpix mode Kotek
   
   static private long[] query_discKotek(long nside,double ra, double dec, double radius, boolean inclusive) throws Exception {
      PixToolsVector3d vector = createPixToolsVector3dKotek(ra,dec);
      org.asterope.healpix.LongRangeSet list = pixtoolsKotek.query_disc(nside, vector, radius, inclusive);
      if( list==null ) return new long[0];
      return ring2nest(nside,list.toArray());
   }
   
   static private long [] ring2nest(long nside,long [] a) {
      long[] b = new long[a.length];
      for( int i=0; i<a.length; i++ ) b[i] = PixTools.ring2nest(nside, a[i]);
      return b;
   }
   
   static private long[] query_polygonKotek(long nside,ArrayList<double[]> cooList) throws Exception {
      ArrayList<PixToolsVector3d> vlist = new ArrayList<PixToolsVector3d>(cooList.size());
      Iterator<double[]> it = cooList.iterator();
      while( it.hasNext() ) {
         double coo[] = it.next();
         vlist.add( createPixToolsVector3dKotek(coo[0], coo[1]) );
         
      }
      org.asterope.healpix.LongRangeSet list = pixtoolsKotek.query_polygon(nside, vlist, 1);
      if( list==null ) return new long[0];
      return ring2nest(nside,list.toArray());
   }      

   static private PixToolsVector3d createPixToolsVector3dKotek(double ra,double dec) {
      return pixtoolsKotek.Ang2Vec(Math.PI/2. -dec/180.*Math.PI,ra/180.*Math.PI);

//      double cd = Math.cos( Math.toRadians(dec) );
//      double x = Math.cos( Math.toRadians(ra)) * cd;
//      double y = Math.sin( Math.toRadians(ra)) * cd;
//      double z = Math.sin( Math.toRadians(dec) );
//      return  new PixToolsVector3d(x, y, z);
   }
   
   
   // ------------------------------- Particularités Will

   static private long nsideWill = -1;  // NSIDE courant dans le mode WILL uniquement
   static private HealpixIndex hWill;  // Objet Healpix dans le mode WILL uniquement
   static private Object lockWill = new Object();
   
   // Initialisation de l'objet de manipulation Healpix (dans le mode WILL uniquement)
   static private void initWillMode(long nside) throws Exception {
      if( nsideWill==nside ) return;
      hWill = new HealpixIndex((int)nside);
      nsideWill=nside;
   }
   
   static private long[] query_discWill(long nside,double ra, double dec, double radius, boolean inclusive) throws Exception {
      synchronized( lockWill ) {
         initWillMode(nside);
         SpatialVector vector = new SpatialVector(ra,dec);
         LongRangeSet list = hWill.queryDisc(vector, radius, 1, inclusive?1:0);
         if( list==null ) return new long[0];
         return list.toArray();
      }
   }

   static private long[] query_polygonWill(long nside,ArrayList<double[]> cooList) throws Exception {
      synchronized( lockWill ) {
         initWillMode(nside);
         ArrayList vlist = new ArrayList(cooList.size());
         Iterator<double[]> it = cooList.iterator();
         while( it.hasNext() ) {
            double coo[] = it.next();
            vlist.add(new SpatialVector(coo[0], coo[1]));

         }
         LongRangeSet list = hWill.query_polygon((int)nside, vlist, 1, 1);
         if( list==null ) return new long[0];
         return list.toArray();
      }
   }      
   
   
//   public static void main(String[] args) {
//      try {
//         int nside = 8;
//         SpatialVector vector = new SpatialVector(219.92904166666668,85.88719444444445);
//         double radius = 3.91698480573189;
//
//         HealpixIndex hi = new HealpixIndex(nside);
//         LongRangeSet vlist = hi.queryDisc(vector, radius / 180 * Constants.PI, 1, 1);
//         long [] list = vlist==null ? new long[0] : vlist.toArray();
//
//         System.out.print("ra="+vector.ra()+ " dec="+vector.dec()+" radius="+radius+" Nside="+nside+" => npixlist:");
//         for( int i = 0; i < list.length; i++ ) {
//            System.out.print(" " + list[i]);
//         }
//         System.out.println();
//      } catch( Exception e ) {
//         e.printStackTrace();
//      }
//   }


   // ------------------------------- Particularités Kuropatkin + Oberto
   
   static private long [] query_discOberto(long nside, double ra, double dec, double radius) {
      radius =  Math.toDegrees(radius) + pixRes(nside)/(3600*2);

      boolean poleN = false, poleS = false;

      // Détermination des cercles concernées
      // theta est inversement proportionnel à delta => on verse min/max
      double thetaMin = Math.PI/2 - Math.toRadians(dec+radius);
      double thetaMax = Math.PI/2 - Math.toRadians(dec-radius);

      // recadre les theta pour etre entre [0;PI]
      if (thetaMin < 0) {
         thetaMin = - thetaMin;
         poleN=true;
      }
      if (thetaMax > Math.PI) {
         thetaMax = 2*Math.PI - thetaMax;
         poleS=true;
      }

      // Détermination de phi, dphi
      double phi = Math.toRadians(ra);
      double dphi = Math.toRadians(radius);
      if ((dphi - Math.PI) > 0)
         dphi = Math.PI;

      long ringMin = PixTools.RingNum(nside, Math.cos(thetaMin));
      long ringMax = PixTools.RingNum(nside, Math.cos(thetaMax));

      Vector candidats = new Vector();

      // si on a un pole dans la vue => taille à PI
      if (poleN || poleS) {
         dphi = Math.PI;
         // Avant le pole
         if (poleS) {
            getNpixListOberto(phi, dphi, nside, ringMin, 4*nside-1, candidats);
            ringMin = 1;
         }
         // Après le pole
         if (poleN) {
            getNpixListOberto(phi, dphi, nside, 1, ringMax, candidats);
            ringMax=4*nside-1;
         }
      }
      else
         getNpixListOberto(phi, dphi, nside, ringMin, ringMax, candidats);

      // Passage sous forme d'un tableau de long[]
      long [] npix = new long[candidats.size()];
      Enumeration e = candidats.elements();
      for( int i=0; i<npix.length; i++ ) {
         npix[i] = ( (Long)e.nextElement()).longValue();
      }

      return npix;
   }

   private static void getNpixListOberto(double phi, double dphi, long nside, long ringMin, long ringMax, Vector candidats) {
      for( long ring=ringMin; ring<=ringMax; ring++ ) {
         if (phi-dphi<0) candidats.addAll( PixTools.InRing(nside,ring,phi+Math.PI*2,dphi,true));
         else candidats.addAll( PixTools.InRing(nside,ring,phi,dphi,true));
      }
   }
   


}
