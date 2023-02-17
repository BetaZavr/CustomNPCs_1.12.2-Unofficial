package noppes.npcs.client.gui.custom.interfaces;

import net.minecraft.client.Minecraft;
import noppes.npcs.api.gui.ICustomGuiComponent;

public interface IGuiComponent {
	int getID();

	void onRender(Minecraft p0, int p1, int p2, int p3, float p4);

	ICustomGuiComponent toComponent();
}
