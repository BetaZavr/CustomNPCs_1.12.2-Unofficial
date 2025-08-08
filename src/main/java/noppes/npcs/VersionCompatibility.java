package noppes.npcs;

import java.util.Collection;
import java.util.List;

import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.data.Line;
import noppes.npcs.controllers.data.Lines;
import noppes.npcs.entity.EntityNPCInterface;

public class VersionCompatibility {
	public static int ModRev = 18;

	public static void CheckAvailabilityCompatibility(ICompatibilty compatibility, NBTTagCompound compound) {
		if (compatibility.getVersion() == VersionCompatibility.ModRev) {
			return;
		}
		CompatabilityFix(compound, compatibility.save(new NBTTagCompound()));
		compatibility.setVersion(VersionCompatibility.ModRev);
	}

	public static void CheckNpcCompatibility(EntityNPCInterface npc, NBTTagCompound compound) {
		if (npc.npcVersion == VersionCompatibility.ModRev) {
			return;
		}
		if (npc.npcVersion < 12) {
			CompatabilityFix(compound, npc.advanced.save(new NBTTagCompound()));
			CompatabilityFix(compound, npc.ais.writeToNBT(new NBTTagCompound()));
			CompatabilityFix(compound, npc.stats.writeToNBT(new NBTTagCompound()));
			CompatabilityFix(compound, npc.display.writeToNBT(new NBTTagCompound()));
			CompatabilityFix(compound, npc.inventory.writeEntityToNBT(new NBTTagCompound()));
		}
		if (npc.npcVersion < 5) {
			String texture = compound.getString("Texture");
			texture = texture.replace("/mob/" + CustomNpcs.MODID + "/", CustomNpcs.MODID + ":textures/entity/");
			texture = texture.replace("/mob/", CustomNpcs.MODID + ":textures/entity/");
			compound.setString("Texture", texture);
		}
		if (npc.npcVersion < 6 && compound.getTag("NpcInteractLines") instanceof NBTTagList) {
			List<String> interactLines = NBTTags.getStringList(compound.getTagList("NpcInteractLines", 10));
			Lines lines = new Lines();
			for (int i = 0; i < interactLines.size(); ++i) {
				Line line = new Line();
				line.setText((String) interactLines.toArray()[i]);
				lines.lines.put(i, line);
			}
			compound.setTag("NpcInteractLines", lines.writeToNBT());
			List<String> worldLines = NBTTags.getStringList(compound.getTagList("NpcLines", 10));
			lines = new Lines();
			for (int j = 0; j < worldLines.size(); ++j) {
				Line line2 = new Line();
				line2.setText((String) worldLines.toArray()[j]);
				lines.lines.put(j, line2);
			}
			compound.setTag("NpcLines", lines.writeToNBT());
			List<String> attackLines = NBTTags.getStringList(compound.getTagList("NpcAttackLines", 10));
			lines = new Lines();
			for (int k = 0; k < attackLines.size(); ++k) {
				Line line3 = new Line();
				line3.setText((String) attackLines.toArray()[k]);
				lines.lines.put(k, line3);
			}
			compound.setTag("NpcAttackLines", lines.writeToNBT());
			List<String> killedLines = NBTTags.getStringList(compound.getTagList("NpcKilledLines", 10));
			lines = new Lines();
			for (int l = 0; l < killedLines.size(); ++l) {
				Line line4 = new Line();
				line4.setText((String) killedLines.toArray()[l]);
				lines.lines.put(l, line4);
			}
			compound.setTag("NpcKilledLines", lines.writeToNBT());
		}
		if (npc.npcVersion == 12) {
			NBTTagList list = compound.getTagList("StartPos", 3);
			if (list.tagCount() == 3) {
				int z = ((NBTTagInt) list.removeTag(2)).getInt();
				int y = ((NBTTagInt) list.removeTag(1)).getInt();
				int x = ((NBTTagInt) list.removeTag(0)).getInt();
				compound.setIntArray("StartPosNew", new int[] { x, y, z });
			}
		}
		if (npc.npcVersion == 13) {
			boolean bo = compound.getBoolean("HealthRegen");
			compound.setInteger("HealthRegen", (bo ? 1 : 0));
			NBTTagCompound comp = compound.getCompoundTag("TransformStats");
			bo = comp.getBoolean("HealthRegen");
			comp.setInteger("HealthRegen", (bo ? 1 : 0));
			compound.setTag("TransformStats", comp);
		}
		if (npc.npcVersion == 15) {
			NBTTagList list = compound.getTagList("ScriptsContainers", 10);
			if (list.tagCount() > 0) {
				ScriptContainer script = new ScriptContainer(npc.script, false);
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < list.tagCount(); ++i) {
					NBTTagCompound scriptOld = list.getCompoundTagAt(i);
					EnumScriptType type = EnumScriptType.values()[scriptOld.getInteger("Type")];
					sb.append(script.script).append("\nfunction ")
							.append(type.function)
							.append("(event) {\n")
							.append(scriptOld.getString("Script"))
							.append("\n}");
					for (String s : NBTTags.getStringList(compound.getTagList("ScriptList", 10))) {
						if (!script.scripts.contains(s)) {
							script.scripts.add(s);
						}
					}
				}
				script.script = sb.toString();
			}
			if (compound.getBoolean("CanDespawn")) {
				compound.setInteger("SpawnCycle", 4);
			}
			if (compound.getInteger("RangeAndMelee") <= 0) {
				compound.setInteger("DistanceToMelee", 0);
			}
		}
		if (npc.npcVersion == 16) {
			compound.setString("HitSound", "random.bowhit");
			compound.setString("GroundSound", "random.break");
		}
		if (npc.npcVersion == 17) {
			if (compound.getString("NpcHurtSound").equals("minecraft:game.player.hurt")) {
				compound.setString("NpcHurtSound", "minecraft:entity.player.hurt");
			}
			if (compound.getString("NpcDeathSound").equals("minecraft:game.player.hurt")) {
				compound.setString("NpcDeathSound", "minecraft:entity.player.hurt");
			}
			if (compound.getString("FiringSound").equals("random.bow")) {
				compound.setString("FiringSound", "minecraft:entity.arrow.shoot");
			}
			if (compound.getString("HitSound").equals("random.bowhit")) {
				compound.setString("HitSound", "minecraft:entity.arrow.hit");
			}
			if (compound.getString("GroundSound").equals("random.break")) {
				compound.setString("GroundSound", "minecraft:block.stone.break");
			}
		}
		npc.npcVersion = VersionCompatibility.ModRev;
	}

	private static void CompatabilityFix(NBTTagCompound compound, NBTTagCompound check) {
		Collection<String> tags = check.getKeySet();
		for (String name : tags) {
			NBTBase nbt = check.getTag(name);
			if (!compound.hasKey(name)) {
				compound.setTag(name, nbt);
			} else {
				if (!(nbt instanceof NBTTagCompound) || !(compound.getTag(name) instanceof NBTTagCompound)) {
					continue;
				}
				CompatabilityFix(compound.getCompoundTag(name), (NBTTagCompound) nbt);
			}
		}
	}

}
