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
import noppes.npcs.controllers.IScriptBlockHandler;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.data.DataTimers;

import javax.annotation.Nonnull;

public class TileScriptedDoor extends TileDoor implements ITickable, IScriptBlockHandler {

	private IBlock blockDummy;
	public float blockHardness;
	public float blockResistance;
	public boolean enabled;
	public long lastInited;
	public int newPower;
	public int prevPower;
	public String scriptLanguage, closeSound, openSound;
	public List<ScriptContainer> scripts;
	public boolean shouldRefreshData;
	private short ticksExisted;
	public DataTimers timers;

	public TileScriptedDoor() {
		this.scripts = new ArrayList<>();
		this.shouldRefreshData = false;
		this.scriptLanguage = "ECMAScript";
		this.closeSound = "";
		this.openSound = "";
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
		Map<Long, String> map = new TreeMap<>();
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
		compound.setString("CloseSound", this.closeSound);
		compound.setString("OpenSound", this.openSound);
		compound.setBoolean("ScriptEnabled", this.enabled);
		compound.setInteger("BlockPrevPower", this.prevPower);
		compound.setFloat("BlockHardness", this.blockHardness);
		compound.setFloat("BlockResistance", this.blockResistance);
		return compound;
	}

	public List<ScriptContainer> getScripts() {
		return this.scripts;
	}

	public String getSound(boolean isOpen) {
		if (isOpen) {
			return this.openSound;
		}
		return this.closeSound;
	}

	public boolean isClient() {
		return this.getWorld().isRemote;
	}

	private boolean isEnabled() {
		return this.enabled && ScriptController.HasStart && !this.world.isRemote;
	}

	public String noticeString() {
		BlockPos pos = this.getPos();
		return MoreObjects.toStringHelper(this).add("dimID", this.world.provider.getDimension()).add("x", pos.getX())
				.add("y", pos.getY()).add("z", pos.getZ()).toString();
	}

	@Override
	public void readFromNBT(@Nonnull NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.setNBT(compound);
		this.timers.readFromNBT(compound);
	}

	@Override
	public void runScript(String type, Event event) {
		if (!this.isEnabled()) {
			return;
		}
		if (ScriptController.Instance.lastLoaded > this.lastInited) {
			this.lastInited = ScriptController.Instance.lastLoaded;
			if (!type.equalsIgnoreCase("init")) {
				for (ScriptContainer tab : this.scripts) {
					tab.getFullCode();
				}
				EventHooks.onScriptBlockInit(this);
			}
		}
		for (ScriptContainer script : this.scripts) {
			script.run(type, event, !this.isClient());
		}
	}

	public void setEnabled(boolean bo) {
		this.enabled = bo;
	}

	public void setLanguage(String lang) {
		this.scriptLanguage = lang;
	}

	public void setNBT(NBTTagCompound compound) {
		this.scripts = NBTTags.GetScript(compound.getTagList("Scripts", 10), this, false);
		this.scriptLanguage = compound.getString("ScriptLanguage");
		this.closeSound = compound.getString("CloseSound");
		this.openSound = compound.getString("OpenSound");
		this.enabled = compound.getBoolean("ScriptEnabled");
		this.prevPower = compound.getInteger("BlockPrevPower");
		if (compound.hasKey("BlockHardness")) {
			this.blockHardness = compound.getFloat("BlockHardness");
			this.blockResistance = compound.getFloat("BlockResistance");
		}
	}

	public void setSound(boolean isOpen, String song) {
		if (song == null) {
			song = "";
		}
		if (isOpen) {
			this.openSound = song;
		} else {
			this.closeSound = song;
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

	@Nonnull
	@Override
	public NBTTagCompound writeToNBT(@Nonnull NBTTagCompound compound) {
		this.getNBT(compound);
		this.timers.writeToNBT(compound);
		return super.writeToNBT(compound);
	}

}
