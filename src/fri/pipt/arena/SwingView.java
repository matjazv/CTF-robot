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

	protected int cellBorder = 2;

	public static interface Palette {
		
		public Color getColor(int tile);
		
	}
	
	private static Palette grassPalette = new Palette() {
		
		private Color[] grassColors = new Color[] {
			new Color(0.1f, 0.6f, 0.1f), new Color(0.12f, 0.61f, 0.1f),
			new Color(0.11f, 0.6f, 0.11f), new Color(0.1f, 0.63f, 0.1f),
			new Color(0.1f, 0.65f, 0.1f), new Color(0.13f, 0.62f, 0.1f),
			new Color(0.12f, 0.61f, 0.1f), new Color(0.15f, 0.6f, 0.1f),
			new Color(0.2f, 0.65f, 0.11f), new Color(0.12f, 0.63f, 0.12f), };
		
		@Override
		public Color getColor(int tile) {

			if (tile < 0) tile = 0;
			if (tile >= grassColors.length) tile = 0;
			
			return grassColors[tile];
		}
	};

	private static Color[] wallColors = new Color[] {
			new Color(0.3f, 0.3f, 0.3f), new Color(0.26f, 0.26f, 0.26f),
			new Color(0.25f, 0.25f, 0.25f), new Color(0.4f, 0.4f, 0.4f),
			new Color(0.3f, 0.3f, 0.3f), new Color(0.25f, 0.25f, 0.25f),
			new Color(0.32f, 0.32f, 0.32f), new Color(0.25f, 0.25f, 0.25f),
			new Color(0.23f, 0.23f, 0.23f), new Color(0.25f, 0.25f, 0.25f), };

	private Polygon flag;

	public static Polygon getFlagGlyph(int cellSize) {

		int polex = (int) (0.1 * cellSize);
		
		Polygon flag = new Polygon();
		flag.addPoint((int) (0.1 * cellSize), (int) (0.6 * cellSize));
		flag.addPoint((int) (0.9 * cellSize), (int) (0.3 * cellSize));
		flag.addPoint(polex, (int) (0.1 * cellSize));
		flag.addPoint(polex, (int) (0.9 * cellSize));
		flag.addPoint(Math.max((int) (0.15 * cellSize), polex+1), (int) (0.9 * cellSize));
		flag.addPoint(Math.max((int) (0.15 * cellSize), polex+1), (int) (0.6 * cellSize));
		
		return flag;
	}

	private Dimension size = new Dimension(100, 100);

	private Arena view;
	
	private Palette palette = null;
	
	public SwingView(int cellSize) {

		setDoubleBuffered(true);

		setCellSize(cellSize);
	}
	
	public SwingView() {

		this(24);

	}

	protected void paintBackground(Graphics g, Arena view) {
		
		Palette p = palette == null ? grassPalette : palette;
		
		for (int j = 0; j < view.getHeight(); j++) {

			for (int i = 0; i < view.getWidth(); i++) {

				int base = view.getBaseTile(i, j);

				g.setColor(p.getColor(base));

				g.fillRect(i * cellSize, j * cellSize, cellSize, cellSize);


			}
		}
	}
	
	protected void paintObjects(Graphics g, Arena view) {
		
		Color color = null;

		for (int j = 0; j < view.getHeight(); j++) {

			for (int i = 0; i < view.getWidth(); i++) {
				
				int body = view.getBodyTile(i, j);

				if (body >= Arena.TILE_WALL_0 && body <= Arena.TILE_WALL_9) {
					color = wallColors[body - Arena.TILE_WALL_0];
					g.setColor(color);
					g.fillRect(i * cellSize + cellBorder, j * cellSize
							+ cellBorder, cellSize - 2 * cellBorder,
							cellSize - 2 * cellBorder);
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
					g.fillOval(i * cellSize + cellBorder + translateX, j * cellSize
							+ cellBorder + translateY, cellSize - 2 * cellBorder,
							cellSize - 2 * cellBorder);
					break;
					
				case Arena.TILE_AGENT_FLAG:
					g.fillOval(i * cellSize + cellBorder + translateX, j * cellSize
							+ cellBorder + translateY, cellSize - 2 * cellBorder,
							cellSize - 2 * cellBorder);
					g.setXORMode(Color.WHITE);
					flag.translate(i * cellSize + translateX, j * cellSize + translateY);
					g.fillPolygon(flag);
					flag.translate(-i * cellSize - translateX, -j * cellSize - translateY);
					g.setPaintMode();
					break;
					
					
				case Arena.TILE_HEADQUARTERS:
					g.fillRect(i * cellSize + cellBorder + translateX, j * cellSize
							+ cellBorder + translateY, cellSize - 2 * cellBorder,
							cellSize - 2 * cellBorder);
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
	public void paint(Graphics g) {
		super.paint(g);

		if (this.view == null)
			return;
		
		Arena view = null;
		synchronized (this) {
			
			view = this.view;

		}

		paintBackground(g, view);
		
		paintObjects(g, view);

	}

	protected Arena getArena() {
		return view;
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

	public void setCellSize(int cellSize) {
		
		this.cellSize = Math.min(64, Math.max(4, cellSize));
		
		this.cellBorder = Math.max(1, Math.round((float)this.cellSize * 0.1f));
		
		flag = getFlagGlyph(this.cellSize);
		
		synchronized (this) {
	
			if (this.view != null)
				this.size = new Dimension(view.getWidth() * cellSize, view.getHeight()
						* cellSize);
		}
		
		revalidate();
		
	}
	
	public int getCellSize() {
		return cellSize;
	}
	
	public static void open(SwingView panel) {

		JFrame window = new JFrame("Game");

		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JScrollPane pane = new JScrollPane(panel);

		window.getContentPane().add(pane);

		window.pack();

		window.setVisible(true);
	}
	
	public void setBasePallette(Palette p) {
		palette = p;
	}
}
