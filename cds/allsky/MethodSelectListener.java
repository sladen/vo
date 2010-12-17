package cds.allsky;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JRadioButton;

public class MethodSelectListener implements ActionListener {
	private JRadioButton samplFast = null;
	private JRadioButton overlayFast = null;
	private JRadioButton samplBest = null;
	private JRadioButton overlayBest = null;

	public MethodSelectListener(JRadioButton[] buttons) {
		super();
		samplFast=buttons[0];
		overlayFast = buttons[1];
		samplBest=buttons[2];
		overlayBest = buttons[3];
	}

	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();
		
		if (source == samplFast) {
			if (!overlayFast.isSelected())
				overlayFast.doClick();
		}
		if (source == overlayFast) {
			if (!samplFast.isSelected())
				samplFast.doClick();
		}
		if (source == samplBest) {
			if (!overlayBest.isSelected())
				overlayBest.doClick();
		}
		if (source == overlayBest) {
			if (!samplBest.isSelected())
				samplBest.doClick();
		}
		
		
	}

}
