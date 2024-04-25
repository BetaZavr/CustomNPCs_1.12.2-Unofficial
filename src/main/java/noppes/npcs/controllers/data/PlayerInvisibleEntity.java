package noppes.npcs.controllers.data;

import java.util.List;
import java.util.UUID;

import com.google.common.collect.Lists;

import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.entity.EntityNPCInterface;

public class PlayerInvisibleEntity {

	public int dimensionID;
	public List<UUID> entityUUIDs;

	public PlayerInvisibleEntity(EntityPlayerMP playerMP, EntityNPCInterface npc) {
		this.dimensionID = playerMP.world.provider.getDimension();
		this.entityUUIDs = Lists.<UUID>newArrayList();
		if (npc != null) {
			this.entityUUIDs.add(npc.getUniqueID());
		}
	}

	public void add(UUID uuid) {
		if (this.contains(uuid)) {
			return;
		}
		this.entityUUIDs.add(uuid);
	}

	public void clear(int dimID) {
		this.dimensionID = dimID;
		this.entityUUIDs.clear();
	}

	public boolean contains(UUID uuid) {
		for (UUID id : this.entityUUIDs) {
			if (id.equals(uuid)) {
				return true;
			}
		}
		return false;
	}

	public void remove(UUID uuid) {
		for (UUID id : this.entityUUIDs) {
			if (id.equals(uuid)) {
				this.entityUUIDs.remove(id);
				return;
			}
		}
	}

}
