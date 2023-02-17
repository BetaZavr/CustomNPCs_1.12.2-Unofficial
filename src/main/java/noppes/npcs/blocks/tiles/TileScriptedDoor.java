package noppes.npcs.blocks.tiles;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.google.common.base.MoreObjects;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.EventHooks;
import noppes.npcs.NBTTags;
import noppes.npcs.api.block.IBlock;
import noppes.npcs.api.wrapper.BlockScriptedDoorWrapper;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.IScriptBlockHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.data.DataTimers;

public class TileScriptedDoor extends TileDoor implements ITickable, IScriptBlockHandler {
	private IBlock blockDummy;
	public float blockHardness;
	public float blockResistance;
	public boolean enabled;
	public long lastInited;
	public int newPower;
	public int prevPower;
	public String scriptLanguage;
	public List<ScriptContainer> scripts;
	public boolean shouldRefreshData;
	private short ticksExisted;
	public DataTimers timers;

	public TileScriptedDoor() {
		this.scripts = new ArrayList<ScriptContainer>();
		this.shouldRefreshData = false;
		this.scriptLanguage = "ECMAScript";
		this.enabled = false;
		this.blockDummy = null;
		this.timers = new DataTimers(this);
		this.lastInited = -1L;
		this.ticksExisted = 0;
		this.newPower = 0;
		this.prevPower = 0;
		this.blockHardness = 5.0f;
		this.blockResistance = 10.0f;
	}

	public void clearConsole() {
		for (ScriptContainer script : this.getScripts()) {
			script.console.clear();
		}
	}

	public IBlock getBlock() {
		if (this.blockDummy == null) {
			this.blockDummy = new BlockScriptedDoorWrapper(this.getWorld(), this.getBlockType(), this.getPos());
		}
		return this.blockDummy;
	}

	public Map<Long, String> getConsoleText() {
		Map<Long, String> map = new TreeMap<Long, String>();
		int tab = 0;
		for (ScriptContainer script : this.getScripts()) {
			++tab;
			for (Map.Entry<Long, String> entry : script.console.entrySet()) {
				map.put(entry.getKey(), " tab " + tab + ":\n" + entry.getValue());
			}
		}
		return map;
	}

	public boolean getEnabled() {
		return this.enabled;
	}

	public String getLanguage() {
		return this.scriptLanguage;
	}

	public NBTTagCompound getNBT(NBTTagCompound compound) {
		compound.setTag("Scripts", NBTTags.NBTScript(this.scripts));
		compound.setString("ScriptLanguage", this.scriptLanguage);
		compound.setBoolean("ScriptEnabled", this.enabled);
		compound.setInteger("BlockPrevPower", this.prevPower);
		compound.setFloat("BlockHardness", this.blockHardness);
		compound.setFloat("BlockResistance", this.blockResistance);
		return compound;
	}

	public List<ScriptContainer> getScripts() {
		return this.scripts;
	}

	public boolean isClient() {
		return this.getWorld().isRemote;
	}

	private boolean isEnabled() {
		return this.enabled && ScriptController.HasStart && !this.world.isRemote;
	}

	public String noticeString() {
		BlockPos pos = this.getPos();
		return MoreObjects.toStringHelper(this).add("x", pos.getX()).add("y", pos.getY()).add("z", pos.getZ())
				.toString();
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.setNBT(compound);
		this.timers.readFromNBT(compound);
	}

	public void runScript(EnumScriptType type, Event event) {
		if (!this.isEnabled()) {
			return;
		}
		if (ScriptController.Instance.lastLoaded > this.lastInited) {
			this.lastInited = ScriptController.Instance.lastLoaded;
			if (type != EnumScriptType.INIT) {
				EventHooks.onScriptBlockInit(this);
			}
		}
		for (ScriptContainer script : this.scripts) {
			script.run(type, event);
		}
	}

	public void setEnabled(boolean bo) {
		this.enabled = bo;
	}

	public void setLanguage(String lang) {
		this.scriptLanguage = lang;
	}

	public void setNBT(NBTTagCompound compound) {
		this.scripts = NBTTags.GetScript(compound.getTagList("Scripts", 10), this);
		this.scriptLanguage = compound.getString("ScriptLanguage");
		this.enabled = compound.getBoolean("ScriptEnabled");
		this.prevPower = compound.getInteger("BlockPrevPower");
		if (compound.hasKey("BlockHardness")) {
			this.blockHardness = compound.getFloat("BlockHardness");
			this.blockResistance = compound.getFloat("BlockResistance");
		}
	}

	@Override
	public void update() {
		super.update();
		++this.ticksExisted;
		if (this.prevPower != this.newPower) {
			EventHooks.onScriptBlockRedstonePower(this, this.prevPower, this.newPower);
			this.prevPower = this.newPower;
		}
		this.timers.update();
		if (this.ticksExisted >= 10) {
			EventHooks.onScriptBlockUpdate(this);
			this.ticksExisted = 0;
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		this.getNBT(compound);
		this.timers.writeToNBT(compound);
		return super.writeToNBT(compound);
	}
}
