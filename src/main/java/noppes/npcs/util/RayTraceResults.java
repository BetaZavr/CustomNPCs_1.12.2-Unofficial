package noppes.npcs.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.util.IRayTraceResults;
import noppes.npcs.api.wrapper.*;

public class RayTraceResults implements IRayTraceResults {

	public static RayTraceResults EMPTY = new RayTraceResults();

	private final LRUHashMap<BlockPos, IBlock> blocks = new LRUHashMap<>(128);
	private List<Entity> entitys = new ArrayList<>();

	public void add(Entity entity, double distance, Vec3d vecStart, Vec3d vecEnd) {
		entitys = EntityWrapper.findEntityOnPath(entity, distance, vecStart, vecEnd);
	}

	public void add(World world, BlockPos pos, IBlockState state) {
		if (blocks.containsKey(pos)) { return; }
		blocks.put(pos, BlockWrapper.createNew(world, pos, state));
	}

	@Override
	public IBlock[] getBlocks() { return blocks.values().toArray(new IBlock[0]); }

	@Override
	public IEntity<?>[] getEntitys() {
		List<IEntity<?>> result = new ArrayList<>();
		for (Entity e : entitys) {
			result.add(Objects.requireNonNull(NpcAPI.Instance()).getIEntity(e));
		}
		return result.toArray(new IEntity[0]);
	}

	@Override
	public void clear() {
		blocks.clear();
		entitys.clear();
	}

}
