package utils;

import java.util.HashMap;

import fri.pipt.protocol.Neighborhood;
import fri.pipt.protocol.Position;
import fri.pipt.protocol.Message.Direction;

public class KnownArena {
	public Position curentPosition;
	public HashMap<Position, Integer> arena;
	public HashMap<Integer, Position> landmarks;
	public int nSize;

	public KnownArena(Neighborhood nbh) {
		this.nSize = nbh.getSize();
		this.curentPosition = getRelativePosition(nbh);
		this.arena = new HashMap<Position, Integer>();
		this.landmarks = new HashMap<Integer, Position>();
		this.updateArena(nbh);

	}

	public void updateArena(Neighborhood n) {
		// System.out.println(n.toString());
		for (int x = -n.getSize(); x <= n.getSize(); x++) {
			for (int y = -n.getSize(); y <= n.getSize(); y++) {
				if (n.getCell(x, y) == Neighborhood.OTHER
						|| !(n.getCell(x, y) == Neighborhood.WALL
								|| n.getCell(x, y) == Neighborhood.EMPTY
								|| n.getCell(x, y) == Neighborhood.HEADQUARTERS
								|| n.getCell(x, y) == Neighborhood.OTHER_HEADQUARTERS
								|| n.getCell(x, y) == Neighborhood.FLAG || n
								.getCell(x, y) == Neighborhood.OTHER_FLAG))
					this.arena
							.put(new Position(x + this.curentPosition.getX(), y
									+ this.curentPosition.getY()),
									Neighborhood.EMPTY);
				else if (n.getCell(x, y) != -100000) {
					this.arena.put(new Position(x + this.curentPosition.getX(),
							y + this.curentPosition.getY()), n.getCell(x, y));
					if (n.getCell(x, y) == Neighborhood.FLAG || n.getCell(x, y) == Neighborhood.HEADQUARTERS) {
						this.landmarks.put(n.getCell(x, y), new Position(x
								+ this.curentPosition.getX(), y
								+ this.curentPosition.getY()));
					}
				}
			}
		}

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
	
	public int getUnknown(Position p) {
		if (this.arena.get(p) == null) return 0;
		int res = 0;
		int mX = p.getX() + nSize;
		int mY = p.getY() + nSize;
		for (int x = p.getX() - nSize; x <= mX; x++) {
			for (int y = p.getY() - nSize; y <= mY; y++) {
				p.setX(x);
				p.setY(y);
				if (arena.get(p) == null) res++;
			}
		}
		return res;
	}

}
