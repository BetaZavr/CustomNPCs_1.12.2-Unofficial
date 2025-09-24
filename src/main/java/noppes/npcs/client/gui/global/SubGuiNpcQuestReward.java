package noppes.npcs.client.gui.global;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.containers.ContainerNpcQuestReward;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class SubGuiNpcQuestReward extends GuiContainerNPCInterface implements ITextfieldListener {

	protected final ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/questreward.png");
	protected final Quest quest;

	public SubGuiNpcQuestReward(EntityNPCInterface npc, ContainerNpcQuestReward container) {
		super(npc, container);
		closeOnEsc = true;

		quest = NoppesUtilServer.getEditingQuest(player);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: quest.setRewardType(button.getValue()); break;
			case 66: onClosed(); break;
		}
	}

	@Override
	public void onGuiClosed() {
		GuiNpcTextField.unfocus();
		player.closeScreen();
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
		addButton(new GuiNpcButton(0, x + 34, y, 62, 16, new String[] { "drop.type.all", "drop.type.one", "drop.type.random" }, quest.rewardType.ordinal())
				.setHoverText("quest.hover.edit.reward.type"));
		addButton(new GuiNpcButton(66, x + xSize - 20, y - 10, 12, 12, "X"));
		// xp
		int max = 99999;
		addLabel(new GuiNpcLabel(1, "quest.exp", x + 1, (y += 19) + 3));
		addTextField(new GuiNpcTextField(0, this, x + 35, y, 60, 14, quest.rewardExp + "")
				.setMinMaxDefault(0, max, quest.rewardExp)
				.setHoverText("quest.hover.edit.reward.xp", "" + max));
		// money
		max = 99999999;
		addLabel(new GuiNpcLabel(2, "gui.money", x + 1, (y += 18) + 3));
		addTextField(new GuiNpcTextField(1, this, x + 35, y, 60, 14, quest.rewardMoney + "")
				.setMinMaxDefault(0, 99999999, quest.rewardMoney)
				.setHoverText("quest.hover.edit.reward.money", "" + max));
		// show reward text
		addButton(new GuiNpcCheckBox(1, x, y + 16, 97, 12, "gui.enabled", "gui.disabled", quest.showRewardText)
				.setHoverText("quest.hover.edit.reward.show"));
	}

    @Override
	public void unFocused(GuiNpcTextField textfield) {
		switch (textfield.getID()) {
			case 0: quest.rewardExp = textfield.getInteger(); break;
			case 1: quest.rewardMoney = textfield.getInteger(); break;
		}
	}

}
