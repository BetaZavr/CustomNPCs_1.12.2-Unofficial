package noppes.npcs.client.gui.mainmenu;

import net.minecraft.entity.EnumCreatureAttribute;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.client.Client;
import noppes.npcs.client.gui.SubGuiNpcMeleeProperties;
import noppes.npcs.client.gui.SubGuiNpcProjectiles;
import noppes.npcs.client.gui.SubGuiNpcRangeProperties;
import noppes.npcs.client.gui.SubGuiNpcResistanceProperties;
import noppes.npcs.client.gui.SubGuiNpcRespawn;
import noppes.npcs.client.gui.util.*;
import noppes.npcs.constants.EnumPacketServer;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataAI;
import noppes.npcs.entity.data.DataDisplay;
import noppes.npcs.entity.data.DataInventory;
import noppes.npcs.entity.data.DataStats;

import javax.annotation.Nonnull;

public class GuiNpcStats extends GuiNPCInterface2 implements ITextfieldListener, IGuiData {

	protected final DataAI ais;
	protected final DataDisplay display;
	protected final DataInventory inventory;
	protected final DataStats stats;

	public GuiNpcStats(EntityNPCInterface npc) {
		super(npc, 2);
		stats = npc.stats;
		display = npc.display;
		ais = npc.ais;
		inventory = npc.inventory;
		Client.sendData(EnumPacketServer.MainmenuStatsGet);
		Client.sendData(EnumPacketServer.MainmenuDisplayGet);
		Client.sendData(EnumPacketServer.MainmenuAIGet);
		Client.sendData(EnumPacketServer.MainmenuInvGet);
	}

	@Override
	public void buttonEvent(@Nonnull GuiNpcButton button, int mouseButton) {
		if (mouseButton != 0) { return; }
		switch (button.getID()) {
			case 0: setSubGui(new SubGuiNpcRespawn(stats)); break;
			case 2: setSubGui(new SubGuiNpcMeleeProperties(stats.melee)); break;
			case 3: setSubGui(new SubGuiNpcRangeProperties(stats)); break;
			case 4: stats.immuneToFire = (button.getValue() == 1); break;
			case 5: stats.canDrown = (button.getValue() == 1); break;
			case 6: stats.burnInSun = (button.getValue() == 1); break;
			case 7: stats.noFallDamage = (button.getValue() == 1); break;
			case 8: stats.creatureType = EnumCreatureAttribute.values()[button.getValue()]; break;
			case 9: setSubGui(new SubGuiNpcProjectiles(stats.ranged)); break;
			case 15: setSubGui(new SubGuiNpcResistanceProperties(stats.resistances)); break;
			case 17: stats.potionImmune = ((GuiNpcButtonYesNo) button).getBoolean(); break;
			case 22: stats.ignoreCobweb = (button.getValue() == 0); break;
			case 40: save(); break;
			case 41: stats.setLevel(1 + button.getValue()); setBaseStats(); break;
			case 43: stats.setRarity(button.getValue()); setBaseStats(); break;
			case 44: stats.calmdown = (button.getValue() == 1); break;
		}
	}

	public double getHP() {
		int[] corr = CustomNpcs.HealthNormal;
		if (stats.getRarity() == 1) { corr = CustomNpcs.HealthElite; }
		else if (stats.getRarity() == 2) { corr = CustomNpcs.HealthBoss; }
		double a = ((double) corr[0] - (double) corr[1]) / (1 - Math.pow(CustomNpcs.MaxLv, 2));
		double b = (double) corr[0] - a;
		double hp = Math.round(a * Math.pow(stats.getLevel(), 2) + b);
		if (hp <= 1.0d) { hp = 1.0d; }
		if (hp > 10000) { hp = Math.ceil(hp / 100.0d) * 100.0d; }
		else if (hp > 1000) { hp = Math.ceil(hp / 25.0d) * 25.0d; }
		else if (hp > 100) { hp = Math.ceil(hp / 10.0d) * 10.0d; }
		else if (hp > 50) { hp = Math.ceil(hp / 5.0d) * 5.0d; }
		else { hp = Math.ceil(hp); }
		if (hp > (double) corr[1]) { hp = corr[1]; }
		return hp;
	}

	public int getMellePower() {
		int[] corr = CustomNpcs.DamageNormal;
		if (stats.getRarity() == 1) { corr = CustomNpcs.DamageElite; }
		else if (stats.getRarity() == 2) { corr = CustomNpcs.DamageBoss; }
		double a = ((double) corr[0] - (double) corr[1]) / (1 - Math.pow(CustomNpcs.MaxLv, 2));
		double b = (double) corr[0] - a;
		return (int) Math.round(a * Math.pow(stats.getLevel(), 2) + b);
	}

	public int getRangePower() {
		int[] corr = CustomNpcs.DamageNormal;
		if (stats.getRarity() == 1) { corr = CustomNpcs.DamageElite; }
		else if (stats.getRarity() == 2) { corr = CustomNpcs.DamageBoss; }
		double a = ((double) corr[2] - (double) corr[3]) / (1 - Math.pow(CustomNpcs.MaxLv, 2));
		double b = (double) corr[2] - a;
		return (int) Math.round(a * Math.pow(stats.getLevel(), 2) + b);
	}

	public int[] getXP() {
		float[] corr = new float[] { (float) CustomNpcs.Experience[0], (float) CustomNpcs.Experience[1],
				(float) CustomNpcs.Experience[2], (float) CustomNpcs.Experience[3] };
		int lv = stats.getLevel();
		if (stats.getRarity() == 1) {
			corr[0] *= 1.75f;
			corr[1] *= 1.75f;
			corr[2] *= 1.75f;
			corr[3] *= 1.75f;
		}
		else if (stats.getRarity() == 2) {
			corr[0] *= 4.75f;
			corr[1] *= 4.75f;
			corr[2] *= 4.75f;
			corr[3] *= 4.75f;
		}
		int subMinLv = CustomNpcs.MaxLv / 3;
		int subMaxLv = CustomNpcs.MaxLv * 2 / 3;
		float subMinXP = corr[1] / 3.0f;
		float subMaxXP = corr[1] * 2.0f / 3.0f;
		float subMinXPM = corr[3] / 3.0f;
		float subMaxXPM = corr[3] * 2.0f / 3.0f;
		double a = ((subMaxXP - corr[1]) * (1 - subMinLv) - (corr[0] - subMinXP) * (subMaxLv - CustomNpcs.MaxLv))
				/ ((subMaxLv - CustomNpcs.MaxLv) * (Math.pow(subMinLv, 2) - 1)
						- (1 - subMinLv) * (Math.pow(CustomNpcs.MaxLv, 2) - Math.pow(subMaxLv, 2)));
		double b = (corr[0] - subMinXP + a * (Math.pow(subMinLv, 2) - 1)) / (1 - subMinLv);
		double c = corr[0] - a - b;
		int min = (int) (Math.pow(lv, 2) * a + lv * b + c);
		a = ((subMaxXPM - corr[3]) * (1 - subMinLv) - (corr[2] - subMinXPM) * (subMaxLv - CustomNpcs.MaxLv))
				/ ((subMaxLv - CustomNpcs.MaxLv) * (Math.pow(subMinLv, 2) - 1)
						- (1 - subMinLv) * (Math.pow(CustomNpcs.MaxLv, 2) - Math.pow(subMaxLv, 2)));
		b = (corr[2] - subMinXPM + a * (Math.pow(subMinLv, 2) - 1)) / (1 - subMinLv);
		c = corr[2] - a - b;
		int max = (int) (Math.pow(lv, 2) * a + lv * b + c);
		return new int[] { min, max };
	}

	@Override
	public void initGui() {
		int level = stats.getLevel() - 1;
		int rarity = stats.getRarity();
		if (getButton(41) != null) { level = getButton(41).getValue(); }
		if (getButton(43) != null) { rarity = getButton(43).getValue(); }
		super.initGui();
		int y = guiTop + 10;
		addLabel(new GuiNpcLabel(0, "stats.health", guiLeft + 5, y + 5));
		addTextField(new GuiNpcTextField(0, this, guiLeft + 85, y, 50, 18, "" + (int) stats.maxHealth)
				.setMinMaxDefault(0, Long.MAX_VALUE, 20)
				.setHoverText("stats.hover.max.health"));
		addLabel(new GuiNpcLabel(1, "stats.aggro", guiLeft + 275, y + 5));
		addTextField(new GuiNpcTextField(1, this, guiLeft + 355, y, 50, 18, stats.aggroRange + "")
				.setMinMaxDefault(1, 64, 2)
				.setHoverText("stats.hover.aggro"));
		addLabel(new GuiNpcLabel(34, "stats.creaturetype", guiLeft + 140, y + 5));
		addButton(new GuiButtonBiDirectional(8, guiLeft + 217, y, 56, 20, new String[] { "stats.normal", "stats.undead", "stats.arthropod" }, stats.creatureType.ordinal())
				.setCheckWidth(false)
				.setHoverText("stats.hover.type"));
		addButton(new GuiNpcButton(0, guiLeft + 82, y += 22, 56, 20, "selectServer.edit")
				.setHoverText("stats.hover.respawn"));
		addLabel(new GuiNpcLabel(2, "stats.respawn", guiLeft + 5, y + 5));
		ITextComponent mess = new TextComponentTranslation("stats.hover.melee");
		if (ais.aiDisabled) { mess.appendSibling(new TextComponentTranslation("hover.ai.disabled")); }
		addButton(new GuiNpcButton(2, guiLeft + 82, y += 22, 56, 20, "selectServer.edit")
				.setIsEnable(!ais.aiDisabled)
				.setHoverText(mess.getFormattedText()));
		addLabel(new GuiNpcLabel(5, "stats.meleeproperties", guiLeft + 5, y + 5));
		mess = new TextComponentTranslation("stats.hover.range");
		if (ais.aiDisabled) { mess.appendSibling(new TextComponentTranslation("hover.ai.disabled")); }
		addButton(new GuiNpcButton(3, guiLeft + 82, y += 22, 56, 20, "selectServer.edit")
				.setIsEnable(!ais.aiDisabled)
				.setHoverText(mess.getFormattedText()));
		addLabel(new GuiNpcLabel(6, "stats.rangedproperties", guiLeft + 5, y + 5));
		mess = new TextComponentTranslation("stats.hover.arrow");
		if (ais.aiDisabled) { mess.appendSibling(new TextComponentTranslation("hover.ai.disabled")); }
		addButton(new GuiNpcButton(9, guiLeft + 217, y, 56, 20, "selectServer.edit")
				.setIsEnable(!ais.aiDisabled)
				.setHoverText(mess.getFormattedText()));
		addLabel(new GuiNpcLabel(7, "stats.projectileproperties", guiLeft + 140, y + 5));
		addButton(new GuiNpcButton(15, guiLeft + 82, y += 34, 56, 20, "selectServer.edit")
				.setHoverText("stats.hover.resists"));
		addLabel(new GuiNpcLabel(15, "effect.resistance", guiLeft + 5, y + 5));
		addButton(new GuiNpcButton(4, guiLeft + 82, y += 34, 56, 20, new String[] { "gui.no", "gui.yes" }, (stats.immuneToFire ? 1 : 0))
				.setHoverText("stats.hover.resist.fire"));
		addLabel(new GuiNpcLabel(10, "stats.fireimmune", guiLeft + 5, y + 5));
		addButton(new GuiNpcButton(5, guiLeft + 217, y, 56, 20, new String[] { "gui.no", "gui.yes" }, (stats.canDrown ? 1 : 0))
				.setHoverText("stats.hover.water"));
		addLabel(new GuiNpcLabel(11, "stats.candrown", guiLeft + 140, y + 5));
		addTextField(new GuiNpcTextField(14, this, guiLeft + 355, y, 56, 20, stats.healthRegen + "")
				.setMinMaxDefault(0, Integer.MAX_VALUE, 1)
				.setHoverText("stats.hover.health.regen"));
		addLabel(new GuiNpcLabel(14, "stats.regenhealth", guiLeft + 275, y + 5));
		addTextField(new GuiNpcTextField(16, this, guiLeft + 355, y += 22, 56, 20, stats.combatRegen + "")
				.setMinMaxDefault(0, Integer.MAX_VALUE, 0)
				.setHoverText("stats.hover.health.combat"));
		addLabel(new GuiNpcLabel(16, "stats.combatregen", guiLeft + 275, y + 5));
		addButton(new GuiNpcButton(6, guiLeft + 82, y, 56, 20, new String[] { "gui.no", "gui.yes" }, (stats.burnInSun ? 1 : 0))
				.setHoverText("stats.hover.resist.sun"));
		addLabel(new GuiNpcLabel(12, "stats.burninsun", guiLeft + 5, y + 5));
		addButton(new GuiNpcButton(7, guiLeft + 217, y, 56, 20, new String[] { "gui.no", "gui.yes" }, (stats.noFallDamage ? 1 : 0))
				.setHoverText("stats.hover.fall"));
		addLabel(new GuiNpcLabel(13, "stats.nofalldamage", guiLeft + 140, y + 5));
		y += 22;
		addButton(new GuiNpcButtonYesNo(17, guiLeft + 82, y, 56, 20, stats.potionImmune)
				.setHoverText("stats.hover.potion"));
		addLabel(new GuiNpcLabel(17, "stats.potionImmune", guiLeft + 5, y + 5));
		addLabel(new GuiNpcLabel(22, "ai.cobwebAffected", guiLeft + 140, y + 5));
		addButton(new GuiNpcButton(22, guiLeft + 217, y, 56, 20, new String[] { "gui.no", "gui.yes" }, (stats.ignoreCobweb ? 0 : 1))
				.setHoverText("stats.hover.web"));
		String[] lvls = new String[CustomNpcs.MaxLv]; // level
		for (int g = 0; g < CustomNpcs.MaxLv; g++) { lvls[g] = "" + (g + 1); }
		addButton(new GuiButtonBiDirectional(41, guiLeft + 217, guiTop + 32, 56, 20, lvls, level)
				.setHoverText("stats.hover.level"));
		addLabel(new GuiNpcLabel(42, "stats.level", guiLeft + 139, guiTop + 37));
		addButton(new GuiNpcButton(43, guiLeft + 217, guiTop + 54, 56, 20,
				new String[] { "stats.rarity.normal", "stats.rarity.elite", "stats.rarity.boss" },
				rarity)
				.setHoverText("stats.hover.rarity")); // rarity
		addLabel(new GuiNpcLabel(44, "stats.rarity", guiLeft + 140, guiTop + 61));
		addLabel(new GuiNpcLabel(45, "stats.calmdown", guiLeft + 275, guiTop + 40));
		addButton(new GuiNpcButton(44, guiLeft + 355, guiTop + 37, 50, 20, new String[] { "gui.no", "gui.yes" }, (stats.calmdown ? 1 : 0))
				.setHoverText("stats.hover.battle"));
	}

	@Override
	public void save() {
		Client.sendData(EnumPacketServer.MainmenuStatsSave, stats.writeToNBT(new NBTTagCompound()));
		Client.sendData(EnumPacketServer.MainmenuDisplaySave, display.writeToNBT(new NBTTagCompound()));
		Client.sendData(EnumPacketServer.MainmenuAISave, ais.writeToNBT(new NBTTagCompound()));
		Client.sendData(EnumPacketServer.MainmenuInvSave, inventory.writeEntityToNBT(new NBTTagCompound()));
	}

	private void setBaseStats() {
		int lv = stats.getLevel();
		int type = stats.getRarity();
		// Resistance and model size
		if (!CustomNpcs.RecalculateLR) { return; }
		int[] sizeModel = CustomNpcs.ModelRaritySize;
		int time = 180;
		float[] resist = new float[] { (float) CustomNpcs.ResistanceNormal[0] / 100.0f,
				(float) CustomNpcs.ResistanceNormal[1] / 100.0f, (float) CustomNpcs.ResistanceNormal[2] / 100.0f,
				(float) CustomNpcs.ResistanceNormal[3] / 100.0f };
		if (type == 2) {
			resist = new float[] { (float) CustomNpcs.ResistanceBoss[0] / 100.0f,
					(float) CustomNpcs.ResistanceBoss[1] / 100.0f, (float) CustomNpcs.ResistanceBoss[2] / 100.0f,
					(float) CustomNpcs.ResistanceBoss[3] / 100.0f };
		} else if (type == 1) {
			resist = new float[] { (float) CustomNpcs.ResistanceElite[0] / 100.0f,
					(float) CustomNpcs.ResistanceElite[1] / 100.0f, (float) CustomNpcs.ResistanceElite[2] / 100.0f,
					(float) CustomNpcs.ResistanceElite[3] / 100.0f };
			if (lv <= 30) { time = (int) (300.0d + (Math.round(((double) lv + 5.5d) / 10.0d) - 1.0d) * 60.0d); }
			else { time = 480; }
		} else {
			if (lv <= 30) { time = (int) (90.0d + (Math.round(((double) lv + 5.5d) / 10.0d) - 1.0d) * 30.0d); }
		}
		display.setSize(sizeModel[type]);
		stats.respawnTime = time;
		stats.resistances.data.put("mob", resist[0]);
		stats.resistances.data.put("arrow", resist[1]);
		stats.resistances.data.put("explosion", resist[2]);
		stats.resistances.data.put("knockback", resist[3]);
		// Health
		double hp = getHP();
		stats.maxHealth = (int) hp;
		npc.setHealth((float) hp);
		stats.setHealthRegen((int) (Math.ceil(hp / 50.0d) * 10.0d));
		// Power
		stats.melee.setStrength(getMellePower());
		stats.ranged.setStrength(getRangePower());
		stats.ranged.setAccuracy((int) Math.round((lv + 94.3333d) / 1.4667d));
		// Speed
		double sp = Math.round((0.000081d * Math.pow(lv, 2) - 0.07272d * lv + 22.072639d));
		stats.melee.setDelay((int) sp);
		stats.ranged.setDelay((int) sp, (int) sp * 2);
		ais.setWalkingSpeed((int) Math.ceil((sp - 7) / 3));
		// Experience
		int[] xp = getXP();
		inventory.setExp(xp[0], xp[1]);
		// renaming
		String rarity = "";
		if (type == 2) { rarity += ((char) 167) + "4Boss "; }
		else if (type == 1) { rarity += ((char) 167) + "6Elite "; }
		rarity += ((char) 167) + "7lv." + ((char) 167) + (lv <= CustomNpcs.MaxLv / 3 ? "2" : (float) lv <= (float) CustomNpcs.MaxLv / 1.5f ? "6" : "4") + lv;
		stats.setRarityTitle(rarity);
		initGui();
	}

	@Override
	public void setGuiData(NBTTagCompound compound) {
		if (compound.hasKey("CreatureType", 3)) { stats.readToNBT(compound); }
		else if (compound.hasKey("MarkovGeneratorId", 3)) { display.readToNBT(compound); }
		else if (compound.hasKey("NpcInv", 9)) { inventory.readEntityFromNBT(compound); }
		else if (compound.hasKey("MovementType", 3)) { ais.readToNBT(compound); }
		initGui();
	}

	@Override
	public void unFocused(GuiNpcTextField textfield) {
		switch (textfield.getID()) {
			case 0: stats.maxHealth = textfield.getInteger(); npc.heal((float) stats.maxHealth); break;
			case 1: stats.aggroRange = textfield.getInteger(); break;
			case 14: stats.healthRegen = textfield.getInteger(); break;
			case 16: stats.combatRegen = textfield.getInteger(); break;
		}
	}

}
