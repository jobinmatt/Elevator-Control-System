package Utils;

import java.util.Date;

public class SimulationParameters {
	
	private Date startTime;
	private int floor;
	private boolean floorButton;
	private int carButton;
	
	public SimulationParameters(Date startTime, int floor, boolean floorButton, int carButton) {
		super();
		this.startTime = startTime;
		this.floor = floor;
		this.floorButton = floorButton;
		this.carButton = carButton;
	}
	
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}
	public int getFloor() {
		return floor;
	}
	public void setFloor(int floor) {
		this.floor = floor;
	}
	public boolean getFloorButton() {
		return floorButton;
	}
	public void setFloorButton(boolean floorButton) {
		this.floorButton = floorButton;
	}
	public int getCarButton() {
		return carButton;
	}
	public void setCarButton(int carButton) {
		this.carButton = carButton;
	}
	
	

}
