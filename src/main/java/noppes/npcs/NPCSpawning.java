package noppes.npcs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.controllers.SpawnController;
import noppes.npcs.controllers.data.SpawnData;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.api.mixin.world.biome.IBiomeMixin;

public class NPCSpawning {

	private static final Set<ChunkPos> eligibleChunksForSpawning = Sets.newHashSet();

	// World generation in progress
	public static void performWorldGenSpawning(World world, int x, int z, Random rand) {
		Biome biome = world.getBiomeForCoordsBody(new BlockPos(x + 8, 0, z + 8));
		SpawnData data = SpawnController.instance.getRandomSpawnData(((IBiomeMixin) biome).npcs$getBiomeName());
		if (data == null) { return; }
		int size = 16;
		int posX = x + rand.nextInt(size);
		int posZ = z + rand.nextInt(size);
		int basePosX = posX;
		int basePosZ = posZ;
		for (int summons = 0; summons < 4; ++summons) {
			BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(posX, 0, posZ));
			if (rand.nextFloat() > data.itemWeight / 100.0f) { continue; }
			Entity entity = null;
			try { entity = EntityList.createEntityFromNBT(data.compound1, world); } catch (Exception e) { LogWriter.error("Error:", e); }
			if (!(entity instanceof EntityLiving)) { return; }
			if (!canCreatureTypeSpawnAtLocation(data, (EntityLiving) entity, world, pos)) {
				for (posX += rand.nextInt(5) - rand.nextInt(5), posZ += rand.nextInt(5) - rand.nextInt(5); posX < x || posX >= x + size || posZ < z || posZ >= z + size; posX = basePosX + rand.nextInt(5) - rand.nextInt(5), posZ = basePosZ + rand.nextInt(5) - rand.nextInt(5)) {
					if (canCreatureTypeSpawnAtLocation(data, (EntityLiving) entity, world, pos)) {
						if (spawnData((EntityLiving) entity, world, pos)) { break; }
					}
				}
			}
			else { spawnData((EntityLiving) entity, world, pos); }
		}
	}
	
	public static void findChunksForSpawning(WorldServer world) {
		if (SpawnController.instance.data.isEmpty() || world.getWorldInfo().getWorldTotalTime() % 100L != 0L) { return; }
		NPCSpawning.eligibleChunksForSpawning.clear();
		for (int i = 0; i < world.playerEntities.size(); ++i) {
			EntityPlayer entityplayer = world.playerEntities.get(i);
			if (!entityplayer.isSpectator()) {
				int j = MathHelper.floor(entityplayer.posX / 16.0);
				int k = MathHelper.floor(entityplayer.posZ / 16.0);
				byte size = 7;
				for (int x = -size; x <= size; ++x) {
					for (int z = -size; z <= size; ++z) {
						ChunkPos chunkcoordintpair = new ChunkPos(x + j, z + k);
						if (!NPCSpawning.eligibleChunksForSpawning.contains(chunkcoordintpair) && world.getWorldBorder().contains(chunkcoordintpair)) {
							PlayerChunkMapEntry playerinstance = world.getPlayerChunkMap().getEntry(chunkcoordintpair.x, chunkcoordintpair.z);
							if (playerinstance != null && playerinstance.isSentToPlayers()) {
								NPCSpawning.eligibleChunksForSpawning.add(chunkcoordintpair);
							}
						}
					}
				}
			}
		}
		ArrayList<ChunkPos> tmp = new ArrayList<>(NPCSpawning.eligibleChunksForSpawning);
		Collections.shuffle(tmp);
		for (ChunkPos chunkcoordintpair2 : tmp) {
			BlockPos chunkposition = getChunk(world, chunkcoordintpair2.x, chunkcoordintpair2.z);
			int basePosX = chunkposition.getX();
			int basePosY = chunkposition.getY();
			int basePosZ = chunkposition.getZ();
			for (int summons = 0; summons < 3; ++summons) {
				int posX = basePosX;
				int posZ = basePosZ;
				byte range = 6;
				posX += world.rand.nextInt(range) - world.rand.nextInt(range);
				posZ += world.rand.nextInt(range) - world.rand.nextInt(range);
				BlockPos pos = new BlockPos(posX, basePosY, posZ);
                String name = ((IBiomeMixin) world.getBiomeForCoordsBody(pos)).npcs$getBiomeName();
				SpawnData data = SpawnController.instance.getRandomSpawnData(name);
				if (data == null || world.rand.nextFloat() > data.itemWeight / 100.0f) { continue; }
				Entity entity = null;
				try { entity = EntityList.createEntityFromNBT(data.compound1, world); } catch (Exception e) { LogWriter.error("Error:", e); }
				if (!(entity instanceof EntityLiving)) { continue; }
				if (canCreatureTypeSpawnAtLocation(data, (EntityLiving) entity, world, pos)) {
					spawnData((EntityLiving) entity, world, pos);
				}
			}
		}
	}

	public static boolean canCreatureTypeSpawnAtLocation(SpawnData data, EntityLiving entity, World world, BlockPos pos) {
		if (data == null || !world.getWorldBorder().contains(pos)) { return false; }
		EntityPlayer player = world.getClosestPlayer(pos.getX(), pos.getY(), pos.getZ(), 128.0, false);
		if (player == null || player.getDistance(pos.getX(), pos.getY(), pos.getZ()) < 12.0d) { return false; }
		if ((data.type == 1 && world.getLight(pos) > 8) || (data.type == 2 && world.getLight(pos) <= 8)) { return false; }
		IBlockState state = world.getBlockState(pos);
		if (data.liquid) { return state.getMaterial().isLiquid() && world.getBlockState(pos.down()).getMaterial().isLiquid() && !world.getBlockState(pos.up()).isNormalCube(); }
		BlockPos blockpos1 = pos.down();
		IBlockState state2 = world.getBlockState(blockpos1);
		Block block2 = state2.getBlock();
		if (!state2.isSideSolid(world, blockpos1, EnumFacing.UP)) { return false; }
		boolean flag = block2 != Blocks.BEDROCK && block2 != Blocks.BARRIER;
		BlockPos down = blockpos1.down();
		flag |= world.getBlockState(down).getBlock().canCreatureSpawn(world.getBlockState(down), world, down, EntityLiving.SpawnPlacementType.ON_GROUND);
		int count = 0;
		List<Entity> list = world.loadedEntityList;
		for (Entity e : list) {
			if (e.isDead) { continue; }
			if (e.getClass() != entity.getClass() || (e instanceof EntityNPCInterface && ((EntityNPCInterface) e).stats.spawnCycle != 4)) { continue; }
			if (Math.sqrt(e.getDistance(pos.getX(), pos.getY(), pos.getZ())) <= data.range) {
				++count;
				if (count >= data.group) { return false; }
			}
		}
		return flag && !state.isNormalCube() && !state.getMaterial().isLiquid() && !world.getBlockState(pos.up()).isNormalCube();
	}
	
	private static boolean checkData(EntityLiving entity, World world) {
		if (entity == null || world == null) { return false; }
		int totalCount = 0, countInPlayer = 0;
		List<Entity> list = world.loadedEntityList;
		for (Entity e : list) {
			if (e.isDead) { continue; }
			boolean isType = e.getClass() == entity.getClass();
			if (e instanceof EntityNPCInterface) { isType = ((EntityNPCInterface) e).stats.spawnCycle == 4; }
			if (!isType) { continue; }
			totalCount++;
			if (world.getClosestPlayer(e.posX, e.posY, e.posZ, 64.0, false) != null) { countInPlayer++; }
			else if (world.getClosestPlayer(e.posX, e.posY, e.posZ, 128.0, false) == null) {
				e.isDead = true;
				totalCount--;
			}
		}
		if (totalCount > NPCSpawning.eligibleChunksForSpawning.size() / 16) {
            return countInPlayer == 0;
        }
		return true;
	}

	protected static BlockPos getChunk(World world, int x, int z) {
		Chunk chunk = world.getChunkFromChunkCoords(x, z);
		int posX = x * 16 + world.rand.nextInt(16);
		int posZ = z * 16 + world.rand.nextInt(16);
		int y = MathHelper.roundUp(chunk.getHeight(new BlockPos(posX, 0, posZ)) + 1, 16);
		int posY = world.rand.nextInt((y > 0) ? y : (chunk.getTopFilledSegment() + 16 - 1));
		return new BlockPos(posX, posY, posZ);
	}

	private static boolean spawnData(EntityLiving entity, World world, BlockPos pos) {
		if (!checkData(entity, world)) { return false; }
		if (entity instanceof EntityNPCInterface) {
			EntityNPCInterface npc = (EntityNPCInterface) entity;
			npc.stats.spawnCycle = 4;
			npc.stats.respawnTime = 0;
			npc.ais.returnToStart = false;
			npc.ais.setStartPos(pos);
		}
		entity.setLocationAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, world.rand.nextFloat() * 360.0f, 0.0f);
		@SuppressWarnings("deprecation")
		Event.Result canSpawn = ForgeEventFactory.canEntitySpawn(entity, world, pos.getX() + 0.5f, pos.getY(), pos.getZ() + 0.5f);
		if (canSpawn == Event.Result.DENY || (canSpawn == Event.Result.DEFAULT && !entity.getCanSpawnHere())) {
			return false;
		}
		world.spawnEntity(entity);
		return true;
	}

}
