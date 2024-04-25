package noppes.npcs.api.wrapper.gui;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.gui.IButton;
import noppes.npcs.api.gui.ICustomGui;
import noppes.npcs.api.gui.ICustomGuiComponent;
import noppes.npcs.api.gui.IGuiEntity;
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.api.gui.ILabel;
import noppes.npcs.api.gui.IScroll;
import noppes.npcs.api.gui.ITextField;
import noppes.npcs.api.gui.ITexturedRect;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.wrapper.ItemScriptedWrapper;
import noppes.npcs.api.wrapper.PlayerWrapper;
import noppes.npcs.controllers.CustomGuiController;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.util.ObfuscationHelper;

public class CustomGuiWrapper implements ICustomGui {

	String backgroundTexture;
	List<ICustomGuiComponent> components;
	int height;
	int id;
	boolean pauseGame;
	// New
	private EntityPlayer player;
	int playerInvX, playerInvY;
	ScriptContainer scriptHandler;
	boolean showPlayerInv, showPlayerSlots, isIndependent;
	List<IItemSlot> slots;
	int width;
	public int stretched, bgW, bgH, bgTx, bgTy;

	public CustomGuiWrapper(EntityPlayer player) {
		this.backgroundTexture = "";
		this.components = new ArrayList<ICustomGuiComponent>();
		this.slots = new ArrayList<IItemSlot>();
		this.isIndependent = false;
		this.showPlayerSlots = true;
		this.player = player;
		this.stretched = 0;
		this.bgW = 0;
		this.bgH = 0;
		this.bgTx = 256;
		this.bgTy = 256;
	}

	public CustomGuiWrapper(int id, int width, int height, boolean pauseGame, EntityPlayer player) {
		this.backgroundTexture = "";
		this.components = new ArrayList<ICustomGuiComponent>();
		this.slots = new ArrayList<IItemSlot>();
		this.id = id;
		this.width = width;
		this.height = height;
		this.pauseGame = pauseGame;
		this.scriptHandler = ScriptContainer.Current;
		this.isIndependent = false;
		this.showPlayerSlots = true;
		this.player = player;
		this.stretched = 0;
		this.bgW = 0;
		this.bgH = 0;
		this.bgTx = 256;
		this.bgTy = 256;
	}

	@Override
	public IButton addButton(int id, String label, int x, int y) {
		CustomGuiButtonWrapper component = new CustomGuiButtonWrapper(id, label, x, y);
		this.components.add(component);
		return (IButton) this.components.get(this.components.size() - 1);
	}

	@Override
	public IButton addButton(int id, String label, int x, int y, int width, int height) {
		CustomGuiButtonWrapper component = new CustomGuiButtonWrapper(id, label, x, y, width, height);
		this.components.add(component);
		return (IButton) this.components.get(this.components.size() - 1);
	}

	@Override
	public IGuiEntity addEntity(int id, int x, int y, IEntity<?> entity) {
		CustomGuiEntityWrapper component = new CustomGuiEntityWrapper(id, x, y, entity);
		this.components.add(component);
		return (IGuiEntity) this.components.get(this.components.size() - 1);
	}

	@Override
	public IItemSlot addItemSlot(int x, int y) {
		return this.addItemSlot(x, y, ItemScriptedWrapper.AIR);
	}

	@Override
	public IItemSlot addItemSlot(int x, int y, IItemStack stack) {
		CustomGuiItemSlotWrapper slot = new CustomGuiItemSlotWrapper(x, y, stack);
		this.slots.add(slot);
		return slot;
	}

	@Override
	public ILabel addLabel(int id, String label, int x, int y, int width, int height) {
		CustomGuiLabelWrapper component = new CustomGuiLabelWrapper(id, label, x, y, width, height);
		this.components.add(component);
		return (ILabel) this.components.get(this.components.size() - 1);
	}

	@Override
	public ILabel addLabel(int id, String label, int x, int y, int width, int height, int color) {
		CustomGuiLabelWrapper component = new CustomGuiLabelWrapper(id, label, x, y, width, height, color);
		this.components.add(component);
		return (ILabel) this.components.get(this.components.size() - 1);
	}

	@Override
	public IScroll addScroll(int id, int x, int y, int width, int height, String[] list) {
		CustomGuiScrollWrapper component = new CustomGuiScrollWrapper(id, x, y, width, height, list);
		this.components.add(component);
		return (IScroll) this.components.get(this.components.size() - 1);
	}

	@Override
	public ITextField addTextField(int id, int x, int y, int width, int height) {
		CustomGuiTextFieldWrapper component = new CustomGuiTextFieldWrapper(id, x, y, width, height);
		this.components.add(component);
		return (ITextField) this.components.get(this.components.size() - 1);
	}

	@Override
	public IButton addTexturedButton(int id, String label, int x, int y, int width, int height, String texture) {
		CustomGuiButtonWrapper component = new CustomGuiButtonWrapper(id, label, x, y, width, height, texture);
		this.components.add(component);
		return (IButton) this.components.get(this.components.size() - 1);
	}

	@Override
	public IButton addTexturedButton(int id, String label, int x, int y, int width, int height, String texture,
			int textureX, int textureY) {
		CustomGuiButtonWrapper component = new CustomGuiButtonWrapper(id, label, x, y, width, height, texture, textureX,
				textureY);
		this.components.add(component);
		return (IButton) this.components.get(this.components.size() - 1);
	}

	@Override
	public ITexturedRect addTexturedRect(int id, String texture, int x, int y, int width, int height) {
		CustomGuiTexturedRectWrapper component = new CustomGuiTexturedRectWrapper(id, texture, x, y, width, height);
		this.components.add(component);
		return (ITexturedRect) this.components.get(this.components.size() - 1);
	}

	@Override
	public ITexturedRect addTexturedRect(int id, String texture, int x, int y, int width, int height, int textureX,
			int textureY) {
		CustomGuiTexturedRectWrapper component = new CustomGuiTexturedRectWrapper(id, texture, x, y, width, height,
				textureX, textureY);
		this.components.add(component);
		return (ITexturedRect) this.components.get(this.components.size() - 1);
	}

	public ICustomGui fromNBT(NBTTagCompound tag) {
		this.id = tag.getInteger("id");
		this.width = tag.getIntArray("size")[0];
		this.height = tag.getIntArray("size")[1];
		this.pauseGame = tag.getBoolean("pause");
		this.backgroundTexture = tag.getString("bgTexture");
		this.stretched = tag.getInteger("bgStretched");
		this.bgW = tag.getInteger("bgWidth");
		this.bgH = tag.getInteger("bgHeight");
		this.bgTx = tag.getInteger("bgTextureX");
		this.bgTy = tag.getInteger("bgTextureY");
		this.isIndependent = tag.getBoolean("isIndependent");
		List<ICustomGuiComponent> components = new ArrayList<ICustomGuiComponent>();
		NBTTagList list = tag.getTagList("components", 10);
		for (NBTBase b : list) {
			CustomGuiComponentWrapper component = CustomGuiComponentWrapper.createFromNBT((NBTTagCompound) b);
			components.add(component);
		}
		this.components = components;
		List<IItemSlot> slots = new ArrayList<IItemSlot>();
		list = tag.getTagList("slots", 10);
		for (NBTBase b2 : list) {
			CustomGuiItemSlotWrapper component2 = (CustomGuiItemSlotWrapper) CustomGuiComponentWrapper
					.createFromNBT((NBTTagCompound) b2);
			slots.add(component2);
		}
		this.slots = slots;
		if (this.player instanceof EntityPlayerMP) {
			this.setPlayer((EntityPlayerMP) this.player);
		} // New
		this.showPlayerInv = tag.getBoolean("showPlayerInv");
		this.showPlayerSlots = tag.getBoolean("showPlayerSlots");
		if (this.showPlayerInv) {
			this.playerInvX = tag.getIntArray("pInvPos")[0];
			this.playerInvY = tag.getIntArray("pInvPos")[1];
		}
		return this;
	}

	public String getBackgroundTexture() {
		return this.backgroundTexture;
	}

	@Override
	public ICustomGuiComponent getComponent(int componentID) {
		for (ICustomGuiComponent component : this.components) {
			if (component.getId() == componentID) {
				return component;
			}
		}
		return null;
	}

	@Override
	public ICustomGuiComponent[] getComponents() {
		return this.components.toArray(new ICustomGuiComponent[this.components.size()]);
	}

	public boolean getDoesPauseGame() {
		return this.pauseGame;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public int getId() {
		return this.id;
	}

	public int getPlayerInvX() {
		return this.playerInvX;
	}

	public int getPlayerInvY() {
		return this.playerInvY;
	}

	public ScriptContainer getScriptHandler() {
		return this.scriptHandler;
	}

	public boolean getShowPlayerInv() {
		return this.showPlayerInv;
	}

	public boolean getShowPlayerSlots() {
		return this.showPlayerInv && this.showPlayerSlots;
	}

	@Override
	public IItemSlot[] getSlots() {
		if (this.player instanceof EntityPlayerMP) {
			this.setPlayer((EntityPlayerMP) this.player);
		}
		return this.slots.toArray(new IItemSlot[this.slots.size()]);
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public void removeComponent(int componentID) {
		for (int i = 0; i < this.components.size(); ++i) {
			if (this.components.get(i).getId() == componentID) {
				this.components.remove(i);
				if (this.player instanceof EntityPlayerMP) { // New
					this.update((IPlayer<?>) NpcAPI.Instance().getIEntity(this.player));
				}
				return;
			}
		}
	}

	@Override
	public void setBackgroundTexture(int width, int height, int textureX, int textureY, int stretched,
			String resourceLocation) {
		this.backgroundTexture = resourceLocation;
		this.stretched = stretched;
		this.bgW = width;
		this.bgH = height;
		this.bgTx = textureX;
		this.bgTy = textureY;
	}

	@Override
	public void setBackgroundTexture(String resourceLocation) {
		this.backgroundTexture = resourceLocation;
		this.stretched = 0;
		this.bgW = 0;
		this.bgH = 0;
		this.bgTx = 256;
		this.bgTy = 256;
	}

	@Override
	public void setDoesPauseGame(boolean pauseGame) {
		this.pauseGame = pauseGame;
	}

	// New
	public void setPlayer(EntityPlayerMP player) {
		this.player = player;
		if (this.slots.size() == 0) {
			return;
		}
		for (int i = 0; i < this.slots.size(); i++) {
			CustomGuiItemSlotWrapper slot = (CustomGuiItemSlotWrapper) this.slots.get(i);
			ObfuscationHelper.setValue(CustomGuiItemSlotWrapper.class, slot, player, EntityPlayer.class);
			ObfuscationHelper.setValue(CustomGuiItemSlotWrapper.class, slot, i, int.class);
		}
	}

	@Override
	public void setSize(int width, int height) {
		if (width <= 0 || height <= 0) {
			throw new CustomNPCsException("Invalid component width or height: [" + width + ", " + height + "]");
		}
		this.width = width;
		this.height = height;
	}

	@Override
	public void showPlayerInventory(int x, int y) {
		this.showPlayerInv = true;
		this.showPlayerSlots = false;
		this.playerInvX = x;
		this.playerInvY = y;
	}

	@Override
	public void showPlayerInventory(int x, int y, boolean showSlots) {
		this.showPlayerInv = true;
		this.playerInvX = x;
		this.playerInvY = y;
		this.showPlayerSlots = showSlots;
	}

	public NBTTagCompound toNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("id", this.id);
		tag.setIntArray("size", new int[] { this.width, this.height });
		tag.setBoolean("pause", this.pauseGame);
		tag.setString("bgTexture", this.backgroundTexture);
		tag.setInteger("bgStretched", this.stretched);
		tag.setInteger("bgWidth", this.bgW);
		tag.setInteger("bgHeight", this.bgH);
		tag.setInteger("bgTextureX", this.bgTx);
		tag.setInteger("bgTextureY", this.bgTy);
		tag.setBoolean("isIndependent", this.isIndependent);
		NBTTagList list = new NBTTagList();
		for (ICustomGuiComponent c : this.components) {
			if (c == null) {
				continue;
			}
			list.appendTag(((CustomGuiComponentWrapper) c).toNBT(new NBTTagCompound()));
		}
		tag.setTag("components", list);
		list = new NBTTagList();
		for (ICustomGuiComponent c : this.slots) {
			list.appendTag(((CustomGuiItemSlotWrapper) c).toNBT(new NBTTagCompound()));
		}
		tag.setTag("slots", list);
		tag.setBoolean("showPlayerInv", this.showPlayerInv);
		tag.setBoolean("showPlayerSlots", this.showPlayerSlots);
		if (this.showPlayerInv) {
			tag.setIntArray("pInvPos", new int[] { this.playerInvX, this.playerInvY });
		}
		return tag;
	}

	@Override
	public void update(IPlayer<?> player) {
		CustomGuiController.updateGui((PlayerWrapper<?>) player, this);
		player.getMCEntity().openContainer.detectAndSendChanges(); // New
	}

	@Override
	public void updateComponent(ICustomGuiComponent component) {
		for (int i = 0; i < this.components.size(); ++i) {
			ICustomGuiComponent c = this.components.get(i);
			if (c.getId() == component.getId()) {
				this.components.set(i, component);
				if (this.player instanceof EntityPlayerMP) { // New
					this.update((IPlayer<?>) NpcAPI.Instance().getIEntity(this.player));
				}
				return;
			}
		}
	}

}
