package noppes.npcs.roles.data;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;

public class SpawnNPCData {
	
	public NBTTagCompound compound;
	public int count = 1;
	public int typeClones = 0;
	
	public SpawnNPCData() { }

	public SpawnNPCData(NBTTagCompound nbt) { this.readFromNBT(nbt); }

	public void readFromNBT(NBTTagCompound nbt) {
		if (nbt.hasKey("EntityNBT", 10)) { this.compound = nbt.getCompoundTag("EntityNBT"); }
		if (nbt.hasKey("Name", 8)) { 
			if (this.compound==null) { this.compound = new NBTTagCompound(); }
			this.compound.setString("Name", nbt.getString("Name"));
		}
		this.count = nbt.getInteger("Count");
		this.typeClones = nbt.getInteger("TypeClone");
	}

	public NBTTagCompound writeToNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagCompound entityNBT = new NBTTagCompound();
		if (this.compound!=null) { entityNBT = this.compound; }
		nbt.setTag("EntityNBT", entityNBT);
		nbt.setInteger("Count", this.count);
		nbt.setInteger("TypeClone", this.typeClones);
		return nbt;
	}

	public String getTitle() {
		char chr = ((char) 167);
		String name = chr + "7" + new TextComponentTranslation("type.empty").getFormattedText();
		if (this.compound!= null) { // client or server clone
			if (this.typeClones==1 && this.compound.hasKey("id", 8)) { // mob
				Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(this.compound.getString("id")), Minecraft.getMinecraft().world);
				if (entity!=null) { name = entity.getName(); }
			}
			else if (this.compound.hasKey("ClonedName", 8)) {
				name = new TextComponentTranslation(this.compound.getString("ClonedName")).getFormattedText();
			} else if (this.compound.hasKey("Name", 8)) {
				name = new TextComponentTranslation(this.compound.getString("Name")).getFormattedText();
			}
		}
		if (this.count>1) { name += chr + "7 x" + this.count; }
		return name;
	}
	
	public String toString() {
		return "{Name: \""+this.getTitle()+"\", TypeClone: "+this.typeClones+"}";
	}
}
