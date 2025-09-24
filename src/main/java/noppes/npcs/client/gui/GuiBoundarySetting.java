package noppes.npcs.client.gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.TreeMap;

import noppes.npcs.client.gui.availability.SubGuiNpcAvailability;
import noppes.npcs.client.gui.select.SubGuiColorSelector;
import noppes.npcs.client.gui.select.SubGuiQuestSelection;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Quest;
import org.lwjgl.opengl.GL11;

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
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.BorderController;
import noppes.npcs.controllers.data.Zone3D;

import javax.annotation.Nonnull;

public class GuiBoundarySetting extends GuiNPCInterface
		implements ICustomScrollListener, ITextfieldListener, GuiSelectionListener {

	protected final TreeMap<Integer, String> dataRegions = new TreeMap<>();
	protected final TreeMap<Integer, String> dataPoints = new TreeMap<>();
	protected GuiCustomScroll regions, points;
	protected Point point;
	protected Zone3D region;
	protected int regID;

	public GuiBoundarySetting(int idReg, int idPoint) {
		super();
		setBackground("bgfilled.png");
		xSize = 405;
		ySize = 216;
		closeOnEsc = true;

		regID = idReg;
		region = BorderController.getInstance().getRegion(idReg);
		if (region != null && region.points.containsKey(idPoint)) { point = region.points.get(idPoint); }
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: {
				if (region == null) { return; }
				setSubGui(new SubGuiColorSelector(region.color));
				return;
			} // color
			case 1: {
				if (region == null) { return; }
				setSubGui(new SubGuiNpcAvailability(region.availability, this));
				return;
			} // availability
			case 2: {
				if (region == null) { return; }
				Client.sendData(EnumPacketServer.RegionData, 1, region.getId());
				region = null;
				point = null;
				break;
			} // del
			case 3: region.offset(-1, 0, 0); break;
			case 4: region.offset(1, 0, 0); break;
			case 5: region.offset(0, 0, -1); break;
			case 6: region.offset(0, 0, 1); break;
			case 7: region.offset(0, -1, 0); break;
			case 8: region.offset(0, 1, 0); break;
			case 10: {
				if (region == null || point == null) { return; }
				TreeMap<Integer, Point> map = new TreeMap<>();
				int i = 0;
				for (int pos : region.points.keySet()) {
					Point p = region.points.get(pos);
					if (p == point || (p.x == point.x && p.y == point.y)) {
						i = pos;
						break;
					}
				}
				int j = 0;
				for (int pos : region.points.keySet()) {
					if (pos == i) { continue; }
					if (pos + 1 == i) { map.put(j++, point); }
					Point p = region.points.get(pos);
					map.put(j++, p);
				}
				region.points.clear();
				region.points.putAll(map);
				break;
			} // Up Point Pos
			case 11: {
				if (region == null || point == null) { return; }
				TreeMap<Integer, Point> map = new TreeMap<>();
				int i = 0;
				for (int pos : region.points.keySet()) {
					Point p = region.points.get(pos);
					if (p == point || (p.x == point.x && p.y == point.y)) {
						i = pos;
						break;
					}
				}
				int j = 0;
				for (int pos : region.points.keySet()) {
					if (pos == i) { continue; }
					Point p = region.points.get(pos);
					map.put(j++, p);
					if (pos - 1 == i) { map.put(j++, point); }
				}
				region.points.clear();
				region.points.putAll(map);
				break;
			} // Down Point Pos
			case 12: {
				if (point == null) { return; }
				point.x--;
				break;
			} // OffSet Point -X
			case 13: {
				if (point == null) { return; }
				point.x++;
				break;
			} // OffSet Point +X
			case 14: {
				if (point == null) { return; }
				point.y--;
				break;
			} // OffSet Point -Z
			case 15: {
				if (point == null) { return; }
				point.y++;
				break;
			} // OffSet Point +Z
			case 18: {
				if (region == null) { return; }
				region.y[1]++;
				break;
			} // Max Y Up +
			case 19: {
				if (region == null) { return; }
				region.y[1]--;
				break;
			} // Max Y Up -
			case 20: {
				if (region == null) { return; }
				region.y[0]++;
				break;
			} // Max Y Down +
			case 21: {
				if (region == null) { return; }
				region.y[0]--;
				break;
			} // Max Y Down -
			case 24: {
				if (region == null) { return; }
				if (point != null) {
					Client.sendData(EnumPacketServer.TeleportTo, region.dimensionID, point.x, region.y[0] + (region.y[1] - region.y[0]) / 2, point.y);
					return;
				}
				IPos pos = region.getCenter();
				Client.sendData(EnumPacketServer.TeleportTo, region.dimensionID, pos.getX(), pos.getY(), pos.getZ());
				return;
			} // Teleport to Center
			case 25: {
				if (region == null) { return; }
				region.keepOut = ((GuiNpcCheckBox) button).isSelected();
				break;
			} // Keep Out Type
			case 26: {
				if (region == null) { return; }
				region.showInClient = ((GuiNpcCheckBox) button).isSelected();
				break;
			} // Show In Client
			case 27: {
				if (region == null) { return; }
				setSubGui(new SubGuiQuestSelection(region.questID));
				break;
			} // select quest
			case 28: {
				if (region == null) { return; }
				region.questID = 0;
				initGui();
				break;
			} // remove quest
			case 29: {
				if (region == null) { return; }
				region.questWhenEnter = ((GuiNpcCheckBox) button).isSelected();
				break;
			} // quest when enter
			case 30: {
				if (region == null) { return; }
				setSubGui(new SubGuiEditText(0, region.message)
						.setHoverTexts(new TextComponentTranslation("region.hover.enter.message")));
				break;
			} // message
		}
		initGui();
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
		if (region == null) { return; }
		double u0, u1, v0, v1;
		for (int i = -1; i < region.points.size() - 1; i++) {
			Point p0;
			if (i == -1) { p0 = region.points.get(region.points.size() - 1); }
			else { p0 = region.points.get(i); }
			Point p1 = region.points.get(i + 1);
			if (p0 == null || p1 == null) { continue; }
			u0 = (p0.x - mu) * su;
			v0 = (p0.y - mv) * sv;
			u1 = (p1.x - mu) * su;
			v1 = (p1.y - mv) * sv;
			GuiBoundarySetting.drawLine(u0, v0, u1, v1, -16776961, 2.0f);
		}
		v0 = (Math.min(region.y[1], 255)) * sy;
		v1 = (Math.max(region.y[0], 0)) * sy;
		drawLine(work + 12, v0, work + 13, v1, -16776961, 2.0f);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (background != null) {
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			GlStateManager.pushMatrix();
			GlStateManager.translate(guiLeft, guiTop, 1.0f);
			GlStateManager.scale(bgScale, bgScale, bgScale);
			mc.getTextureManager().bindTexture(background);
			if (xSize > 256) {
				drawTexturedModalRect(0, ySize - 1, 0, 252, 250, 4);
				drawTexturedModalRect(250, ySize - 1, 256 - (xSize - 250), 252, xSize - 250, 4);
			}
			GlStateManager.popMatrix();
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (subgui != null) { return; }
		int side = 160;
		int work = side - 12;
		int wu = guiLeft + 131, wv = guiTop + 18;
		String ht = "";
		if (isMouseHover(mouseX, mouseY, wu, wv, side - 2, side)) { ht = "region.hover.work.0"; }
		else if (isMouseHover(mouseX, mouseY, wu + side - 2, wv, 12, side)) { ht = "region.hover.work.1"; }
		GlStateManager.pushMatrix();
		GlStateManager.translate(wu, wv, 0.5f);
		int color = new Color(0xA0000000).getRGB();
		Gui.drawRect(0, 0, side + 12, side, color); // Main Place
		Gui.drawRect(6, 6, side - 6, side - 6, color); // Work Place
		Gui.drawRect(side - 2, 6, side + 9, side - 6, color); // Height place
		GlStateManager.translate(6, 6, 0.0d);
		if (region == null) {
			GlStateManager.popMatrix();
			return;
		}
		double mu = region.getMinX();
		double mv = region.getMinZ();
		double nu = region.getMaxX();
		double nv = region.getMaxZ();
		double su = (double) (work) / (nu - mu);
		double sv = (double) (work) / (nv - mv);
		double sy = (double) (work) / 255.0d;
		// Selected InSide Blue
		if (region.size() >= 3) { drawRegion(work, mu, mv, su, sv, sy); }
		int hx, hz;
		Point p = null;
		if (region.dimensionID == mc.player.world.provider.getDimension()) { p = region.points.get(region.getIdNearestPoint(mc.player.getPosition())); }
		// Nearest Point Green
		if (p != null) {
			hx = (int) (((double) p.x - mu) * su);
			hz = (int) (((double) p.y - mv) * sv);
			int d = ((point != null && point.x == p.x && point.y == p.y) ? 1 : 0);
			Gui.drawRect(hx - d - 1, hz - d - 1, hx + d + 1, hz + d + 1, new Color(0xFF00FF00).getRGB());
		}
		// Current Point
		if (point != null) {
			hx = (int) ((point.x - mu) * su);
			hz = (int) ((point.y - mv) * sv);
			Gui.drawRect(hx - 1, hz - 1, hx + 1, hz + 1, new Color(0xFFFFFF00).getRGB());
		}
		if (region.homePos != null) {
			hx = (int) ((region.homePos.getX() - mu) * su);
			hz = (int) ((region.homePos.getZ() - mv) * sv);
			if (hx < 0) { hx = 0; }
			else if (hx > work) { hx = work; }
			if (hz < 0) { hz = 0; }
			else if (hz > work) { hz = work; }
			Gui.drawRect(hx - 1, hz - 1, hx, hz, new Color(0xFFFF0000).getRGB());
		}
		// Center
		IPos c = region.getCenter();
		hx = (int) ((c.getX() - mu) * su);
		hz = (int) ((c.getZ() - mv) * sv);
		Gui.drawRect(hx - 1, hz - 1, hx + 1, hz + 1, new Color(0xFF0000FF).getRGB());
		// Player Position
		color = new Color(0xFFFFFFFF).getRGB();
		int xp = player.getPosition().getX();
		if (xp < mu) {
			xp = (int) mu;
			color = new Color(0xA0FFFFFF).getRGB();
		}
		else if (xp > nu) {
			xp = (int) nu;
			color = new Color(0xA0FFFFFF).getRGB();
		}
		hx = (int) ((xp - mu) * su);
		if (hx < 0) {
			hx = 0;
			color = new Color(0xF0FFFFFF).getRGB();
		}
		else if (hx > work) {
			hx = work;
			color = new Color(0xF0FFFFFF).getRGB();
		}
		int zp = player.getPosition().getZ();
		if (zp < mv) {
			zp = (int) mv;
			color = new Color(0xF0FFFFFF).getRGB();
		}
		else if (zp > nv) {
			zp = (int) nv;
			color = new Color(0xF0FFFFFF).getRGB();
		}
		hz = (int) ((zp - mv) * sv);
		int d = (point != null && point.x == player.getPosition().getX() && point.y == player.getPosition().getZ() ? 1 : 0) + 1;
		if (p != null && p.x == player.getPosition().getX() && p.y == player.getPosition().getZ()) { d++; }
		if (c.getX() == player.getPosition().getX() && c.getZ() == player.getPosition().getZ()) { d++; }
		Gui.drawRect(hx - d - 1, hz - d - 1, hx + d + 1, hz + d + 1, color); // XZ
		int hy = (int) (player.getPosition().getY() * sy);
		Gui.drawRect(side - 6, side - hy - 13, side - 4, side - hy - 11, new Color(0xFFFFFFFF).getRGB()); // Y
		GlStateManager.popMatrix();
		if (subgui != null || !CustomNpcs.ShowDescriptions) { return; }
		if (!ht.isEmpty()) { drawHoverText(ht); }
	}

	@Override
	public void initGui() {
		super.initGui();
		dataRegions.clear();
		dataPoints.clear();
		String selectReg = "";
		String selectP = "";
		int side = 186;
		int r0 = guiLeft + 118;
		int r1 = guiLeft + side + 119;
		int h0 = guiTop + 109;
		BorderController bData = BorderController.getInstance();
		if (region != null && !bData.regions.containsKey(region.getId())) {
			region = null;
			point = null;
		}
		if (region != null && point != null && !region.contains(point.x, point.y)) {
			point = null;
			if (!region.points.isEmpty()) { point = region.points.get(0); }
		}
		for (Zone3D reg : bData.regions.values()) {
			dataRegions.put(reg.getId(), reg.toString());
			if (regID == reg.getId()) {
				region = reg;
				selectReg = dataRegions.get(reg.getId());
				for (int id : reg.points.keySet()) {
					dataPoints.put(id, "ID: " + id + " [" + reg.points.get(id).x + ", " + reg.points.get(id).y + "]");
					if (point != null && (point == reg.points.get(id) || (point.x == reg.points.get(id).x && point.y == reg.points.get(id).y))) {
						selectP = dataPoints.get(id);
					}
				}
			}
		}
		if (regions == null) { (regions = new GuiCustomScroll(this, 0)).setSize(110, 130); }
		regions.setUnsortedList(new ArrayList<>(dataRegions.values()));
		regions.guiLeft = guiLeft + 5;
		regions.guiTop = guiTop + 14;
		if (!selectReg.isEmpty()) { regions.setSelected(selectReg); }
		addScroll(regions);
		// regions
		GuiNpcLabel label = new GuiNpcLabel(103, "gui.regions", guiLeft + 5, guiTop + 4);
		label.setHoverText("region.hover.regions.list", new TextComponentTranslation("item.npcboundary.name").getFormattedText());
		addLabel(label);
		if (points == null) { (points = new GuiCustomScroll(this, 1)).setSize(xSize - side - 124, side / 2); }
		points.setUnsortedList(new ArrayList<>(dataPoints.values()));
		points.guiLeft = r1;
		points.guiTop = guiTop + 14;
		if (!selectP.isEmpty()) { points.setSelected(selectP); }
		addScroll(points);
 		// points
		label = new GuiNpcLabel(104, "gui.points", r1, guiTop + 4);
		label.setHoverText("region.hover.points.list", new TextComponentTranslation("item.npcboundary.name").getFormattedText());
		addLabel(label);
		// ID 0 - color
		String color = "gui.color";
		if (region != null) {
			StringBuilder c = new StringBuilder(Integer.toHexString(region.color));
			while (c.length() < 6) { c.insert(0, "0"); }
			color = c.toString();
		}
		addButton( new GuiNpcButton(0, guiLeft + 5, guiTop + 162, 60, 13, color)
				.setIsEnable(region != null)
				.setTextColor(region != null ? region.color : 0)
				.setHoverText("region.hover.color"));
		// ID 1 - Available
		addButton(new GuiNpcButton(1, guiLeft + 5, guiTop + 147, 110, 13, "availability.available")
				.setIsEnable(region != null)
				.setHoverText("availability.hover"));
		addButton(new GuiNpcButton(2, guiLeft + 67, guiTop + 162, 48, 13, "gui.remove")
				.setIsEnable(region != null)
				.setHoverText("hover.delete"));
		// ID 3 - OffSet -X
		String trRegion = new TextComponentTranslation("gui.region").getFormattedText();
		addButton(new GuiNpcButton(3, r0 + 13, guiTop + 3, 13, 13, "←")
				.setIsEnable(region != null)
				.setHoverText("region.hover.offSet.-x", trRegion));
		// ID 4 - OffSet +X
		addButton(new GuiNpcButton(4, r0 + 27, guiTop + 3, 13, 13, "→")
				.setIsEnable(region != null)
				.setHoverText("region.hover.offSet.+x", trRegion));
		// ID 5 - OffSet -Z
		addButton(new GuiNpcButton(5, r0 - 1, guiTop + 18, 13, 13, "↑")
				.setIsEnable(region != null)
				.setHoverText("region.hover.offSet.-z", trRegion));
		// ID 6 - OffSet +Z
		addButton(new GuiNpcButton(6, r0 - 1, guiTop + 32, 13, 13, "↓")
				.setIsEnable(region != null)
				.setHoverText("region.hover.offSet.+z", trRegion));
		// ID 7 - OffSet -Y
		addButton(new GuiNpcButton(7, r0 - 1, guiTop + side - 21, 13, 13, "↓")
				.setIsEnable(region != null)
				.setHoverText("region.hover.offSet.-y", trRegion));
		// ID 8 - OffSet +Y
		addButton(new GuiNpcButton(8, r0 - 1, guiTop + side - 35, 13, 13, "↑")
				.setIsEnable(region != null)
				.setHoverText("region.hover.offSet.+y", trRegion));

		// ID 10 - Up Point Pos
		addButton(new GuiNpcButton(10, r1, h0, 39, 13, "˄")
				.setIsEnable(region != null)
				.setHoverText("region.hover.offSet.up", trRegion));
		// ID 11 - Down Point Pos
		addButton(new GuiNpcButton(11, r1 + 41, h0, 39, 13, "˅")
				.setIsEnable(region != null && point != null)
				.setHoverText("region.hover.point.offSet.down", trRegion));
		// ID 9 - remove point
		addButton(new GuiNpcButton(9, r1 + 82, h0, 13, 13, "X")
				.setIsEnable(region != null && point != null && dataPoints.size() > 1 && points.hasSelected())
				.setHoverText("region.hover.point.remove", trRegion));

		// ID 12 - OffSet Point -X
		String trPoint = new TextComponentTranslation("gui.point").getFormattedText();
		addButton(new GuiNpcButton(12, r1, h0 + 25, 12, 12, "←")
				.setIsEnable(region != null && point != null)
				.setHoverText("region.hover.offSet.-x", trPoint));
		// ID 13 - OffSet Point +X
		addButton(new GuiNpcButton(13, r1 + 22, h0 + 25, 12, 12, "→")
				.setIsEnable(region != null && point != null)
				.setHoverText("region.hover.offSet.+x", trPoint));
		// ID 14 - OffSet Point -Z
		addButton(new GuiNpcButton(14, r1 + 11, h0 + 14, 12, 12, "↑")
				.setIsEnable(region != null && point != null)
				.setHoverText("region.hover.offSet.-z", trPoint));
		// ID 15 - OffSet Point +Z
		addButton(new GuiNpcButton(15, r1 + 11, h0 + 36, 12, 12, "↓")
				.setIsEnable(region != null && point != null)
				.setHoverText("region.hover.offSet.+z", trPoint));

		// ID 18 - Max Y Up -
		addButton(new GuiNpcButton(18, r1, h0 + 50, 12, 12, "↑")
				.setIsEnable(region != null && point != null)
				.setHoverText("region.hover.offSet.up.-y", trPoint));
		// ID 19 - Max Y Up +
		addButton(new GuiNpcButton(19, r1, h0 + 62, 12, 12, "↓")
				.setIsEnable(region != null && point != null)
				.setHoverText("region.hover.offSet.up.+y", trPoint));
		// ID 20 - Max Y Down -
		addButton(new GuiNpcButton(20, r1 + 49, h0 + 50, 12, 12, "↑")
				.setIsEnable(region != null && point != null)
				.setHoverText("region.hover.offSet.down.-y", trPoint));
		// ID 21 - Max Y Down +
		addButton(new GuiNpcButton(21, r1 + 49, h0 + 62, 12, 12, "↓")
				.setIsEnable(region != null && point != null)
				.setHoverText("region.hover.offSet.down.+y", trPoint));

		// ID 24 - Teleport to Center
		addButton(new GuiNpcButton(24, r1 + 74, h0 + 23, 16, 16, "TP")
				.setIsEnable(region != null && point != null)
				.setHoverText("hover.teleport", trPoint));
		// ID 25 - Keep Out Type
		addButton(new GuiNpcCheckBox(25, guiLeft + 5, guiTop + side + 5, 110, 10, "region.keepout.true", "region.keepout.false" , region != null && region.keepOut)
				.setHoverText("region.hover.keepout"));
		// ID 26 - Keep Out Type
		addButton(new GuiNpcCheckBox(26, guiLeft + 5, guiTop + side + 16, 110, 10, "region.show.in.client.true", "region.show.in.client.false", region != null && region.showInClient)
				.setHoverText("region.hover.show.in.client"));
		// ID 27 - quest id
		String q = new TextComponentTranslation("quest.next").getFormattedText();
		if (region != null && region.questID > 0) {
			Quest quest = QuestController.instance.quests.get(region.questID);
			q = new TextComponentTranslation("gui.quest", ": " + (quest != null ? new TextComponentTranslation(quest.getName()).getFormattedText() : "")).getFormattedText();
		}
		addButton(new GuiNpcButton(27, r1, guiTop + side - 1, 79, 14, q)
				.setIsEnable(region != null)
				.setHoverText("region.hover.quest.id"));
		// ID 28 - remove quest
		addButton(new GuiNpcButton(28, r1 + 81, guiTop + side - 1, 14, 14, "X")
				.setIsEnable(region != null && region.questID > 0)
				.setHoverText("region.hover.quest.remove", trRegion));
		// ID 29 - quest when enter
		addButton(new GuiNpcCheckBox(29, r1, guiTop + side + 16, 95, 10, "region.quest.when.enter.true", "region.quest.when.enter.false", region != null && region.questWhenEnter)
				.setIsEnable(region != null)
				.setHoverText("region.hover.quest.when.enter"));
		// ID 30 - message
		addButton(new GuiNpcButton(30, r0 + 139, guiTop + side + 11, 46, 13, "gui.message")
				.setIsEnable(region != null)
				.setHoverText(new TextComponentTranslation("region.hover.enter.message").getFormattedText() + "<br>" +
						(region != null ? "\"" + region.message + "\":" : "") + "<br>" +
						(region != null ? new TextComponentTranslation(region.message).getFormattedText() : "")));
		// ID 30 - message
		addButton(new GuiNpcButton(30, r0 + 139, guiTop + side + 11, 46, 13, "gui.message")
				.setIsEnable(region != null)
				.setHoverText(new TextComponentTranslation("region.hover.enter.message").getFormattedText() + "<br>" +
						(region != null ? "\"" + region.message + "\":" : null) + "<br>" +
						(region != null ? new TextComponentTranslation(region.message).getFormattedText() : null)));
		// TextFields
		// X Point pos
		addTextField(new GuiNpcTextField(16, this, r1 + 38, h0 + 16, 31, 12, "" + (point != null ? point.x : 0))
				.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, point != null ? point.x : 0)
				.setIsEnable(region != null && point != null)
				.setHoverText("X pos"));
		// ID 17 - Z Point pos
		addTextField(new GuiNpcTextField(17, this, r1 + 38, h0 + 34, 31, 12, "" + (point != null ? point.y : 0))
				.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, point != null ? point.y : 0)
				.setIsEnable(region != null && point != null)
				.setHoverText("Z pos"));
		// ID 22 - Max Y
		addTextField(new GuiNpcTextField(22, this, r1 + 14, h0 + 56, 31, 12, "" + (region != null ? region.y[1] : 0))
				.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, region != null ? region.y[1] : 0)
				.setIsEnable(region != null && point != null)
				.setHoverText("max Y"));
		// ID 23 - Min Y
		addTextField(new GuiNpcTextField(23, this, r1 + 63, h0 + 56, 31, 12, "" + (region != null ? region.y[0] : 0))
				.setMinMaxDefault(Integer.MIN_VALUE, Integer.MAX_VALUE, region != null ? region.y[0] : 0)
				.setIsEnable(region != null && point != null)
				.setHoverText("min Y"));
		// ID 24 - Name
		addTextField(new GuiNpcTextField(24, this, guiLeft + 5, guiTop + 177, 110, 13, region != null ? region.name : "")
				.setIsEnable(region != null && point != null)
				.setHoverText("region.hover.name"));
		// ID 25 - Home X
		addTextField(new GuiNpcTextField(25, this, r0 + 30, guiTop + side + 11, 35, 13, "" + (region != null ? region.homePos.getX() : ""))
				.setIsEnable(region != null && point != null)
				.setHoverText("region.hover.home.axis", "X"));
		// ID 26 - Home Y
		addTextField(new GuiNpcTextField(26, this, r0 + 66, guiTop + side + 11, 35, 13, "" + (region != null ? region.homePos.getY() : ""))
				.setIsEnable(region != null && point != null)
				.setHoverText("region.hover.home.axis", "Y"));
		// ID 27 - Home Z
		addTextField(new GuiNpcTextField(27, this, r0 + 102, guiTop + side + 11, 35, 13, "" + (region != null ? region.homePos.getZ() : ""))
				.setIsEnable(region != null && point != null)
				.setHoverText("region.hover.home.axis", "Z"));
		// Labels
		// ID 99 - Home Pos
		addLabel(new GuiNpcLabel(99, "Home:", r0, guiTop + side + 12));
		getLabel(99).width = 28;
		getLabel(99).height = 12;
		// ID 100 - Min XZ Pos
		addLabel(new GuiNpcLabel(100, "MinXZ: [" + (region == null ? "0, 0" : region.getMinX() + "," + region.getMinZ()) + "]", r0 + 42, guiTop + 6));
		getLabel(100).width = 70;
		getLabel(100).height = 12;
		// ID 101 - Max XZ Pos
		addLabel(new GuiNpcLabel(101, "MaxXZ: [" + (region == null ? "0, 0" : region.getMaxX() + "," + region.getMaxZ()) + "]", r0, guiTop + side - 3));
		getLabel(101).width = 92;
		getLabel(101).height = 12;
		// ID 102 - Min/Max Y Pos
		addLabel(new GuiNpcLabel(102, "Min/Max Y: [" + (region == null ? "0, 0" : region.y[0] + "," + region.y[1]) + "]", r0 + 92, guiTop + side - 3));
		getLabel(102).width = 92;
		getLabel(101).height = 12;
		addLabel(new GuiNpcLabel(105, "(worldID: " + (region == null ? "N/A" : region.dimensionID) + ")", r0 + 115, guiTop + 6, guiTop + 4));
		getLabel(105).width = 70;
		getLabel(105).height = 12;
	}

	@Override
	public void save() {
		if (region != null) {
			NBTTagCompound regionNbt = new NBTTagCompound();
			region.save(regionNbt);
			Client.sendData(EnumPacketServer.RegionData, 2, regionNbt);
		}
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		switch (scroll.getID()) {
			case 0: { // Region List
				if (!dataRegions.containsValue(scroll.getSelected())) { return; }
				BorderController bData = BorderController.getInstance();
				for (int id : dataRegions.keySet()) {
					if (region != null && region.getId() == id) { continue; }
					if (dataRegions.get(id).equals(scroll.getSelected()) && bData.regions.containsKey(id)) {
						region = bData.getRegion(id);
						regID = id;
						point = null;
						if (!region.points.isEmpty()) { point = region.points.get(0); }
						Client.sendData(EnumPacketServer.RegionData, 0, id);
						initGui();
						break;
					}
				}
				break;
			}
			case 1: { // Point List
				if (region == null || !dataPoints.containsValue(scroll.getSelected())) { return; }
				for (int id : dataPoints.keySet()) {
					if (dataPoints.get(id).equals(scroll.getSelected()) && region.points.containsKey(id)) {
						point = region.points.get(id);
						initGui();
						break;
					}
				}
				break;
			}
		}
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		switch (scroll.getID()) {
			case 0: { // Region List
				if (region == null) { return; }
				IPos pos = region.getCenter();
				Client.sendData(EnumPacketServer.TeleportTo, region.dimensionID, pos.getX(), pos.getY(), pos.getZ());
				Client.sendData(EnumPacketServer.RegionData, 0, region.getId());
				onClosed();
				break;
			}
			case 1: { // Point List
				if (region == null || point == null) { return; }
				Client.sendData(EnumPacketServer.TeleportTo, region.dimensionID, point.x, region.y[0] + (region.y[1] - region.y[0]) / 2, point.y);
				Client.sendData(EnumPacketServer.RegionData, 0, region.getId());
				onClosed();
				break;
			}
		}
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (region != null) {
			if (subgui instanceof SubGuiColorSelector) { region.color = ((SubGuiColorSelector) subgui).color; }
			if (subgui instanceof SubGuiEditText) { region.message = ((SubGuiEditText) subgui).text[0]; }
		}
		initGui();
	}

	@Override
	public void selected(int id, String name) {
		region.questID = id;
		initGui();
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (textField.getText().isEmpty()) { return; }
		switch (textField.getID()) {
			case 16: {
				if (point == null || !textField.isInteger()) { return; }
				point.x = textField.getInteger();
				initGui();
				break;
			} // X Point pos
			case 17: {
				if (point == null || !textField.isInteger()) { return; }
				point.y = textField.getInteger();
				initGui();
				break;
			} // Z Point pos
			case 22: {
				if (region == null || !textField.isInteger()) { return; }
				region.y[1] = textField.getInteger();
				initGui();
				break;
			} // Y max
			case 23: {
				if (region == null || !textField.isInteger()) { return; }
				region.y[0] = textField.getInteger();
				initGui();
				break;
			} // Y min
			case 24: {
				if (region == null) { return; }
				region.name = textField.getText();
				break;
			} // Name
			case 25: {
				if (region == null || !textField.isInteger()) { return; }
				IPos pos = region.homePos;
				region.setHomePos(textField.getInteger(), (int) pos.getY(), (int) pos.getZ());
				break;
			} // Home X
			case 26: {
				if (region == null || !textField.isInteger()) { return; }
				IPos pos = region.homePos;
				region.setHomePos((int) pos.getX(), textField.getInteger(), (int) pos.getZ());
				break;
			} // Home Y
			case 27: {
				if (region == null || !textField.isInteger()) { return; }
				IPos pos = region.homePos;
				region.setHomePos((int) pos.getX(), (int) pos.getY(), textField.getInteger());
				break;
			} // Home Z
		}
	}

}
