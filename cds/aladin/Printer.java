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

import java.util.*;
import java.awt.*;

import cds.tools.Util;

/**
 * Objet gerant l'impression
 *
 * @author Pierre Fernique [CDS]
 * @version 1.3 : (déc 2004) - Gestion des mosaïques
 * @version 1.2 : (17 janvier 2004) - Correction décalage image plus petite que
 *                      le cadre + correction taille du champ
 * @version 1.1 : (15 avril 02) - Prise en compte bug de JVM 1.1.8 Windows
 * @version 1.0 : (6 dec 99) - Creation
 */
class Printer implements Runnable {
   Thread thread;                  // Thread pour mettre a jour le log
   private Aladin aladin;
   static int MARGE=22;

   /* INFO:
    * ATTENTION: Java 1.1.8 Windows est completement bugge pour l'impression
    * des images issues de createImage(x,y). La seule methode que j'ai
    * trouvee consiste a faire les overlays directement dans le contexte
    * graphique du printer. Pour cela il a fallu introduire un offset (dx,dy)
    * dans toutes les fonctions draw() des overlays, grilles... et
    * positionner des caches blancs sur les cotes pour "gommer" les overlays
    * qui debordent. C'est vraiment lourd comme contournement de bug
    * mais ca marche
    */
    
   Printer(Aladin aladin) {
      this.aladin=aladin;
      thread = new Thread(this,"AladinPrinter");
      Util.decreasePriority(Thread.currentThread(), thread);
//      thread.setPriority( Thread.NORM_PRIORITY -1);
      thread.start();
   }

   public void run() {
      aladin.setFlagPrint(true);	// Indique qu'il imprime
      try {
         String mode="VIEW";
         if( aladin.view.isMultiView() ) { printMosaique(); mode="MOSAIC"; }
         else printCurrentView(); 
         aladin.log("print",mode);
      } catch( Exception e ) { e.printStackTrace(); }
      aladin.setFlagPrint(false);
     }

   
   /** Impression de la vue courante. */
   private void printCurrentView() {
      int x=MARGE;
      int y=MARGE;
      PrintJob pj = Toolkit.getDefaultToolkit().getPrintJob(aladin.f,"Aladin",null);

      if( pj!=null ) {
         Graphics pg = pj.getGraphics();
         if( pg!=null ) {
            
            ViewSimple v = aladin.view.getCurrentView();
 
            int w = aladin.view.getWidth();
            int h = aladin.view.getHeight();
            // Affichage et calcul de la taille du titre
            y=printTitre(pg,x,y,w);
            
            if( v.pref.active ) v.paintOverlays(pg,null,MARGE,y);

//            // Affichage de l'image raster
//            if( v.pref.active ) pg.drawImage(v.imgprep, MARGE+v.dx, y+v.dy, aladin);
//
//            // Affichage des overlays
//            if( Projection.isOk(v.pref.projd ) ) v.paintOverlays(pg,null,MARGE,y);

            // Positionnement des caches
            int W=pj.getPageDimension().width;
            int H=pj.getPageDimension().height;
            pg.setColor( Color.white );
            pg.fillRect(0,0,W,y);
            pg.fillRect(0,y+h,W,H-y+h);
            pg.fillRect(0,y,MARGE,h);
            pg.fillRect(MARGE+w,y,W,h);

            // affichage des titres et legendes
            pg.setColor(Color.black);
            printTitre(pg,x,MARGE,w);
            pg.drawRect(x,y,w,h);
            printLegende(pg,pj.getPageDimension().width-25,y+20,true);
            y+=h;
            y=printCopyright(pg,w,y,true);

            pg.dispose();
         }
         pj.end();
      }

      aladin.view.repaintAll();	      // Pour forcer le recalcul
   }

   /** Impression de la mosaïque des multivues courantes
    * ATTENTION:  IL Y A UN BUG CAR LES OBJETS QUI SONT A CHEVAL
    * SUR LE BORD D'UNE VUE VONT ETRE TRACE ET DONC APPARAITRE
    * DANS LA VUE ADJACENTE (EN HAUT OU A GAUCHE). JE NE VOIS PAS
    * COMMENT CORRIGER CELA.
    *
    */
   private void printMosaique() {
      int x=MARGE;
      int y=70;
      PrintJob pj = Toolkit.getDefaultToolkit().getPrintJob(aladin.f,"Aladin",null);

      if( pj!=null ) {
         Graphics pg = pj.getGraphics();
         if( pg!=null ) {

            int w = aladin.view.getWidth();
            
            // Affichage et calcul de la taille du titre
//            y=printTitre(pg,x,y,w);
            
            int W=pj.getPageDimension().width;
            
            int offsetY=0;
            int col = aladin.viewControl.getNbCol();

            for( int i=0; i<aladin.view.getModeView(); i++ ) {
               ViewSimple v = aladin.view.viewSimple[i];
               
               // Changement de ligne
               if( i>0 && i%col==0 ) {
                  x=MARGE;
                  y+=offsetY;
                  offsetY=0;
               }
               
               // Impression
               if( !v.isFree() ) {
                  try { pg.setClip(x,y,v.getWidth(),v.getHeight()); }
                  catch( Exception e ) {}
                  if( v.imgprep!=null ) v.paintOverlays(pg,null,x,y);
//                  if( v.imgprep!=null ) pg.drawImage(v.imgprep, x+v.dx, y+v.dy, aladin);
//                  if( Projection.isOk(v.pref.projd ) ) v.paintOverlays(pg,null,x,y);
               }
               try { pg.setClip(0,0,pj.getPageDimension().width,pj.getPageDimension().height); }
               catch( Exception e ) {}
              
               // Encadrement
               pg.setColor(Color.black);
               pg.drawRect(x,y,v.getWidth(),v.getHeight());
               
               // Changement de vue
               x+=v.getWidth();
               if( v.getHeight()>offsetY ) offsetY=v.getHeight();
            }

           y+=offsetY;
           printCopyright(pg,w,y,false);
           printLegende(pg,W-100,y,false);
           pg.dispose();
         }
         pj.end();
      }

      // Pour forcer le recalcul
      aladin.view.repaintAll();
   }

  /** Impression des copyrights */
   private int printCopyright(Graphics pg,int imgW,int y,boolean flagOrigin) {
      Font f  = new Font("TimesRoman",Font.PLAIN,7);
      FontMetrics  fm;

      pg.setColor( Color.black );
      pg.setFont(f);
      fm = pg.getFontMetrics();

      // Le soft
      int cx = MARGE;
      int cy = y+fm.getAscent()+2;
      pg.drawString("Produced by Aladin (Centre de Donnees astronomiques de Strasbourg)",cx,cy);
      cy+=fm.getHeight();
      pg.drawString("http://aladin.u-strasbg.fr",cx,cy);


      // La provenance
      Plan p = aladin.calque.getPlanRef();
      if( flagOrigin && p!=null && p.from!=null ) {
         cy = y+fm.getAscent()+2;
         cx = MARGE+imgW-fm.stringWidth(p.from);
         pg.drawString(p.from,cx,cy);
      }

      return cy+fm.getDescent()+fm.getLeading();
   }


   /** Impression du titre
   * @param Le contexte graphique d'impression
   * @param x,y le coin superieur DROIT
   */
   private int printTitre(Graphics pg,int x,int y,int wImg) {
      Font f  = new Font("TimesRoman",Font.PLAIN,20);
      Font f1 = new Font("TimesRoman",Font.PLAIN,14);
      FontMetrics  fm;
      String titre;
      int cx,cy;

      Plan p = aladin.calque.getPlanRef();
      if( p==null || !Projection.isOk(p.projd) ) return 30;

      if( p.objet==null ) titre=null;
      else {
         char ch = p.objet.charAt(0);
         titre  = (ch<'0' || ch>'9')?p.objet:null;
      }

      pg.setColor( Color.black );
      pg.setFont(f);
      fm = pg.getFontMetrics();
      cy = y+fm.getHeight();

      // Affichage eventuel du survey
      if( p.isImage() ) {
         pg.setFont(f1);
         fm = pg.getFontMetrics();
         cx = MARGE+wImg-fm.stringWidth(p.label)-10;
         pg.drawString(p.label,cx,cy);
         pg.setFont(f);
         fm = pg.getFontMetrics();
      }

      cx=x;
      if( titre!=null ) {
         pg.drawString(titre,cx,cy);
         cy+=fm.getAscent();
      }

      return cy+10;
   }

  /** Impression du cadre des legendes des plans
   * @param Le contexte graphique d'impression
   * @param x,y le coin superieur DROIT
   * @param cadre true si impression d'un cadre autour
   */
   private void printLegende(Graphics pg,int x,int y,boolean cadre) {
      Plan [] plan = aladin.calque.plan;
      int i;
      Font f = new Font("TimesRoman",Font.PLAIN,12);
      FontMetrics  fm;
      Vector v = new Vector(10);
      int w;
      int W=0;
      int H;
      int cx,cy,dy;
      Plan p;

      // Determination de la taille max des legendes
      pg.setFont(f);
      fm = pg.getFontMetrics();
      dy = fm.getHeight();
      for( i=0; i< plan.length; i++ ) {
         p = plan[i];
         if( !p.isCatalog() || !p.flagOk || p.error!=null ) continue;
         v.addElement(p);
         w = fm.stringWidth(p.getLabel());
         if( w>W ) W=w;
      }


      // Pas de legende
      if( v.size()==0 ) return;


      // Determination de la boite
      W += 28;
      H = v.size()*dy+fm.getDescent();


      // Affichage de la boite
      if( cadre ) {
         pg.setColor(Color.white);
         pg.fillRect(x - W,y,W,H);
         pg.setColor(Color.black);
         pg.drawRect(x - W,y,W,H);
      }


      // Affichage de chaque label de plan + logo d'un objet
      cy=y+dy;
      Enumeration e = v.elements();
      while( e.hasMoreElements() ) {
         p = (Plan) e.nextElement();
         if( !p.isCatalog() ) continue;
         Iterator<Obj> it = p.iterator();
         Source o=null;
         while( it.hasNext() ) {
            Obj o1 = it.next();
            if( !(o1 instanceof Source) ) continue;
            o = (Source)o;
            break;
         }
         cx=x-W+10;

         if( o!=null ) o.print(pg,cx,cy-dy/3);
         cx+=10;
         pg.setColor( Color.black );
         pg.drawString(p.getLabel(),cx,cy);

         cy+=dy;
      }
   }


}
