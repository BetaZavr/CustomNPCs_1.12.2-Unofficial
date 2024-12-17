package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

public class Poses {

	public int x;
	public int y;

	public Poses(GuiScreen gui, int i) {
		ScaledResolution scaleW;
		if (gui.mc != null) {
			scaleW = new ScaledResolution(gui.mc);
		} else {
			scaleW = new ScaledResolution(Minecraft.getMinecraft());
		}
		x = scaleW.getScaledWidth() / 2;
		y = scaleW.getScaledHeight() / 2 - 30;
		if (i == 0) {
			y -= 15;
		} else if (i == 1) {
			x -= 11;
			y -= 11;
		} else if (i == 2) {
			x -= 15;
		} else if (i == 3) {
			x -= 11;
			y += 11;
		} else if (i == 4) {
			y += 15;
		} else if (i == 5) {
			x += 11;
			y += 11;
		} else if (i == 6) {
			x += 15;
		} else if (i == 7) {
			x += 11;
			y -= 11;
		}
	}

}
