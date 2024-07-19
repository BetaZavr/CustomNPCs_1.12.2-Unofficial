package noppes.npcs.util;

import net.minecraft.entity.Entity;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

import javax.annotation.Nonnull;

public class CustomNpcsTeleporter extends Teleporter {

	public CustomNpcsTeleporter(WorldServer worldIn) {
		super(worldIn);
	}

	@Override
	public boolean makePortal(@Nonnull Entity p_85188_1_) {
		return true;
	}

	@Override
	public boolean placeInExistingPortal(@Nonnull Entity entityIn, float p_180620_2_) {
		return true;
	}

	@Override
	public void placeInPortal(@Nonnull Entity entityIn, float rotationYaw) {
	}

	@Override
	public void removeStalePortalLocations(long p_85189_1_) {
	}

}