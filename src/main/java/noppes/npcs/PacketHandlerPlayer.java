package noppes.npcs;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetHandlerPlayServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.event.ItemEvent;
import noppes.npcs.api.event.PlayerEvent;
import noppes.npcs.api.event.RoleEvent;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.wrapper.ItemScriptedWrapper;
import noppes.npcs.api.wrapper.PlayerWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiWrapper;
import noppes.npcs.client.ClientProxy;
import noppes.npcs.constants.EnumCompanionTalent;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumNpcRole;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.containers.ContainerMail;
import noppes.npcs.controllers.CustomGuiController;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.PlayerQuestController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.BankData;
import noppes.npcs.controllers.data.Deal;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerFactionData;
import noppes.npcs.controllers.data.PlayerGameData;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.controllers.data.PlayerMailData;
import noppes.npcs.controllers.data.PlayerOverlayHUD;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.PlayerScriptData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.items.ItemBuilder;
import noppes.npcs.items.ItemScripted;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.roles.RoleTransporter;
import noppes.npcs.util.AdditionalMethods;
import noppes.npcs.util.BuilderData;
import noppes.npcs.util.ObfuscationHelper;
import noppes.npcs.util.ServerNpcRecipeBookHelper;

public class PacketHandlerPlayer {

	@SubscribeEvent
	public void onServerPacket(FMLNetworkEvent.ServerCustomPacketEvent event) {
		EntityPlayerMP player = ((NetHandlerPlayServer) event.getHandler()).player;
		ByteBuf buffer = event.getPacket().payload();
		player.getServer().addScheduledTask(() -> {
			EnumPlayerPacket type = null;
			List<EnumPlayerPacket> list = new ArrayList<EnumPlayerPacket>();
			list.add(EnumPlayerPacket.NpcVisualData);
			list.add(EnumPlayerPacket.KeyPressed);
			list.add(EnumPlayerPacket.IsMoved);
			list.add(EnumPlayerPacket.LeftClick);
			list.add(EnumPlayerPacket.MousesPressed);
			try {
				type = EnumPlayerPacket.values()[buffer.readInt()];
				if (!list.contains(type)) {
					LogWriter.debug("Received: " + type);
				}
				this.player(buffer, player, type);
			} catch (Exception e) {
				LogWriter.error("Error with EnumPlayerPacket." + type, e);
			} finally {
				buffer.release();
			}
		});
	}

	@SuppressWarnings("unchecked")
	private void player(ByteBuf buffer, EntityPlayerMP player, EnumPlayerPacket type) throws Exception {
		CustomNpcs.debugData.startDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
		PlayerData data = PlayerData.get(player);
		if (type == EnumPlayerPacket.MarkData) {
			Entity entity = player.getServer().getEntityFromUuid(Server.readUUID(buffer));
			if (entity == null || !(entity instanceof EntityLivingBase)) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			MarkData.get((EntityLivingBase) entity);
		} else if (type == EnumPlayerPacket.KeyPressed) {
			PlayerOverlayHUD hud = data.hud;
			int key = buffer.readInt();
			if (key<0) {
				for (int k : hud.keyPress) { EventHooks.onPlayerKeyPressed(player, k, false, false, false, false, false); }
				hud.keyPress.clear();
				return;
			}
			boolean isDown = buffer.readBoolean();
			if (isDown) { hud.keyPress.add(key); }
			else {
				if (hud.hasOrKeysPressed(key)) { hud.keyPress.remove((Integer)key); }
			}
			if (!CustomNpcs.EnableScripting || ScriptController.Instance.languages.isEmpty()) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			EventHooks.onPlayerKeyPressed(player, key, isDown, buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
			
		} else if (type == EnumPlayerPacket.MousesPressed) {
			PlayerOverlayHUD hud = data.hud;
			int key = buffer.readInt();
			if (key<0) {
				for (int k : hud.mousePress) { EventHooks.onPlayerMousePressed(player, k, false, false, false, false, false); }
				hud.mousePress.clear();
				return;
			}
			boolean isDown = buffer.readBoolean();
			if (isDown) { hud.mousePress.add(key); }
			else {
				if (hud.hasMousePress(key)) { hud.mousePress.remove((Integer) key); }
			}
			if (!CustomNpcs.EnableScripting || ScriptController.Instance.languages.isEmpty()) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			EventHooks.onPlayerMousePressed(player, key, isDown, buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean(), buffer.readBoolean());
		} else if (type == EnumPlayerPacket.LeftClick) {
			if (!CustomNpcs.EnableScripting || ScriptController.Instance.languages.isEmpty()) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			ItemStack item = player.getHeldItemMainhand();
			PlayerScriptData handler = PlayerData.get((EntityPlayer) player).scriptData;
			PlayerEvent.AttackEvent ev = new PlayerEvent.AttackEvent(handler.getPlayer(), 0, null);
			EventHooks.onPlayerAttack(handler, ev);
			if (item.getItem() == CustomItems.scripted_item) {
				ItemScriptedWrapper isw = ItemScripted.GetWrapper(item);
				ItemEvent.AttackEvent eve = new ItemEvent.AttackEvent(isw, handler.getPlayer(), 0, null);
				EventHooks.onScriptItemAttack(isw, eve);
			}
		} else if (type == EnumPlayerPacket.CustomGuiClose) {
			ICustomGui gui = new CustomGuiWrapper(player).fromNBT(Server.readNBT(buffer));
			EventHooks.onCustomGuiClose((PlayerWrapper<EntityPlayerMP>) NpcAPI.Instance().getIEntity(player), gui);
		} else if (type == EnumPlayerPacket.CustomGuiButton) {
			if (player.openContainer instanceof ContainerCustomGui) {
				((ContainerCustomGui) player.openContainer).customGui.fromNBT(Server.readNBT(buffer));
				EventHooks.onCustomGuiButton((PlayerWrapper<EntityPlayerMP>) NpcAPI.Instance().getIEntity(player),
						((ContainerCustomGui) player.openContainer).customGui, buffer.readInt());
			}
		} else if (type == EnumPlayerPacket.CustomGuiScrollClick) {
			if (player.openContainer instanceof ContainerCustomGui) {
				((ContainerCustomGui) player.openContainer).customGui.fromNBT(Server.readNBT(buffer));
				EventHooks.onCustomGuiScrollClick((PlayerWrapper<EntityPlayerMP>) NpcAPI.Instance().getIEntity(player),
						((ContainerCustomGui) player.openContainer).customGui, buffer.readInt(), buffer.readInt(),
						CustomGuiController.readScrollSelection(buffer), buffer.readBoolean());
			}
		} else if (type == EnumPlayerPacket.CloseGui) {
			player.closeContainer();
		} else if (type == EnumPlayerPacket.CompanionTalentExp) {
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc((EntityPlayer) player);
			if (npc == null || !(npc.advanced.roleInterface instanceof RoleCompanion) || player != npc.getOwner()) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			int id = buffer.readInt();
			int exp = buffer.readInt();
			RoleCompanion role = (RoleCompanion) npc.advanced.roleInterface;
			if (exp <= 0 || !role.canAddExp(-exp) || id < 0 || id >= EnumCompanionTalent.values().length) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			EnumCompanionTalent talent = EnumCompanionTalent.values()[id];
			role.addExp(-exp);
			role.addTalentExp(talent, exp);
		} else if (type == EnumPlayerPacket.CompanionOpenInv) {
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc((EntityPlayer) player);
			if (npc == null || !(npc.advanced.roleInterface instanceof RoleCompanion) || player != npc.getOwner()) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			NoppesUtilServer.sendOpenGui((EntityPlayer) player, EnumGuiType.CompanionInv, npc);
		} else if (type == EnumPlayerPacket.FollowerHire) {
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc((EntityPlayer) player);
			if (npc == null || npc.advanced.roleInterface.getEnumType() != EnumNpcRole.FOLLOWER) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			NoppesUtilPlayer.hireFollower(player, npc);
		} else if (type == EnumPlayerPacket.FollowerExtend) {
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc((EntityPlayer) player);
			if (npc == null || npc.advanced.roleInterface.getEnumType() != EnumNpcRole.FOLLOWER) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			NoppesUtilPlayer.extendFollower(player, npc);
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.advanced.roleInterface.writeToNBT(new NBTTagCompound()));
		} else if (type == EnumPlayerPacket.FollowerState) {
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc((EntityPlayer) player);
			if (npc == null || npc.advanced.roleInterface.getEnumType() != EnumNpcRole.FOLLOWER) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			NoppesUtilPlayer.changeFollowerState(player, npc);
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.advanced.roleInterface.writeToNBT(new NBTTagCompound()));
		} else if (type == EnumPlayerPacket.RoleGet) {
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc((EntityPlayer) player);
			if (npc == null) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			Server.sendData(player, EnumPacketClient.GUI_DATA, npc.advanced.roleInterface.writeToNBT(new NBTTagCompound()));
		} else if (type == EnumPlayerPacket.Transport) {
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc((EntityPlayer) player);
			if (npc == null || !(npc.advanced.roleInterface instanceof RoleTransporter)) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			((RoleTransporter) npc.advanced.roleInterface).transport(player, Server.readString(buffer));
		} else if (type == EnumPlayerPacket.BankUpgrade) {
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc((EntityPlayer) player);
			if (npc == null || npc.advanced.roleInterface.getEnumType() != EnumNpcRole.BANK) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			NoppesUtilPlayer.bankUpgrade(player, npc);
		} else if (type == EnumPlayerPacket.BankUnlock) {
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc((EntityPlayer) player);
			if (npc == null || npc.advanced.roleInterface.getEnumType() != EnumNpcRole.BANK) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			NoppesUtilPlayer.bankUnlock(player, npc);
		} else if (type == EnumPlayerPacket.BankSlotOpen) {
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc((EntityPlayer) player);
			if (npc == null || npc.advanced.roleInterface.getEnumType() != EnumNpcRole.BANK) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			int slot = buffer.readInt();
			int bankId = buffer.readInt();
			BankData bd = PlayerDataController.instance.getBankData((EntityPlayer) player, bankId).getBankOrDefault(bankId);
			bd.openBankGui((EntityPlayer) player, npc, bankId, slot);
		} else if (type == EnumPlayerPacket.Dialog) {
			EntityNPCInterface npc = NoppesUtilServer.getEditingNpc((EntityPlayer) player);
			LogWriter.debug("Dialog npc: " + npc);
			if (npc == null) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			NoppesUtilPlayer.dialogSelected(buffer.readInt(), buffer.readInt(), player, npc);
		} else if (type == EnumPlayerPacket.CheckQuestCompletion) {
			int id = buffer.readInt();
			PlayerQuestData playerdata = PlayerData.get((EntityPlayer) player).questData;
			for (QuestData qd : playerdata.activeQuests.values()) {
				if (id>0 && qd.quest.id!=id) { continue; }
				playerdata.checkQuestCompletion(player, qd);
			}
		} else if (type == EnumPlayerPacket.QuestCompletion) {
			NoppesUtilPlayer.questCompletion(player, buffer.readInt());
		} else if (type == EnumPlayerPacket.QuestCompletionReward) {
			int id = buffer.readInt();
			ItemStack stack = new ItemStack(Server.readNBT(buffer));
			NoppesUtilPlayer.questCompletion(player, id, stack);
		}  else if (type == EnumPlayerPacket.FactionsGet) {
			PlayerFactionData data2 = PlayerData.get((EntityPlayer) player).factionData;
			Server.sendData(player, EnumPacketClient.GUI_DATA, data2.getPlayerGuiData());
		} else if (type == EnumPlayerPacket.MailGet) {
			PlayerMailData data3 = PlayerData.get((EntityPlayer) player).mailData;
			Server.sendData(player, EnumPacketClient.GUI_DATA, data3.saveNBTData(new NBTTagCompound()));
		} else if (type == EnumPlayerPacket.MailDelete) {
			long time = buffer.readLong();
			String username = Server.readString(buffer);
			PlayerMailData data4 = PlayerData.get((EntityPlayer) player).mailData;
			Iterator<PlayerMail> it = data4.playermail.iterator();
			while (it.hasNext()) {
				PlayerMail mail = it.next();
				if (mail.time == time && mail.sender.equals(username)) {
					it.remove();
				}
			}
			Server.sendData(player, EnumPacketClient.GUI_DATA, data4.saveNBTData(new NBTTagCompound()));
		} else if (type == EnumPlayerPacket.MailSend) {
			// Changed
			String username = Server.readString(buffer);
			if (username.equalsIgnoreCase(player.getName()) && !player.capabilities.isCreativeMode) {
				username = PlayerDataController.instance.hasPlayer(Server.readString(buffer));
			}
			if (username.isEmpty()) {
				NoppesUtilServer.sendGuiError((EntityPlayer) player, 0);
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			PlayerMail mail2 = new PlayerMail();
			String s = player.getDisplayNameString();
			if (!s.equals(player.getName())) {
				s = s + "(" + player.getName() + ")";
			}
			mail2.readNBT(Server.readNBT(buffer));
			mail2.sender = s;
			mail2.items = ((ContainerMail) player.openContainer).mail.items;
			if (mail2.subject.isEmpty()) {
				NoppesUtilServer.sendGuiError((EntityPlayer) player, 1);
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			NBTTagCompound comp = new NBTTagCompound();
			comp.setString("username", username);
			NoppesUtilServer.sendGuiClose(player, 1, comp);
			EntityNPCInterface npc2 = NoppesUtilServer.getEditingNpc((EntityPlayer) player);
			if (npc2 != null && EventHooks.onNPCRole(npc2,
					new RoleEvent.MailmanEvent((EntityPlayer) player, npc2.wrappedNPC, mail2))) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			PlayerDataController.instance.addPlayerMessage(player.getServer(), username, mail2);
		} else if (type == EnumPlayerPacket.MailboxOpenMail) {
			long time = buffer.readLong();
			String username = Server.readString(buffer);
			player.closeContainer();
			PlayerMailData data4 = PlayerData.get((EntityPlayer) player).mailData;
			for (PlayerMail mail : data4.playermail) {
				if (mail.time == time && mail.sender.equals(username)) {
					ContainerMail.staticmail = mail;
					player.openGui(CustomNpcs.instance, EnumGuiType.PlayerMailman.ordinal(), player.world, 0, 0, 0);
					break;
				}
			}
		} else if (type == EnumPlayerPacket.MailRead) {
			long time = buffer.readLong();
			String username = Server.readString(buffer);
			PlayerMailData data4 = PlayerData.get((EntityPlayer) player).mailData;
			for (PlayerMail mail : data4.playermail) {
				if (!mail.beenRead && mail.time == time && mail.sender.equals(username)) {
					if (mail.hasQuest()) {
						PlayerQuestController.addActiveQuest(mail.getQuest(), (EntityPlayer) player);
					}
					mail.beenRead = true;
				}
			}
		}
		// New
		else if (type == EnumPlayerPacket.QuestRemoveActive) {
			int id = buffer.readInt();
			IQuest quest = QuestController.instance.get(id);
			if (quest == null) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			boolean bo = EventHooks.onQuestCanceled(PlayerData.get(player).scriptData, (Quest) quest);
			if (!bo && PlayerQuestController.getRemoveActiveQuest(player, id)) {
				player.sendMessage(new TextComponentTranslation("quest.removequest", quest.getTitle()));
				Server.sendData(player, EnumPacketClient.GUI_DATA, new NBTTagCompound());
			} else {
				player.sendMessage(new TextComponentTranslation("quest.removequest.not", quest.getTitle()));
			}
		} else if (type == EnumPlayerPacket.QuestChooseReward) {
			int id = buffer.readInt();
			NoppesUtilServer.sendOpenGui(player, EnumGuiType.QuestRewardItem, null, id, 0, 0);
		} else if (type == EnumPlayerPacket.NpcVisualData) {
			int entityID = buffer.readInt();
			Entity entity = player.world.getEntityByID(entityID);
			if (entity != null && entity instanceof EntityNPCInterface) {
				NBTTagCompound compound = new NBTTagCompound();
				EntityNPCInterface npcData = (EntityNPCInterface) entity;
				compound.setInteger("NPCLevel", npcData.stats.getLevel());
				compound.setInteger("NPCRarity", npcData.stats.getRarity());
				compound.setString("RarityTitle", npcData.stats.getRarityTitle());
				Server.sendData(player, EnumPacketClient.NPC_VISUAL_DATA, entityID, compound);
			}
		} else if (type == EnumPlayerPacket.IsMoved) {
			PlayerData.get((EntityPlayer) player).hud.isMoved = buffer.readBoolean();
		} else if (type == EnumPlayerPacket.WindowSize) {
			PlayerOverlayHUD hud = data.hud;
			NBTTagCompound compound = Server.readNBT(buffer);
			hud.setWindowSize(compound.getTagList("WindowSize", 6));
		} else if (type == EnumPlayerPacket.GetGhostRecipe) {
			player.markPlayerActive();
			if (player.isSpectator() || player.openContainer.windowId != buffer.readInt()
					|| !player.openContainer.getCanCraft(player)) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			int recipeId = buffer.readInt();
			boolean isShiftPress = buffer.readBoolean();
			IRecipe recipe = CraftingManager.REGISTRY.getObjectById(recipeId);
			if (recipe == null || !player.getRecipeBook().isUnlocked(recipe)) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			ServerNpcRecipeBookHelper serverRecipeBookHelper = new ServerNpcRecipeBookHelper();
			serverRecipeBookHelper.getGhostRecipe(player, recipe, isShiftPress);
		} else if (type == EnumPlayerPacket.TraderMarketBuy) {
			Marcet m = MarcetController.getInstance().getMarcet(buffer.readInt());
			if (m == null || !m.hasListener(player)) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			Deal d = m.data.get(buffer.readInt());
			if (d == null || d.type == 1 || d.inventorySold.getStackInSlot(0).isEmpty()) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			boolean canGiveItem = d.availability.isAvailable(player);
			if (!player.capabilities.isCreativeMode) {
				// Has Money
				if (canGiveItem && (data!=null && d.money > data.game.money)) {
					canGiveItem = false;
				}
				// Has Items
				if (canGiveItem) {
					canGiveItem = AdditionalMethods.canRemoveItems(player, d.inventoryCurrency.items, d.ignoreDamage, d.ignoreNBT);
				}
			}
			if (canGiveItem) {
				// Del Money
				if (data != null) { data.game.addMoney(-1 * d.money); }
				// Del Items
				for (ItemStack st : d.inventoryCurrency.items) {
					AdditionalMethods.removeItem(player, st, d.ignoreDamage, d.ignoreNBT);
				}
				// Give
				ItemStack stack = d.inventorySold.getStackInSlot(0).copy();
				if (d.count[1] != 0 && d.amount != 0) {
					if (d.amount < stack.getCount()) {
						stack.setCount(d.amount);
					}
					d.amount -= stack.getCount();
				}
				boolean bo = stack.getCount() == 0 ? false : player.inventory.addItemStackToInventory(stack);
				if (bo) {
					player.sendMessage(new TextComponentTranslation("mes.market.buy",
							new Object[] { d.inventorySold.getStackInSlot(0).getDisplayName() + " x"
									+ d.inventorySold.getStackInSlot(0).getCount() }));
					NoppesUtilServer.playSound(player, SoundEvents.ENTITY_ITEM_PICKUP, 0.2f,
							((player.getRNG().nextFloat() - player.getRNG().nextFloat()) * 0.7f + 1.0f) * 2.0f);
					AdditionalMethods.updatePlayerInventory(player);
				}
				m.detectAndSendChanges();
			}
		} else if (type == EnumPlayerPacket.TraderMarketSell) {
			Marcet m = MarcetController.getInstance().getMarcet(buffer.readInt());
			if (m == null || !m.hasListener(player)) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			Deal d = m.data.get(buffer.readInt());
			if (d == null || d.type == 0 || d.inventorySold.getStackInSlot(0).isEmpty()) {
				CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
				return;
			}
			ItemStack stack = d.inventorySold.getStackInSlot(0);
			if (player.capabilities.isCreativeMode
					|| AdditionalMethods.removeItem(player, stack, d.ignoreDamage, d.ignoreNBT)) {
				// Add Items
				List<ItemStack> list = Lists.<ItemStack>newArrayList();
				List<ItemStack> givelist = Lists.<ItemStack>newArrayList();
				for (ItemStack st : d.inventoryCurrency.items) {
					if (st.isEmpty()) { continue; }
					AdditionalMethods.addStackToList(list, st);
				}
				if (list.size()>0) {
					for (ItemStack st : list) {
						int size = (int) Math.floor((double) st.getCount()/4.0d);
						if (size==0) { continue; }
						ItemStack s = st.copy();
						s.setCount(size);
						givelist.add(s); 
					}
					if (givelist.isEmpty()) {
						Random random = new Random();
						ItemStack s = list.get(random.nextInt(list.size())).copy();
						s.setCount(1);
						givelist.add(s);
					}
				}
				if (givelist.size()>0) {
					boolean change = false;
					for (ItemStack st : givelist) {
						if (player.inventory.addItemStackToInventory(st)) {
							change = true;
						}
					}
					if (change) {
						NoppesUtilServer.playSound(player, SoundEvents.ENTITY_ITEM_PICKUP, 0.2f, ((player.getRNG().nextFloat() - player.getRNG().nextFloat()) * 0.7f + 1.0f) * 2.0f);
						AdditionalMethods.updatePlayerInventory(player);
					}
				}
				// Add Money
				int money = (int) Math.floor((double) d.money / 4.0d);
				if (money>0) {
					if (data != null) {
						data.game.addMoney(money);
					}
				}
				if (d.count[1] != 0) {
					d.amount += stack.getCount();
				}
				player.sendMessage(new TextComponentTranslation("mes.market.sell",
						new Object[] { stack.getDisplayName() + " x" + stack.getCount() }));
				m.detectAndSendChanges();
			}
		} else if (type == EnumPlayerPacket.TraderMarketReset) {
			Marcet m = MarcetController.getInstance().getMarcet(buffer.readInt());
			if (m != null) {
				m.lastTime = 0L;
				m.update();
			}
		} else if (type == EnumPlayerPacket.TraderMarketRemove) {
			Marcet m = MarcetController.getInstance().getMarcet(buffer.readInt());
			if (m != null) {
				m.removeListener(player, true);
			}
		} else if (type == EnumPlayerPacket.TakeMoney) {
			String sender = Server.readString(buffer);
			String subject = Server.readString(buffer);
			long time = buffer.readLong();
			for (PlayerMail mail : data.mailData.playermail) {
				if (mail.sender.equals(sender) && mail.subject.equals(subject) && mail.time == time && mail.money > 0) {
					data.game.addMoney(mail.money);
					mail.money = 0;
					break;
				}
			}
		} else if (type == EnumPlayerPacket.ScriptDataGetVar) {
			// AdditionalMethods.createAndSendVarFuncData(player, Server.readNBT(buffer));
		} else if (type == EnumPlayerPacket.CurrentLanguage) {
			ObfuscationHelper.setValue(PlayerGameData.class, ClientProxy.playerData.game, Server.readString(buffer), String.class);
		} else if (type == EnumPlayerPacket.GetBuildData) {
			if (player.getHeldItemMainhand().isEmpty() || !(player.getHeldItemMainhand().getItem() instanceof ItemBuilder) || !player.getHeldItemMainhand().hasTagCompound()) { return; }
			int id = player.getHeldItemMainhand().getTagCompound().getInteger("ID");
			BuilderData builder = CommonProxy.dataBuilder.get(id);
			if (builder==null) {
				ItemBuilder.cheakStack(player.getHeldItemMainhand());
				builder = CommonProxy.dataBuilder.get(id);
				if (builder==null) { return; }
			}
			Server.sendData(player, EnumPacketClient.BUILDER_SETTING, builder.getNbt());
		} else if (type == EnumPlayerPacket.HudTimerEnd) {
			EventHooks.onPlayerTimer(PlayerData.get(player), buffer.readInt());
		} else if (type == EnumPlayerPacket.TrackQuest) {
			data.hud.questID = buffer.readInt();
		} else if (type == EnumPlayerPacket.SaveCompassData) {
			data.hud.compassData.load(Server.readNBT(buffer));
		} else if (type == EnumPlayerPacket.GetTileData) {
			NBTTagCompound compound = Server.readNBT(buffer);
			TileEntity tile = player.world.getTileEntity(new BlockPos(compound.getInteger("x"), compound.getInteger("y"), compound.getInteger("z")));
			if (tile!=null) {
				tile.writeToNBT(compound);
				Server.sendData(player, EnumPacketClient.SET_TILE_DATA, compound);
			}
		}
		CustomNpcs.debugData.endDebug("Server", player, "PacketHandlerPlayer_Received_"+type.toString());
	}
}
