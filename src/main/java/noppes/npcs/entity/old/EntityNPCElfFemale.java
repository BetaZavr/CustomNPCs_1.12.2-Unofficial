package noppes.npcs.entity.old;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.model.part.ModelData;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityNPCElfFemale extends EntityNPCInterface {
	public EntityNPCElfFemale(World world) {
		super(world);
		this.display.setSkinTexture(CustomNpcs.MODID + ":textures/entity/elffemale/ElfFemale.png");
		this.scaleX = 0.8f;
		this.scaleY = 1.0f;
		this.scaleZ = 0.8f;
	}

	@Override
	public void onUpdate() {
		this.setNoAI(this.isDead = true);
		if (!this.world.isRemote) {
			NBTTagCompound compound = new NBTTagCompound();
			this.writeToNBT(compound);
			EntityCustomNpc npc = new EntityCustomNpc(this.world);
			npc.readFromNBT(compound);
			ModelData data = npc.modelData;
			data.getOrCreatePart(EnumParts.BREASTS).type = 2;
			data.getPartConfig(EnumParts.LEG_LEFT).setScale(0.8f, 1.05f);
			data.getPartConfig(EnumParts.ARM_LEFT).setScale(0.8f, 1.05f);
			data.getPartConfig(EnumParts.BODY).setScale(0.8f, 1.05f);
			data.getPartConfig(EnumParts.HEAD).setScale(0.8f, 0.85f);
			this.world.spawnEntity(npc);
		}
		super.onUpdate();
	}
}
