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
	
	private static boolean toUpdate = false;
	public static void setToUpdate(boolean toUpdate) {
		Planer.toUpdate = toUpdate;
	}

	public static LinkedList<KnownPosition> getPlan() {
		//counter = (counter+1)%7;
		switch (AgentState .getCalmState()) {
		case AgentState.EXPLORE:
			if (toUpdate || explorePlan == null || explorePlan.size() < 2 || !explorePlan.getFirst().equals(KnownArena.getARENA().getCurentPosition())) {
				explorePlan = makePlanAStar(KnownArena.getARENA().getBestExploreCandidate(), false);
			}
			toUpdate = false;
			return explorePlan;
		case AgentState.RETURN:
			if ( returnPlan == null || returnPlan.size() < 2 || !returnPlan.getFirst().equals(KnownArena.getARENA().getCurentPosition())) {
				returnPlan = makePlanAStar(KnownArena.getARENA().getPositionAt(0, 0), true);
			}
			toUpdate = false;
			return returnPlan;
		case AgentState.SEEK:
			if (toUpdate || seekPlan == null || seekPlan.size() < 2  || !seekPlan.getFirst().equals(KnownArena.getARENA().getCurentPosition())) {
				seekPlan = makePlanAStar(KnownArena.getARENA().getFlagPosition(), true);
			}
			toUpdate = false;
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
			return null;
		}
		
		KnownPosition.setCompareType(CompareType.PLAN);
		PriorityQueue<KnownPosition> priorityQueue = new PriorityQueue<KnownPosition>();
		HashSet<KnownPosition> visited = new HashSet<KnownPosition>();
		LinkedList<KnownPosition> plan = new  LinkedList<KnownPosition>();
		HashSet<KnownPosition> forbiden =  KnownArena.getARENA().getForbiden();
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
			if (KnownArena.getARENA().canMove(tempPosition) && !visited.contains(tempPosition) && !forbiden.contains(tempPosition)) {
				tempPosition.setGoal(goal);
				tempPosition.setF(position.getF() + 1);
				tempPosition.setParent(position);
				visited.add(tempPosition);
				priorityQueue.add(tempPosition);
			}
			tempPosition = KnownArena.getARENA().getPositionAt(position.getX() - 1, position.getY());
			if (KnownArena.getARENA().canMove(tempPosition) && !visited.contains(tempPosition) && !forbiden.contains(tempPosition)) {
				tempPosition.setGoal(goal);
				tempPosition.setF(position.getF() + 1);
				tempPosition.setParent(position);
				visited.add(tempPosition);
				priorityQueue.add(tempPosition);
			}
			tempPosition = KnownArena.getARENA().getPositionAt(position.getX(), position.getY() + 1);
			if (KnownArena.getARENA().canMove(tempPosition) && !visited.contains(tempPosition) && !forbiden.contains(tempPosition)) {
				tempPosition.setGoal(goal);
				tempPosition.setF(position.getF() + 1);
				tempPosition.setParent(position);
				visited.add(tempPosition);
				priorityQueue.add(tempPosition);
			}
			tempPosition = KnownArena.getARENA().getPositionAt(position.getX(), position.getY() - 1);
			if (KnownArena.getARENA().canMove(tempPosition) && !visited.contains(tempPosition) && !forbiden.contains(tempPosition)) {
				tempPosition.setGoal(goal);
				tempPosition.setF(position.getF() + 1);
				tempPosition.setParent(position);
				visited.add(tempPosition);
				priorityQueue.add(tempPosition);
			}
		}
		return plan;
	}
	
public static void makePlanAStarMulti (HashSet<KnownPosition> goals) {
		
		
		KnownPosition.setCompareType(CompareType.PLAN_MULTI);
		PriorityQueue<KnownPosition> priorityQueue = new PriorityQueue<KnownPosition>();
		HashSet<KnownPosition> visited = new HashSet<KnownPosition>();
		HashSet<KnownPosition> forbiden =  KnownArena.getARENA().getForbiden();
		
		KnownPosition curentPosition = KnownArena.getARENA().getCurentPosition();
		curentPosition.setF(0);
		priorityQueue.add(curentPosition);
		visited.add(curentPosition);
		
		KnownPosition position;
		KnownPosition tempPosition;
		while (!priorityQueue.isEmpty()) {
			position = priorityQueue.poll();
			if (goals.contains(position)) {
				position.setDistance(position.getF());
				goals.remove(position);
				if (goals.isEmpty())return;
			}
			tempPosition = KnownArena.getARENA().getPositionAt(position.getX() + 1, position.getY());
			if (KnownArena.getARENA().canMove(tempPosition) && !visited.contains(tempPosition) && !forbiden.contains(tempPosition)) {
				tempPosition.setF(position.getF() + 1);
				visited.add(tempPosition);
				priorityQueue.add(tempPosition);
			}
			tempPosition = KnownArena.getARENA().getPositionAt(position.getX() - 1, position.getY());
			if (KnownArena.getARENA().canMove(tempPosition) && !visited.contains(tempPosition) && !forbiden.contains(tempPosition)) {
				tempPosition.setF(position.getF() + 1);
				visited.add(tempPosition);
				priorityQueue.add(tempPosition);
			}
			tempPosition = KnownArena.getARENA().getPositionAt(position.getX(), position.getY() + 1);
			if (KnownArena.getARENA().canMove(tempPosition) && !visited.contains(tempPosition) && !forbiden.contains(tempPosition)) {
				tempPosition.setF(position.getF() + 1);
				visited.add(tempPosition);
				priorityQueue.add(tempPosition);
			}
			tempPosition = KnownArena.getARENA().getPositionAt(position.getX(), position.getY() - 1);
			if (KnownArena.getARENA().canMove(tempPosition) && !visited.contains(tempPosition) && !forbiden.contains(tempPosition)) {
				tempPosition.setF(position.getF() + 1);
				visited.add(tempPosition);
				priorityQueue.add(tempPosition);
			}
		}
		return;
	}

}
