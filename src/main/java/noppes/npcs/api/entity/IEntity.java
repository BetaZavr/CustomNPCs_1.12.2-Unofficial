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
	
	void addRider(IEntity<?> p0);

	void addTag(String p0);

	void clearRiders();

	void damage(float amount);
	
	void damage(float amount, IEntityDamageSource source); // New

	void despawn();

	IEntityItem<?> dropItem(IItemStack p0);

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

	boolean hasTag(String p0);

	boolean inFire();

	boolean inLava();

	boolean inWater();

	boolean isAlive();

	boolean isBurning();

	boolean isSneaking();

	boolean isSprinting();

	void kill();

	void knockback(int p0, float p1);

	void playAnimation(int p0);

	IRayTrace rayTraceBlock(double p0, boolean p1, boolean p2);

	IEntity<?>[] rayTraceEntities(double p0, boolean p1, boolean p2);

	void removeTag(String p0);

	void setBurning(int p0);

	void setEntityNbt(INbt p0);

	void setMotionX(double p0);

	void setMotionY(double p0);

	void setMotionZ(double p0);

	void setMount(IEntity<?> p0);

	void setName(String p0);

	void setPitch(float p0);

	void setPos(IPos p0);

	void setPosition(double p0, double p1, double p2);

	void setRotation(float p0);

	void setX(double p0);

	void setY(double p0);

	void setZ(double p0);

	void spawn();

	void storeAsClone(int p0, String p1);

	boolean typeOf(int p0);
	
}
