package noppes.npcs.api.entity;

import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.api.*;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.data.IPlayerMail;
import noppes.npcs.api.entity.data.IPlayerMiniMap;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.gui.IOverlayHUD;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.item.IItemStack;

@SuppressWarnings("all")
public interface IPlayer<T extends EntityPlayer> extends IEntityLivingBase<T> {

	void addDialog(@ParamName("id") int id);

	void addFactionPoints(@ParamName("faction") int faction, @ParamName("points") int points);

	void addMoney(@ParamName("value") long value);

	void cameraShakingPlay(@ParamName("time") int time, @ParamName("amplitude") int amplitude,
						   @ParamName("type") int type, @ParamName("isFading") boolean isFading);

	void cameraShakingStop();

	boolean canQuestBeAccepted(@ParamName("id") int id);

	void clearData();

	void closeGui();

	void completeQuest(@ParamName("id") int id);

	int factionStatus(@ParamName("id") int id);

	boolean finishQuest(@ParamName("id") int id);

	IQuest[] getActiveQuests();

	IContainer getBubblesInventory();

	ICustomGui getCustomGui();

	String getDisplayName();

	int getExpLevel();

	int getFactionPoints(@ParamName("id") int id);

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

	String getSkinType(@ParamName("type") int type);

	IBlock getSpawnPoint();

	ITimers getTimers();

	double[] getWindowSize();

	boolean giveItem(@ParamName("item") IItemStack item);

	boolean giveItem(@ParamName("id") String id, @ParamName("damage") int damage, @ParamName("amount") int amount);

	boolean hasAchievement(@ParamName("achievement") String achievement);

	boolean hasActiveQuest(@ParamName("id") int id);

	boolean hasFinishedQuest(@ParamName("id") int id);

	boolean hasMousePress(@ParamName("key") int key);

	boolean hasOrKeyPressed(@ParamName("key") int[] key);

	boolean hasPermission(@ParamName("permission") String permission);

	boolean hasReadDialog(@ParamName("id") int id);

	@Deprecated
	int inventoryItemCount(@ParamName("item") IItemStack item);

	int inventoryItemCount(@ParamName("id") IItemStack stack, @ParamName("ignoreDamage") boolean ignoreDamage, @ParamName("ignoreNBT") boolean ignoreNBT);

	@Deprecated
	int inventoryItemCount(@ParamName("id") String id, @ParamName("amount") int amount);

	boolean isCompleteQuest(@ParamName("id") int id);

	boolean isMoved();

	void kick(@ParamName("message") String message);

	void message(@ParamName("message") String message);

	void playSound(@ParamName("categoryType") int categoryType, @ParamName("pos") IPos pos,
				   @ParamName("sound") String sound, @ParamName("volume") float volume, @ParamName("pitch") float pitch);

	void playSound(@ParamName("sound") String sound, @ParamName("volume") float volume, @ParamName("pitch") float pitch);

	void removeAllItems(@ParamName("item") IItemStack item);

	void removeDialog(@ParamName("id") int id);

	boolean removeItem(@ParamName("item") IItemStack item, @ParamName("amount") int amount);

	boolean removeItem(@ParamName("id") String id, @ParamName("damage") int damage, @ParamName("amount") int amount);

	void removeQuest(@ParamName("id") int id);

	void resetSpawnpoint();

	void sendMail(@ParamName("mail") IPlayerMail mail);

	void sendNotification(@ParamName("title") String title, @ParamName("message") String message, @ParamName("type") int type);

	void sendTo(@ParamName("nbt") INbt nbt);

	void setExpLevel(@ParamName("level") int level);

	void setGamemode(@ParamName("mode") int mode);

	void setHunger(@ParamName("level") int level);

	void setMoney(@ParamName("value") long value);

	void setSkin(@ParamName("isSmallArms") boolean isSmallArms,
				 @ParamName("body") int body, @ParamName("bodyColor") int bodyColor,
				 @ParamName("hair") int hair, @ParamName("hairColor") int hairColor,
				 @ParamName("face") int face, @ParamName("eyesColor") int eyesColor,
				 @ParamName("leg") int leg, @ParamName("jacket") int jacket,
				 @ParamName("shoes") int shoes, @ParamName("peculiarities") int... peculiarities);

	void setSkinType(@ParamName("location") String location, @ParamName("type") int type);

	void setSpawnpoint(@ParamName("x") int x, @ParamName("y") int y, @ParamName("z") int z);

	void setSpawnPoint(@ParamName("block") IBlock block);

	@Deprecated
	IContainer showChestGui(@ParamName("rows") int rows);

	void showCustomGui(@ParamName("gui") ICustomGui gui);

	void showDialog(@ParamName("id") int id, @ParamName("name") String name);

	void startQuest(@ParamName("id") int id);

	void stopQuest(@ParamName("id") int id);

	void stopSound(@ParamName("categoryType") int categoryType, @ParamName("sound") String sound);

	void trigger(@ParamName("id") int id, @ParamName("arguments") Object... arguments);

	void updatePlayerInventory();

	IEntity<?> getRidingEntity();
	
	IEntity<?> getLookingEntity();
	
	IBlock getLookingBlock();
	
	double getBlockReachDistance();

	void showMarket(@ParamName("marcetId") int marcetId);

}
