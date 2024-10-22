package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import noppes.npcs.LogWriter;
import noppes.npcs.mixin.client.gui.IGuiTextFieldMixin;

public class GuiNpcTextField
		extends GuiTextField
		implements IComponentGui {

	public static char[] filePath = new char[] { ':', '*', '?', '"', '<', '>', '&', '|' };

	public static GuiNpcTextField activeTextfield = null;

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
		this.setMaxStringLength(500);
		this.setText((text == null) ? "" : text);
		if (parent instanceof ITextfieldListener) {
			this.listener = (ITextfieldListener) parent;
		}
	}

	public GuiNpcTextField(int id, GuiScreen parent, int x, int y, int width, int height, String text) {
		this(id, parent, Minecraft.getMinecraft().fontRenderer, x, y, width, height, text);
	}

	private boolean charAllowed(char c, int i) {
		for (char g : this.prohibitedSpecialChars) {
			if (g == c) {
				return false;
			}
		}
		for (int j : this.allowedSpecialKeyIDs) {
			if (j == i) {
				return true;
			}
		}
		if (!latinAlphabetOnly || Character.isLetterOrDigit(c) || c == '_') {
			return true;
		}
		if (allowUppercase || Character.isLowerCase(c)) {
			return true;
		}
		if (!this.numbersOnly || Character.isDigit(c) || (c == '-' && this.getText().isEmpty())) {
			return true;
		}
        return !this.doubleNumbersOnly || Character.isDigit(c) || (c == '-' && this.getText().isEmpty())
                || (c == '.' && this.getText().contains("."));
    }

	public void drawTextBox() {
		if (this.enabled) {
			super.drawTextBox();
		}
	}

	public void drawTextBox(int mouseX, int mouseY) {
		this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width && mouseY < this.y + this.height;
		this.drawTextBox();
	}

	public double getDouble() {
		double d = 0.0d;
		try {
			d = Double.parseDouble(this.getText().replace(",", "."));
		} catch (Exception e) { LogWriter.error("Error:", e); }
		return d;
	}

	public int getInteger() {
		int i = 0;
		try {
			i = Integer.parseInt(this.getText());
		} catch (Exception e) { LogWriter.error("Error:", e); }
		return i;
	}

	public long getLong() {
		long i = 0L;
		try {
			i = Long.parseLong(this.getText());
		} catch (Exception e) { LogWriter.error("Error:", e); }
		return i;
	}

	public boolean isDouble() {
		try {
			Double.parseDouble(this.getText().replace(",", "."));
			return true;
		} catch (NumberFormatException e) { LogWriter.error("Error:", e); }
		return false;
	}

	public boolean isEmpty() {
		return this.getText().trim().isEmpty();
	}

	public boolean isInteger() {
		try {
			Integer.parseInt(this.getText());
			return true;
		} catch (NumberFormatException e) { LogWriter.error("Error:", e); }
		return false;
	}

	public boolean isLong() {
		try {
			Long.parseLong(this.getText());
			return true;
		} catch (NumberFormatException e) { LogWriter.error("Error:", e); }
		return false;
	}

	public boolean isMouseOver() {
		return this.hovered;
	}

	public boolean mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (!this.canEdit) {
			return false;
		}
		boolean isFocused = this.isFocused();
		if (((IGuiTextFieldMixin) this).npcs$getCanLoseFocus()) {
			this.setFocused(hovered);
		}
		if (isFocused && hovered && mouseButton == 0) {
			int i = mouseX - this.x;
			if (((IGuiTextFieldMixin) this).npcs$getEnableBackgroundDrawing()) {
				i -= 4;
			}
			FontRenderer fontRenderer = ((IGuiTextFieldMixin) this).npcs$getFontRenderer();
			int lineScrollOffset = ((IGuiTextFieldMixin) this).npcs$getLineScrollOffset();
			String s = fontRenderer.trimStringToWidth(this.getText().substring(lineScrollOffset), this.getWidth());
			this.setCursorPosition(fontRenderer.trimStringToWidth(s, i).length() + lineScrollOffset);
			return true;
		}
		if (isFocused != this.isFocused() && isFocused) {
			this.unFocused();
		}
		if (this.isFocused()) {
			GuiNpcTextField.activeTextfield = this;
		}
		return false;
	}

	public GuiNpcTextField setDoubleNumbersOnly() {
		this.numbersOnly = false;
		this.doubleNumbersOnly = true;
		return this;
	}

	public void setMinMaxDefault(long min, long max, long def) {
		this.min = min;
		this.max = max;
		this.def = def;
	}

	public void setMinMaxDoubleDefault(double min, double max, double def) {
		this.minD = min;
		this.maxD = max;
		this.defD = def;
	}

	public GuiNpcTextField setNumbersOnly() {
		this.numbersOnly = true;
		this.doubleNumbersOnly = false;
		return this;
	}

	public boolean isAllowUppercase() { return allowUppercase; }

	public void setAllowUppercase(boolean isAllowUppercase) { allowUppercase = isAllowUppercase; }

	public boolean isLatinAlphabetOnly() { return latinAlphabetOnly; }

	public void setLatinAlphabetOnly(boolean isLatinAlphabetOnly) { latinAlphabetOnly = isLatinAlphabetOnly; }

	public boolean textboxKeyTyped(char c, int i) {
		if (latinAlphabetOnly && c == ' ') { c = '_'; }
		return this.charAllowed(c, i) && this.canEdit && super.textboxKeyTyped(c, i);
	}

	public void unFocused() {
		if (this.numbersOnly) {
			if (this.isEmpty() || !this.isInteger()) {
				this.setText(this.def + "");
			} else if (this.getInteger() < this.min) {
				this.setText(this.min + "");
			} else if (this.getInteger() > this.max) {
				this.setText(this.max + "");
			}
		} else if (this.doubleNumbersOnly) { // New
			if (this.isEmpty() || !this.isDouble()) {
				this.setText(this.defD + "");
			} else if (this.getDouble() < this.minD) {
				this.setText(this.minD + "");
			} else if (this.getDouble() > this.maxD) {
				this.setText(this.maxD + "");
			}
		}
		if (this.listener != null) {
			this.listener.unFocused(this);
		}
		if (this == GuiNpcTextField.activeTextfield) {
			GuiNpcTextField.activeTextfield = null;
		}
		this.setFocused(false);
	}

	@Override
	public int[] getCenter() {
		return new int[] { this.x + this.width / 2, this.y + this.height / 2};
	}

}
