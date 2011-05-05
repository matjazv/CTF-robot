package utils;


import java.util.HashSet;
import java.util.PriorityQueue;

import fri.pipt.protocol.Neighborhood;
import fri.pipt.protocol.Position;

public class Plan implements Comparable<Plan> {
	public Plan parent;
	public int f;
	public double h;
	public Position p;

	public Plan(Position p) {
		this.parent = null;
		this.f = 0;
		this.h = 0;
		this.p = p;
	}

	public Plan(Plan parent, Position p, Position goal) {
		this.f = parent.f + 1;
		this.h = h(p, goal);
		this.p = p;
		this.parent = parent;
	}

	@Override
	public int compareTo(Plan o) {
		return (int) ((this.f + this.h) - (o.f + o.h));
	}

	private double h(Position p1, Position p2) {
		return Math.abs(p1.getX() - p2.getX())
		+ Math.abs(p1.getY() - p2.getY());
	}
	
	@Override
	public int hashCode() {
		p.hashCode();
		return super.hashCode();
	}
 
	public static Plan createPlan(KnownArena knownArena, Position pos,
			int maxLength, boolean force, int timeOut) {
		long t = System.currentTimeMillis();
		PriorityQueue<Plan> pq = new PriorityQueue<Plan>();
		HashSet<Position> visited = new HashSet<Position>();

		if (knownArena.arena.get(pos) == null
				|| (knownArena.arena.get(pos) != Neighborhood.EMPTY && !force)) {
			return null;
		}

		pq.add(new Plan(pos));
		visited.add(pos);
		Plan par = null;
		Position temPos = null;
		while (!pq.isEmpty()) {
			par = pq.poll();
			if (par.p.equals(knownArena.curentPosition))
				return par;
			if (par.f > maxLength)
				continue;
			temPos = new Position(par.p.getX() + 1, par.p.getY());
			if (canMove(knownArena, temPos) && !visited.contains(temPos)) {
				visited.add(temPos);
				pq.add(new Plan(par, temPos, knownArena.curentPosition));
			}
			temPos = new Position(par.p.getX() - 1, par.p.getY());
			if (canMove(knownArena, temPos) && !visited.contains(temPos)) {
				visited.add(temPos);
				pq.add(new Plan(par, temPos, knownArena.curentPosition));
			}
			temPos = new Position(par.p.getX(), par.p.getY() + 1);
			if (canMove(knownArena, temPos) && !visited.contains(temPos)) {
				visited.add(temPos);
				pq.add(new Plan(par, temPos, knownArena.curentPosition));
			}
			temPos = new Position(par.p.getX(), par.p.getY() - 1);
			if (canMove(knownArena, temPos) && !visited.contains(temPos)) {
				visited.add(temPos);
				pq.add(new Plan(par, temPos, knownArena.curentPosition));
			}
			if (System.currentTimeMillis() - t > timeOut) {
				return null;
			}
		}

		return null;
	}

	public static boolean canMove(KnownArena knownArena, Position pos) {
		if (knownArena.arena.get(pos) == null
				|| knownArena.arena.get(pos) != Neighborhood.EMPTY) {
			return false;
		}
		return true;
	}

	public void print() {
		System.out.println(p.toString());
		if (parent != null) {
			parent.print();
		}
		
	}
}
