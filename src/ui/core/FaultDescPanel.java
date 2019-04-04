package ui.core;

import java.awt.Color;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class FaultDescPanel extends JPanel{
	
	private JLabel lblErrorFloor;
	
	public FaultDescPanel(int errorCode, int errorFloor, int currFloor){
		setStatus(errorCode, errorFloor, currFloor);
					
	}
	public void setStatus(int errorCode, int errorFloor, int currFloor) {
		if (errorCode == 0) {
			lblErrorFloor = new JLabel("\n\n \n\n");
			setBackground(Color.GREEN);
		}
		else if (errorCode == 1 && errorFloor == currFloor) {
			lblErrorFloor = new JLabel("\n\n"+errorFloor+"\n\n");
			setBackground(Color.RED);
		}
		else if (errorCode == 2) {
			lblErrorFloor = new JLabel("\n\n"+errorFloor+"\n\n");
			setBackground(Color.YELLOW);
		}
	}
}
