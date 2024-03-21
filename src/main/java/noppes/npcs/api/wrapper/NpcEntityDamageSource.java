package noppes.npcs.api.wrapper;

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

public class NpcEntityDamageSource
extends EntityDamageSource
implements IEntityDamageSource
{
	@Nullable
	public Entity damageSourceEntity;
	public Entity indirectEntity;
	public boolean isThornsDamage;
    
	public boolean isUnblockable = false;
	public boolean isDamageAllowedInCreativeMode = false;
	public boolean damageIsAbsolute = false;
	public float hungerDamage = 0.1f;
	public boolean fireDamage = false;
	public boolean projectile = false;
	public boolean difficultyScaled = false;
	public boolean magicDamage = false;
	public boolean explosion = false;
    public String damageType = "npcCustomDamage";
    public String deadMessage = "";
    
	public NpcEntityDamageSource(String damageType, IEntity<?> damageSourceEntityIn) {
		super(damageType, damageSourceEntityIn.getMCEntity());
		if (damageType.isEmpty()) { damageType = "npcCustomDamage"; }
		this.damageType = damageType;
		this.damageSourceEntity = damageSourceEntityIn.getMCEntity();
		this.deadMessage = "";
	}
	
	public ITextComponent getDeathMessage(EntityLivingBase entity) {
        ITextComponent entitySourceName = this.indirectEntity == null ? this.damageSourceEntity.getDisplayName() : this.indirectEntity.getDisplayName();
        ItemStack stack = this.indirectEntity instanceof EntityLivingBase ?
        		((EntityLivingBase)this.indirectEntity).getHeldItemMainhand() :
        			this.damageSourceEntity instanceof EntityLivingBase ?
        	        ((EntityLivingBase)this.damageSourceEntity).getHeldItemMainhand() :
        	        	ItemStack.EMPTY;
        if (!this.deadMessage.isEmpty()) {
			return new TextComponentTranslation(this.deadMessage, new Object[] { entity.getDisplayName(), entitySourceName, new TextComponentTranslation(this.damageType).getFormattedText(), stack.getTextComponent()});
		}
        String s = "death.attack." + this.damageType;
        String s1 = s + ".item";
        ITextComponent ts1 = new TextComponentTranslation(s1, new Object[] { entity.getDisplayName(), entitySourceName, stack.getTextComponent()});
        return !stack.isEmpty() && stack.hasDisplayName() && ts1.getFormattedText().equals(s1) ? ts1 : new TextComponentTranslation(s, new Object[] { entity.getDisplayName(), entitySourceName});
    }

	@Override
	public String getType() { return this.damageType; }

	@Override
	public void setType(String damageType) {
		if (damageType.isEmpty()) { damageType = "npcCustomDamage"; }
		this.damageType = damageType;
	}

	@Override
	public Entity getTrueSource() { return this.damageSourceEntity; }

	@Override
	public IEntity<?> getITrueSource() { return this.damageSourceEntity==null ? null : NpcAPI.Instance().getIEntity(this.damageSourceEntity);}
	
	@Override
	public void setTrueSource(IEntity<?> entity) {
		this.damageSourceEntity = entity==null ? null : entity.getMCEntity();
	}

	@Nullable
    public Entity getImmediateSource() { return this.indirectEntity; }
	
	@Override
	public IEntity<?> getIImmediateSource() { return this.indirectEntity==null ? null : NpcAPI.Instance().getIEntity(this.indirectEntity); }

	@Override
	public void setImmediateSource(IEntity<?> entity) {
		this.indirectEntity = entity==null ? null : entity.getMCEntity();
	}

	@Override
	public String getDeadMessage() { return this.deadMessage; }

	@Override
	public void setDeadMessage(String message) {
		if (message==null) { message = ""; }
		this.deadMessage = message;
	}

}
