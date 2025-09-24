package noppes.npcs.client.gui.model;

import java.util.ArrayList;
import java.util.List;

import noppes.npcs.client.controllers.Preset;
import noppes.npcs.client.controllers.PresetController;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.containers.ContainerLayer;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class GuiCreationLoad extends GuiCreationScreenInterface implements ICustomScrollListener {

	protected final List<String> list = new ArrayList<>();
	protected GuiCustomScroll scroll;

	public GuiCreationLoad(EntityNPCInterface npc, ContainerLayer container) {
		super(npc, container);

		active = 5;
		xOffset = 60;
		PresetController.instance.load();
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton == 0 && button.getID() == 10 && scroll.hasSelected()) {
			PresetController.instance.removePreset(scroll.getSelected());
			initGui();
		}
		super.buttonEvent(button, mouseButton);
	}

	@Override
	public void initGui() {
		super.initGui();
		if (scroll == null) { scroll = new GuiCustomScroll(this, 0); }
		list.clear();
		for (Preset preset : PresetController.instance.presets.values()) { list.add(preset.name); }
		scroll.guiLeft = guiLeft;
		scroll.guiTop = guiTop + 45;
		addScroll(scroll.setList(list).setSize(100, ySize - 96));
		addButton(new GuiNpcButton(10, guiLeft, guiTop + ySize - 46, 120, 20, "gui.remove"));
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		Preset preset = PresetController.instance.getPreset(scroll.getSelected());
		playerdata.load(preset.data.save());
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) { }

}
