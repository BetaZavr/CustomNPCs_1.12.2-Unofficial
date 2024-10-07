package noppes.npcs.mixin.impl.client;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraftforge.client.ItemModelMesherForge;
import net.minecraftforge.registries.IRegistryDelegate;
import noppes.npcs.mixin.client.IItemModelMesherForgeMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Map;

@Mixin(value = ItemModelMesherForge.class, remap = false)
public class ItemModelMesherForgeMixin implements IItemModelMesherForgeMixin {

    @Final
    @Shadow(aliases = "models")
    Map<IRegistryDelegate<Item>, Int2ObjectMap<IBakedModel>> models;

    @Override
    public Map<IRegistryDelegate<Item>, Int2ObjectMap<IBakedModel>> npcs$getModels() { return models; }

}
