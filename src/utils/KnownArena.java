package utils;

import java.util.HashMap;

import fri.pipt.protocol.Neighborhood;
import fri.pipt.protocol.Position;

public class KnownArena {
	private Position curentPosition;
	private HashMap<Position, Integer> arena;

	public KnownArena(Neighborhood nbh) {
		this.curentPosition = getRelativePosition(nbh);
		this.arena = new HashMap<Position, Integer>();
		this.updateArena(nbh);

	}

	public void updateArena(Neighborhood n) {
		for (int x = -n.getSize(); x <= n.getSize(); x++) {
			for (int y = -n.getSize(); y <= n.getSize(); y++) {
				if (n.getCell(x, y) == Neighborhood.OTHER)
					this.arena.put(new Position(x + this.curentPosition.getX(), y + this.curentPosition.getY()), Neighborhood.EMPTY);
				else
					this.arena.put(new Position(x + this.curentPosition.getX(), y + this.curentPosition.getY()), n.getCell(x, y));
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

}
