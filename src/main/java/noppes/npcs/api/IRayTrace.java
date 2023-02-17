package noppes.npcs.api;

import noppes.npcs.api.block.IBlock;

public interface IRayTrace {
	IBlock getBlock();

	IPos getPos();

	int getSideHit();
}
