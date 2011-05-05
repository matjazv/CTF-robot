package utils;

import fri.pipt.protocol.Position;

public class BestPos implements Comparable<BestPos> {

	public Position p;
	double nearWall;
	int mark;
	double distance;
	
	static final int minMark = 0;
	
	public BestPos(Position p) {
		this.p = p;
	}
	
	@Override
	public int compareTo(BestPos o) {
		return (int) -((this.mark*10000/(this.distance+(this.nearWall/o.distance))) - (o.mark*10000/(o.distance+(o.nearWall/o.distance))));
	}

	public boolean eval(KnownArena knownArena) {
		knownArena.getUnknown(this);
		if (this.mark <= minMark) return true;
		distance = Math.sqrt(Math.pow(knownArena.curentPosition.getX() - p.getX(), 2)
				+ Math.pow(knownArena.curentPosition.getY() - p.getY(), 2));
		return false;
	}
	
}
