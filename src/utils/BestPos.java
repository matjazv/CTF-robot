package utils;

import fri.pipt.protocol.Position;

public class BestPos implements Comparable<BestPos> {

	public Position p;
	int mark;
	double distance;
	
	static final int minMark = 0;
	
	public BestPos(Position p) {
		this.p = p;
	}
	
	@Override
	public int compareTo(BestPos o) {
		return this.mark/(this.distance > 3 ? 2 : 1) - o.mark/(o.distance> 3 ? 2 : 1);
	}

	public boolean eval(KnownArena knownArena) {
		this.mark = knownArena.getUnknown(p);
		if (this.mark <= minMark) return true;
		distance = Math.sqrt(Math.pow(knownArena.curentPosition.getX() - p.getX(), 2)
				+ Math.pow(knownArena.curentPosition.getY() - p.getY(), 2));
		return false;
	}
	
}
