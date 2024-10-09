package noppes.npcs.mixin.impl.nbt;

import net.minecraft.nbt.NBTTagLongArray;
import noppes.npcs.mixin.nbt.INBTTagLongArrayMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = NBTTagLongArray.class)
public class NBTTagLongArrayMixin implements INBTTagLongArrayMixin {

    @Shadow
    private long[] data;

    @Override
    public long[] npcs$getData() { return data; }


}
