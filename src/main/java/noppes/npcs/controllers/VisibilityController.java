package noppes.npcs.controllers;

import java.util.*;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.internal.FMLMessage;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.LogWriter;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.entity.EntityNPCInterface;

public class VisibilityController {

	public static final Map<String, EntityNPCInterface> trackedEntityHashTable = new HashMap<>();
	public static final Map<EntityPlayerMP, List<EntityNPCInterface>> invisibleNPCsTable = new HashMap<>();

	public VisibilityController() { }

	public void checkIsVisible(EntityNPCInterface npc, EntityPlayerMP playerMP) {
		if (!CustomNpcs.EnableInvisibleNpcs) {
			return;
		}
		boolean isVisible = playerMP.capabilities.isCreativeMode || playerMP.getHeldItemMainhand().getItem() == CustomRegisters.wand || npc.display.isVisibleTo(playerMP);

		if (!VisibilityController.invisibleNPCsTable.containsKey(playerMP)) { VisibilityController.invisibleNPCsTable.put(playerMP, new ArrayList<>()); }

		if (isVisible && VisibilityController.invisibleNPCsTable.get(playerMP).contains(npc)) {
			if (VisibilityController.invisibleNPCsTable.get(playerMP).remove(npc)) {
				EntityRegistry.EntityRegistration er = EntityRegistry.instance().lookupModSpawn(npc.getClass(), false);
				if (er != null) {
					FMLMessage.EntitySpawnMessage message = new FMLMessage.EntitySpawnMessage(er, npc, er.getContainer());
					Server.sendData(playerMP, EnumPacketClient.VISIBLE_TRUE, npc.getUniqueID(), npc.getEntityId(), message);
				}
			}
		}
		else if (!isVisible && !VisibilityController.invisibleNPCsTable.get(playerMP).contains(npc)) {
			VisibilityController.invisibleNPCsTable.get(playerMP).add(npc);
			Server.sendData(playerMP, EnumPacketClient.VISIBLE_FALSE, npc.getUniqueID(), npc.getEntityId());
		}
	}

	public void onUpdate(EntityPlayerMP playerMP) { // check Visible to Player
		if (!CustomNpcs.EnableInvisibleNpcs) { return; }
		if (!VisibilityController.invisibleNPCsTable.containsKey(playerMP)) {
			VisibilityController.invisibleNPCsTable.put(playerMP, new ArrayList<>());
		}
		EntityNPCInterface npc = null;
		try {
			List<String> del = new ArrayList<>();
			Set<String> set = new HashSet<>(VisibilityController.trackedEntityHashTable.keySet());
			for (String key : set) {
				npc = VisibilityController.trackedEntityHashTable.get(key);
				if (npc == null || npc.world == null || npc.world.getEntityByID(npc.getEntityId()) == null) {
					if (npc != null) { del.add(key); }
					continue;
				}
				if (playerMP.world == null || npc.world.provider.getDimension() != playerMP.world.provider.getDimension() || npc.display.getVisible() != 1) {
					continue;
				}
				this.checkIsVisible(npc, playerMP);
			}
			for (String npcName : del) {
				VisibilityController.trackedEntityHashTable.remove(npcName);
			} // clear RAM
		} catch (Exception e) {
			LogWriter.debug("CNPCs: npc: "+npc);
			LogWriter.debug("CNPCs: npc.world: "+(npc != null ? npc.world : "n/a"));
			LogWriter.debug("CNPCs: npc.getEntityId(): "+(npc != null ? npc.getEntityId() : "n/a"));
			LogWriter.error("Error:", e);
		}
	}

	public void trackNpc(EntityNPCInterface npc) { // from DataDisplay
		if (npc == null || npc.world == null) {
			return;
		}
		String key = npc.world.provider.getDimension() + "_" + npc.getUniqueID();
		if (VisibilityController.trackedEntityHashTable.containsKey(key)) {
			return;
		}
		VisibilityController.trackedEntityHashTable.put(key, npc);
	}

}
