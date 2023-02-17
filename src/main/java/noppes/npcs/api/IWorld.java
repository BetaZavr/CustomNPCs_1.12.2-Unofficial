package noppes.npcs.api;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.data.IData;
import noppes.npcs.api.item.IItemStack;

public interface IWorld {
	void broadcast(String p0);

	IEntity<?> createEntity(String p0);

	IEntity<?> createEntityFromNBT(INbt p0);

	IItemStack createItem(String p0, int p1, int p2);

	IItemStack createItemFromNbt(INbt p0);

	void explode(double p0, double p1, double p2, float p3, boolean p4, boolean p5);

	IEntity<?>[] getAllEntities(int p0);

	IPlayer<?>[] getAllPlayers();

	String getBiomeName(int p0, int p1);

	IBlock getBlock(int p0, int p1, int p2);

	@Deprecated
	IEntity<?> getClone(int p0, String p1);

	@Deprecated
	IEntity<?> getClosestEntity(int p0, int p1, int p2, int p3, int p4);

	IEntity<?> getClosestEntity(IPos p0, int p1, int p2);

	IDimension getDimension();

	IEntity<?> getEntity(String p0);

	float getLightValue(int p0, int p1, int p2);

	BlockPos getMCBlockPos(int p0, int p1, int p2);

	WorldServer getMCWorld();

	String getName();

	@Deprecated
	IEntity<?>[] getNearbyEntities(int p0, int p1, int p2, int p3, int p4);

	IEntity<?>[] getNearbyEntities(IPos p0, int p1, int p2);

	IPlayer<?> getPlayer(String p0);

	int getRedstonePower(int p0, int p1, int p2);

	IScoreboard getScoreboard();

	IBlock getSpawnPoint();

	IData getStoreddata();

	IData getTempdata();

	long getTime();

	long getTotalTime();

	boolean isDay();

	boolean isRaining();

	void playSoundAt(IPos p0, String p1, float p2, float p3);

	void removeBlock(int p0, int p1, int p2);

	void setBlock(int p0, int p1, int p2, String p3, int p4);

	void setRaining(boolean p0);

	void setSpawnPoint(IBlock p0);

	void setTime(long p0);

	@Deprecated
	IEntity<?> spawnClone(double p0, double p1, double p2, int p3, String p4);

	void spawnEntity(IEntity<?> p0);

	void spawnParticle(String p0, double p1, double p2, double p3, double p4, double p5, double p6, double p7, int p8);

	void thunderStrike(double p0, double p1, double p2);
}
