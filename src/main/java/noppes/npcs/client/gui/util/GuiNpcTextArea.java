package noppes.npcs.client.gui.util;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.client.ClientProxy;

public class GuiNpcTextArea
extends GuiNpcTextField implements IGuiTextArea {

	private boolean clickVerticalBar = false;
	private int cursorCounter;
	private int cursorPosition = 0;
	private final ClientProxy.FontContainer font;
	private final int height;
	private int listHeight;
	private final int posX;
	private final int posY;
	private float scrolledY = 0.0f;
	private int startClick = -1;
	private final int width;
	private final boolean wrapLine = true;

	public GuiNpcTextArea(int id, GuiScreen guiscreen, int x, int y, int width, int height, String text) {
		super(id, guiscreen, x, y, width, height, text);
		posX = x;
		posY = y;
		this.width = width;
		this.height = height;
		listHeight = height;
		font = ClientProxy.Font.copy();
		font.useCustomFont = true;
		setMaxStringLength(Integer.MAX_VALUE);
		setFullText(text.replace("\r\n", "\n"));
	}

	private void addScrollY(int scrolled) {
		scrolledY -= 1.0f * scrolled / height;
		if (scrolledY < 0.0f) {
			scrolledY = 0.0f;
		}
		float max = 1.0f - 1.0f * (height + 2) / listHeight;
		if (scrolledY > max) {
			scrolledY = max;
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
		if (endX > posX + width) {
			endX = posX + width;
		}
		if (startX > posX + width) {
			startX = posX + width;
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

	public void drawString(String text, int x, int y, int color) {
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		font.drawString(text, x, y, color);
	}

	@Override
	public void render(IEditNPC gui, int mouseX, int mouseY, float partialTicks) {
		drawRect(posX - 1, posY - 1, posX + width + 1, posY + height + 1, new Color(0xA0, 0xA0, 0xA0, 0xFF).getRGB());
		drawRect(posX, posY, posX + width, posY + height, new Color(0x00, 0x00, 0x00, 0xFF).getRGB());
		int color = new Color(0xE0, 0xE0, 0xE0, 0xFF).getRGB();
		boolean flag = isFocused() && cursorCounter / 6 % 2 == 0;
		int startLine = getStartLineY();
		int maxLine = height / font.height(getFullText()) + startLine;
		List<String> lines = getLines();
		int charCount = 0;
		int lineCount = 0;
		int maxSize = width - (isScrolling() ? 14 : 4);
        for (String wholeLine : lines) {
            String line = "";
            for (char c : wholeLine.toCharArray()) {
                if (font.width(line + c) > maxSize && wrapLine) {
                    if (lineCount >= startLine && lineCount < maxLine) {
                        drawString(line, posX + 4, posY + 4 + (lineCount - startLine) * font.height(line), color);
                    }
                    line = "";
                    ++lineCount;
                }
                if (flag && charCount == cursorPosition && lineCount >= startLine && lineCount < maxLine
                        && canEdit) {
                    int xx = posX + font.width(line) + 4;
                    int yy = posY + (lineCount - startLine) * font.height(line) + 4;
                    if (getFullText().length() == cursorPosition) {
                        font.drawString("_", xx, yy, color);
                    } else {
                        drawCursorVertical(xx, yy, xx + 1, yy + font.height(line));
                    }
                }
                ++charCount;
                line += c;
            }
            if (lineCount >= startLine && lineCount < maxLine) {
                drawString(line, posX + 4, posY + 4 + (lineCount - startLine) * font.height(line), color);
                if (flag && charCount == cursorPosition && canEdit) {
                    int xx2 = posX + font.width(line) + 4;
                    int yy2 = posY + (lineCount - startLine) * font.height(line) + 4;
                    if (getFullText().length() == cursorPosition) {
                        font.drawString("_", xx2, yy2, color);
                    } else {
                        drawCursorVertical(xx2, yy2, xx2 + 1, yy2 + font.height(line));
                    }
                }
            }
            ++lineCount;
            ++charCount;
        }
		if (isFocused()) {
			int dWheel = Mouse.getDWheel();
			if (dWheel != 0) { addScrollY(dWheel < 0 ? -10 : 10); }
		}
		if (Mouse.isButtonDown(0)) {
			if (clickVerticalBar) {
				if (startClick >= 0) {
					addScrollY(startClick - (mouseY - posY));
				}
				startClick = mouseY - posY;
			}
		} else {
			clickVerticalBar = false;
		}
		listHeight = lineCount * font.height(getFullText());
		drawVerticalScrollBar();
	}

	private void drawVerticalScrollBar() {
		if (listHeight <= height - 4) {
			return;
		}
		Minecraft.getMinecraft().getTextureManager().bindTexture(GuiCustomScroll.resource);
		int x = posX + width - 6;
		int y = (int) ((posY + scrolledY * height) + 2);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		int sbSize = getVerticalBarSize();
		drawTexturedModalRect(x, y, width, 9, 5, 1);
		for (int k = 0; k < sbSize; ++k) {
			drawTexturedModalRect(x, y + k, width, 10, 5, 1);
		}
		drawTexturedModalRect(x, y, width, 11, 5, 1);
	}

	private List<String> getLines() {
		List<String> list = new ArrayList<>();
		StringBuilder line = new StringBuilder();
		for (char c : getFullText().toCharArray()) {
			if (c == '\r' || c == '\n') {
				list.add(line.toString());
				line = new StringBuilder();
			} else {
				line.append(c);
			}
		}
		list.add(line.toString());
		return list;
	}

	private int getStartLineY() {
		if (!isScrolling()) {
			scrolledY = 0.0f;
		}
		return MathHelper.ceil(scrolledY * listHeight / font.height(getFullText()));
	}

	private int getVerticalBarSize() {
		return (int) (1.0f * height / listHeight * (height - 4));
	}

	private boolean hoverVerticalScrollBar(int x, int y) {
		return listHeight > height - 4 && (posY < y && posY + height > y && x < posX + width && x > posX + (width - 8));
	}

	private boolean isScrolling() {
		return listHeight > height - 4;
	}

	@Override
	public boolean mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		if (hoverVerticalScrollBar(i, j)) {
			clickVerticalBar = true;
			startClick = -1;
			return false;
		}
		if (k != 0 || !canEdit) {
			return false;
		}
		int x = i - posX;
		int y = (j - posY - 4) / font.height(getFullText()) + getStartLineY();
		cursorPosition = 0;
		List<String> lines = getLines();
		int charCount = 0;
		int lineCount = 0;
		int maxSize = width - (isScrolling() ? 14 : 4);
        for (String wholeLine : lines) {
            StringBuilder line = new StringBuilder();
            for (char c : wholeLine.toCharArray()) {
                cursorPosition = charCount;
                if (font.width(line.toString() + c) > maxSize && wrapLine) {
                    ++lineCount;
                    line = new StringBuilder();
                    if (y < lineCount) {
                        break;
                    }
                }
                if (lineCount == y && x <= font.width(line.toString() + c)) {
                    return true;
                }
                ++charCount;
                line.append(c);
            }
            cursorPosition = charCount;
            ++lineCount;
            ++charCount;
            if (y < lineCount) {
                break;
            }
        }
		if (y >= lineCount) {
			cursorPosition = getFullText().length();
		}
		return true;
	}

	@Override
	public boolean textboxKeyTyped(char c, int i) {
		if (!isFocused() || !canEdit) { return false; }
		String originalText = getFullText();
		setFullText(originalText);
		if (c == '\r' || c == '\n') {
			setFullText(originalText.substring(0, cursorPosition) + c + originalText.substring(cursorPosition));
		}
		setCursorPositionZero();
		moveCursorBy(cursorPosition);
		boolean bo = super.textboxKeyTyped(c, i);
		String newText = getFullText();
		if (i != 211) {
			cursorPosition += newText.length() - originalText.length();
		}
		if (i == 203 && cursorPosition > 0) {
			--cursorPosition;
		}
		if (i == 205 && cursorPosition < newText.length()) {
			++cursorPosition;
		}
		return bo;
	}

	public void updateCursorCounter() {
		++cursorCounter;
	}

}
