package noppes.npcs.client.gui.player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import micdoodle8.mods.galacticraft.api.client.tabs.InventoryTabQuests;
import micdoodle8.mods.galacticraft.api.client.tabs.TabRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.client.Client;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.TextBlockClient;
import noppes.npcs.client.gui.GuiCompassSetings;
import noppes.npcs.client.gui.util.GuiButtonNextPage;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiMenuSideButton;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ITopButtonListener;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.constants.EnumQuestCompletion;
import noppes.npcs.controllers.PlayerQuestController;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.NaturalOrderComparator;

public class GuiQuestLog extends
GuiNPCInterface
implements ITopButtonListener, ICustomScrollListener, GuiYesNoCallback, IGuiData {
	
	public HashMap<String, List<Quest>> activeQuests;
	private HashMap<String, Quest> categoryQuests;
	private int currentPage;
	public int maxLines = 10;
	private int maxPages;
	private Minecraft mc;
	private boolean noQuests;
	private EntityPlayer player;
	private ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/standardbg.png");
	private GuiCustomScroll scroll;
	public String selectedCategory;
	public Quest selectedQuest;
	private HashMap<Integer, GuiMenuSideButton> sideButtons;
	TextBlockClient textblock;
	int yoffset;

	public GuiQuestLog(EntityPlayer player) {
		this.activeQuests = new HashMap<String, List<Quest>>();
		this.categoryQuests = new HashMap<String, Quest>();
		this.selectedQuest = null;
		this.selectedCategory = "";
		this.sideButtons = new HashMap<Integer, GuiMenuSideButton>();
		this.noQuests = false;
		this.currentPage = 0;
		this.maxPages = 1;
		this.textblock = null;
		this.mc = Minecraft.getMinecraft();
		this.player = player;
		this.xSize = 280;
		this.ySize = 180;
		this.drawDefaultBackground = false;
		// New
		this.maxLines = 10;
	}

	@Override
	public void initGui() {
		super.initGui();
		this.yoffset = this.ySize + 2;
		for (Quest quest : PlayerQuestController.getActiveQuests(this.player)) {
			String category = quest.category.title;
			if (!this.activeQuests.containsKey(category)) {
				this.activeQuests.put(category, new ArrayList<Quest>());
			}
			this.activeQuests.get(category).add(quest);
		}
		this.sideButtons.clear();
		this.guiTop += 10;
		TabRegistry.updateTabValues(this.guiLeft, this.guiTop, InventoryTabQuests.class);
		TabRegistry.addTabsToList(this.buttonList);
		this.noQuests = false;
		if (this.activeQuests.isEmpty()) {
			this.noQuests = true;
			return;
		}
		// category Buttons
		List<String> categories = new ArrayList<String>();
		categories.addAll(this.activeQuests.keySet());
		Collections.sort(categories, new NaturalOrderComparator());
		int i = 0;
		for (String cat : categories) {
			if (this.selectedCategory.isEmpty()) {
				this.selectedCategory = cat;
			}
			this.sideButtons.put(i, new GuiMenuSideButton(i, this.guiLeft - 69, this.guiTop + 2 + i * 21, 70, 22, cat));
			++i;
		}
		this.sideButtons.get(categories.indexOf(this.selectedCategory)).active = true;
		// scroll all quests
		if (this.scroll == null) {
			this.scroll = new GuiCustomScroll(this, 0);
		}
		HashMap<String, Quest> categoryQuests = new HashMap<String, Quest>();
		for (Quest q : this.activeQuests.get(this.selectedCategory)) {
			categoryQuests.put(q.getTitle(), q);
		}
		this.categoryQuests = categoryQuests;
		this.scroll.setList(new ArrayList<String>(categoryQuests.keySet()));
		this.scroll.setSize(134, 141);
		this.scroll.guiLeft = this.guiLeft + 5;
		this.scroll.guiTop = this.guiTop + 15;
		this.addScroll(this.scroll);
		// New
		if (this.selectedQuest != null) {
			if (this.selectedQuest.completion == EnumQuestCompletion.Npc) {
				this.yoffset -= 11;
			}
			IQuestObjective[] allObj = this.selectedQuest.questInterface.getObjectives(this.player);
			this.yoffset -= 9 * allObj.length;
			this.yoffset -= 16;
		} else {
			this.scroll.selected = -1;
		}
		this.addButton(new GuiButtonNextPage(1, this.guiLeft + 286, this.guiTop + this.yoffset, true)); // Changed
		this.addButton(new GuiButtonNextPage(2, this.guiLeft + 144, this.guiTop + this.yoffset, false)); // Changed
		this.getButton(1).visible = (this.selectedQuest != null && this.currentPage < this.maxPages - 1);
		this.getButton(2).visible = (this.selectedQuest != null && this.currentPage > 0);
		// New
		this.addButton(new GuiNpcButton(30, this.guiLeft + 5, this.guiTop + 158, 73, 15, "quest.cancel", this.selectedQuest != null && this.selectedQuest.cancelable));
		this.addButton(new GuiNpcButton(31, this.guiLeft + 5, this.guiTop + 175, 90, 15, "quest.track", this.selectedQuest != null && this.selectedQuest.id!=ClientProxy.playerData.hud.questID));
		this.addButton(new GuiNpcButton(32, this.guiLeft + 79, this.guiTop + 158, 60, 15, "gui.settings"));
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		switch(guibutton.id) {
			case 1: {
				if (!(guibutton instanceof GuiButtonNextPage)) { return; }
				++this.currentPage;
				this.initGui();
				break;
			}
			case 2: {
				if (!(guibutton instanceof GuiButtonNextPage)) { return; }
				--this.currentPage;
				this.initGui();
				break;
			}
			case 30: {
				if (this.selectedQuest == null) { return; }
				GuiYesNo guiyesno = new GuiYesNo((GuiYesNoCallback) this, new TextComponentTranslation("drop.quest", new Object[] { new TextComponentTranslation(this.selectedQuest.getTitle()).getFormattedText() }).getFormattedText(), new TextComponentTranslation("quest.cancel.info", new Object[0]).getFormattedText(), 30);
				this.displayGuiScreen((GuiScreen) guiyesno);
				break;
			}
			case 31: {
				if (this.selectedQuest == null) { return; }
				ClientProxy.playerData.hud.questID = this.selectedQuest.id;
				((GuiNpcButton) guibutton).enabled = false;
				Client.sendDataDelayCheck(EnumPlayerPacket.TrackQuest, this, 0, ClientProxy.playerData.hud.questID);
				break;
			}
			case 32: {
				this.displayGuiScreen(new GuiCompassSetings(this));
				break;
			}
		}
	}

	// New
	@Override
	public void confirmClicked(boolean result, int id) {
		NoppesUtil.openGUI((EntityPlayer) this.player, this);
		if (!result) {
			return;
		}
		if (id == 30) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.QuestRemoveActive, this.selectedQuest.id);
		}
	}

	@Override
	public boolean doesGuiPauseGame() {
		return false;
	}

	private void drawProgress() {
		// New
		if (this.selectedQuest == null) {
			return;
		}
		// Finish NPC
		this.yoffset = this.ySize + 2; // end line
		if (this.selectedQuest.completion == EnumQuestCompletion.Npc) {
			this.drawHorizontalLine(this.guiLeft + 142, this.guiLeft + 312, this.guiTop+this.yoffset - 2, -16777216 + CustomNpcResourceListener.DefaultTextColor);
			String complete = this.selectedQuest.getNpcName();
			if (complete != null && !complete.isEmpty()) {
				this.mc.fontRenderer.drawString(new TextComponentTranslation("quest.completewith", new Object[] { complete }).getFormattedText(), this.guiLeft + 142, this.guiTop+this.yoffset, CustomNpcResourceListener.DefaultTextColor);
			}
			this.yoffset -= 11;
		}
		// Quest Objects
		IQuestObjective[] allObj = this.selectedQuest.questInterface.getObjectives(this.player);
		String hyphen = "-" + new String(Character.toChars(0x00A7)) + "r", color = "";
		String colorR = new String(Character.toChars(0x00A7)) + "c";
		String colorG = new String(Character.toChars(0x00A7)) + "c";
		int pos = 0;
		this.yoffset -= (allObj.length - 1) * 9;
		if (this.selectedQuest.step == 2) { // or
			int complite = -1;
			for (int i = 0; i < allObj.length; i++) {
				if (allObj[i].isCompleted()) {
					complite = i;
					break;
				}
			}
			for (int i = 0; i < allObj.length; i++) {
				hyphen = (complite != -1 ? complite == i ? colorG : "" : colorR) + (i + 1) + "(" + new TextComponentTranslation("quest.task.step.2").getFormattedText().toLowerCase() + ")-" + new String(Character.toChars(0x00A7)) + "r" + allObj[i].getText();
				// Progress
				this.mc.fontRenderer.drawString(hyphen, this.guiLeft + 142, this.guiTop+this.yoffset, CustomNpcResourceListener.DefaultTextColor);
				this.yoffset += 9;
			}
		} else {
			for (int i = 0; i < allObj.length; i++) {
				if (this.selectedQuest.step == 1 && i == pos) { // ones
					if (allObj[i].isCompleted()) {
						color = colorG;
						pos++;
					} else {
						color = colorR;
					}
				} else {
					color = "";
				}
				hyphen = color + (i + 1) + "-" + new String(Character.toChars(0x00A7)) + "r" + allObj[i].getText();
				// Progress
				this.mc.fontRenderer.drawString(hyphen, this.guiLeft + 142, this.guiTop+this.yoffset, CustomNpcResourceListener.DefaultTextColor);
				this.yoffset += 9;
			}
		}
		// Main Task
		yoffset -= (allObj.length + 1) * 9 - 7;
		this.drawHorizontalLine(this.guiLeft + 142, this.guiLeft + 312, this.guiTop+this.yoffset, -16777216 + CustomNpcResourceListener.DefaultTextColor);
		this.yoffset -= 9;
		this.mc.fontRenderer.drawString(new TextComponentTranslation("quest.objectives").getFormattedText() + ":", this.guiLeft + 142, this.guiTop+this.yoffset, CustomNpcResourceListener.DefaultTextColor);
		this.yoffset -= 12;
	}

	private void drawQuestText() {
		if (this.selectedQuest == null) {
			return;
		} // New
		if (this.textblock == null) {
			return;
		}
		// New
		int mF = this.fontRenderer.FONT_HEIGHT;
		this.maxLines = (int) Math.floor(((double) this.yoffset + 20.0d) / (double) mF) - 1;
		for (int i = 0; i < this.maxLines; ++i) {
			int index = i + this.currentPage * this.maxLines;
			if (index < this.textblock.lines.size()) {
				String text = this.textblock.lines.get(index).getFormattedText();
				this.fontRenderer.drawString(text, this.guiLeft + 142, this.guiTop + 20 + i * mF,
						CustomNpcResourceListener.DefaultTextColor);
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.scroll != null) {
			this.scroll.visible = !this.noQuests;
		}
		this.drawDefaultBackground();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(this.resource);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, 252, 195);
		this.drawTexturedModalRect(this.guiLeft + 252, this.guiTop, 188, 0, 67, 195);
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.noQuests) {
			this.mc.fontRenderer.drawString(new TextComponentTranslation("quest.noquests").getFormattedText(), this.guiLeft + 84, this.guiTop + 80, CustomNpcResourceListener.DefaultTextColor);
		} else {
			for (GuiMenuSideButton button : this.sideButtons.values().toArray(new GuiMenuSideButton[this.sideButtons.size()])) {
				button.drawButton(this.mc, mouseX, mouseY, partialTicks);
			}
			this.mc.fontRenderer.drawString(this.selectedCategory, this.guiLeft + 5, this.guiTop + 5, CustomNpcResourceListener.DefaultTextColor);
			if (this.selectedQuest == null) { return; }
			this.drawProgress();
			this.drawQuestText();
			// category Buttons
			GlStateManager.pushMatrix();
			GlStateManager.translate((this.guiLeft + 148), this.guiTop, 0.0f);
			GlStateManager.scale(1.25f, 1.25f, 1.25f);
			String title = new TextComponentTranslation("quest.name").getFormattedText() + ": "
					+ new TextComponentTranslation(this.selectedQuest.getName()).getFormattedText();
			this.fontRenderer.drawString(title, (130 - this.fontRenderer.getStringWidth(title)) / 2, 4,
					CustomNpcResourceListener.DefaultTextColor);
			GlStateManager.popMatrix();
			this.drawHorizontalLine(this.guiLeft + 142, this.guiLeft + 312, this.guiTop + 17, -16777216 + CustomNpcResourceListener.DefaultTextColor);
		}
		// hovers
		if (CustomNpcs.showDescriptions) {
			if (this.getButton(30)!=null && this.getButton(30).enabled && this.getButton(30).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("drop.quest", new Object[] { this.title }).getFormattedText());
			} else if (this.getButton(31)!=null && this.getButton(31).enabled && this.getButton(31).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("quest.hover.compass.track").getFormattedText());
			} else if (this.getButton(32)!=null && this.getButton(32).isMouseOver()) {
				this.setHoverText(new TextComponentTranslation("quest.hover.compass.settings").getFormattedText());
			}
		}
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 || i == this.mc.gameSettings.keyBindInventory.getKeyCode()) {
			this.mc.displayGuiScreen((GuiScreen) null);
			this.mc.setIngameFocus();
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseBottom) {
		super.mouseClicked(mouseX, mouseY, mouseBottom);
		if (mouseBottom != 0) {
			return;
		}
		if (this.scroll != null) {
			this.scroll.mouseClicked(mouseX, mouseY, mouseBottom);
		}
		for (GuiMenuSideButton button : new ArrayList<GuiMenuSideButton>(this.sideButtons.values())) {
			if (button.mousePressed(this.mc, mouseX, mouseY)) {
				this.sideButtonPressed(button);
			}
		}
	}

	@Override
	public void save() {
	}

	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
		if (!scroll.hasSelected()) {
			return;
		}
		this.selectedQuest = this.categoryQuests.get(scroll.getSelected());
		this.textblock = new TextBlockClient(this.selectedQuest.getLogText(), 172, true, new Object[] { this.player });
		if (this.textblock.lines.size() > 10) {
			this.maxPages = MathHelper.ceil(1.0f * this.textblock.lines.size() / 10.0f);
		}
		this.currentPage = 0;
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (this.selectedQuest != null
				&& PlayerQuestController.getRemoveActiveQuest(this.player, this.selectedQuest.id)) {
			this.scroll = null;
			this.activeQuests.clear();
			this.categoryQuests.clear();
			this.selectedQuest = null;
			this.selectedCategory = "";
			this.currentPage = 0;
			this.maxPages = 1;
			this.textblock = null;
			this.initGui();
		}
	}

	private void sideButtonPressed(GuiMenuSideButton button) {
		if (button.active) { return; }
		NoppesUtil.clickSound();
		this.selectedCategory = AdditionalMethods.deleteColor(button.displayString);
		this.selectedQuest = null;
		this.initGui();
	}

}
