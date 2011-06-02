package utils.world;

public class AlliesAgent implements Comparable<AlliesAgent> {
	private KnownPosition currentPosition;
	private KnownPosition seenPosition;
	
	private int top;
	private int botom;
	
	public KnownPosition getSeenPosition() {
		return seenPosition;
	}

	private static boolean alternate = true; 
	public void setSeenPosition(KnownPosition seenPosition) {
		
		this.seenPosition = seenPosition;
	}
	
	public int getNextIndex(int size) {
		if (alternate && this.top < size) {
			alternate ^= this.botom > 5;
			return this.top++;
		} else if (alternate && this.botom > 5) {
			this.botom--;
			return this.botom;
		} else if (!alternate && this.botom > 5) {
			this.botom--;
			alternate ^= true;
			return this.botom;
		} else if (!alternate && this.top < size) {
			alternate ^= true;
			return this.top++;
		}
		return -1;
	}

	public KnownPosition getCurrentPosition() {
		return currentPosition;
	}

	public void setCurrentPosition(KnownPosition currentPosition) {
		this.currentPosition = currentPosition;
	}

	public KnownPosition getPlanedPosition() {
		return planedPosition;
	}

	public void setPlanedPosition(KnownPosition planedPosition) {
		this.planedPosition = planedPosition;
	}

	public byte getState() {
		return state;
	}

	public void setState(byte state) {
		this.state = state;
	}

	private KnownPosition planedPosition;
	
	private byte state;
	
	
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
	
	public AlliesAgent(int ID, int tb) {
		this.top = tb-1 >= 0 ? tb-1 : 0;
		this.botom = tb-2 >= 0 ? tb-2 : 0;
		this.ID = ID;
	}

	@Override
	public int compareTo(AlliesAgent arg0) {
		if (this.hasFlag()) return -1;
		if (arg0.hasFlag()) return 1;
		return this.getID() - arg0.getID();
	}
	
	@Override
	public String toString() { return "Agent : " + this.getID();};
	
	@Override
	public boolean equals(Object obj) {
		return obj instanceof AlliesAgent ? this.getID() == ((AlliesAgent) obj).getID() : false;
	}
}
