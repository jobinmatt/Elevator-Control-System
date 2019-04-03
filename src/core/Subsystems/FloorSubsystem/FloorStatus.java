package core.Subsystems.FloorSubsystem;

public class FloorStatus{
		int elevatorNum;
		int floorStatus;
		core.Direction dir;
		public FloorStatus(int eleNum, int floorStatus, core.Direction dir) {
			this.elevatorNum = eleNum;
			this.floorStatus = floorStatus;
			this.dir = dir;
		}
		public int getElevatorNum() {
			return elevatorNum;
		}
		public void setElevatorNum(int elevatorNum) {
			this.elevatorNum = elevatorNum;
		}
		public int getFloorStatus() {
			return floorStatus;
		}
		public void setFloorStatus(int floorStatus) {
			this.floorStatus = floorStatus;
		}
		public core.Direction getDir() {
			return dir;
		}
		public void setDir(core.Direction dir) {
			this.dir = dir;
		}
		
		@Override
		public String toString() {
			return "Elevr Num: "+ elevatorNum+ " Dir:"+ dir;
		}
		
}