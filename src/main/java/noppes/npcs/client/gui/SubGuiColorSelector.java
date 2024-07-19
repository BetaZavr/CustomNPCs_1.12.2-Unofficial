package noppes.npcs.client.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.annotation.Nonnull;
import javax.imageio.ImageIO;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.IResource;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcTextField;
import noppes.npcs.client.gui.util.ITextfieldListener;
import noppes.npcs.client.gui.util.SubGuiInterface;

public class SubGuiColorSelector extends SubGuiInterface implements ITextfieldListener {

	private static final ResourceLocation resource = new ResourceLocation(CustomNpcs.MODID, "textures/gui/color.png");
	public int color;
	private int colorX;
	private int colorY;
	private GuiNpcTextField textfield;

	public SubGuiColorSelector(int color) {
		this.xSize = 176;
		this.ySize = 222;
		this.color = color;
		this.setBackground("smallbg.png");
	}

	@Override
	public void actionPerformed(@Nonnull GuiButton guibutton) {
		super.actionPerformed(guibutton);
		if (guibutton.id == 66) {
			this.close();
		}
	}

	@Override
	public void close() {
		super.close();
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		super.drawScreen(par1, par2, par3);
		mc.renderEngine.bindTexture(SubGuiColorSelector.resource);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		drawTexturedModalRect(colorX, colorY, 0, 0, 120, 120);
		if (textfield == null) {
			return;
		}
		int x = textfield.x + textfield.width + 5;
		drawGradientRect(x, textfield.y - 1, x + 41, textfield.y + 21, 0xFF808080, 0xFF808080);
		int c = color;
		float alpha = (float) (c >> 24 & 255) / 255.0F;
		if (alpha == 0.0f) {
			c += 0xFF000000;
		}
		drawGradientRect(x + 1, textfield.y, x + 40, textfield.y + 20, c, c);
	}

	public String getColor() {
		StringBuilder str = new StringBuilder(Integer.toHexString(this.color));
		while (str.length() < 6) { str.insert(0, "0"); }
		return str.toString();
	}

	@Override
	public void initGui() {
		super.initGui();
		colorX = guiLeft + 30;
		colorY = guiTop + 50;
		addTextField(textfield = new GuiNpcTextField(0, this, this.guiLeft + 31, this.guiTop + 20, 70, 20, getColor()));
		textfield.setMaxStringLength(6);
		addButton(new GuiNpcButton(66, this.guiLeft + 112, this.guiTop + 198, 60, 20, "gui.done"));
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
		} catch (NumberFormatException e) {
			textfield.setText(prev);
		}
	}

	@Override
	public void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		if (i < this.colorX || i > this.colorX + 117 || j < this.colorY || j > this.colorY + 117) {
			return;
		}
		InputStream stream = null;
		try {
			IResource iresource = this.mc.getResourceManager().getResource(SubGuiColorSelector.resource);
			BufferedImage bufferedimage = ImageIO.read(stream = iresource.getInputStream());
			this.color = (bufferedimage.getRGB((i - this.guiLeft - 30) * 4, (j - this.guiTop - 50) * 4) & 0xFFFFFF);
			this.textfield.setText(this.getColor());
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

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		try {
			color = Integer.parseInt(textfield.getText(), 16);
		} catch (NumberFormatException e) { LogWriter.error("Error:", e); }
	}

}
