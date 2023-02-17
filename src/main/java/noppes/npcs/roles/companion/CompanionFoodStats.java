package noppes.npcs.roles.companion;

import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.DamageSource;
import net.minecraft.world.EnumDifficulty;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import noppes.npcs.entity.EntityNPCInterface;

public class CompanionFoodStats {
	private float foodExhaustionLevel;
	private int foodLevel;
	private float foodSaturationLevel;
	private int foodTimer;
	private int prevFoodLevel;

	public CompanionFoodStats() {
		this.foodLevel = 20;
		this.foodSaturationLevel = 5.0f;
		this.prevFoodLevel = 20;
	}

	public void addExhaustion(float exhaustion) {
		this.foodExhaustionLevel = Math.min(this.foodExhaustionLevel + exhaustion, 40.0f);
	}

	private void addStats(int foodLevelIn, float foodSaturationModifier) {
		this.foodLevel = Math.min(foodLevelIn + this.foodLevel, 20);
		this.foodSaturationLevel = Math.min(this.foodSaturationLevel + foodLevelIn * foodSaturationModifier * 2.0f,
				this.foodLevel);
	}

	public int getFoodLevel() {
		return this.foodLevel;
	}

	@SideOnly(Side.CLIENT)
	public int getPrevFoodLevel() {
		return this.prevFoodLevel;
	}

	public float getSaturationLevel() {
		return this.foodSaturationLevel;
	}

	public boolean needFood() {
		return this.foodLevel < 20;
	}

	public void onFoodEaten(ItemFood food, ItemStack itemstack) {
		this.addStats(food.getHealAmount(itemstack), food.getSaturationModifier(itemstack));
	}

	public void onUpdate(EntityNPCInterface npc) {
		EnumDifficulty enumdifficulty = npc.world.getDifficulty();
		this.prevFoodLevel = this.foodLevel;
		if (this.foodExhaustionLevel > 4.0f) {
			this.foodExhaustionLevel -= 4.0f;
			if (this.foodSaturationLevel > 0.0f) {
				this.foodSaturationLevel = Math.max(this.foodSaturationLevel - 1.0f, 0.0f);
			} else if (enumdifficulty != EnumDifficulty.PEACEFUL) {
				this.foodLevel = Math.max(this.foodLevel - 1, 0);
			}
		}
		if (npc.world.getGameRules().getBoolean("naturalRegeneration") && this.foodLevel >= 18 && npc.getHealth() > 0.0f
				&& npc.getHealth() < npc.getMaxHealth()) {
			++this.foodTimer;
			if (this.foodTimer >= 80) {
				npc.heal(1.0f);
				this.addExhaustion(3.0f);
				this.foodTimer = 0;
			}
		} else if (this.foodLevel <= 0) {
			++this.foodTimer;
			if (this.foodTimer >= 80) {
				if (npc.getHealth() > 10.0f || enumdifficulty == EnumDifficulty.HARD
						|| (npc.getHealth() > 1.0f && enumdifficulty == EnumDifficulty.NORMAL)) {
					npc.attackEntityFrom(DamageSource.STARVE, 1.0f);
				}
				this.foodTimer = 0;
			}
		} else {
			this.foodTimer = 0;
		}
	}

	public void readNBT(NBTTagCompound compound) {
		if (compound.hasKey("foodLevel", 99)) {
			this.foodLevel = compound.getInteger("foodLevel");
			this.foodTimer = compound.getInteger("foodTickTimer");
			this.foodSaturationLevel = compound.getFloat("foodSaturationLevel");
			this.foodExhaustionLevel = compound.getFloat("foodExhaustionLevel");
		}
	}

	@SideOnly(Side.CLIENT)
	public void setFoodLevel(int foodLevelIn) {
		this.foodLevel = foodLevelIn;
	}

	@SideOnly(Side.CLIENT)
	public void setFoodSaturationLevel(float foodSaturationLevelIn) {
		this.foodSaturationLevel = foodSaturationLevelIn;
	}

	public void writeNBT(NBTTagCompound compound) {
		compound.setInteger("foodLevel", this.foodLevel);
		compound.setInteger("foodTickTimer", this.foodTimer);
		compound.setFloat("foodSaturationLevel", this.foodSaturationLevel);
		compound.setFloat("foodExhaustionLevel", this.foodExhaustionLevel);
	}
}
