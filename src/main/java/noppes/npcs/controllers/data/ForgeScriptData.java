package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.CustomNpcs;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
import noppes.npcs.api.event.ForgeEvent;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;

public class ForgeScriptData
extends BaseScriptData {

	@Override
	public ITextComponent noticeString(String type, Object event) {
		ITextComponent message = new TextComponentString("Forge Scripts ");
		message.getStyle().setColor(TextFormatting.DARK_GRAY);
		return message.appendSibling(super.noticeString(type, event));
	}
	
	@Override
	public void runScript(String type, Event event) {
		super.runScript(type, event);
		if (!isEnabled()) { return; }
		try {
			CustomNpcs.Server.addScheduledTask(() -> {
				if (ScriptController.Instance.lastLoaded > this.lastInited) {
					this.lastInited = ScriptController.Instance.lastLoaded;
					if (!type.equalsIgnoreCase(EnumScriptType.INIT.function)) {
						EventHooks.onForgeInit(this);
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
		runScript(EnumScriptType.INIT.function, new ForgeEvent.InitEvent());
	}

}
