package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import noppes.npcs.LogWriter;
import noppes.npcs.api.mixin.client.gui.IGuiTextFieldMixin;
import noppes.npcs.util.ValueUtil;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class GuiNpcTextField extends GuiTextField implements IComponentGui {

	public static char[] filePath = new char[] { ':', '*', '?', '"', '<', '>', '&', '|' };

	public static GuiNpcTextField activeTextfield = null;

	public static boolean isActive() {
		return GuiNpcTextField.activeTextfield != null;
	}

	public static void unfocus() {
		GuiNpcTextField prev = GuiNpcTextField.activeTextfield;
		GuiNpcTextField.activeTextfield = null;
		if (prev != null) { prev.unFocus(); }
	}

	protected final List<String> hoverText = new ArrayList<>();
	protected final int[] allowedSpecialKeyIDs = new int[] { 14, 211, 203, 205 };
	protected boolean latinAlphabetOnly = false;
	protected boolean allowUppercase = true;
	protected boolean numbersOnly = false;
	protected boolean doubleNumbersOnly = false;
	protected ITextfieldListener listener;
	public char[] prohibitedSpecialChars = new char[] {};
	public boolean enabled = true;
	public boolean hovered;
	public long min = Integer.MIN_VALUE;
	public long max = Integer.MAX_VALUE;
	public long def = 0;
	public double minD = Double.MIN_VALUE;
	public double maxD = Double.MAX_VALUE;
	public double defD = 0.0d;

	public GuiNpcTextField(int id, GuiScreen parent, int x, int y, int width, int height, Object text) {
		super(id, Minecraft.getMinecraft().fontRenderer, x, y, width, height);
		setMaxStringLength(500);
		setText((text == null) ? "" : text.toString());
		if (parent instanceof ITextfieldListener) { listener = (ITextfieldListener) parent; }
	}

	private boolean charAllowed(char c, int i) {
		for (char g : prohibitedSpecialChars) {
			if (g == c) { return false; }
		}
		for (int j : allowedSpecialKeyIDs) {
			if (j == i) { return true; }
		}
		boolean selectAll = getSelectedText().equals(getText());
		if (numbersOnly) {
			return Character.isDigit(c) || (c == '-' && selectAll || getCursorPosition() == 0 && !getText().contains("" + c));
		}
		if (doubleNumbersOnly) {
			boolean hasDot = getText().contains(".") || getText().contains(",");
			return Character.isDigit(c) || (c == '-' && selectAll || getCursorPosition() == 0 && !getText().contains("" + c)) || (!hasDot || selectAll && (c == '.' || c == ','));
		}
		if (!latinAlphabetOnly || Character.isLetterOrDigit(c) || c == '_') {
			return true;
		}
        return allowUppercase || Character.isLowerCase(c);
    }

	@Override
	public void render(IEditNPC gui, int mouseX, int mouseY, float partialTicks) {
		if (!getVisible()) { hovered = false; return; }
		hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
		if (hovered && !gui.hasSubGui() && !hoverText.isEmpty()) { gui.putHoverText(hoverText); }
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
					setText("" + ValueUtil.correctDouble(Math.round(t * 1000.0d) / 1000.0d, minD, maxD));
				} else {
					int i = getInteger();
					int v = (int) (max - min);
					int f = (dWheel < 0 ? -v : v) / width;
					int t = i + f;
					if (t < min) { t = t - (int) (min + max); }
					else if (t > max) { t = t - (int) (max + min); }
					setText("" + ValueUtil.correctInt((int) (Math.round((double) t * 1000.0d) / 1000.0d), (int) min, (int) max));
				}
				if (listener != null) { listener.unFocused(this); }
			}
		}
		drawTextBox();
	}

	@Override
	public int getID() { return getId(); }

	public double getDouble() {
		double d = defD;
		try { d = Double.parseDouble(getText().replace(",", ".")); } catch (NumberFormatException ignored) { }
		return d;
	}

	public int getInteger() {
		int i = (int) def;
		try { i = Integer.parseInt(getText()); } catch (NumberFormatException ignored) { }
		return i;
	}

	public long getLong() {
		long i = 0L;
		try { i = Long.parseLong(getText()); } catch (NumberFormatException ignored) { }
		return i;
	}

	public boolean isDouble() {
		try {
			Double.parseDouble(getText().replace(",", "."));
			return true;
		} catch (NumberFormatException ignored) { }
		return false;
	}

	public boolean isInteger() {
		try {
			Integer.parseInt(getText());
			return true;
		} catch (NumberFormatException ignored) {  }
		return false;
	}

	public boolean isLong() {
		try {
			Long.parseLong(getText());
			return true;
		} catch (NumberFormatException e) { LogWriter.error(e); }
		return false;
	}

	public boolean isEmpty() { return getText().trim().isEmpty(); }

	@Override
	public List<String> getHoversText() { return hoverText; }

	@Override
	public boolean isHovered() { return hovered; }

	public boolean mouseCnpcsPressed(int mouseX, int mouseY, int mouseButton) {
		if (!enabled || !getVisible()) { return false; }
		boolean isFocused = isFocused();
		if (((IGuiTextFieldMixin) this).npcs$getCanLoseFocus()) { setFocused(hovered); }
		if (isFocused && hovered && mouseButton == 0) {
			int i = mouseX - x;
			if (((IGuiTextFieldMixin) this).npcs$getEnableBackgroundDrawing()) { i -= 4; }
			FontRenderer fontRenderer = ((IGuiTextFieldMixin) this).npcs$getFontRenderer();
			int lineScrollOffset = ((IGuiTextFieldMixin) this).npcs$getLineScrollOffset();
			String s = fontRenderer.trimStringToWidth(getText().substring(lineScrollOffset), getWidth());
			setCursorPosition(fontRenderer.trimStringToWidth(s, i).length() + lineScrollOffset);
			return true;
		}
		if (isFocused != isFocused() && isFocused) { unFocus(); }
		if (isFocused()) { GuiNpcTextField.activeTextfield = this; }
		return false;
	}

	@Override
	public boolean mouseCnpcsReleased(int mouseX, int mouseY, int state) { return false; }

	public GuiNpcTextField setMinMaxDefault(long minValue, long maxValue, long defaultValue) {
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
		return this;
	}

	public GuiNpcTextField setMinMaxDoubleDefault(double minValue, double maxValue, double defaultValue) {
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
		return this;
	}

	@SuppressWarnings("all")
	public boolean isAllowUppercase() { return allowUppercase; }

	public GuiNpcTextField setAllowUppercase(boolean isAllowUppercase) { allowUppercase = isAllowUppercase; return this; }

	@SuppressWarnings("all")
	public boolean isLatinAlphabetOnly() { return latinAlphabetOnly; }

	public GuiNpcTextField setLatinAlphabetOnly(boolean isLatinAlphabetOnly) { latinAlphabetOnly = isLatinAlphabetOnly; return this; }

	@Override
	public boolean keyCnpcsPressed(char typedChar, int keyCode) {
		if (!getVisible() || !isFocused())  { return false; }
		if (!(this instanceof GuiNpcTextArea) && (keyCode == Keyboard.KEY_RETURN || keyCode == Keyboard.KEY_NUMPADENTER)) {
			if (listener != null) {
				listener.unFocused(this);
				return false;
			}
		} // Enter
		if (latinAlphabetOnly && typedChar == ' ') { typedChar = '_'; }
		if (Keyboard.getKeyName(keyCode).startsWith("NUMPAD")) { typedChar = Keyboard.getKeyName(keyCode).replace("NUMPAD", "").charAt(0); }
		if (GuiScreen.isKeyComboCtrlA(keyCode)) {
			setCursorPositionEnd();
			setSelectionPos(0);
			return true;
		} // select all
		if (GuiScreen.isKeyComboCtrlC(keyCode)) {
			GuiScreen.setClipboardString(getSelectedText());
			return true;
		} // copy
		if (GuiScreen.isKeyComboCtrlV(keyCode)) {
			if (enabled) { writeText(GuiScreen.getClipboardString()); }
			return true;
		} // parse
		if (GuiScreen.isKeyComboCtrlX(keyCode)) {
			GuiScreen.setClipboardString(getSelectedText());
			if (enabled) { writeText(""); }
			return true;
		} // cut
		switch (keyCode) {
			case Keyboard.KEY_BACK: {
				if (enabled) {
					if (GuiScreen.isCtrlKeyDown()) { deleteWords(-1); }
					else { deleteFromCursor(-1); }
				}
				return true;
			} // backspace
			case Keyboard.KEY_HOME: {
				if (GuiScreen.isShiftKeyDown()) { setSelectionPos(0); }
				else { setCursorPositionZero(); }
				return true;
			} // home
			case Keyboard.KEY_LEFT: {
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
			case Keyboard.KEY_RIGHT: {
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
			case Keyboard.KEY_END: {
				if (GuiScreen.isShiftKeyDown()) { setSelectionPos(getText().length()); }
				else { setCursorPositionEnd(); }
				return true;
			} // end
			case Keyboard.KEY_DELETE: {
				if (enabled) {
					if (GuiScreen.isCtrlKeyDown()) { deleteWords(1); }
					else { deleteFromCursor(1); }
				}
				return true;
			} // delete
			default: {
				if (enabled && charAllowed(typedChar, keyCode)) {
					writeText(Character.toString(typedChar));
					return true;
				}
			} // any symbol
		}
		return false;
	}

	public void unFocus() {
		if (numbersOnly) {
			if (isEmpty() || !isInteger()) { setText(def + ""); }
			else if (getInteger() < min) { setText(min + ""); }
			else if (getInteger() > max) { setText(max + ""); }
		}
		else if (doubleNumbersOnly) {
			if (isEmpty() || !isDouble()) { setText(defD + ""); }
			else if (getDouble() < minD) { setText(minD + ""); }
			else if (getDouble() > maxD) { setText(maxD + ""); }
		}
		if (listener != null) { listener.unFocused(this); }
		if (this == GuiNpcTextField.activeTextfield) { GuiNpcTextField.activeTextfield = null; }
		setFocused(false);
	}

	@Override
	public int[] getCenter() { return new int[] { x + width / 2, y + height / 2}; }

	public GuiNpcTextField setHoverText(Object... components) {
		hoverText.clear();
		if (components == null) { return this; }
		noppes.npcs.util.Util.instance.putHovers(hoverText, components);
		return this;
	}

	@Override
	public GuiNpcTextField setIsEnable(boolean isEnable) {
		enabled = isEnable;
		return this;
	}

	@Override
	public GuiNpcTextField setIsVisible(boolean bo) {
		super.setVisible(bo);
		return this;
	}

	@Override
	public void moveTo(int addX, int addY) {
		x += addX;
		y += addY;
	}

	@Override
	public void updateCnpcsScreen() {
		if (enabled) { updateCursorCounter(); }
	}

	// New from Unofficial (BetaZavr)
	@Override
	public void writeText(@Nullable String text) {
		if (text == null) { return; }
		String oldText = getText();
		super.writeText(text);
		if (listener instanceof ITextChangeListener && !oldText.equals(getText())) { ((ITextChangeListener) listener).textUpdate(getText()); }
	}

	@Override
	public void deleteFromCursor(int pos) {
		String oldText = getText();
		super.deleteFromCursor(pos);
		if (listener instanceof ITextChangeListener && !oldText.equals(getText())) { ((ITextChangeListener) listener).textUpdate(getText()); }
	}

	@Override
	public void setText(@Nullable String text) {
		if (text == null) { return; }
		String oldText = getText();
		super.setText(text);
		if (listener instanceof ITextChangeListener && !oldText.equals(getText())) { ((ITextChangeListener) listener).textUpdate(getText()); }
	}

}
