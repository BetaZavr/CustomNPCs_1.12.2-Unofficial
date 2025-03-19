package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.CustomNpcs;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
import noppes.npcs.api.event.NpcEvent;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.EntityNPCInterface;

public class NpcScriptData
extends BaseScriptData {
	
	@Override
	public ITextComponent noticeString(String type, Object event) {
		ITextComponent message = new TextComponentString("NPC's Scripts ");
		message.getStyle().setColor(TextFormatting.DARK_GRAY);
		if (type != null) {
			ITextComponent hook = new TextComponentString(" hook \"");
			hook.getStyle().setColor(TextFormatting.DARK_GRAY);
			ITextComponent hookType = new TextComponentString(type);
			hookType.getStyle().setColor(TextFormatting.GRAY);
			ITextComponent hookEnd = new TextComponentString("\"; ");
			hookEnd.getStyle().setColor(TextFormatting.DARK_GRAY);
			message = message.appendSibling(hook).appendSibling(hookType).appendSibling(hookEnd);
		}
		if (event instanceof NpcEvent && ((NpcEvent) event).npc != null) {
			EntityNPCInterface npc = (EntityNPCInterface) ((NpcEvent) event).npc.getMCEntity();

			ITextComponent mesNpc = new TextComponentString("NPC \"");
			mesNpc.getStyle().setColor(TextFormatting.DARK_GRAY);
			ITextComponent name = new TextComponentString(npc.getName());
			name.getStyle().setColor(TextFormatting.GRAY);
			ITextComponent mesUUID = new TextComponentString("\"; UUID: \"");
			mesUUID.getStyle().setColor(TextFormatting.DARK_GRAY);
			ITextComponent uuid = new TextComponentString(npc.getUniqueID().toString());
			uuid.getStyle().setColor(TextFormatting.GRAY);
			ITextComponent mesEnd = new TextComponentString("\" in ");
			mesEnd.getStyle().setColor(TextFormatting.DARK_GRAY);
			message = message.appendSibling(mesNpc).appendSibling(name).appendSibling(mesUUID).appendSibling(uuid).appendSibling(mesEnd);

			int dimID = npc.world == null ? 0 : npc.world.provider.getDimension();
			double x = Math.round(npc.posX * 100.0d) / 100.0d;
			double y = Math.round(npc.posY * 100.0d) / 100.0d;
			double z = Math.round(npc.posZ * 100.0d) / 100.0d;
			ITextComponent posClick = new TextComponentString("dimension ID:" + dimID + "; X:" + x + "; Y:" + y + "; Z:" + z);
			posClick.getStyle().setColor(TextFormatting.BLUE)
					.setUnderlined(true)
					.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/noppes world tp @p " + dimID + " " + x + " " + y + " "+z))
					.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("script.hover.error.pos.tp")));
			message = message.appendSibling(posClick);
		}
		ITextComponent side = new TextComponentString("; Side: " + (isClient() ? "Client" : "Server"));
		side.getStyle().setColor(TextFormatting.DARK_GRAY);
		return message.appendSibling(side);
	}
	
	@Override
	public void runScript(String type, Event event) {
		super.runScript(type, event);
		if (!this.isEnabled()) {
			return;
		}
		try {
			CustomNpcs.Server.addScheduledTask(() -> {
				if (ScriptController.Instance.lastLoaded > this.lastInited) {
					this.lastInited = ScriptController.Instance.lastLoaded;
					if (!type.equalsIgnoreCase(EnumScriptType.INIT.function)) {
						EventHooks.onNPCsInit(this);
					}
				}
                for (ScriptContainer script : this.scripts) {
                    script.run(type, event, !this.isClient());
                }
			});
		} catch (Exception e) { LogWriter.error("Error:", e); }
	}


	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		runScript(EnumScriptType.INIT.function, new NpcEvent.InitEvent(null));
	}

}
