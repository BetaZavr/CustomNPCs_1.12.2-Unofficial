package noppes.npcs.client.gui.custom.components;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.wrapper.gui.CustomGuiTextFieldWrapper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IClickListener;
import noppes.npcs.client.gui.custom.interfaces.IDataHolder;
import noppes.npcs.client.gui.custom.interfaces.IKeyListener;

public class CustomGuiTextField extends GuiTextField implements IDataHolder, IClickListener, IKeyListener {
	public static CustomGuiTextField fromComponent(CustomGuiTextFieldWrapper component) {
		CustomGuiTextField txt = new CustomGuiTextField(component.getID(), component.getPosX(), component.getPosY(),
				component.getWidth(), component.getHeight());
		if (component.hasHoverText()) {
			txt.hoverText = component.getHoverText();
		}
		if (component.getText() != null && !component.getText().isEmpty()) {
			txt.setText(component.getText());
		}
		return txt;
	}

	String[] hoverText;
	public int id;
	GuiCustom parent;
    private final int[] offsets;

	public CustomGuiTextField(int id, int x, int y, int width, int height) {
		super(id, Minecraft.getMinecraft().fontRenderer, GuiCustom.guiLeft + x, GuiCustom.guiTop + y, width, height);
		this.setMaxStringLength(500);
		this.id = id;
        this.offsets = new int [] { 0, 0 };
	}

	public int getID() { return this.id; }

	public void keyTyped(char typedChar, int keyCode) {
		this.textboxKeyTyped(typedChar, keyCode);
	}

	public boolean mouseClicked(GuiCustom gui, int mouseX, int mouseY, int mouseButton) {
		return this.mouseClicked(mouseX, mouseY, mouseButton);
	}

	public void onRender(Minecraft mc, int mouseX, int mouseY, int mouseWheel, float partialTicks) {
		GlStateManager.pushMatrix();
		int x = this.offsets[0] == 0 ? this.x : this.offsets[0] - this.x;
		int y = this.offsets[1] == 0 ? this.y : this.offsets[1] - this.y;
		boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;
		GlStateManager.translate(x-this.x, y-this.y, this.id);
		this.drawTextBox();
		if (hovered && this.hoverText != null && this.hoverText.length > 0) { this.parent.hoverText = this.hoverText; }
		GlStateManager.popMatrix();
	}

	public void setParent(GuiCustom parent) {
		this.parent = parent;
	}

	public ICustomGuiComponent toComponent() {
		CustomGuiTextFieldWrapper component = new CustomGuiTextFieldWrapper(this.id, this.x - GuiCustom.guiLeft,
				this.y - GuiCustom.guiTop, this.width, this.height);
		component.setText(this.getText());
		component.setHoverText(this.hoverText);
		return component;
	}

	public NBTTagCompound toNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("id", this.id);
		tag.setString("text", this.getText());
		return tag;
	}
	
	@Override
	public void offSet(int offsetType, double[] windowSize) {
		switch(offsetType) {
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
	
}
