package noppes.npcs.client.gui;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.*;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.Gui;
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
import noppes.npcs.client.gui.util.GuiCustomScroll;
import noppes.npcs.client.gui.util.GuiNPCInterface;
import noppes.npcs.client.gui.util.GuiNpcButton;
import noppes.npcs.client.gui.util.GuiNpcLabel;
import noppes.npcs.client.gui.util.IGuiData;
import noppes.npcs.client.gui.util.SubGuiInterface;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.util.NBTJsonUtil;

public class GuiNbtBook extends GuiNPCInterface implements IGuiData {

	private ItemStack blockStack;
	public NBTTagCompound compound;
	public Entity entity;
	public int entityId;
	private String errorMessage;
	private String faultyText;
	public NBTTagCompound originalCompound;
	private IBlockState state;
	private TileEntity tile;
	private final int x;
	private final int y;
	private final int z;
	private ItemStack stack;
	private GuiCustomScroll scroll;

	public GuiNbtBook(int x, int y, int z) {
		this.faultyText = null;
		this.errorMessage = null;
		this.x = x;
		this.y = y;
		this.z = z;
		this.setBackground("menubg.png");
		this.xSize = 256;
		this.ySize = 217;
		this.closeOnEsc = true;
	}

	@Override
	public void buttonEvent(GuiNpcButton button) {
		if (button.id == 0) {
			if (faultyText != null) {
				this.setSubGui(new SubGuiNpcTextArea(NBTJsonUtil.Convert(compound), faultyText).enableHighlighting());
			} else {
				this.setSubGui(new SubGuiNpcTextArea(NBTJsonUtil.Convert(compound)).enableHighlighting());
			}
		} else if (button.id == 1) {
			if (stack != null && !stack.isEmpty()) {
				Client.sendData(EnumPacketServer.NbtBookCopyStack, stack.writeToNBT(new NBTTagCompound()));
			}
		} else if (button.id == 67) {
			this.getLabel(0).setLabel("Saved");
			if (this.compound.equals(this.originalCompound)) {
				return;
			}
			if (this.stack != null) {
				Client.sendData(EnumPacketServer.NbtBookSaveItem, this.compound);
				return;
			}
			if (this.tile == null) {
				Client.sendData(EnumPacketServer.NbtBookSaveEntity, this.entityId, this.compound);
				return;
			}
			Client.sendData(EnumPacketServer.NbtBookSaveBlock, this.x, this.y, this.z, this.compound);
			this.originalCompound = this.compound.copy();
			this.getButton(67).enabled = false;
		}
		if (button.id == 66) {
			this.close();
		}
	}

	@Override
	public void closeSubGui(SubGuiInterface gui) {
		super.closeSubGui(gui);
		if (gui instanceof SubGuiNpcTextArea) {
			try {
				this.compound = JsonToNBT.getTagFromJson(((SubGuiNpcTextArea) gui).text);
				this.faultyText = null;
				this.errorMessage = null;
			} catch (NBTException e) {
				this.errorMessage = e.getLocalizedMessage();
				this.faultyText = ((SubGuiNpcTextArea) gui).text;
			}
			this.initGui();
		}
	}

	@Override
	public void drawScreen(int mouseX, int mouseY, float partialTicks) {
		super.drawScreen(mouseX, mouseY, partialTicks);
		if (this.hasSubGui()) {
			return;
		}
		if (this.stack != null || this.state != null) {
			GlStateManager.pushMatrix();
			Gui.drawRect(this.guiLeft + 3, this.guiTop + 3, this.guiLeft + 55, this.guiTop + 55, 0xFF808080);
			Gui.drawRect(this.guiLeft + 4, this.guiTop + 4, this.guiLeft + 54, this.guiTop + 54, 0xFF000000);
			GlStateManager.popMatrix();
			GlStateManager.pushMatrix();
			GlStateManager.translate((this.guiLeft + 5), (this.guiTop + 5), 0.0f);
			GlStateManager.scale(3.0f, 3.0f, 3.0f);
			RenderHelper.enableGUIStandardItemLighting();
			this.itemRender.renderItemAndEffectIntoGUI(this.stack != null ? this.stack : this.blockStack, 0, 0);
			this.itemRender.renderItemOverlays(this.fontRenderer, this.stack != null ? this.stack : this.blockStack, 0,
					0);
			RenderHelper.disableStandardItemLighting();
			GlStateManager.popMatrix();
		}
		if (this.entity != null) {
			GlStateManager.pushMatrix();
			this.drawNpc(this.entity, 30, 80, 1.0f, 0, 0, 1);
			GlStateManager.translate(0.0f, 0.0f, 1.0f);
			int color = 0xFF808080;
			if (EntityRegistry.getEntry(this.entity.getClass()) == null) { color = 0xFFFF4040; }
			Gui.drawRect(this.guiLeft + 5, this.guiTop + 13, this.guiLeft + 55, this.guiTop + 99, color);
			Gui.drawRect(this.guiLeft + 6, this.guiTop + 14, this.guiLeft + 54, this.guiTop + 98, 0xFF000000);
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void initGui() {
		super.initGui();
		boolean onlyClient = this.stack == null && this.state == null && this.entity == null;
		if (this.scroll == null) { (this.scroll = new GuiCustomScroll(this, 0)).setSize(188, 120); }
		this.scroll.guiLeft = this.guiLeft + 60;
		this.scroll.guiTop = this.guiTop + 45;
		if (this.stack != null) {
			this.scroll.setSize(188, 118);
			this.scroll.guiTop -= 20;
			this.addLabel(new GuiNpcLabel(11, "id: \"" + this.stack.getItem().getRegistryName() + "\"", this.guiLeft + 60, this.guiTop + 6));
			this.addButton(new GuiNpcButton(1, this.guiLeft + 38, this.guiTop + 144, 180, 20, "gui.copy"));
			this.setObjectToScroll(this.stack);
		}
		if (this.state != null) {
			this.addLabel(new GuiNpcLabel(11, "x: " + this.x + ", y: " + this.y + ", z: " + this.z, this.guiLeft + 60, this.guiTop + 6));
			this.addLabel(new GuiNpcLabel(12, "id: " + Block.REGISTRY.getNameForObject(this.state.getBlock()), this.guiLeft + 60, this.guiTop + 16));
			this.addLabel(new GuiNpcLabel(13, "meta: " + this.state.getBlock().getMetaFromState(this.state), this.guiLeft + 60, this.guiTop + 26));
			this.setObjectToScroll(this.state);
		}
		if (this.entity != null) {
			this.scroll.setSize(188, 140);
			this.scroll.guiTop -= 20;
			String name;
			if (EntityRegistry.getEntry(this.entity.getClass()) == null) {
				name = "Not registered name!";
				onlyClient = true;
			} else {
				name = "id: " + Objects.requireNonNull(Objects.requireNonNull(EntityRegistry.getEntry(entity.getClass())).getRegistryName());
			}
			this.addLabel(new GuiNpcLabel(12, name, this.guiLeft + 60, this.guiTop + 6));
			this.setObjectToScroll(this.entity);
		}
		this.addScroll(this.scroll);
		this.addButton(new GuiNpcButton(0, this.guiLeft + 38, this.guiTop + 166, 180, 20, "nbt.edit"));
		this.getButton(0).enabled = (this.compound != null && !this.compound.getKeySet().isEmpty());
		this.addLabel(new GuiNpcLabel(0, "", this.guiLeft + 4, this.guiTop + 167));
		this.addLabel(new GuiNpcLabel(1, "", this.guiLeft + 4, this.guiTop + 177));
		this.addButton(new GuiNpcButton(66, this.guiLeft + 128, this.guiTop + 190, 120, 20, "gui.close"));
		
		this.addButton(new GuiNpcButton(67, this.guiLeft + 4, this.guiTop + 190, 120, 20, "gui.save"));
		this.getButton(67).setEnabled(!onlyClient);
		if (!onlyClient) {
			if (this.errorMessage != null) {
				this.getButton(67).enabled = false;
				int i = this.errorMessage.indexOf(" at: ");
				if (i > 0) {
					this.getLabel(0).setLabel(this.errorMessage.substring(0, i));
					this.getLabel(1).setLabel(this.errorMessage.substring(i));
				} else {
					this.getLabel(0).setLabel(this.errorMessage);
				}
			}
			if (this.getButton(67).enabled && this.originalCompound != null) {
				this.getButton(67).enabled = !this.originalCompound.equals(this.compound);
			}
		}
	}

	private void setObjectToScroll(Object obj) {
		this.addLabel(new GuiNpcLabel(15, "(?) Class \"" + obj.getClass().getSimpleName() + "\":", this.guiLeft + 60, this.guiTop + (this.state != null ? 36 : 16)));
		this.getLabel(15).hoverText = new String[] { obj.getClass().getName() };
		
		List<String> list = new ArrayList<>();
		Map<String, Field> fs = new TreeMap<>();
		Map<String, Method> ms = new TreeMap<>();
		Map<String, Class<?>> cs = new TreeMap<>();
		for (Field f : obj.getClass().getDeclaredFields()) { fs.put(f.getName(), f); }
		for (Method m : obj.getClass().getDeclaredMethods()) { ms.put(m.getName(), m); }
		for (Class<?> c : obj.getClass().getDeclaredClasses()) { cs.put(c.getName(), c); }

		this.scroll.hoversTexts = new String[fs.size() + ms.size() + cs.size()][];
		int i = 0;
		for (String key : fs.keySet()) {
			try {
				Field f = fs.get(key);
				boolean isAccessible = f.isAccessible();
				if (!isAccessible) { f.setAccessible(true); }
				int mdf = f.getModifiers();
				list.add(((char) 167) + "6F: " + ((char) 167) + (Modifier.isPublic(mdf) ? "a" : "c") + key);
				String mf = ((char) 167) + "6field: ";
				if (Modifier.isPublic(mdf)) { mf += ((char) 167) + "apublic"; }
				else if (Modifier.isProtected(mdf)) { mf += ((char) 167) + "cprotected"; }
				else { mf += ((char) 167) + "4private"; }
				if (Modifier.isStatic(mdf)) { mf += ((char) 167) + "e static"; }
				if (Modifier.isFinal(mdf)) { mf += ((char) 167) + "b final"; }
				Object v = f.get(obj);
				this.scroll.hoversTexts[i] = new String[] { mf,
						((char) 167) + "7value type: " + ((char) 167) + "r" + f.getType().getName(),
						((char) 167) + "7value = " + ((char) 167) + "r" + (v != null ? v.toString() : "null")};
				if (!isAccessible) { f.setAccessible(false); }
			}
			catch (Exception e) {
				this.scroll.hoversTexts[i] = new String[] { "" };
				LogWriter.error("Error:", e);
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
				List<String> hoverText = getStringList(mdf, m);
				this.scroll.hoversTexts[i] = hoverText.toArray(new String[0]);
				if (!isAccessible) { m.setAccessible(false); }
			}
			catch (Exception e) {
				this.scroll.hoversTexts[i] = new String[] { "" };
				LogWriter.error("Error:", e);
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
			this.scroll.hoversTexts[i] = new String[] { mf, c.getSimpleName() };
			i++;
		}
		this.scroll.setListNotSorted(list);
	}

	private static List<String> getStringList(int mdf, Method m) {
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
		} else {
			hoverText.add(((char) 167) + "7parameters: " + ((char) 167) + "r()");
		}
		return hoverText;
	}

	@Override
	public void save() {
	}

	@SuppressWarnings("deprecation")
	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("Item") && compound.getBoolean("Item")) {
			this.stack = new ItemStack(compound.getCompoundTag("Data"));
		} else if (compound.hasKey("EntityId")) {
			this.entityId = compound.getInteger("EntityId");
			this.entity = this.player.world.getEntityByID(this.entityId);
		} else {
			this.tile = this.player.world.getTileEntity(new BlockPos(this.x, this.y, this.z));
			this.state = this.player.world.getBlockState(new BlockPos(this.x, this.y, this.z));
			this.blockStack = this.state.getBlock().getItem(this.player.world, new BlockPos(this.x, this.y, this.z), this.state);
		}
		this.originalCompound = compound.getCompoundTag("Data");
		this.compound = this.originalCompound.copy();
		this.initGui();
	}

}
