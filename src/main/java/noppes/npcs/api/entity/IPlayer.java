package noppes.npcs.api.entity;

import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.ITimers;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.data.IPlayerMail;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.gui.IOverlayHUD;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.item.IItemStack;

public interface IPlayer<T extends EntityPlayerMP>
extends IEntityLivingBase<T> {
	
	void addDialog(int id);

	void addFactionPoints(int faction, int points);

	boolean canQuestBeAccepted(int id);

	void clearData();

	void closeGui();

	int factionStatus(int id);

	boolean finishQuest(int id);

	IQuest[] getActiveQuests();

	ICustomGui getCustomGui();

	String getDisplayName();

	int getExpLevel();

	int getFactionPoints(int id);

	IQuest[] getFinishedQuests();

	int getGamemode();

	int getHunger();

	IContainer getInventory();

	IItemStack getInventoryHeldItem();

	T getMCEntity();

	IContainer getOpenContainer();

	Object getPixelmonData();

	IBlock getSpawnPoint();

	ITimers getTimers();

	boolean giveItem(IItemStack item);

	boolean giveItem(String id, int damage, int amount);

	boolean hasAchievement(String achievement);

	boolean hasActiveQuest(int id);
	
	boolean isComleteQuest(int id);

	boolean hasFinishedQuest(int id);

	boolean hasPermission(String permission);

	boolean hasReadDialog(int id);

	@Deprecated
	int inventoryItemCount(IItemStack item);

	int inventoryItemCount(IItemStack stack, boolean ignoreDamage, boolean ignoreNBT);

	@Deprecated
	int inventoryItemCount(String id, int amount);

	void kick(String message);

	void message(String message);

	void playSound(String sound, float volume, float pitch);

	void removeAllItems(IItemStack item);

	void removeDialog(int id);

	boolean removeItem(IItemStack item, int amount);

	boolean removeItem(String id, int damage, int amount);

	void removeQuest(int id);

	void resetSpawnpoint();

	void sendMail(IPlayerMail mail);

	void sendNotification(String title, String message, int type);

	void setExpLevel(int level);

	void setGamemode(int mode);

	void setHunger(int level);

	void setSpawnpoint(int x, int y, int z);

	void setSpawnPoint(IBlock block);

	@Deprecated
	IContainer showChestGui(int rows);

	void showCustomGui(ICustomGui gui);

	void showDialog(int id, String name);

	void startQuest(int id);

	void stopQuest(int id);

	void updatePlayerInventory();

	boolean isMoved();

	void addMoney(long value);

	long getMoney();

	void setMoney(long value);

	int[] getKeyPressed();

	boolean hasKeyPressed(int key);

	int[] getMousePressed();

	boolean hasMousePress(int key);
	
	void completeQuest(int id);

	IOverlayHUD getOverlayHUD();

	void trigger(int id, Object ... arguments);

	String getLanguage();
	
}
