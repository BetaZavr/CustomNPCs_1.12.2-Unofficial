package noppes.npcs.client.gui.player;

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

public class GuiQuestCompletion
extends GuiNPCInterface
implements ITopButtonListener {

	private static ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");
	
	private IQuest quest;
	private ResourceLocation resource;
	TextBlockClient textBlockClient;
	int maxLine, currentPage, hover;

	public GuiQuestCompletion(IQuest quest) {
		this.resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/smallbg.png");
		this.xSize = 176;
		this.ySize = 222;
		this.quest = quest;
		this.drawDefaultBackground = false;
		this.title = "";
		this.currentPage = 0;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 0) { this.close(); }
	}

	private void drawQuestText() {
		for (int i = currentPage * maxLine, j = 0; j < maxLine && i < textBlockClient.lines.size(); ++i, ++j) {
			String text = textBlockClient.lines.get(i).getFormattedText();
			this.fontRenderer.drawString(text, this.guiLeft + 4, this.guiTop + 16 + j * this.fontRenderer.FONT_HEIGHT, CustomNpcResourceListener.DefaultTextColor);
		}
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		this.drawDefaultBackground();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(this.resource);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
		this.drawHorizontalLine(this.guiLeft + 4, this.guiLeft + 170, this.guiTop + 13, -16777216 + CustomNpcResourceListener.DefaultTextColor);
		this.drawQuestText();
		hover = -1;
		if (textBlockClient.lines.size() * this.fontRenderer.FONT_HEIGHT > maxLine) {
			String page = "" + (currentPage + 1) + "/" + ((int) Math.ceil((double) textBlockClient.lines.size() / (double) maxLine));
			this.fontRenderer.drawString(page, this.guiLeft + 150 - this.fontRenderer.getStringWidth(page), this.guiTop + this.ySize - 20, CustomNpcResourceListener.DefaultTextColor);
			if (currentPage > 0) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + 6, this.guiTop + this.ySize - 20, 0.0f);
				if (isMouseHover(mouseX, mouseY, this.guiLeft + 6, this.guiTop + this.ySize - 20, 18, 10)) { hover = 0; }
				this.mc.renderEngine.bindTexture(GuiQuestCompletion.bookGuiTextures);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				this.drawTexturedModalRect(0, 0, hover == 0 ? 26 : 3, 207, 18, 10);
				GlStateManager.popMatrix();
			}
			if ((currentPage + 1) * maxLine < textBlockClient.lines.size()) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(this.guiLeft + this.xSize - 24, this.guiTop + this.ySize - 20, 0.0f);
				if (isMouseHover(mouseX, mouseY, this.guiLeft + this.xSize - 24, this.guiTop + this.ySize - 20, 18, 10)) { hover = 1; }
				this.mc.renderEngine.bindTexture(GuiQuestCompletion.bookGuiTextures);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				this.drawTexturedModalRect(0, 0, hover == 1 ? 26 : 3, 194, 18, 10);
				GlStateManager.popMatrix();
			}
		}
		super.drawScreen(i, j, f);
	}

	@Override
	public void initGui() {
		super.initGui();
		String questTitle = new TextComponentTranslation("questlog.completed").getFormattedText() + new TextComponentTranslation(this.quest.getName()).getFormattedText();
		int left = (this.xSize - this.fontRenderer.getStringWidth(questTitle)) / 2;
		this.addLabel(new GuiNpcLabel(0, questTitle, this.guiLeft + left, this.guiTop + 4));
		textBlockClient = new TextBlockClient(this.quest.getCompleteText(), 170, true, this.npc, new Object[] { this.player });
		maxLine = 180 / this.fontRenderer.FONT_HEIGHT;
		GuiNpcButton button;
		if (textBlockClient.lines.size() > maxLine) { button = new GuiNpcButton(0, this.guiLeft + 28, this.guiTop + this.ySize - 24, 80, 20, new TextComponentTranslation("quest.complete").getFormattedText()); }
		else { button = new GuiNpcButton(0, this.guiLeft + 48, this.guiTop + this.ySize - 24, 80, 20, new TextComponentTranslation("quest.complete").getFormattedText()); }
		this.addButton(button);
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 || this.isInventoryKey(i)) {
			this.close();
		}
	}

	@Override
	public void save() {
		if (((Quest) this.quest).rewardType == EnumRewardType.ONE_SELECT) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.QuestChooseReward, this.quest.getId());
		} else {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.QuestCompletion, this.quest.getId());
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseBottom) {
		if (hover != -1) {
			if (hover == 1) { currentPage++; }
			else { currentPage--; }
			return;
		}
		super.mouseClicked(mouseX, mouseY, mouseBottom);
	}

}
