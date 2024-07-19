package noppes.npcs.client.gui.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.controllers.Preset;
import noppes.npcs.client.controllers.PresetController;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class GuiCreationLoad extends GuiCreationScreenInterface implements ICustomScrollListener {

	private final List<String> list;
	private GuiCustomScroll scroll;

	public GuiCreationLoad(EntityNPCInterface npc) {
		super(npc);
		this.list = new ArrayList<>();
		this.active = 5;
		this.xOffset = 60;
		PresetController.instance.load();
	}

	@Override
	protected void actionPerformed(@Nonnull GuiButton btn) {
		super.actionPerformed(btn);
		if (btn.id == 10 && this.scroll.hasSelected()) {
			PresetController.instance.removePreset(this.scroll.getSelected());
			this.initGui();
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.ShowDescriptions) {
			return;
		}
		if (this.getButton(1) != null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.entity").getFormattedText());
		} else if (this.getButton(2) != null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.parts").getFormattedText());
		} else if (this.getButton(3) != null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.size").getFormattedText());
		} else if (this.getButton(4) != null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.save").getFormattedText());
		} else if (this.getButton(5) != null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.load").getFormattedText());
		} else if (this.getButton(10) != null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.delete").getFormattedText());
		} else if (this.getButton(66) != null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		} else {
			for (GuiButton b : this.buttonList) {
				if (b != null && b.isMouseOver()) {
					if (b.id == 500) {
						this.setHoverText(new TextComponentTranslation("display.hover.part.rotate").getFormattedText());
					}
				}
			}
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		if (this.scroll == null) {
			this.scroll = new GuiCustomScroll(this, 0);
		}
		this.list.clear();
		for (Preset preset : PresetController.instance.presets.values()) {
			this.list.add(preset.name);
		}
		this.scroll.setList(this.list);
		this.scroll.guiLeft = this.guiLeft;
		this.scroll.guiTop = this.guiTop + 45;
		this.scroll.setSize(100, this.ySize - 96);
		this.addScroll(this.scroll);
		this.addButton(new GuiNpcButton(10, this.guiLeft, this.guiTop + this.ySize - 46, 120, 20, "gui.remove"));
	}

	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
		Preset preset = PresetController.instance.getPreset(scroll.getSelected());
		this.playerdata.readFromNBT(preset.data.writeToNBT());
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
	}

}
