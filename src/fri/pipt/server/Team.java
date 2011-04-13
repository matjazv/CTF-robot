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

		public Flag(int tile, Team team) {
			super(tile, team);
		}
		
	}

	public static class Headquarters extends TeamBody {

		private Flag flag = null;
		
		public Headquarters(int tile, Team team) {
			super(tile, team);
		}

		public boolean hasFlag() {
			return flag != null;
		}
		
		public void putFlag(Flag flag) {
			
			if (flag != null && flag.getTeam() == getTeam()) {
				this.flag = flag;
			}
			
		}
		
	}
	
	private HashSet<Client> used = new HashSet<Client>();
	
	private ConcurrentLinkedQueue<Client> pool = new ConcurrentLinkedQueue<Client>();
	
	private LinkedList<Agent> removed = new LinkedList<Agent>();
	
	private HashSet<Integer> allocatedIds = new HashSet<Integer>();
	
	private String name;
	
	private Headquarters hq;
	
	private Flag flag;

	private Color color;
	
	public Team(String name, Color color) {
		
		this.name = name;
		this.color = color;
		
		hq = new Headquarters(Arena.TILE_HEADQUARTERS, this);
		flag = new Flag(Arena.TILE_FLAG, this);
		
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
	
	public Flag getFlag() {
		
		return flag;
		
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
		
		synchronized (pool) {
			
			pool.add(client);
			
		}


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
	
	
	public void move(Field field) {
		
		synchronized (pool) {

			for (Client c : used) {
			
				if (c.getAgent() != null) {
					c.getAgent().move(field);
				}
				
			}

		}
		
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
	
}
