package ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import core.Subsystems.FloorSubsystem.FloorStatus;
import core.Subsystems.FloorSubsystem.FloorSubsystem;
import core.Subsystems.FloorSubsystem.FloorThread;
public class FloorSystemView extends JFrame implements Runnable{

	private List<JPanel>mainCol;
	private List<JLabel> labelCol;
	private FloorThread ref;
	private int elevNum;
	public FloorSystemView(int elevNum) {
		this.elevNum = elevNum;
		this.setLayout(new GridLayout(1, elevNum));
		mainCol = new ArrayList<JPanel>();
		labelCol = new ArrayList<JLabel>();
		for (int i=0;i<elevNum;i++) {
			labelCol.add(new JLabel("1",SwingConstants.CENTER));
			labelCol.get(i).setBackground(Color.BLACK);
			labelCol.get(i).setBorder(BorderFactory.createLineBorder(Color.black));
			labelCol.get(i).setFont(new Font("Arial", Font.PLAIN, 44));
		}
		
		for (int i=0;i<elevNum;i++) {
			mainCol.add(new JPanel());
			mainCol.get(i).setLayout(new BorderLayout());
			mainCol.get(i).setBackground(new Color(255,255,255));
			mainCol.get(i).add(labelCol.get(i), BorderLayout.CENTER);
		}
		
		for (JPanel jpnl : mainCol) {
			this.add(jpnl);
		}
		
		
		this.setTitle("Elevator Status");
		this.pack();
		this.setVisible(true);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

	}
	
	public FloorSystemView(int elevNum, FloorSubsystem mySub) {
		this(elevNum);
		this.ref = mySub.getFirstFloor();
	}
	

	@Override
	public void run() {
		while(true) {
			FloorStatus[] states = this.ref.getFloorStatus();
			for (int i=0;i<this.elevNum;i++) {
				labelCol.get(i).setText(states[i].getFloorStatus() + states[i].getDir().name());
				this.repaint();
			}
		}
		
	}
}
