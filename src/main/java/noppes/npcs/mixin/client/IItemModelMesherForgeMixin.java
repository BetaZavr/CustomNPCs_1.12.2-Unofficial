package noppes.npcs.mixin.client;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraftforge.registries.IRegistryDelegate;

import java.util.Map;

public interface IItemModelMesherForgeMixin {

    Map<IRegistryDelegate<Item>, Int2ObjectMap<IBakedModel>> npcs$getModels();

}
