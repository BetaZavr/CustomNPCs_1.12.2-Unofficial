package noppes.npcs.reflection.inv;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import noppes.npcs.LogWriter;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class SlotReflection {

    private static Field slotIndex;
    private static Field inventory;

    @SuppressWarnings("all")
    public static void setSlotIndex(Slot slot, int newSlotID) {
        if (slot == null) { return; }
        if (inventory == null) {
            Exception error = null;
            try { inventory = Slot.class.getDeclaredField("field_75224_c"); } catch (Exception e) { error = e; }
            if (inventory == null) {
                try {
                    inventory = Slot.class.getDeclaredField("inventory");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"inventory\"", error);
                return;
            }
        }
        int sizeInventory;
        try {
            inventory.setAccessible(true);
            sizeInventory = ((IInventory) inventory.get(slot)).getSizeInventory();
        }
        catch (Exception e) {
            LogWriter.error("Error get \"inventory\" in " + slot, e);
            return;
        }
        if (newSlotID < 0 || newSlotID >= sizeInventory) { return; }
        if (slotIndex == null) {
            Exception error = null;
            try { slotIndex = Slot.class.getDeclaredField("field_75225_a"); } catch (Exception e) { error = e; }
            if (slotIndex == null) {
                try {
                    slotIndex = Slot.class.getDeclaredField("slotIndex");
                    error = null;
                } catch (Exception e) { error = e; }
            }
            if (error != null) {
                LogWriter.error("Error found field \"slotIndex\"", error);
                return;
            }
        }
        try {
            slotIndex.setAccessible(true);

            if (Modifier.isFinal(slotIndex.getModifiers())) {
                Field modifiersField = Field.class.getDeclaredField("modifiers");
                modifiersField.setAccessible(true);
                modifiersField.setInt(slotIndex, slotIndex.getModifiers() & ~Modifier.FINAL);
            }

            slotIndex.set(slot, newSlotID);
        }
        catch (Exception e) {
            LogWriter.error("Error set \"slotIndex\":\"" + newSlotID + "\" in " + slot, e);
        }
    }

}
