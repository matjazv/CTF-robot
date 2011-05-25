package utils.world;

import java.awt.Color;
import java.awt.Container;
import java.awt.FlowLayout;
import java.awt.Graphics;

import javax.swing.JFrame;

import fri.pipt.protocol.Neighborhood;
import fri.pipt.protocol.Position;

import utils.KnownArena;

public class KnownArenaView extends JFrame {
	private KnownArena arena;
	private utils.world.KnownArena ka;
	private static final int maxArena = 82; //
	private static final int size = 6;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public KnownArenaView () {
		//WindowUtilities.setNativeLookAndFeel();
	    setTitle("View of Arena");
	    setSize(400, 150);
	    Container content = getContentPane();
	    content.setBackground(Color.white);
	    content.setLayout(new FlowLayout()); 
	    setVisible(true);
	}
	
	public KnownArenaView (KnownArena arena) {
		this.arena = arena;
		//WindowUtilities.setNativeLookAndFeel();
	    setTitle("View of Arena");
	    setSize((2*maxArena+1)*size, (2*maxArena+1)*size);
	    Container content = getContentPane();
	    content.setBackground(Color.white);
	    content.setLayout(new FlowLayout()); 
	    setVisible(true);
	}
	
	public KnownArenaView (utils.world.KnownArena arena) {
		this.ka = arena;
		//WindowUtilities.setNativeLookAndFeel();
	    setTitle("View of Arena");
	    setSize((2*maxArena+1)*size, (2*maxArena+1)*size);
	    Container content = getContentPane();
	    content.setBackground(Color.white);
	    content.setLayout(new FlowLayout()); 
	    setVisible(true);
	}
	
	@Override
	public void paint(Graphics g) {
		//super.paint(g);
		int X =0;
		int Y = 0;
		for (int x = - maxArena; x <= maxArena; x++) {
			for (int y = -maxArena; y <= maxArena; y++) {
				if (arena != null) 
				if (arena.arena.get(new Position(x,y)) != null) {
					switch(arena.arena.get(new Position(x,y))){
					case Neighborhood.WALL:
						g.setColor(Color.GRAY);
						break;
					case Neighborhood.EMPTY:
						g.setColor(Color.GREEN);
						break;
					} 
				}else{
					g.setColor(Color.BLACK);
				}
				else if (ka != null ) {
					if (ka.getPositionAt(x, y) != null)
						g.setColor(ka.getPositionAt(x, y).getColor());
					
					else g.setColor(Color.BLACK);
				}
				else g.setColor(Color.BLACK);
				g.fillRect(X, Y, size, size);
				Y += size;
			}
			Y = 0;
			X += size;
		}
		
	}
	public static void main (String [] args ) {
		KnownArenaView arena = new KnownArenaView();
	}
}
