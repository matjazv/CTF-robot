package utils.world;

import fri.pipt.protocol.Message.Direction;

public class Decision implements Comparable<Decision> {

	private float weight;

	private Direction direction;

	public float getWeight() {
		return weight;
	}

	public void setWeight(float weight) {
		this.weight = weight;
	}
	
	public void multiplyWeight(float f) {
		this.weight *= f;
	}
	
	public Direction getDirection() {
		return direction;
	}

	public void setDirection(Direction direction) {
		this.direction = direction;
	}

	public Decision(float weight, Direction direction) {
		super();
		this.weight = weight;
		this.direction = direction;
	}

	@Override
	public int compareTo(Decision o) {
		if (weight < o.weight)
			return -1;

		if (weight > o.weight)
			return 1;

		return 0;
	}

	public String toString() {
		return String.format("%s (%.2f)", direction.toString(), weight);
	}
	
}