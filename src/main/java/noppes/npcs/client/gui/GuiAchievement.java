package noppes.npcs.client.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.awt.*;

@SideOnly(Side.CLIENT)
public class GuiAchievement implements IToast {

	protected final int type;
	protected boolean newDisplay;
	protected long firstDrawTime;
	protected String subtitle;
	protected String title;

	public GuiAchievement(ITextComponent titleComponent, ITextComponent subtitleComponent, int messageType) {
		super();
		title = titleComponent.getUnformattedText();

		subtitle = ((subtitleComponent == null) ? null : subtitleComponent.getUnformattedText());
		type = messageType;
	}

	public @Nonnull IToast.Visibility draw(@Nonnull GuiToast toastGui, long delta) {
		if (newDisplay) {
			firstDrawTime = delta;
			newDisplay = false;
		}
		toastGui.getMinecraft().getTextureManager().bindTexture(GuiAchievement.TEXTURE_TOASTS);
		GlStateManager.color(1.0f, 1.0f, 1.0f);
		toastGui.drawTexturedModalRect(0, 0, 0, 32 * type, 160, 32);
		int titleColor = new Color(0xFFFFFF00).getRGB();
		int subtitleColor = new Color(0xFFFFFFFF).getRGB();
		if (type == 1 || type == 3) {
			titleColor = new Color(0xFF500050).getRGB();
			subtitleColor = new Color(0xFF000000).getRGB();
		}
		toastGui.getMinecraft().fontRenderer.drawString(title, 18, 7, titleColor);
		toastGui.getMinecraft().fontRenderer.drawString(subtitle, 18, 18, subtitleColor);
		return (delta - firstDrawTime < 5000L) ? IToast.Visibility.SHOW : IToast.Visibility.HIDE;
	}

	public void setDisplayedText(ITextComponent titleComponent, @Nullable ITextComponent subtitleComponent) {
		title = titleComponent.getUnformattedText();
		subtitle = ((subtitleComponent == null) ? null : subtitleComponent.getUnformattedText());
		newDisplay = true;
	}

}
