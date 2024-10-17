package noppes.npcs.util;

import java.util.Arrays;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.util.IRayTraceResults;
import noppes.npcs.api.wrapper.BlockWrapper;
import noppes.npcs.api.wrapper.EntityWrapper;

public class RayTraceResults implements IRayTraceResults {

	private IBlock[] blocks;
	private IEntity<?>[] entitys;

	public RayTraceResults() {
		this.blocks = new IBlock[0];
		this.entitys = new IEntity<?>[0];
	}

	public void add(Entity entity, double distance, Vec3d vecStart, Vec3d vecEnd) {
		this.entitys = EntityWrapper.findEntityOnPath(entity, distance, vecStart, vecEnd);
	}

	@SuppressWarnings("deprecation")
	public void add(World world, BlockPos pos, IBlockState state) {
		for (IBlock bi : this.blocks) {
			if (bi.getPos().getMCBlockPos().equals(pos)) { return; }
		}
		int currentSize = blocks.length;
		IBlock[] newArray = Arrays.copyOf(blocks, currentSize + 1);
		newArray[currentSize] = BlockWrapper.createNew(world, pos, state);
		blocks = newArray;
	}

	@Override
	public IBlock[] getBlocks() { return blocks; }

	@Override
	public IEntity<?>[] getEntitys() { return entitys; }
}
