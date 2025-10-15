package noppes.npcs.api.entity.data;

import net.minecraft.command.CommandException;
import noppes.npcs.api.INbt;
import noppes.npcs.api.ParamName;

public interface IData {

	void clear();

	Object get(@ParamName("key") String key);

	String[] getKeys();

	INbt getNbt();

	boolean has(@ParamName("key") String key);

	void put(@ParamName("key") String key, @ParamName("value") Object value) throws CommandException;

	void remove(@ParamName("key") String key);

	void setNbt(@ParamName("nbt") INbt nbt);

}
