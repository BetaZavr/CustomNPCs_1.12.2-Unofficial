package noppes.npcs.client.gui.util;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiYesNo;
import net.minecraft.client.gui.GuiYesNoCallback;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.animation.GuiNpcAnimation;
import noppes.npcs.client.gui.animation.GuiNpcEmotion;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNpcMenu implements GuiYesNoCallback {

	public int activeMenu;
	private final EntityNPCInterface npc;
	private final GuiScreen parent;
	private GuiMenuTopButton[] topButtons;

	public GuiNpcMenu(GuiScreen parent, int activeMenu, EntityNPCInterface npc) {
		this.topButtons = new GuiMenuTopButton[0];
		this.parent = parent;
		this.activeMenu = activeMenu;
		this.npc = npc;
	}

	public void confirmClicked(boolean flag, int id) {
		Minecraft mc = Minecraft.getMinecraft();
		if (flag) {
			Client.sendData(EnumPacketServer.Delete);
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
		} else {
			NoppesUtil.openGUI(mc.player, this.parent);
		}
	}

	public void drawElements(int mouseX, int mouseY, Minecraft mc, float partialTicks) {
		for (GuiMenuTopButton button : this.getTopButtons()) {
			button.drawButton(mc, mouseX, mouseY, partialTicks);
		}
	}

	public GuiMenuTopButton[] getTopButtons() {
		return topButtons;
	}

	public void initGui(int guiLeft, int guiTop, int width) {
		Keyboard.enableRepeatEvents(true);
		GuiMenuTopButton display = new GuiMenuTopButton(1, guiLeft + 4, guiTop - 17, "menu.display");
		GuiMenuTopButton stats = new GuiMenuTopButton(2, display.x + display.getWidth(), guiTop - 17, "menu.stats");
		GuiMenuTopButton ai = new GuiMenuTopButton(6, stats.x + stats.getWidth(), guiTop - 17, "menu.ai");
		GuiMenuTopButton inv = new GuiMenuTopButton(3, ai.x + ai.getWidth(), guiTop - 17, "menu.inventory");
		GuiMenuTopButton advanced = new GuiMenuTopButton(4, inv.x + inv.getWidth(), guiTop - 17, "menu.advanced");
		GuiMenuTopButton global = new GuiMenuTopButton(5, advanced.x + advanced.getWidth(), guiTop - 17, "menu.global");
		GuiMenuTopButton close = new GuiMenuTopButton(0, guiLeft + width - 22, guiTop - 17, "X");
		GuiMenuTopButton delete = new GuiMenuTopButton(66, guiLeft + width - 72, guiTop - 17, "selectWorld.deleteButton");
		delete.x = close.x - delete.getWidth();
		this.topButtons = new GuiMenuTopButton[] { display, stats, ai, inv, advanced, global, close, delete };
		for (GuiMenuTopButton button : this.getTopButtons()) {
			button.active = (button.id == this.activeMenu);
		}
	}

	public void mouseClicked(int i, int j, int k) {
		if (k == 0) {
			Minecraft mc = Minecraft.getMinecraft();
			for (GuiMenuTopButton button : this.getTopButtons()) {
				boolean bo = button.getVisible() && button.isMouseOver();
				if (button.mousePressed(mc, i, j) || (bo && button.id == 4 && (mc.currentScreen instanceof GuiNpcEmotion || mc.currentScreen instanceof GuiNpcAnimation))) {
					this.topButtonPressed(button);
				}
			}
		}
	}

	public void save() {
		GuiNpcTextField.unfocus();
		if (this.parent instanceof GuiContainerNPCInterface2) {
			((GuiContainerNPCInterface2) this.parent).save();
		}
		if (this.parent instanceof GuiNPCInterface2) {
			((GuiNPCInterface2) this.parent).save();
		}
	}

	public void topButtonPressed(GuiMenuTopButton button) {
		if (button.displayString.equals("" + this.activeMenu)) {
			return;
		}
		Minecraft mc = Minecraft.getMinecraft();
		NoppesUtil.clickSound();
		int id = button.id;
		this.save();
		if (id == 0) {
			if (this.parent instanceof GuiContainerNPCInterface2) {
				((GuiContainerNPCInterface2) this.parent).close();
			}
			if (this.parent instanceof GuiNPCInterface2) {
				((GuiNPCInterface2) this.parent).close();
			}
			if (this.npc != null) {
				this.npc.reset();
				Client.sendData(EnumPacketServer.NpcMenuClose);
			}
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
			return;
		}
		if (id == 66) {
			GuiYesNo guiyesno = new GuiYesNo(this, "", new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
			mc.displayGuiScreen(guiyesno);
			return;
		}
		CustomNpcs.proxy.getPlayerData(mc.player).editingNpc = npc;
		if (id == 1) {
			CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuDisplay);
		} else if (id == 2) {
			CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuStats);
		} else if (id == 3) {
			NoppesUtil.requestOpenGUI(EnumGuiType.MainMenuInv);
		} else if (id == 4) {
			CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuAdvanced);
		} else if (id == 5) {
			CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuGlobal);
		} else if (id == 6) {
			CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuAI);
		}
		this.activeMenu = id;
	}

}
