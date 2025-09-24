package noppes.npcs.client.gui.mainmenu;

import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.global.GuiNpcDialogGuiSettings;
import noppes.npcs.client.gui.global.GuiNpcManagePlayerData;
import noppes.npcs.client.gui.global.GuiNpcNaturalSpawns;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class GuiNPCGlobalMainMenu extends GuiNPCInterface2 {

	public GuiNPCGlobalMainMenu(EntityNPCInterface npc) { super(npc, 5); }

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 2: NoppesUtil.requestOpenGUI(EnumGuiType.ManageBanks); break;
			case 3: NoppesUtil.requestOpenGUI(EnumGuiType.ManageFactions); break;
			case 4: NoppesUtil.requestOpenGUI(EnumGuiType.ManageDialogs); break;
			case 5: NoppesUtil.openGUI(player, new GuiNpcDialogGuiSettings(npc)); break;
			case 11: NoppesUtil.requestOpenGUI(EnumGuiType.ManageQuests); break;
			case 12: NoppesUtil.requestOpenGUI(EnumGuiType.ManageTransport, -1, 0, 0); break;
			case 13: NoppesUtil.openGUI(player, new GuiNpcManagePlayerData(npc)); break;
			case 14: NoppesUtil.requestOpenGUI(EnumGuiType.ManageRecipes, 0, 0, 0); break;
			case 15: NoppesUtil.openGUI(player, new GuiNpcNaturalSpawns(npc)); break;
			case 16: NoppesUtil.requestOpenGUI(EnumGuiType.ManageLinked); break;
			case 17: NoppesUtil.requestOpenGUI(EnumGuiType.SetupTrader, -1, -1, 0); break;
			case 19: NoppesUtil.requestOpenGUI(EnumGuiType.ManageMail, 0, 0, 0); break;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int r0 = guiLeft + 75;
		int r1 = guiLeft + 240;
		int y = guiTop + 10;
		addButton(new GuiNpcButton(2, r0, y, 110, 20, "global.banks")
				.setHoverText("global.hover.banks"));
		addButton(new GuiNpcButton(3, r0, (y += 22), 110, 20, "menu.factions")
				.setHoverText("global.hover.factions"));
		addButton(new GuiNpcButton(4, r0, (y += 22), 110, 20, "dialog.dialogs")
				.setHoverText("global.hover.dialogs"));
		addButton(new GuiNpcButton(5, r0 + 112, y, 20, 20, "GUI")
				.setHoverText("global.hover.dialogs.gui"));
		addButton(new GuiNpcButton(11, r0, (y += 22), 110, 20, "quest.quests")
				.setHoverText("global.hover.quests"));
		addButton(new GuiNpcButton(12, r0, (y += 22), 110, 20, "global.transport")
				.setHoverText("global.hover.transports"));
		addButton(new GuiNpcButton(13, r0, (y += 22), 110, 20, "global.playerdata")
				.setHoverText("global.hover.playerdatas"));
		addButton(new GuiNpcButton(14, r0, (y += 22), 110, 20, "global.recipes")
				.setHoverText("global.hover.recipes"));
		addButton(new GuiNpcButton(15, r0, (y += 22), 110, 20, "global.naturalspawn")
				.setHoverText("global.hover.naturalspawns"));
		addButton(new GuiNpcButton(16, r0, y + 22, 110, 20, "global.linked")
				.setHoverText("global.hover.linkeds"));
		y = guiTop + 10;
		addButton(new GuiNpcButton(17, r1, y, 110, 20, "global.market")
				.setHoverText("global.hover.markets"));
		addButton(new GuiNpcButton(18, r1, (y += 22), 110, 20, "global.auctions")
				.setIsEnable(false)
				.setHoverText(new TextComponentTranslation("global.hover.auctions").appendSibling(new TextComponentString("<br>")).appendSibling(new TextComponentTranslation("gui.wip")).getFormattedText()));
		addButton(new GuiNpcButton(19, r1, y + 22, 110, 20, "global.mail")
				.setHoverText("global.hover.mail"));
	}

}
