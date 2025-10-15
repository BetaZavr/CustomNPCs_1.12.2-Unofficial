package noppes.npcs.client.gui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import noppes.npcs.LogWriter;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.util.CustomNPCsScheduler;
import noppes.npcs.util.NBTJsonUtil;

import javax.annotation.Nonnull;

public class GuiNbtBook extends GuiNPCInterface implements ICustomScrollListener, IGuiData {

	protected ItemStack blockStack;
	protected String errorMessage;
	protected String faultyText;
	protected String jsonCompound;
	protected IBlockState state;
	protected TileEntity tile;
	protected final int x;
	protected final int y;
	protected final int z;
	protected ItemStack stack;
	protected GuiCustomScroll scroll;
	public NBTTagCompound originalCompound;
	public NBTTagCompound compound;
	public Entity entity;
	public int entityId;

	public GuiNbtBook(int xPos, int yPos, int zPos) {
		super();
		setBackground("menubg.png");
		closeOnEsc = true;
		xSize = 256;
		ySize = 217;

		faultyText = null;
		errorMessage = null;
		x = xPos;
		y = yPos;
		z = zPos;
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: {
				if (compound == null) { return; }
				if (jsonCompound == null || jsonCompound.isEmpty()) { jsonCompound = compound.toString(); }
				if (faultyText != null) { setSubGui(new SubGuiNpcTextArea(jsonCompound, faultyText).enableHighlighting()); }
				else { setSubGui(new SubGuiNpcTextArea(0, jsonCompound).enableHighlighting()); }
				break;
			} // edit
			case 1: {
				if (stack != null && !stack.isEmpty()) { Client.sendData(EnumPacketServer.NbtBookCopyStack, stack.writeToNBT(new NBTTagCompound())); }
				break;
			} // copy
			case 2: {
				if (compound == null) { return; }
				if (faultyText != null) { setSubGui(new SubGuiNpcTextArea(compound.toString(), faultyText).enableHighlighting()); }
				else { setSubGui(new SubGuiNpcTextArea(0, compound.toString()).enableHighlighting()); }
				break;
			} // edit fast
			case 66: onClosed(); break;
			case 67: {
				getLabel(0).setLabel("Saved");
				if (compound.equals(originalCompound)) { return; }
				if (stack != null) { Client.sendData(EnumPacketServer.NbtBookSaveItem, compound); }
				if (tile == null) { Client.sendData(EnumPacketServer.NbtBookSaveEntity, entityId, compound); }
				else { Client.sendData(EnumPacketServer.NbtBookSaveBlock, x, y, z, compound); }
				originalCompound = compound.copy();
				button.setIsEnable(false);
				break;
			} // save
		}
	}

	@Override
	public void subGuiClosed(GuiScreen gui) {
		if (gui instanceof SubGuiNpcTextArea) {
			try {
				setCompound(JsonToNBT.getTagFromJson(((SubGuiNpcTextArea) gui).text));
				errorMessage = faultyText = null;
			}
			catch (NBTException e) {
				errorMessage = e.getLocalizedMessage();
				faultyText = ((SubGuiNpcTextArea) gui).text;
			}
			initGui();
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (hasSubGui()) { return; }
		if (stack != null || state != null) {
			GlStateManager.pushMatrix();
			Gui.drawRect(guiLeft + 3, guiTop + 3, guiLeft + 55, guiTop + 55, 0xFF808080);
			Gui.drawRect(guiLeft + 4, guiTop + 4, guiLeft + 54, guiTop + 54, 0xFF000000);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			GlStateManager.translate((guiLeft + 5), (guiTop + 5), 0.0f);
			GlStateManager.scale(3.0f, 3.0f, 3.0f);
			RenderHelper.enableGUIStandardItemLighting();
			itemRender.renderItemAndEffectIntoGUI(stack != null ? stack : blockStack, 0, 0);
			itemRender.renderItemOverlays(fontRenderer, stack != null ? stack : blockStack, 0, 0);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.popMatrix();
		}
		if (entity != null) {
			GlStateManager.pushMatrix();
			drawNpc(entity, 30, 80, 1.0f, 0, 0, 1);
			GlStateManager.translate(0.0f, 0.0f, 1.0f);
			int color = 0xFF808080;
			if (EntityRegistry.getEntry(entity.getClass()) == null) { color = 0xFFFF4040; }
			Gui.drawRect(guiLeft + 5, guiTop + 13, guiLeft + 55, guiTop + 99, color);
			Gui.drawRect(guiLeft + 6, guiTop + 14, guiLeft + 54, guiTop + 98, 0xFF000000);
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		boolean onlyClient = stack == null && state == null && entity == null;
		if (scroll == null) { scroll = new GuiCustomScroll(this, 0).setSize(188, 120); }
		scroll.guiLeft = guiLeft + 60;
		scroll.guiTop = guiTop + 45;
		if (stack != null) {
			scroll.setSize(188, 118);
			scroll.guiTop -= 20;
			addLabel(new GuiNpcLabel(11, "id: \"" + stack.getItem().getRegistryName() + "\"", guiLeft + 60, guiTop + 6));
			addButton(new GuiNpcButton(1, guiLeft + 38, guiTop + 144, 180, 20, "gui.copy"));
			setObjectToScroll(stack);
		}
		else if (state != null) {
			addLabel(new GuiNpcLabel(11, "x: " + x + ", y: " + y + ", z: " + z, guiLeft + 60, guiTop + 6));
			addLabel(new GuiNpcLabel(12, "id: " + Block.REGISTRY.getNameForObject(state.getBlock()), guiLeft + 60, guiTop + 16));
			addLabel(new GuiNpcLabel(13, "meta: " + state.getBlock().getMetaFromState(state), guiLeft + 60, guiTop + 26));
			setObjectToScroll(state);
		}
		else if (entity != null) {
			scroll.setSize(188, 140);
			scroll.guiTop -= 20;
			String name;
			if (EntityRegistry.getEntry(entity.getClass()) == null) {
				name = "Not registered name!";
				onlyClient = true;
			} else {
				name = "id: " + Objects.requireNonNull(Objects.requireNonNull(EntityRegistry.getEntry(entity.getClass())).getRegistryName());
			}
			addLabel(new GuiNpcLabel(12, name, guiLeft + 60, guiTop + 6));
			setObjectToScroll(entity);
		}
		addScroll(scroll);
		addLabel(new GuiNpcLabel(2, "nbt.edit", guiLeft + 4, guiTop + 172));
		addButton(new GuiNpcButton(0, guiLeft + 128, guiTop + 166, 59, 20, "selectServer.edit")
				.setIsEnable(compound != null && !compound.getKeySet().isEmpty()));
		addButton(new GuiNpcButton(2, guiLeft + 189, guiTop + 166, 59, 20, "gui.fast")
				.setIsEnable(compound != null && !compound.getKeySet().isEmpty()));
		addLabel(new GuiNpcLabel(0, "", guiLeft + 4, guiTop + 167));
		addLabel(new GuiNpcLabel(1, "", guiLeft + 4, guiTop + 177));
		addButton(new GuiNpcButton(66, guiLeft + 128, guiTop + 190, 120, 20, "gui.close"));
		addButton(new GuiNpcButton(67, guiLeft + 4, guiTop + 190, 120, 20, "gui.save"));
		getButton(67).setIsEnable(!onlyClient);
		if (!onlyClient) {
			if (errorMessage != null) {
				getButton(67).setIsEnable(false);
				int i = errorMessage.indexOf(" at: ");
				if (i > 0) {
					getLabel(0).setLabel(errorMessage.substring(0, i));
					getLabel(1).setLabel(errorMessage.substring(i));
				}
				else { getLabel(0).setLabel(errorMessage); }
			}
			if (getButton(67).enabled && originalCompound != null) { getButton(67).setIsEnable(!originalCompound.equals(compound)); }
		}
	}

	private void setObjectToScroll(Object obj) {
		addLabel(new GuiNpcLabel(15, "(?) Class \"" + obj.getClass().getSimpleName() + "\":", guiLeft + 60, guiTop + (state != null ? 36 : 16)));
		getLabel(15).setHoverText(obj.getClass().getName());
		List<String> list = new ArrayList<>();
		Map<String, Field> fs = new TreeMap<>();
		Map<String, Method> ms = new TreeMap<>();
		Map<String, Class<?>> cs = new TreeMap<>();
		for (Field f : obj.getClass().getDeclaredFields()) { fs.put(f.getName(), f); }
		for (Field f : obj.getClass().getFields()) { if (!fs.containsKey(f.getName())) { fs.put(f.getName(), f); } }
		for (Method m : obj.getClass().getDeclaredMethods()) { ms.put(m.getName(), m); }
		for (Method m : obj.getClass().getMethods()) { if (!ms.containsKey(m.getName())) { ms.put(m.getName(), m); } }
		for (Class<?> c : obj.getClass().getDeclaredClasses()) { cs.put(c.getName(), c); }
		for (Class<?> c : obj.getClass().getClasses()) { if (!cs.containsKey(c.getName())) { cs.put(c.getName(), c); } }
		LinkedHashMap<Integer, List<String>> hts = new LinkedHashMap<>();
		int i = 0;
		for (String key : fs.keySet()) {
			try {
				Field f = fs.get(key);
				boolean isAccessible = f.isAccessible();
				if (!isAccessible) { f.setAccessible(true); }
				int mdf = f.getModifiers();
				list.add(((char) 167) + "6F: " + ((char) 167) + (Modifier.isPublic(mdf) ? "a" : "c") + key);
				hts.put(i, getFieldTypes(obj, mdf, f));
				if (!isAccessible) { f.setAccessible(false); }
			}
			catch (Exception e) {
				hts.put(i, Collections.singletonList(""));
				LogWriter.error(e);
			}
			i++;
		}
		for (String key : ms.keySet()) {
			try {
				Method m = ms.get(key);
				boolean isAccessible = m.isAccessible();
				if (!isAccessible) { m.setAccessible(true); }
				int mdf = m.getModifiers();
				list.add(((char) 167) + "3M: " + ((char) 167) + (Modifier.isPublic(mdf) ? "a" : "c") + key);
				hts.put(i, getMethodTypes(mdf, m));
				if (!isAccessible) { m.setAccessible(false); }
			}
			catch (Exception e) {
				hts.put(i, Collections.singletonList(""));
				LogWriter.error(e);
			}
			i++;
		}
		for (String key : cs.keySet()) {
			Class<?> c = cs.get(key);
			int mdf = c.getModifiers();
			list.add(((char) 167) + "3M: " + ((char) 167) + (Modifier.isPublic(mdf) ? "a" : "c") + key);
			String mf = ((char) 167) + "9subclass: ";
			if (Modifier.isPublic(mdf)) { mf += ((char) 167) + "apublic"; }
			else if (Modifier.isProtected(mdf)) { mf += ((char) 167) + "cprotected"; }
			else { mf += ((char) 167) + "4private"; }
			if (Modifier.isStatic(mdf)) { mf += ((char) 167) + "e static"; }
			if (Modifier.isFinal(mdf)) { mf += ((char) 167) + "b final"; }
			List<String> l = new ArrayList<>();
			l.add(mf);
			l.add(c.getSimpleName());
			hts.put(i, l);
			i++;
		}
		scroll.setUnsortedList(list).setHoverTexts(hts);
	}

	private static List<String> getFieldTypes(Object obj, int mdf, Field f) throws IllegalAccessException {
		String mf = ((char) 167) + "6field: ";
		if (Modifier.isPublic(mdf)) { mf += ((char) 167) + "apublic"; }
		else if (Modifier.isProtected(mdf)) { mf += ((char) 167) + "cprotected"; }
		else { mf += ((char) 167) + "4private"; }
		if (Modifier.isStatic(mdf)) { mf += ((char) 167) + "e static"; }
		if (Modifier.isFinal(mdf)) { mf += ((char) 167) + "b final"; }
		Object v = f.get(obj);
		List<String> l = new ArrayList<>();
		l.add(mf);
		l.add(((char) 167) + "7value type: " + ((char) 167) + "r" + f.getType().getName());
		l.add(((char) 167) + "7value = " + ((char) 167) + "r" + (v != null ? v.toString() : "null"));
		return l;
	}

	private static List<String> getMethodTypes(int mdf, Method m) {
		String mf = ((char) 167) + "3method: ";
		if (Modifier.isPublic(mdf)) { mf += ((char) 167) + "apublic"; }
		else if (Modifier.isProtected(mdf)) { mf += ((char) 167) + "cprotected"; }
		else { mf += ((char) 167) + "4private"; }
		if (Modifier.isStatic(mdf)) { mf += ((char) 167) + "e static"; }
		if (Modifier.isFinal(mdf)) { mf += ((char) 167) + "b final"; }
		List<String> hoverText = new ArrayList<>();
		hoverText.add(mf);
		hoverText.add(((char) 167) + "7return type: " + ((char) 167) + "r" + m.getReturnType().getName());
		if (m.getParameters() != null && m.getParameters().length > 0) {
			hoverText.add(((char) 167) + "7parameters: (");
			Parameter[] prms = m.getParameters();
			for (int j = 0; j < prms.length; j++) {
				String nm = ((char) 167) + "8" + prms[j].getType().getName();
				nm = nm.replace(prms[j].getType().getSimpleName(), ((char) 167) + "e" + prms[j].getType().getSimpleName());
				nm += ((char) 167) + "r " + prms[j].getName() + (j < prms.length - 1 ? "," : "");
				hoverText.add(nm);
			}
			hoverText.add(((char) 167) + "7)");
		}
		else { hoverText.add(((char) 167) + "7parameters: " + ((char) 167) + "r()"); }
		return hoverText;
	}

    @SuppressWarnings("deprecation")
	@Override
	public void setGuiData(NBTTagCompound nbt) {
		if (nbt.hasKey("Item") && nbt.getBoolean("Item")) { stack = new ItemStack(nbt.getCompoundTag("Data")); }
		else if (nbt.hasKey("EntityId")) {
			entityId = nbt.getInteger("EntityId");
			entity = player.world.getEntityByID(entityId);
		}
		else {
			tile = player.world.getTileEntity(new BlockPos(x, y, z));
			state = player.world.getBlockState(new BlockPos(x, y, z));
			blockStack = state.getBlock().getItem(player.world, new BlockPos(x, y, z), state);
		}
		originalCompound = nbt.getCompoundTag("Data");
		setCompound(originalCompound.copy());
		initGui();
	}

	private void setCompound(NBTTagCompound nbt) {
		compound = nbt;
		jsonCompound = nbt.toString();
		CustomNPCsScheduler.runTack(() -> jsonCompound = NBTJsonUtil.Convert(nbt));
	}

	@Override
	public void scrollClicked(int mouseX, int mouseY, int mouseButton, GuiCustomScroll scroll) { }

	@Override
	public void scrollDoubleClicked(String select, GuiCustomScroll scroll) { }

}
