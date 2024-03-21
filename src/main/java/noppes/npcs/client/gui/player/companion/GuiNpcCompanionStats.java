package noppes.npcs.client.gui.player.companion;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiMenuTopIconButton;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.constants.EnumCompanionJobs;
import noppes.npcs.constants.EnumCompanionTalent;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleCompanion;

public class GuiNpcCompanionStats
extends GuiNPCInterface
implements IGuiData {
	
	public static void addTopMenu(RoleCompanion role, GuiScreen screen, int active) {
		if (screen instanceof GuiNPCInterface) {
			GuiNPCInterface gui = (GuiNPCInterface) screen;
			GuiMenuTopIconButton button;
			gui.addTopButton(button = new GuiMenuTopIconButton(1, gui.guiLeft + 4, gui.guiTop - 27, "menu.stats",
					new ItemStack(Items.BOOK)));
			gui.addTopButton(
					button = new GuiMenuTopIconButton(2, button, "companion.talent", new ItemStack(Items.NETHER_STAR)));
			if (role.hasInv()) {
				gui.addTopButton(
						button = new GuiMenuTopIconButton(3, button, "inv.inventory", new ItemStack(Blocks.CHEST)));
			}
			if (role.job != EnumCompanionJobs.NONE) {
				gui.addTopButton(new GuiMenuTopIconButton(4, button, "job.name", new ItemStack(Items.CARROT)));
			}
			gui.getTopButton(active).active = true;
		}
		if (screen instanceof GuiContainerNPCInterface) {
			GuiContainerNPCInterface gui2 = (GuiContainerNPCInterface) screen;
			GuiMenuTopIconButton button;
			gui2.addTopButton(button = new GuiMenuTopIconButton(1, gui2.guiLeft + 4, gui2.guiTop - 27, "menu.stats",
					new ItemStack(Items.BOOK)));
			gui2.addTopButton(
					button = new GuiMenuTopIconButton(2, button, "companion.talent", new ItemStack(Items.NETHER_STAR)));
			if (role.hasInv()) {
				gui2.addTopButton(
						button = new GuiMenuTopIconButton(3, button, "inv.inventory", new ItemStack(Blocks.CHEST)));
			}
			if (role.job != EnumCompanionJobs.NONE) {
				gui2.addTopButton(new GuiMenuTopIconButton(4, button, "job.name", new ItemStack(Items.CARROT)));
			}
			gui2.getTopButton(active).active = true;
		}
	}

	private boolean isEating;

	private RoleCompanion role;

	public GuiNpcCompanionStats(EntityNPCInterface npc) {
		super(npc);
		this.isEating = false;
		this.role = (RoleCompanion) npc.advanced.roleInterface;
		this.closeOnEsc = true;
		this.setBackground("companion.png");
		this.xSize = 171;
		this.ySize = 166;
		NoppesUtilPlayer.sendData(EnumPlayerPacket.RoleGet, new Object[0]);
	}

	public void actionPerformed(GuiButton guibutton) {
		super.actionPerformed(guibutton);
		int id = guibutton.id;
		if (id == 2) {
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.CompanionTalent);
		}
		if (id == 3) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.CompanionOpenInv, new Object[0]);
		}
	}

	public int drawHealth(int y) {
		this.mc.renderEngine.bindTexture(GuiNpcCompanionStats.ICONS);
		int max = this.role.getTotalArmorValue();
		if (this.role.talents.containsKey(EnumCompanionTalent.ARMOR) || max > 0) {
			for (int i = 0; i < 10; ++i) {
				int x = this.guiLeft + 66 + i * 10;
				if (i * 2 + 1 < max) {
					this.drawTexturedModalRect(x, y, 34, 9, 9, 9);
				}
				if (i * 2 + 1 == max) {
					this.drawTexturedModalRect(x, y, 25, 9, 9, 9);
				}
				if (i * 2 + 1 > max) {
					this.drawTexturedModalRect(x, y, 16, 9, 9, 9);
				}
			}
			y += 10;
		}
		max = MathHelper.ceil(this.npc.getMaxHealth());
		int k = (int) this.npc.getHealth();
		float scale = 1.0f;
		if (max > 40) {
			scale = max / 40.0f;
			k /= scale;
			max = 40;
		}
		for (int j = 0; j < max; ++j) {
			int x2 = this.guiLeft + 66 + j % 20 * 5;
			int offset = j / 20 * 10;
			this.drawTexturedModalRect(x2, y + offset, 52 + j % 2 * 5, 9, (j % 2 == 1) ? 4 : 5, 9);
			if (k > j) {
				this.drawTexturedModalRect(x2, y + offset, 52 + j % 2 * 5, 0, (j % 2 == 1) ? 4 : 5, 9);
			}
		}
		k = this.role.foodstats.getFoodLevel();
		y += 10;
		if (max > 20) {
			y += 10;
		}
		for (int j = 0; j < 20; ++j) {
			int x2 = this.guiLeft + 66 + j % 20 * 5;
			this.drawTexturedModalRect(x2, y, 16 + j % 2 * 5, 27, (j % 2 == 1) ? 4 : 5, 9);
			if (k > j) {
				this.drawTexturedModalRect(x2, y, 52 + j % 2 * 5, 27, (j % 2 == 1) ? 4 : 5, 9);
			}
		}
		return y;
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (this.isEating && !this.role.isEating()) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.RoleGet, new Object[0]);
		}
		this.isEating = this.role.isEating();
		super.drawNpc(34, 150);
		// int y = this.drawHealth(this.guiTop + 88);
	}

	@Override
	public void initGui() {
		super.initGui();
		int y = this.guiTop + 10;
		this.addLabel(new GuiNpcLabel(0, NoppesStringUtils.translate("gui.name", ": ", this.npc.display.getName()),
				this.guiLeft + 4, y));
		int id = 1;
		String translate = NoppesStringUtils.translate("companion.owner", ": ", this.role.ownerName);
		int x = this.guiLeft + 4;
		y += 12;
		this.addLabel(new GuiNpcLabel(id, translate, x, y));
		int id2 = 2;
		String translate2 = NoppesStringUtils.translate("companion.age", ": ", this.role.ticksActive / 18000L + " (",
				this.role.stage.name, ")");
		int x2 = this.guiLeft + 4;
		y += 12;
		this.addLabel(new GuiNpcLabel(id2, translate2, x2, y));
		int id3 = 3;
		String translate3 = NoppesStringUtils.translate("companion.strength", ": ", this.npc.stats.melee.getStrength());
		int x3 = this.guiLeft + 4;
		y += 12;
		this.addLabel(new GuiNpcLabel(id3, translate3, x3, y));
		int id4 = 4;
		String translate4 = NoppesStringUtils.translate("companion.level", ": ", this.role.getTotalLevel());
		int x4 = this.guiLeft + 4;
		y += 12;
		this.addLabel(new GuiNpcLabel(id4, translate4, x4, y));
		int id5 = 5;
		String translate5 = NoppesStringUtils.translate("job.name", ": ", "gui.none");
		int x5 = this.guiLeft + 4;
		y += 12;
		this.addLabel(new GuiNpcLabel(id5, translate5, x5, y));
		addTopMenu(this.role, this, 1);
	}

	@Override
	public void save() {
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.role.readFromNBT(compound);
	}
}
