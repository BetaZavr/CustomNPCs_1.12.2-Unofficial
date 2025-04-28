package noppes.npcs.api.event;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.inventory.Container;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.entity.EntityNPCInterface;

@SideOnly(Side.CLIENT)
public class ClientEvent extends CustomNPCsEvent {

    public EntityNPCInterface npc;
    public GuiScreen returnGui;

    @Cancelable
    public static class PreGetGuiCustomNpcs extends ClientEvent {

        public EnumGuiType guiType;
        public Container container;
        public int x;
        public int y;
        public int z;

        public PreGetGuiCustomNpcs(EntityNPCInterface npc, EnumGuiType gui, Container containerIn, int xIn, int yIn, int zIn) {
            super(npc, null);
            guiType = gui;
            container = containerIn;
            x = xIn;
            y = yIn;
            z = zIn;
        }

    }

    @Cancelable
    public static class PostGetGuiCustomNpcs extends ClientEvent {

        public EnumGuiType guiType;
        public Container container;
        public int x;
        public int y;
        public int z;

        public PostGetGuiCustomNpcs(EntityNPCInterface npc, EnumGuiType gui, Container containerIn, int xIn, int yIn, int zIn, GuiScreen returnGuiIn) {
            super(npc, returnGuiIn);
            guiType = gui;
            container = containerIn;
            x = xIn;
            y = yIn;
            z = zIn;
        }

    }

    @Cancelable
    public static class NextToGuiCustomNpcs extends ClientEvent {

        public GuiScreen parent;

        public NextToGuiCustomNpcs(EntityNPCInterface npc, GuiScreen parentIn, GuiScreen returnGuiIn) {
            super(npc, returnGuiIn);
            parent = parentIn;
        }

    }

    public ClientEvent(EntityNPCInterface npcIn, GuiScreen returnGuiIn) {
        super();
        npc = npcIn;
        returnGui = returnGuiIn;
    }

}
