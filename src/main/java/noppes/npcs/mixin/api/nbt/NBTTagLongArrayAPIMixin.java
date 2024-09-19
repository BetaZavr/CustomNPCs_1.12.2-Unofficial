package noppes.npcs.mixin.api.nbt;

import net.minecraft.nbt.NBTTagLongArray;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = NBTTagLongArray.class)
public interface NBTTagLongArrayAPIMixin {

    @Accessor(value="data")
    long[] npcs$getData();

}
