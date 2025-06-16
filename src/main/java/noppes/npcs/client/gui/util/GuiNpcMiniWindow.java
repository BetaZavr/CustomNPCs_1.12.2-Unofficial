package noppes.npcs.client.gui.util;

import noppes.npcs.api.mixin.client.gui.IGuiTextFieldMixin;
import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class GuiNpcMiniWindow
extends GuiNPCInterface
implements IComponentGui, IGuiNpcMiniWindow, ITextfieldListener, ISliderListener, ICustomScrollListener, IKeyListener {

	private final IEditNPC parent;
	private IComponentGui point;
	public int id, mousePressX, mousePressY;
	private int colorLine = 0x6C00FF;
	public boolean hovered = false;
	public boolean isMoving = false;
	public boolean visible = true;
	public String title;
	public Object[] objs = null;
	private final List<String> hoverText = new ArrayList<>();

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
	public void buttonEvent(IGuiNpcButton button) {
		if (!hovered) { return; }
		if (button.getID() == 2500) {
			parent.closeMiniWindow(this);
			visible = false;
		}
		else if (buttons.containsKey(button.getID())) {
			parent.buttonEvent(button);
		}
	}

	@Override
	public void render(IEditNPC gui, int mouseX, int mouseY, float partialTicks) {
		if (!visible) {
			hovered = false;
			return;
		}
		hovered = isMouseHover(mouseX, mouseY, guiLeft, guiTop, xSize, ySize);
		if (hovered && !hoverText.isEmpty()) { gui.setHoverText(hoverText); }
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
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (isMoving && Mouse.isButtonDown(0)) {
			int x = mouseX - mousePressX;
			int y = mouseY - mousePressY;
			if (x != 0 || y != 0) {
				moveOffset(x, y);
				mousePressX = mouseX;
				mousePressY = mouseY;
			}
		} else { isMoving = false; }
		if (hasSubGui() || !CustomNpcs.ShowDescriptions || hoverMiniWin) { return; }

		for (IComponentGui component : components) {
			if ((component instanceof GuiButton && ((GuiButton) component).isMouseOver() ||
					(component instanceof GuiNpcTextField && component.isHovered()) ||
					(component instanceof GuiCustomScroll && ((GuiCustomScroll) component).hovered)) ||
					(component instanceof GuiNpcLabel && ((GuiNpcLabel) component).hovered)) {
				parent.setMiniHoverText(id, component);
			}
		}
	}

	public void postDrawBackground() {
		GuiNpcMiniWindow.drawTopRect(guiLeft + 3, guiTop + 3, guiLeft + xSize - 3, guiTop + 11, zLevel, colorLine + 0xF0000000, colorLine + 0x40000000);
		drawString(fontRenderer, title, guiLeft + 4, guiTop + 3, CustomNpcs.MainColor.getRGB());
	}

	public void moveOffset(int x, int y) {
		guiLeft += x;
		guiTop += y;
		for (IGuiNpcButton b : buttons.values()) { b.setLeft(b.getLeft() + x); b.setTop(b.getTop() + y); }
		for (IGuiNpcLabel l : labels.values()) { l.setLeft(l.getLeft() + x); l.setTop(l.getTop() + y); }
		for (IGuiCustomScroll s : scrolls.values()) { s.setLeft(s.getLeft() + x); s.setTop(s.getTop() + y); }
		for (IGuiMenuSideButton sb : sideButtons.values()) { sb.setLeft(sb.getLeft() + x); sb.setTop(sb.getTop() + y); }
		for (IGuiNpcSlider sl : sliders.values()) { sl.setLeft(sl.getLeft() + x); sl.setTop(sl.getTop() + y); }
		for (IGuiNpcTextField t : textFields.values()) { t.setLeft(t.getLeft() + x); t.setTop(t.getTop() + y); }
		for (IGuiMenuTopButton tb : topButtons.values()) { tb.setLeft(tb.getLeft() + x); tb.setTop(tb.getTop() + y); }
		for (IGuiNpcMiniWindow mw : miniWindows.values()) { mw.setLeft(mw.getLeft() + x); mw.setTop(mw.getTop() + y); }
	}

	public void keyTyped(char c, int i) {
		if (i == 15 && GuiNpcTextField.isActive() && textFields.containsValue(GuiNpcTextField.activeTextfield)) { // Tab
			int id = GuiNpcTextField.activeTextfield.getID() + 1;
			if (id > (getTextField(9) != null ? 9 : 7)) { id = 5; }
			IGuiNpcTextField textField = getTextField(id);
			if (textField != null) {
				GuiNpcTextField.activeTextfield.unFocus();
				textField.setFocus(true);
				((IGuiTextFieldMixin) textField).npcs$setCursorPosition(0);
				((IGuiTextFieldMixin) textField).npcs$setSelectionEnd(textField.getFullText().length());
				GuiNpcTextField.activeTextfield = textField;
			}
		}
		super.keyTyped(c, i);
	}

	public void mouseClicked(int mouseX, int mouseY, int mouseBottom) {
		if (!hovered) { return; }
		super.mouseClicked(mouseX, mouseY, mouseBottom);
		if (hovered && mouseBottom == 0 && isMouseHover(mouseX, mouseY, guiLeft + 3, guiTop + 3, xSize - 3, 8)) {
			mousePressX = mouseX;
			mousePressY = mouseY;
			isMoving = true;
		}
	}

	public void mouseEvent(int mouseX, int mouseY, int mouseBottom) {
		if (!hovered) { return; }
		super.mouseEvent(mouseX, mouseY, mouseBottom);
	}

	public void mouseReleased(int mouseX, int mouseY, int mouseBottom) {
		if (!hovered) { return; }
		super.mouseReleased(mouseX, mouseY, mouseBottom);
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		if (!hovered) { return; }
		if (scrolls.containsKey(scroll.getID()) && parent instanceof ICustomScrollListener) {
			((ICustomScrollListener) parent).scrollClicked(mouseX, mouseY, mouseButton, scroll);
		}
	}

	@Override
	public void scrollDoubleClicked(String select, IGuiCustomScroll scroll) {
		if (!hovered) { return; }
		if (scrolls.containsKey(scroll.getID()) && parent instanceof ICustomScrollListener) {
			((ICustomScrollListener) parent).scrollDoubleClicked(select, scroll);
		}
	}

	@Override
	public void mouseDragged(IGuiNpcSlider slider) {
		if (!hovered) { return; }
		if (sliders.containsKey(slider.getID())) {
			parent.mouseDragged(slider);
		}
	}

	@Override
	public void mousePressed(IGuiNpcSlider slider) {
		if (!hovered) { return; }
		if (sliders.containsKey(slider.getID())) {
			parent.mousePressed(slider);
		}
	}

	@Override
	public void mouseReleased(IGuiNpcSlider slider) {
		if (!hovered) { return; }
		if (sliders.containsKey(slider.getID())) {
			parent.mouseReleased(slider);
		}
	}

	@Override
	public void unFocused(IGuiNpcTextField textField) {
		if (textFields.containsKey(textField.getID()) ) {
			parent.unFocused(textField);
		}
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

	@Override
	public void resetButtons() {
		buttons.remove(2500);
		components.removeIf(c -> c instanceof GuiNpcButton && c.getID() == 2500);

		GuiNpcButton exit = new GuiNpcButton(2500, guiLeft + xSize - 12, guiTop + 3, 8, 8, "X");
		exit.setTexture(ANIMATION_BUTTONS);
		exit.setHasDefaultBack(false);
		exit.setIsAnim(true);
		exit.setTextureXY(232, 0);
		exit.setTextureUV(24, 24);
		exit.setLayerColor(new Color(0xFFFF0000).getRGB());
		exit.setTextColor(new Color(0xFF404040).getRGB());
		addButton((IGuiNpcButton) exit);
	}

	@Override
	public int getID() { return id; }

	@Override
	public int[] getCenter() { return new int[] { guiLeft + width / 2, guiTop + height / 2}; }

	@Override
	public void setHoverText(String text, Object ... args) {
		hoverText.clear();
		if (text == null || text.isEmpty()) { return; }
		if (!text.contains("%")) { text = new TextComponentTranslation(text, args).getFormattedText(); }
		if (text.contains("~~~")) { text = text.replaceAll("~~~", "%"); }
		while (text.contains("<br>")) {
			hoverText.add(text.substring(0, text.indexOf("<br>")));
			text = text.substring(text.indexOf("<br>") + 4);
		}
		hoverText.add(text);
	}

	@Override
	public int getLeft() { return guiLeft; }

	@Override
	public int getTop() { return guiTop; }

	@Override
	public void setLeft(int left) { guiLeft = left; }

	@Override
	public void setTop(int top) { guiTop = top; }

	@Override
	public int getWidth() { return width; }

	@Override
	public int getHeight() { return height; }

	@Override
	public void customKeyTyped(char c, int id) { keyTyped(c, id); }

	@Override
	public void customMouseClicked(int mouseX, int mouseY, int mouseButton) { mouseClicked(mouseX, mouseY, mouseButton); }

	@Override
	public void customMouseReleased(int mouseX, int mouseY, int mouseButton) { mouseReleased(mouseX, mouseY, mouseButton); }

	@Override
	public boolean isVisible() { return visible; }

	@Override
	public void setIsVisible(boolean bo) { visible = bo; }

	@Override
	public boolean isEnabled() { return true; }

	@Override
	public void setEnabled(boolean bo) { }

	@Override
	public boolean isHovered() { return hovered; }

}
