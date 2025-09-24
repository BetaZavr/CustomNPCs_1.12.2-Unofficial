package noppes.npcs.client.gui.custom.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.gui.CustomGuiTextFieldWrapper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IClickListener;
import noppes.npcs.client.gui.custom.interfaces.ICustomKeyListener;
import noppes.npcs.client.gui.custom.interfaces.IDataHolder;

public class CustomGuiTextField extends GuiTextField implements IDataHolder, IClickListener, ICustomKeyListener {

	public static CustomGuiTextField fromComponent(CustomGuiTextFieldWrapper component) {
		CustomGuiTextField txt = new CustomGuiTextField(component.getId(), component.getPosX(), component.getPosY(),
				component.getWidth(), component.getHeight());
		if (component.hasHoverText()) {
			txt.hoverText = component.getHoverText();
			txt.hoverStack = component.getHoverStack();
		}
		if (component.getText() != null && !component.getText().isEmpty()) { txt.setText(component.getText()); }
		return txt;
	}

	protected String[] hoverText;
	protected IItemStack hoverStack;
	protected GuiCustom parent;
	protected final int[] offsets = new int[] { 0, 0 };
	public int id;

	public CustomGuiTextField(int idIn, int x, int y, int width, int height) {
		super(idIn, Minecraft.getMinecraft().fontRenderer, GuiCustom.guiLeft + x, GuiCustom.guiTop + y, width, height);
		setMaxStringLength(500);
		id = idIn;
	}

	public int getId() { return id; }

	@Override
	public int[] getPosXY() {
		return new int[] { x, y };
	}

	public void keyTyped(char typedChar, int keyCode) { textboxKeyTyped(typedChar, keyCode); }

	public boolean mouseClicked(GuiCustom gui, int mouseX, int mouseY, int mouseButton) { return mouseClicked(mouseX, mouseY, mouseButton); }

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

	public void onRender(Minecraft mc, int mouseX, int mouseY, int mouseWheel, float partialTicks) {
		GlStateManager.pushMatrix();
		int xIn = offsets[0] == 0 ? x : offsets[0] - x;
		int yIn = offsets[1] == 0 ? y : offsets[1] - y;
		boolean hovered = mouseX >= xIn && mouseY >= yIn && mouseX < xIn + width && mouseY < yIn + height;
		GlStateManager.translate(xIn - x, yIn - y, id);
		drawTextBox();
		if (hovered) {
			if (hoverText != null && hoverText.length > 0) { parent.hoverText = hoverText; }
			if (hoverStack != null && !hoverStack.isEmpty()) { parent.hoverStack = hoverStack.getMCItemStack(); }
		}
		GlStateManager.popMatrix();
	}

	@Override
	public void setParent(GuiCustom parentIn) { parent = parentIn; }

	@Override
	public void setPosXY(int newX, int newY) {
		x = newX;
		y = newY;
	}

	public ICustomGuiComponent toComponent() {
		CustomGuiTextFieldWrapper component = new CustomGuiTextFieldWrapper(id, x - GuiCustom.guiLeft, y - GuiCustom.guiTop, width, height);
		component.setText(getText());
		component.setHoverText(hoverText);
		return component;
	}

	public NBTTagCompound toNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("id", id);
		tag.setString("text", getText());
		return tag;
	}

}
