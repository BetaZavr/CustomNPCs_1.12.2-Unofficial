package noppes.npcs.client.gui.advanced;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.select.SubGuiColorSelector;
import noppes.npcs.client.gui.availability.SubGuiNpcAvailability;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.MarkData.Mark;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class GuiNPCMarks extends GuiNPCInterface2 implements ICustomScrollListener {

	protected static final String[] marks = new String[] { "gui.none", "mark.question", "mark.exclamation", "mark.pointer", "mark.skull", "mark.cross", "mark.star" };
	protected final MarkData data;
	protected MarkData.Mark selectedMark;

	// New from Unofficial Betazavr
	protected final EntityNPCInterface npcDisplay;
	protected final MarkData dataDisplay;
	protected final GuiScreen parent;
	protected GuiCustomScroll scroll;
	protected String selMark = "";

	public GuiNPCMarks(EntityNPCInterface npc, GuiScreen gui) {
		super(npc);
		closeOnEsc = true;
		parentGui = EnumGuiType.MainMenuAdvanced;

		data = MarkData.get(npc);
		parent = gui;
		npcDisplay = new EntityCustomNpc(mc.world);
		NBTTagCompound nbtData = new NBTTagCompound();
		npc.writeEntityToNBT(nbtData);
		npcDisplay.readEntityFromNBT(nbtData);
		npcDisplay.display.setShowName(1);
		dataDisplay = MarkData.get(npcDisplay);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		if (selectedMark == null) { return; }
		switch (button.getID()) {
			case 0: {
				selectedMark.setType(button.getValue());
				initGui();
				break;
			}
			case 1: {
				setSubGui(new SubGuiColorSelector(selectedMark.color, new SubGuiColorSelector.ColorCallback() {
					@Override
					public void color(int colorIn) {
						if (selectedMark == null) { return; }
						if (!data.marks.isEmpty() && scroll.hasSelected() && scroll.getSelect() < data.marks.size()) { data.marks.get(scroll.getSelect()).setColor(colorIn); }
						selectedMark.color = colorIn;
						initGui();
					}
					@Override
					public void preColor(int colorIn) {
						if (!dataDisplay.marks.isEmpty()) { dataDisplay.marks.get(0).setColor(colorIn); }
					}
				}));
				break;
			}
			case 2: {
				setSubGui(new SubGuiNpcAvailability(selectedMark.availability, parent));
				break;
			}
			case 3: {
				Mark newark = data.addMark(selectedMark.getType());
				newark.color = selectedMark.color;
				newark.rotate = selectedMark.rotate;
				newark.availability.load(selectedMark.availability.save(new NBTTagCompound()));
				selectedMark = newark;
				initGui();
				break;
			}
			case 4: {
				if (scroll.getSelect() < 0) { return; }
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
				initGui();
				break;
			}
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		List<String> ds = new ArrayList<>();
		List<Integer> colors = new ArrayList<>();
		int i = 0;
		for (Mark mark : data.marks) {
			String name = i + ": " + new TextComponentTranslation(marks[mark.getType()]).getFormattedText();
			ds.add(name);
			colors.add(mark.color);
			if (!selMark.isEmpty() && selMark.equals(name)) { selectedMark = mark; }
			i++;
		}
		if (scroll == null) { scroll = new GuiCustomScroll(this, 0).setSize(130, 174); }
		scroll.setUnsortedList(ds);
		scroll.guiLeft = guiLeft + 5;
		scroll.guiTop = guiTop + 14;
		scroll.setColors(colors);
		if (selectedMark != null && !selMark.isEmpty()) { scroll.setSelected(selMark); }
		addScroll(scroll);
		if (selectedMark == null) { selectedMark = data.getNewMark(); }
		// type
		addButton(new GuiButtonBiDirectional(0, guiLeft + 140, guiTop + 14, 120, 20, marks, selectedMark.getType())
				.setHoverText("mark.hover.type"));
		// color
		StringBuilder color = new StringBuilder(Integer.toHexString(selectedMark.getColor()));
		while (color.length() < 6) { color.insert(0, "0"); }
		addButton(new GuiNpcButton(1, guiLeft + 140, guiTop + 36, 120, 20, color.toString())
				.setHoverText("color.hover")
				.setTextColor(selectedMark.getColor()));
		// availability
		addButton(new GuiNpcButton(2, guiLeft + 140, guiTop + 58, 120, 20, "availability.options")
				.setHoverText("availability.hover"));
		// add
		addButton(new GuiNpcButton(3, guiLeft + 5, guiTop + ySize - 9, 64, 20, "gui.add")
				.setIsEnable(selectedMark.getType() > 0)
				.setHoverText("mark.hover.add"));
		// del
		addButton(new GuiNpcButton(4, guiLeft + 71, guiTop + ySize - 9, 64, 20, "gui.remove")
				.setIsEnable(scroll.hasSelected())
				.setHoverText("mark.hover.del"));
		// is rotation
		addButton(new GuiNpcButton(5, guiLeft + 140, guiTop + 80, 120, 20, new String[] { "movement.rotation", "ai.standing" }, selectedMark.rotate ? 0 : 1)
				.setHoverText("mark.hover.rotate"));
		// view
		addButton(new GuiNpcButton(6, guiLeft + 140, guiTop + 102, 120, 20, new String[] { "3D", "2D" }, selectedMark.is3d ? 0 : 1)
				.setHoverText("mark.hover.is3d"));
		// list
		dataDisplay.marks.clear();
		MarkData.Mark mark = dataDisplay.addMark(selectedMark.getType());
		mark.setColor(selectedMark.color);
		mark.setRotate(selectedMark.rotate);
		mark.set3D(selectedMark.is3d);
		mark.availability = new Availability();
		addLabel(new GuiNpcLabel(5, new TextComponentTranslation("advanced.marks").getFormattedText() + ":", guiLeft + 5, guiTop + 4));
	}

	@Override
	public void save() { Client.sendData(EnumPacketServer.MainmenuAdvancedMarkData, data.getNBT()); }

	// New from Unofficial Betazavr
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0f, 0.0f, 1.0f);
		Gui.drawRect(guiLeft + 319, guiTop + 30, guiLeft + 380, guiTop + 165, 0xFF808080);
		Gui.drawRect(guiLeft + 320, guiTop + 31, guiLeft + 379, guiTop + 164, 0xFF000000);
		GlStateManager.popMatrix();
		GlStateManager.pushMatrix();
		drawNpc(npcDisplay, 350, 150, 1.0f, 0, 0, 1);
		GlStateManager.popMatrix();
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		if (selMark.equals(scroll.getSelected())) { return; }
		selMark = scroll.getSelected();
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { }

}
