package utils.world;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

import utils.world.KnownPosition.CompareType;

import fri.pipt.protocol.Neighborhood;

public class Planer {
	
	public LinkedList<KnownPosition> makePlanAStar(KnownPosition goal, boolean force) {
		
		if (KnownArena.getARENA().getPositionAt(goal) == null || !goal.isAccesible() || (KnownArena.getARENA().getPositionAt(goal).getType() != Neighborhood.EMPTY && !force)) {
			return null;
		}
		
		KnownPosition.setCompareType(CompareType.PLAN);
		PriorityQueue<KnownPosition> priorityQueue = new PriorityQueue<KnownPosition>();
		HashSet<KnownPosition> visited = new HashSet<KnownPosition>();
		LinkedList<KnownPosition> plan = new  LinkedList<KnownPosition>();
		

		priorityQueue.add(goal);
		visited.add(goal);
		
		KnownPosition position;
		KnownPosition tempPosition;
		while (!priorityQueue.isEmpty()) {
			position = priorityQueue.poll();
			if (position.equals(KnownArena.getARENA().getCurentPosition()))
				return plan;

			tempPosition = KnownArena.getARENA().getPositionAt(position.getX() + 1, position.getY());
			if (KnownArena.getARENA().canMove(tempPosition) && !visited.contains(tempPosition)) {
				visited.add(tempPosition);
				plan.addFirst(tempPosition);
			}
			tempPosition = KnownArena.getARENA().getPositionAt(position.getX() - 1, position.getY());
			if (KnownArena.getARENA().canMove(tempPosition) && !visited.contains(tempPosition)) {
				visited.add(tempPosition);
				plan.addFirst(tempPosition);
			}
			tempPosition = KnownArena.getARENA().getPositionAt(position.getX(), position.getY() + 1);
			if (KnownArena.getARENA().canMove(tempPosition) && !visited.contains(tempPosition)) {
				visited.add(tempPosition);
				plan.addFirst(tempPosition);
			}
			tempPosition = KnownArena.getARENA().getPositionAt(position.getX(), position.getY() - 1);
			if (KnownArena.getARENA().canMove(tempPosition) && !visited.contains(tempPosition)) {
				visited.add(tempPosition);
				plan.addFirst(tempPosition);
			}
		}

		return null;
	}

}
