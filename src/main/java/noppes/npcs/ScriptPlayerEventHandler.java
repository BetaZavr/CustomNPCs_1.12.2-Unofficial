package noppes.npcs;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.reflect.ClassPath;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.ItemFishedEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.GetCollisionBoxesEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.eventhandler.Event;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.GenericEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.PlayerEvent.ItemCraftedEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.relauncher.Side;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.event.ForgeEvent;
import noppes.npcs.api.event.ItemEvent;
import noppes.npcs.api.event.PlayerEvent;
import noppes.npcs.api.wrapper.ItemScriptedWrapper;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerScriptData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.items.ItemBoundary;
import noppes.npcs.items.ItemBuilder;
import noppes.npcs.items.ItemNbtBook;
import noppes.npcs.items.ItemScripted;

public class ScriptPlayerEventHandler {
	public class ForgeEventHandler {

		@SubscribeEvent
		public void forgeEntity(Event event) {
			if (CustomNpcs.Server == null || !ScriptController.Instance.forgeScripts.isEnabled()) {
				return;
			}
			if (event instanceof EntityEvent) {
				EntityEvent ev = (EntityEvent) event;
				if (ev.getEntity() == null || !(ev.getEntity().world instanceof WorldServer)) {
					return;
				}
				EventHooks.onForgeEntityEvent(ev);
			} else if (event instanceof WorldEvent) {
				WorldEvent ev2 = (WorldEvent) event;
				if (!(ev2.getWorld() instanceof WorldServer)) {
					return;
				}
				EventHooks.onForgeWorldEvent(ev2);
			} else {
				if (event instanceof TickEvent && ((TickEvent) event).side == Side.CLIENT) {
					return;
				}
				if (event instanceof net.minecraftforge.fml.common.gameevent.PlayerEvent) {
					net.minecraftforge.fml.common.gameevent.PlayerEvent ev3 = (net.minecraftforge.fml.common.gameevent.PlayerEvent) event;
					if (!(ev3.player.world instanceof WorldServer)) {
						return;
					}
				}
				EventHooks.onForgeEvent(new ForgeEvent(event), event);
			}
		}

	}

	@SubscribeEvent
	public void invoke(ArrowLooseEvent event) {
		if (event.getEntityPlayer().world.isRemote || !(event.getWorld() instanceof WorldServer)) {
			return;
		}
		PlayerScriptData handler = PlayerData.get(event.getEntityPlayer()).scriptData;
		PlayerEvent.RangedLaunchedEvent ev = new PlayerEvent.RangedLaunchedEvent(handler.getPlayer());
		event.setCanceled(EventHooks.onPlayerRanged(handler, ev));
	}

	@SubscribeEvent
	public void invoke(BlockEvent.BreakEvent event) {
		if (event.getPlayer().world.isRemote || !(event.getWorld() instanceof WorldServer)) {
			return;
		}
		PlayerScriptData handler = PlayerData.get(event.getPlayer()).scriptData;
		PlayerEvent.BreakEvent ev = new PlayerEvent.BreakEvent(handler.getPlayer(),
				NpcAPI.Instance().getIBlock(event.getWorld(), event.getPos()), event.getExpToDrop());
		event.setCanceled(EventHooks.onPlayerBreak(handler, ev));
		event.setExpToDrop(ev.exp);
	}

	@SubscribeEvent
	public void invoke(EntityItemPickupEvent event) {
		if (!(event.getEntityPlayer().world instanceof WorldServer)) {
			return;
		}
		PlayerScriptData handler = PlayerData.get(event.getEntityPlayer()).scriptData;
		event.setCanceled(EventHooks.onPlayerPickUp(handler, event.getItem()));
	}

	@SubscribeEvent
	public void invoke(ItemCraftedEvent event) {
		if (!(event.player.world instanceof WorldServer)) {
			return;
		}
		EventHooks.onPlayerCrafted(PlayerData.get(event.player).scriptData, event.crafting, event.craftMatrix);
		event.player.world.getChunkFromChunkCoords(0, 0).onLoad();
	}

	// New
	@SubscribeEvent
	public void invoke(ItemFishedEvent event) {
		if (!(event.getEntityPlayer().world instanceof WorldServer)) {
			return;
		}
		event.setCanceled(EventHooks.onPlayerFished(PlayerData.get(event.getEntityPlayer()).scriptData,
				event.getDrops(), event.getRodDamage()));
	}

	@SubscribeEvent
	public void invoke(ItemTossEvent event) {
		if (!(event.getPlayer().world instanceof WorldServer)) {
			return;
		}
		PlayerScriptData handler = PlayerData.get(event.getPlayer()).scriptData;
		event.setCanceled(EventHooks.onPlayerToss(handler, event.getEntityItem()));
	}

	@SubscribeEvent
	public void invoke(LivingAttackEvent event) {
		if (!(event.getEntityLiving().world instanceof WorldServer)) {
			return;
		}
		Entity source = NoppesUtilServer.GetDamageSourcee(event.getSource());
		if (source instanceof EntityPlayer) {
			PlayerScriptData handler = PlayerData.get((EntityPlayer) source).scriptData;
			ItemStack item = ((EntityPlayer) source).getHeldItemMainhand();
			IEntity<?> target = NpcAPI.Instance().getIEntity(event.getEntityLiving());
			PlayerEvent.AttackEvent ev = new PlayerEvent.AttackEvent(handler.getPlayer(), 1, target);
			event.setCanceled(EventHooks.onPlayerAttack(handler, ev));
			if (item.getItem() == CustomItems.scripted_item && !event.isCanceled()) {
				ItemScriptedWrapper isw = ItemScripted.GetWrapper(item);
				ItemEvent.AttackEvent eve = new ItemEvent.AttackEvent(isw, handler.getPlayer(), 1, target);
				eve.setCanceled(event.isCanceled());
				event.setCanceled(EventHooks.onScriptItemAttack(isw, eve));
			}
		}
	}

	@SubscribeEvent
	public void invoke(LivingDeathEvent event) {
		if (!(event.getEntityLiving().world instanceof WorldServer)) {
			return;
		}
		Entity source = NoppesUtilServer.GetDamageSourcee(event.getSource());
		if (event.getEntityLiving() instanceof EntityPlayer) {
			PlayerScriptData handler = PlayerData.get((EntityPlayer) event.getEntityLiving()).scriptData;
			EventHooks.onPlayerDeath(handler, event.getSource(), source);
		}
		if (source instanceof EntityPlayer) {
			PlayerScriptData handler = PlayerData.get((EntityPlayer) source).scriptData;
			EventHooks.onPlayerKills(handler, event.getEntityLiving());
		}
	}

	@SubscribeEvent
	public void invoke(LivingHurtEvent event) {
		if (!(event.getEntityLiving().world instanceof WorldServer)) {
			return;
		}
		Entity source = NoppesUtilServer.GetDamageSourcee(event.getSource());
		if (event.getEntityLiving() instanceof EntityPlayer) {
			PlayerScriptData handler = PlayerData.get((EntityPlayer) event.getEntityLiving()).scriptData;
			PlayerEvent.DamagedEvent pevent = new PlayerEvent.DamagedEvent(handler.getPlayer(), source,
					event.getAmount(), event.getSource());
			event.setCanceled(EventHooks.onPlayerDamaged(handler, pevent));
			event.setAmount(pevent.damage);
		}
		if (source instanceof EntityPlayer) {
			PlayerScriptData handler = PlayerData.get((EntityPlayer) source).scriptData;
			PlayerEvent.DamagedEntityEvent pevent2 = new PlayerEvent.DamagedEntityEvent(handler.getPlayer(),
					event.getEntityLiving(), event.getAmount(), event.getSource());
			event.setCanceled(EventHooks.onPlayerDamagedEntity(handler, pevent2));
			event.setAmount(pevent2.damage);
		}
	}

	@SubscribeEvent
	public void invoke(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.player.world instanceof WorldServer)) {
			return;
		}
		PlayerScriptData handler = PlayerData.get(event.player).scriptData;
		EventHooks.onPlayerLogin(handler);
	}

	@SubscribeEvent
	public void invoke(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent event) {
		if (!(event.player.world instanceof WorldServer)) {
			return;
		}
		PlayerScriptData handler = PlayerData.get(event.player).scriptData;
		EventHooks.onPlayerLogout(handler);
	}

	@SubscribeEvent
	public void invoke(PlayerContainerEvent.Close event) {
		if (!(event.getEntityPlayer().world instanceof WorldServer)) {
			return;
		}
		PlayerScriptData handler = PlayerData.get(event.getEntityPlayer()).scriptData;
		EventHooks.onPlayerContainerClose(handler, event.getContainer());
	}

	@SubscribeEvent
	public void invoke(PlayerContainerEvent.Open event) {
		if (!(event.getEntityPlayer().world instanceof WorldServer)) {
			return;
		}
		PlayerScriptData handler = PlayerData.get(event.getEntityPlayer()).scriptData;
		EventHooks.onPlayerContainerOpen(handler, event.getContainer());
	}

	@SubscribeEvent
	public void invoke(PlayerInteractEvent.EntityInteract event) {
		if (event.getEntityPlayer().world.isRemote || event.getHand() != EnumHand.MAIN_HAND
				|| !(event.getWorld() instanceof WorldServer)) {
			return;
		}
		if (event.getItemStack().getItem() == CustomItems.nbt_book) {
			((ItemNbtBook) event.getItemStack().getItem()).entityEvent(event);
			event.setCanceled(true);
			return;
		}
		PlayerScriptData handler = PlayerData.get(event.getEntityPlayer()).scriptData;
		PlayerEvent.InteractEvent ev = new PlayerEvent.InteractEvent(handler.getPlayer(), 1,
				NpcAPI.Instance().getIEntity(event.getTarget()));
		event.setCanceled(EventHooks.onPlayerInteract(handler, ev));
		if (event.getItemStack().getItem() == CustomItems.scripted_item && !event.isCanceled()) {
			ItemScriptedWrapper isw = ItemScripted.GetWrapper(event.getItemStack());
			ItemEvent.InteractEvent eve = new ItemEvent.InteractEvent(isw, handler.getPlayer(), 1,
					NpcAPI.Instance().getIEntity(event.getTarget()));
			event.setCanceled(EventHooks.onScriptItemInteract(isw, eve));
		}
	}

	@SubscribeEvent
	public void invoke(PlayerInteractEvent.LeftClickBlock event) {
		if (event.getHand()!=EnumHand.MAIN_HAND || event.getEntityPlayer().world.isRemote || !(event.getWorld() instanceof WorldServer)) {
			return;
		}
		if (event.getItemStack().getItem() == CustomItems.npcboundary) {
			((ItemBoundary) event.getItemStack().getItem()).leftClick(event.getItemStack(), (EntityPlayerMP) event.getEntityPlayer());
			event.setCanceled(true);
			return;
		}
		if (event.getItemStack().getItem() == CustomItems.npcbuilder) {
			((ItemBuilder) event.getItemStack().getItem()).leftClick(event.getItemStack(), (EntityPlayerMP) event.getEntityPlayer());
			event.setCanceled(true);
			return;
		}
		PlayerScriptData handler = PlayerData.get(event.getEntityPlayer()).scriptData;
		PlayerEvent.AttackEvent ev = new PlayerEvent.AttackEvent(handler.getPlayer(), 2,
				NpcAPI.Instance().getIBlock(event.getWorld(), event.getPos()));
		event.setCanceled(EventHooks.onPlayerAttack(handler, ev));
		if (event.getItemStack().getItem() == CustomItems.scripted_item && !event.isCanceled()) {
			ItemScriptedWrapper isw = ItemScripted.GetWrapper(event.getItemStack());
			ItemEvent.AttackEvent eve = new ItemEvent.AttackEvent(isw, handler.getPlayer(), 2,
					NpcAPI.Instance().getIBlock(event.getWorld(), event.getPos()));
			eve.setCanceled(event.isCanceled());
			event.setCanceled(EventHooks.onScriptItemAttack(isw, eve));
		}
	}

	@SubscribeEvent
	public void invoke(PlayerInteractEvent.RightClickBlock event) {
		if (event.getHand()!=EnumHand.MAIN_HAND || event.getEntityPlayer().world.isRemote || event.getHand() != EnumHand.MAIN_HAND
				|| !(event.getWorld() instanceof WorldServer)) {
			return;
		}
		if (event.getItemStack().getItem() == CustomItems.nbt_book) {
			((ItemNbtBook) event.getItemStack().getItem()).blockEvent(event);
			event.setCanceled(true);
			return;
		}
		if (event.getItemStack().getItem() == CustomItems.npcboundary) {
			((ItemBoundary) event.getItemStack().getItem()).rightClick(event.getItemStack(), (EntityPlayerMP) event.getEntityPlayer());
			event.setCanceled(true);
			return;
		}
		if (event.getItemStack().getItem() == CustomItems.npcbuilder) {
			((ItemBuilder) event.getItemStack().getItem()).rightClick(event.getItemStack(), (EntityPlayerMP) event.getEntityPlayer());
			event.setCanceled(true);
			return;
		}
		PlayerScriptData handler = PlayerData.get(event.getEntityPlayer()).scriptData;
		handler.hadInteract = true;
		PlayerEvent.InteractEvent ev = new PlayerEvent.InteractEvent(handler.getPlayer(), 2,
				NpcAPI.Instance().getIBlock(event.getWorld(), event.getPos()));
		event.setCanceled(EventHooks.onPlayerInteract(handler, ev));
		if (event.getItemStack().getItem() == CustomItems.scripted_item && !event.isCanceled()) {
			ItemScriptedWrapper isw = ItemScripted.GetWrapper(event.getItemStack());
			ItemEvent.InteractEvent eve = new ItemEvent.InteractEvent(isw, handler.getPlayer(), 2,
					NpcAPI.Instance().getIBlock(event.getWorld(), event.getPos()));
			event.setCanceled(EventHooks.onScriptItemInteract(isw, eve));
		}
	}

	@SubscribeEvent
	public void invoke(PlayerInteractEvent.RightClickItem event) {
		if (event.getHand()!=EnumHand.MAIN_HAND || event.getEntityPlayer().world.isRemote || !(event.getWorld() instanceof WorldServer)) {
			return;
		}
		if (event.getEntityPlayer().isCreative() && event.getEntityPlayer().isSneaking()
				&& event.getItemStack().getItem() == CustomItems.scripted_item) {
			NoppesUtilServer.sendOpenGui(event.getEntityPlayer(), EnumGuiType.ScriptItem, null);
			return;
		}
		// Empty Click:
		if (event.getItemStack().getItem() == CustomItems.nbt_book ||
				event.getItemStack().getItem() == CustomItems.npcboundary ||
				event.getItemStack().getItem() == CustomItems.npcbuilder) {
			EntityPlayer player = event.getEntityPlayer();
			Vec3d vec3d = player.getPositionEyes(1.0f);
			Vec3d vec3d2 = player.getLook(1.0f);
			Vec3d vec3d3 = vec3d.addVector(vec3d2.x * 6.0d, vec3d2.y * 6.0d, vec3d2.z * 6.0d);
			RayTraceResult result = player.world.rayTraceBlocks(vec3d, vec3d3, false, false, false);
			if (result!=null) { return; }
			if (!event.getEntityPlayer().world.isRemote && event.getItemStack().getItem() == CustomItems.nbt_book) {
				if (!player.getHeldItemOffhand().isEmpty()) {
					((ItemNbtBook) event.getItemStack().getItem()).itemEvent(event);
					return;
				}
			}
			if (event.getItemStack().getItem() == CustomItems.npcboundary) {
				((ItemBoundary) event.getItemStack().getItem()).rightClick(event.getItemStack(), (EntityPlayerMP) event.getEntityPlayer());
				return;
			}
			if (event.getItemStack().getItem() == CustomItems.npcbuilder) {
				((ItemBuilder) event.getItemStack().getItem()).rightClick(event.getItemStack(), (EntityPlayerMP) event.getEntityPlayer());
				return;
			}
		}
		PlayerScriptData handler = PlayerData.get(event.getEntityPlayer()).scriptData;
		if (handler.hadInteract) {
			handler.hadInteract = false;
			return;
		}
		PlayerEvent.InteractEvent ev = new PlayerEvent.InteractEvent(handler.getPlayer(), 0, null);
		event.setCanceled(EventHooks.onPlayerInteract(handler, ev));
		if (event.getItemStack().getItem() == CustomItems.scripted_item && !event.isCanceled()) {
			ItemScriptedWrapper isw = ItemScripted.GetWrapper(event.getItemStack());
			ItemEvent.InteractEvent eve = new ItemEvent.InteractEvent(isw, handler.getPlayer(), 0, null);
			event.setCanceled(EventHooks.onScriptItemInteract(isw, eve));
		}
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void invoke(ServerChatEvent event) {
		if (!(event.getPlayer().world instanceof WorldServer)
				|| event.getPlayer() == EntityNPCInterface.ChatEventPlayer) {
			return;
		}
		PlayerScriptData handler = PlayerData.get((EntityPlayer) event.getPlayer()).scriptData;
		String message = event.getMessage();
		PlayerEvent.ChatEvent ev = new PlayerEvent.ChatEvent(handler.getPlayer(), event.getMessage());
		EventHooks.onPlayerChat(handler, ev);
		event.setCanceled(ev.isCanceled());
		if (!message.equals(ev.message)) {
			TextComponentTranslation chat = new TextComponentTranslation("", new Object[0]);
			chat.appendSibling(ForgeHooks.newChatWithLinks(ev.message));
			event.setComponent(chat);
		}
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.PlayerTickEvent event) {
		if (event.side != Side.SERVER || event.phase != TickEvent.Phase.START) {
			return;
		}
		EntityPlayer player = event.player;
		PlayerData data = PlayerData.get(player);
		if (player.ticksExisted % 10 == 0) {
			EventHooks.onPlayerTick(data.scriptData);
			for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
				ItemStack item = player.inventory.getStackInSlot(i);
				if (!item.isEmpty() && item.getItem() == CustomItems.scripted_item) {
					ItemScriptedWrapper isw = (ItemScriptedWrapper) NpcAPI.Instance().getIItemStack(item);
					EventHooks.onScriptItemUpdate(isw, player);
					if (isw.updateClient) {
						isw.updateClient = false;
						Server.sendData((EntityPlayerMP) player, EnumPacketClient.UPDATE_ITEM, i, isw.getMCNbt());
					}
				}
			}
		}
		if (data.playerLevel != player.experienceLevel) {
			EventHooks.onPlayerLevelUp(data.scriptData, data.playerLevel - player.experienceLevel);
			data.playerLevel = player.experienceLevel;
		}
		data.timers.update();
	}

	public ScriptPlayerEventHandler registerForgeEvents() { // Changed
		ForgeEventHandler handler = new ForgeEventHandler();
		LogWriter.info("CustomNpcs: Start load Forge Events:");
		CustomNpcs.forgeEventNames.clear();
		List<Class<?>> listCalsses = new ArrayList<Class<?>>();
		try {
			// Get Maim mod Method for All Events
			Method m = handler.getClass().getMethod("forgeEntity", Event.class);
			// Get Registration Method for Event Methods
			Method register = MinecraftForge.EVENT_BUS.getClass().getDeclaredMethod("register", Class.class,
					Object.class, Method.class, ModContainer.class);
			register.setAccessible(true);

			ClassPath loader = ClassPath.from(this.getClass().getClassLoader());

			// Get all loaded Forge event classes
			List<ClassPath.ClassInfo> list = new ArrayList<ClassPath.ClassInfo>(
					loader.getTopLevelClassesRecursive("net.minecraftforge.event"));
			list.addAll(loader.getTopLevelClassesRecursive("net.minecraftforge.fml.common"));

			// New
			if (list.isEmpty()) { // It shouldn't be like this, but perhaps the manual filling option will help.
				LogWriter.error("CustomNpcs Error: Not found Forge Events in Loaded Classes");
				LogWriter.info("CustomNpcs: Trying to download manually");
				for (int i = 1; i < 85; i++) {
					Class<?> c = null;
					try {
						switch (i) {
						/** Forge Event Classes */
							case 1: { c = Class.forName("net.minecraftforge.event.AnvilUpdateEvent"); break; }
							case 2: { c = Class.forName("net.minecraftforge.event.AttachCapabilitiesEvent"); break; }
							case 3: { c = Class.forName("net.minecraftforge.event.CommandEvent"); break; }
							case 4: { c = Class.forName("net.minecraftforge.event.DifficultyChangeEvent"); break; }
							case 5: { c = Class.forName("net.minecraftforge.event.GameRuleChangeEvent"); break; }
							case 6: { c = Class.forName("net.minecraftforge.event.LootTableLoadEvent"); break; }
							case 7: { c = Class.forName("net.minecraftforge.event.RegistryEvent"); break; }
							case 8: { c = Class.forName("net.minecraftforge.event.ServerChatEvent"); break; }
							case 9: { c = Class.forName("net.minecraftforge.event.brewing.PlayerBrewedPotionEvent"); break; }
							case 10: { c = Class.forName("net.minecraftforge.event.brewing.PotionBrewEvent"); break; }
							case 11: { c = Class.forName("net.minecraftforge.event.enchanting.EnchantmentLevelSetEvent"); break; }
							case 12: { c = Class.forName("net.minecraftforge.event.entity.EntityJoinWorldEvent"); break; }
							case 13: { c = Class.forName("net.minecraftforge.event.entity.EntityMobGriefingEvent"); break; }
							case 14: { c = Class.forName("net.minecraftforge.event.entity.EntityMountEvent"); break; }
							case 15: { c = Class.forName("net.minecraftforge.event.entity.EntityStruckByLightningEvent"); break; }
							case 16: { c = Class.forName("net.minecraftforge.event.entity.EntityTravelToDimensionEvent"); break; }
							case 17: { c = Class.forName("net.minecraftforge.event.entity.PlaySoundAtEntityEvent"); break; }
							case 18: { c = Class.forName("net.minecraftforge.event.entity.ProjectileImpactEvent"); break; }
							case 19: { c = Class.forName("net.minecraftforge.event.entity.ThrowableImpactEvent"); break; }
							case 20: { c = Class.forName("net.minecraftforge.event.entity.item.ItemEvent"); break; }
							case 21: { c = Class.forName("net.minecraftforge.event.entity.item.ItemExpireEvent"); break; }
							case 22: { c = Class.forName("net.minecraftforge.event.entity.item.ItemTossEvent"); break; }
							case 23: { c = Class.forName("net.minecraftforge.event.entity.living.AnimalTameEvent"); break; }
							case 24: { c = Class.forName("net.minecraftforge.event.entity.living.BabyEntitySpawnEvent"); break; }
							case 25: { c = Class.forName("net.minecraftforge.event.entity.living.EnderTeleportEvent"); break; }
							case 26: { c = Class.forName("net.minecraftforge.event.entity.living.LivingAttackEvent"); break; }
							case 27: { c = Class.forName("net.minecraftforge.event.entity.living.LivingDamageEvent"); break; }
							case 28: { c = Class.forName("net.minecraftforge.event.entity.living.LivingDeathEvent"); break; }
							case 29: { c = Class.forName("net.minecraftforge.event.entity.living.LivingDestroyBlockEvent"); break; }
							case 30: { c = Class.forName("net.minecraftforge.event.entity.living.LivingDropsEvent"); break; }
							case 31: { c = Class.forName("net.minecraftforge.event.entity.living.LivingEntityUseItemEvent"); break; }
							case 32: { c = Class.forName("net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent"); break; }
							case 33: { c = Class.forName("net.minecraftforge.event.entity.living.LivingEvent"); break; }
							case 34: { c = Class.forName("net.minecraftforge.event.entity.living.LivingExperienceDropEvent"); break; }
							case 35: { c = Class.forName("net.minecraftforge.event.entity.living.LivingFallEvent"); break; }
							case 36: { c = Class.forName("net.minecraftforge.event.entity.living.LivingHealEvent"); break; }
							case 37: { c = Class.forName("net.minecraftforge.event.entity.living.LivingHurtEvent"); break; }
							case 38: { c = Class.forName("net.minecraftforge.event.entity.living.LivingKnockBackEvent"); break; }
							case 39: { c = Class.forName("net.minecraftforge.event.entity.living.LivingPackSizeEvent"); break; }
							case 40: { c = Class.forName("net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent"); break; }
							case 41: { c = Class.forName("net.minecraftforge.event.entity.living.LivingSpawnEvent"); break; }
							case 42: { c = Class.forName("net.minecraftforge.event.entity.living.LootingLevelEvent"); break; }
							case 43: { c = Class.forName("net.minecraftforge.event.entity.living.PotionColorCalculationEvent"); break; }
							case 44: { c = Class.forName("net.minecraftforge.event.entity.living.ZombieEvent"); break; }
							case 45: { c = Class.forName("net.minecraftforge.event.entity.minecart.MinecartCollisionEvent"); break; }
							case 46: { c = Class.forName("net.minecraftforge.event.entity.minecart.MinecartEvent"); break; }
							case 47: { c = Class.forName("net.minecraftforge.event.entity.minecart.MinecartInteractEvent"); break; }
							case 48: { c = Class.forName("net.minecraftforge.event.entity.minecart.MinecartUpdateEvent"); break; }
							case 49: { c = Class.forName("net.minecraftforge.event.entity.player.AdvancementEvent"); break; }
							case 50: { c = Class.forName("net.minecraftforge.event.entity.player.AnvilRepairEvent"); break; }
							case 51: { c = Class.forName("net.minecraftforge.event.entity.player.ArrowLooseEvent"); break; }
							case 52: { c = Class.forName("net.minecraftforge.event.entity.player.ArrowNockEvent"); break; }
							case 53: { c = Class.forName("net.minecraftforge.event.entity.player.AttackEntityEvent"); break; }
							case 54: { c = Class.forName("net.minecraftforge.event.entity.player.BonemealEvent"); break; }
							case 55: { c = Class.forName("net.minecraftforge.event.entity.player.CriticalHitEvent"); break; }
							case 56: { c = Class.forName("net.minecraftforge.event.entity.player.EntityItemPickupEvent"); break; }
							case 57: { c = Class.forName("net.minecraftforge.event.entity.player.FillBucketEvent"); break; }
							case 58: { c = Class.forName("net.minecraftforge.event.entity.player.ItemFishedEvent"); break; }
							case 59: { c = Class.forName("net.minecraftforge.event.entity.player.PlayerContainerEvent"); break; }
							case 60: { c = Class.forName("net.minecraftforge.event.entity.player.PlayerDestroyItemEvent"); break; }
							case 61: { c = Class.forName("net.minecraftforge.event.entity.player.PlayerDropsEvent"); break; }
							case 62: { c = Class.forName("net.minecraftforge.event.entity.player.PlayerEvent"); break; }
							case 63: { c = Class.forName("net.minecraftforge.event.entity.player.PlayerFlyableFallEvent"); break; }
							case 64: { c = Class.forName("net.minecraftforge.event.entity.player.PlayerInteractEvent"); break; }
							case 65: { c = Class.forName("net.minecraftforge.event.entity.player.PlayerPickupXpEvent"); break; }
							case 66: { c = Class.forName("net.minecraftforge.event.entity.player.PlayerSetSpawnEvent"); break; }
							case 67: { c = Class.forName("net.minecraftforge.event.entity.player.PlayerSleepInBedEvent"); break; }
							case 68: { c = Class.forName("net.minecraftforge.event.entity.player.PlayerWakeUpEvent"); break; }
							case 69: { c = Class.forName("net.minecraftforge.event.entity.player.SleepingLocationCheckEvent"); break; }
							case 70: { c = Class.forName("net.minecraftforge.event.entity.player.SleepingTimeCheckEvent"); break; }
							case 71: { c = Class.forName("net.minecraftforge.event.entity.player.UseHoeEvent"); break; }
							case 72: { c = Class.forName("net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent"); break; }
							case 73: { c = Class.forName("net.minecraftforge.event.world.BlockEvent"); break; }
							case 74: { c = Class.forName("net.minecraftforge.event.world.ChunkDataEvent"); break; }
							case 75: { c = Class.forName("net.minecraftforge.event.world.ChunkEvent"); break; }
							case 76: { c = Class.forName("net.minecraftforge.event.world.ChunkWatchEvent"); break; }
							case 77: { c = Class.forName("net.minecraftforge.event.world.ExplosionEvent"); break; }
							case 78: { c = Class.forName("net.minecraftforge.event.world.NoteBlockEvent"); break; }
							case 79: { c = Class.forName("net.minecraftforge.event.world.WorldEvent"); break; }
							case 80: { c = Class.forName("net.minecraftforge.event.entity.EntityEvent"); break; }
							case 81: { c = Class.forName("net.minecraftforge.fml.common.gameevent.InputEvent"); break; }
							case 82: { c = Class.forName("net.minecraftforge.fml.common.gameevent.PlayerEvent"); break; }
							case 83: { c = Class.forName("net.minecraftforge.fml.common.gameevent.TickEvent"); break; }
							case 84: { c = Class.forName("net.minecraftforge.fluids.FluidEvent"); break; }
						}
					} catch (ClassNotFoundException e) { continue; }
					if (c != null) {
						listCalsses.add(c);
					}
				}
			}
			for (ClassPath.ClassInfo info : list) {
				String name = info.getName();
				if (name.startsWith("net.minecraftforge.event.terraingen")) { continue; }
				try { listCalsses.add(info.load()); } catch (Throwable t) { }
			}
			// Not Assing List
			List<Class<?>> notAssingException = new ArrayList<Class<?>>();
			notAssingException.add(GenericEvent.class);
			notAssingException.add(ItemTooltipEvent.class);
			notAssingException.add(GetCollisionBoxesEvent.class);
			notAssingException.add(EntityEvent.EntityConstructing.class);
			notAssingException.add(WorldEvent.PotentialSpawns.class);
			notAssingException.add(TickEvent.RenderTickEvent.class);
			notAssingException.add(TickEvent.ClientTickEvent.class);
			notAssingException.add(FMLNetworkEvent.ClientCustomPacketEvent.class);
			// Set the main method of the mod for each event
			for (Class<?> infoClass : listCalsses) {
				try {
					List<Class<?>> classes = new ArrayList<Class<?>>(Arrays.asList(infoClass.getDeclaredClasses()));
					if (classes.isEmpty()) { classes.add(infoClass); }

					// Registering events from classes
					for (Class<?> c : classes) {
						// Cheak
						boolean canAdd = true;
						for (Class<?> nae : notAssingException) {
							if (nae.isAssignableFrom(c)) {
								canAdd = false;
								break;
							}
						}
						if (!canAdd || !Event.class.isAssignableFrom(c) || Modifier.isAbstract(c.getModifiers())
								|| !Modifier.isPublic(c.getModifiers()) || CustomNpcs.forgeEventNames.containsKey(c)) {
							continue;
						}
						// Put Name
						String eventName = c.getName();
						int i = eventName.lastIndexOf(".");
						eventName = StringUtils.uncapitalize(eventName.substring(i + 1).replace("$", ""));
						if (CustomNpcs.forgeEventNames.containsValue(eventName)) { continue; }
						// Add
						register.invoke(MinecraftForge.EVENT_BUS, c, handler, m, Loader.instance().activeModContainer());
						CustomNpcs.forgeEventNames.put(c, eventName);
						LogWriter.debug("Add Forge Event["+CustomNpcs.forgeEventNames.size()+"]; "+c.getName());
					}
				} catch (Throwable t) {
					System.out.println("CustomNpcs Error Register Forge Event: " + t);
				}
			}
			if (PixelmonHelper.Enabled) {
				try {
					Field f = ClassLoader.class.getDeclaredField("classes");
					f.setAccessible(true);
					ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
					@SuppressWarnings("unchecked")
					List<Class<?>> classes2 = new ArrayList<Class<?>>(
							(Collection<? extends Class<?>>) f.get(classLoader));
					for (Class<?> c2 : classes2) {
						if (c2.getName().startsWith("com.pixelmonmod.pixelmon.api.events")
								&& Event.class.isAssignableFrom(c2) && !Modifier.isAbstract(c2.getModifiers())
								&& Modifier.isPublic(c2.getModifiers())) {
							if (CustomNpcs.forgeEventNames.containsKey(c2)) { continue; }
							// Put Name
							String eventName = c2.getName();
							int i = eventName.lastIndexOf(".");
							eventName = StringUtils.uncapitalize(eventName.substring(i + 1).replace("$", ""));
							if (CustomNpcs.forgeEventNames.containsValue(eventName)) { continue; }
							// Add
							register.invoke(PixelmonHelper.EVENT_BUS, c2, handler, m, Loader.instance().activeModContainer());
							CustomNpcs.forgeEventNames.put(c2, eventName);
							LogWriter.debug("Add Pixelmon Event["+CustomNpcs.forgeEventNames.size()+"]; "+c2.getName());
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		} catch (Exception e2) {
			e2.printStackTrace();
		}
		LogWriter.info("CustomNpcs: Registered [" + CustomNpcs.forgeEventNames.size() + "] Forge Events out of [" + listCalsses.size() + "] classes");
		return this;
	}

}
