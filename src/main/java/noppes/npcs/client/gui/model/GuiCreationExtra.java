package noppes.npcs.client.gui.model;

import java.lang.reflect.Method;
import java.util.*;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.entity.NPCRendererHelper;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.entity.EntityFakeLiving;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class GuiCreationExtra
extends GuiCreationScreenInterface
implements ICustomScrollListener {

	public abstract static class GuiType {
		public String name;

		public GuiType(String name) {
			this.name = name;
		}

		public void actionPerformed(GuiButton button) {
		}

		public void initGui() {
		}

		public void scrollClicked(int i, int j, int k, IGuiCustomScroll scroll) {
		}
	}

	class GuiTypeBoolean extends GuiType {
		private boolean bo;

		public GuiTypeBoolean(String name, boolean bo) {
			super(name);
			this.bo = bo;
		}

		@Override
		public void actionPerformed(GuiButton button) {
			if (button.id != 11) {
				return;
			}
			this.bo = ((GuiNpcButtonYesNo) button).getBoolean();
			if (this.name.equals("Child")) {
				GuiCreationExtra.this.playerdata.extra.setInteger("Age", this.bo ? -24000 : 0);
				GuiCreationExtra.this.playerdata.clearEntity();
			} else {
				GuiCreationExtra.this.playerdata.extra.setBoolean(this.name, this.bo);
				GuiCreationExtra.this.playerdata.clearEntity();
				GuiCreationExtra.this.updateTexture();
			}
		}

		@Override
		public void initGui() {
			GuiCreationExtra.this.addButton(new GuiNpcButtonYesNo(11, GuiCreationExtra.this.guiLeft + 120,
					GuiCreationExtra.this.guiTop + 50, 60, 20, this.bo));
		}
	}

	class GuiTypeByte extends GuiType {
		private final byte b;

		public GuiTypeByte(String name, byte b) {
			super(name);
			this.b = b;
		}

		@Override
		public void actionPerformed(GuiButton button) {
			if (button.id != 11) {
				return;
			}
			GuiCreationExtra.this.playerdata.extra.setByte(this.name, (byte) ((GuiNpcButton) button).getValue());
			GuiCreationExtra.this.playerdata.clearEntity();
			GuiCreationExtra.this.updateTexture();
		}

		@Override
		public void initGui() {
			GuiCreationExtra.this.addButton(new GuiButtonBiDirectional(
					11, GuiCreationExtra.this.guiLeft + 120, GuiCreationExtra.this.guiTop + 45, 50, 20, new String[] {
							"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" },
					this.b));
		}
	}

	class GuiTypeDoggyStyle extends GuiType {
		public GuiTypeDoggyStyle(String name) {
			super(name);
		}

		@Override
		public void actionPerformed(GuiButton button) {
			if (button.id != 11) {
				return;
			}
			EntityLivingBase entity = GuiCreationExtra.this.playerdata.getEntity(GuiCreationExtra.this.npc);
			GuiCreationExtra.this.playerdata.setExtra(entity, "breed", ((GuiNpcButton) button).getValue() + "");
			GuiCreationExtra.this.updateTexture();
		}

		@Override
		public void initGui() {
			Enum<?> breed = null;
			try {
				Method method = GuiCreationExtra.this.entity.getClass().getMethod("getBreedID", Class[].class);
				breed = (Enum<?>) method.invoke(GuiCreationExtra.this.entity, (Object) new Class[0]);
			} catch (Exception e) { LogWriter.error("Error:", e); }
            if (breed != null) {
				GuiCreationExtra.this.addButton(new GuiButtonBiDirectional(11, GuiCreationExtra.this.guiLeft + 120,
								GuiCreationExtra.this.guiTop + 45, 50, 20,
								new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13",
										"14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26"},
								breed.ordinal()));
			}
		}
	}

	class GuiTypePixelmon extends GuiType {
		public GuiTypePixelmon(String name) {
			super(name);
		}

		@Override
		public void initGui() {
			GuiCustomScroll scroll = new GuiCustomScroll(GuiCreationExtra.this, 1);
			scroll.setSize(120, 200);
			scroll.guiLeft = GuiCreationExtra.this.guiLeft + 120;
			scroll.guiTop = GuiCreationExtra.this.guiTop + 20;
			GuiCreationExtra.this.addScroll(scroll);
			scroll.setList(PixelmonHelper.getPixelmonList());
			scroll.setSelected(PixelmonHelper.getName(GuiCreationExtra.this.entity));
		}

		@Override
		public void scrollClicked(int i, int j, int k, IGuiCustomScroll scroll) {
			String name = scroll.getSelected();
			GuiCreationExtra.this.playerdata.setExtra(GuiCreationExtra.this.entity, "name", name);
			GuiCreationExtra.this.updateTexture();
		}
	}

	public String[] booleanTags;

	private Map<String, GuiType> data;

	private final String[] ignoredTags;

	private GuiCustomScroll scroll;

	private GuiType selected;

	public GuiCreationExtra(EntityNPCInterface npc) {
		super(npc);
		this.ignoredTags = new String[] { "CanBreakDoors", "Bred", "PlayerCreated", "HasReproduced" };
		this.booleanTags = new String[0];
		this.data = new HashMap<>();
		this.active = 2;
	}

	@Override
	protected void actionPerformed(@Nonnull GuiButton btn) {
		super.actionPerformed(btn);
		if (this.selected != null) {
			this.selected.actionPerformed(btn);
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (!CustomNpcs.ShowDescriptions) {
			return;
		}
		String part = this.scroll.getSelected().toLowerCase();
		if (this.getButton(1) != null && this.getButton(1).isHovered()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.entity").getFormattedText());
		} else if (this.getButton(2) != null && this.getButton(2).isHovered()) {
			this.setHoverText(new TextComponentTranslation("display.hover.extra").getFormattedText());
		} else if (this.getButton(4) != null && this.getButton(4).isHovered()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.save").getFormattedText());
		} else if (this.getButton(5) != null && this.getButton(5).isHovered()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part.load").getFormattedText());
		} else if (this.getButton(11) != null && this.getButton(11).isHovered()) {
			this.setHoverText(new TextComponentTranslation("display.hover.part." + part).getFormattedText());
		} else if (this.getButton(66) != null && this.getButton(66).isHovered()) {
			this.setHoverText(new TextComponentTranslation("hover.back").getFormattedText());
		} else if (this.scroll != null && this.scroll.hover > -1) {
			this.drawHoveringText(Collections.singletonList(new TextComponentTranslation(
                            "display.hover.part." + this.scroll.getList().get(this.scroll.hover).toLowerCase())
                            .getFormattedText()),
					mouseX, mouseY, this.fontRenderer);
		} else {
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

	public Map<String, GuiType> getData(EntityLivingBase entity) {
		Map<String, GuiType> data = new HashMap<>();
		NBTTagCompound compound = this.getExtras(entity);
		Set<String> keys = compound.getKeySet();
		for (String name : keys) {
			if (this.isIgnored(name)) {
				continue;
			}
			NBTBase base = compound.getTag(name);
			if (name.equals("Age")) {
				data.put("Child", new GuiTypeBoolean("Child", entity.isChild()));
			} else if (name.equals("Color") && base.getId() == 1) {
				data.put("Color", new GuiTypeByte("Color", compound.getByte("Color")));
			} else {
				if (base.getId() != 1) {
					continue;
				}
				byte b = ((NBTTagByte) base).getByte();
				if (b != 0 && b != 1) {
					continue;
				}
				if (this.playerdata.extra.hasKey(name)) {
					b = this.playerdata.extra.getByte(name);
				}
				data.put(name, new GuiTypeBoolean(name, b == 1));
			}
		}
		if (PixelmonHelper.isPixelmon(entity)) {
			data.put("Model", new GuiTypePixelmon("Model"));
		}
		if (Objects.equals(EntityList.getEntityString(entity), "tgvstyle.Dog")) {
			data.put("Breed", new GuiTypeDoggyStyle("Breed"));
		}
		return data;
	}

	private NBTTagCompound getExtras(EntityLivingBase entity) {
		NBTTagCompound fake = new NBTTagCompound();
		new EntityFakeLiving(entity.world).writeEntityToNBT(fake);
		NBTTagCompound compound = new NBTTagCompound();
		try {
			entity.writeEntityToNBT(compound);
		} catch (Exception e) { LogWriter.error("Error:", e); }
		Set<String> keys = fake.getKeySet();
		for (String name : keys) {
			compound.removeTag(name);
		}
		return compound;
	}

	@Override
	public void initGui() {
		super.initGui();
		if (this.entity == null) {
			this.openGui(new GuiCreationParts(this.npc));
			return;
		}
		if (this.scroll == null) {
			this.data = this.getData(this.entity);
			this.scroll = new GuiCustomScroll(this, 0);
			List<String> list = new ArrayList<>(this.data.keySet());
			this.scroll.setList(list);
			if (list.isEmpty()) {
				return;
			}
			this.scroll.setSelected(list.get(0));
		}
		this.selected = this.data.get(this.scroll.getSelected());
		if (this.selected == null) {
			return;
		}
		this.scroll.guiLeft = this.guiLeft;
		this.scroll.guiTop = this.guiTop + 46;
		this.scroll.setSize(100, this.ySize - 74);
		this.addScroll(this.scroll);
		this.selected.initGui();
	}

	private boolean isIgnored(String tag) {
		for (String s : this.ignoredTags) {
			if (s.equals(tag)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, IGuiCustomScroll scroll) {
		if (scroll.getID() == 0) {
			this.initGui();
		} else if (this.selected != null) {
			this.selected.scrollClicked(mouseX, mouseY, mouseButton, scroll);
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, IGuiCustomScroll scroll) {
	}

	@SuppressWarnings("unchecked")
	private void updateTexture() {
		EntityLivingBase entity = this.playerdata.getEntity(this.npc);
		@SuppressWarnings("rawtypes")
		RenderLivingBase render = (RenderLivingBase) this.mc.getRenderManager().getEntityRenderObject(entity);
        assert render != null;
        this.npc.display.setSkinTexture(NPCRendererHelper.getTexture(render, entity));
	}

}
