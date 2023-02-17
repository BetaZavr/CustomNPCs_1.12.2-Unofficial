package noppes.npcs.entity.old;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;

public class EntityNpcMonsterMale extends EntityNPCInterface {
	public EntityNpcMonsterMale(World world) {
		super(world);
		this.display.setSkinTexture(CustomNpcs.MODID + ":textures/entity/monstermale/ZombieSteve.png");
	}

	@Override
	public void onUpdate() {
		this.setNoAI(this.isDead = true);
		if (!this.world.isRemote) {
			NBTTagCompound compound = new NBTTagCompound();
			this.writeToNBT(compound);
			EntityCustomNpc npc = new EntityCustomNpc(this.world);
			npc.readFromNBT(compound);
			npc.ais.animationType = 3;
			this.world.spawnEntity(npc);
		}
		super.onUpdate();
	}
}
