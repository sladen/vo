package cds.allsky;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.FileDialog;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;

import cds.aladin.Aladin;
import cds.aladin.Chaine;
import cds.aladin.Plan;
import cds.aladin.PlanBG;

public class RGBPanel extends JPanel implements ActionListener {

	private static final String B = "B";
	private static final String G = "G";
	private static final String R = "R";
	
	private static final String OK = "Create RGB";
	
	private static final String txt = "Choose 2 or 3 opened Allsky view to combine and create a colored one.";

	private String REP_DEST;
	private String BROWSE;

	private JLabel		dirLabel = new JLabel();
	private JButton 	browse = new JButton();
	private JTextField 	dir = new JTextField(30);
	
	JProgressBar progressBar = new JProgressBar(0,100);

	private JComboBox[] ch;
	private JButton bOk = new JButton(OK);
	private final Aladin aladin;

	public RGBPanel(Aladin aladin) {
		super(new GridBagLayout());
		this.aladin = aladin;
		createChaine(aladin.getChaine());
		
		GridBagConstraints c = new GridBagConstraints();
//		GridBagLayout g=new GridBagLayout();
//		c.gridx=0;c.gridy=0;
		c.fill=GridBagConstraints.NONE;
		c.insets = new Insets(2,2,2,2);

//		JPanel p=new JPanel();
		setBorder(BorderFactory.createEmptyBorder(5, 55, 5,55));
//		p.setLayout(g);
		
		// Création des lignes pour choisir les plans
		int n=3;
		ch=new JComboBox[n];
		for (int i=0; i<n; i++) {
			ch[i]=new JComboBox();

			JLabel ll=new JLabel(getLabelSelector(i));
			ll.setForeground(getColorLabel(i));

			c.gridwidth=GridBagConstraints.RELATIVE;
			c.weightx=0.0;
			add(ll,c);
			c.gridwidth=GridBagConstraints.REMAINDER;
//			c.weightx=10.0;
			ch[i].setMinimumSize(getPreferredSize());
			add(ch[i],c);
			
			ch[i].addFocusListener(new FocusAdapter() {
				@Override
				public void focusGained(FocusEvent e) {
					init();
				}
			});
		}
				
		init();
		c.gridwidth=GridBagConstraints.RELATIVE;
		c.weightx=0;
		
		// Sélection du répertoire destination
		dirLabel = new JLabel(REP_DEST);
		add(dirLabel,c);
		add(dir,c);
   		browse.setText(BROWSE);
		browse.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) { dirBrowser(dir); }
		});
		c.gridwidth=GridBagConstraints.REMAINDER;
		add(browse,c);
		
//		c.gridx=GridBagConstraints.RELATIVE; c.gridy=GridBagConstraints.RELATIVE;
		c.gridwidth=GridBagConstraints.REMAINDER;
		c.fill=GridBagConstraints.HORIZONTAL;
		
		// bouton OK
		bOk.setEnabled(false);
		bOk.addActionListener(this);
		add(bOk,c);
		
		// barre de progression
		progressBar.setStringPainted(true);
		add(progressBar,c);

//		add(p);
		
	}
	protected String getLabelSelector(int i) {
		return i == 0?R:i == 1?G:i==2?B:"";
	}

	protected Color getColorLabel(int i) {
		return i == 0?Color.red:i == 1?Color.green:i==2?Color.blue:Color.black;
	}
	

	private void createChaine(Chaine chaine) {
		BROWSE = chaine.getString("FILEBROWSE");
		REP_DEST = chaine.getString("REPDALLSKY");
	}
	
	/** Recupere la liste des plans Allsky valides */
	protected PlanBG[] getPlan() {
		try {
         Vector<Plan> v = aladin.calque.getPlanBG();
         if( v==null ) return new PlanBG[0];
         // enlève les plans déjà couleur
         for (Iterator<Plan> iterator = v.iterator(); iterator.hasNext();) {
         	PlanBG plan = (PlanBG) iterator.next();
         	
         	if (plan.isColored())
         		v.remove(plan);
         }
         PlanBG pi [] = new PlanBG[v.size()];
         v.copyInto(pi);
         return pi;
      } catch( Exception e ) {
        return new PlanBG[]{};
      }
	}

	public void setProgress(int value) {
		progressBar.setValue(value);
	}
		
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand() == OK) {
			// récupère les 2 ou 3 plans sélectionnés
			Object[] plans = new Object[3];
			for (int i=0; i<3; i++) {
				plans[i] = ch[i].getSelectedItem();
			}
			
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			bOk.setEnabled(false);
			RGBBuild thread = new RGBBuild(aladin,plans,dir.getText());
			thread.start();
			(new ThreadProgressBar(thread)).start();
		}
	

		
	}
	

	/** Ouverture de la fenêtre de sélection d'un fichier */
	private void dirBrowser(JTextField dir) {
		FileDialog fd = new FileDialog(aladin.frameAllsky,"Running directory selection");
		if( dir!=null && !dir.getText().equals("")) fd.setDirectory(dir.getText());
		else fd.setDirectory(aladin.getDefaultDirectory());

		// (thomas) astuce pour permettre la selection d'un repertoire
		// (c'est pas l'ideal, mais je n'ai pas trouve de moyen plus propre en AWT)
		fd.setFile("-");
		fd.setVisible(true);
		String selectdir = fd.getDirectory();
		String name =  fd.getFile();
		// Si annulé
		if (name == null)
			return;
		// si on n'a pas changé le nom, on a selectionne un repertoire
		if( name!=null && name.equals("-") ) name = "";
		String t = (selectdir==null?"":selectdir)+name;
		dir.setText(t);
		actionPerformed(new ActionEvent(dir,-1, "dirBrowser Action"));
	}

	void init() {
		// sauvegarde les anciennes selections
		Object[] save = new Object[]{ch[0].getSelectedItem(),
		ch[1].getSelectedItem(),
		ch[2].getSelectedItem()};
		// rachaichit les combo box avec la liste des plans allsky
		PlanBG[] plans = getPlan();
		for (int i=0; i<3; i++) {
			ch[i].removeAllItems();
			ch[i].addItem(null);
		}
		for (PlanBG planBG : plans) {
			for (int i=0; i<3; i++) {
				ch[i].addItem(planBG);
				// remet l'ancienne selection
				if (save[i]!=null && planBG==save[i])
					ch[i].setSelectedItem(planBG);
					
			}
		}
		if (save[0]!=null || save[1]!=null || save[2]!=null)
			bOk.setEnabled(true);
	}
	
	class ThreadProgressBar implements Runnable {
		Object thread;
		public ThreadProgressBar(Object source) {
			thread = source;
		}

		public synchronized void start(){
			// lance en arrière plan le travail
			(new Thread(this)).start();
		}
		public void run() {
			int value = 0;
			while(thread != null && value < 99) {
				value = (int)((RGBBuild)thread).getProgress();
				setProgress(value);
				try {
					Thread.sleep(200);
				} catch (InterruptedException e) {
				}
			}
			setProgress(100);
			bOk.setEnabled(false);
			setCursor(null);
			
		}
	}
}