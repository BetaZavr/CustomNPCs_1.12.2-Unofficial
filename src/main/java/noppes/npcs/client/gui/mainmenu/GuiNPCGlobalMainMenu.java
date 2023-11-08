package noppes.npcs.client.gui.mainmenu;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.global.GuiNpcManagePlayerData;
import noppes.npcs.client.gui.global.GuiNpcNaturalSpawns;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCGlobalMainMenu
extends GuiNPCInterface2 {
	
	public GuiNPCGlobalMainMenu(EntityNPCInterface npc) {
		super(npc, 5);
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		switch (guibutton.id) {
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
				NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNpcManagePlayerData(this.npc, this));
				break;
			}
			case 14: { // Changed
				ClientProxy.recipeGroup = "";
				ClientProxy.recipeName = "";
				NoppesUtil.requestOpenGUI(EnumGuiType.ManageRecipes, 4, 0, 0);
				break;
			}
			case 15: {
				NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNpcNaturalSpawns(this.npc));
				break;
			}
			case 16: {
				NoppesUtil.requestOpenGUI(EnumGuiType.ManageLinked);
				break;
			}
			case 17: {
				NoppesUtil.requestOpenGUI(EnumGuiType.SetupTrader, 0, -1, 0);
				break;
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int r0 = this.guiLeft + 75;
		int y = this.guiTop + 10;
		this.addButton(new GuiNpcButton(2, r0, y, 110, 20, "global.banks"));
		y += 22;
		this.addButton(new GuiNpcButton(3, r0, y, 110, 20, "menu.factions"));
		y += 22;
		this.addButton(new GuiNpcButton(4, r0, y, 110, 20, "dialog.dialogs"));
		y += 22;
		this.addButton(new GuiNpcButton(11, r0, y, 110, 20, "quest.quests"));
		y += 22;
		this.addButton(new GuiNpcButton(12, r0, y, 110, 20, "global.transport"));
		y += 22;
		this.addButton(new GuiNpcButton(13, r0, y, 110, 20, "global.playerdata"));
		y += 22;
		this.addButton(new GuiNpcButton(14, r0, y, 110, 20, "global.recipes")); // Changed
		y += 22;
		this.addButton( new GuiNpcButton(15, r0, y, 110, 20, NoppesStringUtils.translate("global.naturalspawn", "(WIP)")));
		y += 22;
		this.addButton(new GuiNpcButton(16, r0, y, 110, 20, "global.linked"));
		int r1 = this.guiLeft + 240;
		y = this.guiTop + 10;
		this.addButton(new GuiNpcButton(17, r1, y, 110, 20, "global.market"));
	}

	@Override
	public void save() {
	}
	
	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		// New
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) { // add new
			this.setHoverText(new TextComponentTranslation("global.hover.banks").getFormattedText());
		} else if (this.getButton(3)!=null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("global.hover.factions").getFormattedText());
		} else if (this.getButton(4)!=null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("global.hover.dialogs").getFormattedText());
		} else if (this.getButton(11)!=null && this.getButton(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("global.hover.quests").getFormattedText());
		} else if (this.getButton(12)!=null && this.getButton(12).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("global.hover.transports").getFormattedText());
		} else if (this.getButton(13)!=null && this.getButton(13).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("global.hover.playerdatas").getFormattedText());
		} else if (this.getButton(14)!=null && this.getButton(14).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("global.hover.recipes").getFormattedText());
		} else if (this.getButton(15)!=null && this.getButton(15).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("global.hover.naturalspawns").getFormattedText());
		} else if (this.getButton(16)!=null && this.getButton(16).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("global.hover.linkeds").getFormattedText());
		} else if (this.getButton(17)!=null && this.getButton(17).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("global.hover.markets").getFormattedText());
		}
	}
	
}
