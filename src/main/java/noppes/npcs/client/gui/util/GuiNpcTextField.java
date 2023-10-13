package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;

public class GuiNpcTextField
extends GuiTextField {
	
	private static GuiNpcTextField activeTextfield = null;
	private int[] allowedSpecialChars;
	public boolean canEdit;
	public boolean doubleNumbersOnly;
	public boolean enabled;
	// New
	public boolean inMenu;
	private ITextfieldListener listener;
	public int min, max, def;

	public double minD, maxD, defD;

	public boolean numbersOnly;
	private boolean hovered;

	public GuiNpcTextField(int id, GuiScreen parent, FontRenderer fontRenderer, int x, int y, int width, int height, String text) {
		super(id, fontRenderer, x, y, width, height);
		this.enabled = true;
		this.inMenu = true;
		this.numbersOnly = false;
		this.min = Integer.MIN_VALUE;
		this.max = Integer.MAX_VALUE;
		this.def = 0;
		this.canEdit = true;
		this.allowedSpecialChars = new int[] { 14, 211, 203, 205 };
		this.setMaxStringLength(500);
		this.setText((text == null) ? "" : text);
		if (parent instanceof ITextfieldListener) {
			this.listener = (ITextfieldListener) parent;
		}
		// New
		this.doubleNumbersOnly = false;
		this.minD = Double.MIN_VALUE;
		this.maxD = Double.MAX_VALUE;
		this.defD = 0;
	}

	public GuiNpcTextField(int id, GuiScreen parent, int x, int y, int width, int height, String text) {
		this(id, parent, Minecraft.getMinecraft().fontRenderer, x, y, width, height, text);
	}

	public static boolean isActive() {
		return GuiNpcTextField.activeTextfield != null;
	}

	public static void unfocus() {
		GuiNpcTextField prev = GuiNpcTextField.activeTextfield;
		GuiNpcTextField.activeTextfield = null;
		if (prev != null) { prev.unFocused(); }
	}
	
	private boolean charAllowed(char c, int i) {
		if (!this.numbersOnly || Character.isDigit(c) || (c == '-' && this.getText().length() == 0)) {
			return true;
		}
		// New
		if (!this.doubleNumbersOnly || Character.isDigit(c) || (c == '-' && this.getText().length() == 0)
				|| (c == '.' && this.getText().indexOf(".") != -1)) {
			return true;
		}
		for (int j : this.allowedSpecialChars) {
			if (j == i) {
				return true;
			}
		}
		return false;
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
		try { d = Double.parseDouble(this.getText().replace(",", ".")); } catch (Exception e) {}
		return d;
	}

	public int getInteger() {
		int i = 0;
		try { i = Integer.parseInt(this.getText()); } catch (Exception e) {}
		return i;
	}

	public boolean isDouble() {
		try {
			Double.parseDouble(this.getText().replace(",", "."));
			return true;
		} catch (NumberFormatException e) { }
		return false;
	}

	public boolean isEmpty() {
		return this.getText().trim().length() == 0;
	}

	public boolean isInteger() {
		try {
			Integer.parseInt(this.getText());
			return true;
		}
		catch (NumberFormatException e) { }
		return false;
	}

	public boolean mouseClicked(int i, int j, int k) {
		if (!this.canEdit) {
			return false;
		}
		boolean wasFocused = this.isFocused();
		boolean clicked = super.mouseClicked(i, j, k);
		if (wasFocused != this.isFocused() && wasFocused) {
			this.unFocused();
		}
		if (this.isFocused()) {
			GuiNpcTextField.activeTextfield = this;
		}
		return clicked;
	}

	public GuiNpcTextField setDoubleNumbersOnly() {
		this.numbersOnly = false;
		this.doubleNumbersOnly = true;
		return this;
	}

	public void setMinMaxDefault(int min, int max, int def) {
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

	public boolean textboxKeyTyped(char c, int i) {
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
		if (this == GuiNpcTextField.activeTextfield) { GuiNpcTextField.activeTextfield = null; }
	}

	public boolean isMouseOver() {
        return this.hovered;
    }

}
