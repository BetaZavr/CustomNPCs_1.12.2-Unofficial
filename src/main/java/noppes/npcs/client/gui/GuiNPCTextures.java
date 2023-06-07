package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCTextures extends GuiNpcSelectionInterface {
	public GuiNPCTextures(EntityNPCInterface npc, GuiScreen parent) {
		super(npc, parent, npc.display.getSkinTexture());
		this.title = "Select Texture";
		this.parent = parent;
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		int l = -50;
		int i2 = this.height / 2 + 30;
		this.drawNpc(this.npc, l, i2, 2.0f, 0, 0, true);
		super.drawScreen(i, j, f);
	}

	@Override
	public void elementClicked() {
		if (this.dataTextures.contains(this.slot.selected) && this.slot.selected != null) {
			this.npc.display.setSkinTexture(this.assets.getAsset(this.slot.selected));
			this.npc.textureLocation = null;
		}
	}

	@Override
	public String[] getExtension() {
		return new String[] { "png" };
	}

	@Override
	public void initGui() {
		super.initGui();
		int index = this.npc.display.getSkinTexture().lastIndexOf("/");
		if (index > 0) {
			String asset = this.npc.display.getSkinTexture().substring(index + 1);
			if (this.npc.display.getSkinTexture().equals(this.assets.getAsset(asset))) {
				this.slot.selected = asset;
			}
		}
	}

	@Override
	public void save() {
	}
}
