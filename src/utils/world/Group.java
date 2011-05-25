package utils.world;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Vector;


public class Group {
	private static HashSet<Group> connectedGroups;
	private HashSet<Group> neighbors;
	public static boolean FIRST = true;
	
	public boolean isConnected() {
		return connectedGroups.contains(this);
	}
	
	public static void connect(Vector<Group> neighborGroups) {
		
		Collections.sort(neighborGroups, new Comparator<Group> () {
			@Override
			public int compare(Group arg0, Group arg1) {
				if (arg0.getNeighbors() == connectedGroups) return -1;
				if (arg1.getNeighbors() == connectedGroups) return 1;
				return arg1.getNeighbors().size() - arg0.getNeighbors().size();
			}});
		
		HashSet<Group> temp = neighborGroups.get(0).getNeighbors();
		for (Group g : neighborGroups) {
			if (g.getNeighbors() == temp) continue;
			for (Group gt : g.getNeighbors()) if (gt != g) gt.setNeighbors(temp);
			temp.addAll(g.getNeighbors());
			g.setNeighbors(temp);
		}
	}

	public HashSet<Group> getNeighbors() {
		return neighbors;
	}

	public void setNeighbors(HashSet<Group> neighbors) {
		this.neighbors = neighbors;
	}

	public Group () {
		if (FIRST) {
			FIRST = false;
			connectedGroups = new HashSet<Group>();
			this.neighbors = connectedGroups;
		}
		else {
			this.neighbors = new HashSet<Group>();
		}
		this.neighbors.add(this);
	}

	
}
