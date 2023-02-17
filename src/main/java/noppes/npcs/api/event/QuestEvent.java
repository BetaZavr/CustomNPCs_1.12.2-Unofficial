package noppes.npcs.api.event;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.data.IQuest;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.controllers.data.FactionOptions;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.controllers.data.Quest;

public class QuestEvent extends CustomNPCsEvent {
	// New
	@Cancelable
	public static class QuestCanceledEvent extends QuestEvent {
		public QuestCanceledEvent(IPlayer<?> player, IQuest quest) {
			super(player, quest);
		}
	}

	public static class QuestCompletedEvent extends QuestEvent {
		public QuestCompletedEvent(IPlayer<?> player, IQuest quest) {
			super(player, quest);
		}
	}

	@Cancelable
	public static class QuestStartEvent extends QuestEvent {
		public QuestStartEvent(IPlayer<?> player, IQuest quest) {
			super(player, quest);
		}
	}

	public static class QuestTurnedInEvent extends QuestEvent {
		
		public int expReward;
		public IItemStack[] itemRewards;
		public FactionOptions factionOptions;
		public PlayerMail mail;
		public int nextQuestId;
		public String command;

		public QuestTurnedInEvent(IPlayer<?> player, IQuest quest) {
			super(player, quest);
			this.itemRewards = new IItemStack[0];
			this.factionOptions = ((Quest) quest).factionOptions.copy();
			this.mail = ((Quest) quest).mail.copy();
			this.nextQuestId = ((Quest) quest).nextQuestid;
			this.command = ""+((Quest) quest).command;
		}
	}

	public IPlayer<?> player;

	public IQuest quest;

	public QuestEvent(IPlayer<?> player, IQuest quest) {
		this.quest = quest;
		this.player = player;
	}
}
