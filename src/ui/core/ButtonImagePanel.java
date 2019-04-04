package ui.core;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import core.Direction;

public class ButtonImagePanel extends JPanel{
	
	   private BufferedImage imageUp, imageDown;
	   private JLabel lblUp, lblDown, lblError; 
	   private Direction dir;
	    public ButtonImagePanel() {
	       try {
	    	  
	    	   	imageUp = ImageIO.read(new File("assets/arrow-up.png"));
	    		imageDown = ImageIO.read(new File("assets/arrow-down.png"));
	    	  	lblUp= new JLabel(new ImageIcon(imageUp));
	    	  	lblDown = new JLabel (new ImageIcon(imageDown));
	    	  	lblUp.setVisible(false);
	    	  	lblDown.setVisible(false);
	    	  	setBackground(new Color(255,255,255));
	    	  	add(lblUp, BorderLayout.WEST);
	    	  	add(lblDown, BorderLayout.EAST);
	       } catch (IOException ex) {
	    	   lblError = new JLabel("IMAGE NOT FOUND");
	           add(lblError);
	       }
	    }

	    public void setDirection(core.Direction dir) {
	    	if (dir == Direction.DOWN) {
	    		lblUp.setVisible(false);
	    		lblDown.setVisible(true);
	    	}
	    	else if (dir == Direction.UP) {
	    		lblUp.setVisible(true);
	    		lblDown.setVisible(false);
	    	}
	    	else {
	    		lblUp.setVisible(false);
	    		lblDown.setVisible(false);
	    	}
	    }
}
