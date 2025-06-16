package noppes.npcs.controllers.data;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
import noppes.npcs.NBTTags;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.event.ForgeEvent;
import noppes.npcs.api.event.potion.AffectEntity;
import noppes.npcs.api.event.potion.EndEffect;
import noppes.npcs.api.event.potion.PerformEffect;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.util.Util;

public class PotionScriptData
extends BaseScriptData {

	@Override
	public ITextComponent noticeString(String type, Object event) {
		ITextComponent message = new TextComponentString("");
		message.getStyle().setColor(TextFormatting.DARK_GRAY);
		if (type != null) {
			ITextComponent hook = new TextComponentString("Hook \"");
			hook.getStyle().setColor(TextFormatting.DARK_GRAY);
			ITextComponent hookType = new TextComponentString(type);
			hookType.getStyle().setColor(TextFormatting.GRAY);
			ITextComponent hookEnd = new TextComponentString("\"; ");
			hookEnd.getStyle().setColor(TextFormatting.DARK_GRAY);
			message = message.appendSibling(hook).appendSibling(hookType).appendSibling(hookEnd);
		}
		ITextComponent mes = new TextComponentString("Potion Scripts");
		mes.getStyle().setColor(TextFormatting.DARK_GRAY);
		message = message.appendSibling(mes);

		IEntity<?> iEntity = null;
		if (event instanceof AffectEntity && ((AffectEntity) event).entity != null) { iEntity = ((AffectEntity) event).entity; }
		else if (event instanceof EndEffect && ((EndEffect) event).entity != null) { iEntity = ((EndEffect) event).entity; }
		else if (event instanceof PerformEffect && ((PerformEffect) event).entity != null) { iEntity = ((PerformEffect) event).entity; }
		if (iEntity != null) {
			ITextComponent mesEntity;
			if (iEntity.getMCEntity() instanceof EntityPlayer) { mesEntity = new TextComponentString("Player \""); }
			else if (iEntity.getMCEntity() instanceof EntityNPCInterface) { mesEntity = new TextComponentString("NPC \""); }
			else { mesEntity = new TextComponentString("Entity \""); }
			mesEntity.getStyle().setColor(TextFormatting.DARK_GRAY);

			ITextComponent name = new TextComponentString(iEntity.getName());
			name.getStyle().setColor(TextFormatting.GRAY);
			ITextComponent mesUUID = new TextComponentString("\"; UUID: \"");
			mesUUID.getStyle().setColor(TextFormatting.DARK_GRAY);
			ITextComponent uuid = new TextComponentString(iEntity.getUUID());
			uuid.getStyle().setColor(TextFormatting.GRAY);
			ITextComponent mesIn = new TextComponentString("\" in ");
			mesIn.getStyle().setColor(TextFormatting.DARK_GRAY);

			double x = Math.round(iEntity.getPos().getX() * 100.0d) / 100.0d;
			double y = Math.round(iEntity.getPos().getY() * 100.0d) / 100.0d;
			double z = Math.round(iEntity.getPos().getZ() * 100.0d) / 100.0d;
			int dimID = iEntity.getWorld().getMCWorld() == null ? 0 : iEntity.getWorld().getMCWorld().provider.getDimension();

			ITextComponent posClick = new TextComponentString("dimension ID:" + dimID + "; X:" + x + "; Y:" + y + "; Z:" + z);
			posClick.getStyle().setColor(TextFormatting.BLUE)
					.setUnderlined(true)
					.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/noppes world tp @p " + dimID + " " + x + " " + y + " "+z))
					.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentTranslation("script.hover.error.pos.tp")));
			message = message.appendSibling(mesEntity).appendSibling(name).appendSibling(mesUUID).appendSibling(uuid).appendSibling(mesIn).appendSibling(posClick);
		}
		ITextComponent side = new TextComponentString("; Side: " + (isClient() ? "Client" : "Server"));
		side.getStyle().setColor(TextFormatting.DARK_GRAY);
		return message.appendSibling(side);
	}
	
	public void readFromNBT(NBTTagCompound compound) {
		this.scripts.clear();
		this.scripts = NBTTags.GetScript(compound.getTagList("Scripts", 10), this, false);
		this.scriptLanguage = Util.instance.deleteColor(compound.getString("ScriptLanguage"));
		this.enabled = compound.getBoolean("ScriptEnabled");
		if (this.scripts.isEmpty() || this.scripts.get(0).script.isEmpty()) {
			ScriptContainer script = new ScriptContainer(this, false);
			char chr = Character.toChars(0x000A)[0];
			script.script = "// IPotion.getCustomName() - String (custom potion name)" + chr
					+ "// IPotion.getNbt() - INbt (nbt data)" + chr + "function isReady(event) {" + chr
					+ "  /* event.potion - IPotion" + chr + "     event.duration - int (ticks)" + chr
					+ "     event.amplifier - int (potion power) */" + chr + "}" + chr
					+ "function performEffect(event) {" + chr + "  /* event.potion - IPotion" + chr
					+ "     event.entity - IEntity" + chr + "     event.amplifier - int (potion power) */" + chr + "}"
					+ chr + "function affectEntity(event) {" + chr + "  /* event.potion - IPotion" + chr
					+ "     event.entity - IEntity" + chr + "     event.source - IEntity" + chr
					+ "     event.indirectSource - IEntity" + chr + "     event.amplifier - int (potion power)" + chr
					+ "     event.health - double (health value) */" + chr + "}" + chr + "function endEffect(event) {"
					+ chr + "  /* event.potion - IPotion" + chr + "     event.entity - IEntity" + chr
					+ "     event.amplifier - int (potion power) */" + chr + "}";
			if (this.scripts.isEmpty()) {
				this.scripts.add(script);
			} else {
				this.scripts.remove(0);
				this.scripts.add(0, script);
			}
		}
		runScript(EnumScriptType.INIT.function, new ForgeEvent.InitEvent());
	}

	@Override
	public void runScript(String type, Event event) {
		super.runScript(type, event);
		if (!this.isEnabled()) {
			return;
		}
		try {
			if (ScriptController.Instance.lastLoaded > this.lastInited) {
				this.lastInited = ScriptController.Instance.lastLoaded;
				if (!type.equalsIgnoreCase(EnumScriptType.INIT.function)) {
					EventHooks.onPotionInit(this);
				}
			}
            for (ScriptContainer script : this.scripts) { script.run(type, event); }
		} catch (Exception e) { LogWriter.error("Error run script:", e); }
	}
	
}