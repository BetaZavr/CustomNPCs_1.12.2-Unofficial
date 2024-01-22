package noppes.npcs.client.controllers;

import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.client.audio.PositionedSound;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.audio.SoundHandler;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.ClientTickHandler;
import noppes.npcs.client.util.MusicData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.JobBard;
import noppes.npcs.util.ObfuscationHelper;
import paulscode.sound.Library;
import paulscode.sound.SoundSystem;
import paulscode.sound.Source;

public class MusicController {
	
	public static MusicController Instance;
	
	public String music = "", song = "";
	public EntityNPCInterface musicBard = null, songBard = null;
	public boolean unloadMusicBard = false, unloadSongBard = false;

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
	
	public boolean isBardPlaying(String song, boolean isStreamer) { // check Any Bards
		return this.isPlaying(song) || (isStreamer ? !this.song.isEmpty() && this.isPlaying(this.song) : !this.music.isEmpty() && this.isPlaying(this.music));
	}
	
	public void forcePlaySound(SoundCategory cat, String sound, float x, float y, float z, float volume, float pitch) {
		if (cat == null || sound==null || sound.isEmpty()) { return; }
		ISound.AttenuationType aType = ISound.AttenuationType.LINEAR;
		Minecraft mc = Minecraft.getMinecraft();
		if (cat==SoundCategory.MUSIC) {
			Minecraft.getMinecraft().getSoundHandler().stop("", SoundCategory.MUSIC);
			ObfuscationHelper.setValue(MusicTicker.class, Minecraft.getMinecraft().getMusicTicker(), null, ISound.class);
			aType = ISound.AttenuationType.NONE;
			x = mc.player!=null ? (float) mc.player.posX : 0.0f;
			y = mc.player!=null ? (float) mc.player.posY + 0.5f : 0.0f;
			z = mc.player!=null ? (float) mc.player.posZ : 0.0f;
		}
		mc.getSoundHandler().playSound(new PositionedSoundRecord(new ResourceLocation(sound), cat, volume, pitch, false, 0, aType, x, y, z));
	}
	
	public void playSound(SoundCategory category, String music, float x, float y, float z, float volume, float pitch) {
		if (this.isPlaying(music)) { return; }
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

	public void bardPlaySound(String song, boolean isStreamer, EntityNPCInterface npc) {
		this.stopSound(song, isStreamer ? SoundCategory.AMBIENT : SoundCategory.MUSIC);
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
				if (!md.name.isEmpty() && md.name.indexOf("minecraft")==0) {
					Minecraft.getMinecraft().getSoundHandler().stop(md.name, SoundCategory.MUSIC);
				}
			}
		}
		Minecraft.getMinecraft().getSoundHandler().playSound(new PositionedSoundRecord(res, isStreamer ? SoundCategory.AMBIENT : SoundCategory.MUSIC, 1.0f, 1.0f, false, 0, aType, x, y, z));
	}

	public void stopSounds() {
		Minecraft.getMinecraft().getSoundHandler().stopSounds();
		this.song = "";
		this.songBard = null;
		this.music = "";
		this.musicBard = null;
	}
	
	public void stopSound(String song, SoundCategory category) {
		if (song==null) { song = ""; }
		Minecraft.getMinecraft().getSoundHandler().stop(song, category);
		if (category == SoundCategory.AMBIENT) {
			this.song = "";
			this.songBard = null;
		}
		else if (category == SoundCategory.MUSIC) {
			this.music = "";
			this.musicBard = null;
		}
	}

	public void setNewPosSong(String song, float x, float y, float z) {
		if (song==null || song.isEmpty()) { return; }
		ResourceLocation resource = new ResourceLocation(song);
		SoundManager sm = ObfuscationHelper.getValue(SoundHandler.class, Minecraft.getMinecraft().getSoundHandler(), SoundManager.class);
		Map<String, ISound> playingSounds = ObfuscationHelper.getValue(SoundManager.class, sm, 8);
		String uuid = null;
		for (String id : playingSounds.keySet()) {
			ISound sound = playingSounds.get(id);
			if (sound.getSound().getSoundLocation().equals(resource) || sound.getSoundLocation().equals(resource) && sound instanceof PositionedSound) {
				ObfuscationHelper.setValue(PositionedSound.class, (PositionedSound) sound, x, 6);
				ObfuscationHelper.setValue(PositionedSound.class, (PositionedSound) sound, y, 7);
				ObfuscationHelper.setValue(PositionedSound.class, (PositionedSound) sound, z, 8);
				uuid = id;
				break;
			}
		}
		System.out.println("New pos song uuid: \""+song+"\" to ["+(int)x+", "+(int)y+", "+(int)z+"]");
		if (uuid!=null) {
			SoundSystem sndSystem = ObfuscationHelper.getValue(SoundManager.class, sm, SoundSystem.class);
			Library soundLibrary = ObfuscationHelper.getValue(SoundSystem.class, sndSystem, 4);
			Source source = soundLibrary.getSources().get(uuid);
			if (source!=null && source.position!=null) {
				source.position.x = x;
				source.position.y = y;
				source.position.z = z;
			}
		}
	}

	public void cheakBards(EntityPlayer player) {
		if (this.music.isEmpty()) { if (this.musicBard!=null) { this.musicBard = null; } }
		else {
			if (this.musicBard==null) { this.stopSound(this.music, SoundCategory.MUSIC); }
			else {
				Entity entity = player.world.getEntityByID(this.musicBard.getEntityId());
				if (entity==null) {
					this.unloadMusicBard = true;
					JobBard job = (JobBard) this.musicBard.advanced.jobInterface;
					if (job.hasOffRange) {
						AxisAlignedBB aabb = this.musicBard.getEntityBoundingBox();
						if (job.isRange) {
							aabb = aabb.grow(job.range[1], job.range[1], job.range[1]);
						} else {
							aabb = new AxisAlignedBB(aabb.minX - job.maxPos[0], aabb.minY - job.maxPos[1], aabb.minZ - job.maxPos[2],
									aabb.maxX + job.maxPos[0], aabb.maxY + job.maxPos[1], aabb.maxZ + job.maxPos[2]);
						}
						List<EntityPlayer> list = player.world.getEntitiesWithinAABB(EntityPlayer.class, aabb);
						if (!list.contains(CustomNpcs.proxy.getPlayer())) {
							this.stopSound(this.song, SoundCategory.MUSIC);
						}
					}
				}
			}
		}
		
		if (this.song.isEmpty()) { if (this.songBard!=null) { this.songBard = null; } }
		else {
			if (this.songBard==null) { this.stopSound(this.song, SoundCategory.AMBIENT); }
			else {
				Entity entity = player.world.getEntityByID(this.songBard.getEntityId());
				if (entity==null) {
					this.unloadSongBard = true;
					JobBard job = (JobBard) this.songBard.advanced.jobInterface;
					if (job.hasOffRange) {
						AxisAlignedBB aabb = this.songBard.getEntityBoundingBox();
						if (job.isRange) {
							aabb = aabb.grow(job.range[1], job.range[1], job.range[1]);
						} else {
							aabb = new AxisAlignedBB(aabb.minX - job.maxPos[0], aabb.minY - job.maxPos[1], aabb.minZ - job.maxPos[2],
									aabb.maxX + job.maxPos[0], aabb.maxY + job.maxPos[1], aabb.maxZ + job.maxPos[2]);
						}
						List<EntityPlayer> list = player.world.getEntitiesWithinAABB(EntityPlayer.class, aabb);
						if (!list.contains(CustomNpcs.proxy.getPlayer())) {
							this.stopSound(this.song, SoundCategory.AMBIENT);
						}
					}
				}
			}
		}
	}
	
}
