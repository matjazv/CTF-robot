package utils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Vector;

import fri.pipt.protocol.Neighborhood;
import fri.pipt.protocol.Position;
import fri.pipt.protocol.Message.Direction;

public class KnownArena {
	public Position curentPosition;
	public HashMap<Position, Integer> arena;
	public HashMap<Integer, Position> landmarks;
	public HashSet<Position> visited;
	public Vector<BestPos> toVisit;
	public int nSize;

	public KnownArena(Neighborhood nbh) {
		this.nSize = nbh.getSize();
		this.curentPosition = getRelativePosition(nbh);
		this.arena = new HashMap<Position, Integer>();
		this.landmarks = new HashMap<Integer, Position>();
		this.visited = new HashSet<Position>();
		this.toVisit = new Vector<BestPos>();
		this.updateArena(nbh);

	}

	public void updateArena(Neighborhood n) {
		// System.out.println(n.toString());
		this.visited.add(this.curentPosition);
		for (int x = -n.getSize(); x <= n.getSize(); x++) {
			for (int y = -n.getSize(); y <= n.getSize(); y++) {
				Position temp = new Position(x + this.curentPosition.getX(), y + this.curentPosition.getY());
				if (n.getCell(x, y) == Neighborhood.OTHER
						|| !(n.getCell(x, y) == Neighborhood.WALL
								|| n.getCell(x, y) == Neighborhood.EMPTY
								|| n.getCell(x, y) == Neighborhood.HEADQUARTERS
								|| n.getCell(x, y) == Neighborhood.OTHER_HEADQUARTERS
								|| n.getCell(x, y) == Neighborhood.FLAG || n
								.getCell(x, y) == Neighborhood.OTHER_FLAG))
					this.arena.put(temp,Neighborhood.EMPTY);
				else if (n.getCell(x, y) != -100000) {
					this.arena.put(temp, n.getCell(x, y));
					if (n.getCell(x, y) == Neighborhood.FLAG || n.getCell(x, y) == Neighborhood.HEADQUARTERS) {
						this.landmarks.put(n.getCell(x, y), temp);
					}
					if (!this.visited.contains(temp)) {
						this.toVisit.add(new BestPos(temp));
					}
				}
			}
		}
		
		cleanAndSort();

	}

	private void cleanAndSort() {
		Vector<BestPos> tmp = new Vector<BestPos>();
		for (BestPos bp : this.toVisit) {
			if (bp.eval(this)) {
				tmp.add(bp);
			}
		}
		for (BestPos bp : tmp) {
			this.toVisit.remove(bp);
			this.visited.add(bp.p);
		}
		Collections.sort(this.toVisit);
		
	}

	private Position getRelativePosition(Neighborhood nbh) {
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				if (nbh.getCell(x, y) == Neighborhood.HEADQUARTERS) {
					return new Position(-x, -y);
				}
			}
		}
		return null;
	}

	public void updatePosition(Direction direction) {
		switch (direction) {
		case UP:
			this.curentPosition.setY(this.curentPosition.getY() - 1);
			break;
		case DOWN:
			this.curentPosition.setY(this.curentPosition.getY() + 1);
			break;
		case LEFT:
			this.curentPosition.setX(this.curentPosition.getX() - 1);
			break;
		case RIGHT:
			this.curentPosition.setX(this.curentPosition.getX() + 1);
			break;
		case NONE:
		}
	}

	public void getUnknown(BestPos p) {
		if (this.arena.get(p.p) == null) {
			p.mark = 0;
			return;
		}
		int res = 0;
		int mX = p.p.getX() + nSize;
		int mY = p.p.getY() + nSize;
		Position tp = new Position(0,0);
		for (int x = p.p.getX() - nSize; x <= mX; x++) {
			for (int y = p.p.getY() - nSize; y <= mY; y++) {
				tp.setX(x);
				tp.setY(y);
				if (arena.get(tp) == null) {
					res++;
				}
			}
		}
		p.mark = res;
	}

}
