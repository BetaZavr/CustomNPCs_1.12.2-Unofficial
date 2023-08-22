package noppes.npcs.api.wrapper.gui;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.api.gui.ICustomGuiComponent;

public abstract class CustomGuiComponentWrapper
implements ICustomGuiComponent {

	String[] hoverText;
	int id, posX, posY, offsetType;
	
	public static CustomGuiComponentWrapper createFromNBT(NBTTagCompound nbt) {
		switch (nbt.getInteger("type")) {
			case 0: { return new CustomGuiButtonWrapper().fromNBT(nbt); }
			case 1: { return new CustomGuiLabelWrapper().fromNBT(nbt); }
			case 2: { return new CustomGuiTexturedRectWrapper().fromNBT(nbt); }
			case 3: { return new CustomGuiTextFieldWrapper().fromNBT(nbt); }
			case 4: { return new CustomGuiScrollWrapper().fromNBT(nbt); }
			case 5: { return new CustomGuiItemSlotWrapper().fromNBT(nbt); }
			case 6: { return new CustomGuiTimerWrapper().fromNBT(nbt); }
			default: { return null; }
		}
	}

	public CustomGuiComponentWrapper fromNBT(NBTTagCompound nbt) {
		this.setId(nbt.getInteger("id"));
		this.setPos(nbt.getIntArray("pos")[0], nbt.getIntArray("pos")[1]);
		if (nbt.hasKey("hover")) {
			NBTTagList list = nbt.getTagList("hover", 8);
			String[] hoverText = new String[list.tagCount()];
			for (int i = 0; i < list.tagCount(); ++i) {
				hoverText[i] = ((NBTTagString) list.get(i)).getString();
			}
			this.setHoverText(hoverText);
		}
		return this;
	}

	@Override
	public String[] getHoverText() {
		return this.hoverText;
	}

	@Override
	public int getId() {
		return this.id;
	}

	@Override
	public int getPosX() {
		return this.posX;
	}

	@Override
	public int getPosY() {
		return this.posY;
	}

	public abstract int getType();

	@Override
	public boolean hasHoverText() {
		return this.hoverText != null && this.hoverText.length > 0;
	}

	@Override
	public ICustomGuiComponent setHoverText(String text) {
		this.hoverText = new String[] { text };
		return this;
	}

	@Override
	public ICustomGuiComponent setHoverText(String[] text) {
		this.hoverText = text;
		return this;
	}

	@Override
	public ICustomGuiComponent setId(int id) {
		this.id = id;
		return this;
	}

	@Override
	public ICustomGuiComponent setPos(int x, int y) {
		this.posX = x;
		this.posY = y;
		return this;
	}

	public NBTTagCompound toNBT(NBTTagCompound nbt) {
		nbt.setInteger("id", this.id);
		nbt.setIntArray("pos", new int[] { this.posX, this.posY });
		if (this.hoverText != null) {
			NBTTagList list = new NBTTagList();
			for (String s : this.hoverText) {
				if (s != null && !s.isEmpty()) {
					list.appendTag(new NBTTagString(s));
				}
			}
			if (list.tagCount() > 0) {
				nbt.setTag("hover", list);
			}
		}
		nbt.setInteger("type", this.getType());
		return nbt;
	}

	@Override
	public void offSet(int type) {
		if (type<0) { type *= -1; }
		this.offsetType = type%4;
	}
	
}
