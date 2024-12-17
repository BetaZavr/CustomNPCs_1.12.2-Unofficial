package noppes.npcs.client.gui.model;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.client.controllers.Preset;
import noppes.npcs.client.controllers.PresetController;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.model.part.ModelData;

import javax.annotation.Nonnull;

public class GuiPresetSave
extends SubGuiInterface {

	private final ModelData data;
	public GuiScreen parent;

	public GuiPresetSave(GuiScreen parent, ModelData data) {
		this.data = data;
		this.parent = parent;
		this.xSize = 200;
		this.drawDefaultBackground = true;
	}

	@Override
	protected void actionPerformed(@Nonnull GuiButton btn) {
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
		GuiNpcTextField textField = new GuiNpcTextField(0, this, this.guiLeft, this.guiTop + 70, 200, 20, "");
		textField.setHoverText("display.hover.part.name");
		addTextField(textField);
		GuiNpcButton button = new GuiNpcButton(0, this.guiLeft, this.guiTop + 100, 98, 20, "Save");
		button.setHoverText("hover.save");
		addButton(button);
		button = new GuiNpcButton(1, this.guiLeft + 100, this.guiTop + 100, 98, 20, "Cancel");
		button.setHoverText("hover.back");
		addButton(button);
	}

}
