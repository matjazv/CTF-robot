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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;

import fri.pipt.server.Dispatcher.Client;
import fri.pipt.server.StackLayout.Orientation;

public class ClientsPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final Color selectedBackground, normalBackground;
	
	public static interface SelectionObserver {
		
		public void clientSelected(Client client);
		
		public void teamSelected(Team team);
		
	}
	
	private interface Selectable {
		
		public void select();
		
		public void deselect();
		
	}
	
	private static class ScrollabeListPanel extends JPanel implements Scrollable {

		private static final long serialVersionUID = 1L;

		public ScrollabeListPanel() {
			super(new StackLayout(Orientation.VERTICAL));
		}
		
		@Override
		public Dimension getPreferredScrollableViewportSize() {
			return new Dimension(1000, 500);
		}

		@Override
		public int getScrollableBlockIncrement(Rectangle visibleRect,
				int orientation, int direction) {
			return 1;
		}

		@Override
		public boolean getScrollableTracksViewportHeight() {
			return false;
		}

		@Override
		public boolean getScrollableTracksViewportWidth() {
			return true;
		}

		@Override
		public int getScrollableUnitIncrement(Rectangle visibleRect,
				int orientation, int direction) {
			return 1;
		}
		
	}
	
	private class TeamPanel extends JPanel implements TeamListener, Selectable {

		private static final long serialVersionUID = 1L;
		
		private Hashtable<Client, ClientPanel> clients = new Hashtable<Client, ClientPanel>();

		private JPanel clientPanel = new ScrollabeListPanel();
		
		private JLabel score = new JLabel("0");
		
		private JLabel title;
		
		private JPanel header = new JPanel();
		
		private Team team;
		
		public TeamPanel(Team team) {
			
			this.team = team;
			
			setLayout(new BorderLayout());

			header.setLayout(new BorderLayout());
			
			title = new JLabel(team.getName());
			title.setForeground(team.getColor());
			title.setFont(getFont().deriveFont(Font.BOLD, 14));
			title.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
			
			header.addMouseListener(new MouseAdapter() {
				
				@Override
				public void mouseClicked(MouseEvent e) {
					ClientsPanel.this.select(TeamPanel.this.team);
				}
				
			});
			
			score.setForeground(team.getColor());
			score.setFont(getFont().deriveFont(Font.BOLD, 14));
			score.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
			score.setHorizontalAlignment(JLabel.RIGHT);
			
			header.add(title, BorderLayout.CENTER);
			header.add(score, BorderLayout.EAST);
			
			add(header, BorderLayout.NORTH);
			add(new JScrollPane(clientPanel), BorderLayout.CENTER);
			
			
			team.addListener(this);

		}
		
		@Override
		public void clientConnect(Team team, Client client) {
			
			if (this.team != team)
				return;
			
			ClientPanel panel = new ClientPanel(client);
			
			clients.put(client, panel);
			clientPanel.add(panel);
			clientPanel.revalidate();
			
		}

		@Override
		public void clientDisconnect(Team team, Client client) {
			
			if (this.team != team)
				return;
			
			ClientPanel panel = clients.remove(client);
			
			if (panel != null)
				clientPanel.remove(panel);
			
			//revalidate();
			clientPanel.repaint();
			clientPanel.revalidate();
		}

		@Override
		public void scoreChange(Team team, int score) {
			this.score.setText(score + "");
		}

		@Override
		public void deselect() {
			setBackground(normalBackground);
			title.setBackground(normalBackground);
			score.setBackground(normalBackground);
			header.setBackground(normalBackground);
			repaint();
		}

		@Override
		public void select() {
			setBackground(selectedBackground);
			title.setBackground(selectedBackground);
			score.setBackground(selectedBackground);
			header.setBackground(selectedBackground);
			repaint();
		}
		
	}
	
	private class ClientPanel extends JPanel implements ClientListener, Selectable {

		private static final long serialVersionUID = 1L;
		
		Client client;
		
		JLabel clientInfo = new JLabel();
		
		JLabel agentInfo = new JLabel();
		
	/*	JButton kill = new JButton("K");
		
		JButton history = new JButton("H");
		*/
		TrafficMonitor traffic = new TrafficMonitor(20);
		
		private ClientPanel(Client cl) {
			super();
			this.client = cl;
			client.addListener(this);
			
			setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			
			setLayout(new BorderLayout(3, 3));
			Box box = new Box(BoxLayout.Y_AXIS);
			add(box, BorderLayout.CENTER);
			
			clientInfo.setPreferredSize(new Dimension(500, 30));
			
			clientInfo.setFont(getFont().deriveFont(Font.BOLD, 12.0f));
			agentInfo.setFont(getFont().deriveFont(Font.PLAIN, 9.0f));
			
			box.add(clientInfo);
			box.add(agentInfo);
			
			clientInfo.setText(client.toString());
			
			agentInfo.setText("n/a");
			
			Box side = new Box(BoxLayout.X_AXIS);
			add(side, BorderLayout.EAST);
			
			side.add(traffic);
			/*side.add(Box.createHorizontalStrut(5));
			side.add(history);
			side.add(Box.createHorizontalStrut(5));
			side.add(kill);*/
			
			addMouseListener(new MouseAdapter() {
				
				@Override
				public void mouseClicked(MouseEvent e) {
					
					ClientsPanel.this.select(ClientPanel.this.client);
				}
				
			});
		}

		@Override
		public void agent(Client client, Agent agent) {

			if (this.client != client)
				return;
			
			agentInfo.setText(agent == null ? "n/a" : "Id: " + agent.getId());
		}

		@Override
		public void transfer(Client client, int messages) {
			if (this.client != client)
				return;
			
			traffic.push(messages);
		}

		@Override
		public void deselect() {
			setBackground(normalBackground);
			clientInfo.setBackground(normalBackground);
			agentInfo.setBackground(normalBackground);
			repaint();
		}

		@Override
		public void select() {
			setBackground(selectedBackground);
			clientInfo.setBackground(selectedBackground);
			agentInfo.setBackground(selectedBackground);
			repaint();
		}
		
	}
	
	private static class TrafficMonitor extends JPanel {

		private static final long serialVersionUID = 1L;

		private ConcurrentLinkedQueue<Integer> buffer = new ConcurrentLinkedQueue<Integer>();
		
		private int max = 30, length;
		
		public TrafficMonitor(int length) {
			super();
			this.length = length;
			setBackground(Color.BLACK);
		}
		
		public void push(int messages) {
			buffer.add(messages);
			if (buffer.size() > length) {
				buffer.poll();
			}
			
			repaint();
		}
		
		@Override
		public void paint(Graphics g) {
			super.paint(g);
			
			int barWidth = getWidth() / length;
			
			g.setColor(Color.GREEN);
			
			Iterator<Integer> traffic = buffer.iterator();
			
			for (int i = 0; i < length; i++) {
				
				if (!traffic.hasNext())
					break;
				
				int barHeight = (getHeight() * traffic.next()) / max; 
				
				g.fillRect(barWidth*i, getHeight() - barHeight, barWidth, barHeight);
				
			}

		}
		
		@Override
		public Dimension getPreferredSize() {
			return new Dimension(3 * length, max);
		}
	}
	
	private Hashtable<Team, TeamPanel> teams = new Hashtable<Team, TeamPanel>();
	
	private SelectionObserver observer;
	
	public ClientsPanel(Game game, SelectionObserver observer) {
		super(true);

		this.observer = observer;
		
		setLayout(new GridLayout(game.getTeams().size(), 1));
		
		selectedBackground = getBackground().darker().darker().darker();
		normalBackground = getBackground();
		
		for (Team t : game.getTeams()) {
			
			TeamPanel tp = new TeamPanel(t);
			
			add(tp);
	
			teams.put(t, tp);
			
		}
	}
	
	@Override
	public Dimension getPreferredSize() {
		return new Dimension(240, 200);
	}
	
	private Selectable selected = null;
	
	private void select(Team team) {
		
		TeamPanel tp = teams.get(team);

		if (tp == null)
			return;
	
		if (selected != null && selected == tp) {
			selected.deselect();	
			selected = null;
			
			if (observer != null)
				observer.teamSelected(null);
			
		} else {
			if (selected != null) {
				selected.deselect();
			}
			
			selected = tp;
			tp.select();
			
			if (observer != null)
				observer.teamSelected(tp.team);
		}

		
		
	}
	
	private void select(Client client) {
		
		TeamPanel tp = teams.get(client.getTeam());
		
		if (tp == null)
			return;
	
		ClientPanel cp = tp.clients.get(client);
		
		if (cp == null)
			return;
		
		if (selected != null && selected == cp) {
			selected.deselect();	
			selected = null;
			
			if (observer != null)
				observer.clientSelected(null);
		} else {
			if (selected != null) {
				selected.deselect();
			}
			
			selected = cp;
			cp.select();
			
			if (observer != null)
				observer.clientSelected(cp.client);
		}

	}
}
