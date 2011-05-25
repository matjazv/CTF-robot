package utils.world;

import java.util.HashMap;
import java.util.Vector;


import fri.pipt.protocol.Neighborhood;
import fri.pipt.protocol.Message.Direction;

public class KnownArena {
	private HashMap<KnownPosition, KnownPosition> arena;
	//private HashMap<Integer, KnownPosition> landmarks;
	
	KnownPosition curentPosition;
	
	private int nSize;
	
	public KnownArena (Neighborhood neighborhood) {
		this.nSize = neighborhood.getSize();
		this.curentPosition = getRelativePosition(neighborhood);
		this.arena = new HashMap<KnownPosition, KnownPosition>();
		curentPosition.setGroup();
		setPositionAt(curentPosition.getX(), curentPosition.getY(), curentPosition);
		for (int x = -neighborhood.getSize(); x <= neighborhood.getSize(); x++) {
			for (int y = -neighborhood.getSize(); y <= neighborhood.getSize(); y++) {
				updateCell(x+curentPosition.getX(), y+curentPosition.getY(), neighborhood.getCell(x, y));
			}
		}
	}
	
	public void updateCell(int x, int y, int type) {
		if (getPositionAt(x, y) != null) return;
		
		KnownPosition tempPositionI = new KnownPosition(x,y,type);
		setPositionAt(x, y, tempPositionI);
		if (tempPositionI.getType() == Neighborhood.EMPTY) {
			
			Vector<Group> neighborGroups = new Vector<Group>();
			
			
			KnownPosition tempPositionN = getPositionAt(x+1, y);
			if (tempPositionN != null && tempPositionN.getType() == Neighborhood.EMPTY) neighborGroups.add(tempPositionN.getGroup());
			tempPositionN = getPositionAt(x-1, y);
			if (tempPositionN != null && tempPositionN.getType() == Neighborhood.EMPTY) neighborGroups.add(tempPositionN.getGroup());
			tempPositionN = getPositionAt(x, y+1);
			if (tempPositionN != null && tempPositionN.getType() == Neighborhood.EMPTY) neighborGroups.add(tempPositionN.getGroup());
			tempPositionN = getPositionAt(x, y-1);
			if (tempPositionN != null && tempPositionN.getType() == Neighborhood.EMPTY) neighborGroups.add(tempPositionN.getGroup());
			
			tempPositionI.setGroup();
			neighborGroups.add(tempPositionI.getGroup());
			Group.connect(neighborGroups);
			
		}
	}
	
	private KnownPosition getRelativePosition(Neighborhood neighborhood) {
		for (int x = -1; x <= 1; x++) {
			for (int y = -1; y <= 1; y++) {
				if (neighborhood.getCell(x, y) == Neighborhood.HEADQUARTERS) {
					return new KnownPosition(-x, -y, Neighborhood.EMPTY);
				}
			}
		}
		return null;
	}

	public void updatePosition(Neighborhood neighborhood, Direction direction) {
		switch (direction) {
		case UP:
			curentPosition.setY(curentPosition.getY()-1);
			updateDirection(0, -1, neighborhood);
			break;
		case DOWN:
			curentPosition.setY(curentPosition.getY()+1);
			updateDirection(0, 1, neighborhood);
			break;
		case LEFT:
			curentPosition.setX(curentPosition.getX()-1);
			updateDirection(-1, 0, neighborhood);
			break;
		case RIGHT:
			curentPosition.setX(curentPosition.getX()+1);
			updateDirection(1, 0, neighborhood);
			break;
		case NONE:
		}
	}
	
	private void updateDirection(int dx, int dy, Neighborhood neighborhood) {
		int mx = curentPosition.getX() + (dx != 0 ? dx : 1) * this.nSize;
		int my = curentPosition.getY() + (dy != 0 ? dy : 1) * this.nSize;
		
		for (int x = curentPosition.getX() + (dx != 0 ? dx : -1) * this.nSize; x <= mx; x++) {
			for (int y = curentPosition.getY() + (dy != 0 ? dy : -1) * this.nSize; y <= my; y++) {
				this.updateCell(x, y, neighborhood.getCell(x-curentPosition.getX(), y-curentPosition.getY()));
			}
		}
	}
	
	public void setPositionAt(int x, int y, KnownPosition position) {
		this.arena.put(new KnownPosition(x,y,0), position);
	}
	
	public KnownPosition getPositionAt(int x, int y) {
		return this.arena.get(new KnownPosition(x,y,0));
	}
}
