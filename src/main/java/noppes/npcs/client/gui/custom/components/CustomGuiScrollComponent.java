package noppes.npcs.client.gui.custom.components;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.wrapper.gui.CustomGuiScrollWrapper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IClickListener;
import noppes.npcs.client.gui.custom.interfaces.IDataHolder;
import noppes.npcs.client.gui.util.GuiCustomScroll;

public class CustomGuiScrollComponent extends GuiCustomScroll implements IDataHolder, IClickListener {

	public CustomGuiScrollWrapper component;
	GuiCustom parent;
	private final int[] offsets;

	public CustomGuiScrollComponent(Minecraft mc, GuiScreen parent, int id, CustomGuiScrollWrapper component) {
		super(parent, id, component.isMultiSelect());
		this.mc = mc;
		this.fontRenderer = mc.fontRenderer;
		this.component = component;
		this.offsets = new int[] { 0, 0 };
	}

	public void fromComponent(CustomGuiScrollWrapper component) {
		this.guiLeft = GuiCustom.guiLeft + component.getPosX();
		this.guiTop = GuiCustom.guiTop + component.getPosY();
		this.setSize(component.getWidth(), component.getHeight());
		this.setListNotSorted(Arrays.asList(component.getList()));
		if (component.getDefaultSelection() >= 0) {
			int defaultSelect = component.getDefaultSelection();
			if (defaultSelect < this.getList().size()) {
				this.selected = defaultSelect;
			}
		}
		if (component.hasHoverText()) {
			component.setHoverText(component.getHoverText());
		}
	}

	public int getId() {
		return this.id;
	}

	@Override
	public int[] getPosXY() {
		return new int[] { this.guiLeft, this.guiTop };
	}

	@Override
	public boolean mouseClicked(GuiCustom gui, int mouseX, int mouseY, int mouseButton) {
		super.mouseClicked(mouseX, mouseY, mouseButton);
		return this.isMouseOver(mouseX, mouseY);
	}

	@Override
	public void offSet(int offsetType, double[] windowSize) {
		switch (offsetType) {
		case 1: { // left down
			this.offsets[0] = 0;
			this.offsets[1] = (int) windowSize[1];
			break;
		}
		case 2: { // right up
			this.offsets[0] = (int) windowSize[0];
			this.offsets[1] = 0;
			break;
		}
		case 3: { // right down
			this.offsets[0] = (int) windowSize[0];
			this.offsets[1] = (int) windowSize[1];
			break;
		}
		default: { // left up
			this.offsets[0] = 0;
			this.offsets[1] = 0;
		}
		}
	}

	public void onRender(Minecraft mc, int mouseX, int mouseY, int mouseWheel, float partialTicks) {
		GlStateManager.pushMatrix();
		int x = this.offsets[0] == 0 ? this.guiLeft : this.offsets[0] - this.guiLeft;
		int y = this.offsets[1] == 0 ? this.guiTop : this.offsets[1] - this.guiTop;
		this.hovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;
		GlStateManager.translate(x - this.guiLeft, y - this.guiTop, this.id);
		super.drawScreen(mouseX, mouseY, mouseWheel);
		if (this.hovered && this.component.hasHoverText()) {
			this.parent.hoverText = this.component.getHoverText();
		}
		GlStateManager.popMatrix();
	}

	public void setParent(GuiCustom parent) {
		this.parent = parent;
	}

	@Override
	public void setPosXY(int newX, int newY) {
		this.guiLeft = newX;
		this.guiTop = newY;
	}

	public ICustomGuiComponent toComponent() {
		List<String> list = this.getList();
		this.component.setList(list.toArray(new String[0]));
		return this.component;
	}

	public NBTTagCompound toNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("id", this.id);
		if (!this.getSelectedList().isEmpty()) {
			NBTTagList tagList = new NBTTagList();
			for (String s : this.getSelectedList()) {
				tagList.appendTag(new NBTTagString(s));
			}
			nbt.setTag("selectedList", tagList);
		} else if (this.getSelected() != null && !this.getSelected().isEmpty()) {
			nbt.setString("selected", this.getSelected());
		} else {
			nbt.setString("selected", "Null");
		}
		return nbt;
	}

}
