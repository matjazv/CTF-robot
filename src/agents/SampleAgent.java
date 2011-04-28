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
import java.util.HashMap;

import fri.pipt.agent.Agent;
import fri.pipt.agent.Membership;
import fri.pipt.arena.TerminalView;
import fri.pipt.protocol.Neighborhood;
import fri.pipt.protocol.Position;
import fri.pipt.protocol.Message.Direction;

// Run: java -cp bin fri.pipt.agent.Agent localhost fri.pipt.agent.sample.SampleAgent

@Membership("losers")
public class SampleAgent extends Agent {

	private static enum AgentState {
		EXPLORE, SEEK, RETURN
	}
	private AgentState state = AgentState.EXPLORE;
	private Direction direction;
	private Neighborhood neighborhood;
	private Object waitMutex = new Object();


	@Override
	public void receive(int from, byte[] message) {

		String msg = new String(message);
	}

	@Override
	public void state(int stamp, Neighborhood neighborhood, Direction direction,
			boolean hasFlag) {

		this.neighborhood = neighborhood;
		this.direction = direction;

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

				arena.update(neighborhood);

				analyzeNeighborhood(neighborhood);

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

	private void analyzeNeighborhood(Neighborhood n) {

		for (int i = -n.getSize(); i <= n.getSize(); i++) {

			for (int j = -n.getSize(); j <= n.getSize(); j++) {

				if (n.getCell(i, j) == Neighborhood.FLAG) {

					System.out.println("Found flag !!!");
					
					registry.put("flag", new Position(x + i, y + j));

					state = AgentState.SEEK;

					continue;
				}

				if (n.getCell(i, j) == Neighborhood.HEADQUARTERS) {

					registry.put("hq", new Position(x + i, y + j));
					
					continue;
				}

				if (n.getCell(i, j) > 0) {

					if (i != 0 && j != 0)
						send(n.getCell(i, j), "Hello " + n.getCell(i, j) + "!");
					
					continue;
				}
				
			}

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
	
	
}
