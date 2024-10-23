package noppes.npcs.client.gui.mainmenu;

import java.util.Arrays;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
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
import noppes.npcs.client.gui.roles.GuiJobFarmer;
import noppes.npcs.client.gui.roles.GuiNpcBard;
import noppes.npcs.client.gui.roles.GuiNpcCompanion;
import noppes.npcs.client.gui.roles.GuiNpcConversation;
import noppes.npcs.client.gui.roles.GuiNpcFollowerJob;
import noppes.npcs.client.gui.roles.GuiNpcGuard;
import noppes.npcs.client.gui.roles.GuiNpcHealer;
import noppes.npcs.client.gui.roles.GuiNpcSpawner;
import noppes.npcs.client.gui.roles.GuiNpcTransporter;
import noppes.npcs.client.gui.roles.GuiRoleDialog;
import noppes.npcs.client.gui.util.GuiButtonBiDirectional;
import noppes.npcs.client.gui.util.GuiNPCInterface2;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataAI;
import noppes.npcs.roles.RoleTrader;

public class GuiNpcAdvanced extends GuiNPCInterface2 implements IGuiData, ISubGuiListener {

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
	public void buttonEvent(GuiNpcButton button) {
		if (button.id != 5 && button.id != 8) {
			this.save();
		}
		switch (button.id) {
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
			getButton(4).setEnabled(npc.advanced.jobInterface.getEnumType().hasSettings);
			break;
		}
		case 7: {
			NoppesUtil.openGUI(this.player, new GuiNPCLinesMenu(this.npc));
			break;
		}
		case 8: {
			this.hasChanges = true;
			this.npc.advanced.setRole(button.getValue());
			this.getButton(3).setEnabled(this.npc.advanced.roleInterface.getEnumType().hasSettings);
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
			NoppesUtil.openGUI(this.player, new GuiNPCMarks(this.npc));
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
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (!CustomNpcs.ShowDescriptions) {
			return;
		}
		if (this.getButton(3) != null && this.getButton(3).isMouseOver() && this.ais.aiDisabled) {
			this.setHoverText(new TextComponentTranslation("hover.ai.disabled").getFormattedText());
		} else if (this.getButton(4) != null && this.getButton(4).isMouseOver() && this.ais.aiDisabled) {
			this.setHoverText(new TextComponentTranslation("hover.ai.disabled").getFormattedText());
		} else if (this.getButton(5) != null && this.getButton(5).isMouseOver()) {
			ITextComponent mess = new TextComponentTranslation("advanced.menu.hover.job." + this.npc.advanced.jobInterface.getType());
			if (this.ais.aiDisabled) { mess.appendSibling(new TextComponentTranslation("hover.ai.disabled")); }
			this.setHoverText(mess.getFormattedText());
		} else if (this.getButton(7) != null && this.getButton(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("advanced.menu.hover.says").getFormattedText());
		} else if (this.getButton(8) != null && this.getButton(8).isMouseOver()) {
			ITextComponent mess = new TextComponentTranslation("advanced.menu.hover.role." + this.npc.advanced.roleInterface.getType());
			if (this.ais.aiDisabled) { mess.appendSibling(new TextComponentTranslation("hover.ai.disabled")); }
			this.setHoverText(mess.getFormattedText());
		} else if (this.getButton(9) != null && this.getButton(9).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("advanced.menu.hover.faction").getFormattedText());
		} else if (this.getButton(10) != null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("advanced.menu.hover.dialogs").getFormattedText());
		} else if (this.getButton(11) != null && this.getButton(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("advanced.menu.hover.sounds").getFormattedText());
		} else if (this.getButton(12) != null && this.getButton(12).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("advanced.menu.hover.night").getFormattedText());
		} else if (this.getButton(13) != null && this.getButton(13).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("advanced.menu.hover.lines").getFormattedText());
		} else if (this.getButton(14) != null && this.getButton(14).isMouseOver()) {
			ITextComponent mess = new TextComponentTranslation("advanced.menu.hover.scenes");
			if (this.ais.aiDisabled) { mess.appendSibling(new TextComponentTranslation("hover.ai.disabled")); }
			this.setHoverText(mess.getFormattedText());
		} else if (this.getButton(15) != null && this.getButton(15).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("advanced.menu.hover.marks").getFormattedText());
		} else if (this.getButton(16) != null && this.getButton(16).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("advanced.menu.hover.anim").getFormattedText());
		} else if (this.getButton(18) != null && this.getButton(18).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.eye",
					new TextComponentTranslation("gui.help.general").getFormattedText(),
					new TextComponentTranslation("selectServer.edit").getFormattedText()).getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = this.guiLeft + 15, x1 = this.guiLeft + 85;
		int y = this.guiTop + 8;
		this.addLabel(new GuiNpcLabel(0, "role.name", x, y + 5));
		this.addButton(new GuiNpcButton(3, x + 230, y, 52, 20, "selectServer.edit"));
		this.getButton(3).setEnabled(!this.ais.aiDisabled && this.npc.advanced.roleInterface.getEnumType().hasSettings);
		this.addButton(new GuiButtonBiDirectional(8, x + 70, y, 155, 20, RoleType.getNames(), this.npc.advanced.roleInterface.getType()));
		this.getButton(8).setEnabled(!this.ais.aiDisabled);

		this.addLabel(new GuiNpcLabel(1, "job.name", x, (y += 22) + 5));
		this.addButton(new GuiNpcButton(4, x + 230, y, 52, 20, "selectServer.edit"));
		this.getButton(4).setEnabled(!this.ais.aiDisabled && this.npc.advanced.jobInterface.getEnumType().hasSettings);
		int id = npc.advanced.jobInterface.getType();
		if (id > 9) { id--; }
		this.addButton(new GuiButtonBiDirectional(5, x + 70, y, 155, 20, JobType.getNames(), id));
		this.getButton(5).setEnabled(!this.ais.aiDisabled);
		
		x1 += 126;
		this.addButton(new GuiNpcButton(7, x, y += 22, 195, 20, "advanced.lines"));
		this.addButton(new GuiNpcButton(9, x1, y, 195, 20, "menu.factions"));

		this.addButton(new GuiNpcButton(10, x, y += 22, 195, 20, "dialog.dialogs"));
		this.addButton(new GuiNpcButton(11, x1, y, 195, 20, "advanced.sounds"));

		this.addButton(new GuiNpcButton(12, x, y += 22, 195, 20, "advanced.night"));
		this.addButton(new GuiNpcButton(13, x1, y, 195, 20, "global.linked"));

		this.addButton(new GuiNpcButton(14, x, y += 22, 195, 20, "advanced.scenes"));
		this.getButton(14).setEnabled(!this.ais.aiDisabled);
		this.addButton(new GuiNpcButton(15, x1, y, 195, 20, "advanced.marks"));

		Class<? extends EntityLivingBase> model = null;
		if (this.npc instanceof EntityCustomNpc) {
			model = ((EntityCustomNpc) this.npc).modelData.entityClass;
		}
		boolean bo = model == null;
		this.addButton(new GuiNpcButton(16, x, y += 22, 195, 20, "movement.animation"));
		this.getButton(16).setEnabled(bo);
		this.addButton(new GuiNpcButton(18, x, y + 22, 195, 20, "advanced.emotion"));
		this.getButton(18).setEnabled(bo);
	}

	@Override
	public void save() {
		if (this.hasChanges) {
			Client.sendData(EnumPacketServer.MainmenuAdvancedSave, this.npc.advanced.writeToNBT(new NBTTagCompound()));
			this.hasChanges = false;
		}
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("RoleData")) {
			if (this.npc.advanced.roleInterface != null) {
				this.npc.advanced.roleInterface.readFromNBT(compound);
			}
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
						this.displayGuiScreen(new GuiNpcTransporter(this.npc));
						break;
					}
					case COMPANION: {
						this.displayGuiScreen(new GuiNpcCompanion(this.npc));
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
		} else if (compound.hasKey("JobData")) {
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
		} else if (compound.hasKey("NpcInteractLines", 10)) {
			this.npc.advanced.readToNBT(compound);
			this.initGui();
		} else if (compound.hasKey("NpcInv", 9)) {
			this.npc.inventory.readEntityFromNBT(compound);
			this.initGui();
		} else if (compound.hasKey("MovementType", 3)) {
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
