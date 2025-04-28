package noppes.npcs.reflection.client.model;

import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;

@SideOnly(Side.CLIENT)
public class ModelPlayerReflection {

    private static Field bipedCape;

    public static void setBipedCape(ModelPlayer parent, ModelRenderer newBipedCape) {
        if (parent == null || newBipedCape == null) { return; }
        if (bipedCape == null) {
            Exception error = null;
            try { bipedCape = ModelPlayer.class.getDeclaredField("field_78115_e"); } catch (Exception e) { error = e; }
            if (bipedCape == null) {
                try {
                    bipedCape = ModelPlayer.class.getDeclaredField("bipedCape");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"bipedCape\"", error);
                return;
            }
        }
        try {
            bipedCape.setAccessible(true);
            bipedCape.set(parent, newBipedCape);
        } catch (Exception e) {
            LogWriter.error("Error set \"bipedCape\":\"" + newBipedCape + "\" in " + parent, e);
        }
    }

}
