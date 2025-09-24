package noppes.npcs.client.gui.util;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.mainmenu.GuiNPCGlobalMainMenu;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.input.Keyboard;

public abstract class GuiNPCInterface2
extends GuiNPCInterface {

	private GuiNpcMenu menu;
	protected EnumGuiType parentGui = EnumGuiType.MainMenuDisplay;

	public GuiNPCInterface2(EntityNPCInterface npc) { this(npc, -1); }

	public GuiNPCInterface2(EntityNPCInterface npc, int activeMenu) {
		super(npc);
		drawDefaultBackground = true;
		background = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menubg.png");
		closeOnEsc = true;
		xSize = 420;
		ySize = 200;

		if (npc != null) { menu = new GuiNpcMenu(this, activeMenu, npc); }
	}

	@Override
	public void drawDefaultBackground() {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(background);
		if (menu == null && this instanceof GuiNPCGlobalMainMenu) {
			drawTexturedModalRect(guiLeft + 70, guiTop, 0, 0, 142, 220);
			drawTexturedModalRect(guiLeft + xSize - 208, guiTop, 113, 0, 143, 220);
		}
		else {
			drawTexturedModalRect(guiLeft, guiTop, 0, 0, 200, 220);
			drawTexturedModalRect(guiLeft + xSize - 230, guiTop, 26, 0, 230, 220);
		}
	}

	@Override
	protected void postDrawScreen(int mouseXIn, int mouseYIn, float partialTicks) {
		int x = mouseX;
		int y = mouseY;
		if (hasSubGui()) { y = x = 0; }
		if (menu != null) { menu.drawElements(this, x, y, partialTicks); }
	}

	@Override
	public void initGui() {
		super.initGui();
		if (!hasSubGui() && menu != null) { menu.initGui(guiLeft, guiTop, xSize); }
	}

	@Override
	public boolean mouseCnpcsPressed(int mouseX, int mouseY, int mouseButton) {
		if (!hasSubGui() && menu != null && menu.mouseCnpcsPressed(mouseX, mouseY, mouseButton)) { return true; }
		return super.mouseCnpcsPressed(mouseX, mouseY, mouseButton);
	}

	// New from Unofficial (BetaZavr)
	@Override
	public boolean keyCnpcsPressed(char typedChar, int keyCode) {
		boolean hasSubGui = hasSubGui();
		boolean bo = super.keyCnpcsPressed(typedChar, keyCode);
		if (!hasSubGui &&
				closeOnEsc &&
				keyCode == Keyboard.KEY_ESCAPE &&
				menu != null &&
				menu.activeMenu != 1 &&
				npc != null) {
			CustomNpcs.proxy.openGui(npc, parentGui);
			return true;
		}
		return bo;
	}

}
