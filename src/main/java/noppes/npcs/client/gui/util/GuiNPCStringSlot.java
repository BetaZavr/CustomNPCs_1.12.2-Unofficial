package noppes.npcs.client.gui.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiSlot;
import noppes.npcs.util.NaturalOrderComparator;

public class GuiNPCStringSlot
extends GuiSlot {

	private List<String> list;
	private final boolean multiSelect;
	private final GuiNPCInterface parent;
	public String selected;
	public HashSet<String> selectedList = new HashSet<>();
	public int size;

	public GuiNPCStringSlot(Collection<String> slotList, GuiNPCInterface gui, boolean isMultiSelect, int slotHeightIn) {
		super(Minecraft.getMinecraft(), gui.width, gui.height, 32, gui.height - 64, slotHeightIn);
		parent = gui;
		(list = new ArrayList<>(slotList)).sort(new NaturalOrderComparator());
		multiSelect = isMultiSelect;
		size = slotHeightIn;
	}

	public void clear() {
		list.clear();
	}

	protected void drawBackground() {
		parent.drawDefaultBackground();
	}

	protected void drawSlot(int i, int j, int k, int l, int var6, int var7, float partialTick) {
		String s = list.get(i);
		parent.drawString(parent.getFontRenderer(), s, j + 50, k + 3, 16777215);
	}

	protected void elementClicked(int i, boolean flag, int j, int k) {
		if (selected != null && selected.equals(list.get(i)) && flag) {
			parent.doubleClicked();
		}
		selected = list.get(i);
		if (selectedList.contains(selected)) {
			selectedList.remove(selected);
		} else {
			selectedList.add(selected);
		}
		parent.elementClicked();
	}

	protected int getContentHeight() {
		return list.size() * size;
	}

	protected int getSize() {
		return list.size();
	}

	protected boolean isSelected(int i) {
		if (!multiSelect) {
			return selected != null && selected.equals(list.get(i));
		}
		return selectedList.contains(list.get(i));
	}

	public void setList(List<String> newList) {
		newList.sort(new NaturalOrderComparator());
		list = newList;
		selected = "";
	}
}
