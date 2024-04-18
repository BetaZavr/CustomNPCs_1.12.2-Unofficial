package noppes.npcs.client.gui.global;

import java.util.Arrays;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.containers.ContainerNpcQuestReward;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNpcQuestReward
extends GuiContainerNPCInterface
implements ITextfieldListener {
	
	// new
	int maxXp = 99999, maxMoney = 99999999;
	private Quest quest;
	private ResourceLocation resource;

	public GuiNpcQuestReward(EntityNPCInterface npc, ContainerNpcQuestReward container) {
		super(npc, container);
		this.quest = NoppesUtilServer.getEditingQuest(this.player);
		this.resource = this.getResource("questreward.png");
		this.closeOnEsc = true;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 5) {
			this.close();
		}
		else if (button.id == 0) {
			this.quest.setRewardType(button.getValue());
		}
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float f, int i, int j) {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(this.resource);
		int l = (this.width - this.xSize) / 2;
		int i2 = (this.height - this.ySize) / 2;
		this.drawTexturedModalRect(l, i2, 0, 0, this.xSize, this.ySize);
		super.drawGuiContainerBackgroundLayer(f, i, j);
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = this.guiLeft + 4;
		int y = this.guiTop + 14;
		this.addLabel(new GuiNpcLabel(0, "quest.reward.get.item", x + 1, y - 10));
		this.addButton(new GuiNpcButton(0, x + 34, y, 62, 20, new String[] { "drop.type.all", "drop.type.one", "drop.type.random" }, this.quest.rewardType.ordinal()));
		this.addButton(new GuiNpcButton(5, x + this.xSize - 20, y - 10, 12, 12, "X"));
		
		this.addLabel(new GuiNpcLabel(1, "quest.exp", x + 1, (y += 22) + 5));
		this.addTextField(new GuiNpcTextField(0, (GuiScreen) this, this.fontRenderer, x + 35, y, 60, 18, this.quest.rewardExp + ""));
		this.getTextField(0).setNumbersOnly();
		this.getTextField(0).setMinMaxDefault(0, this.maxXp, this.quest.rewardExp);

		this.addLabel(new GuiNpcLabel(2, "gui.money", x + 1, (y += 21) + 5));
		this.addTextField(new GuiNpcTextField(1, (GuiScreen) this, this.fontRenderer, x + 35, y, 60, 18, this.quest.rewardMoney + ""));
		this.getTextField(1).setNumbersOnly();
		this.getTextField(1).setMinMaxDefault(0, this.maxMoney, this.quest.rewardMoney);
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (this.subgui != null) { return; }
		if (!CustomNpcs.ShowDescriptions) { return; }
		if (this.getButton(0)!=null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.reward.type").getFormattedText());
		} else if (this.getTextField(0)!=null && this.getTextField(0).isMouseOver()) {
			this.setHoverText( new TextComponentTranslation("quest.hover.edit.reward.xp", "" + this.maxXp).getFormattedText());
		} else if (this.getTextField(1)!=null && this.getTextField(1).isMouseOver()) {
			this.setHoverText( new TextComponentTranslation("quest.hover.edit.reward.money", "" + this.maxMoney).getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}
	
	@Override
	public void close() {
		NoppesUtil.openGUI((EntityPlayer) this.player, GuiNPCManageQuest.Instance);
	}
	
	@Override
	public void save() { }

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		if (textfield.getId() == 0) { this.quest.rewardExp = textfield.getInteger(); }
		else if (textfield.getId() == 1) { this.quest.rewardMoney = textfield.getInteger(); }
	}
	
}
