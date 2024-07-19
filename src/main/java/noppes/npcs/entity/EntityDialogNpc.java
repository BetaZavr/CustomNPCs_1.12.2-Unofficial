package noppes.npcs.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

import javax.annotation.Nonnull;

public class EntityDialogNpc extends EntityNPCInterface {

	public EntityDialogNpc(World world) {
		super(world);
	}

	@Override
	public boolean isInvisible() {
		return true;
	}

	@Override
	public boolean isInvisibleToPlayer(@Nonnull EntityPlayer player) {
		return true;
	}

	@Override
	public void onUpdate() {
	}

	@Override
	public boolean processInteract(@Nonnull EntityPlayer player, @Nonnull EnumHand hand) {
		return false;
	}

}
