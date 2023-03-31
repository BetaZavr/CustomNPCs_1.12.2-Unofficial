package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.Server;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.gui.IGuiTimer;
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.api.gui.ILabel;
import noppes.npcs.api.gui.IOverlayHUD;
import noppes.npcs.api.gui.ITexturedRect;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.ItemScriptedWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiComponentWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiItemSlotWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiLabelWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiTexturedRectWrapper;
import noppes.npcs.api.wrapper.gui.CustomGuiTimerWrapper;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.custom.GuiCustom;
import noppes.npcs.client.gui.custom.components.CustomGuiLabel;
import noppes.npcs.client.gui.custom.components.CustomGuiTexturedRect;
import noppes.npcs.client.gui.custom.components.CustomGuiTimer;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumPlayerPacket;

public class PlayerOverlayHUD
implements IOverlayHUD {

	public boolean isMoved;
	private byte[] showElementTypes;
	private double[] windowSize;
	public List<Integer> keyPress;
	public List<Integer> mousePress;
	private String currentLanguage;
	private int offsetType;
	
	List<ICustomGuiComponent> components;
	private List<IItemSlot> slots;
	private Map<Integer, IGuiComponent> guiComponents;
	
	private boolean update;
	private EntityPlayer player;
	
	public PlayerOverlayHUD() {
		this.isMoved = false;
		this.showElementTypes = new byte[22];
		for (int i = 0; i<22; i++) { this.showElementTypes[i] = (byte) 1; }
		this.windowSize = new double[] { 0, 0 };
		this.keyPress = Lists.<Integer>newArrayList();
		this.mousePress = Lists.<Integer>newArrayList();
		this.components = Lists.<ICustomGuiComponent>newArrayList();
		this.guiComponents = Maps.<Integer, IGuiComponent>newTreeMap();
		this.slots = Lists.<IItemSlot>newArrayList();
		this.currentLanguage = "en_us";
		this.update = false;
		this.player = null;
		this.offsetType = 0;
	}
	
	public NBTTagCompound getNBT() {
		NBTTagCompound hudNBT = new NBTTagCompound();
		hudNBT.setByteArray("ShowElementTypes", this.showElementTypes);

		hudNBT.setBoolean("IsMoved", this.isMoved);
		NBTTagList list = new NBTTagList();
		list.appendTag(new NBTTagDouble(this.windowSize[0]));
		list.appendTag(new NBTTagDouble(this.windowSize[1]));
		hudNBT.setTag("WindowSize", list);
		hudNBT.setIntArray("KeyPress", this.getKeyPressed());
		hudNBT.setIntArray("MousePress", this.getMousePressed());
		hudNBT.setString("CurrentLanguage", this.currentLanguage);
		
		list = new NBTTagList();
		for (ICustomGuiComponent c : this.components) { list.appendTag(((CustomGuiComponentWrapper) c).toNBT(new NBTTagCompound())); } 
		hudNBT.setTag("components", list);
		
		list = new NBTTagList();
		for (ICustomGuiComponent c : this.slots) { list.appendTag(((CustomGuiComponentWrapper) c).toNBT(new NBTTagCompound())); }
		hudNBT.setTag("Slots", list);

		hudNBT.setInteger("OffsetType", this.offsetType);
		
		return hudNBT;
	}
	
	public void loadNBTData(NBTTagCompound compound) {
		if (compound == null || !compound.hasKey("HUDData", 10)) { return; }
		NBTTagCompound hudNBT = compound.getCompoundTag("HUDData");
		this.showElementTypes = hudNBT.getByteArray("ShowElementTypes");
		for (int i = 0; i < 2 && i < hudNBT.getTagList("WindowSize", 6).tagCount(); i++) {
			this.windowSize[i] = hudNBT.getTagList("WindowSize", 6).getDoubleAt(i);
		}
		this.currentLanguage = hudNBT.getString("CurrentLanguage");
		int[] iK = hudNBT.getIntArray("KeyPress");
		int[] iM = hudNBT.getIntArray("MousePress");
		this.keyPress.clear();
		this.mousePress.clear();
		for (int key : iK) { this.keyPress.add(key); }
		for (int key : iM) { this.mousePress.add(key); }
		
		NBTTagList list = hudNBT.getTagList("components", 10);
		List<Integer> ids = Lists.<Integer>newArrayList();
		for (NBTBase b : list) {
			ids.add(((NBTTagCompound) b).getInteger("id"));
			ICustomGuiComponent c = this.getComponent(((NBTTagCompound) b).getInteger("id"));
			if (c!=null) {
				((CustomGuiComponentWrapper) c).fromNBT((NBTTagCompound) b);
				continue;
			}
			this.components.add(CustomGuiComponentWrapper.createFromNBT((NBTTagCompound) b));
		}
		List<ICustomGuiComponent> del = Lists.<ICustomGuiComponent>newArrayList();
		for (ICustomGuiComponent comp : this.components) { if (!ids.contains(comp.getID())) { del.add(comp); } }
		for (ICustomGuiComponent comp : del) { this.components.remove(comp); }
		this.guiComponents.clear();
		
		List<IItemSlot> slots = new ArrayList<IItemSlot>();
		list = hudNBT.getTagList("slots", 10);
		for (NBTBase b : list) {
			slots.add((CustomGuiItemSlotWrapper) CustomGuiComponentWrapper.createFromNBT((NBTTagCompound) b));
		}
		this.slots = slots;
		
		this.offsetType = hudNBT.getInteger("OffsetType");
	}
	
	public NBTTagCompound saveNBTData(NBTTagCompound compound) {
		compound.setTag("HUDData", this.getNBT());
		return compound;
	}

	@Override
	public boolean isShowElementType(int type) {
		if (type<0 || type>=22) { return false; }
		return this.showElementTypes[type] == (byte) 1;
	}

	@Override
	public void setShowElementType(int type, boolean bo) {
		if (type<0 || type>=22) { return; }
		this.showElementTypes[type] = (byte) (bo ? 1 : 0);
	}

	@Override
	public void setShowElementType(String name, boolean bo) {
		int type;
		switch(name.toLowerCase()) {
			case "all": { type = 0; break; }
			case "helmet": { type = 1; break; }
			case "portal": { type = 2; break; }
			case "crosshairs": { type = 3; break; }
			case "bosshealth": { type = 4; break; }
			case "bossinfo": { type = 5; break; }
			case "armor": { type = 6; break; }
			case "health": { type = 7; break; }
			case "food": { type = 8; break; }
			case "air": { type = 9; break; }
			case "hotbar": { type = 10; break; }
			case "experience": { type = 11; break; }
			case "text": { type = 12; break; }
			case "healthmount": { type = 13; break; }
			case "jumpbar": { type = 14; break; }
			case "chat": { type = 15; break; }
			case "player_list": { type = 16; break; }
			case "debug": { type = 17; break; }
			case "potion_icons": { type = 18; break; }
			case "subtitles": { type = 19; break; }
			case "fps_graph": { type = 20; break; }
			case "vignette": { type = 21; break; }
			default: { return; }
		}
		this.showElementTypes[type] = (byte) (bo ? 1 : 0);
	}

	@Override
	public int[] getKeyPressed() {
		int[] ids = new int[this.keyPress.size()];
		int i = 0;
		for (int key : this.keyPress) {
			ids[i] = key;
			i++;
		}
		return ids;
	}

	@Override
	public int[] getMousePressed() {
		int[] ids = new int[this.mousePress.size()];
		int i = 0;
		for (int key : this.mousePress) {
			ids[i] = key;
			i++;
		}
		return ids;
	}

	@Override
	public String getCurrentLanguage() {
		return this.currentLanguage;
	}

	@Override
	public double[] getWindowSize() {
		return this.windowSize;
	}

	@Override
	public boolean hasOrKeysPressed(int ... keys) {
		for (int key : keys) {
			for (int k : this.keyPress) {
				if (k == key) { return true; }
			}
		}
		return false;
	}

	@Override
	public boolean hasMousePress(int key) {
		for (int k : this.mousePress) {
			if (k == key) { return true; }
		}
		return this.mousePress.contains((Integer) key) ;
	}

	@Override
	public boolean isMoved() {
		return this.isMoved;
	}

	@Override
	public IGuiTimer addTimer(int id, long start, long end, int x, int y, int width, int height) {
		ICustomGuiComponent component = this.getComponent(id);
		if (component instanceof CustomGuiTimerWrapper) {
			CustomGuiTimerWrapper timer = (CustomGuiTimerWrapper) component;
			timer.setTime(start, end);
			timer.setPos(x, y);
			timer.setSize(width, height);
			return timer;
		}
		if (component!=null) { this.components.remove(component); }
		CustomGuiTimerWrapper timer = new CustomGuiTimerWrapper(id, start, end, x, y, width, height);
		this.components.add(timer);
		return timer;
	}
	
	@Override
	public IGuiTimer addTimer(int id, long start, long end, int x, int y, int width, int height, int color) {
		ICustomGuiComponent component = this.getComponent(id);
		if (component instanceof CustomGuiTimerWrapper) {
			CustomGuiTimerWrapper timer = (CustomGuiTimerWrapper) component;
			timer.setTime(start, end);
			timer.setPos(x, y);
			timer.setSize(width, height);
			timer.setColor(color);
			return timer;
		}
		if (component!=null) { this.components.remove(component); }
		CustomGuiTimerWrapper timer = new CustomGuiTimerWrapper(id, start, end, x, y, width, height, color);
		this.components.add(timer);
		return timer;
	}
	
	@Override
	public ILabel addLabel(int id, String label, int x, int y, int width, int height) {
		ICustomGuiComponent component = this.getComponent(id);
		if (component instanceof CustomGuiLabelWrapper) {
			CustomGuiLabelWrapper lable = (CustomGuiLabelWrapper) component;
			lable.setText(label);
			lable.setPos(x, y);
			lable.setSize(width, height);
			return lable;
		}
		if (component!=null) { this.components.remove(component); }
		CustomGuiLabelWrapper lable = new CustomGuiLabelWrapper(id, label, x, y, width, height);
		this.components.add(lable);
		return lable;
	}

	@Override
	public ILabel addLabel(int id, String label, int x, int y, int width, int height, int color) {
		ICustomGuiComponent component = this.getComponent(id);
		if (component instanceof CustomGuiLabelWrapper) {
			CustomGuiLabelWrapper lable = (CustomGuiLabelWrapper) component;
			lable.setText(label);
			lable.setPos(x, y);
			lable.setSize(width, height);
			lable.setColor(color);
			return lable;
		}
		if (component!=null) { this.components.remove(component); }
		CustomGuiLabelWrapper lable = new CustomGuiLabelWrapper(id, label, x, y, width, height, color);
		this.components.add(lable);
		return lable;
	}

	@Override
	public ITexturedRect addTexturedRect(int id, String texture, int x, int y, int width, int height) {
		ICustomGuiComponent component = this.getComponent(id);
		if (component instanceof CustomGuiTexturedRectWrapper) {
			CustomGuiTexturedRectWrapper txtr = (CustomGuiTexturedRectWrapper) component;
			txtr.setTexture(texture);
			txtr.setPos(x, y);
			txtr.setSize(width, height);
			return txtr;
		}
		if (component!=null) { this.components.remove(component); }
		CustomGuiTexturedRectWrapper txtr = new CustomGuiTexturedRectWrapper(id, texture, x, y, width, height);
		this.components.add(txtr);
		return txtr;
	}

	@Override
	public ITexturedRect addTexturedRect(int id, String texture, int x, int y, int width, int height, int textureX, int textureY) {
		ICustomGuiComponent component = this.getComponent(id);
		if (component instanceof CustomGuiTexturedRectWrapper) {
			CustomGuiTexturedRectWrapper txtr = (CustomGuiTexturedRectWrapper) component;
			txtr.setTexture(texture);
			txtr.setPos(x, y);
			txtr.setSize(width, height);
			txtr.setTextureOffset(textureX, textureY);
			return txtr;
		}
		if (component!=null) { this.components.remove(component); }
		CustomGuiTexturedRectWrapper txtr = new CustomGuiTexturedRectWrapper(id, texture, x, y, width, height, textureX, textureY);
		this.components.add(txtr);
		return txtr;
	}

	@Override
	public ICustomGuiComponent getComponent(int componentID) {
		for (ICustomGuiComponent component : this.components) {
			if (component.getID() == componentID) {
				return component;
			}
		}
		return null;
	}

	@Override
	public List<ICustomGuiComponent> getComponents() { return this.components; }

	@Override
	public void removeComponent(int componentID) {
		for (int i = 0; i < this.components.size(); ++i) {
			if (this.components.get(i).getID() == componentID) {
				this.components.remove(i);
				this.update = true;
				return;
			}
		}
	}
	
	@Override
	public List<IItemSlot> getSlots() { return this.slots; }
	
	public void update(EntityPlayer player) {
		this.player = player;
		if (this.update && this.player instanceof EntityPlayerMP) {
			Server.sendData((EntityPlayerMP) this.player, EnumPacketClient.UPDATE_HUD, this.saveNBTData(new NBTTagCompound()));
			this.update = false;
		}
	}

	public Map<Integer, IGuiComponent> getGuiComponents() {
		if (this.guiComponents!=null && this.guiComponents.size()>0) {
			List<ICustomGuiComponent> del = Lists.<ICustomGuiComponent>newArrayList();
			for (ICustomGuiComponent c : this.components) {
				if (c instanceof CustomGuiTimerWrapper && ((CustomGuiTimerWrapper) c).end>=0) {
					CustomGuiTimerWrapper t = (CustomGuiTimerWrapper) c;
					if (t.start + System.currentTimeMillis()/50 > t.end) { del.add(c); }
				}
			}
			if (del.isEmpty()) { return this.guiComponents; }
			for (ICustomGuiComponent c : del) {
				this.components.remove(c);
				Client.sendDataDelayCheck(EnumPlayerPacket.HudTimerEnd, this, 0, c.getID());
			}
		}
		this.guiComponents.clear();
		GuiCustom gui = new GuiCustom(null);
		for (ICustomGuiComponent component : this.components) {
			if (component instanceof CustomGuiLabelWrapper) {
				CustomGuiLabel lbl = CustomGuiLabel.fromComponent((CustomGuiLabelWrapper) component);
				lbl.setParent(gui);
				lbl.offSet(this.offsetType, this.windowSize);
				this.guiComponents.put(lbl.getID(), lbl);
			}
			else if (component instanceof CustomGuiTexturedRectWrapper) {
				CustomGuiTexturedRect rect = CustomGuiTexturedRect.fromComponent((CustomGuiTexturedRectWrapper) component);
				rect.setParent(gui);
				rect.offSet(this.offsetType, this.windowSize);
				this.guiComponents.put(rect.getID(), rect);
			}
			else if (component instanceof CustomGuiTimerWrapper) {
				CustomGuiTimer time = CustomGuiTimer.fromComponent((CustomGuiTimerWrapper) component);
				time.setParent(gui);
				time.offSet(this.offsetType, this.windowSize);
				this.guiComponents.put(time.getID(), time);
			}
		}
		return this.guiComponents;
	}
	
	public void clearGuiComponents() { this.guiComponents.clear(); }
	
	@Override
	public int getOffsetType() { return this.offsetType; }

	@Override
	public void setOffsetType(int type) {
		if (type<0 || type>3) { return; }
		this.offsetType = type;
	}

	public void setWindowSize(NBTTagList tagList) {
		if (tagList==null || tagList.getTagType()!=6) { return; }
		for (int i = 0; i < 2 && i < tagList.tagCount(); i++) {
			this.windowSize[i] = tagList.getDoubleAt(i);
		}
	}

	@Override
	public void update() { this.update = true; }

	@Override
	public IItemSlot addItemSlot(int x, int y) { return this.addItemSlot(x, y, ItemScriptedWrapper.AIR); }

	@Override
	public IItemSlot addItemSlot(int x, int y, IItemStack stack) {
		CustomGuiItemSlotWrapper slot = new CustomGuiItemSlotWrapper(x, y, stack);
		this.slots.add(slot);
		return this.slots.get(this.slots.size() - 1);
	}
	
}
