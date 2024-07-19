package noppes.npcs.api.wrapper;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EntityDamageSource;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.api.IEntityDamageSource;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IEntity;

import java.util.Objects;

public class NpcEntityDamageSource extends EntityDamageSource implements IEntityDamageSource {

	public Entity damageSourceEntity;
	public Entity indirectEntity;
	public boolean projectile = false;
	public boolean explosion = false;
	public String damageType;
	public String deadMessage;

	public NpcEntityDamageSource(String damageType, IEntity<?> damageSourceEntityIn) {
		super(damageType, damageSourceEntityIn.getMCEntity());
		if (damageType.isEmpty()) {
			damageType = "npcCustomDamage";
		}
		this.damageType = damageType;
		this.damageSourceEntity = damageSourceEntityIn.getMCEntity();
		this.deadMessage = "";
	}

	@Override
	public String getDeadMessage() {
		return this.deadMessage;
	}

	@Nonnull
	public ITextComponent getDeathMessage(@Nonnull EntityLivingBase entity) {
		ITextComponent entitySourceName = this.indirectEntity == null ? this.damageSourceEntity.getDisplayName()
				: this.indirectEntity.getDisplayName();
		ItemStack stack = this.indirectEntity instanceof EntityLivingBase
				? ((EntityLivingBase) this.indirectEntity).getHeldItemMainhand()
				: this.damageSourceEntity instanceof EntityLivingBase
						? ((EntityLivingBase) this.damageSourceEntity).getHeldItemMainhand()
						: ItemStack.EMPTY;
		if (!this.deadMessage.isEmpty()) {
			return new TextComponentTranslation(this.deadMessage,
                    entity.getDisplayName(), entitySourceName,
                    new TextComponentTranslation(this.damageType).getFormattedText(),
                    stack.getTextComponent());
		}
		String s = "death.attack." + this.damageType;
		String s1 = s + ".item";
		ITextComponent ts1 = new TextComponentTranslation(s1, entity.getDisplayName(), entitySourceName, stack.getTextComponent());
		return !stack.isEmpty() && stack.hasDisplayName() && ts1.getFormattedText().equals(s1) ? ts1
				: new TextComponentTranslation(s, entity.getDisplayName(), entitySourceName);
	}

	@Override
	public IEntity<?> getIImmediateSource() {
		return this.indirectEntity == null ? null : Objects.requireNonNull(NpcAPI.Instance()).getIEntity(this.indirectEntity);
	}

	@Nullable
	public Entity getImmediateSource() {
		return this.indirectEntity;
	}

	@Override
	public IEntity<?> getITrueSource() {
		return this.damageSourceEntity == null ? null : Objects.requireNonNull(NpcAPI.Instance()).getIEntity(this.damageSourceEntity);
	}

	@Override
	public Entity getTrueSource() {
		return this.damageSourceEntity;
	}

	@Override
	public String getType() {
		return this.damageType;
	}

	@Override
	public void setDeadMessage(String message) {
		if (message == null) {
			message = "";
		}
		this.deadMessage = message;
	}

	@Override
	public void setImmediateSource(IEntity<?> entity) {
		this.indirectEntity = entity == null ? null : entity.getMCEntity();
	}

	@Override
	public void setTrueSource(IEntity<?> entity) {
		this.damageSourceEntity = entity == null ? null : entity.getMCEntity();
	}

	@Override
	public void setType(String damageType) {
		if (damageType.isEmpty()) {
			damageType = "npcCustomDamage";
		}
		this.damageType = damageType;
	}

}
