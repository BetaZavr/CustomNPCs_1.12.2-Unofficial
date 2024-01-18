package noppes.npcs.entity.old;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.ModelPartData;
import noppes.npcs.client.model.part.ModelData;
import noppes.npcs.constants.EnumParts;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityNpcEnderchibi extends EntityNPCInterface {
	public EntityNpcEnderchibi(World world) {
		super(world);
		this.display.setSkinTexture(CustomNpcs.MODID + ":textures/entity/enderchibi/MrEnderchibi.png");
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
			data.getPartConfig(EnumParts.LEG_LEFT).setScale(0.65f, 0.75f);
			data.getPartConfig(EnumParts.ARM_LEFT).setScale(0.5f, 1.45f);
			ModelPartData part = data.getOrCreatePart(EnumParts.PARTICLES);
			part.type = 1;
			part.color = 16711680;
			part.playerTexture = true;
			this.world.spawnEntity(npc);
		}
		super.onUpdate();
	}
}
