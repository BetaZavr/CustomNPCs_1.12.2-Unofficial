package noppes.npcs.roles.data;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class SpawnNPCData {

	protected final World world;
	public NBTTagCompound compound;
	public int count = 1;
	public int typeClones = 0;

	public SpawnNPCData(World worldIn) { world = worldIn; }

	public SpawnNPCData(NBTTagCompound nbt, World worldIn) {
		world = worldIn;
		readFromNBT(nbt);
	}

	public String getTitle() {
		char chr = ((char) 167);
		String name = chr + "7" + new TextComponentTranslation("type.empty").getFormattedText();
		if (compound != null) { // client or server clone
			if (typeClones == 1 && compound.hasKey("id", 8)) { // mob
				if (world != null) {
					Entity entity = EntityList.createEntityByIDFromName(new ResourceLocation(compound.getString("id")), world);
					if (entity != null) { name = entity.getName(); }
				}
			}
			else if (compound.hasKey("ClonedName", 8)) { name = new TextComponentTranslation(compound.getString("ClonedName")).getFormattedText(); }
			else if (compound.hasKey("Name", 8)) { name = new TextComponentTranslation(compound.getString("Name")).getFormattedText(); }
		}
		if (count > 1) { name += chr + "7 x" + count; }
		return name;
	}

	public void readFromNBT(NBTTagCompound nbt) {
		if (nbt.hasKey("EntityNBT", 10)) { compound = nbt.getCompoundTag("EntityNBT"); }
		if (nbt.hasKey("Name", 8)) {
			if (compound == null) { compound = new NBTTagCompound(); }
			compound.setString("Name", nbt.getString("Name"));
		}
		count = nbt.getInteger("Count");
		typeClones = nbt.getInteger("TypeClone");
	}

	public String toString() { return "{Name: \"" + getTitle() + "\", TypeClone: " + typeClones + "}"; }

	public NBTTagCompound writeToNBT() {
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagCompound entityNBT = new NBTTagCompound();
		if (compound != null) { entityNBT = compound; }
		nbt.setTag("EntityNBT", entityNBT);
		nbt.setInteger("Count", count);
		nbt.setInteger("TypeClone", typeClones);
		return nbt;
	}

}
