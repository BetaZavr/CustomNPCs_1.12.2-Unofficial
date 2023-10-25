package noppes.npcs.roles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.ForgeChunkManager;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.api.entity.data.role.IJobChunkLoader;
import noppes.npcs.controllers.ChunkController;
import noppes.npcs.entity.EntityNPCInterface;

public class JobChunkLoader
extends JobInterface
implements IJobChunkLoader {
	
	private List<ChunkPos> chunks;
	private long playerLastSeen;
	private int ticks;

	public JobChunkLoader(EntityNPCInterface npc) {
		super(npc);
		this.chunks = new ArrayList<ChunkPos>();
		this.ticks = 20;
		this.playerLastSeen = 0L;
		this.type = JobType.CHUNK_LOADER;
	}

	@Override
	public boolean aiContinueExecute() {
		return false;
	}

	@Override
	public boolean aiShouldExecute() {
		--this.ticks;
		if (this.ticks > 0) {
			return false;
		}
		this.ticks = 20;
		List<EntityPlayer> players = this.npc.world.getEntitiesWithinAABB(EntityPlayer.class,
				this.npc.getEntityBoundingBox().grow(48.0, 48.0, 48.0));
		if (!players.isEmpty()) {
			this.playerLastSeen = System.currentTimeMillis();
		}
		if (System.currentTimeMillis() > this.playerLastSeen + 600000L) {
			ChunkController.instance.deleteNPC(this.npc);
			this.chunks.clear();
			return false;
		}
		ForgeChunkManager.Ticket ticket = ChunkController.instance.getTicket(this.npc);
		if (ticket == null) {
			return false;
		}
		List<ChunkPos> list = new ArrayList<ChunkPos>();
		// New 3x3
		int x = (int) MathHelper.floor(this.npc.posX);
		int z = (int) MathHelper.floor(this.npc.posZ);
		for (int u = -1; u < 2; u++) {
			for (int v = -1; v < 2; v++) {
				list.add(new ChunkPos(x + u, z + v));
			}
		}
		for (ChunkPos chunk : list) {
			if (!this.chunks.contains(chunk)) {
				ForgeChunkManager.forceChunk(ticket, chunk);
			} else {
				this.chunks.remove(chunk);
			}
		}
		for (ChunkPos chunk : this.chunks) {
			ForgeChunkManager.unforceChunk(ticket, chunk);
		}
		this.chunks = list;
		return false;
	}

	@Override
	public void delete() {
	}

	@Override
	public void reset() {
		ChunkController.instance.deleteNPC(this.npc);
		this.chunks.clear();
		this.playerLastSeen = 0L;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.type = JobType.CHUNK_LOADER;
		this.playerLastSeen = compound.getLong("ChunkPlayerLastSeen");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("Type", JobType.CHUNK_LOADER.get());
		compound.setLong("ChunkPlayerLastSeen", this.playerLastSeen);
		return compound;
	}
}
