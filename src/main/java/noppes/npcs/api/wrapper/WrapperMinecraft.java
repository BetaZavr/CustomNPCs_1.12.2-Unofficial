package noppes.npcs.api.wrapper;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import noppes.npcs.api.IGlStateManager;
import noppes.npcs.api.IMinecraft;
import noppes.npcs.mixin.api.client.audio.MusicTickerAPIMixin;

public class WrapperMinecraft
implements IMinecraft {
	
	private final Minecraft minecraft;
	private final IGlStateManager glStateManager;
	
	public WrapperMinecraft(Minecraft mc) {
		this.minecraft = mc;
		this.glStateManager = new WrapperGlStateManager(mc);
	}
	
	@Override
	public Minecraft getMc() { return this.minecraft; }

	@Override
	public GuiScreen getCurrentScreen() { return this.minecraft.currentScreen; }

	@Override
	public int getDisplayWidth() { return this.minecraft.displayWidth; }

	@Override
	public int getDisplayHeight() { return this.minecraft.displayHeight; }

	@Override
	public float getRenderPartialTicks() { return this.minecraft.getRenderPartialTicks(); }

	@Override
	public IResourceManager getResourceManager() { return this.minecraft.getResourceManager(); }

	@Override
	public TextureManager getTextureManager() { return this.minecraft.getTextureManager(); }

	@Override
	public SoundHandler getSoundHandler() { return this.minecraft.getSoundHandler(); }

	@Override
	public ItemRenderer getItemRenderer() { return this.minecraft.getItemRenderer(); }

	@Override
	public double getWidth() { return (new ScaledResolution(this.minecraft)).getScaledWidth_double(); }

	@Override
	public double getHeight() { return (new ScaledResolution(this.minecraft)).getScaledHeight_double(); }

	@Override
	public IGlStateManager getGlStateManager() { return this.glStateManager; }

	@Override
	public void playSound(String category, String sound, float x, float y, float z, float volume, float pitch) {
		if (sound == null || sound.isEmpty()) { return; }
		if (y < 0 && this.minecraft.player != null) {
			x = (float) this.minecraft.player.posX;
			y = (float) this.minecraft.player.posY + 0.9f;
			z = (float) this.minecraft.player.posZ;
		}
		ISound.AttenuationType aType = ISound.AttenuationType.LINEAR;
		SoundCategory cat = this.getCategory(category);
		if (cat == SoundCategory.MUSIC || this.minecraft.world == null) {
			if (cat == SoundCategory.MUSIC) { this.getSoundHandler().stop("", SoundCategory.MUSIC); }
			aType = ISound.AttenuationType.NONE;
			x = this.minecraft.player != null ? (float) this.minecraft.player.posX : 0.0f;
			y = this.minecraft.player != null ? (float) this.minecraft.player.posY + 0.5f : 0.0f;
			z = this.minecraft.player != null ? (float) this.minecraft.player.posZ : 0.0f;
			((MusicTickerAPIMixin) this.minecraft.getMusicTicker()).npcs$setTimeUntilNextMusic(3600);
		}
		this.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(sound), cat, volume, pitch, false, 0, aType, x, y, z));
	}

	@Override
	public void stopSound(String category, String sound) {
		if (sound == null) { sound = ""; }
		this.getSoundHandler().stop(sound, this.getCategory(category));
	}

	@Override
	public GameSettings getGameSettings() { return this.minecraft.gameSettings; }

	@Override
	public float getSoundVolume(String category) { return this.minecraft.gameSettings.getSoundLevel(this.getCategory(category)); }
	
	@Override
	public void setSoundVolume(String category, float volume) { this.minecraft.gameSettings.setSoundLevel(this.getCategory(category), volume); }

	private SoundCategory getCategory(String category) {
		if (category != null && !category.isEmpty()) {
			for (SoundCategory c : SoundCategory.values()) {
				if (c.getName().equalsIgnoreCase(category)) {
					return c;
				}
			}
		}
		return SoundCategory.AMBIENT;
	}

}
