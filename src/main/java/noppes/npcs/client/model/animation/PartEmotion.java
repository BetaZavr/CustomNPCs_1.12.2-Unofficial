package noppes.npcs.client.model.animation;

import java.util.Random;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.entity.data.IEmotion.IEmotionPart;
import noppes.npcs.entity.EntityNPCInterface;

public class PartEmotion
implements IEmotionPart {

	public long blinkStart;
	public int eyesType, color, browColor, skinColor, speed, delay;
	public float browThickness;
	public boolean glint, disabled;
	public float[] offsetEye, offsetBrow, scale, eyePos;

	private Random rnd;
	private EntityNPCInterface npc;
	
	public PartEmotion(EntityNPCInterface npc) {
		this.npc = npc;
		this.color = 0xFFFFFFFF;
		this.eyesType = 0;
		this.rnd = new Random();
		this.glint = true;
		this.browThickness = 4.0f;
		this.skinColor = 0xFFB4846D;
		this.browColor = 0xFF5B4934;
		this.blinkStart = 0L;
		this.color = 0xFF000000 + this.rnd.nextInt(0xFFFFFF);
		
		this.offsetEye = new float[] { 0.5f, 0.5f };
		this.offsetBrow = new float[] { 0.0f, 0.0f };
		this.scale = new float[] { 0.5f, 0.5f };
		this.eyePos = new float[] { 0.0f, 0.0f };
		this.speed = 20;
		this.delay = 0;
	}

	public void readFromNBT(NBTTagCompound compound) {
		if (compound.getKeySet().size() == 0) { return; }
		this.glint = compound.getBoolean("Glint");
		this.skinColor = compound.getInteger("SkinColor");
		this.browColor = compound.getInteger("BrowColor");
		this.browThickness = compound.getFloat("BrowThickness");
	}

	public NBTTagCompound writeToNBT() {
		NBTTagCompound compound = new NBTTagCompound();
		compound.setBoolean("Glint", this.glint);
		compound.setInteger("SkinColor", this.skinColor);
		compound.setInteger("BrowColor", this.browColor);
		compound.setFloat("BrowThickness", this.browThickness);
		return compound;
	}

	public boolean isDisabled() { return this.disabled; }
}
