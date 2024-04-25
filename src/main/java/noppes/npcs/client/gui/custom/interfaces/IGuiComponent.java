package noppes.npcs.client.gui.custom.interfaces;

import net.minecraft.client.Minecraft;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.client.gui.custom.GuiCustom;

public interface IGuiComponent {

	int getId();

	int[] getPosXY();

	void offSet(int offsetType, double[] windowSize);

	void onRender(Minecraft mc, int mouseX, int mouseY, int mouseWheel, float partialTicks);

	void setParent(GuiCustom gui);

	void setPosXY(int newX, int newY);

	ICustomGuiComponent toComponent();

}
