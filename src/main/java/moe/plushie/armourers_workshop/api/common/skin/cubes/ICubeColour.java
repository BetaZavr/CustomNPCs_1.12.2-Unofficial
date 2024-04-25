package moe.plushie.armourers_workshop.api.common.skin.cubes;

import net.minecraft.nbt.NBTTagCompound;

public interface ICubeColour {

	public byte[] getBlue();

	public byte getBlue(int side);

	public byte[] getGreen();

	public byte getGreen(int side);

	public byte[] getPaintType();

	public byte getPaintType(int side);

	public byte[] getRed();

	public byte getRed(int side);

	public void readFromNBT(NBTTagCompound compound);

	public void setBlue(byte blue, int side);

	@Deprecated
	public void setColour(int colour);

	public void setColour(int colour, int side);

	public void setGreen(byte green, int side);

	public void setPaintType(byte type, int side);

	public void setRed(byte red, int side);

	public void writeToNBT(NBTTagCompound compound);
}
