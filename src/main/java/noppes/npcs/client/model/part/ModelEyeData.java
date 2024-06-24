package noppes.npcs.client.model.part;

import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.ModelPartData;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.entity.EntityNPCInterface;

public class ModelEyeData
extends ModelPartData {
	
	public final int[] eyeColor = new int [2];
	public final int[] pupilColor = new int [2];
	public final int[] browColor = new int [2];
	
	public long blinkStart = 0L;
	public int browThickness = 4;
	
	public int eyePos = 1;
	public int skinColor = 0xB4846D;
	public int closed = 0; // 0-no, 1-bolt, 2-left, 3-right
	public boolean glint = true;
	
	public ResourceLocation eyeLeft = new ResourceLocation("moreplayermodels:textures/eyes/eye_0.png");
	public ResourceLocation eyeRight = new ResourceLocation("moreplayermodels:textures/eyes/eye_0.png");
	public ResourceLocation pupilLeft = new ResourceLocation("moreplayermodels:textures/eyes/pupil_0.png");
	public ResourceLocation pupilRight = new ResourceLocation("moreplayermodels:textures/eyes/pupil_0.png");
	public ResourceLocation browLeft = new ResourceLocation("moreplayermodels:textures/eyes/brow_0.png");
	public ResourceLocation browRight = new ResourceLocation("moreplayermodels:textures/eyes/brow_0.png");
	public ResourceLocation glintRes = new ResourceLocation("moreplayermodels:textures/eyes/glint.png");

	private Random rnd = new Random();
	public boolean activeLeft = true;
	public boolean activeRight = true;
	public int ticks = -1;

	public ModelEyeData() {
		super("eyes");
		this.reset();
	}
	
	public void reset() {
		int[] arr = new int[] { 0xF6F6F6, 0xF0F0F0, 0xF02020, 0xF0F0F0, 0x20F020, 0xF0F0F0, 0x2020F0, 0xF0F0F0, 0x4040FF, 0xF0F0F0, 0xEFEF80, 0xF0F0F0 };
		int v = arr[this.rnd.nextInt(arr.length)];
		eyeColor[0] = v;
		eyeColor[1] = v;
		
		arr = new int[] { 0x7FB238, 0xF7E9A3, 0xA0A0FF, 0xA7A7A7, 0xA4A8B8, 0x4040FF, 0xD87F33, 0xB24CD8, 0x6699D8, 0xE5E533,
				0x7FCC19, 0xF27FA5, 0x999999, 0x4C7F99, 0x7F3FB2, 0x334CB2, 0x664C33, 0x667F33, 0x993333, 0xFAEE4D,
				0x5CDBD5, 0x4A80FF, 0x00D93A };
		v = arr[this.rnd.nextInt(arr.length)];
		pupilColor[0] = v;
		pupilColor[1] = v;
		
		arr = new int[] { 0x5B4934, 0x9E9E8F, 0xF02020, 0x302010, 0x2020F0, 0xF48E10, 0xE60CCC };
		v = arr[this.rnd.nextInt(arr.length)];
		browColor[0] = v;
		browColor[1] = v;
		
		arr = new int[] { 0xB4846D, 0x5B4934, 0x9E9E8F, 0xF02020, 0x302010, 0x2020F0, 0xF48E10, 0xE60CCC };
		skinColor = arr[this.rnd.nextInt(arr.length)];
		
		glint = this.rnd.nextFloat() < 0.5f;
		activeLeft = this.rnd.nextFloat() > 0.005f;
		activeRight = this.rnd.nextFloat() > 0.005f;
		
		v = this.rnd.nextInt(3);
		eyeLeft = new ResourceLocation("moreplayermodels:textures/eyes/eye_" + v + ".png");
		eyeRight = new ResourceLocation("moreplayermodels:textures/eyes/eye_" + v + ".png");
		v = this.rnd.nextInt(4);
		pupilLeft = new ResourceLocation("moreplayermodels:textures/eyes/pupil_" + v + ".png");
		pupilRight = new ResourceLocation("moreplayermodels:textures/eyes/pupil_" + v + ".png");
		v = this.rnd.nextInt(4);
		browLeft = new ResourceLocation("moreplayermodels:textures/eyes/brow_" + v + ".png");
		browRight = new ResourceLocation("moreplayermodels:textures/eyes/brow_" + v + ".png");
	}

	public boolean isEnabled() {
		return this.type >= 0;
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if (compound.getKeySet().size() == 0) { return; }
		super.readFromNBT(compound);
		this.glint = compound.getBoolean("Glint");
		
		if (compound.hasKey("Closed", 1)) { this.closed =compound.getBoolean("Closed") ? 1 : 0; }
		else { this.closed = compound.getInteger("Closed"); }
		if (this.closed < 0) { this.closed *= -1; }
		if (this.closed > 3) { this.closed = 3; }
		
		this.skinColor = compound.getInteger("SkinColor");
		this.eyePos = compound.getInteger("PositionY");
		this.browThickness = compound.getInteger("BrowThickness");
		
		if (compound.hasKey("ActiveLeft", 1)) { this.activeLeft = compound.getBoolean("ActiveLeft"); }
		if (compound.hasKey("ActiveRight", 1)) { this.activeRight = compound.getBoolean("ActiveRight"); }
				
		if (compound.hasKey("EyeColor", 11) && compound.getIntArray("EyeColor").length > 1) {
			eyeColor[0] = compound.getIntArray("EyeColor")[0];
			eyeColor[1] = compound.getIntArray("EyeColor")[1];
		}

		if (compound.hasKey("PupilColor", 11) && compound.getIntArray("PupilColor").length > 1) {
			pupilColor[0] = compound.getIntArray("PupilColor")[0];
			pupilColor[1] = compound.getIntArray("PupilColor")[1];
		} else {
			pupilColor[0] = compound.getInteger("Color");
			pupilColor[1] = compound.getInteger("Color");
		}
		
		if (compound.hasKey("BrowColor", 3)) {
			browColor[0] = compound.getInteger("BrowColor");
			browColor[1] = compound.getInteger("BrowColor");
		} else if (compound.hasKey("BrowColor", 11) && compound.getIntArray("BrowColor").length > 1) {
			browColor[0] = compound.getIntArray("BrowColor")[0];
			browColor[1] = compound.getIntArray("BrowColor")[1];
		}
		if (compound.hasKey("EyeResources", 9)) {
			NBTTagList ress = compound.getTagList("EyeResources", 8);
			if (ress.tagCount() > 0) { this.eyeLeft = new ResourceLocation(ress.getStringTagAt(0)); }
			if (ress.tagCount() > 1) { this.eyeRight = new ResourceLocation(ress.getStringTagAt(1)); }
			if (ress.tagCount() > 2) { this.pupilLeft = new ResourceLocation(ress.getStringTagAt(2)); }
			if (ress.tagCount() > 3) { this.pupilRight = new ResourceLocation(ress.getStringTagAt(3)); }
			if (ress.tagCount() > 4) { this.browLeft = new ResourceLocation(ress.getStringTagAt(4)); }
			if (ress.tagCount() > 5) { this.browRight = new ResourceLocation(ress.getStringTagAt(5)); }
		}
	}

	public void update(EntityNPCInterface npc) {
		if (!this.isEnabled() || !npc.isEntityAlive() || (npc.getName().indexOf("1_")!=0 && !npc.isServerWorld())) { return; }
		if (this.blinkStart < 0L) { ++this.blinkStart; }
		else if (this.blinkStart == 0L) {
			if (npc.isDead || npc.isPlayerSleeping()) { return; }
			if (this.rnd.nextInt(150) == 1) { // 140
				this.blinkStart = System.currentTimeMillis();
				if (npc != null) { Server.sendAssociatedData(npc, EnumPacketClient.EYE_BLINK, npc.getEntityId()); }
			}
		} else if (System.currentTimeMillis() - this.blinkStart > 300L) {
			this.blinkStart = -20L;
		}
	}

	@Override
	public NBTTagCompound writeToNBT() {
		NBTTagCompound compound = super.writeToNBT();
		compound.setBoolean("Glint", this.glint);
		compound.setInteger("Closed", this.closed);
		compound.setInteger("SkinColor", this.skinColor);
		compound.setInteger("PositionY", this.eyePos);
		compound.setInteger("BrowThickness", this.browThickness);
		compound.setIntArray("EyeColor", this.eyeColor);
		compound.setIntArray("PupilColor", this.pupilColor);
		compound.setIntArray("BrowColor", this.browColor);

		compound.setBoolean("ActiveLeft", this.activeLeft);
		compound.setBoolean("ActiveRight", this.activeRight);
		
		NBTTagList ress = new NBTTagList();
		ress.appendTag(new NBTTagString(this.eyeLeft.toString()));
		ress.appendTag(new NBTTagString(this.eyeRight.toString()));
		ress.appendTag(new NBTTagString(this.pupilLeft.toString()));
		ress.appendTag(new NBTTagString(this.pupilRight.toString()));
		ress.appendTag(new NBTTagString(this.browLeft.toString()));
		ress.appendTag(new NBTTagString(this.browRight.toString()));
		compound.setTag("EyeResources", ress);
		
		return compound;
	}
}
