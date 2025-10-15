package noppes.npcs.api.entity;

import net.minecraft.entity.Entity;
import noppes.npcs.api.*;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.item.IItemStack;

@SuppressWarnings("all")
public interface IEntity<T extends Entity> {

	void addRider(@ParamName("entity") IEntity<?> entity);

	void addTag(@ParamName("tag") String tag);

	void clearRiders();

	void damage(@ParamName("amount") float amount);

	void damage(@ParamName("amount") float amount, @ParamName("source") IEntityDamageSource source);

	void despawn();

	IEntityItem<?> dropItem(@ParamName("item") IItemStack item);

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

	boolean hasTag(@ParamName("tag") String tag);

	boolean inFire();

	boolean inLava();

	boolean inWater();

	boolean isAlive();

	boolean isBurning();

	boolean isSneaking();

	boolean isSprinting();

	void kill();

	void knockback(@ParamName("power") int power, @ParamName("direction") float direction);

	void playAnimation(@ParamName("type") int type);

	IRayTrace rayTraceBlock(@ParamName("distance") double distance, @ParamName("stopOnLiquid") boolean stopOnLiquid,
							@ParamName("ignoreBlockWithoutBoundingBox") boolean ignoreBlockWithoutBoundingBox);

	IEntity<?>[] rayTraceEntities(@ParamName("distance") double distance, @ParamName("stopOnLiquid") boolean stopOnLiquid,
								  @ParamName("ignoreBlockWithoutBoundingBox") boolean ignoreBlockWithoutBoundingBox);

	void removeTag(@ParamName("tag") String tag);

	void setBurning(@ParamName("seconds") int seconds);

	void setEntityNbt(@ParamName("nbt") INbt nbt);

	void setMotionX(@ParamName("motion") double motion);

	void setMotionY(@ParamName("motion") double motion);

	void setMotionZ(@ParamName("motion") double motion);

	void setMount(@ParamName("entity") IEntity<?> entity);

	void setName(@ParamName("name") String name);

	void setPitch(@ParamName("pitch") float pitch);

	void setPos(@ParamName("pos") IPos pos);

	void setPosition(@ParamName("x") double x, @ParamName("y") double y, @ParamName("z") double z);

	void setRotation(@ParamName("rotation") float rotation);

	void setX(@ParamName("x") double x);

	void setY(@ParamName("y") double y);

	void setZ(@ParamName("z") double z);

	void spawn();

	void storeAsClone(@ParamName("tab") int tab, @ParamName("name") String name);

	boolean typeOf(@ParamName("type") int type);

}
