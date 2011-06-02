package utils.world;

import java.awt.Color;

import agents.LooserAgent;


import fri.pipt.protocol.Neighborhood;
public class KnownPosition implements Comparable<KnownPosition> {
	
	public enum CompareType {
		EXPLORE, PLAN, PLAN_MULTI
	}
	
	private static CompareType compareType;
	
	public static CompareType getCompareType() {
		return compareType;
	}

	public KnownPosition (int x, int y, int type) {
		if (cells == 0) cells = (2*KnownArena.getARENA().getnSize()*KnownArena.getARENA().getnSize() + 1) * (2*KnownArena.getARENA().getnSize()*KnownArena.getARENA().getnSize() + 1);
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

	public int getX() {
		return x;
	}

	private int y;

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
			double tdistance = 0;
			double distance = 0;
			synchronized (KnownArena.getARENA().getAllies()) {
				
				if (KnownArena.getARENA().getAllies() != null && KnownArena.getARENA().getAllies().size() > 0) {
					int xc= 0;
					int yc= 0;
					int xp = 0;
					int yp = 0;
					int ic = 0;
					int i = 0;
					for (AlliesAgent agent : KnownArena.getARENA().getAllies()) {
						if (agent.getSeenPosition() != null) {
							xc += agent.getSeenPosition().getX();
							yc += agent.getSeenPosition().getY();
							ic++;
						}
						if (agent.getPlanedPosition() != null) {
							xp += agent.getPlanedPosition().getX();
							yp += agent.getPlanedPosition().getX();
							i++;
						}
					} 
					tdistance = Math.abs(getX()-(xc*1.0)/ic) + Math.abs(getY()-(yc*1.0)/ic) + 0.5*(Math.abs(getX()-(xp*1.0)/i) + Math.abs(getY()-(yp*1.0)/i) );
					distance = Math.abs(position.getX()-(xc*1.0)/ic) + Math.abs(position.getY()-(yc*1.0)/ic) + 0.5*(Math.abs(position.getX()-(xp*1.0)/i) + Math.abs(position.getY()-(yp*1.0)/i) );
					double deltaD = distance - position.distance;
					double delta = tdistance - this.distance;
					if (deltaD <= 0) return  -1;
					if (delta <= 0) return 1;
				}
			} 
			if (this.mark == 0 || !this.isAccesible() || KnownArena.getARENA().getForbiden().contains(this)) return 1;
			if (position.mark == 0 || !position.isAccesible()  || KnownArena.getARENA().getForbiden().contains(position)) return -1;
			switch(LooserAgent.princip) {
			case 1:
				return ((position.mark - this.mark)) < 0 ? -1 : ((position.mark - this.mark) == 0 ? 0 : 1);
			case 2:
				return (int) -((this.mark*10000/(this.distance+(this.nearWall/position.distance))) - (position.mark*10000/(position.distance+(position.nearWall/position.distance))));
				
			}
			case PLAN:
			return this.getF() + this.getH() - position.getF() - position.getH();
		case PLAN_MULTI:
			return this.getF() - position.getF();
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
	
	private int unknown;
	private int unAccessible;
	private int walls;
	private int distance;
	public void setDistance(int distance) {
		this.distance = distance;
	}

	private double mark;
	private static double cells = 0;
	
	public boolean eval() {
		this.unknown = 0;
		this.unAccessible = 0;
		if ( getType() != Neighborhood.EMPTY ) return true;
		int nSize = KnownArena.getARENA().getnSize();
		int mX = this.getX() + nSize;
		int mY = this.getY() + nSize;
		for (int x = this.getX() - nSize; x <= mX; x++) {
			for (int y = this.getY() - nSize; y <= mY; y++) {
				if (KnownArena.getARENA().getPositionAt(x,y) == null ) {
					this.unknown++;
				} else if (KnownArena.getARENA().getPositionAt(x,y).getType() == Neighborhood.EMPTY) {
					this.unAccessible += KnownArena.getARENA().getPositionAt(x,y).isAccesible() ? 1 : 0;
				} else if (KnownArena.getARENA().getPositionAt(x,y).getType() == Neighborhood.WALL) {
					this.walls++;
				}
			}
		}
		//distance = distance(this, KnownArena.getARENA().getCurentPosition());
		this.mark = 1000 * this.unknown / cells
				* (1 - (this.walls / cells) * LooserAgent.wallImportance)
				* (1 - (this.unAccessible / cells) * LooserAgent.unAccessibleImportance)
				* LooserAgent.randomImportance
				/ ((this.distance / (cells * 0.05)) * LooserAgent.distanceImportance);
		return unknown == 0;
	}

	public static void setCompareType(CompareType compareType) {
		KnownPosition.compareType = compareType;
	}

	

	
	
}
