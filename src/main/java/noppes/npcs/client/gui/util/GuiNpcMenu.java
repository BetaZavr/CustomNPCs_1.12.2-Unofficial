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
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNpcMenu implements GuiYesNoCallback {

	public int activeMenu;
	private final EntityNPCInterface npc;
	private final IEditNPC parent;
	private GuiMenuTopButton[] topButtons = new GuiMenuTopButton[0];

	public GuiNpcMenu(IEditNPC gui, int menu, EntityNPCInterface npc) {
		parent = gui;
		activeMenu = menu;
		this.npc = npc;
	}

	public void confirmClicked(boolean flag, int id) {
		Minecraft mc = Minecraft.getMinecraft();
		if (flag) {
			Client.sendData(EnumPacketServer.Delete);
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
		}
		else { NoppesUtil.openGUI(mc.player, parent); }
	}

	public void drawElements(IEditNPC gui, int mouseX, int mouseY, float partialTicks) {
		for (GuiMenuTopButton button : getTopButtons()) { button.render(gui, mouseX, mouseY, partialTicks); }
	}

	public GuiMenuTopButton[] getTopButtons() { return topButtons; }

	public void initGui(int guiLeft, int guiTop, int width) {
		Keyboard.enableRepeatEvents(true);
		GuiMenuTopButton display = new GuiMenuTopButton(1, guiLeft + 4, guiTop - 17, "menu.display");
		String text = new TextComponentTranslation("display.hover." + display.label).getFormattedText();
		String str_0, str_1;
		display.setHoverText(text + "<br>" + ((char) 167) + "7" + new TextComponentTranslation("gui.name").getFormattedText() + ((char) 167)
				+ "7: " + ((char) 167) + "r" + npc.display.getName() + ((char) 167) + "7;"
				+ "<br>" + ((char) 167) + "7" + new TextComponentTranslation("gui.title").getFormattedText() + ((char) 167)
				+ "7: <" + ((char) 167) + "r" + npc.display.getTitle() + ((char) 167) + "7>;"
				+ "<br>" + ((char) 167) + "7" + new TextComponentTranslation("display.model").getFormattedText()
				+ ((char) 167) + "7 " + new TextComponentTranslation("display.size").getFormattedText() + ((char) 167)
				+ "7: " + ((char) 167) + "r" + npc.display.getSize() + ((char) 167) + "7;");
		GuiMenuTopButton stats = new GuiMenuTopButton(2, display.x + display.width, guiTop - 17, "menu.stats");
		switch (npc.stats.spawnCycle) {
			case 0: str_0 = "gui.yes"; break;
			case 1: str_0 = "gui.day"; break;
			case 2: str_0 = "gui.night"; break;
			case 4: str_0 = "stats.naturally"; break;
			default: str_0 = "gui.no";
		}
		stats.setHoverText(text + "<br>" + ((char) 167) + "7" + new TextComponentTranslation("stats.health").getFormattedText()
				+ ((char) 167) + "7: " + ((char) 167) + "r" + npc.stats.maxHealth + ((char) 167) + "7;"
				+ "<br>" + ((char) 167) + "7" + new TextComponentTranslation("stats.aggro").getFormattedText()
				+ ((char) 167) + "7: " + ((char) 167) + "r" + npc.stats.aggroRange + ((char) 167) + "7;"
				+ "<br>" + ((char) 167) + "7" + new TextComponentTranslation("stats.respawn").getFormattedText()
				+ ((char) 167) + "7: " + ((char) 167) + "r" + new TextComponentTranslation(str_0).getFormattedText() + ((char) 167)
				+ "7;" + "<br>" + ((char) 167) + "7"
				+ new TextComponentTranslation("stats.meleeproperties").getFormattedText() + ((char) 167) + "7 "
				+ new TextComponentTranslation("stats.meleestrength").getFormattedText() + ((char) 167) + "7: "
				+ ((char) 167) + "r" + npc.stats.melee.getStrength() + ((char) 167) + "7;"
				+ "<br>" + ((char) 167) + "7"
				+ new TextComponentTranslation("stats.rangedproperties").getFormattedText() + ((char) 167) + "7 "
				+ new TextComponentTranslation("enchantment.arrowDamage").getFormattedText() + ((char) 167)
				+ "7: " + ((char) 167) + "r" + npc.stats.ranged.getStrength() + ((char) 167) + "7;");
		GuiMenuTopButton ai = new GuiMenuTopButton(6, stats.x + stats.width, guiTop - 17, "menu.ai");
		switch (npc.ais.onAttack) {
			case 0: str_0 = "gui.retaliate"; break;
			case 1: str_0 = "gui.panic"; break;
			case 2: str_0 = "gui.retreat"; break;
			default: str_0 = "gui.nothing";
		}
		switch (npc.ais.getMovingType()) {
			case 0: str_1 = "ai.standing"; break;
			case 1: str_1 = "ai.wandering"; break;
			default: str_1 = "ai.movingpath";
		}
		ai.setHoverText(text + "<br>" + ((char) 167) + "7" + new TextComponentTranslation("ai.enemyresponse").getFormattedText()
				+ ((char) 167) + "7: " + ((char) 167) + "r" + new TextComponentTranslation(str_0).getFormattedText() + ((char) 167)
				+ "7;" + "<br>" + ((char) 167) + "7" + new TextComponentTranslation("movement.type").getFormattedText()
				+ ((char) 167) + "7: " + ((char) 167) + "r" + new TextComponentTranslation(str_1).getFormattedText() + ((char) 167)
				+ "7;"
				+ "<br>" + ((char) 167) + "7" + new TextComponentTranslation("stats.movespeed").getFormattedText()
				+ ((char) 167) + "7: " + ((char) 167) + "r" + npc.ais.getWalkingSpeed() + ((char) 167) + "7;");
		GuiMenuTopButton inv = new GuiMenuTopButton(3, ai.x + ai.width, guiTop - 17, "menu.inventory");
		inv.setHoverText(text + "<br>" + ((char) 167) + "7" + new TextComponentTranslation("quest.exp").getFormattedText() + ((char) 167)
				+ "7: " + ((char) 167) + "r" + npc.inventory.getExpMin() + ((char) 167) + "7/" + ((char) 167) + "r"
				+ npc.inventory.getExpMax() + ((char) 167) + "7;"
				+ "<br>" + ((char) 167) + "7"
				+ new TextComponentTranslation("questlog.all.reward").getFormattedText() + ((char) 167) + "r"
				+ npc.inventory.drops.size() + ((char) 167) + "7;");
		GuiMenuTopButton advanced = new GuiMenuTopButton(4, inv.x + inv.width, guiTop - 17, "menu.advanced");
		advanced.setHoverText(text + "<br>" + ((char) 167) + "7" + new TextComponentTranslation("role.name").getFormattedText() + ((char) 167)
				+ "7: " + ((char) 167) + "r"
				+ new TextComponentTranslation(npc.advanced.roleInterface.getEnumType().name).getFormattedText()
				+ ((char) 167) + "7;"
				+ "<br>" + ((char) 167) + "7" + new TextComponentTranslation("job.name").getFormattedText() + ((char) 167)
				+ "7: " + ((char) 167) + "r"
				+ new TextComponentTranslation(npc.advanced.jobInterface.getEnumType().name).getFormattedText()
				+ ((char) 167) + "7;"
				+ "<br>" + ((char) 167) + "7" + new TextComponentTranslation("menu.factions").getFormattedText()
				+ ((char) 167) + "7: " + ((char) 167) + "r"
				+ new TextComponentTranslation(npc.getFaction().name).getFormattedText() + ((char) 167)
				+ "7;");
		GuiMenuTopButton global = new GuiMenuTopButton(5, advanced.x + advanced.width, guiTop - 17, "menu.global");
		GuiMenuTopButton close = new GuiMenuTopButton(0, guiLeft + width - 22, guiTop - 17, "X");
		GuiMenuTopButton delete = new GuiMenuTopButton(66, guiLeft + width - 72, guiTop - 17, "selectWorld.deleteButton");
		delete.x = close.x - delete.width;
		topButtons = new GuiMenuTopButton[] { display, stats, ai, inv, advanced, global, close, delete };
		for (GuiMenuTopButton button : getTopButtons()) {
			button.active = (button.id == activeMenu);
		}
	}

	public boolean mouseCnpcsPressed(int mouseX, int mouseY, int mouseButton) {
		if (mouseButton == 0) {
			Minecraft mc = Minecraft.getMinecraft();
			for (GuiMenuTopButton button : getTopButtons()) {
				boolean bo = button.visible && button.isMouseOver();
				if (button.mouseCnpcsPressed(mouseX, mouseY, mouseButton) || (bo && button.id == 4 && (mc.currentScreen instanceof GuiNpcEmotion || mc.currentScreen instanceof GuiNpcAnimation))) {
					return topButtonPressed(button);
				}
			}
		}
		return false;
	}

	public void save() {
		GuiNpcTextField.unfocus();
		parent.save();
	}

	public boolean topButtonPressed(GuiMenuTopButton button) {
		if (button.displayString.equals("" + activeMenu)) { return false; }
		Minecraft mc = Minecraft.getMinecraft();
		NoppesUtil.clickSound();
		int id = button.id;
		save();
		if (id == 0) {
			((GuiScreen) parent).onGuiClosed();
			if (npc != null) {
				npc.reset();
				Client.sendData(EnumPacketServer.NpcMenuClose);
			}
			mc.displayGuiScreen(null);
			mc.setIngameFocus();
			return true;
		}
		if (id == 66) {
			GuiYesNo guiyesno = new GuiYesNo(this, "", new TextComponentTranslation("gui.deleteMessage").getFormattedText(), 0);
			mc.displayGuiScreen(guiyesno);
			return true;
		}
		CustomNpcs.proxy.getPlayerData(mc.player).editingNpc = npc;
		switch (id) {
			case 1: CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuDisplay); break;
			case 2: CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuStats); break;
			case 3: NoppesUtil.requestOpenGUI(EnumGuiType.MainMenuInv); break;
			case 4: CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuAdvanced); break;
			case 5: CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuGlobal); break;
			case 6: CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuAI); break;
		}
		activeMenu = id;
		return true;
	}

}
