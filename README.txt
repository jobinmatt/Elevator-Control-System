
******************* README *******************
List of Team Members and Responsibilities:
Brij Patel - Worked on InputParser.java and Scheduler Algorithm and Communication between Subsystems
			 Worked on scheduler refactor
Dharina Hanumunthadu - Worked on Floor Subsystem, HostActions.java and Floor Packet.java
                       Worked on making systems run on single computer
Jobin Mathew - Worked on StateDiagram, Unit test, README and setup Scheduler Subsystem
               Worked on making systems run on single computer
Rajat Bansal - Worked on Floor Subsystem, ConfigurationParser.java
			   Worked on scheduler refactor
Shounak Amladi - Worked On Elevator Subsystem, ElevatorPacket.java
                 Refactored entire packet system
                 
**********************************************
List of Files:

Elevator-Control-System/
	Configuration.xml
	README.txt
	
Elevator-Control-System/Diagrams/
	Elevator Subsystem State Machine Diagram.jpg
	Scheduler Subsystem State Machine Diagram.jpg
	ElevatorSubsystemUML.jpg
	ExceptionsUML.jpg
	FloorSubsystemUML.jpg
	SchedulerSubsystemUML.jpg
	
Elevator-Control-System/UML/
	CorePackageUtils.ucls
	ElevatorSubsystemUML.ucls
	ExceptionsUML.ucls
	FloorSubsystemUML.ucls
	HostActionsUML.ucls
	SchedulerSubsystemUML.ucls
	UtilsUML.ucls
	
Elevator-Control-System/src/core/
	ConfigurationParser.java
	Direction.java
	InputParser.java
	LoggingManager.java
	
Elevator-Control-System/src/core/Exceptions/
	CommunicationException.java
	ConfigurationParserException.java
	ElevatorSubystemException.java
	FloorSubsystemException.java
	GeneralException.java
	HostActionsException.java
	InputParserException.java
	SchedulerPipelineException.java
	SchedulerSubsystemException.java

Elevator-Control-System/src/core/Messages/
	ElevatorMessage.java
	ElevatorSysMessageFactory.java
	FloorMessage.java
	InitMessage.java
	SubsystemMessage.java
	
Elevator-Control-System/src/core/Subsystems/ElevatorSubsystem/
	ElevatorCarThread.java
	ElevatorComponentConstants.java
	ElevatorComponentStates.java
	ElevatorSubsystem.java
	ElevatorSubsystemMain.java
	
Elevator-Control-System/src/core/Subsystems/FloorSubsystem/
	FloorSubsystem.java
	FloorSubsystemMain.java
	FloorThread.java
	Shaft.java
	
Elevator-Control-System/src/core/Subsystems/SchedulerSubsystem/
	Elevator.java
	ElevatorPipeline.java
	FloorPipeline.java
	SchedulerPipeline.java
	SchedulerRequest.java
	SchedulerSubsystem.java
	SchedulerSubsystemMain.java
	
Elevator-Control-System/src/core/Tests/
	InputParserTest.java
	
Elevator-Control-System/src/core/Utils/
	HostActions.java
	SimulationRequest.java
	SubsystemConstants.java
	Utils.java
	
Elevator-Control-System/src/test/core/
	ElevatorCarStateTest.java
	PacketDataTest.java
	
**********************************************
Setup Instructions:
1) Extract project zip file
2) Open project folder in Eclipse
	File->Open Projects from File System...
	Click Directory and navigate to the extracted zip location and select root folder.
	Click Finish
3) Wait for the Project to build
4) On two more computers, follow steps 1 to 3
5) System 1 will be used to run the SchedulerSubsystemMain.java
6) System 2 will be used to run the ElevatorSubsystemMain.java
7) System 3 will be used to run the FloorSubsystemMain.java
8) Modify the Configuration.xml file on all systems with the appropriate IP Addresses
	Modify SchedulerAddress, ElevatorAddress and FloorAddress values
	Note:Follow steps below to find a computers IP Address
	Note:Same computer can be used for all 
9) Hit run on all three system in the order System 1, System 2 and then System 3
	Note:They can be run on same system, follow order of Scheduler, Elevator and then Floor
	
**********************************************
How to find your computers IP address on windows?
1) Open command prompt on your computer
	WindowsButton+R -> type cmd.exe and hit run
2) Type ipconfig and hit enter
3) Identify your network adapter from the list and copy its IPv4 Address
**********************************************		
