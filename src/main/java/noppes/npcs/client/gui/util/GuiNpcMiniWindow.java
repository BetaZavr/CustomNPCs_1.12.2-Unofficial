package noppes.npcs.client.gui.util;

import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.animation.SubGuiEditAnimation;

public class GuiNpcMiniWindow
extends GuiNPCInterface
implements IComponentGui, ITextfieldListener, ISliderListener, ICustomScrollListener, IKeyListener {

	private IEditNPC parent;
	private IComponentGui point;
	public int id, mousePressX, mousePressY;
	private int colorLine = 0x6C00FF;
	public boolean hovered = false, isMoving = false, visible = true;
	public String title = "";
	
	public GuiNpcMiniWindow(IEditNPC parent, int id, int x, int y, int width, int height, String title) {
		this.parent = parent;
		this.id = id;
		this.guiLeft = x;
		this.guiTop = y;
		this.xSize = width;
		this.ySize = height + 12;
		this.title = new TextComponentTranslation(title).getFormattedText();
		this.setBackground("bgfilled.png");
	}

	@Override
	public void save() { }
	
	public void buttonEvent(GuiNpcButton button) {
		if (!this.hovered) { return; }
		if (button.id == 2500) {
			this.parent.closeMiniWindow(this);
			this.visible = false;
		}
		else if (this.buttons.containsKey(button.id)) {
			this.parent.buttonEvent(button);
		}
	}

	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		if (!this.visible) {
			this.hovered = false;
			return;
		}
		this.hovered = this.isMouseHover(mouseX, mouseY, guiLeft, guiTop, xSize, ySize);
		if (this.point != null) {
			double xc = (double)guiLeft + (double)xSize / 2.0d, yc = (double)guiTop + (double)ySize / 2.0d;
			double dist = Math.sqrt((mouseY - yc) * (mouseY - yc) + (mouseX - xc) * (mouseX - xc));
			double base = Math.sqrt(Math.pow(xSize, 2.0d) + Math.pow(ySize, 2.0d)) / 2.0d;
			if (dist <= base * 2.0d) {
				double a = -1.0d / (2.0d * base - base);
				double b = -2.0d * a  * base;
				float alpha = (float) (a * dist + b);
				if (alpha < 0.0f) { alpha = 0.0f; } else if (alpha > 1.0f) { alpha = 1.0f; }
				int[] cr = point.getCenter();
				int color = this.colorLine + ((int) (alpha * 255.0f) << 24);
				this.parent.addLine(cr[0], cr[1], guiLeft + xSize / 2, guiTop + ySize / 2, color, 2);
			}
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (isMoving && Mouse.isButtonDown(0)) {
			int x = mouseX - mousePressX;
			int y = mouseY - mousePressY;
			if (x != 0 || y != 0) {
				this.moveOffset(x, y);
				mousePressX = mouseX;
				mousePressY = mouseY;
			}
		} else { isMoving = false; }
		if (this.hasSubGui() || !CustomNpcs.ShowDescriptions || this.hoverMiniWin) { return; }
		boolean foundHover = false;
		for (GuiButton b : this.buttonList) {
			if (b instanceof IComponentGui && b.isMouseOver()) {
				this.parent.setMiniHoverText(id, (IComponentGui) b);
				foundHover = true;
				break;
			}
		}
		if (!foundHover) {
			for (GuiCustomScroll s : scrolls.values()) {
				if (s.hovered) {
					this.parent.setMiniHoverText(id, s);
					foundHover = true;
					break;
				}
			}
		}
		if (!foundHover) {
			for (GuiNpcTextField t : textfields.values()) {
				if (t.isMouseOver()) {
					this.parent.setMiniHoverText(id, t);
					foundHover = true;
					break;
				}
			}
		}
	}
	
	public void postDrawBackground() {
		GuiNpcMiniWindow.drawTopRect(guiLeft + 3, guiTop + 3, guiLeft + xSize - 3, guiTop + 11, this.zLevel, this.colorLine + 0xF0000000, this.colorLine + 0x40000000);
		this.drawString(this.fontRenderer, this.title, guiLeft + 4, guiTop + 3, CustomNpcs.MainColor.getRGB());
	}

	public void moveOffset(int x, int y) {
		guiLeft += x;
		guiTop += y;
		for (GuiButton b : this.buttonList) { b.x += x; b.y += y; }
		for (GuiNpcLabel l : labels.values()) { l.x += x; l.y += y; }
		for (GuiCustomScroll s : scrolls.values()) { s.guiLeft += x; s.guiTop += y; }
		for (GuiNpcTextField t : textfields.values()) { t.x += x; t.y += y; }
	}

	public void keyTyped(char c, int i) {
		super.keyTyped(c, i);
	}

	public void mouseClicked(int mouseX, int mouseY, int mouseBottom) {
		if (!this.hovered) { return; }
		super.mouseClicked(mouseX, mouseY, mouseBottom);
		if (this.hovered && mouseBottom == 0 && this.isMouseHover(mouseX, mouseY, guiLeft + 3, guiTop + 3, xSize - 3, 8)) {
			mousePressX = mouseX;
			mousePressY = mouseY;
			isMoving = true;
		}
	}

	public void mouseEvent(int mouseX, int mouseY, int mouseBottom) {
		if (!this.hovered) { return; }
		super.mouseEvent(mouseX, mouseY, mouseBottom);
	}

	public void mouseReleased(int mouseX, int mouseY, int mouseBottom) {
		if (!this.hovered) { return; }
		super.mouseReleased(mouseX, mouseY, mouseBottom);
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int time, GuiCustomScroll scroll) {
		if (!this.hovered) { return; }
		if (this.scrolls.containsKey(scroll.id)) {
			this.parent.scrollClicked(mouseX, mouseY, time, scroll);
		}
	}

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {
		if (!this.hovered) { return; }
		if (this.scrolls.containsKey(scroll.id)) {
			this.parent.scrollDoubleClicked(select, scroll);
		}
	}

	@Override
	public void mouseDragged(GuiNpcSlider slider) {
		if (!this.hovered) { return; }
		if (this.sliders.containsKey(slider.id)) {
			this.parent.mouseDragged(slider);
		}
	}

	@Override
	public void mousePressed(GuiNpcSlider slider) {
		if (!this.hovered) { return; }
		if (this.sliders.containsKey(slider.id)) {
			this.parent.mousePressed(slider);
		}
	}

	@Override
	public void mouseReleased(GuiNpcSlider slider) {
		if (!this.hovered) { return; }
		if (this.sliders.containsKey(slider.id)) {
			this.parent.mouseReleased(slider);
		}
	}

	@Override
	public void unFocused(GuiNpcTextField textField) {
		if (this.textfields.containsKey(textField.getId())) {
			this.parent.unFocused(textField);
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
		
		bufferbuilder.pos((double)left, (double)top, (double)zLevel).color(f1, f2, f3, f).endVertex();
		bufferbuilder.pos((double)left, (double)bottom, (double)zLevel).color(f1, f2, f3, f).endVertex();
		bufferbuilder.pos((double)right, (double)bottom, (double)zLevel).color(f5, f6, f7, f4).endVertex();
		bufferbuilder.pos((double)right, (double)top, (double)zLevel).color(f5, f6, f7, f4).endVertex();
		
		tessellator.draw();
		GlStateManager.shadeModel(7424);
		GlStateManager.disableBlend();
		GlStateManager.enableAlpha();
		GlStateManager.enableTexture2D();
	}

	public void setPoint(IComponentGui point) {
		this.point = point;
	}

	@Override
	public int[] getCenter() {
		return new int[] { this.guiLeft + this.width / 2, this.guiTop + this.height / 2};
	}
	
	public void setColorLine(int color) {
		int red = color >> 16 & 255;
		int green = color >> 8 & 255;
		int blue = color & 255;
		this.colorLine = (red << 16) + (green << 8) + blue;
	}
	
	public int getColorLine() { return this.colorLine; }
	
	public void resetButtons() {
		GuiNpcButton exit = new GuiNpcButton(2500, guiLeft + xSize - 12, guiTop + 3, 8, 8, "X");
		exit.texture = SubGuiEditAnimation.btns;
		exit.hasDefBack = false;
		exit.txrX = 232;
		exit.txrW = 24;
		exit.txrH = 24;
		exit.layerColor = 0xFFFF0000;
		exit.textColor = 0xFF404040;
		this.addButton(exit);
	}
	
}
