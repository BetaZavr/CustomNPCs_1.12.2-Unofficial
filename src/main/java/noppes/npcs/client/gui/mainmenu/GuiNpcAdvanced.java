package noppes.npcs.client.gui.mainmenu;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.NoppesUtil;
import noppes.npcs.client.gui.SubGuiNpcSelectTraider;
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
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumNpcJob;
import noppes.npcs.constants.EnumNpcRole;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTrader;

public class GuiNpcAdvanced
extends GuiNPCInterface2
implements IGuiData, ISubGuiListener {
	
	private boolean hasChanges;

	public GuiNpcAdvanced(EntityNPCInterface npc) {
		super(npc, 4);
		this.hasChanges = false;
		Client.sendData(EnumPacketServer.MainmenuAdvancedGet, new Object[0]);
	}

	@Override
	public void initGui() {
		super.initGui();
		int y = this.guiTop + 8;
		this.addButton(new GuiNpcButton(3, this.guiLeft + 85 + 160, y, 52, 20, "selectServer.edit"));
		this.addButton(new GuiButtonBiDirectional(8, this.guiLeft + 85, y, 155, 20, EnumNpcRole.getNames(), this.npc.advanced.roleInterface.getType()));
		this.getButton(3).setEnabled(this.npc.advanced.roleInterface.getEnumType().hasSettings);
		this.addButton(new GuiNpcButton(4, this.guiLeft + 245, y += 22, 52, 20, "selectServer.edit"));
		this.addButton(new GuiButtonBiDirectional(5, this.guiLeft + 85, y, 155, 20, EnumNpcJob.getNames(), this.npc.advanced.jobInterface.getType()));
		this.getButton(4).setEnabled(this.npc.advanced.jobInterface.getEnumType().hasSettings);
		this.addButton(new GuiNpcButton(7, this.guiLeft + 15, y += 22, 190, 20, "advanced.lines"));
		this.addButton(new GuiNpcButton(9, this.guiLeft + 208, y, 190, 20, "menu.factions"));
		this.addButton(new GuiNpcButton(10, this.guiLeft + 15, y += 22, 190, 20, "dialog.dialogs"));
		this.addButton(new GuiNpcButton(11, this.guiLeft + 208, y, 190, 20, "advanced.sounds"));
		this.addButton(new GuiNpcButton(12, this.guiLeft + 15, y += 22, 190, 20, "advanced.night"));
		this.addButton(new GuiNpcButton(13, this.guiLeft + 208, y, 190, 20, "global.linked"));
		this.addButton(new GuiNpcButton(14, this.guiLeft + 15, y += 22, 190, 20, "advanced.scenes"));
		this.addButton(new GuiNpcButton(15, this.guiLeft + 208, y, 190, 20, "advanced.marks"));
		this.addButton(new GuiNpcButton(16, this.guiLeft + 15, y += 22, 190, 20, "movement.animation"));
		this.getButton(16).enabled = ((EntityCustomNpc) this.npc).modelData.entityClass==null;
		this.addButton(new GuiNpcButton(18, this.guiLeft + 15, y += 22, 190, 20, "advanced.emotion"));
		this.getButton(18).enabled = ((EntityCustomNpc) this.npc).modelData.eyes.type!=(byte)-1;
		this.getButton(18).enabled = false; // WIP
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch (button.id) {
			case 3: {
				this.save();
				Client.sendData(EnumPacketServer.RoleGet);
				break;
			}
			case 4: {
				this.save();
				Client.sendData(EnumPacketServer.JobGet);
				break;
			}
			case 5: {
				this.hasChanges = true;
				this.npc.advanced.setJob(button.getValue());
				this.getButton(4).setEnabled(this.npc.advanced.jobInterface.getEnumType().hasSettings);
				break;
			}
			case 7: {
				this.save();
				NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNPCLinesMenu(this.npc));
				break;
			}
			case 8: {
				this.hasChanges = true;
				this.npc.advanced.setRole(button.getValue());
				this.getButton(3).setEnabled(this.npc.advanced.roleInterface.getEnumType().hasSettings);
				break;
			}
			case 9: {
				this.save();
				NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNPCFactionSetup(this.npc));
				break;
			}
			case 10: {
				this.save();
				NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNPCDialogNpcOptions(this.npc, this));
				break;
			}
			case 11: {
				this.save();
				NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNPCSoundsMenu(this.npc));
				break;
			}
			case 12: {
				this.save();
				NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNPCNightSetup(this.npc));
				break;
			}
			case 13: {
				this.save();
				NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNPCAdvancedLinkedNpc(this.npc));
				break;
			}
			case 14: {
				this.save();
				NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNPCScenes(this.npc));
				break;
			}
			case 15: {
				this.save();
				NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNPCMarks(this.npc));
				break;
			}
			case 16: { // Animation Settings
				NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNpcAnimation(this, (EntityCustomNpc) this.npc));
				break;
			}
			case 18: { // Animation Settings
				NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNpcEmotion(this, (EntityCustomNpc) this.npc));
				break;
			}
		}
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
			switch (this.npc.advanced.roleInterface.getEnumType()) {
				case TRADER: {
					this.setSubGui(new SubGuiNpcSelectTraider(((RoleTrader) this.npc.advanced.roleInterface).marcet));
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
					NoppesUtil.openGUI((EntityPlayer) this.player, new GuiRoleDialog(this.npc));
					break;
				}
				default: { break; }
			}
		} else if (compound.hasKey("JobData")) {
			if (this.npc.advanced.jobInterface != null) {
				this.npc.advanced.jobInterface.readFromNBT(compound);
			}
			switch (this.npc.advanced.jobInterface.getEnumType()) {
				case BARD: {
					NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNpcBard(this.npc));
					break;
				}
				case HEALER: {
					NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNpcHealer(this.npc));
					break;
				}
				case GUARD: {
					NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNpcGuard(this.npc));
					break;
				}
				case ITEM_GIVER: {
					NoppesUtil.requestOpenGUI(EnumGuiType.SetupItemGiver);
					break;
				}
				case FOLLOWER: {
					NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNpcFollowerJob(this.npc));
					break;
				}
				case SPAWNER: {
					NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNpcSpawner(this.npc));
					break;
				}
				case CONVERSATION: {
					NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNpcConversation(this.npc));
					break;
				}
				case FARMER: {
					NoppesUtil.openGUI((EntityPlayer) this.player, new GuiJobFarmer(this.npc));
					break;
				}
				default: { break; }
			}
		} else {
			this.npc.advanced.readToNBT(compound);
			this.initGui();
		}
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.getButton(5)!=null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("advanced.menu.hover.job."+this.npc.advanced.jobInterface.getType()).getFormattedText());
		}
		else if (this.getButton(7)!=null && this.getButton(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("advanced.menu.hover.says").getFormattedText());
		}
		else if (this.getButton(8)!=null && this.getButton(8).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("advanced.menu.hover.role."+this.npc.advanced.roleInterface.getType()).getFormattedText());
		}
		else if (this.getButton(9)!=null && this.getButton(9).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("advanced.menu.hover.faction").getFormattedText());
		}
		else if (this.getButton(10)!=null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("advanced.menu.hover.dialogs").getFormattedText());
		}
		else if (this.getButton(11)!=null && this.getButton(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("advanced.menu.hover.sounds").getFormattedText());
		}
		else if (this.getButton(12)!=null && this.getButton(12).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("advanced.menu.hover.night").getFormattedText());
		}
		else if (this.getButton(13)!=null && this.getButton(13).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("advanced.menu.hover.lines").getFormattedText());
		}
		else if (this.getButton(14)!=null && this.getButton(14).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("advanced.menu.hover.scenes").getFormattedText());
		}
		else if (this.getButton(15)!=null && this.getButton(15).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("advanced.menu.hover.marks").getFormattedText());
		}
		else if (this.getButton(15)!=null && this.getButton(15).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("advanced.menu.hover.anim").getFormattedText());
		} else if (this.getButton(18)!=null && this.getButton(18).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("animation.hover.eye",
				new TextComponentTranslation("gui.help.general").getFormattedText(),
				new TextComponentTranslation("selectServer.edit").getFormattedText()).
					appendSibling(new TextComponentString("<br>")).
					appendSibling(new TextComponentTranslation("gui.wip"))
					.getFormattedText());
		}
	}

	// New
	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiNpcSelectTraider) {
			((RoleTrader) this.npc.advanced.roleInterface).marcet = (((SubGuiNpcSelectTraider) subgui)).id;
			Client.sendData(EnumPacketServer.MainmenuAdvancedSave, this.npc.advanced.writeToNBT(new NBTTagCompound()));
		}
	}

}
