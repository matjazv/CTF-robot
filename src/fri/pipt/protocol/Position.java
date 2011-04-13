/**
 * 
 */
package fri.pipt.protocol;

import java.io.Serializable;

public class Position implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private int x;
	
	private int y;

	public int getX() {
		return x;
	}
	
	public void setX(int x) {
		this.x = x;
	}
	
	public int getY() {
		return y;
	}
	
	public void setY(int y) {
		this.y = y;
	}
	

	public Position(int x, int y) {
		super();
		this.x = x;
		this.y = y;
	}
	
	public Position(Position p) {
		super();
		this.x = p.x;
		this.y = p.y;
	}
	
	public String toString() {
		return String.format("Position: %d, %d", getX(), getY());
	}
}