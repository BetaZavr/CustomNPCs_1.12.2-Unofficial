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
	// New
	private int id = 0;
	public String originalText;
	public String text;
	private GuiTextArea textarea;

	// New
	public SubGuiNpcTextArea(int id, String text) {
		this(text);
		this.id = id;
	}

	public SubGuiNpcTextArea(String text) {
		this.highlighting = false;
		this.text = text;
		this.originalText = text;
		this.setBackground("bgfilled.png");
		this.xSize = 256;
		this.ySize = 256;
		this.closeOnEsc = true;
	}

	public SubGuiNpcTextArea(String originalText, String text) {
		this(text);
		this.originalText = originalText;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 100) {
			NoppesStringUtils.setClipboardContents(this.textarea.getText());
		}
		if (button.id == 101) {
			this.textarea.setText(NoppesStringUtils.getClipboardContents());
		}
		if (button.id == 102) {
			this.textarea.setText("");
		}
		if (button.id == 103) {
			this.textarea.setText(this.originalText);
		}
		if (button.id == 0) {
			this.close();
		}
	}

	public SubGuiNpcTextArea enableHighlighting() {
		this.highlighting = true;
		return this;
	}

	public int getId() {
		return this.id;
	}

	@Override
	public void initGui() {
		this.xSize = (int) (this.width * 0.88);
		this.ySize = (int) (this.xSize * 0.56);
		if (this.ySize > this.height * 0.95) {
			this.ySize = (int) (this.height * 0.95);
			this.xSize = (int) (this.ySize / 0.56);
		}
		this.bgScale = this.xSize / 440.0f;
		super.initGui();
		if (this.textarea != null) {
			this.text = this.textarea.getText();
		}
		int yoffset = (int) (this.ySize * 0.02);
		(this.textarea = new GuiTextArea(2, this, this.guiLeft + 1 + yoffset, this.guiTop + yoffset, this.xSize - 100 - yoffset, this.ySize - yoffset * 2, this.text)).setListener(this);
		if (this.highlighting) {
			this.textarea.enableCodeHighlighting();
		}
		this.add(this.textarea);
		this.buttonList.add(
				new GuiNpcButton(102, this.guiLeft + this.xSize - 90 - yoffset, this.guiTop + 20, 56, 20, "gui.clear"));
		this.buttonList.add(
				new GuiNpcButton(101, this.guiLeft + this.xSize - 90 - yoffset, this.guiTop + 43, 56, 20, "gui.paste"));
		this.buttonList.add(
				new GuiNpcButton(100, this.guiLeft + this.xSize - 90 - yoffset, this.guiTop + 66, 56, 20, "gui.copy"));
		this.buttonList.add(new GuiNpcButton(103, this.guiLeft + this.xSize - 90 - yoffset, this.guiTop + 89, 56, 20,
				"remote.reset"));
		this.buttonList.add(
				new GuiNpcButton(0, this.guiLeft + this.xSize - 90 - yoffset, this.guiTop + 160, 56, 20, "gui.close"));
		this.xSize = 420;
		this.ySize = 256;
	}

	@Override
	public void textUpdate(String text) {
		this.text = text;
	}

}
