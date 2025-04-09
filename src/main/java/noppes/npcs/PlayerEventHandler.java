package noppes.npcs;

import java.lang.reflect.*;
import java.util.*;

import javax.annotation.Nonnull;

import net.minecraft.block.BlockBanner;
import net.minecraft.tileentity.TileEntityBanner;
import noppes.npcs.api.item.INPCToolItem;
import noppes.npcs.api.mixin.entity.IEntityLivingBaseMixin;
import noppes.npcs.api.mixin.entity.player.IEntityPlayerMixin;
import noppes.npcs.controllers.*;
import noppes.npcs.controllers.data.*;
import noppes.npcs.api.mixin.tileentity.ITileEntityBanner;
import noppes.npcs.entity.data.DataInventory;
import noppes.npcs.reflection.event.entity.living.LivingAttackEventReflection;
import noppes.npcs.util.CustomNPCsScheduler;
import org.apache.commons.lang3.StringUtils;

import com.google.common.reflect.ClassPath;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemBanner;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.SPacketScoreboardObjective;
import net.minecraft.network.play.server.SPacketUpdateScore;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
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
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.event.world.BlockEvent.EntityPlaceEvent;
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
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.event.ForgeEvent;
import noppes.npcs.api.event.ItemEvent;
import noppes.npcs.api.event.PlayerEvent;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.api.handler.data.IWorldInfo;
import noppes.npcs.api.item.ISpecBuilder;
import noppes.npcs.api.wrapper.BlockWrapper;
import noppes.npcs.api.wrapper.ItemScriptedWrapper;
import noppes.npcs.client.ClientEventHandler;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.dimensions.CustomWorldInfo;
import noppes.npcs.dimensions.DimensionHandler;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.Resistances;
import noppes.npcs.items.ItemBoundary;
import noppes.npcs.items.ItemNbtBook;
import noppes.npcs.items.ItemScripted;
import noppes.npcs.quests.QuestObjective;
import noppes.npcs.util.Util;

public class PlayerEventHandler {

	/**
	 * Using quotes in JavaScripts for any Forge event requires binding to these events;
	 * The method written by the Noppes team is not applicable for all cases (sometimes events simply did not work in JS);
	 * And there is also a conflict with mods written with the "Cleanroom" base;
	 * A manual method for implementing classes is written:
	 */
	private static final String[] pathsToForgeEventClasses = new String[] {
			"net.minecraftforge.event.AnvilUpdateEvent",
			"net.minecraftforge.event.AttachCapabilitiesEvent",
			"net.minecraftforge.event.CommandEvent",
			"net.minecraftforge.event.DifficultyChangeEvent",
			"net.minecraftforge.event.GameRuleChangeEvent",
			"net.minecraftforge.event.LootTableLoadEvent",
			"net.minecraftforge.event.RegistryEvent",
			"net.minecraftforge.event.ServerChatEvent",
			"net.minecraftforge.event.brewing.PlayerBrewedPotionEvent",
			"net.minecraftforge.event.brewing.PotionBrewEvent",
			"net.minecraftforge.event.enchanting.EnchantmentLevelSetEvent",
			"net.minecraftforge.event.entity.EntityJoinWorldEvent",
			"net.minecraftforge.event.entity.EntityMobGriefingEvent",
			"net.minecraftforge.event.entity.EntityMountEvent",
			"net.minecraftforge.event.entity.EntityStruckByLightningEvent",
			"net.minecraftforge.event.entity.EntityTravelToDimensionEvent",
			"net.minecraftforge.event.entity.PlaySoundAtEntityEvent",
			"net.minecraftforge.event.entity.ProjectileImpactEvent",
			"net.minecraftforge.event.entity.ThrowableImpactEvent",
			"net.minecraftforge.event.entity.item.ItemEvent",
			"net.minecraftforge.event.entity.item.ItemExpireEvent",
			"net.minecraftforge.event.entity.item.ItemTossEvent",
			"net.minecraftforge.event.entity.living.AnimalTameEvent",
			"net.minecraftforge.event.entity.living.BabyEntitySpawnEvent",
			"net.minecraftforge.event.entity.living.EnderTeleportEvent",
			"net.minecraftforge.event.entity.living.LivingAttackEvent",
			"net.minecraftforge.event.entity.living.LivingDamageEvent",
			"net.minecraftforge.event.entity.living.LivingDeathEvent",
			"net.minecraftforge.event.entity.living.LivingDestroyBlockEvent",
			"net.minecraftforge.event.entity.living.LivingDropsEvent",
			"net.minecraftforge.event.entity.living.LivingEntityUseItemEvent",
			"net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent",
			"net.minecraftforge.event.entity.living.LivingEvent",
			"net.minecraftforge.event.entity.living.LivingExperienceDropEvent",
			"net.minecraftforge.event.entity.living.LivingFallEvent",
			"net.minecraftforge.event.entity.living.LivingHealEvent",
			"net.minecraftforge.event.entity.living.LivingHurtEvent",
			"net.minecraftforge.event.entity.living.LivingKnockBackEvent",
			"net.minecraftforge.event.entity.living.LivingPackSizeEvent",
			"net.minecraftforge.event.entity.living.LivingSetAttackTargetEvent",
			"net.minecraftforge.event.entity.living.LivingSpawnEvent",
			"net.minecraftforge.event.entity.living.LootingLevelEvent",
			"net.minecraftforge.event.entity.living.PotionColorCalculationEvent",
			"net.minecraftforge.event.entity.living.ZombieEvent",
			"net.minecraftforge.event.entity.minecart.MinecartCollisionEvent",
			"net.minecraftforge.event.entity.minecart.MinecartEvent",
			"net.minecraftforge.event.entity.minecart.MinecartInteractEvent",
			"net.minecraftforge.event.entity.minecart.MinecartUpdateEvent",
			"net.minecraftforge.event.entity.player.AdvancementEvent",
			"net.minecraftforge.event.entity.player.AnvilRepairEvent",
			"net.minecraftforge.event.entity.player.ArrowLooseEvent",
			"net.minecraftforge.event.entity.player.ArrowNockEvent",
			"net.minecraftforge.event.entity.player.AttackEntityEvent",
			"net.minecraftforge.event.entity.player.BonemealEvent",
			"net.minecraftforge.event.entity.player.CriticalHitEvent",
			"net.minecraftforge.event.entity.player.EntityItemPickupEvent",
			"net.minecraftforge.event.entity.player.FillBucketEvent",
			"net.minecraftforge.event.entity.player.ItemFishedEvent",
			"net.minecraftforge.event.entity.player.PlayerContainerEvent",
			"net.minecraftforge.event.entity.player.PlayerDestroyItemEvent",
			"net.minecraftforge.event.entity.player.PlayerDropsEvent",
			"net.minecraftforge.event.entity.player.PlayerEvent",
			"net.minecraftforge.event.entity.player.PlayerFlyableFallEvent",
			"net.minecraftforge.event.entity.player.PlayerInteractEvent",
			"net.minecraftforge.event.entity.player.PlayerPickupXpEvent",
			"net.minecraftforge.event.entity.player.PlayerSetSpawnEvent",
			"net.minecraftforge.event.entity.player.PlayerSleepInBedEvent",
			"net.minecraftforge.event.entity.player.PlayerWakeUpEvent",
			"net.minecraftforge.event.entity.player.SleepingLocationCheckEvent",
			"net.minecraftforge.event.entity.player.SleepingTimeCheckEvent",
			"net.minecraftforge.event.entity.player.UseHoeEvent",
			"net.minecraftforge.event.furnace.FurnaceFuelBurnTimeEvent",
			"net.minecraftforge.event.world.BlockEvent",
			"net.minecraftforge.event.world.ChunkDataEvent",
			"net.minecraftforge.event.world.ChunkEvent",
			"net.minecraftforge.event.world.ChunkWatchEvent",
			"net.minecraftforge.event.world.ExplosionEvent",
			"net.minecraftforge.event.world.NoteBlockEvent",
			"net.minecraftforge.event.world.WorldEvent",
			"net.minecraftforge.event.entity.EntityEvent",
			"net.minecraftforge.fml.common.gameevent.InputEvent",
			"net.minecraftforge.fml.common.gameevent.PlayerEvent",
			"net.minecraftforge.fml.common.gameevent.TickEvent",
			"net.minecraftforge.fluids.FluidEvent",
			"net.minecraftforge.client.event.sound.PlaySoundEvent",
			"net.minecraftforge.client.event.sound.PlaySoundSourceEvent",
			"net.minecraftforge.client.event.sound.PlayStreamingSourceEvent",
			"net.minecraftforge.client.event.sound.SoundEvent",
			"net.minecraftforge.client.event.sound.SoundLoadEvent",
			"net.minecraftforge.client.event.sound.SoundSetupEvent",
			"net.minecraftforge.client.event.ClientChatEvent",
			"net.minecraftforge.client.event.ClientChatReceivedEvent",
			"net.minecraftforge.client.event.ColorHandlerEvent",
			"net.minecraftforge.client.event.DrawBlockHighlightEvent",
			"net.minecraftforge.client.event.EntityViewRenderEvent",
			"net.minecraftforge.client.event.FOVUpdateEvent",
			"net.minecraftforge.client.event.GuiContainerEvent",
			"net.minecraftforge.client.event.GuiOpenEvent",
			"net.minecraftforge.client.event.GuiScreenEvent",
			"net.minecraftforge.client.event.InputUpdateEvent",
			"net.minecraftforge.client.event.ModelBakeEvent",
			"net.minecraftforge.client.event.MouseEvent",
			"net.minecraftforge.client.event.PlayerSPPushOutOfBlocksEvent",
			"net.minecraftforge.client.event.RenderBlockOverlayEvent",
			"net.minecraftforge.client.event.RenderGameOverlayEvent",
			"net.minecraftforge.client.event.RenderHandEvent",
			"net.minecraftforge.client.event.RenderItemInFrameEvent",
			"net.minecraftforge.client.event.RenderLivingEvent",
			"net.minecraftforge.client.event.RenderPlayerEvent",
			"net.minecraftforge.client.event.RenderSpecificHandEvent",
			"net.minecraftforge.client.event.RenderTooltipEvent",
			"net.minecraftforge.client.event.RenderWorldLastEvent",
			"net.minecraftforge.client.event.ScreenshotEvent",
			"net.minecraftforge.client.event.TextureStitchEvent"
	};

	public static class ForgeEventHandler {
		@SubscribeEvent
		public void forgeEntity(Event event) {
			EventHooks.onForgeEvent(new ForgeEvent(event));
		}
	}

	private void doCraftQuest(ItemCraftedEvent event) {
		EntityPlayer player = event.player;
		PlayerData pdata = PlayerData.get(player);
		PlayerQuestData playerdata = pdata.questData;
		for (QuestData data : playerdata.activeQuests.values()) {
			if (data.quest.step == 2 && data.quest.questInterface.isCompleted(player)) {
				continue;
			}
			boolean bo = data.quest.step == 1;
			for (IQuestObjective obj : data.quest.getObjectives((IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player))) {
				if (data.quest.step == 1 && !bo) {
					break;
				}
				bo = obj.isCompleted();
				if (((QuestObjective) obj).getEnumType() != EnumQuestTask.CRAFT) {
					continue;
				}
				int size = 0;
				if (!NoppesUtilServer.IsItemStackNull(event.crafting) && NoppesUtilPlayer.compareItems(
						obj.getItem().getMCItemStack(), event.crafting, obj.isIgnoreDamage(), obj.isItemIgnoreNBT())) {
					size = event.crafting.getCount();
				}
				if (size == 0) {
					continue;
				}
				HashMap<ItemStack, Integer> crafted = ((QuestObjective) obj).getCrafted(data);
				int amount = 0;
				ItemStack key = obj.getItem().getMCItemStack();
				for (ItemStack inData : crafted.keySet()) {
					if (NoppesUtilPlayer.compareItems(obj.getItem().getMCItemStack(), inData, obj.isIgnoreDamage(),
							obj.isItemIgnoreNBT())) {
						amount = crafted.get(inData);
						key = inData;
						break;
					}
				}
				if (amount >= obj.getMaxProgress()) {
					continue;
				}
				if (amount + size > obj.getMaxProgress()) {
					size = obj.getMaxProgress() - amount;
				}
				amount += size;
				crafted.put(key, amount);
				((QuestObjective) obj).setCrafted(data, crafted);
				if (data.quest.showProgressInWindow) {
					NBTTagCompound compound = new NBTTagCompound();
					compound.setInteger("QuestID", data.quest.id);
					compound.setString("Type", "craft");
					compound.setIntArray("Progress", new int[] { amount, obj.getMaxProgress() });
					compound.setTag("Item", event.crafting.writeToNBT(new NBTTagCompound()));
					compound.setInteger("MessageType", 0);
					Server.sendData((EntityPlayerMP) player, EnumPacketClient.MESSAGE_DATA, compound);
				}
				if (data.quest.showProgressInChat) {
					if (amount >= obj.getMaxProgress()) {
						player.sendMessage(new TextComponentTranslation("quest.message.craft.1",
								event.crafting.getDisplayName(), data.quest.getTitle()));
					} else {
						player.sendMessage(
								new TextComponentTranslation("quest.message.craft.0", event.crafting.getDisplayName(),
										"" + amount, "" + obj.getMaxProgress(), data.quest.getTitle()));
					}
				}

				pdata.updateClient = true;
				if (obj.isItemLeave()) {
					boolean ch = player.inventory.getItemStack().isItemEqual(event.crafting);
					event.crafting.splitStack(size);
					player.openContainer.detectAndSendChanges();
					if (ch) {
						NBTTagCompound nbtStack = new NBTTagCompound();
						player.inventory.getItemStack().writeToNBT(nbtStack);
						Server.sendData((EntityPlayerMP) player, EnumPacketClient.DETECT_HELD_ITEM, nbtStack);
					}
				}
				playerdata.checkQuestCompletion(player, data);
				playerdata.updateClient = true;
			}
		}
	}

    @SubscribeEvent
	public void npcArrowLooseEvent(ArrowLooseEvent event) {
		if (event.getEntityPlayer().world.isRemote || !(event.getWorld() instanceof WorldServer)) {
			return;
		}
		CustomNpcs.debugData.startDebug("Server", "Players", "PlayerEventHandler_npcArrowLooseEvent");
		PlayerScriptData handler = PlayerData.get(event.getEntityPlayer()).scriptData;
		PlayerEvent.RangedLaunchedEvent ev = new PlayerEvent.RangedLaunchedEvent(handler.getPlayer());
		event.setCanceled(EventHooks.onPlayerRanged(handler, ev));
		CustomNpcs.debugData.endDebug("Server", "Players", "PlayerEventHandler_npcArrowLooseEvent");
	}

	@SubscribeEvent
	public void npcBlockBreakEvent(BreakEvent event) {
		if (event.getPlayer().world.isRemote || !(event.getWorld() instanceof WorldServer)) {
			return;
		}
		CustomNpcs.debugData.startDebug("Server", "Players", "PlayerEventHandler_npcBlockBreakEvent");
		PlayerScriptData handler = PlayerData.get(event.getPlayer()).scriptData;
		PlayerEvent.BreakEvent ev = new PlayerEvent.BreakEvent(handler.getPlayer(),
				Objects.requireNonNull(NpcAPI.Instance()).getIBlock(event.getWorld(), event.getPos()), event.getExpToDrop());
		event.setCanceled(EventHooks.onPlayerBreak(handler, ev));
		event.setExpToDrop(ev.exp);
		CustomNpcs.debugData.endDebug("Server", "Players", "PlayerEventHandler_npcBlockBreakEvent");
	}

	@SubscribeEvent
	public void npcBlockPlaceEvent(EntityPlaceEvent event) {
		if (event.getWorld().isRemote || !(event.getWorld() instanceof WorldServer)
				|| !(event.getEntity() instanceof EntityPlayer)) {
			return;
		}
		CustomNpcs.debugData.startDebug("Server", "Players", "PlayerEventHandler_npcBlockPlaceEvent");
		EntityPlayer player = (EntityPlayer) event.getEntity();
		PlayerScriptData handler = PlayerData.get(player).scriptData;
		if (event.getPlacedBlock().getBlock() instanceof BlockBanner && player.getHeldItemMainhand().getItem() instanceof ItemBanner) {
			NBTTagCompound nbt = player.getHeldItemMainhand().getTagCompound();
			if (nbt != null && nbt.hasKey("BlockEntityTag", 10)
					&& nbt.getCompoundTag("BlockEntityTag").hasKey("FactionID", 3)) {
				TileEntity tile = event.getWorld().getTileEntity(event.getPos());
				if (tile instanceof TileEntityBanner) {
					((ITileEntityBanner) tile).npcs$setFactionId(nbt.getCompoundTag("BlockEntityTag").getInteger("FactionID"));
				}
			}
		}
		@SuppressWarnings("deprecation")
		IBlock block = BlockWrapper.createNew(event.getWorld(), event.getPos(), event.getPlacedBlock());
		PlayerEvent.PlaceEvent ev = new PlayerEvent.PlaceEvent(handler.getPlayer(), block);
		event.setCanceled(EventHooks.onPlayerPlace(handler, ev));
		if (event.isCanceled() && event.getEntity() instanceof EntityPlayerMP) {
			NBTTagCompound nbt = new NBTTagCompound();
			player.getHeldItemMainhand().writeToNBT(nbt);
			Server.sendData((EntityPlayerMP) player, EnumPacketClient.DETECT_HELD_ITEM, player.inventory.currentItem,
					nbt);
		}
		CustomNpcs.debugData.endDebug("Server", "Players", "PlayerEventHandler_npcBlockPlaceEvent");
	}

	@SubscribeEvent
	public void npcItemCraftedEvent(ItemCraftedEvent event) {
		if (event.player.world.isRemote) {
			return;
		}
		CustomNpcs.debugData.startDebug("Server", "Players", "PlayerEventHandler_npcItemCraftedEvent");
		EventHooks.onPlayerCrafted(PlayerData.get(event.player).scriptData, event.crafting, event.craftMatrix);
		event.player.world.getChunkFromChunkCoords(0, 0).onLoad();
		if (!event.crafting.isEmpty()) {
			this.doCraftQuest(event);
		}
		CustomNpcs.debugData.endDebug("Server", "Players", "PlayerEventHandler_npcItemCraftedEvent");
	}

	@SubscribeEvent
	public void npcItemFishedEvent(ItemFishedEvent event) {
		if (event.getEntityPlayer().world.isRemote) {
			return;
		}
		CustomNpcs.debugData.startDebug("Server", "Players", "PlayerEventHandler_npcItemFishedEvent");
		event.setCanceled(EventHooks.onPlayerFished(PlayerData.get(event.getEntityPlayer()).scriptData,
				event.getDrops(), event.getRodDamage()));
		CustomNpcs.debugData.endDebug("Server", "Players", "PlayerEventHandler_npcItemFishedEvent");
	}

	@SubscribeEvent
	public void npcItemPickupEvent(EntityItemPickupEvent event) {
		if (event.getEntityPlayer().world.isRemote) {
			return;
		}
		CustomNpcs.debugData.startDebug("Server", "Players", "PlayerEventHandler_npcItemPickupEvent");
		PlayerData pd = PlayerData.get(event.getEntityPlayer());
		for (QuestData qd : pd.questData.activeQuests.values()) {
			pd.questData.checkQuestCompletion(event.getEntityPlayer(), qd);
		}
		pd.questData.updateClient = true;
		event.setCanceled(EventHooks.onPlayerPickUp(pd.scriptData, event.getItem()));
		CustomNpcs.debugData.endDebug("Server", "Players", "PlayerEventHandler_npcItemPickupEvent");
	}

	@SubscribeEvent
	public void npcItemTossEvent(ItemTossEvent event) {
		if (event.getPlayer().world.isRemote) {
			return;
		}
		CustomNpcs.debugData.startDebug("Server", "Players", "PlayerEventHandler_npcItemTossEvent");
		PlayerData pd = PlayerData.get(event.getPlayer());
		for (QuestData qd : pd.questData.activeQuests.values()) {
			pd.questData.checkQuestCompletion(event.getPlayer(), qd);
		}
		pd.questData.updateClient = true;
		event.setCanceled(EventHooks.onPlayerToss(pd.scriptData, event.getEntityItem()));
		CustomNpcs.debugData.endDebug("Server", "Players", "PlayerEventHandler_npcItemTossEvent");
	}

	@SubscribeEvent
	public void npcLivingAttackEvent(LivingAttackEvent event) {
		if (event.getEntityLiving().world.isRemote) {
			return;
		}
		CustomNpcs.debugData.startDebug("Server", event.getEntityLiving(), "PlayerEventHandler_npcLivingAttackEvent");
		Entity source = NoppesUtilServer.GetDamageSource(event.getSource());
		String damageType = event.getSource() != null ? event.getSource().damageType : "null";
		Resistances.addDamageName(damageType);
		((IEntityLivingBaseMixin) event.getEntityLiving()).npcs$setCurrentDamageSource(event.getSource());
		if (source instanceof EntityPlayer) {
			PlayerData data = PlayerData.get((EntityPlayer) source);
			PlayerScriptData handler = data.scriptData;
			ItemStack item = ((EntityPlayer) source).getHeldItemMainhand();
			IEntity<?> target = Objects.requireNonNull(NpcAPI.Instance()).getIEntity(event.getEntityLiving());
			PlayerEvent.AttackEvent ev = new PlayerEvent.AttackEvent(handler.getPlayer(), 1, target);
			event.setCanceled(EventHooks.onPlayerAttack(handler, ev));
			if (event.isCanceled() || ev.isCanceled()) {
				LivingAttackEventReflection.setAmount(event, 0.0f);
			}
			if (item.getItem() == CustomRegisters.scripted_item && !event.isCanceled()) {
				ItemScriptedWrapper isw = ItemScripted.GetWrapper(item);
				ItemEvent.AttackEvent eve = new ItemEvent.AttackEvent(isw, handler.getPlayer(), 1, target);
				eve.setCanceled(event.isCanceled());
				event.setCanceled(EventHooks.onScriptItemAttack(isw, eve));
			}
			if (!event.isCanceled()) {
				for (EntityNPCInterface npc : data.game.getMercenaries()) {
					if (!npc.isAttacking()) {
						npc.setAttackTarget(event.getEntityLiving());
					}
				}
			}
		}
		if (event.getEntityLiving() instanceof EntityPlayer && source instanceof EntityLivingBase && !event.isCanceled()) {
			PlayerData data = PlayerData.get((EntityPlayer) event.getEntityLiving());
			for (EntityNPCInterface npc : data.game.getMercenaries()) {
				if (!npc.isAttacking()) {
					npc.setAttackTarget((EntityLivingBase) source);
				}
			}
		}
		CustomNpcs.debugData.endDebug("Server", event.getEntityLiving(), "PlayerEventHandler_npcLivingAttackEvent");
	}

	@SubscribeEvent
	public void npcLivingDeathEvent(LivingDeathEvent event) {
		if (event.getEntityLiving().world.isRemote) {
			return;
		}
		CustomNpcs.debugData.startDebug("Server", event.getEntityLiving(), "PlayerEventHandler_npcLivingDeathEvent");
		Entity source = NoppesUtilServer.GetDamageSource(event.getSource());
		if (event.getEntityLiving() instanceof EntityPlayer) {
			PlayerScriptData handler = PlayerData.get((EntityPlayer) event.getEntityLiving()).scriptData;
			EventHooks.onPlayerDeath(handler, event.getSource(), source);
		}
		if (source instanceof EntityPlayer) {
			PlayerScriptData handler = PlayerData.get((EntityPlayer) source).scriptData;
			EventHooks.onPlayerKills(handler, event.getEntityLiving());
		}
		CustomNpcs.debugData.endDebug("Server", event.getEntityLiving(), "PlayerEventHandler_npcLivingDeathEvent");
	}

	@SubscribeEvent
	public void npcLivingHurtEvent(LivingHurtEvent event) {
		if (event.getEntityLiving().world.isRemote) {
			return;
		}
		CustomNpcs.debugData.startDebug("Server", event.getEntityLiving(), "PlayerEventHandler_npcLivingHurtEvent");
		Entity source = NoppesUtilServer.GetDamageSource(event.getSource());
		if (event.getEntityLiving() instanceof EntityPlayer) {
			PlayerScriptData handler = PlayerData.get((EntityPlayer) event.getEntityLiving()).scriptData;
			PlayerEvent.DamagedEvent pevent = new PlayerEvent.DamagedEvent(handler.getPlayer(), source, event.getAmount(), event.getSource());
			boolean cancel = EventHooks.onPlayerDamaged(handler, pevent);
			event.setCanceled(cancel);
			if (pevent.clearTarget) {
				event.setCanceled(true);
				event.setAmount(0.0f);
			} else {
				event.setAmount(pevent.damage);
			}
		}
		if (source instanceof EntityPlayer) {
			PlayerScriptData handler = PlayerData.get((EntityPlayer) source).scriptData;
			PlayerEvent.DamagedEntityEvent pevent2 = new PlayerEvent.DamagedEntityEvent(handler.getPlayer(),
					event.getEntityLiving(), event.getAmount(), event.getSource());
			event.setCanceled(EventHooks.onPlayerDamagedEntity(handler, pevent2));
			event.setAmount(pevent2.damage);
		}
		CustomNpcs.debugData.endDebug("Server", event.getEntityLiving(), "PlayerEventHandler_npcLivingHurtEvent");
	}

	@SubscribeEvent
	public void npcPlayerContainerCloseEvent(PlayerContainerEvent.Close event) {
		if (event.getEntityPlayer().world.isRemote) {
			return;
		}
		CustomNpcs.debugData.startDebug("Server", "Players", "PlayerEventHandler_npcPlayerContainerCloseEvent");
		PlayerScriptData handler = PlayerData.get(event.getEntityPlayer()).scriptData;
		EventHooks.onPlayerContainerClose(handler, event.getContainer());
		CustomNpcs.debugData.endDebug("Server", "Players", "PlayerEventHandler_npcPlayerContainerCloseEvent");
	}

	@SubscribeEvent
	public void npcPlayerContainerOpenEvent(PlayerContainerEvent.Open event) {
		if (event.getEntityPlayer().world.isRemote) {
			return;
		}
		CustomNpcs.debugData.startDebug("Server", "Players", "PlayerEventHandler_npcPlayerContainerOpenEvent");
		PlayerScriptData handler = PlayerData.get(event.getEntityPlayer()).scriptData;
		EventHooks.onPlayerContainerOpen(handler, event.getContainer());
		CustomNpcs.debugData.endDebug("Server", "Players", "PlayerEventHandler_npcPlayerContainerOpenEvent");
	}
	
	@SubscribeEvent
	public void npcPlayerEntityInteractEvent(PlayerInteractEvent.EntityInteract event) {
		if (event.getEntityPlayer().world.isRemote || event.getHand() != EnumHand.MAIN_HAND || event.getWorld().isRemote) {
			if (event.getHand() == EnumHand.MAIN_HAND &&
					event.getItemStack().getItem() == CustomRegisters.nbt_book &&
					event.getTarget() != null &&
                    !event.getTarget().getClass().getName().contains("minecraft") &&
                    !event.getTarget().getClass().getName().contains("noppes")) {
				ClientEventHandler.entityClientEvent(event);
			}
			return;
		}
		CustomNpcs.debugData.startDebug("Server", "Players", "PlayerEventHandler_npcPlayerEntityInteractEvent");
		if (event.getItemStack().getItem() == CustomRegisters.nbt_book) {
			((ItemNbtBook) event.getItemStack().getItem()).entityEvent((EntityPlayerMP) event.getEntityPlayer(), event.getTarget());
			event.setCanceled(true);
			return;
		}
		PlayerScriptData handler = PlayerData.get(event.getEntityPlayer()).scriptData;
		PlayerEvent.InteractEvent ev = new PlayerEvent.InteractEvent(handler.getPlayer(), 1, Objects.requireNonNull(NpcAPI.Instance()).getIEntity(event.getTarget()));
		event.setCanceled(EventHooks.onPlayerInteract(handler, ev));
		if (event.getItemStack().getItem() == CustomRegisters.scripted_item && !event.isCanceled()) {
			ItemScriptedWrapper isw = ItemScripted.GetWrapper(event.getItemStack());
			ItemEvent.InteractEvent eve = new ItemEvent.InteractEvent(isw, handler.getPlayer(), 1, Objects.requireNonNull(NpcAPI.Instance()).getIEntity(event.getTarget()));
			event.setCanceled(EventHooks.onScriptItemInteract(isw, eve));
		}
		CustomNpcs.debugData.endDebug("Server", "Players", "PlayerEventHandler_npcPlayerEntityInteractEvent");
	}

	@SubscribeEvent
	public void npcPlayerLeftClickBlockEvent(PlayerInteractEvent.LeftClickBlock event) {
		if (event.getHand() != EnumHand.MAIN_HAND || event.getEntityPlayer().world.isRemote || event.getWorld().isRemote) {
			return;
		}
		CustomNpcs.debugData.startDebug("Server", "Players", "PlayerEventHandler_npcPlayerLeftClickBlockEvent");
		if (event.getItemStack().getItem() == CustomRegisters.npcboundary) {
			((ItemBoundary) event.getItemStack().getItem()).leftClick(event.getItemStack(), (EntityPlayerMP) event.getEntityPlayer());
			event.setCanceled(true);
			CustomNpcs.debugData.endDebug("Server", "Players", "PlayerEventHandler_npcPlayerLeftClickBlockEvent");
			return;
		}
		if (event.getItemStack().getItem() instanceof ISpecBuilder) {
			((ISpecBuilder) event.getItemStack().getItem()).leftClick(event.getItemStack(), (EntityPlayerMP) event.getEntityPlayer(), event.getPos());
			event.setCanceled(true);
			CustomNpcs.debugData.endDebug("Server", "Players", "PlayerEventHandler_npcPlayerLeftClickBlockEvent");
			return;
		}
		PlayerScriptData handler = PlayerData.get(event.getEntityPlayer()).scriptData;
		PlayerEvent.AttackEvent ev = new PlayerEvent.AttackEvent(handler.getPlayer(), 2, Objects.requireNonNull(NpcAPI.Instance()).getIBlock(event.getWorld(), event.getPos()));
		event.setCanceled(EventHooks.onPlayerAttack(handler, ev));
		if (event.getItemStack().getItem() == CustomRegisters.scripted_item && !event.isCanceled()) {
			ItemScriptedWrapper isw = ItemScripted.GetWrapper(event.getItemStack());
			ItemEvent.AttackEvent eve = new ItemEvent.AttackEvent(isw, handler.getPlayer(), 2, Objects.requireNonNull(NpcAPI.Instance()).getIBlock(event.getWorld(), event.getPos()));
			eve.setCanceled(event.isCanceled());
			event.setCanceled(EventHooks.onScriptItemAttack(isw, eve));
		}
		CustomNpcs.debugData.endDebug("Server", "Players", "PlayerEventHandler_npcPlayerLeftClickBlockEvent");
	}

	@SubscribeEvent
	public void npcPlayerLoginEvent(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedInEvent event) {
		NoppesUtilServer.sendScriptErrorsTo(event.player);
		if (event.player.world.isRemote) { return; }
		if (!ScriptController.Instance.getErrored().isEmpty()) {
			CustomNPCsScheduler.runTack(() -> event.player.sendMessage(new TextComponentTranslation("command.script.logs.view")), 2500);
		}
		CustomNpcs.debugData.startDebug("Server", "Players", "PlayerEventHandler_npcPlayerLoginEvent");
		PlayerData data = PlayerData.get(event.player);
		EventHooks.onPlayerLogin(data.scriptData);
		EntityPlayerMP player = (EntityPlayerMP) event.player;
		PlayerSkinController.getInstance().logged(player);
		MinecraftServer server = event.player.getServer();
        assert server != null;
        for (WorldServer world : server.worlds) {
			ServerScoreboard board = (ServerScoreboard) world.getScoreboard();
			for (String objective : Availability.scores) {
				ScoreObjective so = board.getObjective(objective);
				if (so != null) {
					if (board.getObjectiveDisplaySlotCount(so) == 0) {
						player.connection.sendPacket(new SPacketScoreboardObjective(so, 0));
					}
					Score sco = board.getOrCreateScore(player.getName(), so);
					player.connection.sendPacket(new SPacketUpdateScore(sco));
				}
			}
		}
		event.player.inventoryContainer.addListener(new IContainerListener() {
			public void sendAllContents(@Nonnull Container containerToSend, @Nonnull NonNullList<ItemStack> itemsList) {}
			public void sendAllWindowProperties(@Nonnull Container containerIn, @Nonnull IInventory inventory) {}
			public void sendSlotContents(@Nonnull Container containerToSend, int slotInd, @Nonnull ItemStack stack) {
				if (player.world.isRemote) { return; }
				for (QuestData qd : data.questData.activeQuests.values()) {
					for (IQuestObjective obj : qd.quest.getObjectives((IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player))) {
						if (obj.getType() != 0) {
							continue;
						}
						data.questData.checkQuestCompletion(player, qd);
					}
				}
			}
			public void sendWindowProperty(@Nonnull Container containerIn, int varToUpdate, int newValue) {}
		});
		Object array = DimensionHandler.getInstance().getAllIDs();
		Server.sendData(player, EnumPacketClient.DIMENSION_IDS, array);
		RecipeController.getInstance().checkRecipeBook((EntityPlayerMP) event.player);
		SyncController.syncPlayer((EntityPlayerMP) event.player);
		if (data.game.logPos != null) { // protection against remote measurements
			NoppesUtilPlayer.teleportPlayer((EntityPlayerMP) event.player, data.game.logPos[0], data.game.logPos[1],
					data.game.logPos[2], (int) data.game.logPos[3], event.player.rotationYaw,
					event.player.rotationPitch);
		}
		data.game.dimID = player.world.provider.getDimension();
		CustomNpcs.debugData.endDebug("Server", "Players", "PlayerEventHandler_npcPlayerLoginEvent");
	}

	@SubscribeEvent
	public void npcPlayerLogoutEvent(net.minecraftforge.fml.common.gameevent.PlayerEvent.PlayerLoggedOutEvent event) {
		if (event.player.world.isRemote) {
			CustomNpcs.debugData.startDebug("Client", "Players", "PlayerEventHandler_npcPlayerLogoutEvent");
			KeyController.getInstance().save();
			CustomNpcs.debugData.endDebug("Client", "Players", "PlayerEventHandler_npcPlayerLogoutEvent");
			return;
		}
		CustomNpcs.debugData.startDebug("Server", "Players", "PlayerEventHandler_npcPlayerLogoutEvent");
		PlayerData data = PlayerData.get(event.player);
		EventHooks.onPlayerLogout(data.scriptData);
		if (data.bankData.lastBank != null) {
			data.bankData.lastBank.save();
			data.bankData.lastBank = null;
		}
		IWorldInfo dim = DimensionHandler.getInstance().getMCWorldInfo(event.player.world.provider.getDimension());
		if (dim instanceof CustomWorldInfo) { // protection against remote measurements
			data.game.logPos = new double[] { event.player.posX, event.player.posY, event.player.posZ,
					event.player.world.provider.getDimension() };
			WorldServer world = Objects.requireNonNull(event.player.getServer()).getWorld(0);
			BlockPos coords = world.getSpawnCoordinate();
			if (coords == null) {
				coords = world.getSpawnPoint();
			}
            if (!world.isAirBlock(coords)) {
                coords = world.getTopSolidOrLiquidBlock(coords);
            } else if (!world.isAirBlock(coords.up())) {
                while (world.isAirBlock(coords) && coords.getY() > 0) {
                    coords = coords.down();
                }
                if (coords.getY() == 0) {
                    coords = world.getTopSolidOrLiquidBlock(coords);
                }
            }
			double x = coords.getX();
			double y = coords.getY();
			double z = coords.getZ();
            NoppesUtilPlayer.teleportPlayer((EntityPlayerMP) event.player, x, y, z, 0, event.player.rotationYaw,
					event.player.rotationPitch);
		} else {
			data.game.logPos = null;
		}
		data.save(false);
		CustomNpcs.debugData.endDebug("Server", "Players", "PlayerEventHandler_npcPlayerLogoutEvent");
	}

	@SubscribeEvent
	public void npcPlayerRightClickBlockEvent(PlayerInteractEvent.RightClickBlock event) {
		if (event.getHand() != EnumHand.MAIN_HAND || event.getWorld().isRemote) { return; }
		CustomNpcs.debugData.startDebug("Server", "Players", "PlayerEventHandler_npcPlayerRightClickBlockEvent");
		// NPC dead inventory
		if (!(event.getItemStack().getItem() instanceof INPCToolItem)) {
			Entity deadTarget = Util.instance.getLookEntity(event.getEntityPlayer(), 4.0d, false);
			if (deadTarget != null && !deadTarget.isEntityAlive() && deadTarget instanceof EntityNPCInterface) {
				DataInventory dataInv = ((EntityNPCInterface) deadTarget).inventory;
				IInventory deadInventory = dataInv.deadLoot;
				if (deadInventory == null && dataInv.deadLoots != null && dataInv.deadLoots.containsKey(event.getEntityPlayer())) { deadInventory = dataInv.deadLoots.get(event.getEntityPlayer()); }
				if (deadInventory != null) {
					NoppesUtilServer.sendOpenGui(event.getEntityPlayer(), EnumGuiType.DeadInventory, (EntityNPCInterface) deadTarget, deadInventory.getSizeInventory(), -1, 0);
					event.setCanceled(true);
					CustomNpcs.debugData.endDebug("Server", "Players", "PlayerEventHandler_npcPlayerRightClickBlockEvent");
					return;
				}
			}
		}
		if (event.getItemStack().getItem() == CustomRegisters.nbt_book) {
			Entity target = Util.instance.getLookEntity(event.getEntityPlayer(), PlayerData.get(event.getEntityPlayer()).game.renderDistance, false);
			if (target != null) { ((ItemNbtBook) event.getItemStack().getItem()).entityEvent((EntityPlayerMP) event.getEntityPlayer(), target); }
			else { ((ItemNbtBook) event.getItemStack().getItem()).blockEvent((EntityPlayerMP) event.getEntityPlayer(), event.getPos()); }
			event.setCanceled(true);
			CustomNpcs.debugData.endDebug("Server", "Players", "PlayerEventHandler_npcPlayerRightClickBlockEvent");
			return;
		}
		if (event.getItemStack().getItem() == CustomRegisters.npcboundary) {
			((ItemBoundary) event.getItemStack().getItem()).rightClick(event.getItemStack(), (EntityPlayerMP) event.getEntityPlayer());
			CustomNpcs.debugData.endDebug("Server", "Players", "PlayerEventHandler_npcPlayerRightClickBlockEvent");
			event.setCanceled(true);
			return;
		}
		if (event.getItemStack().getItem() instanceof ISpecBuilder) {
			((ISpecBuilder) event.getItemStack().getItem()).rightClick(event.getItemStack(), (EntityPlayerMP) event.getEntityPlayer(), event.getPos());
			CustomNpcs.debugData.endDebug("Server", "Players", "PlayerEventHandler_npcPlayerRightClickBlockEvent");
			event.setCanceled(true);
			return;
		}
		PlayerScriptData handler = PlayerData.get(event.getEntityPlayer()).scriptData;
		handler.hadInteract = true;
		PlayerEvent.InteractEvent ev = new PlayerEvent.InteractEvent(handler.getPlayer(), 2, Objects.requireNonNull(NpcAPI.Instance()).getIBlock(event.getWorld(), event.getPos()));
		event.setCanceled(EventHooks.onPlayerInteract(handler, ev));
		if (event.getItemStack().getItem() == CustomRegisters.scripted_item && !event.isCanceled()) {
			ItemScriptedWrapper isw = ItemScripted.GetWrapper(event.getItemStack());
			ItemEvent.InteractEvent eve = new ItemEvent.InteractEvent(isw, handler.getPlayer(), 2, Objects.requireNonNull(NpcAPI.Instance()).getIBlock(event.getWorld(), event.getPos()));
			event.setCanceled(EventHooks.onScriptItemInteract(isw, eve));
		}
		CustomNpcs.debugData.endDebug("Server", "Players", "PlayerEventHandler_npcPlayerRightClickBlockEvent");
	}

	@SubscribeEvent
	public void npcPlayerRightClickItemEvent(PlayerInteractEvent.RightClickItem event) {
		if (event.getHand() != EnumHand.MAIN_HAND || event.getEntityPlayer().world.isRemote || event.getWorld().isRemote) { return; }
		EntityPlayerMP player = (EntityPlayerMP) event.getEntityPlayer();
		CustomNpcs.debugData.startDebug("Server", player, "PlayerEventHandler_npcPlayerRightClickItemEvent");
		if (event.getEntityPlayer().isCreative() && event.getEntityPlayer().isSneaking() && event.getItemStack().getItem() == CustomRegisters.scripted_item) {
			NoppesUtilServer.sendOpenGui(event.getEntityPlayer(), EnumGuiType.ScriptItem, null);
			CustomNpcs.debugData.endDebug("Server", player, "PlayerEventHandler_npcPlayerRightClickItemEvent");
			return;
		}
		if (!(event.getItemStack().getItem() instanceof INPCToolItem)) {
			Entity deadTarget = Util.instance.getLookEntity(event.getEntityPlayer(), 4.0d, false);
			if (deadTarget != null && !deadTarget.isEntityAlive() && deadTarget instanceof EntityNPCInterface) {
				DataInventory dataInv = ((EntityNPCInterface) deadTarget).inventory;
				IInventory deadInventory = dataInv.deadLoot;
				if (deadInventory == null && dataInv.deadLoots != null && dataInv.deadLoots.containsKey(event.getEntityPlayer())) { deadInventory = dataInv.deadLoots.get(event.getEntityPlayer()); }
				if (deadInventory != null) {
					NoppesUtilServer.sendOpenGui(event.getEntityPlayer(), EnumGuiType.DeadInventory, (EntityNPCInterface) deadTarget, deadInventory.getSizeInventory(), -1, 0);
					event.setCanceled(true);
					CustomNpcs.debugData.endDebug("Server", player, "PlayerEventHandler_npcPlayerRightClickBlockEvent");
					return;
				}
			}
		}
		// Empty Click:
		if (event.getItemStack().getItem() instanceof ItemNbtBook) {
			PlayerData data = PlayerData.get(player);
			double d0 = data.game.renderDistance;
			Entity target = Util.instance.getLookEntity(player, d0, false);
			if (target != null) {
				((ItemNbtBook) event.getItemStack().getItem()).entityEvent(player, target);
				CustomNpcs.debugData.endDebug("Server", player, "PlayerEventHandler_npcPlayerRightClickItemEvent");
				return;
			}
			else if (!player.getHeldItemOffhand().isEmpty()) {
				((ItemNbtBook) event.getItemStack().getItem()).itemEvent(player);
				CustomNpcs.debugData.endDebug("Server", player, "PlayerEventHandler_npcPlayerRightClickItemEvent");
				return;
			}
			Vec3d vec3d = player.getPositionEyes(1.0f);
			Vec3d vec3d2 = player.getLook(1.0f);
			Vec3d vec3d3 = vec3d.addVector(vec3d2.x * d0, vec3d2.y * d0, vec3d2.z * d0);
			RayTraceResult result = player.world.rayTraceBlocks(vec3d, vec3d3, false, false, false);
			if (result != null && result.typeOfHit == RayTraceResult.Type.BLOCK) {
				((ItemNbtBook) event.getItemStack().getItem()).blockEvent(player, result.getBlockPos());
				CustomNpcs.debugData.endDebug("Server", player, "PlayerEventHandler_npcPlayerRightClickItemEvent");
				return;
			}
		}
		if (event.getItemStack().getItem() instanceof ItemBoundary) {
			((ItemBoundary) event.getItemStack().getItem()).rightClick(event.getItemStack(),
					(EntityPlayerMP) event.getEntityPlayer());
			CustomNpcs.debugData.endDebug("Server", player, "PlayerEventHandler_npcPlayerRightClickItemEvent");
			return;
		}
		if (event.getItemStack().getItem() instanceof ISpecBuilder) {
			((ISpecBuilder) event.getItemStack().getItem()).rightClick(event.getItemStack(), (EntityPlayerMP) event.getEntityPlayer(), event.getPos());
			CustomNpcs.debugData.endDebug("Server", player, "PlayerEventHandler_npcPlayerRightClickBlockEvent");
			event.setCanceled(true);
			return;
		}
		PlayerScriptData handler = PlayerData.get(event.getEntityPlayer()).scriptData;
		if (handler.hadInteract) {
			handler.hadInteract = false;
			CustomNpcs.debugData.endDebug("Server", player, "PlayerEventHandler_npcPlayerRightClickItemEvent");
			return;
		}
		PlayerEvent.InteractEvent ev = new PlayerEvent.InteractEvent(handler.getPlayer(), 0, null);
		event.setCanceled(EventHooks.onPlayerInteract(handler, ev));
		if (event.getItemStack().getItem() == CustomRegisters.scripted_item && !event.isCanceled()) {
			ItemScriptedWrapper isw = ItemScripted.GetWrapper(event.getItemStack());
			ItemEvent.InteractEvent eve = new ItemEvent.InteractEvent(isw, handler.getPlayer(), 0, null);
			event.setCanceled(EventHooks.onScriptItemInteract(isw, eve));
		}
		CustomNpcs.debugData.endDebug("Server", player, "PlayerEventHandler_npcPlayerRightClickItemEvent");
	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public void npcServerChatEvent(ServerChatEvent event) {
		if (event.getPlayer().world.isRemote || event.getPlayer() == EntityNPCInterface.ChatEventPlayer) {
			return;
		}
		CustomNpcs.debugData.startDebug("Server", "Players", "PlayerEventHandler_npcServerChatEvent");
		PlayerScriptData handler = PlayerData.get(event.getPlayer()).scriptData;
		String message = event.getMessage();
		PlayerEvent.ChatEvent ev = new PlayerEvent.ChatEvent(handler.getPlayer(), event.getMessage());
		EventHooks.onPlayerChat(handler, ev);
		event.setCanceled(ev.isCanceled());
		if (!message.equals(ev.message)) {
			TextComponentTranslation chat = new TextComponentTranslation("");
			chat.appendSibling(ForgeHooks.newChatWithLinks(ev.message));
			event.setComponent(chat);
		}
		Server.sendRangedData(event.getPlayer().world, event.getPlayer().getPosition(), 32, EnumPacketClient.CHAT_BUBBLE,
				event.getPlayer().getEntityId(), event.getMessage(), true);
		CustomNpcs.debugData.endDebug("Server", "Players", "PlayerEventHandler_npcServerChatEvent");
	}

	@SubscribeEvent
	public void npcServerTick(TickEvent.PlayerTickEvent event) {
		if (event.side != Side.SERVER || event.phase != TickEvent.Phase.START) {
			return;
		}
		CustomNpcs.debugData.startDebug("Server", "Players", "PlayerEventHandler_npcServerTick");
		EntityPlayer player = event.player;
		PlayerData data = PlayerData.get(player);
		if (player.ticksExisted % 10 == 0) {
			EventHooks.onPlayerTick(data.scriptData);
			for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
				ItemStack item = player.inventory.getStackInSlot(i);
				if (!item.isEmpty() && item.getItem() == CustomRegisters.scripted_item) {
					ItemScriptedWrapper isw = (ItemScriptedWrapper) Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(item);
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
		int dimId = event.player.world.provider.getDimension();
		if (data.game.dimID != dimId) {
			if (CustomNpcs.SetPlayerHomeWhenChangingDimension) {
				player.setSpawnDimension(dimId);
				player.setSpawnPoint(player.getPosition(), true);
				player.setSpawnChunk(player.getPosition(), true, dimId);
				player.bedLocation = player.getPosition();
				((IEntityPlayerMixin) player).npcs$setSpawnPos(player.getPosition());
			}
			data.game.dimID = event.player.world.provider.getDimension();
		}
		CustomNpcs.debugData.endDebug("Server", "Players", "PlayerEventHandler_npcServerTick");
	}

	public PlayerEventHandler registerForgeEvents(Side side) {
		ForgeEventHandler handler = new ForgeEventHandler();
		LogWriter.info("CustomNpcs: Start load Forge Events:");
		CustomNpcs.debugData.startDebug("Common", "Mod", "PlayerEventHandler_registerForgeEvents");
		ScriptController.forgeEventNames.clear();
		List<Class<?>> listClasses = new ArrayList<>();
		try {
			// Get Maim mod Method for All Events
			Method m = handler.getClass().getMethod("forgeEntity", Event.class);
			// Get Registration Method for Event Methods
			Method register = MinecraftForge.EVENT_BUS.getClass().getDeclaredMethod("register", Class.class, Object.class, Method.class, ModContainer.class);
			register.setAccessible(true);

			for (String forgeEventClassPath : pathsToForgeEventClasses) {
				Class<?> event;
				try { event = Class.forName(forgeEventClassPath); } catch (ClassNotFoundException e) { continue; }
				if (!listClasses.contains(event)) { listClasses.add(event); }
			}
			int eventSize = pathsToForgeEventClasses.length;
			LogWriter.debug("Manually found " + listClasses.size() + " / " + eventSize + " classes of Forge events");

			ClassPath loader = ClassPath.from(this.getClass().getClassLoader());
			// Get all loaded Forge event classes
			List<ClassPath.ClassInfo> list = new ArrayList<>(loader.getTopLevelClassesRecursive("net.minecraftforge.event"));
			list.addAll(loader.getTopLevelClassesRecursive("net.minecraftforge.fml.common"));
			if (eventSize < list.size()) { eventSize = list.size(); }

			for (ClassPath.ClassInfo info : list) {
				String forgeEventClassPath = info.getName();
				if (forgeEventClassPath.startsWith("net.minecraftforge.event.terraingen")) { continue; }
				try {
					Class<?> event = Class.forName(forgeEventClassPath);
					if (!listClasses.contains(event)) { listClasses.add(event); }
				} catch (Throwable ignored) { }
			}
			if (eventSize < listClasses.size()) { eventSize = listClasses.size(); }
			LogWriter.debug("Total of " + listClasses.size() + " / " + eventSize + " classes of Forge events");

			// Not Assing List
			List<Class<?>> notAssingException = new ArrayList<>();
			notAssingException.add(GenericEvent.class);
			notAssingException.add(EntityEvent.EntityConstructing.class);
			notAssingException.add(WorldEvent.PotentialSpawns.class);

			List<Class<?>> isClientEvents = new ArrayList<>();
			isClientEvents.add(ItemTooltipEvent.class);
			isClientEvents.add(GetCollisionBoxesEvent.class);
			isClientEvents.add(TickEvent.RenderTickEvent.class);
			isClientEvents.add(TickEvent.ClientTickEvent.class);
			isClientEvents.add(FMLNetworkEvent.ClientCustomPacketEvent.class);
			// Set the main method of the mod for each event
			boolean threadIsClient = Thread.currentThread().getName().toLowerCase().contains("client");
			for (Class<?> infoClass : listClasses) {
				boolean isClient = false;
				Class<?> debugClass = null;
				try {
					String pfx = "";
					List<Class<?>> classes = new ArrayList<>(Arrays.asList(infoClass.getDeclaredClasses()));
					if (classes.isEmpty()) {
						classes.add(infoClass);
					}

					// Registering events from classes
					for (Class<?> c : classes) {
						debugClass = c;
						// Check
						boolean canAdd = true;
						for (Class<?> nae : notAssingException) {
							if (nae.isAssignableFrom(c)) {
								canAdd = false;
								break;
							}
						}
						isClient = false;
						for (Class<?> nae : isClientEvents) {
							if (nae.isAssignableFrom(c)) {
								isClient = true;
								break;
							}
						}
						if ((side == Side.SERVER && isClient) || !canAdd || !Event.class.isAssignableFrom(c)
								|| Modifier.isAbstract(c.getModifiers()) || !Modifier.isPublic(c.getModifiers())
								|| ScriptController.forgeEventNames.containsKey(c)) {
							continue;
						}
						// Put Name
						String eventName = c.getName();
						if (!isClient) {
							isClient = eventName.toLowerCase().contains("client") || eventName.toLowerCase().contains("render");
						}
						int i = eventName.lastIndexOf(".");
						eventName = pfx + StringUtils.uncapitalize(eventName.substring(i + 1).replace("$", ""));
						if (ScriptController.forgeEventNames.containsValue(eventName)) { continue; }
						if (!isClient) {
							ScriptController.forgeEventNames.put(c, eventName);
							ScriptController.forgeClientEventNames.put(c, eventName);
							register.invoke(MinecraftForge.EVENT_BUS, c, handler, m, Loader.instance().activeModContainer());
						}
						else {
							ScriptController.forgeClientEventNames.put(c, eventName);
							if (threadIsClient) { register.invoke(MinecraftForge.EVENT_BUS, c, handler, m, Loader.instance().activeModContainer()); }
						}
						LogWriter.debug("Add Forge "+(isClient ? "client" : "common")+" Event " +c.getName());
					}
				} catch (Exception t) {
					LogWriter.error("[" + side + "] CustomNpcs Error Register Forge " + (isClient ? "client" : "server")
							+ " Event: " + infoClass.getSimpleName()
							+ (debugClass != null ? "; subClass: " + debugClass.getSimpleName() : ""), t);
				}
			}
			if (PixelmonHelper.Enabled) {
				try {
					Field f = ClassLoader.class.getDeclaredField("classes");
					f.setAccessible(true);
					ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
					@SuppressWarnings("unchecked")
					List<Class<?>> classes2 = new ArrayList<>((Collection<? extends Class<?>>) f.get(classLoader));
					for (Class<?> c2 : classes2) {
						if (c2.getName().startsWith("com.pixelmonmod.pixelmon.api.events")
								&& Event.class.isAssignableFrom(c2) && !Modifier.isAbstract(c2.getModifiers())
								&& Modifier.isPublic(c2.getModifiers())) {
							if (ScriptController.forgeEventNames.containsKey(c2)) { continue; }
							// Put Name
							String eventName = c2.getName();
							int i = eventName.lastIndexOf(".");
							eventName = StringUtils.uncapitalize(eventName.substring(i + 1).replace("$", ""));
							if (ScriptController.forgeEventNames.containsValue(eventName)) { continue; }
							// Add
							register.invoke(PixelmonHelper.EVENT_BUS, c2, handler, m, Loader.instance().activeModContainer());
							ScriptController.forgeEventNames.put(c2, eventName);
							LogWriter.debug("Add Pixelmon Event[" + ScriptController.forgeEventNames.size() + "]; " + c2.getName());
						}
					}
				} catch (Exception e) { LogWriter.error("Error:", e); }
			}
		} catch (Exception e) { LogWriter.error("Error:", e); }
		LogWriter.info("CustomNpcs: Registered [Client:" + ScriptController.forgeClientEventNames.size() + "; Server: " + ScriptController.forgeEventNames.size() + "] Forge Events out of [" + listClasses.size() + "] classes");
		CustomNpcs.debugData.endDebug("Common", "Mod", "PlayerEventHandler_registerForgeEvents");
		return this;
	}

}
