package moe.plushie.armourers_workshop.api.common.skin.data;

import io.netty.buffer.ByteBuf;

public interface ISkinDye {

	public void addDye(byte[] rgbt);

	public void addDye(byte[] rgbt, String name);

	public void addDye(int index, byte[] rgbt);

	public void addDye(int index, byte[] rgbt, String name);

	public byte[] getDyeColour(int index);

	public String getDyeName(int index);

	public int getNumberOfDyes();

	public boolean hasName(int index);

	public boolean haveDyeInSlot(int index);

	public void readFromBuf(ByteBuf buf);

	public void removeDye(int index);

	public void writeToBuf(ByteBuf buf);
}
