package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.Server;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.gui.ICompassData;
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
import noppes.npcs.client.gui.custom.components.CustomGuiLabel;
import noppes.npcs.client.gui.custom.components.CustomGuiTexturedRect;
import noppes.npcs.client.gui.custom.components.CustomGuiTimer;
import noppes.npcs.client.gui.custom.interfaces.IGuiComponent;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.util.CustomNPCsScheduler;

public class PlayerOverlayHUD implements IOverlayHUD {

	public boolean isMoved = false;
	private byte[] showElementTypes;
	private final double[] windowSize = new double[] { 0, 0 };
	public List<Integer> keyPress = new ArrayList<>();
	public List<Integer> mousePress = new ArrayList<>();
	private int offsetType = 0;
	public int questID = 0;

	Map<Integer, List<ICustomGuiComponent>> components = new TreeMap<>();
	private final Map<Integer, List<IItemSlot>> slots = new TreeMap<>();
	private final TreeMap<Integer, TreeMap<Integer, IGuiComponent>> guiComponents = new TreeMap<>();
	private final TreeMap<Integer, TreeMap<Integer, IItemSlot>> guiSlots = new TreeMap<>();

	private boolean update = false;
	private EntityPlayerMP player = null;
	public PlayerCompassHUDData compassData = new PlayerCompassHUDData();
	public String currentGUI = "";

	public PlayerOverlayHUD() {
		this.showElementTypes = new byte[22];
		for (int i = 0; i < 22; i++) {
			this.showElementTypes[i] = (byte) 1;
		}
	}

	@Override
	public IItemSlot addItemSlot(int orientationType, int x, int y) {
		return this.addItemSlot(orientationType, x, y, ItemScriptedWrapper.AIR);
	}

	@Override
	public IItemSlot addItemSlot(int orientationType, int x, int y, IItemStack stack) {
		CustomGuiItemSlotWrapper slot = new CustomGuiItemSlotWrapper(x, y, stack);
		if (!this.slots.containsKey(orientationType)) {
			this.slots.put(orientationType, new ArrayList<>());
		}
		this.slots.get(orientationType).add(slot);
		return slot;
	}

	@Override
	public ILabel addLabel(int id, int orientationType, String label, int x, int y, int width, int height) {
		if (width <= 0 || height <= 0) {
			throw new CustomNPCsException("Invalid component width or height: [" + width + ", " + height + "]");
		}
		ICustomGuiComponent component = this.getComponent(orientationType, id);
		if (component instanceof CustomGuiLabelWrapper) {
			CustomGuiLabelWrapper l = (CustomGuiLabelWrapper) component;
			l.setText(label);
			l.setPos(x, y);
			l.setSize(width, height);
			return l;
		}
		if (component != null) {
			this.components.get(orientationType).remove(component);
		}
		CustomGuiLabelWrapper l = new CustomGuiLabelWrapper(id, label, x, y, width, height);
		if (!this.components.containsKey(orientationType)) {
			this.components.put(orientationType, new ArrayList<>());
		}
		this.components.get(orientationType).add(l);
		return l;
	}

	@Override
	public ILabel addLabel(int id, int orientationType, String label, int x, int y, int width, int height, int color) {
		if (width <= 0 || height <= 0) {
			throw new CustomNPCsException("Invalid component width or height: [" + width + ", " + height + "]");
		}
		ICustomGuiComponent component = this.getComponent(orientationType, id);
		if (component instanceof CustomGuiLabelWrapper) {
			CustomGuiLabelWrapper l = (CustomGuiLabelWrapper) component;
			l.setText(label);
			l.setPos(x, y);
			l.setSize(width, height);
			l.setColor(color);
			return l;
		}
		if (component != null) {
			this.components.get(orientationType).remove(component);
		}
		CustomGuiLabelWrapper l = new CustomGuiLabelWrapper(id, label, x, y, width, height, color);
		if (!this.components.containsKey(orientationType)) {
			this.components.put(orientationType, new ArrayList<>());
		}
		this.components.get(orientationType).add(l);
		return l;
	}

	@Override
	public ITexturedRect addTexturedRect(int id, int orientationType, String texture, int x, int y, int width,
			int height) {
		if (width <= 0 || height <= 0) {
			throw new CustomNPCsException("Invalid component width or height: [" + width + ", " + height + "]");
		}
		ICustomGuiComponent component = this.getComponent(orientationType, id);
		if (component instanceof CustomGuiTexturedRectWrapper) {
			CustomGuiTexturedRectWrapper txtr = (CustomGuiTexturedRectWrapper) component;
			txtr.setTexture(texture);
			txtr.setPos(x, y);
			txtr.setSize(width, height);
			return txtr;
		}
		if (component != null) {
			this.components.get(orientationType).remove(component);
		}
		CustomGuiTexturedRectWrapper txtr = new CustomGuiTexturedRectWrapper(id, texture, x, y, width, height);
		if (!this.components.containsKey(orientationType)) {
			this.components.put(orientationType, new ArrayList<>());
		}
		this.components.get(orientationType).add(txtr);
		return txtr;
	}

	@Override
	public ITexturedRect addTexturedRect(int id, int orientationType, String texture, int x, int y, int width,
			int height, int textureX, int textureY) {
		if (width <= 0 || height <= 0) {
			throw new CustomNPCsException("Invalid component width or height: [" + width + ", " + height + "]");
		}
		ICustomGuiComponent component = this.getComponent(orientationType, id);
		if (component instanceof CustomGuiTexturedRectWrapper) {
			CustomGuiTexturedRectWrapper txtr = (CustomGuiTexturedRectWrapper) component;
			txtr.setTexture(texture);
			txtr.setPos(x, y);
			txtr.setSize(width, height);
			txtr.setTextureOffset(textureX, textureY);
			return txtr;
		}
		if (component != null) {
			this.components.get(orientationType).remove(component);
		}
		CustomGuiTexturedRectWrapper txtr = new CustomGuiTexturedRectWrapper(id, texture, x, y, width, height, textureX,
				textureY);
		if (!this.components.containsKey(orientationType)) {
			this.components.put(orientationType, new ArrayList<>());
		}
		this.components.get(orientationType).add(txtr);
		return txtr;
	}

	@Override
	public IGuiTimer addTimer(int id, int orientationType, long start, long end, int x, int y, int width, int height) {
		if (width == 0 || height == 0) {
			throw new CustomNPCsException("Invalid component width or height: [" + width + ", " + height + "]");
		}
		ICustomGuiComponent component = this.getComponent(orientationType, id);
		if (component instanceof CustomGuiTimerWrapper) {
			CustomGuiTimerWrapper timer = (CustomGuiTimerWrapper) component;
			timer.setTime(start, end);
			timer.setPos(x, y);
			timer.setSize(width, height);
			timer.reverse = start > end;
			return timer;
		}
		if (component != null) {
			this.components.get(orientationType).remove(component);
		}
		CustomGuiTimerWrapper timer = new CustomGuiTimerWrapper(id, start, end, x, y, width, height);
		if (!this.components.containsKey(orientationType)) {
			this.components.put(orientationType, new ArrayList<>());
		}
		this.components.get(orientationType).add(timer);
		return timer;
	}

	@Override
	public IGuiTimer addTimer(int id, int orientationType, long start, long end, int x, int y, int width, int height,
			int color) {
		if (width == 0 || height == 0) {
			throw new CustomNPCsException("Invalid component width or height: [" + width + ", " + height + "]");
		}
		ICustomGuiComponent component = this.getComponent(orientationType, id);
		if (component instanceof CustomGuiTimerWrapper) {
			CustomGuiTimerWrapper timer = (CustomGuiTimerWrapper) component;
			timer.setTime(start, end);
			timer.setPos(x, y);
			timer.setSize(width, height);
			timer.setColor(color);
			return timer;
		}
		if (component != null) {
			this.components.get(orientationType).remove(component);
		}
		CustomGuiTimerWrapper timer = new CustomGuiTimerWrapper(id, start, end, x, y, width, height, color);
		if (!this.components.containsKey(orientationType)) {
			this.components.put(orientationType, new ArrayList<>());
		}
		this.components.get(orientationType).add(timer);
		return timer;
	}

	public void clear() {
		this.components.clear();
		this.slots.clear();
		this.guiComponents.clear();
		this.guiSlots.clear();
		this.update = true;
	}

	public void clearGuiComponents() {
		this.guiComponents.clear();
		this.guiSlots.clear();
	}

	@Override
	public ICompassData getCompassData() {
		return this.compassData;
	}

	@Override
	public ICustomGuiComponent getComponent(int orientationType, int componentId) {
		if (!this.components.containsKey(orientationType)) {
			return null;
		}
		for (ICustomGuiComponent component : this.components.get(orientationType)) {
			if (component.getId() == componentId) {
				return component;
			}
		}
		return null;
	}

	@Override
	public ICustomGuiComponent[] getComponents() {
		List<ICustomGuiComponent> list = new ArrayList<>();
		for (int type : this.components.keySet()) {
            list.addAll(this.components.get(type));
		}
		return list.toArray(new ICustomGuiComponent[0]);
	}

	@Override
	public ICustomGuiComponent[] getComponents(int orientationType) {
		if (!this.components.containsKey(orientationType)) {
			return new ICustomGuiComponent[0];
		}
		return components.get(orientationType).toArray(new ICustomGuiComponent[0]);
	}

	public TreeMap<Integer, TreeMap<Integer, IGuiComponent>> getGuiComponents() {
		if (this.guiComponents.size() != this.components.size()) {
			for (int type : this.components.keySet()) {
				if (!this.guiComponents.containsKey(type)) {
					this.guiComponents.put(type, new TreeMap<>());
				}
				for (ICustomGuiComponent component : this.components.get(type)) {
					if (component instanceof CustomGuiLabelWrapper) {
						CustomGuiLabel lbl = CustomGuiLabel.fromComponent((CustomGuiLabelWrapper) component);
						this.guiComponents.get(type).put(lbl.getId(), lbl);
					} else if (component instanceof CustomGuiTexturedRectWrapper) {
						CustomGuiTexturedRect rect = CustomGuiTexturedRect
								.fromComponent((CustomGuiTexturedRectWrapper) component);
						rect.id -= 200;
						this.guiComponents.get(type).put(rect.getId(), rect);
					} else if (component instanceof CustomGuiTimerWrapper) {
						CustomGuiTimer time = CustomGuiTimer.fromComponent((CustomGuiTimerWrapper) component);
						this.guiComponents.get(type).put(time.getId(), time);
					}
				}
			}
		}
		for (int type : this.guiComponents.keySet()) {
			if (!this.components.containsKey(type)) {
				continue;
			}
			for (int i : this.guiComponents.get(type).keySet()) {
				IGuiComponent gc = this.guiComponents.get(type).get(i);
				int id = (gc instanceof CustomGuiTexturedRect ? 200 : 0) + i;
				ICustomGuiComponent bc = this.getComponent(type, id);
				if (gc == null || bc == null) {
					continue;
				}
				int[] xy = gc.getPosXY();
				if (xy[0] != bc.getPosX() || xy[1] != bc.getPosY()) {
					gc.setPosXY(bc.getPosX(), bc.getPosY());
				}
			}
		}
		return this.guiComponents;
	}

	public TreeMap<Integer, TreeMap<Integer, IItemSlot>> getGuiSlots() {
		if (this.guiSlots.size() != this.slots.size()) {
			Map<Integer, Integer> ids = new TreeMap<>();
			for (int type : this.components.keySet()) {
				if (!ids.containsKey(type)) {
					ids.put(type, 0);
				}
				for (ICustomGuiComponent component : this.components.get(type)) {
					if (ids.get(type) < component.getId()) {
						ids.put(type, component.getId());
					}
				}
			}
			for (int type : this.slots.keySet()) {
				if (!this.guiSlots.containsKey(type)) {
					this.guiSlots.put(type, new TreeMap<>());
				}
				int id = ids.containsKey(type) ? ids.get(type) + 1 : 1;
				for (IItemSlot slot : this.slots.get(type)) {
					this.guiSlots.get(type).put(id++, slot);
				}
			}
		}
		return this.guiSlots;
	}

	public int[] getKeyPressed() {
		int[] ids = new int[this.keyPress.size()];
		int i = 0;
		for (int key : this.keyPress) {
			ids[i] = key;
			i++;
		}
		return ids;
	}

	public int[] getMousePressed() {
		int[] ids = new int[this.mousePress.size()];
		int i = 0;
		for (int key : this.mousePress) {
			ids[i] = key;
			i++;
		}
		return ids;
	}

	public NBTTagCompound getNBT() {
		NBTTagCompound hudNBT = new NBTTagCompound();
		hudNBT.setByteArray("ShowElementTypes", this.showElementTypes);

		NBTTagList list = new NBTTagList();
		list.appendTag(new NBTTagDouble(this.windowSize[0]));
		list.appendTag(new NBTTagDouble(this.windowSize[1]));
		hudNBT.setTag("WindowSize", list);
		hudNBT.setIntArray("KeyPress", this.getKeyPressed());
		hudNBT.setIntArray("MousePress", this.getMousePressed());
		hudNBT.setInteger("QuestID", this.questID);
		hudNBT.setTag("CompassData", this.compassData.getNbt());

		list = new NBTTagList();
		for (int type : this.components.keySet()) {
			NBTTagCompound nbtComp = new NBTTagCompound();
			nbtComp.setByte("OrientationType", (byte) type);
			NBTTagList compList = new NBTTagList();
			for (ICustomGuiComponent c : this.components.get(type)) {
				compList.appendTag(((CustomGuiComponentWrapper) c).toNBT(new NBTTagCompound()));
			}
			nbtComp.setTag("components", compList);
			list.appendTag(nbtComp);
		}
		hudNBT.setTag("AllComponents", list);

		list = new NBTTagList();
		for (int type : this.slots.keySet()) {
			NBTTagCompound nbtSlot = new NBTTagCompound();
			nbtSlot.setByte("OrientationType", (byte) type);
			NBTTagList slotList = new NBTTagList();
			for (ICustomGuiComponent c : this.slots.get(type)) {
				slotList.appendTag(((CustomGuiComponentWrapper) c).toNBT(new NBTTagCompound()));
			}
			nbtSlot.setTag("components", slotList);
			list.appendTag(nbtSlot);
		}
		hudNBT.setTag("AllSlots", list);

		hudNBT.setInteger("OffsetType", this.offsetType);

		return hudNBT;
	}

	@Override
	public IItemSlot[] getSlots() {
		List<IItemSlot> list = new ArrayList<>();
		for (int type : this.slots.keySet()) {
            list.addAll(this.slots.get(type));
		}
		return list.toArray(new IItemSlot[0]);
	}

	@Override
	public IItemSlot[] getSlots(int orientationType) {
        if (!this.slots.containsKey(orientationType)) {
			return new IItemSlot[0];
		}
		return slots.get(orientationType).toArray(new IItemSlot[0]);
	}

	public double[] getWindowSize() {
		return this.windowSize;
	}

	public boolean hasMousePress(int key) {
		for (int k : this.mousePress) {
			if (k == key) {
				return true;
			}
		}
		return this.mousePress.contains(key);
	}

	public boolean hasOrKeysPressed(int... keys) {
		for (int key : keys) {
			for (int k : this.keyPress) {
				if (k == key) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isShowElementType(int type) {
		if (type < 0 || type >= 22) {
			return false;
		}
		return this.showElementTypes[type] == (byte) 1;
	}

	public void loadNBTData(NBTTagCompound compound) {
		if (compound == null || !compound.hasKey("HUDData", 10)) {
			return;
		}
		NBTTagCompound hudNBT = compound.getCompoundTag("HUDData");
		this.showElementTypes = hudNBT.getByteArray("ShowElementTypes");
		for (int i = 0; i < 2 && i < hudNBT.getTagList("WindowSize", 6).tagCount(); i++) {
			this.windowSize[i] = hudNBT.getTagList("WindowSize", 6).getDoubleAt(i);
		}
		int[] iK = hudNBT.getIntArray("KeyPress");
		int[] iM = hudNBT.getIntArray("MousePress");
		this.keyPress.clear();
		this.mousePress.clear();
		for (int key : iK) {
			this.keyPress.add(key);
		}
		for (int key : iM) {
			this.mousePress.add(key);
		}

		this.questID = hudNBT.getInteger("QuestID");

		this.compassData.load(hudNBT.getCompoundTag("CompassData"));

		NBTTagList list = hudNBT.getTagList("AllComponents", 10);
		if (list.tagCount() == 0) {
			this.components.clear();
			this.guiComponents.clear();
		} else {
			for (int i = 0; i < list.tagCount(); i++) {
				List<Integer> ids = new ArrayList<>();
				NBTTagCompound compNbt = list.getCompoundTagAt(i);
				int type = compNbt.getByte("OrientationType");
				if (!this.components.containsKey(type)) {
					this.components.put(type, new ArrayList<>());
				}
				NBTTagList compList = compNbt.getTagList("components", 10);
				for (int j = 0; j < compList.tagCount(); j++) {
					int id = compList.getCompoundTagAt(j).getInteger("id");
					ids.add(id);
					boolean has = false;
					for (ICustomGuiComponent c : this.components.get(type)) {
						if (c.getId() == id) {
							if ((c.getClass() == CustomGuiLabelWrapper.class
									&& compList.getCompoundTagAt(j).getInteger("type") != 1)
									|| (c.getClass() == CustomGuiTexturedRectWrapper.class
											&& compList.getCompoundTagAt(j).getInteger("type") != 2)
									|| (c.getClass() == CustomGuiTimerWrapper.class
											&& compList.getCompoundTagAt(j).getInteger("type") != 6)) {
								this.components.get(type).remove(c);
							} else {
								((CustomGuiComponentWrapper) c).fromNBT(compList.getCompoundTagAt(j));
								has = true;
							}
							break;
						}
					}
					if (!has) {
						this.components.get(type)
								.add(CustomGuiComponentWrapper.createFromNBT(compList.getCompoundTagAt(j)));
					}
				}
				List<ICustomGuiComponent> del = new ArrayList<>();
				for (ICustomGuiComponent c : this.components.get(type)) {
					if (!ids.contains(c.getId())) {
						del.add(c);
					}
				}
				for (ICustomGuiComponent c : del) {
					this.components.get(type).remove(c);
				}
			}
		}

		list = hudNBT.getTagList("AllSlots", 10);
		if (list.tagCount() == 0) {
			this.slots.clear();
			this.guiSlots.clear();
		} else {
			for (int i = 0; i < list.tagCount(); i++) {
				List<Integer> ids = new ArrayList<>();
				NBTTagCompound compNbt = list.getCompoundTagAt(i);
				int type = compNbt.getByte("OrientationType");
				if (!this.slots.containsKey(type)) {
					this.slots.put(type, new ArrayList<>());
				}
				NBTTagList compList = compNbt.getTagList("components", 10);
				for (int j = 0; j < compList.tagCount(); j++) {
					int id = compList.getCompoundTagAt(j).getInteger("id");
					ids.add(id);
					boolean has = false;
					for (IItemSlot s : this.slots.get(type)) {
						if (s.getId() == id) {
							if (s.getClass() != CustomGuiItemSlotWrapper.class) {
								this.slots.get(type).remove(s);
							} else {
								((CustomGuiComponentWrapper) s).fromNBT(compList.getCompoundTagAt(j));
								has = true;
							}
							break;
						}
					}
					if (!has) {
						this.slots.get(type).add((CustomGuiItemSlotWrapper) CustomGuiComponentWrapper
								.createFromNBT(compList.getCompoundTagAt(j)));
					}
				}
				List<IItemSlot> del = new ArrayList<>();
				for (IItemSlot c : this.slots.get(type)) {
					if (!ids.contains(c.getId())) {
						del.add(c);
					}
				}
				for (IItemSlot c : del) {
					this.slots.get(type).remove(c);
				}
			}
		}
		this.offsetType = hudNBT.getInteger("OffsetType");

		this.guiComponents.clear();
		this.guiSlots.clear();
		this.update = false;
	}

	@Override
	public boolean removeComponent(int orientationType, int componentId) {
		if (!this.components.containsKey(orientationType)) {
			return false;
		}
		for (ICustomGuiComponent comp : this.components.get(orientationType)) {
			if (comp.getId() == componentId) {
				this.components.get(orientationType).remove(comp);
				this.update = true;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean removeSlot(int orientationType, int slotId) {
		if (!this.slots.containsKey(orientationType)) {
			return false;
		}
		for (IItemSlot comp : this.slots.get(orientationType)) {
			if (comp.getId() == slotId) {
				this.slots.get(orientationType).remove(comp);
				this.update = true;
				return true;
			}
		}
		return false;
	}

	public NBTTagCompound saveNBTData(NBTTagCompound compound) {
		compound.setTag("HUDData", this.getNBT());
		return compound;
	}

	@Override
	public void setShowElementType(int type, boolean bo) {
		if (type < 0 || type >= 22) {
			return;
		}
		this.showElementTypes[type] = (byte) (bo ? 1 : 0);
	}

	@Override
	public void setShowElementType(String name, boolean bo) {
		int type;
		switch (name.toLowerCase()) {
		case "all": {
			type = 0;
			break;
		}
		case "helmet": {
			type = 1;
			break;
		}
		case "portal": {
			type = 2;
			break;
		}
		case "crosshairs": {
			type = 3;
			break;
		}
		case "bosshealth": {
			type = 4;
			break;
		}
		case "bossinfo": {
			type = 5;
			break;
		}
		case "armor": {
			type = 6;
			break;
		}
		case "health": {
			type = 7;
			break;
		}
		case "food": {
			type = 8;
			break;
		}
		case "air": {
			type = 9;
			break;
		}
		case "hotbar": {
			type = 10;
			break;
		}
		case "experience": {
			type = 11;
			break;
		}
		case "text": {
			type = 12;
			break;
		}
		case "healthmount": {
			type = 13;
			break;
		}
		case "jumpbar": {
			type = 14;
			break;
		}
		case "chat": {
			type = 15;
			break;
		}
		case "player_list": {
			type = 16;
			break;
		}
		case "debug": {
			type = 17;
			break;
		}
		case "potion_icons": {
			type = 18;
			break;
		}
		case "subtitles": {
			type = 19;
			break;
		}
		case "fps_graph": {
			type = 20;
			break;
		}
		case "vignette": {
			type = 21;
			break;
		}
		default: {
			return;
		}
		}
		this.showElementTypes[type] = (byte) (bo ? 1 : 0);
	}

	public void setWindowSize(NBTTagList tagList) {
		if (tagList == null || tagList.getTagType() != 6) {
			return;
		}
		for (int i = 0; i < 2 && i < tagList.tagCount(); i++) {
			this.windowSize[i] = tagList.getDoubleAt(i);
		}
	}

	@Override
	public void update() {
		if (this.player != null) {
			Server.sendData(this.player, EnumPacketClient.UPDATE_HUD,
					this.saveNBTData(new NBTTagCompound()));
			this.update = false;
		} else {
			CustomNPCsScheduler.runTack(() -> this.update = true , 500);
		}
	}

	public void updateHud(EntityPlayerMP player) {
		this.player = player;
		if (this.update) {
			Server.sendData(this.player, EnumPacketClient.UPDATE_HUD,
					this.saveNBTData(new NBTTagCompound()));
			this.update = false;
		}
	}

}
