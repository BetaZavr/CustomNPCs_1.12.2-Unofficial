package noppes.npcs.client.gui.model;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import javax.imageio.ImageIO;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;

public class GuiModelColor
extends SubGuiInterface
implements ITextfieldListener {
	
	public interface ColorCallback {
		void color(int p0);
	}

	private static ResourceLocation colorgui = new ResourceLocation("moreplayermodels:textures/gui/color_gui.png");
	private static ResourceLocation colorPicker = new ResourceLocation("moreplayermodels:textures/gui/color.png");
	private ResourceLocation npcSkin;
	private ColorCallback callback;
	public int color, colorX, colorY, hover;
	public boolean hovered;
	public GuiScreen parent;
	private BufferedImage bufferColor, bufferSkin;

	private GuiNpcTextField textfield;

	public GuiModelColor(GuiScreen parent, int color, ColorCallback callback) {
		this.parent = parent;
		this.callback = callback;
		this.ySize = 230;
		this.closeOnEsc = false;
		this.color = color;
		this.background = GuiModelColor.colorgui;
		this.npcSkin = null;
		this.hover = 0;
		this.hovered = false;
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id == 66) {
			this.close();
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.renderEngine.bindTexture(GuiModelColor.colorPicker);
		this.drawTexturedModalRect(this.colorX, this.colorY, 0, 0, 120, 120);
		if (this.npcSkin != null) {
			this.mc.renderEngine.bindTexture(GuiModelColor.colorgui);
			int x = this.colorX + 128;
			int y = this.colorY;
			this.drawTexturedModalRect(x + 1, y-5, 9, 0, 136, 124);
			this.drawTexturedModalRect(x, y+119, 8, 169, 137, 4);
			GlStateManager.pushMatrix();
			GlStateManager.translate(x + 4, y, 0.0f);
			GlStateManager.scale(0.46f, 0.46f, 0.46f);
			this.mc.renderEngine.bindTexture(this.npcSkin);
			this.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
			GlStateManager.popMatrix();
			
		} else if (this.npc!=null && this.bufferSkin==null && !this.npc.display.getSkinTexture().isEmpty()) {
			this.npcSkin = new ResourceLocation(this.npc.display.getSkinTexture());
			InputStream stream = null;
			try {
				IResource resource = this.mc.getResourceManager().getResource(this.npcSkin);
				this.bufferSkin = ImageIO.read(stream = resource.getInputStream());
			} catch (IOException ex) {}
			finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException ex2) { }
				}
			}
		}
		this.hovered = false;
		if (this.bufferColor==null) {
			InputStream stream = null;
			try {
				IResource resource = this.mc.getResourceManager().getResource(GuiModelColor.colorPicker);
				this.bufferColor = ImageIO.read(stream = resource.getInputStream());
			} catch (IOException ex) {}
			finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException ex2) { }
				}
			}
		} else if (mouseX >= this.colorX && mouseX <= this.colorX + 120 && mouseY >= this.colorY && mouseY <= this.colorY + 120) {
			this.hover = this.bufferColor.getRGB((mouseX - this.colorX) * 4, (mouseY - this.colorY) * 4) & 0xFFFFFF;
			this.hovered = true;
		} else if (this.bufferSkin!=null) {
			int x = (int) ((float) (mouseX - this.colorX - 132) / 0.46f);
			int y = (int) ((float) (mouseY - this.colorY) / 0.46f);
			if (x>=0 && x<256 && y>=0 && y<256) {
				float w = 256.0f / (float) this.bufferSkin.getWidth();
				float h = 256.0f / (float) this.bufferSkin.getHeight();
				try { this.hover = this.bufferSkin.getRGB((int) ((float) x / w), (int) ((float) y / h)) & 0xFFFFFF; }
				catch (Exception e) {}
				this.hovered = true;
			}
		}
		GlStateManager.pushMatrix();
		GlStateManager.translate(this.colorX + 5, this.colorY - 25, 1.0f);
		this.drawGradientRect(-1, -1, 21, 21, 0x80000000, 0x80000000);
		this.drawGradientRect(0, 0, 20, 20, 0xFF000000 + this.hover, 0xFF000000 + this.hover);
		this.drawGradientRect(0, 0, 20, 20, this.hover, this.hover);
		GlStateManager.popMatrix();
		if (!CustomNpcs.ShowDescriptions) { return; }
		if (this.getTextField(0)!=null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.set.color").getFormattedText());
		} else if (this.getButton(66)!=null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}

	public String getColor() {
		String str;
		for (str = Integer.toHexString(this.color); str.length() < 6; str = "0" + str) {
		}
		return str;
	}

	@Override
	public void initGui() {
		super.initGui();
		this.colorX = this.guiLeft + 4;
		this.colorY = this.guiTop + 50;
		this.addTextField(this.textfield = new GuiNpcTextField(0, this, this.guiLeft + 35, this.guiTop + 25, 60, 20,
				this.getColor()));
		this.addButton(new GuiNpcButton(66, this.guiLeft + 107, this.guiTop + 8, 20, 20, "X"));
		this.textfield.setTextColor(this.color);
	}

	@Override
	public void keyTyped(char c, int i) {
		String prev = this.textfield.getText();
		super.keyTyped(c, i);
		String newText = this.textfield.getText();
		if (newText.equals(prev)) {
			return;
		}
		try {
			this.color = Integer.parseInt(this.textfield.getText(), 16);
			this.callback.color(this.color);
			this.textfield.setTextColor(this.color);
		} catch (NumberFormatException e) {
			this.textfield.setText(prev);
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseBottom) {
		super.mouseClicked(mouseX, mouseY, mouseBottom);
		if (this.hovered && this.hover!=0) {
			this.color = this.hover;
			this.callback.color(this.hover);
			this.textfield.setTextColor(this.hover);
			this.textfield.setText(this.getColor());
		}
	}

	@Override
	public void save() {
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		try {
			this.color = Integer.parseInt(textfield.getText(), 16);
		} catch (NumberFormatException e) {
			this.color = 0;
		}
		this.callback.color(this.color);
		textfield.setTextColor(this.color);
	}

}
