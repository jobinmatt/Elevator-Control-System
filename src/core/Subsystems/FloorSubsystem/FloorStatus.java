package core.Subsystems.FloorSubsystem;

public class FloorStatus{
		int elevatorNum;
		int floorStatus;
		core.Direction dir;
		int errorCode, errorFloor;
		public FloorStatus(int eleNum, int floorStatus, core.Direction dir, int errorCode, int errorFloor) {
			this.elevatorNum = eleNum;
			this.floorStatus = floorStatus;
			this.dir = dir;
			this.errorCode = errorCode;
			this.errorFloor = errorFloor;
		}
		public int getErrorCode() {
			return errorCode;
		}
		public void setErrorCode(int errorCode) {
			this.errorCode = errorCode;
		}
		public int getErrorFloor() {
			return errorFloor;
		}
		public void setErrorFloor(int errorFloor) {
			this.errorFloor = errorFloor;
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
			return "Elev_" + elevatorNum + ": Dir:" + dir + " Floor:" + floorStatus;
		}
		
}