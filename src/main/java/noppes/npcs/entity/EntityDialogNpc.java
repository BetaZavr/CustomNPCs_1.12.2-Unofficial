package noppes.npcs.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class EntityDialogNpc
extends EntityNPCInterface {
	
	public EntityDialogNpc(World world) { super(world); }

	@Override
	public boolean isInvisible() { return true; }

	@Override
	public boolean isInvisibleToPlayer(EntityPlayer player) { return true; }

	@Override
	public void onUpdate() { }

	@Override
	public boolean processInteract(EntityPlayer player, EnumHand hand) { return false; }
	
}
