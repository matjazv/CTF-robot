package utils.world;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.PriorityQueue;

import utils.agent.AgentState;
import utils.world.KnownPosition.CompareType;

import fri.pipt.protocol.Neighborhood;

public class Planer {
	
	private static LinkedList<KnownPosition> explorePlan;
	private static LinkedList<KnownPosition> seekPlan;
	private static LinkedList<KnownPosition> returnPlan;
	
	private static int counter = 0;
	public static LinkedList<KnownPosition> getPlan() {
		counter = (counter+1)%5;
		switch (AgentState.getCalmState()) {
		case AgentState.EXPLORE:
			if (counter == 0 || explorePlan == null || explorePlan.size() < 2 || !explorePlan.getFirst().equals(KnownArena.getARENA().getCurentPosition())) {
				explorePlan = makePlanAStar(KnownArena.getARENA().getBestExploreCandidate(), false);
			}
			return explorePlan;
		case AgentState.RETURN:
			if (counter == 0 || returnPlan == null || returnPlan.size() < 2 || !returnPlan.getFirst().equals(KnownArena.getARENA().getCurentPosition())) {
				returnPlan = makePlanAStar(KnownArena.getARENA().getPositionAt(0, 0), true);
			}
			return returnPlan;
		case AgentState.SEEK:
			if (counter == 0 || seekPlan == null || seekPlan.size() < 2  || !seekPlan.getFirst().equals(KnownArena.getARENA().getCurentPosition())) {
				seekPlan = makePlanAStar(KnownArena.getARENA().getFlagPosition(), true);
			}
			return seekPlan;
		default:
			return null;
		}
			
	}
	
	public static LinkedList<KnownPosition> getPlanForPaint() {
		switch (AgentState.getCalmState()) {
		case AgentState.EXPLORE:
			return explorePlan;
		case AgentState.RETURN:
			return returnPlan;
		case AgentState.SEEK:
			return seekPlan;
		default:
			return null;
		}
	}


	public static LinkedList<KnownPosition> makePlanAStar(KnownPosition goal, boolean force) {
		
		if (KnownArena.getARENA().getPositionAt(goal) == null || (KnownArena.getARENA().getPositionAt(goal).getType() != Neighborhood.EMPTY && !force)) {
			System.out.println("dsakfglsdahgsadfjgkjdfajalgfkakgjfasdjAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa");
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
				plan.addLast(goal);
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
		System.out.println("dsakfglsdahgsadfjgkjdfajalgfkakgjfasdjAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAa");
		return plan;
	}

}
