package noppes.npcs.client.gui.model;

import java.lang.reflect.Method;
import java.util.*;

import net.minecraft.client.renderer.entity.NPCRendererHelper;
import net.minecraft.client.renderer.entity.RenderLivingBase;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagByte;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.LogWriter;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.containers.ContainerLayer;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.entity.EntityFakeLiving;
import noppes.npcs.entity.EntityNPCInterface;

import javax.annotation.Nonnull;

public class GuiCreationExtra extends GuiCreationScreenInterface implements ICustomScrollListener {

	public abstract static class GuiType {

		public String name;

		public GuiType(String nameIn) { name = nameIn; }

		public void buttonEvent(GuiNpcButton button) { }

		public void initGui() { }

		void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) { }

	}

	protected class GuiTypeBoolean extends GuiType {

		protected boolean bo;

		public GuiTypeBoolean(String name, boolean boIn) {
			super(name);
			bo = boIn;
		}

		@Override
		public void buttonEvent(GuiNpcButton button) {
			if (button.id != 11) { return; }
			bo = ((GuiNpcButtonYesNo) button).getBoolean();
			if (name.equals("Child")) {
				playerdata.extra.setInteger("Age", bo ? -24000 : 0);
				playerdata.clearEntity();
			} else {
				playerdata.extra.setBoolean(name, bo);
				playerdata.clearEntity();
				updateTexture();
			}
		}

		@Override
		public void initGui() {
			addButton(new GuiNpcButtonYesNo(11, guiLeft + 120, guiTop + 50, 60, 20, bo));
		}

	}

	protected class GuiTypeByte extends GuiType {

		protected final byte b;

		public GuiTypeByte(String name, byte byteIn) {
			super(name);
			b = byteIn;
		}

		@Override
		public void buttonEvent(GuiNpcButton button) {
			if (button.id != 11) { return; }
			playerdata.extra.setByte(name, (byte) button.getValue());
			playerdata.clearEntity();
			updateTexture();
		}

		@Override
		public void initGui() {
			addButton(new GuiButtonBiDirectional(11, guiLeft + 120, guiTop + 45, 50, 20,
					new String[] { "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15" }, b));
		}

	}

	protected class GuiTypeDoggyStyle extends GuiType {

		public GuiTypeDoggyStyle(String name) { super(name); }

		@Override
		public void buttonEvent(@Nonnull GuiNpcButton button) {
			if (button.id != 11) { return; }
			EntityLivingBase entity = playerdata.getEntity(npc);
			playerdata.setExtra(entity, "breed", button.getValue() + "");
			updateTexture();
		}

		@Override
		public void initGui() {
			Enum<?> breed = null;
			try {
				Method method = entity.getClass().getMethod("getBreedID", Class[].class);
				breed = (Enum<?>) method.invoke(entity, (Object) new Class[0]);
			} catch (Exception e) { LogWriter.error(e); }
            if (breed != null) {
				addButton(new GuiButtonBiDirectional(11, guiLeft + 120,
								guiTop + 45, 50, 20,
								new String[]{"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13",
										"14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26"},
								breed.ordinal()));
			}
		}
	}

	protected class GuiTypePixelmon extends GuiType {

		public GuiTypePixelmon(String name) { super(name); }

		@Override
		public void initGui() {
			GuiCustomScroll scroll = new GuiCustomScroll(GuiCreationExtra.this, 1);
			scroll.setSize(120, 200);
			scroll.guiLeft = guiLeft + 120;
			scroll.guiTop = guiTop + 20;
			addScroll(scroll);
			scroll.setList(PixelmonHelper.getPixelmonList());
			scroll.setSelected(PixelmonHelper.getName(entity));
		}

		@Override
		public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
			String name = scroll.getSelected();
			playerdata.setExtra(entity, "name", name);
			updateTexture();
		}

	}

	public String[] booleanTags;

	private Map<String, GuiType> data;

	private final String[] ignoredTags;

	private GuiCustomScroll scroll;

	private GuiType selected;

	public GuiCreationExtra(EntityNPCInterface npc, ContainerLayer container) {
		super(npc, container);
		ignoredTags = new String[] { "CanBreakDoors", "Bred", "PlayerCreated", "HasReproduced" };
		booleanTags = new String[0];
		data = new HashMap<>();
		active = 2;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton == 1 && selected != null) { selected.buttonEvent(button); }
		super.buttonEvent(button, mouseButton);
	}

	public Map<String, GuiType> getData(EntityLivingBase entity) {
		Map<String, GuiType> data = new HashMap<>();
		NBTTagCompound compound = getExtras(entity);
		Set<String> keys = compound.getKeySet();
		for (String name : keys) {
			if (isIgnored(name)) { continue; }
			NBTBase base = compound.getTag(name);
			if (name.equals("Age")) { data.put("Child", new GuiTypeBoolean("Child", entity.isChild())); }
			else if (name.equals("Color") && base.getId() == 1) { data.put("Color", new GuiTypeByte("Color", compound.getByte("Color"))); }
			else {
				if (base.getId() != 1) { continue; }
				byte b = ((NBTTagByte) base).getByte();
				if (b != 0 && b != 1) { continue; }
				if (playerdata.extra.hasKey(name)) { b = playerdata.extra.getByte(name); }
				data.put(name, new GuiTypeBoolean(name, b == 1));
			}
		}
		if (PixelmonHelper.isPixelmon(entity)) { data.put("Model", new GuiTypePixelmon("Model")); }
		if (Objects.equals(EntityList.getEntityString(entity), "tgvstyle.Dog")) { data.put("Breed", new GuiTypeDoggyStyle("Breed")); }
		return data;
	}

	private NBTTagCompound getExtras(EntityLivingBase entity) {
		NBTTagCompound fake = new NBTTagCompound();
		new EntityFakeLiving(entity.world).writeEntityToNBT(fake);
		NBTTagCompound compound = new NBTTagCompound();
		try { entity.writeEntityToNBT(compound); } catch (Exception e) { LogWriter.error(e); }
		Set<String> keys = fake.getKeySet();
		for (String name : keys) { compound.removeTag(name); }
		return compound;
	}

	@Override
	public void initGui() {
		super.initGui();
		if (entity == null) {
			openGui(new GuiCreationParts(npc, (ContainerLayer) inventorySlots));
			return;
		}
		if (scroll == null) {
			data = getData(entity);
			scroll = new GuiCustomScroll(this, 0);
			List<String> list = new ArrayList<>(data.keySet());
			scroll.setList(list);
			if (list.isEmpty()) { return; }
			scroll.setSelected(list.get(0));
		}
		scroll.guiLeft = guiLeft;
		scroll.guiTop = guiTop + 46;
		if (scroll.hasSelected()) { scroll.setHoverText("display.hover.part." + scroll.getList().get(scroll.getSelect()).toLowerCase()); }
		addScroll(scroll.setSize(100, ySize - 74));
		selected = data.get(scroll.getSelected());
		if (selected != null) { selected.initGui(); }
		for (Slot slot : inventorySlots.inventorySlots) {
			slot.xPos = -5000;
			slot.yPos = -5000;
		}
	}

	private boolean isIgnored(String tag) {
		for (String s : ignoredTags) {
			if (s.equals(tag)) { return true; }
		}
		return false;
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) {
		if (scroll.getID() == 0) { initGui(); }
		else {
			if (selected != null) { selected.scrollClicked(mouseX, mouseY, mouseButton, scroll); }
			if (getButton(11) != null) {
				if (scroll.hasSelected()) { getButton(11).setHoverText("display.hover.part." + scroll.getSelected().toLowerCase()); }
				else { getButton(11).setHoverText((Object) null); }
			}
		}
	}

	@Override
	public void scrollDoubleClicked(String selection, GuiCustomScroll scroll) { }

	@SuppressWarnings("unchecked")
	private void updateTexture() {
		EntityLivingBase entity = playerdata.getEntity(npc);
		@SuppressWarnings("rawtypes")
		RenderLivingBase render = (RenderLivingBase) mc.getRenderManager().getEntityRenderObject(entity);
        if (render != null) { npc.display.setSkinTexture(NPCRendererHelper.getTexture(render, entity)); }
	}

}
