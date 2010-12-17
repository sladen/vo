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
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import cds.tools.Util;

public class FrameBookmarks extends JFrame {
   private Aladin aladin;
   private JToolBar toolBar;
   
   FrameBookmarks(Aladin aladin) {
      super();
      this.aladin=aladin;;
      toolBar = new JToolBar();
      Aladin.setIcon(this);
      setTitle(aladin.chaine.getString("BKMTITLE"));

      enableEvents(AWTEvent.WINDOW_EVENT_MASK);
      Util.setCloseShortcut(this, false, aladin);
   }
   
   /** Initialisation des bookmarks */
   protected void init(boolean noCache) {
      aladin.glu.createBookmarks(noCache);
      aladin.command.setFunctionModif(false);
      if( !aladin.NOGUI ) resumeToolBar();
   }
   
   // Ajout d'un bookmark
   private Function addBookmark(String name,String param,String description,String code) {
      Function f = new Function(name,param,code,description);
      f.setBookmark(true);
      aladin.command.addFunction(f);
      return f;
   }
   
   /** Mise à jour de la liste des bookmarks (les noms séparés par une simple virgule).
    * Si la liste commence par '+' ou '-', il s'agit d'une mise à jour */
   protected void setBookmarkList(String names) {
      int mode = 0;
      if( names.length()>1 ) {
         if( names.charAt(0)=='+' ) { mode=1; names=names.substring(1); }
         else if( names.charAt(0)=='-' ) { mode=-1; names=names.substring(1); }
      }
      if( mode==0 ) { aladin.command.resetBookmarks(); mode=1; }
      
      Tok tok = new Tok(names,",");
      while( tok.hasMoreTokens() ) {
         String name = tok.nextToken().trim();
         Function f = aladin.command.getFunction(name);
         if( f==null ) continue;
         f.setBookmark(mode==1);
      }
      
      resumeToolBar();
   }
   
   /** Fournit la toolbar des signets */
   protected JToolBar getToolBar() {
      toolBar.setRollover(true);
      toolBar.setFloatable(false);
      toolBar.setBorder(BorderFactory.createEmptyBorder());
      populateToolBar(toolBar);
      return toolBar;
   }
   
   // Ajoute les boutons qu'il faut dans la toolbar
   private void populateToolBar(JToolBar toolBar) {
      Enumeration e = aladin.command.getBookmarkFunctions().elements();
      while( e.hasMoreElements() ) {
         Function f = (Function)e.nextElement();
         Bookmark bkm = new Bookmark(aladin,f);
         toolBar.add(bkm);
      }
   }
   
   // Remet à jour la toolbar des signets suite à des modifs internes
   protected void resumeToolBar() {
      toolBar.removeAll();
      populateToolBar(toolBar);
      toolBar.validate();
      aladin.validate();
      aladin.repaint();
   }
   
   /** Retourne true si la liste courante des bookmarks correspond à la liste par défaut
    * définie par l'enregistrement GLU (voir glu.createBookmarks()) */
   protected boolean isDefaultList() {
      return memoDefaultList.equals(getBookmarkList());
   }
   
   private String memoDefaultList="";
   
   /** Mémorise la liste courante des bookmarks */
   protected void memoDefaultList(String s) { memoDefaultList = s; }
   
   /** Retourne la liste des bookmarks */
   protected String getBookmarkList() { return getBookmarkList(false); }
   protected String getBookmarkList(boolean onlyLocal) {
      StringBuffer bkm = new StringBuffer();
      Enumeration e = aladin.command.getBookmarkFunctions().elements();
      while( e.hasMoreElements() ) {
         Function f = (Function)e.nextElement();
         if( onlyLocal && !f.isLocalDefinition() ) continue;
         if( bkm.length()>0 ) bkm.append(',');
         bkm.append(f.getName());
      }
      return bkm.toString();
   }   
   
   /********************************* Gère la Frame ***********************************************/
   
   public void show() {
      if( genPanel==null ) {
         createPanel();
         pack();
      }
      super.show();
   }
   
   private JPanel genPanel=null;
   
   private void createPanel() {
      genPanel = (JPanel)getContentPane();
      genPanel.setLayout( new BorderLayout(5,5));
      genPanel.setBorder( BorderFactory.createEmptyBorder(10, 10, 10, 10));
      genPanel.add(getBookmarksPanel(),BorderLayout.NORTH);
      genPanel.add(getAmateurPanel(),BorderLayout.SOUTH);
      
      // Juste pour qu'il soit créé
      getExpertPanel();
   }
   
   
   private boolean expertMode=false;
   private JPanel expertPanel=null,amateurPanel=null;
   
   private JPanel getExpertPanel() {
      if( expertPanel!=null ) return expertPanel;
      JPanel p = expertPanel = new JPanel(new BorderLayout(5,5));
      p.add(getEditPanel(),BorderLayout.CENTER);
      p.add(getControlPanel(),BorderLayout.SOUTH);
      return p;
   }
   
   private JPanel getAmateurPanel() {
      if( amateurPanel!=null ) return amateurPanel;
     JPanel p = amateurPanel = new JPanel( );
      JButton b;
      
      p.add( b=new JButton(aladin.chaine.getString("BKMEDITOR")));
      b.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            genPanel.remove(amateurPanel);
            genPanel.add(getExpertPanel(),BorderLayout.CENTER);
            expertMode=true;
            pack();
         }
      });
      p.add( b=new JButton(aladin.chaine.getString("PROPCLOSE")));
      b.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) { dispose(); }
      });
      p.add( Util.getHelpButton(this, aladin.chaine.getString("BKMHELP")));
      return p;
   }
   
   private JTextArea edit;
   
   protected JPanel getEditPanel() {
      JPanel p = new JPanel( new BorderLayout());
      edit = new JTextArea(10,60);
      edit.addKeyListener(new KeyAdapter(){
         public void keyReleased(KeyEvent e) {
            apply.setEnabled(isModif());
         }
      });
      JScrollPane sc = new JScrollPane(edit);
      p.add(sc,BorderLayout.CENTER);
      return p;
   }
   
   private Function fctEdit=null;
   private void resumeEdit(Function f,boolean valid) {
      if( valid ) valide(true);
      fctEdit=f;      
      edit.setText(f==null ? "" : f.toString());
      apply.setEnabled(isModif());
   }
   
   private boolean isModif() {
      if( fctEdit==null ) return false;
      return !fctEdit.toString().trim().equals(edit.getText().trim());
   }
   
   private void valide(boolean flagTest) {
      if( !isModif() ) return;
      if( !flagTest || aladin.confirmation(this,aladin.chaine.getString("BKMAPPLY")) ) {
         try {
            Function t = new Function(edit.getText());
            fctEdit.setDescription(t.getDescription());
            fctEdit.setName(t.getName());
            fctEdit.setParam(t.getParam());
            fctEdit.setCode(t.getCode());
            fctEdit.setLocalDefinition(true);
            edit.setText(fctEdit.toString());
            resumeTable();
            resumeToolBar();
            aladin.log("Bookmark","create");
         } catch( Exception e ) {
            e.printStackTrace();
         }
      }
   }
   
   private JButton apply,delete;

   protected JPanel getControlPanel() {
      JPanel p = new JPanel( );
      JButton b;
      
      p.add( b=new JButton(aladin.chaine.getString("PROPNEWCALIB")));
      b.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) { createNewBookmark(); }
      });
      p.add( b=delete=new JButton(aladin.chaine.getString("SLMDEL")));
      b.setEnabled(false);
      b.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) { deleteBookmark(); }
      });
      p.add( b=new JButton(aladin.chaine.getString("BKMDEFAULT")));
      b.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) { resetBookmarks(); }
      });
      p.add(new JLabel(" - "));
      p.add( b=apply=new JButton(aladin.chaine.getString("PROPAPPLY")));
      b.setEnabled(false);
      b.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) { valide(false); }
      });
      p.add( b=new JButton(aladin.chaine.getString("PROPCLOSE")));
      b.addActionListener(new ActionListener() {
         public void actionPerformed(ActionEvent e) {
            genPanel.remove(expertPanel);
            genPanel.add(getAmateurPanel(),BorderLayout.SOUTH);
            expertMode=false;
            pack();
         }
      });
      return p;
   }
   
   private void createNewBookmark() {
      Function f = addBookmark("YourName","","Your description","");
      aladin.command.addFunction(f);
      resumeEdit(f,true);
      resumeTable();
      int row = table.getRowCount()-1;
      table.scrollRectToVisible(table.getCellRect(row,NAME,true));
      table.setRowSelectionInterval(row,row);
   }
   
   private void deleteBookmark() {
       int row = table.getSelectedRow();
       Function f = aladin.command.getFunction(row); 
       aladin.command.removeFunction(f);
       resumeTable();
       resumeToolBar();
       resumeEdit(null,false);
   }
   
   private void resetBookmarks() {
      if( !aladin.confirmation(this,aladin.chaine.getString("BKMCONFIRM")) ) return;
      aladin.configuration.resetBookmarks();
      init(true);
      resumeTable();
      resumeToolBar();
      resumeEdit(null,false);
   }
   
   public void dispose() {
      if( expertMode ) {
         genPanel.remove(expertPanel);
         genPanel.add(getAmateurPanel(),BorderLayout.SOUTH);
         pack();
         expertMode=false;
      }
      setVisible(false);
   }
   
   /********************************** Gère la table des bookmarks *********************************/
   
   protected JPanel getBookmarksPanel() {
      JPanel p = new JPanel( new BorderLayout());
      JScrollPane sc = new JScrollPane(createBookmarksTable());
      p.add(sc,BorderLayout.CENTER);
      return p;
   }
   
   private void resumeTable() { tableModel.fireTableDataChanged(); }

   private JTable table;
   private BookmarkTable tableModel;
   
   private JTable createBookmarksTable() {
      table=new JTable(tableModel=new BookmarkTable());
      table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
      table.getColumnModel().getColumn(BKM).setMinWidth(60);
      table.getColumnModel().getColumn(BKM).setMaxWidth(60);
      table.getColumnModel().getColumn(NAME).setMinWidth(70);
      table.getColumnModel().getColumn(NAME).setMaxWidth(70);
      table.setPreferredScrollableViewportSize(new Dimension(320,16*12));

      table.addMouseListener(new MouseAdapter() {
         public void mouseReleased(MouseEvent e) {
            int row = table.rowAtPoint(e.getPoint());
            Function f = aladin.command.getFunction(row);
            if( delete!=null ) delete.setEnabled(true);
            resumeEdit(f,true);
         }
      });

      return table;
   }

   static final int BKM   = 0;
   static final int NAME  = 1;
   static final int DESC  = 2;
   
   class BookmarkTable extends AbstractTableModel {
      
      public int getColumnCount() { return 3; }
      public int getRowCount() { return aladin.command.getNbFunctions(); }
      
      public String getColumnName(int col) { 
         return col==BKM  ? "Bookmark" :
                col==NAME ? "Name" : 
                            "Description" ;
      }
      
      public Class getColumnClass(int col) {
         if( col==BKM ) return (new Boolean(true)).getClass();
         return super.getColumnClass(col);
      }

      public Object getValueAt(int row, int col) {
         Function f = aladin.command.getFunction(row);
         switch( col ) {
            case BKM  : return new Boolean(f.isBookmark());
            case NAME : return f.getName(); 
            default : return f.getDescription();
         }
      }
      
      public boolean isCellEditable(int row, int col) { return true; }
      public void setValueAt(Object value,int row, int col) {
         Function f = aladin.command.getFunction(row);
         if( col==BKM ) { f.setBookmark(!f.isBookmark()); resumeToolBar(); }
         else if( col==NAME || col==DESC ) {
            if( col==NAME ) f.setName((String)value);
            else f.setDescription((String)value);
            f.setLocalDefinition(true);
            resumeEdit(f,false);
            resumeToolBar();
         }
      }
   }
}
