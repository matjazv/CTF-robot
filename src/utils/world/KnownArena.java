package utils.world;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import agents.LooserAgent;
import utils.agent.AgentState;
import utils.world.KnownPosition.CompareType;


import fri.pipt.protocol.Neighborhood;
import fri.pipt.protocol.Message.Direction;

public class KnownArena {
	
	private static KnownArena ARENA;
	
	public static KnownArena getARENA() {
		return ARENA;
	}

	private HashMap<KnownPosition, KnownPosition> arena;
	//private HashMap<Integer, KnownPosition> landmarks;
	
	private Vector<KnownPosition> toVisit;
	
	private Vector<KnownPosition> discoveredPositions;
	
	private Vector<AlliesAgent> allies;
	
	HashSet<KnownPosition> forbiden;
	
	public HashSet<KnownPosition> getForbiden() {
		return forbiden;
	}

	public Vector<AlliesAgent> getAllies() {
		return allies;
	}

	private KnownPosition curentPosition;
	
	private KnownPosition flagPosition;
	
	private int nSize;
	
	public int getnSize() {
		return nSize;
	}
	
	public KnownArena (Neighborhood neighborhood) {
		ARENA = this;
		this.toVisit = new Vector<KnownPosition>();
		this.allies = new Vector<AlliesAgent>();
		this.discoveredPositions = new Vector<KnownPosition>();
		this.nSize = neighborhood.getSize();
		this.curentPosition = getRelativePosition(neighborhood);
		this.arena = new HashMap<KnownPosition, KnownPosition>();
		this.forbiden = new HashSet<KnownPosition>();

		updateCell(curentPosition.getX(), curentPosition.getY(), Neighborhood.EMPTY);
		this.curentPosition = getPositionAt(curentPosition.getX(), curentPosition.getY());
		for (int x = -neighborhood.getSize(); x <= neighborhood.getSize(); x++) {
			for (int y = -neighborhood.getSize(); y <= neighborhood.getSize(); y++) {
				
				updateCell(x+curentPosition.getX(), y+curentPosition.getY(), neighborhood.getCell(x, y));
			}
		}
		analizeNeighborhood(neighborhood);
	}
	
	public KnownPosition getCurentPosition() {
		return curentPosition;
	}
	
	public void analizeNeighborhood(Neighborhood neighborhood) {
		byte reactState = AgentState.CALM;
		Vector<AlliesAgent> temp = new Vector<AlliesAgent>();
		for (int x = -neighborhood.getSize(); x <= neighborhood.getSize(); x++) {
			for (int y = -neighborhood.getSize(); y <= neighborhood.getSize(); y++) {
				
				switch(neighborhood.getCell(x, y)) {
				case Neighborhood.EMPTY:
					break;
				case Neighborhood.WALL:
					break;
				case Neighborhood.FLAG:
					this.flagPosition = getPositionAt(x+getCurentPosition().getX(),y + getCurentPosition().getY());
					AgentState.setCalmState(AgentState.SEEK);
					break;
				case Neighborhood.OTHER:
					reactState |= AgentState.AXIS_NEAR;
					break;
				case Neighborhood.OTHER_FLAG:
					break;
				case Neighborhood.OTHER_HEADQUARTERS:
					break;
				default:
					if (neighborhood.getCell(x, y) == LooserAgent.getAGENT().getId() || neighborhood.getCell(x, y) <= 0) break;
					reactState |= AgentState.ALLIES_NEAR;
					AlliesAgent tempAgent = new AlliesAgent(neighborhood.getCell(x, y), discoveredPositions.size());
					tempAgent.setSeenPosition(getPositionAt(x+getCurentPosition().getX(),y + getCurentPosition().getY()));
					if (allies.contains(tempAgent)) {
						tempAgent = allies.get(allies.indexOf(tempAgent));
						tempAgent.setSeenPosition(getPositionAt(x+getCurentPosition().getX(),y + getCurentPosition().getY()));
					}
					temp.add(tempAgent);
				}
				AgentState.setReactState(reactState);
				synchronized (allies) {
					allies.clear();
					allies.addAll(temp);
				}
				if (LooserAgent.getAGENT().hasFlag()) AgentState.setCalmState(AgentState.RETURN);
			}
		}
	}

	public void updateCell(int x, int y, int type) {
		
		if (getPositionAt(x, y) != null) return;
		
		KnownPosition tempPositionI = new KnownPosition(x,y,type);
		setPositionAt(x, y, tempPositionI);
		
		
		
		if (getPositionAt(x,y).getType() == Neighborhood.EMPTY) {
			discoveredPositions.add(tempPositionI);
			Vector<Group> neighborGroups = new Vector<Group>();
			
			
			KnownPosition tempPositionN = getPositionAt(x+1, y);
			if (tempPositionN != null && tempPositionN.getType() == Neighborhood.EMPTY) neighborGroups.add(tempPositionN.getGroup());
			tempPositionN = getPositionAt(x-1, y);
			if (tempPositionN != null && tempPositionN.getType() == Neighborhood.EMPTY) neighborGroups.add(tempPositionN.getGroup());
			tempPositionN = getPositionAt(x, y+1);
			if (tempPositionN != null && tempPositionN.getType() == Neighborhood.EMPTY) neighborGroups.add(tempPositionN.getGroup());
			tempPositionN = getPositionAt(x, y-1);
			if (tempPositionN != null && tempPositionN.getType() == Neighborhood.EMPTY) neighborGroups.add(tempPositionN.getGroup());
			
			tempPositionI.setGroup();
			synchronized (this.toVisit) {
				this.toVisit.add(this.toVisit.size(), tempPositionI);
			} 
			neighborGroups.add(tempPositionI.getGroup());
			Group.connect(neighborGroups);
			
		}
	}
	
	public KnownPosition getFlagPosition() {
		return flagPosition;
	}

	public Vector<KnownPosition> getDiscoveredPositions() {
		return discoveredPositions;
	}

	private KnownPosition getRelativePosition(Neighborhood neighborhood) {
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				if (neighborhood.getCell(x, y) == Neighborhood.HEADQUARTERS) {
					return new KnownPosition(-x, -y, Neighborhood.EMPTY);
				}
			}
		}
		return null;
	}

	public void updatePosition(Neighborhood neighborhood, Direction direction) {
		switch (direction) {
		case UP:
			curentPosition = getPositionAt(curentPosition.getX(), curentPosition.getY()-1);
			updateDirection(0, -1, neighborhood);
			break;
		case DOWN:
			curentPosition = getPositionAt(curentPosition.getX(), curentPosition.getY()+1);
			updateDirection(0, 1, neighborhood);
			break;
		case LEFT:
			curentPosition = getPositionAt(curentPosition.getX() - 1, curentPosition.getY());
			updateDirection(-1, 0, neighborhood);
			break;
		case RIGHT:
			curentPosition = getPositionAt(curentPosition.getX() + 1, curentPosition.getY());
			updateDirection(1, 0, neighborhood);
			break;
		case NONE:
		}
		analizeNeighborhood(neighborhood);
	}
	
	private void updateDirection(int dx, int dy, Neighborhood neighborhood) {
		int mx = curentPosition.getX() + (dx != 0 ? dx : 1) * this.nSize;
		int my = curentPosition.getY() + (dy != 0 ? dy : 1) * this.nSize;
		
		for (int x = curentPosition.getX() + (dx != 0 ? dx : -1) * this.nSize; x <= mx; x++) {
			for (int y = curentPosition.getY() + (dy != 0 ? dy : -1) * this.nSize; y <= my; y++) {
				this.updateCell(x, y, neighborhood.getCell(x-curentPosition.getX(), y-curentPosition.getY()));
			}
		}
	}
	
	public void setPositionAt(int x, int y, KnownPosition position) {
		this.arena.put(new KnownPosition(x,y,0), position);
	}
	
	public KnownPosition getPositionAt(int x, int y) {
		return this.arena.get(new KnownPosition(x,y,0));
	}
	
	public KnownPosition getPositionAt(KnownPosition position) {
		return this.arena.get(position);
	}

	public boolean canMove(KnownPosition position) {
		if (this.getPositionAt(position) == null
				|| this.getPositionAt(position).getType() != Neighborhood.EMPTY) {
			
			return false;
		}
		return true;
	}

	public HashMap<KnownPosition, KnownPosition> getArena() {
		return arena;
	}
	
	private HashSet<KnownPosition> waitForSuperior() {
		HashSet<KnownPosition> forbiden = new HashSet<KnownPosition>();
		synchronized (allies) {
		if (allies == null || allies.isEmpty()) return forbiden;
		Collections.sort(allies); 
		
			try {
				boolean got = false;
				for (AlliesAgent agent: allies) {
					if (agent.getID() > LooserAgent.getAGENT().getId())
						break;
					if (agent.getSeenPosition() != null && !agent.getSeenPosition().equals(
							agent.getCurrentPosition())) {
						forbiden.add(agent.getSeenPosition());
						got = true;
						break;
					}
				}
				//if (got) wait(10);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (AlliesAgent agent : allies) {
				System.out.println(agent.getID());
				if (agent.getID() > LooserAgent.getAGENT().getId())
					break;
				if (agent.getSeenPosition() != null && !agent.getSeenPosition().equals(
						agent.getCurrentPosition())) {
					forbiden.add(agent.getSeenPosition());
					continue;
				}
				forbiden.add(agent.getCurrentPosition());
				forbiden.add(agent.getPlanedPosition());
			}
		}
		return forbiden;
	}
	
	public KnownPosition getBestExploreCandidate() {
		HashSet<KnownPosition> tempSet = new HashSet<KnownPosition>();
		synchronized (toVisit) {
			for (KnownPosition position : toVisit) {
				if (position.isAccesible()) tempSet.add(position);
			}
		}
		// TODO this.forbiden = waitForSuperior();
		Planer.makePlanAStarMulti(tempSet);
		Vector<KnownPosition> toRemove = new Vector<KnownPosition>();
		synchronized (toVisit) {
			for (KnownPosition position : toVisit) {
				if (position.eval()) toRemove.add(position);
			}
			for (KnownPosition position : toRemove) {
				toVisit.remove(position);
			}
		}
		this.forbiden = waitForSuperior();
		KnownPosition.setCompareType(CompareType.EXPLORE);
		synchronized (toVisit) {
			Collections.sort(toVisit);
		
		//System.out.print(toVisit.firstElement().toString());
		
			if (!toVisit.isEmpty()) return toVisit.firstElement();else
			return discoveredPositions.get((int)(Math.random()*(discoveredPositions.size()-1)));
		}
	}
}
