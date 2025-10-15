package noppes.npcs.api;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.settings.GameSettings;
import noppes.npcs.api.entity.IPlayer;

@SuppressWarnings("all")
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
	
	void playSound(@ParamName("category") String category, @ParamName("sound") String sound,
				   @ParamName("z") float x, @ParamName("y") float y, @ParamName("z") float z,
				   @ParamName("category") float volume, @ParamName("pitch") float pitch);
	
	void stopSound(@ParamName("category") String category, @ParamName("sound") String sound);
	
	float getSoundVolume(@ParamName("category") String category);
	
	void setSoundVolume(@ParamName("category") String category, @ParamName("value") float value);

    IPlayer<?> getPlayer();
}
