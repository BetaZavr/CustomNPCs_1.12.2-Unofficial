package noppes.npcs.entity.old;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.model.part.ModelData;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityNpcNagaFemale extends EntityNPCInterface {
	public EntityNpcNagaFemale(World world) {
		super(world);
		float f = 0.9075f;
		this.scaleZ = f;
		this.scaleY = f;
		this.scaleX = f;
		this.display.setSkinTexture(CustomNpcs.MODID + ":textures/entity/nagafemale/Claire.png");
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
			data.getPartConfig(EnumParts.LEG_LEFT).setScale(0.92f, 0.92f);
			data.getPartConfig(EnumParts.HEAD).setScale(0.95f, 0.95f);
			data.getPartConfig(EnumParts.ARM_LEFT).setScale(0.8f, 0.92f);
			data.getPartConfig(EnumParts.BODY).setScale(0.92f, 0.92f);
			ModelPartData legs = data.getOrCreatePart(EnumParts.LEGS);
			legs.playerTexture = true;
			legs.type = 1;
			this.world.spawnEntity(npc);
		}
		super.onUpdate();
	}
}
