package noppes.npcs.client.gui.util;

import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.entity.EntityNPCInterface;

public class SubGuiInterface
extends GuiNPCInterface {

	public int id;
	public GuiScreen parent;
	public Object object;

	public SubGuiInterface() {
		super(null);
	}

	public SubGuiInterface(EntityNPCInterface npc) {
		super(npc);
	}

	@Override
	public void close() {
		save();
		ISubGuiListener screen = null;
		if (parent instanceof ISubGuiListener) { screen = (ISubGuiListener) parent; }
		else if (mc.currentScreen instanceof ISubGuiListener) { screen = (ISubGuiListener) mc.currentScreen; }
		if (screen != null) {
			screen.subGuiClosed(this);
			if (screen instanceof GuiNPCInterface) { ((GuiNPCInterface) screen).setSubGui(null); }
			else if (screen instanceof GuiContainerNPCInterface) { ((GuiContainerNPCInterface) screen).setSubGui(null); }
			displayGuiScreen((GuiScreen) screen);
			return;
		}
		displayGuiScreen(null);
		mc.setIngameFocus();
	}

	public int getId() { return id; }

	public GuiScreen getParent() {
		if (parent instanceof SubGuiInterface) { return ((SubGuiInterface) parent).getParent(); }
		return parent;
	}

	public void setParent(GuiScreen gui) { parent = gui; }

	public Object getObject() { return object; }

	public void setObject(Object obj) { object = obj; }

}
