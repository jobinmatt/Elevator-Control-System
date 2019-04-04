package ui.core;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class FaultDescPanel extends JPanel{
	
	private JLabel lblErrorFloor;
	
	public FaultDescPanel(int errorCode, int errorFloor){
		setStatus(errorCode, errorFloor);
					
	}
	public void setStatus(int errorCode, int errorFloor) {
		if (errorCode == 0) {
			lblErrorFloor = new JLabel("\n\n \n\n");
			setBackground(Color.GREEN);
		}
		else if (errorCode == 1) {
			lblErrorFloor = new JLabel("\n\n"+errorFloor+"\n\n");
			setBackground(Color.YELLOW);
		}
		else if (errorCode == 2) {
			lblErrorFloor = new JLabel("\n\n"+errorFloor+"\n\n");
			setBackground(Color.RED);
		}
	}
}
