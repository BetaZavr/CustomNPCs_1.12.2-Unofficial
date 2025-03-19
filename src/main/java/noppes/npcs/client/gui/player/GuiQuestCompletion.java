package noppes.npcs.client.gui.player;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.TextBlockClient;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.constants.EnumRewardType;
import noppes.npcs.controllers.data.Quest;

public class GuiQuestCompletion
extends GuiNPCInterface
implements ITopButtonListener {

	private static final ResourceLocation bookGuiTextures = new ResourceLocation("textures/gui/book.png");
	
	private final IQuest quest;
	private final ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/smallbg.png");
	int maxLine;
	int currentPage = 0;
	int hover;
	TextBlockClient textBlockClient;

	public GuiQuestCompletion(IQuest iQuest) {
		xSize = 176;
		ySize = 222;
		quest = iQuest;
		drawDefaultBackground = false;
		title = "";
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (button.getID() == 0) { close(); }
	}

	private void drawQuestText() {
		for (int i = currentPage * maxLine, j = 0; j < maxLine && i < textBlockClient.lines.size(); ++i, ++j) {
			String text = textBlockClient.lines.get(i).getFormattedText();
			fontRenderer.drawString(text, guiLeft + 4, guiTop + 16 + j * fontRenderer.FONT_HEIGHT, CustomNpcResourceListener.DefaultTextColor);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(resource);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		drawHorizontalLine(guiLeft + 4, guiLeft + 170, guiTop + 13, 0xFF000000 + CustomNpcResourceListener.DefaultTextColor);
		drawQuestText();
		hover = -1;
		if (textBlockClient.lines.size() * fontRenderer.FONT_HEIGHT > maxLine) {
			String page = (currentPage + 1) + "/" + ((int) Math.ceil((double) textBlockClient.lines.size() / (double) maxLine));
			fontRenderer.drawString(page, guiLeft + 150 - fontRenderer.getStringWidth(page), guiTop + ySize - 20, CustomNpcResourceListener.DefaultTextColor);
			if (currentPage > 0) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(guiLeft + 6, guiTop + ySize - 20, 0.0f);
				if (isMouseHover(mouseX, mouseY, guiLeft + 6, guiTop + ySize - 20, 18, 10)) { hover = 0; }
				mc.getTextureManager().bindTexture(GuiQuestCompletion.bookGuiTextures);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				drawTexturedModalRect(0, 0, hover == 0 ? 26 : 3, 207, 18, 10);
				GlStateManager.popMatrix();
			}
			if ((currentPage + 1) * maxLine < textBlockClient.lines.size()) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(guiLeft + xSize - 24, guiTop + ySize - 20, 0.0f);
				if (isMouseHover(mouseX, mouseY, guiLeft + xSize - 24, guiTop + ySize - 20, 18, 10)) { hover = 1; }
				mc.getTextureManager().bindTexture(GuiQuestCompletion.bookGuiTextures);
				GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
				drawTexturedModalRect(0, 0, hover == 1 ? 26 : 3, 194, 18, 10);
				GlStateManager.popMatrix();
			}
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void initGui() {
		super.initGui();
		String questTitle = new TextComponentTranslation("questlog.completed").getFormattedText() + new TextComponentTranslation(quest.getName()).getFormattedText();
		int left = (xSize - fontRenderer.getStringWidth(questTitle)) / 2;
		addLabel(new GuiNpcLabel(0, questTitle, guiLeft + left, guiTop + 4));
		textBlockClient = new TextBlockClient(quest.getCompleteText(), 170, true, npc, player);
		maxLine = 180 / fontRenderer.FONT_HEIGHT;
		GuiNpcButton button;
		if (textBlockClient.lines.size() > maxLine) { button = new GuiNpcButton(0, guiLeft + 28, guiTop + ySize - 24, 80, 20, new TextComponentTranslation("quest.complete").getFormattedText()); }
		else { button = new GuiNpcButton(0, guiLeft + 48, guiTop + ySize - 24, 80, 20, new TextComponentTranslation("quest.complete").getFormattedText()); }
		addButton(button);
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 || isInventoryKey(i)) { close(); }
	}

	@Override
	public void save() {
		if (((Quest) quest).rewardType == EnumRewardType.ONE_SELECT) { NoppesUtilPlayer.sendData(EnumPlayerPacket.QuestChooseReward, quest.getId()); }
		else { NoppesUtilPlayer.sendData(EnumPlayerPacket.QuestCompletion, quest.getId()); }
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
