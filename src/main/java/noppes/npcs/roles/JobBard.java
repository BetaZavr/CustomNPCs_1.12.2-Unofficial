package noppes.npcs.roles;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MusicTicker;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.entity.data.role.IJobBard;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ObfuscationHelper;

public class JobBard
extends JobInterface
implements IJobBard {
	
	public boolean hasOffRange;
	public boolean isStreamer;
	public int maxRange;
	public int minRange;
	public String song;

	public JobBard(EntityNPCInterface npc) {
		super(npc);
		this.minRange = 2;
		this.maxRange = 64;
		this.isStreamer = true;
		this.hasOffRange = true;
		this.song = "";
		this.type = JobType.BARD;
	}

	@Override
	public void delete() {
		if (this.npc.world.isRemote && this.hasOffRange && MusicController.Instance.isPlaying(this.song)) {
			MusicController.Instance.stopMusic();
		}
	}

	@Override
	public String getSong() {
		return this.song;
	}

	@Override
	public void killed() {
		this.delete();
	}

	public void onLivingUpdate() {
		if (!this.npc.isRemote() || this.song.isEmpty()) {
			return;
		}
		if (!MusicController.Instance.isPlaying(this.song)) {
			List<EntityPlayer> list = this.npc.world.getEntitiesWithinAABB(EntityPlayer.class, this.npc.getEntityBoundingBox().grow(this.minRange, (this.minRange / 2), this.minRange));
			if (!list.contains(CustomNpcs.proxy.getPlayer())) {
				return;
			}
			MusicController.Instance.playMusic(this.song, this.isStreamer ? SoundCategory.PLAYERS : SoundCategory.MUSIC, this.npc);
		} else if (MusicController.Instance.playingEntity != this.npc) {
			EntityPlayer player = CustomNpcs.proxy.getPlayer();
			if (MusicController.Instance.playingEntity==null || this.npc.getDistance(player) < MusicController.Instance.playingEntity.getDistance(player)) {
				MusicController.Instance.playingEntity = this.npc;
			}
		} else if (this.hasOffRange) {
			List<EntityPlayer> list = this.npc.world.getEntitiesWithinAABB(EntityPlayer.class,
					this.npc.getEntityBoundingBox().grow(this.maxRange, (this.maxRange / 2), this.maxRange));
			if (!list.contains(CustomNpcs.proxy.getPlayer())) {
				MusicController.Instance.stopMusic();
			}
		}
		if (MusicController.Instance.isPlaying(this.song)) {
			ObfuscationHelper.setValue(MusicTicker.class, Minecraft.getMinecraft().getMusicTicker(), 12000, 3);
		}
	}

	@Override
	public void setSong(String song) {
		this.song = song;
		this.npc.updateClient = true;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.type = JobType.BARD;
		this.song = compound.getString("BardSong");
		this.minRange = compound.getInteger("BardMinRange");
		this.maxRange = compound.getInteger("BardMaxRange");
		this.isStreamer = compound.getBoolean("BardStreamer");
		this.hasOffRange = compound.getBoolean("BardHasOff");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("Type", JobType.BARD.get());
		compound.setString("BardSong", this.song);
		compound.setInteger("BardMinRange", this.minRange);
		compound.setInteger("BardMaxRange", this.maxRange);
		compound.setBoolean("BardStreamer", this.isStreamer);
		compound.setBoolean("BardHasOff", this.hasOffRange);
		System.out.println(this.npc.getName()+"; Type: "+this.type+"; compound: "+compound);
		return compound;
	}
}
