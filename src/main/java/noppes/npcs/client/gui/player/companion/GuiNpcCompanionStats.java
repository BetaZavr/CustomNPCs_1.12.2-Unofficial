package noppes.npcs.client.gui.player.companion;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumCompanionJobs;
import noppes.npcs.constants.EnumCompanionTalent;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleCompanion;

import javax.annotation.Nonnull;

public class GuiNpcCompanionStats extends GuiNPCInterface implements IGuiData {

	public static void addTopMenu(RoleCompanion role, GuiScreen screen, int active) {
		if (screen instanceof GuiNPCInterface) {
			GuiNPCInterface gui = (GuiNPCInterface) screen;
			GuiMenuTopIconButton button;
			gui.addTopButton(button = new GuiMenuTopIconButton(1, gui.guiLeft + 4, gui.guiTop - 27, "menu.stats", new ItemStack(Items.BOOK)));
			gui.addTopButton(button = new GuiMenuTopIconButton(2, button, "companion.talent", new ItemStack(Items.NETHER_STAR)));
			if (role.hasInv()) { gui.addTopButton(button = new GuiMenuTopIconButton(3, button, "inv.inventory", new ItemStack(Blocks.CHEST))); }
			if (role.job != EnumCompanionJobs.NONE) { gui.addTopButton(new GuiMenuTopIconButton(4, button, "job.name", new ItemStack(Items.CARROT))); }
			gui.getTopButton(active).setIsActive(true);
		}
		if (screen instanceof GuiContainerNPCInterface) {
			GuiContainerNPCInterface gui2 = (GuiContainerNPCInterface) screen;
			GuiMenuTopIconButton button;
			gui2.addTopButton(button = new GuiMenuTopIconButton(1, gui2.getGuiLeft() + 4, gui2.getGuiTop() - 27, "menu.stats", new ItemStack(Items.BOOK)));
			gui2.addTopButton(button = new GuiMenuTopIconButton(2, button, "companion.talent", new ItemStack(Items.NETHER_STAR)));
			if (role.hasInv()) { gui2.addTopButton(button = new GuiMenuTopIconButton(3, button, "inv.inventory", new ItemStack(Blocks.CHEST))); }
			if (role.job != EnumCompanionJobs.NONE) { gui2.addTopButton(new GuiMenuTopIconButton(4, button, "job.name", new ItemStack(Items.CARROT))); }
			gui2.getTopButton(active).setIsActive(true);
		}
	}

	protected final RoleCompanion role;
	protected boolean isEating;

	public GuiNpcCompanionStats(EntityNPCInterface npc) {
		super(npc);
		setBackground("companion.png");
		closeOnEsc = true;
		xSize = 171;
		ySize = 166;

		isEating = false;
		role = (RoleCompanion) npc.advanced.roleInterface;
		NoppesUtilPlayer.sendData(EnumPlayerPacket.RoleGet);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 2: CustomNpcs.proxy.openGui(npc, EnumGuiType.CompanionTalent); break;
			case 3: NoppesUtilPlayer.sendData(EnumPlayerPacket.CompanionOpenInv); break;
		}
	}

	public void drawHealth(int y) {
		mc.getTextureManager().bindTexture(GuiNpcCompanionStats.ICONS);
		int max = role.getTotalArmorValue();
		if (role.talents.containsKey(EnumCompanionTalent.ARMOR) || max > 0) {
			for (int i = 0; i < 10; ++i) {
				int x = guiLeft + 66 + i * 10;
				if (i * 2 + 1 < max) { drawTexturedModalRect(x, y, 34, 9, 9, 9); }
				if (i * 2 + 1 == max) { drawTexturedModalRect(x, y, 25, 9, 9, 9); }
				if (i * 2 + 1 > max) { drawTexturedModalRect(x, y, 16, 9, 9, 9); }
			}
			y += 10;
		}
		max = MathHelper.ceil(npc.getMaxHealth());
		int k = (int) npc.getHealth();
		float scale;
		if (max > 40) {
			scale = max / 40.0f;
			k /= (int) scale;
			max = 40;
		}
		for (int j = 0; j < max; ++j) {
			int x2 = guiLeft + 66 + j % 20 * 5;
			int offset = j / 20 * 10;
			drawTexturedModalRect(x2, y + offset, 52 + j % 2 * 5, 9, (j % 2 == 1) ? 4 : 5, 9);
			if (k > j) { drawTexturedModalRect(x2, y + offset, 52 + j % 2 * 5, 0, (j % 2 == 1) ? 4 : 5, 9); }
		}
		k = role.foodstats.getFoodLevel();
		y += 10;
		if (max > 20) { y += 10; }
		for (int j = 0; j < 20; ++j) {
			int x2 = guiLeft + 66 + j % 20 * 5;
			drawTexturedModalRect(x2, y, 16 + j % 2 * 5, 27, (j % 2 == 1) ? 4 : 5, 9);
			if (k > j) { drawTexturedModalRect(x2, y, 52 + j % 2 * 5, 27, (j % 2 == 1) ? 4 : 5, 9); }
		}
	}

	@Override
	public void drawScreen(int mouseXIn, int mouseYIn, float partialTicks) {
		super.drawScreen(mouseXIn, mouseYIn, partialTicks);
		if (isEating && !role.isEating()) { NoppesUtilPlayer.sendData(EnumPlayerPacket.RoleGet); }
		isEating = role.isEating();
		super.drawNpc(34, 150);
		drawHealth(guiTop + 88);
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = guiLeft + 4;
		int y = guiTop + 10;
		addLabel(new GuiNpcLabel(0, NoppesStringUtils.translate("gui.name", ": ", npc.display.getName()), x, y));
		addLabel(new GuiNpcLabel(1, NoppesStringUtils.translate("companion.owner", ": ", role.ownerName), x, y += 12));
		addLabel(new GuiNpcLabel(2, NoppesStringUtils.translate("companion.age", ": ", role.ticksActive / 18000L + " (", role.stage.name, ")"), x, y += 12));
		addLabel(new GuiNpcLabel(3, NoppesStringUtils.translate("companion.strength", ": ", npc.stats.melee.getStrength()), x, y += 12));
		addLabel(new GuiNpcLabel(4, NoppesStringUtils.translate("companion.level", ": ", role.getTotalLevel()), x, y += 12));
		addLabel(new GuiNpcLabel(5, NoppesStringUtils.translate("job.name", ": ", "gui.none"), x, y + 12));
		addTopMenu(role, this, 1);
	}

    @Override
	public void setGuiData(NBTTagCompound compound) { role.load(compound); }

}
