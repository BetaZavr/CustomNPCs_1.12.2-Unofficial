package noppes.npcs.client.gui.model;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

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
	private ColorCallback callback;
	public int color;
	private int colorX;
	private int colorY;
	public GuiScreen parent;

	private GuiNpcTextField textfield;

	public GuiModelColor(GuiScreen parent, int color, ColorCallback callback) {
		this.parent = parent;
		this.callback = callback;
		this.ySize = 230;
		this.closeOnEsc = false;
		this.background = GuiModelColor.colorgui;
		this.color = color;
	}

	@Override
	protected void actionPerformed(GuiButton guibutton) {
		if (guibutton.id == 66) {
			this.close();
		}
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		super.drawScreen(par1, par2, par3);
		GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
		this.mc.getTextureManager().bindTexture(GuiModelColor.colorPicker);
		this.drawTexturedModalRect(this.colorX, this.colorY, 0, 0, 120, 120);
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.getTextField(0)!=null && this.getTextField(0).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.set.color").getFormattedText());
		} else if (this.getButton(66)!=null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
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
	public void mouseClicked(int i, int j, int k) {
		super.mouseClicked(i, j, k);
		if (i < this.colorX || i > this.colorX + 120 || j < this.colorY || j > this.colorY + 120) {
			return;
		}
		InputStream stream = null;
		try {
			IResource resource = this.mc.getResourceManager().getResource(GuiModelColor.colorPicker);
			BufferedImage bufferedimage = ImageIO.read(stream = resource.getInputStream());
			int color = bufferedimage.getRGB((i - this.guiLeft - 4) * 4, (j - this.guiTop - 50) * 4) & 0xFFFFFF;
			if (color != 0) {
				this.color = color;
				this.callback.color(color);
				this.textfield.setTextColor(color);
				this.textfield.setText(this.getColor());
			}
		} catch (IOException ex) {
		} finally {
			if (stream != null) {
				try {
					stream.close();
				} catch (IOException ex2) {
				}
			}
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
