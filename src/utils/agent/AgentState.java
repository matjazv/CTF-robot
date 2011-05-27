package utils.agent;

public class AgentState {
	

	private static final byte EXPLORE = 4;
	private static final byte SEEK = 8;
	private static final byte RETURN = 16;
	private static final byte FOLLOW = 32;
	private static final byte ALLIES_NEAR = 2;
	private static final byte AXIS_NEAR = 1;
	private static final byte CALM = 0;
	
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
