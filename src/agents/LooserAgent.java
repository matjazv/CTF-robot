package agents;

import java.util.Arrays;
import java.util.LinkedList;



import utils.agent.AgentState;
import utils.agent.Message;
import utils.world.AlliesAgent;
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
	
	private static LooserAgent AGENT;
	
	public static LooserAgent getAGENT() {
		return AGENT;
	}
	
	private boolean hasFlag;
	
	public boolean hasFlag() {
		return hasFlag;
	}

	private Direction direction;
	private Neighborhood neighborhood;
	private KnownArenaView arenaView;
	
	private Object waitMutex = new Object();

	private Decision left, right, up, down, still;
	
	private Decision[] decisions;
	
	private Decision updateDecisions(Neighborhood n) {
		byte state = AgentState.getCalmState();
		still.setWeight(0.01f);
		down.setWeight(canMove(n, 0, 1, state) ? 1 : 0);
		up.setWeight(canMove(n, 0, -1, state) ? 1 : 0);
		left.setWeight(canMove(n, -1, 0, state) ? 1 : 0);
		right.setWeight(canMove(n, 1, 0, state) ? 1 : 0);
		
		if (utils.agent.AgentState.getReactState() == utils.agent.AgentState.CALM) {
			if(utils.agent.AgentState.getCalmState() == utils.agent.AgentState.EXPLORE) {
				decideOnExplore();
			} else if (utils.agent.AgentState.getCalmState() == utils.agent.AgentState.RETURN)  {
				decideOnReturn();
			}else if (utils.agent.AgentState.getCalmState() == utils.agent.AgentState.SEEK)  {
				decideOnSeek();
			}
	}	
	else if ( utils.agent.AgentState.getReactState() == utils.agent.AgentState.AXIS_NEAR) {
			decideOnAxisNear();
	}
	else if ( (utils.agent.AgentState.getReactState() & utils.agent.AgentState.ALLIES_NEAR) != 0 ) {
			decideOnAlliesNear();
	}
		
		
		
		Arrays.sort(decisions);
		
		return decisions[decisions.length - 1];
		
	}
	
	private void decideOnAlliesNear() {
		if (hasFlag) {
			byte [] message = Message.encodeMessage();
			for (AlliesAgent agent : KnownArena.getARENA().getAllies()) {
				send(agent.getID(), message);
			}
			decideOnReturn();
		} else {
			Planer.setToUpdate(true);
			if(utils.agent.AgentState.getCalmState() == utils.agent.AgentState.EXPLORE) {
				decideOnExplore();
			} else if (utils.agent.AgentState.getCalmState() == utils.agent.AgentState.RETURN)  {
				decideOnReturn();
			}else if (utils.agent.AgentState.getCalmState() == utils.agent.AgentState.SEEK)  {
				decideOnSeek();
			}
			byte [] message = Message.encodeMessage();
			synchronized (KnownArena.getARENA().getAllies()) {
				for (AlliesAgent agent : KnownArena.getARENA().getAllies()) {
					send(agent.getID(), message);
				}
			}
		}
	
	}

	private void decideOnAxisNear() {
	// TODO Auto-generated method stub
	}
	
	private void decideOnSeek() {
		mulDirection(Planer.getPlan());
	}
	
	private void decideOnExplore() {
		mulDirection(Planer.getPlan());
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
		mulDirection(Planer.getPlan());
	}

	@Override
	public void receive(int from, byte[] message) {
		Message.decodeMessage(message, from);
	}
	
	@Override
	public void state(int stamp, Neighborhood neighborhood, Direction direction, boolean hasFlag) {
		synchronized (waitMutex) {
			this.hasFlag = hasFlag;
			if (KnownArena.getARENA() == null) { 
				new KnownArena(neighborhood);
				//arenaView = new KnownArenaView(KnownArena.getARENA());
			} else if ( direction == Direction.NONE ) {
				KnownArena.getARENA().updatePosition(neighborhood, this.direction);

				//arenaView.repaint();
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
			int speed = 5000 / getSpeed();
			while (isAlive()) {
				
				synchronized (waitMutex) {
					scan(0);
					waitMutex.wait();
				
					if (direction == Direction.NONE) {
						direction = updateDecisions(neighborhood).getDirection();
						move(direction);
					}
					//System.out.println(AgentState.getCalmState()); 
				}
				Thread.sleep(speed);
			}
		}
		catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	
	private boolean canMove(Neighborhood n, int x, int y, byte state) {
		if (n.getCell(x, y) == Neighborhood.WALL) return false;
		return true;
		/*switch (state) {
		case AgentState.RETURN:
			return n.getCell(x, y) == Neighborhood.EMPTY || n.getCell(x, y) == Neighborhood.HEADQUARTERS;
		case AgentState.SEEK:
			return n.getCell(x, y) == Neighborhood.EMPTY || n.getCell(x, y) == Neighborhood.FLAG;
		default:
			return n.getCell(x, y) == Neighborhood.EMPTY;		
		}*/
		
	}
	
	public static double wallImportance;
	public static double unAccessibleImportance;
	public static double distanceImportance;
	public static double randomImportance;
	@Override
	public void initialize() {
		left = new Decision(0, Direction.LEFT);
		right = new Decision(0, Direction.RIGHT);
		up = new Decision(0, Direction.UP);
		down = new Decision(0, Direction.DOWN);
		still = new Decision(0, Direction.NONE);
		
		wallImportance = 0.08 + (0.13 * Math.random());
		unAccessibleImportance = 0.02 + (0.08 * Math.random());
		distanceImportance = 0.9 + (0.1 * Math.random());
		randomImportance = 0.98 + (0.02 * Math.random());
		
		decisions = new Decision[] {
			left, right, up, down, still	
		};
		
		AGENT = this;
	}
	
	
}