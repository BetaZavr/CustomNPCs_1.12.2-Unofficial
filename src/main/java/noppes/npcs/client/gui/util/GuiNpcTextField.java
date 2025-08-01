package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.LogWriter;
import noppes.npcs.api.mixin.client.gui.IGuiTextFieldMixin;
import noppes.npcs.util.ValueUtil;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

public class GuiNpcTextField
extends GuiTextField
implements IComponentGui, IGuiNpcTextField {

	public static char[] filePath = new char[] { ':', '*', '?', '"', '<', '>', '&', '|' };

	public static IGuiNpcTextField activeTextfield = null;
	private final List<String> hoverText = new ArrayList<>();

	public static boolean isActive() {
		return GuiNpcTextField.activeTextfield != null;
	}
	public static void unfocus() {
		IGuiNpcTextField prev = GuiNpcTextField.activeTextfield;
		GuiNpcTextField.activeTextfield = null;
		if (prev != null) {
			prev.unFocus();
		}
	}
	private final int[] allowedSpecialKeyIDs = new int[] { 14, 211, 203, 205 };
	public char[] prohibitedSpecialChars = new char[] {};
	public boolean enabled = true;
	public boolean hovered;
	protected boolean canEdit = true;
	private boolean latinAlphabetOnly = false;
	private boolean allowUppercase = true;
	private boolean numbersOnly = false;
	private boolean doubleNumbersOnly = false;
	private ITextfieldListener listener;

	public long min = Integer.MIN_VALUE;
	public long max = Integer.MAX_VALUE;
	public long def = 0;

	public double minD = Double.MIN_VALUE;
	public double maxD = Double.MAX_VALUE;
	public double defD = 0.0d;

	public GuiNpcTextField(int id, GuiScreen parent, FontRenderer fontRenderer, int x, int y, int width, int height, String text) {
		super(id, fontRenderer, x, y, width, height);
		setMaxStringLength(500);
		setFullText((text == null) ? "" : text);
		if (parent instanceof ITextfieldListener) {
			listener = (ITextfieldListener) parent;
		}
	}

	public GuiNpcTextField(int id, GuiScreen parent, int x, int y, int width, int height, String text) {
		this(id, parent, Minecraft.getMinecraft().fontRenderer, x, y, width, height, text);
	}

	private boolean charAllowed(char c, int i) {
		for (char g : prohibitedSpecialChars) {
			if (g == c) {
				return false;
			}
		}
		for (int j : allowedSpecialKeyIDs) {
			if (j == i) {
				return true;
			}
		}
		boolean selectAll = getSelectedText().equals(getFullText());
		//if (Keyboard.getKeyName(i).startsWith("NUMPAD"))
		if (numbersOnly) {
			return Character.isDigit(c) || (c == '-' && selectAll || getCursorPosition() == 0 && !getFullText().contains("" + c));
		}
		if (doubleNumbersOnly) {
			boolean hasDot = getFullText().contains(".") || getFullText().contains(",");
			return Character.isDigit(c) || (c == '-' && selectAll || getCursorPosition() == 0 && !getFullText().contains("" + c)) || (!hasDot || selectAll && (c == '.' || c == ','));
		}
		if (!latinAlphabetOnly || Character.isLetterOrDigit(c) || c == '_') {
			return true;
		}
        return allowUppercase || Character.isLowerCase(c);
    }

	public void drawTextBox() {
		if (!enabled) { return; }
		super.drawTextBox();
	}

	@Override
	public void render(IEditNPC gui, int mouseX, int mouseY, float partialTicks) {
		hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
		if (hovered && !gui.hasSubGui() && !hoverText.isEmpty()) { gui.setHoverText(hoverText); }
		if (hovered && (doubleNumbersOnly || numbersOnly)) {
			int dWheel = Mouse.getDWheel();
			if (dWheel != 0) {
				if (doubleNumbersOnly) {
					double d = getDouble();
					double v = maxD - minD;
					double f = (dWheel < 0 ? -v : v) / (double) width;
					double t = d + f;
					if (t < minD) { t = t - minD + maxD; }
					else if (t > maxD) { t = t - maxD + minD; }
					setFullText("" + ValueUtil.correctDouble(Math.round(t * 1000.0d) / 1000.0d, minD, maxD));
				} else {
					int i = getInteger();
					int v = (int) (max - min);
					int f = (dWheel < 0 ? -v : v) / width;
					int t = i + f;
					if (t < min) { t = t - (int) (min + max); }
					else if (t > max) { t = t - (int) (max + min); }
					setFullText("" + ValueUtil.correctInt((int) (Math.round((double) t * 1000.0d) / 1000.0d), (int) min, (int) max));
				}
				if (listener != null) {
					listener.unFocused(this);
				}
			}
		}
		drawTextBox();
	}

	@Override
	public int getID() { return getId(); }

	@Override
	public double getDouble() {
		double d = defD;
		try {
			d = Double.parseDouble(getFullText().replace(",", "."));
		} catch (NumberFormatException ignored) { }
		return d;
	}

	@Override
	public int getInteger() {
		int i = (int) def;
		try {
			i = Integer.parseInt(getFullText());
		} catch (NumberFormatException ignored) { }
		return i;
	}

	@Override
	public long getLong() {
		long i = 0L;
		try {
			i = Long.parseLong(getFullText());
		} catch (NumberFormatException ignored) { }
		return i;
	}

	@Override
	public boolean isDouble() {
		try {
			Double.parseDouble(getFullText().replace(",", "."));
			return true;
		} catch (NumberFormatException ignored) { }
		return false;
	}

	@Override
	public boolean isEmpty() {
		return getFullText().trim().isEmpty();
	}

	@Override
	public long getDefault() { return def; }

	@Override
	public double getDoubleDefault() { return defD; }

	@Override
	public boolean isInteger() {
		try {
			Integer.parseInt(getFullText());
			return true;
		} catch (NumberFormatException ignored) {  }
		return false;
	}

	@Override
	public boolean isLong() {
		try {
			Long.parseLong(getFullText());
			return true;
		} catch (NumberFormatException e) { LogWriter.error(e); }
		return false;
	}

	@Override
	public boolean isHovered() {
		return hovered;
	}

	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (!canEdit) { return false; }
		boolean isFocused = isFocused();
		if (((IGuiTextFieldMixin) this).npcs$getCanLoseFocus()) { setFocus(hovered); }
		if (isFocused && hovered && mouseButton == 0) {
			int i = mouseX - x;
			if (((IGuiTextFieldMixin) this).npcs$getEnableBackgroundDrawing()) { i -= 4; }
			FontRenderer fontRenderer = ((IGuiTextFieldMixin) this).npcs$getFontRenderer();
			int lineScrollOffset = ((IGuiTextFieldMixin) this).npcs$getLineScrollOffset();
			String s = fontRenderer.trimStringToWidth(getFullText().substring(lineScrollOffset), getWidth());
			setCursorPosition(fontRenderer.trimStringToWidth(s, i).length() + lineScrollOffset);
			return true;
		}
		if (isFocused != isFocused() && isFocused) {
			unFocus();
		}
		if (isFocused()) {
			GuiNpcTextField.activeTextfield = this;
		}
		return false;
	}

	@Override
	public boolean isVisible() { return getVisible(); }

	@Override
	public boolean isEnabled() { return enabled; }

	@Override
	public void setMinMaxDefault(long minValue, long maxValue, long defaultValue) {
		numbersOnly = true;
		doubleNumbersOnly = false;
		if (minValue > maxValue) {
			long i = minValue;
			minValue = maxValue;
			maxValue = i;
		}
		min = minValue;
		max = maxValue;
		def = defaultValue;
	}

	@Override
	public void setMinMaxDoubleDefault(double minValue, double maxValue, double defaultValue) {
		numbersOnly = false;
		doubleNumbersOnly = true;
		if (minValue > maxValue) {
			double i = minValue;
			minValue = maxValue;
			maxValue = i;
		}
		minD = minValue;
		maxD = maxValue;
		defD = defaultValue;
	}

	@Override
	public boolean isAllowUppercase() { return allowUppercase; }

	@Override
	public void setAllowUppercase(boolean isAllowUppercase) { allowUppercase = isAllowUppercase; }

	@Override
	public long getMax() { return max; }

	@Override
	public long getMin() { return min; }

	@Override
	public double getDoubleMax() { return maxD; }

	@Override
	public double getDoubleMin() { return minD; }

	@Override
	public boolean isLatinAlphabetOnly() { return latinAlphabetOnly; }

	@Override
	public void setLatinAlphabetOnly(boolean isLatinAlphabetOnly) { latinAlphabetOnly = isLatinAlphabetOnly; }

	public boolean textboxKeyTyped(char typedChar, int keyCode) {
		if (!isFocused())  { return false; }
		if (keyCode == 28 && !(this instanceof GuiNpcTextArea)) { // Enter
			if (listener != null) {
				listener.unFocused(this);
				return false;
			}
		}
		if (latinAlphabetOnly && typedChar == ' ') { typedChar = '_'; }
		if (Keyboard.getKeyName(keyCode).startsWith("NUMPAD")) {
			typedChar = Keyboard.getKeyName(keyCode).replace("NUMPAD", "").charAt(0);
		}
		if (GuiScreen.isKeyComboCtrlA(keyCode)) {
			setCursorPositionEnd();
			setSelectionPos(0);
			return true;
		}
		else if (GuiScreen.isKeyComboCtrlC(keyCode)) {
			GuiScreen.setClipboardString(getSelectedText());
			return true;
		}
		else if (GuiScreen.isKeyComboCtrlV(keyCode) && canEdit) {
			if (isEnabled()) { writeText(GuiScreen.getClipboardString()); }
			return true;
		}
		else if (GuiScreen.isKeyComboCtrlX(keyCode)) {
			GuiScreen.setClipboardString(getSelectedText());
			if (canEdit && isEnabled()) { writeText(""); }
			return true;
		}
		if (!canEdit) { return false; }
		switch (keyCode) {
			case 14: {
				if (GuiScreen.isCtrlKeyDown()) {
					if (isEnabled()) {
						deleteWords(-1);
					}
				} else if (isEnabled()) {
					deleteFromCursor(-1);
				}
				return true;
			} // backspace
			case 199: {
				if (GuiScreen.isShiftKeyDown()) {
					setSelectionPos(0);
				} else {
					setCursorPositionZero();
				}
				return true;
			} // home
			case 203: {
				if (GuiScreen.isShiftKeyDown()) {
					if (GuiScreen.isCtrlKeyDown()) {
						setSelectionPos(getNthWordFromPos(-1, getSelectionEnd()));
					} else {
						setSelectionPos(getSelectionEnd() - 1);
					}
				} else if (GuiScreen.isCtrlKeyDown()) {
					setCursorPosition(getNthWordFromCursor(-1));
				} else {
					moveCursorBy(-1);
				}
				return true;
			} // left
			case 205: {
				if (GuiScreen.isShiftKeyDown()) {
					if (GuiScreen.isCtrlKeyDown()) {
						setSelectionPos(getNthWordFromPos(1, getSelectionEnd()));
					} else {
						setSelectionPos(getSelectionEnd() + 1);
					}
				} else if (GuiScreen.isCtrlKeyDown()) {
					setCursorPosition(getNthWordFromCursor(1));
				} else {
					moveCursorBy(1);
				}
				return true;
			} // right
			case 207: {
				if (GuiScreen.isShiftKeyDown()) {
					setSelectionPos(getFullText().length());
				} else {
					setCursorPositionEnd();
				}
				return true;
			} // end
			case 211: {
				if (GuiScreen.isCtrlKeyDown()) {
					if (isEnabled()) {
						deleteWords(1);
					}
				} else if (isEnabled()) {
					deleteFromCursor(1);
				}
				return true;
			} // delete
			default: {
				if (charAllowed(typedChar, keyCode)) {
					if (isEnabled()) { writeText(Character.toString(typedChar)); }
					return true;
				}
				return false;
			} // any symbol
		}
	}

	@Override
	public void setFocus(boolean bo) {
		setFocused(bo);
	}

	@Override
	public String getFullText() {
		return getText();
	}

	@Override
	public void setFullText(String text) {
		setText(text);
	}

	@Override
	public void unFocus() {
		if (numbersOnly) {
			if (isEmpty() || !isInteger()) {
				setFullText(def + "");
			} else if (getInteger() < min) {
				setFullText(min + "");
			} else if (getInteger() > max) {
				setFullText(max + "");
			}
		}
		else if (doubleNumbersOnly) {
			if (isEmpty() || !isDouble()) {
				setFullText(defD + "");
			} else if (getDouble() < minD) {
				setFullText(minD + "");
			} else if (getDouble() > maxD) {
				setFullText(maxD + "");
			}
		}
		if (listener != null) {
			listener.unFocused(this);
		}
		if (this == GuiNpcTextField.activeTextfield) { GuiNpcTextField.activeTextfield = null; }
		setFocus(false);
	}

	@Override
	public int[] getCenter() { return new int[] { x + width / 2, y + height / 2}; }

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
	public int getHeight() { return height; }

	@Override
	public void customKeyTyped(char c, int id) { textboxKeyTyped(c, id); }

	@Override
	public void customMouseClicked(int mouseX, int mouseY, int mouseButton) { mouseClicked(mouseX, mouseY, mouseButton); }

	@Override
	public void customMouseReleased(int mouseX, int mouseY, int mouseButton) { }

	@Override
	public void setIsVisible(boolean bo) {
		super.setVisible(bo);
	}

	public void updateScreen() {
		if (enabled) { updateCursorCounter(); }
	}

}
