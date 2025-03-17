package noppes.npcs.controllers.data;

import java.util.Iterator;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.eventhandler.Event;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
import noppes.npcs.NBTTags;
import noppes.npcs.api.entity.IEntity;
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
	public String noticeString(String type, Object event) {
		String notice = "Potion Script";
		if (type != null) { notice += " hook \""+type+"\""; }
		IEntity<?> entity = null;
		if (event instanceof AffectEntity && ((AffectEntity) event).entity != null) { entity = ((AffectEntity) event).entity; }
		else if (event instanceof EndEffect && ((EndEffect) event).entity != null) { entity = ((EndEffect) event).entity; }
		else if (event instanceof PerformEffect && ((PerformEffect) event).entity != null) { entity = ((PerformEffect) event).entity; }
		if (entity != null) {
			if (entity.getMCEntity() instanceof EntityPlayer) { notice += ". Player:"; }
			else if (entity.getMCEntity() instanceof EntityNPCInterface) { notice += ". NPC:"; }
			else  { notice += ". Entity:"; }
			notice += entity.getName() + "\"; UUID: \"" + entity.getUUID() + "\"" +
				" in dimension ID:" + (entity.getWorld().getMCWorld() == null ? 0 : entity.getWorld().getMCWorld().provider.getDimension()) +
					"; X:" + (Math.round(entity.getPos().getX() * 100.0d) / 100.0d) +
					"; Y:" + (Math.round(entity.getPos().getY() * 100.0d) / 100.0d) +
					"; Z:" + (Math.round(entity.getPos().getZ() * 100.0d) / 100.0d);
		}
		return notice + "; Side: " + (isClient() ? "Client" : "Server");
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
			Iterator<ScriptContainer> iterator = this.scripts.iterator();
			boolean bo = !this.isClient();
			while (iterator.hasNext()) {
				iterator.next().run(type, event, bo);
			}
		} catch (Exception e) { LogWriter.error("Error run script:", e); }
	}
	
}