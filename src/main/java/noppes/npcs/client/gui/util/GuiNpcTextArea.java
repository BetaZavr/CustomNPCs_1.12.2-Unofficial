package noppes.npcs.client.gui.util;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.client.ClientProxy;

public class GuiNpcTextArea
extends GuiNpcTextField {
	
	private boolean clickVerticalBar;
	private int cursorCounter;
	private int cursorPosition;
	private ClientProxy.FontContainer font;
	private int height;
	public boolean inMenu;
	private int listHeight;
	public boolean numbersOnly;
	private int posX;
	private int posY;
	private float scrolledY;
	private int startClick;
	private int width;
	private boolean wrapLine;

	public GuiNpcTextArea(int id, GuiScreen guiscreen, int i, int j, int k, int l, String s) {
		super(id, guiscreen, i, j, k, l, s);
		this.inMenu = true;
		this.numbersOnly = false;
		this.cursorPosition = 0;
		this.scrolledY = 0.0f;
		this.startClick = -1;
		this.clickVerticalBar = false;
		this.wrapLine = true;
		this.posX = i;
		this.posY = j;
		this.width = k;
		this.height = l;
		this.listHeight = l;
		this.font = ClientProxy.Font.copy();
		this.font.useCustomFont = true;
		this.setMaxStringLength(Integer.MAX_VALUE);
		this.setText(s.replace("\r\n", "\n"));
	}

	private void addScrollY(int scrolled) {
		this.scrolledY -= 1.0f * scrolled / this.height;
		if (this.scrolledY < 0.0f) {
			this.scrolledY = 0.0f;
		}
		float max = 1.0f - 1.0f * (this.height + 2) / this.listHeight;
		if (this.scrolledY > max) {
			this.scrolledY = max;
		}
	}

	private void drawCursorVertical(int startX, int startY, int endX, int endY) {
		if (startX < endX) {
			int i1 = startX;
			startX = endX;
			endX = i1;
		}
		if (startY < endY) {
			int i1 = startY;
			startY = endY;
			endY = i1;
		}
		if (endX > this.posX + this.width) {
			endX = this.posX + this.width;
		}
		if (startX > this.posX + this.width) {
			startX = this.posX + this.width;
		}
		BufferBuilder tessellator = Tessellator.getInstance().getBuffer();
		GlStateManager.color(0.0f, 0.0f, 255.0f, 255.0f);
		GlStateManager.disableTexture2D();
		GlStateManager.enableColorLogic();
		GlStateManager.colorLogicOp(5387);
		tessellator.begin(7, DefaultVertexFormats.POSITION);
		tessellator.pos(startX, endY, 0.0).endVertex();
		tessellator.pos(endX, endY, 0.0).endVertex();
		tessellator.pos(endX, startY, 0.0).endVertex();
		tessellator.pos(startX, startY, 0.0).endVertex();
		Tessellator.getInstance().draw();
		GlStateManager.disableColorLogic();
		GlStateManager.enableTexture2D();
	}

	public void drawString(FontRenderer fontRendererIn, String text, int x, int y, int color) {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.font.drawString(text, x, y, color);
	}

	@Override
	public void drawTextBox(int mouseX, int mouseY) {
		drawRect(this.posX - 1, this.posY - 1, this.posX + this.width + 1, this.posY + this.height + 1, -6250336);
		drawRect(this.posX, this.posY, this.posX + this.width, this.posY + this.height, -16777216);
		int color = 0xE0E0E0;
		boolean flag = this.isFocused() && this.cursorCounter / 6 % 2 == 0;
		int startLine = this.getStartLineY();
		int maxLine = this.height / this.font.height(this.getText()) + startLine;
		List<String> lines = this.getLines();
		int charCount = 0;
		int lineCount = 0;
		int maxSize = this.width - (this.isScrolling() ? 14 : 4);
		for (int i = 0; i < lines.size(); ++i) {
			String wholeLine = lines.get(i);
			String line = "";
			for (char c : wholeLine.toCharArray()) {
				if (this.font.width(line + c) > maxSize && this.wrapLine) {
					if (lineCount >= startLine && lineCount < maxLine) {
						this.drawString(null, line, this.posX + 4,
								this.posY + 4 + (lineCount - startLine) * this.font.height(line), color);
					}
					line = "";
					++lineCount;
				}
				if (flag && charCount == this.cursorPosition && lineCount >= startLine && lineCount < maxLine
						&& this.canEdit) {
					int xx = this.posX + this.font.width(line) + 4;
					int yy = this.posY + (lineCount - startLine) * this.font.height(line) + 4;
					if (this.getText().length() == this.cursorPosition) {
						this.font.drawString("_", xx, yy, color);
					} else {
						this.drawCursorVertical(xx, yy, xx + 1, yy + this.font.height(line));
					}
				}
				++charCount;
				line += c;
			}
			if (lineCount >= startLine && lineCount < maxLine) {
				this.drawString(null, line, this.posX + 4,
						this.posY + 4 + (lineCount - startLine) * this.font.height(line), color);
				if (flag && charCount == this.cursorPosition && this.canEdit) {
					int xx2 = this.posX + this.font.width(line) + 4;
					int yy2 = this.posY + (lineCount - startLine) * this.font.height(line) + 4;
					if (this.getText().length() == this.cursorPosition) {
						this.font.drawString("_", xx2, yy2, color);
					} else {
						this.drawCursorVertical(xx2, yy2, xx2 + 1, yy2 + this.font.height(line));
					}
				}
			}
			++lineCount;
			++charCount;
		}
		int k2 = Mouse.getDWheel();
		if (k2 != 0 && this.isFocused()) {
			this.addScrollY((k2 < 0) ? -10 : 10);
		}
		if (Mouse.isButtonDown(0)) {
			if (this.clickVerticalBar) {
				if (this.startClick >= 0) {
					this.addScrollY(this.startClick - (mouseY - this.posY));
				}
				if (this.hoverVerticalScrollBar(mouseX, mouseY)) {
					this.startClick = mouseY - this.posY;
				}
				this.startClick = mouseY - this.posY;
			}
		} else {
			this.clickVerticalBar = false;
		}
		this.listHeight = lineCount * this.font.height(this.getText());
		this.drawVerticalScrollBar();
	}

	private void drawVerticalScrollBar() {
		if (this.listHeight <= this.height - 4) {
			return;
		}
		Minecraft.getMinecraft().renderEngine.bindTexture(GuiCustomScroll.resource);
		int x = this.posX + this.width - 6;
		int y = (int) ((this.posY + this.scrolledY * this.height) + 2);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		int sbSize = this.getVerticalBarSize();
		this.drawTexturedModalRect(x, y, this.width, 9, 5, 1);
		for (int k = 0; k < sbSize; ++k) {
			this.drawTexturedModalRect(x, y + k, this.width, 10, 5, 1);
		}
		this.drawTexturedModalRect(x, y, this.width, 11, 5, 1);
	}

	private List<String> getLines() {
		List<String> list = new ArrayList<String>();
		String line = "";
		for (char c : this.getText().toCharArray()) {
			if (c == '\r' || c == '\n') {
				list.add(line);
				line = "";
			} else {
				line += c;
			}
		}
		list.add(line);
		return list;
	}

	private int getStartLineY() {
		if (!this.isScrolling()) {
			this.scrolledY = 0.0f;
		}
		return MathHelper.ceil(this.scrolledY * this.listHeight / this.font.height(this.getText()));
	}

	private int getVerticalBarSize() {
		return (int) (1.0f * this.height / this.listHeight * (this.height - 4));
	}

	private boolean hoverVerticalScrollBar(int x, int y) {
		return this.listHeight > this.height - 4 && (this.posY < y && this.posY + this.height > y
				&& x < this.posX + this.width && x > this.posX + (this.width - 8));
	}

	private boolean isScrolling() {
		return this.listHeight > this.height - 4;
	}

	@Override
	public boolean mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		if (this.hoverVerticalScrollBar(i, j)) {
			this.clickVerticalBar = true;
			this.startClick = -1;
			return false;
		}
		if (k != 0 || !this.canEdit) {
			return false;
		}
		int x = i - this.posX;
		int y = (j - this.posY - 4) / this.font.height(this.getText()) + this.getStartLineY();
		this.cursorPosition = 0;
		List<String> lines = this.getLines();
		int charCount = 0;
		int lineCount = 0;
		int maxSize = this.width - (this.isScrolling() ? 14 : 4);
		for (int g = 0; g < lines.size(); ++g) {
			String wholeLine = lines.get(g);
			String line = "";
			for (char c : wholeLine.toCharArray()) {
				this.cursorPosition = charCount;
				if (this.font.width(line + c) > maxSize && this.wrapLine) {
					++lineCount;
					line = "";
					if (y < lineCount) {
						break;
					}
				}
				if (lineCount == y && x <= this.font.width(line + c)) {
					return true;
				}
				++charCount;
				line += c;
			}
			this.cursorPosition = charCount;
			++lineCount;
			++charCount;
			if (y < lineCount) {
				break;
			}
		}
		if (y >= lineCount) {
			this.cursorPosition = this.getText().length();
		}
		return true;
	}

	@Override
	public boolean textboxKeyTyped(char c, int i) {
		if (this.isFocused() && this.canEdit) {
			String originalText = this.getText();
			this.setText(originalText);
			if (c == '\r' || c == '\n') {
				this.setText(originalText.substring(0, this.cursorPosition) + c
						+ originalText.substring(this.cursorPosition));
			}
			this.setCursorPositionZero();
			this.moveCursorBy(this.cursorPosition);
			boolean bo = super.textboxKeyTyped(c, i);
			String newText = this.getText();
			if (i != 211) {
				this.cursorPosition += newText.length() - originalText.length();
			}
			if (i == 203 && this.cursorPosition > 0) {
				--this.cursorPosition;
			}
			if (i == 205 && this.cursorPosition < newText.length()) {
				++this.cursorPosition;
			}
			return bo;
		}
		return false;
	}

	public void updateCursorCounter() {
		++this.cursorCounter;
	}

}
