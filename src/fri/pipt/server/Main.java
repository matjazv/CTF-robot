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

import java.awt.Color;
import java.awt.Graphics;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;

import javax.swing.JFrame;
import javax.swing.JScrollPane;

import fri.pipt.arena.SwingView;
import fri.pipt.protocol.Message.Direction;
import fri.pipt.server.Field.BodyPosition;

public class Main {

	private static final int PORT = 5000;
	
	private static final String RELEASE = "0.3";
	
	private static Game game;
	
	private static class GameSwingView extends SwingView implements GameListener {

		private static final long serialVersionUID = 1L;
		
		private static final int BUFFER_LIFE = 10;
		
		private LinkedList<Message> buffer = new LinkedList<Message>();
		
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
		
		@Override
		public void paint(Graphics g) {
			super.paint(g);

			LinkedList<Message> active = new LinkedList<Message>();
			int current = game.getStep();
			
			synchronized (buffer) {

				for (Message m : buffer) {
					
					if (current - m.step < BUFFER_LIFE)
						active.add(m);
				}
			}
			
			Field field =  game.getField();
			
			g.setColor(Color.YELLOW);
			
			for (Message m : active) {
				
				BodyPosition p1 = field.getPosition(m.sender);
				BodyPosition p2 = field.getPosition(m.receiver);
				
				if (p1 == null || p2 == null)
					continue;
				
				int x1 = (int)((p1.getX() + p1.getOffsetX()) * cellSize) + cellSize / 2;
				int y1 = (int)((p1.getY() + p1.getOffsetY()) * cellSize) + cellSize / 2;
				int x2 = (int)((p2.getX() + p2.getOffsetX()) * cellSize) + cellSize / 2;
				int y2 = (int)((p2.getY() + p2.getOffsetY()) * cellSize) + cellSize / 2;
				
				g.drawLine(x1, y1, x2, y2);
				
				float progress = (float) (current - m.step) / BUFFER_LIFE;
				
				int size = Math.min(8, Math.max(3, m.length / 32)); 
				
				g.fillRect((int)((1-progress) * x1 + progress * x2) - size / 2, 
						(int)((1-progress) * y1 + progress * y2) - size / 2, size, size);
				
				
			}

			synchronized (buffer) {
				buffer = active;
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
				} catch (NullPointerException e) {}
			}
			
		}

		@Override
		public void move(Team team, int id, Direction direction) {

		}
		
	}
	
	public static void main(String[] args) throws IOException {
		
		System.out.printf("Starting game server (release %s)\n", RELEASE);
		
		if (args.length < 1) {
			System.out.println("Please provide game description file location as an argument.");
			System.exit(1);
		}
			
		
		game = Game.loadFromFile(new File(args[0]));
		
		Dispatcher dispatcher = new Dispatcher(PORT, game);
		
		(new Thread(dispatcher)).start();
		
		final GameSwingView arena = new GameSwingView();
		
		final int gameSpeed = game.getProperty("gameplay.speed", 10);
		
		game.addListener(arena);
		
		(new Thread(new Runnable() {
			
			@Override
			public void run() {
				int sleep = 1000 / gameSpeed;
				
				while (true) {
					
					game.step();
					arena.update(game.getField());

					try {
						Thread.sleep(sleep);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				
				}
				
			}
		})).start();

		JFrame window = new JFrame("Game");

		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JScrollPane pane = new JScrollPane(arena);

		window.getContentPane().add(pane);

		window.pack();

		window.setVisible(true);
		
	}

}
