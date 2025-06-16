package noppes.npcs.client.gui.mainmenu;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiNpcSelectTrader;
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

public class GuiNpcAdvanced
extends GuiNPCInterface2
implements IGuiData, ISubGuiListener {

	private boolean hasChanges;
	private final DataAI ais;

	public GuiNpcAdvanced(EntityNPCInterface npc) {
		super(npc, 4);
		this.ais = npc.ais;
		this.hasChanges = false;
		Client.sendData(EnumPacketServer.MainmenuAIGet);
		Client.sendData(EnumPacketServer.MainmenuAdvancedGet);
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (button.getID() != 5 && button.getID() != 8) { save(); }
		switch (button.getID()) {
			case 3: {
				Client.sendData(EnumPacketServer.RoleGet);
				break;
			}
			case 4: {
				Client.sendData(EnumPacketServer.JobGet);
				break;
			}
			case 5: {
				hasChanges = true;
				int id = button.getValue();
				if (id > 8) { id++; }
				npc.advanced.setJob(id);
				initGui();
				break;
			}
			case 7: {
				NoppesUtil.openGUI(this.player, new GuiNPCLinesMenu(this.npc));
				break;
			}
			case 8: {
				hasChanges = true;
				npc.advanced.setRole(button.getValue());
				initGui();
				break;
			}
			case 9: {
				NoppesUtil.openGUI(this.player, new GuiNPCFactionSetup(this.npc));
				break;
			}
			case 10: {
				NoppesUtil.openGUI(this.player, new GuiNPCDialogNpcOptions(this.npc));
				break;
			}
			case 11: {
				NoppesUtil.openGUI(this.player, new GuiNPCSoundsMenu(this.npc));
				break;
			}
			case 12: {
				NoppesUtil.openGUI(this.player, new GuiNPCNightSetup(this.npc));
				break;
			}
			case 13: {
				NoppesUtil.openGUI(this.player, new GuiNPCAdvancedLinkedNpc(this.npc));
				break;
			}
			case 14: {
				NoppesUtil.openGUI(this.player, new GuiNPCScenes(this.npc));
				break;
			}
			case 15: {
				NoppesUtil.openGUI(this.player, new GuiNPCMarks(this.npc, this));
				break;
			}
			case 16: { // Animation Settings
				NoppesUtil.openGUI(this.player, new GuiNpcAnimation((EntityCustomNpc) this.npc));
				break;
			}
			case 18: { // Emotion Settings
				NoppesUtil.openGUI(this.player, new GuiNpcEmotion((EntityCustomNpc) this.npc));
				break;
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = this.guiLeft + 15, x1 = this.guiLeft + 85;
		int y = this.guiTop + 8;
		this.addLabel(new GuiNpcLabel(0, "role.name", x, y + 5));
		GuiNpcButton button = new GuiNpcButton(3, x + 230, y, 52, 20, "selectServer.edit");
		button.setEnabled(!ais.aiDisabled && this.npc.advanced.roleInterface.getEnumType().hasSettings);
		if (ais.aiDisabled) { button.setHoverText("hover.ai.disabled"); }
		addButton(button);
		button = new GuiButtonBiDirectional(8, x + 70, y, 155, 20, RoleType.getNames(), this.npc.advanced.roleInterface.getType());
		button.setEnabled(!ais.aiDisabled);
		ITextComponent mess = new TextComponentTranslation("advanced.menu.hover.role." + this.npc.advanced.roleInterface.getType());
		if (this.ais.aiDisabled) { mess.appendSibling(new TextComponentTranslation("hover.ai.disabled")); }
		button.setHoverText(mess.getFormattedText());
		addButton(button);

		this.addLabel(new GuiNpcLabel(1, "job.name", x, (y += 22) + 5));
		button = new GuiNpcButton(4, x + 230, y, 52, 20, "selectServer.edit");
		button.setEnabled(!this.ais.aiDisabled && this.npc.advanced.jobInterface.getEnumType().hasSettings);
		if (ais.aiDisabled) { button.setHoverText("hover.ai.disabled"); }
		addButton(button);
		int id = npc.advanced.jobInterface.getType();
		if (id > 9) { id--; }
		button = new GuiButtonBiDirectional(5, x + 70, y, 155, 20, JobType.getNames(), id);
		button.setEnabled(!this.ais.aiDisabled);
		mess = new TextComponentTranslation("advanced.menu.hover.job." + id);
		if (this.ais.aiDisabled) { mess.appendSibling(new TextComponentTranslation("hover.ai.disabled")); }
		button.setHoverText(mess.getFormattedText());
		addButton(button);
		
		x1 += 126;
		button = new GuiNpcButton(7, x, y += 22, 195, 20, "advanced.lines");
		button.setHoverText("advanced.menu.hover.says");
		addButton(button);
		button = new GuiNpcButton(9, x1, y, 195, 20, "menu.factions");
		button.setHoverText("advanced.menu.hover.faction");
		addButton(button);

		button = new GuiNpcButton(10, x, y += 22, 195, 20, "dialog.dialogs");
		button.setHoverText("advanced.menu.hover.dialogs");
		addButton(button);
		button = new GuiNpcButton(11, x1, y, 195, 20, "advanced.sounds");
		button.setHoverText("advanced.menu.hover.sounds");
		addButton(button);

		button = new GuiNpcButton(12, x, y += 22, 195, 20, "advanced.night");
		button.setHoverText("advanced.menu.hover.night");
		addButton(button);
		button = new GuiNpcButton(13, x1, y, 195, 20, "global.linked");
		button.setHoverText("advanced.menu.hover.lines");
		addButton(button);

		button = new GuiNpcButton(14, x, y += 22, 195, 20, "advanced.scenes");
		button.setEnabled(!this.ais.aiDisabled);
		mess = new TextComponentTranslation("advanced.menu.hover.scenes");
		if (this.ais.aiDisabled) { mess.appendSibling(new TextComponentTranslation("hover.ai.disabled")); }
		button.setHoverText(mess.getFormattedText());
		addButton(button);
		button = new GuiNpcButton(15, x1, y, 195, 20, "advanced.marks");
		button.setHoverText("advanced.menu.hover.marks");
		addButton(button);

		Class<? extends EntityLivingBase> model = null;
		if (this.npc instanceof EntityCustomNpc) { model = ((EntityCustomNpc) this.npc).modelData.entityClass; }
		boolean bo = model == null;
		button = new GuiNpcButton(16, x, y += 22, 195, 20, "movement.animation");
		button.setEnabled(bo);
		button.setHoverText("advanced.menu.hover.anim");
		addButton(button);
		button = new GuiNpcButton(18, x, y + 22, 195, 20, "advanced.emotion");
		button.setEnabled(bo);
		button.setHoverText("animation.hover.eye",
				new TextComponentTranslation("gui.help.general").getFormattedText(),
				new TextComponentTranslation("selectServer.edit").getFormattedText());
		addButton(button);
	}

	@Override
	public void save() {
		if (hasChanges) {
			Client.sendData(EnumPacketServer.MainmenuAdvancedSave, npc.advanced.writeToNBT(new NBTTagCompound()));
			this.hasChanges = false;
		}
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("RoleData")) {
			if (this.npc.advanced.roleInterface != null) { npc.advanced.roleInterface.readFromNBT(compound); }
			if (this.npc.advanced.roleInterface != null) {
				switch (this.npc.advanced.roleInterface.getEnumType()) {
					case TRADER: {
						RoleTrader role = (RoleTrader) this.npc.advanced.roleInterface;
						this.setSubGui(new SubGuiNpcSelectTrader(role.getMarketID()));
						break;
					}
					case FOLLOWER: {
						NoppesUtil.requestOpenGUI(EnumGuiType.SetupFollower);
						break;
					}
					case BANK: {
						NoppesUtil.requestOpenGUI(EnumGuiType.SetupBank);
						break;
					}
					case TRANSPORTER: {
						displayGuiScreen(new GuiNpcTransporter(this.npc));
						break;
					}
					case COMPANION: {
						displayGuiScreen(new GuiNpcCompanion(this.npc));
						break;
					}
					case DIALOG: {
						NoppesUtil.openGUI(this.player, new GuiRoleDialog(this.npc));
						break;
					}
					default: {
						break;
					}
				}
			}
		}
		else if (compound.hasKey("JobData")) {
			if (this.npc.advanced.jobInterface != null) {
				this.npc.advanced.jobInterface.readFromNBT(compound);
			}
			if (this.npc.advanced.jobInterface != null) {
				switch (this.npc.advanced.jobInterface.getEnumType()) {
					case BARD: {
						NoppesUtil.openGUI(this.player, new GuiNpcBard(this.npc));
						break;
					}
					case HEALER: {
						NoppesUtil.openGUI(this.player, new GuiNpcHealer(this.npc));
						break;
					}
					case GUARD: {
						NoppesUtil.openGUI(this.player, new GuiNpcGuard(this.npc));
						break;
					}
					case ITEM_GIVER: {
						GuiNpcItemGiver.parent = this;
						NoppesUtil.requestOpenGUI(EnumGuiType.SetupItemGiver);
						break;
					}
					case FOLLOWER: {
						NoppesUtil.openGUI(this.player, new GuiNpcFollowerJob(this.npc));
						break;
					}
					case SPAWNER: {
						NoppesUtil.openGUI(this.player, new GuiNpcSpawner(this.npc));
						break;
					}
					case CONVERSATION: {
						NoppesUtil.openGUI(this.player, new GuiNpcConversation(this.npc));
						break;
					}
					case FARMER: {
						NoppesUtil.openGUI(this.player, new GuiJobFarmer(this.npc));
						break;
					}
					default: {
						break;
					}
				}
			}
		}
		else if (compound.hasKey("NpcInteractLines", 10)) {
			this.npc.advanced.readToNBT(compound);
			this.initGui();
		}
		else if (compound.hasKey("NpcInv", 9)) {
			this.npc.inventory.readEntityFromNBT(compound);
			this.initGui();
		}
		else if (compound.hasKey("MovementType", 3)) {
			this.ais.readToNBT(compound);
		}
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiNpcSelectTrader) {
			this.hasChanges = true;
			RoleTrader role = (RoleTrader) this.npc.advanced.roleInterface;
			role.setMarket((((SubGuiNpcSelectTrader) subgui)).id);
			this.save();
		}
	}

}
