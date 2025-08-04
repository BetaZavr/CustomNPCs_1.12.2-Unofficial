package noppes.npcs.api.util;

import net.minecraft.entity.Entity;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.wrapper.data.DataBlock;

import java.util.List;

@SuppressWarnings("all")
public interface IRayTraceResults {

    IBlock[] getBlocks();

    IEntity<?>[] getEntitys();

    void clear();

    List<DataBlock> getMCBlocks();

    List<Entity> getMCEntitys();

}
