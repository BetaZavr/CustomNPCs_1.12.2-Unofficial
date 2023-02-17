package noppes.npcs;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.Sets;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
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
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.controllers.SpawnController;
import noppes.npcs.controllers.data.SpawnData;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ObfuscationHelper;

public class NPCSpawning {
	
	private static Set<ChunkPos> eligibleChunksForSpawning = Sets.newHashSet();

	public static boolean canCreatureTypeSpawnAtLocation(SpawnData data, World world, BlockPos pos) {
		if (!world.getWorldBorder().contains(pos)) {
			return false;
		}
		if ((data.type == 1 && world.getLight(pos) > 8) || (data.type == 2 && world.getLight(pos) <= 8)) {
			return false;
		}
		IBlockState state = world.getBlockState(pos);
		if (data.liquid) {
			return state.getMaterial().isLiquid() && world.getBlockState(pos.down()).getMaterial().isLiquid()
					&& !world.getBlockState(pos.up()).isNormalCube();
		}
		BlockPos blockpos1 = pos.down();
		IBlockState state2 = world.getBlockState(blockpos1);
		Block block2 = state2.getBlock();
		if (!state2.isSideSolid(world, blockpos1, EnumFacing.UP)) {
			return false;
		}
		boolean flag = block2 != Blocks.BEDROCK && block2 != Blocks.BARRIER;
		BlockPos down = blockpos1.down();
		flag |= world.getBlockState(down).getBlock().canCreatureSpawn(world.getBlockState(down), (IBlockAccess) world,
				down, EntityLiving.SpawnPlacementType.ON_GROUND);
		return flag && !state.isNormalCube() && !state.getMaterial().isLiquid()
				&& !world.getBlockState(pos.up()).isNormalCube();
	}

	public static int countNPCs(World world) {
		int count = 0;
		List<Entity> list = (List<Entity>) world.loadedEntityList;
		for (Entity entity : list) {
			if (entity instanceof EntityNPCInterface) {
				++count;
			}
		}
		return count;
	}

	public static void findChunksForSpawning(WorldServer world) {
		if (SpawnController.instance.data.isEmpty() || world.getWorldInfo().getWorldTotalTime() % 400L != 0L) {
			return;
		}
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
						if (!NPCSpawning.eligibleChunksForSpawning.contains(chunkcoordintpair)
								&& world.getWorldBorder().contains(chunkcoordintpair)) {
							PlayerChunkMapEntry playerinstance = world.getPlayerChunkMap().getEntry(chunkcoordintpair.x,
									chunkcoordintpair.z);
							if (playerinstance != null && playerinstance.isSentToPlayers()) {
								NPCSpawning.eligibleChunksForSpawning.add(chunkcoordintpair);
							}
						}
					}
				}
			}
		}
		if (countNPCs(world) > NPCSpawning.eligibleChunksForSpawning.size() / 16) {
			return;
		}
		ArrayList<ChunkPos> tmp = new ArrayList<ChunkPos>(NPCSpawning.eligibleChunksForSpawning);
		Collections.shuffle(tmp);
		for (ChunkPos chunkcoordintpair2 : tmp) {
			BlockPos chunkposition = getChunk(world, chunkcoordintpair2.x, chunkcoordintpair2.z);
			int j2 = chunkposition.getX();
			int k2 = chunkposition.getY();
			int l1 = chunkposition.getZ();
			for (int m = 0; m < 3; ++m) {
				int x2 = j2;
				int y = k2;
				int z2 = l1;
				byte b1 = 6;
				x2 += world.rand.nextInt(b1) - world.rand.nextInt(b1);
				y += world.rand.nextInt(1) - world.rand.nextInt(1);
				z2 += world.rand.nextInt(b1) - world.rand.nextInt(b1);
				BlockPos pos = new BlockPos(x2, y, z2);
				IBlockState state = world.getBlockState(pos);
				String name = ObfuscationHelper.getValue(Biome.class, world.getBiomeForCoordsBody(pos), 17);
				SpawnData data = SpawnController.instance.getRandomSpawnData(name, state.getMaterial() == Material.AIR);
				if (data != null && canCreatureTypeSpawnAtLocation(data, world, pos)) {
					if (world.getClosestPlayer(x2, y, z2, 24.0, false) == null) {
						spawnData(data, world, pos);
					}
				}
			}
		}
	}

	protected static BlockPos getChunk(World world, int x, int z) {
		Chunk chunk = world.getChunkFromChunkCoords(x, z);
		int k = x * 16 + world.rand.nextInt(16);
		int l = z * 16 + world.rand.nextInt(16);
		int i1 = MathHelper.roundUp(chunk.getHeight(new BlockPos(k, 0, l)) + 1, 16);
		int j1 = world.rand.nextInt((i1 > 0) ? i1 : (chunk.getTopFilledSegment() + 16 - 1));
		return new BlockPos(k, j1, l);
	}

	public static void performWorldGenSpawning(World world, int x, int z, Random rand) {
		Biome biome = world.getBiomeForCoordsBody(new BlockPos(x + 8, 0, z + 8));
		while (rand.nextFloat() < biome.getSpawningChance()) {
			SpawnData data = SpawnController.instance.getRandomSpawnData(ObfuscationHelper.getValue(Biome.class, biome, 17), true);
			if (data == null) {
				continue;
			}
			int size = 16;
			int j1 = x + rand.nextInt(size);
			int k1 = z + rand.nextInt(size);
			int l1 = j1;
			int i2 = k1;
			for (int k2 = 0; k2 < 4; ++k2) {
				BlockPos pos = world.getTopSolidOrLiquidBlock(new BlockPos(j1, 0, k1));
				if (!canCreatureTypeSpawnAtLocation(data, world, pos)) {
					for (j1 += rand.nextInt(5) - rand.nextInt(5), k1 += rand.nextInt(5) - rand.nextInt(5); j1 < x
							|| j1 >= x + size || k1 < z || k1 >= z + size; j1 = l1 + rand.nextInt(5)
									- rand.nextInt(5), k1 = i2 + rand.nextInt(5) - rand.nextInt(5)) {
					}
				} else if (spawnData(data, world, pos)) {
					break;
				}
			}
		}
	}

	private static boolean spawnData(SpawnData data, World world, BlockPos pos) {
		EntityLiving entity = null;
		try {
			entity = (EntityLiving) EntityList.createEntityFromNBT(data.compound1, world);
			if (entity == null || !(entity instanceof EntityLiving)) {
				return false;
			}
			if (entity instanceof EntityCustomNpc) {
				EntityCustomNpc npc = (EntityCustomNpc) entity;
				npc.stats.spawnCycle = 4;
				npc.stats.respawnTime = 0;
				npc.ais.returnToStart = false;
				npc.ais.setStartPos(pos);
			}
			entity.setLocationAndAngles(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, world.rand.nextFloat() * 360.0f,
					0.0f);
		} catch (Exception exception) {
			exception.printStackTrace();
			return false;
		}
		@SuppressWarnings("deprecation")
		Event.Result canSpawn = ForgeEventFactory.canEntitySpawn(entity, world, pos.getX() + 0.5f, pos.getY(),
				pos.getZ() + 0.5f);
		if (canSpawn == Event.Result.DENY || (canSpawn == Event.Result.DEFAULT && !entity.getCanSpawnHere())) {
			return false;
		}
		world.spawnEntity(entity);
		return true;
	}

}
