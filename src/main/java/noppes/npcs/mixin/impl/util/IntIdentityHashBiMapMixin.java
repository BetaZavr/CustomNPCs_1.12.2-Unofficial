package noppes.npcs.mixin.impl.util;

import net.minecraft.util.IntIdentityHashBiMap;
import noppes.npcs.mixin.util.IIntIdentityHashBiMapMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(value = IntIdentityHashBiMap.class)
public class IntIdentityHashBiMapMixin<K> implements IIntIdentityHashBiMapMixin<K> {

    @Shadow
    private K[] values;

    @Shadow
    private K[] byId;

    @Override
    public void npcs$remove(K key, int id) {
        values[id] = null;
        byId[id] = null;
    }

}
