package noppes.npcs.client.gui.util;

import noppes.npcs.api.mixin.client.gui.IGuiTextFieldMixin;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;

import javax.annotation.Nonnull;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiNpcMiniWindow extends GuiNPCInterface implements IComponentGui, ISliderListener {

	protected final List<String> hoverText = new ArrayList<>();
	protected final IEditNPC parent;
	protected IComponentGui point;
	protected GuiNpcButton exit;
	protected int colorLine = 0x6C00FF;

	public int id, mousePressX, mousePressY;
	public boolean hovered = false;
	public boolean isMoving = false;
	public boolean visible = true;
	public String title;
	public Object[] objs = null;

	public GuiNpcMiniWindow(IEditNPC gui, int id, int x, int y, int width, int height, String title) {
		parent = gui;
		this.id = id;
		guiLeft = x;
		guiTop = y;
		xSize = width;
		ySize = height + 12;
		this.title = new TextComponentTranslation(title).getFormattedText();
		setBackground("bgfilled.png");
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (!hovered || mouseButton != 0) { return; }
		if (button == exit) {
			parent.closeMiniWindow(this);
			visible = false;
		}
		else if (buttons.containsKey(button.getID())) { parent.buttonEvent(button, mouseButton); }
	}

	@Override
	public void render(IEditNPC gui, int mouseX, int mouseY, float partialTicks) {
		if (!visible) { hovered = false; return; }
		hovered = isMouseHover(mouseX, mouseY, guiLeft, guiTop, xSize, ySize);
		if (hovered && !hoverText.isEmpty()) { gui.putHoverText(hoverText); }
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (point != null) {
			double xc = (double)guiLeft + (double)xSize / 2.0d, yc = (double)guiTop + (double)ySize / 2.0d;
			double dist = Math.sqrt((mouseY - yc) * (mouseY - yc) + (mouseX - xc) * (mouseX - xc));
			double base = Math.sqrt(Math.pow(xSize, 2.0d) + Math.pow(ySize, 2.0d)) / 2.0d;
			if (dist <= base * 2.0d) {
				double a = -1.0d / (2.0d * base - base);
				double b = -2.0d * a  * base;
				float alpha = (float) (a * dist + b);
				if (alpha < 0.0f) { alpha = 0.0f; } else if (alpha > 1.0f) { alpha = 1.0f; }
				int[] cr = point.getCenter();
				int color = colorLine + ((int) (alpha * 255.0f) << 24);
				parent.addLine(cr[0], cr[1], guiLeft + xSize / 2, guiTop + ySize / 2, color, 2);
			}
		}
		if (isMoving && Mouse.isButtonDown(0)) {
			int x = mouseX - mousePressX;
			int y = mouseY - mousePressY;
			if (x != 0 || y != 0) {
				moveOffset(x, y);
				mousePressX = mouseX;
				mousePressY = mouseY;
			}
		} else { isMoving = false; }
	}

	public void postDrawBackground() {
		GuiNpcMiniWindow.drawTopRect(guiLeft + 3, guiTop + 3, guiLeft + xSize - 3, guiTop + 11, zLevel, colorLine + 0xF0000000, colorLine + 0x40000000);
		drawString(fontRenderer, title, guiLeft + 4, guiTop + 3, CustomNpcs.MainColor.getRGB());
	}

	public void moveOffset(int x, int y) {
		guiLeft += x;
		guiTop += y;
		for (IComponentGui component : new ArrayList<>(components)) { component.moveTo(x, y); }
	}

	public boolean keyCnpcsPressed(char typedChar, int keyCode) {
		if (keyCode == Keyboard.KEY_TAB && GuiNpcTextField.isActive() && textFields.containsValue(GuiNpcTextField.activeTextfield)) { // Tab
			int id = GuiNpcTextField.activeTextfield.getID() + 1;
			if (id > (getTextField(9) != null ? 9 : 7)) { id = 5; }
			GuiNpcTextField textField = getTextField(id);
			if (textField != null) {
				GuiNpcTextField.activeTextfield.unFocus();
				textField.setFocused(true);
				((IGuiTextFieldMixin) textField).npcs$setCursorPosition(0);
				((IGuiTextFieldMixin) textField).npcs$setSelectionEnd(textField.getText().length());
				GuiNpcTextField.activeTextfield = textField;
				return true;
			}
		}
		return super.keyCnpcsPressed(typedChar, keyCode);
	}

	public boolean mouseCnpcsPressed(int mouseX, int mouseY, int mouseBottom) {
		if (!hovered) { return false; }
		if (mouseBottom == 0 && isMouseHover(mouseX, mouseY, guiLeft + 3, guiTop + 3, xSize - 3, 8)) {
			mousePressX = mouseX;
			mousePressY = mouseY;
			isMoving = true;
			return true;
		}
		return super.mouseCnpcsPressed(mouseX, mouseY, mouseBottom);
	}

	public boolean mouseCnpcsReleased(int mouseX, int mouseY, int mouseBottom) {
		if (!hovered) { return false; }
		return super.mouseCnpcsReleased(mouseX, mouseY, mouseBottom);
	}

	public static void drawTopRect(int left, int top, int right, int bottom, float zLevel, int startColor, int endColor) {
		float f = (float)(startColor >> 24 & 255) / 255.0F;
		float f1 = (float)(startColor >> 16 & 255) / 255.0F;
		float f2 = (float)(startColor >> 8 & 255) / 255.0F;
		float f3 = (float)(startColor & 255) / 255.0F;
		float f4 = (float)(endColor >> 24 & 255) / 255.0F;
		float f5 = (float)(endColor >> 16 & 255) / 255.0F;
		float f6 = (float)(endColor >> 8 & 255) / 255.0F;
		float f7 = (float)(endColor & 255) / 255.0F;
		GlStateManager.disableTexture2D();
		GlStateManager.enableBlend();
		GlStateManager.disableAlpha();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.shadeModel(7425);
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder bufferbuilder = tessellator.getBuffer();
		bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);

		bufferbuilder.pos(left, top, zLevel).color(f1, f2, f3, f).endVertex();
		bufferbuilder.pos(left, bottom, zLevel).color(f1, f2, f3, f).endVertex();
		bufferbuilder.pos(right, bottom, zLevel).color(f5, f6, f7, f4).endVertex();
		bufferbuilder.pos(right, top, zLevel).color(f5, f6, f7, f4).endVertex();

		tessellator.draw();
		GlStateManager.shadeModel(7424);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}

	public void setPoint(IComponentGui component) { point = component; }

	public void setColorLine(int color) {
		int red = color >> 16 & 255;
		int green = color >> 8 & 255;
		int blue = color & 255;
		colorLine = (red << 16) + (green << 8) + blue;
	}

	public int getColorLine() { return colorLine; }

	public void resetButtons() {
		if (exit != null) {
			buttons.remove(exit.getID());
			components.removeIf(c -> c == exit);
		}
		addButton(exit = new GuiNpcButton(2500, guiLeft + xSize - 12, guiTop + 3, 8, 8, "X")
				.setTexture(ANIMATION_BUTTONS)
				.setHasDefaultBack(false)
				.setIsAnim(true)
				.setUV(232, 0, 24, 24)
				.setLayerColor(new Color(0xFFFF0000).getRGB())
				.setTextColor(new Color(0xFF404040).getRGB()));
	}

	@Override
	public int getID() { return id; }

	@Override
	public int[] getCenter() { return new int[] { guiLeft + width / 2, guiTop + height / 2}; }

	public GuiNpcMiniWindow setHoverText(Object... components) {
		hoverText.clear();
		if (components == null) { return this; }
		noppes.npcs.util.Util.instance.putHovers(hoverText, components);
		return this;
	}

	@Override
	public GuiNpcMiniWindow setIsVisible(boolean isVisible) { visible = isVisible; return this; }

	@Override
	public void moveTo(int addX, int addY) {
		guiLeft += addX;
		guiTop += addY;
	}

	@Override
	public void updateCnpcsScreen() { super.updateScreen(); }

	@Override
	public GuiNpcMiniWindow setIsEnable(boolean isEnable) { return this; }

	@Override
	public List<String> getHoversText() { return hoverText; }

	@Override
	public boolean isHovered() { return hovered; }

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		if (parent instanceof ISliderListener) { ((ISliderListener) parent).mouseDragged(slider); }
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) {
		if (parent instanceof ISliderListener) { ((ISliderListener) parent).mousePressed(slider); }
	}

	@Override
	public void mouseReleased(GuiNpcSlider slider) {
		if (parent instanceof ISliderListener) { ((ISliderListener) parent).mouseReleased(slider); }
	}

}
