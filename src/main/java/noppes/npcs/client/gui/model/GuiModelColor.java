package noppes.npcs.client.gui.model;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.LogWriter;
import noppes.npcs.client.gui.select.GuiTextureSelection;
import noppes.npcs.client.gui.util.*;

public class GuiModelColor extends SubGuiInterface implements ITextfieldListener {

	public interface ColorCallback {
		void color(int p0);
	}

	private static final ResourceLocation colorGui = new ResourceLocation("moreplayermodels:textures/gui/color_gui.png");
	private static final ResourceLocation colorPicker = new ResourceLocation("moreplayermodels:textures/gui/color.png");
	private ResourceLocation npcSkin;
	private final ColorCallback callback;
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
		this.background = GuiModelColor.colorGui;
		this.npcSkin = null;
		this.hover = 0;
		this.hovered = false;
	}

	@Override
	protected void actionPerformed(@Nonnull GuiButton guibutton) {
		if (guibutton.id == 66) {
			this.close();
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		
		if (this.npcSkin != null) {
			// back
			this.mc.getTextureManager().bindTexture(GuiModelColor.colorGui);
			int xs = this.colorX + 128;
			int ys = this.colorY;
			this.drawTexturedModalRect(xs + 3, ys - 5, 11, 0, 134, 1);
			this.drawTexturedModalRect(xs + 2, ys - 4, 10, 1, 135, 1);
			this.drawTexturedModalRect(xs + 1, ys - 3, 9, 2, 136, 122);
			this.drawTexturedModalRect(xs, ys + 119, 8, 169, 137, 4);
			
			GlStateManager.pushMatrix();
			GlStateManager.translate(xs + 4, ys, 0.0f);
			GlStateManager.scale(0.46f, 0.46f, 0.46f);
			this.mc.getTextureManager().bindTexture(this.npcSkin);
			
			Gui.drawRect(-1, -1, 258, 258, GuiTextureSelection.dark ? 0xFFE0E0E0 : 0xFF202020);
			Gui.drawRect(0, 0, 256, 256, GuiTextureSelection.dark ? 0xFF000000 : 0xFFFFFFFF);
			int g = 16;
			for (int u = 0; u < 16; u ++) {
				for (int v = 0; v < 16; v ++) {
					if (u % 2 == (v % 2 == 0 ? 1 : 0)) {
						Gui.drawRect(u * g, v * g, u * g + g, v * g + g, GuiTextureSelection.dark ? 0xFF343434 : 0xFFCCCCCC);
					}
				}
			}
			
			this.drawTexturedModalRect(0, 0, 0, 0, 256, 256);
			GlStateManager.popMatrix();

		} else if (this.npc != null && this.bufferSkin == null && !this.npc.display.getSkinTexture().isEmpty()) {
			this.npcSkin = new ResourceLocation(this.npc.display.getSkinTexture());
			InputStream stream = null;
			try {
				IResource resource = this.mc.getResourceManager().getResource(this.npcSkin);
				this.bufferSkin = ImageIO.read(stream = resource.getInputStream());
			} catch (IOException e) {
				LogWriter.error("Error:", e);
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException ex) { LogWriter.error("Error:", ex); }
				}
			}
		}
		this.hovered = false;
		if (this.bufferColor == null) {
			InputStream stream = null;
			try {
				IResource resource = this.mc.getResourceManager().getResource(GuiModelColor.colorPicker);
				this.bufferColor = ImageIO.read(stream = resource.getInputStream());
			}
			catch (IOException e) { LogWriter.error("Error:", e); }
			finally {
				if (stream != null) {
					try { stream.close(); }
					catch (IOException ex) { LogWriter.error("Error:", ex); }
				}
			}
		}

		int x = this.colorX + 4, y = this.colorY;
		this.drawGradientRect(x - 2, y - 2, x + 120, y + 119, 0xFFF0F0F0, 0xFF202020);
		this.drawGradientRect(x - 1, y - 1, x + 119, y + 118, 0xFF202020, 0xFFF0F0F0);
		this.mc.getTextureManager().bindTexture(GuiModelColor.colorPicker);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.drawTexturedModalRect(x, y, 0, 1, 120, 120);
		
		if (this.bufferColor != null && isMouseHover(mouseX, mouseY, x, y, 117, 117)) {
			int xb = (mouseX - x) * 4;
			int yb = (mouseY - y + 1) * 4;
			this.hover = this.bufferColor.getRGB(xb, yb) & 0xFFFFFF;
			this.hovered = true;
		}
		else {
			x = this.colorX + 132;
			if (this.bufferSkin != null && isMouseHover(mouseX, mouseY, x, y, 118, 118)) {
				float xb = (float) (mouseX - x) / 0.458823f;
				float yb = (float) (mouseY - y) / 0.458823f;
				float w = 256.0f / (float) this.bufferSkin.getWidth();
				float h = 256.0f / (float) this.bufferSkin.getHeight();
				try { this.hover = this.bufferSkin.getRGB((int) (xb / w), (int) (yb / h)) & 0xFFFFFF; }
				catch (Exception e) { LogWriter.error("Error:", e); }
				this.hovered = true;
			}
		}
		GlStateManager.pushMatrix();
		GlStateManager.translate(this.colorX + 5, this.colorY - 25, 1.0f);
		int c = this.hovered ? this.hover : this.color;
		this.drawGradientRect(-1, -1, 21, 21, 0x80000000, 0x80000000);
		this.drawGradientRect(0, 0, 20, 20, 0xFF000000 + c, 0xFF000000 + c);
		this.drawGradientRect(0, 0, 20, 20, c, c);
		GlStateManager.popMatrix();
	}

	public String getColor() {
		StringBuilder str = new StringBuilder(Integer.toHexString(this.color));
		while (str.length() < 6) { str.insert(0, "0"); }
		return str.toString();
	}

	@Override
	public void initGui() {
		super.initGui();
		this.colorX = guiLeft + 4;
		this.colorY = guiTop + 50;
		textfield = new GuiNpcTextField(0, this, this.guiLeft + 35, this.guiTop + 25, 60, 20, getColor());
		textfield.setHoverText("hover.set.color");
		addTextField(textfield);
		GuiNpcButton button = new GuiNpcButton(66, this.guiLeft + 107, this.guiTop + 8, 20, 20, "X");
		button.setHoverText("hover.back");
		addButton(button);
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
		} catch (NumberFormatException e) {
			this.textfield.setText(prev);
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseBottom) {
		super.mouseClicked(mouseX, mouseY, mouseBottom);
		if (this.hovered && this.hover != 0) {
			this.color = this.hover;
			this.callback.color(this.hover);
			this.textfield.setText(this.getColor());
		}
	}

    @Override
	public void unFocused(IGuiNpcTextField textfield) {
		try {
			this.color = Integer.parseInt(textfield.getText(), 16);
		} catch (NumberFormatException e) {
			this.color = 0;
		}
		this.callback.color(this.color);
	}

}
