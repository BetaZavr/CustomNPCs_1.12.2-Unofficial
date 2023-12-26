package noppes.npcs.entity;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.model.part.ModelData;

public class EntityNpcCrystal extends EntityNPCInterface {
	public EntityNpcCrystal(World world) {
		super(world);
		this.scaleX = 0.7f;
		this.scaleY = 0.7f;
		this.scaleZ = 0.7f;
		this.display.setSkinTexture(CustomNpcs.MODID + ":textures/entity/crystal/EnderCrystal.png");
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
			data.setEntityClass(EntityNpcCrystal.class);
			this.world.spawnEntity(npc);
		}
		super.onUpdate();
	}
}
