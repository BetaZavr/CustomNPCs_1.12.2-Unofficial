package noppes.npcs.mixin.nbt;

import net.minecraft.nbt.NBTBase;

import java.util.List;

public interface INBTTagListMixin {

    List<NBTBase> npcs$getTagList();

}
