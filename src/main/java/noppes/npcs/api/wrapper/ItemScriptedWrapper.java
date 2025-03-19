package noppes.npcs.api.wrapper;

import java.util.*;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.EventHooks;
import noppes.npcs.NBTTags;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.constants.ItemType;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.event.ItemEvent;
import noppes.npcs.api.item.IItemScripted;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.SyncController;
import noppes.npcs.items.ItemScripted;

public class ItemScriptedWrapper extends ItemStackWrapper implements IItemScripted, IScriptHandler {

	public int durabilityColor;
	public boolean durabilityShow;
	public double durabilityValue;
	public boolean enabled;
	public int itemColor;
	public long lastInited;
	public boolean loaded;
	public String scriptLanguage;
	public List<ScriptContainer> scripts;
	public int stackSize;
	public boolean updateClient;

	public ItemScriptedWrapper(ItemStack item) {
		super(item);
		this.scripts = new ArrayList<>();
		this.scriptLanguage = "ECMAScript";
		this.enabled = false;
		this.lastInited = -1L;
		this.updateClient = false;
		this.durabilityShow = true;
		this.durabilityValue = 1.0;
		this.durabilityColor = -1;
		this.itemColor = -1;
		this.stackSize = 64;
		this.loaded = false;
	}

	@Override
	public void clearConsole() {
		for (ScriptContainer script : this.getScripts()) {
			script.console.clear();
		}
	}

	@Override
	public boolean getEnabled() { return this.enabled; }

	@Override
	public int getColor() {
		return this.itemColor;
	}

	@Override
	public TreeMap<Long, String> getConsoleText() {
		TreeMap<Long, String> map = new TreeMap<>();
		int tab = 0;
		for (ScriptContainer script : this.getScripts()) {
			++tab;
			for (Map.Entry<Long, String> entry : script.console.entrySet()) {
				String log;
				if (map.containsKey(entry.getKey())) { log = map.get(entry.getKey()) + "\n\n" + "ScriptTab " + tab + ":\n" + entry.getValue(); }
				else { log = " ScriptTab " + tab + ":\n" + entry.getValue(); }
				map.put(entry.getKey(), log);
			}
		}
		return map;
	}

	@Override
	public void clearConsoleText(Long key) {
		for (ScriptContainer script : this.getScripts()) {
			script.console.remove(key);
		}
	}

	@Override
	public int getDurabilityColor() {
		return this.durabilityColor;
	}

	@Override
	public boolean getDurabilityShow() {
		return this.durabilityShow;
	}

	@Override
	public double getDurabilityValue() {
		return this.durabilityValue;
	}

	@Override
	public String getLanguage() {
		return this.scriptLanguage;
	}

	@Override
	public int getMaxStackSize() {
		return this.stackSize;
	}

	@Override
	public NBTTagCompound getMCNbt() {
		NBTTagCompound compound = super.getMCNbt();
		this.getScriptNBT(compound);
		compound.setBoolean("DurabilityShow", this.durabilityShow);
		compound.setDouble("DurabilityValue", this.durabilityValue);
		compound.setInteger("DurabilityColor", this.durabilityColor);
		compound.setInteger("ItemColor", this.itemColor);
		compound.setInteger("MaxStackSize", this.stackSize);
		return compound;
	}

	public NBTTagCompound getScriptNBT(NBTTagCompound compound) {
		compound.setTag("Scripts", NBTTags.NBTScript(this.scripts));
		compound.setString("ScriptLanguage", this.scriptLanguage);
		compound.setBoolean("ScriptEnabled", this.enabled);
		return compound;
	}

	@Override
	public List<ScriptContainer> getScripts() {
		return this.scripts;
	}

	@Override
	public String getTexture(int damage) {
		return ItemScripted.Resources.get(damage);
	}

	@Override
	public int getType() {
		return ItemType.SCRIPTED.get();
	}

	@Override
	public boolean hasTexture(int damage) {
		return ItemScripted.Resources.containsKey(damage);
	}

	@Override
	public boolean isClient() {
		return false;
	}

	public boolean isEnabled() {
		return this.enabled && ScriptController.HasStart && !this.scripts.isEmpty();
	}

	public void loadScriptData() {
		NBTTagCompound c = this.item.getTagCompound();
		if (c == null) {
			return;
		}
		this.setScriptNBT(c.getCompoundTag("ScriptedData"));
	}

	@Override
	public ITextComponent noticeString(String type, Object event) {
		ITextComponent message = new TextComponentString("Scripted Item Script");
		message.getStyle().setColor(TextFormatting.DARK_GRAY);
		if (type != null) {
			ITextComponent hook = new TextComponentString(" hook \"");
			hook.getStyle().setColor(TextFormatting.DARK_GRAY);
			ITextComponent hookType = new TextComponentString(type);
			hookType.getStyle().setColor(TextFormatting.GRAY);
			ITextComponent hookEnd = new TextComponentString("\"; ");
			hookEnd.getStyle().setColor(TextFormatting.DARK_GRAY);
			message = message.appendSibling(hook).appendSibling(hookType).appendSibling(hookEnd);
		}
		IPlayer<?> iPlayer = getIPlayer(event);
		if (iPlayer != null) {
			ITextComponent mesPlayer = new TextComponentString("Player \"");
			mesPlayer.getStyle().setColor(TextFormatting.DARK_GRAY);
			ITextComponent name = new TextComponentString(iPlayer.getName());
			name.getStyle().setColor(TextFormatting.GRAY);
			ITextComponent mesUUID = new TextComponentString("\"; UUID: \"");
			mesUUID.getStyle().setColor(TextFormatting.DARK_GRAY);
			ITextComponent uuid = new TextComponentString(iPlayer.getUUID());
			uuid.getStyle().setColor(TextFormatting.GRAY);
			ITextComponent mesEnd = new TextComponentString("\" in ");
			mesEnd.getStyle().setColor(TextFormatting.DARK_GRAY);
			message = message.appendSibling(mesPlayer).appendSibling(name).appendSibling(mesUUID).appendSibling(uuid).appendSibling(mesEnd);
			int dimID = iPlayer.getWorld().getMCWorld() == null ? 0 : iPlayer.getWorld().getMCWorld().provider.getDimension();
			double x = Math.round(iPlayer.getPos().getX() * 100.0d) / 100.0d;
			double y = Math.round(iPlayer.getPos().getX() * 100.0d) / 100.0d;
			double z = Math.round(iPlayer.getPos().getX() * 100.0d) / 100.0d;
			ITextComponent posClick = new TextComponentString("dimension ID:" + dimID + "; X:" + x + "; Y:" + y + "; Z:" + z);
			posClick.getStyle().setColor(TextFormatting.BLUE)
					.setUnderlined(true)
					.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/noppes world tp @p " + dimID + " " + x + " " + y + " "+z))
					.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("script.hover.error.pos.tp")));
			message = message.appendSibling(posClick);
		}
		ITextComponent side = new TextComponentString("; Side: " + (isClient() ? "Client" : "Server"));
		side.getStyle().setColor(TextFormatting.DARK_GRAY);
		return message.appendSibling(side);
	}

	private static IPlayer<?> getIPlayer(Object event) {
		if (event instanceof ItemEvent.AttackEvent) { return ((ItemEvent.AttackEvent) event).player; }
		if (event instanceof ItemEvent.InteractEvent) { return ((ItemEvent.InteractEvent) event).player; }
		if (event instanceof ItemEvent.PickedUpEvent) { return ((ItemEvent.PickedUpEvent) event).player; }
		if (event instanceof ItemEvent.TossedEvent) { return ((ItemEvent.TossedEvent) event).player; }
		if (event instanceof ItemEvent.UpdateEvent) { return ((ItemEvent.UpdateEvent) event).player; }
		return null;
	}

	@Override
	public void runScript(String type, Event event) {
		if (!this.loaded) {
			this.loadScriptData();
			this.loaded = true;
		}
		if (!this.isEnabled()) {
			return;
		}
		if (ScriptController.Instance.lastLoaded > this.lastInited) {
			this.lastInited = ScriptController.Instance.lastLoaded;
			if (!type.equalsIgnoreCase(EnumScriptType.INIT.function)) {
				EventHooks.onScriptItemInit(this);
			}
		}
		for (ScriptContainer script : this.scripts) {
			script.run(type, event, !this.isClient());
		}
	}

	public void saveScriptData() {
		NBTTagCompound c = this.item.getTagCompound();
		if (c == null) {
			this.item.setTagCompound(c = new NBTTagCompound());
		}
		c.setTag("ScriptedData", this.getScriptNBT(new NBTTagCompound()));
	}

	@Override
	public void setColor(int color) {
		if (color != this.itemColor) {
			this.updateClient = true;
		}
		this.itemColor = color;
	}

	@Override
	public void setDurabilityColor(int color) {
		if (color != this.durabilityColor) {
			this.updateClient = true;
		}
		this.durabilityColor = color;
	}

	@Override
	public void setDurabilityShow(boolean bo) {
		if (bo != this.durabilityShow) {
			this.updateClient = true;
		}
		this.durabilityShow = bo;
	}

	@Override
	public void setDurabilityValue(float value) {
		if (value != this.durabilityValue) {
			this.updateClient = true;
		}
		this.durabilityValue = value;
	}

	@Override
	public void setEnabled(boolean bo) {
		this.enabled = bo;
	}

	@Override
	public void setLanguage(String lang) {
		this.scriptLanguage = lang;
	}

	@Override
	public void setLastInited(long timeMC) {
		this.lastInited = timeMC;
	}

	@Override
	public void setMaxStackSize(int size) {
		if (size < 1 || size > 64) {
			throw new CustomNPCsException("Stacksize has to be between 1 and 64");
		}
		this.stackSize = size;
	}

	@Override
	public void setMCNbt(NBTTagCompound compound) {
		super.setMCNbt(compound);
		this.setScriptNBT(compound);
		this.durabilityShow = compound.getBoolean("DurabilityShow");
		this.durabilityValue = compound.getDouble("DurabilityValue");
		if (compound.hasKey("DurabilityColor")) {
			this.durabilityColor = compound.getInteger("DurabilityColor");
		}
		this.itemColor = compound.getInteger("ItemColor");
		this.stackSize = compound.getInteger("MaxStackSize");
	}

	public void setScriptNBT(NBTTagCompound compound) {
		if (!compound.hasKey("Scripts")) {
			return;
		}
		this.scripts = NBTTags.GetScript(compound.getTagList("Scripts", 10), this, false);
		this.scriptLanguage = compound.getString("ScriptLanguage");
		this.enabled = compound.getBoolean("ScriptEnabled");
	}

	@Override
	public void setTexture(int damage, String texture) {
		if (damage == 0) {
			throw new CustomNPCsException("Can't set texture for 0");
		}
		String old = ItemScripted.Resources.get(damage);
		if (Objects.equals(old, texture)) {
			return;
		}
		ItemScripted.Resources.put(damage, texture);
		SyncController.syncScriptItemsEverybody();
	}
}
