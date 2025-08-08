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

public class JobChunkLoader extends JobInterface implements IJobChunkLoader {

	private List<ChunkPos> chunks = new ArrayList<>();
	private long playerLastSeen = 0L;
	private int ticks = 20;

	public JobChunkLoader(EntityNPCInterface npc) {
		super(npc);
		this.type = JobType.CHUNK_LOADER;
	}

	@Override
	public boolean isWorking() {
		return !chunks.isEmpty() || ChunkController.instance.hasToNpc(npc);
	}

	@Override
	public boolean aiContinueExecute() {
		return false;
	}

	@Override
	public boolean aiShouldExecute() {
		--ticks;
		if (ticks > 0) {
			return false;
		}
		ticks = 20;
		List<EntityPlayer> players = new ArrayList<>();
		try {
			players = this.npc.world.getEntitiesWithinAABB(EntityPlayer.class, npc.getEntityBoundingBox().grow(48.0, 48.0, 48.0));
		}
		catch (Exception ignored) { }
		if (!players.isEmpty()) {
			playerLastSeen = System.currentTimeMillis();
		}
		if (System.currentTimeMillis() > playerLastSeen + 600000L) {
			ChunkController.instance.deleteNPC(npc);
			chunks.clear();
			return false;
		}
		ForgeChunkManager.Ticket ticket = ChunkController.instance.getTicket(npc);
		if (ticket == null) {
			return false;
		}
		List<ChunkPos> list = new ArrayList<>();
		// 3x3
		int x = MathHelper.floor(npc.posX);
		int z = MathHelper.floor(npc.posZ);
		for (int u = -1; u < 2; u++) {
			for (int v = -1; v < 2; v++) {
				list.add(new ChunkPos(x + u, z + v));
			}
		}
		for (ChunkPos chunk : list) {
			if (!chunks.contains(chunk)) {
				ForgeChunkManager.forceChunk(ticket, chunk);
			} else {
				chunks.remove(chunk);
			}
		}
		for (ChunkPos chunk : chunks) {
			ForgeChunkManager.unforceChunk(ticket, chunk);
		}
		chunks = list;
		return false;
	}

    @Override
	public void load(NBTTagCompound compound) {
		super.load(compound);
		type = JobType.CHUNK_LOADER;
		playerLastSeen = compound.getLong("ChunkPlayerLastSeen");
	}

	@Override
	public void reset() {
		ChunkController.instance.deleteNPC(npc);
		chunks.clear();
		playerLastSeen = 0L;
	}

	@Override
	public NBTTagCompound save(NBTTagCompound compound) {
		super.save(compound);
		compound.setLong("ChunkPlayerLastSeen", this.playerLastSeen);
		return compound;
	}

}
