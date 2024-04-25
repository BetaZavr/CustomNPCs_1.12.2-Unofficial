package noppes.npcs.api.entity;

import net.minecraft.entity.EntityCreature;
import noppes.npcs.api.ITimers;
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

public interface ICustomNpc<T extends EntityCreature> extends IEntityLiving<T> {

	String executeCommand(String command);

	INPCAdvanced getAdvanced();

	INPCAi getAi();

	INPCAnimation getAnimations();

	IDialog getDialog(int id);

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

	void giveItem(IPlayer<?> player, IItemStack item);

	void reset();

	void say(String message);

	void sayTo(IPlayer<?> player, String message);

	void setDialog(int id, IDialog dialog);

	void setFaction(int id);

	void setHome(int x, int y, int z);

	IProjectile<?> shootItem(double x, double y, double z, IItemStack item, int accuracy);

	IProjectile<?> shootItem(IEntityLivingBase<?> entity, IItemStack item, int accuracy);

	void trigger(int id, Object... arguments);

	void updateClient();
}
