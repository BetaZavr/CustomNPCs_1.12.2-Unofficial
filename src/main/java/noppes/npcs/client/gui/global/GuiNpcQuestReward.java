package noppes.npcs.client.gui.global;

import net.minecraft.client.gui.GuiButton;
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
	int maxXp = 99999;
	private Quest quest;
	private ResourceLocation resource;

	public GuiNpcQuestReward(EntityNPCInterface npc, ContainerNpcQuestReward container) {
		super(npc, container);
		this.quest = NoppesUtilServer.getEditingQuest(this.player);
		this.resource = this.getResource("questreward.png");
	}

	public void actionPerformed(GuiButton guibutton) {
		int id = guibutton.id;
		if (id == 5) {
			NoppesUtil.openGUI((EntityPlayer) this.player, GuiNPCManageQuest.Instance);
		}
		if (id == 0) {
			this.quest.setRewardType(((GuiNpcButton) guibutton).getValue());
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

	// New
	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (this.subgui != null) {
			return;
		}
		if (!CustomNpcs.showDescriptions) { return; }
		if (isMouseHover(i, j, this.guiLeft + 4, this.guiTop + 14, 60, 20)) {
			this.setHoverText(new TextComponentTranslation("quest.hover.edit.reward.type").getFormattedText());
		} else if (isMouseHover(i, j, this.guiLeft + 4, this.guiTop + 55, 60, 20)) {
			this.setHoverText(
					new TextComponentTranslation("quest.hover.edit.reward.xp", "" + this.maxXp).getFormattedText());
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addLabel(new GuiNpcLabel(0, "quest.reward.get.item", this.guiLeft + 4, this.guiTop + 4));
		this.addButton(new GuiNpcButton(0, this.guiLeft + 4, this.guiTop + 14, 60, 20,
				new String[] { "drop.type.all", "drop.type.one", "drop.type.random" },
				this.quest.rewardType.ordinal()));
		this.addButton(new GuiNpcButton(5, this.guiLeft, this.guiTop + this.ySize, 98, 20, "gui.back"));
		this.addLabel(new GuiNpcLabel(1, "quest.exp", this.guiLeft + 4, this.guiTop + 45));
		this.addTextField(new GuiNpcTextField(0, (GuiScreen) this, this.fontRenderer, this.guiLeft + 4,
				this.guiTop + 55, 60, 20, this.quest.rewardExp + ""));
		this.getTextField(0).numbersOnly = true;
		this.getTextField(0).setMinMaxDefault(0, this.maxXp, 0); // Change
	}

	@Override
	public void save() {
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		this.quest.rewardExp = textfield.getInteger();
	}
}
