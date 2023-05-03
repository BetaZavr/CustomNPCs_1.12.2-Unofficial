package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagDouble;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.DimensionManager;
import noppes.npcs.api.IPos;
import noppes.npcs.api.gui.ICompassData;
import noppes.npcs.api.wrapper.BlockPosWrapper;
import noppes.npcs.constants.EnumQuestTask;

public class PlayerCompassHUDData
implements ICompassData {
	
	public String npc, name, title;
	public BlockPos pos;
	public boolean show;
	private int dimensionId, range, type;
	public final double[] screenPos = new double[] { 0.15d, 0.765d };
	public float scale = 1.0f, incline = 0.0f, rot = 0.0f;
	public boolean showQuestName, showTaskProgress;
	
	public PlayerCompassHUDData(int type, String name, String title, BlockPos pos, int dimensionId, int range) {
		this();
		if (name!=null) { this.name = name; }
		if (title!=null) { this.title = title; }
		if (pos!=null) { this.pos = pos; }
		this.setDimensionID(dimensionId);
		this.setRange(range);
		this.setType(type);
	}
	
	public PlayerCompassHUDData() {
		this.name = "";
		this.title = "";
		this.npc = "";
		this.pos = new BlockPos(0, 0, 0);
		this.dimensionId = 0;
		this.range = 5;
		this.type = 0;
		this.show = false;
		this.screenPos[0] = 0.15d;
		this.screenPos[1] = 0.765d;
		this.scale = 1.0f;
		this.rot = 0.0f;
		this.incline = 0.0f;
		this.showQuestName = true;
		this.showTaskProgress = true;
	}

	public NBTTagCompound getNbt() {
		NBTTagCompound nbtCompass = new NBTTagCompound();
		nbtCompass.setString("Name", this.name);
		nbtCompass.setString("Title", this.title);
		nbtCompass.setString("NPCName", this.npc);
		nbtCompass.setIntArray("BlockPos", new int[] { this.pos.getX(), this.pos.getY(), this.pos.getZ() });
		nbtCompass.setInteger("DimensionID", this.dimensionId);
		nbtCompass.setInteger("Range", this.range);
		nbtCompass.setInteger("Type", this.type);
		nbtCompass.setBoolean("IsShow", this.show);
		nbtCompass.setFloat("Scale", this.scale);
		nbtCompass.setFloat("Rotation", this.rot);
		nbtCompass.setFloat("Incline", this.incline);
		nbtCompass.setByteArray("Showed", new byte[] { (byte) (this.showQuestName ? 1 : 0), (byte) (this.showTaskProgress ? 1 : 0) }); 
		
		NBTTagList scP = new NBTTagList();
		scP.appendTag(new NBTTagDouble(this.screenPos[0]));
		scP.appendTag(new NBTTagDouble(this.screenPos[1]));
		nbtCompass.setTag("ScreenPos", scP);
		
		return nbtCompass;
	}

	public void load(NBTTagCompound nbtCompass) {
		this.name = nbtCompass.getString("Name");
		this.title = nbtCompass.getString("Title");
		this.npc = nbtCompass.getString("NPCName");
		int[] p = nbtCompass.getIntArray("BlockPos");
		if (p!=null && p.length>=3) { this.pos = new BlockPos(p[0], p[1], p[2]); }
		this.setDimensionID(nbtCompass.getInteger("DimensionID"));
		this.setRange(nbtCompass.getInteger("Range"));
		this.setType(nbtCompass.getInteger("Type"));
		this.show = nbtCompass.getBoolean("IsShow");
		this.scale = nbtCompass.getFloat("Scale");
		this.rot = nbtCompass.getFloat("Rotation");
		this.incline = nbtCompass.getFloat("Incline");
		this.screenPos[0] = nbtCompass.getTagList("ScreenPos", 6).getDoubleAt(0);
		this.screenPos[1] = nbtCompass.getTagList("ScreenPos", 6).getDoubleAt(1);
		if (nbtCompass.hasKey("Showed", 7)) {
			this.showQuestName = nbtCompass.getByteArray("Showed")[0] == (byte) 1;
			this.showTaskProgress = nbtCompass.getByteArray("Showed")[1] == (byte) 1;
		}
	}

	@Override
	public int getType() { return this.type; }
	
	@Override
	public void setType(int type) {
		if (type<0) { type *= -1; }
		if (type > EnumQuestTask.values().length-1) { type %= EnumQuestTask.values().length-1; }
		this.type = type;
	}

	@Override
	public int getRange() { return this.range; }
	
	@Override
	public void setRange(int range) {
		if (range<0) { range *= -1; }
		if (range>64) { range = 64; }
		this.range = range;
	}

	@Override
	public int getDimensionID() { return this.dimensionId; }

	@Override
	public void setDimensionID(int dimID) {
		if (DimensionManager.isDimensionRegistered(dimID)) { dimID = 0; }
		this.dimensionId = dimID;
	}

	@Override
	public IPos getPos() { return new BlockPosWrapper(this.pos); }

	@Override
	public void setPos(IPos pos) { if (pos!=null) { this.pos = pos.getMCBlockPos(); } }

	@Override
	public void setPos(int x, int y, int z) {
		this.pos = new BlockPos(x, y, z);
	}

	@Override
	public String getName() { return this.name; }

	@Override
	public void setName(String name) { if (name!=null) { this.name = name; } }

	@Override
	public String getNPCName() { return this.npc; }

	@Override
	public void setNPCName(String npcName) { if (npcName!=null) { this.name = npcName; } }
	
	@Override
	public String getTitle() { return this.title; }

	@Override
	public void setTitle(String title) { if (title!=null) { this.title = title; } }

	@Override
	public boolean isShow() { return this.show; }
	
	@Override
	public void setShow(boolean show) { this.show = show; }
	
}
