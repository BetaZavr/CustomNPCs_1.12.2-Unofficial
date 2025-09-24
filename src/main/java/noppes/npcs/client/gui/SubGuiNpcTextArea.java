package noppes.npcs.client.gui;

import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.gui.util.*;

import javax.annotation.Nonnull;

public class SubGuiNpcTextArea extends SubGuiInterface implements ITextChangeListener {

	protected boolean highlighting;
	protected GuiTextArea textarea;
	public String originalText;
	public String text;

	public SubGuiNpcTextArea(int id, String t) {
		super(id);
		setBackground("bgfilled.png");
		xSize = 256;
		ySize = 256;
		widthTexture = 256;
		heightTexture = 256;
		closeOnEsc = true;

		highlighting = false;
		text = t;
		originalText = t;
	}

	public SubGuiNpcTextArea(String textOriginal, String t) {
		this(0, t);
		originalText = textOriginal;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: onClosed(); break;
			case 100: NoppesStringUtils.setClipboardContents(textarea.getText()); break;
			case 101: textarea.setText(NoppesStringUtils.getClipboardContents()); break;
			case 102: textarea.setText(""); break;
			case 103: textarea.setText(originalText); break;
		}
	}

	public SubGuiNpcTextArea enableHighlighting() {
		highlighting = true;
		return this;
	}

    @Override
	public void initGui() {
		xSize = (int) (width * 0.88);
		ySize = (int) (height * 0.95);
		if ((double) ySize > (double) height * 0.95D) {
			ySize = (int)((double) height * 0.95D);
			xSize = (int)((double) ySize / 0.56D);
		}
		bgScale = (float) xSize / 440.0F;
		super.initGui();
		if (textarea != null) { text = textarea.getText(); }
		add(textarea = new GuiTextArea(0, guiLeft + 5, guiTop + 5, xSize - 68, ySize - 10, text).setListener(this));
		if (highlighting) { textarea.enableCodeHighlighting(); }
		add(textarea);
		int x = guiLeft + 7 + textarea.width;
		int y = guiTop + 5;
		addButton(new GuiNpcButton(102, x, y, 56, 20, "gui.clear"));
		addButton(new GuiNpcButton(101, x, y += 23, 56, 20, "gui.paste"));
		addButton(new GuiNpcButton(100, x, y += 23, 56, 20, "gui.copy"));
		addButton(new GuiNpcButton(103, x, y + 23, 56, 20, "remote.reset"));
		addButton(new GuiNpcButton(0, x, guiTop + ySize - 25, 56, 20, "gui.close"));
	}

	@Override
	public void textUpdate(String t) { text = t; }

}
