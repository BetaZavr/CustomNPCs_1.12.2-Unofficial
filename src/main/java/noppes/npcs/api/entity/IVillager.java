package noppes.npcs.api.entity;

import net.minecraft.entity.monster.EntityMob;
import net.minecraft.inventory.IInventory;
import net.minecraft.village.MerchantRecipeList;
import noppes.npcs.api.ParamName;

@SuppressWarnings("all")
public interface IVillager<T extends EntityMob> extends IEntityLiving<T> {

	MerchantRecipeList getRecipes(@ParamName("player") IPlayer<?> player);

	IInventory getVillagerInventory();

	String getCareer();

	int getProfession();

}
