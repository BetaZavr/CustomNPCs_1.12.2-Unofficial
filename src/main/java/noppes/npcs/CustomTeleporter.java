package noppes.npcs;

import net.minecraft.entity.Entity;
import net.minecraft.world.Teleporter;
import net.minecraft.world.WorldServer;

import javax.annotation.Nonnull;

public class CustomTeleporter extends Teleporter {

	public CustomTeleporter(WorldServer worldServer) {
		super(worldServer != null ? worldServer : CustomNpcs.Server.getWorld(0));
	}

	public void placeInPortal(@Nonnull Entity entityIn, float rotationYaw) {}

}
