package noppes.npcs.client.gui.util;

import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.input.Keyboard;

public class SubGuiInterface extends GuiNPCInterface {

	public int id;
	public GuiScreen parent;
	public Object object;

	public SubGuiInterface(int id) { this(id, null); }

	public SubGuiInterface(int idIn, EntityNPCInterface npc) {
		super(npc);
		id = idIn;
	}

	@Override
	public void onClosed() {
		GuiNpcTextField.unfocus();
		save();
		IEditNPC screen = null;
		if (parent instanceof IEditNPC) { screen = (IEditNPC) parent; }
		else if (mc.currentScreen instanceof IEditNPC) { screen = (IEditNPC) mc.currentScreen; }
		if (screen != null) {
			screen.subGuiClosed(this);
			screen.setSubGui(null);
			while (screen instanceof SubGuiInterface && ((SubGuiInterface) screen).parent instanceof IEditNPC) { screen = (IEditNPC) ((SubGuiInterface) screen).parent; }
			displayGuiScreen((GuiScreen) screen);
			return;
		}
		displayGuiScreen(null);
		mc.setIngameFocus();
	}

	@Override
	public boolean keyCnpcsPressed(char typedChar, int keyCode) {
		boolean bo = super.keyCnpcsPressed(typedChar, keyCode);
		if (!bo && !hasSubGui() && keyCode == Keyboard.KEY_ESCAPE) {
			onClosed();
			return true;
		}
		return bo;
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
