package noppes.npcs.client.model.part;

import net.minecraft.entity.Entity;

public class AnimData{

	public int part = -1;
	public boolean showArmor = true;
	public Entity displayNpc = null;
	public float red = 1.0f;
	public float green = 1.0f;
	public float blue = 1.0f;
	public float alpha = 0.25f;
	public boolean isNPC = false;

	public void clear() {
		part = -1;
		showArmor = true;
		displayNpc = null;
		red = 1.0f;
		green = 1.0f;
		blue = 1.0f;
		alpha = 0.25f;
		isNPC = false;
	}

}
