package noppes.npcs.client.gui.model;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.inventory.Slot;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.ModelPartConfig;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.containers.ContainerLayer;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class GuiCreationScale extends GuiCreationScreenInterface implements ISliderListener, ICustomScrollListener {

	protected static EnumParts selected = EnumParts.HEAD;
	protected final List<EnumParts> data = new ArrayList<>();
	protected GuiCustomScroll scroll;

	public GuiCreationScale(EntityNPCInterface npc, ContainerLayer container) {
		super(npc, container);
		active = 3;
		xOffset = 140;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton == 0 && button.getID() == 13) {
            playerdata.getPartConfig(GuiCreationScale.selected).notShared = button.getValue() == 0;
			initGui();
		}
		super.buttonEvent(button, mouseButton);
	}

	@Override
	public void initGui() {
		super.initGui();
		if (scroll == null) { scroll = new GuiCustomScroll(this, 0); }
		List<String> list = new ArrayList<>();
		EnumParts[] parts = { EnumParts.HEAD, EnumParts.BODY, EnumParts.ARM_LEFT, EnumParts.ARM_RIGHT, EnumParts.LEG_LEFT, EnumParts.LEG_RIGHT };
		data.clear();
		for (EnumParts part : parts) {
			Label_0210: {
				if (part == EnumParts.ARM_RIGHT) {
					ModelPartConfig config = playerdata.getPartConfig(EnumParts.ARM_LEFT);
					if (!config.notShared) { break Label_0210; }
				}
				if (part == EnumParts.LEG_RIGHT) {
					ModelPartConfig config = playerdata.getPartConfig(EnumParts.LEG_LEFT);
					if (!config.notShared) { break Label_0210; }
				}
				data.add(part);
				list.add(new TextComponentTranslation("part." + part.name).getFormattedText());
			}
		}
		scroll.guiLeft = guiLeft;
		scroll.guiTop = guiTop + 46;
		scroll.setUnsortedList(list)
				.setSelected(new TextComponentTranslation("part." + GuiCreationScale.selected.name).getFormattedText())
				.setSize(100, ySize - 74);
		addScroll(scroll);
		ModelPartConfig config2 = playerdata.getPartConfig(GuiCreationScale.selected);
		int y = guiTop + 65;
		addLabel(new GuiNpcLabel(10, "scale.width", guiLeft + 102, y + 5, 16777215));
		addSlider(new GuiNpcSlider(this, 10, guiLeft + 150, y, 100, 20, config2.scale[0] - 0.5f)
				.setHoverText(new TextComponentTranslation("hover.scale.x").getFormattedText()));
		addLabel(new GuiNpcLabel(11, "scale.height", guiLeft + 102, (y += 22) + 5, 16777215));
		addSlider(new GuiNpcSlider(this, 11, guiLeft + 150, y, 100, 20, config2.scale[1] - 0.5f)
				.setHoverText(new TextComponentTranslation("hover.scale.y").getFormattedText()));
		addLabel(new GuiNpcLabel(12, "scale.depth", guiLeft + 102, (y += 22) + 5, 16777215));
		addSlider(new GuiNpcSlider(this, 12, guiLeft + 150, y, 100, 20, config2.scale[2] - 0.5f)
				.setHoverText(new TextComponentTranslation("hover.scale.z").getFormattedText()));
		if (GuiCreationScale.selected == EnumParts.ARM_LEFT || GuiCreationScale.selected == EnumParts.LEG_LEFT) {
			addLabel(new GuiNpcLabel(13, "scale.shared", guiLeft + 102, (y += 22) + 5, 16777215));
			addButton(new GuiNpcButton(13, guiLeft + 150, y, 50, 20, new String[] { "gui.no", "gui.yes" }, (config2.notShared ? 0 : 1))
					.setHoverText("display.hover.part.pattern"));
		}
		for (Slot slot : inventorySlots.inventorySlots) {
			slot.xPos = -5000;
			slot.yPos = -5000;
		}
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		super.mouseDragged(slider);
		if (slider.getID() >= 10 && slider.getID() <= 12) {
			int percent = (int) (50.0f + slider.sliderValue * 100.0f);
			slider.setString(percent + "%");
			ModelPartConfig config = playerdata.getPartConfig(GuiCreationScale.selected);
			config.scale[slider.getID() - 10] = slider.sliderValue + 0.5f;
			updateTranslate();
		}
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.hasSelected()) {
			GuiCreationScale.selected = data.get(scroll.getSelect());
			initGui();
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) { }

	private void updateTranslate() {
		for (EnumParts part : EnumParts.values()) {
			ModelPartConfig config = playerdata.getPartConfig(part);
			if (config != null) {
				if (part == EnumParts.HEAD) { config.setTranslate(0.0f, playerdata.getBodyY(), 0.0f); }
				else if (part == EnumParts.ARM_LEFT) {
					ModelPartConfig body = playerdata.getPartConfig(EnumParts.BODY);
					float x = (1.0f - body.scale[0]) * 0.25f + (1.0f - config.scale[0]) * 0.075f;
					float y = playerdata.getBodyY() + (1.0f - config.scale[1]) * -0.1f;
					config.setTranslate(-x, y, 0.0f);
					if (!config.notShared) {
						ModelPartConfig arm = playerdata.getPartConfig(EnumParts.ARM_RIGHT);
						arm.copyValues(config);
					}
				} else if (part == EnumParts.ARM_RIGHT) {
					ModelPartConfig body = playerdata.getPartConfig(EnumParts.BODY);
					float x = (1.0f - body.scale[0]) * 0.25f + (1.0f - config.scale[0]) * 0.075f;
					float y = playerdata.getBodyY() + (1.0f - config.scale[1]) * -0.1f;
					config.setTranslate(x, y, 0.0f);
				} else if (part == EnumParts.LEG_LEFT) {
					config.setTranslate(config.scale[0] * 0.125f - 0.113f, playerdata.getLegsY(), 0.0f);
					if (!config.notShared) {
						ModelPartConfig leg = playerdata.getPartConfig(EnumParts.LEG_RIGHT);
						leg.copyValues(config);
					}
				} else if (part == EnumParts.LEG_RIGHT) { config.setTranslate((1.0f - config.scale[0]) * 0.125f, playerdata.getLegsY(), 0.0f); }
				else if (part == EnumParts.BODY) { config.setTranslate(0.0f, playerdata.getBodyY(), 0.0f); }
			}
		}
	}

}
