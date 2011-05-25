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
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import fri.pipt.arena.Arena;
import fri.pipt.arena.SwingView;
import fri.pipt.server.ClientsPanel.SelectionObserver;
import fri.pipt.server.Dispatcher.Client;
import fri.pipt.server.Field.BodyPosition;

public class Main {

	private static final int PORT = 5000;

	private static final String RELEASE = "0.6";

	private static Game game;

	private static long renderTime = 0;

	private static int renderCount = 0;

	private static long stepTime = 0;

	private static int stepCount = 0;

	private static Object mutex = new Object();

	private static History history = new History();

	private static boolean running = false;

	private static GameSwingView view = new GameSwingView();

	private static JLabel gameStepDisplay = new JLabel();
	
	private static final String[] ZOOM_LEVELS = new String[] { "50%", "100%",
			"200%" };

	private static Action playpause = new AbstractAction("Play") {

		private static final long serialVersionUID = 1L;

		@Override
		public void actionPerformed(ActionEvent arg0) {

			running = !running;

			setEnabled(false);
			
			//putValue(AbstractAction.NAME, running ? "Pause" : "Play");
		}
	};

	private static class GameSwingView extends SwingView implements
			GameListener, SelectionObserver {

		private static final long serialVersionUID = 1L;

		private static final int BUFFER_LIFE = 10;

		private LinkedList<Message> buffer = new LinkedList<Message>();

		private HeatMap visualization = null;

		public class Message {

			private int length, step;

			private Agent sender, receiver;

			public Message(Agent sender, Agent receiver, int length) {
				super();
				this.sender = sender;
				this.receiver = receiver;
				this.length = length;
				this.step = game.getStep();
			}

		}

		public GameSwingView() {
			super(12);
		}

		@Override
		public void paint(Graphics g) {

			long start = System.currentTimeMillis();

			Arena view = getArena();

			paintBackground(g, visualization == null ? view : visualization);

			paintObjects(g, view);

			LinkedList<Message> active = new LinkedList<Message>();
			int current = game.getStep();

			synchronized (buffer) {

				for (Message m : buffer) {

					if (current - m.step < BUFFER_LIFE)
						active.add(m);
				}
			}

			Field field = game.getField();

			g.setColor(Color.YELLOW);

			for (Message m : active) {

				BodyPosition p1 = field.getPosition(m.sender);
				BodyPosition p2 = field.getPosition(m.receiver);

				if (p1 == null || p2 == null)
					continue;

				int x1 = (int) ((p1.getX() + p1.getOffsetX()) * cellSize)
						+ cellSize / 2;
				int y1 = (int) ((p1.getY() + p1.getOffsetY()) * cellSize)
						+ cellSize / 2;
				int x2 = (int) ((p2.getX() + p2.getOffsetX()) * cellSize)
						+ cellSize / 2;
				int y2 = (int) ((p2.getY() + p2.getOffsetY()) * cellSize)
						+ cellSize / 2;

				g.drawLine(x1, y1, x2, y2);

				float progress = (float) (current - m.step) / BUFFER_LIFE;

				int size = Math.min(8, Math.max(3, m.length / 32));

				g.fillRect((int) ((1 - progress) * x1 + progress * x2) - size
						/ 2, (int) ((1 - progress) * y1 + progress * y2) - size
						/ 2, size, size);

			}

			synchronized (buffer) {
				buffer = active;
			}

			long used = System.currentTimeMillis() - start;

			synchronized (mutex) {

				renderTime += used;
				renderCount++;

			}

		}

		@Override
		public void message(Team team, int from, int to, int length) {
			synchronized (buffer) {
				try {
					Agent sender = team.findById(from).getAgent();
					Agent receiver = team.findById(to).getAgent();
					if (sender == null || receiver == null)
						return;
					buffer.add(new Message(sender, receiver, length));
				} catch (NullPointerException e) {
				}
			}

		}

		@Override
		public void clientSelected(Client client) {

			synchronized (this) {
				if (client == null) {
					if (visualization != null)
						game.removeListener(visualization);
					visualization = null;
					setBasePallette(null);
					return;
				}

				Agent a = client.getAgent();

				if (a == null)
					return;

				visualization = new HeatMap(game.getField(), history, a, game
						.getNeighborhoodSize());
				setBasePallette((Palette) visualization);
				game.addListener(visualization);
			}

		}

		@Override
		public void teamSelected(Team team) {

			synchronized (this) {
				if (visualization != null)
					game.removeListener(visualization);

				visualization = null;
				setBasePallette(null);
			}

		}

		@Override
		public void position(Team team, int id, BodyPosition p) {

		}

		@Override
		public void step() {

		}

	}

	public static void main(String[] args) throws IOException {

		log("Starting game server (release %s)", RELEASE);

		if (args.length < 1) {
			log("Please provide game description file location as an argument.");
			System.exit(1);
		}

		log("Java2D OpenGL acceleration "
				+ (("true".equalsIgnoreCase(System
						.getProperty("sun.java2d.opengl"))) ? "enabled"
						: "not enabled"));

		game = Game.loadFromFile(new File(args[0]));

		Dispatcher dispatcher = new Dispatcher(PORT, game);

		(new Thread(dispatcher)).start();

		final int gameSpeed = game.getSpeed();

		game.addListener(view);

		game.addListener(history);

		(new Thread(new Runnable() {

			@Override
			public void run() {
				int sleep = 1000 / gameSpeed;
				long start, used;
				while (true) {

					start = System.currentTimeMillis();

					if (running)
						game.step();

					view.update(game.getField());

					used = System.currentTimeMillis() - start;

					stepTime += used;
					stepCount++;

					if (game.getStep() % 100 == 0 && running) {
						long renderFPS, stepFPS;

						synchronized (mutex) {
							renderFPS = (renderCount * 1000)
									/ Math.max(1, renderTime);
							renderCount = 0;
							renderTime = 0;
						}

						stepFPS = (stepCount * 1000) / Math.max(1, stepTime);
						stepCount = 0;
						stepTime = 0;

						log(
										"Game step: %d (step: %d fps, render: %d fps)",
										game.getStep(), stepFPS, renderFPS);
					}

					if (game.getStep() % 10 == 0) {
						gameStepDisplay.setText(String.format("Step: %d", game.getStep()));
					}
					
					try {
						if (used < sleep)
							Thread.sleep(sleep - used);
						else {
							log("Warning: low frame rate");
						}
					} catch (InterruptedException e) {
						e.printStackTrace();
					}

				}

			}
		})).start();

		JFrame window = new JFrame("AgentField - " + game.getName());

		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		final JScrollPane pane = new JScrollPane(view);

		JPanel left = new JPanel(new BorderLayout());

		left.add(pane, BorderLayout.CENTER);

		JPanel status = new JPanel(new BorderLayout());

		status.add(new JButton(playpause), BorderLayout.WEST);

		final JComboBox zoom = new JComboBox(ZOOM_LEVELS);

		zoom.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				int ind = zoom.getSelectedIndex();
				
				if (ind > -1) {
					view.setCellSize(6 * (ind + 1));
					pane.repaint();
				}
			}
		});

		zoom.setSelectedIndex(1);
		zoom.setEditable(false);
		
		status.add(zoom, BorderLayout.EAST);
		
		gameStepDisplay.setHorizontalAlignment(JLabel.CENTER);
		status.add(gameStepDisplay, BorderLayout.CENTER);
		
		left.add(status, BorderLayout.NORTH);

		window.getContentPane().add(
				new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left,
						new ClientsPanel(game, view)));

		GraphicsEnvironment ge = GraphicsEnvironment
				.getLocalGraphicsEnvironment();

		Rectangle r = ge.getDefaultScreenDevice().getDefaultConfiguration()
				.getBounds();

		window.pack();

		Dimension ws = window.getSize();
		
		if (r.width - ws.width < 100) {
			ws.width = r.width - 100;
		}
		
		if (r.height - ws.height < 100) {
			ws.height = r.height - 100;
		}
		
		window.setSize(ws);
		
		window.setVisible(true);

	}

	private static DateFormat date = new SimpleDateFormat("[hh:mm:ss] ");
	
	public static void log(String format, Object ... objects) {
		
		System.out.println(date.format(new Date()) + String.format(format, objects));
		
	}
	
}
