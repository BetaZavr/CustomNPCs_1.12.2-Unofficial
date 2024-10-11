package noppes.npcs.client.gui.player.companion;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.constants.EnumCompanionTalent;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleCompanion;

import javax.annotation.Nonnull;

public class GuiNpcCompanionTalents extends GuiNPCInterface {

	public static class GuiTalent extends GuiScreen {

		private static final ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/talent.png");
		private final RoleCompanion role;
		private final EnumCompanionTalent talent;
		private final int x;
		private final int y;

		public GuiTalent(RoleCompanion role, EnumCompanionTalent talent, int x, int y) {
			this.talent = talent;
			this.x = x;
			this.y = y;
			this.role = role;
		}

		public void drawScreen(int i, int j, float f) {
			Minecraft mc = Minecraft.getMinecraft();
			mc.getTextureManager().bindTexture(GuiTalent.resource);
			ItemStack item = this.talent.item;
            item.getItem();
            GlStateManager.pushMatrix();
			GlStateManager.color(1.0f, 1.0f, 1.0f);
			GlStateManager.enableBlend();
			boolean hover = this.x < i && this.x + 24 > i && this.y < j && this.y + 24 > j;
			this.drawTexturedModalRect(this.x, this.y, 0, hover ? 24 : 0, 24, 24);
			this.zLevel = 100.0f;
			this.itemRender.zLevel = 100.0f;
			GlStateManager.enableLighting();
			GlStateManager.enableRescaleNormal();
			RenderHelper.enableGUIStandardItemLighting();
			this.itemRender.renderItemAndEffectIntoGUI(item, this.x + 4, this.y + 4);
			this.itemRender.renderItemOverlays(mc.fontRenderer, item, this.x + 4, this.y + 4);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.disableLighting();
			GlStateManager.translate(0.0f, 0.0f, 200.0f);
			this.drawCenteredString(mc.fontRenderer, this.role.getTalentLevel(this.talent) + "", this.x + 20,
					this.y + 16, 16777215);
			this.itemRender.zLevel = 0.0f;
			this.zLevel = 0.0f;
			GlStateManager.popMatrix();
		}

	}

	private long lastPressedTime;
	private final RoleCompanion role;
	private GuiNpcButton selected;
	private long startPressedTime;

	private Map<Integer, GuiTalent> talents;

	public GuiNpcCompanionTalents(EntityNPCInterface npc) {
		super(npc);
		this.talents = new HashMap<>();
		this.lastPressedTime = 0L;
		this.startPressedTime = 0L;
		this.role = (RoleCompanion) npc.advanced.roleInterface;
		this.closeOnEsc = true;
		this.setBackground("companion_empty.png");
		this.xSize = 171;
		this.ySize = 166;
	}

	public void actionPerformed(@Nonnull GuiButton guibutton) {
		super.actionPerformed(guibutton);
		int id = guibutton.id;
		if (id == 1) {
			CustomNpcs.proxy.openGui(this.npc, EnumGuiType.Companion);
		}
		if (id == 3) {
			NoppesUtilPlayer.sendData(EnumPlayerPacket.CompanionOpenInv);
		}
		if (id >= 10) {
			this.selected = (GuiNpcButton) guibutton;
			long getWorldTime = this.mc.world.getWorldTime();
			this.startPressedTime = getWorldTime;
			this.lastPressedTime = getWorldTime;
			this.addExperience(1);
		}
	}

	private void addExperience(int exp) {
		EnumCompanionTalent talent = this.talents.get(this.selected.id - 10).talent;
		if (!this.role.canAddExp(-exp) && this.role.currentExp <= 0) {
			return;
		}
		if (exp > this.role.currentExp) {
			exp = this.role.currentExp;
		}
		NoppesUtilPlayer.sendData(EnumPlayerPacket.CompanionTalentExp, talent.ordinal(), exp);
		this.role.talents.put(talent, this.role.talents.get(talent) + exp);
		this.role.addExp(-exp);
		this.getLabel(this.selected.id - 10)
				.setLabel(this.role.talents.get(talent) + "/" + this.role.getNextLevel(talent));
	}

	private void addTalent(int i, EnumCompanionTalent talent) {
		int y = this.guiTop + 28 + i / 2 * 26;
		int x = this.guiLeft + 4 + i % 2 * 84;
		GuiTalent gui = new GuiTalent(this.role, talent, x, y);
		gui.setWorldAndResolution(this.mc, this.width, this.height);
		this.talents.put(i, gui);
		if (this.role.getTalentLevel(talent) < 5) {
			this.addButton(new GuiNpcButton(i + 10, x + 26, y, 14, 14, "+"));
			y += 8;
		}
		this.addLabel(new GuiNpcLabel(i, this.role.talents.get(talent) + "/" + this.role.getNextLevel(talent), x + 26,
				y + 8));
	}

	@Override
	public void drawScreen(int i, int j, float f) {
		super.drawScreen(i, j, f);
		if (this.selected != null && this.mc.world.getWorldTime() - this.startPressedTime > 4L
				&& this.lastPressedTime < this.mc.world.getWorldTime() && this.mc.world.getWorldTime() % 4L == 0L) {
			if (this.selected.mousePressed(this.mc, i, j) && Mouse.isButtonDown(0)) {
				this.lastPressedTime = this.mc.world.getWorldTime();
				if (this.lastPressedTime - this.startPressedTime < 20L) {
					this.addExperience(1);
				} else if (this.lastPressedTime - this.startPressedTime < 40L) {
					this.addExperience(2);
				} else if (this.lastPressedTime - this.startPressedTime < 60L) {
					this.addExperience(4);
				} else if (this.lastPressedTime - this.startPressedTime < 90L) {
					this.addExperience(8);
				} else if (this.lastPressedTime - this.startPressedTime < 140L) {
					this.addExperience(14);
				} else {
					this.addExperience(28);
				}
			} else {
				this.lastPressedTime = 0L;
				this.selected = null;
			}
		}
		this.mc.getTextureManager().bindTexture(Gui.ICONS);
		this.drawTexturedModalRect(this.guiLeft + 4, this.guiTop + 20, 10, 64, 162, 5);
		if (this.role.currentExp > 0) {
			float v = 1.0f * this.role.currentExp / this.role.getMaxExp();
			if (v > 1.0f) {
				v = 1.0f;
			}
			this.drawTexturedModalRect(this.guiLeft + 4, this.guiTop + 20, 10, 69, (int) (v * 162.0f), 5);
		}
		String s = this.role.currentExp + "\\" + this.role.getMaxExp();
		this.mc.fontRenderer.drawString(s, this.guiLeft + this.xSize / 2 - this.mc.fontRenderer.getStringWidth(s) / 2,
				this.guiTop + 10, CustomNpcResourceListener.DefaultTextColor);
		for (GuiTalent talent : this.talents.values()) {
			talent.drawScreen(i, j, f);
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.talents = new HashMap<>();
		// int y = this.guiTop + 12;
		this.addLabel(
				new GuiNpcLabel(0, NoppesStringUtils.translate("quest.exp", ": "), this.guiLeft + 4, this.guiTop + 10));
		GuiNpcCompanionStats.addTopMenu(this.role, this, 2);
		int i = 0;
		for (EnumCompanionTalent e : this.role.talents.keySet()) {
			this.addTalent(i++, e);
		}
	}

	@Override
	public void save() {
	}
}
