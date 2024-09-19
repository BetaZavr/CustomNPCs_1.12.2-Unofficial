package noppes.npcs.api.wrapper;

import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.inventory.IInventory;
import net.minecraft.village.MerchantRecipeList;
import noppes.npcs.api.constants.EntityType;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.entity.IVillager;
import noppes.npcs.mixin.api.entity.passive.EntityVillagerAPIMixin;

@SuppressWarnings("rawtypes")
public class VillagerWrapper<T extends EntityVillager> extends EntityLivingWrapper<T> implements IVillager {

	public VillagerWrapper(T entity) {
		super(entity);
	}

	public String getCareer() {
		return this.entity.getProfessionForge().getCareer(((EntityVillagerAPIMixin) this.entity).npcs$getCareerID()).getName();
	}

	@SuppressWarnings("deprecation")
	public int getProfession() {
		return this.entity.getProfession();
	}

	@Override
	public MerchantRecipeList getRecipes(IPlayer player) {
		return this.entity.getRecipes(player.getMCEntity());
	}

	@Override
	public int getType() {
		return EntityType.VILLAGER.get();
	}

	@Override
	public IInventory getVillagerInventory() {
		return this.entity.getVillagerInventory();
	}

	@Override
	public boolean typeOf(int type) {
		return type == EntityType.VILLAGER.get() || super.typeOf(type);
	}

}
