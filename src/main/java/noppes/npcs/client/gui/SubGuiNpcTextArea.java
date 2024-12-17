package noppes.npcs.client.gui;

import noppes.npcs.NoppesStringUtils;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiTextArea;
import noppes.npcs.client.gui.util.ITextChangeListener;
import noppes.npcs.client.gui.util.SubGuiInterface;

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
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 100) {
			NoppesStringUtils.setClipboardContents(textarea.getText());
		}
		if (button.id == 101) {
			textarea.setText(NoppesStringUtils.getClipboardContents());
		}
		if (button.id == 102) {
			textarea.setText("");
		}
		if (button.id == 103) {
			textarea.setText(originalText);
		}
		if (button.id == 0) {
			close();
		}
	}

	public SubGuiNpcTextArea enableHighlighting() {
		highlighting = true;
		return this;
	}

	public int getId() {
		return id;
	}

	@Override
	public void initGui() {
		xSize = (int) (width * 0.88);
		ySize = (int) (xSize * 0.56);
		if (ySize > height * 0.95) {
			ySize = (int) (height * 0.95);
			xSize = (int) (ySize / 0.56);
		}
		bgScale = xSize / 440.0f;
		super.initGui();
		if (textarea != null) {
			text = textarea.getText();
		}
		int yoffset = (int) (ySize * 0.02);
		(textarea = new GuiTextArea(2, guiLeft + 1 + yoffset, guiTop + yoffset, xSize - 100 - yoffset, ySize - yoffset * 2, text)).setListener(this);
		if (highlighting) {
			textarea.enableCodeHighlighting();
		}
		add(textarea);
		buttonList.add(new GuiNpcButton(102, guiLeft + xSize - 90 - yoffset, guiTop + 20, 56, 20, "gui.clear"));
		buttonList.add(new GuiNpcButton(101, guiLeft + xSize - 90 - yoffset, guiTop + 43, 56, 20, "gui.paste"));
		buttonList.add(new GuiNpcButton(100, guiLeft + xSize - 90 - yoffset, guiTop + 66, 56, 20, "gui.copy"));
		buttonList.add(new GuiNpcButton(103, guiLeft + xSize - 90 - yoffset, guiTop + 89, 56, 20, "remote.reset"));
		buttonList.add(new GuiNpcButton(0, guiLeft + xSize - 90 - yoffset, guiTop + 160, 56, 20, "gui.close"));
		xSize = 420;
		ySize = 256;
	}

	@Override
	public void textUpdate(String t) { text = t; }

}
