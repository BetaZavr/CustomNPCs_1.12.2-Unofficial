package noppes.npcs.entity.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.EntityNPCInterface;

public class DataScript implements IScriptHandler {

	private boolean enabled = false;
	public long lastInited = -1L;
	public EntityNPCInterface npc;
	private String scriptLanguage = "ECMAScript";
	private List<ScriptContainer> scripts = new ArrayList<>();

	public DataScript(EntityNPCInterface npc) {
		this.npc = npc;
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
	public boolean getEnabled() {
		return this.enabled;
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
	public boolean isClient() {
		return Thread.currentThread().getName().toLowerCase().contains("client");
	}

	public boolean isEnabled() {
		return this.enabled && ScriptController.HasStart && !this.scripts.isEmpty();
	}

	@Override
	public ITextComponent noticeString(String type, Object event) {
		ITextComponent message = new TextComponentString("");
		message.getStyle().setColor(TextFormatting.DARK_GRAY);
		if (type != null) {
			ITextComponent hook = new TextComponentString("Hook \"");
			hook.getStyle().setColor(TextFormatting.DARK_GRAY);
			ITextComponent hookType = new TextComponentString(type);
			hookType.getStyle().setColor(TextFormatting.GRAY);
			ITextComponent hookEnd = new TextComponentString("\"; ");
			hookEnd.getStyle().setColor(TextFormatting.DARK_GRAY);
			message = message.appendSibling(hook).appendSibling(hookType).appendSibling(hookEnd);
		}
		ITextComponent mesNpc = new TextComponentString("NPC \"");
		mesNpc.getStyle().setColor(TextFormatting.DARK_GRAY);
		ITextComponent name = new TextComponentString(npc.getName());
		name.getStyle().setColor(TextFormatting.GRAY);
		ITextComponent mesUUID = new TextComponentString("\"; UUID: \"");
		mesUUID.getStyle().setColor(TextFormatting.DARK_GRAY);
		ITextComponent uuid = new TextComponentString(npc.getUniqueID().toString());
		uuid.getStyle().setColor(TextFormatting.GRAY);
		ITextComponent mesEnd = new TextComponentString("\" in ");
		mesEnd.getStyle().setColor(TextFormatting.DARK_GRAY);
		message = message.appendSibling(mesNpc).appendSibling(name).appendSibling(mesUUID).appendSibling(uuid).appendSibling(mesEnd);

		int dimID = npc.world == null ? 0 : npc.world.provider.getDimension();
		double x = Math.round(npc.posX * 100.0d) / 100.0d;
		double y = Math.round(npc.posY * 100.0d) / 100.0d;
		double z = Math.round(npc.posZ * 100.0d) / 100.0d;
		ITextComponent posClick = new TextComponentString("dimension ID:" + dimID + "; X:" + x + "; Y:" + y + "; Z:" + z);
		posClick.getStyle().setColor(TextFormatting.BLUE)
				.setUnderlined(true)
				.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/noppes world tp @p " + dimID + " " + x + " " + y + " "+z))
				.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("script.hover.error.pos.tp")));
		ITextComponent side = new TextComponentString("; Side: " + (isClient() ? "Client" : "Server"));
		side.getStyle().setColor(TextFormatting.DARK_GRAY);
		return message.appendSibling(posClick).appendSibling(side);
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.scripts = NBTTags.GetScript(compound.getTagList("Scripts", 10), this, false);
		this.scriptLanguage = compound.getString("ScriptLanguage");
		this.enabled = compound.getBoolean("ScriptEnabled");
	}

	@Override
	public void runScript(String type, Event event) {
		if (!this.isEnabled()) {
			return;
		}
		if (ScriptController.Instance.lastLoaded > lastInited) {
			lastInited = ScriptController.Instance.lastLoaded;
			if (!type.equalsIgnoreCase(EnumScriptType.INIT.function)) {
				EventHooks.onNPCInit(this.npc);
			}
		}
		for (ScriptContainer script : this.scripts) { script.run(type, event); }
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

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("Scripts", NBTTags.NBTScript(this.scripts));
		compound.setString("ScriptLanguage", this.scriptLanguage);
		compound.setBoolean("ScriptEnabled", this.enabled);
		return compound;
	}

}
