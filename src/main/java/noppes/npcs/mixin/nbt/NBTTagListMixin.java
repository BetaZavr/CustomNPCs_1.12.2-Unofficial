package noppes.npcs.mixin.nbt;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.mixin.api.nbt.NBTTagListAPIMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

@Mixin(value = NBTTagList.class)
public class NBTTagListMixin implements NBTTagListAPIMixin {

    @Shadow(aliases = "tagList")
    private List<NBTBase> tagList;

    @Override
    public List<NBTBase> npcs$getTagList() { return tagList; }

}
