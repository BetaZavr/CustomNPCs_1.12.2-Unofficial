package noppes.npcs.client.gui.script;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.gui.util.*;

import javax.annotation.Nonnull;

public class SubGuiScriptEncrypt extends SubGuiInterface {

	public String path;
	public String ext;
	public boolean onlyTab;
	public boolean send;

	public SubGuiScriptEncrypt(String pathStr, String extStr) {
		super(1);
		setBackground("smallbg.png");
		closeOnEsc = true;
		xSize = 176;
		ySize = 80;

		onlyTab = true;
		pathStr = pathStr.replaceAll("\\\\", "/");
		if (pathStr.contains("./")) { pathStr = pathStr.substring(pathStr.indexOf("./")); }
		path = pathStr + "/";
		ext = extStr.replace(".", ".p");
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: {
				if (!(button instanceof GuiNpcCheckBox)) { return; }
				onlyTab = ((GuiNpcCheckBox) button).isSelected();
				break;
			}
			case 1: send = true; onClosed(); break;
			case 66: onClosed(); break;
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		GlStateManager.translate(0.0f, 0.0f, id);
		drawDefaultBackground();
		if (getButton(1) != null && getTextField(0) != null) { getButton(1).setIsEnable(!getTextField(0).getText().isEmpty()); }
		super.drawScreen(mouseX, mouseY, partialTicks);
	}

	@Override
	public void initGui() {
		super.initGui();
		int x = guiLeft + 5;
		int y = guiTop + 14;
		addLabel(new GuiNpcLabel(0, new TextComponentTranslation("gui.path", ":").getFormattedText(), x + 2, y - 10));
		addTextField(new GuiNpcTextField(0, this, x, y, 166, 20, "default")
				.setHoverText(new TextComponentTranslation("encrypt.hover.path", path + "default" + ext)));
		getTextField(0).prohibitedSpecialChars = GuiNpcTextField.filePath;
		addButton(new GuiNpcCheckBox(0, x + 1, y += 22, 164, 16, "encrypt.only.tab", "encrypt.all.scripts", onlyTab)
				.setHoverText("encrypt.hover.type." + onlyTab));
		addButton(new GuiNpcButton(66, x, y += 20, 82, 20, "gui.back")
				.setHoverText("hover.back"));
		addButton(new GuiNpcButton(1, x + 84, y, 82, 20, "gui.encrypt")
				.setHoverText("encrypt.hover.encrypt"));
	}

}
