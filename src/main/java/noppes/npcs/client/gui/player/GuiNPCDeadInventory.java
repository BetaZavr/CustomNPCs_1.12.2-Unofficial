package noppes.npcs.client.gui.player;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.inventory.Slot;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.client.CustomNpcResourceListener;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPlayerPacket;
import noppes.npcs.containers.ContainerDead;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;

import java.util.HashMap;
import java.util.Vector;

public class GuiNPCDeadInventory
extends GuiContainerNPCInterface
implements ICustomScrollListener,  IScrollData {

    private final ContainerDead container;
    private GuiCustomScroll scroll;
    private boolean wait = false;

    public GuiNPCDeadInventory(EntityNPCInterface npc, ContainerDead cont) {
        super(npc, cont);
        container = cont;
        title = "";
        xSize = 177;
        ySize = container.size + 152;
        closeOnEsc = true;
        drawDefaultBackground = false;
        setBackground("largebg.png");
    }

    @Override
    public void drawDefaultBackground() {
        super.drawDefaultBackground();
        if (npc.isEntityAlive()) { close(); }
        int size = container.size - 1;
        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);

        GlStateManager.pushMatrix();
        GlStateManager.translate(guiLeft, guiTop + 20, 0.0f);
        GlStateManager.scale(bgScale, bgScale, bgScale);
        // background
        mc.getTextureManager().bindTexture(background);
        int w = xSize - 4;
        // up
        drawTexturedModalRect(0, 0, 0, 0, w, ySize - 34);
        int sh = 227;
        int h = 4;
        if (size > 0) {
            sh -= size * 18 - size;
            h += size * 18 - size;
        }
        // down
        drawTexturedModalRect(0, ySize - 34, 0, sh, w, h);
        // left
        if (player.capabilities.isCreativeMode) {
            GlStateManager.translate(xSize - 5, 0.0f, 0.0f);
            drawTexturedModalRect(0, 0, 84, 0, 108, ySize - 34);
            drawTexturedModalRect(0, ySize - 34, 84, sh, 108, h);
            GlStateManager.translate(5 - xSize, 0.0f, 0.0f);
        } else {
            drawTexturedModalRect(w, 0, 189, 0, 3, ySize - 34);
            drawTexturedModalRect(w, ySize - 34, 189, sh, 3, h);
        }
        // inventory slots
        mc.getTextureManager().bindTexture(GuiNPCInterface.RESOURCE_SLOT);
        GlStateManager.translate(-1.0f, -21.0f, 0.0f);
        if (size > 0) { GlStateManager.translate(0.0f, size * 9.0f, 0.0f); }
        for (Slot slot : container.inventorySlots) { drawTexturedModalRect(slot.xPos, slot.yPos, 0, 0, 18, 18); }
        GlStateManager.popMatrix();
        // title
        ITextComponent textComponent= new TextComponentTranslation("inv.loot.0", npc.getName());
        if (container.pos > -1) { textComponent.appendSibling(new TextComponentTranslation("inv.loot.1", container.playerParent)); }
        String customTitle = textComponent.getFormattedText();
        mc.fontRenderer.drawString(customTitle, (width - mc.fontRenderer.getStringWidth(customTitle)) / 2, guiTop + 24, CustomNpcResourceListener.DefaultTextColor);
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (wait) {
            drawWait();
            return;
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    public void initPacket() {
        NoppesUtilPlayer.sendData(EnumPlayerPacket.DropsData);
    }

    @Override
    public void initGui() {
        super.initGui();
        int size = container.size - 1;
        if (size > 0) { guiTop -= size * 9; }
        if (player.capabilities.isCreativeMode) {
            if (scroll == null) { (scroll = new GuiCustomScroll(this, 0)).setSize(100, ySize - 50); }
            scroll.guiLeft = guiLeft + xSize - 1;
            scroll.guiTop = guiTop + 35;
            addScroll(scroll);
            addLabel(new GuiNpcLabel(0, "inv.loot.players", guiLeft + xSize, guiTop + 25));
        }
    }

    @Override
    public void setData(Vector<String> list, HashMap<String, Integer> dataMap) {
        scroll.setList(list);
        scroll.setSelected(container.playerParent);
    }

    @Override
    public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
        if (container.playerParent.equals(scroll.getSelected())) { return; }
        wait = true;
        NoppesUtilPlayer.sendData(EnumPlayerPacket.DropData, Util.instance.deleteColor(scroll.getSelected()));
    }

    @Override
    public void scrollDoubleClicked(String select, IGuiCustomScroll scroll) { }

    @Override
    public void setSelected(String select) { }

}
