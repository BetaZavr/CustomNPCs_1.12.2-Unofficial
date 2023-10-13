package noppes.npcs.client.gui.custom.components;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.renderer.GlStateManager;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.wrapper.gui.CustomGuiLabelWrapper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.util.ObfuscationHelper;

public class CustomGuiLabel
extends GuiLabel
implements IGuiComponent {
	
	public static CustomGuiLabel fromComponent(CustomGuiLabelWrapper component) {
		CustomGuiLabel lbl = new CustomGuiLabel(component.getText(), component.getId(), component.getPosX(), component.getPosY(), component.getWidth(), component.getHeight(), component.getColor());
		lbl.showShedow = component.isShedow();
		lbl.setScale(component.getScale());
		if (component.hasHoverText()) { lbl.hoverText = component.getHoverText(); }
		return lbl;
	}

	int colour;
	String fullLabel;
	String[] hoverText;
	GuiCustom parent;
	float scale;
    private boolean showShedow;
	private final int[] offsets;

	public CustomGuiLabel(String label, int id, int x, int y, int width, int height) {
		this(label, id, x, y, width, height, CustomNpcs.lableColor);
	}

	public CustomGuiLabel(String label, int id, int x, int y, int width, int height, int colour) {
		super(Minecraft.getMinecraft().fontRenderer, id, GuiCustom.guiLeft + x, GuiCustom.guiTop + y, width, height, colour);
		this.scale = 1.0f;
		this.fullLabel = label;
		this.colour = colour;
		this.showShedow = true;
		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
		// int sWidth = fontRenderer.getStringWidth(label);
		for (String s : fontRenderer.listFormattedStringToWidth(label, width)) {
			this.addLine(s);
		}
        this.offsets = new int [] { 0, 0 };
	}

	public int getId() {
		return this.id;
	}

	public void onRender(Minecraft mc, int mouseX, int mouseY, int mouseWheel, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.scale(this.scale, this.scale, 0.0f);
		int x = this.offsets[0] == 0 ? this.x : this.offsets[0] - this.x - this.width;
		int y = this.offsets[1] == 0 ? this.y : this.offsets[1] - this.y - this.height;
		boolean hovered = mouseX >= x && mouseY >= y && mouseX < x + this.width && mouseY < y + this.height;
		GlStateManager.translate(x-this.x, y-this.y, this.id < 1000 ? this.id : 1000);
		if (this.showShedow) { this.drawLabel(mc, mouseX, mouseY); }
		else if (this.visible) {
			GlStateManager.enableBlend();
	        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
	        this.drawLabelBackground(mc, mouseX, mouseY);
	        int border = ObfuscationHelper.getValue(GuiLabel.class, this, 14);
	        boolean centered = ObfuscationHelper.getValue(GuiLabel.class, this, 6);
	        List<String> labels = ObfuscationHelper.getValue(GuiLabel.class, this, List.class);
	        int i = this.y + this.height / 2 + border / 2;
	        int j = i - labels.size() * 10 / 2;
	        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
	        for (int k = 0; k < labels.size(); ++k) {
	            if (centered) { mc.fontRenderer.drawString(labels.get(k), this.x + (this.width - mc.fontRenderer.getStringWidth(labels.get(k))) / 2, j + k * 10, this.colour, false); }
	            else { mc.fontRenderer.drawString(labels.get(k), this.x, j + k * 10, this.colour, false); }
	        }
		}
		if (hovered && this.hoverText != null && this.hoverText.length > 0) {
			this.parent.hoverText = this.hoverText;
		}
		GlStateManager.popMatrix();
	}
	
	@Override
	public void setParent(GuiCustom parent) {
		this.parent = parent;
	}

	public void setScale(float scale) {
		this.scale = scale;
	}

	public ICustomGuiComponent toComponent() {
		CustomGuiLabelWrapper component = new CustomGuiLabelWrapper(this.id, this.fullLabel, this.x, this.y, this.width, this.height, this.colour);
		component.setShedow(this.showShedow);
		component.setHoverText(this.hoverText);
		return component;
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

	@Override
	public int[] getPosXY() { return new int[] { this.x, this.y }; }

	@Override
	public void setPosXY(int newX, int newY) { 
		this.x = newX;
		this.y = newY;
	}
	
}
