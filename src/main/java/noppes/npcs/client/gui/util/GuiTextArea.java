package noppes.npcs.client.gui.util;

import java.awt.Font;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.config.TrueTypeFont;
import noppes.npcs.util.AdditionalMethods;

public class GuiTextArea
extends Gui
implements IGui, IKeyListener, IMouseListener {
	
	class UndoData {
		public int cursorPosition;
		public String text;

		public UndoData(String text, int cursorPosition) {
			this.text = text;
			this.cursorPosition = cursorPosition;
		}
	}

	private static TrueTypeFont font = new TrueTypeFont(new Font("Arial Unicode MS", 0, CustomNpcs.FontSize), 1.0f);

	public static String filter = (""+(char) 9)+(""+(char) 10)+" .+-/*=()[]{}\"\\';"; // Tab, Enter ...
	public boolean active;
	public boolean clicked;
	public boolean clickScrolling;
	public TextContainer container;
	private int cursorCounter;
	private int cursorPosition = -1;
	public boolean doubleClicked;
	private boolean enableCodeHighlighting;
	public boolean enabled;
	private int endSelection;
	public int errorLine = -1;
	public boolean freeze;
	public int height;
	public boolean hovered;
	public int id;
	private long lastClicked;
	private ITextChangeListener listener;
	public List<UndoData> redoList;
	public int scrolledLine;
	private int startSelection;
	public String text;
	public boolean undoing;
	public List<UndoData> undoList;
	public boolean visible;
	public int width;
	public int x;
	public int y;
	public boolean onlyReading = false;

	public GuiTextArea(int id, int x, int y, int width, int height, String text) {
		this.text = null;
		this.container = null;
		this.active = false;
		this.enabled = true;
		this.visible = true;
		this.clicked = false;
		this.doubleClicked = false;
		this.clickScrolling = false;
		this.scrolledLine = 0;
		this.enableCodeHighlighting = false;
		this.undoList = new ArrayList<UndoData>();
		this.redoList = new ArrayList<UndoData>();
		this.undoing = false;
		this.lastClicked = 0L;
		this.id = id;
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
		this.undoing = true;
		this.setText(text);
		this.undoing = false;
		this.hovered = false;
		this.freeze = false;
		this.setIsCode(true);
	}

	private void addText(String s) {
		this.setText(this.getSelectionBeforeText() + s + this.getSelectionAfterText());
		int endSelection = this.startSelection + s.length();
		this.cursorPosition = endSelection;
		this.startSelection = endSelection;
		this.endSelection = endSelection;
	}

	private int cursorDown() {
		for (int i = 0; i < this.container.lines.size(); ++i) {
			TextContainer.LineData data = this.container.lines.get(i);
			if (this.cursorPosition >= data.start && this.cursorPosition < data.end) {
				return this.getSelectionPos( this.x + 1 + GuiTextArea.font.width(data.text.substring(0, this.cursorPosition - data.start)), this.y + 1 + (i + 1 - this.scrolledLine) * this.container.lineHeight);
			}
		}
		return this.text.length();
	}

	private int cursorUp() {
		int i = 0;
		while (i < this.container.lines.size()) {
			TextContainer.LineData data = this.container.lines.get(i);
			if (this.cursorPosition >= data.start && this.cursorPosition < data.end) {
				if (i == 0) {
					return 0;
				}
				return this.getSelectionPos(this.x + 1 + GuiTextArea.font.width(data.text.substring(0, this.cursorPosition - data.start)), this.y + 1 + (i - 1 - this.scrolledLine) * this.container.lineHeight);
			} else {
				++i;
			}
		}
		return 0;
	}

	public void drawScreen(int xMouse, int yMouse) {
		if (!this.visible) {
			return;
		}
		this.hovered = xMouse >= this.x && xMouse <= this.x + this.width && yMouse >= this.y
				&& yMouse <= this.y + this.height;
		drawRect(this.x - 1, this.y - 1, this.x + this.width + 1, this.y + this.height + 1, 0xFFA0A0A0);
		drawRect(this.x, this.y, this.x + this.width, this.y + this.height, 0xFF000000);
		if (this.container == null) {
			return;
		}
		this.container.visibleLines = this.height / this.container.lineHeight;
		if (!this.freeze) {
			if (this.clicked) {
				this.clicked = Mouse.isButtonDown(0);
				int i = this.getSelectionPos(xMouse, yMouse);
				if (i != this.cursorPosition) {
					if (this.doubleClicked) {
						int cursorPosition = this.cursorPosition;
						this.endSelection = cursorPosition;
						this.startSelection = cursorPosition;
						this.doubleClicked = false;
					}
					this.setCursor(i, true);
				}
			} else if (this.doubleClicked) {
				this.doubleClicked = false;
			}
			if (this.clickScrolling) {
				this.clickScrolling = Mouse.isButtonDown(0);
				int diff = this.container.linesCount - this.container.visibleLines;
				this.scrolledLine = (int) Math.min(Math.max((1.0f * diff * (yMouse - this.y) / this.height), 0), diff);
			}
		}

		int startBracket = 0;
		int endBracket = 0;
		if (this.startSelection >= 0 && this.endSelection - this.startSelection == 1
				|| (this.startSelection == this.endSelection && this.startSelection < this.text.length())) {
			char c = this.text.charAt(this.startSelection);
			int found = 0;
			if (c == '{') {
				found = this.findClosingBracket(this.text.substring(this.startSelection), '{', '}');
			} else if (c == '[') {
				found = this.findClosingBracket(this.text.substring(this.startSelection), '[', ']');
			} else if (c == '(') {
				found = this.findClosingBracket(this.text.substring(this.startSelection), '(', ')');
			} else if (c == '}') {
				found = this.findOpeningBracket(this.text.substring(0, this.startSelection + 1), '{', '}');
			} else if (c == ']') {
				found = this.findOpeningBracket(this.text.substring(0, this.startSelection + 1), '[', ']');
			} else if (c == ')') {
				found = this.findOpeningBracket(this.text.substring(0, this.startSelection + 1), '(', ')');
			}
			if (found != 0) {
				startBracket = this.startSelection;
				endBracket = this.startSelection + found;
			}
		}
		List<TextContainer.LineData> list = new ArrayList<TextContainer.LineData>(this.container.lines);
		String wordHightLight = null;
		if (this.startSelection != this.endSelection) {
			Matcher m = this.container.regexWord.matcher(this.text);
			while (m.find()) {
				if (m.start() == this.startSelection && m.end() == this.endSelection) {
					wordHightLight = this.text.substring(this.startSelection, this.endSelection);
				}
			}
		}
		if (this.errorLine >= 0) {
			if (this.errorLine >= list.size()) {
				this.errorLine = list.size() - 1;
			}
			if (this.errorLine >= this.scrolledLine && this.scrolledLine < (this.scrolledLine + this.container.visibleLines)) {
				int posY = this.y + 1 + (this.errorLine - this.scrolledLine) * this.container.lineHeight;
				drawRect(this.x + 1, posY, this.x + this.width - 1, posY + this.container.lineHeight + 1, 0x99CC0000);
			}
		}
		for (int j = 0; j < list.size(); ++j) {
			TextContainer.LineData data = list.get(j);
			String line = data.text;
			int w = line.length();
			if (startBracket != endBracket) {
				if (startBracket >= data.start && startBracket < data.end) {
					int s = GuiTextArea.font.width(line.substring(0, startBracket - data.start));
					int e = GuiTextArea.font.width(line.substring(0, startBracket - data.start + 1)) + 1;
					int posY = this.y + 1 + (j - this.scrolledLine) * this.container.lineHeight;
					drawRect(this.x + 1 + s, posY, this.x + 1 + e, posY + this.container.lineHeight + 1, 0x9900CC00);
				}
				if (endBracket >= data.start && endBracket < data.end) {
					int s = GuiTextArea.font.width(line.substring(0, endBracket - data.start));
					int e = GuiTextArea.font.width(line.substring(0, endBracket - data.start + 1)) + 1;
					int posY = this.y + 1 + (j - this.scrolledLine) * this.container.lineHeight;
					drawRect(this.x + 1 + s, posY, this.x + 1 + e, posY + this.container.lineHeight + 1, 0x9900CC00);
				}
			}
			if (j >= this.scrolledLine && j < this.scrolledLine + this.container.visibleLines) {
				if (wordHightLight != null) {
					Matcher k = this.container.regexWord.matcher(line);
					while (k.find()) {
						if (line.substring(k.start(), k.end()).equals(wordHightLight)) {
							int s2 = GuiTextArea.font.width(line.substring(0, k.start()));
							int e2 = GuiTextArea.font.width(line.substring(0, k.end())) + 1;
							int posY2 = this.y + 1 + (j - this.scrolledLine) * this.container.lineHeight;
							drawRect(this.x + 1 + s2, posY2, this.x + 1 + e2, posY2 + this.container.lineHeight + 1, 0x99004C00);
						}
					}
				}
				if (j != this.errorLine && this.startSelection != this.endSelection && this.endSelection > data.start && this.startSelection <= data.end && this.startSelection < data.end) {
					int s = GuiTextArea.font.width(line.substring(0, Math.max(this.startSelection - data.start, 0)));
					int e = GuiTextArea.font.width(line.substring(0, Math.min(this.endSelection - data.start, w))) + 1;
					int posY = this.y + 1 + (j - this.scrolledLine) * this.container.lineHeight;
					drawRect(this.x + 1 + s, posY, this.x + 1 + e, posY + this.container.lineHeight + 1, 0xFF000099);
				}
				int yPos = this.y + (j - this.scrolledLine) * this.container.lineHeight + 1;
				GuiTextArea.font.draw(data.getFormattedString(), (this.x + 1), yPos, 0xFFE0E0E0);
				if (this.active && this.isEnabled() && this.cursorCounter / 6 % 2 == 0 && this.cursorPosition >= data.start && this.cursorPosition < data.end) {
					int posX = this.x + GuiTextArea.font.width(line.substring(0, this.cursorPosition - data.start));
					drawRect(posX + 1, yPos, posX + 2, yPos + 1 + this.container.lineHeight, 0xFFD0D0D0);
				}
			}
		}
		if (this.hasVerticalScrollbar()) {
			Minecraft.getMinecraft().renderEngine.bindTexture(GuiCustomScroll.resource);
			int sbSize = (int) Math.max((1.0f * this.container.visibleLines / this.container.linesCount * this.height), 2);
			int posX2 = this.x + this.width - 6;
			int posY3 = (int) ((this.y + 1.0f * this.scrolledLine / this.container.linesCount * (this.height - 4)) + 1);
			drawRect(posX2, posY3, posX2 + 5, posY3 + sbSize, 0xFFE0E0E0);
		}
	}

	public void enableCodeHighlighting() {
		this.enableCodeHighlighting = true;
		this.container.formatCodeText();
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

	public int getCursorPosition() {
		return this.cursorPosition;
	}

	public int getID() {
		return this.id;
	}

	private String getIndentCurrentLine() {
		for (TextContainer.LineData data : this.container.lines) {
			if (this.cursorPosition > data.start && this.cursorPosition <= data.end) {
				int i;
				for (i = 0; i < data.text.length() && data.text.charAt(i) == ' '; ++i) {
				}
				return data.text.substring(0, i);
			}
		}
		return "";
	}

	public int getLineID() {
		List<TextContainer.LineData> list = new ArrayList<TextContainer.LineData>(this.container.lines);
		for (int i = 0; i < list.size(); i++) {
			TextContainer.LineData data = list.get(i);
			if (this.cursorPosition >= data.start && this.cursorPosition <= data.end) {
				return i;
			}
		}
		return -1;
	}

	public String getLineText() {
		List<TextContainer.LineData> list = new ArrayList<TextContainer.LineData>(this.container.lines);
		for (int i = 0; i < list.size(); i++) {
			TextContainer.LineData data = list.get(i);
			if (this.cursorPosition >= data.start && this.cursorPosition <= data.end) {
				return data.text;
			}
		}
		return "";
	}

	public String getSelectionAfterText() {
		return this.text.substring(this.endSelection);
	}

	public String getSelectionBeforeText() {
		if (this.startSelection == 0) {
			return "";
		}
		return this.text.substring(0, this.startSelection);
	}

	public int getSelectionPos(int xMouse, int yMouse) {
		xMouse -= this.x + 1;
		yMouse -= this.y + 1;
		List<TextContainer.LineData> list = new ArrayList<TextContainer.LineData>(this.container.lines);
		for (int i = 0; i < list.size(); ++i) {
			TextContainer.LineData data = list.get(i);
			if (i >= this.scrolledLine && i < this.scrolledLine + this.container.visibleLines) {
				int yPos = (i - this.scrolledLine) * this.container.lineHeight;
				if (yMouse >= yPos && yMouse < yPos + this.container.lineHeight) {
					int lineWidth = 0;
					String t = GuiTextArea.font.isCode() ? data.text : AdditionalMethods.deleteColor(data.text);
					char[] chars = t.toCharArray();
					for (int j = 1; j <= chars.length; ++j) {
						int w = GuiTextArea.font.width(t.substring(0, j));
						if (xMouse < lineWidth + (w - lineWidth) / 2) {
							return data.start + j - 1;
						}
						lineWidth = w;
					}
					return data.end - 1;
				}
			}
		}
		return this.container.text.length();
	}

	public Object[] getSelectionText(int xMouse, int yMouse) {
		if (!this.hovered || !this.visible) {
			return new Object[] { -1, "" };
		}
		int p = -1;
		TextContainer.LineData line = null;
		xMouse -= this.x + 1;
		yMouse -= this.y + 1;
		List<TextContainer.LineData> list = new ArrayList<TextContainer.LineData>(this.container.lines);
		int row = 0;
		String t = "";
		for (int i = 0; i < list.size(); ++i) {
			TextContainer.LineData data = list.get(i);
			if (i >= this.scrolledLine && i < this.scrolledLine + this.container.visibleLines) {
				int yPos = (i - this.scrolledLine) * this.container.lineHeight;
				if (yMouse >= yPos && yMouse < yPos + this.container.lineHeight) {
					int lineWidth = 0;
					t = GuiTextArea.font.isCode() ? data.text : AdditionalMethods.deleteColor(data.text);
					char[] chars = t.toCharArray();
					boolean found = false;
					for (int j = 1; j <= chars.length; ++j) {
						int w = GuiTextArea.font.width(t.substring(0, j));
						if (xMouse < lineWidth + (w - lineWidth) / 2) {
							p = data.start + j - 1;
							line = data;
							row = i;
							found = true;
							break;
						}
						lineWidth = w;
					}
					if (!found) {
						p = data.end - 1;
						line = data;
						row = i;
					}
					break;
				}
			}
		}
		if (line == null || p == -1) { return new Object[] { p, "", -1 }; }
		String select = AdditionalMethods.match(t, p - line.start, GuiTextArea.filter, GuiTextArea.filter);
		p = line.text.lastIndexOf(select, p);
		return new Object[] { p, select, row };
	}

	public String getText() {
		return this.text;
	}

	public int[] getXYPosition(int pos) {
		int[] xy = new int[] { this.x, this.y };
		int h = 1;
		for (TextContainer.LineData data : this.container.lines) {
			if (pos >= data.start && pos <= data.end) {
				if (pos == data.end) {
					xy[1] += (h + 1) * GuiTextArea.font.height(null);
				} else {
					xy[0] += pos <= data.start ? 1 : GuiTextArea.font.width(data.text.substring(0, pos - data.start));
					xy[1] += h * GuiTextArea.font.height(null);
				}
				break;
			}
			h++;
		}
		return xy;
	}

	public boolean hasVerticalScrollbar() {
		return this.container.visibleLines < this.container.linesCount;
	}

	public boolean isActive() {
		return this.active;
	}

	public boolean isEnabled() {
		return this.enabled && this.visible;
	}

	public void keyTyped(char c, int i) {
		if (!this.active) {
			return;
		}
		if (GuiScreen.isKeyComboCtrlA(i)) {
			int n = 0;
			this.cursorPosition = n;
			this.startSelection = n;
			this.endSelection = this.text.length();
			return;
		}
		if (!this.isEnabled()) {
			return;
		}
		if (i == 203) {
			int j = 1;
			if (GuiScreen.isCtrlKeyDown()) {
				Matcher m = this.container.regexWord.matcher(this.text.substring(0, this.cursorPosition));
				while (m.find()) {
					if (m.start() == m.end()) {
						continue;
					}
					j = this.cursorPosition - m.start();
				}
			}
			this.setCursor(this.cursorPosition - j, GuiScreen.isShiftKeyDown());
			return;
		}
		if (i == 205) {
			int j = 1;
			if (GuiScreen.isCtrlKeyDown()) {
				Matcher m = this.container.regexWord.matcher(this.text.substring(this.cursorPosition));
				if ((m.find() && m.start() > 0) || m.find()) {
					j = m.start();
				}
			}
			this.setCursor(this.cursorPosition + j, GuiScreen.isShiftKeyDown());
			return;
		}
		if (i == 200) {
			this.setCursor(this.cursorUp(), GuiScreen.isShiftKeyDown());
			return;
		}
		if (i == 208) {
			this.setCursor(this.cursorDown(), GuiScreen.isShiftKeyDown());
			return;
		}
		if (i == 211) {
			String s = this.getSelectionAfterText();
			if (!s.isEmpty() && this.startSelection == this.endSelection) {
				s = s.substring(1);
			}
			this.setText(this.getSelectionBeforeText() + s);
			int startSelection = this.startSelection;
			this.cursorPosition = startSelection;
			this.endSelection = startSelection;
			return;
		}
		if (i == 14) {
			String s = this.getSelectionBeforeText();
			if (this.startSelection > 0 && this.startSelection == this.endSelection) {
				s = s.substring(0, s.length() - 1);
				--this.startSelection;
			}
			this.setText(s + this.getSelectionAfterText());
			int startSelection2 = this.startSelection;
			this.cursorPosition = startSelection2;
			this.endSelection = startSelection2;
			return;
		}
		if (GuiScreen.isKeyComboCtrlX(i)) {
			if (this.startSelection != this.endSelection) {
				NoppesStringUtils.setClipboardContents(this.text.substring(this.startSelection, this.endSelection));
				if (this.onlyReading) { return; }
				String s = this.getSelectionBeforeText();
				this.setText(s + this.getSelectionAfterText());
				int length = s.length();
				this.cursorPosition = length;
				this.startSelection = length;
				this.endSelection = length;
			}
			return;
		}
		if (GuiScreen.isKeyComboCtrlC(i)) {
			if (this.startSelection != this.endSelection) {
				NoppesStringUtils.setClipboardContents(this.text.substring(this.startSelection, this.endSelection));
			}
			return;
		}
		if (GuiScreen.isKeyComboCtrlV(i)) {
			if (this.onlyReading) { return; }
			this.addText(NoppesStringUtils.getClipboardContents());
			return;
		}
		if (i == 44 && GuiScreen.isCtrlKeyDown()) {
			if (this.undoList.isEmpty()) {
				return;
			}
			this.undoing = true;
			this.redoList.add(new UndoData(this.text, this.cursorPosition));
			UndoData data = this.undoList.remove(this.undoList.size() - 1);
			this.setText(data.text);
			int cursorPosition = data.cursorPosition;
			this.cursorPosition = cursorPosition;
			this.startSelection = cursorPosition;
			this.endSelection = cursorPosition;
			this.undoing = false;
		} else {
			if (this.onlyReading) { return; }
			if (i != 21 || !GuiScreen.isCtrlKeyDown()) {
				if (i == 15) {
					this.addText("	");
				}
				if (i == 28) {
					this.addText(Character.toString('\n') + this.getIndentCurrentLine());
				}
				if (ChatAllowedCharacters.isAllowedCharacter(c)) {
					this.addText(Character.toString(c));
				}
				return;
			}
			if (this.redoList.isEmpty()) {
				return;
			}
			this.undoing = true;
			this.undoList.add(new UndoData(this.text, this.cursorPosition));
			UndoData data = this.redoList.remove(this.redoList.size() - 1);
			this.setText(data.text);
			int cursorPosition2 = data.cursorPosition;
			this.cursorPosition = cursorPosition2;
			this.startSelection = cursorPosition2;
			this.endSelection = cursorPosition2;
			this.undoing = false;
		}
	}

	public boolean mouseClicked(int xMouse, int yMouse, int mouseButton) {
		if (this.freeze) {
			return false;
		}
		if (this.onlyReading && !(mouseButton==0 && this.container.linesCount * this.container.lineHeight > this.height && xMouse > this.x + this.width - 8)) { return false; } 
		this.active = (xMouse >= this.x && xMouse < this.x + this.width && yMouse >= this.y && yMouse < this.y + this.height);
		if (this.active) {
			int selectionPos = this.getSelectionPos(xMouse, yMouse);
			this.cursorPosition = selectionPos;
			this.endSelection = selectionPos;
			this.startSelection = selectionPos;
			this.clicked = (mouseButton == 0);
			this.doubleClicked = false;
			long time = System.currentTimeMillis();
			if (this.clicked && this.container.linesCount * this.container.lineHeight > this.height && xMouse > this.x + this.width - 8) {
				this.clicked = false;
				this.clickScrolling = true;
			} else if (time - this.lastClicked < 500L) {
				this.doubleClicked = true;
				Matcher m = this.container.regexWord.matcher(this.text);
				while (m.find()) {
					if (this.cursorPosition > m.start() && this.cursorPosition < m.end()) {
						this.startSelection = m.start();
						this.endSelection = m.end();
						break;
					}
				}
			}
			this.lastClicked = time;
		}
		return this.active;
	}

	private void setCursor(int i, boolean select) {
		i = Math.min(Math.max(i, 0), this.text.length());
		if (i == this.cursorPosition) {
			return;
		}
		if (!select) {
			int endSelection = i;
			this.cursorPosition = endSelection;
			this.startSelection = endSelection;
			this.endSelection = endSelection;
			return;
		}
		int diff = this.cursorPosition - i;
		if (this.cursorPosition == this.startSelection) {
			this.startSelection -= diff;
		} else if (this.cursorPosition == this.endSelection) {
			this.endSelection -= diff;
		}
		if (this.startSelection > this.endSelection) {
			int j = this.endSelection;
			this.endSelection = this.startSelection;
			this.startSelection = j;
		}
		this.cursorPosition = i;
	}

	public void setCursorPosition(int pos) {
		if (pos < 0) {
			pos = 0;
		} else if (pos >= this.getText().length()) {
			pos = this.getText().length() - 1;
		}
		this.cursorPosition = pos;
		this.startSelection = pos;
		this.endSelection = pos;
	}

	public void setListener(ITextChangeListener listener) {
		this.listener = listener;
	}

	public void setText(String text) {
		text = text.replace("\r", "");
		if (this.text != null && this.text.equals(text)) {
			return;
		}
		if (this.listener != null) {
			this.listener.textUpdate(text);
		}
		if (!this.undoing) {
			this.undoList.add(new UndoData(this.text, this.cursorPosition));
			this.redoList.clear();
		}
		this.text = text;
		(this.container = new TextContainer(text)).init(GuiTextArea.font, this.width, this.height);
		if (this.enableCodeHighlighting) {
			this.container.formatCodeText();
		}
		if (this.scrolledLine > this.container.linesCount - this.container.visibleLines) {
			this.scrolledLine = Math.max(0, this.container.linesCount - this.container.visibleLines);
		}
	}

	public void updateScreen() {
		++this.cursorCounter;
		if (this.freeze) {
			return;
		}
		int k2 = Mouse.getDWheel();
		if (k2 != 0) {
			this.scrolledLine += ((k2 > 0) ? -1 : 1);
			this.scrolledLine = Math.max(
					Math.min(this.scrolledLine, this.container.linesCount - this.height / this.container.lineHeight),
					0);
		}
	}

	public void setIsCode(boolean bo) { GuiTextArea.font.setIsCode((char) (bo ? 65535 : 167), bo); }

}
