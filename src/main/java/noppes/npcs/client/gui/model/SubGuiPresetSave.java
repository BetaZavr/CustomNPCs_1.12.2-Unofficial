package noppes.npcs.client.gui.model;

import net.minecraft.client.gui.GuiScreen;
import noppes.npcs.client.controllers.Preset;
import noppes.npcs.client.controllers.PresetController;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.client.model.part.ModelData;

import javax.annotation.Nonnull;

public class SubGuiPresetSave extends SubGuiInterface {

	protected final ModelData data;

	public SubGuiPresetSave(GuiScreen parentIn, ModelData dataIn) {
		super(0);
		drawDefaultBackground = true;
		parent = parentIn;
		xSize = 200;

		data = dataIn;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		if (button.id == 0) {
			String name = this.getTextField(0).getText().trim();
			if (name.isEmpty()) { return; }
			Preset preset = new Preset();
			preset.name = name;
			preset.data = this.data.copy();
			PresetController.instance.addPreset(preset);
		}
		onClosed();
	}

	@Override
	public void initGui() {
		super.initGui();
		addTextField(new GuiNpcTextField(0, this, this.guiLeft, this.guiTop + 70, 200, 20, "")
				.setHoverText("display.hover.part.name"));
		addButton(new GuiNpcButton(0, this.guiLeft, this.guiTop + 100, 98, 20, "Save")
				.setHoverText("hover.save"));
		addButton(new GuiNpcButton(1, this.guiLeft + 100, this.guiTop + 100, 98, 20, "Cancel")
				.setHoverText("hover.back"));
	}

}
