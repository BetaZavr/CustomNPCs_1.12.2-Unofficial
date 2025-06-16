package noppes.npcs.client.gui.advanced;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiColorSelector;
import noppes.npcs.client.gui.availability.SubGuiNpcAvailability;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.client.renderer.MarkRenderer;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.MarkData.Mark;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

public class GuiNPCMarks
extends GuiNPCInterface2
implements ISubGuiListener, ICustomScrollListener {

	private static final String[] marks = new String[] { "gui.none", "mark.question", "mark.exclamation", "mark.pointer", "mark.skull", "mark.cross", "mark.star" };

	private final MarkData data;
	private final MarkData dataDisplay;
	private final EntityNPCInterface npcDisplay;
	private GuiCustomScroll scroll;
	private MarkData.Mark selectedMark;
	private String selMark = "";
	public final GuiScreen parent;

	public GuiNPCMarks(EntityNPCInterface npc, GuiScreen gui) {
		super(npc);
		parent = gui;
		data = MarkData.get(npc);
		npcDisplay = new EntityCustomNpc(npc.world);
		NBTTagCompound nbtData = new NBTTagCompound();
		npc.writeEntityToNBT(nbtData);
		npcDisplay.readEntityFromNBT(nbtData);
		npcDisplay.display.setShowName(1);
		dataDisplay = MarkData.get(npcDisplay);
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (selectedMark == null) {
			return;
		}
		switch (button.getID()) {
			case 0: {
				selectedMark.type = button.getValue();
				initGui();
				break;
			}
			case 1: {
				setSubGui(new SubGuiColorSelector(selectedMark.color));
				break;
			}
			case 2: {
				setSubGui(new SubGuiNpcAvailability(selectedMark.availability, parent));
				break;
			}
			case 3: {
				Mark newark = (Mark) data.addMark(selectedMark.type);
				newark.color = selectedMark.color;
				newark.rotate = selectedMark.rotate;
				newark.availability.readFromNBT(selectedMark.availability.writeToNBT(new NBTTagCompound()));
				selectedMark = newark;
				initGui();
				break;
			}
			case 4: {
				if (scroll.getSelect() < 0) {
					return;
				}
				data.marks.remove(selectedMark);
				scroll.setSelect(-1);
				selMark = "";
				selectedMark = null;
				initGui();
				break;
			}
			case 5: {
				selectedMark.rotate = button.getValue() == 0;
				initGui();
				break;
			}
			case 6: {
				selectedMark.is3d = button.getValue() == 0;
				MarkRenderer.needReload = true;
				initGui();
				break;
			}
		}
	}

	@Override
	public void close() {
		save();
		CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuAdvanced);
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		GlStateManager.pushMatrix();
		drawNpc(npcDisplay, 350, 150, 1.0f, 0, 0, 1);
		GlStateManager.popMatrix();

		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0f, 0.0f, 1.0f);
		Gui.drawRect(guiLeft + 319, guiTop + 30, guiLeft + 380, guiTop + 165, 0xFF808080);
		Gui.drawRect(guiLeft + 320, guiTop + 31, guiLeft + 379, guiTop + 164, 0xFF000000);
		GlStateManager.popMatrix();
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void initGui() {
		super.initGui();
		List<String> ds = new ArrayList<>();
		List<Integer> colors = new ArrayList<>();
		int i = 0;
		for (Mark mark : data.marks) {
			String name = i + ": " + new TextComponentTranslation(marks[mark.type]).getFormattedText();
			ds.add(name);
			colors.add(mark.color);
			if (!selMark.isEmpty() && selMark.equals(name)) {
				selectedMark = mark;
			}
			i++;
		}
		if (scroll == null) {
			(scroll = new GuiCustomScroll(this, 0)).setSize(130, 174);
		}
		scroll.setListNotSorted(ds);
		scroll.guiLeft = guiLeft + 5;
		scroll.guiTop = guiTop + 14;
		scroll.setColors(colors);
		if (selectedMark != null && !selMark.isEmpty()) {
			scroll.setSelected(selMark);
		}
		addScroll(scroll);
		if (selectedMark == null) {
			selectedMark = (Mark) data.getNewMark();
		}
		// type
		GuiNpcButton button = new GuiButtonBiDirectional(0, guiLeft + 140, guiTop + 14, 120, 20, marks, selectedMark.getType());
		button.setHoverText("mark.hover.type");
		addButton(button);
		// color
		StringBuilder color = new StringBuilder(Integer.toHexString(selectedMark.getColor()));
		while (color.length() < 6) { color.insert(0, "0"); }
		button = new GuiNpcButton(1, guiLeft + 140, guiTop + 36, 120, 20, color.toString());
		button.setHoverText("color.hover");
		button.setTextColor(selectedMark.getColor());
		addButton(button);
		// availability
		button = new GuiNpcButton(2, guiLeft + 140, guiTop + 58, 120, 20, "availability.options");
		button.setHoverText("availability.hover");
		addButton(button);
		// add
		button = new GuiNpcButton(3, guiLeft + 5, guiTop + ySize - 9, 64, 20, "gui.add");
		button.enabled = selectedMark.type > 0;
		button.setHoverText("mark.hover.add");
		addButton(button);
		// del
		button = new GuiNpcButton(4, guiLeft + 71, guiTop + ySize - 9, 64, 20, "gui.remove");
		button.enabled = scroll.getSelect() >= 0;
		button.setHoverText("mark.hover.del");
		addButton(button);
		// is rotation
		button = new GuiNpcButton(5, guiLeft + 140, guiTop + 80, 120, 20, new String[] { "movement.rotation", "ai.standing" }, selectedMark.rotate ? 0 : 1);
		button.setHoverText("mark.hover.rotate");
		addButton(button);
		// view
		button = new GuiNpcButton(6, guiLeft + 140, guiTop + 102, 120, 20, new String[] { "3D", "2D" }, selectedMark.is3d ? 0 : 1);
		button.setHoverText("mark.hover.is3d");
		addButton(button);
		// list
		dataDisplay.marks.clear();
		MarkData.Mark mark = (Mark) dataDisplay.addMark(selectedMark.type);
		mark.setColor(selectedMark.color);
		mark.setRotate(selectedMark.rotate);
		mark.set3D(selectedMark.is3d);
		mark.availability = new Availability();
		addLabel(new GuiNpcLabel(5, new TextComponentTranslation("advanced.marks").getFormattedText() + ":", guiLeft + 5, guiTop + 4));
	}

	@Override
	public void keyTyped(char c, int i) {
		if (i == 1 && subgui == null) {
			save();
			CustomNpcs.proxy.openGui(npc, EnumGuiType.MainMenuAdvanced);
		}
		super.keyTyped(c, i);
	}

	@Override
	public void save() {
		Client.sendData(EnumPacketServer.MainmenuAdvancedMarkData, data.getNBT());
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, IGuiCustomScroll scroll) {
		if (selMark.equals(scroll.getSelected())) { return; }
		selMark = scroll.getSelected();
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, IGuiCustomScroll scroll) {
	}

	@Override
	public void subGuiClosed(SubGuiInterface subgui) {
		if (subgui instanceof SubGuiColorSelector) {
			if (selectedMark == null) { return; }
			selectedMark.color = ((SubGuiColorSelector) subgui).color;
			initGui();
		}
	}

}
