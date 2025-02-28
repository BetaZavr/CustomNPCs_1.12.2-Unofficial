package noppes.npcs.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.biome.Biome;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.data.SpawnData;
import noppes.npcs.api.mixin.world.biome.IBiomeMixin;

public class SubGuiNpcBiomes
extends SubGuiInterface {

	private final SpawnData data;
	private GuiCustomScroll scroll1;
	private GuiCustomScroll scroll2;

	public SubGuiNpcBiomes(SpawnData spawnData) {
		setBackground("menubg.png");
		xSize = 346;
		ySize = 216;
		closeOnEsc = true;

		data = spawnData;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (button.getId() == 1 && scroll1.hasSelected()) {
			data.biomes.add(scroll1.getSelected());
			scroll1.setSelect(-1);
            initGui();
		}
		if (button.getId() == 2 && scroll2.hasSelected()) {
			data.biomes.remove(scroll2.getSelected());
			scroll1.setSelect(-1);
			initGui();
		}
		if (button.getId() == 3) {
			data.biomes.clear();
			for (Biome base : Biome.REGISTRY) {
				if (base != null) {
					data.biomes.add(((IBiomeMixin) base).npcs$getBiomeName());
				}
			}
			scroll1.setSelect(-1);
			initGui();
		}
		if (button.getId() == 4) {
			data.biomes.clear();
			scroll1.setSelect(-1);
			initGui();
		}
		if (button.getId() == 66) {
			close();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		if (scroll1 == null) {
			(scroll1 = new GuiCustomScroll(this, 0)).setSize(140, 180);
		}
		scroll1.guiLeft = guiLeft + 4;
		scroll1.guiTop = guiTop + 14;
		addScroll(scroll1);
		addLabel(new GuiNpcLabel(1, "spawning.availableBiomes", guiLeft + 4, guiTop + 4));
		if (scroll2 == null) {
			(scroll2 = new GuiCustomScroll(this, 1)).setSize(140, 180);
		}
		scroll2.guiLeft = guiLeft + 200;
		scroll2.guiTop = guiTop + 14;
		addScroll(scroll2);
		addLabel(new GuiNpcLabel(2, "spawning.spawningBiomes", guiLeft + 200, guiTop + 4));
		List<String> biomes = new ArrayList<>();
		for (Biome base : Biome.REGISTRY) {
			String name = ((IBiomeMixin) base).npcs$getBiomeName();
			if (name != null && !data.biomes.contains(name)) {
				biomes.add(name);
			}
		}
		scroll1.setList(biomes);
		scroll2.setList(data.biomes);
		addButton(new GuiNpcButton(1, guiLeft + 145, guiTop + 40, 55, 20, ">"));
		addButton(new GuiNpcButton(2, guiLeft + 145, guiTop + 62, 55, 20, "<"));
		addButton(new GuiNpcButton(3, guiLeft + 145, guiTop + 90, 55, 20, ">>"));
		addButton(new GuiNpcButton(4, guiLeft + 145, guiTop + 112, 55, 20, "<<"));
		GuiNpcButton button = new GuiNpcButton(66, guiLeft + 260, guiTop + 194, 60, 20, "gui.done");
		button.setHoverText("hover.back");
		addButton(button);
	}
}
