package noppes.npcs.client.gui.script;

import java.util.List;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.constants.EnumScriptType;

public class GuiScriptGlobal extends GuiNPCInterface {

	private String playerEventsList, npcEventsList, potionEventsList;
	private ResourceLocation resource;

	public GuiScriptGlobal() {
		this.resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/smallbg.png");
		this.xSize = 176;
		this.ySize = 222;
		this.drawDefaultBackground = false;
		this.title = "";
		// Player
		List<EnumScriptType> functions = EnumScriptType.getAllFunctions(0);
		this.playerEventsList = "";
		for (EnumScriptType est : functions) {
			if (!this.playerEventsList.isEmpty()) { this.playerEventsList += ", "; }
			this.playerEventsList += est.function;
		}
		// NPC
		functions = EnumScriptType.getAllFunctions(1);
		this.npcEventsList = "";
		for (EnumScriptType est : functions) {
			if (!this.npcEventsList.isEmpty()) { this.npcEventsList += ", "; }
			this.npcEventsList += est.function;
		}
		// Potions
		functions = EnumScriptType.getAllFunctions(5);
		this.potionEventsList = "";
		for (EnumScriptType est : functions) {
			if (!this.potionEventsList.isEmpty()) { this.potionEventsList += ", "; }
			this.potionEventsList += est.function;
		}
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		switch (guibutton.id) {
			case 1: {
				this.displayGuiScreen(new GuiScriptNPCs());
				break;
			}
			case 2: {
				this.displayGuiScreen(new GuiScriptForge());
				break;
			}
			case 3: {
				this.displayGuiScreen(new GuiScriptPotion());
				break;
			}
			case 4: {
				this.displayGuiScreen(new GuiScriptClient());
				break;
			}
			default: {
				this.displayGuiScreen(new GuiScriptPlayers());
			}
		}
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		this.drawDefaultBackground();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(this.resource);
		this.drawTexturedModalRect(this.guiLeft, this.guiTop, 0, 0, this.xSize, this.ySize);
		super.drawScreen(i, j, f);
		if (this.subgui != null || !CustomNpcs.ShowDescriptions) { return; }
		if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("script.hover.players", new Object[] { this.playerEventsList }).getFormattedText());
		} else if (this.getButton(1) != null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("script.hover.npcs", new Object[] { this.npcEventsList }).getFormattedText());
		} else if (this.getButton(2) != null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("script.hover.forge").getFormattedText());
		} else if (this.getButton(3) != null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("script.hover.potion", new Object[] { this.potionEventsList }).getFormattedText());
		} else if (this.getButton(4) != null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("script.hover.client").getFormattedText());
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		for (int i = 0; i < 5; i++) {
			String name;
			switch (i) {
				case 1:
					name = "NPC";
					break;
				case 2:
					name = "Forge";
					break;
				case 3:
					name = "gui.help.potions";
					break;
				case 4:
					name = "gui.client";
					break;
				default:
					name = "playerdata.players";
					break;
			}
			this.addButton(new GuiNpcButton(i, this.guiLeft + 38, this.guiTop + 20 + i * 30, 100, 20, name));
		}
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 || this.isInventoryKey(i)) {
			this.close();
		} else {
			super.keyTyped(c, i);
		}
	}

	@Override
	public void save() { }
	
}
