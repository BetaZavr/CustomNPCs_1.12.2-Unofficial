package noppes.npcs.client.gui;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.gui.toasts.GuiToast;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public class GuiAchievement implements IToast {

	private long firstDrawTime;
	private boolean newDisplay;
	private String subtitle;
	private String title;
	private final int type;

	public GuiAchievement(ITextComponent titleComponent, ITextComponent subtitleComponent, int type) {
		this.title = titleComponent.getUnformattedText();
		this.subtitle = ((subtitleComponent == null) ? null : subtitleComponent.getUnformattedText());
		this.type = type;
	}

	public @Nonnull IToast.Visibility draw(@Nonnull GuiToast toastGui, long delta) {
		if (this.newDisplay) {
			this.firstDrawTime = delta;
			this.newDisplay = false;
		}
		toastGui.getMinecraft().getTextureManager().bindTexture(GuiAchievement.TEXTURE_TOASTS);
		GlStateManager.color(1.0f, 1.0f, 1.0f);
		toastGui.drawTexturedModalRect(0, 0, 0, 32 * this.type, 160, 32);
		int color1 = -256;
		int color2 = -1;
		if (this.type == 1 || this.type == 3) {
			color1 = -11534256;
			color2 = -16777216;
		}
		toastGui.getMinecraft().fontRenderer.drawString(this.title, 18, 7, color1);
		toastGui.getMinecraft().fontRenderer.drawString(this.subtitle, 18, 18, color2);
		return (delta - this.firstDrawTime < 5000L) ? IToast.Visibility.SHOW : IToast.Visibility.HIDE;
	}

	public void setDisplayedText(ITextComponent titleComponent, @Nullable ITextComponent subtitleComponent) {
		this.title = titleComponent.getUnformattedText();
		this.subtitle = ((subtitleComponent == null) ? null : subtitleComponent.getUnformattedText());
		this.newDisplay = true;
	}
}
