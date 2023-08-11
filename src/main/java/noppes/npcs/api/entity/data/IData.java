package noppes.npcs.api.entity.data;

import net.minecraft.command.CommandException;
import noppes.npcs.api.INbt;

public interface IData {
	
	void clear();

	Object get(String key);

	String[] getKeys();

	boolean has(String key);

	void put(String key, Object value) throws CommandException;

	boolean remove(String key);
	
	INbt getNbt();
	
	void setNbt(INbt nbt);
	
}
