package utils.agent;

public class AgentState {
	

	public static final byte EXPLORE = 4;
	public static final byte SEEK = 8;
	public static final byte RETURN = 16;
	public static final byte FOLLOW = 32;
	public static final byte ALLIES_NEAR = 2;
	public static final byte AXIS_NEAR = 1;
	public static final byte CALM = 0;
	
	private static byte calmState = EXPLORE;
	private static byte reactState = CALM;
	
	public static byte getCalmState() {
		return calmState;
	}

	public static void setCalmState(byte state) {
		calmState = state;
	}

	public static byte getReactState() {
		return reactState;
	}

	public static void setReactState(byte state) {
		reactState = state;
	}
	
	public static void updateState() {
		
	}
	
}
