package noppes.npcs.client.gui;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.client.gui.util.*;

public class SubGuiColorSelector
extends SubGuiInterface
implements ITextfieldListener {

	private static final ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/color.png");
	protected final BufferedImage bufferedimage;

	public int color;
	private int colorX;
	private int colorY;
	private GuiNpcTextField textfield;
	protected ColorCallback callback;

	public SubGuiColorSelector(int colorInt) {
		super();
		xSize = 176;
		ySize = 222;
		setBackground("smallbg.png");

		color = colorInt;

		if (mc == null) { mc = Minecraft.getMinecraft(); }

		InputStream stream = null;
		BufferedImage buffer = null;
		try {
			IResource iresource = mc.getResourceManager().getResource(SubGuiColorSelector.resource);
			buffer = ImageIO.read(stream = iresource.getInputStream());
		}
		catch (IOException e) { LogWriter.error(e); }
		finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException ex) { LogWriter.error(ex); }
			}
		}
		bufferedimage = buffer;
	}

	@SuppressWarnings("all")
	public SubGuiColorSelector(int colorIn, ColorCallback callbackIn) {
		this(colorIn);
		callback = callbackIn;
	}

	@Override
	public void actionPerformed(@Nonnull GuiButton guibutton) {
		super.actionPerformed(guibutton);
		if (guibutton.id == 66) {
			close();
		}
	}

	@Override
	public void close() {
		super.close();
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		super.drawScreen(par1, par2, par3);
		mc.getTextureManager().bindTexture(SubGuiColorSelector.resource);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		drawTexturedModalRect(colorX, colorY, 0, 0, 120, 120);
		if (textfield == null) {
			return;
		}
		int x = textfield.x + textfield.width + 5;
		int c = new Color(0xFF808080).getRGB();
		drawGradientRect(x, textfield.y - 1, x + 41, textfield.y + 21, c, c);
		c = color;
		float alpha = (float) (c >> 24 & 255) / 255.0F;
		if (alpha == 0.0f) { c += new Color(0xFF000000).getRGB(); }
		drawGradientRect(x + 1, textfield.y, x + 40, textfield.y + 20, c, c);
	}

	public String getColor() {
		StringBuilder str = new StringBuilder(Integer.toHexString(color));
		while (str.length() < 6) { str.insert(0, "0"); }
		return str.toString();
	}

	@Override
	public void initGui() {
		super.initGui();
		colorX = guiLeft + 30;
		colorY = guiTop + 50;
		addTextField(textfield = new GuiNpcTextField(0, this, guiLeft + 31, guiTop + 20, 70, 20, getColor()));
		textfield.setMaxStringLength(6);
		textfield.setHoverText("color.hover");
		GuiNpcButton button = new GuiNpcButton(66, guiLeft + 112, guiTop + 198, 60, 20, "gui.done");
		button.setHoverText("hover.back");
		addButton(button);
	}

	@Override
	public void keyTyped(char c, int i) {
		String prev = textfield.getText();
		super.keyTyped(c, i);
		String newText = textfield.getText();
		if (!newText.equals(prev)) {
			try { setColor(Integer.parseInt(newText, 16)); }
			catch (NumberFormatException e) { textfield.setText(prev); }
		}
	}

	@Override
	public void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		if (bufferedimage == null || i < colorX || i > colorX + 117 || j < colorY || j > colorY + 117) {
			return;
		}
		setColor(bufferedimage.getRGB((i - guiLeft - 30) * 4, (j - guiTop - 50) * 4) & new Color(0x00FFFFFF).getRGB());
	}

	@Override
	public void unFocused(IGuiNpcTextField textfield) {
		try { setColor(Integer.parseInt(textfield.getFullText(), 16)); }
		catch (NumberFormatException e) { textfield.setFullText(getColor()); }
	}

	private void setColor(int colorIn) {
		color = colorIn;
		textfield.setFullText(getColor());
		if (callback != null) { callback.color(color); }
	}

	public interface ColorCallback {  void color(int colorIn); }

}
