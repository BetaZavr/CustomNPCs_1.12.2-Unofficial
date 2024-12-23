package noppes.npcs.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.settings.GameSettings;
import noppes.npcs.api.entity.IPlayer;

public interface IMinecraft {
	
	Minecraft getMc();
	
	GuiScreen getCurrentScreen();
	
	int getDisplayWidth();
	
	int getDisplayHeight();
	
	double getWidth();
	
	double getHeight();
	
	float getRenderPartialTicks();
	
	IResourceManager getResourceManager();
	
	TextureManager getTextureManager();
	
	SoundHandler getSoundHandler();
	
	ItemRenderer getItemRenderer();
	
	GameSettings getGameSettings();
	
	IGlStateManager getGlStateManager();
	
	void playSound(String category, String sound, float x, float y, float z, float volume, float pitch);
	
	void stopSound(String category, String sound);
	
	float getSoundVolume(String category);
	
	void setSoundVolume(String category, float value);

    IPlayer<?> getPlayer();
}
