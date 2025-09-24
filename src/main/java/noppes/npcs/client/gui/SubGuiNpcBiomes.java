package noppes.npcs.client.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.biome.Biome;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.data.SpawnData;
import noppes.npcs.reflection.world.biome.BiomeReflection;

import javax.annotation.Nonnull;

public class SubGuiNpcBiomes extends SubGuiInterface implements ICustomScrollListener {

	protected final SpawnData data;
	protected GuiCustomScroll scroll1;
	protected GuiCustomScroll scroll2;

	public SubGuiNpcBiomes(SpawnData spawnData) {
		super(0);
		setBackground("menubg.png");
		xSize = 346;
		ySize = 216;
		closeOnEsc = true;

		data = spawnData;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 1: {
				if (scroll1.hasSelected()) {
					data.biomes.add(scroll1.getSelected());
					scroll1.setSelect(-1);
					initGui();
				}
				break;
			}
			case 2: {
				if (scroll2.hasSelected()) {
					data.biomes.remove(scroll2.getSelected());
					scroll1.setSelect(-1);
					initGui();
				}
				break;
			}
			case 3: {
				data.biomes.clear();
				for (Biome biome : Biome.REGISTRY) {
					if (biome != null) { data.biomes.add(BiomeReflection.getBiomeName(biome)); }
				}
				scroll1.setSelect(-1);
				initGui();
				break;
			}
			case 4: {
				data.biomes.clear();
				scroll1.setSelect(-1);
				initGui();
				break;
			}
			case 66: onClosed(); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		if (scroll1 == null) { scroll1 = new GuiCustomScroll(this, 0).setSize(140, 180); }
		scroll1.guiLeft = guiLeft + 4;
		scroll1.guiTop = guiTop + 14;
		addScroll(scroll1);
		addLabel(new GuiNpcLabel(1, "spawning.availableBiomes", guiLeft + 4, guiTop + 4));
		if (scroll2 == null) { scroll2 = new GuiCustomScroll(this, 1).setSize(140, 180); }
		scroll2.guiLeft = guiLeft + 200;
		scroll2.guiTop = guiTop + 14;
		addScroll(scroll2);
		addLabel(new GuiNpcLabel(2, "spawning.spawningBiomes", guiLeft + 200, guiTop + 4));
		List<String> biomes = new ArrayList<>();
		for (Biome biome : Biome.REGISTRY) {
			String name = BiomeReflection.getBiomeName(biome);
			if (name != null && !data.biomes.contains(name)) { biomes.add(name); }
		}
		scroll1.setList(biomes);
		scroll2.setList(data.biomes);
		int x = guiLeft + 145;
		int y = guiTop + 40;
		addButton(new GuiNpcButton(1, x, y, 55, 20, ">"));
		addButton(new GuiNpcButton(2, x, y += 22, 55, 20, "<"));
		addButton(new GuiNpcButton(3, x, y += 28, 55, 20, ">>"));
		addButton(new GuiNpcButton(4, x, y + 22, 55, 20, "<<"));
		addButton(new GuiNpcButton(66, guiLeft + 260, guiTop + 194, 60, 20, "gui.done").setHoverText("hover.back"));
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (!scroll.hasSelected()) { return; }
		if (scroll.getID() == 1) {
			data.biomes.add(scroll.getSelected());
			scroll.setSelect(-1);
			initGui();
		}
		if (scroll.getID() == 2) {
			data.biomes.remove(scroll.getSelected());
			scroll.setSelect(-1);
			initGui();
		}
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { }

}
