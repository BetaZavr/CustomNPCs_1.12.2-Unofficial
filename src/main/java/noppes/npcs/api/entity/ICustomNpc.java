package noppes.npcs.api.entity;

import net.minecraft.entity.EntityCreature;
import noppes.npcs.api.ITimers;
import noppes.npcs.api.entity.data.INPCAdvanced;
import noppes.npcs.api.entity.data.INPCAi;
import noppes.npcs.api.entity.data.INPCDisplay;
import noppes.npcs.api.entity.data.INPCInventory;
import noppes.npcs.api.entity.data.INPCJob;
import noppes.npcs.api.entity.data.INPCRole;
import noppes.npcs.api.entity.data.INPCStats;
import noppes.npcs.api.handler.data.IDialog;
import noppes.npcs.api.handler.data.IFaction;
import noppes.npcs.api.item.IItemStack;

public interface ICustomNpc<T extends EntityCreature>
extends IEntityLiving<T> {
	
	String executeCommand(String p0);

	INPCAdvanced getAdvanced();

	INPCAi getAi();

	IDialog getDialog(int p0);

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

	void giveItem(IPlayer<?> p0, IItemStack p1);

	void reset();

	void say(String p0);

	void sayTo(IPlayer<?> p0, String p1);

	void setDialog(int p0, IDialog p1);

	void setFaction(int p0);

	void setHome(int p0, int p1, int p2);

	IProjectile<?> shootItem(double p0, double p1, double p2, IItemStack p3, int p4);

	IProjectile<?> shootItem(IEntityLivingBase<?> p0, IItemStack p1, int p2);

	void updateClient();

	void trigger(int id, Object ... arguments);
}
