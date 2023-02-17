package noppes.npcs.client.gui.mainmenu;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesStringUtils;
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
import noppes.npcs.client.gui.roles.GuiJobFarmer;
import noppes.npcs.client.gui.roles.GuiNpcBard;
import noppes.npcs.client.gui.roles.GuiNpcCompanion;
import noppes.npcs.client.gui.roles.GuiNpcConversation;
import noppes.npcs.client.gui.roles.GuiNpcFollowerJob;
import noppes.npcs.client.gui.roles.GuiNpcGuard;
import noppes.npcs.client.gui.roles.GuiNpcHealer;
import noppes.npcs.client.gui.roles.GuiNpcPuppet;
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
	protected void actionPerformed(GuiButton guibutton) {
		GuiNpcButton button = (GuiNpcButton) guibutton;
		switch (button.id) {
		case 3: {
			this.save();
			Client.sendData(EnumPacketServer.RoleGet, new Object[0]);
			break;
		}
		case 4: {
			this.save();
			Client.sendData(EnumPacketServer.JobGet, new Object[0]);
			break;
		}
		case 5: {
			this.hasChanges = true;
			this.npc.advanced.setJob(button.getValue());
			this.getButton(4).setEnabled(
					this.npc.advanced.job != 0 && this.npc.advanced.job != 8 && this.npc.advanced.job != 10);
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
			this.getButton(3).setEnabled(this.npc.advanced.role != 0 && this.npc.advanced.role != 5);
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
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int y = this.guiTop + 8;
		this.addButton(new GuiNpcButton(3, this.guiLeft + 85 + 160, y, 52, 20, "selectServer.edit"));
		this.addButton(new GuiButtonBiDirectional(8, this.guiLeft + 85, y, 155, 20,
				new String[] { "role.none", "role.trader", "role.mercenary", "role.bank", "role.transporter",
						"role.mailman", NoppesStringUtils.translate("role.companion", "(WIP)"), "dialog.dialog" },
				this.npc.advanced.role));
		this.getButton(3).setEnabled(this.npc.advanced.role != 0 && this.npc.advanced.role != 5);
		int i = 4;
		int j = this.guiLeft + 85 + 160;
		y += 22;
		this.addButton(new GuiNpcButton(i, j, y, 52, 20, "selectServer.edit"));
		this.addButton(new GuiButtonBiDirectional(5, this.guiLeft + 85, y, 155, 20,
				new String[] { "job.none", "job.bard", "job.healer", "job.guard", "job.itemgiver", "role.follower",
						"job.spawner", "job.conversation", "job.chunkloader", "job.puppet", "job.builder",
						"job.farmer" },
				this.npc.advanced.job));
		this.getButton(4)
				.setEnabled(this.npc.advanced.job != 0 && this.npc.advanced.job != 8 && this.npc.advanced.job != 10);
		int k = 7;
		int l = this.guiLeft + 15;
		y += 22;
		this.addButton(new GuiNpcButton(k, l, y, 190, 20, "advanced.lines"));
		this.addButton(new GuiNpcButton(9, this.guiLeft + 208, y, 190, 20, "menu.factions"));
		int m = 10;
		int j2 = this.guiLeft + 15;
		y += 22;
		this.addButton(new GuiNpcButton(m, j2, y, 190, 20, "dialog.dialogs"));
		this.addButton(new GuiNpcButton(11, this.guiLeft + 208, y, 190, 20, "advanced.sounds"));
		int i2 = 12;
		int j3 = this.guiLeft + 15;
		y += 22;
		this.addButton(new GuiNpcButton(i2, j3, y, 190, 20, "advanced.night"));
		this.addButton(new GuiNpcButton(13, this.guiLeft + 208, y, 190, 20, "global.linked"));
		int i3 = 14;
		int j4 = this.guiLeft + 15;
		y += 22;
		this.addButton(new GuiNpcButton(i3, j4, y, 190, 20, "advanced.scenes"));
		this.addButton(new GuiNpcButton(15, this.guiLeft + 208, y, 190, 20, "advanced.marks"));
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
			if (this.npc.roleInterface != null) {
				this.npc.roleInterface.readFromNBT(compound);
			}
			switch (this.npc.advanced.role) {
				case 1: {
					// NoppesUtil.requestOpenGUI(EnumGuiType.SetupTrader); // Changed
					this.setSubGui(new SubGuiNpcSelectTraider(((RoleTrader) this.npc.roleInterface).marcet));
					break;
				}
				case 2: {
					NoppesUtil.requestOpenGUI(EnumGuiType.SetupFollower);
					break;
				}
				case 3: {
					NoppesUtil.requestOpenGUI(EnumGuiType.SetupBank);
					break;
				}
				case 4: {
					this.displayGuiScreen(new GuiNpcTransporter(this.npc));
					break;
				}
				case 6: {
					this.displayGuiScreen(new GuiNpcCompanion(this.npc));
					break;
				}
				case 7: {
					NoppesUtil.openGUI((EntityPlayer) this.player, new GuiRoleDialog(this.npc));
					break;
				}
			}
		} else if (compound.hasKey("JobData")) {
			if (this.npc.jobInterface != null) {
				this.npc.jobInterface.readFromNBT(compound);
			}
			switch (this.npc.advanced.job) {
				case 1: {
					NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNpcBard(this.npc));
					break;
				}
				case 2: {
					NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNpcHealer(this.npc));
					break;
				}
				case 3: {
					NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNpcGuard(this.npc));
					break;
				}
				case 4: {
					NoppesUtil.requestOpenGUI(EnumGuiType.SetupItemGiver);
					break;
				}
				case 5: {
					NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNpcFollowerJob(this.npc));
					break;
				}
				case 6: {
					NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNpcSpawner(this.npc));
					break;
				}
				case 7: {
					NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNpcConversation(this.npc));
					break;
				}
				case 9: {
					NoppesUtil.openGUI((EntityPlayer) this.player, new GuiNpcPuppet(this, (EntityCustomNpc) this.npc));
					break;
				}
				case 11: {
					NoppesUtil.openGUI((EntityPlayer) this.player, new GuiJobFarmer(this.npc));
					break;
				}
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
			this.setHoverText(new TextComponentTranslation("advanced.menu.hover.job."+this.npc.advanced.job).getFormattedText());
		}
		else if (this.getButton(7)!=null && this.getButton(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("advanced.menu.hover.says").getFormattedText());
		}
		else if (this.getButton(8)!=null && this.getButton(8).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("advanced.menu.hover.role."+this.npc.advanced.role).getFormattedText());
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
	}

	// New
	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiNpcSelectTraider) {
			((RoleTrader) this.npc.roleInterface).marcet = (((SubGuiNpcSelectTraider) subgui)).id;
			Client.sendData(EnumPacketServer.MainmenuAdvancedSave, this.npc.advanced.writeToNBT(new NBTTagCompound()));
		}
	}

}
