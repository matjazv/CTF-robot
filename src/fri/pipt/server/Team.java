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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

import fri.pipt.arena.Arena;
import fri.pipt.server.Dispatcher.Client;
import fri.pipt.server.Field.Body;
import fri.pipt.server.Field.BodyPosition;

public class Team {

	private static final int MAX_ID = 10000000;
	
	public static class TeamBody extends Body {
		
		private Team team;
		
		public TeamBody(int tile, Team team) {
			super(tile);
			this.team = team;
			
			
		}
		
		public Team getTeam() {
			return team;
		}
	}
	
	public static class Flag extends TeamBody {

		private Flag(int tile, Team team) {
			super(tile, team);
		}
		
	}

	public static class Headquarters extends TeamBody {

		public Headquarters(int tile, Team team) {
			super(tile, team);
		}

		public void putFlag(Flag flag) {
			
			if (flag != null && flag.getTeam() == getTeam()) {
				
				if (getTeam().flags.contains(flag)) {
				
					getTeam().score++;
					getTeam().scoreChange(getTeam().score);
					getTeam().flags.remove(flag);
					
				}
			}
			
		}
		
	}
	
	private HashSet<Client> used = new HashSet<Client>();
	
	private ConcurrentLinkedQueue<Client> pool = new ConcurrentLinkedQueue<Client>();
	
	private LinkedList<Agent> removed = new LinkedList<Agent>();
	
	private HashSet<Integer> allocatedIds = new HashSet<Integer>();
	
	private HashSet<Flag> flags = new HashSet<Flag>();
	
	private String name;
	
	private Headquarters hq;
	
	private Color color;
	
	private int score = 0;
	
	public Team(String name, Color color) {
		
		this.name = name;
		this.color = color;
		
		hq = new Headquarters(Arena.TILE_HEADQUARTERS, this);

	}
	
	public Headquarters getHeadquarters() {
		
		return hq;
		
	}
	
	public Color getColor() {
		
		return color;
		
	}
	
	public String getName() {
		
		return name;
		
	}
	
	public Flag newFlag() {
		
		Flag f = new Flag(Arena.TILE_FLAG, this);
		
		flags.add(f);
		
		return f;
		
	}
	
	public Agent newAgent() {
		
		synchronized (pool) {
			
			Client client = pool.poll();
			
			if (client == null)
				return null;
			
			Agent agt = new Agent(this, getUniqueId());
			
			client.setAgent(agt);
			used.add(client);
			
			System.out.println("New agent spawned for team: " + name + " (id: " + agt.getId() + ")");
			
			return agt;
		}
		

	}
	
	public void addClient(Client client) {

		if (client == null)
			return;
		
		synchronized (pool) {
			
			pool.add(client);
			
		}

		clientConnected(client);
	}
	
	public void removeClient(Client client) {
		
		System.out.println("Remove client: " + client);
		
		if (client == null)
			return;
		
		synchronized (pool) {

			if (client.getAgent() != null) {
				client.getAgent().die();
				removed.add(client.getAgent());
			}
			
			// just in case ... remove from everywhere :)
			pool.remove(client);
			used.remove(client);
			
		}

		clientDisconnected(client);
		
	}
	
	public int size() {
		
		synchronized (pool) {
			
			return used.size();
			
		}
		
		
	}

	public void cleanup(Field field) {
		
		synchronized (pool) {
			Vector<Client> remove = new Vector<Client>();
			
			for (Client c : used) {
			
				if (c.getAgent() == null) {
					remove.add(c);
					continue;
				}
				
				if (!c.getAgent().isAlive()) {

					removed.add(c.getAgent());
					
					c.setAgent(null);
					remove.add(c);
				}
				
			}
			
			used.removeAll(remove);
			pool.addAll(remove);
			
			for (Agent a : removed) {
				
				BodyPosition pos = field.getPosition(a);
				
				field.removeBody(a);
				
				if (a.getFlag() != null && pos != null) {
					field.putBodyCloseTo(a.getFlag(), new BodyPosition(pos.getX(), pos.getY()));
				}
			}

			removed.clear();
			
		}
	}
	
	
	public List<Agent> move(Field field) {
		
		Vector<Agent> moved = new Vector<Agent>();
		
		synchronized (pool) {

			for (Client c : used) {
			
				if (c.getAgent() != null) {
					if (c.getAgent().move(field))
						moved.add(c.getAgent());
				}
				
			}

		}
	
		return moved;
	}
	
	public Client findById(int id) {
		
		synchronized (pool) {
		
			for (Client cl : used) {
				
				if (cl.getAgent() != null && cl.getAgent().getId() == id)
					return cl;
				
			}
			
			return null;
		}
		
	}
	
	public int getActiveFlagsCount() {
		return flags.size();
	}
	
	public String toString() {
		return name;
	}
	
	private int getUniqueId() {
		
		while (true) {
		
		int id = (int) (Math.random() * MAX_ID);
		
			if (!allocatedIds.contains(id)) {
				allocatedIds.add(id);
				return id;
			}
		
		}

	}
	
	private Vector<TeamListener> listeners = new Vector<TeamListener>();

	public void addListener(TeamListener listener) {
		synchronized (listeners) {
			listeners.add(listener);
		}
		
	}
	
	public void removeListener(TeamListener listener) {
		synchronized (listeners) {
			listeners.remove(listener);
		}
	}
	
	private void clientConnected(Client client) {
		
		synchronized (listeners) {
			for (TeamListener l : listeners) {
				try {
					l.clientConnect(this, client);
				} catch (Exception e) {e.printStackTrace();}
				
			}
		}
	}
	
	private void clientDisconnected(Client client) {
		
		synchronized (listeners) {
			for (TeamListener l : listeners) {
				try {
					l.clientDisconnect(this, client);
				} catch (Exception e) {e.printStackTrace();}
				
			}
		}
	}
	
	private void scoreChange(int score) {
		
		synchronized (listeners) {
			for (TeamListener l : listeners) {
				try {
					l.scoreChange(this, score);
				} catch (Exception e) {e.printStackTrace();}
				
			}
		}
	}
}
