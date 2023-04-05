package noppes.npcs.client.gui.custom.interfaces;

import net.minecraft.client.Minecraft;
import noppes.npcs.api.gui.ICustomGuiComponent;

public interface IGuiComponent {
	
	int getID();

	void onRender(Minecraft mc, int mouseX, int mouseY, int mouseWheel, float partialTicks);

	ICustomGuiComponent toComponent();

	void offSet(int offsetType, double[] windowSize);
	
}
