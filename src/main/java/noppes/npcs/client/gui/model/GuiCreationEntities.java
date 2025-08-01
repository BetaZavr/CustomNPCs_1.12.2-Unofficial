package noppes.npcs.client.gui.model;

import java.lang.reflect.Modifier;
import java.util.*;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.entity.NPCRendererHelper;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.Slot;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.client.gui.util.IGuiCustomScroll;
import noppes.npcs.containers.ContainerLayer;
import noppes.npcs.entity.EntityNPC64x32;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.EntityNpcAlex;
import noppes.npcs.entity.EntityNpcClassicPlayer;

import javax.annotation.Nonnull;

public class GuiCreationEntities
extends GuiCreationScreenInterface
implements ICustomScrollListener {

	public HashMap<String, Class<? extends EntityLivingBase>> data = new HashMap<>();
	private final List<String> list;
	private boolean resetToSelected = true;
	private GuiCustomScroll scroll;

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
                if (name.toLowerCase().contains("customnpc")) {
					continue;
				}
				this.data.put(name, c.asSubclass(EntityLivingBase.class));
			} catch (Exception e) { LogWriter.error(e); }
		}
		this.data.put("NPC 64x32", EntityNPC64x32.class);
		this.data.put("NPC Alex Arms", EntityNpcAlex.class);
		this.data.put("NPC Classic Player", EntityNpcClassicPlayer.class);
		(this.list = new ArrayList<>(data.keySet())).add("NPC");
		this.list.sort(String.CASE_INSENSITIVE_ORDER);
		this.active = 1;
		this.xOffset = 60;
	}

	@Override
	protected void actionPerformed(@Nonnull GuiButton btn) {
		super.actionPerformed(btn);
		if (btn.id == 10) {
			playerdata.setEntityClass(null);
			resetToSelected = true;
			npc.display.setSkinTexture(CustomNpcs.MODID + ":textures/entity/humanmale/steve.png");
			npc.reset();
			npc.display.width = npc.baseWidth;
			npc.display.height = npc.baseHeight;
			initGui();
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.ShowDescriptions) {
			return;
		}
		if (this.getButton(1) != null && this.getButton(1).isHovered()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.entity").getFormattedText());
		}
		else if (this.getButton(2) != null && this.getButton(2).isHovered()) {
			this.setHoverText(new TextComponentTranslation("display.hover.extra").getFormattedText());
		}
		else if (this.getButton(4) != null && this.getButton(4).isHovered()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.save").getFormattedText());
		}
		else if (this.getButton(5) != null && this.getButton(5).isHovered()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.load").getFormattedText());
		}
		else if (this.getButton(10) != null && this.getButton(10).isHovered()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.reset").getFormattedText());
		}
		else if (this.getButton(66) != null && this.getButton(66).isHovered()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
		else {
			for (GuiButton b : this.buttonList) {
				if (b != null && b.isMouseOver()) {
					if (b.id == 500) {
						this.setHoverText(new TextComponentTranslation("display.hover.part.rotate").getFormattedText());
					}
				}
			}
		}
		drawHoverText(null);
	}

	@Override
	public void initGui() {
		super.initGui();
		if (this.scroll == null) {
			(this.scroll = new GuiCustomScroll(this, 0)).setListNotSorted(this.list);
		}
		this.scroll.guiLeft = this.guiLeft;
		this.scroll.guiTop = this.guiTop + 68;
		this.scroll.setSize(100, this.ySize - 96);
		String selected = "NPC";
		if (this.entity != null) {
			for (Map.Entry<String, Class<? extends EntityLivingBase>> en : this.data.entrySet()) {
				if (en.getValue().toString().equals(this.entity.getClass().toString())) {
					selected = en.getKey();
				}
			}
		}
		
		this.addButton(new GuiNpcButton(10, this.guiLeft, this.guiTop + 46, 120, 20, "Reset To NPC"));
		this.getButton(10).setIsVisible(!selected.equals("NPC"));
		
		this.scroll.setSelected(selected);
		if (this.resetToSelected) {
			this.scroll.scrollTo(this.scroll.getSelected());
			this.resetToSelected = false;
		}
		this.addScroll(this.scroll);
		for (Slot slot : inventorySlots.inventorySlots) {
			slot.xPos = -5000;
			slot.yPos = -5000;
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		this.playerdata.setEntityClass(this.data.get(scroll.getSelected()));
		EntityLivingBase entity = this.playerdata.getEntity(this.npc);
		if (entity != null) {
			@SuppressWarnings("rawtypes")
			RenderLivingBase render = (RenderLivingBase) this.mc.getRenderManager().getEntityClassRenderObject(entity.getClass());
			if (!NPCRendererHelper.getTexture(render, entity).equals(TextureMap.LOCATION_MISSING_TEXTURE.toString())) {
				npc.display.setSkinTexture(NPCRendererHelper.getTexture(render, entity));
			}
		} else {
			npc.display.setSkinTexture(CustomNpcs.MODID + ":textures/entity/humanmale/steve.png");
		}
		npc.reset();
		npc.display.width = npc.baseWidth;
		npc.display.height = npc.baseHeight;
		initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, IGuiCustomScroll scroll) { }
	
}
