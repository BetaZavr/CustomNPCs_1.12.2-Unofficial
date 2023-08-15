package noppes.npcs.client.controllers;

import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
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

	public MusicController() { MusicController.Instance = this; }

	public boolean isPlaying(String music) {
		ResourceLocation resource = new ResourceLocation(music);
		SoundManager sm = ObfuscationHelper.getValue(SoundHandler.class, Minecraft.getMinecraft().getSoundHandler(), SoundManager.class);
		Map<String, ISound> playingSounds = ObfuscationHelper.getValue(SoundManager.class, sm, 8);
		for (ISound sound : playingSounds.values()) {
			if (sound.getSound().getSoundLocation().equals(resource) || sound.getSoundLocation().equals(resource)) { return true; }
		}
		return false;
	}

	public void playMusic(String music, SoundCategory category, Entity entity) {
		if (this.isPlaying(music)) { return; }
		this.stopMusic();
		this.playingEntity = entity;
		ResourceLocation resource = new ResourceLocation(music);
		SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
		handler.playSound(new PositionedSoundRecord(resource, category, 1.0f, 1.0f, false, 0, ISound.AttenuationType.NONE, 0.0f, 0.0f, 0.0f));
	}
	
	public void forcePlaySound(SoundCategory cat, String music, int x, int y, int z, float volumne, float pitch) {
		PositionedSoundRecord rec = new PositionedSoundRecord(new ResourceLocation(music), cat, volumne, pitch, false, 0, ISound.AttenuationType.LINEAR, x + 0.5f, y, z + 0.5f);
		Minecraft.getMinecraft().getSoundHandler().playSound(rec);
	}
	
	public void playSound(SoundCategory cat, String music, int x, int y, int z, float volume, float pitch) {
		if (this.isPlaying(music)) { return; }
		PositionedSoundRecord rec = new PositionedSoundRecord(new ResourceLocation(music), cat, volume, pitch, false, 0, ISound.AttenuationType.LINEAR, x + 0.5f, y, z + 0.5f);
		Minecraft.getMinecraft().getSoundHandler().playSound(rec);
	}

	public void playStreaming(String music, Entity entity) {
		try {
			if (music==null || music.isEmpty() || this.isPlaying(music)) { return; }
			this.stopMusic();
			this.playingEntity = entity;
			ResourceLocation resource = new ResourceLocation(music);
			Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(resource, SoundCategory.RECORDS, 4.0f, 1.0f, false, 0, ISound.AttenuationType.LINEAR, (float) entity.posX+0.5f, (float) entity.posY+0.5f, (float) entity.posZ+0.5f));
		}
		catch (Exception e) { }
	}

	public void stopMusic() {
		SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
		handler.stop("", SoundCategory.MUSIC);
		handler.stop("", SoundCategory.PLAYERS);
		handler.stop("", SoundCategory.AMBIENT);
		handler.stop("", SoundCategory.RECORDS);
		this.playingEntity = null;
	}
	
}
