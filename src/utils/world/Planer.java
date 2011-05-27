package utils.world;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

import utils.world.KnownPosition.CompareType;

import fri.pipt.protocol.Neighborhood;

public class Planer {
	
	private static LinkedList<KnownPosition> explorePlan;
	
	public static LinkedList<KnownPosition> getExplorePlan() {
		if (explorePlan == null || explorePlan.size() < 2 || !explorePlan.getFirst().equals(KnownArena.getARENA().getCurentPosition())) {
			explorePlan = makePlanAStar(KnownArena.getARENA().getBestExploreCandidate(), false);
		}
		return explorePlan;
	}
	
	public static LinkedList<KnownPosition> getExplorePlanForPaint() {
		return explorePlan;
	}


	public static LinkedList<KnownPosition> makePlanAStar(KnownPosition goal, boolean force) {
		
		if (KnownArena.getARENA().getPositionAt(goal) == null || !goal.isAccesible() || (KnownArena.getARENA().getPositionAt(goal).getType() != Neighborhood.EMPTY && !force)) {
			return null;
		}
		
		KnownPosition.setCompareType(CompareType.PLAN);
		PriorityQueue<KnownPosition> priorityQueue = new PriorityQueue<KnownPosition>();
		HashSet<KnownPosition> visited = new HashSet<KnownPosition>();
		LinkedList<KnownPosition> plan = new  LinkedList<KnownPosition>();
		
		goal.setGoal(goal);
		goal.setF(0);
		priorityQueue.add(goal);
		visited.add(goal);
		
		KnownPosition position;
		KnownPosition tempPosition;
		while (!priorityQueue.isEmpty()) {
			position = priorityQueue.poll();
			if (position.equals(KnownArena.getARENA().getCurentPosition())) {
				
				while(!goal.equals(position)) {
					plan.addLast(position);
					position = position.getParent();
				}
				//plan.addLast(goal);
				return plan;
			}
			tempPosition = KnownArena.getARENA().getPositionAt(position.getX() + 1, position.getY());
			if (KnownArena.getARENA().canMove(tempPosition) && !visited.contains(tempPosition)) {
				tempPosition.setGoal(goal);
				tempPosition.setF(position.getF() + 1);
				tempPosition.setParent(position);
				visited.add(tempPosition);
				priorityQueue.add(tempPosition);
			}
			tempPosition = KnownArena.getARENA().getPositionAt(position.getX() - 1, position.getY());
			if (KnownArena.getARENA().canMove(tempPosition) && !visited.contains(tempPosition)) {
				tempPosition.setGoal(goal);
				tempPosition.setF(position.getF() + 1);
				tempPosition.setParent(position);
				visited.add(tempPosition);
				priorityQueue.add(tempPosition);
			}
			tempPosition = KnownArena.getARENA().getPositionAt(position.getX(), position.getY() + 1);
			if (KnownArena.getARENA().canMove(tempPosition) && !visited.contains(tempPosition)) {
				tempPosition.setGoal(goal);
				tempPosition.setF(position.getF() + 1);
				tempPosition.setParent(position);
				visited.add(tempPosition);
				priorityQueue.add(tempPosition);
			}
			tempPosition = KnownArena.getARENA().getPositionAt(position.getX(), position.getY() - 1);
			if (KnownArena.getARENA().canMove(tempPosition) && !visited.contains(tempPosition)) {
				tempPosition.setGoal(goal);
				tempPosition.setF(position.getF() + 1);
				tempPosition.setParent(position);
				visited.add(tempPosition);
				priorityQueue.add(tempPosition);
			}
		}

		return plan;
	}

}
