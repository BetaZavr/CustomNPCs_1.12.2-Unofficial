package moe.plushie.armourers_workshop.api.common.painting;

import java.util.ArrayList;

public interface IPaintTypeRegistry {

	public int getExtraChannels();

	public IPaintType getPaintTypeFormByte(byte index);

	public IPaintType getPaintTypeFormName(String name);

	public IPaintType getPaintTypeFromColour(int trgb);

	public IPaintType getPaintTypeFromIndex(int index);

	public ArrayList<IPaintType> getRegisteredTypes();

	public boolean registerPaintType(IPaintType paintType);

	public int setPaintTypeOnColour(IPaintType paintType, int colour);
}
