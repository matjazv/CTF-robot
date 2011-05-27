package utils.world;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.LinkedList;

import javax.swing.JFrame;

public class KnownArenaView extends JFrame implements WindowListener {
	private static final int maxArena = 82;
	private static final int size = 6;

	private static final long serialVersionUID = 1L;
	
	private Graphics bufferGraphics;
	private Image offscreen;

	public KnownArenaView(utils.world.KnownArena arena) {
		setTitle("View of Arena");
		setSize((2 * maxArena + 1) * size, (2 * maxArena + 1) * size);
		
		setVisible(true);
		addWindowListener(this);
		
		this.offscreen = createImage((2 * maxArena + 1) * size, (2 * maxArena + 1) * size);
		this.bufferGraphics = this.offscreen.getGraphics();
	}

	@Override
	public void paint(Graphics g) {
		
		for (int x = -maxArena; x <= maxArena; x++) {
			for (int y = -maxArena; y <= maxArena; y++) {
				paintPosition(this.bufferGraphics, x, y);
			}
		}
		paintPlan(this.bufferGraphics, Planer.getPlanForPaint(), Color.ORANGE);
		paintPosition(this.bufferGraphics, KnownArena.getARENA().getCurentPosition(), Color.MAGENTA);

		g.drawImage(offscreen, 0, 0, this);
	}

	private void paintPosition(Graphics g, KnownPosition position, Color color) {
		if (position != null) {
			g.setColor(color);
			g.fillRect((position.getX() + maxArena) * size, (position.getY() + maxArena) * size, size, size);
		}
	}
	
	private void paintPlan(Graphics g, LinkedList<KnownPosition> plan, Color color) {
		if (plan == null) return;
		for (KnownPosition position : plan) {
			paintPosition(g, position, color);
		}
	}

	private void paintPosition(Graphics g, int x, int y) {
		KnownPosition position = KnownArena.getARENA().getPositionAt(x, y);
		if (position != null)
			g.setColor(position.getColor());
		else
			g.setColor(Color.BLACK);
		g.fillRect((x + maxArena) * size, (y + maxArena) * size, size, size);
	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		System.exit(0);
	}
	@Override public void windowClosed(WindowEvent arg0) {}
	@Override public void windowActivated(WindowEvent arg0) {}
	@Override public void windowDeactivated(WindowEvent arg0) {}
	@Override public void windowDeiconified(WindowEvent arg0) {}
	@Override public void windowIconified(WindowEvent arg0) {}
	@Override public void windowOpened(WindowEvent arg0) {}

}
