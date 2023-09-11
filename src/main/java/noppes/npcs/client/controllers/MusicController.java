package noppes.npcs.client.controllers;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import noppes.npcs.util.ObfuscationHelper;

public class MusicController {
	
	public static MusicController Instance;
	public Entity playingEntity = null;
	private String currentMusic = "";

	public MusicController() { MusicController.Instance = this; }

	public boolean isPlaying(String music) {
		if (music==null || music.isEmpty()) { return false; }
		ResourceLocation resource = new ResourceLocation(music);
		SoundManager sm = ObfuscationHelper.getValue(SoundHandler.class, Minecraft.getMinecraft().getSoundHandler(), SoundManager.class);
		Map<String, ISound> playingSounds = ObfuscationHelper.getValue(SoundManager.class, sm, 8);
		for (ISound sound : playingSounds.values()) {
			if (sound.getSound().getSoundLocation().equals(resource) || sound.getSoundLocation().equals(resource)) { return true; }
		}
		return false;
	}
	
	public void forcePlaySound(SoundCategory cat, String sound, int x, int y, int z, float volume, float pitch) {
		if (cat == null || sound==null || sound.isEmpty()) { return; }
		ISound.AttenuationType aType = ISound.AttenuationType.LINEAR;
		if (cat==SoundCategory.MUSIC) {
			Minecraft.getMinecraft().getSoundHandler().stop("", SoundCategory.MUSIC);
			ObfuscationHelper.setValue(MusicTicker.class, Minecraft.getMinecraft().getMusicTicker(), null, ISound.class);
			aType = ISound.AttenuationType.NONE;
			x = 0;
			y = 0;
			z = 0;
		}
		Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(sound), cat, volume, pitch, false, 0, aType, x, y, z));
	}
	
	public void forcePlaySound(String sound, float volume, float pitch) {
		Minecraft mc = Minecraft.getMinecraft();
		mc.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(sound), SoundCategory.PLAYERS, volume, pitch, false, 0, ISound.AttenuationType.LINEAR, (int) mc.player.posX, (int) mc.player.posY, (int) mc.player.posZ));
	}
	
	public void playSound(SoundCategory cat, String music, int x, int y, int z, float volume, float pitch) {
		if (this.isPlaying(music)) { return; }
		ISound.AttenuationType aType = ISound.AttenuationType.LINEAR;
		if (cat == SoundCategory.MUSIC) {
			aType = ISound.AttenuationType.NONE;
			x = 0;
			y = 0;
			z = 0;
		}
		Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(music), cat, volume, pitch, false, 0, aType, x, y, z));
	}

	public void playMusic(String music, SoundCategory category, Entity entity) {
		if (this.isPlaying(music)) { return; }
		if (this.isPlaying(this.currentMusic) && this.playingEntity!=null) { return; }
		this.stopMusic(category);
		this.playingEntity = entity;
		this.currentMusic  = music;
		Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(music), category, 1.0f, 1.0f, false, 0, ISound.AttenuationType.NONE, 0.0f, 0.0f, 0.0f));
	}

	public void stopMusic() {
		SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
		handler.stop("", SoundCategory.MUSIC);
		handler.stop("", SoundCategory.PLAYERS);
		handler.stop("", SoundCategory.AMBIENT);
		handler.stop("", SoundCategory.RECORDS);
		this.currentMusic = "";
	}


	public void stopMusic(SoundCategory category) {
		Minecraft.getMinecraft().getSoundHandler().stop("", category);
		this.playingEntity = null;
		this.currentMusic = "";
	}
	
}
