package noppes.npcs.client.gui.swing;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.lwjgl.opengl.Display;

import net.minecraft.client.Minecraft;
import noppes.npcs.client.gui.util.IJTextAreaListener;

public class GuiJTextArea extends JDialog implements WindowListener {
	private static final long serialVersionUID = 4L;
	private JTextArea area;
	public IJTextAreaListener listener;

	public GuiJTextArea(String text) {
		this.setDefaultCloseOperation(2);
		this.setSize(Display.getWidth() - 40, Display.getHeight() - 40);
		this.setLocation(Display.getX() + 20, Display.getY() + 20);
		JScrollPane scroll = new JScrollPane(this.area = new JTextArea(text));
		scroll.setVerticalScrollBarPolicy(22);
		this.add(scroll);
		this.addWindowListener(this);
		this.setVisible(true);
	}

	public GuiJTextArea setListener(IJTextAreaListener listener) {
		this.listener = listener;
		return this;
	}

	@Override
	public void windowActivated(WindowEvent e) {
	}

	@Override
	public void windowClosed(WindowEvent e) {
		if (this.listener == null) {
			return;
		}
		Minecraft.getMinecraft().addScheduledTask(() -> this.listener.saveText(this.area.getText()));
	}

	@Override
	public void windowClosing(WindowEvent e) {
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
	}

	@Override
	public void windowIconified(WindowEvent e) {
	}

	@Override
	public void windowOpened(WindowEvent e) {
	}
}
