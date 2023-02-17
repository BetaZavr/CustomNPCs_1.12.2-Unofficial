package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

public class Poses {

	public int x;
	public int y;

	public Poses(GuiScreen gui, int i) {
		ScaledResolution scaleW = null;
		if (gui.mc != null) {
			scaleW = new ScaledResolution(gui.mc);
		} else {
			scaleW = new ScaledResolution(Minecraft.getMinecraft());
		}
		this.x = scaleW.getScaledWidth() / 2;
		this.y = scaleW.getScaledHeight() / 2 - 30;
		if (i == 0) {
			this.y -= 15;
		} else if (i == 1) {
			this.x -= 11;
			this.y -= 11;
		} else if (i == 2) {
			this.x -= 15;
		} else if (i == 3) {
			this.x -= 11;
			this.y += 11;
		} else if (i == 4) {
			this.y += 15;
		} else if (i == 5) {
			this.x += 11;
			this.y += 11;
		} else if (i == 6) {
			this.x += 15;
		} else if (i == 7) {
			this.x += 11;
			this.y -= 11;
		}
	}

}
