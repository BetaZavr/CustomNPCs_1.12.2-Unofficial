package noppes.npcs.client.gui.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.util.Util;

public class GuiNpcLabel implements IComponentGui {

	protected final List<String> hoverText = new ArrayList<>();
	protected final FontRenderer font;

	protected String message;
	protected boolean hovered;
	protected int backColor = 0;
	protected int borderColor = 0;
	protected int color;
	public boolean enabled = true;
	public boolean hasShadow = false;
	public int height = 9;
	public int width = 0;
	public int id;
	public int x;
	public int y;

	public GuiNpcLabel(int id, Object label, int x, int y) {
		this(id, label, x, y, CustomNpcResourceListener.DefaultTextColor);
	}

	public GuiNpcLabel(int idIn, Object label, int xIn, int yIn, int colorIn) {
		id = idIn;
		x = xIn;
		y = yIn;
		color = colorIn;
		font = Minecraft.getMinecraft().fontRenderer;
		setLabel(label instanceof ITextComponent ? ((ITextComponent) label).getFormattedText() : label.toString());
	}

	public GuiNpcLabel(int id, Object obj, int x, int y, int widthIn, int heightIn) {
		this(id, obj, x, y, CustomNpcResourceListener.DefaultTextColor);
		width = widthIn;
		height = heightIn;
	}

	public GuiNpcLabel setCenter(int widthIn) { x += (widthIn - width) / 2; return this; }

	@Override
	public void render(IEditNPC gui, int mouseX, int mouseY, float partialTicks) {
		if (!enabled) { return; }
		hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
		if (hovered && !hoverText.isEmpty()) { gui.putHoverText(hoverText); }
		if (message == null || message.isEmpty()) { return; }
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		if (borderColor != 0) {
			Gui.drawRect(x - 2, y - 1, x + width + 2, y + height, borderColor);
		}
		if (backColor != 0) {
			Gui.drawRect(x - 1, y, x + width + 1, y + height - 1, backColor);
		}
		GuiNpcButton.renderString(font, message,
				x, y, x + width, y + height,
				color, hasShadow, false);
	}

	@Override
	public boolean keyCnpcsPressed(char typedChar, int keyCode) { return false; }

	@Override
	public boolean mouseCnpcsPressed(int mouseX, int mouseY, int mouseButton) { return enabled && hovered; }

	@Override
	public boolean mouseCnpcsReleased(int mouseX, int mouseY, int state) { return false; }

	@Override
	public int getID() { return id; }

	@Override
	public int[] getCenter() { return new int[] { x + width / 2, y + height / 2}; }

	@Override
	public GuiNpcLabel setHoverText(Object... components) {
		hoverText.clear();
		if (components == null) { return this; }
		noppes.npcs.util.Util.instance.putHovers(hoverText, components);
		return this;
	}

	@Override
	public GuiNpcLabel setIsVisible(boolean isVisible) { enabled = isVisible; return this; }

	@Override
	public void moveTo(int addX, int addY) {
		x += addX;
		y += addY;
	}

	@Override
	public void updateCnpcsScreen() { }

	@Override
	public GuiNpcLabel setIsEnable(boolean isEnable) { enabled = isEnable; return this; }

	@Override
	public List<String> getHoversText() { return hoverText; }

	@Override
	public boolean isHovered() { return hovered; }

	public GuiNpcLabel setColor(int colorIn) { color = colorIn; return this; }

	public GuiNpcLabel setLabel(Object labels) {
		if (labels == null) {
			message = null;
			height = 10;
			width = 0;
			return this;
		}
		if (labels instanceof ITextComponent) { labels = ((ITextComponent) labels).getFormattedText(); }
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
		if (labels instanceof String[]) { labels = new ArrayList<>(Arrays.asList((String[]) labels)); }
		if (labels instanceof List) {
			if (((List<?>) labels).size() == 1) {
				String str = ((List<?>) labels).get(0) == null ? "" : new TextComponentTranslation(labels.toString()).getFormattedText();
				message = str;
				height = 10;
				width = font.getStringWidth(Util.instance.deleteColor(str));
				return this;
			}
			StringBuilder s = new StringBuilder();
			height = 10 * ((List<?>) labels).size();
			width = 0;
			for (Object obj : (List<?>) labels) {
				String str = new TextComponentTranslation(obj.toString()).getFormattedText();
				s.append(str);
				int w = font.getStringWidth(Util.instance.deleteColor(str));
				if (width < w) { width = w; }
			}
			message = s.toString();
		}
		else {
			String str = labels.toString();
			try { str = new TextComponentTranslation(labels.toString()).getFormattedText(); } catch (Exception ignored) { }
			message = str;
			height = 10;
			width = font.getStringWidth(Util.instance.deleteColor(str));
		}
		return this;
	}

	public GuiNpcLabel setBackColor(int color) { backColor = color; return this; }

	public GuiNpcLabel setBorderColor(int color) { borderColor = color; return this; }

	public String getMessage() { return message; }

}
