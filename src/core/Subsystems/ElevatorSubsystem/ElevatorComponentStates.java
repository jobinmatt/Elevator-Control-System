package core.Subsystems.ElevatorSubsystem;

public enum ElevatorComponentStates {
	
	//motor 
	ELEV_MOTOR_UP, 
	ELEV_MOTOR_DOWN, 
	ELEV_MOTOR_ERROR, 
	ELEV_MOTOR_IDLE,
	
	//doors 
	ELEV_DOORS_OPEN, 
	ELEV_DOORS_CLOSE, 
	ELEV_DOORS_IN_PROGRESS, //can be failed state too
	ELEV_DOORS_ERROR
	
}
