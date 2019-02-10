package core.Utils;

import core.Exceptions.HostActionsException;
import core.Exceptions.PortParserException;
import core.Subsystems.ElevatorSubsystem.ElevatorCarThread;
import core.Subsystems.FloorSubsystem.FloorThread;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class PortParser {


    private static final int DATA_SIZE = 1024;
    private static final byte SPACER = (byte) 0;
    private static Logger logger = LogManager.getLogger(PortParser.class);
    /**
     * Sends a packet to the Scheduler with the port information of each elevator
     * @param initPort
     * @throws HostActionsException
     * @throws IOException
     */
    public static void sendPortsToScheduler(int initPort, Map map, SubsystemConstants systemType) throws HostActionsException, IOException {
        byte[] packetData = createPortsArray(map, systemType);
        DatagramPacket packet = new DatagramPacket(packetData, packetData.length, InetAddress.getLocalHost(), initPort);
        HostActions.send(packet, Optional.empty());
    }

    /**
     * Creates a data array with the port information
     * @param map
     * @return
     * @throws IOException
     */
    public static byte[] createPortsArray(Map map1, SubsystemConstants systemType) throws IOException {
        ByteArrayOutputStream data = new ByteArrayOutputStream();

        if(systemType.equals(SubsystemConstants.FLOOR)) {
            HashMap<String, FloorThread> map = (HashMap<String, FloorThread>)map1;
            for (Map.Entry<String, FloorThread> entry : map.entrySet()) {
                System.out.println(entry.getKey() + ":" + entry.getValue());
                int floorNumber = entry.getValue().getFloorNumber();
                int floorPort = entry.getValue().getPort();
                data.write(floorNumber);
                data.write(SPACER);
                try {
                    data.write(ByteBuffer.allocate(4).putInt(floorPort).array());
                } catch (IOException e) {
                    throw new IOException("" + e);
                }
                data.write(SPACER);
                data.write(SPACER);
            }
        }else{
            HashMap<String, ElevatorCarThread> map = (HashMap<String, ElevatorCarThread>)map1;
            for (Map.Entry<String, ElevatorCarThread> entry : map.entrySet()) {
                System.out.println(entry.getKey() + ":" + entry.getValue());
                int elevNumber = entry.getValue().getElevatorNumber();
                int elevPort = entry.getValue().getPort();
                data.write(elevNumber);
                data.write(SPACER);
                try {
                    data.write(ByteBuffer.allocate(4).putInt(elevPort).array());
                } catch (IOException e) {
                    throw new IOException("" + e);
                }
                data.write(SPACER);
                data.write(SPACER);
            }
        }

        data.write(SPACER);
        data.write(SPACER);
        return data.toByteArray();
    }

    public static HashMap<Integer, Integer> convertPacketToMap(byte[] data, int length) throws PortParserException {
        byte SPACER = (byte) 0;
        if(data != null && data[0] != SPACER) {

            HashMap<Integer, Integer> tempPorts = new HashMap<>();
            for(int i = 0; i < length; i = i + 8) {
                int pipelineNumber = data[i];

                byte[] portNumInByte = {data[i+2], data[i+3], data[i+4], data[i+5]};
                int schedulerPort = ByteBuffer.wrap(portNumInByte).getInt();
                tempPorts.put(pipelineNumber, schedulerPort);
                if(data.length<(i+8) || data[i+8] == SPACER) {
                    break;
                }
            }
            return tempPorts;
        }
        else throw new PortParserException("Cannot convert null to ports map or invalid data found");
    }

    public static HashMap<Integer, Integer> receivePortsFromScheduler(int listenPort) throws PortParserException {
        try {
            DatagramPacket packet = new DatagramPacket(new byte[DATA_SIZE], DATA_SIZE);
            DatagramSocket receiveSocket = new DatagramSocket(listenPort);
            try {
                logger.info("Waiting to receive port information from SCHEDULER...");
                HostActions.receive(packet, receiveSocket);
                receiveSocket.close();
                return convertPacketToMap(packet.getData(), packet.getLength());
            } catch (HostActionsException e) {
                throw new PortParserException("Unable to receive scheduler ports packet", e);
            }
        } catch (SocketException e) {
            throw new PortParserException("Unable to create a DatagramSocket", e);
        }
    }

}
