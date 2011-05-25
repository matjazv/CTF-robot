package utils.world;

import java.util.HashSet;

import javax.swing.text.StyledEditorKit.BoldAction;

public class Group {
	private static HashSet<Long> conectedGroups;
	private HashSet<Long> neighbors;
	public static long counter = 1;
	public static final long FIRST = 0;
	
	private long group;
	
	public static void init () {
		conectedGroups = new HashSet<Long>();
		conectedGroups.add(FIRST);
	}
	
	public void setGroup(long group) {
		this.group = group;
	}
	
	public boolean isConected() {
		return conectedGroups.contains(this.group);
	}
	
	/*public void setGroup(long group) {
		this.group = group;
	}*/

	public long getGroup() {
		return group;
	}
	
	public void setGroup(int group) {
		this.group = group;
	}

	public Group () {
		this.neighbors = new HashSet<Long>();
		this.group =  counter;
		counter++;
	}
	
	public Group (int group) {
		
		this.group = group;
	}
	
	@Override
	public boolean equals (Object o) {
		if (o instanceof Group) {
			return (this.group == ((Group)o).getGroup());
		}
		else return false;
	}
	
}
