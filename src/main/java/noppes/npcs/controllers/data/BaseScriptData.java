package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.CustomNpcs;
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
	public String getLanguage() { return scriptLanguage; }

	@Override
	public List<ScriptContainer> getScripts() {
		return this.scripts;
	}

	@Override
	public boolean isClient() { return this.isClient; }

	@Override
	public boolean getEnabled() { return this.enabled; }

	public boolean isEnabled() {
		return CustomNpcs.EnableScripting && enabled && ScriptController.HasStart && !scripts.isEmpty();
	}

	@Override
	public ITextComponent noticeString(String type, Object event) {
		ITextComponent message = new TextComponentString("");
		message.getStyle().setColor(TextFormatting.DARK_GRAY);
		String pos = "";
		int dimID = 0;
		double x = 0.0d, y = 0.0d, z = 0.0d, tpY = 0.0d;
		if (type != null) {
			ITextComponent hook = new TextComponentString("Hook \"");
			hook.getStyle().setColor(TextFormatting.DARK_GRAY);
			ITextComponent hookType = new TextComponentString(type);
			hookType.getStyle().setColor(TextFormatting.GRAY);
			ITextComponent hookEnd = new TextComponentString("\"");
			hookEnd.getStyle().setColor(TextFormatting.DARK_GRAY);
			message = message.appendSibling(hook).appendSibling(hookType).appendSibling(hookEnd);
		}
		IEntity<?> iEntity = null;
		IBlock iBlock = null;
		if (event instanceof Event && !(event instanceof CustomNPCsEvent)) { event = new ForgeEvent((Event) event); }
		if (event instanceof ForgeEvent) {
			((ForgeEvent) event).createData();
			if (((ForgeEvent) event).player != null) { iEntity = ((ForgeEvent) event).player; }
			else if (((ForgeEvent) event).npc != null) { iEntity = ((ForgeEvent) event).npc; }
			else if (((ForgeEvent) event).entity != null) { iEntity = ((ForgeEvent) event).entity; }
			else if (((ForgeEvent) event).block != null) { iBlock = ((ForgeEvent) event).block; }
		}
		if (event instanceof PlayerEvent) {
			if (((PlayerEvent) event).player != null) { iEntity =  ((PlayerEvent) event).player; }
			else {
				ITextComponent mesPlayer = new TextComponentString("; Global players script");
				mesPlayer.getStyle().setColor(TextFormatting.DARK_GRAY);
				message = message.appendSibling(mesPlayer);
			}
			if (((PlayerEvent) event).player != null) { iEntity =  ((PlayerEvent) event).player; }
		}
		else if (event instanceof NpcEvent && ((NpcEvent) event).npc != null) { iEntity = ((NpcEvent) event).npc; }
		if (iEntity != null) {
			ITextComponent mesEntity;
			if (iEntity.getMCEntity() instanceof EntityPlayer) { mesEntity = new TextComponentString("; Player \""); }
			else if (iEntity.getMCEntity() instanceof EntityNPCInterface) { mesEntity = new TextComponentString("; NPC \""); }
			else { mesEntity = new TextComponentString("; Entity \""); }
			mesEntity.getStyle().setColor(TextFormatting.DARK_GRAY);

			ITextComponent name = new TextComponentString(iEntity.getName());
			name.getStyle().setColor(TextFormatting.GRAY);
			ITextComponent mesUUID = new TextComponentString("\"; UUID: \"");
			mesUUID.getStyle().setColor(TextFormatting.DARK_GRAY);
			ITextComponent uuid = new TextComponentString(iEntity.getUUID());
			uuid.getStyle().setColor(TextFormatting.GRAY);
			ITextComponent mesIn = new TextComponentString("\" in ");
			mesIn.getStyle().setColor(TextFormatting.DARK_GRAY);
			message = message.appendSibling(mesEntity).appendSibling(name).appendSibling(mesUUID).appendSibling(uuid).appendSibling(mesIn);

			x = Math.round(iEntity.getPos().getX() * 100.0d) / 100.0d;
			y = Math.round(iEntity.getPos().getY() * 100.0d) / 100.0d;
			z = Math.round(iEntity.getPos().getZ() * 100.0d) / 100.0d;
			dimID = iEntity.getWorld().getMCWorld() == null ? 0 : iEntity.getWorld().getMCWorld().provider.getDimension();
			pos = "dimension ID:" + dimID + "; X:" + x + "; Y:" + y + "; Z:" + z;
		}
		if (event instanceof BlockEvent && ((BlockEvent) event).block != null) { iBlock = ((BlockEvent) event).block; }
		if (iBlock != null) {
			ITextComponent mesBlock = new TextComponentString("; Block in ");
			mesBlock.getStyle().setColor(TextFormatting.DARK_GRAY);
			message = message.appendSibling(mesBlock);
			x = Math.floor(iBlock.getPos().getX());
			y = Math.floor(iBlock.getPos().getY());
			z = Math.floor(iBlock.getPos().getZ());
			dimID = iBlock.getWorld().getMCWorld() == null ? 0 : iBlock.getWorld().getMCWorld().provider.getDimension();
			pos = "dimension ID:" + dimID + "; X:" + x + "; Y:" + y + "; Z:" + z;
			tpY = 1.0d;
		}
		if (!pos.isEmpty()) {
			ITextComponent posClick = new TextComponentString(pos);
			posClick.getStyle().setColor(TextFormatting.BLUE)
					.setUnderlined(true)
					.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/noppes world tp @p " + dimID + " " + x + " " + (y + tpY) + " "+z))
					.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("script.hover.error.pos.tp")));
			message = message.appendSibling(posClick);
		}
		ITextComponent side = new TextComponentString("; Side: " + (isClient() ? "Client" : "Server"));
		side.getStyle().setColor(TextFormatting.DARK_GRAY);
		return message.appendSibling(side);
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
	public void setLanguage(String language) {
		language = Util.instance.deleteColor(language);
		if (ScriptController.Instance.languages.containsKey(language)) {
			scriptLanguage = Util.instance.deleteColor(language);
		}
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
