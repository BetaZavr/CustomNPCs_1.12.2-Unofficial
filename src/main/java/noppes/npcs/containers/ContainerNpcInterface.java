package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.util.math.MathHelper;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.wrapper.ContainerCustomChestWrapper;
import noppes.npcs.api.wrapper.ContainerWrapper;

public class ContainerNpcInterface extends Container {

	public static IContainer getOrCreateIContainer(ContainerNpcInterface container) {
		if (container.scriptContainer != null) {
			return container.scriptContainer;
		}
		if (container instanceof ContainerCustomChest) {
			return container.scriptContainer = new ContainerCustomChestWrapper(container);
		}
		return container.scriptContainer = new ContainerWrapper(container);
	}

	public EntityPlayer player;
	private int posX;
	private int posZ;

	public IContainer scriptContainer;

	public ContainerNpcInterface(EntityPlayer player) {
		this.player = player;
		this.posX = MathHelper.floor(player.posX);
		this.posZ = MathHelper.floor(player.posZ);
		player.motionX = 0.0;
		player.motionZ = 0.0;
	}

	public boolean canInteractWith(EntityPlayer player) {
		return !player.isDead && this.posX == MathHelper.floor(player.posX)
				&& this.posZ == MathHelper.floor(player.posZ);
	}

}
