package noppes.npcs.blocks.tiles;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.Server;
import noppes.npcs.api.NpcAPI;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IQuestObjective;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumQuestTask;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.controllers.data.QuestData;
import noppes.npcs.quests.QuestInterface;
import noppes.npcs.quests.QuestObjective;

public class TileWaypoint extends TileNpcEntity implements ITickable {

	public String name;
	public int range;
	private List<EntityPlayer> recentlyChecked;
	private int ticks;
	private List<EntityPlayer> toCheck;

	public TileWaypoint() {
		this.name = "";
		this.ticks = 10;
		this.recentlyChecked = new ArrayList<EntityPlayer>();
		this.range = 10;
	}

	private List<EntityPlayer> getPlayerList(int x, int y, int z) {
		return this.world.getEntitiesWithinAABB(EntityPlayer.class,
				new AxisAlignedBB(this.pos, this.pos.add(1, 1, 1)).grow(x, y, z));
	}

	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		this.name = compound.getString("LocationName");
		this.range = compound.getInteger("LocationRange");
		if (this.range < 2) {
			this.range = 2;
		}
	}

	public void update() {
		if (this.world.isRemote || this.name.isEmpty()) {
			return;
		}
		--this.ticks;
		if (this.ticks > 0) {
			return;
		}
		this.ticks = 10;

		List<EntityPlayer> around = this.getPlayerList(this.range, this.range, this.range);
		if (around.isEmpty()) {
			this.recentlyChecked = new ArrayList<EntityPlayer>();
			return;
		}
		(this.toCheck = around).removeAll(this.recentlyChecked);
		int rng = this.range + (this.range < 10 ? this.range : 10); // Changed
		List<EntityPlayer> listMax = this.getPlayerList(rng, rng, rng);

		this.recentlyChecked.retainAll(listMax);
		this.toCheck.addAll(this.recentlyChecked);

		if (this.toCheck.isEmpty()) {
			return;
		}
		for (EntityPlayer player : this.toCheck) {
			PlayerData pdata = PlayerData.get(player);
			PlayerQuestData questData = pdata.questData;
			for (QuestData data : questData.activeQuests.values()) {
				if (data.quest.step == 2 && data.quest.questInterface.isCompleted(player)) {
					continue;
				}
				boolean bo = data.quest.step == 1;
				for (IQuestObjective obj : data.quest
						.getObjectives((IPlayer<?>) NpcAPI.Instance().getIEntity(player))) {
					if (data.quest.step == 1 && !bo) {
						break;
					}
					bo = obj.isCompleted();
					if (((QuestObjective) obj).getEnumType() != EnumQuestTask.LOCATION
							|| !((QuestObjective) obj).getTargetName().equals(this.name)) {
						continue;
					}

					QuestInterface quest = data.quest.questInterface;
					if (!quest.setFound(data, this.name)) {
						continue;
					}
					if (data.quest.showProgressInWindow) {
						NBTTagCompound compound = new NBTTagCompound();
						compound.setInteger("QuestID", data.quest.id);
						compound.setString("Type", "location");
						compound.setIntArray("Progress", new int[] { 1, 1 });
						compound.setString("TargetName", ((QuestObjective) obj).getTargetName());
						compound.setInteger("MessageType", 0);
						Server.sendData((EntityPlayerMP) player, EnumPacketClient.MESSAGE_DATA, compound);
					}
					if (data.quest.showProgressInChat) {
						player.sendMessage(new TextComponentTranslation("quest.message.location.1",
								new TextComponentTranslation(((QuestObjective) obj).getTargetName()).getFormattedText(),
								data.quest.getTitle()));
					}

					questData.checkQuestCompletion(player, data);
					questData.updateClient = true;
				}
			}
		}
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		if (!this.name.isEmpty()) {
			compound.setString("LocationName", this.name);
		}
		compound.setInteger("LocationRange", this.range);
		return super.writeToNBT(compound);
	}
}
