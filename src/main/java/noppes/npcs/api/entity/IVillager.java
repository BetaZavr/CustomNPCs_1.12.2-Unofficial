package noppes.npcs.api.entity;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.inventory.IInventory;
import net.minecraft.village.MerchantRecipeList;

public interface IVillager<T extends EntityMob> extends IEntityLiving<T> {

	MerchantRecipeList getRecipes(IPlayer<?> player);

	IInventory getVillagerInventory();

	String getCareer();

	int getProfession();

}
