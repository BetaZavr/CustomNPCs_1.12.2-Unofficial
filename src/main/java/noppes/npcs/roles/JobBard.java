package noppes.npcs.roles;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.entity.data.role.IJobBard;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.entity.EntityNPCInterface;

public class JobBard extends JobInterface implements IJobBard {

	private transient volatile WeakReference<Boolean> cachedInRange = new WeakReference<>(false);
	private transient volatile long checkTime = 0L;

	public boolean hasOffRange = true;
	public boolean isStreamer = true;
	public boolean isRange = true;
	public int[] range = new int[] { 2, 64 }; // min, max
	public int[] minPos = new int[] { 2, 2, 2 }; // x, y, z
	public int[] maxPos = new int[] { 64, 64, 64 }; // x, y, z
	public String song = "";

	public JobBard(EntityNPCInterface npc) {
		super(npc);
		type = JobType.BARD;
	}

	@Override
	public void delete() {
		// stopSound moved to ClientTickHandler.cnpcClientTick(event);
	}

	private int getIntInByte(byte b) {
		return (b <= 0 ? 256 : 0) + b;
	}

	@Override
	public String getSong() {
		return song;
	}

	@Override
	public void killed() {
		if (npc.world.isRemote && isStreamer && hasOffRange && MusicController.Instance.isPlaying(song)) {
			MusicController.Instance.stopSound(song, isStreamer ? SoundCategory.AMBIENT : SoundCategory.MUSIC);
		}
	}

	@Override
	public boolean isWorking() {
		if (npc.isServerWorld() || song.isEmpty()) { return false; }
		MusicController mData = MusicController.Instance;
		return npc.equals(mData.musicBard) || npc.equals(mData.songBard);
	}

	public void onLivingUpdate() {
		if (npc.isServerWorld() || song.isEmpty()) { return; }
		MusicController mData = MusicController.Instance;
		if (isStreamer ? mData.unloadSongBard : mData.unloadMusicBard) {
			EntityNPCInterface oldNPC = isStreamer ? mData.songBard : mData.musicBard;
			if (oldNPC == null) {
				if (isStreamer) {
					mData.unloadSongBard = false;
				} else {
					mData.unloadMusicBard = false;
				}
			} else if (oldNPC.getUniqueID().equals(npc.getUniqueID())) {
				if (isStreamer) {
					mData.unloadSongBard = false;
					mData.songBard = npc;
				} else {
					mData.musicBard = npc;
					mData.unloadMusicBard = false;
				}
			}
		}
		if (!mData.isBardPlaying(song, isStreamer)) { // not bard play song
			if (!getPlayerInRange()) { return; }
			mData.bardPlaySound(song, isStreamer, npc);
		}
		else if (npc.equals(isStreamer ? mData.songBard : mData.musicBard) && !song.equals(isStreamer ? mData.song : mData.music)) {
			if (!mData.song.isEmpty() && npc.equals(mData.songBard)) {
				mData.stopSound(mData.song, SoundCategory.AMBIENT);
			}
			if (!mData.music.isEmpty() && npc.equals(mData.musicBard)) {
				mData.stopSound(mData.music, SoundCategory.MUSIC);
			}
		}
		else if (!npc.equals(isStreamer ? mData.songBard : mData.musicBard)) {
			EntityPlayer player = CustomNpcs.proxy.getPlayer();
			if (player == null) { return; }
			EntityNPCInterface oldNPC = isStreamer ? mData.songBard : mData.musicBard;
			if (oldNPC == null || npc.getDistance(player) < oldNPC.getDistance(player)) {
				if (getPlayerInRange()) {
					String mSong = isStreamer ? mData.song : mData.music;
					if (mSong.equals(song)) {
						if (isStreamer) {
							mData.songBard = npc;
							mData.music = "";
							mData.musicBard = null;
						} else {
							mData.song = "";
							mData.songBard = null;
							mData.musicBard = npc;
						}
						mData.setNewPosSong(mSong, (float) npc.posX, (float) npc.posY, (float) npc.posZ);
					} else {
						mData.stopSound(mSong, isStreamer ? SoundCategory.AMBIENT : SoundCategory.MUSIC);
						mData.bardPlaySound(song, isStreamer, npc);
					}
				}
			}
		} // check main NPC
		else if (hasOffRange && npc.equals(isStreamer ? mData.songBard : mData.musicBard)) {
			if (!getPlayerInRange()) { mData.stopSound(song, isStreamer ? SoundCategory.AMBIENT : SoundCategory.MUSIC); }
		} // check Distance
	}

	private boolean getPlayerInRange() {
		EntityPlayer player = CustomNpcs.proxy.getPlayer();
		if (player == null) { return false; }
		long now = System.currentTimeMillis();
		if (now < checkTime) { return Boolean.TRUE.equals(cachedInRange.get()); }
		AxisAlignedBB aabb = npc.getEntityBoundingBox();
		if (isRange) { aabb = aabb.grow(range[0], range[0], range[0]); }
		else {
			aabb = new AxisAlignedBB(aabb.minX - minPos[0], aabb.minY - minPos[1], aabb.minZ - minPos[2],
					aabb.maxX + minPos[0], aabb.maxY + minPos[1], aabb.maxZ + minPos[2]);
		}
		List<EntityPlayer> list = new ArrayList<>();
		try { list = npc.world.getEntitiesWithinAABB(EntityPlayer.class, aabb); } catch (Exception ignored) { }
		boolean result = list.contains(CustomNpcs.proxy.getPlayer());
		cachedInRange = new WeakReference<>(result);
		checkTime = now + 500L;
		return result;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		type = JobType.BARD;
		song = compound.getString("BardSong");
		isStreamer = compound.getBoolean("BardStreamer");
		hasOffRange = compound.getBoolean("BardHasOff");

		if (compound.hasKey("BardRangeData", 7) && compound.hasKey("BardIsRange", 1)) {
			isRange = compound.getBoolean("BardIsRange");
			byte[] data = compound.getByteArray("BardRangeData");
			if (data.length > 1) {
				range = new int[] { getIntInByte(data[0]), getIntInByte(data[1]) };
			}
			if (data.length > 4) {
				minPos = new int[] { getIntInByte(data[2]), getIntInByte(data[3]), getIntInByte(data[4]) };
			} else {
				maxPos = new int[] { range[0], range[0], range[0] };
			}
			if (data.length > 7) {
				maxPos = new int[] { getIntInByte(data[5]), getIntInByte(data[6]), getIntInByte(data[7]) };
			} else {
				maxPos = new int[] { range[1], range[1], range[1] };
			}
		}
		else if (compound.hasKey("BardMinRange", 3) && compound.hasKey("BardMaxRange", 3)) {
			range = new int[] { compound.getInteger("BardMinRange"), compound.getInteger("BardMaxRange") };
			isRange = true;
			minPos = new int[] { range[0], range[0], range[0] };
			maxPos = new int[] { range[1], range[1], range[1] };
		}
	}

	@Override
	public void setSong(String newSong) {
		song = newSong;
		npc.updateClient = true;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("Type", JobType.BARD.get());
		compound.setString("BardSong", song);
		compound.setBoolean("BardStreamer", isStreamer);
		compound.setBoolean("BardHasOff", hasOffRange);
		compound.setBoolean("BardIsRange", isRange);
		compound.setByteArray("BardRangeData",
				new byte[] { (byte) range[0], (byte) range[1], (byte) minPos[0], (byte) minPos[1],
						(byte) minPos[2], (byte) maxPos[0], (byte) maxPos[1], (byte) maxPos[2] });
		return compound;
	}

}
