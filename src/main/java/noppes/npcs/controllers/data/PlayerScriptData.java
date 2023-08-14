package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.base.MoreObjects;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.EventHooks;
import noppes.npcs.NBTTags;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;

public class PlayerScriptData implements IScriptHandler {
	
	private static Map<Long, String> console = new TreeMap<Long, String>();
	private static List<Integer> errored = new ArrayList<Integer>();
	private boolean enabled;
	public boolean hadInteract;
	public long lastInited;
	private long lastPlayerUpdate;
	private EntityPlayer player;
	private IPlayer<?> playerAPI;
	private String scriptLanguage;
	private List<ScriptContainer> scripts;

	public PlayerScriptData(EntityPlayer player) {
		this.scripts = new ArrayList<ScriptContainer>();
		this.scriptLanguage = "ECMAScript";
		this.lastPlayerUpdate = 0L;
		this.lastInited = -1L;
		this.hadInteract = true;
		this.enabled = false;
		this.player = player;
	}

	public void clear() {
		PlayerScriptData.console = new TreeMap<Long, String>();
		PlayerScriptData.errored = new ArrayList<Integer>();
		this.scripts = new ArrayList<ScriptContainer>();
	}

	@Override
	public void clearConsole() {
		PlayerScriptData.console.clear();
	}

	@Override
	public Map<Long, String> getConsoleText() {
		return PlayerScriptData.console;
	}

	@Override
	public boolean getEnabled() {
		return ScriptController.Instance.playerScripts.enabled;
	}

	@Override
	public String getLanguage() {
		return ScriptController.Instance.playerScripts.scriptLanguage;
	}

	public IPlayer<?> getPlayer() {
		if (this.playerAPI == null) {
			this.playerAPI = (IPlayer<?>) NpcAPI.Instance().getIEntity(this.player);
		}
		return this.playerAPI;
	}

	@Override
	public List<ScriptContainer> getScripts() {
		return this.scripts;
	}

	@Override
	public boolean isClient() {
		return Thread.currentThread().getName().toLowerCase().indexOf("client") != -1;
		//return !this.player.isServerWorld();
	}

	public boolean isEnabled() {
		return ScriptController.Instance.playerScripts.enabled && ScriptController.HasStart
				&& (this.player == null || !this.player.world.isRemote);
	}

	@Override
	public String noticeString() {
		if (this.player == null) {
			return "Global script";
		}
		BlockPos pos = this.player.getPosition();
		return MoreObjects.toStringHelper(this.player).add("name", this.player.getName()).add("dimID", this.player.world.provider.getDimension()).add("x", pos.getX()).add("y", pos.getY()).add("z", pos.getZ())
				.toString();
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.scripts = NBTTags.GetScript(compound.getTagList("Scripts", 10), this, false);
		this.scriptLanguage = compound.getString("ScriptLanguage");
		this.enabled = compound.getBoolean("ScriptEnabled");
		PlayerScriptData.console = NBTTags.GetLongStringMap(compound.getTagList("ScriptConsole", 10));
	}

	@Override
	public void runScript(EnumScriptType type, Event event) {
		if (!this.isEnabled()) { return; }
		if (ScriptController.Instance.lastLoaded > this.lastInited || ScriptController.Instance.lastPlayerUpdate > this.lastPlayerUpdate) {
			this.lastInited = ScriptController.Instance.lastLoaded;
			PlayerScriptData.errored.clear();
			if (this.player != null) {
				this.scripts.clear();
				for (ScriptContainer script : ScriptController.Instance.playerScripts.scripts) {
					ScriptContainer s = new ScriptContainer(this, false);
					s.readFromNBT(script.writeToNBT(new NBTTagCompound()));
					this.scripts.add(s);
				}
			}
			this.lastPlayerUpdate = ScriptController.Instance.lastPlayerUpdate;
			if (type != EnumScriptType.INIT) {
				EventHooks.onPlayerInit(this);
			}
		}
		for (int i = 0; i < this.scripts.size(); ++i) {
			ScriptContainer script = this.scripts.get(i);
			if (!PlayerScriptData.errored.contains(i)) {
				script.run(type, event, !this.isClient());
				if (script.errored) {
					PlayerScriptData.errored.add(i);
				}
				for (Map.Entry<Long, String> entry : script.console.entrySet()) {
					if (!PlayerScriptData.console.containsKey(entry.getKey())) {
						PlayerScriptData.console.put(entry.getKey(), " tab " + (i + 1) + ":\n" + entry.getValue());
					}
				}
				script.console.clear();
			}
		}
	}

	@Override
	public void setEnabled(boolean bo) {
		this.enabled = bo;
	}

	@Override
	public void setLanguage(String lang) {
		this.scriptLanguage = lang;
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("Scripts", NBTTags.NBTScript(this.scripts));
		compound.setString("ScriptLanguage", this.scriptLanguage);
		compound.setBoolean("ScriptEnabled", this.enabled);
		compound.setTag("ScriptConsole", NBTTags.NBTLongStringMap(PlayerScriptData.console));
		return compound;
	}

}
