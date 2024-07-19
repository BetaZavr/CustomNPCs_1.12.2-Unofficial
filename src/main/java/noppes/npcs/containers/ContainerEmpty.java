package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;

import javax.annotation.Nonnull;

public class ContainerEmpty extends Container {
	public boolean canInteractWith(@Nonnull EntityPlayer var1) {
		return false;
	}
}
