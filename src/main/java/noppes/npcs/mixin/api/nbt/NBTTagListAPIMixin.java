package noppes.npcs.mixin.api.nbt;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;

@Mixin(value = NBTTagList.class)
public interface NBTTagListAPIMixin {

    @Accessor(value="tagList")
    List<NBTBase> npcs$getTagList();

}
