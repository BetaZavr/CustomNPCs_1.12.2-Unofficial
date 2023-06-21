package noppes.npcs.client.gui.custom.interfaces;

import noppes.npcs.client.gui.custom.GuiCustom;

public interface IClickListener extends IGuiComponent {
	
	boolean mouseClicked(GuiCustom gui, int mouseX, int mouseY, int mouseButton);
	
}
