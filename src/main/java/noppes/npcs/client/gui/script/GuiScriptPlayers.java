package noppes.npcs.client.gui.script;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.event.CustomContainerEvent;
import noppes.npcs.api.event.CustomGuiEvent;
import noppes.npcs.api.event.DialogEvent;
import noppes.npcs.api.event.PlayerEvent;
import noppes.npcs.api.event.QuestEvent;
import noppes.npcs.api.event.WorldEvent;
import noppes.npcs.client.Client;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.controllers.data.PlayerScriptData;

public class GuiScriptPlayers extends GuiScriptInterface {
	private PlayerScriptData script;

	public GuiScriptPlayers() {
		this.script = new PlayerScriptData(null);
		this.handler = this.script;
		Client.sendData(EnumPacketServer.ScriptPlayerGet, new Object[0]);
		// New
		this.baseFuncNames.put("init", PlayerEvent.InitEvent.class);
		this.baseFuncNames.put("tick", PlayerEvent.UpdateEvent.class);
		this.baseFuncNames.put("interact", PlayerEvent.InteractEvent.class);
		this.baseFuncNames.put("attack", PlayerEvent.AttackEvent.class);
		this.baseFuncNames.put("broken", PlayerEvent.BreakEvent.class);
		this.baseFuncNames.put("toss", PlayerEvent.TossEvent.class);
		this.baseFuncNames.put("pickUp", PlayerEvent.PickUpEvent.class);
		this.baseFuncNames.put("containerOpen", PlayerEvent.ContainerOpen.class);
		this.baseFuncNames.put("containerClosed", PlayerEvent.ContainerClosed.class);
		this.baseFuncNames.put("damagedEntity", PlayerEvent.DamagedEntityEvent.class);
		this.baseFuncNames.put("rangedLaunched", PlayerEvent.RangedLaunchedEvent.class);
		this.baseFuncNames.put("died", PlayerEvent.DiedEvent.class);
		this.baseFuncNames.put("kill", PlayerEvent.KilledEntityEvent.class);
		this.baseFuncNames.put("damaged", PlayerEvent.DamagedEvent.class);
		this.baseFuncNames.put("timer", PlayerEvent.TimerEvent.class);
		this.baseFuncNames.put("login", PlayerEvent.LoginEvent.class);
		this.baseFuncNames.put("logout", PlayerEvent.LogoutEvent.class);
		this.baseFuncNames.put("levelUp", PlayerEvent.LevelUpEvent.class);
		this.baseFuncNames.put("keyPressed", PlayerEvent.KeyPressedEvent.class);
		this.baseFuncNames.put("keyDown", PlayerEvent.KeyPressedEvent.class);
		this.baseFuncNames.put("mousePressed", PlayerEvent.KeyPressedEvent.class);
		this.baseFuncNames.put("mouseDown", PlayerEvent.KeyPressedEvent.class);
		this.baseFuncNames.put("chat", PlayerEvent.ChatEvent.class);
		this.baseFuncNames.put("factionUpdate", PlayerEvent.FactionUpdateEvent.class);
		this.baseFuncNames.put("itemFished", PlayerEvent.ItemFished.class);
		this.baseFuncNames.put("itemCrafted", PlayerEvent.ItemCrafted.class);
		this.baseFuncNames.put("itemFished", PlayerEvent.ItemFished.class);
		// BlockEvent
		this.baseFuncNames.put("plased", PlayerEvent.PlaceEvent.class);
		// DialogEvent
		this.baseFuncNames.put("dialog", DialogEvent.OpenEvent.class);
		this.baseFuncNames.put("dialogClose", DialogEvent.CloseEvent.class);
		this.baseFuncNames.put("dialogOption", DialogEvent.OptionEvent.class);
		// CustomContainerEvent
		this.baseFuncNames.put("customChestClosed", CustomContainerEvent.CloseEvent.class);
		this.baseFuncNames.put("customChestClicked", CustomContainerEvent.SlotClickedEvent.class);
		// QuestEvent
		this.baseFuncNames.put("questStart", QuestEvent.QuestStartEvent.class);
		this.baseFuncNames.put("questCompleted", QuestEvent.QuestCompletedEvent.class);
		this.baseFuncNames.put("questTurnIn", QuestEvent.QuestTurnedInEvent.class);
		this.baseFuncNames.put("questCanceled", QuestEvent.QuestCanceledEvent.class);
		// CustomGuiEvent
		this.baseFuncNames.put("customGuiClosed", CustomGuiEvent.CloseEvent.class);
		this.baseFuncNames.put("customGuiButton", CustomGuiEvent.ButtonEvent.class);
		this.baseFuncNames.put("customGuiSlot", CustomGuiEvent.SlotEvent.class);
		this.baseFuncNames.put("customGuiScroll", CustomGuiEvent.ScrollEvent.class);
		this.baseFuncNames.put("customGuiSlotClicked", CustomGuiEvent.SlotClickEvent.class);
		// WorldEvent
		this.baseFuncNames.put("scriptCommand", WorldEvent.ScriptCommandEvent.class);
		// CommonEvents
		this.baseFuncNames.put("trigger", WorldEvent.ScriptTriggerEvent.class);
	}

	@Override
	public void save() {
		super.save();
		Client.sendData(EnumPacketServer.ScriptPlayerSave, this.script.writeToNBT(new NBTTagCompound()));
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		this.script.readFromNBT(compound);
		super.setGuiData(compound);
	}
}
