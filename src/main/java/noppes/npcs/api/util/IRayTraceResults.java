package noppes.npcs.api.util;

import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.IEntity;

public interface IRayTraceResults {

    IBlock[] getBlocks();

    IEntity<?>[] getEntitys();

}
