package noppes.npcs.client.util;

import java.util.HashMap;
import java.util.Objects;

import javax.sound.sampled.AudioFormat;

import net.minecraft.client.audio.ISound;
import net.minecraft.client.audio.SoundManager;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.event.ForgeEvent.SoundTickEvent;
import noppes.npcs.util.ObfuscationHelper;
import paulscode.sound.Library;
import paulscode.sound.SoundBuffer;
import paulscode.sound.SoundSystem;
import paulscode.sound.Source;

public class MusicData {

	public ISound sound;
	public String uuid;
	public Source source;
	public String name, resource;
	public float millitotal;

	public MusicData(ISound s, String id, SoundManager manager) {
		this.sound = s;
		this.uuid = id;
		this.name = "";
		this.resource = "";
		SoundSystem sndSystem = ObfuscationHelper.getValue(SoundManager.class, manager, SoundSystem.class);
		Library soundLibrary = ObfuscationHelper.getValue(SoundSystem.class, sndSystem, 4);
		HashMap<String, Source> sourceMap = ObfuscationHelper.getValue(Library.class, soundLibrary, 3);
		if (sourceMap != null) {
			this.source = sourceMap.get(id);
			if (s != null) {
				this.name = s.getSoundLocation().toString();
				s.getSound();
				this.resource = this.sound.getSound().getSoundLocation().toString();
			}
			this.millitotal = 0.0f;
			if (this.source != null && this.source.soundBuffer != null) {
				SoundBuffer buffer = this.source.soundBuffer;
				AudioFormat format = buffer.audioFormat;
				float frames = (float) buffer.audioData.length / (float) format.getFrameSize();
				this.millitotal = 1000.0f * frames / format.getFrameRate();
				if (this.name.indexOf("minecraft") == 0) {
					this.millitotal *= 300.0f;
				}
			}
		}
	}

	public SoundTickEvent createEvent(EntityPlayer player) {
		float volume = 1.0f, pitch = 1.0f, milliseconds = 0.0f;
		int[] pos = new int[] { 0, 0, 0 };
		if (this.sound != null) {
			volume = this.sound.getVolume();
			pitch = this.sound.getPitch();
			pos[0] = (int) this.sound.getXPosF();
			pos[1] = (int) this.sound.getYPosF();
			pos[2] = (int) this.sound.getZPosF();
		}
		if (this.source != null) {
			milliseconds = this.source.millisecondsPlayed();
			pos[0] = (int) this.source.position.x;
			pos[1] = (int) this.source.position.y;
			pos[2] = (int) this.source.position.z;
			if (this.millitotal == 0.0f && this.source.soundBuffer != null) {
				SoundBuffer buffer = this.source.soundBuffer;
				AudioFormat format = buffer.audioFormat;
				float frames = (float) buffer.audioData.length / (float) format.getFrameSize();
				this.millitotal = 1000.0f * frames / format.getFrameRate();
				if (this.name.indexOf("minecraft") == 0) {
					this.millitotal *= 300.0f;
				}
			}
		}
		return new SoundTickEvent((IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player), this.name, this.resource,
				Objects.requireNonNull(NpcAPI.Instance()).getIPos(pos[0], pos[1], pos[2]), volume, pitch, milliseconds, this.millitotal);
	}

}
