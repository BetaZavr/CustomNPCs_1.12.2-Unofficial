package noppes.npcs.client.gui.model;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.LogWriter;
import noppes.npcs.client.gui.select.SubGuiTextureSelection;
import noppes.npcs.client.gui.util.*;
import org.lwjgl.input.Keyboard;

public class SubGuiModelColor extends SubGuiInterface implements ITextfieldListener {

	public interface ColorCallback {
		void color(int colorIn);
	}

	protected static final ResourceLocation colorGui = new ResourceLocation("moreplayermodels:textures/gui/color_gui.png");
	protected static final ResourceLocation colorPicker = new ResourceLocation("moreplayermodels:textures/gui/color.png");
	protected final ColorCallback callback;
	protected ResourceLocation npcSkin = null;
	protected BufferedImage bufferColor;
	protected BufferedImage bufferSkin;
	public boolean hovered = false;
	public int color;
	public int colorX;
	public int colorY;
	public int hover = 0;

	private GuiNpcTextField textfield;

	public SubGuiModelColor(GuiScreen parentIn, int colorIn, ColorCallback callbackIn) {
		super(0);
		background = SubGuiModelColor.colorGui;
		closeOnEsc = false;
		ySize = 230;

		callback = callbackIn;
		parent = parentIn;
		color = colorIn;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		if (button.id == 66) { onClosed(); }
	}

	@Override
	public void postDrawScreen(int mouseX, int mouseY, float partialTicks) {
		hovered = false;
		if (npcSkin != null) {
			// back
			mc.getTextureManager().bindTexture(SubGuiModelColor.colorGui);
			int xs = colorX + 128;
			int ys = colorY;
			drawTexturedModalRect(xs + 3, ys - 5, 11, 0, 134, 1);
			drawTexturedModalRect(xs + 2, ys - 4, 10, 1, 135, 1);
			drawTexturedModalRect(xs + 1, ys - 3, 9, 2, 136, 122);
			drawTexturedModalRect(xs, ys + 119, 8, 169, 137, 4);
			GlStateManager.pushMatrix();
			GlStateManager.translate(xs + 4, ys, 0.0f);
			GlStateManager.scale(0.46f, 0.46f, 0.46f);
			mc.getTextureManager().bindTexture(npcSkin);
			Gui.drawRect(-1, -1, 258, 258, SubGuiTextureSelection.dark ? 0xFFE0E0E0 : 0xFF202020);
			Gui.drawRect(0, 0, 256, 256, SubGuiTextureSelection.dark ? 0xFF000000 : 0xFFFFFFFF);
			int g = 16;
			for (int u = 0; u < 16; u ++) {
				for (int v = 0; v < 16; v ++) {
					if (u % 2 == (v % 2 == 0 ? 1 : 0)) {
						Gui.drawRect(u * g, v * g, u * g + g, v * g + g, SubGuiTextureSelection.dark ? 0xFF343434 : 0xFFCCCCCC);
					}
				}
			}
			drawTexturedModalRect(0, 0, 0, 0, 256, 256);
			GlStateManager.popMatrix();
		}
		else if (npc != null && bufferSkin == null && !npc.display.getSkinTexture().isEmpty()) {
			npcSkin = new ResourceLocation(npc.display.getSkinTexture());
			InputStream stream = null;
			try {
				IResource resource = mc.getResourceManager().getResource(npcSkin);
				bufferSkin = ImageIO.read(stream = resource.getInputStream());
			}
			catch (IOException e) { LogWriter.error(e); }
			finally {
				if (stream != null) {
					try { stream.close(); } catch (IOException ex) { LogWriter.error(ex); }
				}
			}
		}
		if (bufferColor == null) {
			InputStream stream = null;
			try {
				IResource resource = mc.getResourceManager().getResource(SubGuiModelColor.colorPicker);
				bufferColor = ImageIO.read(stream = resource.getInputStream());
			}
			catch (IOException e) { LogWriter.error(e); }
			finally {
				if (stream != null) {
					try { stream.close(); }
					catch (IOException ex) { LogWriter.error(ex); }
				}
			}
		}
		int x = colorX + 4;
		int y = colorY;
		drawGradientRect(x - 2, y - 2, x + 120, y + 119, 0xFFF0F0F0, 0xFF202020);
		drawGradientRect(x - 1, y - 1, x + 119, y + 118, 0xFF202020, 0xFFF0F0F0);
		mc.getTextureManager().bindTexture(SubGuiModelColor.colorPicker);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		drawTexturedModalRect(x, y, 0, 1, 120, 120);
		if (bufferColor != null && isMouseHover(mouseX, mouseY, x, y, 117, 117)) {
			int xb = (mouseX - x) * 4;
			int yb = (mouseY - y + 1) * 4;
			hover = bufferColor.getRGB(xb, yb) & 0xFFFFFF;
			hovered = true;
		}
		else {
			x = colorX + 132;
			if (bufferSkin != null && isMouseHover(mouseX, mouseY, x, y, 118, 118)) {
				float xb = (float) (mouseX - x) / 0.458823f;
				float yb = (float) (mouseY - y) / 0.458823f;
				float w = 256.0f / (float) bufferSkin.getWidth();
				float h = 256.0f / (float) bufferSkin.getHeight();
				try { hover = bufferSkin.getRGB((int) (xb / w), (int) (yb / h)) & 0xFFFFFF; }
				catch (Exception e) { LogWriter.error(e); }
				hovered = true;
			}
		}
		GlStateManager.pushMatrix();
		GlStateManager.translate(colorX + 5, colorY - 25, 1.0f);
		int c = hovered ? hover : color;
		drawGradientRect(-1, -1, 21, 21, 0x80000000, 0x80000000);
		drawGradientRect(0, 0, 20, 20, 0xFF000000 + c, 0xFF000000 + c);
		drawGradientRect(0, 0, 20, 20, c, c);
		GlStateManager.popMatrix();
	}

	public String getColor() {
		StringBuilder str = new StringBuilder(Integer.toHexString(color));
		while (str.length() < 6) { str.insert(0, "0"); }
		return str.toString();
	}

	@Override
	public void initGui() {
		super.initGui();
		colorX = guiLeft + 4;
		colorY = guiTop + 50;
		addTextField(textfield = new GuiNpcTextField(0, this, guiLeft + 35, guiTop + 25, 60, 20, getColor())
				.setHoverText("hover.set.color"));
		addButton(new GuiNpcButton(66, guiLeft + 107, guiTop + 8, 20, 20, "X")
				.setHoverText("hover.back"));
	}

	@Override
	public boolean keyCnpcsPressed(char typedChar, int keyCode) {
		if (subgui == null) {
			if (!mc.getLanguageManager().getCurrentLanguage().getLanguageCode().equalsIgnoreCase("en_us")) {
				boolean kase = ("" + typedChar).toLowerCase().equals("" + typedChar);
				typedChar = Keyboard.getKeyName(keyCode).charAt(0);
				if (kase) { typedChar = ("" + typedChar).toLowerCase().charAt(0); }
			}
			String prev = textfield.getText();
			boolean bo = super.keyCnpcsPressed(typedChar, keyCode);
			String newText = textfield.getText();
			if (newText.equals(prev)) { return false; }
			try {
				if (textfield.getText().isEmpty()) { color = 0; }
				else { color = Integer.parseInt(textfield.getText(), 16); }
				callback.color(color);
			}
			catch (NumberFormatException e) { textfield.setText(prev); }
			return bo;
		}
		return super.keyCnpcsPressed(typedChar, keyCode);
	}

	@Override
	public boolean mouseCnpcsPressed(int mouseX, int mouseY, int mouseButton) {
		if (subgui == null && hovered && hover != 0) {
			color = hover;
			callback.color(hover);
			textfield.setText(getColor());
			return true;
		}
		return super.mouseCnpcsPressed(mouseX, mouseY, mouseButton);
	}

    @Override
	public void unFocused(GuiNpcTextField textfield) {
		try { color = Integer.parseInt(textfield.getText(), 16); }
		catch (NumberFormatException e) { color = 0; }
		callback.color(color);
	}

}
