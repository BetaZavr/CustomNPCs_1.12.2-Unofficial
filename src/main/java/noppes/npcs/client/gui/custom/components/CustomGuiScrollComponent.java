package noppes.npcs.client.gui.custom.components;

import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
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
import noppes.npcs.client.gui.util.ICustomScrollListener;

public class CustomGuiScrollComponent extends GuiCustomScroll implements IDataHolder, IClickListener {

	public CustomGuiScrollWrapper component;
	protected GuiCustom parent;
	protected final int[] offsets;

	public CustomGuiScrollComponent(Minecraft mcIn, ICustomScrollListener parent, int id, CustomGuiScrollWrapper componentIn) {
		super(parent, id, true, componentIn.isMultiSelect());
		mc = mcIn;
		fontRenderer = mcIn.fontRenderer;
		component = componentIn;
		offsets = new int[] { 0, 0 };
	}

	public void fromComponent(CustomGuiScrollWrapper component) {
		guiLeft = GuiCustom.guiLeft + component.getPosX();
		guiTop = GuiCustom.guiTop + component.getPosY();
		setSize(component.getWidth(), component.getHeight());
		setUnsortedList(Arrays.asList(component.getList()));
		if (component.getDefaultSelection() >= 0) {
			int defaultSelect = component.getDefaultSelection();
			if (defaultSelect < getList().size()) { selected = defaultSelect; }
		}
		if (component.hasHoverText()) {
			component.setHoverText(component.getHoverText());
			component.setHoverStack(component.getHoverStack());
		}
	}

	@Override
	public int getId() { return id; }

	@Override
	public int[] getPosXY() {
		return new int[] { guiLeft, guiTop };
	}

	@Override
	public boolean mouseClicked(GuiCustom gui, int mouseX, int mouseY, int mouseButton) {
		try	{ super.mouseClicked(mouseX, mouseY, mouseButton); } catch (Exception ignored) { }
        return isMouseOver(mouseX, mouseY);
	}

	@Override
	public void offSet(int offsetType, double[] windowSize) {
		switch (offsetType) {
			case 1: { // left down
				offsets[0] = 0;
				offsets[1] = (int) windowSize[1];
				break;
			}
			case 2: { // right up
				offsets[0] = (int) windowSize[0];
				offsets[1] = 0;
				break;
			}
			case 3: { // right down
				offsets[0] = (int) windowSize[0];
				offsets[1] = (int) windowSize[1];
				break;
			}
			case 4: { // center
				offsets[0] = (int) (windowSize[0] / 2.0d);
				offsets[1] = (int) (windowSize[1] / 2.0d);
				break;
			}
			default: { // left up
				offsets[0] = 0;
				offsets[1] = 0;
			}
		}
	}

	@Override
	public void onRender(Minecraft mc, int mouseX, int mouseY, int mouseWheel, float partialTicks) {
		GlStateManager.pushMatrix();
		int x = offsets[0] == 0 ? guiLeft : offsets[0] - guiLeft;
		int y = offsets[1] == 0 ? guiTop : offsets[1] - guiTop;
		mouseInList = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
		GlStateManager.translate(x - guiLeft, y - guiTop, id);
		super.drawScreen(mouseX, mouseY, mouseWheel);
		if (mouseInList && component.hasHoverText()) {
			if (component.getHoverText() != null && component.getHoverText().length > 0) { parent.hoverText = component.getHoverText(); }
			if (component.getHoverStack() != null && !component.getHoverStack().isEmpty()) { parent.hoverStack = component.getHoverStack().getMCItemStack(); }
		}
		GlStateManager.popMatrix();
	}

	@Override
	public void setParent(GuiCustom parentIn) { parent = parentIn; }

	@Override
	public void setPosXY(int newX, int newY) {
		guiLeft = newX;
		guiTop = newY;
	}

	public ICustomGuiComponent toComponent() {
		List<String> list = getList();
		component.setList(list.toArray(new String[0]));
		return component;
	}

	public NBTTagCompound toNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setInteger("id", id);
		if (!getSelectedList().isEmpty()) {
			NBTTagList tagList = new NBTTagList();
			for (String s : getSelectedList()) { tagList.appendTag(new NBTTagString(s)); }
			nbt.setTag("selectedList", tagList);
		}
		else if (getSelected() != null && !getSelected().isEmpty()) { nbt.setString("selected", getSelected()); }
		else { nbt.setString("selected", "Null"); }
		return nbt;
	}

}
