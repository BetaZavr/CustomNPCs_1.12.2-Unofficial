package noppes.npcs.client.gui.model;

import java.lang.reflect.Modifier;
import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.NPCRendererHelper;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.Slot;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.containers.ContainerLayer;
import noppes.npcs.entity.EntityNPC64x32;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.EntityNpcAlex;
import noppes.npcs.entity.EntityNpcClassicPlayer;

import javax.annotation.Nonnull;

public class GuiCreationEntities extends GuiCreationScreenInterface implements ICustomScrollListener {

	protected final List<String> list;
	protected GuiCustomScroll scroll;
	protected boolean resetToSelected = true;
	public final HashMap<String, Class<? extends EntityLivingBase>> data = new HashMap<>();

	public GuiCreationEntities(EntityNPCInterface npc, ContainerLayer container) {
		super(npc, container);
		for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
			String name = ent.getName();
			Class<? extends Entity> c = ent.getEntityClass();
			try {
				if (!EntityLiving.class.isAssignableFrom(c) || Modifier.isAbstract(c.getModifiers()) || !(Minecraft.getMinecraft().getRenderManager()
                        .getEntityClassRenderObject(c) instanceof RenderLivingBase)) {
					continue;
				}
                if (name.toLowerCase().contains("customnpc")) { continue; }
				data.put(name, c.asSubclass(EntityLivingBase.class));
			} catch (Exception e) { LogWriter.error(e); }
		}
		data.put("NPC 64x32", EntityNPC64x32.class);
		data.put("NPC Alex Arms", EntityNpcAlex.class);
		data.put("NPC Classic Player", EntityNpcClassicPlayer.class);
		(list = new ArrayList<>(data.keySet())).add("NPC");
		list.sort(String.CASE_INSENSITIVE_ORDER);
		active = 1;
		xOffset = 60;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton == 0 && button.id == 10) {
			playerdata.setEntityClass(null);
			resetToSelected = true;
			npc.display.setSkinTexture(CustomNpcs.MODID + ":textures/entity/humanmale/steve.png");
			npc.reset();
			npc.display.width = npc.baseWidth;
			npc.display.height = npc.baseHeight;
			initGui();
		}
		super.buttonEvent(button, mouseButton);
	}

	@Override
	public void initGui() {
		super.initGui();
		if (scroll == null) { scroll = new GuiCustomScroll(this, 0).setUnsortedList(list); }
		scroll.guiLeft = guiLeft;
		scroll.guiTop = guiTop + 46;
		scroll.setSize(121, ySize - 74);
		String selected = "NPC";
		if (entity != null) {
			for (Map.Entry<String, Class<? extends EntityLivingBase>> en : data.entrySet()) {
				if (en.getValue().toString().equals(entity.getClass().toString())) { selected = en.getKey(); }
			}
		}
		addButton(new GuiNpcButton(10, guiLeft, guiTop + 23, 120, 20, "Reset To NPC")
				.setIsVisible(!selected.equals("NPC")));
		scroll.setSelected(selected);
		if (resetToSelected) {
			scroll.scrollTo(scroll.getSelected());
			resetToSelected = false;
		}
		addScroll(scroll);
		for (Slot slot : inventorySlots.inventorySlots) {
			slot.xPos = -5000;
			slot.yPos = -5000;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		playerdata.setEntityClass(data.get(scroll.getSelected()));
		EntityLivingBase entity = playerdata.getEntity(npc);
		if (entity != null) {
			@SuppressWarnings("rawtypes")
			RenderLivingBase render = (RenderLivingBase) mc.getRenderManager().getEntityClassRenderObject(entity.getClass());
			if (!NPCRendererHelper.getTexture(render, entity).equals(TextureMap.LOCATION_MISSING_TEXTURE.toString())) {
				npc.display.setSkinTexture(NPCRendererHelper.getTexture(render, entity));
			}
		}
		else { npc.display.setSkinTexture(CustomNpcs.MODID + ":textures/entity/humanmale/steve.png"); }
		npc.reset();
		npc.display.width = npc.baseWidth;
		npc.display.height = npc.baseHeight;
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) { }
	
}
