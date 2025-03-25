package noppes.npcs.reflection.client;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.client.renderer.ItemModelMesher;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.item.Item;
import net.minecraftforge.client.ItemModelMesherForge;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.registries.IRegistryDelegate;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ItemModelMesherForgeReflection {

    private static Field models;

    @SuppressWarnings("unchecked")
    public static Map<IRegistryDelegate<Item>, Int2ObjectMap<IBakedModel>> getModels(ItemModelMesher itemModelMesher) {
        if (itemModelMesher == null) { return new HashMap<>(); }
        if (models == null) {
            try { models = ItemModelMesherForge.class.getDeclaredField("models"); }
            catch (Exception error) {
                LogWriter.error("Error found field \"models\"", error);
                return new HashMap<>();
            }
        }
        try {
            models.setAccessible(true);
            return (Map<IRegistryDelegate<Item>, Int2ObjectMap<IBakedModel>>) models.get(itemModelMesher);
        } catch (Exception e) {
            LogWriter.error("Error get \"models\" in " + itemModelMesher, e);
        }
        return new HashMap<>();
    }

}
