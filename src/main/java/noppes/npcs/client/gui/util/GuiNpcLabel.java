package noppes.npcs.client.gui.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.util.Util;

public class GuiNpcLabel
implements IComponentGui, IGuiNpcLabel {

	public boolean enabled = true;
	public boolean hovered;
	public int backColor = 0;
	public int borderColor = 0;
	public int height = 9;
	public int width = 0;
	public int color;
	public int id;
	public int x;
	public int y;
	public List<String> label;
	private final List<String> hoverText = new ArrayList<>();

	public GuiNpcLabel(int id, Object label, int x, int y) {
		this(id, label, x, y, CustomNpcResourceListener.DefaultTextColor);
	}

	public GuiNpcLabel(int id, Object label, int x, int y, int color) {
		this.id = id;
		this.x = x;
		this.y = y;
		this.color = color;
		setLabel(label.toString());
	}

	@Override
	public void setCenter(int width) {
		x += (width - this.width) / 2;
	}

	@Override
	public List<String> getLabels() { return label; }

	@Override
	public void render(IEditNPC gui, int mouseX, int mouseY, float partialTicks) {
		if (!enabled) { return; }
		hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
		if (hovered && !hoverText.isEmpty()) { gui.setHoverText(hoverText); }
		if (label == null || label.isEmpty()) { return; }
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		if (borderColor != 0) {
			Gui.drawRect(x - 2, y - 1, x + width + 2, y + height, borderColor);
		}
		if (backColor != 0) {
			Gui.drawRect(x - 1, y, x + width + 1, y + height - 1, backColor);
		}
		int i = 0;
		for (String str : label) {
			Minecraft.getMinecraft().fontRenderer.drawString(str, x, y + i, color);
			i += 10;
		}
	}

	@Override
	public void setLabel(Object labels) {
		if (labels == null) {
			label = null;
			height = 10;
			width = 0;
			return;
		}

		if (labels.toString().contains("\n")) {
			List<String> list = new ArrayList<>();
			String text = labels.toString();
			while (text.contains("\n")) {
				list.add(text.substring(0, text.indexOf("\n")));
				text = text.substring(text.indexOf("\n") + 1);
			}
			list.add(text);
			labels = list;
		}

		if (labels instanceof String[]) {
			labels = new ArrayList<>(Arrays.asList((String[]) labels));
		}
		if (labels instanceof List) {
			if (((List<?>) labels).size() == 1) {
				String str = ((List<?>) labels).get(0) == null ? "" : new TextComponentTranslation(labels.toString()).getFormattedText();
				label = Collections.singletonList(str);
				height = 10;
				width = Minecraft.getMinecraft().fontRenderer.getStringWidth(Util.instance.deleteColor(str));
				return;
			}
			label = new ArrayList<>();
			height = 10 * ((List<?>) labels).size();
			width = 0;
			for (Object obj : (List<?>) labels) {
				String str = new TextComponentTranslation(obj.toString()).getFormattedText();
				label.add(str);
				int w = Minecraft.getMinecraft().fontRenderer.getStringWidth(Util.instance.deleteColor(str));
				if (width < w) { width = w; }
			}
		} else {
			String str = labels.toString();
			try { str = new TextComponentTranslation(labels.toString()).getFormattedText(); } catch (Exception ignored) { }
			label = Collections.singletonList(str);
			height = 10;
			width = Minecraft.getMinecraft().fontRenderer.getStringWidth(Util.instance.deleteColor(str));
		}
	}

	@Override
	public void setBackColor(int color) { backColor = color; }

	@Override
	public int getBorderColor() { return borderColor; }

	@Override
	public void setBorderColor(int color) { borderColor = color; }

	@Override
	public int getColor() { return color; }

	@Override
	public int getBackColor() { return backColor; }

	@Override
	public int getID() { return id; }

	@Override
	public int[] getCenter() { return new int[] { x + width / 2, y + height / 2}; }

	@Override
	public void setHoverText(String text, Object ... args) {
		hoverText.clear();
		if (text == null || text.isEmpty()) { return; }
		if (!text.contains("%")) { text = new TextComponentTranslation(text, args).getFormattedText(); }
		if (text.contains("~~~")) { text = text.replaceAll("~~~", "%"); }
		while (text.contains("<br>")) {
			hoverText.add(text.substring(0, text.indexOf("<br>")));
			text = text.substring(text.indexOf("<br>") + 4);
		}
		hoverText.add(text);
	}

	@Override
	public int getLeft() { return x; }

	@Override
	public int getTop() { return y; }

	@Override
	public void setLeft(int left) { x = left; }

	@Override
	public void setTop(int top) { y = top; }

	@Override
	public int getWidth() { return width; }

	@Override
	public int getHeight() { return height; }

	@Override
	public void customKeyTyped(char c, int id) { }

	@Override
	public void customMouseClicked(int mouseX, int mouseY, int mouseButton) { }

	@Override
	public void customMouseReleased(int mouseX, int mouseY, int mouseButton) { }

	@Override
	public boolean isVisible() { return enabled; }

	@Override
	public void setVisible(boolean bo) { enabled = bo; }

	@Override
	public boolean isEnabled() { return enabled; }

	@Override
	public void setEnabled(boolean bo) { enabled = bo; }

	@Override
	public boolean isMouseOver() { return hovered; }

	@Override
	public void setColor(int color) { this.color = color; }

}
