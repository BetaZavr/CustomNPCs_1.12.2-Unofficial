package moe.plushie.armourers_workshop.api.common.skin.data;

import moe.plushie.armourers_workshop.api.common.library.ILibraryFile;
import moe.plushie.armourers_workshop.api.common.skin.type.ISkinType;

public interface ISkinIdentifier {

	public int getSkinGlobalId();

	public ILibraryFile getSkinLibraryFile();

	public int getSkinLocalId();

	public ISkinType getSkinType();

	public boolean hasGlobalId();

	public boolean hasLibraryFile();

	public boolean hasLocalId();

	public boolean isValid();
}
