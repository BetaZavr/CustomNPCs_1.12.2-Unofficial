package noppes.npcs.client.gui.select;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundRegistry;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.api.mixin.client.audio.ISoundHandlerMixin;

import javax.annotation.Nonnull;

public class SubGuiSoundSelection extends SubGuiInterface implements ICustomScrollListener {

	protected final HashMap<String, List<String>> domains = new HashMap<>();
	protected GuiCustomScroll scrollCategories;
	protected GuiCustomScroll scrollQuests;
	protected String selectedDomain;
	protected String oldPlay = "";
	public ResourceLocation selectedResource;

	public SubGuiSoundSelection(String sound) {
		super(0);
		setBackground("menubg.png");
		drawDefaultBackground = false;
		title = "";
		xSize = 366;
		ySize = 226;

		SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
		SoundRegistry registry = ((ISoundHandlerMixin) handler).npcs$getSoundRegistry();
		if (registry != null) {
			Set<ResourceLocation> set = registry.getKeys();
			for (ResourceLocation location : set) {
				List<String> list = domains.computeIfAbsent(location.getResourceDomain(), k -> new ArrayList<>());
				list.add(location.getResourcePath());
				domains.put(location.getResourceDomain(), list);
			}
		}
		if (sound != null && !sound.isEmpty()) {
			selectedResource = new ResourceLocation(sound);
			selectedDomain = selectedResource.getResourceDomain();
			if (!domains.containsKey(selectedDomain)) { selectedDomain = null; }
		}
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 1: {
				if (MusicController.Instance.isPlaying(oldPlay)) {
					MusicController.Instance.stopSound(oldPlay, SoundCategory.PLAYERS);
					oldPlay = "";
					return;
				}
				BlockPos pos = player.getPosition();
				MusicController.Instance.playSound(SoundCategory.PLAYERS, selectedResource.toString(), pos.getX(), pos.getY(), pos.getZ(), 1.0f, 1.0f);
				oldPlay = selectedResource.toString();
				break;
			}
			case 2: onClosed(); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		addButton(new GuiNpcButton(2, guiLeft + xSize - 26, guiTop + 4, 20, 20, "X"));
		addButton(new GuiNpcButton(1, guiLeft + 160, guiTop + 212, 70, 20, "gui.play", selectedResource != null)
				.setHasSound(false));
		if (scrollCategories == null) { scrollCategories = new GuiCustomScroll(this, 0).setSize(90, 200); }
		scrollCategories.setList(new ArrayList<>(domains.keySet()));
		if (selectedDomain != null) { scrollCategories.setSelected(selectedDomain); }
		scrollCategories.guiLeft = guiLeft + 4;
		scrollCategories.guiTop = guiTop + 14;
		addScroll(scrollCategories);
		if (scrollQuests == null) { scrollQuests = new GuiCustomScroll(this, 1).setSize(250, 200); }
		if (selectedDomain != null) { scrollQuests.setList(domains.get(selectedDomain)); }
		if (selectedResource != null) { scrollQuests.setSelected(selectedResource.getResourcePath()); }
		scrollQuests.guiLeft = guiLeft + 95;
		scrollQuests.guiTop = guiTop + 14;
		addScroll(scrollQuests);
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.getID() == 0) {
			selectedDomain = scroll.getSelected();
			selectedResource = null;
			scrollQuests.setSelect(-1);
		}
		if (scroll.getID() == 1) {
			selectedResource = new ResourceLocation(selectedDomain, scroll.getSelected());
		}
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		if (selectedResource == null) { return; }
		onClosed();
	}

	public void updateScreen() {
		if (selectedResource != null && getButton(1) != null) {
			boolean pl = MusicController.Instance.isPlaying(oldPlay);
			getButton(1).setDisplayText(pl ? "gui.stop" : "gui.play");
			if (!pl) { oldPlay = ""; }
		}
	}

}
