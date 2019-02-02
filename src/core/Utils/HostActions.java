//****************************************************************************
//
// Filename: HostActions.java
//
// Description: Main functionalities for sending and receiving datagram packets.
//
// @author Dharina H.
//***************************************************************************

//package core.HostActions;
package core.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import core.Exceptions.HostActionsException;

public class HostActions {
	private static Logger logger = LogManager.getLogger(HostActions.class);
	/**
	 * @param packet
	 * @param socket
	 * @throws HostActionsException
	 */
	public synchronized static void send(DatagramPacket packet, Optional<DatagramSocket> socket)
			throws HostActionsException {
		DatagramSocket hostSocket = null;
		try {
			hostSocket = socket.isPresent() ? socket.get() : new DatagramSocket();
		} catch (SocketException e) {
			throw new HostActionsException("Socket creation failure.", e);
		}

		try {
			hostSocket.send(packet);
		} catch (IOException e) {
			throw new HostActionsException("Data packet not sent.", e);
		}

	}
	/**
	 * @param packet
	 * @param socket
	 * @throws HostActionsException
	 */
	public static void receive(DatagramPacket packet, DatagramSocket socket) throws HostActionsException {

		try {
			socket.receive(packet);
			logger.debug("Packet recieved: " + Arrays.toString(packet.getData()));
		} catch (IOException e) {
			throw new HostActionsException("Data packet not received.", e);
		}
	}
}
