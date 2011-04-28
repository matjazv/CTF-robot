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
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;

import javax.imageio.ImageIO;

import fri.pipt.arena.Arena;
import fri.pipt.protocol.Position;
import fri.pipt.server.Game.FlagMode;
import fri.pipt.server.Team.TeamBody;

public class Field implements Arena {

	public static class BodyPosition extends Position {
		
		private static final long serialVersionUID = 1L;

		private float offsetX;
		
		private float offsetY;
		
		public float getOffsetX() {
			return offsetX;
		}

		public void setOffsetX(float offsetX) {
			
			if (offsetX <= -0.5) {
				int mx = (int)Math.ceil(-offsetX - 0.5);
				
				offsetX += mx;
				
				setX(getX() - mx); 
			} else if (offsetX >= 0.5) {
				int mx = (int)Math.ceil(offsetX - 0.5);
				
				offsetX -= mx;
				
				setX(getX() + mx); 
			}
			
			this.offsetX = offsetX;
		}

		public float getOffsetY() {
			return offsetY;
		}

		public void setOffsetY(float offsetY) {
			
			if (offsetY <= -0.5) {
				int my = (int)Math.ceil(-offsetY - 0.5);
				
				offsetY += my;
				
				setY(getY() - my); 
			} else if (offsetY >= 0.5) {
				int my = (int)Math.ceil(offsetY - 0.5);
				
				offsetY -= my;
				
				setY(getY() + my); 
			}
			
			this.offsetY = offsetY;
		}

		public BodyPosition(int x, int y) {
			this(x, y, 0, 0);
		}

		public BodyPosition(int x, int y, float offsetX, float offsetY) {
			super(x, y);
			this.offsetX = offsetX;
			this.offsetY = offsetY;
		}
		
		public BodyPosition(Position p, float offsetX, float offsetY) {
			super(p);
			this.offsetX = offsetX;
			this.offsetY = offsetY;
		}
		
		public BodyPosition(BodyPosition p) {
			super(p);
			this.offsetX = p.offsetX;
			this.offsetY = p.offsetY;
		}
		
		public String toString() {
			return String.format("Body position: %d, %d offset: %.1f, %.1f", getX(), getY(), offsetX, offsetY);
		}
		
		public boolean hasOffset() {
			return offsetX != 0 || offsetY != 0;
		}
		
	}
	
	public static abstract class Body {
		
		private int tile;
		
		public Body(int tile) {
			this.tile = tile;
		}
		
		public int getTile() {
			return tile;
		}
		
	}
	
	public static class Wall extends Body {

		public Wall(int tile) {
			super(tile);
		}
		
	}
	
	public class Cell {

		private Position position;
		
		private float offsetX = 0, offsetY = 0;
		
		private Body body = null;
		
		private int tile;
		
		protected Cell(Position position, int tile) {

			this.position = position;
			this.tile = tile;
			
		}
		
		public boolean isEmpty() {
			
			return body == null;
			
		}
		
		public int getTile() {
			
			return tile;
			
		}

		public Body getBody() {
			
			return body;
			
		}
		
		public Position getPosition() {
			return new Position(this.position);
		}
		
		private boolean placeBody(Body body, float offsetX, float offsetY) {
			
			if (!isEmpty() && this.body != body) 
				return false;
				
			synchronized (positions) {
				Cell c = positions.get(body);
				
				if (c != null) 
					c.body = null;

				positions.remove(body);
				positions.put(body, this);
				this.body = body;				
			}

			this.offsetX = offsetX;
			
			this.offsetY = offsetY;
			
			return true;
		}
		
		public float getBodyOffsetX() {
			return offsetX;
		}

		public float getBodyOffsetY() {
			return offsetY;
		}
		
	}
	
	private Hashtable<Body, Cell> positions = new Hashtable<Body, Cell>();
	
	private Cell[] cells; 
	
	private int width, height;
	
	public Field(int width, int height) {
		
		this.width = width;
		this.height = height;
		cells = new Cell[width * height];
		
		int n = 0;
		
		for (int j = 0; j < height; j++) {
			for (int i = 0; i < width; i++) {
				
				int grass = ((int) (Math.random() * 10)) % 9;
				
				cells[n++] = new Cell(new Position(i, j), grass);
				
			}
		}
		
	}
	
	public static Field loadFromFile(File f, Game game) throws IOException {
		
		Dimension size = null;
		
		Vector<Position> walls = new Vector<Position>();
		
		Position[] flags = new Position[25];
		
		Position[] hqs = new Position[25];
		
		if (f.toString().endsWith(".field")) {
			size = loadAscii(f, flags, hqs, walls);
		} else {
			size = loadImage(f, flags, hqs, walls);
		}
		
		Field arena = new Field(size.width, size.height);
		
		for (Position p : walls) {
			
			int wall = Arena.TILE_WALL_0 + ((int) (Math.random() * 10)) % 9;
			
			arena.getCell(p.getX(), p.getY()).body = new Wall(wall);
			
		}
		
		List<Team> teams = game.getTeams();
		
		int count = 0;

		if (hqs.length < teams.size())
			System.out.println("Warning: this map does not contain enough positions for the number of teams declared in the current game!");

		if (count > -1) {
		
			for (int i = 0; i < hqs.length; i++) {
				
				if (hqs[i] != null && flags[i] != null) {

					Team team = teams.get(count);

					arena.putBody(team.getHeadquarters(), new BodyPosition(hqs[i].getX(), hqs[i].getY()));
					
					if (game.getFlagMode() == FlagMode.UNIQUE)
						arena.putBody(team.newFlag(), new BodyPosition(flags[i].getX(), flags[i].getY()));
					
					count++;
					
					if (count >= teams.size())
						break;
					
				}
				
			}
		}
		return arena;
		
	}
	
	private static Dimension loadAscii(File f, Position[] flags, Position[] hqs, Vector<Position> walls) throws IOException {
		
		Dimension dimension = new Dimension(0, 0);

		BufferedReader in = new BufferedReader(new FileReader(f));
		
		while (true) {
			
			String line = in.readLine();
			
			if (line == null)
				break;
			

			dimension.width = Math.max(dimension.width, line.length());
			
			for (int i = 0; i < line.length(); i++) {
				
				if (line.charAt(i) == ' ')
					continue;

				if (Character.isLetter(line.charAt(i))) {
					
					int index = (Character.toLowerCase(line.charAt(i)) - 'a');
					
					if (Character.isUpperCase(line.charAt(i))) {
						
						hqs[index] = new Position(i, dimension.height);
						
					} else {

						flags[index] = new Position(i, dimension.height);
						
					}
					
				}
				
				if (line.charAt(i) == '#')
					walls.add(new Position(i, dimension.height));
				
			}
			
			dimension.height++;
			
		}	
		
		return dimension;
	}
	
	
	private static Dimension loadImage(File f, Position[] flags, Position[] hqs, Vector<Position> walls) throws IOException {
		
		BufferedImage src = ImageIO.read(f);
	
		for (int j = 0; j < src.getHeight(); j++) {
			
			for (int i = 0; i < src.getWidth(); i++) {

				Color color = new Color(src.getRGB(i, j));
				
				if (color.getRed() == color.getBlue() && color.getRed() == color.getGreen()) {
					
					if (color.getRed() < 200) {
						walls.add(new Position(i, j));
					}
					
					continue;
				}
				
				float[] hsv = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
				
				int team = Math.round(hsv[0] * flags.length);

				if (hsv[2] > 0.5) {
					flags[team] = new Position(i, j);
				} else {
					hqs[team] = new Position(i, j);
				}
				
			}

		}
		
		
		
		return new Dimension(src.getWidth(), src.getHeight());
	}
	
	public int getWidth() {
		
		return width;
		
	}

	public int getHeight() {
		
		return height;
		
	}

	public Cell getCell(int x, int y) {
		
		if (x < 0 || x >= width || y < 0 || y >= height)
			return null;
		
		return cells[y * width + x]; 
		
	}

	public BodyPosition getPosition(Body body) {
		
		Cell cell = positions.get(body);
		
		return cell == null ? null : new BodyPosition(cell.position, cell.offsetX, cell.offsetY);
	}
	
	public Collection<Cell> getNeighborhood(int x, int y) {
		
		Collection<Cell> c = new Vector<Cell>();
		
		Cell n = getCell(x - 1, y);
		if (n != null)
			c.add(n);

		n = getCell(x + 1, y);
		if (n != null)
			c.add(n);
		
		n = getCell(x, y - 1);
		if (n != null)
			c.add(n);
		
		n = getCell(x, y + 1);
		if (n != null)
			c.add(n);
	
		return c;
	}

	public boolean putBody(Body body, BodyPosition position) {
		
		Cell cell = getCell(position.getX(), position.getY());
		
		if (cell == null)
			return false;
		
		return cell.placeBody(body, position.getOffsetX(), position.getOffsetY());
		
	}
	
	public boolean putBodyCloseTo(Body body, Position position) {
		
		Cell cell = getCell(position.getX(), position.getY());
		
		if (cell == null)
			return false;
		
		if (!cell.placeBody(body, 0, 0)) {
			for (Cell c : getNeighborhood(position.getX(), position.getY())) {
				
				if (c.placeBody(body, 0, 0))
					return true;
				
			}
		} else return true;
		
		return false;
		
	}
	
	public void removeBody(Body body) {
		
		BodyPosition bp = getPosition(body);
		
		if (bp == null)
			return;
		
		Cell cell = getCell(bp.getX(), bp.getY());
		
		if (cell == null)
			return;
		
		synchronized (positions) {
			cell.body = null;
			positions.remove(body);			
		}

	}

	@Override
	public int getBodyTile(int x, int y) {

		Cell c = getCell(x, y);
		if (c != null && c.getBody() != null)
			return c.getBody().getTile();

		return 0;
	}

	@Override
	public float getBodyOffsetX(int x, int y) {
		Cell c = getCell(x, y);
		if (c != null && c.getBody() != null)
			return c.getBodyOffsetX();

		return 0;
	}

	@Override
	public float getBodyOffsetY(int x, int y) {
		Cell c = getCell(x, y);
		if (c != null && c.getBody() != null)
			return c.getBodyOffsetY();

		return 0;
	}

	@Override
	public int getBaseTile(int x, int y) {

		Cell c = getCell(x, y);
		if (c != null)
			return c.getTile();

		return 0;
	}

	@Override
	public Color getBodyColor(int x, int y) {
		Cell c = getCell(x, y);
		if (c != null && c.getBody() != null
				&& (c.getBody() instanceof TeamBody))
			return ((TeamBody) c.getBody()).getTeam().getColor();

		return null;
	}

	public List<Cell> listEmptyFields(boolean emptyNeighborhood) {
		
		Vector<Cell> list = new Vector<Cell>();
		
		for (int j = 0; j < height; j++) {
		
			for (int i = 0; i < width; i++) {
				
				Cell c = cells[j * width + i];
				
				if (!c.isEmpty()) continue;
				
				if (emptyNeighborhood) {
					
					boolean empty = true;
					
					for (Cell n : getNeighborhood(i, j)) {
						
						empty &= n.isEmpty();
						
					}
					
					if (!empty)
						continue;
						
				}
				
				list.add(c);
				
			}
			
		}
		
		return list;
	}
}
