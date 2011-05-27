package utils.agent;

import java.util.Vector;

import fri.pipt.protocol.Neighborhood;
import utils.world.KnownArena;
import utils.world.KnownPosition;
import utils.world.Planer;



public class Message {
	
	public static final int messageSize = 256;
	
	public static byte[] encodeMessage() {
		int index = 0;
		byte [] message = new byte [messageSize];
		message[index++] = AgentState.getCalmState();
		message[index++] = (byte) Neighborhood.EMPTY;
		message[index++] = (byte) KnownArena.getARENA().getCurentPosition().getX();
		message[index++] = (byte) KnownArena.getARENA().getCurentPosition().getY();
		if (Planer.getPlanForPaint().size() > 1) {
			message[index++] = (byte) Planer.getPlanForPaint().get(1).getX();
			message[index++] = (byte) Planer.getPlanForPaint().get(1).getY();
		} else {
			message[index++] = (byte) KnownArena.getARENA().getCurentPosition().getX();
			message[index++] = (byte) KnownArena.getARENA().getCurentPosition().getY();
		}
		Vector<KnownPosition> temp = KnownArena.getARENA().getDiscoveredPositions();
		int tempIndex = temp.size();
		while(index < messageSize) {
			message[index++] = (byte) temp.get(tempIndex).getX();
			message[index++] = (byte) temp.get(tempIndex++).getY();
		}
		
		
		return message;
	}

}
