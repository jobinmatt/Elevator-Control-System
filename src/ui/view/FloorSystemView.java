package ui.view;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.Border;

import core.Direction;
import core.Subsystems.FloorSubsystem.FloorStatus;
import core.Subsystems.FloorSubsystem.FloorSubsystem;
import core.Subsystems.FloorSubsystem.FloorThread;

import ui.core.ButtonImagePanel;
import ui.core.FaultDescPanel;
public class FloorSystemView extends JFrame implements Runnable{

	private final int FRAME_TO_SCREEN_RATIO = 2; 
	private int frame_width;
	private int frame_height;
	private List<JPanel>mainCol;
	private List<JLabel> labelCol;
	private List<ButtonImagePanel> pnlButtonPanel;
	private List<FaultDescPanel> pnlFault;
	private FloorThread ref;
	private int elevNum;
	public FloorSystemView(int elevNum) {
		this.elevNum = elevNum;
		this.setLayout(new GridLayout(1, elevNum));
		mainCol = new ArrayList<JPanel>();
		labelCol = new ArrayList<JLabel>();
		pnlFault = new ArrayList<FaultDescPanel>();
		pnlButtonPanel = new ArrayList<ButtonImagePanel>();
		for (int i=0;i<elevNum;i++) {
			pnlButtonPanel.add( new ButtonImagePanel());
			pnlFault.add(new FaultDescPanel(0,0,0));
		}
		for (int i=0;i<elevNum;i++) {
			labelCol.add(new JLabel("1",SwingConstants.CENTER));
			labelCol.get(i).setBackground(Color.BLACK);
			labelCol.get(i).setFont(new Font("Arial", Font.PLAIN, 44));
		}
		
		for (int i=0;i<elevNum;i++) {
			mainCol.add(new JPanel());
			mainCol.get(i).setLayout(new BorderLayout());
			mainCol.get(i).setBackground(new Color(255,255,255));
			mainCol.get(i).add(labelCol.get(i), BorderLayout.CENTER);
			mainCol.get(i).add(pnlButtonPanel.get(i), BorderLayout.NORTH);
			mainCol.get(i).add(pnlFault.get(i), BorderLayout.SOUTH);
			mainCol.get(i).setBorder(BorderFactory.createLineBorder(Color.black));
		}
		
		for (JPanel jpnl : mainCol) {
			this.add(jpnl);
		}
		
		this.setTitle("Elevator Status");
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame_width = (int) (screenSize.getWidth()/FRAME_TO_SCREEN_RATIO);
		frame_height = (int) (screenSize.getHeight()/FRAME_TO_SCREEN_RATIO);		
		this.setPreferredSize(new Dimension(frame_width, frame_height));
		this.pack();//pack needs to be after setting all preferred sizes 
		this.setLocationRelativeTo(null);
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
				labelCol.get(i).setText(Integer.toString(states[i].getFloorStatus()));
				
				pnlButtonPanel.get(i).setDirection(states.clone()[i].getDir());
				pnlFault.get(i).setStatus(states.clone()[i].getErrorCode(), states.clone()[i].getErrorFloor(), states.clone()[i].getFloorStatus());
				this.repaint();
			}
		}
		
	}
}
