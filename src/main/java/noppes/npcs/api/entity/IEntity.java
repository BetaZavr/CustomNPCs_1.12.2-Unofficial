package noppes.npcs.api.entity;

import net.minecraft.entity.Entity;
import noppes.npcs.api.IEntityDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IRayTrace;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.item.IItemStack;

public interface IEntity<T extends Entity> {
	
	void addRider(IEntity<?> entity);

	void addTag(String tag);

	void clearRiders();

	void damage(float amount);
	
	void damage(float amount, IEntityDamageSource source); // New

	void despawn();

	IEntityItem<?> dropItem(IItemStack item);

	void extinguish();

	String generateNewUUID();

	long getAge();

	IEntity<?>[] getAllRiders();

	int getBlockX();

	int getBlockY();

	int getBlockZ();

	String getEntityName();

	INbt getEntityNbt();

	float getEyeHeight();

	float getHeight();

	T getMCEntity();

	double getMotionX();

	double getMotionY();

	double getMotionZ();

	IEntity<?> getMount();

	String getName();

	INbt getNbt();

	float getPitch();

	IPos getPos();

	IEntity<?>[] getRiders();

	float getRotation();

	IData getStoreddata();

	String[] getTags();

	IData getTempdata();

	int getType();

	String getTypeName();

	String getUUID();

	float getWidth();

	IWorld getWorld();

	double getX();

	double getY();

	double getZ();

	boolean hasCustomName();

	boolean hasTag(String tag);

	boolean inFire();

	boolean inLava();

	boolean inWater();

	boolean isAlive();

	boolean isBurning();

	boolean isSneaking();

	boolean isSprinting();

	void kill();

	void knockback(int power, float direction);

	void playAnimation(int type);

	IRayTrace rayTraceBlock(double distance, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox);

	IEntity<?>[] rayTraceEntities(double distance, boolean stopOnLiquid, boolean ignoreBlockWithoutBoundingBox);

	void removeTag(String tag);

	void setBurning(int seconds);

	void setEntityNbt(INbt nbt);

	void setMotionX(double motion);

	void setMotionY(double motion);

	void setMotionZ(double motion);

	void setMount(IEntity<?> entity);

	void setName(String name);

	void setPitch(float pitch);

	void setPos(IPos pos);

	void setPosition(double x, double y, double z);

	void setRotation(float rotation);

	void setX(double x);

	void setY(double y);

	void setZ(double z);

	void spawn();

	void storeAsClone(int tab, String name);

	boolean typeOf(int type);
	
}
