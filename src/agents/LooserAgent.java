package agents;

import java.util.Arrays;
import java.util.LinkedList;

import utils.world.Decision;
import utils.world.KnownArena;
import utils.world.KnownArenaView;
import utils.world.KnownPosition;
import utils.world.Planer;
import fri.pipt.agent.Agent;
import fri.pipt.agent.Membership;
import fri.pipt.protocol.Neighborhood;
import fri.pipt.protocol.Message.Direction;

// Run: java -cp bin fri.pipt.agent.Agent localhost fri.pipt.agent.sample.SampleAgent

@Membership("samples")
public class LooserAgent extends Agent {
	private static enum AgentState {
		EXPLORE, SEEK, RETURN
	}
	
	private static enum AgentReactState {
		CALM, ALLIES_NEAR, AXIS_NEAR
	}
	
	private AgentState state = AgentState.EXPLORE;
	private AgentReactState react = AgentReactState.CALM;
	
	
	private Direction direction;
	private Neighborhood neighborhood;
	private KnownArenaView arenaView;
	
	private Object waitMutex = new Object();

	private Decision left, right, up, down, still;
	
	private Decision[] decisions;
	
	private Decision updateDecisions(Neighborhood n, AgentState state) {
		
		still.setWeight(0.01f);
		down.setWeight(canMove(n, 0, 1, state) ? 1 : 0);
		up.setWeight(canMove(n, 0, -1, state) ? 1 : 0);
		left.setWeight(canMove(n, -1, 0, state) ? 1 : 0);
		right.setWeight(canMove(n, 1, 0, state) ? 1 : 0);
		
		switch (this.react) {
		case CALM:
			switch (state) {
			case EXPLORE:
				decideOnExplore();
				break;
			case RETURN: {
				decideOnReturn();
				break;
			}
			case SEEK: {
				decideOnSeek();
				break;
			}
			}
			break;
		
		case AXIS_NEAR:
			decideOnAxisNear();
			break;
		case ALLIES_NEAR:
			decideOnAlliesNear();
			break;
		}
		
		
		
		Arrays.sort(decisions);
		
		return decisions[decisions.length - 1];
		
	}
	
	private void decideOnAlliesNear() {
	// TODO Auto-generated method stub
	
}

	private void decideOnAxisNear() {
	// TODO Auto-generated method stub
	}
	
	private void decideOnSeek() {
	
	}
	
	private void decideOnExplore() {
		mulDirection(Planer.getExplorePlan());
	}
	
	private void mulDirection(LinkedList<KnownPosition> plan) {
		KnownPosition current;
		KnownPosition next;
		if (plan.isEmpty()) {
			return;
		} else if (plan.size() < 2) {
			plan.pollFirst();
			return;
		} else {
			current = plan.pollFirst();
			//System.out.println(current.toString());
			next = plan.getFirst();
			//System.out.println(next.toString());
		}
		if (next.getX() - current.getX()  == 1) {
			this.right.multiplyWeight(2);
			//System.out.println("DESNO");
		} else if (next.getX() - current.getX()  == -1) {
			this.left.multiplyWeight(2);
			//System.out.println("LEVO");
		} else if (next.getY() - current.getY()  == 1) {
			this.down.multiplyWeight(2);
			//System.out.println("DOL");
		} else if (next.getY() - current.getY()  == -1) {
			this.up.multiplyWeight(2);
			//System.out.println("GOR");
		}
	}
	
	private void decideOnReturn() {
	}

	@Override
	public void receive(int from, byte[] message) {
		//String msg = new String(message);
	}
	
	@Override
	public void state(int stamp, Neighborhood neighborhood, Direction direction, boolean hasFlag) {
		synchronized (waitMutex) {
			
			if (KnownArena.getARENA() == null) { 
				new KnownArena(neighborhood);
				//arenaView = new KnownArenaView(KnownArena.getARENA());
			} else if ( direction == Direction.NONE ) {
				KnownArena.getARENA().updatePosition(neighborhood, this.direction);
			}
		
			this.neighborhood = neighborhood;
			this.direction = direction;
		
		/*
		if (knownArena.landmarks.get(Neighborhood.FLAG) != null) {
			state = AgentState.SEEK;
		}
		if (state != AgentState.RETURN && hasFlag) {
			state = AgentState.RETURN;
		}*/
		
			
		
			waitMutex.notify();
		}
	}

	@Override
	public void terminate() {

	}



	@Override
	public void run() {
		try {
			Thread.sleep(200);
		
			while (isAlive()) {
				
				synchronized (waitMutex) {
					scan(0);
					waitMutex.wait();
				
					if (direction == Direction.NONE) {
						//arenaView.repaint();
						direction = updateDecisions(neighborhood, state).getDirection();
						move(direction);
					}
					waitMutex.notify();
				}
				Thread.sleep(100);
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	
	private boolean canMove(Neighborhood n, int x, int y, AgentState state) {
		
		switch (state) {
		case RETURN:
			return n.getCell(x, y) == Neighborhood.EMPTY || n.getCell(x, y) == Neighborhood.HEADQUARTERS;
		case SEEK:
			return n.getCell(x, y) == Neighborhood.EMPTY || n.getCell(x, y) == Neighborhood.FLAG;
		default:
			return n.getCell(x, y) == Neighborhood.EMPTY;		
		}
		
	}

	@Override
	public void initialize() {
		left = new Decision(0, Direction.LEFT);
		right = new Decision(0, Direction.RIGHT);
		up = new Decision(0, Direction.UP);
		down = new Decision(0, Direction.DOWN);
		still = new Decision(0, Direction.NONE);
		
		decisions = new Decision[] {
			left, right, up, down, still	
		};
	}
	
	
}