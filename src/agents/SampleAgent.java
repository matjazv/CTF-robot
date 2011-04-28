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

import utils.KnownArena;
import fri.pipt.agent.Agent;
import fri.pipt.agent.Membership;
import fri.pipt.agent.sample.SampleAgent.AgentState;
import fri.pipt.agent.sample.SampleAgent.Decision;
import fri.pipt.protocol.Neighborhood;
import fri.pipt.protocol.Position;
import fri.pipt.protocol.Message.Direction;

// Run: java -cp bin fri.pipt.agent.Agent localhost fri.pipt.agent.sample.SampleAgent

@Membership("samples")
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
				break;
			case RETURN: {
				break;
			}
			case SEEK: {
				break;
			}
			}
			break;
		
		case AXIS_NEAR:
			break;
		case ALLIES_NEAR:
			break;
		}
		
		
		
		Arrays.sort(decisions);
		
		return decisions[decisions.length - 1];
		
	}
	
	@Override
	public void receive(int from, byte[] message) {

		String msg = new String(message);
	}

	@Override
	public void state(int stamp, Neighborhood neighborhood, Direction direction,
			boolean hasFlag) {

		this.neighborhood = neighborhood;
		this.direction = direction;
		
		if ( this.knownArena == null) {
			knownArena = new KnownArena(neighborhood);
		}
		else
			knownArena.updateArena(neighborhood);
		
		
		if (state != AgentState.RETURN && hasFlag)
			state = AgentState.RETURN;

		synchronized (waitMutex) {
			waitMutex.notify();
		}
	}

	@Override
	public void terminate() {

	}



	@Override
	public void run() {

		while (isAlive()) {

			try {

				scanAndWait();
				move(updateDecisions(n, state));

			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			try {
				Thread.sleep(1000);
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
		// TODO Auto-generated method stub
		
	}
	
	
}
