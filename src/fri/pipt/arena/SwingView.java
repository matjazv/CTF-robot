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
package fri.pipt.arena;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Polygon;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

public class SwingView extends JPanel implements ArenaView {

	private static final long serialVersionUID = 1L;

	protected int cellSize = 24;

	protected static final int CELL_BORDER = 3;

	private static Color[] grassColors = new Color[] {
			new Color(0.1f, 0.6f, 0.1f), new Color(0.12f, 0.61f, 0.1f),
			new Color(0.11f, 0.6f, 0.11f), new Color(0.1f, 0.63f, 0.1f),
			new Color(0.1f, 0.65f, 0.1f), new Color(0.13f, 0.62f, 0.1f),
			new Color(0.12f, 0.61f, 0.1f), new Color(0.15f, 0.6f, 0.1f),
			new Color(0.2f, 0.65f, 0.11f), new Color(0.12f, 0.63f, 0.12f), };

	private static Color[] wallColors = new Color[] {
			new Color(0.3f, 0.3f, 0.3f), new Color(0.26f, 0.26f, 0.26f),
			new Color(0.25f, 0.25f, 0.25f), new Color(0.4f, 0.4f, 0.4f),
			new Color(0.3f, 0.3f, 0.3f), new Color(0.25f, 0.25f, 0.25f),
			new Color(0.32f, 0.32f, 0.32f), new Color(0.25f, 0.25f, 0.25f),
			new Color(0.23f, 0.23f, 0.23f), new Color(0.25f, 0.25f, 0.25f), };

	private Polygon flag;

	public static Polygon getFlagGlyph(int cellSize) {

		Polygon flag = new Polygon();
		flag.addPoint((int) (0.1 * cellSize), (int) (0.6 * cellSize));
		flag.addPoint((int) (0.9 * cellSize), (int) (0.3 * cellSize));
		flag.addPoint((int) (0.1 * cellSize), (int) (0.1 * cellSize));
		flag.addPoint((int) (0.1 * cellSize), (int) (0.9 * cellSize));
		flag.addPoint((int) (0.15 * cellSize), (int) (0.9 * cellSize));
		flag.addPoint((int) (0.15 * cellSize), (int) (0.6 * cellSize));
		
		return flag;
	}

	private Dimension size = new Dimension(100, 100);

	private Arena view;
	
	public SwingView(int cellSize) {

		this.cellSize = cellSize;
		
		setDoubleBuffered(true);

		flag = getFlagGlyph(cellSize);
		
	}
	
	public SwingView() {

		this(24);

	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);

		if (this.view == null)
			return;
		
		Arena view = null;
		synchronized (this) {
			
			view = this.view;

		}

		Color color = null;

		for (int j = 0; j < view.getHeight(); j++) {

			for (int i = 0; i < view.getWidth(); i++) {

				int base = view.getBaseTile(i, j);

				if (base >= Arena.TILE_GRASS_0 && base <= Arena.TILE_GRASS_9) {
					color = grassColors[base - Arena.TILE_GRASS_0];
				}

				g.setColor(color);

				g.fillRect(i * cellSize, j * cellSize, cellSize, cellSize);


			}
		}
		
		for (int j = 0; j < view.getHeight(); j++) {

			for (int i = 0; i < view.getWidth(); i++) {
				
				int body = view.getBodyTile(i, j);

				if (body >= Arena.TILE_WALL_0 && body <= Arena.TILE_WALL_9) {
					color = wallColors[body - Arena.TILE_WALL_0];
					g.setColor(color);
					g.fillRect(i * cellSize + CELL_BORDER, j * cellSize
							+ CELL_BORDER, cellSize - 2 * CELL_BORDER,
							cellSize - 2 * CELL_BORDER);
					continue;
				}
				
				color = view.getBodyColor(i, j);

				if (color == null)
					continue;

				g.setColor(color);

				int translateX = (int) (view.getBodyOffsetX(i, j) * cellSize);
				int translateY = (int) (view.getBodyOffsetY(i, j) * cellSize);
				
				switch (body) {
				case Arena.TILE_AGENT:
					g.fillOval(i * cellSize + CELL_BORDER + translateX, j * cellSize
							+ CELL_BORDER + translateY, cellSize - 2 * CELL_BORDER,
							cellSize - 2 * CELL_BORDER);
					break;
					
				case Arena.TILE_AGENT_FLAG:
					g.fillOval(i * cellSize + CELL_BORDER + translateX, j * cellSize
							+ CELL_BORDER + translateY, cellSize - 2 * CELL_BORDER,
							cellSize - 2 * CELL_BORDER);
					g.setXORMode(Color.WHITE);
					flag.translate(i * cellSize + translateX, j * cellSize + translateY);
					g.fillPolygon(flag);
					flag.translate(-i * cellSize - translateX, -j * cellSize - translateY);
					g.setPaintMode();
					break;
					
					
				case Arena.TILE_HEADQUARTERS:
					g.fillRect(i * cellSize + CELL_BORDER + translateX, j * cellSize
							+ CELL_BORDER + translateY, cellSize - 2 * CELL_BORDER,
							cellSize - 2 * CELL_BORDER);
					break;
				case Arena.TILE_FLAG:
					flag.translate(i * cellSize + translateX, j * cellSize + translateY);
					g.fillPolygon(flag);
					flag.translate(-i * cellSize - translateX, -j * cellSize - translateY);
					break;
				}

			}

		}

	}

	@Override
	public Dimension getPreferredSize() {

		return new Dimension(size);

	}

	@Override
	public void update(Arena view) {

		if (view == null)
			return;
		
		synchronized (this) {
			this.view = view;
			
			this.size = new Dimension(view.getWidth() * cellSize, view.getHeight()
					* cellSize);
		}
		
		repaint();
		
	}

	public static void open(SwingView panel) {

		JFrame window = new JFrame("Game");

		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JScrollPane pane = new JScrollPane(panel);

		window.getContentPane().add(pane);

		window.pack();

		window.setVisible(true);
	}
}
