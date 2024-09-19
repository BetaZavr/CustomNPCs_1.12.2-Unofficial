package noppes.npcs.client.gui.select;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundRegistry;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.mixin.api.client.audio.SoundHandlerAPIMixin;

import javax.annotation.Nonnull;

public class GuiSoundSelection extends SubGuiInterface implements ICustomScrollListener {

	private final HashMap<String, List<String>> domains;
	private GuiCustomScroll scrollCategories;
	private GuiCustomScroll scrollQuests;
	private String selectedDomain;
	public ResourceLocation selectedResource;
	private String oldPlay = "";

	public GuiSoundSelection(String sound) {
		this.domains = new HashMap<>();
		this.drawDefaultBackground = false;
		this.title = "";
		this.setBackground("menubg.png");
		this.xSize = 366;
		this.ySize = 226;
		SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
		SoundRegistry registry = ((SoundHandlerAPIMixin) handler).npcs$getSoundRegistry();
		if (registry != null) {
			Set<ResourceLocation> set = registry.getKeys();
			for (ResourceLocation location : set) {
				List<String> list = this.domains.computeIfAbsent(location.getResourceDomain(), k -> new ArrayList<>());
				list.add(location.getResourcePath());
				this.domains.put(location.getResourceDomain(), list);
			}
		}
		if (sound != null && !sound.isEmpty()) {
			this.selectedResource = new ResourceLocation(sound);
			this.selectedDomain = this.selectedResource.getResourceDomain();
			if (!this.domains.containsKey(this.selectedDomain)) {
				this.selectedDomain = null;
			}
		}
	}

	@Override
	public void actionPerformed(@Nonnull GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		if (button.id == 1 && MusicController.Instance.isPlaying(this.oldPlay)) {
			MusicController.Instance.stopSound(this.oldPlay, SoundCategory.PLAYERS);
			this.oldPlay = "";
			return;
		}
		super.actionPerformed(guibutton);
		if (button.id == 1) {
			BlockPos pos = this.player.getPosition();
			MusicController.Instance.playSound(SoundCategory.PLAYERS, this.selectedResource.toString(), pos.getX(),
					pos.getY(), pos.getZ(), 1.0f, 1.0f);
			this.oldPlay = this.selectedResource.toString();
		}
		if (button.id == 2) {
			this.close();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addButton(new GuiNpcButton(2, this.guiLeft + this.xSize - 26, this.guiTop + 4, 20, 20, "X"));
		this.addButton(new GuiNpcButton(1, this.guiLeft + 160, this.guiTop + 212, 70, 20, "gui.play",
				this.selectedResource != null));
		this.getButton(1).hasSound = false;
		if (this.scrollCategories == null) {
			(this.scrollCategories = new GuiCustomScroll(this, 0)).setSize(90, 200);
		}
		this.scrollCategories.setList(Lists.newArrayList(this.domains.keySet()));
		if (this.selectedDomain != null) {
			this.scrollCategories.setSelected(this.selectedDomain);
		}
		this.scrollCategories.guiLeft = this.guiLeft + 4;
		this.scrollCategories.guiTop = this.guiTop + 14;
		this.addScroll(this.scrollCategories);
		if (this.scrollQuests == null) {
			(this.scrollQuests = new GuiCustomScroll(this, 1)).setSize(250, 200);
		}
		if (this.selectedDomain != null) {
			this.scrollQuests.setList(this.domains.get(this.selectedDomain));
		}
		if (this.selectedResource != null) {
			this.scrollQuests.setSelected(this.selectedResource.getResourcePath());
		}
		this.scrollQuests.guiLeft = this.guiLeft + 95;
		this.scrollQuests.guiTop = this.guiTop + 14;
		this.addScroll(this.scrollQuests);
	}

	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
		if (scroll.id == 0) {
			this.selectedDomain = scroll.getSelected();
			this.selectedResource = null;
			this.scrollQuests.selected = -1;
		}
		if (scroll.id == 1) {
			this.selectedResource = new ResourceLocation(this.selectedDomain, scroll.getSelected());
		}
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
		if (this.selectedResource == null) {
			return;
		}
		this.close();
	}

	public void updateScreen() {
		if (this.selectedResource != null && this.getButton(1) != null) {
			boolean pl = MusicController.Instance.isPlaying(this.oldPlay);
			this.getButton(1).setDisplayText(pl ? "gui.stop" : "gui.play");
			if (!pl) {
				this.oldPlay = "";
			}
		}
	}

}
