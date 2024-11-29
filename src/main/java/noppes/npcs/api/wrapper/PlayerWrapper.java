package noppes.npcs.api.wrapper;

import java.lang.reflect.Method;
import java.util.*;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.SoundEvents;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.stats.StatBase;
import net.minecraft.stats.StatList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.WorldSettings;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.Server;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IRayTrace;
import noppes.npcs.api.ITimers;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.constants.EntityType;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.entity.data.IPixelmonPlayerData;
import noppes.npcs.api.entity.data.IPlayerMail;
import noppes.npcs.api.entity.data.IPlayerMiniMap;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.gui.IOverlayHUD;
import noppes.npcs.api.handler.data.IMarcet;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.gui.CustomGuiWrapper;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.controllers.*;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerDialogData;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.Quest;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.entity.EntityDialogNpc;
import noppes.npcs.api.mixin.entity.player.IEntityPlayerMPMixin;
import noppes.npcs.util.Util;
import noppes.npcs.util.CustomNPCsScheduler;
import noppes.npcs.util.ValueUtil;

@SuppressWarnings("rawtypes")
public class PlayerWrapper<T extends EntityPlayer> extends EntityLivingBaseWrapper<T> implements IPlayer {

	public static Map<String, WrapperEntityData> map = new HashMap<>();
	private PlayerData data;
	private IContainer inventory;
	private Object pixelmonPartyStorage;

	private Object pixelmonPCStorage;

	public PlayerWrapper(T player) {
		super(player);
	}

	@Override
	public void addDialog(int id) {
		PlayerData data = this.getData();
		data.dialogData.dialogsRead.add(id);
		data.updateClient = true;
	}

	@Override
	public void addFactionPoints(int faction, int points) {
		PlayerData data = this.getData();
		data.factionData.increasePoints(this.entity, faction, points);
		data.save(true);
	}

	@Override
	public void addMoney(long money) {
		this.getData().game.addMoney(money);
	}

	@Override
	public void cameraShakingPlay(int time, int amplitude, int type, boolean isFading) {
		if (time <= 1 || time > 1200) {
			throw new CustomNPCsException("Camera shake time should be between 1 and 1200 ticks. You have: " + time);
		}
		if (amplitude <= 1 || amplitude > 25) {
			throw new CustomNPCsException("Amplitude should be between 1 and 25 value. You have: " + amplitude);
		}
		if (type < 0 || type > 5) {
			throw new CustomNPCsException("Type should be between 0 and 5 value. You have: " + type);
		}
		Server.sendData((EntityPlayerMP) this.entity, EnumPacketClient.PLAY_CAMERA_SHAKING, time, amplitude, type,
				isFading);
	}

	@Override
	public void cameraShakingStop() {
		Server.sendData((EntityPlayerMP) this.entity, EnumPacketClient.STOP_CAMERA_SHAKING);
	}

	@Override
	public boolean canQuestBeAccepted(int questId) {
		return PlayerQuestController.canQuestBeAccepted(this.entity, questId);
	}

	@Override
	public void clearData() {
		PlayerData data = this.getData();
		data.setNBT(new NBTTagCompound());
		data.save(true);
	}

	@Override
	public void closeGui() {
		if (!(this.entity instanceof EntityPlayerMP)) {
			return;
		}
		((EntityPlayerMP) this.entity).closeContainer();
		Server.sendData(((EntityPlayerMP) this.entity), EnumPacketClient.GUI_CLOSE, -1, new NBTTagCompound());
	}

	@Override
	public void completeQuest(int id) {
		Quest quest = QuestController.instance.quests.get(id);
		if (quest == null) {
			return;
		}
		PlayerData data = this.getData();
		data.questData.finishedQuests.put(id, System.currentTimeMillis());
        data.questData.activeQuests.remove(id);
		if (this.entity instanceof EntityPlayerMP) {
			Server.sendData((EntityPlayerMP) this.entity, EnumPacketClient.MESSAGE, "quest.completed", quest.getTitle(),
					2);
			Server.sendData((EntityPlayerMP) this.entity, EnumPacketClient.CHAT, "quest.completed", ": ",
					quest.getTitle());
		}
		Server.sendData((EntityPlayerMP) this.entity, EnumPacketClient.QUEST_COMPLETION, id);
	}

	@Override
	public int factionStatus(int factionId) {
		Faction faction = FactionController.instance.getFaction(factionId);
		if (faction == null) {
			throw new CustomNPCsException("Unknown faction: " + factionId);
		}
		return faction.playerStatus(this);
	}

	@Override
	public boolean finishQuest(int id) {
		Quest quest = QuestController.instance.quests.get(id);
		if (quest == null) {
			return false;
		}
		PlayerData data = this.getData();
		boolean hasFinishedQuest = data.questData.finishedQuests.containsKey(id);
		data.questData.finishedQuests.put(id, System.currentTimeMillis());
		if (data.questData.activeQuests.containsKey(id)) {
			data.questData.activeQuests.remove(id);
			hasFinishedQuest = false;
		}
		if (!hasFinishedQuest && this.entity instanceof EntityPlayerMP) {
			Server.sendData((EntityPlayerMP) this.entity, EnumPacketClient.MESSAGE, "quest.completed", quest.getTitle(),
					2);
			Server.sendData((EntityPlayerMP) this.entity, EnumPacketClient.CHAT, "quest.completed", ": ",
					quest.getTitle());
		}
		data.updateClient = true;
		return !hasFinishedQuest;
	}

	@Override
	public IQuest[] getActiveQuests() {
		PlayerQuestData data = this.getData().questData;
		List<IQuest> quests = new ArrayList<>();
		for (int id : data.activeQuests.keySet()) {
			IQuest quest = QuestController.instance.quests.get(id);
			if (quest != null) {
				quests.add(quest);
			}
		}
		return quests.toArray(new IQuest[0]);
	}

	@Override
	public IContainer getBubblesInventory() {
		IContainer invBubbles = null;
		try {
			Class<?> apiBubbles = Class.forName("baubles.api.BaublesApi");
			for (Method m : apiBubbles.getDeclaredMethods()) {
				if (m.getName().equals("getBaubles")) {
					if (!m.isAccessible()) {
						m.setAccessible(true);
					}
					invBubbles = new ContainerWrapper((IInventory) m.invoke(apiBubbles, this.entity));
					break;
				}
			}
		} catch (Exception e) {
			LogWriter.warn("Mod \"Bubbles\" - not found");
		}
		return invBubbles;
	}

	@Override
	public ICustomGui getCustomGui() {
		if (this.entity.openContainer instanceof ContainerCustomGui) {
			return ((ContainerCustomGui) this.entity.openContainer).customGui;
		}
		return null;
	}

	public PlayerData getData() {
		if (this.data == null) {
			this.data = PlayerData.get(this.entity);
		}
		return this.data;
	}

	@Override
	public String getDisplayName() {
		return this.entity.getDisplayNameString();
	}

	@Override
	public int getExpLevel() {
		return this.entity.experienceLevel;
	}

	@Override
	public int getFactionPoints(int faction) {
		return this.getData().factionData.getFactionPoints(this.entity, faction);
	}

	@Override
	public IQuest[] getFinishedQuests() {
		PlayerQuestData data = this.getData().questData;
		List<IQuest> quests = new ArrayList<>();
		for (int id : data.finishedQuests.keySet()) {
			IQuest quest = QuestController.instance.quests.get(id);
			if (quest != null) {
				quests.add(quest);
			}
		}
		return quests.toArray(new IQuest[0]);
	}

	@Override
	public int getGamemode() {
		if (!(this.entity instanceof EntityPlayerMP)) {
			if (this.entity != null && this.entity.capabilities.isCreativeMode) {
				return 1;
			}
			return 0;
		}
		return ((EntityPlayerMP) this.entity).interactionManager.getGameType().getID();
	}

	@Override
	public int getHunger() {
		return this.entity.getFoodStats().getFoodLevel();
	}

	@Override
	public IContainer getInventory() {
		if (this.inventory == null) {
			this.inventory = new ContainerWrapper(this.entity.inventory);
		}
		return this.inventory;
	}

	@Override
	public IItemStack getInventoryHeldItem() {
		return Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(this.entity.inventory.getItemStack());
	}

	// New
	@Override
	public int[] getKeyPressed() {
		return this.getData().hud.getKeyPressed();
	}

	@Override
	public String getLanguage() {
		if (!(this.entity instanceof EntityPlayerMP)) { return "en_en"; }
		return ((IEntityPlayerMPMixin) this.entity).npcs$getLanguage();
	}

	@Override
	public IPlayerMiniMap getMiniMapData() {
		return data.minimap;
	}

	@Override
	public long getMoney() {
		return this.getData().game.getMoney();
	}

	@Override
	public int[] getMousePressed() {
		return this.getData().hud.getMousePressed();
	}

	@Override
	public String getName() {
		return this.entity.getName();
	}

	@Override
	public IContainer getOpenContainer() {
		return Objects.requireNonNull(NpcAPI.Instance()).getIContainer(this.entity.openContainer);
	}

	@Override
	public IOverlayHUD getOverlayHUD() {
		return this.getData().hud;
	}

	@Override
	public IPixelmonPlayerData getPixelmonData() {
		if (!PixelmonHelper.Enabled) {
			throw new CustomNPCsException("Pixelmon not installed");
		}
		return new IPixelmonPlayerData() {
			@Override
			public Object getParty() {
				if (PlayerWrapper.this.pixelmonPartyStorage == null) {
					PlayerWrapper.this.pixelmonPartyStorage = PixelmonHelper
							.getParty((EntityPlayerMP) PlayerWrapper.this.entity);
				}
				return PlayerWrapper.this.pixelmonPartyStorage;
			}

			@Override
			public Object getPC() {
				if (PlayerWrapper.this.pixelmonPCStorage == null) {
					PlayerWrapper.this.pixelmonPCStorage = PixelmonHelper
							.getPc((EntityPlayerMP) PlayerWrapper.this.entity);
				}
				return PlayerWrapper.this.pixelmonPCStorage;
			}
		};
	}

	public IEntity<?> getRidingEntity() {
		if (this.entity.getRidingEntity() == null) {
			return null;
		}
		return Objects.requireNonNull(NpcAPI.Instance()).getIEntity(this.entity.getRidingEntity());
	}

	@Override
	public String getSkinType(int type) {
		return PlayerSkinController.getInstance().get((EntityPlayerMP) entity, type);
	}

	@Override
	public IBlock getSpawnPoint() {
        return Objects.requireNonNull(NpcAPI.Instance()).getIBlock(this.entity.world, this.entity.getBedLocation());
	}

	@Override
	public IData getStoreddata() {
		return super.getStoreddata();
	}

	@Override
	public ITimers getTimers() {
		return this.getData().timers;
	}

	@Override
	public int getType() {
		return EntityType.PLAYER.get();
	}

	@Override
	public double[] getWindowSize() {
		return this.data.hud.getWindowSize();
	}

	@Override
	public boolean giveItem(IItemStack item) {
		ItemStack mcItem = item.getMCItemStack();
		if (mcItem.isEmpty()) {
			return false;
		}
		boolean bo = this.entity.inventory.addItemStackToInventory(mcItem.copy());
		if (bo) {
			NoppesUtilServer.playSound(this.entity, SoundEvents.ENTITY_ITEM_PICKUP, 0.2f,
					((this.entity.getRNG().nextFloat() - this.entity.getRNG().nextFloat()) * 0.7f + 1.0f) * 2.0f);
			this.updatePlayerInventory();
		}
		return bo;
	}

	@Override
	public boolean giveItem(String id, int damage, int amount) {
		Item item = Item.REGISTRY.getObject(new ResourceLocation(id));
		if (item == null) {
			return false;
		}
		ItemStack mcStack = new ItemStack(item);
		IItemStack itemStack = Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(mcStack);
		itemStack.setStackSize(amount);
		itemStack.setItemDamage(damage);
		return this.giveItem(itemStack);
	}

	@Override
	public boolean hasAchievement(String achievement) {
		StatBase statbase = StatList.getOneShotStat(achievement);
        return statbase != null && statbase.isIndependent;
	}

	@Override
	public boolean hasActiveQuest(int id) {
		PlayerQuestData data = this.getData().questData;
		return data.activeQuests.containsKey(id);
	}

	@Override
	public boolean hasFinishedQuest(int id) {
		PlayerQuestData data = this.getData().questData;
		return data.finishedQuests.containsKey(id);
	}

	@Override
	public boolean hasMousePress(int key) {
		return this.getData().hud.hasMousePress(key);
	}

	@Override
	public boolean hasOrKeyPressed(int[] key) {
		return this.getData().hud.hasOrKeysPressed(key);
	}

	@Override
	public boolean hasPermission(String permission) {
		return CustomNpcsPermissions.hasPermissionString(this.entity, permission);
	}

	@Override
	public boolean hasReadDialog(int id) {
		PlayerDialogData data = this.getData().dialogData;
		return data.dialogsRead.contains(id);
	}

	@Override
	public int inventoryItemCount(IItemStack item) {
		int count = 0;
		for (int i = 0; i < this.entity.inventory.getSizeInventory(); ++i) {
			ItemStack is = this.entity.inventory.getStackInSlot(i);
			if (this.isItemEqual(item.getMCItemStack(), is)) {
				count += is.getCount();
			}
		}
		return count;
	}

	@Override
	public int inventoryItemCount(IItemStack stack, boolean ignoreDamage, boolean ignoreNBT) {
		return Util.instance.inventoryItemCount(this.entity, stack.getMCItemStack(), null, ignoreDamage, ignoreNBT);
	}

	@Deprecated
	@Override
	public int inventoryItemCount(String id, int damage) {
		Item item = Item.REGISTRY.getObject(new ResourceLocation(id));
		if (item == null) {
			throw new CustomNPCsException("Unknown item id: " + id);
		}
		return this.inventoryItemCount(Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(new ItemStack(item, 1, damage)));
	}

	@Override
	public boolean isCompleteQuest(int id) {
		PlayerQuestData data = this.getData().questData;
		if (data.finishedQuests.containsKey(id)) {
			return true;
		}
		if (!data.activeQuests.containsKey(id)) {
			return false;
		}
		QuestData qData = data.activeQuests.get(id);
		if (qData.isCompleted) {
			return true;
		}
		Quest quest = (Quest) Objects.requireNonNull(NpcAPI.Instance()).getQuests().get(id);
		return quest.questInterface.isCompleted(this.getMCEntity());
	}

	private boolean isItemEqual(ItemStack stack, ItemStack other) {
		return !other.isEmpty() && stack.getItem() == other.getItem()
				&& (stack.getItemDamage() < 0 || stack.getItemDamage() == other.getItemDamage());
	}

	@Override
	public boolean isMoved() {
		return this.getData().hud.isMoved();
	}

	@Override
	public void kick(String message) {
		if (!(this.entity instanceof EntityPlayerMP)) {
			return;
		}
		((EntityPlayerMP) this.entity).connection.disconnect(new TextComponentTranslation(message));
	}

	public void message(ITextComponent message) {
		this.entity.sendMessage(message);
	}

	@Override
	public void message(String message) {
		this.entity.sendMessage(new TextComponentTranslation(NoppesStringUtils.formatText(message, this.entity)));
	}

	@Override
	public void playSound(int categoryType, IPos pos, String sound, float volume, float pitch) {
		if (!(this.entity instanceof EntityPlayerMP) || sound == null || sound.isEmpty()) {
			return;
		}
		BlockPos p = this.entity.getPosition();
		if (pos != null) {
			p = pos.getMCBlockPos();
		}
		Server.sendData((EntityPlayerMP) this.entity, EnumPacketClient.FORCE_PLAY_SOUND, categoryType, sound, p.getX(),
				p.getY(), p.getZ(), volume, pitch);
	}

	@Override
	public void playSound(String sound, float volume, float pitch) {
		if (!(this.entity instanceof EntityPlayerMP) || sound == null || sound.isEmpty()) {
			return;
		}
		BlockPos pos = this.entity.getPosition();
		Server.sendData((EntityPlayerMP) this.entity, EnumPacketClient.PLAY_SOUND, sound, pos.getX(), pos.getY(),
				pos.getZ(), volume, pitch);
	}

	@Override
	public void removeAllItems(IItemStack item) {
		for (int i = 0; i < this.entity.inventory.getSizeInventory(); ++i) {
			ItemStack is = this.entity.inventory.getStackInSlot(i);
			if (is.isItemEqual(item.getMCItemStack())) {
				this.entity.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
			}
		}
	}

	@Override
	public void removeDialog(int id) {
		PlayerData data = this.getData();
		data.dialogData.dialogsRead.remove(id);
		data.updateClient = true;
	}

	@Override
	public boolean removeItem(IItemStack item, int amount) {
		int count = this.inventoryItemCount(item);
		if (amount > count) {
			return false;
		}
		if (count == amount) {
			this.removeAllItems(item);
		} else {
			for (int i = 0; i < this.entity.inventory.getSizeInventory(); ++i) {
				ItemStack is = this.entity.inventory.getStackInSlot(i);
				if (this.isItemEqual(item.getMCItemStack(), is)) {
					if (amount < is.getCount()) {
						is.splitStack(amount);
						break;
					}
					this.entity.inventory.setInventorySlotContents(i, ItemStack.EMPTY);
					amount -= is.getCount();
				}
			}
		}
		this.updatePlayerInventory();
		return true;
	}

	@Override
	public boolean removeItem(String id, int damage, int amount) {
		Item item = Item.REGISTRY.getObject(new ResourceLocation(id));
		if (item == null) {
			throw new CustomNPCsException("Unknown item id: " + id);
		}
		return this.removeItem(Objects.requireNonNull(NpcAPI.Instance()).getIItemStack(new ItemStack(item, 1, damage)), amount);
	}

	@Override
	public void removeQuest(int id) {
		Quest quest = QuestController.instance.quests.get(id);
		if (quest == null) {
			return;
		}
		PlayerData data = this.getData();
		data.questData.activeQuests.remove(id);
		data.questData.finishedQuests.remove(id);
		data.updateClient = true;
	}

	@Override
	public void resetSpawnpoint() {
		this.entity.setSpawnPoint(this.entity.world.getSpawnPoint(), false);
	}

	@Override
	public void sendMail(IPlayerMail mail) {
		PlayerDataController.instance.addPlayerMessage(this.entity.world.getMinecraftServer(), this.entity.getName(), (PlayerMail) mail);
	}

	@Override
	public void sendNotification(String title, String msg, int type) {
		if (!(this.entity instanceof EntityPlayerMP)) {
			return;
		}
		if (type < 0 || type > 3) {
			throw new CustomNPCsException("Wrong type value given " + type);
		}
		Server.sendData((EntityPlayerMP) this.entity, EnumPacketClient.MESSAGE, title, msg, type);
	}

	@Override
	public void sendTo(INbt nbt) {
		CustomNPCsScheduler.runTack(() -> {
			if (this.entity instanceof EntityPlayerMP) {
				Server.sendData((EntityPlayerMP) this.entity, EnumPacketClient.SCRIPT_PACKAGE, nbt.getMCNBT());
			} else {
				NoppesUtilPlayer.sendData(EnumPlayerPacket.ScriptPackage, nbt.getMCNBT());
			}
		}, 10);
	}

	@Override
	public void setExpLevel(int level) {
		this.entity.experienceLevel = level;
		this.entity.addExperienceLevel(0);
	}

	@Override
	public void setGamemode(int type) {
		this.entity.setGameType(WorldSettings.getGameTypeById(type));
	}

	@Override
	public void setHunger(int level) {
		this.entity.getFoodStats().setFoodLevel(level);
	}

	@Override
	public void setMoney(long money) {
		this.getData().game.setMoney(money);
	}

	@Override
	public void setPos(IPos pos) {
		this.setPosition(pos.getX(), pos.getY(), pos.getZ());
	}

	@Override
	public void setPosition(double x, double y, double z) {
		if (!(this.entity instanceof EntityPlayerMP)) {
			return;
		}
		NoppesUtilPlayer.teleportPlayer((EntityPlayerMP) this.entity, x, y, z, this.entity.dimension,
				this.entity.rotationYaw, this.entity.rotationPitch);
	}

	@Override
	public void setSkin(boolean isSmallArms, int body, int bodyColor, int hair, int hairColor, int face, int eyesColor,
			int leg, int jacket, int shoes, int... peculiarities) {
		PlayerSkinController.getInstance().set((EntityPlayerMP) entity, isSmallArms, body, bodyColor, hair, hairColor,
				face, eyesColor, leg, jacket, shoes, peculiarities);
	}

	@Override
	public void setSkinType(String location, int type) {
		PlayerSkinController.getInstance().set((EntityPlayerMP) entity, location, type);
	}

	@Override
	public void setSpawnpoint(int x, int y, int z) {
		x = ValueUtil.correctInt(x, -30000000, 30000000);
		z = ValueUtil.correctInt(z, -30000000, 30000000);
		y = ValueUtil.correctInt(y, 0, 256);
		this.entity.setSpawnPoint(new BlockPos(x, y, z), true);
	}

	@Override
	public void setSpawnPoint(IBlock block) {
		this.entity.setSpawnPoint(new BlockPos(block.getX(), block.getY(), block.getZ()), true);
	}

	@Deprecated
	@Override
	public IContainer showChestGui(int rows) {
		ScriptContainer current = ScriptContainer.Current;
		this.entity.closeScreen();
		this.entity.openGui(CustomNpcs.instance, EnumGuiType.CustomChest.ordinal(), this.entity.world, rows, 0, 0);
		ContainerCustomChestWrapper container = (ContainerCustomChestWrapper) Objects.requireNonNull(NpcAPI.Instance()).getIContainer(this.entity.openContainer);
		container.script = current;
		return container;
	}

	@Override
	public void showCustomGui(ICustomGui gui) {
		CustomGuiController.openGui(this, (CustomGuiWrapper) gui);
	}

	@Override
	public void showDialog(int id, String name) {
		Dialog dialog = DialogController.instance.dialogs.get(id);
		if (dialog == null) {
			throw new CustomNPCsException("Unknown Dialog id: " + id);
		}
		if (!dialog.availability.isAvailable(this.entity)) {
			return;
		}
		EntityDialogNpc npc = new EntityDialogNpc(this.getWorld().getMCWorld());
		npc.display.setName(name);
		EntityUtil.Copy(this.entity, npc);
		npc.dialogs = new int[] { id };
		NoppesUtilServer.openDialog(this.entity, npc, dialog);
	}

	@Override
	public void startQuest(int id) {
		if (!(this.entity instanceof EntityPlayerMP)) {
			return;
		}
		Quest quest = QuestController.instance.quests.get(id);
		if (quest == null) {
			return;
		}
		PlayerQuestController.addActiveQuest(quest, entity, true);
	}

	@Override
	public void stopQuest(int id) {
		Quest quest = QuestController.instance.quests.get(id);
		if (quest == null) {
			return;
		}
		PlayerData data = this.getData();
		data.questData.activeQuests.remove(id);
		data.updateClient = true;
	}

	@Override
	public void stopSound(int categoryType, String sound) {
		if (!(this.entity instanceof EntityPlayerMP)) {
			return;
		}
		if (sound == null) {
			sound = "";
		}
		if (categoryType < 0) {
			categoryType = -1;
		}
		Server.sendData((EntityPlayerMP) this.entity, EnumPacketClient.STOP_SOUND, sound, categoryType);
	}

	@Override
	public void trigger(int id, Object... arguments) {
		EventHooks.onScriptTriggerEvent(this.getData().scriptData, id, this.getWorld(), this.getPos(), this, arguments);
	}

	@Override
	public boolean typeOf(int type) {
		return type == EntityType.PLAYER.get() || super.typeOf(type);
	}

	@Override
	public void updatePlayerInventory() {
		this.entity.inventoryContainer.detectAndSendChanges();
		PlayerQuestData playerdata = this.getData().questData;
		for (QuestData data : playerdata.activeQuests.values()) { // Changed
			for (IQuestObjective obj : data.quest
					.getObjectives((IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(this.entity))) {
				if (obj.getType() != 0) {
					continue;
				}
				playerdata.checkQuestCompletion(this.entity, data);
			}
		}
	}

	@Override
	public IEntity getLookingEntity() {
		Entity target = Util.instance.getLookEntity(this.entity, null);
		return target == null ? null : Objects.requireNonNull(NpcAPI.Instance()).getIEntity(target);
	}

	@Override
	public IBlock getLookingBlock() {
		IRayTrace rt = this.rayTraceBlock(this.data.game.blockReachDistance, false, false);
		if (rt.getBlock() == null) { return null; }
		return rt.getBlock();
	}

	@Override
	public double getBlockReachDistance() { return this.data.game.blockReachDistance; }

	@Override
	public void showMarket(int marcetID) {
		IMarcet market = MarcetController.getInstance().getMarcet(marcetID);
		if (market != null) {
			Server.sendDataChecked((EntityPlayerMP) entity, EnumPacketClient.GUI, EnumGuiType.PlayerTrader.ordinal(), marcetID, 0, 0);
		}
	}

}
