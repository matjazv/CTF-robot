package utils.agent;

import java.util.Vector;

import agents.LooserAgent;

import fri.pipt.protocol.Neighborhood;
import utils.world.AlliesAgent;
import utils.world.KnownArena;
import utils.world.KnownPosition;
import utils.world.Planer;



public class Message {
	
	public static final int messageSize = 256;
	
	public static byte[] encodeMessage(AlliesAgent agent) {
		int index = 0;
		byte [] message = new byte [messageSize];
		message[index++] = AgentState.getCalmState();
		message[index++] = (byte) Neighborhood.EMPTY;
		message[index++] = (byte) KnownArena.getARENA().getCurentPosition().getX();
		message[index++] = (byte) KnownArena.getARENA().getCurentPosition().getY();
		if (Planer.getPlanForPaint() != null && Planer.getPlanForPaint().size() > 1) {
			message[index++] = (byte) Planer.getPlanForPaint().get(1).getX();
			message[index++] = (byte) Planer.getPlanForPaint().get(1).getY();
		} else {
			message[index++] = (byte) KnownArena.getARENA().getCurentPosition().getX();
			message[index++] = (byte) KnownArena.getARENA().getCurentPosition().getY();
		}
		Vector<KnownPosition> temp = KnownArena.getARENA().getDiscoveredPositions();
		int tempIndex;
		while(index < messageSize) {
			synchronized (agent) {
				
				tempIndex = agent.getNextIndex(temp.size());
				if (tempIndex > 0 && temp.get(tempIndex).getType() != Neighborhood.EMPTY) {
					continue;
				}
				if (tempIndex > 0) {
					message[index++] = (byte) temp.get(tempIndex).getX();
					message[index++] = (byte) temp.get(tempIndex).getY();
				} else {
					message[index++] = 0;
					message[index++] = 0;
				}
			}
		}
		
		
		return message;
	}

	public static void decodeMessage(byte[] message, int ID) {
		
		AlliesAgent tempAgent = new AlliesAgent(ID, KnownArena.getARENA().getDiscoveredPositions().size());
		if (KnownArena.getARENA() == null) return;
		
		if (KnownArena.getARENA().getAllies() != null
					&& KnownArena.getARENA().getAllies().contains(tempAgent)) {
				tempAgent = KnownArena.getARENA().getAllies().get(
						KnownArena.getARENA().getAllies().indexOf(tempAgent));
			} else {
				KnownArena.getARENA().getAllies().add(tempAgent);
			}
		
			
			
		int index = 0;
		tempAgent.setState(message[index++]);
		int type = (int) message[index++];
		int x = message[index++];
		int y = message[index++];
		tempAgent.setCurrentPosition(new KnownPosition(x,y,0));
		KnownArena.getARENA().updateCell(x, y, type);
		
		x = message[index++];
		y = message[index++];
		tempAgent.setPlanedPosition(new KnownPosition(x,y,0));
		KnownArena.getARENA().updateCell(x, y, type);
		while(index < messageSize) {
			x = message[index++];
			y = message[index++];
			if (x != 0 || y != 0) KnownArena.getARENA().updateCell(x, y, type);
		}
	}
}
