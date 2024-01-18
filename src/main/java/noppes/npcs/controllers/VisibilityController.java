package noppes.npcs.controllers;

import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.internal.FMLMessage;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomRegisters;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.entity.EntityNPCInterface;

public class VisibilityController {
	
	public static Map<String, EntityNPCInterface> trackedEntityHashTable = Maps.<String, EntityNPCInterface>newHashMap();
	public static Map<EntityPlayerMP, List<EntityNPCInterface>> invisibleNPCsTable = Maps.<EntityPlayerMP, List<EntityNPCInterface>>newHashMap();
	
	public static void trackNpc(EntityNPCInterface npc) { // trom DataDisplay
		if (npc == null || npc.world == null) { return; }
		String key = npc.world.provider.getDimension()+"_"+npc.getUniqueID().toString();
		if (VisibilityController.trackedEntityHashTable.containsKey(key)) { return; }
		VisibilityController.trackedEntityHashTable.put(key, npc);
	}
	
	public static void onUpdate(EntityPlayerMP playerMP) { // cheak Visible to Player
		if (!CustomNpcs.EnableInvisibleNpcs) { return; }
		if (!VisibilityController.invisibleNPCsTable.containsKey(playerMP)) { VisibilityController.invisibleNPCsTable.put(playerMP, Lists.<EntityNPCInterface>newArrayList()); }
		try {
			List<String> del = Lists.<String>newArrayList();
			for (String key : VisibilityController.trackedEntityHashTable.keySet()) {
				EntityNPCInterface npc = VisibilityController.trackedEntityHashTable.get(key);
				if (npc==null || npc.world.getEntityByID(npc.getEntityId()) == null) {
					if (npc != null) { del.add(key); }
					continue;
				}
				if (npc.world.provider.getDimension()!=playerMP.world.provider.getDimension() || !npc.display.hasVisibleOptions()) { continue; }
				checkIsVisible(npc, playerMP);
			}
			for (String npc : del) { VisibilityController.trackedEntityHashTable.remove(npc); } // clear RAM
		}
		catch (Exception e) { }
	}
	
	public static void checkIsVisible(EntityNPCInterface npc, EntityPlayerMP playerMP) {
		if (!CustomNpcs.EnableInvisibleNpcs) { return; }
		boolean anyVisible = playerMP.capabilities.isCreativeMode || playerMP.getHeldItemMainhand().getItem()==CustomRegisters.wand;
		boolean isVisible = npc.display.isVisibleTo(playerMP);
		if (!VisibilityController.invisibleNPCsTable.containsKey(playerMP)) { VisibilityController.invisibleNPCsTable.put(playerMP, Lists.<EntityNPCInterface>newArrayList()); }
		if (anyVisible || (isVisible && VisibilityController.invisibleNPCsTable.get(playerMP).contains(npc))) {
			if (VisibilityController.invisibleNPCsTable.get(playerMP).remove(npc)) {
				EntityRegistry.EntityRegistration er = EntityRegistry.instance().lookupModSpawn(npc.getClass(), false);
				FMLMessage.EntitySpawnMessage message = new FMLMessage.EntitySpawnMessage(er, npc, er.getContainer());
				Server.sendData(playerMP, EnumPacketClient.VISIBLE_TRUE, npc.getUniqueID(), npc.getEntityId(), message);
			}
		}
		else if (!isVisible && !VisibilityController.invisibleNPCsTable.get(playerMP).contains(npc)) {
			VisibilityController.invisibleNPCsTable.get(playerMP).add(npc);
			Server.sendData(playerMP, EnumPacketClient.VISIBLE_FALSE, npc.getUniqueID(), npc.getEntityId());
		}
	}
	
}
