package utils.world;

public class AlliesAgent implements Comparable<AlliesAgent> {
	KnownPosition currentPosition;
	KnownPosition planedPosition;
	private int ID;
	public int getID() {
		return ID;
	}

	public void setID(int iD) {
		ID = iD;
	}

	public boolean hasFlag() {
		return hasFlag;
	}

	public void setHasFlag(boolean hasFlag) {
		this.hasFlag = hasFlag;
	}

	private boolean hasFlag;
	
	public AlliesAgent(int ID) {
		this.ID = ID;
	}

	@Override
	public int compareTo(AlliesAgent arg0) {
		if (this.hasFlag()) return -1;
		if (arg0.hasFlag()) return 1;
		return this.getID() - arg0.getID();
	}
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof AlliesAgent ? this.getID() == ((AlliesAgent) obj).getID() : false;
	}
}
