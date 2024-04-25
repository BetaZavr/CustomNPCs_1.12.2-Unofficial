package noppes.npcs.client.gui.util;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.util.AdditionalMethods;

public class GuiNpcLabel {

	public int backColor = 0, borderColor = 0;
	public int color;
	public boolean enabled;
	public int id;
	public List<String> label;
	public int x;
	public int y;
	public int height = 9, width = 0;
	public String[] hoverText;
	public boolean hovered;

	public GuiNpcLabel(int id, Object label, int x, int y) {
		this(id, label, x, y, CustomNpcResourceListener.DefaultTextColor);
	}

	public GuiNpcLabel(int id, Object label, int x, int y, int color) {
		this.enabled = true;
		this.id = id;
		this.x = x;
		this.y = y;
		this.color = color;
		this.setLabel(label.toString());
	}

	public void center(int width) {
		this.x += (width - this.width) / 2;
	}

	public void drawLabel(GuiScreen gui, FontRenderer fontRenderer, int mouseX, int mouseY, float partialTicks) {
		if (this.enabled && this.label != null && this.label.size() != 0) {
			this.hovered = mouseX >= this.x && mouseY >= this.y && mouseX < this.x + this.width
					&& mouseY < this.y + this.height;
			GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
			if (this.hovered && this.hoverText != null) {
				GlStateManager.color(0.5f, 0.5f, 0.5f, 0.5f);
				if (this.hoverText != null) {
					if (gui instanceof GuiContainerNPCInterface) {
						((GuiContainerNPCInterface) gui).hoverText = this.hoverText;
					} else if (gui instanceof GuiNPCInterface) {
						((GuiNPCInterface) gui).hoverText = this.hoverText;
					}
				}
			}
			if (this.borderColor != 0) {
				Gui.drawRect(this.x - 2, this.y - 1, this.x + this.width + 2, this.y + this.height, this.borderColor);
			}
			if (this.backColor != 0) {
				Gui.drawRect(this.x - 1, this.y, this.x + this.width + 1, this.y + this.height - 1, this.backColor);
			}
			int i = 0;
			for (String str : this.label) {
				fontRenderer.drawString(str, this.x, this.y + i, this.color);
				i += 10;
			}
		}
	}

	public void setLabel(Object labels) {
		if (labels == null) {
			this.label = null;
			this.height = 10;
			this.width = 0;
			return;
		}
		char chr = Character.toChars(0x000A)[0];

		if (labels.toString().indexOf(chr) != -1) {
			List<String> list = Lists.newArrayList();
			String text = labels.toString();
			while (text.indexOf(chr) != -1) {
				list.add(text.substring(0, text.indexOf(chr)));
				text = text.substring(text.indexOf(chr) + 1);
			}
			list.add(text);
			labels = list;
		}

		if (labels instanceof String[]) {
			labels = Lists.newArrayList((String[]) labels);
		}
		if (labels instanceof List) {
			if (((List<?>) labels).size() == 1) {
				String str = ((List<?>) labels).get(0) == null ? ""
						: new TextComponentTranslation(labels.toString()).getFormattedText();
				this.label = Lists.newArrayList(str);
				this.height = 10;
				this.width = Minecraft.getMinecraft().fontRenderer
						.getStringWidth(AdditionalMethods.instance.deleteColor(str));
				return;
			}
			this.label = Lists.newArrayList();
			this.height = 10 * ((List<?>) labels).size();
			this.width = 0;
			for (Object obj : (List<?>) labels) {
				String str = new TextComponentTranslation(obj.toString()).getFormattedText();
				this.label.add(str);
				int w = Minecraft.getMinecraft().fontRenderer
						.getStringWidth(AdditionalMethods.instance.deleteColor(str));
				if (this.width < w) {
					this.width = w;
				}
			}
		} else {
			String str = labels.toString();
			try {
				str = new TextComponentTranslation(labels.toString()).getFormattedText();
			} catch (Exception e) {
			}
			this.label = Lists.newArrayList(str);
			this.height = 10;
			this.width = Minecraft.getMinecraft().fontRenderer
					.getStringWidth(AdditionalMethods.instance.deleteColor(str));
		}
	}
}
