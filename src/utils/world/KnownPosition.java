package utils.world;

import java.awt.Color;


import fri.pipt.protocol.Neighborhood;
public class KnownPosition implements Comparable<KnownPosition> {
	
	public enum CompareType {
		EXPLORE, PLAN
	}
	
	private static CompareType compareType;
	
	public static CompareType getCompareType() {
		return compareType;
	}

	public KnownPosition (int x, int y, int type) {
		this.x = x;
		this.y = y;
		setType(type);
	}
	
	private Group group;
	
	public Group getGroup() {
		return group;
	}
	
	public void setGroup() {
		this.group = new Group();
	}
	
	public boolean isAccesible() {
		return this.group == null ? false : this.group.isConnected();
	}

	private int x;
	public void setX(int x) {
		this.x = x;
	}

	public int getX() {
		return x;
	}

	private int y;
	public void setY(int y) {
		this.y = y;
	}

	public int getY() {
		return y;
	}
	
	private int type;

	public void setType(int type) {
		switch(type) {
		case Neighborhood.EMPTY:
		case Neighborhood.OTHER:
			this.type = Neighborhood.EMPTY;
			break;
		case Neighborhood.HEADQUARTERS:
		case Neighborhood.FLAG:
		case Neighborhood.OTHER_FLAG:
		case Neighborhood.OTHER_HEADQUARTERS:
		case Neighborhood.WALL:
			this.type = type;
			break;
		default:
			type = Neighborhood.EMPTY;
		}
		this.type = type;
	}

	public int getType() {
		/*switch(this.Type) {
		case: Neighborhood.EMPTY;
		case: Neighborhood.
		}*/
		return type;
	}
	
	public String toString() {
		return String.format("Position: %d, %d", getX(), getY());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj != null && obj instanceof KnownPosition) 
			return (((KnownPosition) obj).x == x && ((KnownPosition) obj).y == y);
		else return false;
	}

    @Override
    public int hashCode()
    {
        int hash = 17;
        hash = 31*hash + x;
        hash = 31*hash + y;
        return hash;
    }

    public static int distance(KnownPosition p1, KnownPosition p2) {
		return Math.abs(p1.getX() - p2.getX()) + Math.abs(p1.getY()
				- p2.getY());
    }
    
    // FOR VISUALISATION
    
    public Color getColor() {
    	switch (this.type) {
    	case Neighborhood.EMPTY:
    		int  group = this.isAccesible() ? 0 : 50;
    		return new Color(group, 120+group, group);
    	case Neighborhood.WALL:
    		return Color.GRAY;
    	case Neighborhood.HEADQUARTERS:
    		return Color.RED;
    	case Neighborhood.OTHER_HEADQUARTERS:
    		return Color.BLUE;
    	case Neighborhood.OTHER_FLAG:
    		return Color.CYAN;
    	case Neighborhood.FLAG:
    		return Color.PINK;
    	default:
    		return Color.BLACK;
    	}
    }

	@Override
	public int compareTo(KnownPosition position) {
		switch(compareType) {
		case EXPLORE:
			return (int) (((position.mark*2/(position.distance+1)) - (this.mark*2/(this.distance+1))) * (0.9 + 0.2*Math.random()) );
		case PLAN:
			return this.getF() + this.getH() - position.getF() - position.getH();
		}
		return 0;
	}
	
	//FOR PLANING

	private KnownPosition goal;
	private KnownPosition parent;
	private int h;
	
	public void setParent(KnownPosition parent) {
		this.parent = parent;
	}
	
	public KnownPosition getParent() {
		return parent;
	}

	public void setGoal(KnownPosition goal) {
		this.h = -1;
		this.goal = goal;
	}

	private int getH() {
		if (h == -1 && goal != null) {
			h = distance(this, goal);
		}
		return h ;
	}

	private int f;
	
	public int getF() {
		return f;
	}
	
	public void setF(int f) {
		this.f = f;
	}
	// FOR EXPLORE
	
	private int mark;
	private int distance;
	//private int nearWall;
	
	
	
	//private final int minMark = 0;
	public boolean eval() {
		this.mark = isAccesible() ? this.getUnknown(): Integer.MIN_VALUE/4;
		//if (this.mark <= minMark) return true;
		distance = distance(this, KnownArena.getARENA().getCurentPosition());
		return false;
	}
	
	private int getUnknown() {
		int unknown = 0;
		int nSize = KnownArena.getARENA().getnSize();
		int mX = this.getX() + nSize;
		int mY = this.getY() + nSize;
		for (int x = this.getX() - nSize; x <= mX; x++) {
			for (int y = this.getY() - nSize; y <= mY; y++) {
				unknown += KnownArena.getARENA().getPositionAt(x,y) == null ? 1 : (KnownArena.getARENA().getPositionAt(x,y).isAccesible() ? 0 : -1);
			}
		}
		return unknown;
	}

	public static void setCompareType(CompareType compareType) {
		KnownPosition.compareType = compareType;
	}

	

	
	
}
