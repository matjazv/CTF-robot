/*
 *  AgentField - a simple capture-the-flag simulation for distributed intelligence
 *  Copyright (C) 2011 Luka Cehovin <http://vicos.fri.uni-lj.si/lukacu>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>. 
 */
package agents;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.Vector;

import utils.BestPos;
import utils.KnownArena;
import utils.Plan;
import utils.world.KnownPosition;
import utils.world.Planer;
import fri.pipt.agent.Agent;
import fri.pipt.agent.Membership;
import fri.pipt.protocol.Neighborhood;
import fri.pipt.protocol.Position;
import fri.pipt.protocol.Message.Direction;

// Run: java -cp bin fri.pipt.agent.Agent localhost fri.pipt.agent.sample.SampleAgent

@Membership("humans")
public class SampleAgent extends Agent {
	private static enum AgentState {
		EXPLORE, SEEK, RETURN
	}
	
	private static enum AgentReactState {
		CALM, ALLIES_NEAR, AXIS_NEAR
	}
	
	private AgentState state = AgentState.EXPLORE;
	private AgentReactState react = AgentReactState.CALM;
	
	
	private Direction direction;
	private KnownArena knownArena;
	private utils.world.KnownArena ka;
	private Neighborhood neighborhood;
	
	
	private Object waitMutex = new Object();

	private Decision left, right, up, down, still;
	
	private Decision[] decisions;
	
	protected static class Decision implements Comparable<Decision> {

		private float weight;

		private Direction direction;

		public float getWeight() {
			return weight;
		}

		public void setWeight(float weight) {
			this.weight = weight;
		}
		
		public void multiplyWeight(float f) {
			this.weight *= f;
		}
		
		public Direction getDirection() {
			return direction;
		}

		public void setDirection(Direction direction) {
			this.direction = direction;
		}

		public Decision(float weight, Direction direction) {
			super();
			this.weight = weight;
			this.direction = direction;
		}

		@Override
		public int compareTo(Decision o) {
			if (weight < o.weight)
				return -1;

			if (weight > o.weight)
				return 1;

			return 0;
		}

		public String toString() {
			return String.format("%s (%.2f)", direction.toString(), weight);
		}
		
	}
	
private Decision updateDecisions(Neighborhood n, AgentState state) {
		
		still.setWeight(0.01f);
		down.setWeight(canMove(n, 0, 1, state) ? 1 : 0);
		up.setWeight(canMove(n, 0, -1, state) ? 1 : 0);
		left.setWeight(canMove(n, -1, 0, state) ? 1 : 0);
		right.setWeight(canMove(n, 1, 0, state) ? 1 : 0);
		
		switch (this.react) {
		case CALM:
			switch (state) {
			case EXPLORE:
				decideOnExplore();
				break;
			case RETURN: {
				decideOnReturn();
				break;
			}
			case SEEK: {
				decideOnSeek();
				break;
			}
			}
			break;
		
		case AXIS_NEAR:
			decideOnAxisNear();
			break;
		case ALLIES_NEAR:
			decideOnAlliesNear();
			break;
		}
		
		
		
		Arrays.sort(decisions);
		
		return decisions[decisions.length - 1];
		
	}
	
	private void decideOnAlliesNear() {
	// TODO Auto-generated method stub
	
}

	private void decideOnAxisNear() {
	// TODO Auto-generated method stub
	
}
	private Explore seek;
	private void decideOnSeek() {
		if (knownArena.landmarks.get(Neighborhood.FLAG) == null) {
			state = AgentState.EXPLORE;
			decideOnExplore();
		}
		if (this.seek == null) this.seek = new Explore();
		if (this.seek.plan == null  || !this.seek.p.equals(knownArena.curentPosition) ) {
			this.seek.plan = Plan.createPlan(knownArena, knownArena.landmarks.get(Neighborhood.FLAG), 10000, true, 200);
		}
		//this.explore.plan.print();
		this.seek.p = this.seek.plan.p; 
		this.seek.plan = mulDirection(this.seek.plan);
}

	class Explore {
		Plan plan;
		Position p;
	}
	
	
	
	private Explore explore;
	private void decideOnExplore() {
		if (this.explore == null) this.explore = new Explore();		
		
		if (this.explore.plan == null && !knownArena.toVisit.isEmpty()) {// || !this.explore.p.equals(knownArena.curentPosition) ) {
			
			Vector<BestPos> tmp = new Vector<BestPos>();
			for ( BestPos bp :  knownArena.toVisit) {
				this.explore.plan = Plan.createPlan(knownArena, bp.p , 15, false, 100);
				tmp.add(bp);
				if ( this.explore.plan != null ) {
					//this.explore.plan.print();
					//System.out.print("\n\n");
					break;
				}
			}
			for (BestPos bp : tmp) {
				knownArena.toVisit.remove(bp);
				knownArena.visited.add(bp.p);
			}
		}
		
		if (this.explore.plan != null) {
			this.explore.p = this.explore.plan.p;
			this.explore.plan = mulDirection(this.explore.plan);
		}
}
	
	
	
	private Plan mulDirection(Plan p) {
		if (p.parent == null) return null;
		if (p.parent.p.getX() - p.p.getX()  == 1) {
			this.right.multiplyWeight(2);
		} else if (p.parent.p.getX() - p.p.getX()  == -1) {
			this.left.multiplyWeight(2);
		} else if (p.parent.p.getY() - p.p.getY()  == 1) {
			this.down.multiplyWeight(2);
		} else if (p.parent.p.getY() - p.p.getY()  == -1) {
			this.up.multiplyWeight(2);
		}
		return p.parent;
	}
	
	private void mulDirection(LinkedList<KnownPosition> plan) {
		KnownPosition current;
		KnownPosition next;
		if (plan.isEmpty()) {
			return;
		} if (plan.size() < 2) {
			plan.pollFirst();
			return;
		} else {
			current = plan.pollFirst();
			System.out.println(current.toString());
			next = plan.getFirst();
			System.out.println(next.toString());
		}
		if (next.getX() - current.getX()  == 1) {
			this.right.multiplyWeight(2);
			System.out.println("DESNO");
		} else if (next.getX() - current.getX()  == -1) {
			this.left.multiplyWeight(2);
			System.out.println("LEVO");
		} else if (next.getY() - current.getY()  == 1) {
			this.down.multiplyWeight(2);
			System.out.println("DOL");
		} else if (next.getY() - current.getY()  == -1) {
			this.up.multiplyWeight(2);
			System.out.println("GOR");
		}
	}

	private Explore ret;
	private void decideOnReturn() {
		if (this.ret == null) this.ret = new Explore();
		if (this.ret.plan == null  || !this.ret.p.equals(knownArena.curentPosition) ) {
			this.ret.plan = Plan.createPlan(knownArena, knownArena.landmarks.get(Neighborhood.HEADQUARTERS), 10000, true, 150);
			if (this.ret.plan == null) {
				decideOnExplore();
				return;
			}
		}
		//this.explore.plan.print();
		this.ret.p = this.ret.plan.p; 
		this.ret.plan = mulDirection(this.ret.plan);
}

	@Override
	public void receive(int from, byte[] message) {

		//String msg = new String(message);
	}
	@Override
	public void state(int stamp, Neighborhood neighborhood, Direction direction,
			boolean hasFlag) {
		
		if ( this.knownArena == null) {
			knownArena = new KnownArena(neighborhood);
			ka = new utils.world.KnownArena(neighborhood);
			//arenaviev = new KnownArenaView(ka);
			//arenavievKA = new KnownArenaView(knownArena);
		}
		else if ( direction == Direction.NONE ) {
			knownArena.updatePosition(this.direction);
			knownArena.updateArena(neighborhood);
			
			ka.updatePosition(neighborhood, this.direction);
			
			//arenavievKA.repaint();
		}
		
		this.neighborhood = neighborhood;
		this.direction = direction;
		
		
		if (knownArena.landmarks.get(Neighborhood.FLAG) != null) {
			state = AgentState.SEEK;
		}
		if (state != AgentState.RETURN && hasFlag) {
			state = AgentState.RETURN;
		}
		
			
		synchronized (waitMutex) {
			waitMutex.notify();
		}
	}

	@Override
	public void terminate() {
		knownArena = null;
	}



	@Override
	public void run() {
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
		}
		while (isAlive()) {
			
			try {

				scanAndWait();
				if (direction == Direction.NONE) {
					//arenaviev.repaint();
					direction = updateDecisions(neighborhood, state).getDirection();
					move(direction);
				}

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			try {
				Thread.sleep(5000);
			} catch (InterruptedException e) {
			}

		}

	}




	private void scanAndWait() throws InterruptedException {

		synchronized (waitMutex) {
			scan(0);
			waitMutex.wait();
		}

	}


	
	private boolean canMove(Neighborhood n, int x, int y, AgentState state) {
		
		switch (state) {
		case RETURN:
			return n.getCell(x, y) == Neighborhood.EMPTY || n.getCell(x, y) == Neighborhood.HEADQUARTERS;
		case SEEK:
			return n.getCell(x, y) == Neighborhood.EMPTY || n.getCell(x, y) == Neighborhood.FLAG;
		default:
			return n.getCell(x, y) == Neighborhood.EMPTY;		
		}
		
	}

	@Override
	public void initialize() {
		left = new Decision(0, Direction.LEFT);
		right = new Decision(0, Direction.RIGHT);
		up = new Decision(0, Direction.UP);
		down = new Decision(0, Direction.DOWN);
		still = new Decision(0, Direction.NONE);
		
		decisions = new Decision[] {
			left, right, up, down, still	
		};
	}
	
	
}
