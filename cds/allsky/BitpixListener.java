package cds.allsky;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBox;

public class BitpixListener implements ActionListener {

	int defaultBitpix = TableNside.DEFAULT_BITPIX;
	
	public BitpixListener(JCheckBox keepCheckBox) {
		check = keepCheckBox;
	}
	JCheckBox check = null;
	public void setDefault(int bitpix) {
		defaultBitpix = bitpix;
	}
	
	public void actionPerformed(ActionEvent arg0) {
		int i = Integer.parseInt(arg0.getActionCommand());
		if (i == defaultBitpix)
			setKeepOn();
		else
			setKeepOff();
			
	}
	private void setKeepOn() {
		check.setEnabled(true);
		check.setSelected(true);
	}
	private void setKeepOff() {
		check.setSelected(false);
		check.setEnabled(false);
	}

}
