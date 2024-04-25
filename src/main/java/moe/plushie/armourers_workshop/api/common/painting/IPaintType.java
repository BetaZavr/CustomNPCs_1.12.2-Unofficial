package moe.plushie.armourers_workshop.api.common.painting;

import moe.plushie.armourers_workshop.api.common.IExtraColours.ExtraColourType;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface IPaintType {

	public int getChannelIndex();

	public ExtraColourType getColourType();

	public int getId();

	@SideOnly(Side.CLIENT)
	public String getLocalizedName();

	public int getMarkerIndex();

	public String getName();

	public float getU();

	public String getUnlocalizedName();

	public float getV();

	public boolean hasAverageColourChannel();

	public void setColourChannelIndex(int channelIndex);
}
