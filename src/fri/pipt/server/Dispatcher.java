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
package fri.pipt.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Vector;

import fri.pipt.protocol.Message;
import fri.pipt.protocol.Neighborhood;
import fri.pipt.protocol.ProtocolSocket;
import fri.pipt.protocol.Message.AcknowledgeMessage;
import fri.pipt.protocol.Message.MoveMessage;
import fri.pipt.protocol.Message.RegisterMessage;
import fri.pipt.protocol.Message.ScanMessage;
import fri.pipt.protocol.Message.SendMessage;

public class Dispatcher implements Runnable {

	public static enum Status {UNKNOWN, REGISTERED, USED}
	
	public class Client extends ProtocolSocket {

		private Status status = Status.UNKNOWN;
		
		private Team team;
		
		private Agent agent = null;
		
		private int messages = 0;
		
		public Client(Socket socket)
				throws IOException {
			super(socket);
		}
		
		protected void handleMessage(Message message) {
			
			synchronized (this) {
				messages++;
			}
			
			switch (status) {
			case UNKNOWN: {
				
				if (message instanceof RegisterMessage) {

					team = game.getTeam(((RegisterMessage) message).getTeam());
	
					if (team == null) {
						
						System.out.println("Unknown team: " + ((RegisterMessage) message).getTeam());
						close();
						return;
					}
	
					System.out.println("New client joined team " + team + ": " + this);
					
					sendMessage(new Message.AcknowledgeMessage());
					
					team.addClient(this);
					
					status = Status.REGISTERED;
					
				}
				
				break;
			}
			case REGISTERED: {
				
				if (agent != null && (message instanceof AcknowledgeMessage)) {
					
					status = Status.USED;
					
				}
				
				break;
			}
			case USED: {
				
				if (agent == null)
					return;
				
				if (message instanceof ScanMessage) {
					
					Neighborhood n = game.scanNeighborhood(neighborhoodSize, getAgent());
					
					sendMessage(new Message.StateMessage(getAgent().getDirection(), n, agent.getFlag() != null));
					
					return;
				}
				
				if (message instanceof SendMessage) {
					
					int to = ((SendMessage) message).getTo();
					
					if (((SendMessage)message).getMessage() == null || ((SendMessage)message).getMessage().length > maxMessageSize) {
						System.out.printf("Message from %d to %d rejected: too long", agent.getId(), to);
						return;
					}
					
					game.message(team, agent.getId(), to, ((SendMessage)message).getMessage());						
					
					return;
				}				

				if (message instanceof MoveMessage) {
										
					game.move(team, agent.getId(), ((MoveMessage) message).getDirection());
					
					return;
				}	
				
			}
			}

			
		}

		public Agent getAgent() {
			return agent;
		}

		public Team getTeam() {
			return team;
		}
		
		public void setAgent(Agent agent) {
		
			if (this.agent != null)
				sendMessage(new Message.TerminateMessage());
			
			this.agent = agent;
			
			agent(agent);
			
			if (agent == null)
				return;
			
			sendMessage(new Message.InitializeMessage(agent.getId(), maxMessageSize, game.getSpeed()));
			
		}

		@Override
		protected void onTerminate() {
			
			if (team != null)
				team.removeClient(this);
			
			synchronized (clients) {
				clients.remove(this);
			}
			
			
		}
		
		public String toString() {
			
			return getRemoteAddress() + ":" + getRemotePort(); 
			
		}
		
		public int queryMessageCounter() {
			synchronized (this) {
				int tmp = messages;
				messages = 0;
				return tmp;
			}
		}

		private Vector<ClientListener> listeners = new Vector<ClientListener>();

		public void addListener(ClientListener listener) {
			synchronized (listeners) {
				listeners.add(listener);
			}
			
		}
		
		public void removeListener(ClientListener listener) {
			synchronized (listeners) {
				listeners.remove(listener);
			}
		}
		
		private void agent(Agent agent) {
			
			synchronized (listeners) {
				for (ClientListener l : listeners) {
					try {
						l.agent(this, agent);
					} catch (Exception e) {e.printStackTrace();}
					
				}
			}
		}
		
		private void transfer(int messages) {
			
			synchronized (listeners) {
				for (ClientListener l : listeners) {
					try {
						l.transfer(this, messages);
					} catch (Exception e) {e.printStackTrace();}
					
				}
			}
		}
		
		public void traffic() {
			
			synchronized (this) {
				transfer(messages);
				
				messages = 0;				
			}

		}
		
	}
	
	private HashSet<Client> clients = new HashSet<Client>();
	
	private ServerSocket socket;
	
	private Game game;
	
	private int maxMessageSize = 1024;
	
	private int neighborhoodSize = 5;	
	
	public Dispatcher(int port, Game game) throws IOException {
		
		socket = new ServerSocket(port);
		
		this.game = game;
		
		this.maxMessageSize = game.getProperty("message.size", 256);

		this.neighborhoodSize = game.getNeighborhoodSize();
		
	}

	@Override
	public void run() {
		
		Thread traffic = new Thread(new Runnable() {
			
			@Override
			public void run() {
				
				while (true) {
					synchronized (clients) {
					
						for (Client cl : clients) {
							cl.traffic();
						}
						
					}
				
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {

					}
				
				}
			}
		});
		traffic.setDaemon(true);
		traffic.start();
		
		
		while (true) {
			try {
				Socket sck = socket.accept();
				
				synchronized (clients) {
					clients.add(new Client(sck));
				}
				
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		
		
		}
	}
	

	
}
