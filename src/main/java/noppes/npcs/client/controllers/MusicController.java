package noppes.npcs.client.controllers;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import noppes.npcs.LogWriter;
import noppes.npcs.client.ClientTickHandler;
import noppes.npcs.client.util.MusicData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.mixin.client.audio.*;
import noppes.npcs.roles.JobBard;
import paulscode.sound.Library;
import paulscode.sound.SoundSystem;
import paulscode.sound.Source;

public class MusicController {

	public static MusicController Instance;

	public String music = "", song = "";
	public EntityNPCInterface musicBard = null, songBard = null;
	public boolean unloadMusicBard = false, unloadSongBard = false;

	public MusicController() {
		MusicController.Instance = this;
	}

	public void bardPlaySound(String song, boolean isStreamer, EntityNPCInterface npc) {
		stopSound(song, isStreamer ? SoundCategory.AMBIENT : SoundCategory.MUSIC);
		ISound.AttenuationType aType = ISound.AttenuationType.LINEAR;
		ResourceLocation res = new ResourceLocation(song);
		float x = (float) npc.posX;
		float y = (float) npc.posY;
		float z = (float) npc.posZ;
		if (isStreamer) {
			this.song = song;
			this.songBard = npc;
		} else {
			this.music = song;
			this.musicBard = npc;
			aType = ISound.AttenuationType.NONE;
			x = 0.0f;
			y = 0.0f;
			z = 0.0f;
			for (MusicData md : ClientTickHandler.musics.values()) {
				if (!md.name.isEmpty() && md.name.indexOf("minecraft") == 0) {
					Minecraft.getMinecraft().getSoundHandler().stop(md.name, SoundCategory.MUSIC);
				}
			}
		}
		Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(res, isStreamer ? SoundCategory.AMBIENT : SoundCategory.MUSIC, 1.0f, 1.0f, false, 0, aType, x, y, z));
	}

	public void checkBards(EntityPlayer player) {
		if (this.music.isEmpty()) {
			if (this.musicBard != null) {
				this.musicBard = null;
			}
		} else {
			if (this.musicBard == null || !(this.musicBard.advanced.jobInterface instanceof JobBard)) {
				this.stopSound(this.music, SoundCategory.MUSIC);
			} else {
				Entity entity = player.world.getEntityByID(this.musicBard.getEntityId());
				if (entity == null) {
					this.unloadMusicBard = true;
					JobBard job = (JobBard) this.musicBard.advanced.jobInterface;
					if (job.hasOffRange) {
						int x = job.range[1], y = job.range[1], z = job.range[1];
						if (!job.isRange) {
							x = job.maxPos[0];
							y = job.maxPos[1];
							z = job.maxPos[2];
						}
						int xD = (int) Math.abs(player.posX - musicBard.posX);
						int yD = (int) Math.abs(player.posY - musicBard.posY);
						int zD = (int) Math.abs(player.posZ - musicBard.posZ);
						if (xD > x || yD > y || zD > z) {
							this.stopSound(this.song, SoundCategory.MUSIC);
						}
					}
				}
			}
		}

		if (this.song.isEmpty()) {
			if (this.songBard != null) {
				this.songBard = null;
			}
		} else {
			if (this.songBard == null || !(this.songBard.advanced.jobInterface instanceof JobBard)) {
				this.stopSound(this.song, SoundCategory.AMBIENT);
			} else {
				Entity entity = player.world.getEntityByID(this.songBard.getEntityId());
				if (entity == null) {
					this.unloadSongBard = true;
					JobBard job = (JobBard) this.songBard.advanced.jobInterface;
					if (job.hasOffRange) {
						int x = job.range[1], y = job.range[1], z = job.range[1];
						if (!job.isRange) {
							x = job.maxPos[0];
							y = job.maxPos[1];
							z = job.maxPos[2];
						}
						int xD = (int) Math.abs(player.posX - songBard.posX);
						int yD = (int) Math.abs(player.posY - songBard.posY);
						int zD = (int) Math.abs(player.posZ - songBard.posZ);
						if (xD > x || yD > y || zD > z) {
							this.stopSound(this.song, SoundCategory.AMBIENT);
						}
					}
				}
			}
		}
	}

	public void forcePlaySound(SoundCategory cat, String sound, float x, float y, float z, float volume, float pitch) {
		if (cat == null || sound == null || sound.isEmpty()) {
			return;
		}
		ISound.AttenuationType aType = ISound.AttenuationType.LINEAR;
		Minecraft mc = Minecraft.getMinecraft();
		if (cat == SoundCategory.MUSIC) {
			Minecraft.getMinecraft().getSoundHandler().stop("", SoundCategory.MUSIC);
			aType = ISound.AttenuationType.NONE;
			x = mc.player != null ? (float) mc.player.posX : 0.0f;
			y = mc.player != null ? (float) mc.player.posY + 0.5f : 0.0f;
			z = mc.player != null ? (float) mc.player.posZ : 0.0f;
		}
		mc.getSoundHandler().playSound(
				new PositionedSoundRecord(new ResourceLocation(sound), cat, volume, pitch, false, 0, aType, x, y, z));
	}

	public boolean isBardPlaying(String song, boolean isStreamer) { // check Any Bards
		return isPlaying(song) || (isStreamer ? !song.isEmpty() && isPlaying(this.song) : !music.isEmpty() && isPlaying(this.music));
	}

	public boolean isPlaying(String music) {
		if (music == null || music.isEmpty()) {
			return false;
		}
		ResourceLocation resource = new ResourceLocation(music);
		SoundManager sm = ((ISoundHandlerMixin) Minecraft.getMinecraft().getSoundHandler()).npcs$getSndManager();
		Map<String, ISound> playingSounds = ((ISoundManagerMixin) sm).npcs$getPlayingSounds();
		if (playingSounds == null) {
			return false;
		}
		for (ISound sound : playingSounds.values()) {
			if (sound.getSound().getSoundLocation().equals(resource) || sound.getSoundLocation().equals(resource)) {
				return true;
			}
		}
		return false;
	}

	public void playSound(SoundCategory category, String music, float x, float y, float z, float volume, float pitch) {
		if (this.isPlaying(music)) {
			return;
		}
		ISound.AttenuationType aType = ISound.AttenuationType.LINEAR;
		ResourceLocation res = new ResourceLocation(music);
		if (category == SoundCategory.MUSIC) {
			aType = ISound.AttenuationType.NONE;
			x = 0.0f;
			y = 0.0f;
			z = 0.0f;
		}
		Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(res, category, volume, pitch, false, 0, aType, x, y, z));
	}

	public void setNewPosSong(String song, float x, float y, float z) {
		if (song == null || song.isEmpty()) {
			return;
		}
		ResourceLocation resource = new ResourceLocation(song);
		SoundManager sm = ((ISoundHandlerMixin) Minecraft.getMinecraft().getSoundHandler()).npcs$getSndManager();
		Map<String, ISound> playingSounds = ((ISoundManagerMixin) sm).npcs$getPlayingSounds();
		if (playingSounds == null) { return; }
		String uuid = null;
		for (String id : playingSounds.keySet()) {
			ISound sound = playingSounds.get(id);
			if (sound.getSound().getSoundLocation().equals(resource)
					|| sound.getSoundLocation().equals(resource) && sound instanceof PositionedSound) {
				((IPositionedSoundMixin) sound).npcs$setXPosF(x);
				((IPositionedSoundMixin) sound).npcs$setYPosF(y);
				((IPositionedSoundMixin) sound).npcs$setZPosF(z);
				uuid = id;
				break;
			}
		}
		System.out
				.println("New pos song uuid: \"" + uuid + "\" to [" + (int) x + ", " + (int) y + ", " + (int) z + "]");
		if (uuid != null) {
			SoundSystem sndSystem = null;
			for (Field f : sm.getClass().getDeclaredFields()) {
				if (f.getType().getName().contains("SoundSystem")) {
					try {
						f.setAccessible(true);
						sndSystem = (SoundSystem) f.get(sm);
					}
					catch (IllegalAccessException e) { LogWriter.debug(e.toString()); }
					break;
				}
			}
			if (sndSystem == null) { return; }
			Library soundLibrary = ((ISoundSystemMixin) sndSystem).npcs$getSoundLibrary();
			if (soundLibrary == null) { return; }
			Source source = soundLibrary.getSources().get(uuid);
			if (source != null && source.position != null) {
				source.position.x = x;
				source.position.y = y;
				source.position.z = z;
			}
		}
	}

	public void stopSound(String song, SoundCategory category) {
		if (song == null) {
			song = "";
		}
		Minecraft.getMinecraft().getSoundHandler().stop(song, category);
		if (category == SoundCategory.AMBIENT) {
			this.song = "";
			this.songBard = null;
		} else if (category == SoundCategory.MUSIC) {
			this.music = "";
			this.musicBard = null;
		}
	}

	public void stopSounds() {
		Minecraft.getMinecraft().getSoundHandler().stopSounds();
		this.song = "";
		this.songBard = null;
		this.music = "";
		this.musicBard = null;
	}

}
