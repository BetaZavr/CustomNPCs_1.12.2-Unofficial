package noppes.npcs.client.gui.util;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Container;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.entity.EntityNPCInterface;
import org.lwjgl.input.Keyboard;

public abstract class GuiContainerNPCInterface2
extends GuiContainerNPCInterface {

	protected ResourceLocation defaultBackground = new ResourceLocation(CustomNpcs.MODID, "textures/gui/menubg.png");
	protected EnumGuiType parentGui = EnumGuiType.MainMenuDisplay;
	private GuiNpcMenu menu;
	public int menuYOffset;

	public GuiContainerNPCInterface2(EntityNPCInterface npc, Container cont) {
		this(npc, cont, -1);
	}

	public GuiContainerNPCInterface2(EntityNPCInterface npc, Container cont, int activeMenu) {
		super(npc, cont);
		closeOnEsc = true;
		title = "";
		xSize = 420;

		menuYOffset = 0;
		if (npc != null) { menu = new GuiNpcMenu(this, activeMenu, npc); }
	}

	@Override
	public void drawDefaultBackground() {
		super.drawDefaultBackground();
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(background);
		drawTexturedModalRect(guiLeft, guiTop, 0, 0, 256, 256);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		mc.getTextureManager().bindTexture(defaultBackground);
		drawTexturedModalRect(guiLeft + xSize - 200, guiTop, 56, 0, 200, 220);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
	}

	@Override
	protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
		super.drawGuiContainerBackgroundLayer(partialTicks, mouseX, mouseY);
		if (menu != null) { menu.drawElements(this, mouseX, mouseY, partialTicks); }
	}

    @Override
	public void initGui() {
		super.initGui();
		if (menu != null) { menu.initGui(guiLeft, guiTop + menuYOffset, xSize); }
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
