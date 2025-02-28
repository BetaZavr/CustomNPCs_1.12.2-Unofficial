package noppes.npcs.client.gui.script;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.constants.EnumScriptType;

import javax.annotation.Nonnull;

public class GuiScriptGlobal
extends GuiNPCInterface {

	private final String playerEventsList;
    private final String npcEventsList;
    private final String potionEventsList;
	private final ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/smallbg.png");

	public GuiScriptGlobal() {
		super();
		xSize = 176;
		ySize = 222;
		drawDefaultBackground = false;
		title = "";

		// Player
		StringBuilder sb = new StringBuilder();
		for (EnumScriptType est : EnumScriptType.getAllFunctions(0)) {
			if (sb.length() != 0) { sb.append(", "); }
			sb.append(est.function);
		}
		playerEventsList = sb.toString();
		// NPC
		StringBuilder sb1 = new StringBuilder();
		for (EnumScriptType est : EnumScriptType.getAllFunctions(1)) {
			if (sb1.length() != 0) { sb1.append(", "); }
			sb1.append(est.function);
		}
		npcEventsList = sb1.toString();
		// Potions
		StringBuilder sb2 = new StringBuilder();
		for (EnumScriptType est : EnumScriptType.getAllFunctions(5)) {
			if (sb2.length() != 0) { sb2.append(", "); }
			sb2.append(est.function);
		}
		potionEventsList = sb2.toString();
	}

	@Override
	protected void actionPerformed(@Nonnull GuiButton guibutton) {
		switch (guibutton.id) {
			case 1: {
				displayGuiScreen(new GuiScriptNPCs());
				break;
			}
			case 2: {
				displayGuiScreen(new GuiScriptForge());
				break;
			}
			case 3: {
				displayGuiScreen(new GuiScriptPotion());
				break;
			}
			case 4: {
				displayGuiScreen(new GuiScriptClient());
				break;
			}
			default: {
				displayGuiScreen(new GuiScriptPlayers());
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(resource);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void initGui() {
		super.initGui();
		for (int i = 0; i < 5; i++) {
			GuiNpcButton button = new GuiNpcButton(i, guiLeft + 38, guiTop + 20 + i * 30, 100, 20, "");
			switch (i) {
				case 1:
					button.setDisplayText("NPC");
					button.setHoverText("script.hover.npcs", npcEventsList);
					break;
				case 2:
					button.setDisplayText("Forge");
					button.setHoverText("script.hover.forge");
					break;
				case 3:
					button.setDisplayText("gui.help.potions");
					button.setHoverText("script.hover.potion", potionEventsList);
					break;
				case 4:
					button.setDisplayText("gui.client");
					button.setHoverText("script.hover.client");
					break;
				default:
					button.setDisplayText("playerdata.players");
					button.setHoverText("script.hover.players", playerEventsList);
					break;
			}
			addButton(button);
		}
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 || isInventoryKey(i)) { close(); }
		else { super.keyTyped(c, i); }
	}

}
