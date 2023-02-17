package noppes.npcs.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class EntityChairMount extends Entity {
	public EntityChairMount(World world) {
		super(world);
		this.setSize(0.0f, 0.0f);
	}

	public boolean canBeCollidedWith() {
		return false;
	}

	public boolean canBePushed() {
		return false;
	}

	protected void entityInit() {
	}

	public void fall(float distance, float damageMultiplier) {
	}

	public double getMountedYOffset() {
		return 0.5;
	}

	public boolean isEntityInvulnerable(DamageSource source) {
		return true;
	}

	public boolean isInvisible() {
		return true;
	}

	public void move(MoverType type, double x, double y, double z) {
	}

	public void onEntityUpdate() {
		super.onEntityUpdate();
		if (this.world != null && !this.world.isRemote && this.getPassengers().isEmpty()) {
			this.isDead = true;
		}
	}

	protected void readEntityFromNBT(NBTTagCompound tagCompound) {
	}

	@SideOnly(Side.CLIENT)
	public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch,
			int posRotationIncrements, boolean bo) {
		this.setPosition(x, y, z);
		this.setRotation(yaw, pitch);
	}

	protected void writeEntityToNBT(NBTTagCompound tagCompound) {
	}
}
