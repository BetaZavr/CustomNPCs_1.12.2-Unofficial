package noppes.npcs.api.entity;

import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.ITimers;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.data.IPlayerMail;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.item.IItemStack;

public interface IPlayer<T extends EntityPlayerMP> extends IEntityLivingBase<T> {
	
	void addDialog(int id);

	void addFactionPoints(int p0, int p1);

	void addMoney(long value);

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

	// New
	int[] getKeyPressed();

	T getMCEntity();

	long getMoney();

	int[] getMousePressed();

	IContainer getOpenContainer();

	Object getPixelmonData();

	IBlock getSpawnPoint();

	ITimers getTimers();

	double[] getWindowSize();

	boolean giveItem(IItemStack p0);

	boolean giveItem(String p0, int p1, int p2);

	boolean hasAchievement(String p0);

	boolean hasActiveQuest(int p0);

	boolean hasFinishedQuest(int p0);

	boolean hasKeyPressed(int key);

	boolean hasMousePress(int key);

	boolean hasPermission(String p0);

	boolean hasReadDialog(int p0);

	@Deprecated
	int inventoryItemCount(IItemStack p0);

	int inventoryItemCount(IItemStack stack, boolean ignoreDamage, boolean ignoreNBT);

	@Deprecated
	int inventoryItemCount(String p0, int p1);

	boolean isMoved();

	void kick(String p0);

	void message(String p0);

	void playSound(String p0, float p1, float p2);

	void removeAllItems(IItemStack p0);

	void removeDialog(int p0);

	boolean removeItem(IItemStack p0, int p1);

	boolean removeItem(String p0, int p1, int p2);

	void removeQuest(int p0);

	void resetSpawnpoint();

	void sendMail(IPlayerMail p0);

	void sendNotification(String p0, String p1, int p2);

	void setExpLevel(int p0);

	void setGamemode(int p0);

	void setHunger(int p0);

	void setMoney(long value);

	void setSpawnpoint(int p0, int p1, int p2);

	void setSpawnPoint(IBlock p0);

	@Deprecated
	IContainer showChestGui(int p0);

	void showCustomGui(ICustomGui p0);

	void showDialog(int p0, String p1);

	void startQuest(int p0);

	void stopQuest(int p0);

	void updatePlayerInventory();
	
	void completeQuest(int id); // New
	
}
