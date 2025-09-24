package noppes.npcs.client.gui.mainmenu;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.advanced.GuiNPCAdvancedLinkedNpc;
import noppes.npcs.client.gui.advanced.GuiNPCDialogNpcOptions;
import noppes.npcs.client.gui.advanced.GuiNPCFactionSetup;
import noppes.npcs.client.gui.advanced.GuiNPCLinesMenu;
import noppes.npcs.client.gui.advanced.GuiNPCMarks;
import noppes.npcs.client.gui.advanced.GuiNPCNightSetup;
import noppes.npcs.client.gui.advanced.GuiNPCScenes;
import noppes.npcs.client.gui.advanced.GuiNPCSoundsMenu;
import noppes.npcs.client.gui.animation.GuiNpcAnimation;
import noppes.npcs.client.gui.animation.GuiNpcEmotion;
import noppes.npcs.client.gui.roles.*;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataAI;
import noppes.npcs.roles.RoleTrader;

import javax.annotation.Nonnull;

public class GuiNpcAdvanced extends GuiNPCInterface2 implements IGuiData {

	protected boolean hasChanges = false;
	protected final DataAI ais;

	public GuiNpcAdvanced(EntityNPCInterface npc) {
		super(npc, 4);

		ais = npc.ais;
		Client.sendData(EnumPacketServer.MainmenuAIGet);
		Client.sendData(EnumPacketServer.MainmenuAdvancedGet);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		if (button.getID() != 5 && button.getID() != 8) { save(); }
		switch (button.getID()) {
			case 3: Client.sendData(EnumPacketServer.RoleGet); break;
			case 4: Client.sendData(EnumPacketServer.JobGet); break;
			case 5: {
				hasChanges = true;
				int id = button.getValue();
				if (id > 8) { id++; }
				npc.advanced.setJob(id);
				initGui();
				break;
			} // set job
			case 7: NoppesUtil.openGUI(player, new GuiNPCLinesMenu(npc)); break;
			case 8: {
				hasChanges = true;
				npc.advanced.setRole(button.getValue());
				initGui();
				break;
			} // set role
			case 9: NoppesUtil.openGUI(player, new GuiNPCFactionSetup(npc)); break;
			case 10: NoppesUtil.openGUI(player, new GuiNPCDialogNpcOptions(npc)); break;
			case 11: NoppesUtil.openGUI(player, new GuiNPCSoundsMenu(npc)); break;
			case 12: NoppesUtil.openGUI(player, new GuiNPCNightSetup(npc)); break;
			case 13: NoppesUtil.openGUI(player, new GuiNPCAdvancedLinkedNpc(npc)); break;
			case 14: NoppesUtil.openGUI(player, new GuiNPCScenes(npc)); break;
			case 15: NoppesUtil.openGUI(player, new GuiNPCMarks(npc, this)); break;
			case 16: NoppesUtil.openGUI(player, new GuiNpcAnimation((EntityCustomNpc) npc)); break; // Animation Settings
			case 18: NoppesUtil.openGUI(player, new GuiNpcEmotion((EntityCustomNpc) npc)); break; // Emotion Settings
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = guiLeft + 15;
		int x1 = guiLeft + 85;
		int y = guiTop + 8;
		addLabel(new GuiNpcLabel(0, "role.name", x, y + 5));
		addButton(new GuiNpcButton(3, x + 230, y, 52, 20, "selectServer.edit")
				.setIsEnable(!ais.aiDisabled && npc.advanced.roleInterface.getEnumType().hasSettings));
		if (ais.aiDisabled) { getButton(3).setHoverText("hover.ai.disabled"); }
		ITextComponent mess = new TextComponentTranslation("advanced.menu.hover.role." + npc.advanced.roleInterface.getType());
		if (ais.aiDisabled) { mess.appendSibling(new TextComponentTranslation("hover.ai.disabled")); }
		addButton(new GuiButtonBiDirectional(8, x + 70, y, 155, 20, RoleType.getNames(), npc.advanced.roleInterface.getType())
				.setIsEnable(!ais.aiDisabled)
				.setHoverText(mess.getFormattedText()));
		addLabel(new GuiNpcLabel(1, "job.name", x, (y += 22) + 5));
		addButton(new GuiNpcButton(4, x + 230, y, 52, 20, "selectServer.edit")
				.setIsEnable(!ais.aiDisabled && npc.advanced.jobInterface.getEnumType().hasSettings));
		if (ais.aiDisabled) { getButton(4).setHoverText("hover.ai.disabled"); }
		int id = npc.advanced.jobInterface.getType();
		if (id > 9) { id--; }
		mess = new TextComponentTranslation("advanced.menu.hover.job." + id);
		if (ais.aiDisabled) { mess.appendSibling(new TextComponentTranslation("hover.ai.disabled")); }
		addButton(new GuiButtonBiDirectional(5, x + 70, y, 155, 20, JobType.getNames(), id)
				.setIsEnable(!ais.aiDisabled)
				.setHoverText(mess.getFormattedText()));
		addButton(new GuiNpcButton(7, x, y += 22, 195, 20, "advanced.lines")
				.setHoverText("advanced.menu.hover.says"));
		addButton(new GuiNpcButton(9, x1 += 126, y, 195, 20, "menu.factions")
				.setHoverText("advanced.menu.hover.faction"));
		addButton(new GuiNpcButton(10, x, y += 22, 195, 20, "dialog.dialogs")
				.setHoverText("advanced.menu.hover.dialogs"));
		addButton(new GuiNpcButton(11, x1, y, 195, 20, "advanced.sounds")
				.setHoverText("advanced.menu.hover.sounds"));
		addButton(new GuiNpcButton(12, x, y += 22, 195, 20, "advanced.night")
				.setHoverText("advanced.menu.hover.night"));
		addButton(new GuiNpcButton(13, x1, y, 195, 20, "global.linked")
				.setHoverText("advanced.menu.hover.lines"));
		mess = new TextComponentTranslation("advanced.menu.hover.scenes");
		if (ais.aiDisabled) { mess.appendSibling(new TextComponentTranslation("hover.ai.disabled")); }
		addButton(new GuiNpcButton(14, x, y += 22, 195, 20, "advanced.scenes")
				.setIsEnable(!ais.aiDisabled)
				.setHoverText(mess.getFormattedText()));
		addButton(new GuiNpcButton(15, x1, y, 195, 20, "advanced.marks")
				.setHoverText("advanced.menu.hover.marks"));
		Class<? extends EntityLivingBase> model = null;
		if (npc instanceof EntityCustomNpc) { model = ((EntityCustomNpc) npc).modelData.entityClass; }
		boolean bo = model == null;
		addButton(new GuiNpcButton(16, x, y += 22, 195, 20, "movement.animation")
				.setIsEnable(bo)
				.setHoverText("advanced.menu.hover.anim"));
		addButton(new GuiNpcButton(18, x, y + 22, 195, 20, "advanced.emotion")
				.setIsEnable(bo)
				.setHoverText("animation.hover.eye",
				new TextComponentTranslation("gui.help.general").getFormattedText(),
				new TextComponentTranslation("selectServer.edit").getFormattedText()));
	}

	@Override
	public void save() {
		if (hasChanges) {
			NBTTagCompound compound = new NBTTagCompound();
			npc.advanced.save(compound);
			Client.sendData(EnumPacketServer.MainmenuAdvancedSave, compound);
			hasChanges = false;
		}
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("RoleData")) {
			if (npc.advanced.roleInterface != null) { npc.advanced.roleInterface.load(compound); }
			if (npc.advanced.roleInterface != null) {
				switch (npc.advanced.roleInterface.getEnumType()) {
					case TRADER: setSubGui(new SubGuiNpcSelectTrader(((RoleTrader) npc.advanced.roleInterface).getMarketID())); break;
					case FOLLOWER: NoppesUtil.requestOpenGUI(EnumGuiType.SetupFollower); break;
					case BANK: NoppesUtil.requestOpenGUI(EnumGuiType.SetupBank); break;
					case TRANSPORTER: displayGuiScreen(new GuiNpcTransporter(npc)); break;
					case COMPANION: displayGuiScreen(new GuiNpcCompanion(npc)); break;
					case DIALOG: NoppesUtil.openGUI(player, new GuiRoleDialog(npc)); break;
				}
			}
		}
		else if (compound.hasKey("JobData")) {
			if (npc.advanced.jobInterface != null) { npc.advanced.jobInterface.load(compound); }
			if (npc.advanced.jobInterface != null) {
				switch (npc.advanced.jobInterface.getEnumType()) {
					case BARD: NoppesUtil.openGUI(player, new GuiNpcBard(npc)); break;
					case HEALER: NoppesUtil.openGUI(player, new GuiNpcHealer(npc)); break;
					case GUARD: NoppesUtil.openGUI(player, new GuiNpcGuard(npc)); break;
					case ITEM_GIVER: GuiNpcItemGiver.parent = this; NoppesUtil.requestOpenGUI(EnumGuiType.SetupItemGiver); break;
					case FOLLOWER: NoppesUtil.openGUI(player, new GuiNpcFollowerJob(npc)); break;
					case SPAWNER: NoppesUtil.openGUI(player, new GuiNpcSpawner(npc)); break;
					case CONVERSATION: NoppesUtil.openGUI(player, new GuiNpcConversation(npc)); break;
					case FARMER: NoppesUtil.openGUI(player, new GuiJobFarmer(npc)); break;
				}
			}
		}
		else if (compound.hasKey("NpcInteractLines", 10)) { npc.advanced.load(compound); initGui(); }
		else if (compound.hasKey("NpcInv", 9)) { npc.inventory.readEntityFromNBT(compound); initGui(); }
		else if (compound.hasKey("MovementType", 3)) { ais.readToNBT(compound); }
		initGui();
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiNpcSelectTrader) {
			hasChanges = true;
			((RoleTrader) npc.advanced.roleInterface).setMarket(subgui.id);
			save();
		}
	}

}
