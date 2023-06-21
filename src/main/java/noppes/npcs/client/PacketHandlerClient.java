package noppes.npcs.client;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.google.common.collect.Lists;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.recipebook.IRecipeShownListener;
import net.minecraft.client.gui.toasts.IToast;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import net.minecraftforge.fml.common.network.internal.EntitySpawnMessageHelper;
import noppes.npcs.CommonProxy;
import noppes.npcs.CustomItems;
import noppes.npcs.CustomNpcs;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
import noppes.npcs.ModelData;
import noppes.npcs.NBTTags;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.PacketHandlerServer;
import noppes.npcs.Server;
import noppes.npcs.ServerEventsHandler;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.wrapper.ItemStackWrapper;
import noppes.npcs.client.controllers.MusicController;
import noppes.npcs.client.gui.GuiAchievement;
import noppes.npcs.client.gui.GuiNpcMobSpawnerAdd;
import noppes.npcs.client.gui.GuiNpcRemoteEditor;
import noppes.npcs.client.gui.global.GuiNPCManageDialogs;
import noppes.npcs.client.gui.global.GuiNPCManageQuest;
import noppes.npcs.client.gui.player.GuiCustomChest;
import noppes.npcs.client.gui.player.GuiNPCTrader;
import noppes.npcs.client.gui.player.GuiQuestCompletion;
import noppes.npcs.client.gui.script.GuiScriptInterface;
import noppes.npcs.client.gui.util.GuiContainerNPCInterface;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.IGuiClose;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.IGuiError;
import noppes.npcs.client.gui.util.IScrollData;
import noppes.npcs.client.model.animation.AnimationConfig;
import noppes.npcs.client.renderer.MarkRenderer;
import noppes.npcs.constants.EnumAnimationType;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.constants.EnumRewardType;
import noppes.npcs.controllers.BorderController;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.DropController;
import noppes.npcs.controllers.MarcetController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.SchematicController;
import noppes.npcs.controllers.SyncController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DropsTemplate;
import noppes.npcs.controllers.data.Marcet;
import noppes.npcs.controllers.data.MarkData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityDialogNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataAnimation;
import noppes.npcs.items.ItemScripted;
import noppes.npcs.schematics.Schematic;
import noppes.npcs.schematics.SchematicWrapper;
import noppes.npcs.util.BuilderData;

public class PacketHandlerClient
extends PacketHandlerServer {

	@SubscribeEvent
	public void onPacketData(FMLNetworkEvent.ClientCustomPacketEvent event) {
		EntityPlayer player = (EntityPlayer) Minecraft.getMinecraft().player;
		if (player == null) {
			return;
		}
		ByteBuf buffer = event.getPacket().payload();
		Minecraft.getMinecraft().addScheduledTask(() -> {
			EnumPacketClient type = null;
			try {
				type = EnumPacketClient.values()[buffer.readInt()];
				if (type != EnumPacketClient.EYE_BLINK && type != EnumPacketClient.NPC_VISUAL_DATA && type != EnumPacketClient.UPDATE_NPC) {
					LogWriter.debug("Received: " + type);
				} // Changed
				this.client(buffer, player, type);
			} catch (Exception e) {
				LogWriter.error("Error with EnumPacketClient." + type, e);
			} finally {
				buffer.release();
			}
		});
	}
	
	private void client(ByteBuf buffer, EntityPlayer player, EnumPacketClient type) throws Exception {
		CustomNpcs.debugData.startDebug("Client", player, "PackageReceived_"+type.toString());
		if (type == EnumPacketClient.CHATBUBBLE) {
			Entity entity = Minecraft.getMinecraft().world.getEntityByID(buffer.readInt());
			if (entity == null || !(entity instanceof EntityNPCInterface)) {
				CustomNpcs.debugData.endDebug("Client", player, "PackageReceived_"+type.toString());
				return;
			}
			EntityNPCInterface npc = (EntityNPCInterface) entity;
			if (npc.messages == null) {
				npc.messages = new RenderChatMessages();
			}
			String text = NoppesStringUtils.formatText(Server.readString(buffer), player, npc);
			npc.messages.addMessage(text, npc);
			if (buffer.readBoolean()) {
				player.sendMessage(new TextComponentTranslation(npc.getName() + ": " + text, new Object[0]));
			}
		} else if (type == EnumPacketClient.CHAT) {
			String message = "";
			String str;
			while ((str = Server.readString(buffer)) != null && !str.isEmpty()) {
				message += new TextComponentTranslation(str).getFormattedText();
			}
			player.sendMessage(new TextComponentTranslation(message, new Object[0]));
		} else if (type == EnumPacketClient.EYE_BLINK) {
			Entity entity = Minecraft.getMinecraft().world.getEntityByID(buffer.readInt());
			if (entity == null || !(entity instanceof EntityNPCInterface)) {
				CustomNpcs.debugData.endDebug("Client", player, "PackageReceived_"+type.toString());
				return;
			}
			ModelData data = ((EntityCustomNpc) entity).modelData;
			data.eyes.blinkStart = System.currentTimeMillis();
		} else if (type == EnumPacketClient.MESSAGE) {
			TextComponentTranslation title = new TextComponentTranslation(Server.readString(buffer), new Object[0]);
			TextComponentTranslation message = new TextComponentTranslation(Server.readString(buffer), new Object[0]);
			int btype = buffer.readInt();
			Minecraft.getMinecraft().getToastGui().add((IToast) new GuiAchievement(title, message, btype));
		} else if (type == EnumPacketClient.UPDATE_ITEM) {
			int id = buffer.readInt();
			NBTTagCompound compound = Server.readNBT(buffer);
			ItemStack stack = player.inventory.getStackInSlot(id);
			if (!stack.isEmpty()) {
				((ItemStackWrapper) NpcAPI.Instance().getIItemStack(stack)).setMCNbt(compound);
			}
		} else if (type == EnumPacketClient.VISIBLE_FALSE) {
			WorldClient w = (WorldClient) player.world;
			UUID uuid = Server.readUUID(buffer);
			int id = buffer.readInt();
			List<EntityNPCInterface> npcInterfaces = w.getEntities(EntityNPCInterface.class, entity -> entity.getUniqueID().equals(uuid));
			if (npcInterfaces.size()==0) {
				npcInterfaces = w.getEntities(EntityNPCInterface.class, entity -> entity.getEntityId()==id);
			}
			for (EntityNPCInterface npc : npcInterfaces) {
				if (npc == null) { continue; }
				w.removeEntity(npc);
			}
			if (!ClientProxy.notVisibleNPC.containsKey(player.world.provider.getDimension())) {
				ClientProxy.notVisibleNPC.put(player.world.provider.getDimension(), Lists.<UUID>newArrayList());
			}
			ClientProxy.notVisibleNPC.get(player.world.provider.getDimension()).add(uuid);
		} else if (type == EnumPacketClient.VISIBLE_TRUE) {
			WorldClient w = (WorldClient) player.world;
			UUID uuid = Server.readUUID(buffer);
			int id = buffer.readInt();
			List<EntityNPCInterface> npcInterfaces = w.getEntities(EntityNPCInterface.class, entity -> entity.getUniqueID().equals(uuid));
			if (npcInterfaces.size()==0) {
				npcInterfaces = w.getEntities(EntityNPCInterface.class, entity -> entity.getEntityId()==id);
			}
			if (npcInterfaces.size()==0) {
				EntitySpawnMessageHelper.spawn(buffer);
			}
			if (npcInterfaces.size()!=0) {
				for (EntityNPCInterface npc : npcInterfaces) {
					if (npc == null) {
						EntitySpawnMessageHelper.spawn(buffer);
					}
				}
			}
			if (!ClientProxy.notVisibleNPC.containsKey(player.world.provider.getDimension())) {
				ClientProxy.notVisibleNPC.put(player.world.provider.getDimension(), Lists.<UUID>newArrayList());
			}
			for (UUID uID : ClientProxy.notVisibleNPC.get(player.world.provider.getDimension())) {
				if (uuid.equals(uID)) {
					ClientProxy.notVisibleNPC.get(player.world.provider.getDimension()).remove(uID);
					return;
				}
			}
		} else if (type == EnumPacketClient.SYNC_ADD || type == EnumPacketClient.SYNC_END) {
			int synctype = buffer.readInt();
			NBTTagCompound compound = Server.readNBT(buffer);
			SyncController.clientSync(synctype, compound, type == EnumPacketClient.SYNC_END, player);
			
			if (synctype == 3) { // Quest
				if (Minecraft.getMinecraft().currentScreen instanceof GuiNPCManageQuest) {
					((GuiNPCManageQuest) Minecraft.getMinecraft().currentScreen).initGui();
				}
			} else if (synctype == 5) { // Dialogs
				if (Minecraft.getMinecraft().currentScreen instanceof GuiNPCManageDialogs) {
					((GuiNPCManageDialogs) Minecraft.getMinecraft().currentScreen).initGui();
				}
			} else if (synctype == 8) {
				ClientProxy.playerData.setNBT(compound);
			} else if (synctype == 9) {
				if (player.getServer() == null) {
					ItemScripted.Resources = NBTTags.getIntegerStringMap(compound.getTagList("List", 10));
				}
				for (Map.Entry<Integer, String> entry : ItemScripted.Resources.entrySet()) {
					ModelResourceLocation mrl = new ModelResourceLocation((String) entry.getValue(), "inventory");
					Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register((Item) CustomItems.scripted_item, entry.getKey(), mrl);
					ModelLoader.setCustomModelResourceLocation((Item) CustomItems.scripted_item, entry.getKey(), mrl);
				}
			}
		} else if (type == EnumPacketClient.SYNC_UPDATE) {
			int synctype = buffer.readInt();
			NBTTagCompound compound = Server.readNBT(buffer);
			SyncController.clientSyncUpdate(synctype, compound, buffer, player);
		} else if (type == EnumPacketClient.CHEST_NAME) {
			GuiScreen screen = Minecraft.getMinecraft().currentScreen;
			if (screen instanceof GuiCustomChest) {
				((GuiCustomChest) screen).title = new TextComponentTranslation(Server.readString(buffer))
						.getFormattedText();
			}
		} else if (type == EnumPacketClient.SYNC_REMOVE) {
			int synctype = buffer.readInt();
			int id = buffer.readInt();
			SyncController.clientSyncRemove(synctype, id, player);
		} else if (type == EnumPacketClient.MARK_DATA) {
			Entity entity = Minecraft.getMinecraft().world.getEntityByID(buffer.readInt());
			if (entity == null || !(entity instanceof EntityLivingBase)) {
				CustomNpcs.debugData.endDebug("Client", player, "PackageReceived_"+type.toString());
				return;
			}
			MarkData data = MarkData.get((EntityLivingBase) entity);
			data.setNBT(Server.readNBT(buffer));
			MarkRenderer.needReload = true;
		} else if (type == EnumPacketClient.DIALOG) {
			Entity entity = Minecraft.getMinecraft().world.getEntityByID(buffer.readInt());
			if (entity == null || !(entity instanceof EntityNPCInterface)) {
				CustomNpcs.debugData.endDebug("Client", player, "PackageReceived_"+type.toString());
				return;
			}
			Dialog dialog = DialogController.instance.dialogs.get(buffer.readInt());
			NoppesUtil.openDialog(dialog, (EntityNPCInterface) entity, player);
		} else if (type == EnumPacketClient.DIALOG_DUMMY) {
			EntityDialogNpc npc = new EntityDialogNpc(player.world);
			npc.display.setName(Server.readString(buffer));
			EntityUtil.Copy(player, npc);
			Dialog dialog = new Dialog(null);
			dialog.readNBT(Server.readNBT(buffer));
			NoppesUtil.openDialog(dialog, npc, player);
		} else if (type == EnumPacketClient.QUEST_COMPLETION) {
			int id = buffer.readInt();
			Quest quest = (Quest) QuestController.instance.get(id);
			if (!quest.completeText.isEmpty()) { NoppesUtil.openGUI(player, new GuiQuestCompletion(quest)); }
			else if (quest.rewardType==EnumRewardType.ONE && !quest.rewardItems.isEmpty()) { NoppesUtilPlayer.sendData(EnumPlayerPacket.QuestChooseReward, quest.id); }
			else { NoppesUtilPlayer.sendData(EnumPlayerPacket.QuestCompletion, id); }
		} else if (type == EnumPacketClient.EDIT_NPC) {
			Entity entity = Minecraft.getMinecraft().world.getEntityByID(buffer.readInt());
			if (entity == null || !(entity instanceof EntityNPCInterface)) {
				NoppesUtil.setLastNpc(null);
			} else {
				NoppesUtil.setLastNpc((EntityNPCInterface) entity);
			}
		} else if (type == EnumPacketClient.PLAY_MUSIC) {
			MusicController.Instance.playMusic(Server.readString(buffer), SoundCategory.PLAYERS, player);
		} else if (type == EnumPacketClient.PLAY_SOUND) {
			MusicController.Instance.playSound(SoundCategory.VOICE, Server.readString(buffer), buffer.readInt(),
					buffer.readInt(), buffer.readInt(), buffer.readFloat(), buffer.readFloat());
		} else if (type == EnumPacketClient.UPDATE_NPC) {
			NBTTagCompound compound = Server.readNBT(buffer);
			Entity entity = Minecraft.getMinecraft().world.getEntityByID(compound.getInteger("EntityId"));
			if (entity == null || !(entity instanceof EntityNPCInterface)) {
				CustomNpcs.debugData.endDebug("Client", player, "PackageReceived_"+type.toString());
				return;
			}
			((EntityNPCInterface) entity).readSpawnData(compound);
		} else if (type == EnumPacketClient.ROLE) {
			NBTTagCompound compound = Server.readNBT(buffer);
			Entity entity = Minecraft.getMinecraft().world.getEntityByID(compound.getInteger("EntityId"));
			if (entity == null || !(entity instanceof EntityNPCInterface)) {
				CustomNpcs.debugData.endDebug("Client", player, "PackageReceived_"+type.toString());
				return;
			}
			((EntityNPCInterface) entity).advanced.setRole(compound.getInteger("Type"));
			((EntityNPCInterface) entity).advanced.roleInterface.readFromNBT(compound);
			NoppesUtil.setLastNpc((EntityNPCInterface) entity);
		} else if (type == EnumPacketClient.GUI) {
			EnumGuiType gui = EnumGuiType.values()[buffer.readInt()];
			CustomNpcs.proxy.openGui(NoppesUtil.getLastNpc(), gui, buffer.readInt(), buffer.readInt(), buffer.readInt());
		} else if (type == EnumPacketClient.PARTICLE) {
			NoppesUtil.spawnParticle(buffer);
		} else if (type == EnumPacketClient.DELETE_ENTITY) {
			Entity entity = Minecraft.getMinecraft().world.getEntityByID(buffer.readInt());
			if (entity == null || !(entity instanceof EntityLivingBase)) {
				CustomNpcs.debugData.endDebug("Client", player, "PackageReceived_"+type.toString());
				return;
			}
			if (entity instanceof EntityNPCInterface) { ((EntityNPCInterface) entity).delete(); }
			else { entity.setDead(); }
		} else if (type == EnumPacketClient.SCROLL_LIST) {
			NoppesUtil.setScrollList(buffer);
		} else if (type == EnumPacketClient.SCROLL_DATA) {
			NoppesUtil.setScrollData(buffer);
		} else if (type == EnumPacketClient.SCROLL_DATA_PART) {
			NoppesUtil.addScrollData(buffer);
		} else if (type == EnumPacketClient.SCROLL_SELECTED) {
			GuiScreen gui = Minecraft.getMinecraft().currentScreen;
			String selected = Server.readString(buffer);
			if (selected.equals("Unfreeze Npcs") || selected.equals("Freeze Npcs")) {
				if (CustomNpcs.Server.isDedicatedServer()) { CustomNpcs.FreezeNPCs = selected.equals("Freeze Npcs"); }
				if (gui instanceof GuiNpcRemoteEditor) { gui.initGui(); }
			}
			if (gui == null || !(gui instanceof IScrollData)) {
				CustomNpcs.debugData.endDebug("Client", player, "PackageReceived_"+type.toString());
				return;
			}
			((IScrollData) gui).setSelected(selected);
		} else if (type == EnumPacketClient.CLONE) {
			NBTTagCompound compound = Server.readNBT(buffer);
			NoppesUtil.openGUI(player, new GuiNpcMobSpawnerAdd(compound));
		} else if (type == EnumPacketClient.GUI_DATA) {
			GuiScreen gui = Minecraft.getMinecraft().currentScreen;
			if (gui == null) {
				CustomNpcs.debugData.endDebug("Client", player, "PackageReceived_"+type.toString());
				return;
			}
			if (gui instanceof GuiNPCInterface && ((GuiNPCInterface) gui).hasSubGui()) {
				gui = ((GuiNPCInterface) gui).getSubGui();
			} else if (gui instanceof GuiContainerNPCInterface && ((GuiContainerNPCInterface) gui).hasSubGui()) {
				gui = ((GuiContainerNPCInterface) gui).getSubGui();
			}
			if (gui instanceof IGuiData) {
				NBTTagCompound compound = Server.readNBT(buffer);
				((IGuiData) gui).setGuiData(compound);
			}
		} else if (type == EnumPacketClient.GUI_UPDATE) {
			GuiScreen gui = Minecraft.getMinecraft().currentScreen;
			if (gui == null) {
				CustomNpcs.debugData.endDebug("Client", player, "PackageReceived_"+type.toString());
				return;
			}
			gui.initGui();
		} else if (type == EnumPacketClient.GUI_ERROR) {
			GuiScreen gui = Minecraft.getMinecraft().currentScreen;
			if (gui == null || !(gui instanceof IGuiError)) {
				CustomNpcs.debugData.endDebug("Client", player, "PackageReceived_"+type.toString());
				return;
			}
			int i = buffer.readInt();
			NBTTagCompound compound = Server.readNBT(buffer);
			((IGuiError) gui).setError(i, compound);
		} else if (type == EnumPacketClient.GUI_CLOSE) {
			GuiScreen gui = Minecraft.getMinecraft().currentScreen;
			if (gui == null) {
				CustomNpcs.debugData.endDebug("Client", player, "PackageReceived_"+type.toString());
				return;
			}
			if (gui instanceof IGuiClose) {
				int i = buffer.readInt();
				NBTTagCompound compound = Server.readNBT(buffer);
				((IGuiClose) gui).setClose(i, compound);
			}
			Minecraft mc = Minecraft.getMinecraft();
			mc.displayGuiScreen((GuiScreen) null);
			mc.setIngameFocus();
		} else if (type == EnumPacketClient.VILLAGER_LIST) {
			MerchantRecipeList merchantrecipelist = MerchantRecipeList.readFromBuf(new PacketBuffer(buffer));
			ServerEventsHandler.Merchant.setRecipes(merchantrecipelist);
		} else if (type == EnumPacketClient.CONFIG) {
			int config = buffer.readInt();
			if (config == 0) {
				String font = Server.readString(buffer);
				int size = buffer.readInt();
				Runnable run = () -> {
					if (!font.isEmpty()) {
						CustomNpcs.FontType = font;
						CustomNpcs.FontSize = size;
						ClientProxy.Font.clear();
						ClientProxy.Font = new ClientProxy.FontContainer(CustomNpcs.FontType, CustomNpcs.FontSize);
						CustomNpcs.Config.updateConfig();
						player.sendMessage(new TextComponentTranslation("Font set to %s",
								new Object[] { ClientProxy.Font.getName() }));
					} else {
						player.sendMessage(new TextComponentTranslation("Current font is %s",
								new Object[] { ClientProxy.Font.getName() }));
					}
					CustomNpcs.debugData.endDebug("Client", player, "PackageReceived_"+type.toString());
					return;
				};
				Minecraft.getMinecraft().addScheduledTask(run);
			}
		}
		// New
		else if (type == EnumPacketClient.MESSAGE_DATA) {
			NBTTagCompound compound = Server.readNBT(buffer);
			TextComponentTranslation title = new TextComponentTranslation(compound.getString("Title"));
			TextComponentTranslation message = new TextComponentTranslation(compound.getString("Message"));
			if (compound.hasKey("QuestID", 3)) {
				IQuest quest = QuestController.instance.get(compound.getInteger("QuestID"));
				if (quest == null) {
					CustomNpcs.debugData.endDebug("Client", player, "PackageReceived_"+type.toString());
					return;
				}
				title = new TextComponentTranslation("quest.name");
				title.appendSibling(new TextComponentString(": "));
				title.appendSibling(new TextComponentString(quest.getTitle()));

				int[] pr = compound.getIntArray("Progress");

				if (compound.getString("Type").equalsIgnoreCase("craft")) {
					ItemStack item = new ItemStack(compound.getCompoundTag("Item"));
					message = new TextComponentTranslation(item.getDisplayName());
				} else {
					message = new TextComponentTranslation(compound.getString("TargetName"));
				}
				if (pr[0] >= pr[1]) { // is complite
					message.appendSibling(new TextComponentString(" -"));
					message.appendSibling(
							new TextComponentTranslation("quest.task." + compound.getString("Type") + ".0"));
				} else {
					message.appendSibling(new TextComponentString(" = " + pr[0] + "/" + pr[1]));
				}
			}
			Minecraft.getMinecraft().getToastGui()
					.add(new GuiAchievement(title, message, compound.getInteger("MessageType")));
		} else if (type == EnumPacketClient.NPC_VISUAL_DATA) {
			int entityID = buffer.readInt();
			Entity entity = player.world.getEntityByID(entityID);
			if (entity != null && entity instanceof EntityNPCInterface) {
				NBTTagCompound compound = Server.readNBT(buffer);
				EntityNPCInterface npcData = (EntityNPCInterface) entity;
				npcData.stats.setLevel(compound.getInteger("NPCLevel"));
				npcData.stats.setRarity(compound.getInteger("NPCRarity"));
				npcData.stats.setRarityTitle(compound.getString("RarityTitle"));
			}
		} else if (type == EnumPacketClient.SET_GHOST_RECIPE) {
			Container container = player.openContainer;
			int id = buffer.readInt();
			if (container.windowId != id || !container.getCanCraft(player)) {
				CustomNpcs.debugData.endDebug("Client", player, "PackageReceived_"+type.toString());
				return;
			}
			GuiScreen gui = Minecraft.getMinecraft().currentScreen;
			if (!(gui instanceof IRecipeShownListener)) {
				CustomNpcs.debugData.endDebug("Client", player, "PackageReceived_"+type.toString());
				return;
			}
			IRecipe recipe = CraftingManager.REGISTRY.getObjectById(buffer.readInt());
			((IRecipeShownListener) gui).func_194310_f().setupGhostRecipe(recipe, container.inventorySlots);
		} else if (type == EnumPacketClient.SET_MARCETS) {
			MarcetController mData = MarcetController.getInstance();
			mData.loadMarcets(Server.readNBT(buffer));
			GuiScreen gui = Minecraft.getMinecraft().currentScreen;
			if (gui == null) {
				CustomNpcs.debugData.endDebug("Client", player, "PackageReceived_"+type.toString());
				return;
			}
			if (gui instanceof GuiNPCInterface && ((GuiNPCInterface) gui).hasSubGui()) {
				gui = ((GuiNPCInterface) gui).getSubGui();
			} else if (gui instanceof GuiContainerNPCInterface && ((GuiContainerNPCInterface) gui).hasSubGui()) {
				gui = ((GuiContainerNPCInterface) gui).getSubGui();
			}
			if (gui instanceof IGuiData) {
				((IGuiData) gui).setGuiData(mData.getNBT());
			}
		} else if (type == EnumPacketClient.MARCET_UPDATE) {
			NBTTagCompound compound = Server.readNBT(buffer);
			Marcet m = MarcetController.getInstance().getMarcet(compound.getInteger("MarcetID"));
			if (m != null) {
				m.readEntityFromNBT(compound);
				m.addListener(player, false);
			}
			GuiScreen gui = Minecraft.getMinecraft().currentScreen;
			if (gui instanceof GuiNPCTrader) {
				((IGuiData) gui).setGuiData(compound);
			}
		} else if (type == EnumPacketClient.MARCET_CLOSE) {
			Marcet m = MarcetController.getInstance().getMarcet(buffer.readInt());
			if (m != null) {
				m.removeListener(player, false);
			}
			GuiScreen gui = Minecraft.getMinecraft().currentScreen;
			if (gui instanceof GuiNPCTrader) {
				gui.onGuiClosed();
			}
		} else if (type == EnumPacketClient.SCRIPT_DATA) {
			GuiScreen gui = Minecraft.getMinecraft().currentScreen;
			if (gui instanceof GuiScriptInterface) {
				// ((GuiScriptInterface) gui).setData(Server.readNBT(buffer));
			}
		} else if (type == EnumPacketClient.DETECT_HELD_ITEM) {
			try {
				player.inventory.setInventorySlotContents(buffer.readInt(), new ItemStack(Server.readNBT(buffer)));
				return;
			} catch (Exception e) { }
			ItemStack held = new ItemStack(Server.readNBT(buffer));
			player.inventory.setItemStack(held);
		} else if (type == EnumPacketClient.BORDER_DATA) {
			int id = buffer.readInt();
			if (id==-1) {
				BorderController.getInstance().regions.clear();
				return;
			}
			else if (id==-2) {
				GuiScreen gui = Minecraft.getMinecraft().currentScreen;
				if (gui instanceof GuiNPCInterface) {
					((GuiNPCInterface) gui).initGui();
				}
				return;
			}
			BorderController.getInstance().loadRegion(Server.readNBT(buffer));
			
		} else if (type == EnumPacketClient.BUILDER_SETTING) {
			NBTTagCompound compound = Server.readNBT(buffer);
			if (!CommonProxy.dataBuilder.containsKey(compound.getInteger("ID"))) { CommonProxy.dataBuilder.put(compound.getInteger("ID"), new BuilderData()); }
			CommonProxy.dataBuilder.get(compound.getInteger("ID")).read(compound);
		} else if (type == EnumPacketClient.SAVE_SCHEMATIC) {
			NBTTagCompound compound = Server.readNBT(buffer);
			String name = compound.getString("Name")+".schematic";
			Schematic schema = new Schematic(name);
			schema.load(compound);
			schema.save(player);
			SchematicController.Instance.map.put(name.toLowerCase(), new SchematicWrapper(schema));
		} else if (type == EnumPacketClient.GET_SCHEMATIC) {
			if (ClientEventHandler.schemaPos == null || ClientEventHandler.schema==null) { return; }
			int x = ClientEventHandler.schemaPos.getX();
			int y = ClientEventHandler.schemaPos.getY();
			int z = ClientEventHandler.schemaPos.getZ();
			Client.sendData(EnumPacketServer.SchematicsBuild, x, y, z, ClientEventHandler.rotaion, ClientEventHandler.schema.getNBT());
		} else if (type == EnumPacketClient.UPDATE_HUD) {
			NBTTagCompound compound = Server.readNBT(buffer);
			ClientProxy.playerData.hud.loadNBTData(compound);
		} else if (type == EnumPacketClient.DIMENSIOS_IDS) {
			ClientHandler.getInstance().sync(Server.readIntArray(buffer));
		} else if (type == EnumPacketClient.DROP_GROUP_DATA) {
			NBTTagCompound nbtTemplate = Server.readNBT(buffer);
			if (nbtTemplate.getKeySet().size()==0) {
				DropController.getInstance().templates.clear();
				return;
			}
			if (!nbtTemplate.hasKey("Name", 8)) { return; }
			DropController.getInstance().templates.put(nbtTemplate.getString("Name"), new DropsTemplate(nbtTemplate.getCompoundTag("Groups")));
		} else if (type == EnumPacketClient.SET_TILE_DATA) {
			NBTTagCompound compound = Server.readNBT(buffer);
			TileEntity tile = player.world.getTileEntity(new BlockPos(compound.getInteger("x"), compound.getInteger("y"), compound.getInteger("z")));
			if (tile!=null) { tile.readFromNBT(compound); }
		} else if (type == EnumPacketClient.UPDATE_NPC_ANIMATION) {
			int t = buffer.readInt();
			NBTTagCompound compound = Server.readNBT(buffer);
			Entity entity = Minecraft.getMinecraft().world.getEntityByID(compound.getInteger("EntityId"));
			if (!(entity instanceof EntityNPCInterface)) {
				CustomNpcs.debugData.endDebug("Client", player, "PackageReceived_"+type.toString());
				return;
			}
			DataAnimation anim = ((EntityNPCInterface) entity).animation;
			switch(t) {
				case 0: ((EntityNPCInterface) entity).animation.readFromNBT(compound); break; // reload
				case 1: ((EntityNPCInterface) entity).animation.stopAnimation(); break; // stopAnimation
				case 2: // start
					int animationType = -1, variant = -1;
					if (compound.hasKey("Vars", 11)) {
						int[] vars = compound.getIntArray("Vars");
						if (vars.length>=1) { animationType = vars[0]; }
						if (vars.length>=2) { variant = vars[1]; }
					}
					if (animationType < 0 || animationType >= EnumAnimationType.values().length ) { return; }
					anim.startAnimation(animationType, variant);
					break;
				case 3: // startAnimationFromSaved
					if (!compound.hasKey("CustomAnim", 10)) { return; }
					AnimationConfig ac = new AnimationConfig(0);
					ac.readFromNBT(compound.getCompoundTag("CustomAnim"));
					((EntityNPCInterface) entity).animation.activeAnim = ac;
					break;
					
			}
		} else if (type == EnumPacketClient.SCRIPT_PACKAGE) {
			EventHooks.onScriptPackage(player, Server.readNBT(buffer));
		}
		CustomNpcs.debugData.endDebug("Client", player, "PackageReceived_"+type.toString());
	}
	
}
