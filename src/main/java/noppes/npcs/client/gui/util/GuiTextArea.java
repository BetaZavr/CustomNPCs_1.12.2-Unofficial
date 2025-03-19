package noppes.npcs.client.gui.util;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextComponentTranslation;
import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.config.TrueTypeFont;

public class GuiTextArea
extends Gui
implements IComponentGui, IKeyListener, IMouseListener, IGuiTextArea {

	private static TrueTypeFont font;

	static {
		GuiTextArea.font = new TrueTypeFont(new Font(CustomNpcs.FontType, Font.PLAIN, CustomNpcs.FontSize), 1.0f);
	}

	public int id;
	public int x;
	public int y;
	public int width;
	public int height;
	private int cursorCounter;
	private ITextChangeListener listener;
	public String text = null;
	private TextContainer container = null;
	public boolean active = false;
	public boolean enabled = true;
	public boolean visible = true;
	public boolean clicked = false;
	public boolean doubleClicked = false;
	public boolean clickScrolling = false;
	private int startSelection;
	private int endSelection;
	private int cursorPosition;
	private int scrolledLine = 0;
	private boolean enableCodeHighlighting = false;
	public List<UndoData> undoList = new ArrayList<>();
	public List<UndoData> redoList = new ArrayList<>();
	public boolean undoing;
	public boolean hovered = false;
	public boolean freeze = false;
	private long lastClicked = 0L;
	public int errorLine = -1;
	private final List<String> hoverText = new ArrayList<>();

	public GuiTextArea(int id, int x, int y, int width, int height, String label) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		undoing = true;
		setText(label);
		undoing = false;
		GuiTextArea.font.setSpecial('\uffff');
	}

	private void addText(String s) {
		setText(getSelectionBeforeText() + s + getSelectionAfterText());
		int endSel = startSelection + s.length();
		cursorPosition = endSel;
		startSelection = endSel;
		endSelection = endSel;
	}

	private int cursorDown() {
		for (int i = 0; i < container.lines.size(); ++i) {
			LineData data = container.lines.get(i);
			if (cursorPosition >= data.start && cursorPosition < data.end) {
				return getSelectionPos(x + 1 + GuiTextArea.font.width(data.text.substring(0, cursorPosition - data.start)), y + 1 + (i + 1 - scrolledLine) * container.lineHeight);
			}
		}
		return text.length();
	}

	private int cursorUp() {
		int i = 0;
		while (i < container.lines.size()) {
			LineData data = container.lines.get(i);
			if (cursorPosition >= data.start && cursorPosition < data.end) {
				if (i == 0) {
					return 0;
				}
				return getSelectionPos(x + 1 + GuiTextArea.font.width(data.text.substring(0, cursorPosition - data.start)), y + 1 + (i - 1 - scrolledLine) * container.lineHeight);
			} else {
				++i;
			}
		}
		return 0;
	}

	@Override
	public void render(IEditNPC gui, int xMouse, int yMouse, float partialTicks) {
		if (!visible) {
			return;
		}
		hovered = xMouse >= x && xMouse <= x + width && yMouse >= y && yMouse <= y + height;
		if (hovered && !gui.hasSubGui() && !hoverText.isEmpty()) { gui.setHoverText(hoverText); }
		drawRect(x - 1, y - 1, x + width + 1, y + height + 1, new Color(0xFFA0A0A0).getRGB());
		drawRect(x, y, x + width, y + height, new Color(0xFF000000).getRGB());
		container.visibleLines = height / container.lineHeight;
		if (!freeze) {
			if (clicked) {
				clicked = Mouse.isButtonDown(0);
				int i = getSelectionPos(xMouse, yMouse);
				if (i != cursorPosition) {
					if (doubleClicked) {
						endSelection = cursorPosition;
						startSelection = cursorPosition;
						doubleClicked = false;
					}
					setCursor(i, true);
				}
			} else if (doubleClicked) {
				doubleClicked = false;
			}
			if (clickScrolling) {
				clickScrolling = Mouse.isButtonDown(0);
				int diff = container.linesCount - container.visibleLines;
				scrolledLine = (int) Math.min(Math.max((1.0f * diff * (yMouse - y) / height), 0), diff);
			}
		}
		int startBracket = 0;
		int endBracket = 0;
		if (endSelection - startSelection == 1 || (startSelection == endSelection && startSelection < text.length())) {
			final int found = getFound();
			if (found != 0) {
				startBracket = startSelection;
				endBracket = startSelection + found;
			}
		}
		List<LineData> list = new ArrayList<>(container.lines);
		String wordHeightLight = null;
		if (startSelection != endSelection) {
			Matcher m = container.regexWord.matcher(text);
			while (m.find()) {
				if (m.start() == startSelection && m.end() == endSelection) {
					wordHeightLight = text.substring(startSelection, endSelection);
				}
			}
		}
		if (errorLine >= 0) {
			if (errorLine >= list.size()) {
				errorLine = list.size() - 1;
			}
			if (errorLine >= scrolledLine && scrolledLine < (scrolledLine + container.visibleLines)) {
				int posY = y + 1 + (errorLine - scrolledLine) * container.lineHeight;
				drawRect(x + 1, posY, x + width - 1, posY + container.lineHeight + 1, 0x99CC0000);
			}
		}

		GlStateManager.pushMatrix();
		GlStateManager.translate(0.0f, 0.0f, 1.0f);
		for (int j = 0; j < list.size(); ++j) {
			LineData data = list.get(j);
			String line = data.text;
			int w = line.length();
			if (startBracket != endBracket) {
				if (startBracket >= data.start && startBracket < data.end) {
					int s = GuiTextArea.font.width(line.substring(0, startBracket - data.start));
					int e = GuiTextArea.font.width(line.substring(0, startBracket - data.start + 1)) + 1;
					int posY = y + 1 + (j - scrolledLine) * container.lineHeight;
					drawRect(x + 1 + s, posY, x + 1 + e, posY + container.lineHeight + 1, -1728001024);
				}
				if (endBracket >= data.start && endBracket < data.end) {
					int s = GuiTextArea.font.width(line.substring(0, endBracket - data.start));
					int e = GuiTextArea.font.width(line.substring(0, endBracket - data.start + 1)) + 1;
					int posY = y + 1 + (j - scrolledLine) * container.lineHeight;
					drawRect(x + 1 + s, posY, x + 1 + e, posY + container.lineHeight + 1, -1728001024);
				}
			}
			if (j >= scrolledLine && j < scrolledLine + container.visibleLines) {
				if (wordHeightLight != null) {
					Matcher k = container.regexWord.matcher(line);
					while (k.find()) {
						if (line.substring(k.start(), k.end()).equals(wordHeightLight)) {
							int s2 = GuiTextArea.font.width(line.substring(0, k.start()));
							int e2 = GuiTextArea.font.width(line.substring(0, k.end())) + 1;
							int posY2 = y + 1 + (j - scrolledLine) * container.lineHeight;
							drawRect(x + 1 + s2, posY2, x + 1 + e2, posY2 + container.lineHeight + 1, 0x99004C00);
						}
					}
				}
				if (j != errorLine && startSelection != endSelection && endSelection > data.start && startSelection <= data.end && startSelection < data.end) {
					int s = GuiTextArea.font.width(line.substring(0, Math.max(startSelection - data.start, 0)));
					int e = GuiTextArea.font.width(line.substring(0, Math.min(endSelection - data.start, w))) + 1;
					int posY = y + 1 + (j - scrolledLine) * container.lineHeight;
					drawRect(x + 1 + s, posY, x + 1 + e, posY + container.lineHeight + 1, 0x990000FF);
				}
				int yPos = y + (j - scrolledLine) * container.lineHeight + 1;
				GuiTextArea.font.draw(data.getFormattedString(container.makeup), (x + 1), yPos, 0xFFE0E0E0); // draw text
				if (active && isEnabled() && cursorCounter / 6 % 2 == 0 && cursorPosition >= data.start && cursorPosition < data.end) {
					int posX = x + GuiTextArea.font.width(line.substring(0, cursorPosition - data.start));
					drawRect(posX + 1, yPos, posX + 2, yPos + 1 + container.lineHeight, 0xFFD0D0D0);
				}
			}
		}
		GlStateManager.popMatrix();

		if (hasVerticalScrollbar()) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(GuiCustomScroll.resource);
			int sbSize = (int) Math.max((1.0f * container.visibleLines / container.linesCount * height), 2);
			int posX2 = x + width - 6;
			int posY3 = (int) ((y + 1.0f * scrolledLine / container.linesCount * (height - 4)) + 1);
			drawRect(posX2, posY3, posX2 + 5, posY3 + sbSize, 0xFFE0E0E0);
		}
	}

	private int getFound() {
		char c = text.charAt(startSelection);
		int found = 0;
		if (c == '{') {
			found = findClosingBracket(text.substring(startSelection), '{', '}');
		} else if (c == '[') {
			found = findClosingBracket(text.substring(startSelection), '[', ']');
		} else if (c == '(') {
			found = findClosingBracket(text.substring(startSelection), '(', ')');
		} else if (c == '}') {
			found = findOpeningBracket(text.substring(0, startSelection + 1), '{', '}');
		} else if (c == ']') {
			found = findOpeningBracket(text.substring(0, startSelection + 1), '[', ']');
		} else if (c == ')') {
			found = findOpeningBracket(text.substring(0, startSelection + 1), '(', ')');
		}
		return found;
	}

	public void enableCodeHighlighting() {
		enableCodeHighlighting = true;
		container.formatCodeText();
	}

	private int findClosingBracket(String str, char s, char e) {
		int found = 0;
		char[] chars = str.toCharArray();
		for (int i = 0; i < chars.length; ++i) {
			char c = chars[i];
			if (c == s) {
				++found;
			} else if (c == e && --found == 0) {
				return i;
			}
		}
		return 0;
	}

	private int findOpeningBracket(String str, char s, char e) {
		int found = 0;
		char[] chars = str.toCharArray();
		for (int i = chars.length - 1; i >= 0; --i) {
			char c = chars[i];
			if (c == e) {
				++found;
			} else if (c == s && --found == 0) {
				return i - chars.length + 1;
			}
		}
		return 0;
	}

	private String getIndentCurrentLine() {
		for (LineData data : container.lines) {
			if (cursorPosition > data.start && cursorPosition <= data.end) {
				int i = 0;
				while (i < data.text.length() && data.text.charAt(i) == ' ') { ++i; }
				return data.text.substring(0, i);
			}
		}
		return "";
	}

	public String getSelectionAfterText() {
		return text.substring(endSelection);
	}

	public String getSelectionBeforeText() {
		if (startSelection == 0) {
			return "";
		}
		return text.substring(0, startSelection);
	}

	private int getSelectionPos(int xMouse, int yMouse) {
		xMouse -= x + 1;
		yMouse -= y + 1;
		List<LineData> list = new ArrayList<>(container.lines);
		for (int i = 0; i < list.size(); ++i) {
			LineData data = list.get(i);
			if (i >= scrolledLine && i < scrolledLine + container.visibleLines) {
				int yPos = (i - scrolledLine) * container.lineHeight;
				if (yMouse >= yPos && yMouse < yPos + container.lineHeight) {
					int lineWidth = 0;
					char[] chars = data.text.toCharArray();
					for (int j = 1; j <= chars.length; ++j) {
						int w = GuiTextArea.font.width(data.text.substring(0, j));
						if (xMouse < lineWidth + (w - lineWidth) / 2) {
							return data.start + j - 1;
						}
						lineWidth = w;
					}
					return data.end - 1;
				}
			}
		}
		return container.text.length();
	}

	@Override
	public void setFocused(boolean bo) { }

	@Override
	public String getText() {
		return text;
	}

	@Override
	public void unFocused() {  }

	public boolean hasVerticalScrollbar() {
		return container.visibleLines < container.linesCount;
	}

	public boolean isActive() {
		return active;
	}

	@Override
	public boolean isEnabled() {
		return enabled && visible;
	}

	@Override
	public void setEnabled(boolean bo) { enabled = bo; }

	@Override
	public boolean isMouseOver() {
		return false;
	}

	@Override
	public void keyTyped(char c, int i) {
		if (!active) {
			return;
		}
		if (GuiScreen.isKeyComboCtrlA(i)) {
			int n = 0;
			cursorPosition = n;
			startSelection = n;
			endSelection = text.length();
			return;
		}
		if (!isEnabled()) {
			return;
		}
		if (i == 203) {
			int j = 1;
			if (GuiScreen.isCtrlKeyDown()) {
				Matcher m = container.regexWord.matcher(text.substring(0, cursorPosition));
				while (m.find()) {
					if (m.start() == m.end()) {
						continue;
					}
					j = cursorPosition - m.start();
				}
			}
			setCursor(cursorPosition - j, GuiScreen.isShiftKeyDown());
			return;
		}
		if (i == 205) {
			int j = 1;
			if (GuiScreen.isCtrlKeyDown()) {
				Matcher m = container.regexWord.matcher(text.substring(cursorPosition));
				if ((m.find() && m.start() > 0) || m.find()) {
					j = m.start();
				}
			}
			setCursor(cursorPosition + j, GuiScreen.isShiftKeyDown());
			return;
		}
		if (i == 200) {
			setCursor(cursorUp(), GuiScreen.isShiftKeyDown());
			return;
		}
		if (i == 208) {
			setCursor(cursorDown(), GuiScreen.isShiftKeyDown());
			return;
		}
		if (i == 211) {
			String s = getSelectionAfterText();
			if (!s.isEmpty() && startSelection == endSelection) {
				s = s.substring(1);
			}
			setText(getSelectionBeforeText() + s);
			cursorPosition = startSelection;
			endSelection = startSelection;
			return;
		}
		if (i == 14) {
			String s = getSelectionBeforeText();
			if (startSelection > 0 && startSelection == endSelection) {
				s = s.substring(0, s.length() - 1);
				--startSelection;
			}
			setText(s + getSelectionAfterText());
			cursorPosition = startSelection;
			endSelection = startSelection;
			return;
		}
		if (GuiScreen.isKeyComboCtrlX(i)) {
			if (startSelection != endSelection) {
				NoppesStringUtils.setClipboardContents(text.substring(startSelection, endSelection));
				String s = getSelectionBeforeText();
				setText(s + getSelectionAfterText());
				int length = s.length();
				cursorPosition = length;
				startSelection = length;
				endSelection = length;
			}
			return;
		}
		if (GuiScreen.isKeyComboCtrlC(i)) {
			if (startSelection != endSelection) {
				NoppesStringUtils.setClipboardContents(text.substring(startSelection, endSelection));
			}
			return;
		}
		if (GuiScreen.isKeyComboCtrlV(i)) {
			addText(NoppesStringUtils.getClipboardContents());
			return;
		}
		if (i == 44 && GuiScreen.isCtrlKeyDown()) {
			if (undoList.isEmpty()) {
				return;
			}
			undoing = true;
			redoList.add(new UndoData(text, cursorPosition));
			UndoData data = undoList.remove(undoList.size() - 1);
			setText(data.text);
			cursorPosition = data.cursorPosition;
			startSelection = data.cursorPosition;
			endSelection = data.cursorPosition;
        } else {
			if (i != 21 || !GuiScreen.isCtrlKeyDown()) {
				if (i == 15) {
					addText("	");
				}
				if (i == 28) {
					addText('\n' + getIndentCurrentLine());
				}
				if (ChatAllowedCharacters.isAllowedCharacter(c)) {
					addText(Character.toString(c));
				}
				return;
			}
			if (redoList.isEmpty()) {
				return;
			}
			undoing = true;
			undoList.add(new UndoData(text, cursorPosition));
			UndoData data = redoList.remove(redoList.size() - 1);
			setText(data.text);
			cursorPosition = data.cursorPosition;
			startSelection = data.cursorPosition;
			endSelection = data.cursorPosition;
        }
        undoing = false;
    }

	@Override
	public boolean mouseClicked(int xMouse, int yMouse, int mouseButton) {
		if (freeze) {
			return false;
		}
		active = (xMouse >= x && xMouse < x + width && yMouse >= y && yMouse < y + height);
		if (active) {
			int selectionPos = getSelectionPos(xMouse, yMouse);
			cursorPosition = selectionPos;
			endSelection = selectionPos;
			startSelection = selectionPos;
			clicked = (mouseButton == 0);
			doubleClicked = false;
			long time = System.currentTimeMillis();
			if (clicked && container.linesCount * container.lineHeight > height && xMouse > x + width - 8) {
				clicked = false;
				clickScrolling = true;
			} else if (time - lastClicked < 500L) {
				doubleClicked = true;
				Matcher m = container.regexWord.matcher(text);
				while (m.find()) {
					if (cursorPosition > m.start() && cursorPosition < m.end()) {
						startSelection = m.start();
						endSelection = m.end();
						break;
					}
				}
			}
			lastClicked = time;
		}
		return active;
	}

	private void setCursor(int i, boolean select) {
		i = Math.min(Math.max(i, 0), text.length());
		if (i == cursorPosition) {
			return;
		}
		if (!select) {
            cursorPosition = i;
			startSelection = i;
			endSelection = i;
			return;
		}
		int diff = cursorPosition - i;
		if (cursorPosition == startSelection) {
			startSelection -= diff;
		} else if (cursorPosition == endSelection) {
			endSelection -= diff;
		}
		if (startSelection > endSelection) {
			int j = endSelection;
			endSelection = startSelection;
			startSelection = j;
		}
		cursorPosition = i;
	}

	public void setListener(ITextChangeListener gui) {
		listener = gui;
	}

	@Override
	public void setText(String newText) {
		newText = newText.replace("\r", "");
		if (text != null && text.equals(newText)) {
			return;
		}
		if (listener != null) {
			listener.textUpdate(newText);
		}
		if (!undoing) {
			undoList.add(new UndoData(text, cursorPosition));
			redoList.clear();
		}
		text = newText;
		container = new TextContainer(newText);
		container.init(GuiTextArea.font, width, height);
		if (enableCodeHighlighting) {
			container.formatCodeText();
		}
		if (scrolledLine > container.linesCount - container.visibleLines) {
			scrolledLine = Math.max(0, container.linesCount - container.visibleLines);
		}
	}

	@Override
	public int getInteger() { return 0; }

	@Override
	public long getLong() { return 0; }

	@Override
	public double getDouble() { return 0; }

	@Override
	public boolean isDouble() { return false; }

	@Override
	public boolean isEmpty() { return false; }

	@Override
	public void setTextColor(int color) { }

	@Override
	public void setDisabledTextColour(int color) { }

	@Override
	public boolean isFocused() { return false; }

	@Override
	public long getDefault() { return 0; }

	@Override
	public double getDoubleDefault() { return 0; }

	@Override
	public boolean isInteger() { return false; }

	@Override
	public boolean isLong() { return false; }

	@Override
	public void setMinMaxDefault(long minValue, long maxValue, long defaultValue) { }

	@Override
	public boolean isLatinAlphabetOnly() { return false; }

	@Override
	public void setLatinAlphabetOnly(boolean latinAlphabetOnly) { }

	@Override
	public void setMinMaxDoubleDefault(double minValue, double maxValue, double defaultValue) { }

	@Override
	public boolean isAllowUppercase() { return false; }

	@Override
	public void setAllowUppercase(boolean allowUppercase) { }

	@Override
	public long getMax() { return 0; }

	@Override
	public long getMin() { return 0; }

	@Override
	public double getDoubleMax() { return 0; }

	@Override
	public double getDoubleMin() { return 0; }

	@Override
	public int getID() { return id; }

	@Override
	public int[] getCenter() { return new int[] { x + width / 2, y + height  / 2}; }

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
	public int getLeft() { return x; }

	@Override
	public int getTop() { return y; }

	@Override
	public void setLeft(int left) { x = left; }

	@Override
	public void setTop(int top) { y = top; }

	@Override
	public int getWidth() { return width; }

	@Override
	public int getHeight() { return height; }

	@Override
	public void customKeyTyped(char c, int id) { keyTyped(c, id); }

	@Override
	public void customMouseClicked(int mouseX, int mouseY, int mouseButton) { mouseClicked(mouseX, mouseY, mouseButton); }

	@Override
	public void customMouseReleased(int mouseX, int mouseY, int mouseButton) { }

	@Override
	public boolean isVisible() { return visible; }

	@Override
	public void setVisible(boolean bo) { visible = bo; }

	public void updateScreen() {
		++cursorCounter;
		if (freeze) { return; }
		int dWheel = Mouse.getDWheel();
		if (dWheel != 0) {
			scrolledLine += (dWheel > 0 ? -1 : 1);
			scrolledLine = Math.max(Math.min(scrolledLine, container.linesCount - height / container.lineHeight), 0);
		}
	}

}
