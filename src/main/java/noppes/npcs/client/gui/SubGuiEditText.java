package noppes.npcs.client.gui;

import net.minecraft.client.renderer.GlStateManager;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.util.Util;

public class SubGuiEditText
extends SubGuiInterface {

	public boolean cancelled;
	public int[] numbersOnly; // min, max, def
	public String label;
	public String[] hovers;
	public String[] text;
	public boolean latinAlphabetOnly = false;
	public boolean allowUppercase = true;

	public SubGuiEditText(int id, String text) {
		this(new String[] { text });
		this.id = id;
	}

	public SubGuiEditText(int id, String[] texts) {
		this(texts);
		this.id = id;
	}

	public SubGuiEditText(String[] texts) {
		numbersOnly = null;
		label = null;
		cancelled = true;
		text = new String[Math.min(texts.length, 5)];
		hovers = new String[Math.min(texts.length, 5)];
		for (int i = 0; i < texts.length && i < 5; i++) {
			text[i] = Util.instance.deleteColor(texts[i]);
			hovers[i] = "";
		}
		setBackground("smallbg.png");
		closeOnEsc = true;
		xSize = 176;
		ySize = 49 + text.length * 22;
	}

	@Override
	public void buttonEvent(IGuiNpcButton button) {
		if (button.getID() == 0) {
			cancelled = false;
			for (int i = 0; i < text.length; i++) {
				text[i] = getTextField(i).getFullText();
			}
		}
		close();
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		for (int i = 0; i < hovers.length; i++) {
			if (hovers[i] == null || hovers[i].isEmpty() || getTextField(i) == null) {
				continue;
			}
			if (isMouseHover(mouseX, mouseY, guiLeft + 6, guiTop + 16 + i * 22, 164, 16)) {
				setHoverText(hovers[i]);
				break;
			}
		}
		super.drawScreen(mouseX, mouseY, partialTicks);
		GlStateManager.pushMatrix();
		GlStateManager.translate(guiLeft, guiTop, 0.0f);
		GlStateManager.scale(bgScale, bgScale, bgScale);
		mc.getTextureManager().bindTexture(background);
		GlStateManager.color(2.0f, 2.0f, 2.0f, 1.0f);
		if (xSize > 256) {
			drawTexturedModalRect(0, ySize - 1, 0, 218, 250, ySize);
			drawTexturedModalRect(250, ySize - 1, 256 - (xSize - 250), 218, xSize - 250, ySize);
		} else {
			drawTexturedModalRect(0, ySize - 1, 0, 218, xSize, 4);
		}
		GlStateManager.popMatrix();
	}

	@Override
	public void initGui() {
		super.initGui();
		for (int i = 0; i < text.length && i < 5; i++) {
			addTextField(new GuiNpcTextField(i, parent, guiLeft + 4, guiTop + 16 + i * 22 + (label != null ? 2 : 0), 168, 20, text[i]));
			if (numbersOnly != null) {
				getTextField(i).setMinMaxDefault(numbersOnly[0], numbersOnly[1], numbersOnly[2]);
			}
			getTextField(i).setLatinAlphabetOnly(latinAlphabetOnly);
			getTextField(i).setAllowUppercase(allowUppercase);
		}
		addButton( new GuiNpcButton(0, guiLeft + 4, guiTop + 22 + text.length * 22, 80, 20, "gui.done"));
		addButton( new GuiNpcButton(1, guiLeft + 90, guiTop + 22 + text.length * 22, 80, 20, "gui.cancel"));
		if (label != null) {
			addLabel(new GuiNpcLabel(0, label, guiLeft + 7, guiTop + 5));
		}
	}

    public void setHoverTexts(String[] newHovers) {
		for (int i = 0; i < hovers.length && i < newHovers.length; i++) {
			hovers[i] = newHovers[i];
		}
	}

}
