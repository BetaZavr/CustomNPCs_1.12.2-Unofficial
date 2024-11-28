package noppes.npcs.client.gui.global;

import java.util.Arrays;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.containers.ContainerNpcQuestReward;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNpcQuestReward extends GuiContainerNPCInterface implements ITextfieldListener {

	int maxXp = 99999, maxMoney = 99999999;
	private final Quest quest;
	private final ResourceLocation resource;

	public GuiNpcQuestReward(EntityNPCInterface npc, ContainerNpcQuestReward container) {
		super(npc, container);
		quest = NoppesUtilServer.getEditingQuest(player);
		resource = getResource("questreward.png");
		closeOnEsc = true;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 5) {
			close();
		} else if (button.id == 0) {
			quest.setRewardType(button.getValue());
		}
	}

	@Override
	public void close() {
		NoppesUtil.openGUI(player, GuiNPCManageQuest.Instance);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(resource);
		int l = (width - xSize) / 2;
		int i2 = (height - ySize) / 2;
		drawTexturedModalRect(l, i2, 0, 0, xSize, ySize);
		super.drawGuiContainerBackgroundLayer(f, i, j);
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (subgui != null) {
			return;
		}
		if (!CustomNpcs.ShowDescriptions) {
			return;
		}
		if (getButton(0) != null && getButton(0).isMouseOver()) {
			setHoverText(new TextComponentTranslation("quest.hover.edit.reward.type").getFormattedText());
		} else if (getButton(1) != null && getButton(1).isMouseOver()) {
			setHoverText(new TextComponentTranslation("quest.hover.edit.reward.show").getFormattedText());
		} else if (getTextField(0) != null && getTextField(0).isMouseOver()) {
			setHoverText(new TextComponentTranslation("quest.hover.edit.reward.xp", "" + maxXp).getFormattedText());
		} else if (getTextField(1) != null && getTextField(1).isMouseOver()) {
			setHoverText(new TextComponentTranslation("quest.hover.edit.reward.money", "" + maxMoney).getFormattedText());
		}
		if (hoverText != null) {
			drawHoveringText(Arrays.asList(hoverText), mouseX, mouseY, fontRenderer);
			hoverText = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = guiLeft + 4;
		int y = guiTop + 14;

		addLabel(new GuiNpcLabel(0, "quest.reward.get.item", x + 1, y - 10));
		addButton(new GuiNpcButton(0, x + 34, y, 62, 16, new String[] { "drop.type.all", "drop.type.one", "drop.type.random" }, quest.rewardType.ordinal()));
		addButton(new GuiNpcButton(5, x + xSize - 20, y - 10, 12, 12, "X"));

		addLabel(new GuiNpcLabel(1, "quest.exp", x + 1, (y += 19) + 3));
		addTextField(new GuiNpcTextField(0, this, fontRenderer, x + 35, y, 60, 14, quest.rewardExp + ""));
		getTextField(0).setNumbersOnly();
		getTextField(0).setMinMaxDefault(0, maxXp, quest.rewardExp);

		addLabel(new GuiNpcLabel(2, "gui.money", x + 1, (y += 18) + 3));
		addTextField(new GuiNpcTextField(1, this, fontRenderer, x + 35, y, 60, 14, quest.rewardMoney + ""));
		getTextField(1).setNumbersOnly();
		getTextField(1).setMinMaxDefault(0, maxMoney, quest.rewardMoney);

		addButton(new GuiNpcCheckBox(1, x, y + 16, 97, 12, "gui.enabled", "gui.disabled", quest.showRewardText));
	}

	@Override
	public void save() {
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getId() == 0) {
			quest.rewardExp = textfield.getInteger();
		} else if (textfield.getId() == 1) {
			quest.rewardMoney = textfield.getInteger();
		}
	}

}
