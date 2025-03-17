package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.NBTTags;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.event.*;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;

public class BaseScriptData
implements IScriptHandler {

	private final boolean isClient = Thread.currentThread().getName().toLowerCase().contains("client");
	protected boolean enabled = false;
	public boolean hadInteract = true;
	public long lastInited = -1L;
	protected String scriptLanguage = "ECMAScript";
	protected List<ScriptContainer> scripts = new ArrayList<>();

	public void clear() {
		this.scripts = new ArrayList<>();
	}

	@Override
	public void clearConsole() {
		for (ScriptContainer script : this.getScripts()) {
			script.console.clear();
		}
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
	public String getLanguage() {
		return this.scriptLanguage;
	}

	@Override
	public List<ScriptContainer> getScripts() {
		return this.scripts;
	}

	@Override
	public boolean isClient() { return this.isClient; }

	@Override
	public boolean getEnabled() { return this.enabled; }

	public boolean isEnabled() {
		return this.enabled && ScriptController.HasStart && !this.scripts.isEmpty();
	}

	@Override
	public String noticeString(String type, Object event) {
		String notice = "";
		if (type != null) { notice += " hook \""+type+"\""; }
		IEntity<?> iEntity = null;
		IBlock iBlock = null;
		if (event instanceof Event && !(event instanceof CustomNPCsEvent)) {
			event = new ForgeEvent((Event) event);
		}
		if (event instanceof ForgeEvent) {
			((ForgeEvent) event).createData();
			if (((ForgeEvent) event).player != null) { iEntity = ((ForgeEvent) event).player; }
			else if (((ForgeEvent) event).npc != null) { iEntity = ((ForgeEvent) event).npc; }
			else if (((ForgeEvent) event).entity != null) { iEntity = ((ForgeEvent) event).entity; }
			else if (((ForgeEvent) event).block != null) { iBlock = ((ForgeEvent) event).block; }
		}
		if (event instanceof PlayerEvent) {
			notice = ((PlayerEvent) event).player == null ? ". Global players script" : ". Player:";
			if (type != null) { notice += " hook \""+type+"\""; }
			if (((PlayerEvent) event).player == null) { return notice + "; Side: " + (isClient() ? "Client" : "Server"); }
			iEntity =  ((PlayerEvent) event).player;
		}
		else if (event instanceof NpcEvent && ((NpcEvent) event).npc != null) { iEntity = ((NpcEvent) event).npc; }
		if (iEntity != null) {
			if (iEntity.getMCEntity() instanceof EntityPlayer) { notice += ". Player "; }
			else if (iEntity.getMCEntity() instanceof EntityNPCInterface) { notice += ". NPC "; }
			else { notice += ". Entity "; }
			return notice + "\"" + iEntity.getName() + "\"; UUID: \"" + iEntity.getUUID() + "\"" +
					" in dimension ID:" + (iEntity.getWorld().getMCWorld() == null ? 0 : iEntity.getWorld().getMCWorld().provider.getDimension()) +
					"; X:" + (Math.round(iEntity.getPos().getX() * 100.0d) / 100.0d) +
					"; Y:" + (Math.round(iEntity.getPos().getY() * 100.0d) / 100.0d) +
					"; Z:" + (Math.round(iEntity.getPos().getZ() * 100.0d) / 100.0d) +
					"; Side: " + (isClient() ? "Client" : "Server");
		}
		if (event instanceof BlockEvent && ((BlockEvent) event).block != null) { iBlock = ((BlockEvent) event).block; }
		if (iBlock != null) {
			notice += ". Block in dimension ID:" + (iBlock.getWorld().getMCWorld() == null ? 0 : iBlock.getWorld().getMCWorld().provider.getDimension()) +
					"; X:" + ((int) iBlock.getPos().getX()) +
					"; Y:" + ((int) iBlock.getPos().getY()) +
					"; Z:" + ((int) iBlock.getPos().getZ());
		}
		return notice;
	}

	@Override
	public void runScript(String type, Event event) { }

	public void readFromNBT(NBTTagCompound compound) {
		this.scripts.clear();
		this.scripts = NBTTags.GetScript(compound.getTagList("Scripts", 10), this, false);
		this.scriptLanguage = Util.instance.deleteColor(compound.getString("ScriptLanguage"));
		this.enabled = compound.getBoolean("ScriptEnabled");
	}

	@Override
	public void setEnabled(boolean bo) {
		this.enabled = bo;
	}

	@Override
	public void setLanguage(String lang) {
		this.scriptLanguage = Util.instance.deleteColor(lang);
	}

	@Override
	public void setLastInited(long timeMC) {
		this.lastInited = timeMC;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("Scripts", NBTTags.NBTScript(this.scripts));
		compound.setString("ScriptLanguage", this.scriptLanguage);
		compound.setBoolean("ScriptEnabled", this.enabled);
		return compound;
	}

	public boolean isEmpty() {
		for (ScriptContainer cont : this.scripts) {
			if (cont.hasScriptCode()) { return false; }
		}
		return true;
	}

}
