package noppes.npcs.client.controllers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;

public class MusicController {
	
	public static MusicController Instance;
	public PositionedSoundRecord playing;
	public Entity playingEntity;
	public ResourceLocation playingResource;

	public MusicController() {
		MusicController.Instance = this;
	}

	public boolean isPlaying(String music) {
		ResourceLocation resource = new ResourceLocation(music);
		return this.playingResource != null && this.playingResource.equals(resource) && Minecraft.getMinecraft().getSoundHandler().isSoundPlaying(this.playing);
	}

	public void playMusic(String music, Entity entity) {
		if (this.isPlaying(music)) { return; }
		this.stopMusic();
		this.playingResource = new ResourceLocation(music);
		this.playingEntity = entity;
		SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
		handler.playSound((this.playing = new PositionedSoundRecord(this.playingResource, SoundCategory.MUSIC, 1.0f, 1.0f, false, 0, ISound.AttenuationType.NONE, 0.0f, 0.0f, 0.0f)));
	}

	public void playSound(SoundCategory cat, String music, int x, int y, int z, float volumne, float pitch) {
		PositionedSoundRecord rec = new PositionedSoundRecord(new ResourceLocation(music), cat, volumne, pitch, false, 0, ISound.AttenuationType.LINEAR, x + 0.5f, y, z + 0.5f);
		Minecraft.getMinecraft().getSoundHandler().playSound(rec);
	}

	public void playStreaming(String music, Entity entity) {
		try {
			if (music==null || music.isEmpty() || this.playingResource==null || this.isPlaying(music)) { return; }
			this.stopMusic();
			this.playingEntity = entity;
			this.playingResource = new ResourceLocation(music);
			this.playing = new PositionedSoundRecord(this.playingResource, SoundCategory.RECORDS, 4.0f, 1.0f, false, 0, ISound.AttenuationType.LINEAR, (float)this.playingEntity.posX+0.5f, (float)this.playingEntity.posY+0.5f, (float)this.playingEntity.posZ+0.5f);
			Minecraft.getMinecraft().getSoundHandler().playSound(this.playing);
		}
		catch (Exception e) { }
	}

	public void stopMusic() {
		SoundHandler handler = Minecraft.getMinecraft().getSoundHandler();
		if (this.playing != null) { handler.stopSound(this.playing); }
		handler.stop("", SoundCategory.MUSIC);
		handler.stop("", SoundCategory.AMBIENT);
		handler.stop("", SoundCategory.RECORDS);
		this.playingResource = null;
		this.playingEntity = null;
		this.playing = null;
	}
}
