package noppes.npcs.controllers;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.ForgeChunkManager;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.constants.JobType;
import noppes.npcs.entity.EntityNPCInterface;

public class ChunkController implements ForgeChunkManager.LoadingCallback {
	public static ChunkController instance;
	private final HashMap<Entity, ForgeChunkManager.Ticket> tickets = new HashMap<>();

	public ChunkController() {
		ChunkController.instance = this;
	}

	public void clear() {
		tickets.clear();
	}

	public void deleteNPC(EntityNPCInterface npc) {
		ForgeChunkManager.Ticket ticket = tickets.get(npc);
		if (ticket != null) {
			tickets.remove(npc);
			ForgeChunkManager.releaseTicket(ticket);
		}
	}

	public ForgeChunkManager.Ticket getTicket(EntityNPCInterface npc) {
		ForgeChunkManager.Ticket ticket = tickets.get(npc);
		if (ticket != null) {
			return ticket;
		}
		if (this.size() >= CustomNpcs.ChuckLoaders) {
			return null;
		}
		ticket = ForgeChunkManager.requestTicket(CustomNpcs.instance, npc.world, ForgeChunkManager.Type.ENTITY);
		if (ticket == null) {
			return null;
		}
		ticket.bindEntity(npc);
		ticket.setChunkListDepth(6);
		tickets.put(npc, ticket);
		return null;
	}

	public int size() {
		return tickets.size();
	}

	public boolean hasToNpc(EntityNPCInterface npc) {
		return tickets.containsKey(npc);
	}

	@SuppressWarnings("unlikely-arg-type")
	@Override
	public void ticketsLoaded(List<ForgeChunkManager.Ticket> tickets, World world) {
		CustomNpcs.debugData.start(null);
		for (ForgeChunkManager.Ticket ticket : tickets) {
			if (!(ticket.getEntity() instanceof EntityNPCInterface)) {
				continue;
			}
			EntityNPCInterface npc = (EntityNPCInterface) ticket.getEntity();
			if (npc.advanced.jobInterface.getEnumType() != JobType.CHUNK_LOADER) {
				continue;
			}
			this.tickets.put(npc, ticket);
			// 3x3
			int x = MathHelper.floor(npc.posX);
			int z = MathHelper.floor(npc.posZ);
			for (int u = -1; u < 2; u++) {
				for (int v = -1; v < 2; v++) {
					ForgeChunkManager.forceChunk(ticket, new ChunkPos(x + u, z + v));
				}
			}
		}
		CustomNpcs.debugData.end(null);
	}

	public void unload(int toRemove) {
		Iterator<Entity> ite = tickets.keySet().iterator();
		int i = 0;
		while (ite.hasNext()) {
			if (i >= toRemove) {
				return;
			}
			Entity entity = ite.next();
			ForgeChunkManager.releaseTicket(tickets.get(entity));
			ite.remove();
			++i;
		}
	}

}
