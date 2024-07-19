package noppes.npcs.api.wrapper;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import noppes.npcs.Server;
import noppes.npcs.api.IContainerCustomChest;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.containers.ContainerNpcInterface;
import noppes.npcs.controllers.ScriptContainer;

public class ContainerCustomChestWrapper extends ContainerWrapper implements IContainerCustomChest {

	public String name;
	public ScriptContainer script;

	public ContainerCustomChestWrapper(Container container) {
		super(container);
		this.script = null;
		this.name = "";
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setName(String name) {
		if (name == null) {
			name = "";
		}
		if (this.name.equals(name)) {
			return;
		}
		this.name = name;
		Server.sendDataDelayed((EntityPlayerMP) ((ContainerNpcInterface) this.getMCContainer()).player,
				EnumPacketClient.CHEST_NAME, 10, name);
	}
}
