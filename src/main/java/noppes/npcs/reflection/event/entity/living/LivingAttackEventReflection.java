package noppes.npcs.reflection.event.entity.living;

import net.minecraftforge.event.entity.living.LivingAttackEvent;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;

public class LivingAttackEventReflection {

    private static Field amount;

    public static void setAmount(LivingAttackEvent event, float newAmount) {
        if (event == null) { return; }
        if (newAmount < 0.0f) { newAmount = 0.0f; }
        if (amount == null) {
            try {
                amount = LivingAttackEvent.class.getDeclaredField("amount");
            } catch (Exception error) {
                LogWriter.error("Error found field \"openSet\"", error);
                return;
            }
        }
        try {
            amount.setAccessible(true);
            amount.set(event, newAmount);
        } catch (Exception e) {
            LogWriter.error("Error set \"amount\":\"" + newAmount + "\" in " + event, e);
        }
    }

}
