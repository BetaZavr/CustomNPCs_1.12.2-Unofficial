package noppes.npcs.api.wrapper.data;

import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.wrapper.BlockWrapper;

public class DataBlock {

    public final World world;
    public final BlockPos pos;
    public final IBlockState state;

    public DataBlock(World worldIn, BlockPos posIn, IBlockState stateIn) {
        world = worldIn;
        pos = posIn;
        state = stateIn;
    }

    public IBlock getIBlock() { return BlockWrapper.createNew(world, pos, state); }

}
