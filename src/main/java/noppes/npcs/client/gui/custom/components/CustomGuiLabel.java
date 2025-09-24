package noppes.npcs.client.gui.custom.components;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiLabel;
import net.minecraft.client.renderer.GlStateManager;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.gui.CustomGuiLabelWrapper;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.api.mixin.client.gui.IGuiLabelMixin;
import noppes.npcs.util.ValueUtil;

public class CustomGuiLabel extends GuiLabel implements IGuiComponent {

	public static CustomGuiLabel fromComponent(CustomGuiLabelWrapper componentIn) {
		CustomGuiLabel lbl = new CustomGuiLabel(componentIn.getText(), componentIn.getId(), componentIn.getPosX(),
				componentIn.getPosY(), componentIn.getWidth(), componentIn.getHeight(), componentIn.getColor());
		lbl.showShadow = componentIn.isShadow();
		lbl.setScale(componentIn.getScale());
		if (componentIn.hasHoverText()) {
			lbl.hoverText = componentIn.getHoverText();
			lbl.hoverStack = componentIn.getHoverStack();
		}
		return lbl;
	}

	protected final int[] offsets = new int[] { 0, 0 };
	protected IItemStack hoverStack;
	protected GuiCustom parent;
	protected String[] hoverText;
	protected String fullLabel;
	protected boolean showShadow = true;
	protected float scale = 1.0f;
	protected int colour;

	public CustomGuiLabel(String label, int id, int x, int y, int width, int height, int colourIn) {
		super(Minecraft.getMinecraft().fontRenderer, id, GuiCustom.guiLeft + x, GuiCustom.guiTop + y, width, height, colourIn);
		fullLabel = label;
		colour = colourIn;
		FontRenderer fontRenderer = Minecraft.getMinecraft().fontRenderer;
		for (String s : fontRenderer.listFormattedStringToWidth(label, width)) { addLine(s); }
	}

	public int getId() {
		return id;
	}

	@Override
	public int[] getPosXY() {
		return new int[] { x, y };
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

	public void onRender(Minecraft mc, int mouseX, int mouseY, int mouseWheel, float partialTicks) {
		GlStateManager.pushMatrix();
		GlStateManager.scale(scale, scale, 0.0f);
		int xIn = offsets[0] == 0 ? x : offsets[0] - x - width;
		int yIn = offsets[1] == 0 ? y : offsets[1] - y - height;
		boolean hovered = mouseX >= xIn && mouseY >= yIn && mouseX < xIn + width && mouseY < yIn + height;
		GlStateManager.translate(xIn - x, yIn - y, Math.min(id, 1000));
		if (showShadow) { drawLabel(mc, mouseX, mouseY); }
		else if (visible) {
			GlStateManager.enableBlend();
			GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
					GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
					GlStateManager.DestFactor.ZERO);
			drawLabelBackground(mc, mouseX, mouseY);
			int border = ((IGuiLabelMixin) this).npcs$getBorder();
			boolean centered = ((IGuiLabelMixin) this).npcs$getCentered();
			List<String> labels = ((IGuiLabelMixin) this).npcs$getLabels();
			int i = y + height / 2 + border / 2;
			int j = i - labels.size() * 10 / 2;
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			for (int k = 0; k < labels.size(); ++k) {
				if (centered) { mc.fontRenderer.drawString(labels.get(k), (float) (width - mc.fontRenderer.getStringWidth(labels.get(k))) / 2, j + k * 10, colour, false); }
				else { mc.fontRenderer.drawString(labels.get(k), x, j + k * 10, colour, false); }
			}
		}
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

	public void setScale(float scaleIn) { scale = ValueUtil.correctFloat(scaleIn, 0.0f, 10.0f); }

	@Override
	public ICustomGuiComponent toComponent() {
		CustomGuiLabelWrapper component = new CustomGuiLabelWrapper(id, fullLabel, x, y, width, height, colour);
		component.setShadow(showShadow);
		component.setHoverText(hoverText);
		return component;
	}

}
