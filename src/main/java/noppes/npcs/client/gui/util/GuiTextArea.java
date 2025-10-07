package noppes.npcs.client.gui.util;

import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.config.TrueTypeFont;

public class GuiTextArea extends Gui implements IComponentGui {

	protected static final TrueTypeFont font = new TrueTypeFont(new Font(CustomNpcs.FontType, Font.PLAIN, CustomNpcs.FontSize), 1.0f);

	protected TextContainer container = null;
	protected ITextChangeListener listener;
	protected final List<String> hoverText = new ArrayList<>();
	protected boolean enableCodeHighlighting = false;
	protected int cursorCounter;
	protected int startSelection;
	protected int endSelection;
	protected int cursorPosition;
	protected int scrolledLine = 0;
	protected long lastClicked = 0L;

	public int id;
	public int x;
	public int y;
	public int width;
	public int height;
	public String text = null;
	public boolean active = false;
	public boolean enabled = true;
	public boolean visible = true;
	public boolean clicked = false;
	public boolean doubleClicked = false;
	public boolean clickScrolling = false;
	public List<UndoData> undoList = new ArrayList<>();
	public List<UndoData> redoList = new ArrayList<>();
	public boolean undoing = true;
	public boolean freeze = false;
	public int errorLine = -1;

	public GuiTextArea(int idIn, int xIn, int yIn, int widthIn, int heightIn, String label) {
		id = idIn;
		x = xIn;
		y = yIn;
		width = widthIn;
		height = heightIn;
		setText(label);
		GuiTextArea.font.setSpecial('\uffff');
	}

	private void addText(String s) {
		if (s == null || s.isEmpty()) { return;}
		undoList.add(new UndoData(text, cursorPosition, startSelection, endSelection, scrolledLine));
		if (undoList.size() > 100) { undoList.remove(0); }
		setText(getSelectionBeforeText() + s + getSelectionAfterText());
		endSelection = startSelection + s.length();
		cursorPosition = endSelection;
		startSelection = endSelection;
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
				if (i == 0) { return 0; }
				return getSelectionPos(x + 1 + GuiTextArea.font.width(data.text.substring(0, cursorPosition - data.start)), y + 1 + (i - 1 - scrolledLine) * container.lineHeight);
			}
			else { ++i; }
		}
		return 0;
	}

	@Override
	public void render(IEditNPC gui, int xMouse, int yMouse, float partialTicks) {
		if (!visible) { return; }
		active = xMouse >= x && xMouse <= x + width && yMouse >= y && yMouse <= y + height;
		if (active && !gui.hasSubGui() && !hoverText.isEmpty()) { gui.putHoverText(hoverText); }
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
			}
			else if (doubleClicked) { doubleClicked = false; }
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
		try {
			String wordHeightLight = null;
			if (startSelection != endSelection) {
				Matcher m = container.regexWord.matcher(text);
				while (m.find()) {
					if (m.start() == startSelection && m.end() == endSelection) { wordHeightLight = text.substring(startSelection, endSelection); }
				}
			}
			List<LineData> list = new ArrayList<>(container.lines);
			if (errorLine >= 0) {
				if (errorLine >= list.size()) { errorLine = list.size() - 1; }
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

					if (active && enabled && cursorCounter / 6 % 2 == 0) {
						if (cursorPosition >= data.start && cursorPosition < data.end) {
							int posX = x + GuiTextArea.font.width(line.substring(0, cursorPosition - data.start));
							drawRect(posX + 1, yPos, posX + 2, yPos + 1 + container.lineHeight, 0xFFD0D0D0);
						}
						else if (j == list.size() - 1 && cursorPosition == text.length()) {
							yPos = y + (j - scrolledLine) * container.lineHeight + 1;
							drawRect(x + 1, yPos, x + 2, yPos + 1 + container.lineHeight, 0xFFD0D0D0);
						}
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
		catch (Exception ignored) { }
    }

	private int getFound() {
		char c = text.charAt(startSelection);
		int found = 0;
		if (c == '{') { found = findClosingBracket(text.substring(startSelection), '{', '}'); }
		else if (c == '[') { found = findClosingBracket(text.substring(startSelection), '[', ']'); }
		else if (c == '(') { found = findClosingBracket(text.substring(startSelection), '(', ')'); }
		else if (c == '}') { found = findOpeningBracket(text.substring(0, startSelection + 1), '{', '}'); }
		else if (c == ']') { found = findOpeningBracket(text.substring(0, startSelection + 1), '[', ']'); }
		else if (c == ')') { found = findOpeningBracket(text.substring(0, startSelection + 1), '(', ')'); }
		return found;
	}

	public void enableCodeHighlighting() {
		enableCodeHighlighting = true;
		container.setLighting(true);
	}

	private int findClosingBracket(String str, char s, char e) {
		int found = 0;
		char[] chars = str.toCharArray();
		for (int i = 0; i < chars.length; ++i) {
			char c = chars[i];
			if (c == s) { ++found; }
			else if (c == e && --found == 0) { return i; }
		}
		return 0;
	}

	private int findOpeningBracket(String str, char s, char e) {
		int found = 0;
		char[] chars = str.toCharArray();
		for (int i = chars.length - 1; i >= 0; --i) {
			char c = chars[i];
			if (c == e) { ++found; }
			else if (c == s && --found == 0) { return i - chars.length + 1; }
		}
		return 0;
	}

	public String getSelectionAfterText() { return text.substring(endSelection); }

	public String getSelectionBeforeText() {
		if (startSelection == 0) { return ""; }
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
						if (xMouse < lineWidth + (w - lineWidth) / 2) { return data.start + j - 1; }
						lineWidth = w;
					}
					return data.end - 1;
				}
			}
		}
		return container.text.length();
	}

	public String getText() { return text; }

	public boolean hasVerticalScrollbar() { return container.visibleLines < container.linesCount; }

	@Override
	public GuiTextArea setIsEnable(boolean bo) { enabled = bo; return this; }

	@Override
	public List<String> getHoversText() { return Collections.emptyList(); }

	@Override
	public boolean isHovered() { return active; }

	public boolean keyCnpcsPressed(char typedChar, int keyCode) {
		if (!active) { return false; }
		if (GuiScreen.isKeyComboCtrlA(keyCode)) {
			int n = 0;
			cursorPosition = n;
			startSelection = n;
			endSelection = text.length();
			return true;
		} // select all
		int j;
		if (keyCode == Keyboard.KEY_LEFT) {
			j = 1;
			if (GuiScreen.isCtrlKeyDown()) {
				Matcher m = container.regexWord.matcher(text.substring(0, cursorPosition));
				while (m.find()) {
					if (m.start() == m.end()) { continue; }
					j = cursorPosition - m.start();
				}
			}
			setCursor(cursorPosition - j, GuiScreen.isShiftKeyDown());
			return true;
		} // left arrow
		if (keyCode == Keyboard.KEY_RIGHT) {
			j = 1;
			if (GuiScreen.isCtrlKeyDown()) {
				Matcher m = container.regexWord.matcher(text.substring(cursorPosition));
				if ((m.find() && m.start() > 0) || m.find()) {
					j = m.start();
				}
			}
			setCursor(cursorPosition + j, GuiScreen.isShiftKeyDown());
			return true;
		}// right arrow
		if (keyCode == Keyboard.KEY_UP) {
			setCursor(cursorUp(), GuiScreen.isShiftKeyDown());
			return true;
		} // up arrow
		if (keyCode == Keyboard.KEY_DOWN) {
			setCursor(cursorDown(), GuiScreen.isShiftKeyDown());
			return true;
		} // down arrow
		String select;
		if (GuiScreen.isKeyComboCtrlX(keyCode)) {
			if (startSelection != endSelection) {
				NoppesStringUtils.setClipboardContents(text.substring(startSelection, endSelection));
				if (enabled) {
					select = getSelectionBeforeText();
					setText(select + getSelectionAfterText());
					cursorPosition = startSelection = endSelection = select.length();
				}
			}
			return true;
		} // cut
		if (GuiScreen.isKeyComboCtrlC(keyCode)) {
			if (startSelection != endSelection) {
				NoppesStringUtils.setClipboardContents(text.substring(startSelection, endSelection));
			}
			return true;
		} // copy
		if (!enabled) { return false; }
		if (keyCode == Keyboard.KEY_DELETE) {
			select = getSelectionAfterText();
			if (!select.isEmpty() && startSelection == endSelection) { select = select.substring(1); }
			setText(getSelectionBeforeText() + select);
			cursorPosition = startSelection;
			endSelection = startSelection;
			return true;
		} // delete
		if (keyCode == Keyboard.KEY_BACK) {
			select = getSelectionBeforeText();
			if (startSelection > 0 && startSelection == endSelection) {
				select = select.substring(0, select.length() - 1);
				--startSelection;
			}
			setText(select + getSelectionAfterText());
			cursorPosition = startSelection;
			endSelection = startSelection;
			return true;
		} // backspace
		if (GuiScreen.isKeyComboCtrlV(keyCode)) {
			addText(NoppesStringUtils.getClipboardContents());
			return true;
		} // parse
		if (keyCode == Keyboard.KEY_Z && GuiScreen.isCtrlKeyDown()) {
			if (!undoList.isEmpty()) {
				redoList.add(new UndoData(text, cursorPosition, startSelection, endSelection, scrolledLine));
				setUndoData(undoList.remove(undoList.size() - 1));
			}
			return true;
        } // undo (Ctrl+Z)
		if (keyCode == Keyboard.KEY_Y && GuiScreen.isCtrlKeyDown()) {
			if (!redoList.isEmpty()) {
				undoList.add(new UndoData(text, cursorPosition, startSelection, endSelection, scrolledLine));
				if (undoList.size() > 100) { undoList.remove(0); }
				setUndoData(redoList.remove(redoList.size() - 1));
			}
			return true;
		} // redo (Ctrl+Y)
		if (keyCode == Keyboard.KEY_TAB) { addText("\t"); } // Tab
		if (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER) {
			addText('\n' + getIndentCurrentLine());
		} // Enter
		if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) { addText(Character.toString(typedChar)); }
		return true;
    }

	private void setUndoData(UndoData data) {
		undoing = true;
		setText(data.text);
		undoing = false;
		cursorPosition = data.cursorPosition;
		startSelection = data.startSelection;
		endSelection = data.endSelection;
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

	@Override
	public boolean mouseCnpcsPressed(int mouseX, int mouseY, int mouseButton) {
		if (freeze) { return false; }
		active = (mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height);
		if (active) {
			int selectionPos = getSelectionPos(mouseX, mouseY);
			cursorPosition = selectionPos;
			endSelection = selectionPos;
			startSelection = selectionPos;
			clicked = (mouseButton == 0);
			doubleClicked = false;
			long time = System.currentTimeMillis();
			if (clicked && container.linesCount * container.lineHeight > height && mouseX > x + width - 8) {
				clicked = false;
				clickScrolling = true;
			}
			else if (time - lastClicked < 500L) {
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

	@Override
	public boolean mouseCnpcsReleased(int mouseX, int mouseY, int state) { return false; }

	private void setCursor(int i, boolean select) {
		i = Math.min(Math.max(i, 0), text.length());
		if (i == cursorPosition) { return; }
		if (!select) {
            cursorPosition = i;
			startSelection = i;
			endSelection = i;
			return;
		}
		int diff = cursorPosition - i;
		if (cursorPosition == startSelection) { startSelection -= diff; }
		else if (cursorPosition == endSelection) { endSelection -= diff; }
		if (startSelection > endSelection) {
			int j = endSelection;
			endSelection = startSelection;
			startSelection = j;
		}
		cursorPosition = i;
	}

	public GuiTextArea setListener(ITextChangeListener gui) { listener = gui; return this; }

	public void setText(String newText) {
		CustomNpcs.debugData.start(null);
		newText = newText.replace("\r", "");
		if (text != null && text.equals(newText)) { return; }
		if (listener != null) { listener.textUpdate(newText); }
		if (!undoing && container != null) { undoList.add(new UndoData(text, cursorPosition, startSelection, endSelection, scrolledLine)); }
		if (undoList.size() > 100) { undoList.remove(0); }
		text = newText;
		container = new TextContainer(newText, GuiTextArea.font, width, height, enableCodeHighlighting);
		container.init();
		if (scrolledLine > container.linesCount - container.visibleLines) { scrolledLine = Math.max(0, container.linesCount - container.visibleLines); }
		CustomNpcs.debugData.end(null);
	}

	public boolean isEmpty() { return text.isEmpty(); }

	@Override
	public int getID() { return id; }

	@Override
	public int[] getCenter() { return new int[] { x + width / 2, y + height  / 2}; }

	public GuiTextArea setHoverText(Object... components) {
		hoverText.clear();
		if (components == null) { return this; }
		noppes.npcs.util.Util.instance.putHovers(hoverText, components);
		return this;
	}

	@Override
	public GuiTextArea setIsVisible(boolean bo) { visible = bo; return this; }

	@Override
	public void moveTo(int addX, int addY) {
		x += addX;
		y += addY;
	}

	@Override
	public void updateCnpcsScreen() {
		++cursorCounter;
		if (freeze) { return; }
		int dWheel = Mouse.getDWheel();
		if (dWheel != 0) {
			scrolledLine += (dWheel > 0 ? -1 : 1);
			scrolledLine = Math.max(Math.min(scrolledLine, container.linesCount - height / container.lineHeight), 0);
		}
	}

}
