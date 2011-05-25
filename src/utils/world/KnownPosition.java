package utils.world;

import java.awt.Color;

import fri.pipt.protocol.Neighborhood;
public class KnownPosition implements Comparable<KnownPosition> {
	
	public enum CompareType {
		GROUP
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
	
	public long getGroup() {
		return group.getGroup();
	}
	
	public Group getGroupObject() {
		return group;
	}
	
	public void setGroup(Group group) {
		this.group = group;
	}

	public void setGroup(long group) {
		if (this.group == null) this.group = new Group();
		this.group.setGroup(group);
	}
	
	public void setGroup() {
		this.group = new Group();
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
    		int  group = 1 + (int)(this.group.getGroup()%15);
    		return new Color(9*group, 120+9*group, 9*group);
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
		case GROUP:
			long l = this.getGroup() - position.getGroup();
			return l < 0 ? -1 : (l == 0 ? 0 : 1); 
		}
		return 0;
	}

	public static void setCompareType(CompareType compareType) {
		KnownPosition.compareType = compareType;
	}
}