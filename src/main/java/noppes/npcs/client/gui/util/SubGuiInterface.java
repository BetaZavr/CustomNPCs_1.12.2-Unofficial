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
		this.save();
		if (this.parent instanceof ISubGuiListener) {
			((ISubGuiListener) this.parent).subGuiClosed(this);
		}
		if (this.parent instanceof GuiNPCInterface) {
			((GuiNPCInterface) this.parent).closeSubGui(this);
		} else if (this.parent instanceof GuiContainerNPCInterface) {
			((GuiContainerNPCInterface) this.parent).closeSubGui(this);
		} else if (this.parent instanceof ISubGuiListener) {
			((ISubGuiListener) this.parent).subGuiClosed(this);
		} else {
			super.close();
		}
	}

	public GuiScreen getParent() {
		if (this.parent instanceof SubGuiInterface) {
			return ((SubGuiInterface) this.parent).getParent();
		}
		return this.parent;
	}

	@Override
	public void save() {
	}
}
