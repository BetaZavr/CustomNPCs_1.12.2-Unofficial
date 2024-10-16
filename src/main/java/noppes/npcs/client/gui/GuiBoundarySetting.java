package noppes.npcs.client.gui;

import java.awt.Point;
import java.util.Arrays;
import java.util.TreeMap;

import org.lwjgl.opengl.GL11;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.IPos;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.ISubGuiListener;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.BorderController;
import noppes.npcs.controllers.data.Zone3D;

public class GuiBoundarySetting extends GuiNPCInterface
		implements ICustomScrollListener, ITextfieldListener, ISubGuiListener {

	private final int regID;
	private final TreeMap<Integer, String> dataRegions = Maps.newTreeMap();
	private final TreeMap<Integer, String> dataPoints = Maps.newTreeMap();
	private GuiCustomScroll regions, points;
	private Point point;
	private Zone3D region;

	public GuiBoundarySetting(int idReg, int idPoint) {
		this.setBackground("bgfilled.png");
		this.xSize = 405;
		this.ySize = 216;
		this.closeOnEsc = true;
		this.regID = idReg;
		this.region = (Zone3D) BorderController.getInstance().getRegion(this.regID);
		if (this.region != null && this.region.points.containsKey(idPoint)) {
			this.point = this.region.points.get(idPoint);
		}
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
		case 0: { // color
			if (this.region == null) {
				return;
			}
			this.setSubGui(new SubGuiColorSelector(this.region.color));
			return;
		}
		case 1: { // availability
			if (this.region == null) {
				return;
			}
			this.setSubGui(new SubGuiNpcAvailability(this.region.availability));
			return;
		}
		case 2: { // del
			if (this.region == null) {
				return;
			}
			Client.sendData(EnumPacketServer.RegionData, 1, this.region.getId());
			this.region = null;
			this.point = null;
			break;
		}
		case 3: { // -X
			this.region.offset(-1, 0, 0);
			break;
		}
		case 4: { // +X
			this.region.offset(1, 0, 0);
			break;
		}
		case 5: { // -Z
			this.region.offset(0, 0, -1);
			break;
		}
		case 6: { // +Z
			this.region.offset(0, 0, 1);
			break;
		}
		case 7: { // -Y
			this.region.offset(0, -1, 0);
			break;
		}
		case 8: { // +Y
			this.region.offset(0, 1, 0);
			break;
		}
		case 10: { // Up Point Pos
			if (this.region == null || this.point == null) {
				return;
			}
			TreeMap<Integer, Point> map = Maps.newTreeMap();
			int i = 0;
			for (int pos : this.region.points.keySet()) {
				Point p = this.region.points.get(pos);
				if (p == this.point || (p.x == this.point.x && p.y == this.point.y)) {
					i = pos;
					break;
				}
			}
			int j = 0;
			for (int pos : this.region.points.keySet()) {
				if (pos == i) {
					continue;
				}
				if (pos + 1 == i) {
					map.put(j++, this.point);
				}
				Point p = this.region.points.get(pos);
				map.put(j++, p);
			}
			this.region.points = map;
			break;
		}
		case 11: { // Down Point Pos
			if (this.region == null || this.point == null) {
				return;
			}
			TreeMap<Integer, Point> map = Maps.newTreeMap();
			int i = 0;
			for (int pos : this.region.points.keySet()) {
				Point p = this.region.points.get(pos);
				if (p == this.point || (p.x == this.point.x && p.y == this.point.y)) {
					i = pos;
					break;
				}
			}
			int j = 0;
			for (int pos : this.region.points.keySet()) {
				if (pos == i) {
					continue;
				}
				Point p = this.region.points.get(pos);
				map.put(j++, p);
				if (pos - 1 == i) {
					map.put(j++, this.point);
				}
			}
			this.region.points = map;
			break;
		}
		case 12: { // OffSet Point -X
			if (this.point == null) {
				return;
			}
			this.point.x--;
			break;
		}
		case 13: { // OffSet Point +X
			if (this.point == null) {
				return;
			}
			this.point.x++;
			break;
		}
		case 14: { // OffSet Point -Z
			if (this.point == null) {
				return;
			}
			this.point.y--;
			break;
		}
		case 15: { // OffSet Point +Z
			if (this.point == null) {
				return;
			}
			this.point.y++;
			break;
		}
		case 18: { // Max Y Up +
			if (this.region == null) {
				return;
			}
			this.region.y[1]++;
			break;
		}
		case 19: { // Max Y Up -
			if (this.region == null) {
				return;
			}
			this.region.y[1]--;
			break;
		}
		case 20: { // Max Y Down +
			if (this.region == null) {
				return;
			}
			this.region.y[0]++;
			break;
		}
		case 21: { // Max Y Down -
			if (this.region == null) {
				return;
			}
			this.region.y[0]--;
			break;
		}
		case 24: { // Teleport to Center
			if (this.region == null) {
				return;
			}
			if (this.point != null) {
				Client.sendData(EnumPacketServer.TeleportTo, this.region.dimensionID, this.point.x,
                        this.region.y[0] + (this.region.y[1] - this.region.y[0]) / 2, this.point.y);
				return;
			}
			IPos pos = this.region.getCenter();
			Client.sendData(EnumPacketServer.TeleportTo, this.region.dimensionID, pos.getX(), pos.getY(), pos.getZ());
			return;
		}
		case 25: { // Keep Out Type
			if (this.region == null) {
				return;
			}
			this.region.keepOut = ((GuiNpcCheckBox) button).isSelected();
			break;
		}
		case 26: { // Show In Client
			if (this.region == null) {
				return;
			}
			this.region.showInClient = ((GuiNpcCheckBox) button).isSelected();
			break;
		}
		}
		this.initGui();
	}

	public static void drawLine(double left, double top, double right, double bottom, int color, float wLine) {
		float f3 = (float) (color >> 24 & 255) / 255.0F;
		float f = (float) (color >> 16 & 255) / 255.0F;
		float f1 = (float) (color >> 8 & 255) / 255.0F;
		float f2 = (float) (color & 255) / 255.0F;
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder buffer = tessellator.getBuffer();
		GlStateManager.enableBlend();
		GlStateManager.disableTexture2D();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
				GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		GlStateManager.glLineWidth(wLine);
		GlStateManager.color(f, f1, f2, f3);
		buffer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);
		buffer.pos(left, top, 0.0D).endVertex();
		buffer.pos(right, bottom, 0.0D).endVertex();
		tessellator.draw();
		GlStateManager.enableTexture2D();
		GlStateManager.disableBlend();
	}

	private void drawRegion(int work, double mu, double mv, double su, double sv, double sy) {
		if (this.region == null) {
			return;
		}
		double u0, u1, v0, v1;
		for (int i = -1; i < this.region.points.size() - 1; i++) {
			Point p0;
			if (i == -1) {
				p0 = this.region.points.get(this.region.points.size() - 1);
			} else {
				p0 = this.region.points.get(i);
			}
			Point p1 = this.region.points.get(i + 1);
			if (p0 == null || p1 == null) {
				continue;
			}
			u0 = (p0.x - mu) * su;
			v0 = (p0.y - mv) * sv;
			u1 = (p1.x - mu) * su;
			v1 = (p1.y - mv) * sv;
			GuiBoundarySetting.drawLine(u0, v0, u1, v1, -16776961, 2.0f);
		}
		v0 = (Math.min(this.region.y[1], 255)) * sy;
		v1 = (Math.max(this.region.y[0], 0)) * sy;
		drawLine(work + 12, v0, work + 13, v1, -16776961, 2.0f);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (this.background != null) {
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.pushMatrix();
			GlStateManager.translate(this.guiLeft, this.guiTop, 1.0f);
			GlStateManager.scale(this.bgScale, this.bgScale, this.bgScale);
			this.mc.getTextureManager().bindTexture(this.background);
			if (this.xSize > 256) {
				this.drawTexturedModalRect(0, this.ySize - 1, 0, 252, 250, 4);
				this.drawTexturedModalRect(250, this.ySize - 1, 256 - (this.xSize - 250), 252, this.xSize - 250, 4);
			}
			GlStateManager.popMatrix();
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.subgui != null) {
			return;
		}

		int side = 160;
		int work = side - 12;
		int wu = this.guiLeft + 131, wv = this.guiTop + 18;

		String ht = "";
		if (isMouseHover(mouseX, mouseY, wu, wv, side - 2, side)) {
			ht = "region.hover.work.0";
		} else if (isMouseHover(mouseX, mouseY, wu + side - 2, wv, 12, side)) {
			ht = "region.hover.work.1";
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(wu, wv, 0.5f);
		Gui.drawRect(0, 0, side + 12, side, 0xA0000000); // Main Place
		Gui.drawRect(6, 6, side - 6, side - 6, 0xA0000000); // Work Place
		Gui.drawRect(side - 2, 6, side + 9, side - 6, 0xA0000000); // Height place
		GlStateManager.translate(6, 6, 0.0d);
		if (this.region == null) {
			GlStateManager.popMatrix();
			return;
		}
		double mu = this.region.getMinX();
		double mv = this.region.getMinZ();
		double nu = this.region.getMaxX();
		double nv = this.region.getMaxZ();
		double su = (double) (work) / (nu - mu);
		double sv = (double) (work) / (nv - mv);
		double sy = (double) (work) / 255.0d;
		// Selected InSide Blue
		if (this.region.size() >= 3) {
			this.drawRegion(work, mu, mv, su, sv, sy);
		}

		int hx, hz;
		Point p = null;
		if (this.region.dimensionID == this.mc.player.world.provider.getDimension()) {
			p = this.region.points.get(this.region.getIdNearestPoint(this.mc.player.getPosition()));
		}
		// Nearest Point Green
		if (p != null) {
			hx = (int) (((double) p.x - mu) * su);
			hz = (int) (((double) p.y - mv) * sv);
			int d = ((this.point != null && this.point.x == p.x && this.point.y == p.y) ? 1 : 0);
			Gui.drawRect(hx - d - 1, hz - d - 1, hx + d + 1, hz + d + 1, 0xFF00FF00);
		}

		// Current Point
		if (this.point != null) {
			hx = (int) ((this.point.x - mu) * su);
			hz = (int) ((this.point.y - mv) * sv);
			Gui.drawRect(hx - 1, hz - 1, hx + 1, hz + 1, 0xFFFFFF00);
		}
		if (this.region.homePos != null) {
			hx = (int) ((this.region.homePos.getX() - mu) * su);
			hz = (int) ((this.region.homePos.getZ() - mv) * sv);
			if (hx < 0) {
				hx = 0;
			} else if (hx > work) {
				hx = work;
			}
			if (hz < 0) {
				hz = 0;
			} else if (hz > work) {
				hz = work;
			}
			Gui.drawRect(hx - 1, hz - 1, hx, hz, 0xFFFF0000);
		}
		// Center
		IPos c = this.region.getCenter();
		hx = (int) ((c.getX() - mu) * su);
		hz = (int) ((c.getZ() - mv) * sv);
		Gui.drawRect(hx - 1, hz - 1, hx + 1, hz + 1, 0xFF0000FF);

		// Player Position
		int color = 0xFFFFFFFF;
		int xp = this.player.getPosition().getX();
		if (xp < mu) {
			xp = (int) mu;
			color = 0xA0FFFFFF;
		} else if (xp > nu) {
			xp = (int) nu;
			color = 0xA0FFFFFF;
		}
		hx = (int) ((xp - mu) * su);
		if (hx < 0) {
			hx = 0;
			color = 0xF0FFFFFF;
		} else if (hx > work) {
			hx = work;
			color = 0xF0FFFFFF;
		}
		int zp = this.player.getPosition().getZ();
		if (zp < mv) {
			zp = (int) mv;
			color = 0xF0FFFFFF;
		} else if (zp > nv) {
			zp = (int) nv;
			color = 0xF0FFFFFF;
		}
		hz = (int) ((zp - mv) * sv);
		int d = (this.point != null && this.point.x == this.player.getPosition().getX()
				&& this.point.y == this.player.getPosition().getZ() ? 1 : 0) + 1;
		if (p != null && p.x == this.player.getPosition().getX() && p.y == this.player.getPosition().getZ()) {
			d++;
		}
		if (c.getX() == this.player.getPosition().getX() && c.getZ() == this.player.getPosition().getZ()) {
			d++;
		}
		Gui.drawRect(hx - d - 1, hz - d - 1, hx + d + 1, hz + d + 1, color); // XZ
		int hy = (int) (this.player.getPosition().getY() * sy);
		Gui.drawRect(side - 6, side - hy - 13, side - 4, side - hy - 11, 0xFFFFFFFF); // Y
		GlStateManager.popMatrix();
		// New
		if (this.subgui != null || !CustomNpcs.ShowDescriptions) {
			return;
		}
		// hover text
		if (!ht.isEmpty()) {
			this.setHoverText(new TextComponentTranslation(ht).getFormattedText());
		} else if (this.getTextField(16) != null && this.getTextField(16).isMouseOver()) {
			this.setHoverText("X pos");
		} else if (this.getTextField(17) != null && this.getTextField(17).isMouseOver()) {
			this.setHoverText("Z pos");
		} else if (this.getTextField(22) != null && this.getTextField(22).isMouseOver()) {
			this.setHoverText("max Y");
		} else if (this.getTextField(23) != null && this.getTextField(23).isMouseOver()) {
			this.setHoverText("min Y");
		} else if (this.getTextField(24) != null && this.getTextField(24).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("region.hover.name").getFormattedText());
		} else if (this.getTextField(25) != null && this.getTextField(25).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("region.hover.home.axis", "X").getFormattedText());
		} else if (this.getTextField(26) != null && this.getTextField(26).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("region.hover.home.axis", "Y").getFormattedText());
		} else if (this.getTextField(27) != null && this.getTextField(27).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("region.hover.home.axis", "Z").getFormattedText());
		} else if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("region.hover.color").getFormattedText());
		} else if (this.getButton(1) != null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("availability.hover").getFormattedText());
		} else if (this.getButton(2) != null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.delete").getFormattedText());
		} else if (this.getButton(3) != null && this.getButton(3).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("region.hover.offSet.-x",
					new TextComponentTranslation("gui.region").getFormattedText()).getFormattedText());
		} else if (this.getButton(4) != null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("region.hover.offSet.+x",
					new TextComponentTranslation("gui.region").getFormattedText()).getFormattedText());
		} else if (this.getButton(5) != null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("region.hover.offSet.-z",
					new TextComponentTranslation("gui.region").getFormattedText()).getFormattedText());
		} else if (this.getButton(6) != null && this.getButton(6).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("region.hover.offSet.+z",
					new TextComponentTranslation("gui.region").getFormattedText()).getFormattedText());
		} else if (this.getButton(7) != null && this.getButton(7).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("region.hover.offSet.-y",
					new TextComponentTranslation("gui.region").getFormattedText()).getFormattedText());
		} else if (this.getButton(8) != null && this.getButton(8).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("region.hover.offSet.+y",
					new TextComponentTranslation("gui.region").getFormattedText()).getFormattedText());
		} else if (this.getButton(10) != null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("region.hover.point.offSet.up",
					new TextComponentTranslation("gui.region").getFormattedText()).getFormattedText());
		} else if (this.getButton(11) != null && this.getButton(11).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("region.hover.point.offSet.down",
					new TextComponentTranslation("gui.region").getFormattedText()).getFormattedText());
		} else if (this.getButton(12) != null && this.getButton(12).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("region.hover.offSet.-x",
					new TextComponentTranslation("gui.point").getFormattedText()).getFormattedText());
		} else if (this.getButton(13) != null && this.getButton(13).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("region.hover.offSet.+x",
					new TextComponentTranslation("gui.point").getFormattedText()).getFormattedText());
		} else if (this.getButton(14) != null && this.getButton(14).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("region.hover.offSet.-z",
					new TextComponentTranslation("gui.point").getFormattedText()).getFormattedText());
		} else if (this.getButton(15) != null && this.getButton(15).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("region.hover.offSet.+z",
					new TextComponentTranslation("gui.point").getFormattedText()).getFormattedText());
		} else if (this.getButton(18) != null && this.getButton(18).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("region.hover.offSet.up.-y",
					new TextComponentTranslation("gui.point").getFormattedText()).getFormattedText());
		} else if (this.getButton(19) != null && this.getButton(19).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("region.hover.offSet.up.+y",
					new TextComponentTranslation("gui.point").getFormattedText()).getFormattedText());
		} else if (this.getButton(20) != null && this.getButton(20).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("region.hover.offSet.down.-y",
					new TextComponentTranslation("gui.point").getFormattedText()).getFormattedText());
		} else if (this.getButton(21) != null && this.getButton(21).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("region.hover.offSet.down.+y",
					new TextComponentTranslation("gui.point").getFormattedText()).getFormattedText());
		} else if (this.getButton(24) != null && this.getButton(24).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.teleport",
					new TextComponentTranslation("gui.point").getFormattedText()).getFormattedText());
		} else if (this.getButton(25) != null && this.getButton(25).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("region.hover.keepout").getFormattedText());
		} else if (this.getButton(25) != null && this.getButton(25).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("region.hover.show.in.client").getFormattedText());
		} else if (this.getLabel(103) != null && this.getLabel(103).hovered) {
			this.setHoverText(new TextComponentTranslation("region.hover.regions.list",
					new TextComponentTranslation("item.npcboundary.name").getFormattedText()).getFormattedText());
		} else if (this.getLabel(104) != null && this.getLabel(104).hovered) {
			this.setHoverText(new TextComponentTranslation("region.hover.points.list",
					new TextComponentTranslation("item.npcboundary.name").getFormattedText()).getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.dataRegions.clear();
		this.dataPoints.clear();
		String selectReg = "", selectP = "";
		int side = 186, r0 = this.guiLeft + 118, r1 = this.guiLeft + side + 119, h0 = this.guiTop + 109;
		BorderController bData = BorderController.getInstance();
		if (this.region != null && !bData.regions.containsKey(this.region.getId())) {
			this.region = null;
			this.point = null;
		}
		if (this.region != null && this.point != null && !this.region.contains(this.point.x, this.point.y)) {
			this.point = null;
			if (!this.region.points.isEmpty()) {
				this.point = this.region.points.get(0);
			}
		}
		for (Zone3D reg : bData.regions.values()) {
			this.dataRegions.put(reg.getId(), reg.toString());
			if (this.regID == reg.getId()) {
				this.region = reg;
				selectReg = this.dataRegions.get(reg.getId());
				for (int id : reg.points.keySet()) {
					this.dataPoints.put(id,
							"ID: " + id + " [" + reg.points.get(id).x + ", " + reg.points.get(id).y + "]");
					if (this.point != null && (this.point == reg.points.get(id)
							|| (this.point.x == reg.points.get(id).x && this.point.y == reg.points.get(id).y))) {
						selectP = this.dataPoints.get(id);
					}
				}
			}
		}
		if (this.regions == null) {
			(this.regions = new GuiCustomScroll(this, 0)).setSize(110, 130);
		}
		this.regions.setListNotSorted(Lists.newArrayList(this.dataRegions.values()));
		this.regions.guiLeft = this.guiLeft + 5;
		this.regions.guiTop = this.guiTop + 14;
		if (!selectReg.isEmpty()) {
			this.regions.setSelected(selectReg);
		}
		this.addScroll(this.regions);

		this.addLabel(new GuiNpcLabel(103, "gui.regions", this.guiLeft + 5, this.guiTop + 4, 0xFF202020));

		if (this.points == null) {
			(this.points = new GuiCustomScroll(this, 1)).setSize(this.xSize - side - 124, side / 2);
		}
		this.points.setListNotSorted(Lists.newArrayList(this.dataPoints.values()));
		this.points.guiLeft = r1;
		this.points.guiTop = this.guiTop + 14;
		if (!selectP.isEmpty()) {
			this.points.setSelected(selectP);
		}
		this.addScroll(this.points);

		this.addLabel(new GuiNpcLabel(104, "gui.points", r1, this.guiTop + 4, 0xFF202020));

		// ID 0 - color
		String color = "gui.color";
		if (this.region != null) {
			StringBuilder c = new StringBuilder(Integer.toHexString(this.region.color));
			while (c.length() < 6) { c.insert(0, "0"); }
			color = c.toString();
		}
		GuiNpcButton button = new GuiNpcButton(0, this.guiLeft + 5, this.guiTop + 162, 60, 13, color);
		button.enabled = this.region != null;
		if (this.region != null) {
			button.setTextColor(this.region.color);
		}
		this.addButton(button);
		// ID 1 - Available
		button = new GuiNpcButton(1, this.guiLeft + 5, this.guiTop + 147, 110, 13, "availability.available");
		button.enabled = this.region != null;
		this.addButton(button);
		button = new GuiNpcButton(2, this.guiLeft + 67, this.guiTop + 162, 48, 13, "gui.remove");
		button.enabled = this.region != null;
		this.addButton(button);
		// ID 3 - OffSet -X
		button = new GuiNpcButton(3, r0 + 13, this.guiTop + 3, 13, 13, new String(Character.toChars(0x25C4)));
		button.enabled = this.region != null;
		this.addButton(button);
		// ID 4 - OffSet +X
		button = new GuiNpcButton(4, r0 + 27, this.guiTop + 3, 13, 13, new String(Character.toChars(0x25BA)));
		button.enabled = this.region != null;
		this.addButton(button);
		// ID 5 - OffSet -Z
		button = new GuiNpcButton(5, r0 - 1, this.guiTop + 18, 13, 13, new String(Character.toChars(0x25B2)));
		button.enabled = this.region != null;
		this.addButton(button);
		// ID 6 - OffSet +Z
		button = new GuiNpcButton(6, r0 - 1, this.guiTop + 32, 13, 13, new String(Character.toChars(0x25BC)));
		button.enabled = this.region != null;
		this.addButton(button);
		// ID 7 - OffSet -Y
		button = new GuiNpcButton(7, r0 - 1, this.guiTop + side - 21, 13, 13, new String(Character.toChars(0x25BC)));
		button.enabled = this.region != null;
		this.addButton(button);
		// ID 8 - OffSet +Y
		button = new GuiNpcButton(8, r0 - 1, this.guiTop + side - 35, 13, 13, new String(Character.toChars(0x25B2)));
		button.enabled = this.region != null;
		this.addButton(button);
		// ID 10 - Up Point Pos
		button = new GuiNpcButton(10, r1, h0, 39, 13, new String(Character.toChars(0x02C4)));
		button.enabled = this.region != null && this.point != null;
		this.addButton(button);
		// ID 11 - Down Point Pos
		button = new GuiNpcButton(11, r1 + 42, h0, 39, 13, new String(Character.toChars(0x02C5)));
		button.enabled = this.region != null && this.point != null;
		this.addButton(button);
		// ID 12 - OffSet Point -X
		button = new GuiNpcButton(12, r1, h0 + 27, 13, 13, new String(Character.toChars(0x25C4)));
		button.enabled = this.region != null && this.point != null;
		this.addButton(button);
		// ID 13 - OffSet Point +X
		button = new GuiNpcButton(13, r1 + 24, h0 + 27, 13, 13, new String(Character.toChars(0x25BA)));
		button.enabled = this.region != null && this.point != null;
		this.addButton(button);
		// ID 14 - OffSet Point -Z
		button = new GuiNpcButton(14, r1 + 12, h0 + 14, 13, 13, new String(Character.toChars(0x25B2)));
		button.enabled = this.region != null && this.point != null;
		this.addButton(button);
		// ID 15 - OffSet Point +Z
		button = new GuiNpcButton(15, r1 + 12, h0 + 40, 13, 13, new String(Character.toChars(0x25BC)));
		button.enabled = this.region != null && this.point != null;
		this.addButton(button);
		// ID 18 - Max Y Up -
		button = new GuiNpcButton(18, r1, h0 + 55, 13, 13, new String(Character.toChars(0x25B2)));
		button.enabled = this.region != null && this.point != null;
		this.addButton(button);
		// ID 19 - Max Y Up +
		button = new GuiNpcButton(19, r1, h0 + 72, 13, 13, new String(Character.toChars(0x25BC)));
		button.enabled = this.region != null && this.point != null;
		this.addButton(button);
		// ID 20 - Max Y Down -
		button = new GuiNpcButton(20, r1 + 47, h0 + 55, 13, 13, new String(Character.toChars(0x25B2)));
		button.enabled = this.region != null && this.point != null;
		this.addButton(button);
		// ID 21 - Max Y Down +
		button = new GuiNpcButton(21, r1 + 47, h0 + 72, 13, 13, new String(Character.toChars(0x25BC)));
		button.enabled = this.region != null && this.point != null;
		this.addButton(button);
		// ID 24 - Teleport to Center
		button = new GuiNpcButton(24, r1 + 74, h0 + 23, 20, 20, "TP");
		button.enabled = this.region != null && this.point != null;
		this.addButton(button);
		// ID 25 - Keep Out Type
		GuiNpcCheckBox checkBox = new GuiNpcCheckBox(25, this.guiLeft + 5, this.guiTop + side + 9, 110, 12,
				"region.keepout." + (this.region != null ? this.region.keepOut : "false"));
		checkBox.setSelected(this.region != null && this.region.keepOut);
		this.addButton(checkBox);
		// ID 26 - Keep Out Type
		checkBox = new GuiNpcCheckBox(26, this.guiLeft + 275, this.guiTop + side + 9, 110, 12,
				"region.show.in.client." + (this.region != null ? this.region.keepOut : "false"));
		checkBox.setSelected(this.region != null && this.region.showInClient);
		this.addButton(checkBox);
		// TextFields
		// X Point pos
		GuiNpcTextField textField = new GuiNpcTextField(16, this, r1 + 39, h0 + 17, 31, 15,
				"" + (this.point != null ? this.point.x : 0));
		textField.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, this.point != null ? this.point.x : 0);
		textField.enabled = this.region != null && this.point != null;
		this.addTextField(textField);
		// ID 17 - Z Point pos
		textField = new GuiNpcTextField(17, this, r1 + 39, h0 + 33, 31, 15,
				"" + (this.point != null ? this.point.y : 0));
		textField.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, this.point != null ? this.point.y : 0);
		textField.enabled = this.region != null && this.point != null;
		textField.setText("" + (this.point != null ? this.point.y : 0));
		this.addTextField(textField);
		// ID 22 - Max Y
		textField = new GuiNpcTextField(22, this, r1 + 14, h0 + 63, 31, 15,
				"" + (this.region != null ? this.region.y[1] : 0));
		textField.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, this.region != null ? this.region.y[1] : 0);
		textField.enabled = this.region != null && this.point != null;
		this.addTextField(textField);
		// ID 23 - Min Y
		textField = new GuiNpcTextField(23, this, r1 + 61, h0 + 63, 31, 15,
				"" + (this.region != null ? this.region.y[0] : 0));
		textField.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, this.region != null ? this.region.y[0] : 0);
		textField.enabled = this.region != null && this.point != null;
		this.addTextField(textField);
		// ID 24 - Name
		textField = new GuiNpcTextField(24, this, this.guiLeft + 5, this.guiTop + 178, 110, 15,
                this.region != null ? this.region.name : "");
		textField.enabled = this.region != null && this.point != null;
		this.addTextField(textField);
		// ID 25 - Home X
		textField = new GuiNpcTextField(25, this, r0 + 39, this.guiTop + side + 11, 35, 13,
				"" + (this.region != null ? this.region.homePos.getX() : ""));
		textField.enabled = this.region != null && this.point != null;
		this.addTextField(textField);
		// ID 26 - Home Y
		textField = new GuiNpcTextField(26, this, r0 + 77, this.guiTop + side + 11, 35, 13,
				"" + (this.region != null ? this.region.homePos.getY() : ""));
		textField.enabled = this.region != null && this.point != null;
		this.addTextField(textField);
		// ID 27 - Home Z
		textField = new GuiNpcTextField(27, this, r0 + 115, this.guiTop + side + 11, 35, 13,
				"" + (this.region != null ? this.region.homePos.getZ() : ""));
		textField.enabled = this.region != null && this.point != null;
		this.addTextField(textField);
		// Labels
		// ID 99 - Home Pos
		this.addLabel(new GuiNpcLabel(99, "Home POS:", r0, this.guiTop + side + 13, 0xFF202020));
		// ID 100 - Min XZ Pos
		this.addLabel(new GuiNpcLabel(100,
				"MinXZ: [" + (this.region == null ? "0, 0" : this.region.getMinX() + "," + this.region.getMinZ()) + "]",
				r0 + 47, this.guiTop + 6, 0xFF202020));
		// ID 101 - Max XZ Pos
		this.addLabel(new GuiNpcLabel(101,
				"MaxXZ: [" + (this.region == null ? "0, 0" : this.region.getMaxX() + "," + this.region.getMaxZ()) + "]",
				r0, this.guiTop + side - 3, 0xFF202020));
		// ID 102 - Min/Max Y Pos
		String text = "Min/Max Y: [" + (this.region == null ? "0, 0" : this.region.y[0] + "," + this.region.y[1]) + "]";
		this.addLabel(new GuiNpcLabel(102, text, r0 + side - this.mc.fontRenderer.getStringWidth(text) - 1,
				this.guiTop + side - 3, 0xFF202020));
		text = "(worldID: " + (this.region == null ? "N/A" : this.region.dimensionID) + ")";
		this.addLabel(new GuiNpcLabel(105, text, r0 + side - this.mc.fontRenderer.getStringWidth(text) - 5,
				this.guiTop + 4, 0xFF202020));
	}

	@Override
	public void save() {
		if (this.region != null) {
			NBTTagCompound regionNbt = new NBTTagCompound();
			this.region.writeToNBT(regionNbt);
			Client.sendData(EnumPacketServer.RegionData, 2, regionNbt);
		}
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		switch (scroll.id) {
		case 0: { // Region List
			if (!this.dataRegions.containsValue(scroll.getSelected())) {
				return;
			}
			BorderController bData = BorderController.getInstance();
			for (int id : this.dataRegions.keySet()) {
				if (this.region != null && this.region.getId() == id) {
					continue;
				}
				if (this.dataRegions.get(id).equals(scroll.getSelected()) && bData.regions.containsKey(id)) {
					this.region = (Zone3D) bData.getRegion(id);
					this.point = null;
					if (!this.region.points.isEmpty()) {
						this.point = this.region.points.get(0);
					}
					Client.sendData(EnumPacketServer.RegionData, 0, id);
					this.initGui();
					break;
				}
			}
			break;
		}
		case 1: { // Point List
			if (this.region == null || !this.dataPoints.containsValue(scroll.getSelected())) {
				return;
			}
			for (int id : this.dataPoints.keySet()) {
				if (this.dataPoints.get(id).equals(scroll.getSelected()) && this.region.points.containsKey(id)) {
					this.point = this.region.points.get(id);
					this.initGui();
					break;
				}
			}
			break;
		}
		}
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		switch (scroll.id) {
		case 0: { // Region List
			if (this.region == null) {
				return;
			}
			IPos pos = this.region.getCenter();
			Client.sendData(EnumPacketServer.TeleportTo, this.region.dimensionID, pos.getX(), pos.getY(), pos.getZ());
			Client.sendData(EnumPacketServer.RegionData, 0, this.region.getId());
			this.close();
			break;
		}
		case 1: { // Point List
			if (this.region == null || this.point == null) {
				return;
			}
			Client.sendData(EnumPacketServer.TeleportTo, this.region.dimensionID, this.point.x,
                    this.region.y[0] + (this.region.y[1] - this.region.y[0]) / 2, this.point.y);
			Client.sendData(EnumPacketServer.RegionData, 0, this.region.getId());
			this.close();
			break;
		}
		}
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiColorSelector && this.region != null) {
			this.region.color = ((SubGuiColorSelector) subgui).color;
			this.initGui();
		}

	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (textField.getText().isEmpty()) {
			return;
		}
		switch (textField.getId()) {
		case 16: { // X Point pos
			if (this.point == null || !textField.isInteger()) {
				return;
			}
			this.point.x = textField.getInteger();
			this.initGui();
			break;
		}
		case 17: { // Z Point pos
			if (this.point == null || !textField.isInteger()) {
				return;
			}
			this.point.y = textField.getInteger();
			this.initGui();
			break;
		}
		case 22: { // Y max
			if (this.region == null || !textField.isInteger()) {
				return;
			}
			this.region.y[1] = textField.getInteger();
			this.initGui();
			break;
		}
		case 23: { // Y min
			if (this.region == null || !textField.isInteger()) {
				return;
			}
			this.region.y[0] = textField.getInteger();
			this.initGui();
			break;
		}
		case 24: { // Name
			if (this.region == null) {
				return;
			}
			this.region.name = textField.getText();
			break;
		}
		case 25: { // Home X
			if (this.region == null || !textField.isInteger()) {
				return;
			}
			IPos pos = this.region.homePos;
			this.region.setHomePos(textField.getInteger(), (int) pos.getY(), (int) pos.getZ());
			break;
		}
		case 26: { // Home Y
			if (this.region == null || !textField.isInteger()) {
				return;
			}
			IPos pos = this.region.homePos;
			this.region.setHomePos((int) pos.getX(), textField.getInteger(), (int) pos.getZ());
			break;
		}
		case 27: { // Home Z
			if (this.region == null || !textField.isInteger()) {
				return;
			}
			IPos pos = this.region.homePos;
			this.region.setHomePos((int) pos.getX(), (int) pos.getY(), textField.getInteger());
			break;
		}
		}
	}

}
