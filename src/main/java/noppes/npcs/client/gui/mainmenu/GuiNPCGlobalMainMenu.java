package noppes.npcs.client.gui.mainmenu;

import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.global.GuiNpcManagePlayerData;
import noppes.npcs.client.gui.global.GuiNpcNaturalSpawns;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.IGuiNpcButton;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCGlobalMainMenu
extends GuiNPCInterface2 {

	public GuiNPCGlobalMainMenu(EntityNPCInterface npc) {
		super(npc, 5);
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getId()) {
			case 2: {
				NoppesUtil.requestOpenGUI(EnumGuiType.ManageBanks);
				break;
			}
			case 3: {
				NoppesUtil.requestOpenGUI(EnumGuiType.ManageFactions);
				break;
			}
			case 4: {
				NoppesUtil.requestOpenGUI(EnumGuiType.ManageDialogs);
				break;
			}
			case 11: {
				NoppesUtil.requestOpenGUI(EnumGuiType.ManageQuests);
				break;
			}
			case 12: {
				NoppesUtil.requestOpenGUI(EnumGuiType.ManageTransport, -1, 0, 0);
				break;
			}
			case 13: {
				NoppesUtil.openGUI(this.player, new GuiNpcManagePlayerData(this.npc));
				break;
			}
			case 14: {
				NoppesUtil.requestOpenGUI(EnumGuiType.ManageRecipes, 0, 0, 0);
				break;
			}
			case 15: {
				NoppesUtil.openGUI(this.player, new GuiNpcNaturalSpawns(this.npc));
				break;
			}
			case 16: {
				NoppesUtil.requestOpenGUI(EnumGuiType.ManageLinked);
				break;
			}
			case 17: {
				NoppesUtil.requestOpenGUI(EnumGuiType.SetupTrader, -1, -1, 0);
				break;
			}
			case 19: {
				NoppesUtil.requestOpenGUI(EnumGuiType.ManageMail, 0, 0, 0);
				break;
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int r0 = guiLeft + 75;
		int r1 = guiLeft + 240;
		int y = guiTop + 10;
		GuiNpcButton button = new GuiNpcButton(2, r0, y, 110, 20, "global.banks");
		button.setHoverText("global.hover.banks");
		addButton(button);
		button = new GuiNpcButton(3, r0, (y += 22), 110, 20, "menu.factions");
		button.setHoverText("global.hover.factions");
		addButton(button);
		button = new GuiNpcButton(4, r0, (y += 22), 110, 20, "dialog.dialogs");
		button.setHoverText("global.hover.dialogs");
		addButton(button);
		button = new GuiNpcButton(11, r0, (y += 22), 110, 20, "quest.quests");
		button.setHoverText("global.hover.quests");
		addButton(button);
		button = new GuiNpcButton(12, r0, (y += 22), 110, 20, "global.transport");
		button.setHoverText("global.hover.transports");
		addButton(button);
		button = new GuiNpcButton(13, r0, (y += 22), 110, 20, "global.playerdata");
		button.setHoverText("global.hover.playerdatas");
		addButton(button);
		button = new GuiNpcButton(14, r0, (y += 22), 110, 20, "global.recipes");
		button.setHoverText("global.hover.recipes");
		addButton(button);
		button = new GuiNpcButton(15, r0, (y += 22), 110, 20, "global.naturalspawn");
		button.setHoverText("global.hover.naturalspawns");
		addButton(button);
		button = new GuiNpcButton(16, r0, y + 22, 110, 20, "global.linked");
		button.setHoverText("global.hover.linkeds");
		addButton(button);
		y = guiTop + 10;
		button = new GuiNpcButton(17, r1, y, 110, 20, "global.market");
		button.setHoverText("global.hover.markets");
		addButton(button);
		button = new GuiNpcButton(18, r1, (y += 22), 110, 20, "global.auctions");
		button.setEnabled(false);
		button.setHoverText(new TextComponentTranslation("global.hover.auctions").appendSibling(new TextComponentString("<br>")).appendSibling(new TextComponentTranslation("gui.wip")).getFormattedText());
		addButton(button);
		button = new GuiNpcButton(19, r1, y + 22, 110, 20, "global.mail");
		button.setHoverText("global.hover.mail");
		addButton(button);
	}

	@Override
	public void save() { }

}
