package noppes.npcs.api.entity;

import net.minecraft.entity.EntityCreature;
import noppes.npcs.api.ITimers;
import noppes.npcs.api.ParamName;
import noppes.npcs.api.entity.data.INPCAdvanced;
import noppes.npcs.api.entity.data.INPCAi;
import noppes.npcs.api.entity.data.INPCAnimation;
import noppes.npcs.api.entity.data.INPCDisplay;
import noppes.npcs.api.entity.data.INPCInventory;
import noppes.npcs.api.entity.data.INPCJob;
import noppes.npcs.api.entity.data.INPCRole;
import noppes.npcs.api.entity.data.INPCStats;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.handler.data.IFaction;
import noppes.npcs.api.item.IItemStack;

@SuppressWarnings("all")
public interface ICustomNpc<T extends EntityCreature> extends IEntityLiving<T> {

	String executeCommand(@ParamName("command") String command);

	INPCAdvanced getAdvanced();

	INPCAi getAi();

	INPCAnimation getAnimations();

	IDialog getDialog(@ParamName("id") int id);

	INPCDisplay getDisplay();

	IFaction getFaction();

	int getHomeX();

	int getHomeY();

	int getHomeZ();

	INPCInventory getInventory();

	INPCJob getJob();

	IEntityLivingBase<?> getOwner();

	INPCRole getRole();

	INPCStats getStats();

	ITimers getTimers();

	void giveItem(@ParamName("player") IPlayer<?> player, @ParamName("item") IItemStack item);

	void reset();

	void say(@ParamName("message") String message);

	void sayTo(@ParamName("player") IPlayer<?> player, @ParamName("message") String message);

	void setDialog(@ParamName("id") int id, @ParamName("dialog") IDialog dialog);

	void setFaction(@ParamName("id") int id);

	void setHome(@ParamName("x") int x, @ParamName("y") int y, @ParamName("z") int z);

	IProjectile<?> shootItem(@ParamName("x") double x, @ParamName("y") double y, @ParamName("z") double z,
							 @ParamName("item") IItemStack item, @ParamName("accuracy") int accuracy);

	IProjectile<?> shootItem(@ParamName("entity") IEntityLivingBase<?> entity,
							 @ParamName("item") IItemStack item, @ParamName("accuracy") int accuracy);

	void trigger(@ParamName("id") int id, @ParamName("arguments") Object... arguments);

	void updateClient();

	int getOffsetX();

	int getOffsetY();

	int getOffsetZ();

	void setOffset(@ParamName("x") int x, @ParamName("y") int y, @ParamName("z") int z);

}
