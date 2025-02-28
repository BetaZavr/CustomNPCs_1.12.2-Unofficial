package noppes.npcs.client.gui.util;

import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.entity.EntityNPCInterface;

public class SubGuiInterface
extends GuiNPCInterface
implements ISubGuiInterface {

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
		if (parent instanceof IEditNPC) {
			((IEditNPC) parent).closeSubGui(this);
			displayGuiScreen(parent);
		}
		if (parent instanceof ISubGuiListener) {
			((ISubGuiListener) parent).subGuiClosed(this);
			displayGuiScreen(parent);
		}
		else {
			if (mc.currentScreen != null) {
				if (mc.currentScreen instanceof IEditNPC) {
					((IEditNPC) mc.currentScreen).closeSubGui(this);
					displayGuiScreen(mc.currentScreen);
				}
				if (mc.currentScreen instanceof ISubGuiListener) {
					((ISubGuiListener) mc.currentScreen).subGuiClosed(this);
					displayGuiScreen(mc.currentScreen);
				}
				return;
			}
			super.close();
		}
	}

	@Override
	public int getId() { return id; }

	@Override
	public GuiScreen getParent() {
		if (parent instanceof SubGuiInterface) { return ((SubGuiInterface) parent).getParent(); }
		return parent;
	}

	@Override
	public void setParent(GuiScreen gui) { parent = gui; }

	@Override
	public Object getObject() { return object; }

	@Override
	public void setObject(Object obj) { object = obj; }

	@Override
	public void save() { }

}
