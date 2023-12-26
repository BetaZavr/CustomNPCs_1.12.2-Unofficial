package noppes.npcs.client.gui.player;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.TextBlockClient;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.ITopButtonListener;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.constants.EnumRewardType;
import noppes.npcs.controllers.data.Quest;

public class GuiQuestCompletion extends GuiNPCInterface implements ITopButtonListener {
	private IQuest quest;
	private ResourceLocation resource;

	public GuiQuestCompletion(IQuest quest) {
		this.resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/smallbg.png");
		this.xSize = 176;
		this.ySize = 222;
		this.quest = quest;
		this.drawDefaultBackground = false;
		this.title = "";
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id == 0) {
			this.close();
		}
	}

	private void drawQuestText() {
		TextBlockClient block = new TextBlockClient(this.quest.getCompleteText(), 172, true, this.npc, new Object[] { this.player });
		for (int i = 0; i < block.lines.size(); ++i) {
			String text = block.lines.get(i).getFormattedText();
			this.fontRenderer.drawString(text, this.guiLeft + 4, this.guiTop + 16 + i * this.fontRenderer.FONT_HEIGHT,
					CustomNpcResourceListener.DefaultTextColor);
		}
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		this.drawDefaultBackground();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(this.resource);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
		this.drawHorizontalLine(this.guiLeft + 4, this.guiLeft + 170, this.guiTop + 13,
				-16777216 + CustomNpcResourceListener.DefaultTextColor);
		this.drawQuestText();
		super.drawScreen(i, j, f);
	}

	@Override
	public void initGui() {
		super.initGui();
		String questTitle = new TextComponentTranslation("questlog.completed").getFormattedText() + new TextComponentTranslation(this.quest.getName()).getFormattedText();
		int left = (this.xSize - this.fontRenderer.getStringWidth(questTitle)) / 2;
		this.addLabel(new GuiNpcLabel(0, questTitle, this.guiLeft + left, this.guiTop + 4));
		this.addButton(new GuiNpcButton(0, this.guiLeft + 38, this.guiTop + this.ySize - 24, 100, 20, new TextComponentTranslation("quest.complete").getFormattedText()));
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 || this.isInventoryKey(i)) {
			this.close();
		}
	}

	@Override
	public void save() {
		if (((Quest) this.quest).rewardType==EnumRewardType.ONE_SELECT) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.QuestChooseReward, this.quest.getId());
		}
		else {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.QuestCompletion, this.quest.getId());
		}
	}
}
