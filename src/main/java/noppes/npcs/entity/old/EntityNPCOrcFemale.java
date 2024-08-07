package noppes.npcs.entity.old;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.model.part.ModelData;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityNPCOrcFemale extends EntityNPCInterface {
	public EntityNPCOrcFemale(World world) {
		super(world);
		float f = 0.9375f;
		this.scaleZ = f;
		this.scaleY = f;
		this.scaleX = f;
		this.display.setSkinTexture(CustomNpcs.MODID + ":textures/entity/orcfemale/StrandedFemaleOrc.png");
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
			data.getPartConfig(EnumParts.LEG_LEFT).setScale(1.1f, 1.0f);
			data.getPartConfig(EnumParts.ARM_LEFT).setScale(1.1f, 1.0f);
			data.getPartConfig(EnumParts.BODY).setScale(1.1f, 1.0f, 1.25f);
			this.world.spawnEntity(npc);
		}
		super.onUpdate();
	}
}
