/*
 *  AgentField - a simple capture-the-flag simulation for distributed intelligence
 *  Copyright (C) 2011 Luka Cehovin <http://vicos.fri.uni-lj.si/lukacu>
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program. If not, see <http://www.gnu.org/licenses/>. 
 */
package fri.pipt.agent;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.ConcurrentLinkedQueue;

import fri.pipt.agent.sample.SampleAgent;
import fri.pipt.protocol.Message;
import fri.pipt.protocol.Neighborhood;
import fri.pipt.protocol.ProtocolSocket;
import fri.pipt.protocol.Message.Direction;
import fri.pipt.protocol.Message.ReceiveMessage;
import fri.pipt.protocol.Message.StateMessage;

/**
 * The base class for all agents. This class also includes main method that is
 * used to launch the client and handles all low level protocol communication
 * and the lifecycle of the agent.
 * 
 * To run the sample agent type:
 * java -cp bin/ fri.pipt.agent.Agent localhost fri.pipt.agent.sample.SampleAgent
 * 
 * @author lukacu
 * @see SampleAgent
 */
@Membership("default")
public abstract class Agent {

	private static ConcurrentLinkedQueue<Message> inbox = new ConcurrentLinkedQueue<Message>();

	private static ClientProtocolSocket client;

	private static Class<Agent> agentClass = null;

	private static Agent agent = null;

	private static class ClientProtocolSocket extends ProtocolSocket {

		public static enum Status {
			UNKNOWN, REGISTERED, INITIALIZED
		}

		private Status status = Status.UNKNOWN;

		public ClientProtocolSocket(Socket sck) throws IOException {
			super(sck);

			String team = "default";

			Membership m = agentClass.getAnnotation(Membership.class);

			if (m != null)
				team = m.value();

			sendMessage(new Message.RegisterMessage(team));

		}

		@Override
		protected void handleMessage(Message message) {

			switch (status) {
			case UNKNOWN:
				if (message instanceof Message.AcknowledgeMessage)
					status = Status.REGISTERED;
				break;

			case REGISTERED:
				if (message instanceof Message.InitializeMessage) {

					try {
						
						agent = agentClass.newInstance();

						agent.id = ((Message.InitializeMessage) message)
								.getId();
						try {
							agent.initialize();
						} catch (Exception e) {
							e.printStackTrace();
						}

						status = Status.INITIALIZED;

						sendMessage(new Message.AcknowledgeMessage());

					} catch (Throwable e) {
						e.printStackTrace();
					}

				}

				break;

			case INITIALIZED:
				if (message instanceof Message.StateMessage)
					super.handleMessage(message);

				if ((message instanceof Message.ReceiveMessage)
						|| (message instanceof Message.StateMessage)) {

					synchronized (inbox) {

						inbox.add(message);
						inbox.notifyAll();

					}

				}

				if (message instanceof Message.TerminateMessage) {

					try {
						agent.terminate();
					} catch (Throwable e) {
						e.printStackTrace();
					}

					agent = null;
					status = Status.REGISTERED;

				}

				break;

			}

		}

		public boolean isAlive() {
			return status == Status.INITIALIZED;
		}

		@Override
		protected void onTerminate() {
			super.onTerminate();
			System.exit(0);
		}

	}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws NumberFormatException,
			UnknownHostException, IOException, ClassNotFoundException {

		agentClass = (Class<Agent>) Class.forName(args[1]);

		Socket socket = new Socket(args[0], 5000);

		client = new ClientProtocolSocket(socket);

		Thread messages = new Thread(new Runnable() {

			@Override
			public void run() {

				while (true) {

					synchronized (inbox) {
						while (inbox.isEmpty()) {
							try {
								inbox.wait();
							} catch (InterruptedException e) {
							}
						}
					}

					Message msg = inbox.poll();

					if (agent != null && client.isAlive()) {
						try {

							if (msg instanceof ReceiveMessage) {
								agent.receive(((ReceiveMessage) msg).getFrom(),
										((ReceiveMessage) msg).getMessage());
							} else if (msg instanceof StateMessage) {
								agent.state(((StateMessage) msg).getStamp(),
										((StateMessage) msg).getNeighborhood(),
										((StateMessage) msg).getDirection(),
										((StateMessage) msg).hasFlag());
							}

						} catch (Exception e) {
							e.printStackTrace();
						}
					}

				}
			}
		});
		messages.start();

		try {

			while (true) {

				if (agent != null)
					agent.run();

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}

			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		System.exit(0);

	}

	private int id;

	/**
	 * Called when the agent is no longer needed.
	 */
	public abstract void terminate();

	/**
	 * Called when the agent is initialized.
	 */
	public abstract void initialize();

	/**
	 * Send a message to another agent in the same team. Note that if distance
	 * criteria apply in the game the agent may not receive the message if it is
	 * too far.
	 * 
	 * @param to
	 *            the id of the agent in the same team that should receive this
	 *            message
	 * @param message
	 *            the message as a byte array
	 */
	public final void send(int to, byte[] message) {

		client.sendMessage(new Message.SendMessage(to, message));

	}

	/**
	 * Send a message to another agent in the same team. Note that if distance
	 * criteria apply in the game the agent may not receive the message if it is
	 * too far.
	 * 
	 * @param to
	 *            the id of the agent in the same team that should receive this
	 *            message
	 * @param message
	 *            the message as a string
	 */
	public final void send(int to, String message) {

		client.sendMessage(new Message.SendMessage(to, message.getBytes()));

	}

	/**
	 * Sends a move command to the server. Note that depending on the current
	 * state of the agent, the command may be acknowledged or ignored. You
	 * should check the state of the agent to see the actual
	 * 
	 * @param direction
	 *            the desired direction
	 */
	public final void move(Direction direction) {

		client.sendMessage(new Message.MoveMessage(direction));

	}

	/**
	 * Sends a scan request to the server. The server will respond with the local state
	 * of the environment that will be returned to the agent using the 
	 * {@link #state(int, Neighborhood, Direction, boolean)} callback. 
	 * 
	 * @param stamp the stamp of the request
	 */
	public final void scan(int stamp) {

		client.sendMessage(new Message.ScanMessage(stamp));

	}

	/**
	 * Called when a new message arrives. Should execute quickly.
	 * 
	 * @param from
	 *            the id of the sender agent
	 * @param message
	 *            the message as a byte array
	 */
	public abstract void receive(int from, byte[] message);

	/**
	 * Called as a result of a {@link #scan(int)} instruction
	 * 
	 * @param stamp the stamp of the request
	 * @param neighborhood the neighborhood information
	 * @param direction the direction of the movement
	 * @param hasFlag does this agent carry the flag of the team
	 */
	public abstract void state(int stamp, Neighborhood neighborhood,
			Direction direction, boolean hasFlag);

	/**
	 * The main method of the agent. Should loop while the agent is alive.
	 */
	public abstract void run();

	/**
	 * Checks if the agent is alive.
	 * 
	 * @return true if the agent is alive, false otherwise
	 */
	public final boolean isAlive() {
		return agent == this;
	}

	/**
	 * Returns the id of the local agent
	 * 
	 * @return the id of the agent
	 */
	public final int getId() {
		return id;
	}
}
