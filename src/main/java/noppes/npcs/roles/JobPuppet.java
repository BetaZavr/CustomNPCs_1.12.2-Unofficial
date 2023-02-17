package noppes.npcs.roles;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.api.CustomNPCsException;
import noppes.npcs.api.entity.data.role.IJobPuppet;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.ValueUtil;

public class JobPuppet extends JobInterface implements IJobPuppet {
	public class PartConfig implements IJobPuppetPart {
		public boolean disabled;
		public float rotationX;
		public float rotationY;
		public float rotationZ;

		public PartConfig() {
			this.rotationX = 0.0f;
			this.rotationY = 0.0f;
			this.rotationZ = 0.0f;
			this.disabled = false;
		}

		@Override
		public int getRotationX() {
			return (int) ((this.rotationX + 1.0f) * 180.0f);
		}

		@Override
		public int getRotationY() {
			return (int) ((this.rotationY + 1.0f) * 180.0f);
		}

		@Override
		public int getRotationZ() {
			return (int) ((this.rotationZ + 1.0f) * 180.0f);
		}

		public void readNBT(NBTTagCompound compound) {
			this.rotationX = ValueUtil.correctFloat(compound.getFloat("RotationX"), -1.0f, 1.0f);
			this.rotationY = ValueUtil.correctFloat(compound.getFloat("RotationY"), -1.0f, 1.0f);
			this.rotationZ = ValueUtil.correctFloat(compound.getFloat("RotationZ"), -1.0f, 1.0f);
			this.disabled = compound.getBoolean("Disabled");
		}

		@Override
		public void setRotation(int x, int y, int z) {
			this.disabled = false;
			this.rotationX = ValueUtil.correctFloat(x / 180.0f - 1.0f, -1.0f, 1.0f);
			this.rotationY = ValueUtil.correctFloat(y / 180.0f - 1.0f, -1.0f, 1.0f);
			this.rotationZ = ValueUtil.correctFloat(z / 180.0f - 1.0f, -1.0f, 1.0f);
			JobPuppet.this.npc.updateClient = true;
		}

		public NBTTagCompound writeNBT() {
			NBTTagCompound compound = new NBTTagCompound();
			compound.setFloat("RotationX", this.rotationX);
			compound.setFloat("RotationY", this.rotationY);
			compound.setFloat("RotationZ", this.rotationZ);
			compound.setBoolean("Disabled", this.disabled);
			return compound;
		}
	}

	public boolean animate;
	public int animationSpeed;
	public PartConfig body;
	public PartConfig body2;
	public PartConfig head;
	public PartConfig head2;
	public PartConfig larm;
	public PartConfig larm2;
	public PartConfig lleg;
	public PartConfig lleg2;
	private int prevTicks;
	public PartConfig rarm;
	public PartConfig rarm2;
	public PartConfig rleg;
	public PartConfig rleg2;
	private int startTick;
	private float val;
	private float valNext;
	public boolean whileAttacking;
	public boolean whileMoving;

	public boolean whileStanding;

	public JobPuppet(EntityNPCInterface npc) {
		super(npc);
		this.head = new PartConfig();
		this.larm = new PartConfig();
		this.rarm = new PartConfig();
		this.body = new PartConfig();
		this.lleg = new PartConfig();
		this.rleg = new PartConfig();
		this.head2 = new PartConfig();
		this.larm2 = new PartConfig();
		this.rarm2 = new PartConfig();
		this.body2 = new PartConfig();
		this.lleg2 = new PartConfig();
		this.rleg2 = new PartConfig();
		this.whileStanding = true;
		this.whileAttacking = false;
		this.whileMoving = false;
		this.animate = false;
		this.animationSpeed = 4;
		this.prevTicks = 0;
		this.startTick = 0;
		this.val = 0.0f;
		this.valNext = 0.0f;
	}

	@Override
	public boolean aiShouldExecute() {
		return false;
	}

	private float calcRotation(float r, float r2, float partialTicks) {
		if (!this.animate) {
			return r;
		}
		if (this.prevTicks != this.npc.ticksExisted) {
			float speed = 0.0f;
			if (this.animationSpeed == 0) {
				speed = 40.0f;
			} else if (this.animationSpeed == 1) {
				speed = 24.0f;
			} else if (this.animationSpeed == 2) {
				speed = 13.0f;
			} else if (this.animationSpeed == 3) {
				speed = 10.0f;
			} else if (this.animationSpeed == 4) {
				speed = 7.0f;
			} else if (this.animationSpeed == 5) {
				speed = 4.0f;
			} else if (this.animationSpeed == 6) {
				speed = 3.0f;
			} else if (this.animationSpeed == 7) {
				speed = 2.0f;
			}
			int ticks = this.npc.ticksExisted - this.startTick;
			this.val = 1.0f - (MathHelper.cos(ticks / speed * 3.1415927f / 2.0f) + 1.0f) / 2.0f;
			this.valNext = 1.0f - (MathHelper.cos((ticks + 1) / speed * 3.1415927f / 2.0f) + 1.0f) / 2.0f;
			this.prevTicks = this.npc.ticksExisted;
		}
		float f = this.val + (this.valNext - this.val) * partialTicks;
		return r + (r2 - r) * f;
	}

	@Override
	public void delete() {
	}

	@Override
	public int getAnimationSpeed() {
		return this.animationSpeed;
	}

	@Override
	public boolean getIsAnimated() {
		return this.animate;
	}

	@Override
	public IJobPuppetPart getPart(int part) {
		if (part == 0) {
			return this.head;
		}
		if (part == 1) {
			return this.larm;
		}
		if (part == 2) {
			return this.rarm;
		}
		if (part == 3) {
			return this.body;
		}
		if (part == 4) {
			return this.lleg;
		}
		if (part == 5) {
			return this.rleg;
		}
		if (part == 6) {
			return this.head2;
		}
		if (part == 7) {
			return this.larm2;
		}
		if (part == 8) {
			return this.rarm2;
		}
		if (part == 9) {
			return this.body2;
		}
		if (part == 10) {
			return this.lleg2;
		}
		if (part == 11) {
			return this.rleg2;
		}
		throw new CustomNPCsException("Unknown part " + part, new Object[0]);
	}

	public float getRotationX(PartConfig part1, PartConfig part2, float partialTicks) {
		return this.calcRotation(part1.rotationX, part2.rotationX, partialTicks);
	}

	public float getRotationY(PartConfig part1, PartConfig part2, float partialTicks) {
		return this.calcRotation(part1.rotationY, part2.rotationY, partialTicks);
	}

	public float getRotationZ(PartConfig part1, PartConfig part2, float partialTicks) {
		return this.calcRotation(part1.rotationZ, part2.rotationZ, partialTicks);
	}

	public boolean isActive() {
		return this.npc.isEntityAlive() && ((this.whileAttacking && this.npc.isAttacking())
				|| (this.whileMoving && this.npc.isWalking()) || (this.whileStanding && !this.npc.isWalking()));
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.head.readNBT(compound.getCompoundTag("PuppetHead"));
		this.larm.readNBT(compound.getCompoundTag("PuppetLArm"));
		this.rarm.readNBT(compound.getCompoundTag("PuppetRArm"));
		this.body.readNBT(compound.getCompoundTag("PuppetBody"));
		this.lleg.readNBT(compound.getCompoundTag("PuppetLLeg"));
		this.rleg.readNBT(compound.getCompoundTag("PuppetRLeg"));
		this.head2.readNBT(compound.getCompoundTag("PuppetHead2"));
		this.larm2.readNBT(compound.getCompoundTag("PuppetLArm2"));
		this.rarm2.readNBT(compound.getCompoundTag("PuppetRArm2"));
		this.body2.readNBT(compound.getCompoundTag("PuppetBody2"));
		this.lleg2.readNBT(compound.getCompoundTag("PuppetLLeg2"));
		this.rleg2.readNBT(compound.getCompoundTag("PuppetRLeg2"));
		this.whileStanding = compound.getBoolean("PuppetStanding");
		this.whileAttacking = compound.getBoolean("PuppetAttacking");
		this.whileMoving = compound.getBoolean("PuppetMoving");
		this.setIsAnimated(compound.getBoolean("PuppetAnimate"));
		this.setAnimationSpeed(compound.getInteger("PuppetAnimationSpeed"));
	}

	@Override
	public void reset() {
		this.val = 0.0f;
		this.valNext = 0.0f;
		this.prevTicks = 0;
		this.startTick = this.npc.ticksExisted;
	}

	@Override
	public void setAnimationSpeed(int speed) {
		this.animationSpeed = ValueUtil.correctInt(speed, 0, 7);
		this.npc.updateClient = true;
	}

	@Override
	public void setIsAnimated(boolean bo) {
		if (!(this.animate = bo)) {
			this.val = 0.0f;
			this.valNext = 0.0f;
			this.prevTicks = 0;
		} else {
			this.startTick = this.npc.ticksExisted;
		}
		this.npc.updateClient = true;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("PuppetHead", this.head.writeNBT());
		compound.setTag("PuppetLArm", this.larm.writeNBT());
		compound.setTag("PuppetRArm", this.rarm.writeNBT());
		compound.setTag("PuppetBody", this.body.writeNBT());
		compound.setTag("PuppetLLeg", this.lleg.writeNBT());
		compound.setTag("PuppetRLeg", this.rleg.writeNBT());
		compound.setTag("PuppetHead2", this.head2.writeNBT());
		compound.setTag("PuppetLArm2", this.larm2.writeNBT());
		compound.setTag("PuppetRArm2", this.rarm2.writeNBT());
		compound.setTag("PuppetBody2", this.body2.writeNBT());
		compound.setTag("PuppetLLeg2", this.lleg2.writeNBT());
		compound.setTag("PuppetRLeg2", this.rleg2.writeNBT());
		compound.setBoolean("PuppetStanding", this.whileStanding);
		compound.setBoolean("PuppetAttacking", this.whileAttacking);
		compound.setBoolean("PuppetMoving", this.whileMoving);
		compound.setBoolean("PuppetAnimate", this.animate);
		compound.setInteger("PuppetAnimationSpeed", this.animationSpeed);
		return compound;
	}
}
