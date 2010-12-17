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

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

import javax.swing.*;
import javax.swing.event.CellEditorListener;
import javax.swing.tree.*;

/**
 * Le formulaire d'interrogation par cat�gorie sous la forme d'un arbre
 * D�velopements dans le cadre du projet WFP5 AIDA
 * 
 * M�thode : t�l�charge � l'ouverture du formulaire les d�finitions GLU qui d�vrivent
 * l'arbre des cat�gories (voir loadRemoteTree())
 *
 * @author Pierre Fernique [CDS]
 * @version 1.0 : aout 2009
 */
public class ServerCategory extends ServerTree  {
   	
  /** Initialisation des variables propres */
   protected void init() {
      type    = APPLI;
      aladinLabel     = "watch";
      aladinLogo    = "Watch.gif";
   }
   
   protected void createChaine() {
      super.createChaine();
      title = aladin.chaine.getString("TREETITLE");
      info = aladin.chaine.getString("TREEINFO1");
      info1 = aladin.chaine.getString("TREEINFO2");
   }

 /** Creation du formulaire d'interrogation par arbre. */
   protected ServerCategory(Aladin aladin) { super(aladin); }
   
   protected DefaultMutableTreeNode getRoot() {
      if( root==null ) root = new DefaultMutableTreeNode( new TreeNodeCategory(aladin,"WP5:root","","") );
      return root;
   }
   
   private boolean dynTree=false;
   protected void initTree() {
      if( dynTree ) return;
      (new Thread("initTree") {
         public void run() {
            loadRemoteTree();
            populateTree(aladin.glu.vGluCategory.elements());
         }
      }).start();
   }
   
   /** Chargement des descriptions de l'arbre */
   protected void loadRemoteTree() {
      if( dynTree ) return;
      try {
         dynTree=true;
         Aladin.trace(3,"Loading Tree definitions...");
         DataInputStream dis = new DataInputStream(aladin.cache.getWithBackup(Aladin.TREEURL));
         aladin.glu.loadGluDic(dis,0,false,false);
      } catch( Exception e1 ) { if( Aladin.levelTrace>=3 ) e1.printStackTrace(); }
   }
}
