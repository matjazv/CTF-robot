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
package fri.pipt.server;

import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Vector;

import fri.pipt.protocol.Message;
import fri.pipt.protocol.Neighborhood;
import fri.pipt.protocol.Message.Direction;
import fri.pipt.server.Dispatcher.Client;
import fri.pipt.server.Field.BodyPosition;
import fri.pipt.server.Field.Cell;
import fri.pipt.server.Field.Wall;
import fri.pipt.server.Team.Flag;
import fri.pipt.server.Team.Headquarters;
import fri.pipt.server.Team.TeamBody;

public class Game {

	private int spawnFrequency = 10;

	private Field field;

	private HashMap<String, Team> teams = new HashMap<String, Team>();

	private int maxAgentsPerTeam = 10;

	private int maxMessageDistance = 10;

	private Properties properties = null;

	private File gameSource;
	
	private Vector<GameListener> listeners = new Vector<GameListener>();
	
	private static final Color[] colors = new Color[] { Color.red, Color.blue,
			Color.green, Color.yellow, Color.pink, Color.orange, Color.black,
			Color.white };

	/**
	 * Computes Manhattan distance between two agents
	 * 
	 * @param a1 agent 1
	 * @param a2 agent 2
	 * @return Manhattan distance
	 */
	private int distance(Agent a1, Agent a2) {
		
		if (a1 == null || a2 == null)
			return -1;
		
		BodyPosition bp1 = field.getPosition(a1);
		BodyPosition bp2 = field.getPosition(a2);
		
		return Math.abs(bp1.getX() - bp2.getX()) + Math.abs(bp1.getY() - bp2.getY());
	}
	
	private Game() throws IOException {

	}

	public Team getTeam(String id) {

		if (id == null)
			return null;

		return teams.get(id);

	}

	public List<Team> getTeams() {

		return new Vector<Team>(teams.values());

	}

	public static Game loadFromFile(File f) throws IOException {

		Game game = new Game();

		game.properties = new Properties();

		game.properties.load(new FileReader(f));

		game.gameSource = f;
		
		int index = 0;

		while (true) {

			index++;

			String id = game.properties.getProperty("team" + index);
			String name = game.properties.getProperty("team" + index + ".name");

			if (id == null)
				break;

			if (name == null)
				name = id;

			game.teams.put(id, new Team(name, colors[index]));

			System.out.println("Registered team: " + id);

		}

		String fldPath = game.getProperty("gameplay.field", f.getAbsolutePath()
				+ ".field");

		File fldFile = new File(fldPath);

		if (!fldPath.startsWith(File.separator)) {
			fldFile = new File(f.getParentFile(), fldPath);
		}

		game.field = Field.loadFromFile(fldFile, game);

		game.spawnFrequency = game.getProperty("gameplay.respawn", 30);

		game.maxAgentsPerTeam = game.getProperty("gameplay.agents", 10);

		game.maxMessageDistance = game.getProperty("message.distance", 10);
		
		return game;

	}

	public Field getField() {

		return field;

	}

	private int spawnCounter = spawnFrequency;

	private int step = 0;

	public synchronized void step() {

		step++;

		if (step % 100 == 0)
			System.out.println("Game step: " + step);

		// handle moves and collisions
		for (Team t : teams.values()) {
			t.move(field);
		}

		// spawn new agents
		spawnCounter--;
		if (spawnCounter == 0) {
			spawnNewAgents();
			spawnCounter = spawnFrequency;
		}

		// remove dead agents
		for (Team t : teams.values()) {
			t.cleanup(field);
		}

		// check end conditions?
		// TODO

	}

	private void spawnNewAgents() {

		for (Team t : teams.values()) {

			if (t.size() < maxAgentsPerTeam) {

				BodyPosition pos = field.getPosition(t.getHeadquarters());

				if (pos == null)
					continue;

				Collection<Cell> cells = field.getNeighborhood(pos.getX(), pos
						.getY());

				for (Cell c : cells) {

					if (!c.isEmpty())
						continue;

					Agent agt = t.newAgent();

					if (agt == null)
						break;

					field.putBody(agt, new BodyPosition(c.getPosition(), 0, 0));

					break;
				}

			}

		}

	}

	public Neighborhood scanNeighborhood(int size, Agent agent) {

		Neighborhood n = new Neighborhood(size);

		BodyPosition bp = field.getPosition(agent);

		if (bp == null)
			return null;

		for (int j = -size; j <= size; j++) {
			for (int i = -size; i <= size; i++) {

				Cell c = field.getCell(bp.getX() + i, bp.getY() + j);

				if (c == null) {
					n.setCell(i, j, Neighborhood.WALL);
					continue;
				}

				if (c.isEmpty()) {
					n.setCell(i, j, Neighborhood.EMPTY);
					continue;
				}

				if (c.getBody() instanceof Wall) {
					n.setCell(i, j, Neighborhood.WALL);
					continue;
				}

				if (c.getBody() instanceof TeamBody) {

					Team t = ((TeamBody) c.getBody()).getTeam();

					if (c.getBody() instanceof Headquarters) {
						n
								.setCell(
										i,
										j,
										t == agent.getTeam() ? Neighborhood.HEADQUARTERS
												: Neighborhood.OTHER_HEADQUARTERS);
						continue;
					}

					if (c.getBody() instanceof Flag) {
						n.setCell(i, j,
								t == agent.getTeam() ? Neighborhood.FLAG
										: Neighborhood.OTHER_FLAG);
						continue;
					}

					if (c.getBody() instanceof Agent) {
						n.setCell(i, j, t == agent.getTeam() ? ((Agent) c
								.getBody()).getId() : Neighborhood.OTHER);
						continue;
					}
				}
			}
		}

		return n;

	}

	public int getProperty(String key, int def) {

		try {
			return Integer.parseInt(properties.getProperty(key));
		} catch (Exception e) {
			return def;
		}

	}

	public String getProperty(String key, String def) {

		if (properties.getProperty(key) == null)
			return def;

		return properties.getProperty(key);

	}

	public String getName() {
		if (properties.contains("title"))
			return properties.getProperty("title");
		
		return gameSource.getAbsolutePath();
	}
	
	public int getStep() {
		return step;
	}

	public void addListener(GameListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
		
	}
	
	public void removeListener(GameListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	
	public synchronized void message(Team team, int from, int to, byte[] message) {
		Client cltto = team.findById(to);
		Client cltfrom = team.findById(from);
		
		if (from == to) {
			System.out.printf("Message from %d to %d rejected: same agent", from, to);
			return;
		}
		
		if (cltto != null && cltfrom != null) {
			
			int dst = distance(cltfrom.getAgent(), cltto.getAgent());
			if (dst > maxMessageDistance || dst < 0) {
				System.out.printf("Message from %d to %d rejected: too far away", from, to);
				return;
			}
			
			cltto.sendMessage(new Message.ReceiveMessage(from, message));
									
		} else return;
		
		
		synchronized (listeners) {
			for (GameListener l : listeners) {
				try {
					l.message(team, from, to, message.length);
				} catch (Exception e) {e.printStackTrace();}
				
			}
		}
	}
	
	public synchronized void move(Team team, int agent, Direction direction) {
		
		Client clt = team.findById(agent);
		
		if (clt != null && clt.getAgent() != null) {
			clt.getAgent().setDirection(direction);
		}
		
		synchronized (listeners) {
			for (GameListener l : listeners) {
				try {
					l.move(team, agent, direction);
				} catch (Exception e) {e.printStackTrace();}
				
			}
		}
		
	}
	
}
