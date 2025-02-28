package noppes.npcs.client.gui.script;

import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.gui.util.*;

public class GuiScriptEncrypt
extends SubGuiInterface {

	public String path;
	public String ext;
	public boolean onlyTab;
	public boolean send;

	public GuiScriptEncrypt(String pathStr, String extStr) {
		super();
		xSize = 176;
		ySize = 80;
		setBackground("smallbg.png");
		closeOnEsc = true;

		onlyTab = true;
		pathStr = pathStr.replaceAll("\\\\", "/");
		if (pathStr.contains("./")) { pathStr = pathStr.substring(pathStr.indexOf("./")); }
		path = pathStr + "/";
		ext = extStr.replace(".", ".p");
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		switch (button.getId()) {
			case 0: {
				if (!(button instanceof GuiNpcCheckBox)) { return; }
				onlyTab = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 1: {
				send = true;
				close();
				break;
			}
			case 66: {
				close();
				break;
			}
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		drawDefaultBackground();
		if (getButton(1) != null && getTextField(0) != null) { getButton(1).setEnabled(!getTextField(0).getText().isEmpty()); }
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = guiLeft + 5;
		int y = guiTop + 14;
		addLabel(new GuiNpcLabel(0, new TextComponentTranslation("gui.path", ":").getFormattedText(), x + 2, y - 10));
		GuiNpcTextField textField = new GuiNpcTextField(0, this, x, y, 166, 20, "default");
		textField.prohibitedSpecialChars = GuiNpcTextField.filePath;
		textField.setHoverText("encrypt.hover.path", path + textField.getText() + ext);
		addTextField(textField);
		GuiNpcButton button = new GuiNpcCheckBox(0, x + 1, y += 22, 164, 16, "encrypt.only.tab", "encrypt.all.scripts", onlyTab);
		button.setHoverText("encrypt.hover.type." + onlyTab);
		addButton(button);
		button = new GuiNpcButton(66, x, y += 20, 82, 20, "gui.back");
		button.setHoverText("hover.back");
		addButton(button);
		button = new GuiNpcButton(1, x + 84, y, 82, 20, "gui.encrypt");
		button.setHoverText("encrypt.hover.encrypt");
		addButton(button);
	}

}
