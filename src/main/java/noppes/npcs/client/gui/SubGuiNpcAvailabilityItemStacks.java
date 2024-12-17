package noppes.npcs.client.gui;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumAvailabilityStackData;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.containers.ContainerAvailabilityInv;
import noppes.npcs.controllers.data.Availability;
import noppes.npcs.controllers.data.AvailabilityStackData;
import noppes.npcs.util.Util;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class SubGuiNpcAvailabilityItemStacks
extends GuiContainerNPCInterface
implements ICustomScrollListener {

    public static GuiScreen parent;
    public static SubGuiNpcAvailability setting;

    private final Availability availability;
    private final ContainerAvailabilityInv cont;
    private GuiCustomScroll scroll;
    private int reset = 0;

    public SubGuiNpcAvailabilityItemStacks(ContainerAvailabilityInv container) {
        super(null, container);
        setBackground("itemsetup.png");
        xSize = 176;
        ySize = 202;
        drawDefaultBackground = true;
        closeOnEsc = true;
        title = "Availability Stacks";

        availability = container.availability;
        cont = container;
    }

    @Override
    public void buttonEvent(GuiNpcButton button) {
        AvailabilityStackData aData = availability.stacksData.get(cont.slot.getSlotIndex());
        switch (button.id) {
            case 0: {
                aData.ignoreDamage = button.getValue() == 0;
                button.setHoverText("gui.ignoreDamage." + button.getValue());
                break;
            }
            case 1: {
                aData.ignoreNBT = button.getValue() == 0;
                button.setHoverText("gui.ignoreNBT." + button.getValue());
                break;
            }
            case 2: {
                aData.type = EnumAvailabilityStackData.values()[(aData.type.ordinal() + 1) % EnumAvailabilityStackData.values().length];
                button.setHoverText("availability.hover.stack.type." + aData.type.name().toLowerCase());
                initGui();
                break;
            }
            case 66: {
                close();
                break;
            }
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        super.drawScreen(mouseX, mouseY, partialTicks);
        if (reset > 0) {
            reset--;
            if (reset == 0) { initGui(); }
        }
    }

    @Override
    public void initGui() {
        super.initGui();
        int x = guiLeft + 8;
        int y = guiTop + 3;
        // exit
        GuiNpcButton button = new GuiNpcButton(66, guiLeft + xSize / 2 - 35, guiTop + 189, 70, 20, "gui.done");
        button.setHoverText("hover.back");
        addButton(button);
        // data
        List<String> list = new ArrayList<>();
        List<String> suffixes = new ArrayList<>();
        List<ItemStack> stacks = new ArrayList<>();
        char c = ((char) 167);
        String select = "";
        for (int i = 0; i < cont.inv.getSizeInventory(); i++) {
            ItemStack stack = cont.inv.getStackInSlot(i);
            AvailabilityStackData aData = availability.stacksData.get(i);
            String name, suffix;
            if (stack.isEmpty()) {
                name = Util.instance.deleteColor(new TextComponentTranslation("info.item.cloner.empty.0").getFormattedText());
            }
            else {
                name = stack.getDisplayName();
                if (stack.getCount() > 1) {
                    name += " x" + stack.getCount();
                }
            }
            if (aData.type == EnumAvailabilityStackData.Always) {
                suffix = c + "aA";
            } else if (aData.type == EnumAvailabilityStackData.Contains) {
                suffix = c + "bC";
            } else {
                suffix = c + "cE";
            }
            String key = c + "7" + (i + 1) + ": " + c + "r" + name;
            list.add(key);
            stacks.add(stack);
            suffixes.add(suffix);
            if (i == cont.slot.getSlotIndex()) {
                select = key;
            }
        }
        if (scroll == null) { (scroll = new GuiCustomScroll(this, 0)).setSize(102, 107); }
        scroll.setList(list);
        scroll.setStacks(stacks);
        scroll.setSuffixes(suffixes);
        scroll.guiLeft = guiLeft + 70;
        scroll.guiTop = guiTop + 4;
        if (!select.isEmpty()) { scroll.setSelected(select); }
        addScroll(scroll);
        // ignore damage
        AvailabilityStackData aData = availability.stacksData.get(cont.slot.getSlotIndex());
        addLabel(new GuiNpcLabel(0, "gui.ignoreDamage", x, y + 2));
        button = new GuiNpcButton(0, x, y += 12, 50, 14, new String[] { "gui.yes", "gui.no" }, aData == null || aData.ignoreDamage ? 0 : 1);
        button.setHoverText("gui.ignoreDamage." + button.getValue());
        addButton(button);
        // ignore nbt
        addLabel(new GuiNpcLabel(1, "gui.ignoreNBT", x, (y += 16) + 2));
        button = new GuiNpcButton(1, x, y += 12, 50, 14, new String[] { "gui.yes", "gui.no" }, aData == null || aData.ignoreNBT ? 0 : 1);
        button.setHoverText("gui.ignoreNBT." + button.getValue());
        addButton(button);
        // type
        addLabel(new GuiNpcLabel(2, "gui.type", x, (y += 16) + 2));
        button = new GuiNpcButton(2, x, y + 12, 50, 14, "availability." + (aData == null ? "always" : aData.type.name().toLowerCase()));
        button.setHoverText("availability.hover.stack.type." + (aData == null ? "always" : aData.type.name().toLowerCase()));
        addButton(button);
        // id slot
        addLabel(new GuiNpcLabel(3, "ID: " + cont.slot.getSlotIndex(), x + 20, guiTop + 87));
    }

    @Override
    public void close() {
        super.close();
        if (parent != null) {
            displayGuiScreen(parent);
        }
    }

    @Override
    public void save() {
        if (setting != null) {
            NBTTagCompound compound = new NBTTagCompound();
            availability.writeToNBT(compound); // temp availability
            setting.availability.readFromNBT(compound); // edit availability
        }
    }

    @Override
    protected void handleMouseClick(@Nonnull Slot slotIn, int slotId, int mouseButton, @Nonnull ClickType type) {
        super.handleMouseClick(slotIn, slotId, mouseButton, type);
        if (slotIn == cont.slot) {
            reset = 3;
        }
    }

    @Override
    public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
        cont.slot.setSlotIndex(scroll.selected);
        scroll.selected = cont.slot.getSlotIndex();
        Client.sendData(EnumPacketServer.AvailabilitySlot, cont.slot.getSlotIndex());
        initGui();
    }

    @Override
    public void scrollDoubleClicked(String select, GuiCustomScroll scroll) {

    }

}
