package noppes.npcs.client.gui.model;

import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.ModContainer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.api.mixin.client.renderer.entity.IRenderLivingBaseMixin;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.client.renderer.RenderCustomNpc;
import noppes.npcs.containers.ContainerLayer;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;
import java.util.*;

public class GuiCreationLayers  extends GuiCreationScreenInterface implements ICustomScrollListener {

    private GuiCustomScroll scroll;

    public GuiCreationLayers(EntityNPCInterface npc, ContainerLayer container) {
        super(npc, container);
        closeOnEsc = true;
        active = 6;
        xOffset = 60;
    }

    @Override
    public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
        super.buttonEvent(button, mouseButton);
    }

    @Override
    public void initGui() {
        GuiNpcTextField.unfocus();
        super.initGui();
        int x = guiLeft;
        int y = guiTop + 48;
        int h = 152;
        if (getButton(2) == null && getButton(3) == null) {
            y -= 22;
            h += 22;
        }
        if (scroll == null) { scroll = new GuiCustomScroll(this, 1, true, true); }
        addLabel(new GuiNpcLabel(20, "part.layers.info.1", x, y)
                .setColor(CustomNpcs.MainColor.getRGB()));
        Render<Entity> render = mc.getRenderManager().getEntityRenderObject(npc);
        Map<String, List<String>> map = new TreeMap<>();
        Map<String, String> sfx = new HashMap<>();
        if (render instanceof RenderCustomNpc) {
            for (LayerRenderer<?> layer : ((RenderCustomNpc<?>) render).getLayers()) {
                String name = layer.getClass().getSimpleName();
                if (name.isEmpty()) { continue; }
                map.put(name, Collections.singletonList(((char) 167) + "7Layer from " +
                        ((char) 167) + "aRenderCustomNpc" +
                        ((char) 167) + "7; in mod:" + ((char) 167) + "6 CustomNpc"));
                sfx.put(name, ((char) 167) + "6CN");
            }
        }
        EntityLivingBase customModel = playerdata.getEntity(npc);
        if (customModel != null) {
            render = mc.getRenderManager().getEntityRenderObject(customModel);
            if (render instanceof RenderLivingBase) {
                for (LayerRenderer<?> layer : ((IRenderLivingBaseMixin) render).npcs$getLayers()) {
                    String name = layer.getClass().getSimpleName();
                    if (name.isEmpty()) { continue; }
                    if (!map.containsKey(name)) {
                        String modName = "";
                        for (ModContainer mod : Loader.instance().getActiveModList()) {
                            if (mod.getOwnedPackages().contains(render.getClass().getName())) {
                                modName = mod.getModId();
                                break;
                            }
                        }
                        if (modName.isEmpty()) {
                            modName = "in" + ((char) 167) + "e Minecraft";
                            sfx.put(name, ((char) 167) + "eMC");
                        } else {
                            modName = "in mod: " + ((char) 167) + "e " + modName;
                            sfx.put(name, ((char) 167) + "c" + String.valueOf(modName.charAt(0)).toUpperCase());
                        }
                        map.put(name, Collections.singletonList(((char) 167) + "7Layer from " +
                                ((char) 167) + "b" + render.getClass().getSimpleName() +
                                ((char) 167) + "7; " +modName));
                    }
                }
            }
        }
        LinkedHashMap<Integer, List<String>> hts = new LinkedHashMap<>();
        List<String> suffixes = new ArrayList<>();
        int i = 0;
        for (String key : map.keySet()) {
            hts.put(i++, map.get(key));
            suffixes.add(sfx.get(key));
        }
        scroll.setUnsortedList(new ArrayList<>(map.keySet()))
                .setSize(121, h)
                .setSelectedList(new HashSet<>(Arrays.asList(playerdata.getDisableLayers())))
                .setHoverTexts(hts)
                .setSuffixes(suffixes);
        y += 12;
        scroll.guiLeft = x;
        scroll.guiTop = y;
        addScroll(scroll);
        for (Slot slot : inventorySlots.inventorySlots) {
            slot.xPos = -5000;
            slot.yPos = -5000;
        }
    }

    @Override
    public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
        playerdata.setDisableLayers(scroll.getSelectedList().toArray(new String[0]));
        initGui();
    }

    @Override
    public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { }

}
