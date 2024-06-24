package noppes.npcs.entity.data;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.DamageSource;

public class Resistances {

	public static final List<String> allDamageNames = Lists.newArrayList();
	public final Map<String, Float> data = Maps.<String, Float>newHashMap();

	public Resistances() {
		this.data.put("arrow", 1.0f);
		this.data.put("mob", 1.0f);
		this.data.put("knockback", 1.0f);
		this.data.put("explosion", 1.0f);
		allDamageNames.add("arrow");
		allDamageNames.add("mob");
		allDamageNames.add("knockback");
		allDamageNames.add("explosion");
		for (Field f : DamageSource.class.getDeclaredFields()) {
			if (f.getType() != DamageSource.class) { continue; }
			try {
				if (!f.isAccessible()) { f.setAccessible(true); }
				String name = ((DamageSource) f.get(DamageSource.class)).damageType;
				if (name.equals("generic") || name.equals("outOfWorld")) { continue; }
				allDamageNames.add(name);
			}
			catch (Exception e) { }
		}
	}

	public float applyResistance(DamageSource source, float damage) {
		if (source.damageType.equals("arrow") || source.damageType.equals("thrown") || source.isProjectile()) {
			damage *= 2.0f - this.data.get("arrow");
		} else if (source.damageType.equals("player") || source.damageType.equals("mob")) {
			damage *= 2.0f - this.data.get("mob");
		} else if (source.damageType.equals("explosion") || source.damageType.equals("explosion.player")) {
			damage *= 2.0f - this.data.get("explosion");
		} else if (this.data.containsKey(source.damageType)) {
			damage *= 2.0f - this.data.get(source.damageType);
		}
		return damage;
	}

	public void readToNBT(NBTTagList list) {
		this.data.clear();
		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound nbt = list.getCompoundTagAt(i);
			String key = nbt.getString("K");
			this.data.put(key, nbt.getFloat("V"));
			if (!allDamageNames.contains(key)) { allDamageNames.add(key); }
		}
	}
	
	public void oldReadToNBT(NBTTagCompound compound) {
		this.data.put("arrow", compound.getFloat("Arrow"));
		this.data.put("mob", compound.getFloat("Melee"));
		this.data.put("knockback", compound.getFloat("Knockback"));
		this.data.put("explosion", compound.getFloat("Explosion"));
	}

	public NBTTagList writeToNBT() {
		NBTTagList list = new NBTTagList();
		for (String key : this.data.keySet()) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setString("K", key);
			nbt.setFloat("V", this.data.get(key));
			list.appendTag(nbt);
		}
		return list;
	}

	public float get(String damageName) {
		if (this.data.containsKey(damageName)) { return this.data.get(damageName); }
		if (damageName.equals("explosion.player") && this.data.containsKey("explosion")) { return this.data.get("explosion"); }
		if (damageName.equals("player") && this.data.containsKey("mob")) { return this.data.get("mob"); }
		if (damageName.equals("thrown") && this.data.containsKey("arrow")) { return this.data.get("arrow"); }
		return 1.0f;
	}

	public static void addDamageName(String damageType) {
		if (damageType == null || damageType.isEmpty() || allDamageNames.contains(damageType) ||
				damageType.equals("thrown") || damageType.equals("player") || damageType.equals("explosion.player") ||
				damageType.equals("generic") || damageType.equals("outOfWorld")) { return; }
		allDamageNames.add(damageType);
		Collections.sort(allDamageNames);
	}
	
}
