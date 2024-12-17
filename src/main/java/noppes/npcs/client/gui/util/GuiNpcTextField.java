package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.LogWriter;
import noppes.npcs.api.mixin.client.gui.IGuiTextFieldMixin;
import noppes.npcs.util.ValueUtil;
import org.lwjgl.input.Mouse;

import java.util.ArrayList;
import java.util.List;

public class GuiNpcTextField
extends GuiTextField
implements IComponentGui {

	public static char[] filePath = new char[] { ':', '*', '?', '"', '<', '>', '&', '|' };

	public static GuiNpcTextField activeTextfield = null;
	private final List<String> hoverText = new ArrayList<>();

	public static boolean isActive() {
		return GuiNpcTextField.activeTextfield != null;
	}
	public static void unfocus() {
		GuiNpcTextField prev = GuiNpcTextField.activeTextfield;
		GuiNpcTextField.activeTextfield = null;
		if (prev != null) {
			prev.unFocused();
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
		setText((text == null) ? "" : text);
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
				if (listener != null) {
					listener.unFocused(this);
				}
			}
		}
		drawTextBox();
	}

	public double getDouble() {
		double d = 0.0d;
		try {
			d = Double.parseDouble(getText().replace(",", "."));
		} catch (Exception e) { LogWriter.error("Error:", e); }
		return d;
	}

	public int getInteger() {
		int i = 0;
		try {
			i = Integer.parseInt(getText());
		} catch (Exception e) { LogWriter.error("Error:", e); }
		return i;
	}

	public long getLong() {
		long i = 0L;
		try {
			i = Long.parseLong(getText());
		} catch (Exception e) { LogWriter.error("Error:", e); }
		return i;
	}

	public boolean isDouble() {
		try {
			Double.parseDouble(getText().replace(",", "."));
			return true;
		} catch (NumberFormatException ignored) { }
		return false;
	}

	public boolean isEmpty() {
		return getText().trim().isEmpty();
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
		} catch (NumberFormatException e) { LogWriter.error("Error:", e); }
		return false;
	}

	public boolean isMouseOver() {
		return hovered;
	}

	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (!canEdit) {
			return false;
		}
		boolean isFocused = isFocused();
		if (((IGuiTextFieldMixin) this).npcs$getCanLoseFocus()) {
			setFocused(hovered);
		}
		if (isFocused && hovered && mouseButton == 0) {
			int i = mouseX - x;
			if (((IGuiTextFieldMixin) this).npcs$getEnableBackgroundDrawing()) {
				i -= 4;
			}
			FontRenderer fontRenderer = ((IGuiTextFieldMixin) this).npcs$getFontRenderer();
			int lineScrollOffset = ((IGuiTextFieldMixin) this).npcs$getLineScrollOffset();
			String s = fontRenderer.trimStringToWidth(getText().substring(lineScrollOffset), getWidth());
			setCursorPosition(fontRenderer.trimStringToWidth(s, i).length() + lineScrollOffset);
			return true;
		}
		if (isFocused != isFocused() && isFocused) {
			unFocused();
		}
		if (isFocused()) {
			GuiNpcTextField.activeTextfield = this;
		}
		return false;
	}

	public void setMinMaxDefault(long minValue, long maxValue, long defaultValue) {
		numbersOnly = true;
		doubleNumbersOnly = false;
		min = minValue;
		max = maxValue;
		def = defaultValue;
	}

	public void setMinMaxDoubleDefault(double minValue, double maxValue, double defaultValue) {
		numbersOnly = false;
		doubleNumbersOnly = true;
		minD = minValue;
		maxD = maxValue;
		defD = defaultValue;
	}

	public boolean isAllowUppercase() { return allowUppercase; }

	public void setAllowUppercase(boolean isAllowUppercase) { allowUppercase = isAllowUppercase; }

	public boolean isLatinAlphabetOnly() { return latinAlphabetOnly; }

	public void setLatinAlphabetOnly(boolean isLatinAlphabetOnly) { latinAlphabetOnly = isLatinAlphabetOnly; }

	public boolean textboxKeyTyped(char typedChar, int keyCode) {
		if (GuiScreen.isCtrlKeyDown() && !super.textboxKeyTyped(typedChar, keyCode)) { return false; }
		if (latinAlphabetOnly && typedChar == ' ') { typedChar = '_'; }
		return charAllowed(typedChar, keyCode) && canEdit && super.textboxKeyTyped(typedChar, keyCode);
	}

	public void unFocused() {
		if (numbersOnly) {
			if (isEmpty() || !isInteger()) {
				setText(def + "");
			} else if (getInteger() < min) {
				setText(min + "");
			} else if (getInteger() > max) {
				setText(max + "");
			}
		} else if (doubleNumbersOnly) {
			if (isEmpty() || !isDouble()) {
				setText(defD + "");
			} else if (getDouble() < minD) {
				setText(minD + "");
			} else if (getDouble() > maxD) {
				setText(maxD + "");
			}
		}
		if (listener != null) {
			listener.unFocused(this);
		}
		if (this == GuiNpcTextField.activeTextfield) {
			GuiNpcTextField.activeTextfield = null;
		}
		setFocused(false);
	}

	@Override
	public int[] getCenter() { return new int[] { x + width / 2, x + height / 2}; }

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
	public void updateScreen() {
		if (enabled) { updateCursorCounter(); }
	}

}
