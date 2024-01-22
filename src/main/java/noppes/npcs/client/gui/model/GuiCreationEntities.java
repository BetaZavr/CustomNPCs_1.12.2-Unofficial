package noppes.npcs.client.gui.model;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.entity.NPCRendererHelper;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.registry.EntityEntry;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.ICustomScrollListener;
import noppes.npcs.entity.EntityNPC64x32;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.EntityNpcAlex;
import noppes.npcs.entity.EntityNpcClassicPlayer;

public class GuiCreationEntities
extends GuiCreationScreenInterface
implements ICustomScrollListener {
	
	public HashMap<String, Class<? extends EntityLivingBase>> data;
	private List<String> list;
	private boolean resetToSelected;
	private GuiCustomScroll scroll;

	public GuiCreationEntities(EntityNPCInterface npc) {
		super(npc);
		this.data = new HashMap<String, Class<? extends EntityLivingBase>>();
		this.resetToSelected = true;
		for (EntityEntry ent : ForgeRegistries.ENTITIES.getValuesCollection()) {
			String name = ent.getName();
			Class<? extends Entity> c = (Class<? extends Entity>) ent.getEntityClass();
			try {
				if (!EntityLiving.class.isAssignableFrom(c) || c.getConstructor(World.class) == null
						|| Modifier.isAbstract(c.getModifiers()) || !(Minecraft.getMinecraft().getRenderManager()
								.getEntityClassRenderObject(c) instanceof RenderLivingBase)) {
					continue;
				}
				String s = name;
				if (s.toLowerCase().contains("customnpc")) {
					continue;
				}
				this.data.put(name, c.asSubclass(EntityLivingBase.class));
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException ex) {
			}
		}
		this.data.put("NPC 64x32", (Class<? extends EntityLivingBase>) EntityNPC64x32.class);
		this.data.put("NPC Alex Arms", (Class<? extends EntityLivingBase>) EntityNpcAlex.class);
		this.data.put("NPC Classic Player", (Class<? extends EntityLivingBase>) EntityNpcClassicPlayer.class);
		(this.list = new ArrayList<String>(this.data.keySet())).add("NPC");
		Collections.sort(this.list, String.CASE_INSENSITIVE_ORDER);
		this.active = 1;
		this.xOffset = 60;
	}

	@Override
	protected void actionPerformed(GuiButton btn) {
		super.actionPerformed(btn);
		if (btn.id == 10) {
			this.playerdata.setEntityClass(null);
			this.resetToSelected = true;
			this.initGui();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		this.addButton(new GuiNpcButton(10, this.guiLeft, this.guiTop + 46, 120, 20, "Reset To NPC"));
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
		this.getButton(10).visible = !selected.equals("NPC");
		this.scroll.setSelected(selected);
		if (this.resetToSelected) {
			this.scroll.scrollTo(this.scroll.getSelected());
			this.resetToSelected = false;
		}
		this.addScroll(this.scroll);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void scrollClicked(int i, int j, int k, GuiCustomScroll scroll) {
		this.playerdata.setEntityClass(this.data.get(scroll.getSelected()));
		Entity entity = this.playerdata.getEntity(this.npc);
		if (entity != null) {
			@SuppressWarnings("rawtypes")
			RenderLivingBase render = (RenderLivingBase) this.mc.getRenderManager().getEntityClassRenderObject(entity.getClass());
			if (!NPCRendererHelper.getTexture(render, (EntityLivingBase) entity).equals(TextureMap.LOCATION_MISSING_TEXTURE.toString())) {
				this.npc.display.setSkinTexture(NPCRendererHelper.getTexture(render, (EntityLivingBase) entity));
			}
		} else {
			this.npc.display.setSkinTexture(CustomNpcs.MODID + ":textures/entity/humanmale/steve.png");
		}
		this.initGui();
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) {
	}
	
	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.showDescriptions) { return; }
		if (this.getButton(1)!=null && this.getButton(1).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.entity").getFormattedText());
		} else if (this.getButton(2)!=null && this.getButton(2).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.extra").getFormattedText());
		}else if (this.getButton(4)!=null && this.getButton(4).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.save").getFormattedText());
		} else if (this.getButton(5)!=null && this.getButton(5).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.load").getFormattedText());
		} else if (this.getButton(10)!=null && this.getButton(10).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.reset").getFormattedText());
		} else if (this.getButton(66)!=null && this.getButton(66).isMouseOver()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		}
		else {
			for (GuiButton b : this.buttonList) {
				if (b!=null && b.isMouseOver()) {
					if (b.id==500) { this.setHoverText(new TextComponentTranslation("display.hover.part.rotate").getFormattedText()); }
				}
			}
		}
		if (this.hoverText != null) {
			this.drawHoveringText(Arrays.asList(this.hoverText), mouseX, mouseY, this.fontRenderer);
			this.hoverText = null;
		}
	}
}
