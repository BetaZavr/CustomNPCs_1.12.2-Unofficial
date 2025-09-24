package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.constants.GuiComponentType;
import noppes.npcs.api.gui.IScroll;

public class CustomGuiScrollWrapper extends CustomGuiComponentWrapper implements IScroll {

	protected String[] list;
	protected boolean multiSelect = false;
	protected int defaultSelection = -1;
	protected int height;
	protected int width;

	public CustomGuiScrollWrapper() { }

	public CustomGuiScrollWrapper(int id, int x, int y, int width, int height, String[] list) {
		setId(id);
		setPos(x, y);
		setSize(width, height);
		setList(list);
	}

	@Override
	public CustomGuiComponentWrapper fromNBT(NBTTagCompound nbt) {
		super.fromNBT(nbt);
		setSize(nbt.getIntArray("size")[0], nbt.getIntArray("size")[1]);
		if (nbt.hasKey("default")) { setDefaultSelection(nbt.getInteger("default")); }
		NBTTagList tagList = nbt.getTagList("list", 8);
		String[] list = new String[tagList.tagCount()];
		for (int i = 0; i < tagList.tagCount(); ++i) { list[i] = ((NBTTagString) tagList.get(i)).getString(); }
		setList(list);
		setMultiSelect(nbt.getBoolean("multiSelect"));
		return this;
	}

	@Override
	public int getDefaultSelection() { return defaultSelection; }

	@Override
	public int getHeight() { return height; }

	@Override
	public String[] getList() { return list; }

	@Override
	public int getType() { return GuiComponentType.SCROLL.get(); }

	@Override
	public int getWidth() { return width; }

	@Override
	public boolean isMultiSelect() { return multiSelect; }

	@Override
	public IScroll setDefaultSelection(int defaultSelectionIn) {
		defaultSelection = defaultSelectionIn;
		return this;
	}

	@Override
	public IScroll setList(String[] listIn) {
		list = listIn;
		return this;
	}

	@Override
	public IScroll setMultiSelect(boolean multiSelectIn) {
		multiSelect = multiSelectIn;
		return this;
	}

	@Override
	public IScroll setSize(int widthIn, int heightIn) {
		if (widthIn <= 0 || heightIn <= 0) {
			throw new CustomNPCsException("Invalid component width or height: [" + widthIn + ", " + heightIn + "]");
		}
		width = widthIn;
		height = heightIn;
		return this;
	}

	@Override
	public NBTTagCompound toNBT(NBTTagCompound nbt) {
		super.toNBT(nbt);
		nbt.setIntArray("size", new int[] { width, height });
		if (defaultSelection >= 0) { nbt.setInteger("default", defaultSelection); }
		NBTTagList listTag = new NBTTagList();
		for (String s : list) { listTag.appendTag(new NBTTagString(s)); }
		nbt.setTag("list", listTag);
		nbt.setBoolean("multiSelect", multiSelect);
		return nbt;
	}

}
