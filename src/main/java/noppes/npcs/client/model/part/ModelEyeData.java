package noppes.npcs.client.model.part;

import java.awt.*;
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
	public int centerColor = 0;
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

	private final Random rnd = new Random();
	public boolean activeLeft = true;
	public boolean activeRight = true;
	public boolean activeCenter = false;
	public int ticks = -1;

	public ModelEyeData() {
		super("eyes");
		type = -1;
		reset();
	}
	
	public void reset() {
		int[] arr = new int[] {
				new Color(0xE6E6E6).getRGB(),
				new Color(0xE64040).getRGB(),
				new Color(0xE6E6E6).getRGB(),
				new Color(0x40E640).getRGB(),
				new Color(0xE6E6E6).getRGB(),
				new Color(0x4040E6).getRGB(),
				new Color(0xE6E6E6).getRGB(),
				new Color(0x92A3A3).getRGB(),
				new Color(0xE6E6E6).getRGB(),
				new Color(0xDCDC80).getRGB()};
		int v = arr[rnd.nextInt(arr.length)];
		eyeColor[0] = v;
		eyeColor[1] = v;
		
		arr = new int[] {
				new Color(0x7FB238).getRGB(),
				new Color(0xF7E9A3).getRGB(),
				new Color(0xA0A0FF).getRGB(),
				new Color(0xA7A7A7).getRGB(),
				new Color(0xA4A8B8).getRGB(),
				new Color(0x4040FF).getRGB(),
				new Color(0xD87F33).getRGB(),
				new Color(0xB24CD8).getRGB(),
				new Color(0x6699D8).getRGB(),
				new Color(0xE5E533).getRGB(),
				new Color(0x7FCC19).getRGB(),
				new Color(0xF27FA5).getRGB(),
				new Color(0x999999).getRGB(),
				new Color(0x4C7F99).getRGB(),
				new Color(0x7F3FB2).getRGB(),
				new Color(0x334CB2).getRGB(),
				new Color(0x664C33).getRGB(),
				new Color(0x667F33).getRGB(),
				new Color(0x993333).getRGB(),
				new Color(0xFAEE4D).getRGB(),
				new Color(0x5CDBD5).getRGB(),
				new Color(0x4A80FF).getRGB(),
				new Color(0x00D93A).getRGB()
		};
		v = arr[rnd.nextInt(arr.length)];
		pupilColor[0] = v;
		pupilColor[1] = v;

		centerColor = new Color(0x00000).getRGB();

		arr = new int[] {
				new Color(0x5B4934).getRGB(),
				new Color(0x9E9E8F).getRGB(),
				new Color(0x302010).getRGB(),
				new Color(0xF48E10).getRGB()};
		v = arr[rnd.nextInt(arr.length)];
		browColor[0] = v;
		browColor[1] = v;
		
		arr = new int[] {
				new Color(0xB4846D).getRGB(),
				new Color(0x5B4934).getRGB(),
				new Color(0x9E9E8F).getRGB(),
				new Color(0x302010).getRGB(),
				new Color(0xF48E10).getRGB()};
		skinColor = arr[rnd.nextInt(arr.length)];
		
		glint = rnd.nextFloat() < 0.5f;
		activeLeft = rnd.nextFloat() > 0.005f;
		activeRight = rnd.nextFloat() > 0.005f;
		activeCenter = rnd.nextFloat() > 0.9f;
		
		v = rnd.nextInt(3);
		eyeLeft = new ResourceLocation("moreplayermodels:textures/eyes/eye_" + v + ".png");
		eyeRight = new ResourceLocation("moreplayermodels:textures/eyes/eye_" + v + ".png");
		v = rnd.nextInt(4);
		pupilLeft = new ResourceLocation("moreplayermodels:textures/eyes/pupil_" + v + ".png");
		pupilRight = new ResourceLocation("moreplayermodels:textures/eyes/pupil_" + v + ".png");
		v = rnd.nextInt(4);
		browLeft = new ResourceLocation("moreplayermodels:textures/eyes/brow_" + v + ".png");
		browRight = new ResourceLocation("moreplayermodels:textures/eyes/brow_" + v + ".png");
	}

	public boolean isEnabled() { return type >= 0; }

	@Override
	public void load(NBTTagCompound compound) {
		if (compound.getKeySet().isEmpty()) { return; }
		super.load(compound);
		glint = compound.getBoolean("Glint");
		
		if (compound.hasKey("Closed", 1)) { closed =compound.getBoolean("Closed") ? 1 : 0; }
		else { closed = compound.getInteger("Closed"); }
		if (closed < 0) { closed *= -1; }
		if (closed > 3) { closed = 3; }
		
		skinColor = compound.getInteger("SkinColor");
		eyePos = compound.getInteger("PositionY");
		browThickness = compound.getInteger("BrowThickness");
		
		if (compound.hasKey("ActiveLeft", 1)) { activeLeft = compound.getBoolean("ActiveLeft"); }
		if (compound.hasKey("ActiveRight", 1)) { activeRight = compound.getBoolean("ActiveRight"); }
		if (compound.hasKey("ActiveCenter", 1)) { activeCenter = compound.getBoolean("ActiveCenter"); }
				
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

		if (compound.hasKey("CenterColor", 3)) { centerColor = compound.getInteger("CenterColor"); }
		else { centerColor = 0x000000; }
		
		if (compound.hasKey("BrowColor", 3)) {
			browColor[0] = compound.getInteger("BrowColor");
			browColor[1] = compound.getInteger("BrowColor");
		} else if (compound.hasKey("BrowColor", 11) && compound.getIntArray("BrowColor").length > 1) {
			browColor[0] = compound.getIntArray("BrowColor")[0];
			browColor[1] = compound.getIntArray("BrowColor")[1];
		}
		if (compound.hasKey("EyeResources", 9)) {
			NBTTagList ress = compound.getTagList("EyeResources", 8);
			if (ress.tagCount() > 0) { eyeLeft = new ResourceLocation(ress.getStringTagAt(0)); }
			if (ress.tagCount() > 1) { eyeRight = new ResourceLocation(ress.getStringTagAt(1)); }
			if (ress.tagCount() > 2) { pupilLeft = new ResourceLocation(ress.getStringTagAt(2)); }
			if (ress.tagCount() > 3) { pupilRight = new ResourceLocation(ress.getStringTagAt(3)); }
			if (ress.tagCount() > 4) { browLeft = new ResourceLocation(ress.getStringTagAt(4)); }
			if (ress.tagCount() > 5) { browRight = new ResourceLocation(ress.getStringTagAt(5)); }
		}
	}

	public void update(EntityNPCInterface npc) {
		if (!isEnabled() || !npc.isEntityAlive() || (npc.getName().indexOf("1_")!=0 && !npc.isServerWorld())) { return; }
		if (blinkStart < 0L) { ++blinkStart; }
		else if (blinkStart == 0L) {
			if (npc.isDead || npc.isPlayerSleeping()) { return; }
			if (rnd.nextInt(150) == 1) { // 140
				blinkStart = System.currentTimeMillis();
                Server.sendAssociatedData(npc, EnumPacketClient.EYE_BLINK, npc.getEntityId());
            }
		} else if (System.currentTimeMillis() - blinkStart > 300L) {
			blinkStart = -20L;
		}
	}

	@Override
	public NBTTagCompound save() {
		NBTTagCompound compound = super.save();
		compound.setBoolean("Glint", glint);
		compound.setInteger("Closed", closed);
		compound.setInteger("SkinColor", skinColor);
		compound.setInteger("PositionY", eyePos);
		compound.setInteger("BrowThickness", browThickness);
		compound.setIntArray("EyeColor", eyeColor);
		compound.setIntArray("PupilColor", pupilColor);
		compound.setInteger("CenterColor", centerColor);
		compound.setIntArray("BrowColor", browColor);

		compound.setBoolean("ActiveLeft", activeLeft);
		compound.setBoolean("ActiveRight", activeRight);
		compound.setBoolean("ActiveCenter", activeCenter);
		
		NBTTagList resources = new NBTTagList();
		resources.appendTag(new NBTTagString(eyeLeft.toString()));
		resources.appendTag(new NBTTagString(eyeRight.toString()));
		resources.appendTag(new NBTTagString(pupilLeft.toString()));
		resources.appendTag(new NBTTagString(pupilRight.toString()));
		resources.appendTag(new NBTTagString(browLeft.toString()));
		resources.appendTag(new NBTTagString(browRight.toString()));
		compound.setTag("EyeResources", resources);

		return compound;
	}
}
