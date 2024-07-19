package noppes.npcs.roles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.NpcMiscInventory;
import noppes.npcs.Server;
import noppes.npcs.api.constants.RoleType;
import noppes.npcs.api.entity.data.role.IRolePostman;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityNPCInterface;

public class RolePostman extends RoleInterface implements IRolePostman {

	public NpcMiscInventory inventory;
	private final List<EntityPlayer> recentlyChecked;

    public RolePostman(EntityNPCInterface npc) {
		super(npc);
		this.inventory = new NpcMiscInventory(1);
		this.recentlyChecked = new ArrayList<>();
		this.type = RoleType.POSTMAN;
	}

    @Override
	public boolean aiShouldExecute() {
		if (this.npc.ticksExisted % 20 != 0) {
			return false;
		}
        List<EntityPlayer> toCheck;
        (toCheck = this.npc.world.getEntitiesWithinAABB(EntityPlayer.class,
				this.npc.getEntityBoundingBox().grow(10.0, 10.0, 10.0))).removeAll(this.recentlyChecked);

		List<EntityPlayer> listMax = this.npc.world.getEntitiesWithinAABB(EntityPlayer.class,
				this.npc.getEntityBoundingBox().grow(20.0, 20.0, 20.0));
		this.recentlyChecked.retainAll(listMax);
		this.recentlyChecked.addAll(toCheck);
		for (EntityPlayer player : toCheck) {
			if (PlayerData.get(player).mailData.hasMail()) {
				this.npc.say(player,
						new Line(new TextComponentTranslation("mail.player.has.letter").getFormattedText()));
			}
		}
		return false;
	}

	@Override
	public void interact(EntityPlayer player) {
		Server.sendData((EntityPlayerMP) player, EnumPacketClient.GUI, EnumGuiType.PlayerMailbox, 1, 1, 0);
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		this.type = RoleType.POSTMAN;
		this.inventory.setFromNBT(compound.getCompoundTag("PostInv"));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setInteger("Type", RoleType.POSTMAN.get());
		compound.setTag("PostInv", this.inventory.getToNBT());
		return compound;
	}

}
