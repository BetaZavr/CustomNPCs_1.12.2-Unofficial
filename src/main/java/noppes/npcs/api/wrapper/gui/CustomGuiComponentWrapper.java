package noppes.npcs.api.wrapper.gui;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.item.IItemStack;

import java.util.Objects;

public abstract class CustomGuiComponentWrapper implements ICustomGuiComponent {

	public static CustomGuiComponentWrapper createFromNBT(NBTTagCompound nbt) {
		switch (nbt.getInteger("type")) {
			case 0: return new CustomGuiButtonWrapper().fromNBT(nbt);
			case 1: return new CustomGuiLabelWrapper().fromNBT(nbt);
			case 2: return new CustomGuiTexturedRectWrapper().fromNBT(nbt);
			case 3: return new CustomGuiTextFieldWrapper().fromNBT(nbt);
			case 4: return new CustomGuiScrollWrapper().fromNBT(nbt);
			case 5: return new CustomGuiItemSlotWrapper().fromNBT(nbt);
			case 6: return new CustomGuiTimerWrapper().fromNBT(nbt);
			case 7: return new CustomGuiEntityWrapper().fromNBT(nbt);
		}
		return null;
	}

	protected String[] hoverText;
	protected IItemStack hoverStack;
	protected int offsetType;
	protected int posX;
	protected int posY;
	protected int id;

	public CustomGuiComponentWrapper fromNBT(NBTTagCompound nbt) {
		setId(nbt.getInteger("id"));
		setPos(nbt.getIntArray("pos")[0], nbt.getIntArray("pos")[1]);
		if (nbt.hasKey("hover")) {
			NBTTagList list = nbt.getTagList("hover", 8);
			String[] hoverText = new String[list.tagCount()];
			for (int i = 0; i < list.tagCount(); ++i) { hoverText[i] = ((NBTTagString) list.get(i)).getString(); }
			setHoverText(hoverText);
		}
		if (nbt.hasKey("hoverStack")) { setHoverStack(Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(new ItemStack(nbt.getCompoundTag("hoverStack")))); }
		return this;
	}

	@Override
	public String[] getHoverText() { return hoverText; }

	@Override
	public int getId() { return id; }

	@Override
	public int getPosX() { return posX; }

	@Override
	public int getPosY() { return posY; }

	public abstract int getType();

	@Override
	public boolean hasHoverText() { return hoverStack != null || hoverText != null && hoverText.length > 0; }

	@Override
	public void offSet(int type) {
		if (type < 0) { type *= -1; }
		offsetType = type % 4;
	}

	@Override
	public IItemStack getHoverStack() { return hoverStack; }

	@Override
	public ICustomGuiComponent setHoverStack(IItemStack item) {
		hoverStack = item;
		return this;
	}

	@Override
	public ICustomGuiComponent setHoverText(String text) {
		hoverText = new String[] { text };
		return this;
	}

	@Override
	public ICustomGuiComponent setHoverText(String[] text) {
		hoverText = text;
		return this;
	}

	@Override
	public ICustomGuiComponent setId(int idIn) {
		id = idIn;
		return this;
	}

	@Override
	public ICustomGuiComponent setPos(int x, int y) {
		posX = x;
		posY = y;
		return this;
	}

	public NBTTagCompound toNBT(NBTTagCompound nbt) {
		nbt.setInteger("id", id);
		nbt.setIntArray("pos", new int[] { posX, posY });
		if (hoverText != null) {
			NBTTagList list = new NBTTagList();
			for (String s : hoverText) {
				if (s != null && !s.isEmpty()) { list.appendTag(new NBTTagString(s)); }
			}
			if (list.tagCount() > 0) { nbt.setTag("hover", list); }
		}
		if (hoverStack != null && !hoverStack.isEmpty()) { nbt.setTag("hoverStack", hoverStack.getMCItemStack().writeToNBT(new NBTTagCompound())); }
		nbt.setInteger("type", getType());
		return nbt;
	}

}
