package utils;

import java.util.LinkedList;
import java.util.PriorityQueue;

import fri.pipt.protocol.Neighborhood;
import fri.pipt.protocol.Position;

class Plan implements Comparable<Plan> {
	Plan parent;
	int f;
	double h;
	Position p;

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
		return Math.sqrt(Math.pow(p1.getX() - p2.getX(), 2)
				+ Math.pow(p1.getY() - p2.getY(), 2));
	}

	public int length() {
		if (parent == null)
			return 1;
		return parent.length() + 1;
	}

	public static Plan createPlan(KnownArena knownArena, Position pos,
			int maxLength, boolean force) {
		PriorityQueue<Plan> pq = new PriorityQueue<Plan>();

		if ((knownArena.arena.get(pos) != Neighborhood.EMPTY && !force)
				|| knownArena.arena.get(pos) == null) {
			return null;
		}

		pq.add(new Plan(pos));

		Plan par = null;
		Position temPos = null;
		while (!pq.isEmpty()) {
			par = pq.poll();
			if (par.p.equals(knownArena.curentPosition))
				return par;
			if (par.length() > maxLength)
				continue;
			temPos = new Position(par.p.getX() + 1, par.p.getY());
			if (canMove(knownArena, temPos))
				pq.add(new Plan(par, temPos, knownArena.curentPosition));
			temPos = new Position(par.p.getX() - 1, par.p.getY());
			if (canMove(knownArena, temPos))
				pq.add(new Plan(par, temPos, knownArena.curentPosition));
			temPos = new Position(par.p.getX(), par.p.getY() + 1);
			if (canMove(knownArena, temPos))
				pq.add(new Plan(par, temPos, knownArena.curentPosition));
			temPos = new Position(par.p.getX(), par.p.getY() - 1);
			if (canMove(knownArena, temPos))
				pq.add(new Plan(par, temPos, knownArena.curentPosition));
		}

		return null;
	}

	public static boolean canMove(KnownArena knownArena, Position pos) {
		if (knownArena.arena.get(pos) != Neighborhood.EMPTY
				|| knownArena.arena.get(pos) == null) {
			return false;
		}
		return true;
	}
}
