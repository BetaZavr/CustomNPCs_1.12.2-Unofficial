package noppes.npcs.client.gui.script;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;

public class GuiScriptGlobal
extends GuiNPCInterface {
	
	protected List<String> baseFuncNames = new ArrayList<String>();
	protected List<String> baseFuncBlocks = new ArrayList<String>();
	protected List<String> baseFuncPotions = new ArrayList<String>();
	private String playerEventsList, blockEventsList, potionEventsList;
	private ResourceLocation resource;

	public GuiScriptGlobal() {
		this.resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/smallbg.png");
		this.xSize = 176;
		this.ySize = 222;
		this.drawDefaultBackground = false;
		this.title = "";
		// New
		this.baseFuncNames.add("init");
		this.baseFuncNames.add("login");
		this.baseFuncNames.add("logout");
		this.baseFuncNames.add("attack");
		this.baseFuncNames.add("interact");
		this.baseFuncNames.add("customChestClosed");
		this.baseFuncNames.add("customChestClicked");
		this.baseFuncNames.add("containerOpen");
		this.baseFuncNames.add("containerClosed");
		this.baseFuncNames.add("broken");
		this.baseFuncNames.add("keyPressed");
		this.baseFuncNames.add("damagedEntity");
		this.baseFuncNames.add("damaged");
		this.baseFuncNames.add("died");
		this.baseFuncNames.add("pickUp");
		this.baseFuncNames.add("tick");
		this.baseFuncNames.add("kill");
		this.baseFuncNames.add("timer");
		this.baseFuncNames.add("toss");
		this.baseFuncNames.add("questStart");
		this.baseFuncNames.add("questCompleted");
		this.baseFuncNames.add("questTurnIn");
		this.baseFuncNames.add("chat");
		this.baseFuncNames.add("levelUp");
		this.baseFuncNames.add("factionUpdate");
		this.baseFuncNames.add("questCanceled");
		this.baseFuncNames.add("itemFished");
		this.baseFuncNames.add("itemCrafted");
		Collections.sort(this.baseFuncNames);
		this.playerEventsList = "";
		for (String name : this.baseFuncNames) {
			if (this.playerEventsList.length() > 0) { this.playerEventsList += ", "; }
			this.playerEventsList += name;
		}
		this.baseFuncPotions.add("isReady");
		this.baseFuncPotions.add("performEffect");
		this.baseFuncPotions.add("affectEntity");
		this.baseFuncPotions.add("endEffect");
		Collections.sort(this.baseFuncPotions);
		this.potionEventsList = "";
		for (String name : this.baseFuncPotions) {
			if (this.potionEventsList.length() > 0) { this.potionEventsList += ", "; }
			this.potionEventsList += name;
		}

		this.baseFuncBlocks.add("interact");
		this.baseFuncBlocks.add("redstone");
		this.baseFuncBlocks.add("broken");
		this.baseFuncBlocks.add("exploded");
		this.baseFuncBlocks.add("rainFilled");
		this.baseFuncBlocks.add("neighborChanged");
		this.baseFuncBlocks.add("init");
		this.baseFuncBlocks.add("tick");
		this.baseFuncBlocks.add("clicked");
		this.baseFuncBlocks.add("harvested");
		this.baseFuncBlocks.add("collide");
		this.baseFuncBlocks.add("timer");
		Collections.sort(this.baseFuncBlocks);
		this.blockEventsList = "";
		for (String name : this.baseFuncBlocks) {
			if (this.blockEventsList.length() > 0) { this.blockEventsList += ", "; }
			this.blockEventsList += name;
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		switch(guibutton.id) {
			case 1: { this.displayGuiScreen(new GuiScriptForge()); break; }
			case 2: { this.displayGuiScreen(new GuiScriptPotion()); break; }
			case 3: { this.displayGuiScreen(new GuiScriptClient()); break; }
			default: { this.displayGuiScreen(new GuiScriptPlayers()); }
		}
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		this.drawDefaultBackground();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(this.resource);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
		super.drawScreen(i, j, f);
		if (this.subgui != null || !CustomNpcs.showDescriptions) { return; }
		if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("script.hover.players", new Object[] { this.playerEventsList }) .getFormattedText());
		} else if (this.getButton(1) != null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("script.hover.forge").getFormattedText());
		} else if (this.getButton(2) != null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("script.hover.potion", new Object[] { this.potionEventsList }).getFormattedText());
		} else if (this.getButton(3) != null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("script.hover.client").getFormattedText());
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		for (int i=0; i<4; i++) {
			String name;
			switch(i) {
				case 1: name = "Forge"; break;
				case 2: name = "gui.help.potions"; break;
				case 3: name = "gui.client"; break;
				default: name = "playerdata.players"; break;
			}
			this.addButton(new GuiNpcButton(i, this.guiLeft + 38, this.guiTop + 20 + i * 30, 100, 20, name));
		}
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 || this.isInventoryKey(i)) {
			this.close();
		}
		else { super.keyTyped(c, i); }
	}

	@Override
	public void save() {
	}
}
