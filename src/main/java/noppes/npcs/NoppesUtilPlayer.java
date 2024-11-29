package noppes.npcs;

import java.util.*;

import io.netty.buffer.Unpooled;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;
import net.minecraftforge.oredict.OreDictionary;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.constants.OptionType;
import noppes.npcs.api.event.QuestEvent.QuestTurnedInEvent;
import noppes.npcs.api.event.RoleEvent;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerNPCBank;
import noppes.npcs.containers.ContainerNPCFollowerHire;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.PlayerQuestController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.BankData;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogOption;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.RoleDialog;
import noppes.npcs.roles.RoleFollower;
import noppes.npcs.util.Util;

public class NoppesUtilPlayer {

	private static final Map<Object, Long> delaySendMap = new HashMap<>();

    public static void bankClearCeil(EntityPlayerMP player, EntityNPCInterface npc) {
		if (!player.capabilities.isCreativeMode || !(player.openContainer instanceof ContainerNPCBank)) {
			return;
		}
		int ceilId = ((ContainerNPCBank) player.openContainer).ceil;
		BankData bd = ((ContainerNPCBank) player.openContainer).data;
		if (!bd.cells.containsKey(ceilId)) {
			return;
		}
		((ContainerNPCBank) player.openContainer).items.clear();
		player.openContainer.detectAndSendChanges();
		bd.save();
		NoppesUtilPlayer.openBankGui(bd, player, npc, ceilId);
	}

	public static void bankLock(EntityPlayerMP player, EntityNPCInterface npc) {
		if (!player.capabilities.isCreativeMode || !(player.openContainer instanceof ContainerNPCBank)) {
			return;
		}
		int ceilId = ((ContainerNPCBank) player.openContainer).ceil;
		BankData bd = ((ContainerNPCBank) player.openContainer).data;
		if (!bd.bank.ceilSettings.containsKey(ceilId) || bd.bank.ceilSettings.get(ceilId).openStack.isEmpty()) {
			return;
		}
		bd.cells.put(ceilId, new NpcMiscInventory(0));
		bd.save();
		NoppesUtilPlayer.openBankGui(bd, player, npc, ceilId);
	}

	public static void bankRegrade(EntityPlayerMP player, EntityNPCInterface npc) {
		if (!player.capabilities.isCreativeMode || !(player.openContainer instanceof ContainerNPCBank)) {
			return;
		}
		int ceilId = ((ContainerNPCBank) player.openContainer).ceil;
		BankData bd = ((ContainerNPCBank) player.openContainer).data;
		if (!bd.cells.containsKey(ceilId) || bd.cells.get(ceilId).getSizeInventory() < 0) {
			return;
		}
		if (bd.cells.get(ceilId).getSizeInventory() == 1) {
			NoppesUtilPlayer.bankLock(player, npc);
			return;
		}
        bd.cells.computeIfPresent(ceilId, (k, inv) -> new NpcMiscInventory(inv.getSizeInventory() - 1).fill(inv));
		bd.save();
		NoppesUtilPlayer.openBankGui(bd, player, npc, ceilId);
	}

	public static void bankResetCeil(EntityPlayerMP player, EntityNPCInterface npc) {
		if (!player.capabilities.isCreativeMode || !(player.openContainer instanceof ContainerNPCBank)) {
			return;
		}
		int ceilId = ((ContainerNPCBank) player.openContainer).ceil;
		BankData bd = ((ContainerNPCBank) player.openContainer).data;
		if (!bd.cells.containsKey(ceilId) || !bd.bank.ceilSettings.containsKey(ceilId)) {
			return;
		}
		((ContainerNPCBank) player.openContainer).items.clear();
		player.openContainer.detectAndSendChanges();
		bd.cells.put(ceilId, new NpcMiscInventory(bd.bank.ceilSettings.get(ceilId).startCells));
		bd.save();
		NoppesUtilPlayer.openBankGui(bd, player, npc, ceilId);
	}

	public static void bankUnlock(EntityPlayerMP player, EntityNPCInterface npc, boolean isStack) {
		if (!(player.openContainer instanceof ContainerNPCBank) || npc == null) {
			return;
		}
		int ceilId = ((ContainerNPCBank) player.openContainer).ceil;
		BankData bd = ((ContainerNPCBank) player.openContainer).data;

		boolean canOpen = player.capabilities.isCreativeMode || bd.bank.isPublic || player.getUniqueID().equals(bd.getUUID()) || bd.bank.owner.equals(player.getName());
		if (canOpen) {
			if (isStack) {
				canOpen = Util.instance.removeItem(player, bd.bank.ceilSettings.get(ceilId).openStack, false, false);
			}
			else {
				PlayerData data = PlayerData.get(player);
				canOpen = bd.bank.ceilSettings.get(ceilId).openMoney <= data.game.getMoney();
				if (canOpen) { data.game.addMoney(-bd.bank.ceilSettings.get(ceilId).openMoney); }
			}
		}
		if (canOpen) {
			bd.cells.put(ceilId, new NpcMiscInventory(bd.bank.ceilSettings.get(ceilId).startCells));
			bd.save();
			RoleEvent.BankUnlockedEvent event = new RoleEvent.BankUnlockedEvent(player, npc.wrappedNPC, ceilId);
			EventHooks.onNPCRole(npc, event);
		}
		NoppesUtilPlayer.openBankGui(bd, player, npc, ceilId);
	}

	public static void bankUpgrade(EntityPlayerMP player, EntityNPCInterface npc, boolean isStack, int count) {
		if (!(player.openContainer instanceof ContainerNPCBank) || npc == null) {
			return;
		}
		int ceilId = ((ContainerNPCBank) player.openContainer).ceil;
		BankData bd = ((ContainerNPCBank) player.openContainer).data;
		if (!bd.cells.containsKey(ceilId)) {
			return;
		}
		boolean canUpgrade = player.capabilities.isCreativeMode || bd.bank.isPublic || player.getUniqueID().equals(bd.getUUID()) || bd.bank.owner.equals(player.getName());
		if (canUpgrade) {
			if (isStack) {
				canUpgrade = Util.instance.removeItem(player, bd.bank.ceilSettings.get(ceilId).upgradeStack, count, false, false);
			}
			else {
				PlayerData data = PlayerData.get(player);
				int need = bd.bank.ceilSettings.get(ceilId).upgradeMoney * count;
				canUpgrade = need <= data.game.getMoney();
				if (canUpgrade) { data.game.addMoney(-1 * need); }
			}
		}
		if (canUpgrade) {
			bd.cells.computeIfPresent(ceilId, (k, inv) -> new NpcMiscInventory(inv.getSizeInventory() + count).fill(inv));
			bd.save();
			RoleEvent.BankUpgradedEvent event = new RoleEvent.BankUpgradedEvent(player, npc.wrappedNPC, ceilId);
			EventHooks.onNPCRole(npc, event);
			if (((ContainerNPCBank) player.openContainer).items.getSizeInventory() == 45) {
				bd.openBankGui(player, npc, ceilId);
			} else {
				NoppesUtilPlayer.openBankGui(bd, player, npc, ceilId);
			}
		} else {
			NoppesUtilPlayer.openBankGui(bd, player, npc, ceilId);
		}
	}

	public static void changeFollowerState(EntityPlayerMP player, EntityNPCInterface npc) {
		if (!(npc.advanced.roleInterface instanceof RoleFollower)) {
			return;
		}
		RoleFollower role = (RoleFollower) npc.advanced.roleInterface;
		EntityPlayer owner = role.owner;
		if (owner == null || !owner.getName().equals(player.getName())) {
			return;
		}
		role.isFollowing = !role.isFollowing;
	}

	public static void closeDialog(EntityPlayerMP player, EntityNPCInterface npc, boolean notifyClient) {
		PlayerData data = PlayerData.get(player);
		Dialog dialog = DialogController.instance.dialogs.get(data.dialogId);
		EventHooks.onNPCDialogClose(npc, player, dialog);
		if (notifyClient) {
			Server.sendData(player, EnumPacketClient.GUI_CLOSE, -1, new NBTTagCompound());
		}
		data.dialogId = -1;
	}

	private static boolean compareItemDetails(ItemStack item, ItemStack item2, boolean ignoreDamage, boolean ignoreNBT) {
		return item.getItem() == item2.getItem()
				&& (ignoreDamage || item.getItemDamage() == -1 || item.getItemDamage() == item2.getItemDamage())
				&& (ignoreNBT || item.getTagCompound() == null || (item2.getTagCompound() != null && item.getTagCompound().equals(item2.getTagCompound())))
				&& (ignoreNBT || item2.getTagCompound() == null || item.getTagCompound() != null);
	}

	public static boolean compareItems(EntityPlayer player, ItemStack item, boolean ignoreDamage, boolean ignoreNBT) {
		int size = 0;
		for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
			ItemStack is = player.inventory.getStackInSlot(i);
			if (!NoppesUtilServer.IsItemStackNull(is) && compareItems(item, is, ignoreDamage, ignoreNBT)) {
				size += is.getCount();
			}
		}
		return size >= item.getCount();
	}

	public static boolean compareItems(EntityPlayer player, ItemStack item, boolean ignoreDamage, boolean ignoreNBT,
			int amount) {
		int size = 0;
		if (player == null) {
			return false;
		}
		for (int i = 0; i < player.inventory.getSizeInventory(); ++i) {
			ItemStack is = player.inventory.getStackInSlot(i);
			if (!NoppesUtilServer.IsItemStackNull(is) && compareItems(item, is, ignoreDamage, ignoreNBT)) {
				size += is.getCount();
			}
		}
		return size >= amount;
	}

	public static boolean compareItems(ItemStack item, ItemStack item2, boolean ignoreDamage, boolean ignoreNBT) {
		if (NoppesUtilServer.IsItemStackNull(item) || NoppesUtilServer.IsItemStackNull(item2)) {
			return false;
		}
		OreDictionary.itemMatches(item, item2, false); // meta
		int[] ids = OreDictionary.getOreIDs(item);
        for (int id : ids) {
            boolean match1 = false;
            boolean match2 = false;
            for (ItemStack is : OreDictionary.getOres(OreDictionary.getOreName(id))) {
                if (compareItemDetails(item, is, ignoreDamage, ignoreNBT)) {
                    match1 = true;
                }
                if (compareItemDetails(item2, is, ignoreDamage, ignoreNBT)) {
                    match2 = true;
                }
            }
            if (match1 && match2) {
                return true;
            }
        }
        return compareItemDetails(item, item2, ignoreDamage, ignoreNBT);
	}

	public static void dialogSelected(int diaId, int optionId, EntityPlayerMP player, EntityNPCInterface npc) {
		PlayerData data = PlayerData.get(player);
		if (data.dialogId != diaId) {
			return;
		}
		if (data.dialogId < 0 && npc.advanced.roleInterface instanceof RoleDialog) {
			String text = ((RoleDialog) npc.advanced.roleInterface).optionsTexts.get(optionId);
			if (text != null && !text.isEmpty()) {
				Dialog d = new Dialog(null);
				d.text = text;
				NoppesUtilServer.openDialog(player, npc, d);
			}
			return;
		}
		Dialog dialog = DialogController.instance.dialogs.get(data.dialogId);
		if (dialog == null) {
			return;
		}
		if (!dialog.hasDialogs(player) && dialog.notHasOtherOptions()) {
			closeDialog(player, npc, true);
			return;
		}
		DialogOption option = dialog.options.get(optionId);
		if (option == null || EventHooks.onNPCDialogOption(npc, player, dialog, option)
				|| (option.optionType == OptionType.DIALOG_OPTION
						&& (!option.isAvailable(player) || !option.hasDialogs()))
				|| option.optionType == OptionType.DISABLED || option.optionType == OptionType.QUIT_OPTION) {
			closeDialog(player, npc, true);
			return;
		}
		if (option.optionType == OptionType.ROLE_OPTION) {
			closeDialog(player, npc, true);
			if (npc.advanced.roleInterface != null) {
				if (npc.advanced.roleInterface instanceof RoleCompanion) {
					((RoleCompanion) npc.advanced.roleInterface).interact(player, true);
				} else {
					npc.advanced.roleInterface.interact(player);
				}
			}
		} else if (option.optionType == OptionType.DIALOG_OPTION) {
			closeDialog(player, npc, false);
			NoppesUtilServer.openDialog(player, npc, option.getDialog(player));
		} else if (option.optionType == OptionType.COMMAND_BLOCK) {
			closeDialog(player, npc, true);
			NoppesUtilServer.runCommand(npc, npc.getName(), option.command, player);
		} else {
			closeDialog(player, npc, true);
		}
	}

	public static void extendFollower(EntityPlayerMP player, EntityNPCInterface npc, int pos) {
		if (!(npc.advanced.roleInterface instanceof RoleFollower)) {
			return;
		}
		Container con = player.openContainer;
		if (!(con instanceof ContainerNPCFollowerHire)) {
			return;
		}
		RoleFollower role = (RoleFollower) npc.advanced.roleInterface;
		followerBuy(role, pos, player, npc);
	}

	private static void followerBuy(RoleFollower role, int pos, EntityPlayerMP player, EntityNPCInterface npc) {
		if (pos < 0 || pos > 3 || !role.rates.containsKey(pos)) {
			return;
		}
		if (pos == 3) {
			if (!player.capabilities.isCreativeMode) {
				if (PlayerData.get(player).game.getMoney() < role.rentalMoney) {
					return;
				}
				PlayerData.get(player).game.addMoney(role.rentalMoney * -1);
			}
		} else {
			ItemStack currency = role.rentalItems.getStackInSlot(0);
			if (currency.isEmpty()) {
				return;
			}
			if (!player.capabilities.isCreativeMode) {
				Map<ItemStack, Integer> map = new HashMap<>();
				map.put(currency, currency.getCount());
				if (!Util.instance.canRemoveItems(role.rentalItems.items, map, false, false)) {
					return;
				}
				Util.instance.removeItem(player, currency, false, false);
			}
		}

		int days = role.rates.get(pos);
		RoleEvent.FollowerHireEvent event = new RoleEvent.FollowerHireEvent(player, npc.wrappedNPC, days);
		if (EventHooks.onNPCRole(npc, event)) {
			return;
		}
		if (event.days == 0) {
			return;
		}
		npc.say(player,
				new Line(NoppesStringUtils.formatText(role.dialogHire.replace("{days}", days + ""), player, npc)));
		role.setOwner(player);
		role.addDays(days);
	}

	public static void hireFollower(EntityPlayerMP player, EntityNPCInterface npc, int pos) {
		if (!(npc.advanced.roleInterface instanceof RoleFollower)) {
			return;
		}
		Container con = player.openContainer;
		if (!(con instanceof ContainerNPCFollowerHire)) {
			return;
		}
		RoleFollower role = (RoleFollower) npc.advanced.roleInterface;
		followerBuy(role, pos, player, npc);
	}

	public static void openBankGui(BankData bd, EntityPlayerMP player, EntityNPCInterface npc, int ceilId) {
		bd.openBankGui(player, npc, ceilId);
		if (CustomNpcs.Server != null) {
			if (bd.bank.isPublic) {
				for (EntityPlayerMP pl : CustomNpcs.Server.getPlayerList().getPlayers()) {
					if (!pl.equals(player) && pl.openContainer instanceof ContainerNPCBank
							&& ((ContainerNPCBank) pl.openContainer).bank.id == bd.bank.id
							&& ((ContainerNPCBank) pl.openContainer).ceil == ceilId) {
						if (!bd.bank.access.isEmpty() && !bd.bank.access.contains(pl.getName())) {
							pl.closeContainer();
							player.sendMessage(new TextComponentTranslation("message.bank.changed"));
							continue;
						}
						bd.openBankGui(pl, npc, ceilId);
					}
				}
			} else if (!player.getUniqueID().equals(bd.getUUID())) {
				EntityPlayerMP pl = CustomNpcs.Server.getPlayerList().getPlayerByUUID(bd.getUUID());
				if (!pl.equals(player) && pl.openContainer instanceof ContainerNPCBank && ((ContainerNPCBank) pl.openContainer).bank.id == bd.bank.id && ((ContainerNPCBank) pl.openContainer).ceil == ceilId) {
					bd.openBankGui(pl, npc, ceilId);
				}
			}
		}
	}

	/*
	 * At the beginning, a package with text is thrown at the client upon completion, then from the client
	 * the answer comes that everything is normal and this method is called:
	 */
	public static void questCompletion(EntityPlayerMP player, int questId) {
		NoppesUtilPlayer.questCompletion(player, questId, ItemStack.EMPTY);
	}

	public static void questCompletion(EntityPlayerMP player, int questId, ItemStack stack) {
		PlayerData data = PlayerData.get(player);
		PlayerQuestData playerdata = data.questData;
		QuestData activeData = playerdata.activeQuests.get(questId);
		if (activeData == null) {
			return;
		}
		Quest quest = activeData.quest;
		if (!quest.questInterface.isCompleted(player) && !activeData.isCompleted) {
			return;
		}
		QuestTurnedInEvent event = new QuestTurnedInEvent(data.scriptData.getPlayer(), quest);
		event.expReward = quest.rewardExp;
		event.moneyReward = quest.rewardMoney;
		List<IItemStack> rewardList = new ArrayList<>();
		for (ItemStack item : quest.rewardItems.items) {
			if (item != null && !item.isEmpty()) {
				rewardList.add(Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(item));
			}
		}
		if (!rewardList.isEmpty()) {
			switch (quest.rewardType) {
			case RANDOM_ONE: {
				event.itemRewards = new IItemStack[] { rewardList.get(player.getRNG().nextInt(rewardList.size())) };
				break;
			}
			case ONE_SELECT: {
				if (stack == null) {
					stack = ItemStack.EMPTY;
				}
				event.itemRewards = new IItemStack[] { Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(stack) };
				break;
			}
			default: { // ALL
				event.itemRewards = new IItemStack[rewardList.size()];
				int i = 0;
				for (IItemStack item : rewardList) {
					event.itemRewards[i] = item;
					i++;
				}
			}
			}
		}
		event.factionOptions = quest.factionOptions;
		EventHooks.onQuestTurnedIn(data.scriptData, event);

		quest.questInterface.handleComplete(player); // take away items according to the tasks of the quest
		// Give out rewards:
		if (event.expReward > 0) {
			NoppesUtilServer.playSound(player, SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, 0.1f,
					0.5f * ((player.world.rand.nextFloat() - player.world.rand.nextFloat()) * 0.7f + 1.8f));
			player.addExperience(event.expReward);
		}
		if (event.moneyReward > 0) {
			data.game.addMoney(event.moneyReward);
		}
		event.factionOptions.addPoints(player);

		if (event.mail.isValid()) {
			PlayerDataController.instance.addPlayerMessage(player.getServer(), player.getName(), event.mail);
		}

		if (!event.command.isEmpty()) {
			FakePlayer com_player = EntityNPCInterface.CommandPlayer;
			com_player.setWorld(player.world);
			com_player.setPosition(player.posX, player.posY, player.posZ);
			NoppesUtilServer.runCommand(com_player, "QuestCompletion", event.command, player);
		}

        for (IItemStack stackRew : event.itemRewards) {
            NoppesUtilServer.GivePlayerItem(player, player, stackRew.getMCItemStack());
        }
        PlayerQuestController.setQuestFinished(quest, player);
		Quest nextQuest = (QuestController.instance == null) ? null
				: QuestController.instance.quests.get(event.nextQuestId);
		if (nextQuest != null) {
			PlayerQuestController.addActiveQuest(nextQuest, player, false);
		}
		Server.sendData(player, EnumPacketClient.MESSAGE, "quest.finished", quest.getTitle(), 2);
		Server.sendData(player, EnumPacketClient.CHAT, "quest.finished", ": ", quest.getTitle());
	}

	public static void sendData(EnumPlayerPacket enu, Object... obs) {
		PacketBuffer buffer = new PacketBuffer(Unpooled.buffer());
		try {
			if (!Server.fillBuffer(buffer, enu, obs)) {
				return;
			}
			CustomNpcs.ChannelPlayer.sendToServer(new FMLProxyPacket(buffer, "CustomNPCsPlayer"));
		} catch (Exception e) {
			LogWriter.error("Error send data:", e);
		}
	}

	public static void teleportPlayer(EntityPlayerMP player, double x, double y, double z, int dimension, float yaw,
			float pitch) {
		if (player.dimension != dimension) {
			MinecraftServer server = player.getServer();
            assert server != null;
            WorldServer wor = server.getWorld(dimension);
            net.minecraftforge.common.ForgeHooks.onTravelToDimension(player, dimension);
			player.setLocationAndAngles(x, y, z, yaw, pitch);
			server.getPlayerList().transferPlayerToDimension(player, dimension, new CustomTeleporter(wor));
			player.connection.setPlayerLocation(x, y, z, yaw, pitch);
			if (!wor.playerEntities.contains(player)) {
				wor.spawnEntity(player);
			}
		} else {
			player.connection.setPlayerLocation(x, y, z, yaw, pitch);
		}
		player.world.updateEntityWithOptionalForce(player, false);
	}

	public static void sendDataCheckDelay(EnumPlayerPacket enu, Object key, long time, Object... obs) {
		if (NoppesUtilPlayer.delaySendMap.containsKey(key)
				&& NoppesUtilPlayer.delaySendMap.get(key) > System.currentTimeMillis()) {
			return;
		}
		List<Object> del = new ArrayList<>();
		for (Object k : NoppesUtilPlayer.delaySendMap.keySet()) {
			if (NoppesUtilPlayer.delaySendMap.get(k) <= System.currentTimeMillis()) {
				del.add(k);
			}
		}
		for (Object k : del) {
			NoppesUtilPlayer.delaySendMap.remove(k);
		}
		NoppesUtilPlayer.delaySendMap.put(key, time + System.currentTimeMillis());
		NoppesUtilPlayer.sendData(enu, obs);
	}

}
