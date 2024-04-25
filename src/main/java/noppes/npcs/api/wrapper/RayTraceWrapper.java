package noppes.npcs.api.wrapper;

import noppes.npcs.api.IPos;
import noppes.npcs.api.IRayTrace;
import noppes.npcs.api.block.IBlock;

public class RayTraceWrapper implements IRayTrace {

	private IBlock block;
	public IPos pos;
	private int sideHit;

	public RayTraceWrapper(IBlock block, int sideHit) {
		this.block = block;
		this.sideHit = sideHit;
		this.pos = block.getPos();
	}

	@Override
	public IBlock getBlock() {
		return this.block;
	}

	@Override
	public IPos getPos() {
		return this.block.getPos();
	}

	@Override
	public int getSideHit() {
		return this.sideHit;
	}
}
