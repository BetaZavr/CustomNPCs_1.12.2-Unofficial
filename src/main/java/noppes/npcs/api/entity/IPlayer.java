package noppes.npcs.api.entity;

import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IPos;
import noppes.npcs.api.ITimers;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.data.IPlayerMail;
import noppes.npcs.api.entity.data.IPlayerMiniMap;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.gui.IOverlayHUD;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.item.IItemStack;

public interface IPlayer<T extends EntityPlayer> extends IEntityLivingBase<T> {

	void addDialog(int id);

	void addFactionPoints(int faction, int points);

	void addMoney(long value);

	void cameraShakingPlay(int time, int amplitude, int type, boolean isFading);

	void cameraShakingStop();

	boolean canQuestBeAccepted(int id);

	void clearData();

	void closeGui();

	void completeQuest(int id);

	int factionStatus(int id);

	boolean finishQuest(int id);

	IQuest[] getActiveQuests();

	IContainer getBubblesInventory();

	ICustomGui getCustomGui();

	String getDisplayName();

	int getExpLevel();

	int getFactionPoints(int id);

	IQuest[] getFinishedQuests();

	int getGamemode();

	int getHunger();

	IContainer getInventory();

	IItemStack getInventoryHeldItem();

	int[] getKeyPressed();

	String getLanguage();

	T getMCEntity();

	IPlayerMiniMap getMiniMapData();

	long getMoney();

	int[] getMousePressed();

	IContainer getOpenContainer();

	IOverlayHUD getOverlayHUD();

	Object getPixelmonData();

	String getSkinType(int type);

	IBlock getSpawnPoint();

	ITimers getTimers();

	double[] getWindowSize();

	boolean giveItem(IItemStack item);

	boolean giveItem(String id, int damage, int amount);

	boolean hasAchievement(String achievement);

	boolean hasActiveQuest(int id);

	boolean hasFinishedQuest(int id);

	boolean hasMousePress(int key);

	boolean hasOrKeyPressed(int[] key);

	boolean hasPermission(String permission);

	boolean hasReadDialog(int id);

	@Deprecated
	int inventoryItemCount(IItemStack item);

	int inventoryItemCount(IItemStack stack, boolean ignoreDamage, boolean ignoreNBT);

	@Deprecated
	int inventoryItemCount(String id, int amount);

	boolean isComleteQuest(int id);

	boolean isMoved();

	void kick(String message);

	void message(String message);

	void playSound(int categoryType, IPos pos, String sound, float volume, float pitch);

	void playSound(String sound, float volume, float pitch);

	void removeAllItems(IItemStack item);

	void removeDialog(int id);

	boolean removeItem(IItemStack item, int amount);

	boolean removeItem(String id, int damage, int amount);

	void removeQuest(int id);

	void resetSpawnpoint();

	void sendMail(IPlayerMail mail);

	void sendNotification(String title, String message, int type);

	void sendTo(INbt nbt);

	void setExpLevel(int level);

	void setGamemode(int mode);

	void setHunger(int level);

	void setMoney(long value);

	void setSkin(boolean isSmallArms, int body, int bodyColor, int hair, int hairColor, int face, int eyesColor,
			int leg, int jacket, int shoes, int... peculiarities);

	void setSkinType(String location, int type);

	void setSpawnpoint(int x, int y, int z);

	void setSpawnPoint(IBlock block);

	@Deprecated
	IContainer showChestGui(int rows);

	void showCustomGui(ICustomGui gui);

	void showDialog(int id, String name);

	void startQuest(int id);

	void stopQuest(int id);

	void stopSound(int categoryType, String sound);

	void trigger(int id, Object... arguments);

	void updatePlayerInventory();

}
