package noppes.npcs.api.wrapper.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

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

public class CustomGuiWrapper implements ICustomGui {

	protected List<ICustomGuiComponent> components = new ArrayList<>();
	protected ScriptContainer scriptHandler = ScriptContainer.Current;
	protected String backgroundTexture = "";
	protected List<IItemSlot> slots = new ArrayList<>();
	protected EntityPlayer player;
	protected boolean showPlayerSlots = true;
	protected boolean showPlayerInv;
	protected boolean isIndependent = false;
	protected boolean pauseGame;
	protected int height;
	protected int id;
	protected int playerInvX;
	protected int playerInvY;
	protected int width;
	public int stretched = 0;
	public int bgW = 0;
	public int bgH = 0;
	public int bgTx = 256;
	public int bgTy = 256;

	public CustomGuiWrapper(EntityPlayer playerIn) { player = playerIn; }

	public CustomGuiWrapper(int idIn, int widthIn, int heightIn, boolean pauseGameIn, EntityPlayer player) {
		this(player);
		id = idIn;
		width = widthIn;
		height = heightIn;
		pauseGame = pauseGameIn;
	}

	@Override
	public IButton addButton(int id, String label, int x, int y) {
		CustomGuiButtonWrapper component = new CustomGuiButtonWrapper(id, label, x, y);
		components.add(component);
		return (IButton) components.get(components.size() - 1);
	}

	@Override
	public IButton addButton(int id, String label, int x, int y, int width, int height) {
		CustomGuiButtonWrapper component = new CustomGuiButtonWrapper(id, label, x, y, width, height);
		components.add(component);
		return (IButton) components.get(components.size() - 1);
	}

	@Override
	public IGuiEntity addEntity(int id, int x, int y, IEntity<?> entity) {
		CustomGuiEntityWrapper component = new CustomGuiEntityWrapper(id, x, y, entity);
		components.add(component);
		return (IGuiEntity) components.get(components.size() - 1);
	}

	@Override
	public IItemSlot addItemSlot(int x, int y) {
		return addItemSlot(x, y, ItemScriptedWrapper.AIR);
	}

	@Override
	public IItemSlot addItemSlot(int x, int y, IItemStack stack) {
		CustomGuiItemSlotWrapper slot = new CustomGuiItemSlotWrapper(x, y, stack);
		slots.add(slot);
		return slot;
	}

	@Override
	public ILabel addLabel(int id, String label, int x, int y, int width, int height) {
		CustomGuiLabelWrapper component = new CustomGuiLabelWrapper(id, label, x, y, width, height);
		components.add(component);
		return (ILabel) components.get(components.size() - 1);
	}

	@Override
	public ILabel addLabel(int id, String label, int x, int y, int width, int height, int color) {
		CustomGuiLabelWrapper component = new CustomGuiLabelWrapper(id, label, x, y, width, height, color);
		components.add(component);
		return (ILabel) components.get(components.size() - 1);
	}

	@Override
	public IScroll addScroll(int id, int x, int y, int width, int height, String[] list) {
		CustomGuiScrollWrapper component = new CustomGuiScrollWrapper(id, x, y, width, height, list);
		components.add(component);
		return (IScroll) components.get(components.size() - 1);
	}

	@Override
	public ITextField addTextField(int id, int x, int y, int width, int height) {
		CustomGuiTextFieldWrapper component = new CustomGuiTextFieldWrapper(id, x, y, width, height);
		components.add(component);
		return (ITextField) components.get(components.size() - 1);
	}

	@Override
	public IButton addTexturedButton(int id, String label, int x, int y, int width, int height, String texture) {
		CustomGuiButtonWrapper component = new CustomGuiButtonWrapper(id, label, x, y, width, height, texture);
		components.add(component);
		return (IButton) components.get(components.size() - 1);
	}

	@Override
	public IButton addTexturedButton(int id, String label, int x, int y, int width, int height, String texture,
			int textureX, int textureY) {
		CustomGuiButtonWrapper component = new CustomGuiButtonWrapper(id, label, x, y, width, height, texture, textureX,
				textureY);
		components.add(component);
		return (IButton) components.get(components.size() - 1);
	}

	@Override
	public ITexturedRect addTexturedRect(int id, String texture, int x, int y, int width, int height) {
		CustomGuiTexturedRectWrapper component = new CustomGuiTexturedRectWrapper(id, texture, x, y, width, height);
		components.add(component);
		return (ITexturedRect) components.get(components.size() - 1);
	}

	@Override
	public ITexturedRect addTexturedRect(int id, String texture, int x, int y, int width, int height, int textureX, int textureY) {
		CustomGuiTexturedRectWrapper component = new CustomGuiTexturedRectWrapper(id, texture, x, y, width, height, textureX, textureY);
		components.add(component);
		return (ITexturedRect) components.get(components.size() - 1);
	}

	public ICustomGui fromNBT(NBTTagCompound tag) {
		id = tag.getInteger("id");
		width = tag.getIntArray("size")[0];
		height = tag.getIntArray("size")[1];
		pauseGame = tag.getBoolean("pause");
		backgroundTexture = tag.getString("bgTexture");
		stretched = tag.getInteger("bgStretched");
		bgW = tag.getInteger("bgWidth");
		bgH = tag.getInteger("bgHeight");
		bgTx = tag.getInteger("bgTextureX");
		bgTy = tag.getInteger("bgTextureY");
		isIndependent = tag.getBoolean("isIndependent");
		components.clear();
		NBTTagList list = tag.getTagList("components", 10);
		for (NBTBase b : list) {
			CustomGuiComponentWrapper component = CustomGuiComponentWrapper.createFromNBT((NBTTagCompound) b);
			components.add(component);
		}
		slots.clear();
		list = tag.getTagList("slots", 10);
		for (NBTBase b2 : list) {
			CustomGuiItemSlotWrapper component2 = (CustomGuiItemSlotWrapper) CustomGuiComponentWrapper.createFromNBT((NBTTagCompound) b2);
			slots.add(component2);
		}
		if (player instanceof EntityPlayerMP) { setPlayer((EntityPlayerMP) player); }
		showPlayerInv = tag.getBoolean("showPlayerInv");
		showPlayerSlots = tag.getBoolean("showPlayerSlots");
		if (showPlayerInv) {
			playerInvX = tag.getIntArray("pInvPos")[0];
			playerInvY = tag.getIntArray("pInvPos")[1];
		}
		return this;
	}

	public String getBackgroundTexture() { return backgroundTexture; }

	@Override
	public ICustomGuiComponent getComponent(int componentID) {
		for (ICustomGuiComponent component : components) {
			if (component.getId() == componentID) { return component; }
		}
		return null;
	}

	@Override
	public ICustomGuiComponent[] getComponents() {
		return components.toArray(new ICustomGuiComponent[0]);
	}

	public boolean getDoesPauseGame() {
		return pauseGame;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public int getId() {
		return id;
	}

	public int getPlayerInvX() {
		return playerInvX;
	}

	public int getPlayerInvY() {
		return playerInvY;
	}

	public ScriptContainer getScriptHandler() {
		return scriptHandler;
	}

	public boolean getShowPlayerInv() {
		return showPlayerInv;
	}

	public boolean getShowPlayerSlots() {
		return showPlayerInv && showPlayerSlots;
	}

	@Override
	public IItemSlot[] getSlots() {
		if (player instanceof EntityPlayerMP) { setPlayer((EntityPlayerMP) player); }
		return slots.toArray(new IItemSlot[0]);
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public void removeComponent(int componentID) {
		for (int i = 0; i < components.size(); ++i) {
			if (components.get(i).getId() == componentID) {
				components.remove(i);
				if (player instanceof EntityPlayerMP) {
					update((IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player));
				}
				return;
			}
		}
	}

	@Override
	public void setBackgroundTexture(int width, int height, int textureX, int textureY, int stretchedIn, String resourceLocation) {
		backgroundTexture = resourceLocation;
		stretched = stretchedIn;
		bgW = width;
		bgH = height;
		bgTx = textureX;
		bgTy = textureY;
	}

	@Override
	public void setBackgroundTexture(String resourceLocation) {
		backgroundTexture = resourceLocation;
		stretched = 0;
		bgW = 0;
		bgH = 0;
		bgTx = 256;
		bgTy = 256;
	}

	@Override
	public void setDoesPauseGame(boolean pauseGameIn) { pauseGame = pauseGameIn; }

	public void setPlayer(EntityPlayerMP playerIn) {
		player = playerIn;
		if (slots.isEmpty()) { return; }
		for (int i = 0; i < slots.size(); i++) {
			CustomGuiItemSlotWrapper slot = (CustomGuiItemSlotWrapper) slots.get(i);
			slot.setPlayer(playerIn);
			slot.setIndex(i);
		}
	}

	@Override
	public void setSize(int widthIn, int heightIn) {
		if (widthIn <= 0 || heightIn <= 0) {
			throw new CustomNPCsException("Invalid component width or height: [" + widthIn + ", " + heightIn + "]");
		}
		width = widthIn;
		height = heightIn;
	}

	@Override
	public void showPlayerInventory(int x, int y) {
		showPlayerInv = true;
		showPlayerSlots = false;
		playerInvX = x;
		playerInvY = y;
	}

	@Override
	public void showPlayerInventory(int x, int y, boolean showSlots) {
		showPlayerInv = true;
		playerInvX = x;
		playerInvY = y;
		showPlayerSlots = showSlots;
	}

	public NBTTagCompound toNBT() {
		NBTTagCompound tag = new NBTTagCompound();
		tag.setInteger("id", id);
		tag.setIntArray("size", new int[] { width, height });
		tag.setBoolean("pause", pauseGame);
		tag.setString("bgTexture", backgroundTexture);
		tag.setInteger("bgStretched", stretched);
		tag.setInteger("bgWidth", bgW);
		tag.setInteger("bgHeight", bgH);
		tag.setInteger("bgTextureX", bgTx);
		tag.setInteger("bgTextureY", bgTy);
		tag.setBoolean("isIndependent", isIndependent);
		NBTTagList list = new NBTTagList();
		for (ICustomGuiComponent c : components) {
			if (c == null) { continue; }
			list.appendTag(((CustomGuiComponentWrapper) c).toNBT(new NBTTagCompound()));
		}
		tag.setTag("components", list);
		list = new NBTTagList();
		for (ICustomGuiComponent c : slots) {
			list.appendTag(((CustomGuiItemSlotWrapper) c).toNBT(new NBTTagCompound()));
		}
		tag.setTag("slots", list);
		tag.setBoolean("showPlayerInv", showPlayerInv);
		tag.setBoolean("showPlayerSlots", showPlayerSlots);
		if (showPlayerInv) { tag.setIntArray("pInvPos", new int[] { playerInvX, playerInvY }); }
		return tag;
	}

	@Override
	public void update(IPlayer<?> player) {
		CustomGuiController.updateGui((PlayerWrapper<?>) player, this);
		player.getMCEntity().openContainer.detectAndSendChanges();
	}

	@Override
	public void updateComponent(ICustomGuiComponent component) {
		for (int i = 0; i < components.size(); ++i) {
			ICustomGuiComponent c = components.get(i);
			if (c.getId() == component.getId()) {
				components.set(i, component);
				if (player instanceof EntityPlayerMP) { update((IPlayer<?>) Objects.requireNonNull(NpcAPI.Instance()).getIEntity(player)); }
				return;
			}
		}
	}

}
