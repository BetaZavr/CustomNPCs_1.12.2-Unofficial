package noppes.npcs.client.gui;

import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.gui.util.*;

public class SubGuiNpcTextArea
extends SubGuiInterface
implements ITextChangeListener {

	private boolean highlighting;
	public String originalText;
	public String text;
	private GuiTextArea textarea;

	public SubGuiNpcTextArea(int i, String text) {
		this(text);
		id = i;
	}

	public SubGuiNpcTextArea(String t) {
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
		this(t);
		originalText = textOriginal;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getID()) {
			case 0: close(); break;
			case 100: NoppesStringUtils.setClipboardContents(textarea.getFullText()); break;
			case 101: textarea.setFullText(NoppesStringUtils.getClipboardContents()); break;
			case 102: textarea.setFullText(""); break;
			case 103: textarea.setFullText(originalText); break;
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
		super.initGui();
		if (textarea != null) { text = textarea.getFullText(); }
		textarea = new GuiTextArea(2, guiLeft + 5, guiTop + 5, xSize - 68, ySize - 10, text);
		textarea.setListener(this);
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
