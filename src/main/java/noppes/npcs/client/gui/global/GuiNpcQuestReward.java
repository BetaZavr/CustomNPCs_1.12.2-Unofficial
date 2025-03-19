package noppes.npcs.client.gui.global;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.containers.ContainerNpcQuestReward;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNpcQuestReward
extends GuiContainerNPCInterface
implements ITextfieldListener {

	private final Quest quest;
	private final ResourceLocation resource;

	public GuiNpcQuestReward(EntityNPCInterface npc, ContainerNpcQuestReward container) {
		super(npc, container);
		quest = NoppesUtilServer.getEditingQuest(player);
		resource = getResource("questreward.png");
		closeOnEsc = true;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (button.getID() == 5) {
			close();
		}
		else if (button.getID() == 0) {
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
	public void initGui() {
		super.initGui();
		int x = guiLeft + 4;
		int y = guiTop + 14;
		// type
		addLabel(new GuiNpcLabel(0, "quest.reward.get.item", x + 1, y - 10));
		GuiNpcButton button = new GuiNpcButton(0, x + 34, y, 62, 16, new String[] { "drop.type.all", "drop.type.one", "drop.type.random" }, quest.rewardType.ordinal());
		button.setHoverText("quest.hover.edit.reward.type");
		addButton(button);
		addButton(new GuiNpcButton(5, x + xSize - 20, y - 10, 12, 12, "X"));
		// xp
		addLabel(new GuiNpcLabel(1, "quest.exp", x + 1, (y += 19) + 3));
		GuiNpcTextField textField = new GuiNpcTextField(0, this, fontRenderer, x + 35, y, 60, 14, quest.rewardExp + "");
		textField.setMinMaxDefault(0, 99999, quest.rewardExp);
		textField.setHoverText("quest.hover.edit.reward.xp", "" + textField.max);
		addTextField(textField);
		//
		addLabel(new GuiNpcLabel(2, "gui.money", x + 1, (y += 18) + 3));
		textField = new GuiNpcTextField(1, this, fontRenderer, x + 35, y, 60, 14, quest.rewardMoney + "");
		textField.setMinMaxDefault(0, 99999999, quest.rewardMoney);
		textField.setHoverText("quest.hover.edit.reward.money", "" + textField.max);
		addTextField(textField);
		// show reward text
		button = new GuiNpcCheckBox(1, x, y + 16, 97, 12, "gui.enabled", "gui.disabled", quest.showRewardText);
		button.setHoverText("quest.hover.edit.reward.show");
		addButton(button);
	}

    @Override
	public void unFocused(IGuiNpcTextField textfield) {
		if (textfield.getID() == 0) {
			quest.rewardExp = textfield.getInteger();
		}
		else if (textfield.getID() == 1) {
			quest.rewardMoney = textfield.getInteger();
		}
	}

}
