package noppes.npcs.client.gui.script;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcCheckBox;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.SubGuiInterface;

public class GuiScriptEncrypt extends SubGuiInterface {

	public String path, ext;
	public boolean onlyTab, send;

	public GuiScriptEncrypt(String path, String ext) {
		super();
		this.xSize = 176;
		this.ySize = 80;
		this.setBackground("smallbg.png");
		this.closeOnEsc = true;
		this.onlyTab = true;
		while (path.contains("\\")) {
			path = path.replace("\\", "/");
		}
		if (path.contains("./")) {
			path = path.substring(path.indexOf("./"));
		}
		this.path = path + "/";
		this.ext = ext.replace(".", ".p");
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		switch (button.id) {
		case 0: {
			if (!(button instanceof GuiNpcCheckBox)) {
				return;
			}
			GuiNpcCheckBox checkBox = (GuiNpcCheckBox) button;
			this.onlyTab = checkBox.isSelected();
			checkBox.setText(this.onlyTab ? "encrypt.only.tab" : "encrypt.all.scripts");
			break;
		}
		case 1: {
			this.send = true;
			this.close();
			break;
		}
		case 66: {
			this.close();
			break;
		}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		this.drawDefaultBackground();
		if (this.getButton(1) != null && this.getTextField(0) != null) {
			this.getButton(1).setEnabled(!this.getTextField(0).getText().isEmpty());
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.ShowDescriptions) {
			return;
		}
		if (this.getTextField(0) != null && this.getTextField(0).isMouseOver()) {
			String path = this.path + this.getTextField(0).getText() + ext;
			this.setHoverText(new TextComponentTranslation("encrypt.hover.path", path).getFormattedText());
		} else if (this.getButton(0) != null && this.getButton(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("encrypt.hover.type." + this.onlyTab).getFormattedText());
		} else if (this.getButton(1) != null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("encrypt.hover.encrypt").getFormattedText());
		} else if (this.getButton(66) != null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = this.guiLeft + 5;
		int y = this.guiTop + 14;
		this.addLabel(
				new GuiNpcLabel(0, new TextComponentTranslation("gui.path", ":").getFormattedText(), x + 2, y - 10));
		GuiNpcTextField textField = new GuiNpcTextField(0, this, x, y, 166, 20, "default");
		textField.prohibitedSpecialChars = GuiNpcTextField.filePath;
		this.addTextField(textField);
		GuiNpcCheckBox checkBox = new GuiNpcCheckBox(0, x + 1, y += 22, 164, 16, "");
		checkBox.setText(this.onlyTab ? "encrypt.only.tab" : "encrypt.all.scripts");
		checkBox.setSelected(this.onlyTab);
		this.addButton(checkBox);
		this.addButton(new GuiNpcButton(66, x, y += 20, 82, 20, "gui.back"));
		this.addButton(new GuiNpcButton(1, x + 84, y, 82, 20, "gui.encrypt"));
	}

}
