package noppes.npcs.client.gui.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ModelPartConfig;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcSlider;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ISliderListener;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class GuiCreationScale extends GuiCreationScreenInterface implements ISliderListener, ICustomScrollListener {

	private static EnumParts selected = EnumParts.HEAD;
	private final List<EnumParts> data = new ArrayList<>();
	private GuiCustomScroll scroll;

	public GuiCreationScale(EntityNPCInterface npc) {
		super(npc);
		this.active = 3;
		this.xOffset = 140;
	}

	@Override
	protected void actionPerformed(@Nonnull GuiButton guibutton) {
		super.actionPerformed(guibutton);
		if (guibutton.id == 13) {
            this.playerdata.getPartConfig(GuiCreationScale.selected).notShared = ((GuiNpcButton) guibutton).getValue() == 0;
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
		} else if (this.getButton(13) != null && this.getButton(13).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.pattern").getFormattedText());
		} else if (this.getButton(66) != null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		} else {
			for (GuiButton b : this.buttonList) {
				if (b != null && b.isMouseOver()) {
					if (b.id == 10) {
						this.setHoverText(new TextComponentTranslation("hover.scale.x").getFormattedText());
					} else if (b.id == 11) {
						this.setHoverText(new TextComponentTranslation("hover.scale.y").getFormattedText());
					} else if (b.id == 12) {
						this.setHoverText(new TextComponentTranslation("hover.scale.z").getFormattedText());
					} else if (b.id == 500) {
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
		List<String> list = new ArrayList<>();
		EnumParts[] parts = { EnumParts.HEAD, EnumParts.BODY, EnumParts.ARM_LEFT, EnumParts.ARM_RIGHT, EnumParts.LEG_LEFT, EnumParts.LEG_RIGHT };
		this.data.clear();
		for (EnumParts part : parts) {
			Label_0210: {
				if (part == EnumParts.ARM_RIGHT) {
					ModelPartConfig config = this.playerdata.getPartConfig(EnumParts.ARM_LEFT);
					if (!config.notShared) {
						break Label_0210;
					}
				}
				if (part == EnumParts.LEG_RIGHT) {
					ModelPartConfig config = this.playerdata.getPartConfig(EnumParts.LEG_LEFT);
					if (!config.notShared) {
						break Label_0210;
					}
				}
				this.data.add(part);
				list.add(new TextComponentTranslation("part." + part.name).getFormattedText());
			}
		}
		this.scroll.setListNotSorted(list);
		this.scroll
				.setSelected(new TextComponentTranslation("part." + GuiCreationScale.selected.name).getFormattedText());
		this.scroll.guiLeft = this.guiLeft;
		this.scroll.guiTop = this.guiTop + 46;
		this.scroll.setSize(100, this.ySize - 74);
		this.addScroll(this.scroll);
		ModelPartConfig config2 = this.playerdata.getPartConfig(GuiCreationScale.selected);
		int y = this.guiTop + 65;
		this.addLabel(new GuiNpcLabel(10, "scale.width", this.guiLeft + 102, y + 5, 16777215));
		this.addSlider(new GuiNpcSlider(this, 10, this.guiLeft + 150, y, 100, 20, config2.scale[0] - 0.5f));
		y += 22;
		this.addLabel(new GuiNpcLabel(11, "scale.height", this.guiLeft + 102, y + 5, 16777215));
		this.addSlider(new GuiNpcSlider(this, 11, this.guiLeft + 150, y, 100, 20, config2.scale[1] - 0.5f));
		y += 22;
		this.addLabel(new GuiNpcLabel(12, "scale.depth", this.guiLeft + 102, y + 5, 16777215));
		this.addSlider(new GuiNpcSlider(this, 12, this.guiLeft + 150, y, 100, 20, config2.scale[2] - 0.5f));
		if (GuiCreationScale.selected == EnumParts.ARM_LEFT || GuiCreationScale.selected == EnumParts.LEG_LEFT) {
			y += 22;
			this.addLabel(new GuiNpcLabel(13, "scale.shared", this.guiLeft + 102, y + 5, 16777215));
			this.addButton(new GuiNpcButton(13, this.guiLeft + 150, y, 50, 20, new String[] { "gui.no", "gui.yes" },
					(config2.notShared ? 0 : 1)));
		}
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		super.mouseDragged(slider);
		if (slider.id >= 10 && slider.id <= 12) {
			int percent = (int) (50.0f + slider.sliderValue * 100.0f);
			slider.setString(percent + "%");
			ModelPartConfig config = this.playerdata.getPartConfig(GuiCreationScale.selected);
			if (slider.id == 10) {
				config.scale[0] = slider.sliderValue + 0.5f;
			}
			if (slider.id == 11) {
				config.scale[1] = slider.sliderValue + 0.5f;
			}
			if (slider.id == 12) {
				config.scale[2] = slider.sliderValue + 0.5f;
			}
			this.updateTranslate();
		}
	}

	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
		if (scroll.selected >= 0) {
			GuiCreationScale.selected = this.data.get(scroll.selected);
			this.initGui();
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
	}

	private void updateTranslate() {
		for (EnumParts part : EnumParts.values()) {
			ModelPartConfig config = this.playerdata.getPartConfig(part);
			if (config != null) {
				if (part == EnumParts.HEAD) {
					config.setTranslate(0.0f, this.playerdata.getBodyY(), 0.0f);
				} else if (part == EnumParts.ARM_LEFT) {
					ModelPartConfig body = this.playerdata.getPartConfig(EnumParts.BODY);
					float x = (1.0f - body.scale[0]) * 0.25f + (1.0f - config.scale[0]) * 0.075f;
					float y = this.playerdata.getBodyY() + (1.0f - config.scale[1]) * -0.1f;
					config.setTranslate(-x, y, 0.0f);
					if (!config.notShared) {
						ModelPartConfig arm = this.playerdata.getPartConfig(EnumParts.ARM_RIGHT);
						arm.copyValues(config);
					}
				} else if (part == EnumParts.ARM_RIGHT) {
					ModelPartConfig body = this.playerdata.getPartConfig(EnumParts.BODY);
					float x = (1.0f - body.scale[0]) * 0.25f + (1.0f - config.scale[0]) * 0.075f;
					float y = this.playerdata.getBodyY() + (1.0f - config.scale[1]) * -0.1f;
					config.setTranslate(x, y, 0.0f);
				} else if (part == EnumParts.LEG_LEFT) {
					config.setTranslate(config.scale[0] * 0.125f - 0.113f, this.playerdata.getLegsY(), 0.0f);
					if (!config.notShared) {
						ModelPartConfig leg = this.playerdata.getPartConfig(EnumParts.LEG_RIGHT);
						leg.copyValues(config);
					}
				} else if (part == EnumParts.LEG_RIGHT) {
					config.setTranslate((1.0f - config.scale[0]) * 0.125f, this.playerdata.getLegsY(), 0.0f);
				} else if (part == EnumParts.BODY) {
					config.setTranslate(0.0f, this.playerdata.getBodyY(), 0.0f);
				}
			}
		}
	}

}
