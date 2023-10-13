package noppes.npcs.controllers;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraftforge.fml.common.network.internal.FMLMessage;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import noppes.npcs.CustomRegisters;
import noppes.npcs.CustomNpcs;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.entity.EntityNPCInterface;

public class VisibilityController {
	
	public static Set<EntityNPCInterface> trackedEntityHashTable = Sets.<EntityNPCInterface>newHashSet();
	public static Map<EntityPlayerMP, List<EntityNPCInterface>> invisibleNPCsTable = Maps.<EntityPlayerMP, List<EntityNPCInterface>>newHashMap();
	
	public static void trackNpc(EntityNPCInterface npc) { // trom DataDisplay
		if (VisibilityController.trackedEntityHashTable.contains(npc)) { return; }
		VisibilityController.trackedEntityHashTable.add(npc);
	}
	
	public static void onUpdate(EntityPlayerMP playerMP) { // cheak Visible to Player
		if (!CustomNpcs.EnableInvisibleNpcs) { return; }
		if (!VisibilityController.invisibleNPCsTable.containsKey(playerMP)) { VisibilityController.invisibleNPCsTable.put(playerMP, Lists.<EntityNPCInterface>newArrayList()); }
		for (EntityNPCInterface npc : VisibilityController.trackedEntityHashTable) {
			if (!npc.display.hasVisibleOptions()) { continue; }
			if (npc==null || npc.world.provider.getDimension()!=playerMP.world.provider.getDimension()) { continue; }
			checkIsVisible(npc, playerMP);
		}
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
