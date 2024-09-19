package noppes.npcs.mixin.nbt;

import net.minecraft.nbt.NBTTagLongArray;
import noppes.npcs.mixin.api.nbt.NBTTagLongArrayAPIMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = NBTTagLongArray.class)
public class NBTTagLongArrayMixin implements NBTTagLongArrayAPIMixin {

    @Shadow(aliases = "data")
    private long[] data;

    @Override
    public long[] npcs$getData() { return data; }


}
