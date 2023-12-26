package noppes.npcs.client.gui.model;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.controllers.Preset;
import noppes.npcs.client.controllers.PresetController;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.model.part.ModelData;

public class GuiPresetSave
extends SubGuiInterface {
	
	private ModelData data;
	public GuiScreen parent;

	public GuiPresetSave(GuiScreen parent, ModelData data) {
		this.data = data;
		this.parent = parent;
		this.xSize = 200;
		this.drawDefaultBackground = true;
	}

	@Override
	protected void actionPerformed(GuiButton btn) {
		super.actionPerformed(btn);
		GuiNpcButton button = (GuiNpcButton) btn;
		if (button.id == 0) {
			String name = this.getTextField(0).getText().trim();
			if (name.isEmpty()) {
				return;
			}
			Preset preset = new Preset();
			preset.name = name;
			preset.data = this.data.copy();
			PresetController.instance.addPreset(preset);
		}
		this.close();
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addTextField(new GuiNpcTextField(0, this, this.guiLeft, this.guiTop + 70, 200, 20, ""));
		this.addButton(new GuiNpcButton(0, this.guiLeft, this.guiTop + 100, 98, 20, "Save"));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 100, this.guiTop + 100, 98, 20, "Cancel"));
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.getTextField(0)!=null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.name").getFormattedText());
		} else if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.save").getFormattedText());
		} else if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
	}
	
}
